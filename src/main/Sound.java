package main;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Sound - Handles audio playback with RELIABLE looping
 */
public class Sound {
    
    private Clip clip;
    private FloatControl volumeControl;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private boolean shouldLoop = false;  // ✨ NEW - Track if we want looping
    private long clipPosition = 0;
    
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
            
            // ✨ NEW - Add listener to detect when clip finishes
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && shouldLoop && !isPaused) {
                    // Clip stopped but we want looping - restart it
                    System.out.println("Clip ended, restarting loop...");
                    clip.setFramePosition(0);
                    clip.start();
                }
            });
            
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
        String[] soundFiles = {
            "res/sounds/song.wav",
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
        
        shouldLoop = false;  // ✨ Single play, no loop
        
        if (isPaused) {
            clip.setFramePosition((int) clipPosition);
            clip.start();
            isPaused = false;
        } else {
            clip.setFramePosition(0);
            clip.start();
        }
        
        isPlaying = true;
        System.out.println("Playing sound");
    }
    
    /**
     * Play sound on loop - FIXED VERSION
     */
    public void loop() {
        if (clip == null) return;
        
        shouldLoop = true;  // ✨ Enable auto-loop
        
        // Stop any current playback
        if (clip.isRunning()) {
            clip.stop();
        }
        
        // Start from beginning
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        
        isPlaying = true;
        isPaused = false;
        
        System.out.println("Looping sound (LOOP_CONTINUOUSLY mode)");
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
     * Resume from pause
     */
    public void resume() {
        if (clip == null || !isPaused) return;
        
        clip.setFramePosition((int) clipPosition);
        
        if (shouldLoop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);  // Resume with looping
        } else {
            clip.start();  // Resume without looping
        }
        
        isPlaying = true;
        isPaused = false;
        
        System.out.println("Resumed sound");
    }
    
    /**
     * Stop sound completely (resets to beginning)
     */
    public void stop() {
        if (clip == null) return;
        
        shouldLoop = false;  // ✨ Disable looping
        clip.stop();
        clip.setFramePosition(0);
        isPlaying = false;
        isPaused = false;
        clipPosition = 0;
        
        System.out.println("Stopped sound");
    }
    
    /**
     * Toggle between play and pause - FIXED
     */
    public void togglePlayPause() {
        if (isPaused) {
            resume();  // ✨ Use resume instead of play
        } else if (isPlaying) {
            pause();
        } else {
            // Not playing and not paused - start fresh
            if (shouldLoop) {
                loop();
            } else {
                play();
            }
        }
    }
    
    /**
     * Set volume (0.0 to 1.0)
     */
    public void setVolume(float volume) {
        if (volumeControl == null) return;
        
        volume = Math.max(0.0f, Math.min(1.0f, volume));
        
        // Convert linear volume to decibels
        // Special case: volume 0 should be minimum dB, not -infinity
        float dB;
        if (volume < 0.01f) {
            dB = volumeControl.getMinimum();  // Mute
        } else {
            dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            // Clamp to valid range
            dB = Math.max(volumeControl.getMinimum(), 
                         Math.min(volumeControl.getMaximum(), dB));
        }
        
        volumeControl.setValue(dB);
        System.out.println("Volume set to: " + (int)(volume * 100) + "% (" + dB + " dB)");
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
     * Check if looping is enabled
     */
    public boolean isLooping() {
        return shouldLoop;
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