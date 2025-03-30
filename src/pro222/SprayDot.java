package pro222;



import java.awt.Color;
import java.awt.Graphics2D;

public class SprayDot implements Drawable {
    private int x, y;
    private Color color;
    private int size;
    
    public SprayDot(int x, int y, Color color, int size) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.size = size;
    }
    
    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval(x, y, size, size);
    }
}