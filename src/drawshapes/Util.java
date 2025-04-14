package drawshapes;

import java.awt.Color;

/**
 * Utilities class containing methods to convert
 * strings to colors, colors to strings, write to files,
 * and so on.
 * 
 * This class contains static utility methods. It doesn't
 * make sense to create instances of this class.
 */
public class Util
{
    // private constructor
    private Util() {}

    public static String colorToString(Color color) {
        if (color == Color.RED) {
            return "RED";
        } else if (color == Color.BLUE) {
            return "BLUE";
        } else if (color == Color.GREEN){
            return "GREEN";
        } else if (color == Color.MAGENTA){
            return "MAGENTA";
        }
        throw new UnsupportedOperationException("Unexpected color: "+color);
    }

    public static Color stringToColor(String color) {
        if (color.equals("RED")) {
            return Color.RED;
        } else if (color.equals("BLUE")) {
            return Color.BLUE;
        } else if (color.equals("GREEN")){
            return Color.GREEN;
        } else if (color.equals("MAGENTA")){
            return Color.MAGENTA;
        }
        throw new UnsupportedOperationException("Unexpected color: "+color);
    }
}
