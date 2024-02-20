package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import java.awt.Font

enum class PromptKeyword(val text: String, val description: String, val mandatory: Boolean) {
    NAME("NAME", "The name of the code under test (Class name, method name, line number)", true),
    CODE("CODE", "The code under test (Class, method, or line)", true),
    LANGUAGE("LANGUAGE", "Programming language of the project under test (only Java supported at this point)", true),
    TESTING_PLATFORM(
        "TESTING_PLATFORM",
        "Testing platform used in the project (Only JUnit 4 is supported at this point)",
        true,
    ),
    MOCKING_FRAMEWORK(
        "MOCKING_FRAMEWORK",
        "Mock framework that can be used in generated test (Only Mockito is supported at this point)",
        false,
    ),
    METHODS("METHODS", "Signature of methods used in the code under tests", false),
    POLYMORPHISM("POLYMORPHISM", "Polymorphism relations between classes involved in the code under test.", false),
    TEST_SAMPLE("TEST_SAMPLE", "TODO.", false),//TODO
    ;

    fun getOffsets(prompt: String): Pair<Int, Int>? {
        val textToHighlight = "\$$text"
        if (!prompt.contains(textToHighlight)) {
            return null
        }

        val startOffset = prompt.indexOf(textToHighlight)
        val endOffset = startOffset + textToHighlight.length
        return Pair(startOffset, endOffset)
    }
}

@Service
class PromptParserService {

    private val attributes = TextAttributes(JBColor.ORANGE, null, null, null, Font.BOLD or Font.ITALIC)

    fun highlighter(textField: EditorTextField, prompt: String): EditorTextField {
        val editor = textField.editor
        var markup: MarkupModel? = null
        if (editor != null) {
            markup = textField.editor!!.markupModel
            markup.removeAllHighlighters()
        }

        PromptKeyword.values().forEach {
            it.getOffsets(prompt)?.let { offsets ->
                val startOffset = offsets.first
                val endOffset = offsets.second

                if (editor != null) {
                    markup!!.addRangeHighlighter(
                        startOffset,
                        endOffset,
                        HighlighterLayer.LAST,
                        attributes,
                        HighlighterTargetArea.EXACT_RANGE,
                    )
                } else {
                    textField.addSettingsProvider { textFieldSettings ->
                        textFieldSettings.markupModel.addRangeHighlighter(
                            startOffset,
                            endOffset,
                            HighlighterLayer.LAST,
                            attributes,
                            HighlighterTargetArea.EXACT_RANGE,
                        )
                    }
                }
            }
        }

        return textField
    }

    fun getKeywords(): Array<PromptKeyword> {
        return PromptKeyword.values()
    }

    fun isPromptValid(prompt: String): Boolean {
        PromptKeyword.values().forEach {
            if (it.mandatory) {
                val text = "\$${it.text}"
                if (!prompt.contains(text)) {
                    return false
                }
            }
        }
        return true
    }
}
