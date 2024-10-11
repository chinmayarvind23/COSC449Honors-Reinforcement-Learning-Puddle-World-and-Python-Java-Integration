// package ygraph.ai.smartfox;

// import java.io.File;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import com.smartfoxserver.v2.core.ISFSEvent;
// import com.smartfoxserver.v2.core.SFSEventParam;
// import com.smartfoxserver.v2.core.SFSEventType;
// import com.smartfoxserver.v2.entities.Room;
// import com.smartfoxserver.v2.entities.User;
// import com.smartfoxserver.v2.entities.data.ISFSObject;
// import com.smartfoxserver.v2.entities.data.SFSObject;
// import com.smartfoxserver.v2.exceptions.SFSException;
// import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
// import com.smartfoxserver.v2.extensions.IServerEventHandler;
// import com.smartfoxserver.v2.extensions.SFSExtension;

// import ygraph.ai.smartfox.gaming.AmazonsGame;

// /**
//  * Game extension for the AmazonsGame
//  *  
//  * @author (yong.gao@ubc.ca)
//  */
// public class AmazonsGameExtension extends SFSExtension{

// 	protected AmazonsGame amazonsGame = null;
// 	protected Room room = null;
	
// 	protected Map<String, User> spectators = null;
	
// 	protected AmazonsGameEventHandler serverEventHandler = null;
	
// 	//nothing to do
// 	public AmazonsGameExtension(){}

// 	@Override
// 	public void init() {
// 		trace("Amazons game extension for COSC 322-2 at UBC Okanagan");
		
// 		File dir = new File("game-records-322-2021");
// 		if(!dir.exists()) {
// 			dir.mkdir();
// 		}
		
// 		room = this.getParentRoom();
// 		spectators = new HashMap<String, User>();
		
// 		//game related requests such as "move"
// 		this.addRequestHandler("cosc322.game-action", new AmazonsGameRequestHandler(this));
		
// 		//game room related requests
// 		serverEventHandler =  new AmazonsGameEventHandler();		
// 		this.addEventHandler(SFSEventType.USER_JOIN_ROOM, serverEventHandler);
// 		this.addEventHandler(SFSEventType.USER_DISCONNECT, serverEventHandler);
// 		this.addEventHandler(SFSEventType.USER_LEAVE_ROOM, serverEventHandler);
// 		this.addEventHandler(SFSEventType.USER_LOGOUT, serverEventHandler);
// 	}

// 	/**
// 	 * Handles server events
// 	 * @author (yong.gao@ubc.ca)
// 	 */
//     class AmazonsGameEventHandler extends BaseServerEventHandler{

// 		@Override
// 		public void handleServerEvent(ISFSEvent evt) throws SFSException {
// 			//Room room = this.getParentExtension().getParentRoom();			
// 			User user = (User)evt.getParameter(SFSEventParam.USER); 
// 			SFSEventType eventType = evt.getType();
			
// 			trace("Server-Event in AmazonGameEventHandler by " + user.getName());
			
// 			if(eventType.equals(SFSEventType.USER_DISCONNECT) || eventType.equals(SFSEventType.USER_LEAVE_ROOM)){
// 				this.handleUserLost(user);
// 			}
// 			else if(eventType.equals(SFSEventType.USER_JOIN_ROOM)) {											
// 				if(user.isSpectator()){ //user-spectator status is set when joinging the room (See COSC322MultiHandler.java)
// 					//this.sendGameState(user);
// 					this.sendGameState(user);
// 					spectators.put(user.getName(), user);
// 				}
// 				else{
// 					this.handlePlayerJoin(user);				
// 				}				
// 			}
// 		}
		
		
// 		private void handlePlayerJoin(User user) {
// 			if((amazonsGame == null) || amazonsGame.completed){
// 				//first player just joined the room
// 				amazonsGame = new AmazonsGame(user, this.getParentExtension().getParentRoom().getName());
// 				this.sendGameState(user);
// 			}
// 			else{
// 				//second player joined. Start the game and send the game start message 
// 				amazonsGame.setSecondPlayer(user);
// 				this.sendGameState(user);
				
// 				amazonsGame.startGame();
			
// 				SFSObject params = new SFSObject();
// 				//params.putIntArray("game-state", amazonsGame.stateToArrayList());
// 				params.putUtfString("room.name", room.getName());
// 				params.putUtfString("player-black", amazonsGame.playerB.getName());
// 				params.putUtfString("player-white" , user.getName());
// 				System.out.println("room:" + room.getName());
// 				this.send(GameMessage.GAME_ACTION_START, params,  this.getParentExtension().getParentRoom().getUserList());
// 			}	
// 		}
		
// 		private void handleUserLost(User user) {
// 			if(!(user.getName().equalsIgnoreCase(amazonsGame.playerB.getName()) || 
// 					user.getName().equalsIgnoreCase(amazonsGame.playerW.getName()))) {
// 				trace("User (spectator) leaving: " + user.getName());
// 			}
// 			else {
// 				SFSObject params = new SFSObject();
// 				params.putUtfString("player-lost", user.getName());
// 				this.send(GameMessage.GAME_STATE_PLAYER_LOST, params, this.getParentExtension().getParentRoom().getUserList());
			
// 				if(amazonsGame != null){
// 					amazonsGame.finish();
// 					amazonsGame = null;
// 				}
// 			}
// 		}
    	
// 		private void sendGameState(User usr) {
// 			SFSObject params = new SFSObject();
// 			params.putIntArray("game-state", amazonsGame.stateToArrayList()); 
// 			params.putUtfString("room.name", room.getName());
// 			trace(params.getKeys().toString() + "(sendGameState)");
// 			this.send(GameMessage.GAME_STATE_BOARD, params, usr);
// 		}
//     }
	
// } 
