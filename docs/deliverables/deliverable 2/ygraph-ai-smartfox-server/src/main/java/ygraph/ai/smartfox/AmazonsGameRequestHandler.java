package ygraph.ai.smartfox;
 
import java.util.List;

import com.smartfoxserver.v2.annotations.Instantiation;
import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;


@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
@MultiHandler
public class AmazonsGameRequestHandler extends BaseClientRequestHandler{

	protected AmazonsGameExtension ext = null;
	
	/**
	 * 
	 * @param ext
	 */
	public AmazonsGameRequestHandler(AmazonsGameExtension ext){
		this.ext = ext;
	}
	
	@Override
	/**
	 * only action of the type "cosc322.game-action.*"
	 */
	public void handleClientRequest(User usr, ISFSObject req) {
			
		String cmd = req.getUtfString(SFSExtension.MULTIHANDLER_REQUEST_ID);
		Room room = ext.getParentRoom();
	
		trace("Amazons Request Handler: " + cmd);
		
		//req will be forward to other users. So, add room information
		req.putUtfString("room.name", room.getName()); 
		
        if((ext.amazonsGame != null) && cmd.equals("move")){//request: cosc322.game-action.move
        	
        	ext.amazonsGame.recordMove(req); //req hash-map for queen's position and arrow's position 
        	
        	if(usr.getId() == ext.amazonsGame.playerB.getId()){
        		this.send(GameMessage.GAME_ACTION_MOVE, req, ext.amazonsGame.playerW);
        	}
        	else{
        		this.send(GameMessage.GAME_ACTION_MOVE, req,  ext.amazonsGame.playerB);
        	}
        		
        	//forward to all spectators, including those left the room a "moment" ago
        	List<User> sps = room.getSpectatorsList();
        	if(sps.size() > 0){
        		this.send(GameMessage.GAME_ACTION_MOVE, req, sps);
        	}
        }		
	}
}//end of class