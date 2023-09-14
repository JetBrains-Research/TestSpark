package org.jetbrains.research.testspark.services

import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.helpers.storage.KeyValueStore
import org.jetbrains.research.testspark.helpers.storage.KeyValueStoreFactory
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel

class TestPreferenceButtonsManager (val frame: JFrame, private val workingDir: Path, private val testCode: String, private val testName: String) {
    private val likeButton = JButton(TestSparkLabelsBundle.defaultValue("likeButton"))
    private val dislikeButton = JButton(TestSparkLabelsBundle.defaultValue("dislikeButton"))
    private val likedTestsDir = Paths.get(workingDir.toString(), "liked-tests/")
    private val dislikedTestsDir = Paths.get(workingDir.toString(), "disliked-tests/")
    private val maxValueFileSize = 1024;

    public enum class TestState {
        UNSPECIFIED,
        LIKED,
        DISLIKED
    }


    init {
        Files.createDirectories(likedTestsDir)
        Files.createDirectories(dislikedTestsDir)

        likeButton.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent) {
                JOptionPane.showMessageDialog(frame, testCode, "'$testName' liked", JOptionPane.INFORMATION_MESSAGE)

                val key = testName.toByteArray(Charsets.UTF_8);
                val value = testCode.toByteArray(Charsets.UTF_8);

                addIfDoesNotExist(likedTestsDir, dislikedTestsDir, key, value);

                println("State of test '${testName}' after liked: ${getTestState()}")
            }
        })

        dislikeButton.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent) {
                JOptionPane.showMessageDialog(frame, testCode, "'$testName' disliked", JOptionPane.INFORMATION_MESSAGE)

                val key = testName.toByteArray(Charsets.UTF_8);
                val value = testCode.toByteArray(Charsets.UTF_8);

                addIfDoesNotExist(dislikedTestsDir, likedTestsDir, key, value);

                println("State of test '${testName}' after disliked: ${getTestState()}")
            }
        })
    }

    public fun getPreferenceButtons() : JPanel {
        val preferenceButtons = JPanel()
        preferenceButtons.add(likeButton)
        preferenceButtons.add(dislikeButton)
        return preferenceButtons
    }

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

    private fun existsInDir(sourceDir: Path) : Boolean {
        val store: KeyValueStore = KeyValueStoreFactory.create(sourceDir, maxValueFileSize);
        var result = false
        try {
            val key = testName.toByteArray(Charsets.UTF_8)
            result = store.contains(key)
        }
        finally {
            store.close();
        }

        return result;
    }

    private fun addIfDoesNotExist(addDir: Path, removeDir: Path, key: ByteArray, value: ByteArray) {
        val addStore: KeyValueStore = KeyValueStoreFactory.create(addDir, maxValueFileSize);
        val removeStore: KeyValueStore = KeyValueStoreFactory.create(removeDir, maxValueFileSize);

        try {
            var written = false
            var removed = false
            if (!addStore.contains(key)) {
                addStore.upsert(key, value)
                removed = removeStore.remove(key)
                written = true
            }

            val result = String(addStore.loadValue(key)!!, Charsets.UTF_8)

            println("For dir='${addDir.toString()}':\nwritten=$written, key='$key', value='$result'")
            println("For dir='${removeDir.toString()}':\nremoved=$removed, key='$key'")
        }
        finally {
            removeStore.close()
            addStore.close()
        }
    }
}