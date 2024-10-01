package task1;

public class Task extends Thread{
	Broker broker;
	
	public Task(Broker b, Runnable r){
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
