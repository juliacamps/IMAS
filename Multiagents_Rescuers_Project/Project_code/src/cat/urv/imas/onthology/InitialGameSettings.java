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
import cat.urv.imas.agent.UtilsAgents;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.HospitalCell;
import cat.urv.imas.map.MountainCell;
import cat.urv.imas.map.MountainHutCell;
import jade.core.AID;
import jade.wrapper.AgentContainer;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Initial game settings and automatic loading from file.
 *
 * Use the GenerateGameSettings to build the game.settings configuration file.
 */
@XmlRootElement(name = "InitialGameSettings")
public class InitialGameSettings extends GameSettings {

    /*
     * Constants that define the type of content into the initialMap.
     * Any other value in a cell means that a cell is a building and
     * the value is the number of people in it.
     *
     * Cells with mobile vehicles are street cells after vehicles
     * move around.
     */
    /**
     * Path cell.
     */
    public static final int P = 0;
    /**
     * Hospital cell.
     */
    public static final int H = -1;
    /**
     * Mountain cell.
     */
    public static final int M = -2;
    /**
     * Mountain hut cell.
     */
    public static final int MH = -3;

    /**
     * City initialMap. Each number is a cell. The type of each is expressed by
     * a constant (if a letter, see above), or a building (indicating the number
     * of people in that building).
     */
    private int[][] initialMap
            = {
                {M, M, M, M, M, M, M, M, M, M, M, M, M, M, M, M, M, M, M, M},
                {M, MH, P, P, P, P, P, P, P, P, P, P, P, P, P, P, P, P, P, M},
                {M, M, P, M, M, M, M, P, M, M, P, M, M, M, M, M, M, M, P, M},
                {M, M, P, M, M, M, M, P, MH, M, P, M, M, M, M, M, M, M, P, M},
                {M, M, P, M, M, M, M, P, M, M, P, M, M, M, M, M, M, M, P, M},
                {M, M, P, M, M, P, P, P, P, P, P, M, M, P, P, P, P, P, P, M},
                {M, P, P, M, M, P, M, M, M, M, P, M, M, P, M, M, M, MH, P, M},
                {M, P, M, M, M, P, M, M, M, P, P, M, M, P, P, M, M, M, P, M},
                {M, P, M, M, M, P, M, M, M, P, M, M, M, M, P, M, M, M, P, M},
                {M, P, M, M, M, P, M, M, M, P, M, M, M, M, P, M, M, M, P, M},
                {M, P, M, M, M, P, M, M, M, P, M, M, M, M, P, M, M, M, P, M},
                {M, P, M, M, M, P, M, M, M, P, M, M, M, M, P, M, M, M, P, M},
                {M, P, MH, M, M, P, M, M, M, P, M, M, M, MH, P, H, M, P, P, M},
                {M, P, P, M, M, P, M, M, M, P, P, M, M, M, P, M, M, P, M, M},
                {M, M, P, M, M, P, M, M, M, M, P, M, M, M, P, M, M, P, M, M},
                {M, M, P, M, M, P, M, M, M, M, P, M, M, M, P, M, M, P, M, M},
                {M, P, P, M, M, P, P, M, M, P, P, M, M, M, P, M, M, P, P, M},
                {M, P, M, M, H, M, P, MH, M, P, M, M, M, M, P, M, M, M, P, M},
                {M, P, P, P, P, P, P, M, M, P, P, P, P, P, P, M, M, MH, P, M},
                {M, M, M, M, M, M, M, M, M, M, M, M, M, M, M, M, M, M, M, M},};

    /**
     * Each number in the list means the number of the helicopters appearing on
     * each hospital, in the order that appear in the map.
     */
    private List<Integer> numberOfHelicoptersPerHospital = new LinkedList();
    /**
     * Each number in the list means the number of rural agents appearing in
     * each mountain hut, in the order they appear in the map.
     */
    private List<Integer> numberOfRuralAgentsPerMountainHut = new LinkedList();

    public int[][] getInitialMap() {
        return initialMap;
    }

    @XmlElement(required = true)
    public void setInitialMap(int[][] initialMap) {
        this.initialMap = initialMap;
    }

    public List<Integer> getNumberOfHelicoptersPerHospital() {
        return numberOfHelicoptersPerHospital;
    }

    @XmlElement(required = true)
    public void setNumberOfHelicoptersPerHospital(List<Integer> numberOfHelicoptersPerHospital) {
        this.numberOfHelicoptersPerHospital = numberOfHelicoptersPerHospital;
    }

    public List<Integer> getNumberOfRuralAgentsPerMountainHut() {
        return numberOfRuralAgentsPerMountainHut;
    }

    @XmlElement(required = true)
    public void setNumberOfRuralAgentsPerMountainHut(List<Integer> numberOfRuralAgentsPerMountainHut) {
        this.numberOfRuralAgentsPerMountainHut = numberOfRuralAgentsPerMountainHut;
    }

    public InitialGameSettings() {
        // place a helicopter in each hospital
        numberOfHelicoptersPerHospital.add(1);
        numberOfHelicoptersPerHospital.add(1);
        // place a rural agent in each mountain hut
        numberOfRuralAgentsPerMountainHut.add(1);
        numberOfRuralAgentsPerMountainHut.add(1);
        numberOfRuralAgentsPerMountainHut.add(1);
        numberOfRuralAgentsPerMountainHut.add(1);
        numberOfRuralAgentsPerMountainHut.add(1);
        numberOfRuralAgentsPerMountainHut.add(1);
        numberOfRuralAgentsPerMountainHut.add(1);
    }

    public static final GameSettings load(String filename) {
        if (filename == null) {
            filename = "game.settings";
        }
        try {
            // create JAXBContext which will be used to update writer
            JAXBContext context = JAXBContext.newInstance(InitialGameSettings.class);
            Unmarshaller u = context.createUnmarshaller();
            InitialGameSettings starter = (InitialGameSettings) u.unmarshal(new FileReader(filename));
            starter.initMap();
            return starter;
        } catch (Exception e) {
            System.err.println(filename);
            System.exit(-1);
        }
        return null;
    }

    /**
     * Initializes the cell map.
     *
     * @throws Exception if some error occurs when adding agents.
     */
    private void initMap() throws Exception {       
        int rows = this.initialMap.length;
        int cols = this.initialMap[0].length;
        map = new Cell[rows][cols];
        this.agentList = new HashMap();
        int cell;
        Cell c;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cell = initialMap[row][col];
                switch (cell) {
                    case P:
                        c = new PathCell(row, col);
                        map[row][col] = c;
                        break;
                    case H:
                        c = new HospitalCell(row, col);
                        map[row][col] = c;

                        addAgentToList(AgentType.HELICOPTER, c);
                        break;
                    case M:
                        c = new MountainCell(row, col);
                        map[row][col] = c;
                        break;
                    case MH:
                        c = new MountainHutCell(row, col);
                        map[row][col] = c;
                        addAgentToList(AgentType.RURAL_AGENT, c);
                        break;
                }
            }
        }
     }

    /**
     * Ensure agent list is correctly updated.
     *
     * @param type agent type.
     * @param cell cell where appears the agent.
     */
    private void addAgentToList(AgentType type, Cell cell) {
        List<Cell> list = this.agentList.get(type);
        if (list == null) {
            list = new ArrayList();
            this.agentList.put(type, list);
        }
        list.add(cell);
    }
}
