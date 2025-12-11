package admin.ui;

import admin.AdminNetworkClient;
import javax.swing.*;
import java.awt.*;

/**
 * Login/Authentication panel
 */
public class LoginPanel extends JPanel {
    
    private AdminFrame parentFrame;
    private AdminNetworkClient client;
    
    private JTextField hostField;
    private JTextField portField;
    private JPasswordField secretField;
    private JButton connectButton;
    private JLabel messageLabel;
    
    public LoginPanel(AdminFrame parentFrame, AdminNetworkClient client) {
        this.parentFrame = parentFrame;
        this.client = client;
        
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(45, 45, 55));
        
        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(60, 60, 70));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90), 1),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("Futura Admin Console");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Server Administration");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(150, 150, 160));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Host
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createLabel("Host:"), gbc);
        
        gbc.gridx = 1;
        hostField = createTextField("localhost");
        formPanel.add(hostField, gbc);
        
        // Port
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createLabel("Port:"), gbc);
        
        gbc.gridx = 1;
        portField = createTextField("5001");
        formPanel.add(portField, gbc);
        
        // Secret Key
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createLabel("Secret Key:"), gbc);
        
        gbc.gridx = 1;
        secretField = new JPasswordField(20);
        secretField.setFont(new Font("Arial", Font.PLAIN, 14));
        secretField.setText("your-secret-key-123");  // Default for testing
        formPanel.add(secretField, gbc);
        
        // Connect button
        connectButton = new JButton("Connect");
        connectButton.setFont(new Font("Arial", Font.BOLD, 14));
        connectButton.setBackground(new Color(0, 120, 215));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        connectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.addActionListener(e -> attemptConnection());
        
        // Message label
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(255, 100, 100));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add components
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalStrut(30));
        centerPanel.add(formPanel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(connectButton);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(messageLabel);
        
        add(centerPanel);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(200, 200, 210));
        return label;
    }
    
    private JTextField createTextField(String defaultValue) {
        JTextField field = new JTextField(defaultValue, 20);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        return field;
    }
    
    private void attemptConnection() {
        String host = hostField.getText().trim();
        int port;
        
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Invalid port number");
            return;
        }
        
        String secret = new String(secretField.getPassword());
        
        if (secret.isEmpty()) {
            showError("Please enter secret key");
            return;
        }
        
        // Disable button during connection
        connectButton.setEnabled(false);
        connectButton.setText("Connecting...");
        messageLabel.setText(" ");
        
        // Connect in background thread
        new Thread(() -> {
            // Create new client with entered host/port
            AdminNetworkClient newClient = new AdminNetworkClient(host, port);
            
            boolean connected = newClient.connect();
            
            if (!connected) {
                SwingUtilities.invokeLater(() -> {
                    showError("Could not connect to server");
                    resetButton();
                });
                return;
            }
            
            boolean authenticated = newClient.authenticate(secret);
            
            SwingUtilities.invokeLater(() -> {
                if (authenticated) {
                    // Replace client in parent frame
                    updateClient(newClient);
                    parentFrame.showDashboard();
                } else {
                    newClient.disconnect();
                    showError("Authentication failed - invalid secret key");
                }
                resetButton();
            });
            
        }).start();
    }
    
    private void updateClient(AdminNetworkClient newClient) {
        // Access the dashboard and update its client reference
        // This is a bit hacky - in production you'd use dependency injection
        try {
            java.lang.reflect.Field clientField = parentFrame.getClass().getDeclaredField("client");
            clientField.setAccessible(true);
            clientField.set(parentFrame, newClient);
            
            // Also update dashboard's client
            java.lang.reflect.Field dashboardField = parentFrame.getClass().getDeclaredField("dashboardPanel");
            dashboardField.setAccessible(true);
            DashboardPanel dashboard = (DashboardPanel) dashboardField.get(parentFrame);
            dashboard.setClient(newClient);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setForeground(new Color(255, 100, 100));
    }
    
    private void resetButton() {
        connectButton.setEnabled(true);
        connectButton.setText("Connect");
    }
}