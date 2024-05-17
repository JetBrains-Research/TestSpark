package org.jetbrains.research.testspark.display.custom

import com.intellij.openapi.progress.ProgressIndicator
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator

class IJProgressIndicator(private val indicator: ProgressIndicator) : CustomProgressIndicator {
    private var isStopped = false

    override fun setText(text: String) {
        indicator.text = text
    }

    override fun getText(): String = indicator.text

    override fun setIndeterminate(value: Boolean) {
        indicator.isIndeterminate = value
    }

    override fun isIndeterminate(): Boolean = indicator.isIndeterminate

    override fun setFraction(value: Double) {
        indicator.fraction = value
    }

    override fun getFraction(): Double = indicator.fraction

    override fun isCanceled(): Boolean = indicator.isCanceled

    override fun isStopped(): Boolean = isStopped

    override fun start() {
        indicator.start()
    }

    override fun stop() {
        isStopped = true
        indicator.stop()
    }

    override fun cancel() {
        indicator.cancel()
    }
}
