// package main;
// import javax.swing.*;
// import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.awt.event.MouseAdapter;
// import java.awt.event.MouseEvent;
// import java.awt.event.WindowAdapter;
// import java.awt.event.WindowEvent;
// import java.util.concurrent.atomic.AtomicBoolean;



// public class Main {
//     public static void main(String[] args) {

//         String username = "Dominique";
//         String gender = "male";
//         AtomicBoolean flag = new AtomicBoolean(false);

//         if (args.length > 0 && args[0].startsWith("futura://")) {
//             String query = args[0].substring(args[0].indexOf("?") + 1);
//             String[] pairs = query.split("&");
//             for (String pair : pairs) {
//                 String[] kv = pair.split("=");
//                 if (kv.length == 2) {
//                     if (kv[0].equalsIgnoreCase("user")) {
//                         username = kv[1];
//                     } else if (kv[0].equalsIgnoreCase("gender")) {
//                         gender = kv[1];
//                     }
//                 }
//             }
//             flag.set(true);
//         }

        

//         String finalUsername = username;
//         String finalGender = gender;
//         System.out.println("gender is" + finalGender + "\n");
//         System.out.println("username is" + finalUsername + " \n");

//         SwingUtilities.invokeLater(() -> {
//             if (flag.get()) {
//                 new GamePanelFrame(finalUsername, finalGender);
//             } else {
//                 new LoginFrame();
//             }
//         });
//     }
// }




// // Login Frame (Landing Page)
// class LoginFrame extends JFrame {
//     private JTextField usernameField;
//     private JPasswordField passwordField;

//     public LoginFrame() {
//         setTitle("Login Page");
//         setSize(400, 200);
//         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         setLocationRelativeTo(null);

//         // Layout
//         setLayout(new GridLayout(3, 2));

//         // Add components
//         JLabel usernameLabel = new JLabel("Username:");
//         JLabel passwordLabel = new JLabel("Password:");
//         usernameField = new JTextField();
//         passwordField = new JPasswordField();
//         JButton loginButton = new JButton("Login");
//         JLabel registerLabel = new JLabel("<html><a href='#'>No account? Click here</a></html>");
//         registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

//         add(usernameLabel);
//         add(usernameField);
//         add(passwordLabel);
//         add(passwordField);
//         add(registerLabel);
//         add(loginButton);

//         registerLabel.addMouseListener(new MouseAdapter(){

//             @Override
//             public void mouseClicked(MouseEvent e){
//                 // Open a registration window or perform an action
//                 JOptionPane.showMessageDialog(null, "Redirecting to registration!");
//             }

//         });

    

//           // Login button action
//           loginButton.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 String username = usernameField.getText();
//                 String password = new String(passwordField.getPassword());

//                 // Example validation (replace with your logic)
//                 if (username.equals("user") && password.equals("1234")) {
//                     JOptionPane.showMessageDialog(null, "Login successful!");
//                     dispose(); // Close login window
//                     //! here we can change the gender
//                     new GamePanelFrame(username, "male"); // Open game panel
//                 } else {
//                     JOptionPane.showMessageDialog(null, "Invalid credentials. Try again.");
//                 }
//             }
//         });
    

//         setVisible(true);
//     }
// }



//     class GamePanelFrame {

//         private GamePanel gamepanel;
//         private JTextField messageField;
//         private JButton sendButton;

//         public GamePanelFrame(String username, String gender) {

//             JFrame window = new JFrame("2D Adventure - Multiplayer");
//             window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//             window.setResizable(false);
//             window.setLayout(new BorderLayout());

//             // --- Main Game Area ---
//             gamepanel = new GamePanel(username, gender);
//             window.add(gamepanel, BorderLayout.CENTER);


//             // --- Chat Input Panel ---
//             JPanel chatInputPanel = new JPanel();
//             chatInputPanel.setBackground(new Color(40, 40, 40));
//             chatInputPanel.setPreferredSize(new Dimension(gamepanel.screenWidth, 60));
//             chatInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
//             chatInputPanel.setLayout(new GridBagLayout()); // to center the inner panel

//             // Inner panel to hold field + button
//             JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
//             innerPanel.setOpaque(false); // transparent

//             // Input field
//             messageField = new JTextField();
//             messageField.setPreferredSize(new Dimension(300, 35));
//             messageField.setBackground(Color.WHITE);
//             messageField.setForeground(Color.BLACK);
//             messageField.setCaretColor(Color.BLACK);
//             messageField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true)); // rounded black border

//             // Placeholder setup
//             String placeholderText = "type here to write a message";
//             messageField.setForeground(Color.GRAY);
//             messageField.setFont(messageField.getFont().deriveFont(Font.ITALIC));
//             messageField.setText(placeholderText);

