/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.map.InjuredPerson;
import cat.urv.imas.onthology.GameSettings;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maria
 */
public class ReceiveSettingsBehaviour extends CyclicBehaviour {

    private HashMap<InjuredPerson, AID> assignedRescuers;

    private int findIp() {
        CoordinatorAgent agent = ((CoordinatorAgent) myAgent);
        for (int i = currentIp + 1; i < agent.getGame().getInjuredPeople().size(); i++) {
            if (!agent.getGame().getInjuredPeople().get(i).isAsignedRescuer()) {
                return i;
            }
        }
        return -1;
    }

    private void createAuction() {
        try {
            System.out.println("Starting new auction");
            InjuredPerson ip = ((CoordinatorAgent) myAgent).getGame().getInjuredPeople().get(currentIp);
            ACLMessage announcement = new ACLMessage(ACLMessage.CFP);
            announcement.setContentObject(ip);
            announcement.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.HELICOPTER_AGENT_COORDINATOR.toString());
            AID hcoord = UtilsAgents.searchAgent(myAgent, searchCriterion);
            announcement.addReceiver(hcoord);
            searchCriterion.setType(AgentType.RURAL_AGENT_COORDINATOR.toString());
            AID racoord = UtilsAgents.searchAgent(myAgent, searchCriterion);
            announcement.addReceiver(racoord);
            announcement.setConversationId(Integer.toString(ip.hashCode()));
            //Create an announcement
            myAgent.send(announcement);
        } catch (IOException ex) {
            Logger.getLogger(ReceiveSettingsBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void noMoreIP() {
        try {
            System.out.println("No more ip in this step");
            state = State.WAITING_FOR_SETTINGS;
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            //reply.setContent("Next step please");
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.SYSTEM.toString());
            AID system = UtilsAgents.searchAgent(myAgent, searchCriterion);
            reply.setContentObject(assignedRescuers);
            reply.addReceiver(system);
            myAgent.send(reply);
            assignedRescuers = new HashMap<>();
        } catch (IOException ex) {
            Logger.getLogger(ReceiveSettingsBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private enum State {

        WAITING_FOR_SETTINGS, WAITING_FOR_AUCTION
    };

    private State state;
    private MessageTemplate mt;
    private int currentIp;

    public ReceiveSettingsBehaviour(Agent a, MessageTemplate mt) {
        super(a);
        this.mt = mt;
        currentIp = -1;
        state = State.WAITING_FOR_SETTINGS;
        assignedRescuers = new HashMap<>();
    }

    @Override
    public void action() {
        CoordinatorAgent agent = ((CoordinatorAgent) myAgent);
        if (state == State.WAITING_FOR_SETTINGS) {
            ACLMessage inform = myAgent.receive(mt);
            if (inform != null) {
                try {
                    GameSettings game = (GameSettings) inform.getContentObject();
                    agent.setGame(game);
                    agent.log(game.getShortString());
                    if (game.getInjuredPeople().isEmpty()) {
                        ACLMessage reply = inform.createReply();
                        reply.setContentObject(assignedRescuers);

                        myAgent.send(reply);
                        assignedRescuers = new HashMap<>();

                    } else {
                        currentIp = findIp(); //First auction
                        if (currentIp == -1) {
                            noMoreIP();
                            return;
                        } else {
                            state = State.WAITING_FOR_AUCTION;
                            createAuction();
                        }
                    }
                } catch (UnreadableException ex) {
                    Logger.getLogger(ReceiveSettingsBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ReceiveSettingsBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (state == State.WAITING_FOR_AUCTION) {
            ACLMessage auctionDone = myAgent.receive(MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_QUERY),
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)));
            if (auctionDone != null) {
                try {
                    if (auctionDone.getContentObject()!= null) {
                        assignedRescuers.put(agent.getGame().getInjuredPeople().get(currentIp), (AID) auctionDone.getContentObject());
                    }
                } catch (UnreadableException ex) {
                    //Logger.getLogger(ReceiveSettingsBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentIp = findIp();
                if (currentIp == -1) {
                    noMoreIP();

                } else {
                    createAuction();
                }
            }

        }
        block();
    }
}
