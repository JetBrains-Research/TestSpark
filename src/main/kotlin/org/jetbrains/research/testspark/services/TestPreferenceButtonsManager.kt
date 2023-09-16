package org.jetbrains.research.testspark.services

import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.helpers.storage.KeyValueStore
import org.jetbrains.research.testspark.helpers.storage.KeyValueStoreFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.nio.file.Path
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel

class TestPreferenceButtonsManager (private val jsonFilepath: Path, private val testCode: String, private val testName: String) {
    private val likeButton = JButton(TestSparkLabelsBundle.defaultValue("likeButton"))
    private val dislikeButton = JButton(TestSparkLabelsBundle.defaultValue("dislikeButton"))
    private val defaultButtonBackgroundColor = JButton("").background

    val clickNotificationFrame = JFrame("Kotlin Swing Application")


    /**
     * Creating liked/disliked dirs if needed
     * Assigning click listeners on like/dislike buttons
     */
    init {
        clickNotificationFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        likeButton.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent) {
                val keyToAdd = "tests.liked.$testName";
                val keyToRemove = "tests.disliked.$testName";
                val value = testCode;

                val entryAdded = addIfDoesNotExist(keyToAdd, keyToRemove, value);

                // showing message whether the test registered as liked or no-op
                if (entryAdded) {
                    likeButton.background = Color.GREEN
                    dislikeButton.background = defaultButtonBackgroundColor

                    JOptionPane.showMessageDialog(clickNotificationFrame,
                            "Test code for the test '$testName' successfully registered as liked", "'$testName' liked", JOptionPane.INFORMATION_MESSAGE)
                }
                else {
                    JOptionPane.showMessageDialog(clickNotificationFrame,
                            "No operation performed: test code for the test '$testName' already registered as liked", "'$testName' liked", JOptionPane.INFORMATION_MESSAGE)
                }
            }
        })

        dislikeButton.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent) {
                val keyToAdd = "tests.disliked.$testName";
                val keyToRemove = "tests.liked.$testName";
                val value = testCode;

                val entryAdded = addIfDoesNotExist(keyToAdd, keyToRemove, value);

                // showing message whether the test registered as disliked or no-op
                if (entryAdded) {
                    dislikeButton.background = Color.RED
                    likeButton.background = defaultButtonBackgroundColor

                    JOptionPane.showMessageDialog(clickNotificationFrame,
                            "Test code for the test '$testName' successfully registered as disliked", "'$testName' disliked", JOptionPane.INFORMATION_MESSAGE)
                }
                else {
                    JOptionPane.showMessageDialog(clickNotificationFrame,
                            "No operation performed: test code for the test '$testName' already registered as disliked", "'$testName' disliked", JOptionPane.INFORMATION_MESSAGE)
                }
            }
        })
    }

    /**
     * Creates container with like/dislike buttons inside
     */
    public fun getPreferenceButtons() : JPanel {
        val buttonsContainer = JPanel(FlowLayout())

        buttonsContainer.add(likeButton)
        buttonsContainer.add(dislikeButton)

        val preferenceButtonsPanel = JPanel()
        preferenceButtonsPanel.layout = BorderLayout()
        preferenceButtonsPanel.add(buttonsContainer, BorderLayout.EAST)

        return preferenceButtonsPanel
    }


    /**
     * @param keyToAdd - key by which a provided value must be stored
     * @param keyToRemove - key that should be removed from the store
     * @param value - value that should be added
     *
     * @return wether the value was successfully added into the file
     */
    private fun addIfDoesNotExist(keyToAdd: String, keyToRemove: String, value: String): Boolean {
        val store: KeyValueStore = KeyValueStoreFactory.create(jsonFilepath);

        var added = false
        var removed = false

        if (!store.contains(keyToAdd)) {
            store.upsert(keyToAdd, value)
            removed = store.remove(keyToRemove)
            added = true
        }

        val result = store.get(keyToAdd)!!

        println("For filepath='${jsonFilepath.toString()}':\nadded=$added, key='$keyToAdd', value='$result'")
        println("For filepath='${jsonFilepath.toString()}':\nremoved=$removed, key='$keyToRemove'")

        return added
    }
}