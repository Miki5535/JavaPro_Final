package pro222;


import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ImageShape implements Drawable {
    private BufferedImage image;

    public ImageShape(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.drawImage(image, 0, 0, null);
    }
}