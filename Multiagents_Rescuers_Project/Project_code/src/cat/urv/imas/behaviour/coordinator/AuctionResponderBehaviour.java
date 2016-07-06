/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.AbstractCoordinator;
import cat.urv.imas.onthology.Proposal;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maria
 */
public class AuctionResponderBehaviour extends CyclicBehaviour {

    private enum AuctionState {

        WAITING_CFP,
        WAITING_CONTRACTNET_RESPONSE,
        WAITING_AUCTION_RESPONSE
    }
    private AuctionState state;
    private MessageTemplate mt;
    private ACLMessage cfp;
    private String conversationId;

    public AuctionResponderBehaviour(Agent a, MessageTemplate mt) {
        super(a);
        this.mt = mt;
        this.state = AuctionState.WAITING_CFP;
    }

    protected void handleAnnouncement(ACLMessage cfp) {
        System.out.println("Agent " + myAgent.getLocalName() + ": auction announcement received from " + cfp.getSender().getName());
        //((AbstractCoordinator) myAgent).sendCFP(cfp);
        
        this.cfp = cfp;
        createContractNet(cfp);

    }

    protected void handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        System.out.println(getAgent().getName() + " won the auction");

    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        //Answer to the correspondant agent with a reject
       /* reject = propose.createReply();
         reject.setContent("Reject");
         reject.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
         reject.setPerformative(ACLMessage.REJECT_PROPOSAL);
         myAgent.send(reject);*/
        System.out.println("Agent " + myAgent.getLocalName() + ": rejected bid");
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION));
        // ACLMessage msg=myAgent.receive();
        try {
            if (msg != null) {
                if (state == AuctionState.WAITING_CFP && msg.getPerformative() == ACLMessage.CFP) {
                    handleAnnouncement(msg);
                    state = AuctionState.WAITING_AUCTION_RESPONSE;
                } /*else if (state == AuctionState.WAITING_CONTRACTNET_RESPONSE && msg.getPerformative() == ACLMessage.PROPOSE) {
                 sendProposal(msg, cfp);
                 state = AuctionState.WAITING_AUCTION_RESPONSE;
                 }*/ else if (state == AuctionState.WAITING_AUCTION_RESPONSE) {
                    if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        handleAcceptProposal(cfp, msg, new ACLMessage(ACLMessage.ACCEPT_PROPOSAL));
                        state = AuctionState.WAITING_CFP;
                    } else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                        handleRejectProposal(cfp, msg, new ACLMessage(ACLMessage.REJECT_PROPOSAL));
                        state = AuctionState.WAITING_CFP;
                    }
                }
            }
        } catch (FailureException ex) {
            Logger.getLogger(AuctionResponderBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }

       block();
    }

    private void sendProposal(ACLMessage msg, ACLMessage cfp) {
        System.out.println("Auction received response from CONTRACT-NET");
        msg.clearAllReceiver();
        msg.addReceiver(cfp.getSender());
        myAgent.send(msg);
    }

    private void createContractNet(ACLMessage announcement) {
        try {
            //TODO: Check for rural and helicopter agent
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            cfp.setContentObject(announcement.getContentObject());
            cfp.setConversationId(announcement.getConversationId());
            List<AID> responders = ((AbstractCoordinator) myAgent).getResponders();
            for (AID a : responders) {
                cfp.addReceiver(a);
            }
            myAgent.send(cfp);

        } catch (UnreadableException ex) {
            Logger.getLogger(AuctionResponderBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AuctionResponderBehaviour.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
