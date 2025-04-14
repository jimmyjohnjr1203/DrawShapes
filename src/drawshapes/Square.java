package drawshapes;



import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class Square extends Rectangle
{
    public Square(Color color, int centerX, int centerY, int length) {
        super(new Point(centerX, centerY), length, length, color);
    }
    public Square(Color color, Point center, int length) {
        super(center, length, length, color);
    }
    
    public String toString() {
        return String.format("SQUARE %d %d %d %s %s", 
                getAnchorPoint().x,
                getAnchorPoint().y,
                width, //we are treating width as length (don't need a new var)
                Util.colorToString(getColor()),
                selected);
    }

    @Override
    public void draw(Graphics g) {
        if (isSelected()){
            g.setColor(color.darker());
        } else {
            g.setColor(getColor());
        }
        g.fillRect(getAnchorPoint().x, getAnchorPoint().y, width, height);
    }

    @Override
    public IShape copy() {
        Square copy = new Square(color, getAnchorPoint().x + width/2, getAnchorPoint().y + width/2, width);
        copy.selected = this.selected;
        return copy;
    }
}
