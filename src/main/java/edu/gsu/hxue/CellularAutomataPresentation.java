package edu.gsu.hxue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferStrategy;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides a visual presentation of 2D automata.
 *
 * @author Haidong Xue
 */
public class CellularAutomataPresentation extends Canvas implements Cloneable {
    private static final long serialVersionUID = 4608746494824151448L;

    private static final Color FRAME_COLOR = Color.gray;

    // cell space
    private Dimension spaceDimension;
    private Point canvasCoordinateOfSpaceCenter; // default space center coordinates
    private double scalar = 1; //default scalar

    // dimensions
    public int getXDim() {
        return spaceDimension.width;
    }

    public int getYDim() {
        return spaceDimension.height;
    }

    // coordinate transform variables
    AffineTransform customizedTrans = new AffineTransform();

    // the window
    JFrame frame;

    // the coordinate label
    JLabel label;

    // buffer strategy implementing multiple buffer drawing
    private BufferStrategy strategy;

    // drawing reference frame flag
    private boolean drawFrameOfReference = true;

    public boolean isDrawFrameOfReference() {
        return drawFrameOfReference;
    }

    public void setDrawFrameOfReference(boolean drawFrameOfReference) {
        this.drawFrameOfReference = drawFrameOfReference;
    }


    // cellular space color state
    private Color[][] cellsColor;
    private String[][] cellTexts;

    // cells needs to be updated
    private Set<Point> dirtyCells = new HashSet<Point>();

