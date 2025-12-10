package service.kafka;

/**
 * Kafka 4.1 configuration constants
 * KRaft mode - No ZooKeeper required!
 * 
 * Part of Service Layer - handles external system integration
 */
public class KafkaConfig {
    
    // Kafka broker address (KRaft mode via Docker)
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    
    // Topics for friend system
    public static final String TOPIC_FRIEND_REQUESTS = "futura-friend-requests";
    public static final String TOPIC_FRIEND_RESPONSES = "futura-friend-responses";
    public static final String TOPIC_FRIEND_STATUS = "futura-friend-status";
    
    // Topics for room system (future use)
    public static final String TOPIC_ROOM_EVENTS = "futura-room-events";
    public static final String TOPIC_ROOM_CHAT = "futura-room-chat";
    
    // Consumer group prefix (each player gets unique group)
    public static final String CONSUMER_GROUP_PREFIX = "futura-client-";
    
    // Kafka 4.1 specific settings
    public static final int REQUEST_TIMEOUT_MS = 30000;
    public static final int SESSION_TIMEOUT_MS = 45000;
    public static final int HEARTBEAT_INTERVAL_MS = 3000;
    
    // Retry settings
    public static final int MAX_RETRIES = 3;
    public static final int RETRY_BACKOFF_MS = 1000;
}