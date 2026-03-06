package view;
import model.*;
import util.FileManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;


public class FloorMapUI extends JFrame {
    // --- Components ---
    private DrawPanel drawPanel;
    private JPanel sidebar;
    private JButton computeBtn, resetBtn, saveBtn, loadBtn, exportTxtBtn, loadBgBtn, clearBgBtn, summaryBtn;
    private JCheckBox realtimeChk;
    private JComboBox<String> algoCombo;
    private JTextField costPerMeterField;
    private JLabel statusLabel;
    // Placement state
    private String placementType = null;
    // Colors & Fonts
    private final Color COLOR_BG_DARK = new Color(30, 30, 35);
    private final Color COLOR_SIDEBAR = new Color(40, 42, 50);
    private final Color COLOR_ACCENT = new Color(75, 110, 240); // Modern Blue
    private final Color COLOR_ACCENT_HOVER = new Color(95, 130, 255);
    private final Color COLOR_TEXT = new Color(220, 220, 220);
    private final Color COLOR_GRID = new Color(50, 50, 55);
    private final Font FONT_UI = new Font("Segoe UI", Font.PLAIN, 12);
    public FloorMapUI() {
        setTitle("Network Cable Optimizer");
        setSize(1280, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Use a dark background for the frame
        getContentPane().setBackground(COLOR_BG_DARK);
        setLayout(new BorderLayout());
        // 1. Initialize Components
        initComponents();
        // 2. Build Layouts
        buildSidebar();
        
        drawPanel = new DrawPanel();
        add(drawPanel, BorderLayout.CENTER);
        
        add(sidebar, BorderLayout.WEST);
        add(buildStatusBar(), BorderLayout.SOUTH);
        setVisible(true);
    }
    
    private void initComponents() {
        // Dropdown
        algoCombo = new JComboBox<>(new String[]{"Kruskal", "Prim"});
        algoCombo.setBackground(COLOR_SIDEBAR);
        algoCombo.setForeground(Color.BLACK); 
        
        // Checkbox
        realtimeChk = new JCheckBox("Real-time Mode");
        realtimeChk.setBackground(COLOR_SIDEBAR);
        realtimeChk.setForeground(COLOR_TEXT);
        realtimeChk.setFocusPainted(false);
        // Text Field
        costPerMeterField = new JTextField("2.0");
        costPerMeterField.setCaretColor(Color.WHITE);
        costPerMeterField.setBackground(new Color(60, 62, 70));
        costPerMeterField.setForeground(Color.WHITE);
        costPerMeterField.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        // Buttons - Using custom ModernButton helper
        computeBtn = new ModernButton("Compute MST", true);
        resetBtn = new ModernButton("Reset Canvas", false);
        saveBtn = new ModernButton("Save Project", false);
        loadBtn = new ModernButton("Load Project", false);
        exportTxtBtn = new ModernButton("Export Report", false);
        loadBgBtn = new ModernButton("Set Floorplan", false);
        clearBgBtn = new ModernButton("Clear Floorplan", false);
        summaryBtn = new ModernButton("Detailed Costs", false);
        // Actions
        computeBtn.addActionListener(e -> drawPanel.computeMST(true));
        resetBtn.addActionListener(e -> drawPanel.resetAll());
        saveBtn.addActionListener(e -> saveDesign());
        loadBtn.addActionListener(e -> loadDesign());
        exportTxtBtn.addActionListener(e -> exportMstTxt());
        loadBgBtn.addActionListener(e -> loadBackground());
        clearBgBtn.addActionListener(e -> drawPanel.setBackgroundImagePath(null));
        summaryBtn.addActionListener(e -> drawPanel.showCostSummary());
        realtimeChk.addActionListener(e -> drawPanel.setRealtime(realtimeChk.isSelected()));
    }
    
    private void buildSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_SIDEBAR);
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));
        sidebar.setPreferredSize(new Dimension(240, 0));
        // --- Section: Configuration ---
        addSidebarHeader(sidebar, "CONFIGURATION");
        
        JPanel configPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        configPanel.setBackground(COLOR_SIDEBAR);
        configPanel.add(new JLabel("Algorithm:") {{ setForeground(COLOR_TEXT); }});
        configPanel.add(algoCombo);
        configPanel.add(new JLabel("Cost ($/m):") {{ setForeground(COLOR_TEXT); }});
        configPanel.add(costPerMeterField);
        
        configPanel.setMaximumSize(new Dimension(240, 60));
        configPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(configPanel);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(realtimeChk);
        
        sidebar.add(Box.createVerticalStrut(20));
        // --- Section: Devices ---
        addSidebarHeader(sidebar, "DEVICE PALETTE");
        sidebar.add(createDeviceButton("PC", new Color(46, 204, 113)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createDeviceButton("Router", new Color(155, 89, 182)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createDeviceButton("Switch", new Color(52, 152, 219)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createDeviceButton("Server", new Color(231, 76, 60)));
        
        sidebar.add(Box.createVerticalStrut(10));
        JButton cancelBtn = new ModernButton("Stop Placing", false);
        cancelBtn.setBackground(new Color(60,60,60));
        cancelBtn.addActionListener(e -> {
            placementType = null;
            drawPanel.setCursor(Cursor.getDefaultCursor());
            statusLabel.setText("Ready");
        });
        sidebar.add(cancelBtn);
        sidebar.add(Box.createVerticalStrut(30));
        // --- Section: Actions ---
        addSidebarHeader(sidebar, "ACTIONS");
        sidebar.add(computeBtn);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(summaryBtn);
        sidebar.add(Box.createVerticalStrut(20)); // Spacer
        // --- Section: File ---
        addSidebarHeader(sidebar, "FILE / EDIT");
        
        JPanel fileGrid = new JPanel(new GridLayout(3, 2, 5, 5));
        fileGrid.setBackground(COLOR_SIDEBAR);
        fileGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        fileGrid.setMaximumSize(new Dimension(240, 100));
        
        fileGrid.add(saveBtn);
        fileGrid.add(loadBtn);
        fileGrid.add(loadBgBtn);
        fileGrid.add(clearBgBtn);
        fileGrid.add(exportTxtBtn);
        fileGrid.add(resetBtn);
        
        sidebar.add(fileGrid);
    }
    
    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        p.setBackground(new Color(25, 25, 30));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50,50,50)));
        
        statusLabel = new JLabel("Ready | Left Click: Add/Drag | Right Click: Context Menu | Ctrl+Drag: Link");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        p.add(statusLabel);
        return p;
    }
    private void addSidebarHeader(JPanel p, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(100, 100, 100));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(5));
    }
    private JButton createDeviceButton(String type, Color indicatorColor) {
        JButton b = new JButton(type) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) g2.setColor(new Color(60, 60, 60));
                else if (getModel().isRollover()) g2.setColor(new Color(50, 50, 55));
                else g2.setColor(new Color(45, 47, 55));
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Color Dot
                g2.setColor(indicatorColor);
                g2.fillOval(15, getHeight()/2 - 4, 8, 8);
                
                // Text
                g2.setColor(COLOR_TEXT);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.drawString(getText(), 35, getHeight()/2 + 5);
                
                // Active Indicator
                if (type.equals(placementType)) {
                    g2.setStroke(new BasicStroke(2));
                    g2.setColor(COLOR_ACCENT);
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                }
            }
        };
        
        b.setPreferredSize(new Dimension(200, 35));
        b.setMaximumSize(new Dimension(240, 35));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        b.addActionListener(e -> {
            placementType = type;
            drawPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            statusLabel.setText("Placing: " + type + " (Click on canvas to place)");
            sidebar.repaint(); // To show selection border
        });
        
        return b;
    }
  
    // --- Custom UI Classes ---
    private class ModernButton extends JButton {
        private boolean isPrimary;
        public ModernButton(String text, boolean isPrimary) {
            super(text);
            this.isPrimary = isPrimary;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(FONT_UI);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) {
                g2.setColor(isPrimary ? COLOR_ACCENT.darker() : new Color(50,50,50));
            } else if (getModel().isRollover()) {
                g2.setColor(isPrimary ? COLOR_ACCENT_HOVER : new Color(70,70,70));
            } else {
                g2.setColor(isPrimary ? COLOR_ACCENT : new Color(60,62,70));
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.drawString(getText(), x, y);
            
            g2.dispose();
        }
    }
    // --- Data & Logic methods ---
    private void saveDesign() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Network design JSON (*.json)", "json"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".json")) {
                f = new File(f.getAbsolutePath() + ".json");
            }
            try {
                double costPerMeter = Double.parseDouble(costPerMeterField.getText());
                FileManager.saveAsJson(f, drawPanel.getNodesCopy(), drawPanel.getEdgesCopy(), drawPanel.getBackgroundImagePath(), costPerMeter);
                statusLabel.setText("Saved to " + f.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
            }
        }
    }
    private void loadDesign() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Network design JSON (*.json)", "json"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                FileManager.LoadedData ld = FileManager.loadFromJson(f);
                drawPanel.replaceNodes(ld.nodes);
                drawPanel.replaceEdges(ld.edges);
                drawPanel.setBackgroundImagePath(ld.backgroundPath);
                costPerMeterField.setText(String.valueOf(ld.costPerMeter));
                drawPanel.repaint();
                if (realtimeChk.isSelected()) {
                    drawPanel.computeMST(false);
                }
                statusLabel.setText("Loaded " + f.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage());
            }
        }
    }
    private void exportMstTxt() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("mst-report.txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(f)) {
                List<Edge> mst = drawPanel.getMstEdges();
                pw.println("MST Report");
                pw.println("==========");
                if (mst == null || mst.isEmpty()) {
                    pw.println("No MST computed.");
                } else {
                    double costPerMeter = Double.parseDouble(costPerMeterField.getText());
                    double totalCost = 0.0;
                    for (Edge e : mst) {
                        double cost = e.weight * costPerMeter;
                        totalCost += cost;
                        pw.printf("%d - %d : %d m  cost=%.2f%n", e.src, e.dest, e.weight, cost);
                    }
                    pw.println("----------");
                    pw.printf("Total cost: %.2f%n", totalCost);
                }
                statusLabel.setText("Report exported to " + f.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
            }
        }
    }
    private void loadBackground() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            drawPanel.setBackgroundImagePath(f.getAbsolutePath());
        }
    }
    // --- DrawPanel Inner Class ---
    class DrawPanel extends JPanel {
        private java.util.List<Node> nodes = new ArrayList<>();
        private java.util.List<Edge> edges = new ArrayList<>();
        private java.util.List<Edge> mstEdges = new ArrayList<>();
        private String backgroundPath = null;
        private Image backgroundImage = null;
        private int nextId = 0;
        private boolean realtime = false;
        private int draggingNodeId = -1;
        private Point dragOffset = null;
        private int pressedNodeId = -1;
        private Map<String, Integer> typeCounters = new HashMap<>();
        
        // Modern Rendering Constants
        private final int NODE_SIZE = 50;
        public DrawPanel() {
            setBackground(COLOR_BG_DARK);
            setOpaque(true);
            MouseAdapter ma = new MouseAdapter() {
                private Point pressPoint = null;
                @Override
                public void mousePressed(MouseEvent e) {
                    pressPoint = e.getPoint();
                    pressedNodeId = getNodeIdAtPoint(pressPoint);
                    if (placementType != null && SwingUtilities.isLeftMouseButton(e)) {
                        String type = placementType;
                        String label = defaultNameFor(type);
                        Node n = new Node(nextId++, pressPoint.x, pressPoint.y, type, label);
                        nodes.add(n);
                        // Reset cursor but keep tool selected for multiple placements if desired? 
                        // Original behavior: reset tool
                        placementType = null;
                        setCursor(Cursor.getDefaultCursor());
                        statusLabel.setText("Device Placed: " + label);
                        sidebar.repaint(); // remove active border
                        repaint();
                        if (realtime) computeMST(false);
                        return;
                    }
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (pressedNodeId == -1) {
                            // Only add custom node if not clicking on anything
                            Node n = new Node(nextId++, pressPoint.x, pressPoint.y, "Custom", "Device-" + nextId);
                            nodes.add(n);
                            repaint();
                            if (realtime) computeMST(false);
                        } else {
                            draggingNodeId = pressedNodeId;
                            Node n = getNodeById(draggingNodeId);
                            dragOffset = new Point(pressPoint.x - n.x, pressPoint.y - n.y);
                        }
                    }
                    if (SwingUtilities.isRightMouseButton(e)) {
                        if (pressedNodeId != -1) {
                            showNodeMenu(pressedNodeId, e.getX(), e.getY());
                        } else {
                            int edgeIdx = getEdgeIndexNearPoint(e.getPoint());
                            if (edgeIdx != -1) {
                                showEdgeMenu(edgeIdx, e.getX(), e.getY());
                            }
                        }
                    }
                }
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (draggingNodeId != -1 && !e.isControlDown()) {
                        Node n = getNodeById(draggingNodeId);
                        n.x = e.getX() - dragOffset.x;
                        n.y = e.getY() - dragOffset.y;
                        repaint();
                        if (realtime) computeMST(false);
                    }
                    statusLabel.setText(String.format("Coords: %d, %d", e.getX(), e.getY()));
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (draggingNodeId != -1 && e.isControlDown()) {
                        int target = getNodeIdAtPoint(e.getPoint());
                        if (target != -1 && target != draggingNodeId) {
                            Node a = getNodeById(draggingNodeId);
                            Node b = getNodeById(target);
                            int dist = (int) Math.round(a.getPoint().distance(b.getPoint()));
                            String input = JOptionPane.showInputDialog(DrawPanel.this,
                                    "Create Link\nComputed length: " + dist + "m\nEnter logical length (m):",
                                    String.valueOf(dist));
                            if (input != null) {
                                try {
                                    int len = Integer.parseInt(input);
                                    edges.add(new Edge(draggingNodeId, target, len));
                                    repaint();
                                    if (realtime) computeMST(false);
                                } catch (Exception ex) {
                                    // ignore
                                }
                            }
                        }
                    }
                    draggingNodeId = -1;
                    dragOffset = null;
                }
                
                @Override
                public void mouseMoved(MouseEvent e) {
                    statusLabel.setText(String.format("Coords: %d, %d", e.getX(), e.getY()));
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }
        private String defaultNameFor(String type) {
            int c = typeCounters.getOrDefault(type, 0) + 1;
            typeCounters.put(type, c);
            return type + "-" + c;
        }
        public void setRealtime(boolean r) {
            this.realtime = r;
            if(r) computeMST(false);
        }
        public void setBackgroundImagePath(String path) {
            this.backgroundPath = path;
            if (path == null) {
                backgroundImage = null;
            } else {
                try {
                    backgroundImage = Toolkit.getDefaultToolkit().getImage(path);
                } catch (Exception ex) {
                    backgroundImage = null;
                }
            }
            repaint();
        }
        public String getBackgroundImagePath() { return backgroundPath; }
        public List<Node> getNodesCopy() {
            List<Node> out = new ArrayList<>();
            for (Node n : nodes) out.add(new Node(n.id, n.x, n.y, n.deviceType, n.label));
            return out;
        }
        public List<Edge> getEdgesCopy() {
            List<Edge> out = new ArrayList<>();
            for (Edge e : edges) out.add(new Edge(e.src, e.dest, e.weight));
            return out;
        }
        public List<Edge> getMstEdges() { return mstEdges; }
        public void replaceNodes(List<Node> newNodes) {
            nodes.clear();
            nodes.addAll(newNodes);
            nextId = 0;
            for (Node n : nodes) if (n.id >= nextId) nextId = n.id + 1;
            repaint();
        }
        public void replaceEdges(List<Edge> newEdges) {
            edges.clear();
            edges.addAll(newEdges);
            repaint();
        }
        private Node getNodeById(int id) {
            for (Node n : nodes) if (n.id == id) return n;
            return null;
        }
        private int getNodeIdAtPoint(Point p) {
            for (Node n : nodes) {
                if (p.distance(n.getPoint()) < (NODE_SIZE / 2 + 5)) {
                    return n.id;
                }
            }
            return -1;
        }
        private int getEdgeIndexNearPoint(Point p) {
            for (int i = 0; i < edges.size(); i++) {
                Edge e = edges.get(i);
                Node a = getNodeById(e.src);
                Node b = getNodeById(e.dest);
                if (a == null || b == null) continue;
                Line2D line = new Line2D.Double(a.x, a.y, b.x, b.y);
                if (line.ptSegDist(p) < 8) return i;
            }
            return -1;
        }
        private void showNodeMenu(int nodeId, int x, int y) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem connect = new JMenuItem("Connect...");
            JMenuItem rename = new JMenuItem("Rename");
            JMenuItem changeType = new JMenuItem("Change Type");
            JMenuItem delete = new JMenuItem("Delete");
            connect.addActionListener(a -> {
                String s = JOptionPane.showInputDialog(this, "Target Node ID:");
                if (s != null) {
                    try {
                        int tgt = Integer.parseInt(s);
                        if (getNodeById(tgt) != null) {
                            String lenS = JOptionPane.showInputDialog(this, "Length (m):", "10");
                            if (lenS != null) {
                                edges.add(new Edge(nodeId, tgt, Integer.parseInt(lenS)));
                                repaint();
                                if (realtime) computeMST(false);
                            }
                        }
                    } catch (Exception ex) { /* ignored */ }
                }
            });
            rename.addActionListener(a -> {
                Node n = getNodeById(nodeId);
                String s = JOptionPane.showInputDialog(this, "New Name:", n.label);
                if (s != null && !s.trim().isEmpty()) {
                    n.label = s.trim();
                    repaint();
                }
            });
            changeType.addActionListener(a -> {
                String[] opts = {"PC", "Router", "Switch", "Server", "Custom"};
                String sel = (String) JOptionPane.showInputDialog(this, "Type:", "Change Device", JOptionPane.PLAIN_MESSAGE, null, opts, getNodeById(nodeId).deviceType);
                if (sel != null) {
                    Node n = getNodeById(nodeId);
                    n.deviceType = sel;
                    repaint();
                }
            });
            delete.addActionListener(a -> {
                nodes.removeIf(n -> n.id == nodeId);
                edges.removeIf(e -> e.src == nodeId || e.dest == nodeId);
                repaint();
                if (realtime) computeMST(false);
            });
            menu.add(connect);
            menu.add(rename);
            menu.add(changeType);
            menu.add(new JSeparator());
            menu.add(delete);
            menu.show(this, x, y);
        }
        private void showEdgeMenu(int edgeIdx, int x, int y) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem edit = new JMenuItem("Edit Weight");
            JMenuItem del = new JMenuItem("Delete Connection");
            edit.addActionListener(a -> {
                Edge e = edges.get(edgeIdx);
                String s = JOptionPane.showInputDialog(this, "Length (m):", e.weight);
                try {
                    if (s != null) {
                        e.weight = Integer.parseInt(s);
                        repaint();
                        if (realtime) computeMST(false);
                    }
                } catch (Exception ex) {}
            });
            del.addActionListener(a -> {
                edges.remove(edgeIdx);
                repaint();
                if (realtime) computeMST(false);
            });
            menu.add(edit);
            menu.add(del);
            menu.show(this, x, y);
        }
        public void computeMST(boolean showSummaryDialog) {
            if (nodes.size() < 2 || edges.size() < 1) {
                if(showSummaryDialog) JOptionPane.showMessageDialog(this, "Need at least 2 nodes and 1 edge.");
                return;
            }
            Map<Integer, Integer> idToIdx = new HashMap<>();
            int idx = 0;
            for (Node n : nodes) idToIdx.put(n.id, idx++);
            Graph g = new Graph(nodes.size());
            for (Edge e : edges) {
                if (!idToIdx.containsKey(e.src) || !idToIdx.containsKey(e.dest)) continue;
                g.addEdge(idToIdx.get(e.src), idToIdx.get(e.dest), e.weight);
            }
            List<Edge> result;
            String algo = (String) algoCombo.getSelectedItem();
            if ("Prim".equals(algo)) {
                PrimMST prim = new PrimMST();
                result = prim.computeMST(g);
            } else {
                KruskalMST kr = new KruskalMST();
                result = kr.computeMST(g);
            }
            Map<Integer, Integer> idxToId = new HashMap<>();
            for (Map.Entry<Integer, Integer> en : idToIdx.entrySet()) idxToId.put(en.getValue(), en.getKey());
            mstEdges.clear();
            for (Edge e : result) {
                int origSrc = idxToId.get(e.src);
                int origDest = idxToId.get(e.dest);
                int w = e.weight;
                for (Edge oe : edges) {
                    if ((oe.src == origSrc && oe.dest == origDest) || (oe.src == origDest && oe.dest == origSrc)) {
                        w = oe.weight;
                        break;
                    }
                }
                mstEdges.add(new Edge(origSrc, origDest, w));
            }
            repaint();
            if (showSummaryDialog) showCostSummary();
            
            // Update status bar
            double cost = computeCurrentMstCost();
            statusLabel.setText("MST Computed via " + algo + " | Total Cost: " + String.format("$%.2f", cost));
        }
        public void showCostSummary() {
            if (mstEdges == null || mstEdges.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No MST computed.");
                return;
            }
            double costPerMeter;
            try {
                costPerMeter = Double.parseDouble(costPerMeterField.getText());
            } catch (Exception ex) {
                costPerMeter = 1.0;
            }
            double totalCost = 0.0;
            int totalMeters = 0;
            for (Edge e : mstEdges) {
                totalMeters += e.weight;
                totalCost += e.weight * costPerMeter;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("PROJECT SUMMARY\n");
            sb.append("================\n\n");
            sb.append("Active Nodes:   ").append(nodes.size()).append("\n");
            sb.append("Cable Length:   ").append(totalMeters).append(" m\n");
            sb.append("Cost Rate:      $").append(String.format("%.2f", costPerMeter)).append(" /m\n");
            sb.append("TOTAL COST:     $").append(String.format("%.2f", totalCost)).append("\n");
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            ta.setBackground(new Color(240,240,240));
            ta.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
            ta.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            JOptionPane.showMessageDialog(this, ta, "Cost Analysis", JOptionPane.PLAIN_MESSAGE);
        }
        public void resetAll() {
            nodes.clear();
            edges.clear();
            mstEdges.clear();
            nextId = 0;
            typeCounters.clear();
            statusLabel.setText("Canvas Cleared");
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // 1. Draw Background
            if (backgroundImage != null) {
                g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                g2.setColor(new Color(0,0,0, 150)); // Dark overlay to make UI pop
                g2.fillRect(0,0,getWidth(),getHeight());
            } else {
                // Draw Grid
                g2.setColor(COLOR_GRID);
                for (int i=0; i<getWidth(); i+=40) g2.drawLine(i, 0, i, getHeight());
                for (int i=0; i<getHeight(); i+=40) g2.drawLine(0, i, getWidth(), i);
            }
            // 2. Draw Edges (Inactive)
            for (Edge e : edges) {
                Node a = getNodeById(e.src);
                Node b = getNodeById(e.dest);
                if (a == null || b == null) continue;
                
                // Check if this edge is in MST to skip drawing it twice (or draw under)
                boolean isMst = false;
                for(Edge me : mstEdges) if((me.src == e.src && me.dest == e.dest) || (me.src == e.dest && me.dest == e.src)) isMst = true;
                
                if(!isMst) {
                    Stroke old = g2.getStroke();
                    g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                    g2.setColor(new Color(80, 80, 80));
                    g2.drawLine(a.x, a.y, b.x, b.y);
                    g2.setStroke(old);
                    
                    // Draw Label (Dimmed)
                    drawEdgeLabel(g2, a, b, e.weight + "m", new Color(60,60,60), Color.GRAY);
                }
            }
            // 3. Draw MST Edges (Active)
            g2.setStroke(new BasicStroke(3));
            for (Edge e : mstEdges) {
                Node a = getNodeById(e.src);
                Node b = getNodeById(e.dest);
                if (a == null || b == null) continue;
                g2.setColor(new Color(50, 200, 150)); // Neon Green/Teal
                g2.drawLine(a.x, a.y, b.x, b.y);
                
                // Draw Label (Bright)
                drawEdgeLabel(g2, a, b, e.weight + "m", new Color(50, 200, 150), Color.BLACK);
            }
            // 4. Draw Nodes
            for (Node n : nodes) {
                drawModernNode(g2, n);
            }
        }
        
        private void drawEdgeLabel(Graphics2D g2, Node a, Node b, String text, Color bgColor, Color txtColor) {
            int mx = (a.x + b.x) / 2;
            int my = (a.y + b.y) / 2;
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(text) + 10;
            int h = fm.getHeight() + 4;
            
            g2.setColor(bgColor);
            g2.fillRoundRect(mx - w/2, my - h/2, w, h, 8, 8);
            g2.setColor(txtColor);
            g2.drawString(text, mx - w/2 + 5, my + h/2 - 5);
        }
        private void drawModernNode(Graphics2D g2, Node n) {
            Color c = getColorForType(n.deviceType);
            
            // Glow
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
            g2.fillOval(n.x - NODE_SIZE/2 - 4, n.y - NODE_SIZE/2 - 4, NODE_SIZE + 8, NODE_SIZE + 8);
            
            // Body
            g2.setColor(c);
            g2.fillOval(n.x - NODE_SIZE/2, n.y - NODE_SIZE/2, NODE_SIZE, NODE_SIZE);
            
            // Icon/Emoji
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            String emoji = getEmojiFor(n.deviceType);
            FontMetrics fm = g2.getFontMetrics();
            int ew = fm.stringWidth(emoji);
            int eh = fm.getAscent();
            g2.drawString(emoji, n.x - ew/2, n.y + eh/2 - 5);
            
            // --- ID BADGE (ADDED HERE) ---
            String idStr = String.valueOf(n.id);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            fm = g2.getFontMetrics();
            int idW = fm.stringWidth(idStr) + 8;
            int idH = 16;
            
            // Draw badge top-right of the node
            int badgeX = n.x + 10;
            int badgeY = n.y - 28;
            
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(badgeX, badgeY, idW, idH, 6, 6);
            g2.setColor(Color.BLACK);
            g2.drawString(idStr, badgeX + 4, badgeY + 12);
            // -----------------------------
            
            // Label Tag
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            fm = g2.getFontMetrics();
            int tw = fm.stringWidth(n.label) + 10;
            
            g2.setColor(new Color(20,20,20, 200));
            g2.fillRoundRect(n.x - tw/2, n.y + NODE_SIZE/2 + 5, tw, 20, 10, 10);
            
            g2.setColor(Color.WHITE);
            g2.drawString(n.label, n.x - tw/2 + 5, n.y + NODE_SIZE/2 + 19);
        }
        private Color getColorForType(String type) {
            switch (type.toLowerCase()) {
                case "pc": return new Color(46, 204, 113);
                case "router": return new Color(155, 89, 182);
                case "switch": return new Color(52, 152, 219);
                case "server": return new Color(231, 76, 60);
                default: return Color.GRAY;
            }
        }
        
        private String getEmojiFor(String type) {
            switch (type.toLowerCase()) {
                case "pc": return "\uD83D\uDCBB";
                case "router": return "\uD83D\uDCE1";
                case "switch": return "\uD83D\uDD00";
                case "server": return "\uD83D\uDDA5"; 
                default: return "\u25EF";
            }
        }
        private double computeCurrentMstCost() {
            double costPerMeter;
            try {
                costPerMeter = Double.parseDouble(costPerMeterField.getText());
            } catch (Exception ex) {
                costPerMeter = 1.0;
            }
            double t = 0.0;
            for (Edge e : mstEdges) {
                t += e.weight * costPerMeter;
            }
            return t;
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new FloorMapUI();
        });
    }
}
