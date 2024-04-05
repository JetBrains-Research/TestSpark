package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import org.jetbrains.research.testspark.core.data.CollectorsData

@Service(Service.Level.PROJECT)
class CollectorService {
    val data: CollectorsData = CollectorsData()
}
