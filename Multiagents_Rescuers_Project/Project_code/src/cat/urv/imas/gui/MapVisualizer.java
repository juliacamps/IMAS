package cat.urv.imas.gui;

import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.RenderingHints;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.HospitalCell;
import cat.urv.imas.map.MountainCell;
import cat.urv.imas.map.MountainHutCell;
import cat.urv.imas.map.PathCell;

/**
 * Visualization of the map. There are several elements to depict, as buildings,
 * streets, the agents, and the rest of items.<br>
 * This class *should be* modified and improved in order to show as good as
 * possible all the changes in the simulation. We provide several high-level
 * methods which can be rewritten as needed.<br>
 *
 * <b>Company:</b> Universitat Rovira i Virgili (<a
 * href="http://www.urv.cat">URV</a>)
 */
public class MapVisualizer extends JPanel implements CellVisualizer {

    /**
     * Margin area surrouding the city map.
     */
    private static final int INSET = 50;
    /**
     * Cell gap.
     */
    private static final int GAP = 5;
    /**
     * City map.
     */
    private final Cell[][] map;
    /**
     * Cell width.
     */
    private final int dx;
    /**
     * Cell height.
     */
    private final int dy;
    /**
     * Dimensions for the cell's border.
     */
    private final Rectangle2D.Double cellBorder;
    /**
     * Graphics when painting the city map.
     *
     * @see cat.urv.imas.gui.CellVisualizer
     */
    private Graphics2D temporaryGraphics;

    /**
     * Initializes values for a correct map painting.
     *
     * @param map
     */
    public MapVisualizer(Cell[][] map) {
        this.map = map;
        int nrows = map.length;
        int ncols = map[0].length;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point start = new Point(INSET, INSET);
        Point end = new Point(screenSize.width - INSET * 2, screenSize.height - INSET * 2);

        dx = (end.x - start.x) / ncols;
        dy = ((end.y - start.y) / nrows) - 4;

        cellBorder = new Rectangle2D.Double(GAP + 10, GAP + 10, dx, dy);

    }

    /**
     * Show the whole city map.
     *
     * @param graphics
     */
    @Override
    public void paintComponent(Graphics graphics) {
        Graphics2D g2d = (Graphics2D) graphics;

        // 0. initializing graphic element.
        clear(graphics);

        // 1. set graphics for painting
        this.updateGraphics(g2d);

        // 2. iterate cells and paint them.
        for (Cell[] row : map) {

            for (Cell cell : row) {
                // 2.1. draw the border.
                g2d.draw(cellBorder);
                // 2.2. draw the cell
                cell.draw(this);
                // 2.3. move on
                g2d.translate(dx, 0);
            }

            // 2.4. move on
            g2d.translate(-(dx * row.length), dy);
        }

        // 5. unsetting graphics
        this.updateGraphics(null);
        // 6. painting reinforcement
        this.repaint();
    }

    protected void clear(Graphics g) {
        super.paintComponent(g);
    }

    protected void drawAgent(Color fillingColor, String message, Color textColor) {
        drawString(message, textColor, dx - 40, dy - 10);
    }

    /**
     * Just draws a cell with the given filling in color, and the given border
     * color.
     *
     * @param fillingColor cell's color
     * @param borderColor color for the cell's border
     */
    protected void drawCell(Color fillingColor, Color borderColor) {
        temporaryGraphics.setPaint(fillingColor);
        temporaryGraphics.fill(cellBorder);
        temporaryGraphics.setPaint(borderColor);
        temporaryGraphics.draw(cellBorder);
    }

    /**
     * Draws the message into the cell, in the given position within the cell.
     *
     * @param message text to show.
     * @param textColor color for the text
     * @param x some value related to dx
     * @param y some value realted to dy
     */
    protected void drawString(String message, Color textColor, int x, int y) {
        if (!message.equals("")) {
            temporaryGraphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Font font = new Font("Serif", Font.PLAIN, 11);
            temporaryGraphics.setFont(font);
            temporaryGraphics.setPaint(textColor);
            temporaryGraphics.drawString(message, x, y);
        }
    }

    /* ****************** CellVisualizer implementation ********************* */
    @Override
    public void updateGraphics(Graphics2D graphics) {
        this.temporaryGraphics = graphics;
    }

    @Override
    public void drawPath(PathCell cell) {
        drawEmptyCell(cell);
        drawString(cell.getMapMessage(), Color.BLACK, dx - 40, dy);
    }

    @Override
    public void drawAvalanche(PathCell cell) {
        drawCell(Color.WHITE, Color.DARK_GRAY);
    }

    @Override
    public void drawMountain(MountainCell cell) {
        drawCell(Color.GREEN.darker(), Color.GREEN.darker());
        drawString(cell.getMapMessage(), Color.BLACK, dx - 40, dy);
    }

    @Override
    public void drawMountainHut(MountainHutCell cell) {
        drawCell(Color.orange.darker(), Color.GRAY.darker());
        drawString(cell.getMapMessage(), Color.BLACK, dx - 40, dy);
    }

    @Override
    public void drawHospital(HospitalCell cell) {
        drawCell(Color.PINK.brighter(), Color.DARK_GRAY);
        drawString(cell.getMapMessage(), Color.BLACK, dx - 40, dy);
    }

    private void drawEmptyCell(PathCell cell) {
        drawCell(Color.LIGHT_GRAY, Color.DARK_GRAY);
    }
}
