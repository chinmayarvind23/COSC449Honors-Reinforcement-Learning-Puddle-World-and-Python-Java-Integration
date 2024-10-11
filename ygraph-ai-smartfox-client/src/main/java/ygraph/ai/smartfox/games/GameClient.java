package ygraph.ai.smartfox.games;

 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.LeaveRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.requests.LogoutRequest;
import sfs2x.client.requests.SubscribeRoomGroupRequest;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;


/**
 *  handles the interaction between a Game Player 
 * and the gaming server, including establishing the connection, the login, and 
 * the message passing.          
 *  
 * <p>
 * Messages that are relevant and specific to game playing will be relayed to a GamePlayer (the delegate) 
 * by calling its method: <code>handleMessages(GameMessage msg)</code>.  
 * 
 * Requires SFS2X_API_Java.jar and sfs-client-core.jar, ...  
 * 
 * @author yong.gao@ubc.ca
 *
 */
/**
 * 
 * A GameClient handles the communication between a GamePlayer 
 * and the gaming server, taking care of tasks such as establishing the connection, the login, and 
 * the message passing.          
 *  
 * <p>
 * Messages relevant and specific to game playing will be relayed to a GamePlayer (the delegate) 
 * by calling its method: <code>handleMessages(GameMessage msg)</code>.  
 * 
 * Requires SFS2X_API_Java.jar and sfs-client-core.jar, ...  
 * 
 * @author Yong Gao (yong.gao@ubc.ca)
 * Dec 12, 2020
 *
 */

public class GameClient {

	//Login Sequence:  
	// 1)connect() in constructor, 
	// 2)login() (called in dispatch() in EventHandler on event SFSEvent.CONNECTION 
	// 3)this.delegate.onlogin() (called in in dispatch() in EventHandler on event SFSEvent.LOGIN) 
	// 4)waiting for message from the extension (dispatch() --- 
	//   dispatchGameMessage() [Start, Move, etc] on SFSEvent.EXTENSION_RESPONSE), and call 
	//   delegate.handleGameMessage(). Delegate being a Gameplayer
	
	private String usrName = "";
	private String usrPasswd = "";
	private String IP = "206.87.25.37";
	//private String IP = "192.168.1.78"; 
	//private String IP = "localhost";
	
	
	private SmartFox sfsInstance = null;
	public List<Room> rooms;
	//private ArrayList<String> roomNames;
	
	
	private GamePlayer delegate = null;
	
	private Room room = null;
	private User user = null;
	
	
	private Timer timeScheduler = null; 
	private int numOfTimeOut = 0;

	    
	/**
	 * Create a new instance of the game client
	 * @param handle --- the login name
	 * @param passwd --- the login password
	 * @param delegate --- a GamePlayer that receives and handles game-related server messages
	 */
	public GameClient(String handle, String passwd, GamePlayer delegate){
		usrName = handle;
		usrPasswd = passwd;		
		this.delegate = delegate;		
		sfsInstance = new SmartFox(false);  // true for enabling debug message
 		configureEvents(); 
		connect();
	}
	
	/**
	 * Testing purpose, no delegate set
	 */
	public GameClient(String handle, String passwd) {
		usrName = handle;
		usrPasswd = passwd;	
		sfsInstance = new SmartFox(false); // true for enabling debug message	 
		configureEvents();
		connect();
	}
	
	
	//Configure the event handler
	private void configureEvents(){
		
		EventHandler eventH = new EventHandler(this);
		sfsInstance.addEventListener(SFSEvent.CONNECTION, eventH); 
		sfsInstance.addEventListener(SFSEvent.LOGIN, eventH); 
		sfsInstance.addEventListener(SFSEvent.LOGOUT, eventH); 
		sfsInstance.addEventListener(SFSEvent.LOGIN_ERROR, eventH);
		sfsInstance.addEventListener(SFSEvent.ROOM_JOIN, eventH);

		sfsInstance.addEventListener(SFSEvent.EXTENSION_RESPONSE, eventH);
		
		EventHandlerZoneWide eventHZ = new EventHandlerZoneWide(this);
		//sfsInstance.addEventListener(SFSEvent.ROOM_GROUP_SUBSCRIBE, eventHZ);
		//sfsInstance.addEventListener(SFSEvent.ROOM_GROUP_SUBSCRIBE_ERROR, eventHZ);
		sfsInstance.addEventListener(SFSEvent.USER_COUNT_CHANGE, eventHZ);
	}
	
