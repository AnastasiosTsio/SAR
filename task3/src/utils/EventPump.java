package utils;

import java.util.LinkedList;

import java.util.List;

public class EventPump {
	List<Runnable> queue;
	
	public EventPump() {
		queue = new LinkedList<Runnable>();
	}
	public synchronized void run() {
		Runnable r;
		while(true) {
			r = queue.remove(0);
			while (r!=null) {
				r.run();
				r = queue.remove(0);
			}
			sleep();
		}
	}
	public synchronized void post(Runnable r) {
		queue.add(r); // at the end…
		notify();
	}
	private void sleep() {
		try {
			wait();
		} catch (InterruptedException ex){
			// nothing to do here.
		}
	}
}
