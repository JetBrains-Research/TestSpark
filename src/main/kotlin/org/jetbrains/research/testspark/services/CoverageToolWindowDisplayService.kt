package org.jetbrains.research.testspark.services

import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.table.JBTable
import org.jetbrains.research.testspark.TestSparkLabelsBundle
import java.awt.Dimension
import javax.swing.JScrollPane
import javax.swing.table.AbstractTableModel

/**
 * Class to display EvoSuite coverage in the tool window.
 */
class CoverageToolWindowDisplayService {
    var mainPanel: JScrollPane? = null
    var data: ArrayList<String> = arrayListOf("", "", "", "")

    // Implementation of abstract table model
    private var tableModel = object : AbstractTableModel() {
        /**
         * Returns the number of rows.
         *
         * @return row count
         */
        override fun getRowCount(): Int {
            return 1
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
            ScrollPaneFactory.createScrollPane(table)

        val tableColumnModel = table.columnModel
        tableColumnModel.getColumn(0).headerValue = TestSparkLabelsBundle.defaultValue("unitsUndertest")
        tableColumnModel.getColumn(1).headerValue = TestSparkLabelsBundle.defaultValue("lineCoverage")
        tableColumnModel.getColumn(2).headerValue = TestSparkLabelsBundle.defaultValue("branchCoverage")
        tableColumnModel.getColumn(3).headerValue = TestSparkLabelsBundle.defaultValue("weakMutationCoverage")
        table.columnModel = tableColumnModel

        table.minimumSize = Dimension(700, 100)
    }
}
