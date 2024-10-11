// package ygraph.ai.smartfox.games;

// import java.util.TimerTask;

// /**
//  * An implementation of java.util.TimerTask for timeout notification.
//  * Used by GameClient. 
//  * For COSC322 project purpose, you do not need to use this class.
//  *  
//  * @author yong.gao@ubc.ca
//  *
//  */

// public class GameTimer extends TimerTask {

// 	public static final int TIMEOUT = 30000; // in milliseconds 
	
	
// 	private GamePlayer delegate = null;
// 	private GameClient gameClient = null;
	
// 	private int violationCount = 0;
	
// 	public GameTimer(GamePlayer delegate, GameClient gameClient, int numOfTimeOut){
// 		this.delegate = delegate;	
// 		this.gameClient = gameClient;
// 		this.violationCount = numOfTimeOut;
// 	}
	
	
// 	/**
// 	 * call GamePlayer's method handleMessage(String msg) when timeout 
// 	 */
// 	public void run() {
		
// 		violationCount++;
// 		gameClient.updateTimeOut();
		
//         try {
// 			delegate.handleMessage("Timeout Count = " + violationCount + " !!!");
// 		} catch (Exception e) {
// 			e.printStackTrace();
// 		}
// 	}

// }
