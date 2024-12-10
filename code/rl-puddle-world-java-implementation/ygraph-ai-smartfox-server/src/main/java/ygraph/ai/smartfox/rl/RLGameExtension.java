package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// This class creates the RL environment within the server and sets up request handler to handle client-related RL logic
public class RLGameExtension extends SFSExtension {

    private RLGameManager gameManager;
    private ScheduledExecutorService scheduler;
    private Set<String> currentUsers;

    @Override
    public void init() {
        System.out.println("RLGameExtension initialized for COSC 322.");

        // Initialize RLGameManager
        gameManager = new RLGameManager(0.1, 0.9, 1.0);
        System.out.println("RLGameManager initialized in RLGameExtension. Instance ID: " + System.identityHashCode(gameManager));

        // Register request handler
        addRequestHandler("rl.action", new RLGameRequestHandler(gameManager));
        currentUsers = new HashSet<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::checkUserList, 0, 5, TimeUnit.SECONDS);
    }

    // Extension killer post game finish
    @Override
    public void destroy() {
        System.out.println("RLGameExtension destroyed for COSC 322.");
        gameManager.clearAllUsers();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }

        super.destroy();
    }

    // Check and update RLGameManager
    private void checkUserList() {
        try {
            Room room = getParentZone().getRoomByName("RLRoom");
            if (room == null) {
                System.out.println("RLRoom does not exist.");
                return;
            }

            Set<String> usersInRoom = new HashSet<>();
            for (User user : room.getUserList()) {
                usersInRoom.add(user.getName().trim().toLowerCase());
            }

            // Finds users to keep and remove
            Set<String> usersToAdd = new HashSet<>(usersInRoom);
            usersToAdd.removeAll(currentUsers);

            Set<String> usersToRemove = new HashSet<>(currentUsers);
            usersToRemove.removeAll(usersInRoom);

            // Adds new users to RLGameManager
            for (String userName : usersToAdd) {
                User userToAdd = null;
                for (User user : room.getUserList()) {
                    if (user.getName().trim().toLowerCase().equals(userName)) {
                        userToAdd = user;
                        break;
                    }
                }

                if (userToAdd != null) {
                    boolean added = gameManager.addUser(userToAdd);
                    if (added) {
                        System.out.println("User " + userToAdd.getName() + " added to RLGameManager.");
                    } else {
                        System.out.println("User " + userToAdd.getName() + " was already in RLGameManager.");
                    }
                } else {
                    System.out.println("Failed to find User object for username: " + userName);
                }
            }

            // Track users removed from room
            for (String userName : usersToRemove) {
                RLGameUser rlUser = gameManager.getUserByUsername(userName);
                if (rlUser != null) {
                    User userToRemove = rlUser.getUser();
                    boolean removed = gameManager.removeUser(userToRemove);
                    if (removed) {
                        System.out.println("User " + userToRemove.getName() + " removed from RLGameManager.");
                    } else {
                        System.out.println("User " + userToRemove.getName() + " was not found in RLGameManager.");
                    }
                } else {
                    System.out.println("RLGameUser not found for username: " + userName + ". Cannot remove.");
                }
            }

            currentUsers = usersInRoom;

        } catch (Exception e) {
            System.out.println("Error during user list check: " + e.getMessage());
            e.printStackTrace();
        }
    }
}