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
        
        int bytesRead = 0;
        try {
            for (int i = 0; i < length; i++) {
                bytes[offset + i] = bufferIn.pull();
                bytesRead++;
            }
        } catch (IllegalStateException e) {
            if (disconnected()) {
                throw new DisconnectedException(); // Handle disconnection during read
            }
        }
        return bytesRead;
    }

    @Override
    public synchronized int write(byte[] bytes, int offset, int length) throws DisconnectedException {
        if (disconnected()) {
            throw new DisconnectedException();
        }
        
        int bytesWritten = 0;
        try {
            for (int i = 0; i < length; i++) {
                bufferOut.push(bytes[offset + i]);
                bytesWritten++;
            }
        } catch (IllegalStateException e) {
            if (disconnected()) {
                throw new DisconnectedException(); // Handle disconnection during write
            }
        }
        return bytesWritten;
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
