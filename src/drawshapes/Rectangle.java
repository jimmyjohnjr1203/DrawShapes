package drawshapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class Rectangle extends AbstractShape
{
    protected int width;
    protected int height;
    
    public Rectangle(Point clicked, int width, int height, Color color){
        super(new Point(clicked.x - width/2, clicked.y - height/2)); //rectangle achor point is at top left of rectangle
        setBoundingBox(clicked.x - width/2, clicked.x + width/2, clicked.y - height/2, clicked.y + height/2);
        this.color = color;
        this.width = width;
        this.height = height;
    }
    
    public Rectangle(int left, int right, int top, int bottom) {
        super(new Point(left, top));
        setBoundingBox(left, right, top, bottom);
        this.color = Color.BLUE;
        this.width = right - left;
        this.height = bottom - top;
    }
    public Rectangle(int left, int right, int top, int bottom, Color color) {
        super(new Point(left, top));
        setBoundingBox(left, right, top, bottom);
        this.color = color;
        this.width = right - left;
        this.height = bottom - top;
    }

    /* (non-Javadoc)
     * @see drawshapes.sol.Shape#draw(java.awt.Graphics)
     */
    @Override
    public void draw(Graphics g) {
        if (isSelected()){
            g.setColor(color.darker());
        } else {
            g.setColor(getColor());
        }
        g.fillRect(getAnchorPoint().x, getAnchorPoint().y, width, height);
    }

    public String toString() {
        return String.format("RECTANGLE %d %d %d %d %s %s", 
                getAnchorPoint().x,
                getAnchorPoint().y,
                width,
                height,
                Util.colorToString(getColor()),
                selected);
    }
    

    /* (non-Javadoc)
     * @see drawshapes.sol.Shape#setAnchorPoint(java.awt.Point)
     */
    @Override
    public void setAnchorPoint(Point p) {
        setBoundingBox(p.x, p.x + width, p.y, p.y + height); //Anchor point is top left (but you click in the center)
        this.anchorPoint = p;
    }

    //Scaling is relative to top left (because that is the location of the anchor point)
    
    @Override
    public void scaleUp(){
        this.width += 5;
        this.height += 5;
    }

    @Override
    public void scaleDown(){
        this.width -= 5;
        this.height -= 5;

    }

    @Override
    public IShape copy() {
        Rectangle copy = new Rectangle(anchorPoint.x, anchorPoint.x + width, anchorPoint.y, anchorPoint.y + height, color);
        copy.selected = this.selected;
        return copy;
    }

}
