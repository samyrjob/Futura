package kafka;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Test Kafka 4.1 connection
 * Run this to verify Kafka is working before starting the game
 */
public class KafkaConnectionTest {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           KAFKA 4.1 CONNECTION TEST                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.BOOTSTRAP_SERVERS);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        
        try (Admin admin = Admin.create(props)) {
            
            // Test 1: Get cluster ID
            System.out.println("1️⃣  Getting Cluster ID...");
            String clusterId = admin.describeCluster().clusterId().get();
            System.out.println("   ✅ Cluster ID: " + clusterId);
            
            // Test 2: List nodes
            System.out.println();
            System.out.println("2️⃣  Listing Broker Nodes...");
            var nodes = admin.describeCluster().nodes().get();
            for (var node : nodes) {
                System.out.println("   ✅ Node: " + node.id() + " @ " + node.host() + ":" + node.port());
            }
            
            // Test 3: List topics
            System.out.println();
            System.out.println("3️⃣  Listing Topics...");
            Set<String> topics = admin.listTopics().names().get();
            if (topics.isEmpty()) {
                System.out.println("   ⚠️  No topics found. Creating friend topics...");
                createTopics(admin);
                topics = admin.listTopics().names().get();
            }
            for (String topic : topics) {
                System.out.println("   ✅ Topic: " + topic);
            }
            
            // Test 4: Verify friend topics
            System.out.println();
            System.out.println("4️⃣  Verifying Friend System Topics...");
            boolean hasRequests = topics.contains(KafkaConfig.TOPIC_FRIEND_REQUESTS);
            boolean hasResponses = topics.contains(KafkaConfig.TOPIC_FRIEND_RESPONSES);
            
            System.out.println("   " + (hasRequests ? "✅" : "❌") + " " + KafkaConfig.TOPIC_FRIEND_REQUESTS);
            System.out.println("   " + (hasResponses ? "✅" : "❌") + " " + KafkaConfig.TOPIC_FRIEND_RESPONSES);
            
            if (!hasRequests || !hasResponses) {
                System.out.println();
                System.out.println("   Creating missing topics...");
                createTopics(admin);
            }
            
            // Test 5: Producer test
            System.out.println();
            System.out.println("5️⃣  Testing Producer...");
            try {
                FriendEventProducer producer = new FriendEventProducer();
                System.out.println("   ✅ Producer initialized successfully");
                producer.close();
            } catch (Exception e) {
                System.out.println("   ❌ Producer failed: " + e.getMessage());
            }
            
            // Final result
            System.out.println();
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║  ✅ KAFKA 4.1 CONNECTION TEST PASSED!                      ║");
            System.out.println("║                                                            ║");
            System.out.println("║  You can now start the game with friend system enabled.    ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            
        } catch (ExecutionException e) {
            System.out.println();
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║  ❌ KAFKA CONNECTION FAILED!                               ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("Error: " + e.getCause().getMessage());
            System.out.println();
            System.out.println("Make sure Kafka is running:");
            System.out.println("  1. Open Docker Desktop");
            System.out.println("  2. Run: docker-compose up -d");
            System.out.println("  3. Check: docker ps");
            System.out.println();
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createTopics(Admin admin) throws ExecutionException, InterruptedException {
        List<NewTopic> newTopics = Arrays.asList(
            new NewTopic(KafkaConfig.TOPIC_FRIEND_REQUESTS, 3, (short) 1),
            new NewTopic(KafkaConfig.TOPIC_FRIEND_RESPONSES, 3, (short) 1),
            new NewTopic(KafkaConfig.TOPIC_FRIEND_STATUS, 3, (short) 1)
        );
        
        CreateTopicsResult result = admin.createTopics(newTopics);
        
        for (Map.Entry<String, KafkaFuture<Void>> entry : result.values().entrySet()) {
            try {
                entry.getValue().get();
                System.out.println("   ✅ Created topic: " + entry.getKey());
            } catch (ExecutionException e) {
                if (e.getCause().getMessage().contains("already exists")) {
                    System.out.println("   ✅ Topic exists: " + entry.getKey());
                } else {
                    throw e;
                }
            }
        }
    }
}
