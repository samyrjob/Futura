package message;

import javax.swing.*;

import org.w3c.dom.events.MouseEvent;

import main.GamePanel;
import Entity.Player;
import Entity.Player.Message;

import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

public class ChatBox{

    GamePanel gp;
    Player player;
     
    JButton sendButton;
    JTextField inputField;
    private Timer timer;
    public int scrollY = 0; // Allows scrolling to view older messages
    public int lastMouseY;
    public boolean draggingMessage = false; // Track if user clicked a message



    public ChatBox(GamePanel gp, Player player){
        this.gp = gp;
        this.player = player;
    }


    
    public void setChatBox() {

        gp.setLayout(null);

        // Initialize components AFTER layeredPane is added
        inputField = new JTextField(20);
        sendButton = new JButton("Send");

        gp.add(inputField);
        gp.add(sendButton);
        gp.setComponentZOrder(inputField, 0);
        gp.setComponentZOrder(sendButton, 0);
    
        inputField.setBounds(10 + 75, gp.getHeight() - 40, 300, 30); 
        sendButton.setBounds(320 + 75, gp.getHeight() - 40, 80, 30);
        // Ensure inputField and sendButton are visible





        timer = new Timer(1000, e -> {
            for (Message msg : player.messages) {
                msg.y -= 20; // Move message up by 20 pixels
            }
            gp.repaint(); // Refresh the chat display
        });
        timer.start(); // Start the timer

       

             sendButton.addActionListener(e -> {
                String text = inputField.getText();
                if (!text.isEmpty()) {
                    addMessage(text);
                    inputField.setText(""); // Clear input field
                }
                });

                    // Add ActionListener for the input field to send message when Enter is pressed
             inputField.addActionListener(e -> {
                    String text = inputField.getText();
                    if (!text.isEmpty()) {
                        addMessage(text); // Send the message
                        inputField.setText(""); // Clear input field after sending
                    }
                });
    }


      
    // Function to check if mouse click is inside any message
    public boolean isClickOnMessage(int x, int y) {

        for (Message message : player.messages){
            if (x <= message.text.length() * 7 + 20 && x>=10 && y <= message.adjustedY + 30 && y>= message.adjustedY){
                return true;
            }

        }
        return false;
    }

  

    

    public void addMessage(String message) {
        // Calculate the starting Y position of the new message
        int startY =  gp.getHeight() - 95;
        player.messages.add(new Message(message, startY)); // Add the new message to the list
        gp.repaint();
    }






  
}
