/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator.rural;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.RuralAgentCoordinator;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.onthology.Proposal;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;

/**
 *
 * @author Arkard
 */
public class RuralContractNetInitiator extends CyclicBehaviour {

    private int nResponders;
    private Vector proposals;
    private State state;

    private enum State {

        WAITING_FOR_PROPOSALS,
        WAITING_FOR_INFORM,
        WAITING_FOR_AUCTION
    }

    private int getNResponders() {
        if (nResponders == 0) {
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.RURAL_AGENT.toString());
            nResponders = UtilsAgents.searchAgents(myAgent, searchCriterion).size();
        }
        return nResponders;
    }

    public RuralContractNetInitiator(Agent a, int nResponders) {
        super(a);
        this.nResponders = nResponders;
        this.proposals = new Vector();
        state = State.WAITING_FOR_PROPOSALS;
    }
    Proposal mutex;

    protected void handlePropose(ACLMessage propose, Vector v) {
        v.add(propose);
        System.out.println("RURAL COORDINATOR " + propose.getSender().getName() + " made a proposal.");
    }

    protected void handleRefuse(ACLMessage refuse) {
        proposals.add(refuse);
        System.out.println(refuse.getSender().getName() + " refuse.");
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            System.out.println("Responder does not exist.");
        } else {
            System.out.println("RURAL COORDINATOR " + failure.getSender().getName() + " failed.");
        }
    }

    protected void handleAllResponses(Vector responses) {
        if (responses.size() < nResponders) {
            System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
        } else {
            System.out.println("All rural contract net responses received.");
        }

        Proposal best = new Proposal(Double.POSITIVE_INFINITY, Proposal.RURAL_AGENT_TYPE);

        Enumeration e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                try {
                    Proposal p = (Proposal) msg.getContentObject();
                    if (p.getD1() < best.getD1()) {
                        best = p;
                        best.setAid(msg.getSender());
                    }
                } catch (UnreadableException ex) {
                    Logger.getLogger(RuralAgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (msg.getPerformative() == ACLMessage.CONFIRM) {
                try {
                    mutex = (Proposal) msg.getContentObject();
                } catch (UnreadableException ex) {
                    Logger.getLogger(RuralAgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
        }

        try {
            ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
            reply.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
            reply.setPerformative(ACLMessage.PROPOSE);
            reply.setContentObject(best);
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.COORDINATOR.toString());
            AID coord = UtilsAgents.searchAgent(myAgent, searchCriterion);
            reply.addReceiver(coord);
            myAgent.send(reply);
        } catch (IOException ex) {
            Logger.getLogger(RuralAgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }

        state = State.WAITING_FOR_AUCTION;
        mutex = best;
    }

    protected void handleInform(ACLMessage inform) {
        try {
            System.out.println("Inform from contract net received.");
            ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
            reply.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
            reply.setPerformative(ACLMessage.CONFIRM);
            reply.clearAllReceiver();
            reply.setContentObject(inform.getContentObject());
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.COORDINATOR.toString());
            reply.addReceiver(UtilsAgents.searchAgent(myAgent, searchCriterion));
            myAgent.send(reply);
        } catch (UnreadableException ex) {
            java.util.logging.Logger.getLogger(RuralContractNetInitiator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(RuralContractNetInitiator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendAllResponses(ACLMessage auctionAccept, Proposal promisingPropos, Vector responses, Vector acceptances, boolean won) {
        try {
            mutex = (Proposal) auctionAccept.getContentObject();
        } catch (UnreadableException ex) {

        }
        System.out.println("Sending all rural contractnet responses");
        if (mutex.compareTo(promisingPropos) == 0) {
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                try {
                    ACLMessage msgResp = (ACLMessage) e.nextElement();

                    ACLMessage reply = msgResp.createReply();
                    reply.clearAllReceiver();
                    reply.addReceiver(msgResp.getSender());

                    Proposal origPro = (Proposal) msgResp.getContentObject();
                    reply.setContentObject(origPro);
                    if (won && msgResp.getSender().getName().equals(mutex.getAid().getName()) && origPro.compareTo(mutex) == 0) {
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    } else {
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    }
                    acceptances.addElement(reply);
                } catch (UnreadableException ex) {
                    Logger.getLogger(RuralAgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(RuralContractNetInitiator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        for (Object m : acceptances) {
            myAgent.send((ACLMessage) m);
        }
        proposals = new Vector();
        state = State.WAITING_FOR_INFORM;

    }

    @Override
    public void action() {
        if (state == State.WAITING_FOR_AUCTION) {
            ACLMessage msg = myAgent.receive(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION));
            if (msg != null) {
                int perf = msg.getPerformative();
                if (perf == ACLMessage.ACCEPT_PROPOSAL) {
                    sendAllResponses(msg, mutex, proposals, new Vector(), true);
                } else if (perf == ACLMessage.REJECT_PROPOSAL) {
                    sendAllResponses(msg, mutex, proposals, new Vector(), false);
                }

            }
        } else {
            ACLMessage msg = myAgent.receive(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET));

            if (msg != null) {
                switch (msg.getPerformative()) {
                    case ACLMessage.PROPOSE:
                        handlePropose(msg, proposals);
                        break;
                    case ACLMessage.REFUSE:
                        handleRefuse(msg);
                        break;
                    case ACLMessage.FAILURE:
                        handleFailure(msg);
                        state = State.WAITING_FOR_PROPOSALS;
                        break;
                    case ACLMessage.INFORM:
                        handleInform(msg);
                        break;
                }
                if (proposals.size() == getNResponders() && (msg.getPerformative() == ACLMessage.PROPOSE || msg.getPerformative() == ACLMessage.REFUSE )) {
                    handleAllResponses(proposals);
                }
            }
        }
        //block();
    }

}
