package task1;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import except.DisconnectedException;
import impl.BrokerManager;
import impl.ConcreteBroker;
class Tests {

    private static Broker echoBroker;
    private static BrokerManager brokerManager;

    @BeforeAll
    static void setUpServer() {
        brokerManager = new BrokerManager();
        echoBroker = new ConcreteBroker("localhost", brokerManager);
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
    void testEchoServer(Broker broker) throws InterruptedException {
        int clientCount = 5;
        int port = 1234;
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            tasks.add(() -> {
                try {
                	System.out.println("Client " + clientId + " submitted.");
                	
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
                    
                    System.out.println("Client " + clientId + " finished.");
                    return null;
                } catch (AssertionError e) {
                    System.err.println("AssertionError: " + e.getMessage());
                    throw e;
                } catch (Exception e) {
                    System.err.println("Exception: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });
        }

        List<Future<Void>> futures = executor.invokeAll(tasks);
        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            System.out.println("Some tasks did not finish in time.");
        }

        for (Future<Void> future : futures) {
            try {
                future.get(); 
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }


    private static Stream<Broker> createBroker() {
        Broker broker = new ConcreteBroker("EchoBroker", brokerManager);
        brokerManager.registerBroker(broker);
        return Stream.of(broker);
    }
}

