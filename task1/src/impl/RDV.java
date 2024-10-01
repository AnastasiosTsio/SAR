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
    }

    public RDV(Broker localBroker, Broker remoteBroker, int port) {
        this.localBroker = localBroker;
        this.remoteBroker = remoteBroker;
        this.port = port;
    }

    public ConcreteChannel getChannelForServer() {
        synchronized (lock) {
            isServerReady = true;
            
            while (!isClientReady) {
                try {
                    lock.wait(); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (channelForServer == null) {
                channelForServer = new ConcreteChannel(buffera, bufferb); 
            }

            lock.notifyAll();

            return channelForServer;
        }
    }

    public ConcreteChannel getChannelForClient() {
        synchronized (lock) {
            isClientReady = true;

            while (!isServerReady) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (channelForClient == null) {
                channelForClient = new ConcreteChannel(bufferb, buffera);
            }
            lock.notifyAll();

            return channelForClient;
        }
    }
}

