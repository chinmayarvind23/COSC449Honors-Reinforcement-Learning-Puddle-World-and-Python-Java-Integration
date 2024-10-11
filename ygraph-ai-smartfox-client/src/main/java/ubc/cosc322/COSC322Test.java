// package ubc.cosc322;


// import java.util.List;
// import java.util.Map;

// import sfs2x.client.entities.Room;
// import ygraph.ai.smartfox.games.BaseGameGUI;
// import ygraph.ai.smartfox.games.GameClient;
// import ygraph.ai.smartfox.games.GamePlayer;

 
// /**
//  * An example illustrating how to implement a GamePlayer 
//  * @author Yong Gao (yong.gao@ubc.ca)
//  * Nov 23, 2020
//  *
//  *<hr>
//  *<b>COSC 322 Lab 01 (Warmup-1)</b> In a text-based player, 
//  *<ol>
//  *<li>In the onLogin() method, print out the list of rooms on the server and 
//  *join a room from the room list 
//  *<li>print out (in the method hanldeGameMessage(...) the message from the room.
//  *</ol> 
//  *To see how to get the room list and how to join a room, see API document of
// *GameClient.java
// *<hr><hr>
// *
// *<b>COSC 322 Lab 02 (Warmup-2)</b> <br>
// *
// *Turn this into a GUI-based Spectator by initialize an instance of BaseGameGUI.java
// *and assign it to this.gamegui, and complete the following
// *<ul>
// *<li>Replace the code in your method onLogin() with the following two statements:
// *<dl><dt></dt> 
// *<dd>userName = gameClient.getUserName();
// *<dd>               
// *<dd>if(gamegui != null) {                
// *<dd>  gamegui.setRoomInformation(gameClient.getRoomList()); 
// * <dd>} 
// * </dl>
// *<li>Add Implementation in the method handleGameMessage(...) to handle the two types 
// *game messages: 
// *<ol>
// *<li>GameMessage.GAME_STATE_BOARD (call BaseGameGui.setGameState(...) to set the game board) 
// *<li>GameMessage.GAME_ACTION_MOVE (call BaseGameGui.updateGaemState(...) to update the game board)
// *<ul>
// *<li>In a game player, upon receiving this message about your opponent's move, you will also need 
// *to calculate your move and send your move to the server using the method  
// *<code>GameClient.sendMoveMessage(...)</code> (these are the core tasks of this project you will 
// *have to by the middle of March)   
// *</ul>
// *</ol>
// *</ul>
// *For a detailed description of the message format, see API document on
// *<code>GamePlayer.handleGameMessage(...)</code>  
// * 
// *<br>
// *If everything goes well, your program can be used as a spectator.
// *To test your implementation, start two instances of HumanPlayer and 
// *have them join a game room, play a few moves, and then start your 
// *program and join the same room.  
// ***********************************************************************/  

// public class COSC322Test extends GamePlayer{

//     private GameClient gameClient = null; 
//     private BaseGameGUI gamegui = null;
	
//     private String userName = null;
//     private String passwd = null;
 
	
//     /**
//      * The main method
//      * @param args for name and passwd (current, any string would work)
//      */
//     public static void main(String[] args) {				 
//     	COSC322Test player = new COSC322Test(args[0], args[1]);
    	
//     	if(player.getGameGUI() == null) {
//     		player.Go();
//     	}
//     	else {
//     		BaseGameGUI.sys_setup();
//             java.awt.EventQueue.invokeLater(new Runnable() {
//                 public void run() {
//                 	player.Go();
//                 }
//             });
//     	}
//     }
	
//     /**
//      * Any name and passwd 
//      * @param userName
//       * @param passwd
//      */
//     public COSC322Test(String userName, String passwd) {
//     	this.userName = userName;
//     	this.passwd = passwd;
    	
//     	//To make a GUI-based player, create an instance of BaseGameGUI
//     	//and implement the method getGameGUI() accordingly
//     }
 


//     @Override
//     public void onLogin() {
//     	System.out.println("Congratualations!!! "
//     			+ "I am called because the server indicated that the login is successfully");
//     	System.out.println("The next step is to find a room and join it: "
//     			+ "the gameClient instance created in my constructor knows how!");
        		
//     	List<Room> rooms = gameClient.getRoomList();
    	
// 		this.gameClient.joinRoom(rooms.get(0).getName());	   	
//     	this.gameClient.logout();  //Don't forget to logout()
//     }

//     @Override
//     public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
//     	//This method will be called by the GameClient when it receives a game-related message
//     	//from the server.
	
//     	//For a detailed description of the message types and format, 
//     	//see the method GamePlayer.handleGameMessage() in the game-client-api document. 
    	
    	
//     	return true;
    	
//     }
    
    
//     @Override
//     public String userName() {
//     	return userName;
//     }

// 	@Override
// 	public GameClient getGameClient() {
// 		return this.gameClient;
// 	}

// 	@Override
// 	public BaseGameGUI getGameGUI() {
// 		return null;
// 	}

// 	@Override
// 	public void connect() {
//     	gameClient = new GameClient(userName, passwd, this);			
// 	}

 
// }//end of class
