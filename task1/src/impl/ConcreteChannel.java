package impl;

import except.DisconnectedException;
import task1.Channel;
import utils.CircularBuffer;

public class ConcreteChannel extends Channel {
    private CircularBuffer bufferIn, bufferOut;
    private boolean isDisconnected = false;

    public ConcreteChannel(CircularBuffer bufferIn, CircularBuffer bufferOut) {
        this.bufferIn = bufferIn;
        this.bufferOut = bufferOut;
    }

    @Override
    public synchronized int read(byte[] bytes, int offset, int length) throws DisconnectedException {
        if (disconnected()) {
            throw new DisconnectedException();
        }

        int totalBytesRead = 0;
        try {
            while (totalBytesRead < length) {
                try {
                    bytes[offset + totalBytesRead] = bufferIn.pull();
                    totalBytesRead++;
                } catch (IllegalStateException e) {
                    wait(10);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return totalBytesRead;
    }


    @Override
    public synchronized int write(byte[] bytes, int offset, int length) throws DisconnectedException {
        if (disconnected()) {
            throw new DisconnectedException();
        }
        
        int totalBytesWritten = 0;
        try {
            while (totalBytesWritten < length) {
                if (disconnected()) {
                    throw new DisconnectedException();
                }
                try {
                    bufferOut.push(bytes[offset + totalBytesWritten]);
                    totalBytesWritten++;
                } catch (IllegalStateException e) {
                    wait(10);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return totalBytesWritten;
    }



    @Override
    public void disconnect() {
        isDisconnected = true;
    }

    @Override
    public boolean disconnected() {
        return isDisconnected;
    }
}