//             // Add focus listener for placeholder behavior
//             messageField.addFocusListener(new java.awt.event.FocusAdapter() {
//                 @Override
//                 public void focusGained(java.awt.event.FocusEvent e) {
//                     if (messageField.getText().equals(placeholderText)) {
//                         messageField.setText("");
//                         messageField.setForeground(Color.BLACK);
//                         messageField.setFont(messageField.getFont().deriveFont(Font.PLAIN));
//                     }
//                 }

//                 @Override
//                 public void focusLost(java.awt.event.FocusEvent e) {
//                     if (messageField.getText().isEmpty()) {
//                         messageField.setForeground(Color.GRAY);
//                         messageField.setFont(messageField.getFont().deriveFont(Font.ITALIC));
//                         messageField.setText(placeholderText);
//                     }
//                 }
//             });

//             // Send button
//             sendButton = new JButton("SEND");
//             sendButton.setPreferredSize(new Dimension(80, 35));
//             sendButton.setBackground(Color.WHITE);
//             sendButton.setForeground(Color.BLACK);
//             sendButton.setFont(new Font("Arial", Font.BOLD, 13));
//             sendButton.setFocusPainted(false);
//             sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
//             sendButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true)); // same rounded black border

//             // Hover effect (optional subtle feedback)
//             sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
//                 @Override
//                 public void mouseEntered(java.awt.event.MouseEvent e) {
//                     sendButton.setBackground(new Color(230, 230, 230));
//                 }
//                 @Override
//                 public void mouseExited(java.awt.event.MouseEvent e) {
//                     sendButton.setBackground(Color.WHITE);
//                 }
//             });

//             // Add to inner panel
//             innerPanel.add(messageField);
//             innerPanel.add(sendButton);

//             // Center in parent panel
//             chatInputPanel.add(innerPanel, new GridBagConstraints());


//             // Add chat input panel at bottom
//             window.add(chatInputPanel, BorderLayout.SOUTH);

//             // --- Frame Setup ---
//             Toolkit toolkit = Toolkit.getDefaultToolkit();
//             Dimension screenSize = toolkit.getScreenSize();
//             window.setSize(
//                 Math.min(gamepanel.screenWidth, screenSize.width),
//                 Math.min(gamepanel.screenHeight + 60, screenSize.height)
//             );
//             window.setLocationRelativeTo(null);

//             // --- Send message logic ---
//             sendButton.addActionListener(e -> sendChatMessage());
//             messageField.addActionListener(e -> sendChatMessage()); // allow Enter key

//             // --- Handle close event ---
//             window.addWindowListener(new WindowAdapter() {
//                 @Override
//                 public void windowClosing(WindowEvent e) {
//                     gamepanel.cleanup();
//                     window.dispose();
//                     System.exit(0);
//                 }
//             });

//             window.setVisible(true);
//             gamepanel.startGameThread();
//             gamepanel.setupGame();
//         }

//         private void sendChatMessage() {

//             String text = messageField.getText().trim();
//                 if (text.isEmpty() || text.equals("type here to write a message")) return;





//             // Add message to player messages
//             gamepanel.player.messages.add(
//                 new Entity.Player.Message(gamepanel.player.name + ": " + text, gamepanel.getHeight() - 95)
//             );

//             // Send message to other players if multiplayer enabled
//             if (gamepanel.networkManager != null) {
//                 gamepanel.networkManager.sendChatMessage(text);
//             }

//             messageField.setText("");
//             gamepanel.repaint();
//         }
//     }



// class OpenRegistrationSite {

    


// }

package main;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;



public class Main {
    public static void main(String[] args) {

        String username = "Dominique";
        String gender = "male";
        AtomicBoolean flag = new AtomicBoolean(false);

        if (args.length > 0 && args[0].startsWith("futura://")) {
            String query = args[0].substring(args[0].indexOf("?") + 1);
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    if (kv[0].equalsIgnoreCase("user")) {
                        username = kv[1];
                    } else if (kv[0].equalsIgnoreCase("gender")) {
                        gender = kv[1];
                    }
                }
            }
            flag.set(true);
        }

        

        String finalUsername = username;
        String finalGender = gender;
        System.out.println("gender is" + finalGender + "\n");
        System.out.println("username is" + finalUsername + " \n");

        SwingUtilities.invokeLater(() -> {
            if (flag.get()) {
                new GamePanelFrame(finalUsername, finalGender);
            } else {
                new LoginFrame();
            }
        });
    }
}




