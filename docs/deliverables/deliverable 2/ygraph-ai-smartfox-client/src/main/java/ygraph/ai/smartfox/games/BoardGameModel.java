package ygraph.ai.smartfox.games;

/**
 * For testing purpose only
 * @author yongg
 *
 */
public class BoardGameModel extends GameModel {

	public static final String POS_MARKED_BLACK = "black";
	public static final String POS_MARKED_WHITE = "white";
	public static final String POS_MARKED_ARROW = "arrow";
	public static final String POS_AVAILABLE = "available";
	
	String[][] gameBoard = null; 
	//int[][] posScores = null;
	
	
	/**
	 * 
	 * @param rows
	 * @param columns
	 */
	public BoardGameModel(int rows, int columns){
		
		gameBoard = new String[rows + 1][columns + 1];
		//posScores = new int[rows][columns];
		for(int i = 1; i < rows + 1; i++){
			for(int j = 1; j < columns + 1; j++){
				gameBoard[i][j] = BoardGameModel.POS_AVAILABLE;
				//posScores[i][j] = 0;
			}
		}
	}
	
	
	public boolean positionMarked(int row, int column, int arow, int acol,
			 int qfr, int qfc, boolean opponentMove){
		boolean valid = true;
		
 
		
		if(row >= gameBoard.length | column >= gameBoard.length 
				 || row <= 0 || column <= 0){
			valid = false;
		}
		else if (!gameBoard[row][column].equalsIgnoreCase(BoardGameModel.POS_AVAILABLE)){
			valid = false;
		}
//		else if(opponentMove){
//			gameBoard[row][column] = BoardGameModel.POS_MARKED_OPP;
//		}
//		else{
//			gameBoard[row][column] = BoardGameModel.POS_MARKED_SELF;
//		}
        
		if(valid){
			gameBoard[row][column] = gameBoard[qfr][qfc];		
			gameBoard[qfr][qfc] = BoardGameModel.POS_AVAILABLE;		
			gameBoard[arow][acol] = BoardGameModel.POS_MARKED_ARROW;
		}
		
		//System.out.println(this.toString());
		
		return valid;
	}	
	
  public String toString(){
	  String b = null;
	  
	  for(int i = 1; i < 11; i++){
		  for(int j = 1; j< 11; j++){
			  b = b + gameBoard[i][j] + " ";
		  }
		  b = b + "\n";
	  }
	  
	  
	  return b;
  }
}
