package admin.ui;

import admin.AdminNetworkClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main window for the Admin Client
 */
public class AdminFrame extends JFrame {
    
    private AdminNetworkClient client;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Panels
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    
    // Status bar
    private JLabel statusLabel;
    private JLabel connectionIndicator;
    
    public AdminFrame() {
        setTitle("Futura Admin Console");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        
        // Initialize client
        client = new AdminNetworkClient("localhost", 5001);
        
        // Setup UI
        initializeUI();
        
        // Handle window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null) {
                    client.disconnect();
                }
            }
        });
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Card layout for switching between login and dashboard
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Create panels
        loginPanel = new LoginPanel(this, client);
        dashboardPanel = new DashboardPanel(this, client);
        
        mainPanel.add(loginPanel, "login");
        mainPanel.add(dashboardPanel, "dashboard");
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Status bar
        add(createStatusBar(), BorderLayout.SOUTH);
        
        // Show login first
        showLogin();
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        statusBar.setBackground(new Color(240, 240, 240));
        
        statusLabel = new JLabel("Not connected");
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        connectionIndicator = new JLabel("●");
        connectionIndicator.setForeground(Color.RED);
        connectionIndicator.setFont(new Font("Arial", Font.BOLD, 16));
        statusBar.add(connectionIndicator, BorderLayout.EAST);
        
        return statusBar;
    }
    
    // ═══════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════
    
    public void showLogin() {
        cardLayout.show(mainPanel, "login");
        updateStatus("Not connected", false);
    }
    
    public void showDashboard() {
        cardLayout.show(mainPanel, "dashboard");
        updateStatus("Connected to Admin Server", true);
        dashboardPanel.onShow();  // Refresh data
    }
    
    public void updateStatus(String message, boolean connected) {
        statusLabel.setText(message);
        connectionIndicator.setForeground(connected ? new Color(0, 180, 0) : Color.RED);
    }
    
    public void logout() {
        client.disconnect();
        showLogin();
    }
}
