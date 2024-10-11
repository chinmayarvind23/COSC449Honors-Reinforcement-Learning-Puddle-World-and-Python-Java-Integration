// package ygraph.ai.smartfox.games;

// import java.util.Map;

// import sfs2x.client.core.SFSEvent;
 
// /**
//  * A GamePlayer shall implements methods that handle game-specific message. A
//  * GameClient delegates all the game-specific message
//  * to a GamePlayer. See <code>GameClient.java</code>  and 
//  * <code>GameMessage.java</code>
//  *
//  * @author Yong Gao (yong.gao@ubc.ca)
//  * Dec 12, 2020
//  *
//  */
// public abstract class GamePlayer {
			
// 	/**
// 	 * The method will be invoked by GameClient once the player has successfully 
// 	 * logged onto the server. In this method, we usually find a room and join it.      
// 	 */
// 	public abstract void onLogin();

// 	/**
// 	 * Call-back method upon receiving a message from the server
// 	 * @param messageType --- message type
// 	 * @param msgDetails --- message details
// 	 * @return true unless something is wrong
// 	 *  
// 	 * 
// 	 * <p>
// 	 * This method is called back by a GameClient whenever there is a game-related message from the 
// 	 * server. 
// 	 * 
// 	 * For the COSC322 class, there are three types of messages as defined by 
// 	 * the Strings: GameMessage.GAME_STATE_BOARD, GameMessage.GAME_ACTION_START,  
// 	 * and GameMessage.GAME_ACTION_MOVE. 
// 	 * The details of a message are included in the second argument "msgDetails" - a HashMap. 
// 	 * The content of msgDetails for two types of messages is as follows.  
// 	 * <dl> 
// 	 * <dt><b>Type: <code>GameMessage.GAME_STATE_BOARD</code></b></dt> 
// 	 * </dl>
// 	 * <table style="width:900px;border:1px solid black;border-collapse:collapse">
// 	 * <tr><td style="text-align:center;border:1px solid black">Hash Key</td>
// 	 * <td style="text-align:center;border:1px solid black">Object</td></tr>
// 	 * <tr>
// 	 * <td style="text-align:center;border:1px solid black"><code>AmazonsGameMessage.GAME_STATE</code></td>
// 	 * <td style="border:1px solid black">An <code>ArrayList &lt;Integer&gt; </code> encoding the game board</td>
// 	 * </tr>
// 	 * <caption></caption>
// 	 * </table> 
// 	 * <dl> 
// 	 * <dt><b>Type: <code>GameMessage.GAME_ACTION_START</code></b></dt>
// 	 * </dl>
// 	 * <table style="width:900px;border:1px solid black;border-collapse:collapse">
// 	 * <tr><td style="text-align:center;border:1px solid black">Hash Key</td>
// 	 * <td style="text-align:center;border:1px solid black">Object</td></tr>
// 	 * <tr>
// 	 * <td style="text-align:center;border:1px solid black"><code>AmazonsGameMessage.GAME_STATE</code></td>
// 	 * <td style="1px solid black">An <code>ArrayList &lt;Integer&gt; </code> encoding the game board</td>
// 	 * </tr>
// 	 * <tr>
// 	 * <td style="text-align:center;border:1px solid black"><code>AmazonsGameMessage.PLAYER_BLACK</code></td>
// 	 * <td style="border:1px solid black">A String for the user name of the player assigned to play the Black stone</td>
// 	 * </tr>
// 	 * <tr>
// 	 * <td style="text-align:center;border:1px solid black">AmazonsGameMessage.PLAYER_WHITE</td>
// 	 * <td style="border:1px solid black">A String for the user name of the player assigned to play the White stone</td>
// 	 * </tr><caption></caption>
// 	 * </table>
// 	 * <dl>  
// 	 * <dt><b>Type: <code>GameMessage.GAME_ACTION_MOVE</code></b></dt>
// 	 * </dl>
// 	 * <table style="width:900px;border:1px solid black;border-collapse:collapse">
// 	 * <tr>
// 	 * <td style="text-align:center;border:1px solid black">Hash Key</td>
// 	 * <td style="text-align:center;border:1px solid black">Object</td></tr>
// 	 * <tr>
// 	 * <td style="text-align:center;border:1px solid black"><code>AmazonsGameMessage.QUEEN_POS_CURR</code></td>
// 	 * <td style="border:1px solid black">An <code>ArrayList &lt;Integer&gt; </code> of size 2, for the current position (row, column)
// 	 *   of the queen to be moved</td>
// 	 * </tr>
// 	 * <tr>
// 	 * <td style="text-align:center;border:1px solid black"><code>AmazonsGameMessage.QUEEN_POS_NEXT</code></td>
// 	 * <td style="border:1px solid black">An <code>ArrayList &lt;Integer&gt; </code> of size 2, for the new position (row, column)
// 	 *   for the queen to move to.</td>
// 	 * </tr>
// 	 * <tr>
// 	 * <td style="text-align:center;border:1px solid black"><code>AmazonsGameMessage.ARROW_POS</code></td>
// 	 * <td style="border:1px solid black">An <code>ArrayList &lt;Integer&gt; </code> of size 2, for the position (row, column)
// 	 *   of the arrow</td>
// 	 * </tr>
// 	 *  <caption></caption>
// 	 * </table>
// 	 */
// 	public abstract boolean handleGameMessage(String messageType, Map<String, Object> msgDetails);
	
