package ygraph.ai.smartfox;

// import java.util.Map;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

//import sfs2x.client.requests.ExtensionRequest;

public class GameMessage {

	
	/** 
	 * The string name of the  game message for Game_Action_START: game start. (Created by the server)
	 */
	public static final String GAME_ACTION_START = "cosc322.game-action.start";
	
	/**
	 * The string name of the game message for Game_Action_MOVE: play move. (Created by the server)
	 */
	public static final String GAME_ACTION_MOVE = "cosc322.game-action.move";
	
	
	/**
	 * The string name of the game message for Game_STate_JOIN: join a game. 
	 */
	public static final String GAME_STATE_JOIN = "cosc322.game-state.join";
	
	/**
	 * The string name of the game message for Game_STate_BOARD: board data. 
	 */
	public static final String GAME_STATE_BOARD = "cosc322.game-state.board";
	
	/**
	 * 
	 */
	public static final String GAME_STATE_PLAYER_LOST = "cosc322.game-state.userlost";
	//protected ExtensionRequest req = null;
	
	
	protected String messageType = null;
	protected ISFSObject params = null;
	
	/**
	 * Objects of the class not in current use
	 */
	public GameMessage(String messageType) {
		this.messageType = messageType;
		params = new SFSObject();	 	
	}	
}
