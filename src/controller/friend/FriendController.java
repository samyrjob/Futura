package controller.friend;

import main.GamePanel;
import model.friend.Friend;
import model.friend.FriendRequest;
import model.friend.FriendRequest.RequestType;
import service.kafka.FriendKafkaService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller for Friend operations.
 * Handles all business logic: sending/receiving requests, managing friend list.
 * 
 * Part of MVC architecture - this is the Controller layer (business logic).
 * 
 * Responsibilities:
 * - Validate friend operations
 * - Manage friend/request lists
 * - Coordinate with repository (persistence)
 * - Coordinate with Kafka service (messaging)
 * - Notify listeners of changes
 */
public class FriendController {
    
    // ═══════════════════════════════════════════════════════════
    // DEPENDENCIES
    // ═══════════════════════════════════════════════════════════
    
    private final GamePanel gp;
    private final FriendRepository repository;
    private final FriendKafkaService kafkaService;
    
    // ═══════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════
    
    private final String playerUsername;
    private final String playerGender;
    
    private final List<Friend> friends;
    private final List<FriendRequest> pendingRequests;   // Requests we sent
    private final List<FriendRequest> incomingRequests;  // Requests from others
    
    // ═══════════════════════════════════════════════════════════
    // LISTENERS (Observer pattern for UI updates)
    // ═══════════════════════════════════════════════════════════
    
    private Consumer<Friend> onFriendAdded;
    private Consumer<Friend> onFriendRemoved;
    private Consumer<FriendRequest> onRequestReceived;
    private Consumer<FriendRequest> onRequestAccepted;
    private Consumer<FriendRequest> onRequestRejected;
    private Runnable onListChanged;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════
    
    public FriendController(GamePanel gp, String playerUsername, String playerGender) {
        this.gp = gp;
        this.playerUsername = playerUsername;
        this.playerGender = playerGender;
        
        this.friends = new ArrayList<>();
        this.pendingRequests = new ArrayList<>();
        this.incomingRequests = new ArrayList<>();
        
        // Initialize repository
        this.repository = new FriendRepository(playerUsername);
        
        // Initialize Kafka service
        this.kafkaService = new FriendKafkaService(this, playerUsername);
        
        // Load saved friends
        loadFriends();
        
        System.out.println("[FRIEND CTRL] Initialized for: " + playerUsername);
    }
    
    // ═══════════════════════════════════════════════════════════
    // LISTENER SETTERS (Observer pattern)
    // ═══════════════════════════════════════════════════════════
    
    public void setOnFriendAdded(Consumer<Friend> listener) {
        this.onFriendAdded = listener;
    }
    
    public void setOnFriendRemoved(Consumer<Friend> listener) {
        this.onFriendRemoved = listener;
    }
    
    public void setOnRequestReceived(Consumer<FriendRequest> listener) {
        this.onRequestReceived = listener;
    }
    
    public void setOnRequestAccepted(Consumer<FriendRequest> listener) {
        this.onRequestAccepted = listener;
    }
    
    public void setOnRequestRejected(Consumer<FriendRequest> listener) {
        this.onRequestRejected = listener;
    }
    
    public void setOnListChanged(Runnable listener) {
        this.onListChanged = listener;
    }
    
    // ═══════════════════════════════════════════════════════════
    // SEND FRIEND REQUEST
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Send a friend request to another player
     * 
     * @param targetUsername Username to send request to
     * @param targetGender Gender of target (for display)
     * @return true if request was sent successfully
     */
    public boolean sendFriendRequest(String targetUsername, String targetGender) {
        // Validation
        if (targetUsername.equalsIgnoreCase(playerUsername)) {
            System.out.println("[FRIEND CTRL] Cannot send friend request to yourself!");
            return false;
        }
        
        if (isFriend(targetUsername)) {
            System.out.println("[FRIEND CTRL] Already friends with: " + targetUsername);
            return false;
        }
        
        if (hasPendingRequestTo(targetUsername)) {
            System.out.println("[FRIEND CTRL] Request already pending for: " + targetUsername);
            return false;
        }
        
        // Create request
        FriendRequest request = new FriendRequest(
            playerUsername,
            targetUsername,
            playerGender,
            RequestType.SEND_REQUEST
        );
        
        // Send via Kafka (or fallback to server)
        boolean sent = kafkaService.sendRequest(request);
        
        if (!sent && gp.networkManager != null && gp.networkManager.isConnected()) {
            gp.networkManager.sendFriendRequest(targetUsername);
            sent = true;
        }
        
        if (sent) {
            pendingRequests.add(request);
            System.out.println("[FRIEND CTRL] Friend request sent to: " + targetUsername);
            return true;
        }
        
        System.err.println("[FRIEND CTRL] Failed to send request - no connection");
        return false;
    }
    
