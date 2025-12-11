package admin.ui;

import admin.AdminNetworkClient;
import javax.swing.*;
import java.awt.*;

/**
 * Main dashboard with tabs for different admin functions
 */
public class DashboardPanel extends JPanel {
    
    private AdminFrame parentFrame;
    private AdminNetworkClient client;
    
    private JTabbedPane tabbedPane;
    private PlayersPanel playersPanel;
    private RoomsPanel roomsPanel;
    private BroadcastPanel broadcastPanel;
    private ConsolePanel consolePanel;
    
    public DashboardPanel(AdminFrame parentFrame, AdminNetworkClient client) {
        this.parentFrame = parentFrame;
        this.client = client;
        
        initializeUI();
    }
    
    public void setClient(AdminNetworkClient client) {
        this.client = client;
        playersPanel.setClient(client);
        roomsPanel.setClient(client);
        broadcastPanel.setClient(client);
        consolePanel.setClient(client);
        
        // Set message handler
        client.setMessageHandler(this::handleServerMessage);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Header
        add(createHeader(), BorderLayout.NORTH);
        
        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));
        
        playersPanel = new PlayersPanel(client);
        roomsPanel = new RoomsPanel(client);
        broadcastPanel = new BroadcastPanel(client);
        consolePanel = new ConsolePanel(client);
        
        tabbedPane.addTab("👥 Players", playersPanel);
        tabbedPane.addTab("🏠 Rooms", roomsPanel);
        tabbedPane.addTab("📢 Broadcast", broadcastPanel);
        tabbedPane.addTab("💻 Console", consolePanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 204));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);
        
        // Right side buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        JButton refreshButton = createHeaderButton("↻ Refresh");
        refreshButton.addActionListener(e -> refreshCurrentTab());
        
        JButton logoutButton = createHeaderButton("Logout");
        logoutButton.addActionListener(e -> parentFrame.logout());
        
        rightPanel.add(refreshButton);
        rightPanel.add(logoutButton);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JButton createHeaderButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBackground(new Color(255, 255, 255, 50));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MESSAGE HANDLING
    // ═══════════════════════════════════════════════════════════
    
    private void handleServerMessage(String message) {
        // Route messages to appropriate panels
        consolePanel.appendMessage(message);
        
        if (message.startsWith("PLAYERS_") || message.startsWith("PLAYER ") || message.startsWith("COUNT ")) {
            playersPanel.handleMessage(message);
        } else if (message.startsWith("ROOMS_") || message.startsWith("ROOM ") || message.startsWith("ROOM_INFO")) {
            roomsPanel.handleMessage(message);
        } else if (message.startsWith("SUCCESS") || message.startsWith("ERROR")) {
            // Show notification
            showNotification(message);
        }
    }
    
    private void showNotification(String message) {
        boolean isError = message.startsWith("ERROR");
        Color bgColor = isError ? new Color(255, 200, 200) : new Color(200, 255, 200);
        
        // Could implement a proper toast notification here
        System.out.println("[NOTIFICATION] " + message);
    }
    
    // ═══════════════════════════════════════════════════════════
    // ACTIONS
    // ═══════════════════════════════════════════════════════════
    
    public void onShow() {
        // Called when dashboard becomes visible
        if (client != null && client.isAuthenticated()) {
            client.setMessageHandler(this::handleServerMessage);
            refreshCurrentTab();
        }
    }
    
    private void refreshCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        
        switch (selectedIndex) {
            case 0:
                playersPanel.refresh();
                break;
            case 1:
                roomsPanel.refresh();
                break;
        }
    }
}