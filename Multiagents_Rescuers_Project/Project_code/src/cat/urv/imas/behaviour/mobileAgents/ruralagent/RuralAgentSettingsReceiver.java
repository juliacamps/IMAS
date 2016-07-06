/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.mobileAgents.ruralagent;

import cat.urv.imas.agent.RuralAgent;
import cat.urv.imas.behaviour.coordinator.ReceiveSettingsBehaviour;
import cat.urv.imas.onthology.GameSettings;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arkard
 */
public class RuralAgentSettingsReceiver extends CyclicBehaviour{
    
    private MessageTemplate mt;

    public RuralAgentSettingsReceiver(Agent a, MessageTemplate mt) {
        super(a);
        this.mt = mt;
    }
    
    
    @Override
    public void action() {
        ACLMessage inf = myAgent.receive(mt);
        if (inf != null) {
            try {
                RuralAgent agent = (RuralAgent) myAgent;
                GameSettings game = (GameSettings) inf.getContentObject();
                agent.setGame(game);
                agent.log(game.getShortString());

            } catch (UnreadableException ex) {
                Logger.getLogger(ReceiveSettingsBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        block();
    }
}
