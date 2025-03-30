package pro222;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class DrawingApp extends JFrame {
    private DrawingPanel drawingPanel;

    public DrawingApp() {
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
        
        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);
        
        Toolbars toolbars = new Toolbars(drawingPanel);
        add(toolbars.createTopToolbar(), BorderLayout.NORTH);
        add(toolbars.createSideToolbar(), BorderLayout.WEST);
        add(toolbars.createSliderPanel(), BorderLayout.EAST);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(drawingPanel.createMenuItem("New", "new"));
        fileMenu.add(drawingPanel.createMenuItem("Open", "open"));
        fileMenu.add(drawingPanel.createMenuItem("Save As...", "save"));
        fileMenu.addSeparator();
        fileMenu.add(drawingPanel.createMenuItem("Exit", "exit"));
        
        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(drawingPanel.createMenuItem("Undo", "undo"));
        editMenu.add(drawingPanel.createMenuItem("Redo", "redo"));
        editMenu.addSeparator();
        editMenu.add(drawingPanel.createMenuItem("Clear", "clear"));
        
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

    private void setupKeyboardShortcuts() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.isControlDown()) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_Z: drawingPanel.undo(); return true;
                    case KeyEvent.VK_Y: drawingPanel.redo(); return true;
                    case KeyEvent.VK_N: drawingPanel.newDrawing(); return true;
                    case KeyEvent.VK_O: drawingPanel.openImage(); return true;
                    case KeyEvent.VK_S: drawingPanel.saveImage(); return true;
                }
            }
            return false;
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DrawingApp app = new DrawingApp();
            app.setVisible(true);
        });
    }
}