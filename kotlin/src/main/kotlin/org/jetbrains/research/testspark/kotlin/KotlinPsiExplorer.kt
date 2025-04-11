package org.jetbrains.research.testspark.kotlin

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiStatement
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeParameter
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.research.testspark.core.data.ClassType
import org.jetbrains.research.testspark.core.generation.llm.ranker.Graph
import org.jetbrains.research.testspark.core.generation.llm.ranker.GraphEdge
import org.jetbrains.research.testspark.core.generation.llm.ranker.GraphEdgeType
import org.jetbrains.research.testspark.core.generation.llm.ranker.GraphNode
import org.jetbrains.research.testspark.core.generation.llm.ranker.GraphNodeType
import org.jetbrains.research.testspark.langwrappers.PsiClassWrapper
import org.jetbrains.research.testspark.langwrappers.PsiMethodWrapper

const val MAX_DEPTH = 5

class KotlinPsiExplorer(
    val graph: Graph,
    private val project: Project,
) {
    val classVisited = mutableSetOf<String>()
    val methodVisited = mutableSetOf<String>()

    fun explore(
        classesToTest: List<PsiClassWrapper>,
        interestingClasses: Set<PsiClassWrapper>,
        psiMethod: PsiMethodWrapper?,
        depth: Int = MAX_DEPTH,
    ): Graph {
        val kotlinPsiMethod = psiMethod as? KotlinPsiMethodWrapper
        var isUnderTest = true
        if (kotlinPsiMethod != null) {
            exploreMethod(kotlinPsiMethod, isUnderTest = true, depth = depth)?.let { methodFqName ->
                if (kotlinPsiMethod.containingClass is KotlinPsiClassWrapper) {
                    exploreClass(
                        kotlinPsiMethod.containingClass,
                        isUnderTest = false,
                        depth = depth,
                    )?.let { classFqName ->
                        graph.addEdge(
                            GraphEdge(
                                from = classFqName,
                                to = methodFqName,
                                type = GraphEdgeType.HAS_METHOD,
                            ),
                        )
                        graph.addEdge(
                            GraphEdge(
                                from = methodFqName,
                                to = classFqName,
                                type = GraphEdgeType.FROM,
                            ),
                        )
                    }
                }
            }
            isUnderTest = false
        }
        classesToTest.forEach {
            exploreClass(it as KotlinPsiClassWrapper, isUnderTest = isUnderTest, depth)
            // only the first classesToTest is the Class under test.
            isUnderTest = false
        }
        interestingClasses.forEach { exploreClass(it as KotlinPsiClassWrapper, isUnderTest = false, depth) }
        return graph
    }

    private fun exploreClass(
        kotlinCls: KotlinPsiClassWrapper,
        isUnderTest: Boolean = false,
        depth: Int = 0,
    ): String? {
        val clsFqName = kotlinCls.qualifiedName
        val isStdLib = clsFqName.startsWith("kotlin.") || clsFqName.startsWith("java.") || clsFqName.startsWith("javax.")
        if (clsFqName.isEmpty() || isStdLib) return null
        if (classVisited.contains(kotlinCls.qualifiedName)) return clsFqName
        classVisited.add(clsFqName)
        graph.addNode(
            GraphNode(
                kotlinCls.name,
                clsFqName,
                type = GraphNodeType.CLASS,
                isUnitUnderTest = isUnderTest,
                isStandardLibrary = isStdLib,
                properties = mapOf("type" to kotlinCls.classType.representation),
            ),
        )

        kotlinCls.allMethods.forEach {
            exploreMethod(it as KotlinPsiMethodWrapper, depth = depth)?.let { methodFqName ->
                graph.addEdge(
                    GraphEdge(
                        from = clsFqName,
                        to = methodFqName,
                        type = GraphEdgeType.HAS_METHOD,
                    ),
                )
            }
        }

        if (depth <= 0) {
            return clsFqName
        }
        // super class
        val superClass = kotlinCls.superClass
        if (superClass is KotlinPsiClassWrapper) {
            exploreClass(superClass, depth = depth - 1)?.let { superClassFqName ->
                graph.addEdge(
                    GraphEdge(
                        from = clsFqName,
                        to = superClassFqName,
                        type = GraphEdgeType.INHERITANCE,
                    ),
                )
            }
        }
        // subclass
        if (kotlinCls.classType == ClassType.ABSTRACT_CLASS || kotlinCls.classType == ClassType.INTERFACE) {
            kotlinCls.searchSubclasses(project).forEach {
                exploreClass(it as KotlinPsiClassWrapper, depth = depth - 1)?.let { subClassFqName ->
                    graph.addEdge(
                        GraphEdge(
                            from = subClassFqName,
                            to = clsFqName,
                            type = GraphEdgeType.INHERITANCE,
                        ),
                    )
                }
            }
        }
        return clsFqName
    }

    private fun exploreMethod(
        kotlinMethod: KotlinPsiMethodWrapper,
        isUnderTest: Boolean = false,
        depth: Int = 0,
    ): String? {
        val methodFqName = kotlinMethod.fqName
        if (methodFqName.startsWith("java.") || methodFqName.startsWith("javax.")) return null
        if (methodVisited.contains(methodFqName)) return methodFqName
        methodVisited.add(methodFqName)
        val properties = mutableMapOf<String, Any>()
        properties["isConstructor"] = kotlinMethod.isPrimaryConstructor || kotlinMethod.isSecondaryConstructor
        graph.addNode(
            GraphNode(
                kotlinMethod.name,
                methodFqName,
                type = GraphNodeType.METHOD,
                isUnitUnderTest = isUnderTest,
                properties = properties,
            ),
        )

        // explore the class if we come from calls
        if (kotlinMethod.containingClass is KotlinPsiClassWrapper) {
            exploreClass(kotlinMethod.containingClass, depth = depth - 1)?.let { classFqName ->
                if (kotlinMethod.isPrimaryConstructor || kotlinMethod.isSecondaryConstructor) {
                    graph.addEdge(
                        GraphEdge(
                            from = methodFqName,
                            to = classFqName,
                            type = GraphEdgeType.HAS_RETURN_TYPE,
                        ),
                    )
                }
            }
        }

        // TODO: rework: Need access to psi instance
        val psiClass = PsiTreeUtil.getChildOfType<PsiClass>(kotlinMethod.containingFile, PsiClass::class.java)
        val psiMethod = psiClass?.methods?.firstOrNull { it.name == kotlinMethod.name } ?: return methodFqName
        // return type
        val psiReturnType = psiMethod.returnType
        if (psiReturnType != null) {
            exploreType(psiReturnType, depth = depth - 1).forEach { classFqName ->
                graph.addEdge(
                    GraphEdge(
                        from = methodFqName,
                        to = classFqName,
                        type = GraphEdgeType.HAS_RETURN_TYPE,
                    ),
                )
            }
        }
        // parameters
        psiMethod.parameterList.parameters.forEach { parameter ->
            val psiType = parameter.type
            exploreType(psiType, depth - 1).forEach { paramFqName ->
                graph.addEdge(
                    GraphEdge(
                        from = methodFqName,
                        to = paramFqName,
                        type = GraphEdgeType.HAS_TYPE_PARAMETER,
                    ),
                )
            }
        }
        // exceptions
        psiMethod.throwsList.referencedTypes.forEach { throwType ->
            exploreType(throwType, depth - 1).forEach { throwFqName ->
                graph.addEdge(
                    GraphEdge(
                        from = methodFqName,
                        to = throwFqName,
                        type = GraphEdgeType.THROWS,
                    ),
                )
            }
        }
        // body
        psiMethod.body?.statements?.forEach { statement ->
            exploreStatement(statement, depth = depth - 1).forEach { methodCalledFqName ->
                graph.addEdge(
                    GraphEdge(
                        from = methodFqName,
                        to = methodCalledFqName,
                        type = GraphEdgeType.CALLS,
                    ),
                )
            }
        }
        return methodFqName
    }

    private fun exploreType(
        psiType: PsiType,
        depth: Int,
    ): List<String> {
        val result = mutableListOf<String>()
        if (psiType is PsiClassReferenceType) {
            psiType.resolve()?.let {
                if (it !is PsiTypeParameter) {
                    KotlinPsiClassWrapper.fromPsiClass(it)?.let { classWrapper ->
                        exploreClass(classWrapper, depth = depth - 1)?.let { classFqName ->
                            result.add(classFqName)
                            psiType.parameters.forEach { param ->
                                result.addAll(exploreType(param, depth - 1))
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    private fun exploreStatement(
        psiStatement: PsiStatement,
        depth: Int = 0,
    ): List<String> {
        val result = mutableListOf<String>()
        PsiTreeUtil
            .findChildrenOfType<PsiMethodCallExpression>(psiStatement, PsiMethodCallExpression::class.java)
            .forEach { itMethodCall ->
                itMethodCall.resolveMethod()?.let {
                    KotlinPsiMethodWrapper.fromPsiMethod(it)?.let { psiMethod ->
                        exploreMethod(psiMethod, depth = depth - 1)?.let { methodFqName ->
                            result.add(methodFqName)
                        }
                    }
                }
            }
        return result
    }
}
