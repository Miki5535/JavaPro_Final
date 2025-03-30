package pro222;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class RectangleShape extends Shape {
    public RectangleShape(int x1, int y1, int x2, int y2, Color color, boolean fill, int size) {
        super(x1, y1, x2, y2, color, fill, size);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(size));
        
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        
        if (fill) {
            g2d.fillRect(minX, minY, maxX - minX, maxY - minY);
        } else {
            drawDDALine(g2d, minX, minY, maxX, minY);
            drawDDALine(g2d, maxX, minY, maxX, maxY);
            drawDDALine(g2d, maxX, maxY, minX, maxY);
            drawDDALine(g2d, minX, maxY, minX, minY);
        }
    }
}