// 	/**
// 	 * Error or timeout messages go here 
// 	 * @param msg - the text msg
// 	 * @return true - if the message can be handled properly 
// 	 */
// 	public boolean handleMessage(String msg){
// 		System.out.println(msg);
// 		return true;
// 	}

  
// 	/**
// 	 * Error, player timeout, and other system messages will be relayed here  
// 	 * If you don't care about the message type, do what you want in the method handleMessage(String msg)
// 	 * 
// 	 * @param msgType The type of the message
// 	 * @param msg the the message
// 	 * @return always returns true
// 	 */
// 	public boolean handleMessage(String msgType, String msg){
// 		handleMessage(msg);
// 		if(msgType.equalsIgnoreCase(SFSEvent.USER_COUNT_CHANGE)) {
// 			if(this.getGameGUI() != null) {
// 				getGameGUI().setRoomInformation(getGameClient().getRoomList());
// 			}
// 		}
// 		return true;
// 	}
	
	
// 	/**
// 	 * Subclass of the GamePlayer shall decide the type of the gameGUI and cast it according
// 	 * When overriding this method, call super.postSetup() before doing anything else
// 	 */
// 	public void postSetup() {
// 		//do not have to do anything 
// 		if(this.getGameGUI() != null) {
// 			getGameGUI().configureControlPanel(getGameGUI().getControlPanel());
// 		}
// 	}
	
// 	/**
// 	 * This method starts the game GUI (if there is one attached to the player) and then call 
// 	 * the method connect() to connect to the game server.
// 	 */
// 	public void Go() {
//   	 	postSetup();
//   	 	if(this.getGameGUI() != null) {
//   	 		getGameGUI().pack();
//   	 		getGameGUI().setVisible(true);
//   	 	}
//    	 	connect();  	 	
// 	}
	


// 	/** 
// 	 * 
// 	 * @return the GameClient registered with this player
// 	 */
// 	public abstract GameClient getGameClient();
	
// 	/**
// 	 * 
// 	 * @return the GameGUI attached to the player
// 	 */
// 	public abstract BaseGameGUI getGameGUI();
	
// 	/**
// 	 * create a game client and use "this" class (a GamePlayer) as the delegate.
// 	 * The game client will take care of the communication with the server. 
// 	 * See <code>GameClient.java</code>
// 	 */
// 	public abstract void connect();
	
	
// 	/**
// 	 * @return the user name that you have used to login onto the server
// 	 */
// 	public abstract String userName(); 

// }
