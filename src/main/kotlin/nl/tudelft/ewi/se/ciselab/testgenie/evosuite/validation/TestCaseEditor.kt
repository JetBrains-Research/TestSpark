package nl.tudelft.ewi.se.ciselab.testgenie.evosuite.validation

import com.github.javaparser.JavaParser
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.visitor.ModifierVisitor
import com.github.javaparser.ast.visitor.Visitable
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.intellij.openapi.diagnostic.Logger

class TestCaseEditor(private val text: String, private val edits: HashMap<String, String>) {
    private val log: Logger = Logger.getInstance(this.javaClass)

    class TestCaseReplacer(private val edits: HashMap<String, BlockStmt>) : ModifierVisitor<Void>() {
        private val log: Logger = Logger.getInstance(this.javaClass)

        override fun visit(n: MethodDeclaration?, arg: Void?): Visitable {
            val name = n?.name!!

            val testName = name.toString()
            val modifiedBody = edits[testName]
            if (modifiedBody != null) {
                log.info("Test case modified! $testName")
                n.setBody(modifiedBody)
            } else {
                log.info("Test case not modified! $testName")
            }

            return super.visit(n, arg)
        }
    }

    class BodyExtractor : VoidVisitorAdapter<ArrayList<BlockStmt>>() {
        override fun visit(n: MethodDeclaration?, arg: ArrayList<BlockStmt>) {
            super.visit(n, arg)
            val body = n?.body?.get()!!
            if (body != null) {
                arg.add(body)
            }
        }
    }

    fun edit(): String {

        val parser = JavaParser()
        val unit = parser.parse(text).result.get()

        val map = HashMap<String, BlockStmt>()

        for (edit in edits) {
            // hack needed to make java parser parse a method
            val code = "package p; public class c {${edit.value}}"
            val parsedModified = StaticJavaParser.parse(code)
            val extractor = BodyExtractor()
            val body = ArrayList<BlockStmt>()
            extractor.visit(parsedModified, body)

            if (body.isEmpty()) continue

            val blockStmt = body[0]
            map[edit.key] = blockStmt
        }

        val replacer = TestCaseReplacer(map)
        replacer.visit(unit, null)

        val result = unit.toString()
        log.trace("EDITED TEST SUITE:\n$result")

        return result
    }
}
