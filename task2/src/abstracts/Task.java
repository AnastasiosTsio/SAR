package abstracts;

import task1.Broker;

public class Task extends Thread {
	Broker broker;
	QueueBroker queueBroker;
	
	public Task(Broker b, Runnable r){
		super(r);
		this.broker = b;
	}
	public Task(QueueBroker b, Runnable r){
		super(r);
		this.queueBroker = b;
	}
	
	static Broker getBroker() throws Exception {
		Thread currentThread = Thread.currentThread();
		if(currentThread instanceof Task) {
			return ((Task)currentThread).broker;
		}
		else {
			throw new Exception("Thread is not a Task");
		}
	}
	static QueueBroker getQueueBroker() throws Exception {
		Thread currentThread = Thread.currentThread();
		if(currentThread instanceof Task) {
			return ((Task)currentThread).queueBroker;
		}
		else {
			throw new Exception("Thread is not a Task");
		}
	}
}
