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
    private val maxValueFileSize = 1024;

    init {
        likeButton.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent) {
                JOptionPane.showMessageDialog(frame, testCode, "'$testName' liked", JOptionPane.INFORMATION_MESSAGE)

                val kvStore: KeyValueStore = KeyValueStoreFactory.create(workingDir, maxValueFileSize);

                try {
                    val key = testName.toByteArray(Charsets.UTF_8);

                    var written = false
                    if (!kvStore.contains(key)) {
                        written = true
                        kvStore.upsert(key, testCode.toByteArray(Charsets.UTF_8))
                    }

                    val res = String(kvStore.loadValue(key)!!, Charsets.UTF_8)
                    println("Written= $written; value for test:\n'${testName}': '${res}'")
                }
                finally {
                    kvStore.close()
                }
            }
        })
    }

    public fun getPreferenceButtons() : JPanel {
        val preferenceButtons = JPanel()
        preferenceButtons.add(likeButton)
        preferenceButtons.add(dislikeButton)
        return preferenceButtons
    }


}