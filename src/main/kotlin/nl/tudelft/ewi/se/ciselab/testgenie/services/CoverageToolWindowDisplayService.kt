package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel
import javax.swing.table.AbstractTableModel

class CoverageToolWindowDisplayService {
    var mainPanel: JPanel? = null
    var data: ArrayList<String> = arrayListOf("Coverage", "Lines", "Branches", "Mutants", "", "", "", "")

    // Implementation of abstract table model
    private var tableModel = object : AbstractTableModel() {
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
    private var table = JBTable(tableModel)

    /**
     * Show the labels for statistics on code coverage by tests in "coverage visualisation" tab
     */
    init {
        mainPanel =
            FormBuilder.createFormBuilder().addComponent(table, 20).addComponentFillVertically(JPanel(), 20).panel
    }
}