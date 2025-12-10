package service.kafka;

import controller.friend.FriendController;
import model.friend.FriendRequest;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * Kafka 4.1 Consumer for receiving friend events
 * Uses the new consumer rebalance protocol (KIP-848)
 * 
 * Part of Service Layer - handles external messaging
 * 
 * Updated for MVC Architecture:
 * - Uses FriendController instead of FriendManager
 * - Uses model.friend.FriendRequest
 */
public class FriendEventConsumer extends Thread {
    
    private KafkaConsumer<String, String> consumer;
    private FriendController friendController;
    private final String playerUsername;
    private volatile boolean running = true;
    
    public FriendEventConsumer(FriendController friendController, String playerUsername) {
        super("FriendEventConsumer-" + playerUsername);
        this.friendController = friendController;
        this.playerUsername = playerUsername;
        
        initializeConsumer();
    }
    
    private void initializeConsumer() {
        Properties props = new Properties();
        
        // Bootstrap server (Kafka 4.1 KRaft mode)
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS);
        
        // Consumer group - unique per player for individual message delivery
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfig.CONSUMER_GROUP_PREFIX + playerUsername);
        
        // Deserializers
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // Kafka 4.1 - Start from latest messages (don't replay old requests)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        
        // Auto commit
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        
        // Session management
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, KafkaConfig.SESSION_TIMEOUT_MS);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, KafkaConfig.HEARTBEAT_INTERVAL_MS);
        
        // Fetch settings for low latency
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100);
        
        // Client ID
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "futura-friend-consumer-" + playerUsername);
        
        try {
            this.consumer = new KafkaConsumer<>(props);
            
            // Subscribe to friend topics
            consumer.subscribe(Arrays.asList(
                KafkaConfig.TOPIC_FRIEND_REQUESTS,
                KafkaConfig.TOPIC_FRIEND_RESPONSES
            ));
            
            System.out.println("[KAFKA CONSUMER] Initialized for player: " + playerUsername);
            System.out.println("[KAFKA CONSUMER] Subscribed to topics:");
            System.out.println("[KAFKA CONSUMER]   - " + KafkaConfig.TOPIC_FRIEND_REQUESTS);
            System.out.println("[KAFKA CONSUMER]   - " + KafkaConfig.TOPIC_FRIEND_RESPONSES);
        } catch (Exception e) {
            System.err.println("[KAFKA CONSUMER] Failed to initialize: " + e.getMessage());
            this.consumer = null;
        }
    }
    
    @Override
    public void run() {
        if (consumer == null) {
            System.err.println("[KAFKA CONSUMER] Cannot start - consumer not initialized");
            return;
        }
        
        System.out.println("[KAFKA CONSUMER] Started listening for friend events...");
        
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                
                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record);
                }
            }
        } catch (WakeupException e) {
            // Expected when shutdown() is called
            if (running) {
                System.err.println("[KAFKA CONSUMER] Unexpected wakeup: " + e.getMessage());
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("[KAFKA CONSUMER] Error: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            closeConsumer();
        }
    }
    
    private void processRecord(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String key = record.key();
        String value = record.value();
        
        // Only process messages intended for this player
        if (key == null || !key.equalsIgnoreCase(playerUsername)) {
            return;
        }
        
        System.out.println("[KAFKA CONSUMER] ═══════════════════════════════════════");
        System.out.println("[KAFKA CONSUMER] Received message for: " + playerUsername);
        System.out.println("[KAFKA CONSUMER]   Topic: " + topic);
        System.out.println("[KAFKA CONSUMER]   Partition: " + record.partition());
        System.out.println("[KAFKA CONSUMER]   Offset: " + record.offset());
        System.out.println("[KAFKA CONSUMER]   Value: " + value);
        System.out.println("[KAFKA CONSUMER] ═══════════════════════════════════════");
        
        try {
            FriendRequest request = FriendRequest.fromJson(value);
            
            if (topic.equals(KafkaConfig.TOPIC_FRIEND_REQUESTS)) {
                // Someone sent us a friend request
                if (request.getType() == FriendRequest.RequestType.SEND_REQUEST) {
                    System.out.println("[KAFKA CONSUMER] 💬 Friend request from: " + request.getFromUsername());
                    friendController.receiveRequest(request);
                }
            } else if (topic.equals(KafkaConfig.TOPIC_FRIEND_RESPONSES)) {
                // Response to our friend request
                System.out.println("[KAFKA CONSUMER] 📬 Friend response from: " + request.getFromUsername() + 
                                  " - " + request.getType());
                friendController.handleResponse(request);
            }
        } catch (Exception e) {
            System.err.println("[KAFKA CONSUMER] Failed to parse message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void closeConsumer() {
        if (consumer != null) {
            try {
                consumer.close(Duration.ofSeconds(5));
                System.out.println("[KAFKA CONSUMER] Closed gracefully");
            } catch (Exception e) {
                System.err.println("[KAFKA CONSUMER] Error closing: " + e.getMessage());
            }
        }
    }
    
    public void shutdown() {
        System.out.println("[KAFKA CONSUMER] Shutting down...");
        running = false;
        if (consumer != null) {
            consumer.wakeup();  // Interrupt the poll() call
        }
    }

    public void setFriendController(FriendController controller) {
        this.friendController = controller;
    }
}