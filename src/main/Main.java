package main;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class Main {
    public static void main(String[] args) {
        // Start the application with the login page
        SwingUtilities.invokeLater(() -> new LoginFrame());
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
                    new GamePanelFrame(); // Open game panel
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials. Try again.");
                }
            }
        });
    

        setVisible(true);
    }
}



class GamePanelFrame {

    public GamePanelFrame() {

        // System.setProperty("sun.java2d.uiScale", "1.0");


        JFrame window = new JFrame();


        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        window.setResizable(false);

        GamePanel gamepanel = new GamePanel();
        window.add(gamepanel);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();


// Ensure the frame fits within the screen dimensions
        window.setSize(Math.min(gamepanel.screenWidth, screenSize.width), Math.min(gamepanel.screenHeight, screenSize.height));


        window.pack();


        window.setTitle("2D Adventure");
        window.setLocationRelativeTo(null);
        window.setVisible(true);

//        gamepanel.setupGame();

        gamepanel.startGameThread();
        gamepanel.setupGame();
    }
}


class OpenRegistrationSite {

    


}