package task1;

import except.DisconnectedException;

public abstract class Channel {
	public abstract int read(byte[] bytes, int offset, int length) throws DisconnectedException;
	public abstract int write(byte[] bytes, int offset, int length) throws DisconnectedException;
	public abstract void disconnect();
	public abstract boolean disconnected();
}
