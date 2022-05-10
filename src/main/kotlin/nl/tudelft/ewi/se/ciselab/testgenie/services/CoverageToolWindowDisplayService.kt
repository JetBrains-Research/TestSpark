package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.table.AbstractTableModel

class CoverageToolWindowDisplayService {
    var mainPanel: JPanel ?= null
    var panelTitleAbsolute = JLabel("Absolute Test Coverage")
    var panelTitleRelative = JLabel("Relative Test Coverage")
    var absoluteLines = JBLabel("Amount of Lines covered: " + " Total: ")
    var absoluteBranch = JBLabel("Amount of Branches covered: " + " Total: ")
    var absoluteMutant = JBLabel("Amount of Mutants covered: "  + " Total: ")
    var relativeLines = JBLabel("Percentage of Lines covered: ")
    var relativeBranch = JBLabel("Percentage of Branches covered: ")
    var relativeMutant = JBLabel("Percentage of Mutants covered: ")
    var data: ArrayList<String> = arrayListOf("Coverage", "Lines", "Branches", "Mutants", "", "", "", "")

    private var listLabels = listOf(absoluteLines, absoluteBranch, absoluteMutant, relativeLines, relativeBranch, relativeMutant)
    // Implementation of abstract table model
    var tableModel = object : AbstractTableModel() {
        /**
         * Returns the number of rows.
         *
         * @return row count
         */
        override fun getRowCount(): Int {
            return 2
        }

        /**
         * Returns the number of columns.
         *
         * @return column count
         */
        override fun getColumnCount(): Int {
            return 4
        }

        /**
         * Returns the value at index.
         *
         * @param rowIndex index of row
         * @param columnIndex index of column
         * @return value at row
         */
        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            return data[rowIndex * 4 + columnIndex]
        }

    }
    var table = JBTable(tableModel)

    /**
     * Show the labels for statistics on code coverage by tests in "coverage visualisation" tab
     */
    init {
        panelTitleAbsolute.font = Font("Monochrome", Font.BOLD, 20)
        panelTitleRelative.font = Font("Monochrome", Font.BOLD, 20)

        for (listLabel in listLabels) {
            listLabel.font = Font("Monochrome", Font.BOLD, 17)
        }
        mainPanel = FormBuilder.createFormBuilder()
                .setFormLeftIndent(30)
                .addVerticalGap(7)
                .addComponent(panelTitleAbsolute,25)
                .addComponent(absoluteLines, 20)
                .addComponent(absoluteBranch, 20)
                .addComponent(absoluteMutant, 20)
                .addComponent(panelTitleRelative, 25)
                .addComponent(relativeLines, 20)
                .addComponent(relativeBranch, 20)
                .addComponent(relativeMutant, 20)
                .addComponentFillVertically(JPanel(), 20)
                .panel
    }
}