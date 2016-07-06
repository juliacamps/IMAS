/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.coordinator.AuctionResponderBehaviour;
//import cat.urv.imas.behaviour.coordinator.RequesterHelicopterCoordinator;
import cat.urv.imas.behaviour.coordinator.helicopter.HelicopterContractNetInitiator;
import cat.urv.imas.onthology.GameSettings;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import java.util.List;

/**
 *
 * @author
 */
public class HelicopterAgentCoordinator extends AbstractCoordinator {

    /**
     * Game settings in use.
     */
    private GameSettings game;
    private int nResponders;

    public HelicopterAgentCoordinator() {
        super(AgentType.HELICOPTER_AGENT_COORDINATOR);

    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/
        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.HELICOPTER_AGENT_COORDINATOR.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.HELICOPTER.toString());
        List<AID> helicopters = UtilsAgents.searchAgents(this, searchCriterion);
        this.setResponders(helicopters);
       // this.addBehaviour(new CyclicContractNetInitiator(nResponders, this));
        System.out.println("HELICOPTER COORDINATOR: Hello World! My name is " + getLocalName());
        if(responders != null && responders.size() > 0){
            nResponders = responders.size();
            System.out.println("Trying to delegate dummy-action to one out of " + nResponders + " responders.");

        } else {
            System.out.println("No responder specified.");
        }
        MessageTemplate template =MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
        addBehaviour(new AuctionResponderBehaviour(this, template));
        addBehaviour(new HelicopterContractNetInitiator(this, helicopters.size()));
        // searchAgent is a blocking method, so we will obtain always a correct AID

        /* ********************************************************************/
        /*   ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
         initialRequest.clearAllReceiver();
         initialRequest.addReceiver(system);
         initialRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
         log("Request message to agent");
         try {
         initialRequest.setContent(MessageContent.GET_MAP);
         log("Request message content:" + initialRequest.getContent());
         } catch (Exception e) {
         e.printStackTrace();
         }*/
        //we add a behaviour that sends the message and waits for an answer
        //this.addBehaviour(new RequesterHelicopterCoordinator(this, initialRequest));
        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    private boolean evalLandingRequest() {
        return false;
    }

//    private HelicopterAgent choosePromisingHelicopterAgent(InjuredPerson ip) {
//        // the HelicopterCoordinator has all the HelicopterAgent bids, he will
//        // choose the most promising HelicopterAgent  and,  in  case  of  tie, 
//        // the  Helicopter  with the least amount of takeoffs and landings
//        // will be selected.
//        
//        ArrayList<Proposal> bhp = new ArrayList<>();
//        HelicopterAgent promisingha = null;
//        //double promisingPropos = Double.POSITIVE_INFINITY;
//        int cases = -1;
//        
//        //for all ha,...
//        ServiceDescription searchCriterion = new ServiceDescription();
//        searchCriterion.setType(AgentType.HELICOPTER.toString());
//        List<AID> helicopters = UtilsAgents.searchAgents(this, searchCriterion);
//
//        if (helicopters != null && helicopters.size() > 0) {
//            
//            HelicopterAgent ha = null;
//            Proposal promisingPropos = null;
//            Proposal proDis = ha.eval(ip);
//            if (proDis.getLength() == 1) {
//                if (proDis.getD1() < ip.getStepToDie() &&
//                        proDis.getD1() < promisingPropos.getD1()) {
//
//                    promisingPropos = proDis;//proDis.getD1();
//                    promisingPropos.setHa(ha);
//                    //promisingha = ha;
//                    cases = 0;
//                }
//            } else if (proDis.getLength() == 2) {
//                proDis.setHa(ha);
//                bhp.add(proDis);//new BusyHelicoptersProposals(proDis,ha));
//            } else {
//                System.err.println("ERROR proDis has inconsistent length: "+proDis.getLength());
//            }
//            //final of for all
//            for (Proposal prop:bhp) {
//                if (prop.getD2() < promisingPropos.getD1()) {
//                    if (prop.getD2() < ip.getStepToDie()) {
//                        //The busy helicopter can add to his route the rescue of
//                        // this injured people too
//                        promisingPropos = prop;
//                        //promisingha = prop.getHa();
//                        //CASE ADD TO ROUTE
//                        cases = 1;
//                    }else if (prop.getD1() < ip.getStepToDie()){
//                        //The busy helicopter can only rescue this injured person
//                        // if rejects the previous rescue
//
//                        cases = 2;
//                    }
//                    //otherwise, for now, no helicopters can rescue the injured person
//                }
//            }
//        }
//        //Now we have the candidate
//        //Case -1 -> No candidate
//        //Case 0 -> Free helicopter
//        //Case 1 -> Busy Helicopter
//        //Case 2 -> Rejector Helicopter
//        
//        return null;
//    }
    @Override
    public void searchResponders() {
        if (responders == null || responders.size() == 0) {
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.HELICOPTER.toString());
            List<AID> helicopters = UtilsAgents.searchAgents(this, searchCriterion);
            this.setResponders(helicopters);

        }

    }

}
