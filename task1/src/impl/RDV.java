package impl;
import utils.CircularBuffer;

public class RDV {
    private ConcreteChannel channelForServer;
    private ConcreteChannel channelForClient;
    private boolean isServerReady = false;
    private boolean isClientReady = false;

    private final Object lock = new Object();

    private CircularBuffer buffera = new CircularBuffer(1024);
    private CircularBuffer bufferb = new CircularBuffer(1024);

    public RDV() {
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
            
            createChannels();
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
            createChannels();
            return channelForClient;
        }
    }
    
    private void createChannels() {
        if (channelForServer == null && channelForClient == null) {
            channelForServer = new ConcreteChannel(buffera, bufferb);
            channelForClient = new ConcreteChannel(bufferb, buffera);

            channelForServer.setOppositeChannel(channelForClient);
            channelForClient.setOppositeChannel(channelForServer);
        }
        lock.notifyAll();
    }
    
    public boolean isReady() {
        return isServerReady && isClientReady;
    }

}

