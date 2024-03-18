package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import java.io.File

@Service(Service.Level.PROJECT)
class TestStorageProcessingService(private val project: Project) {
    private val sep = File.separatorChar



    private val javaHomeDirectory = ProjectRootManager.getInstance(project).projectSdk!!.homeDirectory!!

    private val log = Logger.getInstance(this::class.java)

    private val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!









}
