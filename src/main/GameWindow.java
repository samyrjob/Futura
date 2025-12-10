package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * GameWindow - Main game window container
 * 
 * Responsibilities:
 * - Create and setup JFrame
 * - Initialize GamePanel
 * - Create chat UI (input field + buttons)
 * - Handle window events (close, etc.)
 */
public class GameWindow {
    
    // Window constants
    private static final String WINDOW_TITLE = "Futura - Virtual World";
    private static final Color CHAT_PANEL_BG = new Color(40, 40, 40);
    private static final int CHAT_PANEL_HEIGHT = 60;
    
    // Button colors
    private static final Color INVENTORY_BG = new Color(255, 193, 7);  // Gold
    private static final Color INVENTORY_BG_HOVER = new Color(255, 213, 79);
    private static final Color ROOMS_BG = new Color(76, 175, 80);  // ✨ NEW - Green
    private static final Color ROOMS_BG_HOVER = new Color(102, 187, 106);  // ✨ NEW
    private static final Color SEND_BG = Color.WHITE;
    private static final Color SEND_BG_HOVER = new Color(230, 230, 230);

    private static final Color FRIENDS_BG = new Color(30, 30, 30);  // Dark/Black
    private static final Color FRIENDS_BG_HOVER = new Color(60, 60, 60);  // Lighter on hover
    
    // Components
    private final JFrame window;
    private final GamePanel gamePanel;
    private JTextField messageField;
    private JButton sendButton;
    private JButton inventoryButton;
    private JButton roomsButton;  // ✨ NEW
    private JButton friendsButton;
    
    // Placeholder text
    private static final String PLACEHOLDER = "type here to write a message";
    
    public GameWindow(String username, String gender) {
        this.window = new JFrame(WINDOW_TITLE);
        this.gamePanel = new GamePanel(username, gender);
        
        initializeWindow();
        initializeChatUI();
        initializeEventHandlers();
        
        // Show and start
        window.setVisible(true);
        gamePanel.startGameThread();
        gamePanel.setupGame();
    }
    
    // ═══════════════════════════════════════════════════════════
    // WINDOW INITIALIZATION
    // ═══════════════════════════════════════════════════════════
    
    private void initializeWindow() {
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.setResizable(false);
        window.setLayout(new BorderLayout());
        
        // Add game panel to center
        window.add(gamePanel, BorderLayout.CENTER);
        
        // Set window size (fit to screen if needed)
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        window.setSize(
            Math.min(gamePanel.screenWidth, screenSize.width),
            Math.min(gamePanel.screenHeight + CHAT_PANEL_HEIGHT, screenSize.height)
        );
        
        window.setLocationRelativeTo(null);
    }
    
    // ═══════════════════════════════════════════════════════════
    // CHAT UI INITIALIZATION
    // ═══════════════════════════════════════════════════════════
    
    private void initializeChatUI() {
        JPanel chatInputPanel = createChatPanel();
        JPanel innerPanel = createInnerPanel();
        
        // Create components
        inventoryButton = createInventoryButton();
        roomsButton = createRoomsButton();  // ✨ NEW
        friendsButton = createFriendsButton();
        messageField = createMessageField();
        sendButton = createSendButton();
        
        // Add to inner panel
        innerPanel.add(inventoryButton);
        innerPanel.add(roomsButton);  // ✨ NEW - Add rooms button
        innerPanel.add(friendsButton); 
        innerPanel.add(messageField);
        innerPanel.add(sendButton);
        
        chatInputPanel.add(innerPanel, new GridBagConstraints());
        window.add(chatInputPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createChatPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CHAT_PANEL_BG);
        panel.setPreferredSize(new Dimension(gamePanel.screenWidth, CHAT_PANEL_HEIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.setLayout(new GridBagLayout());
        return panel;
    }
    
    private JPanel createInnerPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setOpaque(false);
        return panel;
    }
    
    // ═══════════════════════════════════════════════════════════
    // BUTTON CREATION
    // ═══════════════════════════════════════════════════════════
    
    private JButton createInventoryButton() {
        JButton button = createStyledButton("INVENTORY", 130, 35, INVENTORY_BG);

             
        try {
            ImageIcon originalIcon = new ImageIcon("src/res/icons/inventory_icon.png");
            // Scale to 20x20 pixels (adjust as needed)
            Image scaledImage = originalIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            System.out.println("inventory Icon not found");
        }
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(INVENTORY_BG_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(INVENTORY_BG);
            }
        });
        
