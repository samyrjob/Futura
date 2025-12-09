package kafka;

import friend.FriendRequest;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Kafka 4.1 Producer for sending friend events
 * Uses KRaft mode (no ZooKeeper)
 */
public class FriendEventProducer {
    
    private KafkaProducer<String, String> producer;
    private volatile boolean isConnected = false;
    
    public FriendEventProducer() {
        Properties props = new Properties();
        
        // Bootstrap server (Kafka 4.1 KRaft mode)
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS);
        
        // Serializers
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // Kafka 4.1 - Idempotence is enabled by default for exactly-once semantics
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        
        // Retries and timeouts
        props.put(ProducerConfig.RETRIES_CONFIG, KafkaConfig.MAX_RETRIES);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, KafkaConfig.RETRY_BACKOFF_MS);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, KafkaConfig.REQUEST_TIMEOUT_MS);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        
        // Batching (Kafka 4.x default linger.ms is 5ms)
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        
        // Client ID for tracking
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "futura-friend-producer-" + System.currentTimeMillis());
        
        try {
            this.producer = new KafkaProducer<>(props);
            this.isConnected = true;
            System.out.println("[KAFKA PRODUCER] Initialized successfully");
            System.out.println("[KAFKA PRODUCER] Connected to: " + KafkaConfig.BOOTSTRAP_SERVERS);
        } catch (Exception e) {
            System.err.println("[KAFKA PRODUCER] Failed to initialize: " + e.getMessage());
            this.isConnected = false;
        }
    }
    
    /**
     * Send a friend request event
     */
    public Future<RecordMetadata> sendFriendRequest(FriendRequest request) {
        if (producer == null || !isConnected) {
            System.err.println("[KAFKA PRODUCER] Not connected, cannot send request");
            return null;
        }
        
        String key = request.getToUsername();  // Route by recipient
        String value = request.toJson();
        
        ProducerRecord<String, String> record = new ProducerRecord<>(
            KafkaConfig.TOPIC_FRIEND_REQUESTS,
            key,
            value
        );
        
        System.out.println("[KAFKA PRODUCER] Sending friend request to topic: " + KafkaConfig.TOPIC_FRIEND_REQUESTS);
        System.out.println("[KAFKA PRODUCER] Key: " + key + ", Value: " + value);
        
        return producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("[KAFKA PRODUCER] Failed to send request: " + exception.getMessage());
                isConnected = false;
            } else {
                System.out.println("[KAFKA PRODUCER] ✓ Request sent successfully!");
                System.out.println("[KAFKA PRODUCER]   Topic: " + metadata.topic());
                System.out.println("[KAFKA PRODUCER]   Partition: " + metadata.partition());
                System.out.println("[KAFKA PRODUCER]   Offset: " + metadata.offset());
                isConnected = true;
            }
        });
    }
    
    /**
     * Send a friend response event (accept/reject)
     */
    public Future<RecordMetadata> sendFriendResponse(FriendRequest response) {
        if (producer == null || !isConnected) {
            System.err.println("[KAFKA PRODUCER] Not connected, cannot send response");
            return null;
        }
        
        String key = response.getToUsername();  // Route by recipient
        String value = response.toJson();
        
        ProducerRecord<String, String> record = new ProducerRecord<>(
            KafkaConfig.TOPIC_FRIEND_RESPONSES,
            key,
            value
        );
        
        System.out.println("[KAFKA PRODUCER] Sending friend response to topic: " + KafkaConfig.TOPIC_FRIEND_RESPONSES);
        
        return producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("[KAFKA PRODUCER] Failed to send response: " + exception.getMessage());
                isConnected = false;
            } else {
                System.out.println("[KAFKA PRODUCER] ✓ Response sent successfully!");
                System.out.println("[KAFKA PRODUCER]   Topic: " + metadata.topic());
                System.out.println("[KAFKA PRODUCER]   Partition: " + metadata.partition());
                System.out.println("[KAFKA PRODUCER]   Offset: " + metadata.offset());
                isConnected = true;
            }
        });
    }
    
    /**
     * Send friend online/offline status update
     */
    public void sendStatusUpdate(String username, boolean online, String currentRoom) {
        if (producer == null) return;
        
        String key = username;
        String value = String.format(
            "{\"username\":\"%s\",\"online\":%b,\"room\":\"%s\",\"timestamp\":%d}",
            username, online, currentRoom != null ? currentRoom : "", System.currentTimeMillis()
        );
        
        ProducerRecord<String, String> record = new ProducerRecord<>(
            KafkaConfig.TOPIC_FRIEND_STATUS,
            key,
            value
        );
        
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("[KAFKA PRODUCER] Failed to send status: " + exception.getMessage());
            }
        });
    }
    
    public boolean isConnected() {
        return isConnected && producer != null;
    }
    
    public void flush() {
        if (producer != null) {
            producer.flush();
        }
    }
    
    public void close() {
        if (producer != null) {
            System.out.println("[KAFKA PRODUCER] Closing...");
            producer.flush();
            producer.close();
            producer = null;
            isConnected = false;
            System.out.println("[KAFKA PRODUCER] Closed");
        }
    }
}