    // ═══════════════════════════════════════════════════════════
    // RECEIVE FRIEND REQUEST
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Handle receiving a friend request from another player
     * Called by Kafka consumer or network manager
     */
    public void receiveRequest(FriendRequest request) {
        String fromUser = request.getFromUsername();
        
        // Don't receive requests from ourselves
        if (fromUser.equalsIgnoreCase(playerUsername)) {
            return;
        }
        
        // Check if already friends
        if (isFriend(fromUser)) {
            System.out.println("[FRIEND CTRL] Already friends with: " + fromUser);
            return;
        }
        
        // Check for mutual request (we already sent them a request!)
        FriendRequest ourRequest = findPendingRequestTo(fromUser);
        if (ourRequest != null) {
            System.out.println("[FRIEND CTRL] 🎉 Mutual friend request! Auto-accepting...");
            handleMutualRequest(request, ourRequest);
            return;
        }
        
        // Check for duplicate
        if (hasIncomingRequestFrom(fromUser)) {
            System.out.println("[FRIEND CTRL] Duplicate request ignored from: " + fromUser);
            return;
        }
        
        // Add to incoming requests
        incomingRequests.add(request);
        System.out.println("[FRIEND CTRL] Received friend request from: " + fromUser);
        
        // Notify listener
        if (onRequestReceived != null) {
            onRequestReceived.accept(request);
        }
    }
    
    private void handleMutualRequest(FriendRequest theirRequest, FriendRequest ourRequest) {
        // Remove our pending request
        pendingRequests.remove(ourRequest);
        
        // Add as friend directly
        Friend newFriend = new Friend(theirRequest.getFromUsername(), theirRequest.getFromGender());
        addFriendInternal(newFriend);
        
        // Notify
        if (onFriendAdded != null) {
            onFriendAdded.accept(newFriend);
        }
        notifyListChanged();
    }
    
    // ═══════════════════════════════════════════════════════════
    // RESPOND TO FRIEND REQUEST
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Accept a friend request
     */
    public void acceptRequest(String fromUsername) {
        FriendRequest original = removeIncomingRequest(fromUsername);
        if (original == null) {
            System.err.println("[FRIEND CTRL] No request found from: " + fromUsername);
            return;
        }
        
        // Add to friends list
        Friend newFriend = new Friend(fromUsername, original.getFromGender());
        addFriendInternal(newFriend);
        
        // Send acceptance response
        FriendRequest response = new FriendRequest(
            playerUsername,
            fromUsername,
            playerGender,
            RequestType.ACCEPT_REQUEST
        );
        
        boolean sent = kafkaService.sendResponse(response);
        if (!sent && gp.networkManager != null && gp.networkManager.isConnected()) {
            gp.networkManager.sendFriendResponse(fromUsername, true);
        }
        
        System.out.println("[FRIEND CTRL] Accepted friend request from: " + fromUsername);
        
        // Notify listeners
        if (onFriendAdded != null) {
            onFriendAdded.accept(newFriend);
        }
        if (onRequestAccepted != null) {
            onRequestAccepted.accept(original);
        }
        notifyListChanged();
    }
    
