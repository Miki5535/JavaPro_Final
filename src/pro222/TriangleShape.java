package pro222;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class TriangleShape implements Drawable {
    private Point p1, p2, p3;
    private Color color;
    private boolean fill;
    private int size;
    
    public TriangleShape(Point start, Point end, Color color, boolean fill, int size) {
        this.color = color;
        this.fill = fill;
        this.size = size;
        
        int width = Math.abs(end.x - start.x);
        @SuppressWarnings("unused")
		int height = Math.abs(end.y - start.y);
        
        this.p1 = new Point(start.x + width/2, start.y);
        this.p2 = new Point(start.x, end.y);
        this.p3 = new Point(end.x, end.y);
    }
    
    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(size));
        
        int[] xPoints = {p1.x, p2.x, p3.x};
        int[] yPoints = {p1.y, p2.y, p3.y};
        
        if (fill) {
            g2d.fillPolygon(xPoints, yPoints, 3);
        } else {
            drawDDALine(g2d, p1.x, p1.y, p2.x, p2.y);
            drawDDALine(g2d, p2.x, p2.y, p3.x, p3.y);
            drawDDALine(g2d, p3.x, p3.y, p1.x, p1.y);
        }
    }
    
    private void drawDDALine(Graphics2D g, int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        
        float xIncrement = dx / (float) steps;
        float yIncrement = dy / (float) steps;
        
        float x = x1;
        float y = y1;
        
        for (int i = 0; i <= steps; i++) {
            g.drawLine(Math.round(x), Math.round(y), Math.round(x), Math.round(y));
            x += xIncrement;
            y += yIncrement;
        }
    }
}