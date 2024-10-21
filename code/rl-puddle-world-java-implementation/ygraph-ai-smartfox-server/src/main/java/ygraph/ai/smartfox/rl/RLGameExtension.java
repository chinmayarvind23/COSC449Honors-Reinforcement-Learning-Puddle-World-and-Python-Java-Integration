package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

// This class create the RL environment within the server and sets up request handles for client logic and event handlers for client interaction
public class RLGameExtension extends SFSExtension {

    private RLGameManager gameManager;

    // Instantiation of RLGameManager
    public RLGameManager getGameManager() {
        return gameManager;
    }

    @Override
    public void init() {
        trace("RLGameExtension initialized for COSC 322.");

        // Initialize a RLGameManager
        gameManager = new RLGameManager();
        
        // Create request handlers for all client-to-server message types
        addRequestHandler(RLGameMessage.GAME_STATE, RLGameRequestHandler.class);
        addRequestHandler(RLGameMessage.GAME_AVAILABLE_ACTIONS, RLGameRequestHandler.class);
        addRequestHandler(RLGameMessage.GAME_AVAILABLE_REWARDS, RLGameRequestHandler.class);
        addRequestHandler(RLGameMessage.GAME_ACTION_MOVE, RLGameRequestHandler.class);
        addRequestHandler(RLGameMessage.GAME_ACTION_REWARD, RLGameRequestHandler.class);
        addRequestHandler(RLGameMessage.GAME_FINAL_STATE, RLGameRequestHandler.class);
        addRequestHandler(RLGameMessage.GAME_RESET, RLGameRequestHandler.class);

        // Create event handlers for users joiing and leaving a game room
        addEventHandler(SFSEventType.USER_JOIN_ROOM, RLMultiHandler.class);
        addEventHandler(SFSEventType.USER_LEAVE_ROOM, RLMultiHandler.class);
    }
}