    /**
     * Reject a friend request
     */
    public void rejectRequest(String fromUsername) {
        FriendRequest original = removeIncomingRequest(fromUsername);
        if (original == null) {
            System.err.println("[FRIEND CTRL] No request found from: " + fromUsername);
            return;
        }
        
        // Send rejection response
        FriendRequest response = new FriendRequest(
            playerUsername,
            fromUsername,
            playerGender,
            RequestType.REJECT_REQUEST
        );
        
        boolean sent = kafkaService.sendResponse(response);
        if (!sent && gp.networkManager != null && gp.networkManager.isConnected()) {
            gp.networkManager.sendFriendResponse(fromUsername, false);
        }
        
        System.out.println("[FRIEND CTRL] Rejected friend request from: " + fromUsername);
        
        // Notify listener
        if (onRequestRejected != null) {
            onRequestRejected.accept(original);
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // HANDLE RESPONSE (other player accepted/rejected our request)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Handle response to our friend request
     * Called by Kafka consumer or network manager
     */
    public void handleResponse(FriendRequest response) {
        String otherPlayer = response.getFromUsername();
        
        // Remove from pending
        pendingRequests.removeIf(req -> req.getToUsername().equalsIgnoreCase(otherPlayer));
        
        if (response.getType() == RequestType.ACCEPT_REQUEST) {
            // They accepted - add to friends
            Friend newFriend = new Friend(otherPlayer, response.getFromGender());
            addFriendInternal(newFriend);
            
            System.out.println("[FRIEND CTRL] " + otherPlayer + " accepted your friend request!");
            
            if (onFriendAdded != null) {
                onFriendAdded.accept(newFriend);
            }
        } else if (response.getType() == RequestType.REJECT_REQUEST) {
            System.out.println("[FRIEND CTRL] " + otherPlayer + " rejected your friend request");
        }
        
        notifyListChanged();
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
        Friend removed = null;
        for (Friend f : friends) {
            if (f.getUsername().equalsIgnoreCase(username)) {
                removed = f;
                break;
            }
        }
        
        if (removed != null) {
            friends.remove(removed);
            saveFriends();
            System.out.println("[FRIEND CTRL] Removed friend: " + username);
            
            if (onFriendRemoved != null) {
                onFriendRemoved.accept(removed);
            }
            notifyListChanged();
        }
    }
    
    public void updateFriendStatus(String username, boolean online, String room) {
        for (Friend f : friends) {
            if (f.getUsername().equalsIgnoreCase(username)) {
                f.setOnline(online);
                f.setCurrentRoom(room);
                System.out.println("[FRIEND CTRL] Updated " + username + " status: " + 
                                  (online ? "online in " + room : "offline"));
                notifyListChanged();
                break;
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS (Read-only access)
    // ═══════════════════════════════════════════════════════════
    
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
    
    public int getIncomingRequestCount() {
        return incomingRequests.size();
    }
    
    public String getPlayerUsername() {
        return playerUsername;
    }
    
    // ═══════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════
    
    private void loadFriends() {
        List<Friend> loaded = repository.load();
        friends.clear();
        friends.addAll(loaded);
    }
    
    private void saveFriends() {
        repository.save(friends);
    }
    
    // ═══════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Shutdown the controller (save data, close connections)
     */
    public void shutdown() {
        System.out.println("[FRIEND CTRL] Shutting down...");
        kafkaService.shutdown();
        saveFriends();
    }
    
    // ═══════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════
    
    private void addFriendInternal(Friend friend) {
        if (!friends.contains(friend)) {
            friends.add(friend);
            saveFriends();
        }
    }
    
    private boolean hasPendingRequestTo(String username) {
        for (FriendRequest req : pendingRequests) {
            if (req.getToUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
    
    private FriendRequest findPendingRequestTo(String username) {
        for (FriendRequest req : pendingRequests) {
            if (req.getToUsername().equalsIgnoreCase(username)) {
                return req;
            }
        }
        return null;
    }
    
    private boolean hasIncomingRequestFrom(String username) {
        for (FriendRequest req : incomingRequests) {
            if (req.getFromUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
    
    private FriendRequest removeIncomingRequest(String fromUsername) {
        for (int i = 0; i < incomingRequests.size(); i++) {
            if (incomingRequests.get(i).getFromUsername().equalsIgnoreCase(fromUsername)) {
                return incomingRequests.remove(i);
            }
        }
        return null;
    }
    
    private void notifyListChanged() {
        if (onListChanged != null) {
            onListChanged.run();
        }
    }
}