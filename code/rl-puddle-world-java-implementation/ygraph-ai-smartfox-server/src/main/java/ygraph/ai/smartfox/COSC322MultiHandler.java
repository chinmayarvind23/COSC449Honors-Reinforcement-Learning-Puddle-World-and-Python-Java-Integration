package ygraph.ai.smartfox;

import com.smartfoxserver.v2.annotations.Instantiation;
import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
// import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;


@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
@MultiHandler
public class COSC322MultiHandler extends BaseClientRequestHandler{
	
	COSC322Extension ext = null;

	public COSC322MultiHandler(COSC322Extension ext){
		this.ext = ext;
	}
	
	@Override
	/**
	 * registered to handle room join
	 */
	public void handleClientRequest(User usr, ISFSObject req) {
		String cmd = req.getUtfString(SFSExtension.MULTIHANDLER_REQUEST_ID);
		trace(cmd + " --- COSC322 Extension (Version 2)");
		
		if(cmd.equals("join")){
			String roomName =  req.getUtfString("room.name");
			Room room = ext.getParentZone().getRoomByName(roomName);
			
			int numOfPlayers = room.getPlayersList().size();
			boolean asSpectator = true; 
			if(numOfPlayers < 2){
				asSpectator = false;
			}			

			//firing a client response event  and a server event (for AmazonsGameExtension)
			try{
				ext.getApi().joinRoom(usr, room, COSC322Extension.zoneName, asSpectator, usr.getLastJoinedRoom());
			} catch (SFSJoinRoomException e) {
				e.printStackTrace();
			}				
		}
	}
}//end of class
