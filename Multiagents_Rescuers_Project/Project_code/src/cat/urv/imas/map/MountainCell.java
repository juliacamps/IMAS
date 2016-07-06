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
package cat.urv.imas.map;

import cat.urv.imas.gui.CellVisualizer;

/**
 * Building cell.
 */
public class MountainCell extends Cell {

    /**
     * Builds a cell corresponding to mountain.
     *
     * @param row row number.
     * @param col column number.
     */
    public MountainCell(int row, int col) {
        super(CellType.MOUNTAIN, row, col);
    }

    /* ***************** Map visualization API ********************************/
    
    @Override
    public void draw(CellVisualizer visual) {
        visual.drawMountain(this);
    }
    @Override
    public String getMapMessage() {
        String str = super.getMapMessage();
        return str;
    }
}
