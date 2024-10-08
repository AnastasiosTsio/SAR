package abstracts;

import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import utils.Message;


class Tests {

    private static EventQueueBroker echoQueueBroker;
    private static EventQueueBroker bigQueueBroker;
    private static ByteArrayOutputStream bufferBig = new ByteArrayOutputStream();
    
    @BeforeAll
    static void setUpServer() {
        echoQueueBroker = new EventQueueBroker("localhost") {
            @Override
            public boolean bind(int port, AcceptListener listener) {
                return true;
            }

            @Override
            public boolean unbind(int port) {
                return true;
            }

            @Override
            public boolean connect(String name, int port, ConnectListener listener) {
                return true;
            }
        };
        echoQueueBroker.bind(1234, (queue) -> {
            queue.setListener(new EventMessageQueue.Listener() {
                @Override
                public void received(byte[] msg) {
                    queue.send(new Message(msg, 0, msg.length));
                }

                @Override
                public void sent(Message msg) {
                }

                @Override
                public void closed() {
                }
            });
        });

        
        
        bigQueueBroker = new EventQueueBroker("bigBroker") {
            @Override
            public boolean bind(int port, AcceptListener listener) {
                return true;
            }

            @Override
            public boolean unbind(int port) {
                return true;
            }

            @Override
            public boolean connect(String name, int port, ConnectListener listener) {
                return true;
            }
        };
        bigQueueBroker.bind(4567, (queue) -> {
            queue.setListener(new EventMessageQueue.Listener() {
                @Override
                public void received(byte[] msg) {
                    try {
                        bufferBig.write(msg);
                    } catch (Exception e) {
                        fail("Error while writing to buffer: " + e.getMessage());
                    }
                }

                @Override
                public void sent(Message msg) {
                }

                @Override
                public void closed() {
                }
            });
        });
    }

    @ParameterizedTest
    @MethodSource("createQueueBroker")
    void testEchoQueueServer(EventQueueBroker queueBroker) throws InterruptedException {
        System.out.println("--------------------testEchoQueueServer--------------------");
        int clientCount = 5;
        int port = 1234;
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);

        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            executor.submit(() -> {
                queueBroker.connect("localhost", port, new EventQueueBroker.ConnectListener() {
                    @Override
                    public void connected(EventMessageQueue queue) {
                        byte[] sendBytes = new byte[255];
                        for (int j = 0; j < 255; j++) {
                            sendBytes[j] = (byte) (j + 1);
                        }
                        queue.setListener(new EventMessageQueue.Listener() {
                            @Override
                            public void received(byte[] msg) {
                            }

                            @Override
                            public void sent(Message msg) {
                            }

                            @Override
                            public void closed() {
                            }
                        });
                        queue.send(new Message(sendBytes, 0, sendBytes.length));
                    }

                    @Override
                    public void refused() {
                        fail("Connection refused for client " + clientId);
                    }
                });
            });
        }

        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            System.out.println("Some tasks did not finish in time.");
        }

        System.out.println("testEchoQueueServer finished successfully");
    }

    @ParameterizedTest
    @MethodSource("createQueueBroker")
    void testLargeDataTransfer(EventQueueBroker queueBroker) throws InterruptedException {
        System.out.println("--------------------testLargeDataTransfer--------------------");
        int port = 4567;

        queueBroker.connect("bigBroker", port, new EventQueueBroker.ConnectListener() {
            @Override
            public void connected(EventMessageQueue queue) {
                byte[] sendBytes = new byte[1024 * 5];
                for (int i = 0; i < sendBytes.length; i++) {
                    sendBytes[i] = (byte) (i % 255);
                }
                queue.setListener(new EventMessageQueue.Listener() {
                    @Override
                    public void received(byte[] msg) {
                        assertArrayEquals(sendBytes, msg, "The large data transfer does not match the sent data");
                        queue.close();
                    }

                    @Override
                    public void sent(Message msg) {
                    }

                    @Override
                    public void closed() {
                    }
                });
                queue.send(new Message(sendBytes, 0, sendBytes.length));
            }

            @Override
            public void refused() {
                fail("Connection refused for large data transfer");
            }
        });

        System.out.println("testLargeDataTransfer finished successfully");
    }

    private static Stream<EventQueueBroker> createQueueBroker() {
    	var broker = new EventQueueBroker("EchoBroker") {
            @Override
            public boolean bind(int port, AcceptListener listener) {
                return true;
            }

            @Override
            public boolean unbind(int port) {
                return true;
            }

            @Override
            public boolean connect(String name, int port, ConnectListener listener) {
                return true;
            }
        };
        return Stream.of(broker);
    }
}
