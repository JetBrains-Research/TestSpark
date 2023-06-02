package org.jetbrains.research.testgenie.tools.evosuite.validation

import com.github.javaparser.JavaParser
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.expr.SimpleName
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.visitor.ModifierVisitor
import com.github.javaparser.ast.visitor.Visitable
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.intellij.openapi.diagnostic.Logger

/**
 * This class edits the test suite by setting the modified body of each test if it has been modified. It also removes scaffolding.
 */
class TestCaseEditor(
    private val text: String,
    private val activeTestList: HashMap<String, String>
) {
    private val log: Logger = Logger.getInstance(this.javaClass)

    class TestCaseReplacer(
        private val activeTestList: HashMap<String, Pair<BlockStmt, String>>
    ) : ModifierVisitor<Void>() {
        private val log: Logger = Logger.getInstance(this.javaClass)

        private val activeTestNames =
            activeTestList.entries.associate {
                it.value.second to it.key
            }

        override fun visit(n: ClassOrInterfaceDeclaration?, arg: Void?): Visitable {
            n ?: return super.visit(n, arg)

            val methods = n.methods.map { it.nameAsString }.toSet()

            for (entry in activeTestList) {
                if (!methods.contains(entry.value.second)) {
                    log.info("Test ${entry.key} in cache but not in file, inserting")
                    val methodDeclaration = n.addMethod(entry.value.second, Modifier.Keyword.PUBLIC)
                    methodDeclaration.setBody(entry.value.first)
                    val testAnnotation = NormalAnnotationExpr()
                    testAnnotation.setName("Test")
                    testAnnotation.addPair("timeout", "4000")
                    methodDeclaration.addAnnotation(testAnnotation)
                }
            }

            return super.visit(n, arg)
        }

        override fun visit(n: MethodDeclaration?, arg: Void?): Visitable {
            val name = n?.name!!
            val testName = name.toString()

            if (activeTestNames.contains(testName)) {
                val modifiedBody = activeTestList[activeTestNames[testName]]
                if (modifiedBody != null) {
                    log.info("Test case modified $testName")
                    n.setBody(modifiedBody.first)
                } else {
                    log.info("Test case not modified $testName")
                }
            } else {
                log.info("Test case not selected by user $testName")
                n.remove()
            }

            return super.visit(n, arg)
        }
    }

    class BodyExtractor : VoidVisitorAdapter<ArrayList<Pair<BlockStmt, String>>>() {
        override fun visit(n: MethodDeclaration?, arg: ArrayList<Pair<BlockStmt, String>>) {
            super.visit(n, arg)

            val body = n?.body?.get()
            if (body != null) {
                arg.add(Pair(body, n.nameAsString))
            }
        }
    }

    class ScaffoldRemover : ModifierVisitor<Void>() {

        // removes @EvoRunnerParameters(..)
        override fun visit(n: NormalAnnotationExpr?, arg: Void?): Visitable {
            n ?: return super.visit(n, arg)
            if (n.nameAsString.contains("EvoRunnerParameters")) {
                n.remove()
            }
            return super.visit(n, arg)
        }

        // removes @RunWith(EvoRunner.class)
        override fun visit(n: SingleMemberAnnotationExpr?, arg: Void?): Visitable {
            n?.remove()
            return super.visit(n, arg)
        }

        // Removes extension of scaffolding
        // Changes class name
        override fun visit(n: ClassOrInterfaceDeclaration?, arg: Void?): Visitable {
            val scaffoldClass = n?.extendedTypes?.get(0) ?: return super.visit(n, arg)
            scaffoldClass.remove()
            val baseName = n.nameAsString
            n.name = SimpleName("${baseName}_Cov")
            return super.visit(n, arg)
        }
    }

    fun edit(): String {

        val parser = JavaParser()
        val unit = parser.parse(text).result.get()

        val map = HashMap<String, Pair<BlockStmt, String>>()

        for (edit in activeTestList) {
            // hack needed to make java parser parse a method
            val code = "package p; public class c {${edit.value}}"
            val parsedModified = StaticJavaParser.parse(code)
            val extractor = BodyExtractor()
            val body = ArrayList<Pair<BlockStmt, String>>()
            extractor.visit(parsedModified, body)

            if (body.isEmpty()) continue

            val blockStmt = body[0]
            map[edit.key] = blockStmt
        }

        val replacer = TestCaseReplacer(map)
        replacer.visit(unit, null)

        val result = unit.toString()
        log.debug("EDITED TEST SUITE:\n$result")

        return result
    }

    fun editRemoveScaffold(testClass: String): String {
        val parser = JavaParser()
        val unit = parser.parse(testClass).result.get()

        val remover = ScaffoldRemover()
        remover.visit(unit, null)

        val result = unit.toString()
        log.debug("EDITED TEST SUITE NO SCAFFOLD:\n$result")

        return result
    }
}
