package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;

// This class manages non-RL-specific client requests like joining rooms and disconnecting from the server, and interacting with RLGameManager
public class RLMultiHandler extends BaseClientRequestHandler {

    private final RLGameManager gameManager;

    public RLMultiHandler(RLGameManager gameManager) {
        this.gameManager = gameManager;
    }

    // Handles client requests to join a room from the client
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        if (params == null) {
            System.out.println("Received null params from user: " + sender.getName());
            return;
        }

        String messageType = params.getUtfString("messageType");
        System.out.println("Received command: " + messageType + " from user: " + sender.getName());

        switch (messageType) {
            case "join":
                handleJoinRequest(sender, params);
                break;
            default:
                System.out.println("Unknown message type received in RLMultiHandler: " + messageType);
                sendErrorMessage(sender, "Unknown message type: " + messageType);
                break;
        }
    }

    // Handles the specific room joiining command from the client for a zone by checking if the room exists, and if it does, then tries to make the user join the room and prints to console if the room is occupied
    // And notifies the game manager that the user has been added to a room
    private void handleJoinRequest(User user, ISFSObject params) {
        String roomName = params.getUtfString("room.name");
        String roomPassword = params.getUtfString("room.password");
        System.out.println("User " + user.getName() + " requests to join room: " + roomName + " with password: " + roomPassword);
    
        if (!"RLRoom".equals(roomName)) {
            System.out.println("User " + user.getName() + " is attempting to join a room that is not RLRoom. Ignoring request.");
            return;
        }
    
        Room room = getParentExtension().getParentZone().getRoomByName(roomName);
    
        if (room == null) {
            System.out.println("Room " + roomName + " does not exist.");
            sendErrorMessage(user, "Room " + roomName + " does not exist.");
            return;
        }
    
        int numOfPlayers = room.getPlayersList().size();
        if (numOfPlayers > 0) {
            System.out.println("Room " + roomName + " is already occupied.");
            sendErrorMessage(user, "Room " + roomName + " is already occupied.");
            return;
        }
    
        try {
            getApi().joinRoom(user, room, roomPassword, false, user.getLastJoinedRoom());
            System.out.println("User " + user.getName() + " successfully joined room: " + roomName + ".");
            
            if (gameManager != null) {
                if (gameManager.addUser(user)) {
                    System.out.println("User " + user.getName() + " added to RLGameManager.");
                } else {
                    System.out.println("Failed to add user to RLGameManager.");
                }
            }
        }
        catch (SFSJoinRoomException e) {
            System.out.println("Failed to join room " + roomName + " for user " + user.getName() + ": " + e.getMessage());
            sendErrorMessage(user, "Failed to join room " + roomName + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error while user " + user.getName() + " attempts to join room " + roomName + ": " + e.getMessage());
            sendErrorMessage(user, "An unexpected error occurred.");
        }
    }

    // Sends an error message to the user
    private void sendErrorMessage(User user, String message) {
        ISFSObject response = new SFSObject();
        response.putUtfString("error", message);
        send("rl.error", response, user);
    }
}