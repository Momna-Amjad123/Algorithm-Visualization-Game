import java.util.ArrayList;
import java.util.HashMap;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

public class AlgorithmOdyssey extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContainer = new JPanel(cardLayout);
    public boolean world2Unlocked = false;
    public boolean world3Unlocked = false;

    // Custom modern font
    public static final Font MAIN_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font TITLE_FONT = new Font("Serif", Font.BOLD, 42);

    public AlgorithmOdyssey() {
        setTitle("Algorithm Odyssey: The Saga");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        refreshMap(); 
        mainContainer.add(new SortingWorld(this), "SORTING_WORLD");
        mainContainer.add(new SearchWorld(this), "SEARCH_WORLD");
        mainContainer.add(new DeliveryWorld(this), "DELIVERY_WORLD");
        mainContainer.add(new VictoryScreen(this), "VICTORY");

        add(mainContainer);
        showScreen("MENU");
    }

    public void refreshMap() {
        Component[] comps = mainContainer.getComponents();
        for (Component c : comps) if (c instanceof WorldMap) mainContainer.remove(c);
        mainContainer.add(new WorldMap(this), "MENU");
        mainContainer.revalidate();
        mainContainer.repaint();
    }

    public void playBeep() { Toolkit.getDefaultToolkit().beep(); }
    public void showScreen(String name) { cardLayout.show(mainContainer, name); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AlgorithmOdyssey().setVisible(true));
    }

    // Helper to style buttons consistently
    public static void styleButton(JButton btn, Color bg) {
        btn.setFont(MAIN_FONT);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 2),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }
}

/**
 * WORLD MAP
 */
class WorldMap extends JPanel {
    private AlgorithmOdyssey parent;
    private Image backgroundImage;

    public WorldMap(final AlgorithmOdyssey parent) {
        this.parent = parent;
        setLayout(null);
        try {
            backgroundImage = new ImageIcon("map_bg.jpg").getImage();
        } catch (Exception e) {}

        // --- Level Buttons ---
        add(createLevelBtn("1", 150, 520, true, "SORTING_WORLD"));
        add(createLevelBtn("2", 460, 360, parent.world2Unlocked, "SEARCH_WORLD"));
        add(createLevelBtn("3", 770, 210, parent.world3Unlocked, "DELIVERY_WORLD"));
    }

    private JButton createLevelBtn(String text, int x, int y, boolean unlocked, final String screen) {
        JButton btn = new JButton(unlocked ? text : "🔒");
        btn.setBounds(x, y, 95, 95);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 28));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (unlocked) {
            btn.setBackground(new Color(255, 204, 0));
            btn.setForeground(new Color(44, 62, 80));
            btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
            btn.addActionListener(e -> parent.showScreen(screen));
        } else {
            btn.setBackground(new Color(52, 73, 94, 200));
            btn.setForeground(new Color(189, 195, 199));
            btn.setBorder(BorderFactory.createLineBorder(new Color(127, 140, 141), 2));
        }
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        g2d.setColor(new Color(44, 62, 80, 220)); 
        g2d.fillRoundRect(200, 20, 600, 80, 40, 40);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(AlgorithmOdyssey.TITLE_FONT);
        String title = "ALGORITHM ODYSSEY";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, 75);

        g2d.setColor(new Color(44, 62, 80, 180));
        g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{12}, 0));
        g2d.drawLine(240, 565, 460, 405);
        g2d.drawLine(550, 405, 770, 255);

        int markerX = 195, markerY = 500;
        if (parent.world3Unlocked) { markerX = 815; markerY = 190; }
        else if (parent.world2Unlocked) { markerX = 505; markerY = 340; }
        
        g2d.setColor(new Color(231, 76, 60));
        g2d.fillPolygon(new int[]{markerX, markerX+20, markerX-20}, new int[]{markerY, markerY-35, markerY-35}, 3);
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillOval(markerX - 15, markerY + 5, 30, 10);
    }
}

/**
 * WORLD 1: THE SORTING DOJO
 */
class SortingWorld extends JPanel {
    private ArrayList<Integer> dataList = new ArrayList<>();
    private AlgorithmOdyssey parent;
    private int selectedIndex = -1;
    private int energy = 100;
    private int lives = 3; 
    private String status = "Manual Mode: Sort the team and click 'CHECK'!";
    private Timer autoTimer;
    private boolean isGameOver = false;

