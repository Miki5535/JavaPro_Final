package pro222;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class DrawingApp extends JFrame {
    private final int WIDTH = 1000;
    private final int HEIGHT = 800;
    private String currentTool = "pencil"; // Default tool
    private Color currentColor = Color.BLACK;
    private boolean fillShape = false;
    private int brushSize = 2; // Current brush size
    private int eraserSize = 10; // Eraser size
    private Point lastPoint = null; // Track the last point for pencil drawing
    private final ArrayList<Drawable> shapes = new ArrayList<>();
    private final Stack<ArrayList<Drawable>> undoStack = new Stack<>(); // Stack for undo functionality
    private JPanel sliderPanel; // Panel for the slider
    private CardLayout cardLayout; // Layout to toggle slider visibility
    private Point startPoint = null; // Track the start point for shape drawing
    private Point endPoint = null; // Track the end point for preview
    
    private final Stack<ArrayList<Drawable>> redoStack = new Stack<>();  // เพิ่มนี้ใกล้กับ undoStack

    public DrawingApp() {
        setTitle("Drawing Application");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // Main layout
        setLayout(new BorderLayout());
        DrawingPanel drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);
        JPanel toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);
        // Create and add slider panel
        sliderPanel = createSliderPanel();
        add(sliderPanel, BorderLayout.EAST);
        
        // Add keyboard listener for undo (Ctrl+Z)
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.isControlDown()) {
                if (e.getKeyCode() == KeyEvent.VK_Z) {
                    undo();
                    return true;
                } else if (e.getKeyCode() == KeyEvent.VK_Y) {  // เพิ่มส่วนนี้
                    redo();
                    return true;
                }
            }
            return false;
        });
    }

    private void saveState() {
        // ล้าง redo stack เมื่อมีการกระทำใหม่
        redoStack.clear();
        
        // บันทึกสถานะปัจจุบัน (เหมือนเดิม)
        ArrayList<Drawable> copy = new ArrayList<>(shapes.size());
        for (Drawable shape : shapes) {
            copy.add(shape);
        }
        undoStack.push(copy);
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            // บันทึกสถานะปัจจุบันลง redoStack ก่อนทำ undo
            ArrayList<Drawable> currentState = new ArrayList<>(shapes);
            redoStack.push(currentState);
            
            // กู้คืนสถานะจาก undoStack (เหมือนเดิม)
            shapes.clear();
            shapes.addAll(undoStack.pop());
            repaint();
        }
    }
    
    private void redo() {
        if (!redoStack.isEmpty()) {
            // บันทึกสถานะปัจจุบันลง undoStack ก่อนทำ redo
            ArrayList<Drawable> currentState = new ArrayList<>(shapes);
            undoStack.push(currentState);
            
            // กู้คืนสถานะจาก redoStack
            shapes.clear();
            shapes.addAll(redoStack.pop());
            repaint();
        }
    }


    private JPanel createToolbar() {
        JPanel toolbar = new JPanel();

        toolbar.add(createToolButton("resources/pencil.png", "pencil", "empty"));
        toolbar.add(createToolButton("resources/line.png", "line", "empty"));
        toolbar.add(createToolButton("resources/rectangle.png", "rectangle", "empty"));
        toolbar.add(createToolButton("resources/circle.png", "circle", "empty"));
        toolbar.add(createToolButton("resources/ellipse.png", "ellipse", "empty"));
        toolbar.add(createToolButton("resources/eraser.png", "eraser", "slider"));
        toolbar.add(createToolButton("resources/fill.png", "fill", "empty"));

        // ปุ่มเลือกสีพิเศษ
        JButton colorButton = createButtonWithIcon("resources/color.png");
        colorButton.addActionListener(e -> {
            Color selectedColor = JColorChooser.showDialog(this, "Choose a color", currentColor);
            if (selectedColor != null) {
                currentColor = selectedColor;
            }
            if (cardLayout != null && sliderPanel != null) {
                cardLayout.show(sliderPanel, "empty");
            }
        });
        toolbar.add(colorButton);

        JCheckBox fillCheckBox = new JCheckBox("Fill");
        fillCheckBox.addActionListener(e -> fillShape = fillCheckBox.isSelected());
        toolbar.add(fillCheckBox);

        return toolbar;
    }

    // เมธอดช่วยสร้างปุ่มที่มีไอคอนและกำหนด tool
    private JButton createToolButton(String iconPath, String toolName, String panelToShow) {
        JButton button = createButtonWithIcon(iconPath);
        button.addActionListener(e -> {
            currentTool = toolName;
            if (cardLayout != null && sliderPanel != null) {
                cardLayout.show(sliderPanel, panelToShow);
            }
        });
        return button;
    }

    // เมธอดช่วยโหลดไอคอนและปรับขนาด
    private JButton createButtonWithIcon(String iconPath) {
        return new JButton(new ImageIcon(new ImageIcon(iconPath)
                .getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
    }


    private JPanel createSliderPanel() {
        cardLayout = new CardLayout();
        JPanel panel = new JPanel(cardLayout);
        // Empty panel
        JPanel emptyPanel = new JPanel();
        panel.add(emptyPanel, "empty");
        // Slider panel
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BorderLayout());
        sliderPanel.setPreferredSize(new Dimension(50, HEIGHT));
        JLabel sizeLabel = new JLabel("Size:", SwingConstants.CENTER);
        sliderPanel.add(sizeLabel, BorderLayout.NORTH);
        JSlider sizeSlider = new JSlider(10, 100, eraserSize);
        sizeSlider.setOrientation(SwingConstants.VERTICAL);
        sizeSlider.setMajorTickSpacing(10);
        sizeSlider.setMinorTickSpacing(5);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setPaintLabels(true);
        sizeSlider.addChangeListener(e -> {
            eraserSize = sizeSlider.getValue();
            repaint();
        });
        sliderPanel.add(sizeSlider, BorderLayout.CENTER);
        panel.add(sliderPanel, "slider");
        return panel;
    }

    private class DrawingPanel extends JPanel {
        private Point mousePosition = null;

        public DrawingPanel() {
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
            	@Override
            	public void mousePressed(MouseEvent e) {
            	    if ("fill".equals(currentTool)) {
            	        saveState();
            	        floodFill(e.getX(), e.getY(), currentColor);
            	    } else {
            	        saveState(); // บันทึกสถานะก่อนใช้ดินสอ
            	        startPoint = e.getPoint();
            	        lastPoint = startPoint;
            	    }
            	}


                @Override
                public void mouseReleased(MouseEvent e) {
                    if ("pencil".equals(currentTool)) {
                        lastPoint = null; // Reset the last point when the mouse is released
                    } else if (startPoint != null) {
                        saveState(); // Save state before adding new shape
                        endPoint = e.getPoint(); // Finalize the end point
                        switch (currentTool) {
                            case "line":
                                shapes.add(new Line(startPoint.x, startPoint.y, endPoint.x, endPoint.y, currentColor, fillShape, brushSize));
                                break;
                            case "rectangle":
                                shapes.add(new Rectangle(startPoint.x, startPoint.y, endPoint.x, endPoint.y, currentColor, fillShape, brushSize));
                                break;
                            case "circle":
                                shapes.add(new Circle(startPoint.x, startPoint.y, endPoint.x, endPoint.y, currentColor, fillShape, brushSize));
                                break;
                            case "ellipse":
                                shapes.add(new Ellipse(startPoint.x, startPoint.y, endPoint.x, endPoint.y, currentColor, fillShape, brushSize));
                                break;
                        }
                        startPoint = null; // Reset start point
                        endPoint = null; // Reset end point
                        repaint();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if ("pencil".equals(currentTool)) {
                        Point currentPoint = e.getPoint();
                        if (lastPoint != null) {
                        	shapes.add(new Line(lastPoint.x, lastPoint.y, currentPoint.x, currentPoint.y, currentColor, false, brushSize));
                        }
                        lastPoint = currentPoint; // Update the last point to the current point
                        repaint();
                    } else if ("eraser".equals(currentTool)) {
                        int halfSize = eraserSize / 2;
                        shapes.add(new Rectangle(
                                e.getX() - halfSize, e.getY() - halfSize,
                                e.getX() + halfSize, e.getY() + halfSize,
                                Color.WHITE, true, eraserSize));
                        repaint();
                    } else if (!"pencil".equals(currentTool) && !"eraser".equals(currentTool)) {
                        endPoint = e.getPoint(); // Update the end point for preview
                        repaint();
                    }
                    mousePosition = e.getPoint(); // Update mouse position for preview
                    repaint();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePosition = e.getPoint(); // Update mouse position for preview
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            // Draw all shapes
            for (Drawable shape : shapes) {
                shape.draw(g2d);
            }
            // Draw eraser preview as a square
            if ("eraser".equals(currentTool) && mousePosition != null) {
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(1));
                int halfSize = eraserSize / 2;
                g2d.drawRect(mousePosition.x - halfSize, mousePosition.y - halfSize, eraserSize, eraserSize);
            }
            // Draw preview shape
            if (startPoint != null && endPoint != null) {
                switch (currentTool) {
                    case "line":
                        new Line(startPoint.x, startPoint.y, endPoint.x, endPoint.y, Color.GRAY, false, brushSize).draw(g2d);
                        break;
                    case "rectangle":
                        new Rectangle(startPoint.x, startPoint.y, endPoint.x, endPoint.y, Color.GRAY, fillShape, brushSize).draw(g2d);
                        break;
                    case "circle":
                        new Circle(startPoint.x, startPoint.y, endPoint.x, endPoint.y, Color.GRAY, fillShape, brushSize).draw(g2d);
                        break;
                    case "ellipse":
                        new Ellipse(startPoint.x, startPoint.y, endPoint.x, endPoint.y, Color.GRAY, fillShape, brushSize).draw(g2d);
                        break;
                }
            }
        }

        private void floodFill(int x, int y, Color fillColor) {
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            paint(g2d); // Draw everything onto the image
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
    }

    private interface Drawable {
        void draw(Graphics2D g2d);
    }

    private abstract class Shape implements Drawable {
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

        public abstract boolean contains(Point p);

        public void fill(Color fillColor) {
            this.color = fillColor;
            this.fill = true;
        }
    }

    private class Line extends Shape {
        public Line(int x1, int y1, int x2, int y2, Color color, boolean fill, int size) {
            super(x1, y1, x2, y2, color, fill, size);
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(size));
            g2d.drawLine(x1, y1, x2, y2);
        }

        @Override
        public boolean contains(Point p) {
            double distance = Math.abs((y2 - y1) * p.x - (x2 - x1) * p.y + x2 * y1 - y2 * x1) /
                    Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
            return distance <= size / 2;
        }
    }

    private class Rectangle extends Shape {
        public Rectangle(int x1, int y1, int x2, int y2, Color color, boolean fill, int size) {
            super(x1, y1, x2, y2, color, fill, size);
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(size));
            if (fill) {
                g2d.fillRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
            } else {
                g2d.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
            }
        }

        @Override
        public boolean contains(Point p) {
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            int width = Math.abs(x2 - x1);
            int height = Math.abs(y2 - y1);
            return p.x >= minX && p.x <= minX + width && p.y >= minY && p.y <= minY + height;
        }
    }

    private class Circle extends Shape {
        public Circle(int x1, int y1, int x2, int y2, Color color, boolean fill, int size) {
            super(x1, y1, x2, y2, color, fill, size);
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(size));
            int diameter = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
            if (fill) {
                g2d.fillOval(x1, y1, diameter, diameter);
            } else {
                g2d.drawOval(x1, y1, diameter, diameter);
            }
        }

        @Override
        public boolean contains(Point p) {
            int centerX = x1 + (x2 - x1) / 2;
            int centerY = y1 + (y2 - y1) / 2;
            int radius = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)) / 2;
            return Math.pow(p.x - centerX, 2) + Math.pow(p.y - centerY, 2) <= Math.pow(radius, 2);
        }
    }

    private class Ellipse extends Shape {
        public Ellipse(int x1, int y1, int x2, int y2, Color color, boolean fill, int size) {
            super(x1, y1, x2, y2, color, fill, size);
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(size));
            if (fill) {
                g2d.fillOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
            } else {
                g2d.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
            }
        }

        @Override
        public boolean contains(Point p) {
            int centerX = Math.min(x1, x2) + Math.abs(x2 - x1) / 2;
            int centerY = Math.min(y1, y2) + Math.abs(y2 - y1) / 2;
            int radiusX = Math.abs(x2 - x1) / 2;
            int radiusY = Math.abs(y2 - y1) / 2;
            return Math.pow((p.x - centerX) / (double) radiusX, 2) + Math.pow((p.y - centerY) / (double) radiusY, 2) <= 1;
        }
    }

    private class ImageShape implements Drawable {
        private BufferedImage image;

        public ImageShape(BufferedImage image) {
            this.image = image;
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.drawImage(image, 0, 0, null);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DrawingApp app = new DrawingApp();
            app.setVisible(true);
        });
    }
}