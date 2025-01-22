package mouse;

import main.GamePanel;

import java.awt.*;
import java.awt.event.MouseEvent;

import java.awt.event.*;

public class MyMouseAdapter extends MouseAdapter {



    GamePanel gp;

    // Constructor to initialize GamePanel
    public MyMouseAdapter(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void mouseMoved(MouseEvent e) {

//
        int mouseX = e.getX();
        int mouseY = e.getY();
//
//
        gp.mouseX = (mouseX);
        gp.mouseY = (mouseY);



    }


}