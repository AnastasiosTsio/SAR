package impl;

import abstracts.EventQueueBroker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import abstracts.EventMessageQueue;
import abstracts.MessageQueue;
import abstracts.Task;
import utils.EventPump;

public class ConcreteEventQueueBroker extends EventQueueBroker{

	private ConcreteQueueBroker broker;
	private EventPump eventPump; 
	private Map<Integer, AcceptListener> portBindings;
	
	public ConcreteEventQueueBroker(String name, BrokerManager bk) {
		super(name);
		this.broker = new ConcreteQueueBroker(new ConcreteBroker(name,bk));
		this.eventPump = EventPump.getInstance();
		this.portBindings = new ConcurrentHashMap<>();
	}

	@Override
	public boolean bind(int port, AcceptListener listener) {
		
		if (portBindings.containsKey(port)) {
            return false;
        }
        portBindings.put(port, listener);
        
		new Thread(() -> {
            while (true) {
                try {
                    MessageQueue msgsQueue = broker.accept(port);
                    EventMessageQueue queue = new ConcreteEventMessageQueue(msgsQueue);
                    eventPump.post(new Task(() -> listener.accepted(queue)));
                } catch (Exception e) {
                    break;
                }
            }
        }).start();
		return true;
	}

	@Override
	public boolean unbind(int port) {
		if (!portBindings.containsKey(port)) {
            return false;
        }
        portBindings.remove(port);
        return true;
	}

	@Override
	public boolean connect(String name, int port, ConnectListener listener) {
		new Thread(() -> {
            try {
                MessageQueue msgsQueue = broker.connect(name, port);
                EventMessageQueue queue = new ConcreteEventMessageQueue(msgsQueue);
                eventPump.post(new Task(() -> listener.connected(queue)));
            } catch (Exception e) {
                eventPump.post(new Task(listener::refused));
            }
        }).start();
        return true;
	}

}
