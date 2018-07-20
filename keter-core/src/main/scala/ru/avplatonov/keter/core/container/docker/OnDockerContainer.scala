package ru.avplatonov.keter.core.container.docker

import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.messages.{ContainerConfig, ContainerCreation, HostConfig}
import ru.avplatonov.keter.core.container.{AppContainer, Command}

import scala.collection.JavaConverters._

case class OnDockerContainer(docker: DockerClient, imageName: String) extends AppContainer {
    var creationDescr: Option[ContainerCreation] = None

    override def run(command: Command): Unit = {
        val binds = command.sharedPaths().asScala
            .map(p => p.normalize().toString)
            .map(p => HostConfig.Bind.builder().from(p).to(p).build())
        val hostConfig = HostConfig.builder().binds(binds: _*).build()
        val containerConfig = ContainerConfig.builder()
            .hostConfig(hostConfig)
            .image(imageName)
            .cmd("bash", "-c", command.script())
            .build()
        creationDescr = Some(docker.createContainer(containerConfig))
        docker.startContainer(creationDescr.get.id())
    }

    def status(): String = inspectContainer
        .map(_.state().status())
        .getOrElse("unknown")

    def exitCode(): Int = inspectContainer.map(_.state().exitCode()) match {
        case None => 0
        case Some(value) => value
    }

    private def inspectContainer = {
        creationDescr.map(_.id())
            .map(docker.inspectContainer)
    }
}
