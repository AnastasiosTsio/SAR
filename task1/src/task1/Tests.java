package task1;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
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
    private static Broker bigBroker;
    private static BrokerManager brokerManager;
    private static ByteArrayOutputStream bufferBig = new ByteArrayOutputStream();;

    @BeforeAll
    static void setUpServer() {
        brokerManager = new BrokerManager();
        echoBroker = new ConcreteBroker("localhost", brokerManager);
        brokerManager.registerBroker(echoBroker);

        new Task(echoBroker, () -> {
        	Channel channel = echoBroker.accept(1234);
            while (true) {
                try {
                    byte[] buffer = new byte[255];
                    int bytesRead;

                    while ((bytesRead = channel.read(buffer, 0, buffer.length)) > 0) {
                        channel.write(buffer, 0, bytesRead);
                    }
                } catch (DisconnectedException e) {
                	if(channel.disconnected()) {
                		channel = echoBroker.accept(1234);
                	}
                }
            }
        }).start();
        
        bigBroker = new ConcreteBroker("bigBroker",brokerManager);
        brokerManager.registerBroker(bigBroker);
        new Task(bigBroker, () -> {
        	Channel channel = bigBroker.accept(4567);
            while (true) {
                try {
                    
                    byte[] buffer = new byte[255];
                    int bytesRead;

                    while (!channel.disconnected() && (bytesRead = channel.read(buffer, 0, buffer.length)) > 0) {
                    	bufferBig.write(buffer, 0, bytesRead);
                    }
                } catch (DisconnectedException e) {
                	if(channel.disconnected()) {
                		channel = bigBroker.accept(1234);
                	}
                }
            }
        }).start();
    }

    @ParameterizedTest
    @MethodSource("createBroker")
    void testEchoServer(Broker broker) throws InterruptedException {
    	System.out.println("--------------------testEchoServer--------------------");
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
        
        System.out.println("testEchoServer finished successfully");
    }

    @ParameterizedTest
    @MethodSource("createBroker")
    void testLargeDataTransfer(Broker broker) throws InterruptedException {
    	System.out.println("--------------------testLargeDataTransfer--------------------");
    	int port = 4567;
        
        try {
            Channel channel = broker.connect("bigBroker", port);
            assertNotNull(channel, "Channel should not be null");

            byte[] sendBytes = new byte[1024 * 5];
            for (int i = 0; i < sendBytes.length; i++) {
                sendBytes[i] = (byte) (i % 255);
            }

            int totalBytesWritten = 0;
            while (totalBytesWritten < sendBytes.length) {
                totalBytesWritten += channel.write(sendBytes, totalBytesWritten, sendBytes.length - totalBytesWritten);
            }
            assertEquals(sendBytes.length, totalBytesWritten, "Failed to write all bytes to the channel");

            assertArrayEquals(sendBytes, bufferBig.toByteArray(), "The echoed bytes do not match the sent bytes");
            channel.disconnect();
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
        System.out.println("testLargeDataTransfer finished successfully");
    }

    private static Stream<Broker> createBroker() {
        Broker broker = new ConcreteBroker("EchoBroker", brokerManager);
        brokerManager.registerBroker(broker);
        return Stream.of(broker);
    }
}

