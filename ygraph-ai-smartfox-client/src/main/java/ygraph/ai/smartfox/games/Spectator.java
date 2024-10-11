package ygraph.ai.smartfox.games;

import java.util.ArrayList;
import java.util.Map;

import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

public class Spectator extends GamePlayer{

	private String userName = null;
	
	private BaseGameGUI gamegui = null;
	private GameClient gameClient = null;
	
	public Spectator() {
		gamegui = new BaseGameGUI(this);
	}

	@Override
	public void onLogin() {
        userName = gameClient.getUserName();
               
        if(gamegui != null) {                
        	gamegui.setRoomInformation(gameClient.getRoomList()); 
        }
	}

	@Override
	public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
		if(messageType.equals(GameMessage.GAME_STATE_BOARD)){
			ArrayList<Integer> gameS = (ArrayList<Integer>) msgDetails.get("game-state");
			if(getGameGUI() != null) {
				getGameGUI().setGameState(gameS);
			}
		}
		else if(messageType.equals(GameMessage.GAME_ACTION_MOVE)){
			//System.out.println(msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR));
	    	gamegui.updateGameState(msgDetails);
			//handleOpponentMove(msgDetails);
	    }
 		
		//getGameClient().leaveCurrentRoom();
		//getGameClient().logout();
		
		return false;
	}

	@Override
	public GameClient getGameClient() {
		return this.gameClient;
	}  

	@Override
	public BaseGameGUI getGameGUI() {
		return this.gamegui;
	}
	
	/**
	 * 
	 */
	public void connect() {
		// create a game client and use "this" class (a GamePlayer) as the delegate.
		// the game client client take care of the communication with the server.
		
		//use empty user name and passwd. The system will assign one for you
		gameClient = new GameClient("", "", this);
	}
	
	@Override
	public String userName() {
		return this.userName;
	}
	
    public static void main(String args[]) {
        
        try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(BaseGameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(BaseGameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(BaseGameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(BaseGameGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
                 
               
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
            	 Spectator s = new Spectator();
            	 s.Go();
             }
         });        
     }//end of main()
    	
}//end of class
