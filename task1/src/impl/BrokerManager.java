package impl;

import java.util.HashMap;
import java.util.Map;

import task1.Broker;

public class BrokerManager {
    private Map<String, Broker> brokers = new HashMap<>();

    public Broker getBroker(String name) {
        return brokers.get(name);
    }

    public void registerBroker(Broker broker) {
        brokers.put(broker.name, broker);
    }
}
