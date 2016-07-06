/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.central;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.InjuredPerson;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.InfoAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLParserConstants;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.nashorn.internal.parser.TokenType;

/**
 *
 * @author maria
 */
public class StepBehaviour extends Behaviour {

    private final long seed;
    private int step;
    private final SystemAgent system;
    private List<AID> mobileAgents;

    public StepBehaviour(SystemAgent system) {
        step = 0;
        this.system = system;
        this.seed = (long) system.getGame().getSeed();
    }

    @Override
    public void action() {
        MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

        ACLMessage msg = system.receive(mt1);
        if (msg != null) {
            try {
                HashMap<InjuredPerson, AID> assignedRescuers = (HashMap<InjuredPerson, AID>) msg.getContentObject();
                system.getGame().updateInjuredPeople(assignedRescuers);
                System.out.println("Step: " + step);
                notifyMobileAgentsToMove();
                generateInjuredPeople();
                updateCells();
                generateAvalanches(this.system.getGame().getAvalancheDuration());

                updateAvalanches();
                updateInjuredPeople();
                system.getStatistics().addDiedPeople(cleanDiedPeople());
                system.updateGUI();
                step++;
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                try {
                    reply.setContentObject(system.getGame());
                    for (AID mobileAgent : mobileAgents) {
                        reply.addReceiver(mobileAgent);
                    }
                    system.send(reply);
                } catch (IOException ex) {
                    Logger.getLogger(StepBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(StepBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (UnreadableException ex) {
                Logger.getLogger(StepBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        block();

    }

    @Override
    public boolean done() {
        return step == system.getGame().getSimulationSteps();
    }

    @Override
    public int onEnd() {
        myAgent.doDelete();
        return super.onEnd();
    }

    /**
     * Adding avalanches in a random and dynamic way. Avalanches disappear
     * automatically on up to 5 steps.
     */
    private void generateAvalanches(int duration) {
        Random r = new Random();
        double f = r.nextInt(100) * (0.5);
        System.out.println("Avalanch Probability is " + f);

        if (f > 0.5) {
            r = new Random();
            int n = r.nextInt(3);  /* number of avalanches */

            int m = 0;
            do {
                Cell c;
                PathCell pc;
                boolean foundPath;
                do {
                    c = system.getGame().get(r.nextInt((system.getGame().getMap()).length),
                            r.nextInt((system.getGame().getMap()[0]).length));
                    if (c.getCellType() == CellType.PATH) {
                        pc = (PathCell) c;
                        foundPath = !pc.getInjuredPeople().isThereInjuredPeople() && !pc.isThereAnAgent();
                    } else {
                        foundPath = false;
                    }

                } while (!foundPath);

                PathCell p = (PathCell) c;
                if (!p.isAvalanche()) {
                    System.out.println("Avalanche appeared in Cell ("
                            + p.getRow() + ", " + p.getCol() + ")");
                    p.setAvalanche(duration);
                    m++;
                }
            } while (m < n);
        }
    }

    /**
     * Updates the remaining steps for each existing avalanche to disappear.
     */
    private void updateAvalanches() {
        for (Cell[] rows : this.system.getGame().getMap()) {
            for (Cell c : rows) {
                if (c instanceof PathCell) {
                    PathCell p = (PathCell) c;
                    if (p.isAvalanche()) {
                        p.updateAvalanche();
                    }
                }
            }
        }
    }

    private void generateInjuredPeople() {
        Random r = new Random();
        double f = r.nextInt(100);

        List<InjuredPerson> addedIp = new ArrayList<>();
        //50% de posibilidades de que aparezcan nuevos heridos
        if (f > 0.5) {
            r = new Random();
            //number of injured people to generate
            //max 5 per step
            //int n = r.nextInt(5) + 1;
            int n = 1;
            int m = 0;
            do {
                Cell c = null;
                boolean foundPath;
                do {
                    c = system.getGame().get(
                            r.nextInt((system.getGame().getMap()).length),
                            r.nextInt((system.getGame().getMap()[0]).length));
                    foundPath = (c.getCellType() == CellType.PATH);
                } while (!foundPath);
                PathCell p = (PathCell) c;
                InjuredPerson ip = new InjuredPerson(
                        step + system.getGame().getStepsToFreeze(),
                        r.nextInt(100) > system.getGame().getLightSeverity(),
                        new Point(c.getRow(), c.getCol()), step);
                p.addInjuredPeople(ip);
                addedIp.add(ip);
                m++;
            } while (m < n);
        }
        system.getGame().addInjuredPeople(addedIp);

    }

    private int cleanDiedPeople() {
        int dead = 0;
        for (int i = 0; i < system.getGame().getMap().length; i++) {
            for (int j = 0; j < system.getGame().getMap()[0].length; j++) {
                Cell c = system.getGame().get(i, j);
                if (c.getCellType() == CellType.PATH) {
                    dead += ((PathCell) c).getInjuredPeople().cleanDiedPeople(step);
                }
            }
        }
        List<InjuredPerson> newIp = new ArrayList<>();
        for (InjuredPerson ip : system.getGame().getInjuredPeople()) {
            if (ip.getStepToDie() > step) {
                newIp.add(ip);
            }
        }
        system.getGame().setInjuredPeople(newIp);
        return dead;
    }

    private void notifyMobileAgentsToMove() {
        if (mobileAgents == null || mobileAgents.isEmpty()) {
            searchMobileAgents();
        }
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM_REF);
        msg.setContent(STATE_READY);
        for (AID a : mobileAgents) {
            msg.addReceiver(a);
        }
        myAgent.send(msg);
    }

    private void searchMobileAgents() {
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.HELICOPTER.toString());
        List<AID> helicopters = UtilsAgents.searchAgents(system, searchCriterion);
        mobileAgents = helicopters;
        searchCriterion.setType(AgentType.RURAL_AGENT.toString());
        List<AID> ruralAgents = UtilsAgents.searchAgents(system, searchCriterion);
        mobileAgents.addAll(ruralAgents);
    }

    private void updateInjuredPeople() {
        for (AID a : mobileAgents) {
            ACLMessage msg = new ACLMessage(99);
            msg.setContent("RESCUING");
            msg.addReceiver(a);
            myAgent.send(msg);
            ACLMessage reply = myAgent.receive(MessageTemplate.MatchPerformative(99));
            if (reply != null) {
                try {
                    Object o = reply.getContentObject();
                    if (o != null) {
                        InjuredPerson ip = (InjuredPerson) o;
                        System.out.println("First visit to " + ip.getLocation());
                        ((PathCell) system.getGame().get(ip.getLocation().x, ip.getLocation().y)).getInjuredPeople().saveInjuredPerson(ip);
                        system.getStatistics().addFirstVisit(step - ip.getStepAppeared());

                    }
                } catch (UnreadableException e) {
                    Logger.getLogger(StepBehaviour.class.getName()).log(Level.SEVERE, null, e);
                }
            }
           /* ACLMessage rescued = new ACLMessage(42);
            rescued.setContent("RESCUED");
            rescued.addReceiver(a);
            myAgent.send(rescued);*/
            ACLMessage rescuedReply = myAgent.receive(MessageTemplate.MatchPerformative(42));
            if (rescuedReply != null) {
                try {
                    Object o = rescuedReply.getContentObject();
                    if (o != null) {
                        InjuredPerson ip = (InjuredPerson) o;
                        System.out.println("Rescued " + ip.getLocation());

                        if (rescuedReply.getSender().getName().startsWith("ra")) {
                            system.getStatistics().addRuralAgentRescue(1);
                        } else {
                            system.getStatistics().addHospitalRescue(1);
                        }
                    }
                } catch (UnreadableException e) {
                    Logger.getLogger(StepBehaviour.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private void updateCells() {
        Map<AgentType, List<Cell>> map = new HashMap<>();
        map.put(AgentType.HELICOPTER, new ArrayList<Cell>());
        map.put(AgentType.RURAL_AGENT, system.getGame().getAgentList().get(AgentType.RURAL_AGENT));
        for (AID a : mobileAgents) {

            //TODO: ONLY FOR HELICOPTERS
            if (a.getName().startsWith("ha")) {

                ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
                msg.setContent("LOCATION");
                msg.addReceiver(a);
                myAgent.send(msg);
                ACLMessage reply = null;
                do {
                    reply = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.UNKNOWN));
                    block();
                } while (reply == null);
                if (reply.getSender().getName().startsWith("ha")) {
                    try {
                        int[] loc = (int[]) reply.getContentObject();
                        InfoAgent agent = new InfoAgent(AgentType.HELICOPTER, reply.getSender());
                        Cell old = system.getGame().getMap()[loc[0]][loc[1]];

                        Cell c = system.getGame().getMap()[loc[2]][loc[3]];
                        if (old.getRow() != c.getRow() || c.getCol() != old.getCol()) {
                            if (old.isThereAnAgent()) {
                                old.removeAgent(agent);
                                c.addAgent(agent);

                            }
                            map.get(AgentType.HELICOPTER).add(c);
                            map.get(AgentType.HELICOPTER).remove(old);

                        }
                    } catch (UnreadableException ex) {
                        Logger.getLogger(StepBehaviour.class
                                .getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(StepBehaviour.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else if (a.getName().startsWith("ra")) {
                ACLMessage msg = new ACLMessage(ACLMessage.UNKNOWN);
                msg.setContent("LOCATION");
                msg.addReceiver(a);
                myAgent.send(msg);
                ACLMessage reply = null;
                do {
                    reply = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.UNKNOWN));
                    block();
                } while (reply == null);
                if (reply.getSender().getName().startsWith("ra")) {
                    try {
                        int[] loc = (int[]) reply.getContentObject();
                        InfoAgent agent = new InfoAgent(AgentType.RURAL_AGENT, reply.getSender());
                        Cell old = system.getGame().getMap()[loc[0]][loc[1]];

                        Cell c = system.getGame().getMap()[loc[2]][loc[3]];
                        if (old.getRow() != c.getRow() || c.getCol() != old.getCol()) {
                            if (old.isThereAnAgent()) {
                                old.removeAgent(agent);
                                c.addAgent(agent);

                            }
                            //map.get(AgentType.RURAL_AGENT).add(c);
                            //map.get(AgentType.RURAL_AGENT).remove(old);
                        }
                    } catch (UnreadableException ex) {
                        Logger.getLogger(StepBehaviour.class
                                .getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(StepBehaviour.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

}
