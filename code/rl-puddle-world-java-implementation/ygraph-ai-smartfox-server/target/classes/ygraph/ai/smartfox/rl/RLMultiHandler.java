package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;

// This class manages RL-specific client requests like joining rooms and disconnecting from the server, and interacting with RLGameManager
public class RLMultiHandler extends BaseClientRequestHandler {

    private final RLGameManager gameManager;

    public RLMultiHandler(RLGameManager gameManager) {
        this.gameManager = gameManager;
    }

    // Handles client requests to join a room and disconnect from the server
    @Override
    public void handleClientRequest(User sender, ISFSObject params) {
        String cmd = params.getUtfString("command");
        trace("Received command: " + cmd + " from user: " + sender.getName());

        switch (cmd) {
            case "join":
                handleJoinRequest(sender, params);
                break;
            case "disconnect":
                handleDisconnectRequest(sender, params);
                break;
            default:
                trace("Unknown command: " + cmd + " from user: " + sender.getName());
                sendErrorMessage(sender, "Unknown command: " + cmd);
                break;
        }
    }

    // Handles the specific room joiining command from the client for a zone by checking if the room exists, and if it does, then tries to make the user join the room and prints to console if the room is occupied
    // And notifies the game manager that the user has been added to a room
    private void handleJoinRequest(User user, ISFSObject params) {
        String roomName = params.getUtfString("room.name");
        String roomPassword = params.getUtfString("room.password");
        trace("User " + user.getName() + " requests to join room: " + roomName + " with password: " + roomPassword);
    
        // Only allow RLRoom logic
        if (!"RLRoom".equals(roomName)) {
            trace("User " + user.getName() + " is attempting to join a room that is not RLRoom. Ignoring request.");
            return;
        }
    
        Room room = getParentExtension().getParentZone().getRoomByName(roomName);
    
        if (room == null) {
            trace("Room " + roomName + " does not exist.");
            sendErrorMessage(user, "Room " + roomName + " does not exist.");
            return;
        }
    
        int numOfPlayers = room.getPlayersList().size();
    
        // Allow user to join only if the room is empty
        if (numOfPlayers > 0) {
            trace("Room " + roomName + " is already occupied.");
            sendErrorMessage(user, "Room " + roomName + " is already occupied.");
            return;
        }
    
        try {
            getApi().joinRoom(user, room, roomPassword, false, user.getLastJoinedRoom());
            trace("User " + user.getName() + " successfully joined room: " + roomName + ".");
            gameManager.addUser(user);
        }
        catch (SFSJoinRoomException e) {
            trace("Failed to join room " + roomName + " for user " + user.getName() + ": " + e.getMessage());
            sendErrorMessage(user, "Failed to join room " + roomName + ": " + e.getMessage());
        } catch (Exception e) {
            trace("Unexpected error while user " + user.getName() + " attempts to join room " + roomName + ": " + e.getMessage());
            sendErrorMessage(user, "An unexpected error occurred.");
        }
    }        

    // Handles the disconnect command from the client by removing the user from their game and disconnecting them from the server by notifiying the game manager
    private void handleDisconnectRequest(User user, ISFSObject params) {
        trace("User " + user.getName() + " requests to disconnect.");
        gameManager.removeUser(user);
        getApi().logout(user);
        trace("User " + user.getName() + " has been disconnected.");
    }

    // Sends an error message to the user
    private void sendErrorMessage(User user, String message) {
        ISFSObject response = new SFSObject();
        response.putUtfString("error", message);
        send("rl.error", response, user);
    }
}