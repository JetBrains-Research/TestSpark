package com.github.mitchellolsthoorn.testgenie.services

import com.intellij.openapi.project.Project
import com.github.mitchellolsthoorn.testgenie.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
