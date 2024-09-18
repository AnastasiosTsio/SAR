package task1;

public abstract class Task extends Thread{
	Broker broker;
	
	Task(Broker b, Runnable r){
		super(r);
		this.broker = b;
	}
	
	static Broker getBroker() {
		Thread currentThread = Thread.currentThread();
		Broker b = null;
		if(currentThread instanceof Task) {
			b = ((Task)currentThread).broker;
		}
		return b;
	}
}
