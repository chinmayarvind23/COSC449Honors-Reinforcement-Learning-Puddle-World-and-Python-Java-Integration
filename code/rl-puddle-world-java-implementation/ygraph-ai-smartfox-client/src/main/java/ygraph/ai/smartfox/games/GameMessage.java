package ygraph.ai.smartfox.games;

// import java.util.Map;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

import sfs2x.client.requests.ExtensionRequest;


/**
 * Defines string constants for message types.       
 * 
 * @author yong.gao@ubc.ca
 */
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
	 * The string name of the game message for GAME_STATE: join a game. 
	 */
	public static final String GAME_STATE_JOIN = "cosc322.game-state.join";
	
	public static final String GAME_STATE_BOARD = "cosc322.game-state.board";
	
	public static final String GAME_STATE_PLAYER_LOST = "cosc322.game-state.userlost";
	
	public static final String GAME_TEXT_MESSAGE = "cosc322.game-state.textmessage";
	
	
	protected ExtensionRequest req = null;
	
	
	protected String messageType = null;
	protected ISFSObject params = null;
	
	//private Map<String, Object> parameters = null;
	
 
	/**
	 * 
	 * @param messageType
	 */
	public GameMessage(String messageType) {
		this.messageType = messageType;
		params = new SFSObject();	 	
	}	
}