	//send the login request
	private void login(){
		//LoginRequest(java.lang.String userName, java.lang.String password, java.lang.String zoneName) 
		LoginRequest loginReq = 
				new LoginRequest(usrName, usrPasswd, "cosc322-2");
		
		sfsInstance.send(loginReq);			
	}
	
	//connect to the server
	private void connect(){
		sfsInstance.setUseBlueBox(true);
		sfsInstance.connect(IP, 3220);
	}
	
	//set up the time scheduler
	private void setUpScheduler(){
		if(timeScheduler != null){
			timeScheduler.cancel();
		}
				
		timeScheduler = new Timer();
		GameTimer gameTimer = new GameTimer(this.delegate, 
				this, this.numOfTimeOut);
		timeScheduler.schedule(gameTimer, GameTimer.TIMEOUT, 
				GameTimer.TIMEOUT); //1 second = 1000 milliseconds
	}
	
	
	/**
	 * Request to join a game room 
	 * @param roomName --- the name of the room to join 
	 */
	public void joinRoom(String roomName){
		//sfsInstance.send(new JoinRoomRequest(roomName));	
		ISFSObject params = new SFSObject();
		params.putUtfString("room.name", roomName);
		
		ExtensionRequest req = new ExtensionRequest(GameMessage.GAME_STATE_JOIN, params); 
		sfsInstance.send(req);
	}
	
	/**
	 * Request to leave a game room
	 */
	public void leaveCurrentRoom() {
		if(this.room != null) {
			sfsInstance.send(new LeaveRoomRequest());
			room = null;
		}
	}
	
	//public List<Room> getRoomList(){
	//	return rooms;
	//}
	
	/**
	 * Get the list of game room names available on the server
	 * @return An ArrayList&lt;String&gt; of names 
	 */
	public  List<Room> getRoomList(){
		if(rooms == null){
			rooms = sfsInstance.getRoomList();
			//roomNames = new ArrayList<String>();		
			//rooms.forEach((r) -> {roomNames.add(r.getName());});
		}		
		return rooms;		
	}
	
	public String getUserName() {
		return user.getName();
	}
 	
	/**
	 * Send a move message to the server
	 * @param queenPosCurrent - Queen's current position [row, column]
	 * @param queenPosNew - Queen's new position
	 * @param arrowPos - Arrow's position
	 */
	public void sendMoveMessage(ArrayList<Integer> queenPosCurrent, 
								ArrayList<Integer> queenPosNew, 
								ArrayList<Integer> arrowPos){
 
		 SFSObject data = new SFSObject();
 	 
		 //ArrayList<Integer> qCurr= new ArrayList<Integer>();
		 //
		 //qCurr.add(queenPosCurrent[0]);
		 //qCurr.add(queenPosCurrent[1]);	 
	     data.putIntArray(AmazonsGameMessage.QUEEN_POS_CURR, queenPosCurrent);
	     
		 //ArrayList<Integer> qNew= new ArrayList<Integer>();
		 //
		 //qNew.add(queenPosNew[0]);
		 //qNew.add(queenPosNew[1]);	 
	     data.putIntArray(AmazonsGameMessage.QUEEN_POS_NEXT, queenPosNew);
	     
		 //ArrayList<Integer> arrow= new ArrayList<Integer>();
		 //
		 //arrow.add(arrowPos[0]);
		 //arrow.add(arrowPos[1]);	 
	     data.putIntArray(AmazonsGameMessage.ARROW_POS, arrowPos);
	     
	     
	     sendToServer(GameMessage.GAME_ACTION_MOVE, data);		
	}
	
	 
	/**
	 * Send a move message to the server
	 * @param msDetails
	 */
	public void sendMoveMessage(Map<String, Object> msDetails){
 
		SFSObject data = new SFSObject();
	    data.putIntArray(AmazonsGameMessage.QUEEN_POS_CURR, (ArrayList<Integer>)msDetails.get(AmazonsGameMessage.QUEEN_POS_CURR));
	     
		 //ArrayList<Integer> qNew= new ArrayList<Integer>();
		 //
		 //qNew.add(queenPosNew[0]);
		 //qNew.add(queenPosNew[1]);	 
	     data.putIntArray(AmazonsGameMessage.QUEEN_POS_NEXT, (ArrayList<Integer>)msDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT));
	     
