package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import java.awt.Font

enum class PROMPT_KEYWORD (val text: String, val description: String){
    CODE("CODE","Code dsc"),
    LANGUAGE("LANGUAGE", "lang dsc"),
    TESTING_PLATFORM("TESTING_PLATFORM", "tetsingpl dsc"),
    MOCKING_PLATFORM("MOCKING_PLATFORM", "mock dsc"),
    METHODS("METHODS", "methods dsc"),
    POLYMORPHISM("POLYMORPHISM", "poly dsc"),
}

@Service
class PromptParserService {

    private val attributes = TextAttributes(JBColor.ORANGE, null, null, null, Font.BOLD or Font.ITALIC)


    fun highlighter(textField: EditorTextField, prompt: String): EditorTextField {
        val editor = textField.editor
        var markup: MarkupModel? = null
        if (editor != null){
            markup = textField.editor!!.markupModel
            markup.removeAllHighlighters()
        }

        PROMPT_KEYWORD.values().forEach {
            val textToHighlight = "\$${it.text}"
            if(prompt.contains(textToHighlight)){
                val startOffset = prompt.indexOf(textToHighlight)
                val endOffset = startOffset + textToHighlight.length

                if (editor != null) {
                    markup!!.addRangeHighlighter(
                        startOffset, endOffset, HighlighterLayer.LAST,
                        attributes, HighlighterTargetArea.EXACT_RANGE
                    )
                }else{
                    textField.addSettingsProvider {textFieldSettings ->
                    textFieldSettings.markupModel.addRangeHighlighter(
                        startOffset, endOffset, HighlighterLayer.LAST,
                        attributes, HighlighterTargetArea.EXACT_RANGE
                    )
                }
                }
            }
        }

        return textField
    }

    fun getKeywords(): Array<PROMPT_KEYWORD> {
        return PROMPT_KEYWORD.values()
    }


}