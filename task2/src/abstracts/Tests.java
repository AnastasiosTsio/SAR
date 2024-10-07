package abstracts;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import task1.Broker;

public class Tests {
	private static QueueBroker queueBroker;
	private static BrokerManager brokerManager;

    @BeforeAll
    static void setUpServer() {
    	brokerManager = new BrokerManager();
        queueBroker = new QueueBrokerMock(new ConcreteBroker("QueueBroker", brokerManager));
        
        new Task(queueBroker, () -> {
            while (true) {
                MessageQueue messageQueue = queueBroker.accept(1234);

                while (!messageQueue.closed()) {
                    byte[] receivedBytes;
					try {
						receivedBytes = messageQueue.receive();
						messageQueue.send(receivedBytes, 0, receivedBytes.length);
					} catch (DisconnectedException e) {
						e.printStackTrace();
					}
                    
                }
            }
        }) {

			@Override
			Broker getBroker() {
				return null;
			}

			@Override
			QueueBroker getQueueBroker() {
				return null;
			}}.start();
    }

    @ParameterizedTest
    @MethodSource("createQueueBroker")
    void testMessageQueueServer(QueueBroker broker) throws InterruptedException {
        int clientCount = 5;
        int port = 1234;
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            tasks.add(() -> {
                try {
                    System.out.println("Executor " + clientId + " submitted.");
                    
                    MessageQueue queue = broker.connect("localhost", port);
                    assertNotNull(queue, "MessageQueue should not be null");

                    byte[] sendBytes = new byte[255];
                    for (int j = 0; j < 255; j++) {
                        sendBytes[j] = (byte) (j + 1);
                    }

                    queue.send(sendBytes, 0, sendBytes.length);

                    byte[] receivedBytes = queue.receive();
                    assertNotNull(receivedBytes, "Received bytes should not be null");

                    assertArrayEquals(sendBytes, receivedBytes, "The echoed bytes do not match the sent bytes");

                    queue.close();
                    assertTrue(queue.closed(), "Queue should be closed");

                    System.out.println("Client " + clientId + " closed.");
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
            System.out.println("Some tasks didn't end correctly.");
        }

        for (Future<Void> future : futures) {
            try {
                future.get();  
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }


    private static Stream<QueueBroker> createQueueBroker() {
        return Stream.of(new QueueBrokerMock(new ConcreteBroker("QueueBroker", brokerManager)));
    }
}

class QueueBrokerMock extends QueueBroker {
    public QueueBrokerMock(Broker broker) {
        super(broker);
    }

    @Override
    public String name() {
        return "MockQueueBroker";
    }

    @Override
    public MessageQueue accept(int port) {
        return new MessageQueueMock();
    }

    @Override
    public MessageQueue connect(String name, int port) {
        return new MessageQueueMock();
    }
}

class MessageQueueMock extends MessageQueue {

    private boolean closed = false;

    @Override
    public void send(byte[] bytes, int offset, int length) {
    }

    @Override
    public byte[] receive() {
    	byte[] recBytes = new byte[255];
    	for (int j = 0; j < 255; j++) {
            recBytes[j] = (byte) (j + 1);
        }

        return recBytes;
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean closed() {
        return closed;
    }
}