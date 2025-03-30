package pro222;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Circle extends Shape {
    public Circle(int x1, int y1, int x2, int y2, Color color, boolean fill, int size) {
        super(x1, y1, x2, y2, color, fill, size);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(size));
        
        int diameter = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        int radius = diameter / 2;
        int xCenter = x1 + (x2 > x1 ? radius : -radius);
        int yCenter = y1 + (y2 > y1 ? radius : -radius);
        
        if (fill) {
            drawFilledMidpointCircle(g2d, xCenter, yCenter, radius);
        } else {
            drawMidpointCircle(g2d, xCenter, yCenter, radius);
        }
    }
    
    private void plot8Points(Graphics2D g, int x, int y, int xCenter, int yCenter) {
        g.drawLine(xCenter + x, yCenter + y, xCenter + x, yCenter + y);
        g.drawLine(xCenter + y, yCenter + x, xCenter + y, yCenter + x);
        g.drawLine(xCenter + y, yCenter - x, xCenter + y, yCenter - x);
        g.drawLine(xCenter + x, yCenter - y, xCenter + x, yCenter - y);
        g.drawLine(xCenter - x, yCenter - y, xCenter - x, yCenter - y);
        g.drawLine(xCenter - y, yCenter - x, xCenter - y, yCenter - x);
        g.drawLine(xCenter - y, yCenter + x, xCenter - y, yCenter + x);
        g.drawLine(xCenter - x, yCenter + y, xCenter - x, yCenter + y);
    }
    
    private void drawMidpointCircle(Graphics2D g, int xCenter, int yCenter, int radius) {
        int x = 0;
        int y = radius;
        int p = 1 - radius;
        
        plot8Points(g, x, y, xCenter, yCenter);
        
        while (x <= y) {
            if (p < 0) {
                x++;
                p += 2 * x + 1;
            } else {
                x++;
                y--;
                p += 2 * x + 1 - 2 * y;
            }
            plot8Points(g, x, y, xCenter, yCenter);
        }
    }
    
    private void drawFilledMidpointCircle(Graphics2D g, int xCenter, int yCenter, int radius) {
        int x = 0;
        int y = radius;
        int p = 1 - radius;
        
        drawFilled8Points(g, x, y, xCenter, yCenter);
        
        while (x <= y) {
            if (p < 0) {
                x++;
                p += 2 * x + 1;
            } else {
                x++;
                y--;
                p += 2 * x + 1 - 2 * y;
            }
            drawFilled8Points(g, x, y, xCenter, yCenter);
        }
    }
    
    private void drawFilled8Points(Graphics2D g, int x, int y, int xCenter, int yCenter) {
        g.drawLine(xCenter - x, yCenter + y, xCenter + x, yCenter + y);
        g.drawLine(xCenter - y, yCenter + x, xCenter + y, yCenter + x);
        g.drawLine(xCenter - y, yCenter - x, xCenter + y, yCenter - x);
        g.drawLine(xCenter - x, yCenter - y, xCenter + x, yCenter - y);
    }
}