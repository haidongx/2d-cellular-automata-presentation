package edu.gsu.hxue;

import java.awt.*;

public class Test {
    public static void main(String[] args) {
        int x = 500;
        int y = 500;
        CellularAutomataPresentation c = new CellularAutomataPresentation(x, y);

        c.drawACellInBuffer(260, 260, Color.black);
        c.showBufferOnScreen();

        c.drawACellInBuffer(240, 240, Color.red);
        c.showBufferOnScreen();

        c.drawACellInBuffer(250, 250, Color.yellow);
        c.showBufferOnScreen();
    }
}
