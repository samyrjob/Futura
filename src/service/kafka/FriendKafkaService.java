package service.kafka;

import controller.friend.FriendController;
import model.friend.FriendRequest;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * Service for Kafka-based friend messaging.
 * Wraps Kafka producer and consumer for friend requests/responses.
 * 
 * Part of MVC architecture - this is the Service layer.
 * Handles all Kafka communication, isolating it from business logic.
 */
public class FriendKafkaService {
    
    // ═══════════════════════════════════════════════════════════
    // CONFIGURATION
    // ═══════════════════════════════════════════════════════════
    
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC_FRIEND_REQUESTS = "futura-friend-requests";
    private static final String TOPIC_FRIEND_RESPONSES = "futura-friend-responses";
    private static final String CONSUMER_GROUP_PREFIX = "futura-client-";
    
    // ═══════════════════════════════════════════════════════════
    // COMPONENTS
    // ═══════════════════════════════════════════════════════════
    
    private final FriendController controller;
    private final String playerUsername;
    
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    private Thread consumerThread;
    
    private volatile boolean connected;
    private volatile boolean running;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public FriendKafkaService(FriendController controller, String playerUsername) {
        this.controller = controller;
        this.playerUsername = playerUsername;
        this.connected = false;
        this.running = false;
        
        initializeProducer();
        initializeConsumer();
        startConsumerThread();
    }
    
    // ═══════════════════════════════════════════════════════════
    // PRODUCER
    // ═══════════════════════════════════════════════════════════
    
    private void initializeProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "futura-friend-producer-" + playerUsername);
        
        try {
            this.producer = new KafkaProducer<>(props);
            this.connected = true;
            System.out.println("[KAFKA SVC] Producer initialized for: " + playerUsername);
        } catch (Exception e) {
            System.err.println("[KAFKA SVC] Failed to initialize producer: " + e.getMessage());
            this.producer = null;
            this.connected = false;
        }
    }
    
    /**
     * Send a friend request via Kafka
     * @return true if sent successfully
     */
    public boolean sendRequest(FriendRequest request) {
        return send(TOPIC_FRIEND_REQUESTS, request.getToUsername(), request.toJson());
    }
    
    /**
     * Send a friend response via Kafka
     * @return true if sent successfully
     */
    public boolean sendResponse(FriendRequest response) {
        return send(TOPIC_FRIEND_RESPONSES, response.getToUsername(), response.toJson());
    }
    
    private boolean send(String topic, String key, String value) {
        if (producer == null || !connected) {
            System.err.println("[KAFKA SVC] Not connected, cannot send message");
            return false;
        }
        
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        
        try {
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("[KAFKA SVC] Send failed: " + exception.getMessage());
                    connected = false;
                } else {
                    System.out.println("[KAFKA SVC] ✓ Sent to " + topic + 
                                      " partition " + metadata.partition() + 
                                      " offset " + metadata.offset());
                    connected = true;
                }
            });
            return true;
        } catch (Exception e) {
            System.err.println("[KAFKA SVC] Send error: " + e.getMessage());
            return false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONSUMER
    // ═══════════════════════════════════════════════════════════
    
    private void initializeConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_PREFIX + playerUsername);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "45000");
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "futura-friend-consumer-" + playerUsername);
        
        try {
            this.consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(TOPIC_FRIEND_REQUESTS, TOPIC_FRIEND_RESPONSES));
            System.out.println("[KAFKA SVC] Consumer initialized for: " + playerUsername);
        } catch (Exception e) {
            System.err.println("[KAFKA SVC] Failed to initialize consumer: " + e.getMessage());
            this.consumer = null;
        }
    }
    
    private void startConsumerThread() {
        if (consumer == null) {
            System.err.println("[KAFKA SVC] Cannot start consumer thread - consumer not initialized");
            return;
        }
        
        running = true;
        consumerThread = new Thread(() -> {
            System.out.println("[KAFKA SVC] Consumer thread started");
            
            try {
                while (running) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    
                    for (ConsumerRecord<String, String> record : records) {
                        processRecord(record);
                    }
                }
            } catch (WakeupException e) {
                if (running) {
                    System.err.println("[KAFKA SVC] Unexpected wakeup: " + e.getMessage());
                }
            } catch (Exception e) {
                if (running) {
                    System.err.println("[KAFKA SVC] Consumer error: " + e.getMessage());
                }
            } finally {
                closeConsumer();
            }
        }, "FriendKafkaConsumer-" + playerUsername);
        
        consumerThread.setDaemon(true);
        consumerThread.start();
    }
    
    private void processRecord(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String key = record.key();
        String value = record.value();
        
        // Only process messages intended for this player
        if (key == null || !key.equalsIgnoreCase(playerUsername)) {
            return;
        }
        
        System.out.println("[KAFKA SVC] ═══════════════════════════════════════");
        System.out.println("[KAFKA SVC] Received message for: " + playerUsername);
        System.out.println("[KAFKA SVC]   Topic: " + topic);
        System.out.println("[KAFKA SVC]   Value: " + value);
        System.out.println("[KAFKA SVC] ═══════════════════════════════════════");
        
        try {
            FriendRequest request = FriendRequest.fromJson(value);
            
            if (topic.equals(TOPIC_FRIEND_REQUESTS)) {
                if (request.getType() == FriendRequest.RequestType.SEND_REQUEST) {
                    System.out.println("[KAFKA SVC] 💬 Friend request from: " + request.getFromUsername());
                    controller.receiveRequest(request);
                }
            } else if (topic.equals(TOPIC_FRIEND_RESPONSES)) {
                System.out.println("[KAFKA SVC] 📬 Friend response from: " + request.getFromUsername() + 
                                  " - " + request.getType());
                controller.handleResponse(request);
            }
        } catch (Exception e) {
            System.err.println("[KAFKA SVC] Failed to parse message: " + e.getMessage());
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // STATUS
    // ═══════════════════════════════════════════════════════════
    
    public boolean isConnected() {
        return connected && producer != null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════
    
    public void shutdown() {
        System.out.println("[KAFKA SVC] Shutting down...");
        running = false;
        
        if (consumer != null) {
            consumer.wakeup();
        }
        
        closeProducer();
    }
    
    private void closeProducer() {
        if (producer != null) {
            try {
                producer.flush();
                producer.close();
                System.out.println("[KAFKA SVC] Producer closed");
            } catch (Exception e) {
                System.err.println("[KAFKA SVC] Error closing producer: " + e.getMessage());
            }
            producer = null;
        }
        connected = false;
    }
    
    private void closeConsumer() {
        if (consumer != null) {
            try {
                consumer.close(Duration.ofSeconds(5));
                System.out.println("[KAFKA SVC] Consumer closed");
            } catch (Exception e) {
                System.err.println("[KAFKA SVC] Error closing consumer: " + e.getMessage());
            }
            consumer = null;
        }
    }
}