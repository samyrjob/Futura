// package main;
// import javax.swing.*;
// import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.awt.event.MouseAdapter;
// import java.awt.event.MouseEvent;
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



// class GamePanelFrame {

//     public GamePanelFrame(String username, String gender) {

//         // System.setProperty("sun.java2d.uiScale", "1.0");


//         JFrame window = new JFrame();


//         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


//         window.setResizable(false);

//         GamePanel gamepanel = new GamePanel(username, gender);
//         window.add(gamepanel);

//         Toolkit toolkit = Toolkit.getDefaultToolkit();
//         Dimension screenSize = toolkit.getScreenSize();


// // Ensure the frame fits within the screen dimensions
//         window.setSize(Math.min(gamepanel.screenWidth, screenSize.width), Math.min(gamepanel.screenHeight, screenSize.height));


//         window.pack();


//         window.setTitle("2D Adventure");
//         window.setLocationRelativeTo(null);
//         window.setVisible(true);

// //        gamepanel.setupGame();

//         gamepanel.startGameThread();
//         gamepanel.setupGame();
//     }
// }


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



// class GamePanelFrame {

//     private GamePanel gamepanel;

//     public GamePanelFrame(String username, String gender) {

//         JFrame window = new JFrame();

//         window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//         window.setResizable(false);

//         gamepanel = new GamePanel(username, gender);
//         window.add(gamepanel);

//         Toolkit toolkit = Toolkit.getDefaultToolkit();
//         Dimension screenSize = toolkit.getScreenSize();

//         // Ensure the frame fits within the screen dimensions
//         window.setSize(Math.min(gamepanel.screenWidth, screenSize.width), Math.min(gamepanel.screenHeight, screenSize.height));

//         window.pack();

//         window.setTitle("2D Adventure - Multiplayer");
//         window.setLocationRelativeTo(null);
        
//         // Add window listener to handle cleanup on close
//         window.addWindowListener(new WindowAdapter() {
//             @Override
//             public void windowClosing(WindowEvent e) {
//                 // Cleanup network connections
//                 gamepanel.cleanup();
                
//                 // Close the window
//                 window.dispose();
//                 System.exit(0);
//             }
//         });
        
//         window.setVisible(true);

//         gamepanel.startGameThread();
//         gamepanel.setupGame();
//     }
// }

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
            JPanel chatInputPanel = new JPanel(new BorderLayout());
            chatInputPanel.setBackground(new Color(40, 40, 40));
            chatInputPanel.setPreferredSize(new Dimension(gamepanel.screenWidth, 40));
            chatInputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            messageField = new JTextField();
            sendButton = new JButton("Send");

            chatInputPanel.add(messageField, BorderLayout.CENTER);
            chatInputPanel.add(sendButton, BorderLayout.EAST);

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
            if (text.isEmpty()) return;

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