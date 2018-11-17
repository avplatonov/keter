package ru.avplatonov.keter.core.worker.docker

import com.spotify.docker.client.DefaultDockerClient
import org.scalatest.FlatSpec

class ContainerTest extends FlatSpec {
    behavior of "DockerContainer"

    val dockerClient = new DefaultDockerClient("unix:///var/run/docker.sock")

    it must "execute command" in {
//        Container("sleep 1; cat 'hello' > test.file", )
    }
}
