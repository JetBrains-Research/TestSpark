package org.jetbrains.research.testspark.services

import com.github.dockerjava.api.exception.NotFoundException
import com.github.dockerjava.core.DockerClientBuilder
import com.intellij.openapi.components.Service
import javax.ws.rs.ProcessingException


@Service(Service.Level.PROJECT)
class DockerUtilsService {

    val dockerClient = DockerClientBuilder.getInstance().build()


    fun isDockerDaemonAccessible(): Boolean{
        return try {
            dockerClient.versionCmd().exec()
            true
        }catch (e: ProcessingException){
            false
        }
    }

    fun imageLocallyExists(imageID: String): Boolean{
        return try {
            dockerClient.inspectImageCmd(imageID).exec()
            true
        }catch (e: NotFoundException){
            false
        }
    }


    fun imageExistsOnDockerHub(): Boolean{
        // ToDo
    }

}