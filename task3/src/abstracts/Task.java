package abstracts;


public class Task extends Thread{
	
	public Task(Runnable r){
		super(r);
	}
}
