package org.jetbrains.research.testspark.java

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiStatement
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeParameter
import com.intellij.psi.PsiVariable
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
    private val log = Logger.getInstance(this::class.java)

    val classVisited = mutableMapOf<String, Int>()
    val methodVisited = mutableMapOf<String, Int>()

    fun explore(
        classesToTest: List<PsiClassWrapper>,
        interestingClasses: Set<PsiClassWrapper>,
        psiMethod: PsiMethodWrapper?,
        depth: Int = MAX_DEPTH,
    ): Graph {
        val javaPsiMethod = psiMethod as? JavaPsiMethodWrapper
        if (javaPsiMethod != null) {
            log.info("Explore method: ${psiMethod.fqName}")
            exploreMethod(javaPsiMethod, isUnderTest = true, depth = depth)
        } else {
            classesToTest.firstOrNull()?.let {
                log.info("Explore class: ${it.qualifiedName}")
                exploreClass(it as JavaPsiClassWrapper, isUnderTest = true, depth)
            }
        }
        log.info("Number of Class visited: ${classVisited.size}")
        log.info("Number of Method visited: ${methodVisited.size}")
        return graph
    }

    private fun exploreClass(
        javaCls: JavaPsiClassWrapper,
        isUnderTest: Boolean = false,
        depth: Int = 0,
    ): String? {
        val clsFqName = javaCls.qualifiedName
        if (clsFqName.isEmpty() || isStdLib(clsFqName)) return null

        if (depth <= 0) {
            return clsFqName
        }

        val oldDepth = classVisited.put(clsFqName, depth)

        if (oldDepth == null) {
            graph.addNode(
                GraphNode(
                    javaCls.name,
                    clsFqName,
                    type = GraphNodeType.CLASS,
                    isUnitUnderTest = isUnderTest,
                    properties = mapOf("type" to javaCls.classType.representation),
                ),
            )
        } else if (oldDepth >= depth) {
            classVisited.put(clsFqName, oldDepth)
            return clsFqName
        }

        javaCls.methods.forEach {
            exploreMethod(it as JavaPsiMethodWrapper, depth = depth)?.let { methodFqName ->
                graph.addEdge(
                    GraphEdge(
                        from = clsFqName,
                        to = methodFqName,
                        type = if (it.isConstructor || it.isDefaultConstructor) GraphEdgeType.HAS_CONSTRUCTOR else GraphEdgeType.HAS_METHOD,
                    ),
                )
            }
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
        val methodFqName = javaMethod.fqName
        if (depth <= 0 || isStdLib(methodFqName)) return null

        val oldDepth = methodVisited.put(methodFqName, depth)

        if (oldDepth == null) {
            val properties = mutableMapOf<String, Any>()
            properties["isConstructor"] = javaMethod.isConstructor
            graph.addNode(
                GraphNode(
                    javaMethod.name,
                    methodFqName,
                    type = GraphNodeType.METHOD,
                    isUnitUnderTest = isUnderTest,
                    properties = properties,
                ),
            )
        } else if (oldDepth >= depth) {
            methodVisited.put(methodFqName, oldDepth)
            return methodFqName
        }

        val usedClasses = mutableListOf<String>()

        // explore the class if we come from calls
        if (javaMethod.containingClass is JavaPsiClassWrapper) {
            exploreClass(javaMethod.containingClass, depth = depth)?.let {
                usedClasses.add(it)
            }
        }

        // TODO: rework: Need access to psi instance
        val psiClass = PsiTreeUtil.getChildOfType(javaMethod.containingFile, PsiClass::class.java)
        val psiMethod = psiClass?.methods?.firstOrNull { it.name == javaMethod.name } ?: return methodFqName
        // return type
        val psiReturnType = psiMethod.returnType
        if (psiReturnType != null) {
            exploreType(psiReturnType, depth = depth - 1).forEach { classFqName ->
                usedClasses.add(classFqName)
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
                usedClasses.add(paramFqName)
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
                usedClasses.add(throwFqName)
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
            exploreStatement(statement, depth = depth - 1).forEach { relations ->
                val fqName = relations.first
                val edge = relations.second
                if (fqName == methodFqName) return@forEach
                if (usedClasses.contains(fqName)) return@forEach
                graph.addEdge(
                    GraphEdge(
                        from = methodFqName,
                        to = fqName,
                        type = edge,
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
        if (depth < 0) {
            return emptyList()
        }
        val result = mutableListOf<String>()
        if (psiType is PsiClassReferenceType) {
            if (!isStdLib(psiType.canonicalText)) {
                psiType.resolve()?.let {
                    if (it !is PsiTypeParameter) {
                        exploreClass(JavaPsiClassWrapper(it), depth = depth)?.let { classFqName ->
                            result.add(classFqName)
                        }
                    } else {
                        psiType.superTypes.forEach { superType ->
                            result.addAll(exploreType(superType, depth))
                        }
                    }
                }
            }
            psiType.parameters.forEach { param ->
                result.addAll(exploreType(param, depth - 1))
            }
        }
        return result
    }

    private fun exploreStatement(
        psiStatement: PsiStatement,
        depth: Int = 0,
    ): List<Pair<String, GraphEdgeType>> {
        val result = mutableListOf<Pair<String, GraphEdgeType>>()
        PsiTreeUtil
            .findChildrenOfType(psiStatement, PsiMethodCallExpression::class.java)
            .forEach { itMethodCall ->
                itMethodCall.resolveMethod()?.let {
                    val psiMethod = JavaPsiMethodWrapper(it)
                    exploreMethod(psiMethod, depth = depth)?.let { methodFqName ->
                        result.add(Pair(methodFqName, GraphEdgeType.CALLS))
                    }
                }
            }
        PsiTreeUtil.findChildrenOfType(psiStatement, PsiReferenceExpression::class.java).forEach { itReference ->
            itReference.references.forEach {
                val ty = it.resolve()
                if (ty is PsiVariable) {
                    exploreType(ty.type, depth = depth).forEach { classFqName ->
                        result.add(Pair(classFqName, GraphEdgeType.USES))
                    }
                }
            }
        }
        return result
    }

    private fun isStdLib(qualifiedName: String): Boolean = qualifiedName.startsWith("java.") || qualifiedName.startsWith("javax.")
}
