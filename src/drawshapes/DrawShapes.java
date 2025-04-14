package drawshapes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class DrawShapes extends JFrame
{
    public enum ShapeType {
        SQUARE,
        CIRCLE,
        RECTANGLE
    }
    
    // what operations can be done on shapes?
    public enum OperationState {
        MOVE,
        SCALE,
        DEPTH //change the order that the shapes are drawn
    }
    
    private DrawShapesPanel shapePanel;
    private Scene scene;
    private ShapeType shapeType = ShapeType.SQUARE;
    private OperationState activeOperation = null; //what operation is active? (will be affected by arrow keys)
    private Color color = Color.RED;

    //list of previous scenes, to be lengthened as operations happen, undo removes the most recent addition
    private Deque<Scene> history =new LinkedList<Scene>();

    public DrawShapes(int width, int height)
    {
        setTitle("Draw Shapes!");
        scene=new Scene();
        
        // create our canvas, add to this frame's content pane
        shapePanel = new DrawShapesPanel(width,height,scene);
        this.getContentPane().add(shapePanel, BorderLayout.CENTER);
        this.setResizable(false);
        this.pack();
        this.setLocation(100,100);
        //initialize history to an empty scene (so you can undo the first action)
        cacheScene();
        
        // Add key and mouse listeners to our canvas
        initializeMouseListener();
        initializeKeyListener();
        
        // initialize the menu options
        initializeMenu();

        // Handle closing the window.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    /** Should be called to update the scene (don't change scene var directly)
     * 
     * @return 
     */
    private void updateScene(Scene scene){
        this.scene = scene;
        shapePanel.setScene(scene);
        repaint(); //the scene has changed, it needs to be shown
    }


    /** Should be called whenever an action is taken, will save the previous Scene to the history stack
     * 
     * @return 
     */
    private void cacheScene(){
        history.addFirst(scene); //store the current scene
        System.out.println("This scene was stored then a copy was made");
        System.out.println(scene);
        //make a copy of scene to be changed by further edits
        updateScene(scene.copy());
    }
    
    private void initializeMouseListener()
    {
        MouseAdapter a = new MouseAdapter() {
            
            public void mouseClicked(MouseEvent e)
            {
                System.out.printf("Mouse clicked at (%d, %d)\n", e.getX(), e.getY());
                
                if (e.getButton()==MouseEvent.BUTTON1) { 
                    cacheScene(); //adding shapes can be undone
                    if (shapeType == ShapeType.SQUARE) {
                        scene.addShape(new Square(color, 
                                e.getX(), 
                                e.getY(),
                                100));
                    } else if (shapeType == ShapeType.CIRCLE){
                        scene.addShape(new Circle(color,
                                e.getPoint(),
                                100));
                    } else if (shapeType == ShapeType.RECTANGLE) {
                        scene.addShape(new Rectangle(
                                e.getPoint(),
                                150, 
                                100,
                                color));
                    } 
                } else if (e.getButton()==MouseEvent.BUTTON2) {
                    // apparently this is middle click
                } else if (e.getButton()==MouseEvent.BUTTON3){
                    // right right-click
                    Point p = e.getPoint();
                    System.out.printf("Right click is (%d, %d)\n", p.x, p.y);
                    List<IShape> selected = scene.select(p);
                    if (selected.size() > 0){
                        for (IShape s : selected){
                            s.setSelected(true);
                        }
                    } else {
                        for (IShape s : scene){
                            s.setSelected(false);
                        }
                    }
                    System.out.printf("Select %d shapes\n", selected.size());
                }
                repaint();
            }
            
            /* (non-Javadoc)
             * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
             */
            public void mousePressed(MouseEvent e)
            {
                System.out.printf("mouse pressed at (%d, %d)\n", e.getX(), e.getY());
                scene.startDrag(e.getPoint());
                
            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
             */
            public void mouseReleased(MouseEvent e)
            {
                System.out.printf("mouse released at (%d, %d)\n", e.getX(), e.getY());
                scene.stopDrag();
                repaint();
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.printf("mouse drag! (%d, %d)\n", e.getX(), e.getY());
                scene.updateSelectRect(e.getPoint());
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) { //mouse scrolls dont work on touchpad T_T
                System.out.printf("mouse scrolled! (%d)\n", e.getWheelRotation());
                if (e.getWheelRotation() > 0){
                    cacheScene();
                    for (IShape shape : scene.selected()) {
                        shape.scaleUp();
                    }
                } else if (e.getWheelRotation() < 0){
                    cacheScene();
                    for (IShape shape : scene.selected()) {
                        shape.scaleDown();
                    }
                }

            }
            
        };
        shapePanel.addMouseMotionListener(a);
        shapePanel.addMouseListener(a);
    }
    
    /**
     * Initialize the menu options
     */
    private void initializeMenu()
    {
        // menu bar
        JMenuBar menuBar = new JMenuBar();
        
        // file menu
        JMenu fileMenu=new JMenu("File");
        menuBar.add(fileMenu);
        // load
        JMenuItem loadItem = new JMenuItem("Load");
        fileMenu.add(loadItem);
        loadItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                JFileChooser jfc = new JFileChooser(".");

                int returnValue = jfc.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    System.out.println("load from " +selectedFile.getAbsolutePath());
                    //load from file
                    try {
                        updateScene(Scene.loadFromFile(selectedFile));
                    } catch (Exception exc){
                        JOptionPane.showMessageDialog(null, exc);
                    }
                }
            }
        });
        // save
        JMenuItem saveItem = new JMenuItem("Save");
        fileMenu.add(saveItem);
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                JFileChooser jfc = new JFileChooser(".");

                // int returnValue = jfc.showOpenDialog(null);
                int returnValue = jfc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    System.out.println("save to " +selectedFile.getAbsolutePath());
                    String stringScene = scene.toString();
                    try (PrintWriter out = new PrintWriter(selectedFile)){
                        out.println(stringScene);
                    } catch (FileNotFoundException exc){
                        //TODO: tell the user that was a bad file (with an error window)
                        JOptionPane.showMessageDialog(null, exc.toString() + "\n Pick a different file");
                    }
                    
                }
            }
        });
        fileMenu.addSeparator();
        // edit
        JMenuItem itemExit = new JMenuItem ("Exit");
        fileMenu.add(itemExit);
        itemExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                System.exit(0);
            }
        });

        // color menu
        JMenu colorMenu = new JMenu("Color");
        menuBar.add(colorMenu);

        // red color
        JMenuItem redColorItem= new JMenuItem ("Red");
        colorMenu.add(redColorItem);
        redColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to red
                color = Color.RED;
            }
        });
        
        
        // blue color
        JMenuItem blueColorItem = new JMenuItem ("Blue");
        colorMenu.add(blueColorItem);
        blueColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to blue
                color = Color.BLUE;
            }
        });
        
        // green color
        JMenuItem greenColorItem = new JMenuItem ("Green");
        colorMenu.add(greenColorItem);
        greenColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to green
                color = Color.GREEN;
            }
        });
        
        // purple color
        JMenuItem purpleColorItem = new JMenuItem ("Purple");
        colorMenu.add(purpleColorItem);
        purpleColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to purple
                color = Color.MAGENTA; //need to add this to the list of colors for the colorToString stuff to work in Util
            }
        });
        
        // shape menu
        JMenu shapeMenu = new JMenu("Shape");
        menuBar.add(shapeMenu);
        
        // square
        JMenuItem squareItem = new JMenuItem("Square");
        shapeMenu.add(squareItem);
        squareItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Square");
                shapeType = ShapeType.SQUARE;
            }
        });
        //rectangle
        JMenuItem rectangleItem = new JMenuItem("Rectangle");
        shapeMenu.add(rectangleItem);
        rectangleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Rectangle");
                shapeType = ShapeType.RECTANGLE;
            }
        });
        
        // circle
        JMenuItem circleItem = new JMenuItem("Circle");
        shapeMenu.add(circleItem);
        circleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Circle");
                shapeType = ShapeType.CIRCLE;
            }
        });
        
        
        // operation mode menu
        JMenu operationModeMenu=new JMenu("Operation");
        menuBar.add(operationModeMenu);
        
        // draw option
        JMenuItem drawItem=new JMenuItem("Resize");
        operationModeMenu.add(drawItem);
        drawItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activeOperation = OperationState.SCALE;
                System.out.println("Shape can be resized with up and down key");
            }
        });
        
        // select option
        JMenuItem selectItem=new JMenuItem("Move");
        operationModeMenu.add(selectItem);
        selectItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activeOperation = OperationState.MOVE;
                System.out.println("Shape can be moved with arrow keys");
            }
        });

        // select option
        JMenuItem depthItem=new JMenuItem("Depth");
        operationModeMenu.add(depthItem);
        depthItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activeOperation = OperationState.DEPTH;
                System.out.println("Shape drawing order can be changed with arrow keys");
            }
        });
        
        // select option
        JMenuItem blankItem=new JMenuItem("None");
        operationModeMenu.add(blankItem);
        blankItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activeOperation = null;
                System.out.println("No shape operations will be performed");
            }
        });
        

        // set the menu bar for this frame
        this.setJMenuBar(menuBar);
    }
    
    /**
     * Initialize the keyboard listener.
     */
    private void initializeKeyListener()
    {
        shapePanel.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (activeOperation == OperationState.MOVE && !scene.selected().isEmpty()) { //don't try and move anything if nothing is selected
                    
                    if (keyCode == KeyEvent.VK_UP) {
                        cacheScene(); //moves can be undone
                        scene.moveSelected(0,-5);
                        System.out.println("Moved shapes up");
                    } else if (keyCode == KeyEvent.VK_LEFT) {
                        cacheScene(); //moves can be undone
                        scene.moveSelected(-5,0);
                        System.out.println("Moved shapes left");
                    } else if (keyCode == KeyEvent.VK_RIGHT) {
                        cacheScene(); //moves can be undone
                        scene.moveSelected(5,0);
                        System.out.println("Moved shapes right");
                    } else if (keyCode == KeyEvent.VK_DOWN) {
                        cacheScene(); //moves can be undone
                        scene.moveSelected(0,5);
                        System.out.println("Moved shapes down");
                    }
                } else if (activeOperation == OperationState.SCALE && !scene.selected().isEmpty()){
                    if (keyCode == KeyEvent.VK_UP){
                        cacheScene(); //scaling can be undone
                        scene.scaleUpSelected();
                    } else if (keyCode == KeyEvent.VK_DOWN){
                        cacheScene(); //scaling can be undone
                        scene.scaleDownSelected();
                    }
                } else if (activeOperation == OperationState.DEPTH && !scene.selected().isEmpty()){
                    if (keyCode == KeyEvent.VK_UP){
                        cacheScene(); //deepening can be undone
                        scene.deepenSelected();
                    } else if (keyCode == KeyEvent.VK_DOWN){
                        cacheScene(); //lifting can be undone
                        scene.liftSelected();
                    }
                }

            
                //need to update scene
                repaint();

                //check for ctrl + z i.e. undo
                if (keyCode == KeyEvent.VK_Z && e.isControlDown()) {// if both ctrl and z are down
                    if(history.peek() != null) {
                        updateScene(history.removeFirst()); //take off the first item (which is the previous state)
                        System.out.println("Action undone! Now displaying scene:");
                        System.out.println(scene);
                    }
                    else System.out.println("No more actions to undo");
                }
            } 
            public void keyReleased(KeyEvent e){
                // TODO: implement this method if you need it
            }
            public void keyTyped(KeyEvent e) {
                // TODO: implement this method if you need it
            }
        });
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DrawShapes shapes=new DrawShapes(700, 600);
        shapes.setVisible(true);
    }

}
