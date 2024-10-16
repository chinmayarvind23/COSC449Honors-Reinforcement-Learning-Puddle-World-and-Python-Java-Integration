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
    public RLMultiHandler(RLGameManager manager) {
        this.gameManager = manager;
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
        trace("User " + user.getName() + " requests to join room: " + roomName);

        Room room = getParentExtension().getParentZone().getRoomByName(roomName);

        if (room == null) {
            trace("Room " + roomName + " does not exist.");
            sendErrorMessage(user, "Room " + roomName + " does not exist.");
            return;
        }

        int numOfPlayers = room.getPlayersList().size();
        boolean canJoin = numOfPlayers <= 1;

        if (!canJoin) {
            trace("Room " + roomName + " is already occupied.");
            sendErrorMessage(user, "Room " + roomName + " is already occupied.");
            return;
        }

        try {
            String zoneName = getParentExtension().getParentZone().getName();
            getApi().joinRoom(user, room, zoneName, false, user.getLastJoinedRoom());
            trace("User " + user.getName() + " joined room: " + roomName + " as Player.");
            gameManager.addUser(user);
        } catch (SFSJoinRoomException e) {
            trace("Failed to join room " + roomName + ": " + e.getMessage());
            sendErrorMessage(user, "Failed to join room " + roomName + ": " + e.getMessage());
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