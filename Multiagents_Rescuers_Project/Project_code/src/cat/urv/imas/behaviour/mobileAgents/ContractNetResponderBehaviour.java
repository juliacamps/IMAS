/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.mobileAgents;

import cat.urv.imas.agent.RescuingAgentInterface;
import cat.urv.imas.map.InjuredPerson;
import cat.urv.imas.onthology.Proposal;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.domain.introspection.IntrospectionVocabulary;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author axel
 */
public class ContractNetResponderBehaviour extends CyclicBehaviour {
    
    private State state;
    
    private enum State {
        
        WAITING_RESPONSE,
        WAITING_CFP
    }
    
    public ContractNetResponderBehaviour(Agent a) {
        super(a);
        state = State.WAITING_CFP;
    }
    
    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET));
        if (msg != null) {
            switch (msg.getPerformative()) {
                case ACLMessage.CFP:
                    if (state == State.WAITING_CFP) {
                        handleCfp(msg);
                        state = State.WAITING_RESPONSE;
                    }
                    break;
                case ACLMessage.ACCEPT_PROPOSAL:
                    if (state == State.WAITING_RESPONSE) {
                        handleAcceptProposal(msg);
                        state = State.WAITING_CFP;
                    }
                    break;
                case ACLMessage.REJECT_PROPOSAL:
                    handleRejectProposal(msg);
                    state = State.WAITING_CFP;
                    break;
            }
        }
        //block();
    }
    
    private void handleCfp(ACLMessage cfp) {
        System.out.println("Agent " + myAgent.getLocalName() + ": CFP received from " + cfp.getSender().getName());
        InjuredPerson ip = null;
        try {
            ip = (InjuredPerson) cfp.getContentObject();
            Proposal proposal = ((RescuingAgentInterface) myAgent).eval(ip);
            if (proposal != null) {
                ACLMessage response = cfp.createReply();
                response.setPerformative(ACLMessage.PROPOSE);
                if (proposal.getD1() == Double.POSITIVE_INFINITY) {
                    response.setPerformative(ACLMessage.REFUSE);
                }
                response.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                proposal.setIp(ip);
                try {
                    response.setContentObject(proposal);
                    System.out.println("Agent " + myAgent.getLocalName() + " sent a response.");
                } catch (Exception e) {
                    response.setPerformative(ACLMessage.FAILURE);
                    e.printStackTrace();
                }
                System.out.println("Agent " + myAgent.getLocalName() + ": proposal");

                //response.addReceiver(cfp.getSender());
                myAgent.send(response);
                //return null;
            } else {
                // We refuse to provide a proposal
                System.out.println("Agent " + myAgent.getLocalName() + ": Refuse");
                ACLMessage refuse = new ACLMessage(ACLMessage.REFUSE);
                refuse.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                refuse.setContent("Refuse");
                refuse.clearAllReceiver();
                refuse.addReceiver(cfp.getSender());
                myAgent.send(refuse);
            }
        } catch (UnreadableException ex) {
            System.out.println("Agent " + myAgent.getLocalName() + ": Failure");
            //Logger.getLogger(HelicopterAgent.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Agent " + myAgent.getLocalName() + ": couldn't read CFP message");
            ACLMessage refuse = new ACLMessage(ACLMessage.REFUSE);
            refuse.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            refuse.setContent("Refuse");
            refuse.clearAllReceiver();
            refuse.addReceiver(cfp.getSender());
            myAgent.send(refuse);
        }
    }
    
    private void handleAcceptProposal(ACLMessage accept) {
        try {
            System.out.println("Agent " + myAgent.getLocalName() + ": Proposal accepted");
            // if (rescue()) {
            //TODO: GET Injured person from propose. Still pending of seeing the Proposal class
            //((RescuingAgentInterface) myAgent).setCurrentState(HelicopterAgent.STATE_DUTY);
            ((RescuingAgentInterface) myAgent).setObjective((Proposal) accept.getContentObject());
            System.out.println("Agent " + myAgent.getLocalName() + ": Action successfully performed");
            ACLMessage inform = accept.createReply();
            inform.setContentObject(myAgent.getAID());
            inform.setPerformative(ACLMessage.INFORM);
            myAgent.send(inform);
            // } else {
            //System.out.println("Agent " + myAgent.getLocalName() + ": Action execution failed");
            //throw new FailureException("unexpected-error");
            //}
        } catch (UnreadableException ex) {
            Logger.getLogger(ContractNetResponderBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ContractNetResponderBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleRejectProposal(ACLMessage reject) {
        System.out.println("Agent " + myAgent.getLocalName() + ": Proposal rejected");
        
    }
    
}