		 //ArrayList<Integer> arrow= new ArrayList<Integer>();
		 //
		 //arrow.add(arrowPos[0]);
		 //arrow.add(arrowPos[1]);	 
	     data.putIntArray(AmazonsGameMessage.ARROW_POS, (ArrayList<Integer>)msDetails.get(AmazonsGameMessage.ARROW_POS));
	     sendToServer(GameMessage.GAME_ACTION_MOVE, data);		
	}
	
 
	/**
	 * Send a text message to the server
	 * @param msg - the message
	 */
	public void sendTextMessage(String msg){
		SFSObject data = new SFSObject();
		data.putUtfString(GameMessage.GAME_TEXT_MESSAGE, msg);
		
		sendToServer(GameMessage.GAME_TEXT_MESSAGE, data);
	}
	
	
	/**
	 * A GamePlayer should call this method before terminating 
	 */
	public void logout(){
		
		this.room = null;
		this.user = null;
		this.rooms = null;
		this.numOfTimeOut = 0;
		if(this.timeScheduler != null){
			this.timeScheduler.cancel();
			this.timeScheduler = null;
		}
		
		LogoutRequest logoutReq = new LogoutRequest();
		sfsInstance.send(logoutReq);
	}
	
	/**
	 * 
	 */
	protected void updateTimeOut(){
		this.numOfTimeOut++;
	}
	
	//Send a message to the server
	private void sendToServer(String messageType, SFSObject params) {
		if(messageType.equals(GameMessage.GAME_ACTION_MOVE) 
				&& timeScheduler != null){
			timeScheduler.cancel();
			timeScheduler = null;
		}
		
		ExtensionRequest req = new ExtensionRequest(messageType, params, this.room);
		sfsInstance.send(req);
	}
	
	
	public class EventHandlerZoneWide implements IEventListener{
		
		GameClient gc = null;
		
		public EventHandlerZoneWide(GameClient gc){
			this.gc = gc;
		}
		
		@Override
		public final void dispatch(final BaseEvent event) throws SFSException {
			String eventType = event.getType();
			
			if(eventType.equalsIgnoreCase(SFSEvent.USER_COUNT_CHANGE)) {
	             //Room room = (Room) event.getArguments().get("room");
	             //int uCount = (Integer)event.getArguments().get("uCount");
	             //int sCount = (Integer)event.getArguments().get("sCount");
	             //room.getUserCount();
				
	             delegate.handleMessage(SFSEvent.USER_COUNT_CHANGE,null);
			}
			//System.out.println("Group subscribed. The following rooms are now accessible: " 
			//			+ event.toString() +  event.getArguments().get("errorMessage") + event.getArguments().get("groupId") +  event.getArguments().get("newRooms"));
			
		}
	}
	
	/**
	 * Implements IEventListener 
	 * @author yong.gao@ubc.ca
	 *
	 */
	public class EventHandler implements IEventListener{
		
		GameClient gc = null;
		
		public EventHandler(GameClient gc){
			this.gc = gc;
		}
		
		
		private void dispatchGameMessage(String cmd, SFSObject params, String fromRoomName){

			HashMap<String, Object> data = new HashMap<String, Object>();

			System.out.println("cmd:" + cmd + "; From:" + fromRoomName + "; CurrRoom: " + room.getName());
			
			//After a spectator leave a room, it still receives a few messages from that room 
			//(due to smartfox server issue). In this case, ignore the received message
			if((room == null) || (!room.getName().equalsIgnoreCase(fromRoomName))) {
				return;
			}
			
			if(cmd.equals(GameMessage.GAME_STATE_BOARD)){//game state  					 
				data.put("game-state", params.getIntArray("game-state"));		
			}
			
			 
			if(cmd.equals(GameMessage.GAME_ACTION_START)){//game start  					 
				data.put("room.name", params.getUtfString("room.name"));
				data.put("player-black", params.getUtfString("player-black"));
				data.put("player-white", params.getUtfString("player-white"));
	
				//Black moves first, so set the timer
				if(((String) params.getUtfString("player-black")).equals(gc.delegate.userName())){
					gc.setUpScheduler();
				}				
			}
						
				 
			if(cmd.equals(GameMessage.GAME_ACTION_MOVE)){//message for a move
				data.put(AmazonsGameMessage.QUEEN_POS_CURR, 
						params.getIntArray(AmazonsGameMessage.QUEEN_POS_CURR)); 
				data.put(AmazonsGameMessage.QUEEN_POS_NEXT, params.getIntArray(AmazonsGameMessage.QUEEN_POS_NEXT));
				data.put(AmazonsGameMessage.ARROW_POS, params.getIntArray(AmazonsGameMessage.ARROW_POS));
				
				if((user != null) && (!user.isSpectator())) {
					gc.setUpScheduler(); // Opponent move received - starts the timer 
				}
			}	
			
			//callback the delegate (GamePlayer) to handle the opponoent move 
			gc.delegate.handleGameMessage(cmd, data);
		}
		
		@Override
		public final void dispatch(final BaseEvent event) throws SFSException {
			//System.out.println("GameClient " + event.toString());			  
			
			String eventType = event.getType();
			
			if(eventType.equalsIgnoreCase(SFSEvent.CONNECTION)){
	             if(event.getArguments().get("success").equals(true)){
	                 //System.out.println("Connected!");
	            	 //to login as guest in current zone, use empty name and passwd	          
	                 gc.login();
	             }
	             else {
	            	 gc.delegate.handleMessage("Connection failed!");
	             }
	         }
			 else if(eventType.equalsIgnoreCase(SFSEvent.LOGIN)){
				 				 
				 user = (User)event.getArguments().get("user");
				 usrName = user.getName();
				 //roomNames = gc.getRoomList();
				 				 
				 
				 if(gc.delegate != null){
					 gc.delegate.onLogin();
				 }
				 else{
					 gc.joinRoom(rooms.get(0).getName());
					 System.out.println("Warning: no player attached to this client!" );
				 }
				 
				 //sfsInstance.send(new SubscribeRoomGroupRequest("Amazons"));
			 }
			 else if(eventType.equalsIgnoreCase(SFSEvent.ROOM_JOIN)){				  
				 room = (Room) event.getArguments().get("room");
				 user = room.getUserByName(delegate.userName()); 

				 System.out.println("(Room-join-response) RoomName:" +  room.getName() + "; NumUser: " +
				   room.getUserCount() + "; PlayerID: " + user.getPlayerId());
	
				 //If joined successfully, the next message will be of the type EXTENSION_RESPONSE
				 
				 //gc.delegate.handleGameMessage(GameMessage.GAME_STATE_JOIN, event.getArguments()); 
			 }		 
			 else if(eventType.equalsIgnoreCase(SFSEvent.EXTENSION_RESPONSE)){
				//Response from a particular extension installed at the server
				 
				 String cmd = (String) event.getArguments().get("cmd");	
				 //Room r = (Room) event.getArguments().get("sourceRoom");
				 SFSObject par = (SFSObject)event.getArguments().get("params");
				 String from = par.getUtfString("room.name");
				 System.out.println(event.getArguments().keySet().toString() + "---" + par.getKeys().toString());
				 
				
				 System.out.println("Extension Request from dispatch(): " + 
				   event.getArguments().get("cmd") + "---" + from); // toString());
				 

 				 
				 if(cmd.startsWith("cosc322.game-action") || 
						 cmd.startsWith("cosc322.game-state")){
					 SFSObject params = (SFSObject)event.getArguments().get("params");
					 
					 this.dispatchGameMessage(cmd, params, from);
				 }
				 
				 //System.out.println(event.getArguments().get("params").toString());				 
			 }
			 else if(eventType.equalsIgnoreCase(SFSEvent.OBJECT_MESSAGE)){
				 //ISFSObject objMsg = (SFSObject)event.getArguments().get("message");             
	             //User sender = evt.getArguments().get("sender");	             
				 //objMsg.getBool(GameMessage.ACTION_MOVE);				 
	             //objMsg.getInt("x");
	             //objMsg.getInt("y");
			 }
		}		
	}
	
	
	public static void main(String[] args) {		
		GameClient gc = new GameClient("yong.gao", "cosc322");				 
	}
	
}//end of class
