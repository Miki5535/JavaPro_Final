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
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

@SuppressWarnings("serial")
public class DrawingApp_old extends JFrame {

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
    
    private final ArrayList<Drawable> shapes = new ArrayList<>();
    private final Stack<ArrayList<Drawable>> undoStack = new Stack<>();
    private final Stack<ArrayList<Drawable>> redoStack = new Stack<>();
    
    private DrawingPanel drawingPanel;
    private JPanel sliderPanel;
    private CardLayout cardLayout;
    private boolean erasing = false;

    public DrawingApp_old() {
        setTitle("Drawing Application Pro");
        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        
        initComponents();
        
        
        setupMenuBar();
        
        
        setupKeyboardShortcuts();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Create drawing panel
        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);
        
        // Create top toolbar
        JPanel topToolbar = createTopToolbar();
        add(topToolbar, BorderLayout.NORTH);
        
        // Create side toolbar
        JPanel sideToolbar = createSideToolbar();
        add(sideToolbar, BorderLayout.WEST);
        
        // Create slider panel
        sliderPanel = createSliderPanel();
        add(sliderPanel, BorderLayout.EAST);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save As...");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        newItem.addActionListener(e -> newDrawing());
        openItem.addActionListener(e -> openImage());
        saveItem.addActionListener(e -> saveImage());
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        JMenuItem clearItem = new JMenuItem("Clear");
        
        undoItem.addActionListener(e -> undo());
        redoItem.addActionListener(e -> redo());
        clearItem.addActionListener(e -> clearCanvas());
        
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(clearItem);
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem gridItem = new JCheckBoxMenuItem("Show Grid");
        gridItem.addActionListener(e -> drawingPanel.setShowGrid(gridItem.isSelected()));
        
