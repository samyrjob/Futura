package main;

import java.net.URL;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;


public class Sound {

    URL soundURL[]= new URL[30];
    Clip clip;

    public Sound(){

        soundURL[0] = getClass().getResource("/res/sound/BlueBoyAdventure.wav");
        soundURL[1] = getClass().getResource("/res/sound/coin.wav");
        soundURL[2] = getClass().getResource("/res/sound/fanfare.wav");
        soundURL[3] = getClass().getResource("/res/sound/powerup.wav");
        soundURL[4] = getClass().getResource("/res/sound/unlock.wav");
    }

    public void setFile(int i){

        try {

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        }
        catch (Exception e){

        }

    }

    public void play(){
        clip.start();
    }

    public void loop(){
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop(){
        clip.stop();
    }



}
