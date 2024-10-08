package impl;

import abstracts.EventMessageQueue;
import abstracts.MessageQueue;
import abstracts.Task;
import except.DisconnectedException;
import utils.EventPump;
import utils.Message;

public class ConcreteEventMessageQueue extends EventMessageQueue {
	
	private MessageQueue messageQueue;
	private Listener listener;
	private EventPump eventPump; 
	
	public ConcreteEventMessageQueue(MessageQueue messageQueue) {
		this.messageQueue = messageQueue;
		this.eventPump = EventPump.getInstance();
	}

	@Override
	public void setListener(Listener l) {
		this.listener = l;
	}

	@Override
	public boolean send(Message msg) {
		new Thread(() -> {
			eventPump.post(new Task(() -> {
				try {
					messageQueue.send(msg.bytes, msg.offset, msg.length);
					eventPump.post(new Task(() -> listener.sent(msg)));
				} catch (DisconnectedException e) {
					eventPump.post(new Task(() -> listener.closed()));
				}
			}));
        }).start();
		return true;
	}

	@Override
	public void close() {
		new Thread(() -> {
			messageQueue.close();
			eventPump.post(new Task(() -> listener.closed()));
        }).start();
	}
	
	@Override
	public boolean closed() {
		return messageQueue.closed();
	}
	
	public boolean received() {
		new Thread(() -> {
			eventPump.post(new Task(() -> {
				byte[] msg;
				try {
					msg = messageQueue.receive();
					eventPump.post(new Task(() -> listener.received(msg)));
				} catch (DisconnectedException e) {
					eventPump.post(new Task(() -> listener.closed()));
				}
			}));
        }).start();
		return true;
	}

}
