 
// package ygraph.ai.smartfox.games.amazons;

// import java.awt.event.MouseAdapter;
// import java.awt.event.MouseEvent;
// import java.util.ArrayList;
// import java.util.Map;

// import javax.swing.JFrame;
// import ygraph.ai.smartfox.games.BaseGameGUI;
// import ygraph.ai.smartfox.games.GameClient;
// import ygraph.ai.smartfox.games.GameMessage;
// import ygraph.ai.smartfox.games.GamePlayer;

// /**
//  *  
//  * @author Yong Gao (yong.gao@ubc.ca)
//  * Nov 9, 2020
//  *
//  */
// public final class HumanPlayer extends GamePlayer{
   
// 	public GameGUI gm;
// 	private GameClient gameClient; 
	
// 	private Map<String, ArrayList<Integer>> moveDetails;
// 	private ArrayList<Integer> queenfrom = null;
// 	private ArrayList<Integer> queennew = null;
// 	private ArrayList<Integer> arrow = null;
// 	private int counter;
	
// 	private String userName = "";
	
// 	private AmazonsBoard gameb = null; //a workaround to access it in postSetup(), don't touch it unless absolute necessary


// 	/**
// 	 * 
// 	 */
// 	public HumanPlayer(){
// 		//userName = "cosc322b";
// 		gm = new GameGUI(this);
// 	}
	 
	
// 	/**
// 	 * Attach a GUI (of the type JFrame) to the player 
// 	 * @param gameGui
// 	 */
// 	public void setGameGui(JFrame gameGui) {
// 		gm = (GameGUI) gameGui;
// 	}

// 	public BaseGameGUI getGameGUI() {
// 		return this.gm;
// 	}
	
	
// 	public void postSetup() {
// 		super.postSetup();
		
// 		//gameb.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
// 		System.out.println("post-setup");
	    
// 		counter = 0;   
// 	    PlayerMoveHandler  moveHandler;
// 	    for(int i = 1; i < 11; i++) {
// 	    	for(int j = 1; j < 11; j++) {
// 	    		moveHandler = new PlayerMoveHandler(i, j, this);
// 	    		gameb.setTHandler(i, j, moveHandler);
// 	    	}
// 	    }
	    
// 	    //gameGUI.pack();
// 	    //gameGUI.setVisible(true);
// 	}

// 	/**
// 	 * 
// 	 */
// 	public void connect() {
// 		// create a game client and use "this" class (a GamePlayer) as the delegate.
// 		// the game client client take care of the communication with the server.
		
// 		//gameClient = new GameClient(userName, "cosc322", this);
		
// 		//use empty user name and passwd. The system will assign one for you
// 		gameClient = new GameClient(userName, "cosc322", this);
// 	}
     
// 	//@Override
// 	public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
// 	//This method will be called by the GameClient when it receives a game-related message
// 	//from the server.
	
// 	//For a detailed description of the message types and format, 
// 	//see the method GamePlayer.handleGameMessage() in the game-client-api document. 
		
// 		//System.out.println("Game State: " +  msgDetails.toString());
		
// 		if(messageType.equals(GameMessage.GAME_STATE_BOARD)){
// 			ArrayList<Integer> gameS = (ArrayList<Integer>) msgDetails.get("game-state");
// 			System.out.println("Game Board: " + gameS);
// 			gm.setGameState(gameS);
// 		}
// 		else if(messageType.equals(GameMessage.GAME_ACTION_START)){	 
			
// 			System.out.println("Game Start: Black Played by " + msgDetails.get(AmazonsGameMessage.PLAYER_BLACK));
// 			System.out.println("Game Start: White Played by " + msgDetails.get(AmazonsGameMessage.PLAYER_WHITE));
// 			System.out.println("Timer Started on Black");
// 			//if(((String) msgDetails.get("player-black")).equals(this.userName())){
// 			//	System.out.println("Game Start: " +  msgDetails.get("player-black"));
// 			//}
						
// 		}
// 		else if(messageType.equals(GameMessage.GAME_ACTION_MOVE)){
// 			System.out.println(msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR));
// 	    	gm.updateGameState(msgDetails);
// 			//handleOpponentMove(msgDetails);
// 	    }
// 		else if(messageType.equals("user-count-change")) {
// 	        //DefaultListModel<String> m = new DefaultListModel<>(); 
// 	        //gameClient.getRoomList().forEach(room -> {m.addElement(room.getName() + "(" + room.getUserCount() + room.getSpectatorCount() + ")");});                
// 	        //gm.setRoomInformation(m); 
// 	        gm.setRoomInformation(gameClient.getRoomList());
// 		}
// 		return true;	
// 	}

//     //@Override
//     public void onLogin() {
//     	System.out.println("Congratualations!!! "
//     			+ "I am called because the server indicated that the login is successfully");
//     	System.out.println("The next step is to find a room and join it: "
//     			+ "the gameClient instance created in my constructor knows how!");
//         System.out.println(gameClient.getRoomList());
        
