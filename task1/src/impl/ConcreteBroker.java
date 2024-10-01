package impl;

import java.util.HashMap;
import java.util.Map;

import task1.Broker;
import task1.Channel;

public class ConcreteBroker extends Broker {
    private final Map<Integer, RDV> rdvMap = new HashMap<>();
    private final BrokerManager brokerManager;

    public ConcreteBroker(String name, BrokerManager brokerManager) {
        super(name);
        this.brokerManager = brokerManager;
    }

    @Override
    public Channel accept(int port) {
        synchronized (rdvMap) {
            RDV rdv = rdvMap.get(port);
            if (rdv == null) {
                rdv = new RDV(this, port);
                rdvMap.put(port, rdv);
            }
            return rdv.getChannelForServer();
        }
    }

    @Override
    public Channel connect(String remoteBrokerName, int port) {
        Broker remoteBroker = brokerManager.getBroker(remoteBrokerName);
        if (remoteBroker == null) {
            return null;
        }

        synchronized (remoteBroker) {
            RDV rdv = ((ConcreteBroker) remoteBroker).getRdvForPort(port);
            if (rdv == null) {
                rdv = new RDV(remoteBroker, this, port);
                ((ConcreteBroker) remoteBroker).registerRdv(port, rdv);
            }
            return rdv.getChannelForClient();
        }
    }

    public RDV getRdvForPort(int port) {
        return rdvMap.get(port);
    }

    public void registerRdv(int port, RDV rdv) {
        rdvMap.put(port, rdv);
    }
}