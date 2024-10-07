package abstracts;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

import impl.ConcreteQueueBroker;
class Tests {

	 private static ConcreteQueueBroker echoQueueBroker;
	    private static ConcreteQueueBroker bigQueueBroker;
	    private static BrokerManager brokerManager;
	    private static ByteArrayOutputStream bufferBig = new ByteArrayOutputStream();
	    
    @BeforeAll
    static void setUpServer() {
        brokerManager = new BrokerManager();
        echoQueueBroker = new ConcreteQueueBroker(new ConcreteBroker("localhost", brokerManager));
        brokerManager.registerBroker(echoQueueBroker.broker);

        new Task(echoQueueBroker, () -> {
            var queue = echoQueueBroker.accept(1234);
            while (true) {
                try {
                    byte[] receivedBytes = queue.receive();
                    queue.send(receivedBytes, 0, receivedBytes.length);
                } catch (DisconnectedException e) {
                    if (queue.closed()) {
                        queue = echoQueueBroker.accept(1234);
                    }
                }
            }
        }).start();

        bigQueueBroker = new ConcreteQueueBroker(new ConcreteBroker("bigBroker", brokerManager));
        brokerManager.registerBroker(bigQueueBroker.broker);
        new Task(bigQueueBroker, () -> {
            var queue = bigQueueBroker.accept(4567);
            while (true) {
                try {
                    byte[] receivedBytes = queue.receive();
                    bufferBig.write(receivedBytes, 0, receivedBytes.length);
                } catch (DisconnectedException e) {
                    if (queue.closed()) {
                        queue = bigQueueBroker.accept(4567);
                    }
                }
            }
        }).start();
    }

    @ParameterizedTest
    @MethodSource("createQueueBroker")
    void testEchoQueueServer(ConcreteQueueBroker queueBroker) throws InterruptedException {
        System.out.println("--------------------testEchoQueueServer--------------------");
        int clientCount = 5;
        int port = 1234;
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            tasks.add(() -> {
                try {
                    System.out.println("Client " + clientId + " submitted.");

                    var queue = queueBroker.connect("localhost", port);
                    assertNotNull(queue, "Queue should not be null");

                    byte[] sendBytes = new byte[255];
                    for (int j = 0; j < 255; j++) {
                        sendBytes[j] = (byte) (j + 1);
                    }
                    
                    
                    queue.send(sendBytes, 0, sendBytes.length);
                    byte[] receivedBytes = queue.receive();
                    assertArrayEquals(sendBytes, receivedBytes, "The echoed bytes do not match the sent bytes");

                    queue.close();
                    assertTrue(queue.closed(), "Queue should be closed");

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

        System.out.println("testEchoQueueServer finished successfully");
    }

    @ParameterizedTest
    @MethodSource("createQueueBroker")
    void testLargeDataTransfer(ConcreteQueueBroker queueBroker) throws InterruptedException {
        System.out.println("--------------------testLargeDataTransfer--------------------");
        int port = 4567;

        try {
            var queue = queueBroker.connect("bigBroker", port);
            assertNotNull(queue, "Queue should not be null");

            byte[] sendBytes = new byte[1024 * 5];
            for (int i = 0; i < sendBytes.length; i++) {
                sendBytes[i] = (byte) (i % 255);
            }
            queue.send(sendBytes, 0, sendBytes.length);
            while (bufferBig.size() < sendBytes.length) {
            	//Added that to be sure that the assert happens after the receive and write of the task of bigQueueBroker
            }
            assertArrayEquals(sendBytes, bufferBig.toByteArray(), "The echoed bytes do not match the sent bytes");
            queue.close();
        } catch (Exception e) {
        	System.err.println("Exception occurred: " + e.getMessage());
            fail("Exception occurred: " + e.getMessage());
        } catch (AssertionError e) {
            System.err.println("AssertionError: " + e.getMessage());
            throw e;
        } 
        System.out.println("testLargeDataTransfer finished successfully");
    }

    private static Stream<ConcreteQueueBroker> createQueueBroker() {
        var broker = new ConcreteQueueBroker(new ConcreteBroker("EchoBroker", brokerManager));
        brokerManager.registerBroker(broker.broker);
        return Stream.of(broker);
    }
}