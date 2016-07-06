/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.mobileAgents;

import cat.urv.imas.agent.RescuingAgentInterface;
import cat.urv.imas.behaviour.mobileAgents.helicopter.HelicopterStepBehaviour;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maria
 */
public class MobileAgentLocationBehaviour extends CyclicBehaviour {

    public MobileAgentLocationBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        ACLMessage askForLocation = myAgent.receive(MessageTemplate.MatchContent("LOCATION"));
        if (askForLocation != null) {
            try {
                ACLMessage reply = askForLocation.createReply();
                reply.setPerformative(ACLMessage.UNKNOWN);
                int[] current = ((RescuingAgentInterface) myAgent).getCurrentLocation();
                int[] old = ((RescuingAgentInterface) myAgent).getPreviousLocation();
                int[] both = new int[4];
                both[0] = old[0];
                both[1] = old[1];
                both[2] = current[0];
                both[3] = current[1];

                reply.setContentObject(both);
                myAgent.send(reply);
            } catch (IOException ex) {
                Logger.getLogger(HelicopterStepBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    block();
    }

}
