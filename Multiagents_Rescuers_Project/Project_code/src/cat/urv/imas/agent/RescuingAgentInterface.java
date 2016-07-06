package cat.urv.imas.agent;

import cat.urv.imas.map.InjuredPerson;
import cat.urv.imas.onthology.Proposal;

/**
 * Methods to be implemented by both HelicopterAgent RuralAgent.
 * 
 * @author Vicent
 */
public interface RescuingAgentInterface {    
    public Proposal eval(InjuredPerson ip);          /* cost function */
    public boolean rescue();        /* pickup injured people from a certain cell of the map */
    public boolean rescueCompleted();
    public void setObjective(Proposal obj);
    public InjuredPerson getObjective();
    public int[] getCurrentLocation(); /* necessary function for the painting process */ 
    public int[] getPreviousLocation();
}