    public SortingWorld(final AlgorithmOdyssey parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 250));
        resetLevel();

        JPanel header = new JPanel(new GridLayout(3, 1));
        header.setBackground(new Color(44, 62, 80));
        
        JLabel title = new JLabel("WORLD 1: THE SORTING DOJO", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        JPanel lifePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(44, 62, 80));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(231, 76, 60)); 
                for(int i = 0; i < lives; i++) {
                    int hX = getWidth()/2 - 30 + (i * 25);
                    g.fillOval(hX, 5, 12, 12);
                    g.fillOval(hX + 8, 5, 12, 12);
                    g.fillPolygon(new int[]{hX, hX + 10, hX + 20}, new int[]{12, 22, 12}, 3);
                }
            }
        };
        lifePanel.setPreferredSize(new Dimension(1000, 30));

        JPanel energyBar = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(46, 204, 113)); 
                g.fillRect(0, 0, (int)(getWidth() * (energy / 100.0)), getHeight());
                g.setColor(Color.WHITE);
                g.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g.drawString(energy + "% LOGIC CAPACITY", getWidth()/2 - 50, 14);
            }
        };
        energyBar.setPreferredSize(new Dimension(1000, 20));

        header.add(title);
        header.add(lifePanel);
        header.add(energyBar);
        add(header, BorderLayout.NORTH);

        JPanel canvas = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCharacters((Graphics2D) g);
            }
        };
        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { if (!isGameOver) handleSelection(e.getX()); }
        });
        add(canvas, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footer.setBackground(new Color(44, 62, 80));
        
        JButton checkBtn = new JButton("CHECK SORT");
        AlgorithmOdyssey.styleButton(checkBtn, new Color(52, 152, 219));
        
        JButton autoBtn = new JButton("AUTO-SORT");
        AlgorithmOdyssey.styleButton(autoBtn, new Color(155, 89, 182));

        JButton resetBtn = new JButton("RESET (" + lives + ")");
        AlgorithmOdyssey.styleButton(resetBtn, new Color(231, 76, 60));

        JButton backBtn = new JButton("BACK TO MAP");
        AlgorithmOdyssey.styleButton(backBtn, new Color(149, 165, 166));

        checkBtn.addActionListener(e -> {
            if (!isGameOver && isSorted()) {
                JOptionPane.showMessageDialog(null, "EXCELLENT! World 2 Unlocked.");
                parent.world2Unlocked = true;
                parent.refreshMap();
                parent.showScreen("MENU");
            } else if (!isGameOver) {
                reduceEnergy(10);
                repaint();
            }
        });

        // UPDATED: Logic for disabling button and showing message when lives run out
        resetBtn.addActionListener(e -> {
        if (lives > 1) { 
            lives--; 
            resetBtn.setText("RESET (" + lives + ")"); 
            autoTimer.stop(); 
            resetLevel(); 
            repaint(); 
        } else {
            lives = 0;
            isGameOver = true; // Sets the flag to disable character interaction
            resetBtn.setText("OUT OF LIVES");
            resetBtn.setEnabled(false); // Disables the button UI
            JOptionPane.showMessageDialog(null, "The Sensei has no more energy left to guide you. GAME OVER!");
            parent.showScreen("MENU");
        }
    });

        autoBtn.addActionListener(e -> {
            if (!isGameOver && energy > 20) { reduceEnergy(20); autoTimer.start(); status = "Master Sensei is sorting..."; }
        });

        backBtn.addActionListener(e -> { autoTimer.stop(); parent.showScreen("MENU"); });

        footer.add(checkBtn); footer.add(autoBtn); footer.add(resetBtn); footer.add(backBtn);
        add(footer, BorderLayout.SOUTH);

        autoTimer = new Timer(300, e -> { if (!bubbleSortStep()) autoTimer.stop(); repaint(); });
    }

    private void resetLevel() {
        dataList.clear();
        int[] vals = {224, 149, 259, 345, 163, 196, 297, 198, 345, 287, 118, 299};
        for (int v : vals) dataList.add(v);
        energy = 100; isGameOver = false; status = "Manual Mode: Sort the team by height!";
    }

    private void reduceEnergy(int amount) {
        energy = Math.max(0, energy - amount);
        if (energy <= 0) { isGameOver = true; status = "MASTER IS EXHAUSTED!"; }
    }

    private void handleSelection(int x) {
        // Updated: Added lives > 0 check to strictly stop play when lost
        if (isGameOver || lives <= 0) return; 

        int index = (x - 80) / 65;
        if (index >= 0 && index < dataList.size()) {
            if (selectedIndex == -1) selectedIndex = index;
            else {
                if (Math.abs(selectedIndex - index) == 1) {
                    int temp = dataList.get(selectedIndex);
                    dataList.set(selectedIndex, dataList.get(index));
                    dataList.set(index, temp);
                    reduceEnergy(3); // Standard operation cost
                }
                selectedIndex = -1;
            }
            repaint();
        }
    }

    private boolean bubbleSortStep() {
        for (int i = 0; i < dataList.size() - 1; i++) {
            if (dataList.get(i) > dataList.get(i + 1)) {
                int temp = dataList.get(i);
                dataList.set(i, dataList.get(i + 1));
                dataList.set(i + 1, temp);
                return true;
            }
        }
        return false;
    }

    private boolean isSorted() {
        for (int i = 0; i < dataList.size() - 1; i++) {
            if (dataList.get(i) > dataList.get(i + 1)) return false;
        }
        return true;
    }

    private void drawCharacters(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(44, 62, 80));
        g.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        g.drawString(status, 80, 50);

        for (int i = 0; i < dataList.size(); i++) {
            int x = 80 + (i * 65), h = dataList.get(i), y = 450 - h;
            g.setColor(isGameOver ? Color.LIGHT_GRAY : new Color(52, 73, 94));
            g.fillOval(x + 10, y - 25, 25, 25); 
            if (isGameOver) g.setColor(new Color(189, 195, 199));
            else if (i == selectedIndex) g.setColor(new Color(231, 76, 60));
            else g.setColor(new Color(52, 152, 219));
            g.fillRoundRect(x, y, 45, h, 12, 12);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString(dataList.get(i).toString(), x + 10, 475);
        }
    }
}
/**
 * WORLD 2: SEARCH WOODS
 */
