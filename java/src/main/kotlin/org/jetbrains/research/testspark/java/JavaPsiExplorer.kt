package org.jetbrains.research.testspark.java

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
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

class JavaPsiExplorer(
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
        val javaPsiMethod = psiMethod as? JavaPsiMethodWrapper
        var isUnderTest = true
        if (javaPsiMethod != null) {
            exploreMethod(javaPsiMethod, isUnderTest = true, depth = depth)?.let { methodFqName ->
                if (javaPsiMethod.containingClass is JavaPsiClassWrapper) {
                    exploreClass(javaPsiMethod.containingClass, isUnderTest = false, depth = depth)?.let { classFqName ->
                        graph.addEdge(
                            GraphEdge(
                                from = classFqName,
                                to = methodFqName,
                                type = GraphEdgeType.HAS_METHOD,
                            ),
                        )
                    }
                }
            }
            isUnderTest = false
        }
        classesToTest.forEach {
            exploreClass(it as JavaPsiClassWrapper, isUnderTest = isUnderTest, depth)
        }
        interestingClasses.forEach { exploreClass(it as JavaPsiClassWrapper, isUnderTest = false, depth) }
        return graph
    }

    private fun exploreClass(
        javaCls: JavaPsiClassWrapper,
        isUnderTest: Boolean = false,
        depth: Int = 0,
    ): String? {
        val clsFqName = javaCls.qualifiedName
        val isStdLib = clsFqName.startsWith("java.") || clsFqName.startsWith("javax.")
        if (clsFqName.isEmpty() || isStdLib) return null
        if (classVisited.contains(javaCls.qualifiedName)) return clsFqName
        classVisited.add(clsFqName)
        graph.addNode(
            GraphNode(
                javaCls.name,
                clsFqName,
                type = GraphNodeType.CLASS,
                isUnitUnderTest = isUnderTest,
                isStandardLibrary = isStdLib,
                properties = mapOf("type" to javaCls.classType.representation),
            ),
        )

        javaCls.methods.forEach {
            exploreMethod(it as JavaPsiMethodWrapper, depth = depth)?.let { methodFqName ->
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
        val superClass = javaCls.superClass
        if (superClass is JavaPsiClassWrapper) {
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
        if (javaCls.classType == ClassType.ABSTRACT_CLASS || javaCls.classType == ClassType.INTERFACE) {
            javaCls.searchSubclasses(project).forEach {
                exploreClass(it as JavaPsiClassWrapper, depth = depth - 1)?.let { subClassFqName ->
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
        javaMethod: JavaPsiMethodWrapper,
        isUnderTest: Boolean = false,
        depth: Int = 0,
    ): String? {
        val methodFqName =
            (javaMethod.containingClass?.qualifiedName ?: javaMethod.containingFile.name) + "#" +
                javaMethod.name
        if (methodFqName.startsWith("java.")) return null
        if (methodVisited.contains(methodFqName)) return methodFqName
        methodVisited.add(methodFqName)
        graph.addNode(
            GraphNode(
                javaMethod.name,
                methodFqName,
                type = GraphNodeType.METHOD,
                isUnitUnderTest = isUnderTest,
            ),
        )
        // TODO: rework: Need access to psi instance
        val psiClass = PsiTreeUtil.getChildOfType<PsiClass>(javaMethod.containingFile, PsiClass::class.java)
        val psiMethod = psiClass?.methods?.firstOrNull { it.name == javaMethod.name } ?: return methodFqName
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
                    exploreClass(JavaPsiClassWrapper(it), depth = depth - 1)?.let { classFqName ->
                        result.add(classFqName)
                        psiType.parameters.forEach { param ->
                            result.addAll(exploreType(param, depth - 1))
                        }
                    }
                }
            }
        }
        return result
    }
}
