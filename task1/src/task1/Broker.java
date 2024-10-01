package task1;

public abstract class Broker {
	public String name;
	public Broker(String name){
		this.name = name;
	}
	public abstract Channel accept(int port);
	public abstract Channel connect(String name, int port);
}
