package abstracts;

public abstract class MessageQueue {
	abstract void send(byte[] bytes, int offset, int length);
	abstract byte[] receive();
	abstract void close();
	abstract boolean closed();
}
