package pro222;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Line extends Shape {
    public Line(int x1, int y1, int x2, int y2, Color color, boolean fill, int size) {
        super(x1, y1, x2, y2, color, fill, size);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(size));
        drawDDALine(g2d, x1, y1, x2, y2);
    }
}