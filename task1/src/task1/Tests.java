package task1;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import except.DisconnectedException;
import impl.BrokerManager;
import impl.ConcreteBroker;
class Tests {

    private static Broker echoBroker;

    @BeforeAll
    static void setUpServer() {
        BrokerManager brokerManager = new BrokerManager();
        echoBroker = new ConcreteBroker("EchoBroker", brokerManager);
        brokerManager.registerBroker(echoBroker);

        new Task(echoBroker, () -> {
            while (true) {
                try {
                    Channel channel = echoBroker.accept(1234);
                    byte[] buffer = new byte[255];
                    int bytesRead;

                    while ((bytesRead = channel.read(buffer, 0, buffer.length)) > 0) {
                        channel.write(buffer, 0, bytesRead);
                    }
                } catch (DisconnectedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @ParameterizedTest
    @MethodSource("createBroker")
    void testEchoServer(Broker broker) {
        int clientCount = 5;
        int port = 1234;
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);

        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            executor.submit(() -> {
                Channel channel = broker.connect("localhost", port);
                assertNotNull(channel, "Channel should not be null");

                byte[] sendBytes = new byte[255];
                for (int j = 0; j < 255; j++) {
                    sendBytes[j] = (byte) (j + 1);
                }

                try {
                    int bytesWritten = channel.write(sendBytes, 0, sendBytes.length);
                    assertEquals(255, bytesWritten, "Failed to write all bytes to the channel");

                    byte[] receivedBytes = new byte[255];
                    int bytesRead = channel.read(receivedBytes, 0, receivedBytes.length);
                    assertEquals(255, bytesRead, "Failed to read all bytes from the channel");

                    assertArrayEquals(sendBytes, receivedBytes, "The echoed bytes do not match the sent bytes");
                } catch (DisconnectedException e) {
                    fail("Channel disconnected unexpectedly: " + e.getMessage());
                }

                channel.disconnect();
                assertTrue(channel.disconnected(), "Channel should be disconnected");
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    private static Stream<Broker> createBroker() {
        BrokerManager brokerManager = new BrokerManager();
        Broker broker = new ConcreteBroker("EchoBroker", brokerManager);
        brokerManager.registerBroker(broker);

        return Stream.of(broker);
    }
}

