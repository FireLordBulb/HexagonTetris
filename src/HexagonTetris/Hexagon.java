package HexagonTetris;
import java.awt.Graphics;
import java.awt.Color;

public class Hexagon {
    public static final int NUM_POINTS = 6;
    public static final double ROTATION_PER_POINT = Math.TAU/NUM_POINTS ;
    public final int[] xPoints, yPoints;
    public Color color;
    Hexagon(double x, double y, int radius){
        xPoints = new int[NUM_POINTS];
        yPoints = new int[NUM_POINTS];
        for (int i = 0; i < NUM_POINTS; i++){
            xPoints[i] = (int)Math.round(x + radius*Math.cos(i*ROTATION_PER_POINT));
            yPoints[i] = (int)Math.round(y + radius*Math.sin(i*ROTATION_PER_POINT));
        }
        color = null;
    }
    public void draw(Graphics g, boolean doHideColor){
        g.setColor(doHideColor || color == null ? Color.BLACK : color);
        g.fillPolygon(xPoints, yPoints, NUM_POINTS);
        g.setColor(HexagonTetris.BACKGROUND_COLOR);
        g.drawPolygon(xPoints, yPoints, NUM_POINTS);
    }
}