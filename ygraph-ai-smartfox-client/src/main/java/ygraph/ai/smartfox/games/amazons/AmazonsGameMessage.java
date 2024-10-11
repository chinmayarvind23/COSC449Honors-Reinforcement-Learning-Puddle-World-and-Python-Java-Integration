package ygraph.ai.smartfox.games.amazons;

import java.util.Collection;

// import com.smartfoxserver.v2.entities.data.SFSObject;

import sfs2x.client.requests.ExtensionRequest;
import ygraph.ai.smartfox.games.GameMessage;

 public class AmazonsGameMessage extends GameMessage{
   
	public final static String QUEEN_POS_CURR = "queen-position-current"; 
	public final static String QUEEN_POS_NEXT = "queen-position-next";
	public final static String ARROW_POS = "arrow-position";
	
	public final static String GAME_STATE = "game-state";
	public final static String PLAYER_BLACK = "player-black";
	public final static String PLAYER_WHITE = "player-white";
	
	
	//private boolean msgCompiled = false;
	//private SFSObject  params = null;
	
	public AmazonsGameMessage(String msgType) {
		super(GameMessage.GAME_ACTION_MOVE);
		
		//params  = new SFSObject();
	}

	
	@SuppressWarnings("unchecked")
	public void setMessageDetails(String key, Object value){
		if(super.messageType.equals(GAME_ACTION_MOVE)){
			params.putIntArray(key, (Collection<Integer>) value); 
		}
		else{
			params.putUtfString(key, (String) value);
		}
			
	}
		
	public void compile(){
		super.req = new ExtensionRequest(messageType, params);
		// msgCompiled = true;
	}
	
}
