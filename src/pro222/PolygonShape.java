package pro222;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class PolygonShape implements Drawable {
    private int[] xPoints, yPoints;
    private int nPoints;
    private Color color;
    private boolean fill;
    private int size;
    
    public PolygonShape(Point start, Point end, int sides, Color color, boolean fill, int size) {
        this.color = color;
        this.fill = fill;
        this.size = size;
        this.nPoints = sides;
        
        xPoints = new int[sides];
        yPoints = new int[sides];
        
        int centerX = (start.x + end.x) / 2;
        int centerY = (start.y + end.y) / 2;
        int radius = Math.min(Math.abs(end.x - start.x), Math.abs(end.y - start.y)) / 2;
        
        double angle = 2 * Math.PI / sides;
        for (int i = 0; i < sides; i++) {
            xPoints[i] = centerX + (int)(radius * Math.cos(i * angle - Math.PI/2));
            yPoints[i] = centerY + (int)(radius * Math.sin(i * angle - Math.PI/2));
        }
    }
    
    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(size));
        if (fill) {
            g2d.fillPolygon(xPoints, yPoints, nPoints);
        } else {
            g2d.drawPolygon(xPoints, yPoints, nPoints);
        }
    }
}