// Login Frame (Landing Page)
class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Login Page");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout
        setLayout(new GridLayout(3, 2));

        // Add components
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JLabel registerLabel = new JLabel("<html><a href='#'>No account? Click here</a></html>");
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(registerLabel);
        add(loginButton);

        registerLabel.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e){
                // Open a registration window or perform an action
                JOptionPane.showMessageDialog(null, "Redirecting to registration!");
            }

        });

    

          // Login button action
          loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Example validation (replace with your logic)
                if (username.equals("user") && password.equals("1234")) {
                    JOptionPane.showMessageDialog(null, "Login successful!");
                    dispose(); // Close login window
                    //! here we can change the gender
                    new GamePanelFrame(username, "male"); // Open game panel
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials. Try again.");
                }
            }
        });
    

        setVisible(true);
    }
}



    class GamePanelFrame {

        private GamePanel gamepanel;
        private JTextField messageField;
        private JButton sendButton;

        public GamePanelFrame(String username, String gender) {

            JFrame window = new JFrame("2D Adventure - Multiplayer");
            window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            window.setResizable(false);
            window.setLayout(new BorderLayout());

            // --- Main Game Area ---
            gamepanel = new GamePanel(username, gender);
            window.add(gamepanel, BorderLayout.CENTER);


            // --- Chat Input Panel ---
            JPanel chatInputPanel = new JPanel();
            chatInputPanel.setBackground(new Color(40, 40, 40));
            chatInputPanel.setPreferredSize(new Dimension(gamepanel.screenWidth, 60));
            chatInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            chatInputPanel.setLayout(new GridBagLayout()); // to center the inner panel

            // Inner panel to hold field + button
            JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            innerPanel.setOpaque(false); // transparent

            // Input field
            messageField = new JTextField();
            messageField.setPreferredSize(new Dimension(300, 35));
            messageField.setBackground(Color.WHITE);
            messageField.setForeground(Color.BLACK);
            messageField.setCaretColor(Color.BLACK);
            messageField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true)); // rounded black border

            // Placeholder setup
            String placeholderText = "type here to write a message";
            messageField.setForeground(Color.GRAY);
            messageField.setFont(messageField.getFont().deriveFont(Font.ITALIC));
            messageField.setText(placeholderText);

            // Add focus listener for placeholder behavior
            messageField.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (messageField.getText().equals(placeholderText)) {
                        messageField.setText("");
                        messageField.setForeground(Color.BLACK);
                        messageField.setFont(messageField.getFont().deriveFont(Font.PLAIN));
                    }
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (messageField.getText().isEmpty()) {
                        messageField.setForeground(Color.GRAY);
                        messageField.setFont(messageField.getFont().deriveFont(Font.ITALIC));
                        messageField.setText(placeholderText);
                    }
                    // ✨ NEW: Stop typing indicator when focus lost
                    gamepanel.player.setTyping(false);
                }
            });

            // ✨ NEW: Add typing detection listener!
            messageField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    // User is typing!
                    String text = messageField.getText();
                    if (!text.equals(placeholderText) && !text.trim().isEmpty()) {
                        gamepanel.player.setTyping(true);
                    }
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    // User is still typing (deleting)
                    String text = messageField.getText();
                    if (!text.isEmpty() && !text.equals(placeholderText)) {
                        gamepanel.player.setTyping(true);
                    } else {
                        gamepanel.player.setTyping(false);
                    }
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    // Plain text components don't fire these events
                }
            });

            // Send button
            sendButton = new JButton("SEND");
            sendButton.setPreferredSize(new Dimension(80, 35));
            sendButton.setBackground(Color.WHITE);
            sendButton.setForeground(Color.BLACK);
            sendButton.setFont(new Font("Arial", Font.BOLD, 13));
            sendButton.setFocusPainted(false);
            sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            sendButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true)); // same rounded black border

            // Hover effect (optional subtle feedback)
            sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    sendButton.setBackground(new Color(230, 230, 230));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    sendButton.setBackground(Color.WHITE);
                }
            });

            // Add to inner panel
            innerPanel.add(messageField);
            innerPanel.add(sendButton);

            // Center in parent panel
            chatInputPanel.add(innerPanel, new GridBagConstraints());


            // Add chat input panel at bottom
            window.add(chatInputPanel, BorderLayout.SOUTH);

            // --- Frame Setup ---
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            window.setSize(
                Math.min(gamepanel.screenWidth, screenSize.width),
                Math.min(gamepanel.screenHeight + 60, screenSize.height)
            );
            window.setLocationRelativeTo(null);

            // --- Send message logic ---
            sendButton.addActionListener(e -> sendChatMessage());
            messageField.addActionListener(e -> sendChatMessage()); // allow Enter key

            // --- Handle close event ---
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    gamepanel.cleanup();
                    window.dispose();
                    System.exit(0);
                }
            });

            window.setVisible(true);
            gamepanel.startGameThread();
            gamepanel.setupGame();
        }

        private void sendChatMessage() {

            String text = messageField.getText().trim();
            if (text.isEmpty() || text.equals("type here to write a message")) return;

            // ✨ NEW: Stop typing indicator when message sent
            gamepanel.player.setTyping(false);

            // Add message to player messages
            gamepanel.player.messages.add(
                new Entity.Player.Message(gamepanel.player.name + ": " + text, gamepanel.getHeight() - 95)
            );

            // Send message to other players if multiplayer enabled
            if (gamepanel.networkManager != null) {
                gamepanel.networkManager.sendChatMessage(text);
            }

            messageField.setText("");
            gamepanel.repaint();
        }
    }



class OpenRegistrationSite {

    


}