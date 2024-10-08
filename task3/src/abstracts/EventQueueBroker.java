package abstracts;

public abstract class EventQueueBroker {
	public final String name;

    public EventQueueBroker (String name) {
        this.name = name;
    }

    public interface AcceptListener {
        void accepted(EventMessageQueue queue);
    }

    public abstract boolean bind(int port, AcceptListener listener);
    public abstract boolean unbind(int port);

    public interface ConnectListener {
        void connected(EventMessageQueue queue);
        void refused();
    }

    public abstract boolean connect(String name, int port, ConnectListener listener);
}