        viewMenu.add(gridItem);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);
    }

    private JPanel createTopToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Drawing tools
        toolbar.add(createToolButton("Pencil", "pencil", "brush"));
        toolbar.add(createToolButton("Eraser", "eraser", "eraser"));
        toolbar.add(createToolButton("Line", "line", "brush"));
        toolbar.add(createToolButton("Rectangle", "rectangle", "brush"));
        toolbar.add(createToolButton("Triangle", "triangle", "brush"));
        toolbar.add(createToolButton("Circle", "circle", "brush"));
        toolbar.add(createToolButton("Ellipse", "ellipse", "brush"));
        toolbar.add(createToolButton("Polygon", "polygon", "sides"));
        toolbar.add(createToolButton("Spray", "spray", "spray"));
        toolbar.add(createToolButton("Text", "text", "text"));
        toolbar.add(createToolButton("Fill", "fill", "empty"));
        
        // Color selection
        JButton primaryColorBtn = new JButton("Primary Color");
        primaryColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose Primary Color", currentColor);
            if (c != null) currentColor = c;
        });
        toolbar.add(primaryColorBtn);
        

        
        // Options
        JCheckBox fillCheck = new JCheckBox("Fill");
        fillCheck.addActionListener(e -> fillShape = fillCheck.isSelected());
        toolbar.add(fillCheck);
        

        

        
        return toolbar;
    }

    private JPanel createSideToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Shape options
        JPanel shapePanel = new JPanel(new GridLayout(0, 1, 5, 5));
        shapePanel.setBorder(BorderFactory.createTitledBorder("Shape Options"));
        
        // Polygon sides
        JPanel sidesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sidesPanel.add(new JLabel("Polygon Sides:"));
        JSpinner sidesSpinner = new JSpinner(new SpinnerNumberModel(polygonSides, 3, 20, 1));
        sidesSpinner.addChangeListener(e -> polygonSides = (Integer)sidesSpinner.getValue());
        sidesPanel.add(sidesSpinner);
        shapePanel.add(sidesPanel);
        
        // Rounded rectangle radius
        JPanel radiusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radiusPanel.add(new JLabel("Corner Radius:"));
        JSpinner radiusSpinner = new JSpinner(new SpinnerNumberModel(roundedRectArc, 0, 100, 5));
        radiusSpinner.addChangeListener(e -> roundedRectArc = (Integer)radiusSpinner.getValue());
        radiusPanel.add(radiusSpinner);
        shapePanel.add(radiusPanel);
        
        // Text options
        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textPanel.add(new JLabel("Text Size:"));
        JSpinner textSizeSpinner = new JSpinner(new SpinnerNumberModel(textSize, 8, 72, 1));
        textSizeSpinner.addChangeListener(e -> textSize = (Integer)textSizeSpinner.getValue());
        textPanel.add(textSizeSpinner);
        shapePanel.add(textPanel);
        
        toolbar.add(shapePanel);
        
        // Layer controls
       
        
        JButton bringToFrontBtn = new JButton("Bring to Front");
        bringToFrontBtn.addActionListener(e -> bringToFront());
        
        JButton sendToBackBtn = new JButton("Send to Back");
        sendToBackBtn.addActionListener(e -> sendToBack());
        

        
        toolbar.add(Box.createVerticalStrut(20));

        
        return toolbar;
    }

    private JPanel createSliderPanel() {
        cardLayout = new CardLayout();
        JPanel panel = new JPanel(cardLayout);
        
        // Empty panel
        panel.add(new JPanel(), "empty");
        
        // Brush size slider
        panel.add(createVerticalSliderPanel("Brush Size", 1, 50, brushSize, 
            e -> brushSize = ((JSlider)e.getSource()).getValue()), "brush");
        
        // Eraser size slider
        panel.add(createVerticalSliderPanel("Eraser Size", 5, 100, eraserSize, 
            e -> eraserSize = ((JSlider)e.getSource()).getValue()), "eraser");
        
        // Spray density slider
        panel.add(createVerticalSliderPanel("Spray Density", 10, 200, sprayDensity,
            e -> sprayDensity = ((JSlider)e.getSource()).getValue()), "spray");
        
        // Polygon sides panel
        JPanel sidesPanel = new JPanel(new BorderLayout());
        sidesPanel.add(new JLabel("Polygon Sides:", SwingConstants.CENTER), BorderLayout.NORTH);
        JSpinner sidesSpinner = new JSpinner(new SpinnerNumberModel(polygonSides, 3, 20, 1));
        sidesSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                polygonSides = (Integer)sidesSpinner.getValue();
            }
        });
        sidesPanel.add(sidesSpinner, BorderLayout.CENTER);
        panel.add(sidesPanel, "sides");
        
        // Text input panel
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(new JLabel("Enter Text:", SwingConstants.CENTER), BorderLayout.NORTH);
        JTextField textField = new JTextField();
        textField.addActionListener(e -> currentText = textField.getText());
        textPanel.add(textField, BorderLayout.CENTER);
        panel.add(textPanel, "text");
        
        return panel;
    }

    private JPanel createVerticalSliderPanel(String title, int min, int max, int initial, ChangeListener listener) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JSlider slider = new JSlider(JSlider.VERTICAL, min, max, initial);
        slider.setMajorTickSpacing((max - min) / 5);
        slider.setMinorTickSpacing((max - min) / 10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(listener);
        
        panel.add(slider, BorderLayout.CENTER);
        return panel;
    }

    
    
    
    private JButton createToolButton(String text, String toolName, String panelToShow) {
        JButton button = new JButton(text);
        button.addActionListener(e -> {
            currentTool = toolName;
            if (cardLayout != null && sliderPanel != null) {
                cardLayout.show(sliderPanel, panelToShow);
            }
        });
        return button;
    }

    private void setupKeyboardShortcuts() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.isControlDown()) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_Z: undo(); return true;
                    case KeyEvent.VK_Y: redo(); return true;
                    case KeyEvent.VK_N: newDrawing(); return true;
                    case KeyEvent.VK_O: openImage(); return true;
                    case KeyEvent.VK_S: saveImage(); return true;
                }
            }
            return false;
        });
    }

    private void saveState() {
        redoStack.clear();
        ArrayList<Drawable> copy = new ArrayList<>(shapes.size());
        for (Drawable shape : shapes) {
            copy.add(shape);
        }
        undoStack.push(copy);
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            ArrayList<Drawable> currentState = new ArrayList<>(shapes);
            redoStack.push(currentState);
            shapes.clear();
            shapes.addAll(undoStack.pop());
            repaint();
        }
    }
    
    private void redo() {
        if (!redoStack.isEmpty()) {
            ArrayList<Drawable> currentState = new ArrayList<>(shapes);
            undoStack.push(currentState);
            shapes.clear();
            shapes.addAll(redoStack.pop());
            repaint();
        }
    }

    private void newDrawing() {
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

    private void clearCanvas() {
        shapes.clear();
        repaint();
    }

    private void bringToFront() {
        // Implementation to bring selected shape to front
        // (Would need selection logic to be implemented)
    }

    private void sendToBack() {
        // Implementation to send selected shape to back
        // (Would need selection logic to be implemented)
    }

    private void openImage() {
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

    private void saveImage() {
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
                    drawingPanel.getWidth(), drawingPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = image.createGraphics();
                drawingPanel.paint(g2d);
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

    private class DrawingPanel extends JPanel {
        private Point mousePosition = null;
        private boolean showGrid = false;
        private final int GRID_SIZE = 20;

        public void setShowGrid(boolean show) {
            showGrid = show;
            repaint();
        }

        public DrawingPanel() {
            setBackground(Color.WHITE);
            setupMouseListeners();
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
                        erasing = false; // จบการลบ
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
                    }  else if ("eraser".equals(currentTool)) {
                        if (!erasing) { // เริ่มการลบใหม่
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
            
            // Draw grid if enabled
            if (showGrid) {
                g2d.setColor(Color.LIGHT_GRAY);
                for (int x = 0; x < getWidth(); x += GRID_SIZE) {
                    g2d.drawLine(x, 0, x, getHeight());
                }
                for (int y = 0; y < getHeight(); y += GRID_SIZE) {
                    g2d.drawLine(0, y, getWidth(), y);
                }
            }
            
            // Draw all shapes
            for (Drawable shape : shapes) {
                shape.draw(g2d);
            }
            
            // Draw tool previews
            drawToolPreviews(g2d);
        }

        private void drawToolPreviews(Graphics2D g2d) {
            if (mousePosition == null) return;
            
            // Eraser preview
         // ในเมธอด paintComponent ของ DrawingPanel
            if ("eraser".equals(currentTool) && mousePosition != null) {
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(1));
                int halfSize = eraserSize / 2;
                // วาด preview เป็นสี่เหลี่ยม
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
                int height = Math.abs(endPoint.y - startPoint.y);
                
                int[] xPoints = {
                    startPoint.x + width/2,  // จุดยอดบน
                    startPoint.x,             // จุดซ้ายล่าง
                    endPoint.x                // จุดขวาล่าง
                };
                
                int[] yPoints = {
                    startPoint.y,             // จุดยอดบน
                    endPoint.y,               // จุดซ้ายล่าง
                    endPoint.y                // จุดขวาล่าง
                };
                
                if (fillShape) {
                    g2d.fillPolygon(xPoints, yPoints, 3);
                } else {
                    g2d.drawPolygon(xPoints, yPoints, 3);
                }
                
                g2d.setStroke(oldStroke);
            }
            
            // Shape preview
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
            
            // Spray can preview
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
                case "triangle":  // เพิ่มกรณีสำหรับสามเหลี่ยม
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
    }

    private class Line extends Shape {
        public Line(int x1, int y1, int x2, int y2, Color color, boolean fill, int size) {
            super(x1, y1, x2, y2, color, fill, size);
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            if (dashedLine) {
                g2d.setStroke(new BasicStroke(size, BasicStroke.CAP_BUTT, 
                    BasicStroke.JOIN_BEVEL, 0, dashPattern, 0));
            } else {
                g2d.setStroke(new BasicStroke(size));
            }
            
            // ใช้อัลกอริทึม DDA ในการวาดเส้นตรง
            drawDDALine(g2d, x1, y1, x2, y2);
        }
        
        private void drawDDALine(Graphics2D g, int x1, int y1, int x2, int y2) {
            // คำนวณความแตกต่างในแกน x และ y
            int dx = x2 - x1;
            int dy = y2 - y1;
            
            // คำนวณจำนวนขั้นตอนที่จำเป็นสำหรับการสร้างพิกเซล
            int steps = Math.max(Math.abs(dx), Math.abs(dy));
            
            // คำนวณการเพิ่มค่าใน x และ y สำหรับแต่ละขั้นตอน
            float xIncrement = dx / (float) steps;
            float yIncrement = dy / (float) steps;
            
            // วาดพิกเซลสำหรับแต่ละขั้นตอน
            float x = x1;
            float y = y1;
            
            for (int i = 0; i <= steps; i++) {
                // วาดพิกเซลที่ตำแหน่ง (x,y) โดยปัดเศษเป็นจำนวนเต็มที่ใกล้ที่สุด
                g.drawLine(Math.round(x), Math.round(y), Math.round(x), Math.round(y));
                x += xIncrement;
                y += yIncrement;
            }
        }
    }

    
    
    private class RectangleShape extends Shape {
        public RectangleShape(int x1, int y1, int x2, int y2, Color color, boolean fill, int size) {
            super(x1, y1, x2, y2, color, fill, size);
        }

        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(size));
            
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            int maxX = Math.max(x1, x2);
            int maxY = Math.max(y1, y2);
            
            if (fill) {
                // หากต้องการเติมสี่เหลี่ยม
                g2d.fillRect(minX, minY, maxX - minX, maxY - minY);
            } else {
                // ใช้ DDA Line วาด 4 ด้านของสี่เหลี่ยม
                drawDDALine(g2d, minX, minY, maxX, minY); // ด้านบน
                drawDDALine(g2d, maxX, minY, maxX, maxY); // ด้านขวา
                drawDDALine(g2d, maxX, maxY, minX, maxY); // ด้านล่าง
                drawDDALine(g2d, minX, maxY, minX, minY); // ด้านซ้าย
            }
        }
        
        private void drawDDALine(Graphics2D g, int x1, int y1, int x2, int y2) {
            // คำนวณความแตกต่างในแกน x และ y
            int dx = x2 - x1;
            int dy = y2 - y1;
            
            // คำนวณจำนวนขั้นตอนที่จำเป็นสำหรับการสร้างพิกเซล
            int steps = Math.max(Math.abs(dx), Math.abs(dy));
            
            // คำนวณการเพิ่มค่าใน x และ y สำหรับแต่ละขั้นตอน
            float xIncrement = dx / (float) steps;
            float yIncrement = dy / (float) steps;
            
            // กำหนดสีและความหนาของเส้น
            g.setColor(color);
            g.setStroke(new BasicStroke(size));
            
            // วาดแต่ละพิกเซลตามแนวเส้น
            float x = x1;
            float y = y1;
            
            for (int i = 0; i <= steps; i++) {
                // วาดพิกเซลที่ตำแหน่ง (x,y) โดยปัดเศษเป็นจำนวนเต็มที่ใกล้ที่สุด
                g.drawLine(Math.round(x), Math.round(y), Math.round(x), Math.round(y));
                x += xIncrement;
                y += yIncrement;
            }
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
            g.drawLine(xCenter + x, yCenter + y, xCenter + x, yCenter + y); // Quadrant 1
            g.drawLine(xCenter + y, yCenter + x, xCenter + y, yCenter + x); // Quadrant 2
            g.drawLine(xCenter + y, yCenter - x, xCenter + y, yCenter - x); // Quadrant 3
            g.drawLine(xCenter + x, yCenter - y, xCenter + x, yCenter - y); // Quadrant 4
            g.drawLine(xCenter - x, yCenter - y, xCenter - x, yCenter - y); // Quadrant 5
            g.drawLine(xCenter - y, yCenter - x, xCenter - y, yCenter - x); // Quadrant 6
            g.drawLine(xCenter - y, yCenter + x, xCenter - y, yCenter + x); // Quadrant 7
            g.drawLine(xCenter - x, yCenter + y, xCenter - x, yCenter + y); // Quadrant 8
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
            // วาดเส้นแนวนอนเพื่อเติมวงกลม
            g.drawLine(xCenter - x, yCenter + y, xCenter + x, yCenter + y);
            g.drawLine(xCenter - y, yCenter + x, xCenter + y, yCenter + x);
            g.drawLine(xCenter - y, yCenter - x, xCenter + y, yCenter - x);
            g.drawLine(xCenter - x, yCenter - y, xCenter + x, yCenter - y);
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
            
            int width = Math.abs(x2 - x1);
            int height = Math.abs(y2 - y1);
            int xCenter = Math.min(x1, x2) + width / 2;
            int yCenter = Math.min(y1, y2) + height / 2;
            int rx = width / 2;
            int ry = height / 2;
            
            if (fill) {
                drawFilledMidpointEllipse(g2d, xCenter, yCenter, rx, ry);
            } else {
                drawMidpointEllipse(g2d, xCenter, yCenter, rx, ry);
            }
        }
        
        private void plot4Points(Graphics2D g, int x, int y, int xCenter, int yCenter) {
            g.drawLine(xCenter + x, yCenter + y, xCenter + x, yCenter + y); // Quadrant 1
            g.drawLine(xCenter + x, yCenter - y, xCenter + x, yCenter - y); // Quadrant 2
            g.drawLine(xCenter - x, yCenter - y, xCenter - x, yCenter - y); // Quadrant 3
            g.drawLine(xCenter - x, yCenter + y, xCenter - x, yCenter + y); // Quadrant 4
        }
        
        private void drawMidpointEllipse(Graphics2D g, int xCenter, int yCenter, int rx, int ry) {
            int x = 0;
            int y = ry;
            int rx2 = rx * rx;
            int ry2 = ry * ry;
            int tworx2 = 2 * rx2;
            int twory2 = 2 * ry2;
            int px = 0;
            int py = tworx2 * y;
            int p;
            
            // วาดจุดเริ่มต้น
            plot4Points(g, x, y, xCenter, yCenter);
            
            // Region 1
            p = ry2 - (rx2 * ry) + (rx2 / 4);
            while (px < py) {
                x++;
                px += twory2;
                if (p < 0) {
                    p += ry2 + px;
                } else {
                    y--;
                    py -= tworx2;
                    p += ry2 + px - py;
                }
                plot4Points(g, x, y, xCenter, yCenter);
            }
            
            // Region 2
            p = ry2 * (x + 1) * (x + 1) + rx2 * (y - 1) * (y - 1) - rx2 * ry2;
            while (y > 0) {
                y--;
                py -= tworx2;
                if (p > 0) {
                    p += rx2 - py;
                } else {
                    x++;
                    px += twory2;
                    p += rx2 - py + px;
                }
                plot4Points(g, x, y, xCenter, yCenter);
            }
        }
        
        private void drawFilledMidpointEllipse(Graphics2D g, int xCenter, int yCenter, int rx, int ry) {
            int x = 0;
            int y = ry;
            int rx2 = rx * rx;
            int ry2 = ry * ry;
            int tworx2 = 2 * rx2;
            int twory2 = 2 * ry2;
            int px = 0;
            int py = tworx2 * y;
            int p;
            
            // วาดเส้นแนวนอนเริ่มต้น
            drawHorizontalLine(g, x, y, xCenter, yCenter);
            
            // Region 1
            p = ry2 - (rx2 * ry) + (rx2 / 4);
            while (px < py) {
                x++;
                px += twory2;
                if (p < 0) {
                    p += ry2 + px;
                } else {
                    y--;
                    py -= tworx2;
                    p += ry2 + px - py;
                }
                drawHorizontalLine(g, x, y, xCenter, yCenter);
            }
            
            // Region 2
            p = ry2 * (x + 1) * (x + 1) + rx2 * (y - 1) * (y - 1) - rx2 * ry2;
            while (y > 0) {
                y--;
                py -= tworx2;
                if (p > 0) {
                    p += rx2 - py;
                } else {
                    x++;
                    px += twory2;
                    p += rx2 - py + px;
                }
                drawHorizontalLine(g, x, y, xCenter, yCenter);
            }
        }
        
        private void drawHorizontalLine(Graphics2D g, int x, int y, int xCenter, int yCenter) {
            // วาดเส้นแนวนอนเพื่อเติมวงรี
            g.drawLine(xCenter - x, yCenter + y, xCenter + x, yCenter + y);
            g.drawLine(xCenter - x, yCenter - y, xCenter + x, yCenter - y);
        }
    }

    
    
    private class PolygonShape implements Drawable {
        private int[] xPoints, yPoints;
        private int nPoints;
        private Color color;
        private boolean fill;
        private int size;
        
        public PolygonShape(Point start, Point end, int sides, Color color, boolean fill, int size) {
            this.color = color;
            this.fill = fill;
            this.size = size;
            this.nPoints = sides;
            
            xPoints = new int[sides];
            yPoints = new int[sides];
            
            int centerX = (start.x + end.x) / 2;
            int centerY = (start.y + end.y) / 2;
            int radius = Math.min(Math.abs(end.x - start.x), Math.abs(end.y - start.y)) / 2;
            
            double angle = 2 * Math.PI / sides;
            for (int i = 0; i < sides; i++) {
                xPoints[i] = centerX + (int)(radius * Math.cos(i * angle - Math.PI/2));
                yPoints[i] = centerY + (int)(radius * Math.sin(i * angle - Math.PI/2));
            }
        }
        
        @Override
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(size));
            if (fill) {
                g2d.fillPolygon(xPoints, yPoints, nPoints);
            } else {
                g2d.drawPolygon(xPoints, yPoints, nPoints);
            }
        }
    }
    
    
    private class TriangleShape implements Drawable {
        private Point p1, p2, p3;
        private Color color;
        private boolean fill;
        private int size;
        
        public TriangleShape(Point start, Point end, Color color, boolean fill, int size) {
            this.color = color;
            this.fill = fill;
            this.size = size;
            
            // คำนวณจุด 3 จุดของสามเหลี่ยม
            int width = Math.abs(end.x - start.x);
            int height = Math.abs(end.y - start.y);
            
            this.p1 = new Point(start.x + width/2, start.y); // จุดยอดบน
            this.p2 = new Point(start.x, end.y);             // จุดซ้ายล่าง
            this.p3 = new Point(end.x, end.y);               // จุดขวาล่าง
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
                // วาดด้วย DDA Line ทั้ง 3 ด้าน
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
    
    
    
    private class SprayDot implements Drawable {
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

    private class TextShape implements Drawable {
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
            DrawingApp_old app = new DrawingApp_old();
            app.setVisible(true);
        });
    }
}