package impl;

import abstracts.MessageQueue;
import abstracts.QueueBroker;
import task1.Broker;

public class ConcreteQueueBroker extends QueueBroker {
    public ConcreteQueueBroker(Broker broker) {
        super(broker);
    }

    @Override
    public String name() {
        return broker.name;
    }

    @Override
    public MessageQueue accept(int port){
        return new ConcreteMessageQueue(broker.accept(port));
    }

    @Override
    public MessageQueue connect(String name, int port){
        return new ConcreteMessageQueue(broker.connect(name, port));
    }
}
