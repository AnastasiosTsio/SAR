package abstracts;

import task1.Broker;

public abstract class Task extends Thread {
	Broker broker;
	QueueBroker queueBroker;
	
	Task(Broker b, Runnable r){
		super(r);
		this.broker = b;
	}
	Task(QueueBroker b, Runnable r){
		super(r);
		this.queueBroker = b;
	}
	
	abstract Broker getBroker();
	abstract QueueBroker getQueueBroker();
	//static Task getTask(); TODO
}
