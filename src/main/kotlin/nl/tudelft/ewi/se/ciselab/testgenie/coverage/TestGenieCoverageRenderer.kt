package nl.tudelft.ewi.se.ciselab.testgenie.coverage

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.hint.HintManagerImpl
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.editor.markup.ActiveGutterRenderer
import com.intellij.openapi.editor.markup.LineMarkerRendererEx
import com.intellij.ui.HintHint
import com.intellij.ui.LightweightHint
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.MouseEvent

/**
 * This class extends the line marker and gutter editor to allow more functionality.
 * 
 * @param color color of marker
 * @param lineNumber lineNumber to color
 * @param tests list of tests that cover this line
 */
class TestGenieCoverageRenderer(private val color: Color, private val lineNumber : Int, private val tests : List<String>) : ActiveGutterRenderer,
    LineMarkerRendererEx {

    /**
     * Perform the action - show toolTip on mouse click.
     *
     * @param editor the editor
     * @param e mouse event
     */
    override fun doAction(editor: Editor, e: MouseEvent) {
        e.consume()
        val panel = FormBuilder
            .createFormBuilder()
            .addComponent(JBLabel(" Covered by tests: $tests "), 10)
            .addVerticalGap(10)
            .panel

        val hint = LightweightHint(panel)
        val point = HintManagerImpl.getHintPosition(hint, editor, LogicalPosition(lineNumber, 0), HintManager.RIGHT)
        HintManagerImpl.getInstanceImpl().showEditorHint(
            hint,
            editor,
            point,
            HintManager.HIDE_BY_ANY_KEY or HintManager.HIDE_BY_TEXT_CHANGE or HintManager.HIDE_BY_OTHER_HINT or HintManager.HIDE_BY_SCROLLING,
            -1,
            false,
            HintHint(editor, point)
        )
    }

    /**
     * Test if action can be performed - mouse is within the gutter.
     *
     * @param editor the editor
     * @param e mouse event
     * @return true iff mouse has interacted with gutter
     */
    override fun canDoAction(editor: Editor, e: MouseEvent): Boolean {
        val component = e.component
        if (component is EditorGutterComponentEx) {
            return e.x > component.lineMarkerAreaOffset && e.x < component.iconAreaOffset
        }
        return false
    }

    /**
     * Sets area to paint in given color.
     *
     * @param editor the editor
     * @param g graphics used to draw
     * @param r rectangle object
     */
    override fun paint(editor: Editor, g: Graphics, r: Rectangle) {
        g.fillRect(r.x, r.y, r.width, r.height)
        g.color = color
    }

    /**
     * Getter for offset of gutter color.
     *
     * @return position
     */
    override fun getPosition(): LineMarkerRendererEx.Position {
        return LineMarkerRendererEx.Position.LEFT
    }
}