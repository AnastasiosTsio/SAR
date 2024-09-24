package impl;
import task1.Broker;
import utils.CircularBuffer;

public class RDV {
    private Broker localBroker;
    private Broker remoteBroker;
    private int port;
    private ConcreteChannel channelForServer;
    private ConcreteChannel channelForClient;
    private boolean isServerReady = false;
    private boolean isClientReady = false;

    private final Object lock = new Object();

    private CircularBuffer buffera = new CircularBuffer(1024);
    private CircularBuffer bufferb = new CircularBuffer(1024);

    public RDV(Broker localBroker, int port) {
        this.localBroker = localBroker;
        this.port = port;
        this.channelForServer = new ConcreteChannel(buffera, bufferb);
    }

    public RDV(Broker localBroker, Broker remoteBroker, int port) {
        this.localBroker = localBroker;
        this.remoteBroker = remoteBroker;
        this.port = port;
        this.channelForClient = new ConcreteChannel(bufferb, buffera);
    }

    public ConcreteChannel getChannelForServer() {
        synchronized (lock) {
            isServerReady = true;
            
            // Check if client is already ready
            if (!isClientReady) {
                try {
                    lock.wait(); // Wait for the client to be ready
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Notify client that the server is ready
            lock.notifyAll();
            return channelForServer;
        }
    }

    public ConcreteChannel getChannelForClient() {
        synchronized (lock) {
            isClientReady = true;
            
            // Check if server is already ready
            if (!isServerReady) {
                try {
                    lock.wait(); // Wait for the server to be ready
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Notify server that the client is ready
            lock.notifyAll();
            return channelForClient;
        }
    }

}

