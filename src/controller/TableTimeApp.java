package controller;

import model.Restaurant;
import view.TableTimeWindow;

/**
 * Starts Table Time: builds one model, one view and one controller and
 * wires them together.
 *
 * @author Sieon Lee
 * @version 2026.11.11
 */
public class TableTimeApp {
    /**
     * @param args not used
     */
    public static void main(String[] args) {
        Restaurant r = new Restaurant();
        TableTimeWindow w = new TableTimeWindow(r);
        w.setController(new HostController(r, w));
        w.setVisible(true);
    }
}
