package friend;

import main.GamePanel;
import kafka.FriendEventProducer;

import java.io.*;
import java.util.*;

/**
 * Manages friends list and friend requests
 * Handles persistence to file and Kafka communication
 */
public class FriendManager {
    
    private GamePanel gp;
    private String playerUsername;
    private List<Friend> friends;
    private List<FriendRequest> pendingRequests;  // Requests we sent, waiting for response
    private List<FriendRequest> incomingRequests; // Requests from others
    private FriendEventProducer kafkaProducer;
    
    private static final String FRIENDS_DIR = "friends/";
    
    public FriendManager(GamePanel gp, String playerUsername) {
        this.gp = gp;
        this.playerUsername = playerUsername;
        this.friends = new ArrayList<>();
        this.pendingRequests = new ArrayList<>();
        this.incomingRequests = new ArrayList<>();
        
        // Create friends directory if it doesn't exist
        new File(FRIENDS_DIR).mkdirs();
        
        // Load friends from file
        loadFriends();
        
        // Initialize Kafka producer
        initializeKafka();
    }
    
    private void initializeKafka() {
        try {
            this.kafkaProducer = new FriendEventProducer();
            System.out.println("[FRIEND MANAGER] Kafka producer initialized for: " + playerUsername);
        } catch (Exception e) {
            System.err.println("[FRIEND MANAGER] Failed to initialize Kafka: " + e.getMessage());
            System.err.println("[FRIEND MANAGER] Friend requests will use server fallback");
            this.kafkaProducer = null;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // SEND FRIEND REQUEST
    // ═══════════════════════════════════════════════════════════
    
    public boolean sendFriendRequest(String targetUsername, String targetGender) {
        // Can't friend yourself
        if (targetUsername.equalsIgnoreCase(playerUsername)) {
            System.out.println("[FRIEND MANAGER] Cannot send friend request to yourself!");
            return false;
        }
        
        // Check if already friends
        if (isFriend(targetUsername)) {
            System.out.println("[FRIEND MANAGER] Already friends with: " + targetUsername);
            return false;
        }
        
        // Check if request already pending
        for (FriendRequest req : pendingRequests) {
            if (req.getToUsername().equalsIgnoreCase(targetUsername)) {
                System.out.println("[FRIEND MANAGER] Request already pending for: " + targetUsername);
                return false;
            }
        }
        
        // Create request
        FriendRequest request = new FriendRequest(
            playerUsername,
            targetUsername,
            gp.player.gender.toString(),
            FriendRequest.RequestType.SEND_REQUEST
        );
        
        // Send via Kafka
        if (kafkaProducer != null && kafkaProducer.isConnected()) {
            kafkaProducer.sendFriendRequest(request);
            pendingRequests.add(request);
            System.out.println("[FRIEND MANAGER] Friend request sent via Kafka to: " + targetUsername);
            return true;
        } else {
            // Fallback: Send via game server
            if (gp.networkManager != null && gp.networkManager.isConnected()) {
                gp.networkManager.sendFriendRequest(targetUsername);
                pendingRequests.add(request);
                System.out.println("[FRIEND MANAGER] Friend request sent via server to: " + targetUsername);
                return true;
            }
        }
        
        System.err.println("[FRIEND MANAGER] Failed to send request - no connection");
        return false;
    }
    
    // ═══════════════════════════════════════════════════════════
    // RECEIVE FRIEND REQUEST
    // ═══════════════════════════════════════════════════════════
    
    public void receiveRequest(FriendRequest request) {

          String fromUser = request.getFromUsername();

    // Check if we already sent THEM a request (mutual request!)
    // FIX: Loop through pendingRequests properly
    boolean isMutual = false;
    FriendRequest ourRequest = null;
    for (FriendRequest req : pendingRequests) {
        if (req.getToUsername().equalsIgnoreCase(fromUser)) {
            isMutual = true;
            ourRequest = req;
            break;
        }
    }
    
    if (isMutual) {
        System.out.println("[FRIEND MANAGER] 🎉 Mutual friend request! Auto-accepting...");
        
        // Remove from our pending
        pendingRequests.remove(ourRequest);
        
        // Add as friend directly
        Friend newFriend = new Friend(fromUser, request.getFromGender());
        friends.add(newFriend);
        saveFriends();
        
        // Notify user
        gp.showFriendNotification("You and " + fromUser + " are now friends!", true);
        
        // Refresh panel
        if (gp.friendsPanel != null) {
            gp.friendsPanel.refresh();
        }
        return;
    }

        // Don't receive requests from ourselves
        if (request.getFromUsername().equalsIgnoreCase(playerUsername)) {
            return;
        }
        
        // Don't add duplicate requests
        for (FriendRequest req : incomingRequests) {
            if (req.getFromUsername().equalsIgnoreCase(request.getFromUsername())) {
                System.out.println("[FRIEND MANAGER] Duplicate request ignored from: " + request.getFromUsername());
                return;
            }
        }
        
        // Check if already friends
        if (isFriend(request.getFromUsername())) {
            System.out.println("[FRIEND MANAGER] Already friends with: " + request.getFromUsername());
            return;
        }
        
        incomingRequests.add(request);
        System.out.println("[FRIEND MANAGER] Received friend request from: " + request.getFromUsername());
        
        // Trigger popup in game
        if (gp != null) {
            gp.showFriendRequestPopup(request);
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // RESPOND TO FRIEND REQUEST
    // ═══════════════════════════════════════════════════════════
    
    public void acceptRequest(String fromUsername) {
        FriendRequest original = removeIncomingRequest(fromUsername);
        if (original == null) {
            System.err.println("[FRIEND MANAGER] No request found from: " + fromUsername);
            return;
        }
        
        // Add to friends list
        Friend newFriend = new Friend(fromUsername, original.getFromGender());
        if (!friends.contains(newFriend)) {
            friends.add(newFriend);
            saveFriends();
        }
        
        // Send acceptance via Kafka
        FriendRequest response = new FriendRequest(
            playerUsername,
            fromUsername,
            gp.player.gender.toString(),
            FriendRequest.RequestType.ACCEPT_REQUEST
        );
        
        if (kafkaProducer != null && kafkaProducer.isConnected()) {
            kafkaProducer.sendFriendResponse(response);
        } else if (gp.networkManager != null && gp.networkManager.isConnected()) {
            gp.networkManager.sendFriendResponse(fromUsername, true);
        }
        
        System.out.println("[FRIEND MANAGER] Accepted friend request from: " + fromUsername);
        
        // Show confirmation
        gp.showFriendNotification("You are now friends with " + fromUsername + "!", true);
    }
    
    public void rejectRequest(String fromUsername) {
        FriendRequest original = removeIncomingRequest(fromUsername);
        if (original == null) {
            System.err.println("[FRIEND MANAGER] No request found from: " + fromUsername);
            return;
        }
        
        // Send rejection via Kafka
        FriendRequest response = new FriendRequest(
            playerUsername,
            fromUsername,
            gp.player.gender.toString(),
            FriendRequest.RequestType.REJECT_REQUEST
        );
        
        if (kafkaProducer != null && kafkaProducer.isConnected()) {
            kafkaProducer.sendFriendResponse(response);
        } else if (gp.networkManager != null && gp.networkManager.isConnected()) {
            gp.networkManager.sendFriendResponse(fromUsername, false);
        }
        
        System.out.println("[FRIEND MANAGER] Rejected friend request from: " + fromUsername);
    }
    
    // ═══════════════════════════════════════════════════════════
    // HANDLE RESPONSE (when other player accepts/rejects our request)
    // ═══════════════════════════════════════════════════════════
    
    public void handleResponse(FriendRequest response) {
        String otherPlayer = response.getFromUsername();
        
        // Remove from pending
        pendingRequests.removeIf(req -> req.getToUsername().equalsIgnoreCase(otherPlayer));
        
        if (response.getType() == FriendRequest.RequestType.ACCEPT_REQUEST) {
            // They accepted - add to friends
            Friend newFriend = new Friend(otherPlayer, response.getFromGender());
            if (!friends.contains(newFriend)) {
                friends.add(newFriend);
                saveFriends();
            }
            System.out.println("[FRIEND MANAGER] " + otherPlayer + " accepted your friend request!");
            
            // Show notification
            if (gp != null) {
                gp.showFriendNotification(otherPlayer + " accepted your friend request!", true);
            }
        } else if (response.getType() == FriendRequest.RequestType.REJECT_REQUEST) {
            System.out.println("[FRIEND MANAGER] " + otherPlayer + " rejected your friend request");
            
            // Show notification
            if (gp != null) {
                gp.showFriendNotification(otherPlayer + " rejected your friend request", false);
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // FRIEND LIST OPERATIONS
    // ═══════════════════════════════════════════════════════════
    
    public boolean isFriend(String username) {
        for (Friend f : friends) {
            if (f.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
    
    public void removeFriend(String username) {
        friends.removeIf(f -> f.getUsername().equalsIgnoreCase(username));
        saveFriends();
        System.out.println("[FRIEND MANAGER] Removed friend: " + username);
    }
    
    public List<Friend> getFriends() {
        return new ArrayList<>(friends);
    }
    
    public List<FriendRequest> getIncomingRequests() {
        return new ArrayList<>(incomingRequests);
    }
    
    public List<FriendRequest> getPendingRequests() {
        return new ArrayList<>(pendingRequests);
    }
    
    public int getFriendCount() {
        return friends.size();
    }
    
    // ═══════════════════════════════════════════════════════════
    // UPDATE FRIEND STATUS (online/offline)
    // ═══════════════════════════════════════════════════════════
    
    public void updateFriendStatus(String username, boolean online, String room) {
        for (Friend f : friends) {
            if (f.getUsername().equalsIgnoreCase(username)) {
                f.setOnline(online);
                f.setCurrentRoom(room);
                System.out.println("[FRIEND MANAGER] Updated " + username + " status: " + 
                                  (online ? "online" : "offline"));
                break;
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════
    
    private String getFriendsFilePath() {
        return FRIENDS_DIR + "friends_" + playerUsername + ".dat";
    }
    
    public void saveFriends() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFriendsFilePath()))) {
            for (Friend friend : friends) {
                writer.write(friend.serialize());
                writer.newLine();
            }
            System.out.println("[FRIEND MANAGER] Saved " + friends.size() + " friends to file");
        } catch (IOException e) {
            System.err.println("[FRIEND MANAGER] Failed to save friends: " + e.getMessage());
        }
    }
    
    public void loadFriends() {
        File file = new File(getFriendsFilePath());
        if (!file.exists()) {
            System.out.println("[FRIEND MANAGER] No friends file found for: " + playerUsername);
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Friend friend = Friend.deserialize(line);
                    friends.add(friend);
                }
            }
            System.out.println("[FRIEND MANAGER] Loaded " + friends.size() + " friends from file");
        } catch (IOException e) {
            System.err.println("[FRIEND MANAGER] Failed to load friends: " + e.getMessage());
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════
    
    private FriendRequest removeIncomingRequest(String fromUsername) {
        for (int i = 0; i < incomingRequests.size(); i++) {
            if (incomingRequests.get(i).getFromUsername().equalsIgnoreCase(fromUsername)) {
                return incomingRequests.remove(i);
            }
        }
        return null;
    }
    
    public void shutdown() {
        System.out.println("[FRIEND MANAGER] Shutting down...");
        if (kafkaProducer != null) {
            kafkaProducer.close();
        }
        saveFriends();
    }
}

