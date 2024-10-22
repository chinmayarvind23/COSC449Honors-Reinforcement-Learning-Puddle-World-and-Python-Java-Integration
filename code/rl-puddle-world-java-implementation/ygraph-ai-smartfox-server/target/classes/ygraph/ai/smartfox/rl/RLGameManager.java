package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// This class takes RLGameUser instances and binds them with RLWorld intances allowing for concurrent access
public class RLGameManager {

    // Thread-safe map to store username and their corresponding RLGameUser
    private final Map<String, RLGameUser> userMap = new ConcurrentHashMap<>();
    private double alpha;
    private double gamma;
    private double epsilon;

    // RLGameManager constructor that initializes a concurrent hashmap to store the user and their corresponding world instances distinctly from other users' instances
    public RLGameManager() {
        this.alpha = 0.1;
        this.gamma = 0.9;
        this.epsilon = 1.0;
    }

    public RLGameManager(double alpha, double gamma, double epsilon) {
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon = epsilon;
        System.out.println("RLGameManager instantiated. Instance ID: " + System.identityHashCode(this));
    }

    // Adds a user to an RL puddle world game by creating an RL world and binding the RLGameUser to it
    // Then inserting the pair into the hashmap
    public synchronized boolean addUser(User user) {
        String userName = user.getName().trim().toLowerCase();
        System.out.println("Adding User: " + userName + " with memory reference: " + System.identityHashCode(user));

        if (userMap.containsKey(userName)) {
            System.out.println("Attempted to add user who already exists: " + userName);
            return false;
        }

        RLWorld world = new RLWorld(user, alpha, gamma, epsilon);
        RLGameUser rlUser = new RLGameUser(user, world);
        userMap.put(userName, rlUser);

        System.out.println("RLGameUser created for user: " + userName);
        System.out.println("User map contents after adding:");
        for (String name : userMap.keySet()) {
            System.out.println(" - " + name + " (equals agent1? " + name.equals("agent1") + ")");
        }

        RLGameUser addedUser = userMap.get(userName);
        if (addedUser == null) {
            System.out.println("User addition failed unexpectedly: " + userName);
            return false;
        }

        System.out.println("Current number of active users: " + userMap.size());
        return true;
    }    

    public RLGameUser getUserByUsername(String userName) {
        if (userName == null) return null;
        return userMap.get(userName.trim().toLowerCase());
    }

    // Removes a user from an RL puddle world game and cleans up the user removed
    public boolean removeUser(User user) {
        String userName = user.getName().trim().toLowerCase();
        RLGameUser removedUser = userMap.remove(userName);
        if (removedUser != null) {
            removedUser.cleanup();
            System.out.println("RLGameUser removed for user: " + userName);
            System.out.println("Current number of active users: " + userMap.size());
            return true;
        } else {
            System.out.println("Attempted to remove non-existent user: " + userName);
            return false;
        }
    }

    // Gets a user from the hashmap
    public synchronized RLGameUser getUser(User user) {
        String userName = user.getName().trim().toLowerCase();
        System.out.println("Attempting to retrieve user: " + userName + " with memory reference: " + System.identityHashCode(user));

        System.out.println("Current users in userMap:");
        for (String uName : userMap.keySet()) {
            System.out.println(" - " + uName + " (equals provided username? " + uName.equals(userName) + ")");
        }

        RLGameUser rlUser = userMap.get(userName);
        if (rlUser == null) {
            System.out.println("User not found in userMap: " + userName);
        } else {
            System.out.println("User found: " + userName);
        }
        return rlUser;
    }    

    // Check if an RLWorld instance has a user
    public boolean hasUser(User user) {
        return userMap.containsKey(user.getName().trim().toLowerCase());
    }    

    // Gets a list of all active users in the hashmap
    public List<User> getAllUsers() {
        return userMap.values().stream()
            .map(RLGameUser::getUser)
            .collect(Collectors.toList());
    }

    // Removes all users from their world instances (admin) and ensures that the user instances are cleaned up
    public void clearAllUsers() {
        for (RLGameUser rlUser : userMap.values()) {
            rlUser.cleanup();
        }
        userMap.clear();
        System.out.println("All RLGameUsers have been cleared from the game.");
    }

    // Gets the total number of active users over all the worlds in the hashmap
    public int getActiveUserCount() {
        return userMap.size();
    }
}