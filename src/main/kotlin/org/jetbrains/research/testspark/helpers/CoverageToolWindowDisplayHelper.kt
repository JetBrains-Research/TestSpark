package org.jetbrains.research.testspark.helpers

import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.table.JBTable
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import java.awt.Dimension
import javax.swing.JScrollPane
import javax.swing.table.AbstractTableModel

/**
 * Class to display coverage in the tool window.
 */
object CoverageToolWindowDisplayHelper {
    fun getPanel(data: ArrayList<String>): JScrollPane {
        // Implementation of abstract table model
        val tableModel = object : AbstractTableModel() {
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

        val table = JBTable(tableModel)

        val mainPanel = ScrollPaneFactory.createScrollPane(table)

        val tableColumnModel = table.columnModel
        tableColumnModel.getColumn(0).headerValue = PluginLabelsBundle.get("unitsUndertest")
        tableColumnModel.getColumn(1).headerValue = PluginLabelsBundle.get("lineCoverage")
        tableColumnModel.getColumn(2).headerValue = PluginLabelsBundle.get("branchCoverage")
        tableColumnModel.getColumn(3).headerValue = PluginLabelsBundle.get("weakMutationCoverage")

        table.columnModel = tableColumnModel
        table.minimumSize = Dimension(700, 100)

        return mainPanel
    }
}
