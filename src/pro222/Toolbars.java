package pro222;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

public class Toolbars {
    private DrawingPanel drawingPanel;
    private CardLayout cardLayout;
    private JPanel sliderPanel;

    public Toolbars(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
    }

    public JPanel createTopToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        toolbar.add(createToolButton("resources/pencil.png", "pencil", "brush"));
        toolbar.add(createToolButton("resources/eraser.png", "eraser", "eraser"));
        toolbar.add(createToolButton("resources/line.png", "line", "brush"));
        toolbar.add(createToolButton("resources/rectangle.png", "rectangle", "brush"));
        toolbar.add(createToolButton("resources/triangle.png", "triangle", "brush"));
        toolbar.add(createToolButton("resources/circle.png", "circle", "brush"));
        toolbar.add(createToolButton("resources/ellipse.png", "ellipse", "brush"));
        toolbar.add(createToolButton("resources/polygon.png", "polygon", "sides"));
        toolbar.add(createToolButton("resources/spray.png", "spray", "spray"));
        toolbar.add(createToolButton("resources/text.png", "text", "text"));
        toolbar.add(createToolButton("resources/fill.png", "fill", "empty"));
        
        JButton primaryColorBtn = new JButton(new ImageIcon(new ImageIcon("resources/color.png")
                .getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        primaryColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(null, "Choose Primary Color", drawingPanel.getCurrentColor());
            if (c != null) drawingPanel.setCurrentColor(c);
        });
        toolbar.add(primaryColorBtn);
        
        JCheckBox fillCheck = new JCheckBox("Fill");
        fillCheck.addActionListener(e -> drawingPanel.setFillShape(fillCheck.isSelected()));
        toolbar.add(fillCheck);
        
        return toolbar;
    }

    public JPanel createSideToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Shape Options Panel
        JPanel shapePanel = new JPanel(new GridLayout(0, 1, 5, 5));
        shapePanel.setBorder(BorderFactory.createTitledBorder("Shape Options"));
        
        // Polygon sides spinner
        JPanel sidesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sidesPanel.add(new JLabel("Polygon Sides:"));
        JSpinner sidesSpinner = new JSpinner(new SpinnerNumberModel(drawingPanel.getPolygonSides(), 3, 20, 1));
        sidesSpinner.addChangeListener(e -> drawingPanel.setPolygonSides((Integer)sidesSpinner.getValue()));
        sidesPanel.add(sidesSpinner);
        shapePanel.add(sidesPanel);
        
        // Rounded rectangle radius spinner
        JPanel radiusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radiusPanel.add(new JLabel("Corner Radius:"));
        JSpinner radiusSpinner = new JSpinner(new SpinnerNumberModel(drawingPanel.getRoundedRectArc(), 0, 100, 5));
        radiusSpinner.addChangeListener(e -> drawingPanel.setRoundedRectArc((Integer)radiusSpinner.getValue()));
        radiusPanel.add(radiusSpinner);
        shapePanel.add(radiusPanel);
        
        // Text size spinner
        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textPanel.add(new JLabel("Text Size:"));
        JSpinner textSizeSpinner = new JSpinner(new SpinnerNumberModel(drawingPanel.getTextSize(), 8, 72, 1));
        textSizeSpinner.addChangeListener(e -> drawingPanel.setTextSize((Integer)textSizeSpinner.getValue()));
        textPanel.add(textSizeSpinner);
        shapePanel.add(textPanel);
        
        // Spray density spinner
        JPanel sprayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sprayPanel.add(new JLabel("Spray Density:"));
        JSpinner spraySpinner = new JSpinner(new SpinnerNumberModel(drawingPanel.getSprayDensity(), 10, 200, 10));
        spraySpinner.addChangeListener(e -> drawingPanel.setSprayDensity((Integer)spraySpinner.getValue()));
        sprayPanel.add(spraySpinner);
        shapePanel.add(sprayPanel);
        
        toolbar.add(shapePanel);
        toolbar.add(Box.createVerticalStrut(20));
        
       
        
        return toolbar;
    }

    public JPanel createSliderPanel() {
        cardLayout = new CardLayout();
        sliderPanel = new JPanel(cardLayout);
        
        // Empty panel (default)
        sliderPanel.add(new JPanel(), "empty");
        
        // Brush size slider
        JPanel brushPanel = createVerticalSliderPanel("Brush Size", 1, 50, 
            drawingPanel.getBrushSize(), e -> {
                JSlider source = (JSlider)e.getSource();
                drawingPanel.setBrushSize(source.getValue());
            });
        sliderPanel.add(brushPanel, "brush");
        
        // Eraser size slider
        JPanel eraserPanel = createVerticalSliderPanel("Eraser Size", 5, 100, 
            drawingPanel.getEraserSize(), e -> {
                JSlider source = (JSlider)e.getSource();
                drawingPanel.setEraserSize(source.getValue());
            });
        sliderPanel.add(eraserPanel, "eraser");
        
        // Spray density slider
        JPanel sprayPanel = createVerticalSliderPanel("Spray Density", 10, 200,
            drawingPanel.getSprayDensity(), e -> {
                JSlider source = (JSlider)e.getSource();
                drawingPanel.setSprayDensity(source.getValue());
            });
        sliderPanel.add(sprayPanel, "spray");
        
        // Polygon sides panel
        JPanel sidesPanel = new JPanel(new BorderLayout());
        sidesPanel.add(new JLabel("Polygon Sides:", SwingConstants.CENTER), BorderLayout.NORTH);
        JSpinner sidesSpinner = new JSpinner(new SpinnerNumberModel(drawingPanel.getPolygonSides(), 3, 20, 1));
        sidesSpinner.addChangeListener(e -> 
            drawingPanel.setPolygonSides((Integer)sidesSpinner.getValue()));
        sidesPanel.add(sidesSpinner, BorderLayout.CENTER);
        sliderPanel.add(sidesPanel, "sides");
        
        // Text input panel
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(new JLabel("Enter Text:", SwingConstants.CENTER), BorderLayout.NORTH);
        JTextField textField = new JTextField();
        textField.addActionListener(e -> 
            drawingPanel.setCurrentText(textField.getText()));
        textPanel.add(textField, BorderLayout.CENTER);
        sliderPanel.add(textPanel, "text");
        
        return sliderPanel;
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
//        JButton button = new JButton(text);
    	JButton button = new JButton(new ImageIcon(new ImageIcon(text)
                .getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        button.addActionListener(e -> {
            drawingPanel.setCurrentTool(toolName);
            if (cardLayout != null && sliderPanel != null) {
                cardLayout.show(sliderPanel, panelToShow);
            }
        });
        return button;
    }
}