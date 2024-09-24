package impl;


import java.util.ArrayList;
import java.util.List;
import task1.Broker;
import task1.Channel;

public class ConcreteBroker extends Broker {
    private List<RDV> rdvs = new ArrayList<>();
    private BrokerManager brokerManager;

    public ConcreteBroker(String name, BrokerManager brokerManager) {
        super(name);
        this.brokerManager = brokerManager;
    }

    @Override
    public Channel accept(int port) {
        synchronized (rdvs) {
            RDV rdv = new RDV(this, port);
            rdvs.add(rdv);
            return rdv.getChannelForServer();
        }
    }

    @Override
    public Channel connect(String remoteBrokerName, int port) {
        Broker remoteBroker = brokerManager.getBroker(remoteBrokerName);
        if (remoteBroker == null) {
            return null;
        }

        synchronized (rdvs) {
            RDV rdv = new RDV(this, remoteBroker, port);
            rdvs.add(rdv);
            return rdv.getChannelForClient();
        }
    }
}
