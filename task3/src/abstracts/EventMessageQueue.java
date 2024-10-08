package abstracts;

import utils.Message;

public abstract class EventMessageQueue {
	public interface Listener {
        public void received(byte[] msg);
        public void sent(Message msg);
        public void closed();
    }

    public abstract void setListener(Listener l);

    public abstract boolean send(Message msg);
    public abstract void close();
    public abstract boolean closed();
}