class SearchWorld extends JPanel {
    private AlgorithmOdyssey parent;
    private ArrayList<Integer> chestValues = new ArrayList<>();
    private int targetValue, energy = 100, lives = 3;
    private boolean found = false;
    private JPanel gridPanel;
    private JLabel targetLabel;
    private JButton resetBtn;

    public SearchWorld(final AlgorithmOdyssey parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(39, 174, 96));
        initLevel();

        // --- Header Section ---
        JPanel header = new JPanel(new GridLayout(4, 1));
        header.setBackground(new Color(44, 62, 80));
        
        JLabel title = new JLabel("WORLD 2: THE SEARCH WOODS", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        // Life Panel (The Hearts)
        JPanel lifePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(44, 62, 80));
                g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(new Color(231, 76, 60)); 
                for(int i=0; i<lives; i++) {
                    int hX = getWidth()/2 - 45 + (i * 30);
                    g.fillOval(hX, 5, 12, 12);
                    g.fillOval(hX + 8, 5, 12, 12);
                    g.fillPolygon(new int[]{hX, hX + 10, hX + 20}, new int[]{12, 22, 12}, 3);
                }
            }
        };
        lifePanel.setPreferredSize(new Dimension(1000, 30));

        targetLabel = new JLabel("TARGET KEY VALUE: " + targetValue, SwingConstants.CENTER);
        targetLabel.setForeground(new Color(241, 196, 15));
        targetLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JPanel energyBar = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.DARK_GRAY); g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(243, 156, 18)); // Orange
                g.fillRect(0, 0, (int)(getWidth() * (energy/100.0)), getHeight());
            }
        };
        energyBar.setPreferredSize(new Dimension(1000, 15));

        header.add(title);
        header.add(lifePanel);
        header.add(targetLabel);
        header.add(energyBar);
        add(header, BorderLayout.NORTH);

        // --- Grid Section ---
        gridPanel = new JPanel(new GridLayout(10, 10, 8, 8));
        gridPanel.setBackground(new Color(39, 174, 96));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        createGrid();
        add(gridPanel, BorderLayout.CENTER);

        // --- Footer Section ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        footer.setBackground(new Color(44, 62, 80));
        
        resetBtn = new JButton("RESET LIVES (" + lives + ")");
        AlgorithmOdyssey.styleButton(resetBtn, new Color(231, 76, 60));
        
        resetBtn.addActionListener(e -> {
            if(lives > 1) { 
                lives--; // Correctly subtract life
                resetBtn.setText("RESET LIVES (" + lives + ")");
                initLevel(); 
                targetLabel.setText("TARGET KEY VALUE: " + targetValue); 
                createGrid(); 
                repaint(); 
            } else {
                lives = 0;
                resetBtn.setText("OUT OF LIVES");
                resetBtn.setEnabled(false);
                JOptionPane.showMessageDialog(null, "GAME OVER! No lives left in the woods.");
                parent.showScreen("MENU");
            }
        });

        JButton backBtn = new JButton("BACK TO MAP");
        AlgorithmOdyssey.styleButton(backBtn, new Color(149, 165, 166));
        backBtn.addActionListener(e -> parent.showScreen("MENU"));
        
        footer.add(resetBtn); footer.add(backBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void initLevel() {
        chestValues.clear();
        for(int i=0; i<100; i++) chestValues.add((i + 1) * 10);
        targetValue = chestValues.get((int)(Math.random() * 100));
        energy = 100;
        found = false;
    }

    private void createGrid() {
        gridPanel.removeAll();
        for(int i=0; i<100; i++) {
            final int index = i;
            final JButton btn = new JButton("?");
            
            // Styled Chests
            btn.setBackground(new Color(139, 69, 19));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createLineBorder(new Color(101, 67, 33), 2));

            btn.addActionListener(e -> {
                if(found || energy <= 0 || lives <= 0) return;
                int val = chestValues.get(index);
                btn.setText(String.valueOf(val));
                btn.setEnabled(false);
                
                if(val == targetValue) {
                    found = true;
                    btn.setBackground(new Color(46, 204, 113)); // Success Green
                    parent.world3Unlocked = true;
                    parent.refreshMap();
                    JOptionPane.showMessageDialog(null, "Key Found! World 3 Unlocked.");
                    parent.showScreen("MENU");
                } else {
                    energy -= 15;
                    btn.setBackground(new Color(60, 40, 20)); // Empty
                    if(energy <= 0) {
                        JOptionPane.showMessageDialog(null, "Logic Energy Depleted! Use a Reset to try again.");
                    }
                }
                repaint();
            });
            gridPanel.add(btn);
        }
        gridPanel.revalidate();
    }
}

