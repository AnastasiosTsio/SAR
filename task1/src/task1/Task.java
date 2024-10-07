package task1;

public class Task extends Thread{
	Broker broker;
	
	public Task(Broker b, Runnable r){
		super(r);
		this.broker = b;
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
}
