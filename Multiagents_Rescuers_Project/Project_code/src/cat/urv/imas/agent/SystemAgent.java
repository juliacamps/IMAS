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

import cat.urv.imas.behaviour.central.StepBehaviour;
import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.gui.Statistics;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.InfoAgent;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends ImasAgent {

    /**
     * GUI with the map, central agent log and statistics.
     */
    private GraphicInterface gui;
    /**
     * Game settings. At the very beginning, it will contain the loaded initial
     * configuration settings.
     */
    private GameSettings game;
    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private AID coordinatorAgent;

    /**
     * Set of statistics to show up.
     */
    private Statistics statistics;

    /**
     * Builds the Central agent.
     */
    public SystemAgent() {
        super(AgentType.SYSTEM);
    }

    /**
     * A message is shown in the log area of the GUI, as well as in the stantard
     * output.
     *
     * @param log String to show
     */
    @Override
    public void log(String log) {
        if (gui != null) {
            gui.log(getLocalName() + ": " + log + "\n");
        }
        super.log(log);
    }

    /**
     * An error message is shown in the log area of the GUI, as well as in the
     * error output.
     *
     * @param error Error to show
     */
    @Override
    public void errorLog(String error) {
        if (gui != null) {
            gui.log("ERROR: " + getLocalName() + ": " + error + "\n");
        }
        super.errorLog(error);
    }

    /**
     * Gets the game settings.
     *
     * @return game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    /**
     * Informs about the set of statistics.,
     *
     * @return statistics.
     */
    public Statistics getStatistics() {
        return this.statistics;
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviors.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ************************************* */
        this.setEnabledO2ACommunication(true, 1);

        // 1. Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.SYSTEM.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " failed registration to DF [ko]. Reason: " + e.getMessage());
            doDelete();
        }

        // 2. Load game settings.
        this.game = InitialGameSettings.load("game.settings");
        log("Initial configuration settings loaded");
        this.statistics = new Statistics(this.game.getRuralAgentCost(), this.game.getHelicopterCost());

        // 3. Load GUI
        try {
            //Create mobile agents
            createMobileAgents((InitialGameSettings) game);

            this.gui = new GraphicInterface(game);
            gui.showStatistics(statistics);
            gui.setVisible(true);
            log("GUI loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // search CoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        searchCriterion.setType(AgentType.RURAL_AGENT.toString());
        List<AID> ruralAgents = UtilsAgents.searchAgents(this,searchCriterion);
        ACLMessage initialSettings = new ACLMessage(ACLMessage.INFORM);
        try {
            initialSettings.setContentObject(game);
            initialSettings.addReceiver(coordinatorAgent);
            for(AID ra: ruralAgents)
                initialSettings.addReceiver(ra);
            send(initialSettings);

        } catch (IOException ex) {
            Logger.getLogger(SystemAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        //  this.addBehaviour(new RequestResponseBehaviour(this, mt));
        addBehaviour(new StepBehaviour(this));
        // Setup finished. When the last inform is received, the agent itself will add
        // a behaviour to send/receive actions
    }

    public void updateGUI() {
        this.gui.showStatistics(statistics);
        this.gui.updateGame();
    }

    private void createMobileAgents(InitialGameSettings game) throws Exception {
        Map<AgentType, List<Cell>> agentCells = game.getAgentList();
        Iterator<Integer> helicopters = game.getNumberOfHelicoptersPerHospital().iterator();
        List<Cell> hospitals = agentCells.get(AgentType.HELICOPTER);
        jade.wrapper.AgentContainer helicopterContainer = null;
        jade.wrapper.AgentContainer ruralAgentContainer = null;
        for (Cell c : hospitals) {
            if (helicopters.hasNext()) {
                int amount = helicopters.next();
                for (int i = 0; i < amount; i++) {
                    String agentName = "ha" + c.getRow() + "" + c.getCol() + "" + i;
                    Object[] position = new Object[3];
                    position[0] = c.getRow();
                    position[1] = c.getCol();
                    position[2] = game.getPeoplePerHelicopter();

                    if (helicopterContainer == null) {
                        helicopterContainer = UtilsAgents.createAgentGetContainer(agentName, "cat.urv.imas.agent.HelicopterAgent", position);
                    } else {
                        UtilsAgents.createAgent(helicopterContainer, agentName, "cat.urv.imas.agent.HelicopterAgent", position);
                    }
                    AID aid = new AID(helicopterContainer.getAgent(agentName).getName(), AID.ISGUID);

                    c.addAgent(new InfoAgent(AgentType.HELICOPTER, aid));
                }
            } else {
                throw new Error(getClass().getCanonicalName() + " : Less helicopters in the map than in the list.");
            }
        }
        List<Cell> mountainHuts = agentCells.get(AgentType.RURAL_AGENT);
        Iterator<Integer> ruralAgents = game.getNumberOfRuralAgentsPerMountainHut().iterator();
        for (Cell c : mountainHuts) {
            if (ruralAgents.hasNext()) {
                int amount = ruralAgents.next();
                for (int i = 0; i < amount; i++) {
                    String raName = "ra" + c.getRow() + "" + c.getCol() + "" + i;
                    Object[] position = new Object[2];
                    position[0] = c.getRow();
                    position[1] = c.getCol();
                    if (ruralAgentContainer == null) {

                        ruralAgentContainer = UtilsAgents.createAgentGetContainer(raName, "cat.urv.imas.agent.RuralAgent", position);
                    } else {
                        UtilsAgents.createAgent(ruralAgentContainer, raName, "cat.urv.imas.agent.RuralAgent", position);
                    }

                    AID raid = new AID(ruralAgentContainer.getAgent(raName).getName(), AID.ISGUID);
                    c.addAgent(new InfoAgent(AgentType.RURAL_AGENT, raid));
                }
            } else {
                throw new Error(getClass().getCanonicalName() + " : Less rural agents in the list than in the map.");
            }
        }
        if (ruralAgents.hasNext()) {
            throw new Error(getClass().getCanonicalName() + " : Less rural agents in the map than in the list.");
        }
        if (helicopters.hasNext()) {
            throw new Error(getClass().getCanonicalName() + " : Less hospitals in the map than in the list.");
        }
    }

}
