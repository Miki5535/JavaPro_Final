package pro222;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class DrawingPanel extends JPanel {
    private String currentTool = "pencil";
    private Color currentColor = Color.BLACK;
    private boolean fillShape = false;
    private boolean dashedLine = false;
    private int brushSize = 2;
    private int eraserSize = 10;
    private int sprayDensity = 50;
    private int polygonSides = 5;
    private int roundedRectArc = 20;
    private int textSize = 12;
    private String currentText = "";
    private float[] dashPattern = {10f, 4f};
    
    private Point lastPoint = null;
    private Point startPoint = null;
    private Point endPoint = null;
    private Point mousePosition = null;
    private boolean showGrid = false;
    private final int GRID_SIZE = 20;
    private boolean erasing = false;
    
    private final ArrayList<Drawable> shapes = new ArrayList<>();
    private final Stack<ArrayList<Drawable>> undoStack = new Stack<>();
    private final Stack<ArrayList<Drawable>> redoStack = new Stack<>();
    
 // In DrawingPanel class
    public int getBrushSize() { return brushSize; }
    public void setBrushSize(int size) { this.brushSize = size; }

    public int getEraserSize() { return eraserSize; }
    public void setEraserSize(int size) { this.eraserSize = size; }

    public int getSprayDensity() { return sprayDensity; }
    public void setSprayDensity(int density) { this.sprayDensity = density; }

    public int getPolygonSides() { return polygonSides; }
    public void setPolygonSides(int sides) { this.polygonSides = sides; }

    public int getRoundedRectArc() { return roundedRectArc; }
    public void setRoundedRectArc(int arc) { this.roundedRectArc = arc; }

    public int getTextSize() { return textSize; }
    public void setTextSize(int size) { this.textSize = size; }

    public String getCurrentText() { return currentText; }
    public void setCurrentText(String text) { this.currentText = text; }

    public void bringToFront() { /* implementation */ }
    public void sendToBack() { /* implementation */ }

    public DrawingPanel() {
        setBackground(Color.WHITE);
        setupMouseListeners();
    }

    public void setCurrentTool(String tool) {
        currentTool = tool;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color color) {
        currentColor = color;
    }

    public void setFillShape(boolean fill) {
        fillShape = fill;
    }

    public void setShowGrid(boolean show) {
        showGrid = show;
        repaint();
    }

    public JMenuItem createMenuItem(String text, String action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(e -> {
            switch (action) {
                case "new": newDrawing(); break;
                case "open": openImage(); break;
                case "save": saveImage(); break;
                case "exit": System.exit(0); break;
                case "undo": undo(); break;
                case "redo": redo(); break;
                case "clear": clearCanvas(); break;
            }
        });
        return item;
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }

                if ("fill".equals(currentTool)) {
                    saveState();
                    floodFill(e.getX(), e.getY(), currentColor);
                } else if ("text".equals(currentTool)) {
                    saveState();
                    startPoint = e.getPoint();
                    shapes.add(new TextShape(startPoint.x, startPoint.y, currentText, 
                        currentColor, textSize));
                    repaint();
                } else {
                    saveState();
                    startPoint = e.getPoint();
                    lastPoint = startPoint;
                    
                    if ("spray".equals(currentTool)) {
                        sprayPaint(startPoint.x, startPoint.y);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) || startPoint == null) {
                    return;
                }
                if ("eraser".equals(currentTool)) {
                    erasing = false;
                }

                if ("pencil".equals(currentTool) || "brush".equals(currentTool) || 
                    "eraser".equals(currentTool) || "spray".equals(currentTool)) {
                    lastPoint = null;
                } else if (!"text".equals(currentTool)) {
                    endPoint = e.getPoint();
                    createShape();
                    startPoint = null;
                    endPoint = null;
                }
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mousePosition = e.getPoint();
                
                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }

                if ("pencil".equals(currentTool) || "brush".equals(currentTool)) {
                    Point currentPoint = e.getPoint();
                    if (lastPoint != null) {
                        shapes.add(new Line(lastPoint.x, lastPoint.y, 
                            currentPoint.x, currentPoint.y, currentColor, false, brushSize));
                    }
                    lastPoint = currentPoint;
                } else if ("eraser".equals(currentTool)) {
                    if (!erasing) {
                        saveState();
                        erasing = true;
                    }
                    Point currentPoint = e.getPoint();
                    if (lastPoint != null) {
                        shapes.add(new Line(lastPoint.x, lastPoint.y, 
                                currentPoint.x, currentPoint.y, Color.WHITE, true, eraserSize));
                    }
                    lastPoint = currentPoint;
                } else if ("spray".equals(currentTool)) {
                    sprayPaint(e.getX(), e.getY());
                } else if (startPoint != null) {
                    endPoint = e.getPoint();
                }
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition = e.getPoint();
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        if (showGrid) {
            g2d.setColor(Color.LIGHT_GRAY);
            for (int x = 0; x < getWidth(); x += GRID_SIZE) {
                g2d.drawLine(x, 0, x, getHeight());
            }
            for (int y = 0; y < getHeight(); y += GRID_SIZE) {
                g2d.drawLine(0, y, getWidth(), y);
            }
        }
        
        for (Drawable shape : shapes) {
            shape.draw(g2d);
        }
        
        drawToolPreviews(g2d);
    }

    private void drawToolPreviews(Graphics2D g2d) {
        if (mousePosition == null) return;
        
        if ("eraser".equals(currentTool) && mousePosition != null) {
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(1));
            int halfSize = eraserSize / 2;
            g2d.drawRect(
                mousePosition.x - halfSize, 
                mousePosition.y - halfSize, 
                eraserSize, 
                eraserSize);
        }
        
        if (startPoint != null && endPoint != null && "triangle".equals(currentTool)) {
            g2d.setColor(Color.GRAY);
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(brushSize));
            
            int width = Math.abs(endPoint.x - startPoint.x);
            @SuppressWarnings("unused")
			int height = Math.abs(endPoint.y - startPoint.y);
            
            int[] xPoints = {
                startPoint.x + width/2,
                startPoint.x,
                endPoint.x
            };
            
            int[] yPoints = {
                startPoint.y,
                endPoint.y,
                endPoint.y
            };
            
            if (fillShape) {
                g2d.fillPolygon(xPoints, yPoints, 3);
            } else {
                g2d.drawPolygon(xPoints, yPoints, 3);
            }
            
            g2d.setStroke(oldStroke);
        }
        
        if (startPoint != null && endPoint != null) {
            g2d.setColor(Color.GRAY);
            Stroke oldStroke = g2d.getStroke();
            
            if (dashedLine) {
                g2d.setStroke(new BasicStroke(brushSize, BasicStroke.CAP_BUTT, 
                    BasicStroke.JOIN_BEVEL, 0, dashPattern, 0));
            } else {
                g2d.setStroke(new BasicStroke(brushSize));
            }
            
            switch (currentTool) {
                case "line":
                    g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
                    break;
                case "rectangle":
                    if (fillShape) {
                        g2d.fillRect(Math.min(startPoint.x, endPoint.x), 
                            Math.min(startPoint.y, endPoint.y),
                            Math.abs(endPoint.x - startPoint.x), 
                            Math.abs(endPoint.y - startPoint.y));
                    } else {
                        g2d.drawRect(Math.min(startPoint.x, endPoint.x), 
                            Math.min(startPoint.y, endPoint.y),
                            Math.abs(endPoint.x - startPoint.x), 
                            Math.abs(endPoint.y - startPoint.y));
                    }
                    break;
                case "roundedrect":
                    if (fillShape) {
                        g2d.fillRoundRect(Math.min(startPoint.x, endPoint.x), 
                            Math.min(startPoint.y, endPoint.y),
                            Math.abs(endPoint.x - startPoint.x), 
                            Math.abs(endPoint.y - startPoint.y),
                            roundedRectArc, roundedRectArc);
                    } else {
                        g2d.drawRoundRect(Math.min(startPoint.x, endPoint.x), 
                            Math.min(startPoint.y, endPoint.y),
                            Math.abs(endPoint.x - startPoint.x), 
                            Math.abs(endPoint.y - startPoint.y),
                            roundedRectArc, roundedRectArc);
                    }
                    break;
                case "circle":
                    int diameter = Math.max(Math.abs(endPoint.x - startPoint.x), 
                        Math.abs(endPoint.y - startPoint.y));
                    if (fillShape) {
                        g2d.fillOval(startPoint.x, startPoint.y, diameter, diameter);
                    } else {
                        g2d.drawOval(startPoint.x, startPoint.y, diameter, diameter);
                    }
                    break;
                case "ellipse":
                    if (fillShape) {
                        g2d.fillOval(Math.min(startPoint.x, endPoint.x), 
                            Math.min(startPoint.y, endPoint.y),
                            Math.abs(endPoint.x - startPoint.x), 
                            Math.abs(endPoint.y - startPoint.y));
                    } else {
                        g2d.drawOval(Math.min(startPoint.x, endPoint.x), 
                            Math.min(startPoint.y, endPoint.y),
                            Math.abs(endPoint.x - startPoint.x), 
                            Math.abs(endPoint.y - startPoint.y));
                    }
                    break;
                case "polygon":
                    int sides = polygonSides;
                    int[] xPoints = new int[sides];
                    int[] yPoints = new int[sides];
                    double angle = 2 * Math.PI / sides;
                    
                    int centerX = (startPoint.x + endPoint.x) / 2;
                    int centerY = (startPoint.y + endPoint.y) / 2;
                    int radius = Math.min(Math.abs(endPoint.x - startPoint.x), 
                        Math.abs(endPoint.y - startPoint.y)) / 2;
                    
                    for (int i = 0; i < sides; i++) {
                        xPoints[i] = centerX + (int)(radius * Math.cos(i * angle - Math.PI/2));
                        yPoints[i] = centerY + (int)(radius * Math.sin(i * angle - Math.PI/2));
                    }
                    
                    if (fillShape) {
                        g2d.fillPolygon(xPoints, yPoints, sides);
                    } else {
                        g2d.drawPolygon(xPoints, yPoints, sides);
                    }
                    break;
                case "text":
                    if (!currentText.isEmpty()) {
                        g2d.setFont(new Font("Arial", Font.PLAIN, textSize));
                        g2d.drawString(currentText, startPoint.x, startPoint.y);
                    }
                    break;
            }
            
            g2d.setStroke(oldStroke);
        }
        
        if ("spray".equals(currentTool)) {
            g2d.setColor(Color.GRAY);
            g2d.drawOval(mousePosition.x - sprayDensity/4, mousePosition.y - sprayDensity/4, 
                sprayDensity/2, sprayDensity/2);
        }
    }

    private void createShape() {
        switch (currentTool) {
            case "line":
                shapes.add(new Line(startPoint.x, startPoint.y, endPoint.x, endPoint.y, 
                    currentColor, false, brushSize));
                break;
            case "rectangle":
                shapes.add(new RectangleShape(startPoint.x, startPoint.y, 
                    endPoint.x, endPoint.y, currentColor, fillShape, brushSize));
                break;
            case "circle":
                shapes.add(new Circle(startPoint.x, startPoint.y, 
                    endPoint.x, endPoint.y, currentColor, fillShape, brushSize));
                break;
            case "ellipse":
                shapes.add(new Ellipse(startPoint.x, startPoint.y, 
                    endPoint.x, endPoint.y, currentColor, fillShape, brushSize));
                break;
            case "polygon":
                shapes.add(new PolygonShape(startPoint, endPoint, polygonSides, 
                    currentColor, fillShape, brushSize));
                break;
            case "triangle":
                shapes.add(new TriangleShape(startPoint, endPoint, 
                    currentColor, fillShape, brushSize));
                break;
        }
    }

    private void sprayPaint(int x, int y) {
        int radius = sprayDensity / 2;
        int centerX = x;
        int centerY = y;
        
        for (int i = 0; i < sprayDensity; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * radius;
            
            int dotX = (int)(centerX + distance * Math.cos(angle));
            int dotY = (int)(centerY + distance * Math.sin(angle));
            
            shapes.add(new SprayDot(dotX, dotY, currentColor, brushSize));
        }
    }

    private void floodFill(int x, int y, Color fillColor) {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        paint(g2d);
        g2d.dispose();

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        int targetRGB = image.getRGB(x, y);
        if (targetRGB == fillColor.getRGB()) return;

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            int index = p.y * image.getWidth() + p.x;

            if (p.x < 0 || p.x >= image.getWidth() || p.y < 0 || p.y >= image.getHeight() ||
                    pixels[index] != targetRGB) {
                continue;
            }

            pixels[index] = fillColor.getRGB();
            queue.add(new Point(p.x + 1, p.y));
            queue.add(new Point(p.x - 1, p.y));
            queue.add(new Point(p.x, p.y + 1));
            queue.add(new Point(p.x, p.y - 1));
        }

        image.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        shapes.add(new ImageShape(image));
        repaint();
    }

    public void saveState() {
        redoStack.clear();
        ArrayList<Drawable> copy = new ArrayList<>(shapes.size());
        for (Drawable shape : shapes) {
            copy.add(shape);
        }
        undoStack.push(copy);
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            ArrayList<Drawable> currentState = new ArrayList<>(shapes);
            redoStack.push(currentState);
            shapes.clear();
            shapes.addAll(undoStack.pop());
            repaint();
        }
    }
    
    public void redo() {
        if (!redoStack.isEmpty()) {
            ArrayList<Drawable> currentState = new ArrayList<>(shapes);
            undoStack.push(currentState);
            shapes.clear();
            shapes.addAll(redoStack.pop());
            repaint();
        }
    }

    public void newDrawing() {
        int response = JOptionPane.showConfirmDialog(this, 
            "Start a new drawing? Current drawing will be lost.", 
            "New Drawing", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            shapes.clear();
            undoStack.clear();
            redoStack.clear();
            repaint();
        }
    }

    public void clearCanvas() {
        shapes.clear();
        repaint();
    }

    public void openImage() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image Files", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage image = ImageIO.read(fileChooser.getSelectedFile());
                shapes.add(new ImageShape(image));
                repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error opening image: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG Images", "png");
        FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPEG Images", "jpg", "jpeg");
        FileNameExtensionFilter gifFilter = new FileNameExtensionFilter("GIF Images", "gif");
        FileNameExtensionFilter bmpFilter = new FileNameExtensionFilter("BMP Images", "bmp");
        
        fileChooser.addChoosableFileFilter(pngFilter);
        fileChooser.addChoosableFileFilter(jpgFilter);
        fileChooser.addChoosableFileFilter(gifFilter);
        fileChooser.addChoosableFileFilter(bmpFilter);
        fileChooser.setFileFilter(pngFilter);
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                String format = "png";
                
                if (fileChooser.getFileFilter() == jpgFilter) {
                    format = "jpg";
                    if (!file.getName().toLowerCase().endsWith(".jpg") && 
                        !file.getName().toLowerCase().endsWith(".jpeg")) {
                        file = new File(file.getAbsolutePath() + ".jpg");
                    }
                } else if (fileChooser.getFileFilter() == pngFilter) {
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new File(file.getAbsolutePath() + ".png");
                    }
                } else if (fileChooser.getFileFilter() == gifFilter) {
                    format = "gif";
                    if (!file.getName().toLowerCase().endsWith(".gif")) {
                        file = new File(file.getAbsolutePath() + ".gif");
                    }
                } else if (fileChooser.getFileFilter() == bmpFilter) {
                    format = "bmp";
                    if (!file.getName().toLowerCase().endsWith(".bmp")) {
                        file = new File(file.getAbsolutePath() + ".bmp");
                    }
                }
                
                BufferedImage image = new BufferedImage(
                    getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = image.createGraphics();
                paint(g2d);
                g2d.dispose();
                
                ImageIO.write(image, format, file);
                JOptionPane.showMessageDialog(this, "Image saved successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}