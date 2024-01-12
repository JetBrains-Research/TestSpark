package org.jetbrains.research.testspark.services

import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.command.ExecCreateCmdResponse
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.exception.NotFoundException
import com.github.dockerjava.api.model.ContainerConfig
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.command.ExecStartResultCallback
import com.intellij.openapi.components.Service
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
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

    fun pullImage(imageID: String): Boolean {
        if (imageLocallyExists(imageID)){
            return true
        }

        return try {
            dockerClient.pullImageCmd(imageID)
                .exec(PullImageResultCallback())
                .awaitCompletion()
            true
        }catch (e: NotFoundException){
            false
        }
    }

    fun createContainer(imageID: String, username: String): String? {

        val container:CreateContainerResponse  = dockerClient.createContainerCmd(imageID)
            .withCmd("/bin/bash", "-c", "while true; do sleep 1; done")
            .exec()

        dockerClient.startContainerCmd(container.id).exec()


        // Creat exec command to install shadow yum install -y shadow-utils
        val shadowInstallCmd:ExecCreateCmdResponse = dockerClient.execCreateCmd(container.id)
            .withCmd("yum", "install", "-y", "shadow-utils").exec()

        dockerClient.execStartCmd(shadowInstallCmd.id)
            .exec(ExecStartResultCallback(System.out, System.err))
            .awaitCompletion()

        // Wait until the command has finished
        var shadowInstalled= false

        while (true){
            val useraddCheck:ExecCreateCmdResponse = dockerClient.execCreateCmd(container.id)
                .withCmd("useradd", username).
                exec()
            // Execute command
        dockerClient.execStartCmd(
            dockerClient.execCreateCmd(container.id)
                .withCmd("useradd", username).
                exec().id
        ).exec(ResultCallback.Adapter()).awaitCompletion()

        }


        // Execute command
//        dockerClient.execStartCmd(shadowInstallCmd.id).start().awaitCompletion()

        // Create exec command to add new user
        val execCreateCmdResponse:ExecCreateCmdResponse = dockerClient.execCreateCmd(container.id)
            .withCmd("useradd", username).
            exec()
        // Execute command
//        dockerClient.execStartCmd(
//            dockerClient.execCreateCmd(container.id)
//                .withCmd("useradd", username).
//                exec().id
//        ).start().awaitCompletion()





//        val execStartResultCallback2 = object : ExecStartResultCallback(outputStream, errorStream) {}

//        dockerClient.execStartCmd(execCreateCmdResponse.id) // Start the command
//            .withDetach(false) // Don't detach, we want to capture the output
//            .exec(execStartResultCallback2)
//
//// Wait until the command has finished
//// Note: Provide an appropriate timeout value for your command, or use awaitCompletion() to wait indefinitely
//        execStartResultCallback2.awaitCompletion(10, TimeUnit.SECONDS)
//
//// Convert the outputStream and errorStream to String to get the command output and error output
//        val commandOutput = outputStream.toString("UTF-8")
//        val errorOutput = errorStream.toString("UTF-8")
//
//        println("Command Output:\n$commandOutput")
//        println("Error Output:\n$errorOutput")

//        // Create exec command to add new user
        val execCreateCmdResponse2:ExecCreateCmdResponse = dockerClient.execCreateCmd(container.id)
            .withCmd("useradd", username).
            withUser("root").
            exec()
        // Execute command
        dockerClient.execStartCmd(execCreateCmdResponse2.id).start().awaitCompletion()

        return container.id
    }


    fun makeDir(containerID: String, username: String, dir: String){
        val execCreateCmdResponse: ExecCreateCmdResponse = dockerClient.execCreateCmd(containerID)
            .withCmd("mkdir", "-p", dir)
            .withUser(username)
            .exec()

        // Execute the mkdir command inside the running container
        dockerClient.execStartCmd(execCreateCmdResponse.id).start().awaitCompletion()

    }

    fun copyFilesToContainer(containerID: String, srcToCopyOnHost: String, destinationPath: String){
        // Copy files to the container
        val src = File(srcToCopyOnHost) // specify the source path on the host
        dockerClient.copyArchiveToContainerCmd(containerID)
            .withHostResource(src.absolutePath)
            .withRemotePath(destinationPath) // specify the destination path in the container
            .exec()
    }

    fun stopContainer(containerID: String){
        // Stop the container
        dockerClient.stopContainerCmd(containerID).exec()
    }

}