package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
// import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

// This class takes RLGameUser instances and binds them with RLWorld intances allowing for concurrent access
public class RLGameManager {
    private static final HashMap<String, String> ENV = loadEnv();
    // Thread-safe map to store username and their corresponding RLGameUser
    private final ConcurrentMap<String, RLGameUser> userMap = new ConcurrentHashMap<>();
    private double alpha;
    private double gamma;
    private double epsilon;

    // RLGameManager constructor that initializes a concurrent hashmap to store the user and their corresponding world instances distinctly from other users' instances
    public RLGameManager() {
        this.alpha = Double.parseDouble(ENV.getOrDefault("ALPHA", "0.1"));
        this.gamma = Double.parseDouble(ENV.getOrDefault("GAMMA", "0.9"));
        this.epsilon = Double.parseDouble(ENV.getOrDefault("EPSILON", "1"));
    }

    public RLGameManager(double alpha, double gamma, double epsilon) {
        this.alpha = Double.parseDouble(ENV.getOrDefault("ALPHA", "0.1"));
        this.gamma = Double.parseDouble(ENV.getOrDefault("GAMMA", "0.9"));
        this.epsilon = Double.parseDouble(ENV.getOrDefault("EPSILON", "1"));
        System.out.println("RLGameManager instantiated. Instance ID: " + System.identityHashCode(this));
    }

    private static HashMap<String, String> loadEnv() {
        HashMap<String, String> env = new HashMap<>();
        String workingDir = System.getProperty("user.dir");
        System.out.println("Current Working Directory: " + workingDir);
        try (BufferedReader br = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    env.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read .env file: " + e.getMessage());
        }
        return env;
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
        RLGameUser existing = userMap.putIfAbsent(userName, rlUser);
    
        if (existing != null) {
            System.out.println("User already exists after putIfAbsent: " + userName);
            return false;
        }
    
        System.out.println("RLGameUser created for user: " + userName);
        System.out.println("User map contents after adding:");
        for (String name : userMap.keySet()) {
            System.out.println(" - " + name + " (equals agent1? " + name.equals("agent1") + ")");
        }
    
        System.out.println("Current number of active users: " + userMap.size());
        return true;
    }    

    public RLGameUser getUserByUsername(String userName) {
        if (userName == null) return null;
        return userMap.get(userName.trim().toLowerCase());
    }

    // Removes a user from an RL puddle world game and cleans up the user removed
    public synchronized boolean removeUser(User user) {
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
    public RLGameUser getUser(User user) {
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