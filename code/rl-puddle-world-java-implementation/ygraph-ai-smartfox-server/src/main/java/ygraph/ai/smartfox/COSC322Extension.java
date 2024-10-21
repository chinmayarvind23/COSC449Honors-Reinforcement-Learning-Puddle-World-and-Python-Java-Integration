package ygraph.ai.smartfox;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.extensions.SFSExtension;
import com.smartfoxserver.v2.exceptions.SFSRuntimeException;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import ygraph.ai.smartfox.rl.RLGameExtension;
import ygraph.ai.smartfox.rl.RLGameManager;
import ygraph.ai.smartfox.rl.RLMultiHandler;

/**
 * Server extension for COSC 322 (V2) at UBC Okanagan
 * This class forwards requests to room-level RLGameExtension.
 */
public class COSC322Extension extends SFSExtension {

    public static final String zoneName = "cosc322-2";

    @Override
    public void init() {
        trace("Server extension for COSC 322 (V2) at UBC Okanagan");
        addEventHandler(SFSEventType.USER_JOIN_ROOM, this::onUserJoinRoom);
        addRequestHandler(GameMessage.GAME_STATE_JOIN, new COSC322MultiHandler(this));
        addRequestHandler("rl.action", new RLMultiHandler(new RLGameManager()));
    }

    // Event handler for when a user joins a room
    private void onUserJoinRoom(ISFSEvent event) throws SFSRuntimeException {
        Room room = (Room) event.getParameter(SFSEventParam.ROOM);
        User user = (User) event.getParameter(SFSEventParam.USER);
        if (room.getExtension() != null && room.getExtension() instanceof RLGameExtension) {
            RLGameExtension rlGameExtension = (RLGameExtension) room.getExtension(); // Cast the extension to RLGameExtension
            trace("User " + user.getName() + " joined game room: " + room.getName());
            rlGameExtension.handleGameJoin(user);
        } else {
            trace("User " + user.getName() + " joined a non-game room: " + room.getName());
        }
    }

    @Override
    public void destroy() {
        trace("COSC322Extension destroyed for Zone level.");
        super.destroy();
    }
}