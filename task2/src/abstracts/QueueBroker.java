package abstracts;

import task1.Broker;

public abstract class QueueBroker {
	public Broker broker;
	public QueueBroker(Broker broker){
		this.broker = broker;
	}
	public abstract String name();
	public abstract MessageQueue accept(int port);
	public abstract MessageQueue connect(String name, int port);
}
