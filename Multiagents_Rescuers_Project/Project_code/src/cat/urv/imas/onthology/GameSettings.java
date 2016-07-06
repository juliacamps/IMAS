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
package cat.urv.imas.onthology;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.InjuredPerson;
import jade.core.AID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Current game settings. Cell coordinates are zero based: row and column values
 * goes from [0..n-1], both included.
 *
 * Use the GenerateGameSettings to build the game.settings configuration file.
 *
 */
@XmlRootElement(name = "GameSettings")
public class GameSettings implements java.io.Serializable {

    /* Default values set to all attributes, just in case. */
    /**
     * Seed for random numbers.
     */
    private float seed = 0.0f;
    /**
     * Capacity of helicopter, in number of people.
     */
    private int peoplePerHelicopter = 2;
    /**
     * Number of people loaded into a helicopter per simulation step.
     */
    private int loadingSpeed = 1;
    /**
     * Number of steps an avalanche can take long.
     */
    private int avalancheDuration = 5;
    /**
     * After this number of steps, injured people will die.
     */
    private int stepsToFreeze = 20;
    /**
     * This sets the light severity ratio of injured people. Grave severity will
     * be 100 - lightSeverity.
     */
    private int lightSeverity = 90;
    /**
     * Cost (think about money) for each person rescued by a rural agent.
     */
    private int ruralAgentCost = 1;
    /**
     * Cost (think about money) for each person rescued by a helicopter.
     */
    private int helicopterCost = 20;
    /**
     * Total number of simulation steps.
     */
    private int simulationSteps = 100;
    /**
     * City map.
     */
    protected Cell[][] map;
    /**
     * Computed summary of the position of agents in the city. For each given
     * type of mobile agent, we get the list of their positions.
     */
    protected Map<AgentType, List<Cell>> agentList;
    /**
     * Computed summary of the available list of injured people.
     */
    protected List<InjuredPerson> injuredPeople;
    /**
     * Title to set to the GUI.
     */
    protected String title = "Demo title";

    public float getSeed() {
        return seed;
    }

    @XmlElement(required = true)
    public void setSeed(float seed) {
        this.seed = seed;
    }

    public int getSimulationSteps() {
        return simulationSteps;
    }

    @XmlElement(required = true)
    public void setSimulationSteps(int simulationSteps) {
        this.simulationSteps = simulationSteps;
    }

    public String getTitle() {
        return title;
    }

    @XmlElement(required = true)
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the full current city map.
     *
     * @return the current city map.
     */
    @XmlTransient
    public Cell[][] getMap() {
        return map;
    }

    /**
     * Gets the cell given its coordinate.
     *
     * @param row row number (zero based)
     * @param col column number (zero based).
     * @return a city's Cell.
     */
    public Cell get(int row, int col) {
        return map[row][col];
    }

    public int getPeoplePerHelicopter() {
        return peoplePerHelicopter;
    }

    @XmlElement(required = true)
    public void setPeoplePerHelicopter(int peoplePerHelicopter) {
        this.peoplePerHelicopter = peoplePerHelicopter;
    }

    public int getLoadingSpeed() {
        return loadingSpeed;
    }

    @XmlElement(required = true)
    public void setLoadingSpeed(int loadingSpeed) {
        this.loadingSpeed = loadingSpeed;
    }

    public int getAvalancheDuration() {
        return avalancheDuration;
    }

    @XmlElement(required = true)
    public void setAvalancheDuration(int avalancheDuration) {
        this.avalancheDuration = avalancheDuration;
    }

    public int getStepsToFreeze() {
        return stepsToFreeze;
    }

    @XmlElement(required = true)
    public void setStepsToFreeze(int stepsToFreeze) {
        this.stepsToFreeze = stepsToFreeze;
    }

    public int getLightSeverity() {
        return lightSeverity;
    }

    @XmlElement(required = true)
    public void setLightSeverity(int lightSeverity) {
        this.lightSeverity = lightSeverity;
    }

    public int getRuralAgentCost() {
        return ruralAgentCost;
    }

    @XmlElement(required = true)
    public void setRuralAgentCost(int ruralAgentCost) {
        this.ruralAgentCost = ruralAgentCost;
    }

    public int getHelicopterCost() {
        return helicopterCost;
    }

    @XmlElement(required = true)
    public void setHelicopterCost(int helicopterCost) {
        this.helicopterCost = helicopterCost;
    }

    @XmlTransient
    public Map<AgentType, List<Cell>> getAgentList() {
        return agentList;
    }

    @XmlTransient
    public List<InjuredPerson> getInjuredPeople() {
        if (injuredPeople == null) {
            injuredPeople = new ArrayList<>();
        }
        return injuredPeople;
    }

    public void setInjuredPeople(List<InjuredPerson> injuredPeople) {
        this.injuredPeople = injuredPeople;
    }

    public void addInjuredPeople(List<InjuredPerson> toAdd) {
        getInjuredPeople().addAll(toAdd);
        Collections.sort(injuredPeople);
    }

    public String toString() {
        //TODO: show a human readable summary of the game settings.
        return "Game settings";
    }

    public String getShortString() {
        //TODO: list of agents and other relevant settings.
        return "Game settings: agent related string";
    }

    public void setAgentList(Map<AgentType, List<Cell>> agentList) {
        this.agentList = agentList;
    }

    public void updateInjuredPeople(HashMap<InjuredPerson, AID> assignedRescuers) {
        for (InjuredPerson ip : assignedRescuers.keySet()) {
            injuredPeople.get(injuredPeople.indexOf(ip)).setAsignedRescuer(assignedRescuers.get(ip));
            injuredPeople.get(injuredPeople.indexOf(ip)).setIsAsignedRescuer(true);
        }
    }

}
