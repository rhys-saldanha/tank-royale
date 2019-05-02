package net.robocode2.gui.ui

import net.robocode2.gui.client.Client
import net.robocode2.gui.model.BotResults
import net.robocode2.gui.ui.ResourceBundles.STRINGS
import java.awt.EventQueue
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table
import java.awt.Dimension


class ResultsWindow(results: List<BotResults>) : JFrame(getWindowTitle()), AutoCloseable {

    init {
        val table = JTable(getData(results), getColumns())

        val tableHeader = table.tableHeader
        val headerFontMetrics = tableHeader.getFontMetrics(tableHeader.font)

        val columnModel = tableHeader.columnModel

        for (columnIndex in 0 until columnModel.columnCount) {
            val column = columnModel.getColumn(columnIndex)
            val title = "" + column.headerValue
            column.minWidth = 10 + headerFontMetrics.stringWidth(title)

            val cellRenderer = DefaultTableCellRenderer()
            cellRenderer.horizontalAlignment = JLabel.CENTER
            column.cellRenderer = cellRenderer
        }

        val tableSize = Dimension(700, table.model.rowCount * table.rowHeight)
        table.preferredScrollableViewportSize = tableSize
        table.preferredSize = tableSize
        table.minimumSize = tableSize

        val scrollPane = JScrollPane(table)
        contentPane.add(scrollPane)

        pack()
        setLocationRelativeTo(null) // center on screen
    }

    override fun close() {
    }

    private fun getData(results: List<BotResults>): Array<Array<String>> {
        val list = ArrayList<Array<String>>()
        results.forEach {
            val name = "${it.name} ${it.version}"
            list.add(arrayOf(
                    "" + it.rank,
                    "" + name,
                    "" + it.totalScore,
                    "" + it.survival,
                    "" + it.lastSurvivorBonus,
                    "" + it.bulletDamage,
                    "" + it.bulletKillBonus,
                    "" + it.ramDamage,
                    "" + it.ramKillBonus,
                    "" + it.firstPlaces,
                    "" + it.secondPlaces,
                    "" + it.thirdPlaces
            ))
        }
        return list.toArray(arrayOfNulls<Array<String>>(list.size))
    }

    private fun getColumns(): Array<String> {
        return arrayOf(
                STRINGS.get("results.rank"),
                STRINGS.get("results.robot_name"),
                STRINGS.get("results.total_score"),
                STRINGS.get("results.survival_score"),
                STRINGS.get("results.last_survivor_bonus"),
                STRINGS.get("results.bullet_damage_score"),
                STRINGS.get("results.bullet_kill_bonus"),
                STRINGS.get("results.ram_damage"),
                STRINGS.get("results.ram_kill_bonus"),
                STRINGS.get("results.firsts"),
                STRINGS.get("results.seconds"),
                STRINGS.get("results.thirds")
        )
    }
}

private fun getWindowTitle(): String {
    val numberOfRounds: Int = Client.currentGameSetup?.numberOfRounds ?: 0
    return ResourceBundles.UI_TITLES.get("results_window").replace("$1", "$numberOfRounds")
}

private fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val results: List<BotResults> = listOf(
            BotResults(
                    name = "Rampage",
                    version = "1.0",
                    id = 1,
                    rank = 1,
                    totalScore = 2204,
                    survival = 250,
                    lastSurvivorBonus = 50,
                    bulletDamage = 1724,
                    bulletKillBonus = 180,
                    ramDamage = 10,
                    ramKillBonus = 20,
                    firstPlaces = 5,
                    secondPlaces = 6,
                    thirdPlaces = 7
            ),
            BotResults(
                    name = "Master",
                    version = "0.0.2",
                    rank = 2,
                    id = 2,
                    totalScore = 2108,
                    survival = 245,
                    lastSurvivorBonus = 40,
                    bulletDamage = 797,
                    bulletKillBonus = 21,
                    ramDamage = 0,
                    ramKillBonus = 0,
                    firstPlaces = 3,
                    secondPlaces = 1,
                    thirdPlaces = 9
            )
    )

    EventQueue.invokeLater {
        ResultsWindow(results).isVisible = true
    }
}