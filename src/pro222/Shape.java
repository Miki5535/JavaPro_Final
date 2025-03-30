package pro222;

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class Shape implements Drawable {
    protected int x1, y1, x2, y2;
    protected Color color;
    protected boolean fill;
    protected int size;

    public Shape(int x1, int y1, int x2, int y2, Color color, boolean fill, int size) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.fill = fill;
        this.size = size;
    }

    public abstract void draw(Graphics2D g2d);
    
    protected void drawDDALine(Graphics2D g, int x1, int y1, int x2, int y2) {
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