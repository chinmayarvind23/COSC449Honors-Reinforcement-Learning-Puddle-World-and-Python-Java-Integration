package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// This class takes RLGameUser instances and binds them with RLWorld intances allowing for concurrent access
public class RLGameManager {

    // Thread-safe map to store User and their corresponding RLGameUser
    private final Map<User, RLGameUser> userMap;
    private double alpha;
    private double gamma;
    private double epsilon;

    // RLGameManager constructor that initializes a concurrent hashmap to store the user and their corresponding world instances distinctly from other users' instances
    public RLGameManager() {
        this.userMap = new ConcurrentHashMap<>();
        this.alpha = 0.1;
        this.gamma = 0.9;
        this.epsilon = 1.0;
    }

    // Adds a user to an RL puddle world game by creating an RL world and binding the RLGameUser to it
    // Then inserting the pair into the hashmap
    public boolean addUser(User user) {
        if (userMap.containsKey(user)) {
            System.out.println("Attempted to add user who already exists: " + user.getName());
            return false;
        }

        RLWorld world = new RLWorld(user, alpha, gamma, epsilon);
        RLGameUser rlUser = new RLGameUser(user, world);
        userMap.put(user, rlUser);
        System.out.println("RLGameUser created for user: " + user.getName());
        System.out.println("Current number of active users: " + userMap.size());
        return true;
    }

    // Removes a user from an RL puddle world game and cleans up the user removed
    public boolean removeUser(User user) {
        RLGameUser removedUser = userMap.remove(user);
        if (removedUser != null) {
            removedUser.cleanup();
            System.out.println("RLGameUser removed for user: " + user.getName());
            System.out.println("Current number of active users: " + userMap.size());
            return true;
        } else {
            System.out.println("Attempted to remove non-existent user: " + user.getName());
            return false;
        }
    }

    // Gets a user from the hashmap
    public RLGameUser getUser(User user) {
        return userMap.get(user);
    }

    // Check if an RLWorld instance has a user
    public boolean hasUser(User user) {
        return userMap.containsKey(user);
    }

    // Gets a list of all active users in the hashmap
    public List<User> getAllUsers() {
        return userMap.keySet().stream().collect(Collectors.toList());
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