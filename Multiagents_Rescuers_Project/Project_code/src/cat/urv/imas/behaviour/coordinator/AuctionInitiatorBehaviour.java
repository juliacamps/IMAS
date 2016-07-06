/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.map.InjuredPerson;
import cat.urv.imas.onthology.Proposal;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maria
 */
public class AuctionInitiatorBehaviour extends CyclicBehaviour {

    private int nResponders;

    private AID system;
    private boolean done = false;
    private Vector responses;
    private String conversationId;

    public AuctionInitiatorBehaviour(Agent a, AID system, int nResponders) {
        super(a);
        this.system = system;
        this.nResponders = nResponders;
        //We create our vector of responses
        responses = new Vector();
        //We send the announcement
        // myAgent.send(cfp);
        System.out.println("Initiating auction");
        // this.conversationId=cfp.getConversationId();

    }

    protected void handlePropose(ACLMessage propose, Vector v) {
        v.add(propose);
        System.out.println(propose.getSender().getName() + " proposed ");
    }

    protected void handleRefuse(ACLMessage refuse) {
        System.out.println(refuse.getSender().getName() + " refused");
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            System.out.println("Responder does not exist");
        } else {
            System.out.println(failure.getSender().getName() + " failed");
        }
        // Immediate failure --> we will not receive a response from this agent
        nResponders--;
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        //Select best and answer with an accept. Reject the others
        System.out.println(responses);
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
        }
        // Evaluate proposals.
        Proposal bestProposal = new Proposal(Double.POSITIVE_INFINITY, Proposal.HELICOPTER_AGENT_TYPE);
        AID bestProposer = null;
        ACLMessage accept = null;
        Enumeration e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ACLMessage reply = msg.createReply();
                reply.setConversationId(conversationId);
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                try {
                    reply.setContentObject(msg.getContentObject());
                    reply.addReceiver(msg.getSender());
                    acceptances.addElement(reply);
                    Proposal proposal = (Proposal) msg.getContentObject();
                    if (proposal != null && proposal.compareTo(bestProposal) < 0) {
                        bestProposal = proposal;
                        bestProposer = msg.getSender();
                        accept = reply;
                    }
                } catch (UnreadableException ex) {
                    Logger.getLogger(AuctionInitiatorBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(AuctionInitiatorBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

        // Accept the proposal of the best proposer
        if (accept != null) {
            try {
                System.out.println("Accepting proposal " + " from responder " + bestProposer.getName());

                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                accept.setContentObject(bestProposal);
                accept.addReceiver(bestProposer);
                //myAgent.send(accept);
            } //If we are not accepting anyone we need to ask for settings again
            catch (IOException ex) {
                Logger.getLogger(AuctionInitiatorBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.out.println("No auction winner");
            ACLMessage inform = new ACLMessage(ACLMessage.CONFIRM);
            inform.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
            inform.setContent("ggj");
            inform.addReceiver(myAgent.getAID());
            handleInform(inform);

        }
        for (Object m : acceptances) {
            myAgent.send((ACLMessage) m);
        }
        this.responses = new Vector();

    }

    protected void handleInform(ACLMessage inform) {
        //Send to system
        System.out.println("Auction inform received");
        inform.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
        myAgent.send(inform);
        /* try {
         //  this.finalize();
         } catch (Throwable ex) {
         Logger.getLogger(AuctionInitiatorBehaviour.class.getName()).log(Level.SEVERE, null, ex);
         }*/
    }

    public int getnResponders() {
        return nResponders;
    }

    public void setnResponders(int nResponders) {
        this.nResponders = nResponders;
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);

        ACLMessage msg = myAgent.receive(mt);
        if (msg != null) {
            switch (msg.getPerformative()) {
                case ACLMessage.PROPOSE:
                    handlePropose(msg, responses);
                    break;
                case ACLMessage.REFUSE:
                    handleRefuse(msg);
                    responses.add(msg);
                    break;
                case ACLMessage.FAILURE:
                    handleFailure(msg);
                    break;
                case ACLMessage.CONFIRM:
                    handleInform(msg);
                    //done = true;
                    break;
            }
            if (responses.size() == nResponders && msg.getPerformative() == ACLMessage.PROPOSE) {
                handleAllResponses(responses, new Vector());
            }
        }

        block();
    }

}
