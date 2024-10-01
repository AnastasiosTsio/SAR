package abstracts;

import task1.Broker;

public abstract class QueueBroker {
	public Broker broker;
	public QueueBroker(Broker broker){
		this.broker = broker;
	}
	abstract String name();
	abstract MessageQueue accept(int port);
	abstract MessageQueue connect(String name, int port);
}
