package abstracts;

import utils.Message;

public abstract class MessageQueue {
	interface Listener {
        void received(byte[] msg);
        void sent(Message msg);
        void closed();
    }

    public abstract void setListener(Listener l);

    public abstract boolean send(Message msg);
    public abstract void close();
    public abstract boolean closed();
}
