/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator.helicopter;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.HelicopterAgentCoordinator;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maria
 */
public class HelicopterContractNetInitiator extends CyclicBehaviour {

    private int nResponders;
    private Vector proposals;
    private State state;

    private int getNResponders() {
        if (nResponders == 0) {
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.HELICOPTER.toString());
            nResponders = UtilsAgents.searchAgents(myAgent, searchCriterion).size();

        }
        return nResponders;
    }

    private enum State {

        WAITING_FOR_PROPOSALS,
        WAITING_FOR_INFORM,
        WAITING_FOR_AUCTION
    }

    public HelicopterContractNetInitiator(Agent a, int nResponders) {
        super(a);
        this.nResponders = nResponders;
        this.proposals = new Vector();
        state = State.WAITING_FOR_INFORM;
    }
    Proposal mutex;

    //final InjuredPerson ip = (InjuredPerson) msg.getContentObject();
    protected void handlePropose(ACLMessage propose, Vector v) {
        v.add(propose);
        System.out.println("HELICOPTER COORDINATOR " + propose.getSender().getName() + " made a proposal");
    }

    protected void handleRefuse(ACLMessage refuse) {
        proposals.add(refuse);
        System.out.println(refuse.getSender().getName() + " refused");
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            System.out.println("Responder does not exist");
        } else {
            System.out.println("HELICOPTER COORDINATOR " + failure.getSender().getName() + " failed");
        }
        // Immediate failure --> we will not receive a response from this agent
        //nResponders--;
    }

    protected void handleAllResponses(Vector responses) {
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
        }
        // Evaluate proposals.
        //int bestProposal = -1;
        //AID bestProposer = null;
        //ACLMessage accept = null;
        System.out.println("All helicopter contract net responses received");
        Proposal promisingPropos = null;
        ArrayList<Proposal> bhp = new ArrayList<>();
        int cases = -1;

        Enumeration e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msgResp = (ACLMessage) e.nextElement();
            if (msgResp.getPerformative() == ACLMessage.PROPOSE) {

                Proposal proDis;
                try {
                    //Obtaining proposal of one Helicopter Agent
                    proDis = (Proposal) msgResp.getContentObject();

                    if (proDis.getLength() == 1) {
                        if (proDis.getD1() < proDis.getIp().getStepToDie()
                                && (promisingPropos == null
                                || proDis.getD1() < promisingPropos.getD1())) {

                            promisingPropos = proDis;//proDis.getD1();
                            promisingPropos.setAid(msgResp.getSender());
                            //promisingha = ha;
                            cases = 0;
                        }
                    } else if (proDis.getLength() == 2) {
                        proDis.setAid(msgResp.getSender());
                        bhp.add(proDis);//new BusyHelicoptersProposals(proDis,ha));
                    } else {
                        System.err.println("ERROR proDis has inconsistent length: " + proDis.getLength());
                    }

                } catch (UnreadableException ex) {
                    Logger.getLogger(HelicopterAgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (msgResp.getPerformative() == ACLMessage.CONFIRM) {
                try {
                    mutex = (Proposal) msgResp.getContentObject();
                } catch (UnreadableException ex) {
                    Logger.getLogger(HelicopterAgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            }
        }
        //final of for all
        for (Proposal prop : bhp) {
            if (promisingPropos!=null && prop.getD2() < promisingPropos.getD1()) {
                if (prop.getD2() < prop.getIp().getStepToDie()) {
                    //The busy helicopter can add to his route the rescue of
                    // this injured people too
                    promisingPropos = prop;
                    //promisingha = prop.getHa();
                    //CASE ADD TO ROUTE
                    cases = 1;
                } else if (prop.getD1() < prop.getIp().getStepToDie()) {
                    //The busy helicopter can only rescue this injured person
                    // if rejects the previous rescue

                    cases = 2;
                }
                //otherwise, for now, no helicopters can rescue the injured person
            }
        }

        //Now we have the candidate
        //Case -1 -> No candidate
        //Case 0 -> Free helicopter
        //Case 1 -> Busy Helicopter
        //Case 2 -> Rejector Helicopter
        /// NOW WE NEED TO CONTACT WITH AUCTION PROTOCOL AND SEND THE
        /// PROPOSAL
        try {
            ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
            reply.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
            reply.setPerformative(ACLMessage.PROPOSE);
            reply.setContentObject(promisingPropos);
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.COORDINATOR.toString());
            AID coord = UtilsAgents.searchAgent(myAgent, searchCriterion);
            reply.addReceiver(coord);
            myAgent.send(reply);

            /*ACLMessage response = new ACLMessage(ACLMessage.PROPOSE);
             response.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);

             //Then, in Coordinator, it is necessary to understand in
             // what case we are doing the correspondent comprovation.
             response.setContentObject(promisingPropos);
             ServiceDescription searchCriterion = new ServiceDescription();
             searchCriterion.setType(AgentType.COORDINATOR.toString());
             response.addReceiver(UtilsAgents.searchAgent(myAgent, searchCriterion));
             //myAgent.postMessage(response);
             myAgent.send(response);*/
        } catch (IOException ex) {
            Logger.getLogger(HelicopterAgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }

        state = State.WAITING_FOR_AUCTION;
        mutex = promisingPropos;

//        ACLMessage auctionAccept = myAgent.blockingReceive(MessageTemplate.or(
//                    MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION), MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL)),
//                    MessageTemplate.or(
//                            MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION), MessageTemplate.MatchPerformative(ACLMessage.FAILURE)),
//                            MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION), MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)))));
//        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
//        inform.setContent("Next auction please");
//        inform.clearAllReceiver();
//        inform.addReceiver(UtilsAgents.searchAgent(myAgent, searchCriterion));
//        myAgent.send(inform);
    }

    protected void handleInform(ACLMessage inform) {
        System.out.println("Inform from contract net received");
        inform.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
        inform.setPerformative(ACLMessage.CONFIRM);
        inform.clearAllReceiver();
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        inform.addReceiver(UtilsAgents.searchAgent(myAgent, searchCriterion));
        myAgent.send(inform);
    }

    public void sendAllResponses(ACLMessage auctionAccept, Proposal promisingPropos, Vector responses, Vector acceptances, boolean won) {

//        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
//        inform.setContent("Next auction please");
//        inform.clearAllReceiver();
//        inform.addReceiver(UtilsAgents.searchAgent(myAgent, searchCriterion));
//        myAgent.send(inform);
        try {
            mutex = (Proposal) auctionAccept.getContentObject();
        } catch (UnreadableException ex) {
            Logger.getLogger(HelicopterContractNetInitiator.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Sending all helicopter contractnet responses");

        //if(true){
      //  if (mutex.compareTo(promisingPropos) == 0) {
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
                    Logger.getLogger(HelicopterAgentCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(HelicopterContractNetInitiator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        //}
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
                        state = State.WAITING_FOR_PROPOSALS;
                        break;
                }
                if (proposals.size() == getNResponders() && (msg.getPerformative() == ACLMessage.PROPOSE || msg.getPerformative() == ACLMessage.REFUSE )) {
                    handleAllResponses(proposals);
                }
            }
        }
        // block();
    }

}
