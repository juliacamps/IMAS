/*
 + * To change this license header, choose License Headers in Project Properties.
 + * To change this template file, choose Tools | Templates
 + * and open the template in the editor.
 + */
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.behaviour.mobileAgents.MobileAgentLocationBehaviour;
import cat.urv.imas.behaviour.mobileAgents.MobileAgentRescueBehaviour;
import cat.urv.imas.behaviour.mobileAgents.ContractNetResponderBehaviour;
import cat.urv.imas.behaviour.mobileAgents.ruralagent.RuralAgentSettingsReceiver;
import cat.urv.imas.behaviour.mobileAgents.ruralagent.RuralAgentStepBehaviour;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.InjuredPerson;
import cat.urv.imas.map.MapNodeGraph;
import cat.urv.imas.map.Path;
import cat.urv.imas.map.PathCell;
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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vicent
 */
public class RuralAgent extends ImasAgent implements RescuingAgentInterface {

    /**
     * Game settings in use.
     */
    private GameSettings game;
    private int nResponders;

    // STATES
    private State currentState;

    @Override
    public boolean rescueCompleted() {
        return rescued;
    }

    public void setCurrentState(State state) {
        this.currentState = state;
    }

    public enum State {

        IDLE,
        DUTY,
        BACK
    }
    // Map Node Graph
    private MapNodeGraph nodeGraph;

    // Positions & Path
    private Point mountainHutPos;
    private Point currentPos;
    private Point prevPos;
    private Path path;
    private Path auxPath;

    // Objective
    private InjuredPerson objective;
    private Boolean rescued;

    // 2-D Direction
    private int[] dir;

