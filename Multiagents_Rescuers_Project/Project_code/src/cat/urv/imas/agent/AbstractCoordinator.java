/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import java.util.Date;
import java.util.List;

/**
 *
 * @author maria
 */
public abstract class AbstractCoordinator extends ImasAgent {

    public List<AID> responders;

    public AbstractCoordinator(AgentType type) {
        super(type);
    }

    public List<AID> getResponders() {
        if(responders==null || responders.isEmpty())
            searchResponders();
        return responders;
    }

    public void setResponders(List<AID> responders) {
        this.responders = responders;
    }

    public abstract void searchResponders();
}
