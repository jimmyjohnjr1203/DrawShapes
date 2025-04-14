package drawshapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class Circle extends AbstractShape
{
    private int diameter;
    
    public Circle(Color color, Point center, int diameter) {
        super(new Point(center.x, center.y));
        setBoundingBox(center.x - diameter/2, center.x + diameter/2, center.y - diameter/2, center.y + diameter/2);
        this.anchorPoint = center; //anchorPoint for circle is at the center of the circle
        this.color = color;
        this.diameter = diameter;
    }

    @Override
    public void draw(Graphics g) {
        if (isSelected()){
            g.setColor(this.color.darker());
        } else {
            g.setColor(getColor());
        }
        g.fillOval((int)getAnchorPoint().getX() - diameter/2,
                (int)getAnchorPoint().getY() - diameter/2,
                diameter,
                diameter);
    }
    
    public String toString() {
        return String.format("CIRCLE %d %d %d %s %s", 
                this.getAnchorPoint().x, 
                this.getAnchorPoint().y,
                this.diameter,
                Util.colorToString(this.getColor()),
                this.isSelected());
    }

    @Override
    public void setAnchorPoint(Point p) {
        setBoundingBox(p.x - diameter/2, p.x + diameter/2, p.y - diameter/2, p.y + diameter/2);
        this.anchorPoint = p;
    }

    @Override
    public void scaleUp(){
        this.diameter *= 1.1;
    }

    @Override
    public void scaleDown(){
        this.diameter *= 0.9;
    }

    @Override
    public Circle copy() {
        //need to remember if its selected
        Circle copy = new Circle(this.color, this.anchorPoint, this.diameter);
        copy.selected = this.selected;
        return copy;
    }

}
