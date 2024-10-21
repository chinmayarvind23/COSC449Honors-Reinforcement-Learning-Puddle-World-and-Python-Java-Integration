package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// This class takes RLGameUser instances and binds them with RLWorld intances allowing for concurrent access
public class RLGameManager {

    // Thread-safe map to store User and their corresponding RLGameUser
    private final Map<String, RLGameUser> userMap;
    private double alpha;
    private double gamma;
    private double epsilon;

    // RLGameManager constructor that initializes a concurrent hashmap to store the user and their corresponding world instances distinctly from other users' instances
    public RLGameManager() {
        this.userMap = new ConcurrentHashMap<String, RLGameUser>();
        this.alpha = 0.1;
        this.gamma = 0.9;
        this.epsilon = 1.0;
    }

    // Adds a user to an RL puddle world game by creating an RL world and binding the RLGameUser to it
    // Then inserting the pair into the hashmap
    public boolean addUser(User user) {
        String userName = user.getName();
        if (userMap.containsKey(userName)) {
            System.out.println("Attempted to add user who already exists: " + userName);
            return false;
        }
    
        RLWorld world = new RLWorld(user, alpha, gamma, epsilon);
        RLGameUser rlUser = new RLGameUser(user, world);
        userMap.put(userName, rlUser);
        System.out.println("RLGameUser created for user: " + userName);
        System.out.println("Current number of active users: " + userMap.size());
        return true;
    }

    // Removes a user from an RL puddle world game and cleans up the user removed
    public boolean removeUser(User user) {
        String userName = user.getName();
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
        String userName = user.getName();
        RLGameUser rlUser = userMap.get(userName);
        if (rlUser == null) {
            System.out.println("User not found in userMap: " + userName);
            System.out.println("Current users in userMap:");
            for (String uName : userMap.keySet()) {
                System.out.println(" - " + uName);
            }
        }
        return rlUser;
    }

    // Check if an RLWorld instance has a user
    public boolean hasUser(User user) {
        return userMap.containsKey(user.getName());
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