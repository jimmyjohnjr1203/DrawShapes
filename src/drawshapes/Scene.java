package drawshapes;



import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * A scene of shapes.  Uses the Model-View-Controller (MVC) design pattern,
 * though note that model knows something about the view, as the draw() 
 * method both in Scene and in Shape uses the Graphics object. That's kind of sloppy,
 * but it also helps keep things simple.
 * 
 * This class allows us to talk about a "scene" of shapes,
 * rather than individual shapes, and to apply operations
 * to collections of shapes.
 * 
 * @author jspacco
 *
 */
public class Scene implements Iterable<IShape>
{
    private List<IShape> shapeList=new LinkedList<IShape>();
    private SelectionRectangle selectRect;
    private boolean isDrag;
    private Point startDrag;

    public Scene copy(){
        Scene returnVal = new Scene();
        //copy over all of the data, including making copies of shapes
        for (IShape shape: this.shapeList){
            returnVal.shapeList.add(shape.copy());
        }

        return returnVal;
    }
    
    public void updateSelectRect(Point drag) {
        for (IShape s : this){
            s.setSelected(false);
        }
        if (drag.x > startDrag.x){
            if (drag.y > startDrag.y){
                // top-left to bottom-right
                selectRect = new SelectionRectangle(startDrag.x, drag.x, startDrag.y, drag.y);
            } else {
                // bottom-left to top-right
                selectRect = new SelectionRectangle(startDrag.x, drag.x, drag.y, startDrag.y);
            }
        } else {
            if (drag.y > startDrag.y){
                // top-right to bottom-left
                selectRect = new SelectionRectangle(drag.x, startDrag.x, startDrag.y, drag.y);
            } else {
                // bottom-left to top-right
                selectRect = new SelectionRectangle(drag.x, startDrag.x, drag.y, startDrag.y);
            }
        }
        List<IShape> selectedShapes = this.select(selectRect);
        for (IShape s : selectedShapes){
            s.setSelected(true);
        }
    }
    
    public void stopDrag() {
        this.isDrag = false;
    }
    
    public void startDrag(Point p){
        this.isDrag = true;
        this.startDrag = p;
    }
    
    /**
     * Draw all the shapes in the scene using the given Graphics object.
     * @param g
     */
    public void draw(Graphics g) {
        for (IShape s : shapeList) {
            if (s!=null){
                s.draw(g);
            }
        }
        if (isDrag) {
            selectRect.draw(g);
        }
    }
    
    /**
     * Get an iterator that can iterate through all the shapes
     * in the scene.
     */
    public Iterator<IShape> iterator() {
        return shapeList.iterator();
    }
    
    /**
     * Return a list of shapes that contain the given point.
     * @param point The point
     * @return A list of shapes that contain the given point.
     */
    public List<IShape> select(Point point)
    {
        List<IShape> selected = new LinkedList<IShape>();
        for (IShape s : shapeList){
            if (s.contains(point)){
                selected.add(s);
            }
        }
        return selected;
    }
    
    /**
     * Return a list of shapes in the scene that intersect the given shape.
     * @param s The shape
     * @return A list of shapes intersecting the given shape.
     */
    public List<IShape> select(IShape shape)
    {
        List<IShape> selected = new LinkedList<IShape>();
        for (IShape s : shapeList){
            if (s.intersects(shape)){
                selected.add(s);
            }
        }
        return selected;
    }
    
    /**
     * Return a list of shapes in the scene that are selected
     * @return A list of shapes that have been selected.
     */
    public List<IShape> selected()
    {
        List<IShape> selected = new LinkedList<IShape>();
        for (IShape s : shapeList){
            if (s.isSelected()){
                selected.add(s);
            }
        }
        return selected;
    }
    
    /**
     * Add a shape to the scene.  It will be rendered next time
     * the draw() method is invoked.
     * @param s
     */
    public void addShape(IShape s) {
        shapeList.add(s);
    }
    
    /**
     * Remove a list of shapes from the given scene.
     * @param shapesToRemove
     */
    public void removeShapes(Collection<IShape> shapesToRemove) {
        shapeList.removeAll(shapesToRemove);
    }
    
    @Override   
    public String toString() {
        String shapeText = "";
        for (IShape s : shapeList) {
            shapeText += s.toString() + "\n";
        }
        return shapeText;
    }

    public static Scene loadFromFile(File file) throws IOException { //can't fix IO exception here
        Scene scene = new Scene();
        Scanner scan = new Scanner(new FileInputStream(file));

        while (scan.hasNext()){
            String shapeName = scan.next();
            IShape shape;
            if (shapeName.equalsIgnoreCase("SQUARE")){
                //read square
                int x = scan.nextInt();
                int y = scan.nextInt();
                int side = scan.nextInt();
                Color color = Util.stringToColor(scan.next());
                boolean selected = scan.nextBoolean();
                shape = new Square(color, x, y, side);
                shape.setSelected(selected);
            } else if (shapeName.equalsIgnoreCase("RECTANGLE")){
                //read rectangle
                int x = scan.nextInt();
                int y = scan.nextInt();
                int width = scan.nextInt();
                int height = scan.nextInt();
                Color color = Util.stringToColor(scan.next());
                boolean selected = scan.nextBoolean();
                shape = new Rectangle(x, x+width, y, y+height, color);
                shape.setSelected(selected);
            } else if (shapeName.equalsIgnoreCase("CIRCLE")){
                //read circle
                int x = scan.nextInt();
                int y = scan.nextInt();
                int diameter = scan.nextInt();
                Color color = Util.stringToColor(scan.next());
                boolean selected = scan.nextBoolean();
                shape = new Circle(color, new Point(x, y), diameter);
                shape.setSelected(selected);
            } else {
                scan.close();
                throw new IOException("Unknown Shape Type");
            }
            //if (line.strip().equals("")) continue; //it was an empty line, skip it

            //add shape to scene
            scene.addShape(shape);
        }
        scan.close();
        return scene;
    }

    public void moveSelected(int x, int y) {
        // for each shape that is selected, move x and y
        for (IShape shape : this.selected()) {
            shape.move(x, y);
        }
    }

    public void scaleUpSelected() {
        // for each shape that is selected, scale up
        for (IShape shape : this.selected()) {
            shape.scaleUp();
        }
    }

    public void scaleDownSelected() {
        // for each shape that is selected, scale down
        for (IShape shape : this.selected()) {
            shape.scaleDown();
        }
    }

    //take the selected shapes and move them down in the drawing order (later shapes are drawn on top)
    public void liftSelected(){
        //take the shapes that are selected and move each one back in the list, starting from the back
        for(int i=shapeList.size()-2; i>=0; i--){
            //start at shapeList.size()-2 because you can't move back the last shape
            IShape shape = shapeList.get(i);
            if (shape.isSelected()){
                //remove shape, then add it one spot later
                shapeList.remove(i);
                shapeList.add(i+1, shape);
            }
        }
    }
    //take the selected shapes and move them up in the drawing order (earlier shapes are drawn on bottom)
    public void deepenSelected(){
        //take the shapes that are selected and move each one forward in the list, starting from the front
        for(int i=1; i<shapeList.size(); i++){
            //start at 1 because you can't move forward the first shape
            IShape shape = shapeList.get(i);
            if (shape.isSelected()){
                //remove shape, then add it one spot earlier
                shapeList.remove(i);
                shapeList.add(i-1, shape);
            }
        }
    }

    
}
