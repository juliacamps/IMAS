/**
 * IMAS base code for the practical work. Copyright (C) 2014 DEIM - URV
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.coordinator.AuctionInitiatorBehaviour;
import cat.urv.imas.behaviour.coordinator.ReceiveSettingsBehaviour;
import cat.urv.imas.onthology.GameSettings;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The main Coordinator agent. TODO: This coordinator agent should get the game
 * settings from the System agent every round and share the necessary
 * information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgent {

    /**
     * Game settings in use.
     */
    private GameSettings game;

    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
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
        sd1.setType(AgentType.COORDINATOR.toString());
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

        // search CentralAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        AID system = UtilsAgents.searchAgent(this, searchCriterion);
        /*  System.out.println("Hello World! My name is " + getLocalName());
         ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
         initialRequest.clearAllReceiver();
         initialRequest.addReceiver(system);
         initialRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
         log("Request message to agent");
         try {
         initialRequest.setContent(MessageContent.GET_MAP);
         log("Request message content:" + initialRequest.getContent());
         } catch (Exception e) {
         e.printStackTrace();
         }*/
        // this.addBehaviour(new RequesterBehaviour(this, initialRequest));

       /* ACLMessage initialAnnouncement = new ACLMessage(ACLMessage.CFP);
        initialAnnouncement.setContent("Initial announcement");
        initialAnnouncement.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
        searchCriterion.setType(AgentType.HELICOPTER_AGENT_COORDINATOR.toString());
        AID hcoord = UtilsAgents.searchAgent(this, searchCriterion);
        initialAnnouncement.addReceiver(hcoord);*/
        //TODO ADD RURAL AGENT COORDINATOR
        addBehaviour(new ReceiveSettingsBehaviour(this,
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)));
        AuctionInitiatorBehaviour aibh = new AuctionInitiatorBehaviour(this, system, 2);
        //AuctionInitiatorBehaviour aibh = new AuctionInitiatorBehaviour(this, system, 1);
        addBehaviour(aibh);

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

}
