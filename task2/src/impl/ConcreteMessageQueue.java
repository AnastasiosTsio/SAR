package impl;

import java.nio.ByteBuffer;

import abstracts.MessageQueue;
import except.DisconnectedException;
import task1.Channel;

public class ConcreteMessageQueue extends MessageQueue{
	private final Channel channel;

    public ConcreteMessageQueue(Channel channel) {
        this.channel = channel;
    }

    @Override
    public synchronized void send(byte[] bytes, int offset, int length) throws DisconnectedException {
        try {
            byte[] lengthBytes = ByteBuffer.allocate(4).putInt(length).array();
            var lengthIndex = 0;
            while (lengthIndex < 4) {
                lengthIndex += channel.write(lengthBytes, lengthIndex, 4);
            }

            var index = 0;
            while (index < length) {
                index += channel.write(bytes, offset + index, length - index);
            }

        } catch (Exception e) {
            throw new DisconnectedException();
        }
    }

    @Override
    public synchronized byte[] receive() throws DisconnectedException {
        try {
            var lengthBytes = new byte[4];
            var lengthIndex = 0;
            while (lengthIndex < 4) {
                lengthIndex += channel.read(lengthBytes, lengthIndex, 4);
            }
            int length = ByteBuffer.wrap(lengthBytes).getInt();
            var bytes = new byte[length];
            var index = 0;
            while (index < length) {
                index += channel.read(bytes, index, length - index);
            }
            return bytes;
        } catch (Exception e) {
            throw new DisconnectedException();
        }
    }

    @Override
    public void close() {
        channel.disconnect();
    }

    @Override
    public boolean closed() {
        return channel.disconnected();
    }
}