    public RuralAgent() {
        super(AgentType.RURAL_AGENT);
        currentState = State.IDLE;
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
        Object[] args = getArguments();
        this.currentPos = new Point((int) args[0], (int) args[1]);
        this.mountainHutPos = new Point(currentPos);
        this.prevPos = new Point(currentPos);
        this.dir = new int[2];
        this.rescued = false;

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/
        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.RURAL_AGENT.toString());
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
        System.out.println("RURAL AGENT: Hello World! my name is " + getLocalName());

        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));

        // Behaviours
        addBehaviour(new ContractNetResponderBehaviour(this));
        addBehaviour(new RuralAgentSettingsReceiver(this, MessageTemplate.MatchPerformative(ACLMessage.INFORM)));
        addBehaviour(new RuralAgentStepBehaviour(this));
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
        if (nodeGraph == null) {
            nodeGraph = new MapNodeGraph(game.getMap());
        }
    }

    public void setMapNodeGraph(MapNodeGraph nodeGraph) {
        this.nodeGraph = nodeGraph;
    }

    @Override
    public void setObjective(Proposal objective) {
        if (objective == null) {
            this.objective = null;
        } else {
            this.objective = objective.getIp();
            this.rescued = false;
            this.currentState = State.DUTY;
            setPath();
        }
    }

    @Override
    public InjuredPerson getObjective() {
        return objective;
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
        Point objPos = ip.getLocation();
        double d;
        if (!ip.isSeverelyInjured()) {
            switch (currentState) {
                case IDLE:
                    d = (double) distanceTo(currentPos, objPos) + 1;
                    break;

                case DUTY:
                    d = Double.POSITIVE_INFINITY;
                    break;

                case BACK:
                    d = Double.POSITIVE_INFINITY;
                    break;

                default:
                    d = Double.POSITIVE_INFINITY;
                    break;
            }
        } else {
            d = Double.POSITIVE_INFINITY;
        }
        return new Proposal(d, Proposal.RURAL_AGENT_TYPE);
    }

    public State getCurrentState() {
        return currentState;
    }

    private int distanceTo(Point p1, Point p2) {
        List<Point> departurePoints, arrivalPoints;
        if (game.get(p1.x, p1.y).getCellType() == CellType.MOUNTAIN_HUT) {
            departurePoints = getSorroundingPathCells(p1);
        } else {
            departurePoints = new ArrayList<>();
            departurePoints.add(p1);
        }
        if (game.get(p2.x, p2.y).getCellType() == CellType.MOUNTAIN_HUT) {
            arrivalPoints = getSorroundingPathCells(p2);
        } else {
            arrivalPoints = new ArrayList<>();
            arrivalPoints.add(p2);
        }
        int minDist = 1000;
        Path auxPath2;
        for (int i = 0; i < departurePoints.size(); i++) {
            for (int j = 0; j < arrivalPoints.size(); j++) {
                auxPath2 = new Path(departurePoints.get(i), arrivalPoints.get(j));
                nodeGraph.minDistance(auxPath2);
                if (auxPath2.getDistance() < minDist) {
                    auxPath = auxPath2;
                    minDist = auxPath2.getDistance();
                }
            }
        }

        return auxPath.getDistance();

    }

    private List<Point> getSorroundingPathCells(Point p) {
        List<Point> points = new ArrayList<>();
        for (int i = -1; i < 2; i = i + 2) {
            if (game.get(p.x + i, p.y).getCellType() == CellType.PATH || game.get(p.x + i, p.y).getCellType() == CellType.AVALANCHE) {
                points.add(new Point(p.x + i, p.y));
            }
            if (game.get(p.x, p.y + i).getCellType() == CellType.PATH || game.get(p.x, p.y + i).getCellType() == CellType.AVALANCHE) {
                points.add(new Point(p.x, p.y + i));
            }
        }
        return points;
    }

    private void setPath() {
        // Al 'ganar' el contractNet, confirma la ruta previamente calculada
        this.path = this.auxPath;
        nodeGraph.optimalRoute(path);
        if (currentPos == path.getPath().peek()) {
            path.getPath().poll();
        }
        updateDirection();
    }

    public void updateDirection() {
        if (path.getPath().size() > 0) {
            // Cambia la direcciÃ³n a seguir para llegar al siguiente Point del path
            dir[0] = (int) Math.signum(path.getPath().peek().x - currentPos.x); // -1,0,1
            dir[1] = (int) Math.signum(path.getPath().peek().y - currentPos.y); // -1,0,1
        } else {
            dir[0] = (int) Math.signum(mountainHutPos.x - currentPos.x);
            dir[1] = (int) Math.signum(mountainHutPos.y - currentPos.y);
        }
    }

    public enum Next {

        IS_EMPTY,
        IS_AVALANCHE,
        IS_OBJECTIVE,
        IS_MOUNTAIN_HUT,
        UNKNOWN
    }

    public Next checkPosition() {
        Next R = Next.UNKNOWN;
        Point nextPos = new Point(currentPos.x + dir[0], currentPos.y + dir[1]);
        CellType cellType = game.get(nextPos.x, nextPos.y).getCellType();
        if(cellType == CellType.PATH)
            R = Next.IS_EMPTY;
        if (cellType == CellType.AVALANCHE) {
            R = Next.IS_AVALANCHE;
        } else if (nextPos.x == objective.getLocation().x && nextPos.y == objective.getLocation().y) {
            R = Next.IS_OBJECTIVE;
        } else if (cellType == CellType.MOUNTAIN_HUT) {
            R = Next.IS_MOUNTAIN_HUT;
        }
        return R;
    }

    @Override
    public boolean rescue() {
        PathCell c = (PathCell) game.get(objective.getLocation().x, objective.getLocation().y);
        if (c.getInjuredPeople().isThereInjuredPeople()) {
            c.getInjuredPeople().saveInjuredPerson(objective);
            rescued = true;
        }
        getBack();
        return rescued;
    }

    private void getBack() {
        if (currentPos.x == mountainHutPos.x && currentPos.y == mountainHutPos.y) {
            this.currentState = State.IDLE;
        } else {
            this.currentState = State.BACK;
            distanceTo(currentPos, mountainHutPos);
            path = auxPath;
            nodeGraph.optimalRoute(path);
            if (currentPos == path.getPath().peek()) {
                path.getPath().poll();
            }
            updateDirection();
        }
    }

    public void move() {
        if (!path.getPath().isEmpty()) {
            prevPos = new Point(currentPos);
            currentPos.translate(dir[0], dir[1]);
            if (currentPos.x == path.getPath().peek().x && currentPos.y == path.getPath().peek().y) {
                path.getPath().poll();
                updateDirection();
            }
        } else {
            if (currentState == State.DUTY) {
                getBack();
            } else if (currentState == State.BACK) {
                prevPos = new Point(currentPos);
                currentPos = new Point(mountainHutPos);
                currentState = State.IDLE;
            }
        }
        System.out.println(getName() + " moved from "
                + prevPos.x + ", "
                + prevPos.y
                + " to " + currentPos.x
                + ", " + currentPos.y + " to go back");
    }

    @Override
    public int[] getCurrentLocation() {
        return new int[]{currentPos.x, currentPos.y};
    }

    @Override
    public int[] getPreviousLocation() {
        return new int[]{prevPos.x, prevPos.y};
    }
}
