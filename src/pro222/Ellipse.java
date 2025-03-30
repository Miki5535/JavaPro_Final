package pro222;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Ellipse extends Shape {
    public Ellipse(int x1, int y1, int x2, int y2, Color color, boolean fill, int size) {
        super(x1, y1, x2, y2, color, fill, size);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(size));
        
        int width = Math.abs(x2 - x1);
        int height = Math.abs(y2 - y1);
        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        
        if (fill) {
            g2d.fillOval(x, y, width, height);
        } else {
            g2d.drawOval(x, y, width, height);
        }
    }
    
    private void plot4Points(Graphics2D g, int x, int y, int xCenter, int yCenter) {
        g.drawLine(xCenter + x, yCenter + y, xCenter + x, yCenter + y);
        g.drawLine(xCenter + x, yCenter - y, xCenter + x, yCenter - y);
        g.drawLine(xCenter - x, yCenter - y, xCenter - x, yCenter - y);
        g.drawLine(xCenter - x, yCenter + y, xCenter - x, yCenter + y);
    }
    
    @SuppressWarnings("unused")
	private void drawMidpointEllipse(Graphics2D g, int xCenter, int yCenter, int rx, int ry) {
        int rx2 = rx * rx;
        int ry2 = ry * ry;
        int twoRx2 = 2 * rx2;
        int twoRy2 = 2 * ry2;
        int p;
        int x = 0;
        int y = ry;
        int px = 0;
        int py = twoRx2 * y;

        // Region 1
        plot4Points(g, x, y, xCenter, yCenter);
        p = (int) (ry2 - (rx2 * ry) + (0.25 * rx2));
        while (px < py) {
            x++;
            px += twoRy2;
            if (p < 0) {
                p += ry2 + px;
            } else {
                y--;
                py -= twoRx2;
                p += ry2 + px - py;
            }
            plot4Points(g, x, y, xCenter, yCenter);
        }

        // Region 2
        p = (int) (ry2 * (x + 0.5) * (x + 0.5) + rx2 * (y - 1) * (y - 1) - rx2 * ry2);
        while (y > 0) {
            y--;
            py -= twoRx2;
            if (p > 0) {
                p += rx2 - py;
            } else {
                x++;
                px += twoRy2;
                p += rx2 - py + px;
            }
            plot4Points(g, x, y, xCenter, yCenter);
        }
    }
    
    @SuppressWarnings("unused")
	private void drawFilledMidpointEllipse(Graphics2D g, int xCenter, int yCenter, int rx, int ry) {
        int rx2 = rx * rx;
        int ry2 = ry * ry;
        int twoRx2 = 2 * rx2;
        int twoRy2 = 2 * ry2;
        int p;
        int x = 0;
        int y = ry;
        int px = 0;
        int py = twoRx2 * y;

        // Region 1
        drawHorizontalLines(g, x, y, xCenter, yCenter);
        p = (int) (ry2 - (rx2 * ry) + (0.25 * rx2));
        while (px < py) {
            x++;
            px += twoRy2;
            if (p < 0) {
                p += ry2 + px;
            } else {
                y--;
                py -= twoRx2;
                p += ry2 + px - py;
            }
            drawHorizontalLines(g, x, y, xCenter, yCenter);
        }

        // Region 2
        p = (int) (ry2 * (x + 0.5) * (x + 0.5) + rx2 * (y - 1) * (y - 1) - rx2 * ry2);
        while (y > 0) {
            y--;
            py -= twoRx2;
            if (p > 0) {
                p += rx2 - py;
            } else {
                x++;
                px += twoRy2;
                p += rx2 - py + px;
            }
            drawHorizontalLines(g, x, y, xCenter, yCenter);
        }
    }
    
    private void drawHorizontalLines(Graphics2D g, int x, int y, int xCenter, int yCenter) {
        g.drawLine(xCenter - x, yCenter + y, xCenter + x, yCenter + y);
        g.drawLine(xCenter - x, yCenter - y, xCenter + x, yCenter - y);
    }
}