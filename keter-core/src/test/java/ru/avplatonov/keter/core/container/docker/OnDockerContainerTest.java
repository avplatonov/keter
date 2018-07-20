package ru.avplatonov.keter.core.container.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import ru.avplatonov.keter.core.container.Command;

import static org.junit.Assert.*;

public class OnDockerContainerTest {
    @Test
    public void test1() throws DockerCertificateException, InterruptedException {
        DockerClient client = new DefaultDockerClient("unix:///var/run/docker.sock");
        OnDockerContainer container = OnDockerContainer.apply(client, "aplatonov/linux_with_java");
        container.run(new Command() {
            @Override public String script() {
                return "echo hello world' > /tmp/hello_world_msg; sleep 5";
            }

            @Override public List<Path> sharedPaths() {
                return Arrays.asList(Paths.get("/tmp"));
            }
        });

        System.out.println(container.status());
        System.out.println(container.exitCode());
        Thread.sleep(6000);
        System.out.println(container.status());
        System.out.println(container.exitCode());
    }
}
