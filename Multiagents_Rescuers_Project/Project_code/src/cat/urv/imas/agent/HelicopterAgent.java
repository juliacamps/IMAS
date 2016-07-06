/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.mobileAgents.MobileAgentLocationBehaviour;
import cat.urv.imas.behaviour.mobileAgents.MobileAgentRescueBehaviour;
import cat.urv.imas.behaviour.mobileAgents.ContractNetResponderBehaviour;
import cat.urv.imas.behaviour.mobileAgents.helicopter.HelicopterStepBehaviour;
import cat.urv.imas.map.InjuredPerson;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.Proposal;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.Point;
import static java.lang.Math.abs;
import java.util.ArrayList;

/**
 *
 * @author marialeyva
 */
public class HelicopterAgent extends ImasAgent implements RescuingAgentInterface {

    /**
     * Game settings in use.
     */
    private GameSettings game;
    private int nResponders;
    private Point currrentPosition;
    private Point hospitalPosition;
    private int currentState;
    private ArrayList<InjuredPerson> injuredPersons;
    public final static int STATE_FREE = -1;
    public final static int STATE_DUTY = 0;
    public final static int STATE_ONE_INJURED = 1;
    public final static int STATE_FULL = 2;
    private HelicopterLocation location;
    private int personsPerHelicopter;
    private ArrayList<Proposal> objectives;
    private int[] oldLocation;
    private boolean rescuedCompleted;
    private InjuredPerson objective;

