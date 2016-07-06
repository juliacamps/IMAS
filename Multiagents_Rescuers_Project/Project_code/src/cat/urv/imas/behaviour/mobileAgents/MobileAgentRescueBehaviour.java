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
 * @author Arkard
 */
public class MobileAgentRescueBehaviour extends CyclicBehaviour {

    public MobileAgentRescueBehaviour(Agent a) {
        super(a);
    }

    @Override
    public void action() {
        ACLMessage askForRescue = myAgent.receive(MessageTemplate.MatchContent("RESCUING"));
        if (askForRescue != null) {
            try {
                ACLMessage reply = askForRescue.createReply();
                reply.setPerformative(99);
                boolean r = ((RescuingAgentInterface) myAgent).rescueCompleted();
                if (r) {
                    reply.setContentObject(((RescuingAgentInterface) myAgent).getObjective());
                } else {
                    reply.setContentObject(null);
                }
                myAgent.send(reply);
            } catch (IOException ex) {
                Logger.getLogger(HelicopterStepBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //block();
    }

}
