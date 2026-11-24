package com.example.betsettlement;

import com.github.dockerjava.api.command.InspectContainerResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.List;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

class RocketMqContainer extends GenericContainer<RocketMqContainer> {
  private static final int NAME_SERVER_PORT = 9876;
  private static final String CREATE_TOPIC_COMMAND =
      "sh mqadmin updateTopic -n 127.0.0.1:9876 -c DefaultCluster -t bet-settlements";

  RocketMqContainer() {
    super(DockerImageName.parse("apache/rocketmq:5.5.0"));
    var brokerPort = findFreePort();

    withExposedPorts(NAME_SERVER_PORT);
    // clients connect to the address advertised by the broker, so both ports must be equal
    setPortBindings(List.of("%d:%d".formatted(brokerPort, brokerPort)));
    withEnv("JAVA_OPT_EXT", "-Xms256m -Xmx512m");
    withCommand("sh", "-c", startCommand(brokerPort));
    waitingFor(
        Wait.forSuccessfulCommand("sh mqadmin clusterList -n 127.0.0.1:9876 | grep -q broker-a")
            .withStartupTimeout(Duration.ofMinutes(2)));
  }

  String getNameServer() {
    return getHost() + ":" + getMappedPort(NAME_SERVER_PORT);
  }

  @Override
  protected void containerIsStarted(InspectContainerResponse containerInfo) {
    try {
      execInContainer("sh", "-c", CREATE_TOPIC_COMMAND);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Topic was not created", e);
    }
  }

  private String startCommand(int brokerPort) {
    return """
    printf 'brokerClusterName=DefaultCluster\nbrokerName=broker-a\nbrokerId=0\n' > /tmp/broker.conf
    printf 'brokerIP1=%s\nlistenPort=%d\nautoCreateTopicEnable=false\n' >> /tmp/broker.conf
    sh mqnamesrv &
    sleep 3
    sh mqbroker -n 127.0.0.1:9876 -c /tmp/broker.conf
    """
        .formatted(getHost(), brokerPort);
  }

  private int findFreePort() {
    try (var socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new IllegalStateException("No free port for RocketMQ broker", e);
    }
  }
}
