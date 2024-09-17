package task1;

public abstract class Broker {
	String name;
	public Broker(String name){
		this.name = name;
	}
	abstract Channel accept(int port);
	abstract Channel connect(String name, int port);
}
