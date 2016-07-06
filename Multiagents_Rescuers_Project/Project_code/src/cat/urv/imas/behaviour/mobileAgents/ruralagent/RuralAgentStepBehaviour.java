/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.mobileAgents.ruralagent;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.HelicopterAgent;
import cat.urv.imas.agent.RescuingAgentInterface;
import cat.urv.imas.agent.RuralAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.behaviour.mobileAgents.helicopter.HelicopterStepBehaviour;
import cat.urv.imas.map.InjuredPerson;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arkard
 */
public class RuralAgentStepBehaviour extends CyclicBehaviour {

    private MessageTemplate template;

    public RuralAgentStepBehaviour(RuralAgent ra) {
        this.setAgent(ra);
        template = MessageTemplate.MatchContent(STATE_READY);
    }

    @Override
    public void action() {
        RuralAgent ra = (RuralAgent) myAgent;
        ACLMessage msg = ra.receive(template);
        if (msg != null) {
            switch (ra.getCurrentState()) {
                case IDLE:
                    // Empty
                    break;

                case DUTY:
                    switch (ra.checkPosition()) {
                        case IS_EMPTY:
                            ra.move();
                            break;

                        case IS_AVALANCHE:
                            // TO-DO
                            break;

                        case IS_OBJECTIVE:
                            ra.rescue();
                            break;
                            
                        case UNKNOWN:
                            ra.updateDirection();
                            ra.move();
                            break;
                    }
                    break;

                case BACK:
                    switch (ra.checkPosition()) {
                        case IS_EMPTY:
                            ra.move();
                            break;

                        case IS_AVALANCHE:
                            // TO-DO
                            break;

                        case IS_MOUNTAIN_HUT:
                            ra.move();
                            try {
                                ACLMessage reply = new ACLMessage(42);
                                reply.setPerformative(42);
                                reply.setContentObject(((RescuingAgentInterface) myAgent).getObjective());
                                ServiceDescription searchCriterion = new ServiceDescription();
                                searchCriterion.setType(AgentType.SYSTEM.toString());
                                AID system = UtilsAgents.searchAgent(myAgent, searchCriterion);
                                reply.addReceiver(system);
                                ((RuralAgent) myAgent).setCurrentState(RuralAgent.State.IDLE);
                                ((RescuingAgentInterface) myAgent).setObjective(null);

                                myAgent.send(reply);
                            } catch (IOException ex) {
                                Logger.getLogger(HelicopterStepBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            break;
                    }
                    break;
            }
        }

        block();
    }
}
