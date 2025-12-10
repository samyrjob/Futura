package service.kafka;

import controller.friend.FriendController;
import model.friend.FriendRequest;

/**
 * KafkaService - Main facade for Kafka messaging
 * 
 * Part of Service Layer - provides clean API for controllers
 * 
 * This class encapsulates all Kafka functionality:
 * - Friend event publishing (requests, responses, status)
 * - Friend event consumption
 * - Connection management
 * 
 * Usage in GamePanel:
 *   kafkaService = new KafkaService(playerUsername);
 *   kafkaService.setFriendController(friendController);
 *   kafkaService.start();
 *   
 *   // Later:
 *   kafkaService.sendFriendRequest(request);
 *   
 *   // On shutdown:
 *   kafkaService.shutdown();
 */
public class KafkaService {
    
    private final String playerUsername;
    private FriendEventProducer producer;
    private FriendEventConsumer consumer;
    private FriendController friendController;
    private boolean started = false;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public KafkaService(String playerUsername) {
        this.playerUsername = playerUsername;
        System.out.println("[KAFKA SERVICE] Created for player: " + playerUsername);
    }
    
    // ═══════════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Set the FriendController to receive incoming events
     * Must be called before start()
     */
    public void setFriendController(FriendController controller) {
        this.friendController = controller;
        
        // Update consumer if already created
        if (consumer != null) {
            consumer.setFriendController(controller);
        }
    }
    
    /**
     * Start the Kafka service (producer and consumer)
     */
    public void start() {
        if (started) {
            System.out.println("[KAFKA SERVICE] Already started");
            return;
        }
        
        System.out.println("[KAFKA SERVICE] Starting...");
        
        // Initialize producer
        this.producer = new FriendEventProducer();
        
        // Initialize and start consumer
        if (friendController != null) {
            this.consumer = new FriendEventConsumer(friendController, playerUsername);
            this.consumer.start();
        } else {
            System.err.println("[KAFKA SERVICE] Warning: No FriendController set, consumer not started");
        }
        
        started = true;
        System.out.println("[KAFKA SERVICE] Started successfully");
    }
    
    // ═══════════════════════════════════════════════════════════
    // FRIEND MESSAGING API
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Send a friend request to another player
     */
    public void sendFriendRequest(FriendRequest request) {
        if (producer != null && producer.isConnected()) {
            producer.sendFriendRequest(request);
        } else {
            System.err.println("[KAFKA SERVICE] Cannot send request - producer not connected");
        }
    }
    
    /**
     * Send a response to a friend request (accept/reject)
     */
    public void sendFriendResponse(FriendRequest response) {
        if (producer != null && producer.isConnected()) {
            producer.sendFriendResponse(response);
        } else {
            System.err.println("[KAFKA SERVICE] Cannot send response - producer not connected");
        }
    }
    
    /**
     * Send online/offline status update
     */
    public void sendStatusUpdate(boolean online, String currentRoom) {
        if (producer != null) {
            producer.sendStatusUpdate(playerUsername, online, currentRoom);
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // STATUS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Check if Kafka is connected
     */
    public boolean isConnected() {
        return producer != null && producer.isConnected();
    }
    
    /**
     * Check if service has been started
     */
    public boolean isStarted() {
        return started;
    }
    
    /**
     * Get the player username this service is for
     */
    public String getPlayerUsername() {
        return playerUsername;
    }
    
    // ═══════════════════════════════════════════════════════════
    // SHUTDOWN
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Shutdown the Kafka service gracefully
     */
    public void shutdown() {
        System.out.println("[KAFKA SERVICE] Shutting down...");
        
        // Send offline status
        if (producer != null && producer.isConnected()) {
            producer.sendStatusUpdate(playerUsername, false, null);
            producer.flush();
        }
        
        // Shutdown consumer
        if (consumer != null) {
            consumer.shutdown();
            try {
                consumer.join(5000);  // Wait up to 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Shutdown producer
        if (producer != null) {
            producer.close();
        }
        
        started = false;
        System.out.println("[KAFKA SERVICE] Shutdown complete");
    }
}