/**
 * WORLD 3: DELIVERY DASH
 */
class DeliveryWorld extends JPanel {
    private AlgorithmOdyssey parent;
    private HashMap<String, ArrayList<String>> adj = new HashMap<>();
    private HashMap<String, Integer> weights = new HashMap<>();
    private String currentPos = "A";
    private int totalTime = 0;
    private final int[][] coords = {{50, 300}, {250, 100}, {250, 500}, {500, 300}, {450, 50}, {450, 550}, {750, 150}, {750, 450}, {900, 300}};
    private final String[] labels = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};

    public DeliveryWorld(final AlgorithmOdyssey parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(236, 240, 241));
        setupTangledGraph();

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(44, 62, 80));
        JLabel title = new JLabel("WORLD 3: THE TANGLED DELIVERY", SwingConstants.CENTER);
        title.setForeground(Color.WHITE); title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        final JLabel timeLabel = new JLabel("Total Travel Time: 0 mins", SwingConstants.CENTER);
        timeLabel.setForeground(new Color(241, 196, 15)); timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title); header.add(timeLabel);
        add(header, BorderLayout.NORTH);

        JPanel canvas = new JPanel() {
            protected void paintComponent(Graphics g) { super.paintComponent(g); drawTangledRoads((Graphics2D) g); }
        };
        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { handleMove(e.getX(), e.getY(), timeLabel); }
        });
        add(canvas, BorderLayout.CENTER);
        
        JPanel footer = new JPanel();
        footer.setBackground(new Color(44, 62, 80));
        JButton reset = new JButton("RESET GPS");
        AlgorithmOdyssey.styleButton(reset, new Color(231, 76, 60));
        reset.addActionListener(e -> { currentPos = "A"; totalTime = 0; timeLabel.setText("Total Travel Time: 0 mins"); repaint(); });
        footer.add(reset);
        add(footer, BorderLayout.SOUTH);
    }

    private void setupTangledGraph() {
        addPath("A", "B", 6); addPath("A", "C", 10);
        addPath("B", "D", 5); addPath("B", "E", 8);
        addPath("C", "D", 4); addPath("C", "F", 12);
        addPath("D", "G", 6); addPath("D", "H", 9);
        addPath("E", "G", 15); addPath("F", "H", 3);
        addPath("G", "I", 5); addPath("H", "I", 8);
    }

    private void addPath(String u, String v, int w) {
        if(!adj.containsKey(u)) adj.put(u, new ArrayList<>());
        adj.get(u).add(v); weights.put(u + "-" + v, w);
    }

    private void drawTangledRoads(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (String startNode : adj.keySet()) {
            for (String endNode : adj.get(startNode)) {
                int i1 = getIdx(startNode), i2 = getIdx(endNode);
                drawRoadLine(g, coords[i1][0]+25, coords[i1][1]+25, coords[i2][0]+25, coords[i2][1]+25, weights.get(startNode+"-"+endNode));
            }
        }
        for (int i = 0; i < labels.length; i++) {
            g.setColor(new Color(52, 73, 94));
            g.fillRoundRect(coords[i][0], coords[i][1], 50, 50, 15, 15);
            g.setColor(Color.WHITE); g.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g.drawString(labels[i], coords[i][0]+20, coords[i][1]+32);
            if(labels[i].equals(currentPos)) {
                g.setColor(new Color(241, 196, 15)); g.fillRoundRect(coords[i][0]+5, coords[i][1]+10, 40, 25, 8, 8);
            }
        }
    }

    private void drawRoadLine(Graphics2D g, int x1, int y1, int x2, int y2, int w) {
        g.setStroke(new BasicStroke(14)); g.setColor(new Color(127, 140, 141)); g.drawLine(x1, y1, x2, y2);
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{10}, 0));
        g.setColor(Color.WHITE); g.drawLine(x1, y1, x2, y2);
        g.setColor(new Color(44, 62, 80)); g.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g.drawString(String.valueOf(w), (x1 + x2) / 2, (y1 + y2) / 2);
    }

    private int getIdx(String l) {
        for(int i=0; i<labels.length; i++) if(labels[i].equals(l)) return i;
        return 0;
    }

    private void handleMove(int x, int y, JLabel label) {
        for(int i = 0; i < labels.length; i++) {
            if(Math.abs(x - (coords[i][0] + 25)) < 30 && Math.abs(y - (coords[i][1] + 25)) < 30) {
                String next = labels[i];
                if(adj.containsKey(currentPos) && adj.get(currentPos).contains(next)) {
                    totalTime += weights.get(currentPos + "-" + next); currentPos = next;
                    label.setText("Total Travel Time: " + totalTime + " mins");
                    if(currentPos.equals("I")) {
                        if (totalTime == 22) { JOptionPane.showMessageDialog(this, "PERFECT PATH!"); parent.showScreen("VICTORY"); }
                        else { JOptionPane.showMessageDialog(this, "Delivered, but not optimal! Try 22 mins."); parent.showScreen("MENU"); }
                        currentPos = "A"; totalTime = 0;
                    }
                }
                repaint(); return;
            }
        }
    }
}

