package ygraph.ai.smartfox.gaming;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
//import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Calendar;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import ygraph.ai.smartfox.games.AmazonGameMessage;

public class AmazonsGame {

	public User playerB = null;
	public User playerW = null;
	
	
	public String roomName = "default";
	
	public boolean started = false;
	public boolean completed = false;
	
	public ArrayList<ArrayList<Integer>> moveRecord = null;
	public int[][] state = null;
	
	
	private File gameHistory = null;
	private PrintWriter out = null;
	
	public final static int WHITE = 2;
	public final static int BLACK = 1;
	public final static int ARROW = 3;
	
	
	/**
	 * initialize the game board info: state 
	 */
	public AmazonsGame() {
		state = new int[11][11];
		state[4][1] = AmazonsGame.WHITE;
		state[4][10] = AmazonsGame.WHITE;
		state[1][4] = AmazonsGame.WHITE;
		state[1][7] = AmazonsGame.WHITE;
		
		state[7][1] = AmazonsGame.BLACK;
		state[7][10] = AmazonsGame.BLACK;
		state[10][4] = AmazonsGame.BLACK;
		state[10][7] = AmazonsGame.BLACK;
	}
	
	/**
	 * 
	 * @param playerB First player in the room, and plays Black
	 */
	public AmazonsGame(User playerB){
		this();
		this.playerB = playerB;
	}
	
	
	public AmazonsGame(User playerB, String roomName){
		this(playerB);
		this.roomName = roomName;		
	}
	
	
	/**
	 * 
	 * @param playerW Seond player in the room, playing White
	 */
	public void setSecondPlayer(User playerW){
		this.playerW = playerW;
	}
	
	public void startGame(){
		moveRecord =  new ArrayList<ArrayList<Integer>>(); 		
					
		String fileName = playerB.getName() + "-" + playerW.getName();
		
		fileName = "game-records-322-2021" + File.separatorChar + fileName +  "-" + roomName 
		   + "-" + Calendar.getInstance().getTimeInMillis(); 
		
		gameHistory = new File(fileName);
		
		try {
			out = new PrintWriter(gameHistory);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String header = "// Game played by " + playerB.getName() + " and " + playerW.getName() + " on ";
		header = header + Calendar.getInstance().getTime().toString();
		
		out.println(header);
		
		started = true;
	}
	
	
	public void recordMove(ISFSObject req){
		ArrayList<Integer> qcurr = (ArrayList<Integer>)req.getIntArray(AmazonGameMessage.QUEEN_POS_CURR);
		ArrayList<Integer> qnext = (ArrayList<Integer>)req.getIntArray(AmazonGameMessage.Queen_POS_NEXT);
		ArrayList<Integer> arrow = (ArrayList<Integer>)req.getIntArray(AmazonGameMessage.ARROW_POS); 
		
		moveRecord.add(qcurr);
		moveRecord.add(qnext);
		moveRecord.add(arrow);
		
		//update the state array
		this.state[qnext.get(0)][qnext.get(1)] = state[qcurr.get(0)][qcurr.get(1)];
		state[qcurr.get(0)][qcurr.get(1)] = 0;
		state[arrow.get(0)][arrow.get(1)] = AmazonsGame.ARROW;
		
		
        String move = qcurr.toString() + qnext.toString() + arrow.toString(); 
        out.println(move);
	}
	
	public void  finish(){
		completed = true;
		if(out != null){
			out.flush();
			out.close();
		}
	}
	
	public ArrayList<Integer> stateToArrayList(){
		ArrayList<Integer> s = new ArrayList<Integer>();
		for(int i = 0; i < 11; i++){
			for(int j = 0; j < 11; j++){
				s.add(state[i][j]);
			}
		}
		
		return s;
	}
	
}//end of class
