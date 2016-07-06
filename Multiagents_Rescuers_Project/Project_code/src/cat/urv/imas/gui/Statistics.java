/**
 * IMAS base code for the practical work. 
 * Copyright (C) 2014 DEIM - URV
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
package cat.urv.imas.gui;

/**
 * Game statistics
 */
public class Statistics {
    
    /**
     * Total cost for saving people.
     */
    private int totalCost = 0;
    /**
     * Number of people brought to hospitals.
     */
    private int rescuedPeopleAtHospitals = 0;
    /**
     * Number of people brought to mountain huts.
     */
    private int rescuedPeopleAtMountainHuts = 0;
    /**
     * Number of people died.
     */
    private int diedPeople = 0;
    /**
     * Number of rescues accounted as the first element arriving into a cell
     * with injured people.
     */
    private int numberOfFirstRescues = 0;
    /**
     * Number of simulation steps to get to a cell with injured people for the
     * first rescuing element (rural agent or helicopter).
     */
    private int stepsForFirstRescue = 0;
    /**
     * Cost for saving people with rural agents.
     */
    private final int costPerRuralAgent;
    /**
     * Cost for saving people with helicopters.
     */
    private final int costPerHelicopter;
    
    public Statistics(int costPerRuralAgent, int costPerHelicopter) {
        this.costPerRuralAgent = costPerRuralAgent;
        this.costPerHelicopter = costPerHelicopter;
    }
    
    /**
     * Adds a new rescue of "amount" people by helicopters.
     * @param amount number of rescued people.
     */
    public void addHospitalRescue(int amount) {
        rescuedPeopleAtHospitals += amount;
        totalCost += amount * costPerHelicopter;
    }
    
    /**
     * Adds a new rescue of "amount" people by rural agents.
     * @param amount number of rescued people.
     */
    public void addRuralAgentRescue(int amount) {
        rescuedPeopleAtMountainHuts += amount;
        totalCost += amount * costPerRuralAgent;
    }
    
    /**
     * Total cost for the whole set of rescues.
     * @return cost of all rescues.
     */
    public int getTotalCost() {
        return totalCost;
    }
    
    /**
     * Cost per person from rescues.
     * @return cost per person from rescues.
     */
    public float getCostRatio() {
        return (getRescuedPeople() != 0)
                ? (float)totalCost/(float)getRescuedPeople()
                : 0;
    }
    
    /**
     * Informs the number of rescued people.
     * @return number of rescued people.
     */
    public int getRescuedPeople() {
        return rescuedPeopleAtHospitals + rescuedPeopleAtMountainHuts;
    }
    
    /**
     * Gets the ratio of the rescued people among rescued and died people.
     * @return ratio of rescued people.
     */
    public float getRescuedPeopleRatio() {
        return (getRescuedPeople() != 0)
                ? (float)(getRescuedPeople() + diedPeople)/(float)getRescuedPeople()
                : 0;
    }
    
    /**
     * Gets the number of died people during the simulation.
     * @return number of died people.
     */
    public int getDiedPeople() {
        return diedPeople;
    }
    
    /**
     * Adds the "amount" of died people.
     * @param amount number of died people in this step.
     */
    public void addDiedPeople(int amount) {
        diedPeople += amount;
    }
    
    /**
     * Adds a first visit into a cell with new injured people.
     * @param steps number of steps to get to that cell for first time.
     */
    public void addFirstVisit(int steps) {
        numberOfFirstRescues ++;
        this.stepsForFirstRescue += steps;
    }
    
    /**
     * Informs the average of the number of steps to get for the first time
     * to a cell with injured people.
     * @return 
     */
    public float getFirstVisitAverage() {        
        return (numberOfFirstRescues!=0)
                ? (float)stepsForFirstRescue/(float)numberOfFirstRescues
                : 0;
    }
    
    /**
     * Gets the ratio of rescued people at mountain huts.
     * @return ratio of rescued people at mountain huts.
     */
    public float getRescuedPeopleAtMountainHuts() {
        return (rescuedPeopleAtMountainHuts != 0) 
                ? (float)getRescuedPeople() / (float)rescuedPeopleAtMountainHuts
                : 0;
    }
    
    /**
     * Gets the ratio of rescued people at hostpitals.
     * @return ratio of rescued people at hospitals.
     */
    public float getRescuedPeopleAtHospitals() {
        return (rescuedPeopleAtHospitals != 0)
                ? (float)getRescuedPeople() / (float)rescuedPeopleAtHospitals
                : 0;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newline = System.getProperty("line.separator");
        sb.append("Total cost: " + getTotalCost() + newline);
        sb.append("Ratio of cost per person: " + this.getCostRatio() + newline);
        sb.append("Died people: " + this.getDiedPeople() + newline);
        sb.append("Rescued people: " + this.getRescuedPeople() + newline);
        sb.append("Ratio of rescued people: " + this.getRescuedPeopleRatio() + newline);
        sb.append("Average of steps for first visit: " + this.getFirstVisitAverage() + newline);
        sb.append("People ratio brought to hospitals: " + this.getRescuedPeopleAtHospitals() + newline);
        sb.append("People ratio brought to mountain huts: " + this.getRescuedPeopleAtMountainHuts() + newline);
        return sb.toString();
    }
}
