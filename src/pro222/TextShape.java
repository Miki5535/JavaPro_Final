package pro222;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class TextShape implements Drawable {
    private int x, y;
    private String text;
    private Color color;
    private int size;
    
    public TextShape(int x, int y, String text, Color color, int size) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.size = size;
    }
    
    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.setFont(new Font("Arial", Font.PLAIN, size));
        g2d.drawString(text, x, y);
    }
}
