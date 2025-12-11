package admin.ui;

import admin.AdminNetworkClient;
import javax.swing.*;
import java.awt.*;

/**
 * Panel for broadcasting messages to all players
 */
public class BroadcastPanel extends JPanel {
    
    private AdminNetworkClient client;
    
    private JTextArea messageArea;
    private JComboBox<String> templateCombo;
    private JList<String> historyList;
    private DefaultListModel<String> historyModel;
    
    public BroadcastPanel(AdminNetworkClient client) {
        this.client = client;
        initializeUI();
    }
    
    public void setClient(AdminNetworkClient client) {
        this.client = client;
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Left panel - compose message
        JPanel composePanel = new JPanel(new BorderLayout(10, 10));
        composePanel.setBorder(BorderFactory.createTitledBorder("Compose Message"));
        
        // Templates
        JPanel templatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        templatePanel.add(new JLabel("Template:"));
        
        templateCombo = new JComboBox<>(new String[]{
            "-- Select Template --",
            "Server will restart in 5 minutes",
            "Server maintenance starting soon",
            "Welcome to Futura!",
            "Please follow the rules",
            "Event starting in the lobby!"
        });
        templateCombo.addActionListener(e -> {
            int index = templateCombo.getSelectedIndex();
            if (index > 0) {
                messageArea.setText((String) templateCombo.getSelectedItem());
            }
        });
        templatePanel.add(templateCombo);
        
        composePanel.add(templatePanel, BorderLayout.NORTH);
        
        // Message input
        messageArea = new JTextArea(5, 30);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 14));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        composePanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        
        // Send button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> messageArea.setText(""));
        
        JButton sendBtn = new JButton("📢 Send Broadcast");
        sendBtn.setFont(new Font("Arial", Font.BOLD, 14));
        sendBtn.setBackground(new Color(0, 120, 215));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.addActionListener(e -> sendBroadcast());
        
        buttonPanel.add(clearBtn);
        buttonPanel.add(sendBtn);
        
        composePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Right panel - history
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setBorder(BorderFactory.createTitledBorder("Broadcast History"));
        historyPanel.setPreferredSize(new Dimension(250, 0));
        
        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Double click to reuse
        historyList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selected = historyList.getSelectedValue();
                    if (selected != null) {
                        messageArea.setText(selected);
                    }
                }
            }
        });
        
        historyPanel.add(new JScrollPane(historyList), BorderLayout.CENTER);
        
        JLabel hintLabel = new JLabel("Double-click to reuse");
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        hintLabel.setForeground(Color.GRAY);
        historyPanel.add(hintLabel, BorderLayout.SOUTH);
        
        // Main split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(composePanel);
        splitPane.setRightComponent(historyPanel);
        splitPane.setResizeWeight(0.7);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void sendBroadcast() {
        String message = messageArea.getText().trim();
        
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a message");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Send this message to ALL players?\n\n\"" + message + "\"",
            "Confirm Broadcast",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            client.broadcast(message);
            
            // Add to history
            historyModel.insertElementAt(message, 0);
            if (historyModel.size() > 20) {
                historyModel.remove(historyModel.size() - 1);
            }
            
            // Clear input
            messageArea.setText("");
            templateCombo.setSelectedIndex(0);
            
            JOptionPane.showMessageDialog(
                this,
                "Broadcast sent successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}