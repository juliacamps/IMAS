/**
 * IMAS base code for the practical work. Copyright (C) 2015 DEIM - URV
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
package cat.urv.imas.map;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

/**
 * List of injured people in a cell.
 *
 * An injured person is stated into a list as a number. The number means the
 * simulation step the person is going to die, according to simulation settings.
 */
public class InjuredPeople implements java.io.Serializable {

    /**
     * List of injured people.
     */
    private LinkedList<InjuredPerson> people = new LinkedList();

    /**
     * Appends the "amount" of injured people to the existing list, setting that
     * they will die on "stepToDie".
     *
     * @param amount number of injured people to add.
     * @param stepToDie simulation step that these people are going to die.
     * @param severities list of severity of the injured people
     * @param c cell where the injured people is located
     */
    public void addInjuredPeople(int amount, int stepToDie, List<Boolean> severities, Cell c, int step) {
        for (int i = 0; i < amount; i++) {
            people.add(new InjuredPerson(stepToDie, severities.get(i), new Point(c.getRow(), c.getCol()), step));
        }
    }

    public void addInjuredPeople(InjuredPerson p) {
        people.add(p);

    }

    /**
     * Saving an injured person means removing the first person from the
     * existing set of injured people.
     */
    public void saveInjuredPerson(InjuredPerson ip) {
        people.remove(ip);
    }

    /**
     * Tell whether there is at least an injured person in this place.
     *
     * @return true if there is at least an injured person in this place. false
     * otherwise.
     */
    public boolean isThereInjuredPeople() {
        return !people.isEmpty();
    }

    /**
     * Tell the number of simulation steps remaining before at least a person is
     * going to die.
     *
     * @param currentStep current simulation step.
     * @return number of steps before a person is going to die.
     * @throws Exception if there is no people in this place.
     */
    public int firstToDie(int currentStep) throws Exception {
        if (people.isEmpty()) {
            throw new Exception("There is no injured people!");
        }
        return people.getFirst().getStepToDie() - currentStep;
    }

    /**
     * Removes all died people from this list.
     *
     * @param currentStep current simulation step.
     * @return number of died people.
     */
    public int cleanDiedPeople(int currentStep) {
        int died = 0;
        // having the same step in the current step and the first element
        // means it is the last step that person is alive.
        while (!people.isEmpty() && people.getFirst().getStepToDie() < currentStep) {
            people.remove();
            died++;
        }
        return died;
    }

    public int getAmount() {
        return people.size();
    }
}