    public HelicopterAgent() {
        super(AgentType.HELICOPTER);
        location = HelicopterLocation.HOSPITAL;
        currentState = STATE_FREE;
        //nOnBoard number of persons that the helicopter can carry

    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
        Object[] args = getArguments();
        this.currrentPosition = new Point((int) args[0], (int) args[1]);
        this.oldLocation = new int[2];
        this.oldLocation[0] = currrentPosition.x;
        this.oldLocation[1] = currrentPosition.y;
        this.personsPerHelicopter = (int) args[2];
        this.injuredPersons = new ArrayList<>();
        this.hospitalPosition = currrentPosition;
        this.objectives = new ArrayList<>();

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/
        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.HELICOPTER.toString());
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
        System.out.println("Hello World! My name is " + getLocalName());
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));

        addBehaviour(new ContractNetResponderBehaviour(this));
        addBehaviour(new HelicopterStepBehaviour(this));
        addBehaviour(new MobileAgentLocationBehaviour(this));
        addBehaviour(new MobileAgentRescueBehaviour(this));

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
    public Proposal eval(InjuredPerson ip) {
        //double[] proposal = null;
        Proposal proposal = null;
        switch (this.currentState) {
            //Helicopter idle at the hospital:
            // 1 step to take off + distance to the injured mountaineer.
            case STATE_FREE:
                proposal = new Proposal(distanceForHelicopter(currrentPosition.getLocation(), ip.getLocation()) + 1,
                        Proposal.HELICOPTER_AGENT_TYPE);
                //proposal = new double[1];
                //proposal[0] = distanceForHelicopter(currrentPosition.getLocation(), ip.getLocation()) + 1;
                break;
            //persons on board and no other duty:
            // Distance to the injured person
            case STATE_DUTY:
                proposal = new Proposal(distanceForHelicopter(currrentPosition.getLocation(), ip.getLocation()),
                        Proposal.HELICOPTER_AGENT_TYPE);
                //proposal = new double[1];
                //proposal[0] = distanceForHelicopter(currrentPosition.getLocation(), ip.getLocation());
                break;
            //No-one  on  board  and  on  its  way  to  rescue  a  person:
            // He  will  respond  with  two distances:  The distance from 
            // him to the new injured person (D1) and the distance from 
            // him to the injured persons who were previously planned that 
            // were going to be rescued + the distance between both peoples 
            // injured (D2).
            case STATE_ONE_INJURED:
                double distance = 1000;//distanceForHelicopter(currrentPosition.getLocation(), injuredPersons.get(0).getLocation());
                for (int i = 0; i < injuredPersons.size() - 1; i++) {
                    distance += distanceForHelicopter(injuredPersons.get(i).getLocation(), injuredPersons.get(i + 1).getLocation());
                }
                distance += distanceForHelicopter(injuredPersons.get(injuredPersons.size() - 1).getLocation(), ip.getLocation());
                proposal = new Proposal(distanceForHelicopter(currrentPosition.getLocation(), ip.getLocation()),
                        distance,
                        Proposal.HELICOPTER_AGENT_TYPE);
                //proposal = new double[2];
                //proposal[0] = distanceForHelicopter(currrentPosition.getLocation(), ip.getLocation());
                //proposal[1] = distanceForHelicopter(currrentPosition.getLocation(), injuredPersons[0].getLocation())
                //        + distanceForHelicopter(injuredPersons[0].getLocation(), ip.getLocation());
                break;
            case STATE_FULL:
                proposal = new Proposal(distanceForHelicopter(currrentPosition.getLocation(), hospitalPosition.getLocation())
                        + 2 + distanceForHelicopter(hospitalPosition.getLocation(), ip.getLocation()), Proposal.HELICOPTER_AGENT_TYPE);
                //proposal = new double[1];
                //proposal[0] = distanceForHelicopter(currrentPosition.getLocation(), hospitalPosition.getLocation()) + 2 + distanceForHelicopter(hospitalPosition.getLocation(), ip.getLocation());
                break;
            default:
                proposal = new Proposal(Double.POSITIVE_INFINITY, Proposal.HELICOPTER_AGENT_TYPE);
            //proposal = new double[1];
            //proposal[0] = Double.POSITIVE_INFINITY;
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return proposal;
    }

    private double distanceForHelicopter(Point p1, Point p2) {
        return abs(p1.getX() - p2.getX()) + abs(p1.getY() - p2.getY());
    }

    @Override
    public boolean rescue() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] getCurrentLocation() {
        int[] res = new int[2];
        res[0] = currrentPosition.x;
        res[1] = currrentPosition.y;
        return res;
    }

    public HelicopterLocation getLocation() {
        return location;
    }

    public Point getCurrrentPosition() {
        return currrentPosition;
    }

    public Point getHospitalPosition() {
        return hospitalPosition;
    }

    public void setLocation(HelicopterLocation location) {
        this.location = location;
    }

    public void setCurrrentPosition(Point currrentPosition) {
        this.currrentPosition = currrentPosition;
    }

    public void setInjuredPersons(ArrayList<InjuredPerson> injuredPersons) {
        this.injuredPersons = injuredPersons;
    }

    public ArrayList<InjuredPerson> getInjuredPersons() {
        return injuredPersons;
    }

    public int getPersonsPerHelicopter() {
        return personsPerHelicopter;
    }

    public void setPersonsPerHelicopter(int personsPerHelicopter) {
        this.personsPerHelicopter = personsPerHelicopter;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    @Override
    public void setObjective(Proposal objective) {
        if(objective==null){
            this.objective=null;
            return;
        }
        this.objectives.add(objective);
        if (objectives.size() < this.personsPerHelicopter) {
            this.currentState = STATE_ONE_INJURED;
            this.injuredPersons.add(objective.getIp());
        } else {
            this.currentState = STATE_FULL;
            this.injuredPersons.add(objective.getIp());

        }
    }

    public InjuredPerson getFistObjectiveIp() {
        if (objectives.isEmpty()) {
            return null;
        }
        return objectives.get(0).getIp();
    }

    public int getCurrentState() {
        return currentState;
    }

    @Override
    public int[] getPreviousLocation() {
        return oldLocation;
    }

    public void setPreviousLocation(int[] currentLocation) {
        this.oldLocation = currentLocation;
    }

    @Override
    public boolean rescueCompleted() {
        return this.rescuedCompleted;
    }

    @Override
    public InjuredPerson getObjective() {
        return objective;
    }

}
