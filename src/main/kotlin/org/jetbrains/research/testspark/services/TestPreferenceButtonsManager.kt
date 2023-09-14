package org.jetbrains.research.testspark.services

import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.helpers.storage.KeyValueStore
import org.jetbrains.research.testspark.helpers.storage.KeyValueStoreFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.border.LineBorder
import org.jetbrains.research.testspark.data.TestState
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

class TestPreferenceButtonsManager (
        private val likedTestsDir: Path,
        private val dislikedTestsDir: Path,
        private val testCode: String,
        private val testName: String) {
    private val likeButton = JButton(TestSparkLabelsBundle.defaultValue("likeButton"))
    private val dislikeButton = JButton(TestSparkLabelsBundle.defaultValue("dislikeButton"))
    private val defaultButtonBackgroundColor = JButton("").background

    val clickNotificationFrame = JFrame("Kotlin Swing Application")


    /**
     * Creating liked/disliked dirs if needed
     * Assigning click listeners on like/dislike buttons
     */
    init {
        Files.createDirectories(likedTestsDir)
        Files.createDirectories(dislikedTestsDir)

        clickNotificationFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        likeButton.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent) {
                val key = testName.toByteArray(Charsets.UTF_8);
                val value = testCode.toByteArray(Charsets.UTF_8);

                val entryAdded = addIfDoesNotExist(likedTestsDir, dislikedTestsDir, key, value);

                println("State of test '${testName}' after liked: ${getTestState()}")

                likeButton.background = Color.GREEN
                dislikeButton.background = defaultButtonBackgroundColor

                // showing message whether the test registered as liked or no-op
                if (entryAdded) {
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
                val key = testName.toByteArray(Charsets.UTF_8);
                val value = testCode.toByteArray(Charsets.UTF_8);

                val entryAdded = addIfDoesNotExist(dislikedTestsDir, likedTestsDir, key, value);

                println("State of test '${testName}' after disliked: ${getTestState()}")

                 dislikeButton.background = Color.RED
                 likeButton.background = defaultButtonBackgroundColor

                // showing message whether the test registered as disliked or no-op
                if (entryAdded) {
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
     * Determintes test state depending on its liked/disliked/unspecified state
     */
    public fun getTestState() : TestState {
        assert(!(existsInDir(likedTestsDir) && existsInDir(dislikedTestsDir)))

        if (existsInDir(likedTestsDir)) {
            return TestState.LIKED;
        }
        if (existsInDir(dislikedTestsDir)) {
            return TestState.DISLIKED;
        }
        return TestState.UNSPECIFIED;
    }

    /**
     * Checks whether the source code of the test is contained inside provided dir and tracked KeyValueStore
     */
    private fun existsInDir(sourceDir: Path) : Boolean {
        val store: KeyValueStore = KeyValueStoreFactory.create(sourceDir);
        var result: Boolean
        try {
            val key = testName.toByteArray(Charsets.UTF_8)
            result = store.contains(key)
        }
        finally {
            store.close();
        }

        return result;
    }

    /**
     * Adds {@code (key, value)} pair via {@link KeyValueStore} to {@code addDir} path if it has not been added yet,
     * and removes the same pair from {@code removeDir} if it exists there.
     *
     * Returns {@code true} if value has been added, otherwise returns {@code false}.
     */
    private fun addIfDoesNotExist(addDir: Path, removeDir: Path, key: ByteArray, value: ByteArray): Boolean {
        val addStore: KeyValueStore = KeyValueStoreFactory.create(addDir);
        val removeStore: KeyValueStore = KeyValueStoreFactory.create(removeDir);

        try {
            var added = false
            var removed = false
            if (!addStore.contains(key)) {
                addStore.upsert(key, value)
                removed = removeStore.remove(key)
                added = true
            }

            val result = String(addStore.loadValue(key)!!, Charsets.UTF_8)

            println("For dir='${addDir.toString()}':\nadded=$added, key='$key', value='$result'")
            println("For dir='${removeDir.toString()}':\nremoved=$removed, key='$key'")

            return added
        }
        finally {
            removeStore.close()
            addStore.close()
        }
    }
}