        // Action
        button.addActionListener(e -> {
            gamePanel.inventoryWindow.toggle();
            gamePanel.repaint();
        });
        
        return button;
    }
    
    // ✨ NEW - Rooms button
    private JButton createRoomsButton() {
        JButton button = createStyledButton(" ROOMS", 110, 35, ROOMS_BG);


            
        try {
            ImageIcon originalIcon = new ImageIcon("src/res/icons/room_icon.png");
            // Scale to 20x20 pixels (adjust as needed)
            Image scaledImage = originalIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            System.out.println("Room Icon not found");
        }


        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(ROOMS_BG_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(ROOMS_BG);
            }
        });
        
        // Action
        button.addActionListener(e -> {
            gamePanel.roomNavigator.toggle();
            gamePanel.repaint();
        });
        
        return button;
    }


    //NEW METHOD
    private JButton createFriendsButton() {
        JButton button = createStyledButton("FRIENDS", 110, 35, FRIENDS_BG);
        
        // Try to load friends icon
        try {
            ImageIcon originalIcon = new ImageIcon("src/res/icons/friends_icon.png");
            Image scaledImage = originalIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            // Fallback: just use text
            System.out.println("Friends icon not found - using text only");
        }
        
        // White text on purple background
        button.setForeground(Color.BLACK);
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(FRIENDS_BG_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(FRIENDS_BG);
            }
        });
        
        // Action - toggle friends panel
        button.addActionListener(e -> {
            gamePanel.friendsPanel.toggle();
            gamePanel.repaint();
        });
        
        return button;
    }
    
    private JButton createSendButton() {
        JButton button = createStyledButton("SEND", 80, 35, SEND_BG);
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(SEND_BG_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(SEND_BG);
            }
        });
        
        // Action
        button.addActionListener(e -> sendChatMessage());
        
        return button;
    }
    
    /**
     * Helper method to create styled buttons with consistent appearance
     */
    private JButton createStyledButton(String text, int width, int height, Color bg) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(width, height));
        button.setBackground(bg);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
        return button;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MESSAGE FIELD CREATION
    // ═══════════════════════════════════════════════════════════
    
    private JTextField createMessageField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(300, 35));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.GRAY);  // Start with gray (placeholder)
        field.setCaretColor(Color.BLACK);
        field.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
        field.setFont(field.getFont().deriveFont(Font.ITALIC));
        field.setText(PLACEHOLDER);
        
        setupMessageFieldListeners(field);
        
        return field;
    }
    
    private void setupMessageFieldListeners(JTextField field) {
        // Focus listeners for placeholder
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(PLACEHOLDER)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setFont(field.getFont().deriveFont(Font.PLAIN));
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setFont(field.getFont().deriveFont(Font.ITALIC));
                    field.setText(PLACEHOLDER);
                }
                gamePanel.player.setTyping(false);
            }
        });
        
        // Document listener for typing indicator
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateTypingStatus();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateTypingStatus();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
            
            private void updateTypingStatus() {
                String text = field.getText();
                boolean isTyping = !text.equals(PLACEHOLDER) && !text.trim().isEmpty();
                gamePanel.player.setTyping(isTyping);
            }
        });
        
        // Enter key to send
        field.addActionListener(e -> sendChatMessage());
    }
    
    // ═══════════════════════════════════════════════════════════
    // EVENT HANDLERS
    // ═══════════════════════════════════════════════════════════
    
    private void initializeEventHandlers() {
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });
    }
    
    private void handleWindowClose() {
        gamePanel.cleanup();
        window.dispose();
        System.exit(0);
    }
    
    private void sendChatMessage() {
        String text = messageField.getText().trim();
        
        // Ignore empty or placeholder text
        if (text.isEmpty() || text.equals(PLACEHOLDER)) {
            return;
        }
        
        // Clear typing indicator
        gamePanel.player.setTyping(false);
        
        // Add message to local player
        int bubbleStartY = gamePanel.player.spriteY + 50;
        gamePanel.player.messages.add(
            new Entity.Player.Message(gamePanel.player.name + ": " + text, bubbleStartY)
        );
        
        // Send to network if connected
        if (gamePanel.networkManager != null) {
            gamePanel.networkManager.sendChatMessage(text);
        }
        
        // Clear field and repaint
        messageField.setText("");
        gamePanel.repaint();
    }
}