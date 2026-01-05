import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * TrafficControl4PageApp.java
 * Single-file Java Swing application with 4 pages:
 * 1) Login
 * 2) Dashboard
 * 3) Traffic Simulation
 * 4) Reports
 * Run:
 *   javac TrafficControl.java
 *   java TrafficControl
 * user: admin
 * pass: 1234
 * 
 */
public class TrafficControl{

    // Keep simple runtime stats for demo (shared)
    static class Stats {
        int simulationStarts = 0;
        int totalCongestionAlerts = 0;
        int routeOptimizations = 0;
        long totalSimulationSeconds = 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame(new Stats()));
    }

    // -------------------- Login Frame --------------------
    static class LoginFrame extends JFrame {
        private JTextField userField;
        private JPasswordField passField;
        private Stats stats;

        public LoginFrame(Stats stats) {
            this.stats = stats;
            setTitle("Login - Traffic Control System");
            setSize(380, 220);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout(8, 8));
            add(createForm(), BorderLayout.CENTER);
            setVisible(true);
        }

        private JPanel createForm() {
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            JPanel inputs = new JPanel(new GridLayout(2, 2, 8, 8));
            inputs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            inputs.add(new JLabel("Username:"));
            userField = new JTextField();
            inputs.add(userField);

            inputs.add(new JLabel("Password:"));
            passField = new JPasswordField();
            inputs.add(passField);

            panel.add(inputs, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            JButton loginBtn = new JButton("Login");
            JButton exitBtn = new JButton("Exit");

            loginBtn.addActionListener(e -> doLogin());
            exitBtn.addActionListener(e -> System.exit(0));

            buttons.add(loginBtn);
            buttons.add(exitBtn);
            panel.add(buttons, BorderLayout.SOUTH);
            return panel;
        }

        private void doLogin() {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword()).trim();

            // Demo credentials (change as you like)
            if ("admin".equals(user) && "1234".equals(pass)) {
                // Open Dashboard and close login
                new DashboardFrame(stats);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
    "Invalid username or password!",
    "Login Failed", JOptionPane.ERROR_MESSAGE);

            }
        }
    }

    // -------------------- Dashboard Frame --------------------
    static class DashboardFrame extends JFrame {
        private Stats stats;

        public DashboardFrame(Stats stats) {
            this.stats = stats;
            setTitle("Dashboard - Traffic Control System");
            setSize(500, 300);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout(10, 10));

            JLabel title = new JLabel("Traffic Control Management - Dashboard", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 16));
            add(title, BorderLayout.NORTH);

            add(createCenterPanel(), BorderLayout.CENTER);
            add(createBottomPanel(), BorderLayout.SOUTH);

            setVisible(true);
        }

        private JPanel createCenterPanel() {
            JPanel p = new JPanel(new GridLayout(1, 2, 12, 12));
            p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JPanel left = new JPanel(new BorderLayout(6,6));
            JPanel right = new JPanel(new BorderLayout(6,6));

            JLabel info = new JLabel("<html><b>System Info</b><br/>"
                    + "Active user: admin<br/>"
                    + "Start time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "</html>");
            info.setBorder(BorderFactory.createTitledBorder("Info"));
            left.add(info, BorderLayout.CENTER);

            JPanel nav = new JPanel(new GridLayout(4,1,8,8));
            JButton simBtn = new JButton("Open Simulation");
            JButton reportsBtn = new JButton("Open Reports");
            JButton logoutBtn = new JButton("Logout");
            JButton exitBtn = new JButton("Exit");

            simBtn.addActionListener(e -> new SimulationFrame(stats));
            reportsBtn.addActionListener(e -> new ReportsFrame(stats));
            logoutBtn.addActionListener(e -> {
                new LoginFrame(stats);
                dispose();
            });
            exitBtn.addActionListener(e -> System.exit(0));

            nav.add(simBtn);
            nav.add(reportsBtn);
            nav.add(logoutBtn);
            nav.add(exitBtn);

            right.setBorder(BorderFactory.createTitledBorder("Actions"));
            right.add(nav, BorderLayout.CENTER);

            p.add(left);
            p.add(right);
            return p;
        }

        private JPanel createBottomPanel() {
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JLabel hint = new JLabel("Tip: Use Simulation to start/stop traffic simulation & generate reports.");
            bottom.add(hint);
            return bottom;
        }
    }

    // -------------------- Simulation Frame --------------------
    static class SimulationFrame extends JFrame implements ActionListener {
        private JPanel signalPanel;
        private JLabel redLight, yellowLight, greenLight;
        private JLabel statusLabel, routeLabel, timerLabel;
        private JButton startBtn, stopBtn, routeBtn, backBtn;
        private Timer timer;
        private int signalState = 0; // 0=Red,1=Yellow,2=Green
        private Random random = new Random();
        private Stats stats;
        private long simulationStartMillis = 0;

        public SimulationFrame(Stats stats) {
            this.stats = stats;
            setTitle("Traffic Simulation");
            setSize(700, 520);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout(10, 10));
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            add(createTopPanel(), BorderLayout.NORTH);
            add(createCenterPanel(), BorderLayout.CENTER);
            add(createBottomPanel(), BorderLayout.SOUTH);

            // timer every 1 second: update lights every N seconds (simulate sub-second control)
            timer = new Timer(1000, e -> {
                // change signal every few seconds depending on state for demo
                changeSignal();
                simulateCongestion();
                updateTimerLabel();
            });

            setVisible(true);
        }

        private JPanel createTopPanel() {
            JPanel p = new JPanel(new BorderLayout());
            routeLabel = new JLabel("Route Suggestion: N/A", SwingConstants.CENTER);
            routeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            p.add(routeLabel, BorderLayout.CENTER);

            timerLabel = new JLabel("Simulation Time: 0s", SwingConstants.RIGHT);
            timerLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 12));
            p.add(timerLabel, BorderLayout.EAST);

            return p;
        }

        private JPanel createCenterPanel() {
            JPanel center = new JPanel(new BorderLayout(10, 10));
            center.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Signal visual
            signalPanel = new JPanel(new GridLayout(3, 1, 8, 8));
            signalPanel.setBackground(Color.WHITE);
            signalPanel.setBorder(BorderFactory.createTitledBorder("Traffic Signal"));

            redLight = createLight(Color.RED.darker());
            yellowLight = createLight(Color.YELLOW.darker());
            greenLight = createLight(Color.GREEN.darker());

            signalPanel.add(redLight);
            signalPanel.add(yellowLight);
            signalPanel.add(greenLight);

            center.add(signalPanel, BorderLayout.CENTER);

            // Right side: status
            JPanel right = new JPanel(new GridLayout(3, 1, 8, 8));
            statusLabel = new JLabel("Status: Idle", SwingConstants.CENTER);
            statusLabel.setBorder(BorderFactory.createTitledBorder("Congestion Status"));

            JPanel statsPanel = new JPanel(new GridLayout(3, 1));
            statsPanel.setBorder(BorderFactory.createTitledBorder("Runtime Stats"));
            JLabel simStarts = new JLabel("Sim Starts: " + stats.simulationStarts);
            JLabel congestionCount = new JLabel("Congestion Alerts: " + stats.totalCongestionAlerts);
            JLabel routeCount = new JLabel("Route Opt Count: " + stats.routeOptimizations);
            // We'll refresh these on events
            statsPanel.add(simStarts);
            statsPanel.add(congestionCount);
            statsPanel.add(routeCount);

            right.add(statusLabel);
            right.add(statsPanel);

            center.add(right, BorderLayout.EAST);

            return center;
        }

        private JPanel createBottomPanel() {
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
            startBtn = new JButton("Start");
            stopBtn = new JButton("Stop");
            routeBtn = new JButton("Optimize Route");
            backBtn = new JButton("Back to Dashboard");

            startBtn.addActionListener(this);
            stopBtn.addActionListener(this);
            routeBtn.addActionListener(this);
            backBtn.addActionListener(this);

            bottom.add(startBtn);
            bottom.add(stopBtn);
            bottom.add(routeBtn);
            bottom.add(backBtn);

            return bottom;
        }

        private JLabel createLight(Color color) {
            JLabel l = new JLabel();
            l.setOpaque(true);
            l.setBackground(color);
            l.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
            l.setPreferredSize(new Dimension(130, 80));
            return l;
        }

        private int secCounter = 0;
        private final int RED_SECONDS = 5;    // demo durations
        private final int YELLOW_SECONDS = 2;
        private final int GREEN_SECONDS = 4;

        private int secondsInState = 0;

        private void changeSignal() {
            secondsInState++;
            switch (signalState) {
                case 0: // RED
                    if (secondsInState >= RED_SECONDS) {
                        toYellow();
                    }
                    break;
                case 1: // YELLOW
                    if (secondsInState >= YELLOW_SECONDS) {
                        toGreen();
                    }
                    break;
                case 2: // GREEN
                    if (secondsInState >= GREEN_SECONDS) {
                        toRed();
                    }
                    break;
            }
        }

        private void setLights(Color red, Color yellow, Color green) {
            redLight.setBackground(red);
            yellowLight.setBackground(yellow);
            greenLight.setBackground(green);
        }

        private void toRed() {
            setLights(Color.RED, Color.YELLOW.darker(), Color.GREEN.darker());
            signalState = 0;
            secondsInState = 0;
        }

        private void toYellow() {
            setLights(Color.RED.darker(), Color.YELLOW, Color.GREEN.darker());
            signalState = 1;
            secondsInState = 0;
        }

        private void toGreen() {
            setLights(Color.RED.darker(), Color.YELLOW.darker(), Color.GREEN);
            signalState = 2;
            secondsInState = 0;
        }

        private void simulateCongestion() {
            // random traffic measure
            int level = random.nextInt(100);
            if (level < 30) {
                statusLabel.setText("Status: Low Traffic");
                statusLabel.setForeground(Color.GREEN.darker());
            } else if (level < 70) {
                statusLabel.setText("Status: Moderate Traffic");
                statusLabel.setForeground(Color.ORANGE.darker());
            } else {
                statusLabel.setText("Status: HEAVY CONGESTION!");
                statusLabel.setForeground(Color.RED.darker());
                stats.totalCongestionAlerts++;
            }
        }

        private void updateTimerLabel() {
            if (simulationStartMillis != 0) {
                long elapsed = (System.currentTimeMillis() - simulationStartMillis) / 1000;
                timerLabel.setText("Simulation Time: " + elapsed + "s");
                stats.totalSimulationSeconds = elapsed;
            }
        }

        private void optimizeRoute() {
            String[] routes = {"Route A - 12 min", "Route B - 9 min", "Route C - 6 min"};
            int best = random.nextInt(routes.length);
            routeLabel.setText("Best Route: " + routes[best]);
            stats.routeOptimizations++;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == startBtn) {
                if (!timer.isRunning()) {
                    timer.start();
                    stats.simulationStarts++;
                    simulationStartMillis = System.currentTimeMillis();
                    secondsInState = 0;
                    toRed(); // start at red for clarity
                }
            } else if (src == stopBtn) {
                if (timer.isRunning()) timer.stop();
                updateTimerLabel();
            } else if (src == routeBtn) {
                optimizeRoute();
            } else if (src == backBtn) {
                // close simulation window
                dispose();
            }
        }
    }

    // -------------------- Reports Frame --------------------
    static class ReportsFrame extends JFrame {
        private Stats stats;
        private JTextArea reportArea;
        private JButton saveBtn, refreshBtn, closeBtn;

        public ReportsFrame(Stats stats) {
            this.stats = stats;
            setTitle("Reports - Traffic Control System");
            setSize(600, 420);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout(8,8));
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            reportArea = new JTextArea();
            reportArea.setEditable(false);
            reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            add(new JScrollPane(reportArea), BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
            saveBtn = new JButton("Save Report");
            refreshBtn = new JButton("Refresh");
            closeBtn = new JButton("Close");

            saveBtn.addActionListener(e -> saveReport());
            refreshBtn.addActionListener(e -> refreshReport());
            closeBtn.addActionListener(e -> dispose());

            bottom.add(saveBtn);
            bottom.add(refreshBtn);
            bottom.add(closeBtn);

            add(bottom, BorderLayout.SOUTH);

            refreshReport();
            setVisible(true);
        }

        private String generateReportText() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Traffic Control System - Report ===\n");
            sb.append("Generated at: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
            sb.append("Simulation starts: ").append(stats.simulationStarts).append("\n");
            sb.append("Total congestion alerts: ").append(stats.totalCongestionAlerts).append("\n");
            sb.append("Route optimizations: ").append(stats.routeOptimizations).append("\n");
            sb.append("Total simulation time (s): ").append(stats.totalSimulationSeconds).append("\n\n");

            sb.append("Notes:\n");
            sb.append("- Congestion alerts are simulated randomly during runs.\n");
            sb.append("- Route optimization suggestions are sample/demo values.\n");
            sb.append("\nEnd of report.\n");
            return sb.toString();
        }

        private void refreshReport() {
            reportArea.setText(generateReportText());
        }

        private void saveReport() {
            String text = generateReportText();
            String filename = "TrafficReport_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
            try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
                pw.print(text);
                JOptionPane.showMessageDialog(this, "Report saved as: " + filename, "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to save report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
