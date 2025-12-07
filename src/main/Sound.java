// package main;

// import java.net.URL;
// import javax.sound.sampled.AudioSystem;
// import javax.sound.sampled.AudioInputStream;
// import javax.sound.sampled.Clip;


// public class Sound {

//     URL soundURL[]= new URL[30];
//     Clip clip;

//     public Sound(){

//         soundURL[0] = getClass().getResource("/res/sound/BlueBoyAdventure.wav");
//         soundURL[1] = getClass().getResource("/res/sound/coin.wav");
//         soundURL[2] = getClass().getResource("/res/sound/fanfare.wav");
//         soundURL[3] = getClass().getResource("/res/sound/powerup.wav");
//         soundURL[4] = getClass().getResource("/res/sound/unlock.wav");
//     }

//     public void setFile(int i){

//         try {

//             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL[i]);
//             clip = AudioSystem.getClip();
//             clip.open(audioInputStream);
//         }
//         catch (Exception e){

//         }

//     }

//     public void play(){
//         clip.start();
//     }

//     public void loop(){
//         clip.loop(Clip.LOOP_CONTINUOUSLY);
//     }

//     public void stop(){
//         clip.stop();
//     }



// }
package main;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Sound - Handles audio playback with play/pause/stop/loop
 */
public class Sound {
    
    private Clip clip;
    private FloatControl volumeControl;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private long clipPosition = 0;  // Remember position when paused
    
    /**
     * Load sound from file path
     */
    public void setFile(String filePath) {
        try {
            // Close existing clip if any
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
            
            // Load audio file
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            
            // Create clip
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            // Get volume control if available
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            }
            
            System.out.println("Loaded sound: " + filePath);
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio format: " + filePath);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not find sound file: " + filePath);
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable");
            e.printStackTrace();
        }
    }
    
    /**
     * Load sound from index (for backward compatibility)
     */
    public void setFile(int index) {
        // Map index to file - you can customize this
        String[] soundFiles = {
            "res/sounds/song.wav",  // Index 0 = main song
            "res/sounds/effect1.wav",
            "res/sounds/effect2.wav"
        };
        
        if (index >= 0 && index < soundFiles.length) {
            setFile(soundFiles[index]);
        }
    }
    
    /**
     * Play sound once
     */
    public void play() {
        if (clip == null) return;
        
        if (isPaused) {
            // Resume from paused position
            clip.setFramePosition((int) clipPosition);
            clip.start();
            isPaused = false;
        } else {
            // Start from beginning
            clip.setFramePosition(0);
            clip.start();
        }
        
        isPlaying = true;
        System.out.println("Playing sound");
    }
    
    /**
     * Play sound on loop
     */
    public void loop() {
        if (clip == null) return;
        
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        isPlaying = true;
        isPaused = false;
        
        System.out.println("Looping sound");
    }
    
    /**
     * Pause sound (can be resumed)
     */
    public void pause() {
        if (clip == null || !isPlaying) return;
        
        clipPosition = clip.getFramePosition();
        clip.stop();
        isPlaying = false;
        isPaused = true;
        
        System.out.println("Paused sound at position: " + clipPosition);
    }
    
    /**
     * Stop sound completely (resets to beginning)
     */
    public void stop() {
        if (clip == null) return;
        
        clip.stop();
        clip.setFramePosition(0);
        isPlaying = false;
        isPaused = false;
        clipPosition = 0;
        
        System.out.println("Stopped sound");
    }
    
    /**
     * Toggle between play and pause
     */
    public void togglePlayPause() {
        if (isPlaying) {
            pause();
        } else {
            play();
        }
    }
    
    /**
     * Set volume (0.0 to 1.0)
     */
    public void setVolume(float volume) {
        if (volumeControl == null) return;
        
        // Clamp volume between 0 and 1
        volume = Math.max(0.0f, Math.min(1.0f, volume));
        
        // Convert linear volume to decibels
        float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
        volumeControl.setValue(dB);
        
        System.out.println("Volume set to: " + (volume * 100) + "%");
    }
    
    /**
     * Check if sound is currently playing
     */
    public boolean isPlaying() {
        return isPlaying && clip != null && clip.isRunning();
    }
    
    /**
     * Check if sound is paused
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Get current playback position (0.0 to 1.0)
     */
    public float getProgress() {
        if (clip == null) return 0.0f;
        
        long current = clip.getFramePosition();
        long total = clip.getFrameLength();
        
        return (float) current / total;
    }
    
    /**
     * Clean up resources
     */
    public void close() {
        if (clip != null) {
            clip.close();
            System.out.println("Sound clip closed");
        }
    }
}