package admin.ui;

import admin.AdminNetworkClient;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Raw console for sending commands and viewing responses
 */
public class ConsolePanel extends JPanel {
    
    private AdminNetworkClient client;
    
    private JTextArea consoleArea;
    private JTextField commandField;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    
    public ConsolePanel(AdminNetworkClient client) {
        this.client = client;
        initializeUI();
    }
    
    public void setClient(AdminNetworkClient client) {
        this.client = client;
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Console output
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        consoleArea.setBackground(new Color(30, 30, 30));
        consoleArea.setForeground(new Color(0, 255, 0));
        consoleArea.setCaretColor(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(consoleArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Command input
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        
        JLabel promptLabel = new JLabel("Command: ");
        promptLabel.setFont(new Font("Consolas", Font.BOLD, 13));
        
        commandField = new JTextField();
        commandField.setFont(new Font("Consolas", Font.PLAIN, 13));
        commandField.addActionListener(e -> sendCommand());
        
        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(e -> sendCommand());
        
        JButton helpBtn = new JButton("Help");
        helpBtn.addActionListener(e -> showHelp());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(sendBtn);
        buttonPanel.add(helpBtn);
        
        inputPanel.add(promptLabel, BorderLayout.WEST);
        inputPanel.add(commandField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(inputPanel, BorderLayout.SOUTH);
        
        // Initial message
        appendMessage("=== Admin Console ===");
        appendMessage("Type 'HELP' for available commands");
        appendMessage("");
    }
    
    public void appendMessage(String message) {
        String timestamp = timeFormat.format(new Date());
        consoleArea.append("[" + timestamp + "] " + message + "\n");
        
        // Auto-scroll to bottom
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }
    
    private void sendCommand() {
        String command = commandField.getText().trim();
        
        if (command.isEmpty()) return;
        
        appendMessage("> " + command);
        
        if (client != null && client.isAuthenticated()) {
            client.sendCommand(command);
        } else {
            appendMessage("ERROR: Not connected");
        }
        
        commandField.setText("");
    }
    
    private void showHelp() {
        String helpText = """
            Available Commands:
            
            PING                    - Test connection
            HELP                    - Show this help
            LIST_PLAYERS            - List all connected players
            LIST_ROOMS              - List all active rooms
            ROOM_INFO <roomId>      - Get details about a room
            KICK <username>         - Kick a player
            MOVE_PLAYER <user> <room> - Move player to room
            CLEAR_ROOM <roomId>     - Remove all players from room
            BROADCAST <message>     - Send message to all players
            """;
        
        JOptionPane.showMessageDialog(
            this,
            helpText,
            "Command Help",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}