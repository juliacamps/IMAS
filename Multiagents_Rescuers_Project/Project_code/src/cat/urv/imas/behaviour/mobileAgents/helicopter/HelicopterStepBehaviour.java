/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.mobileAgents.helicopter;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.HelicopterAgent;
import cat.urv.imas.agent.HelicopterLocation;
import cat.urv.imas.agent.RescuingAgentInterface;
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.map.InjuredPerson;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.tools.sniffer.Message;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author maria
 */
public class HelicopterStepBehaviour extends CyclicBehaviour {

    private MessageTemplate template;

    public HelicopterStepBehaviour(HelicopterAgent ha) {
        this.setAgent(ha);
        template = MessageTemplate.MatchContent(STATE_READY);
    }

    @Override
    public void action() {
        //We wait until we receive a message telling us to go on
        HelicopterAgent ha = (HelicopterAgent) myAgent;
        ACLMessage msg = ha.receive(template);
        if (msg != null && ha.getLocation() != null) {
            switch (ha.getLocation()) {
                case HOSPITAL:
                    if (ha.getCurrentState() == HelicopterAgent.STATE_FREE) //do nothing
                    {
                        break;
                    } else {
                        System.out.println(ha.getName() + " tries to take off");
                        //request to take off
                        ha.setLocation(HelicopterLocation.TAKING_OFF);
                    }
                    break;
                case TAKING_OFF:
                    //take off
                    System.out.println(ha.getName() + " takes off");
                    ha.setLocation(HelicopterLocation.GOING_TO_RESCUE);
                    ha.setCurrentState(HelicopterAgent.STATE_ONE_INJURED);
                    break;
                case GOING_TO_RESCUE:
                    System.out.println(ha.getName() + " moved from "
                            + ha.getPreviousLocation()[0] + ", "
                            + ha.getPreviousLocation()[1]
                            + " to " + ha.getCurrrentPosition().x
                            + ", " + ha.getCurrrentPosition().y + " to rescue");

                    //move one position heading to the injured person objective
                    move_to_rescue(ha);
                    ha.setCurrentState(HelicopterAgent.STATE_ONE_INJURED);
                    break;
                case RESCUING:
                    System.out.println(ha.getName() + " rescuing");

                    //perform rescue
                    rescue(ha);
                    ha.setLocation(HelicopterLocation.COMING_BACK);
                    break;
                case COMING_BACK:
                    System.out.println(ha.getName() + " moved from "
                            + ha.getPreviousLocation()[0] + ", "
                            + ha.getPreviousLocation()[1]
                            + " to " + ha.getCurrentLocation()[0]
                            + ", " + ha.getCurrentLocation()[1] + " to go back");

                    //move one position heading to the hospital
                    move_to_hospital(ha);
                    break;
                case LANDING:
                    System.out.println(ha.getName() + " landed back at the hospital");

                    //land
                    ha.setLocation(HelicopterLocation.HOSPITAL);
                    //leave injured people in the hospital

                    try {
                        ACLMessage reply = new ACLMessage(42);
                        reply.setContentObject(((RescuingAgentInterface) myAgent).getObjective());
                        ServiceDescription searchCriterion = new ServiceDescription();
                        searchCriterion.setType(AgentType.SYSTEM.toString());
                        AID system = UtilsAgents.searchAgent(myAgent, searchCriterion);
                        reply.addReceiver(system);
                        ha.setInjuredPersons(new ArrayList<InjuredPerson>());
                        ha.setCurrentState(HelicopterAgent.STATE_FREE);
                        ha.setObjective(null);

                        myAgent.send(reply);
                    } catch (IOException ex) {
                        Logger.getLogger(HelicopterStepBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    break;
                default:
                    break;

            };
        }
        block();
    }

    private void rescue(HelicopterAgent ha) {
        //Get person to helicopter
//        for (int i = 0; i < ha.getPersonsPerHelicopter(); i++) {
//            if (ha.getInjuredPersons()[i] == null) {
//                ha.getInjuredPersons()[i] = ha.getObjectiveIp();
//            }
//        }
        ha.setPreviousLocation(ha.getCurrentLocation());
        if (ha.getInjuredPersons().size() < ha.getPersonsPerHelicopter()) {
            ArrayList<InjuredPerson> tmp = ha.getInjuredPersons();
            tmp.add(ha.getFistObjectiveIp());
            ha.setInjuredPersons(tmp);
            if (ha.getInjuredPersons().size() < ha.getPersonsPerHelicopter()) {
                ha.setCurrentState(HelicopterAgent.STATE_DUTY);
            } else {
                ha.setCurrentState(HelicopterAgent.STATE_FULL);
            }
        }
    }

    private void move_to_rescue(HelicopterAgent ha) {
        Point objective = ha.getFistObjectiveIp().getLocation();
        Point currentPosition = ha.getCurrrentPosition();
        move(currentPosition, objective, ha);
        if (ha.getCurrrentPosition().equals(objective)) {
            ha.setLocation(HelicopterLocation.RESCUING);
        }
    }

    private void move_to_hospital(HelicopterAgent ha) {
        Point objective = ha.getHospitalPosition();
        Point currentPosition = ha.getCurrrentPosition();
        move(currentPosition, objective, ha);
        if (ha.getCurrrentPosition().equals(objective)) {
            ha.setLocation(HelicopterLocation.LANDING);
        }
    }

    private void move(Point currentPosition, Point objective, HelicopterAgent ha) {
        Point res = currentPosition;
        Point old = new Point(currentPosition);
        if (currentPosition.x > objective.x) {
            res.x = res.x - 1;
        } else if (currentPosition.x < objective.x) {
            res.x = res.x + 1;
        }
        if (currentPosition.y > objective.y) {
            res.y = res.y - 1;
        } else if (currentPosition.y < objective.y) {
            res.y = res.y + 1;
        }
        if (res.equals(objective)) {
            ha.setLocation(HelicopterLocation.RESCUING);
        } else {
            int[] previous = {old.x, old.y};
            ha.setPreviousLocation(previous);
            ha.setCurrrentPosition(res);
        }
    }

}
