package drawshapes;

import java.awt.Graphics;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/** A type of scene that holds all the other scenes, a singleton
 * 
 * Has all the same methods as Scene, applies them to the currently active scene
 * 
 * Packages undo and redo (removing them from DrawShapes)
 * 
 */
public class SceneViewer extends Scene {
    
    private Scene activeScene;
    private static boolean exists = false;
    private static SceneViewer self;
    
    //list of previous scenes, to be lengthened as operations happen, undo removes the most recent addition
    // Java says use Deques instead of stacks?
    private Deque<Scene> undoStack = new LinkedList<Scene>(); 
    // stack with all the scenes that have just been undone
    private Deque<Scene> redoStack = new LinkedList<Scene>();


    private SceneViewer(){
        activeScene = new Scene(); //initalize activeScene
        cacheScene(); //cache it to start
    }

    public static SceneViewer getSceneViewer(){
        if (!exists) {
            self = new SceneViewer();
            exists = true;
            return self;
        } else {
            return self;
        } 
    }

    /**Updates the active scene that is being drawn
     * 
     * @return
     */
    private void updateScene(Scene s){
        activeScene = s;
    }
    
    /** Should be called whenever an action is taken, will save the previous Scene to the undoStack
     * Will also clear the redoStack (can't redo after doing something else)
     * 
     * @return 
     */
    private void cacheScene(){
        undoStack.addFirst(activeScene); //store the current scene
        System.out.println("This scene was stored then a copy was made");
        System.out.println(activeScene);
        //make a copy of scene to be changed by further edits
        updateScene(activeScene.copy());
        //clear the redo stack, can't redo after doing something else
        if (!redoStack.isEmpty()) redoStack.clear();
    }

    
    /** Undo a previous operation
     * 
     * @return 
     */
    public void undo(){
        if(undoStack.peek() != null) {
            //add removed scene to the redo stack
            redoStack.addFirst(activeScene);
            updateScene(undoStack.removeFirst()); //take off the first item (which is the previous state)
            System.out.println("Action undone! Now displaying scene:");
            System.out.println(activeScene);
        }
        else System.out.println("No more actions to undo");
    }
    
    /** Redo a previous operation
     * 
     * @return 
     */
    public void redo(){
        if(redoStack.peek() != null) {
            //add removed scene to the undo stack
            undoStack.addFirst(activeScene);
            updateScene(redoStack.removeFirst()); //take off the first item (which is the previous state)
            System.out.println("Action redone! Now displaying scene:");
            System.out.println(activeScene);
        }
        else System.out.println("No more actions to redo");
    }


    public void load(File file) throws IOException{
        activeScene = Scene.loadFromFile(file);
    }

    @Override
    public void addShape(IShape s) {
        cacheScene();
        activeScene.addShape(s);
    }
    @Override
    public void deepenSelected() {
        cacheScene();
        activeScene.deepenSelected();
    }
    @Override
    public void draw(Graphics g) {
        activeScene.draw(g);
    }

    @Override
    public void liftSelected() {
        cacheScene();
        activeScene.liftSelected();
    }
    @Override
    public void moveSelected(int x, int y) {
        cacheScene();
        activeScene.moveSelected(x, y);
    }
    @Override
    public void removeShapes(Collection<IShape> shapesToRemove) {
        cacheScene();
        activeScene.removeShapes(shapesToRemove);
    }
    @Override
    public void scaleDownSelected() {
        cacheScene();
        activeScene.scaleDownSelected();
    }
    @Override
    public void scaleUpSelected() {
        cacheScene();
        activeScene.scaleUpSelected();
    }
    @Override
    public List<IShape> select(Point point) {
        return activeScene.select(point);
    }
    @Override
    public List<IShape> select(IShape shape) {
        return activeScene.select(shape);
    }
    @Override
    public List<IShape> selected() {
        return activeScene.selected();
    }
    @Override
    public void startDrag(Point p) {
        activeScene.startDrag(p);
    }
    @Override
    public void stopDrag() {
        activeScene.stopDrag();
    }
    @Override
    public String toString() {
        return activeScene.toString();
    }
    @Override
    public void updateSelectRect(Point drag) {
        activeScene.updateSelectRect(drag);
    }

    
}
