package org.jetbrains.research.testspark.data

import com.intellij.util.xmlb.Converter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JUnitVersionConverter : Converter<JUnitVersion>() {
    override fun fromString(value: String): JUnitVersion {
        return Json.decodeFromString(value)
    }

    override fun toString(value: JUnitVersion): String {
        return Json.encodeToString(value)
    }
}
