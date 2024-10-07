package impl;

import except.DisconnectedException;
import task1.Channel;
import utils.CircularBuffer;

public class ConcreteChannel extends Channel {
	private CircularBuffer bufferIn, bufferOut;
	private boolean isDisconnected = false;
	private ConcreteChannel oppositeChannel;

	public ConcreteChannel(CircularBuffer bufferIn, CircularBuffer bufferOut) {
		this.bufferIn = bufferIn;
		this.bufferOut = bufferOut;
	}

	@Override
	public int read(byte[] bytes, int offset, int length) throws DisconnectedException {
		synchronized(bufferIn) {
			try {
				if (disconnected()) {
					throw new DisconnectedException();
				}

				while(bufferIn.empty()) {
					if (disconnected()) {
						throw new DisconnectedException();
					}
					try {
						bufferIn.wait();
					} catch (InterruptedException e) {
						disconnect();
						throw new DisconnectedException();
					}
				}
				int totalBytesRead = 0;
				while (totalBytesRead < length) {
					try {
						bytes[offset + totalBytesRead] = bufferIn.pull();
						totalBytesRead++;
					} catch (IllegalStateException e) {
						return totalBytesRead;
					}
				}
				return totalBytesRead;

			}finally {
				bufferIn.notify();
			}
		}
	}


	@Override
	public  int write(byte[] bytes, int offset, int length) throws DisconnectedException {
		synchronized(oppositeChannel.bufferIn) {
			try {
				if (disconnected()) {
					throw new DisconnectedException();
				}

				while(bufferOut.full()) {
					if (disconnected()) {
						throw new DisconnectedException();
					}
					try {
						oppositeChannel.bufferIn.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				int totalBytesWritten = 0;
				while (totalBytesWritten < length) {
					if (disconnected()) {
						throw new DisconnectedException();
					}
					try {
						bufferOut.push(bytes[offset + totalBytesWritten]);
						totalBytesWritten++;
					} catch (IllegalStateException e) {
						return totalBytesWritten;
					}
				}
				return totalBytesWritten;
			} finally {
				oppositeChannel.bufferIn.notify();
			}
		} 
	}


	@Override
	public void disconnect() {
		synchronized (bufferIn) {
			if (isDisconnected) {
				return;
			}
			isDisconnected = true;
			bufferIn.notifyAll();
		}
		synchronized (oppositeChannel.bufferIn) {
			oppositeChannel.bufferIn.notifyAll();
		}
	}

	@Override
	public boolean disconnected() {
		return isDisconnected || oppositeChannel.isDisconnected;
	}

	public void setOppositeChannel(ConcreteChannel opposite) {
		this.oppositeChannel = opposite;
	}
}