/**
 * VICTORY SCREEN
 */
class VictoryScreen extends JPanel {
    public VictoryScreen(final AlgorithmOdyssey parent) {
        setBackground(new Color(44, 62, 80));
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setFont(new Font("Segoe UI", Font.BOLD, 64)); g2.setColor(Color.WHITE);
                String msg = "VICTORY!";
                g2.drawString(msg, cx - (g2.getFontMetrics().stringWidth(msg)/2), cy - 150);
                g2.setColor(new Color(255, 215, 0)); // Gold
                g2.fillOval(cx - 60, cy - 60, 120, 100); 
                g2.fillRect(cx - 10, cy + 40, 20, 60); g2.fillRect(cx - 50, cy + 100, 100, 20);
                g2.setStroke(new BasicStroke(10));
                g2.drawArc(cx - 85, cy - 40, 40, 50, 90, 180); g2.drawArc(cx + 45, cy - 40, 40, 50, 270, 180);
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 22)); g2.setColor(new Color(46, 204, 113));
                String sub = "You have mastered the Algorithms!";
                g2.drawString(sub, cx - (g2.getFontMetrics().stringWidth(sub)/2), cy + 160);
            }
        };
        centerPanel.setOpaque(false);
        add(centerPanel, BorderLayout.CENTER);

        JButton replayBtn = new JButton("RETURN TO MAP");
        AlgorithmOdyssey.styleButton(replayBtn, new Color(52, 152, 219));
        replayBtn.addActionListener(e -> parent.showScreen("MENU"));
        
        JPanel btnPanel = new JPanel(){{setOpaque(false);}};
        btnPanel.add(replayBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }
}