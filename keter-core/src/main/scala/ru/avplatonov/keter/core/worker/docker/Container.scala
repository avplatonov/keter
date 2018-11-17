package ru.avplatonov.keter.core.worker.docker

import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.messages.{ContainerConfig, ContainerCreation, HostConfig}

case class Container(command: String, sharedPaths: List[Path], imageName: String) {
    val creationDescrRef: AtomicReference[ContainerCreation] = new AtomicReference[ContainerCreation]()

    def run(docker: DockerClient): Unit = {
        val binds = sharedPaths
            .map(p => p.normalize().toString)
            .map(p => HostConfig.Bind.builder().from(p).to(p).build())
        val hostConfig = HostConfig.builder().binds(binds: _*).build()
        val containerConfig = ContainerConfig.builder()
            .hostConfig(hostConfig)
            .image(imageName)
            .cmd("bash", "-c", command)
            .build()

        val creation = docker.createContainer(containerConfig)
        creationDescrRef.set(creation)
        docker.startContainer(creation.id())

    }

    def status(docker: DockerClient): String = inspectContainer(docker)
        .map(_.state().status())
        .getOrElse("unknown")

    def exitCode(docker: DockerClient): Int = inspectContainer(docker).map(_.state().exitCode()) match {
        case None => 0
        case Some(value) => value
    }

    private def inspectContainer(docker: DockerClient) =
        creationDescr.map(_.id()).map(docker.inspectContainer)

    private def creationDescr = creationDescrRef.get() match {
        case null => None
        case value => Some(value)
    }
}
