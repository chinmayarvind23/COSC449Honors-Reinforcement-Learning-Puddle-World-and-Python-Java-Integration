package ygraph.ai.smartfox.games;

import java.util.Collection;
import com.smartfoxserver.v2.entities.data.SFSObject;
import sfs2x.client.requests.ExtensionRequest;
import ygraph.ai.smartfox.GameMessage;

public class AmazonGameMessage extends GameMessage{
   
	public final static String QUEEN_POS_CURR = "queen-position-current"; 
	public final static String Queen_POS_NEXT = "queen-position-next";
	public final static String ARROW_POS = "arrow-position";
 	
	/**
	 * not in use currently
	 * @param msgType
	 */
	public AmazonGameMessage(String msgType) {
		super(GameMessage.GAME_ACTION_MOVE);
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
}//end of class
