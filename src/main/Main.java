package main;
import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

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
    }
}