    public CellularAutomataPresentation(CellularAutomataPresentation original) {
        // set scalar
        this.scalar = original.scalar;

        // set space dimension
        spaceDimension = (Dimension) original.spaceDimension.clone();

        int cellNumberOnXDimension = spaceDimension.width;
        int cellNumberOnYDimension = spaceDimension.height;

        cellTexts = new String[cellNumberOnXDimension][cellNumberOnYDimension];
        cellsColor = new Color[cellNumberOnXDimension][cellNumberOnYDimension];
        for (int x = 0; x < cellNumberOnXDimension; x++)
            for (int y = 0; y < cellNumberOnYDimension; y++) {
                cellsColor[x][y] = original.cellsColor[x][y];
                cellTexts[x][y] = original.cellTexts[x][y];
            }

        // frame
        frame = new JFrame();

        // panel in the frame
        JPanel panel = (JPanel) frame.getContentPane();
        panel.setPreferredSize(new Dimension((int) (spaceDimension.width * scalar), (int) (spaceDimension.height * scalar))); // initialize the canvas dimension the same as the space dimension
        panel.setLayout(new BorderLayout()); // use BorderLayout
        panel.setToolTipText("hello");

        // mouse label
        label = new JLabel("Coordinates Indicator");
        panel.add(label, BorderLayout.NORTH);

        // canvas in the panel
        panel.add(this); // by default, it is at the CENTER position of the BorderLayout

        // disable repainting and going to use BufferStrategy
        this.setIgnoreRepaint(true);

        // add listeners
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //System.exit(0);
                frame.dispose();
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                canvasCoordinateOfSpaceCenter.setLocation(getWidth() / 2, getHeight() / 2);
                matchCenter();
                drawWholeSpaceInBuffer();
                showBufferOnScreen();
            }

        });


        MouseEventHandler mouseHandler = new MouseEventHandler(this.cellTexts);
        this.addMouseWheelListener(mouseHandler);
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
        this.addKeyListener(new KeyEvenHandler());

        // show the frame
        frame.setResizable(true);
        frame.setVisible(true);
        frame.pack();

        // create buffering strategy
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // initialize transform
        this.customizedTrans.scale(1, -1);
        this.customizedTrans.scale(scalar, scalar);
        this.customizedTrans.translate(0, this.spaceDimension.getHeight());

        // initialize space center
        canvasCoordinateOfSpaceCenter = new Point(this.getWidth() / 2, this.getHeight() / 2); // set the preferred center as the canvas center

        // draw initial space
        this.drawWholeSpaceInBuffer();

    }

    public CellularAutomataPresentation(int cellNumberOnXDimension, int cellNumberOnYDimension, double s, int iniX, int iniY) {
        //set scalar
        this.scalar = s;

        // set space dimension
        spaceDimension = new Dimension(cellNumberOnXDimension, cellNumberOnYDimension);

        // initialize cell texts
        cellTexts = new String[cellNumberOnXDimension][cellNumberOnYDimension];

        // initialize cellular space color (state)
        cellsColor = new Color[cellNumberOnXDimension][cellNumberOnYDimension];
        for (int x = 0; x < cellNumberOnXDimension; x++)
            for (int y = 0; y < cellNumberOnYDimension; y++)
                cellsColor[x][y] = Color.white;

        // frame
        frame = new JFrame();

        // panel in the frame
        JPanel panel = (JPanel) frame.getContentPane();
        panel.setPreferredSize(new Dimension((int) (spaceDimension.width * scalar), (int) (spaceDimension.height * scalar))); // initialize the canvas dimension the same as the space dimension
        panel.setLayout(new BorderLayout()); // use BorderLayout
        panel.setToolTipText("hello");

        // mouse label
        label = new JLabel("Coordinates Indicator");
        panel.add(label, BorderLayout.NORTH);

        // canvas in the panel
        panel.add(this); // by default, it is at the CENTER position of the BorderLayout

        // disable repainting and going to use BufferStrategy
        this.setIgnoreRepaint(true);

        // add listeners
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //System.exit(0);
                frame.dispose();
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                canvasCoordinateOfSpaceCenter.setLocation(getWidth() / 2, getHeight() / 2);
                matchCenter();
                drawWholeSpaceInBuffer();
                showBufferOnScreen();
            }

        });


        MouseEventHandler mouseHandler = new MouseEventHandler(this.cellTexts);
        this.addMouseWheelListener(mouseHandler);
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
        this.addKeyListener(new KeyEvenHandler());

        // show the frame
        frame.setResizable(true);
        frame.setVisible(true);
        frame.pack();
        frame.setLocation(iniX, iniY);

        // create buffering strategy
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // initialize transform
        this.customizedTrans.scale(1, -1);
        this.customizedTrans.scale(scalar, scalar);
        this.customizedTrans.translate(0, this.spaceDimension.getHeight());

        // initialize space center
        canvasCoordinateOfSpaceCenter = new Point(this.getWidth() / 2, this.getHeight() / 2); // set the preferred center as the canvas center

        // draw initial space
        this.drawWholeSpaceInBuffer();
    }

    public CellularAutomataPresentation(int cellNumberOnXDimension, int cellNumberOnYDimension, double s) {
        //set scalar
        this.scalar = s;

        // set space dimension
        spaceDimension = new Dimension(cellNumberOnXDimension, cellNumberOnYDimension);

        // initialize cell texts
        cellTexts = new String[cellNumberOnXDimension][cellNumberOnYDimension];

        // initialize cellular space color (state)
        cellsColor = new Color[cellNumberOnXDimension][cellNumberOnYDimension];
        for (int x = 0; x < cellNumberOnXDimension; x++)
            for (int y = 0; y < cellNumberOnYDimension; y++)
                cellsColor[x][y] = Color.white;

        // frame
        frame = new JFrame();

        // panel in the frame
        JPanel panel = (JPanel) frame.getContentPane();
        panel.setPreferredSize(new Dimension((int) (spaceDimension.width * scalar), (int) (spaceDimension.height * scalar))); // initialize the canvas dimension the same as the space dimension
        panel.setLayout(new BorderLayout()); // use BorderLayout
        panel.setToolTipText("hello");

        // mouse label
        label = new JLabel("Coordinates Indicator");
        panel.add(label, BorderLayout.NORTH);

        // canvas in the panel
        panel.add(this); // by default, it is at the CENTER position of the BorderLayout

        // disable repainting and going to use BufferStrategy
        this.setIgnoreRepaint(true);

        // add listeners
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //System.exit(0);
                frame.dispose();
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                canvasCoordinateOfSpaceCenter.setLocation(getWidth() / 2, getHeight() / 2);
                matchCenter();
                drawWholeSpaceInBuffer();
                showBufferOnScreen();
            }

        });


        MouseEventHandler mouseHandler = new MouseEventHandler(this.cellTexts);
        this.addMouseWheelListener(mouseHandler);
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
        this.addKeyListener(new KeyEvenHandler());

        // show the frame
        frame.setResizable(true);
        frame.setVisible(true);
        frame.pack();

        // create buffering strategy
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // initialize transform
        this.customizedTrans.scale(1, -1);
        this.customizedTrans.scale(scalar, scalar);
        this.customizedTrans.translate(0, this.spaceDimension.getHeight());

        // initialize space center
        canvasCoordinateOfSpaceCenter = new Point(this.getWidth() / 2, this.getHeight() / 2); // set the preferred center as the canvas center

        // draw initial space
        this.drawWholeSpaceInBuffer();
    }

    public CellularAutomataPresentation(int cellNumberOnXDimension, int cellNumberOnYDimension) {
        // set space dimension
        spaceDimension = new Dimension(cellNumberOnXDimension, cellNumberOnYDimension);

        cellTexts = new String[cellNumberOnXDimension][cellNumberOnYDimension];

        // initialize cellular space color (state)
        cellsColor = new Color[cellNumberOnXDimension][cellNumberOnYDimension];
        for (int x = 0; x < cellNumberOnXDimension; x++)
            for (int y = 0; y < cellNumberOnYDimension; y++)
                cellsColor[x][y] = Color.white;

        // frame
        frame = new JFrame();

        // panel in the frame
        JPanel panel = (JPanel) frame.getContentPane();
        panel.setPreferredSize(new Dimension(
                (int) (spaceDimension.width * scalar),
                (int) (spaceDimension.height * scalar)
        )); // initialize the canvas dimension the same as the space dimension
        panel.setLayout(new BorderLayout()); // use BorderLayout
        panel.setToolTipText("hello");

        // mouse label
        label = new JLabel("Coordinates Indicator");
        panel.add(label, BorderLayout.NORTH);

        // canvas in the panel
        panel.add(this);  // by default, it is at the CENTER position of the BorderLayout

        // disable repainting and going to use BufferStrategy
        this.setIgnoreRepaint(true);


        // add listeners
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //System.exit(0);
                frame.dispose();
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                canvasCoordinateOfSpaceCenter.setLocation(getWidth() / 2, getHeight() / 2);
                matchCenter();
                drawWholeSpaceInBuffer();
                showBufferOnScreen();
            }

        });


        MouseEventHandler mouseHandler = new MouseEventHandler(this.cellTexts);
        this.addMouseWheelListener(mouseHandler);
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
        this.addKeyListener(new KeyEvenHandler());


        // show the frame
        frame.setResizable(true);
        frame.setVisible(true);
        frame.pack();

        // create buffering strategy
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // initialize transform
        this.customizedTrans.scale(1, -1);
        this.customizedTrans.scale(scalar, scalar);
        this.customizedTrans.translate(0, this.spaceDimension.getHeight());

        // initialize space center
        canvasCoordinateOfSpaceCenter = new Point(this.getWidth() / 2, this.getHeight() / 2);  // set the preferred center as the canvas center

        // draw initial space
        this.drawWholeSpaceInBuffer();
    }

    public void setCellText(int x, int y, String text) {
        if (x < 0 || x >= this.spaceDimension.width || y < 0 || y >= this.spaceDimension.height)
            return;

        this.cellTexts[x][y] = text;
    }

    /**
     * Set the color of a cell. It is not drawn in the buffer.
     */
    public void setCellColor(int x, int y, Color c) {
        if (x < spaceDimension.width && x >= 0 && y < spaceDimension.height && y >= 0) {
            this.cellsColor[x][y] = c;
            this.dirtyCells.add(new Point(x, y));
        }
    }

    public void drawDirtyCellsInBuffer() {
        // get the graphic context
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

        // save original transform
        AffineTransform originalTrans = g.getTransform();

        // adjust coordinate system
        g.setTransform(this.customizedTrans);

        // draw
        for (Point p : dirtyCells) {
            g.setColor(cellsColor[p.x][p.y]);
            g.fillRect(p.x, p.y, 1, 1);
        }

        dirtyCells.clear();

        // draw a frame of reference
        if (drawFrameOfReference) {
            drawReferenceFrame(originalTrans, g);
        }
        // release the graphic context
        g.dispose();
    }

    public void drawACellInBuffer(int x, int y, Color c) {
        this.cellsColor[x][y] = c;

        // get the graphic context
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

        // save original transform
        AffineTransform originalTrans = g.getTransform();

        // adjust coordinate system
        g.setTransform(this.customizedTrans);

        // draw
        g.setColor(c);
        g.fillRect(x, y, 1, 1);

        // draw a frame of reference
        if (drawFrameOfReference)
            drawReferenceFrame(originalTrans, g);

        // release the graphic context
        g.dispose();

    }

    private void matchCenter() {
        // current space center
        Point2D spaceCenter = new Point2D.Double(spaceDimension.width / 2, spaceDimension.height / 2);
        customizedTrans.transform(spaceCenter, spaceCenter);

        // wanted space center
        Point2D wantedCenter = this.canvasCoordinateOfSpaceCenter;

        // move to the wanted center
        AffineTransform f = new AffineTransform();
        f.translate(wantedCenter.getX() - spaceCenter.getX(), wantedCenter.getY() - spaceCenter.getY());
        customizedTrans.preConcatenate(f);
    }

    private void drawWholeSpaceInBuffer() {
        // get the graphic context
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

        // clear the background
        g.setColor(Color.black);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        // save original transform
        AffineTransform originalTrans = g.getTransform();

        // adjust coordinate system
        g.setTransform(this.customizedTrans);


        // draw
        for (int x = 0; x < spaceDimension.width; x++)
            for (int y = 0; y < spaceDimension.height; y++) {
                g.setColor(this.cellsColor[x][y]);
                g.fillRect(x, y, 1, 1);
            }

        // draw a frame of reference
        if (drawFrameOfReference)
            drawReferenceFrame(originalTrans, g);

        // release the graphic context
        g.dispose();
    }

    private void drawReferenceFrame(AffineTransform originalTrans, Graphics2D g) {
        // prepare end points
        Point2D leftMid = new Point2D.Double(0, spaceDimension.height / 2);
        Point2D rightMid = new Point2D.Double(spaceDimension.width - 1, spaceDimension.height / 2);
        Point2D bottomMid = new Point2D.Double(spaceDimension.width / 2, 0);
        Point2D topMid = new Point2D.Double(spaceDimension.width / 2, spaceDimension.height - 1);

        // transform each point
        customizedTrans.transform(leftMid, leftMid);
        customizedTrans.transform(rightMid, rightMid);
        customizedTrans.transform(bottomMid, bottomMid);
        customizedTrans.transform(topMid, topMid);

        // set transform to original
        g.setTransform(originalTrans);

        // draw
        g.setColor(FRAME_COLOR);
        g.draw(new Line2D.Double(leftMid, rightMid));
        g.draw(new Line2D.Double(bottomMid, topMid));

        // set transform back
        g.setTransform(customizedTrans);
    }

    public void showBufferOnScreen() {
        strategy.show();
    }

    private class MouseEventHandler extends MouseAdapter {
        private int pressedX;
        private int pressedY;
        private String[][] texts;

        public MouseEventHandler(String[][] texts) {
            this.texts = texts;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.getWheelRotation() > 0) {
                customizedTrans.scale(2.0 / 3, 2.0 / 3);
            } else {
                customizedTrans.scale(1.5, 1.5);
            }

            // match center
            matchCenter();

            // redraw
            drawWholeSpaceInBuffer();

            // show
            showBufferOnScreen();

            System.out.println("Transform: " + customizedTrans);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            double dx = e.getX() - pressedX;
            double dy = e.getY() - pressedY;

            canvasCoordinateOfSpaceCenter.setLocation(canvasCoordinateOfSpaceCenter.getX() + dx, canvasCoordinateOfSpaceCenter.getY() + dy);
            // match center
            matchCenter();

            // draw
            drawWholeSpaceInBuffer();

            // show
            showBufferOnScreen();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.pressedX = e.getX();
            this.pressedY = e.getY();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point2D coordinates = new Point2D.Double(e.getX(), e.getY());
            try {

                customizedTrans.inverseTransform(coordinates, coordinates);
                String t = "";
                if ((int) coordinates.getX() >= 0 || (int) coordinates.getX() < texts.length || (int) coordinates.getY() >= 0 || (int) coordinates.getY() < texts[(int) coordinates.getX()].length)
                    if (texts != null && (int) coordinates.getX() >= 0 && (int) coordinates.getX() < texts.length && (int) coordinates.getY() >= 0 && (int) coordinates.getY() < texts[(int) coordinates.getX()].length)
                        t = texts[(int) coordinates.getX()][(int) coordinates.getY()];
                String text = "x=" + (int) coordinates.getX() + " y=" + (int) coordinates.getY() + " " + t;
                label.setText(text);

            } catch (NoninvertibleTransformException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class KeyEvenHandler implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            System.out.println(e.getKeyChar() + " is typed");
            if (e.getKeyChar() == '-') {
                customizedTrans.scale(2.0 / 3, 2.0 / 3);
                // match center
                matchCenter();

                // redraw
                drawWholeSpaceInBuffer();

                // show
                showBufferOnScreen();

                System.out.println("Transform: " + customizedTrans);
            } else if (e.getKeyChar() == '=') {
                customizedTrans.scale(1.5, 1.5);
                // match center
                matchCenter();

                // redraw
                drawWholeSpaceInBuffer();

                // show
                showBufferOnScreen();

                System.out.println("Transform: " + customizedTrans);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            System.out.println(e.getKeyChar() + " is pressed");
        }

        @Override
        public void keyReleased(KeyEvent e) {
            System.out.println(e.getKeyChar() + " is released");
        }


    }


    public void setTitle(String string) {
        frame.setTitle(string);

    }

}
