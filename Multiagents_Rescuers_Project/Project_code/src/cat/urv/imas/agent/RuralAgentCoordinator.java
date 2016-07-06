/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.coordinator.AuctionResponderBehaviour;
import cat.urv.imas.behaviour.coordinator.rural.RuralContractNetInitiator;
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
 * @author Juli
 */
public class RuralAgentCoordinator extends AbstractCoordinator {

    /**
     * Game settings in use.
     */
    private GameSettings game;
    private int nResponders;

    public RuralAgentCoordinator() {
        super(AgentType.RURAL_AGENT_COORDINATOR);

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
        sd1.setType(AgentType.RURAL_AGENT_COORDINATOR.toString());
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
        System.out.println("RURAL AGENT COORDINATOR: Hello World! My name is " + getLocalName());

        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.RURAL_AGENT.toString());
        List<AID> ruralAgents = UtilsAgents.searchAgents(this, searchCriterion);
        this.setResponders(ruralAgents);
        MessageTemplate template = MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
        addBehaviour(new AuctionResponderBehaviour(this, template));
        addBehaviour(new RuralContractNetInitiator(this, 7));
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

    @Override
    public void searchResponders() {
        if (responders == null || responders.size() == 0) {
            ServiceDescription searchCriterion = new ServiceDescription();
            searchCriterion.setType(AgentType.RURAL_AGENT.toString());
            List<AID> agents = UtilsAgents.searchAgents(this, searchCriterion);
            this.setResponders(agents);

        }

    }
}
