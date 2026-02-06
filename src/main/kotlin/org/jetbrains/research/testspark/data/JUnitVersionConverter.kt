package org.jetbrains.research.testspark.data

import com.intellij.util.xmlb.Converter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.research.testspark.core.data.JUnitVersion

class JUnitVersionConverter : Converter<JUnitVersion>() {
    override fun fromString(value: String): JUnitVersion = Json.decodeFromString(value)

    override fun toString(value: JUnitVersion): String = Json.encodeToString(value)
}
