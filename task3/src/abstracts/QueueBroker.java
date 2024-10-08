package abstracts;

public abstract class QueueBroker {
	public final String name;

    QueueBroker (String name) {
        this.name = name;
    }

    public interface AcceptListener {
        void accepted(MessageQueue queue);
    }

    public abstract boolean bind(int port, AcceptListener listener);
    public abstract boolean unbind(int port);

    public interface ConnectListener {
        void connected(MessageQueue queue);
        void refused();
    }

    public abstract boolean connect(String name, int port, ConnectListener listener);
}
