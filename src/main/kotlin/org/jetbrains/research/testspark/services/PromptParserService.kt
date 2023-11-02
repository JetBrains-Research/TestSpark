package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import java.awt.Font

enum class PROMPT_KEYWORD (val text: String){
    CODE("CODE"),
    LANGUAGE("LANGUAGE"),
    TESTING_PLATFORM("TESTING_PLATFORM"),
    MOCKING_PLATFORM("MOCKING_PLATFORM"),
    METHODS("METHODS"),
    POLYMORPHISM("POLYMORPHISM"),
}

@Service
class PromptParserService {

    private val attributes = TextAttributes(JBColor.ORANGE, null, null, null, Font.BOLD or Font.ITALIC)

    fun highlighter(textField: EditorTextField, prompt: String): EditorTextField {
        textField.document.setText(prompt)
        PROMPT_KEYWORD.values().forEach {
            val textToHighlight = "\$${it.text}"
            if(prompt.contains(textToHighlight)){
                val startOffset = prompt.indexOf(textToHighlight)
                val endOffset = startOffset + textToHighlight.length

                textField.addSettingsProvider {textFieldSettings ->
                    textFieldSettings.markupModel.addRangeHighlighter(
                        startOffset, endOffset, HighlighterLayer.LAST,
                        attributes, HighlighterTargetArea.EXACT_RANGE
                    )
                }
            }
        }

        return textField
    }


}