//         userName = gameClient.getUserName();
        
//         //DefaultListModel<String> m = new DefaultListModel<>(); 
//         //gameClient.getRoomList().forEach(room -> {m.addElement(room.getName() + "(" + room.getUserCount() + room.getSpectatorCount() + ")");});                
//         //gm.setRoomInformation(m);
//         gm.setRoomInformation(gameClient.getRoomList());
//     }

    
    
//     public String userName() {
//     	return userName;
//     }
    
//     public GameClient getGameClient() {
//     	return this.gameClient;
//     }
 
    
 
// 	/* Handle mouse events
// 	 * 
// 	 * @author yongg
// 	*/
// 	class PlayerMoveHandler extends MouseAdapter{
	 		    		
// 	    int idi = 0;
// 	    int idj = 0;
	    
// 	    HumanPlayer mn;
	    
// 	    public PlayerMoveHandler(int idi, int idj, GamePlayer mn) {
// 	    	this.idi = idi;
// 	    	this.idj = idj;
// 	    	this.mn = (HumanPlayer)mn;
// 	    }
			    
//         public void mousePressed(MouseEvent e) {
        	
//         	//if(!gameStarted){
//         		//return; 
//         	//}
          
//         	if(mn.counter == 0){
//                 	//mn.moveDetails = new HashMap<String, ArrayList<Integer>>(); 
//             	queenfrom = new ArrayList<>();
//             	queenfrom.add(0, idi);
//             	queenfrom.add(1, idj);
//             	//mn.moveDetails.put(AmazonsGameMessage.QUEEN_POS_CURR, queenfrom);	
            	
//             	mn.counter++;
//             }
//             else if(mn.counter == 1){
//             	queennew = new ArrayList<>();
//             	queennew.add(0, idi);
//             	queennew.add(1, idj);
//             	//mn.moveDetails.put(AmazonsGameMessage.Queen_POS_NEXT, queennew);
            	
//             	counter++;
//             }
//             else if (mn.counter == 2){
//             	arrow = new ArrayList<>();
//             	arrow.add(0, idi);
//             	arrow.add(1, idj);
//             	//mn.moveDetails.put(AmazonsGameMessage.ARROW_POS, arrow);
//             	counter++;
//             }
            
//             if(mn.counter == 3){
//               mn.counter = 0; 		               
//               mn.gm.updateGameState(queenfrom, queennew, arrow);
              	        		
//     		//To send a move message, call this method with the required data  
//     		//mn.getGameClient().sendMoveMessage(qf, qn, ar);
//             //mn.getGameClient().sendMoveMessage(mn.moveDetails);
              
//               mn.getGameClient().sendMoveMessage(queenfrom, queennew, arrow);
//               queenfrom = null;
//               queennew = null;
//               arrow = null;
//             }
//         }			 
// 	}//end of GameEventHandler	

		
		
// 	public static void main(String args[]) {
	        
// 	        try {
// 	             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
// 	                 if ("Nimbus".equals(info.getName())) {
// 	                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
// 	                     break;
// 	                 }
// 	             }
// 	         } catch (ClassNotFoundException ex) {
// 	             java.util.logging.Logger.getLogger(BaseGameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
// 	         } catch (InstantiationException ex) {
// 	             java.util.logging.Logger.getLogger(BaseGameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
// 	         } catch (IllegalAccessException ex) {
// 	             java.util.logging.Logger.getLogger(BaseGameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
// 	         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
// 	             java.util.logging.Logger.getLogger(BaseGameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
// 	         }
	                 
	               
// 	         java.awt.EventQueue.invokeLater(new Runnable() {
// 	             public void run() {
// 	            	 HumanPlayer m = new HumanPlayer();
// 	            	 m.Go();
// 	            	 //GameMonitor gm = new GameMonitor(new Monitor());
// 	            	 //monitor.postSetup(this);
// 	            	 //monitor.connect();
// 	                 //gm.setVisible(true);
// 	             }
// 	         });        
// 	 }
	    
// 	    /**
// 	     * 
// 	     * @author Yong Gao (yong.gao@ubc.ca)
// 	     * Nov 15, 2020
// 	     *
// 	     */
// 	    private class GameGUI extends BaseGameGUI{

// 			public GameGUI(HumanPlayer humanPlayer) {
// 				super(humanPlayer);
// 			}
	    	
// 			protected AmazonsBoard createBoard() {
// 				System.out.println("GameGUI");
// 				gameb = new AmazonsBoard();
// 				return gameb;
// 			}
 
// 	     	//protected AmazonsBoard getGameBoard() {
// 			//return super.;
// 	     	//}
// 	    }
	    
	    
// }//end of class: HumanPlayer
