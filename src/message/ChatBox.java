package message;

import javax.swing.*;



import main.GamePanel;
import Entity.Player;
import Entity.Player.Message;

import java.awt.*;



public class ChatBox{

    GamePanel gp;
    Player player;
     
    // JButton sendButton;
    // JTextField inputField;
    private Timer timer;
    public int scrollY = 0; // Allows scrolling to view older messages
    public int lastMouseY;
    public boolean draggingMessage = false; // Track if user clicked a message



    public ChatBox(GamePanel gp, Player player){
        this.gp = gp;
        this.player = player;
    }


    
    public void setChatBox() {


   





        timer = new Timer(1000, e -> {
            for (Message msg : player.messages) {
                msg.y -= 20; // Move message up by 20 pixels
            }
            gp.repaint(); // Refresh the chat display
        });
        timer.start(); // Start the timer

       
           
    }       //!


      
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

     public void draw(Graphics2D g2d){

    }






  
}

