package ygraph.ai.smartfox.games.amazons;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * 
 * @author Yong Gao (yong.gao@ubc.ca)
 * Nov 7, 2020
 *
 */
 
public final class AmazonsBoard extends JPanel{
	private static final long serialVersionUID = 2L;
	
	//GamePlayer game = null; 
    //private BoardGameModel gameModel = null;
	
	boolean playerAMove;
	
	
	private javax.swing.JLabel[][] tileArray;
	public static final Color[] bgColor = {Color.DARK_GRAY, Color.WHITE, null, Color.CYAN};
	
    //private Monitor monitor;
    //javax.swing.JPanel jpanel;
    private javax.swing.ImageIcon whitequeen = null; //("white-queen.png");
    private javax.swing.ImageIcon blackqueen = null;
	private ImageIcon[] icon = null;
    
    
    /**
     * 
     */
	public AmazonsBoard() {
      	//this.initGameBoard();
      	this.setup();
	}
	
	//public void attachPlayer(GamePlayer gamePlayer) {
		//this.game = gamePlayer;		
	//}
	
	/**
	 * Set the position of the queens and the arrows according to information provided in 
	 * gameS --- a list representation of the 10X10 game matrix encoding the game state (from the server)
	 * @param gameS
	 */
    public void setGameState(ArrayList<Integer> gameS) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	for(int i = 1; i < 11; i++) {
            		for(int j = 1; j < 11; j++) {
            			
            			tileArray[i][j].setBackground(bgColor[Math.abs(i - j) % 2]);
//                        if(Math.abs(i - j) % 2 == 0){
//                       	 tileArray[i][j].setBackground(Color.DARK_GRAY); 
//                        }
//                        else{
//                       	 tileArray[i][j].setBackground(Color.WHITE);    
//                        }
            			
            			int a = gameS.get(11 * i + j);
            			
            			if(a == 0) {
            				tileArray[i][j].setIcon(null); 
            			}
            			else if(a == 1) {
            				tileArray[i][j].setIcon(blackqueen);
            			}
            			else if (a == 2) {
            				tileArray[i][j].setIcon(whitequeen);
            			}
            			else if (a == 3) {
            				tileArray[i][j].setBackground(AmazonsBoard.bgColor[a]);;
            			}
            			//tileArray[i][j].setBackground(bgColor[a]);
            		}
            	} 
            }
          });
    }
	
  
    /**
     * Update the position of the queens and arrows based on information msgDetails that contians
     * a move made the the opponent
     * @param queenCurrent
     * @param queenNew
     * @param arrow
     */
        public void updateGameState(ArrayList<Integer> queenCurrent, 
    								ArrayList<Integer> queenNew, 
    								ArrayList<Integer> arrow) {
        	//handle the event that the opponent makes a move. 
            //javax.swing.SwingUtilities.invokeLater(new Runnable() {
            //    public void run() {
        		//System.out.println("OpponentMove(): " + msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR));
        		//ArrayList<Integer> qcurr = msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
        		//ArrayList<Integer> qnew = msgDetails.get(AmazonsGameMessage.Queen_POS_NEXT);
        		//ArrayList<Integer> arrow = msgDetails.get(AmazonsGameMessage.ARROW_POS);
        		//System.out.println("QCurr: " + queenCurrent);
        		//System.out.println("QNew: " + queenNew);
        		//System.out.println("Arrow: " + arrow);
        		
        		//tileArray[qcurr.get(0)][qcurr.get(1)].setIcon(null);
        		tileArray[queenNew.get(0)][queenNew.get(1)].setIcon(tileArray[queenCurrent.get(0)][queenCurrent.get(1)].getIcon());
        		tileArray[queenCurrent.get(0)][queenCurrent.get(1)].setIcon(null);
        		tileArray[arrow.get(0)][arrow.get(1)].setBackground(AmazonsBoard.bgColor[3]);
                //}
            //});
        }
    	
    
    /**
     * Update the position of the queens and arrows based on information msgDetails that contians
     * a move made the the opponent
     * 
     * @param msgDetails
     */
    public void updateGameState(Map<String, Object> msgDetails) {
    	//handle the event that the opponent makes a move. 
    	//private void handleOpponentMove(Map<String, Object> msgDetails){
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
    		//System.out.println("OpponentMove(): " + msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR));
    		ArrayList<Integer> qcurr = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
    		ArrayList<Integer> qnew = (ArrayList<Integer>)msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
    		ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);
    		System.out.println("QCurr: " + qcurr);
    		System.out.println("QNew: " + qnew);
    		System.out.println("Arrow: " + arrow);
    		
    		//tileArray[qcurr.get(0)][qcurr.get(1)].setIcon(null);
    		tileArray[qnew.get(0)][qnew.get(1)].setIcon(tileArray[qcurr.get(0)][qcurr.get(1)].getIcon());
    		tileArray[qcurr.get(0)][qcurr.get(1)].setIcon(null);
    		tileArray[arrow.get(0)][arrow.get(1)].setBackground(AmazonsBoard.bgColor[3]);
            }
        });
    }
	
    //do the layout of the game board
    private void setup(){
    	initGameBoard();
    	javax.swing.GroupLayout jDesktopPane1Layout = new javax.swing.GroupLayout(this);
    	this.setLayout(jDesktopPane1Layout);
    
    	javax.swing.GroupLayout.SequentialGroup g = jDesktopPane1Layout.createSequentialGroup();
    
    	for(int j = 0; j < 11; j++){
    		javax.swing.GroupLayout.ParallelGroup gc = 
    				jDesktopPane1Layout.createParallelGroup(GroupLayout.Alignment.LEADING);   
    		for(int i = 10; i >= 0; i--){
    			gc = gc.addComponent(tileArray[i][j]);
    		}
    
    		g = g.addGroup(gc);              
    	}
    	jDesktopPane1Layout.setHorizontalGroup(g);
    
    
    	javax.swing.GroupLayout.SequentialGroup gv = 
    			jDesktopPane1Layout.createSequentialGroup();

    	for(int i = 10; i >= 0; i--){
    		javax.swing.GroupLayout.ParallelGroup gr = 
    				jDesktopPane1Layout.createParallelGroup(GroupLayout.Alignment.LEADING);
    		for(int j = 0; j < 11; j++){ 
    			gr = gr.addComponent(tileArray[i][j]); 
    		}
       
    		gv.addGroup(gr);
    	}
     
    	jDesktopPane1Layout.setVerticalGroup(gv);
    }
	
    /**
     * Set the images to use for the black and white queens
     * @param images --- images[0] for black and images[1] for white 
     */
    public void setQueenImage(ImageIcon[] images) {
    	this.icon[1] = images[0];
    	this.icon[2] = images[1];
    }
    
    protected void setTHandler(int i, int j,  MouseAdapter h) {
    	tileArray[i][j].addMouseListener(h);
    }
	
    //create the 10 by 10 game board
	private void initGameBoard(){
	    
        this.setBackground(new java.awt.Color(1.0f, 0.5f, 0.6f));
        this.setMinimumSize(new java.awt.Dimension(532, 530));
        this.setPreferredSize(new java.awt.Dimension(532, 530));
		
        icon = new ImageIcon[4]; 
        
		java.net.URL imageURL = AmazonsBoard.class.getResource("images/white-queen.png");
		
		if (imageURL != null) {
			whitequeen = new javax.swing.ImageIcon(imageURL);
			icon[2] = whitequeen;
		}
		
		imageURL = AmazonsBoard.class.getResource("images/black-queen.png");
		
		if (imageURL != null) {
			blackqueen = new javax.swing.ImageIcon(imageURL);
			icon[1] = blackqueen;
		}
	    	 
	    	
    	tileArray = new javax.swing.JLabel[11][11];
      
    	for(int i = 0; i < 11; i++) {
    		tileArray[i][0] = new javax.swing.JLabel(Integer.toString(i));
    		tileArray[0][i] = new javax.swing.JLabel(Character.toString(i + 96));
    		tileArray[0][i].setHorizontalAlignment(SwingConstants.CENTER);
    		
			 tileArray[0][i].setMinimumSize(new java.awt.Dimension(50,50));
			 tileArray[0][i].setOpaque(true); 
			 tileArray[i][0].setMinimumSize(new java.awt.Dimension(30,50));
			 tileArray[i][0].setOpaque(true); 
    	}
	  
    	java.awt.Dimension dimension = new java.awt.Dimension(50,50);
    	for(int i = 1; i < 11; i++){            
    		 for(int j = 1; j < 11; j++){
    			 tileArray[i][j] = new javax.swing.JLabel();//Integer.toString(i) + Integer.toString(j));       
    			 tileArray[i][j].setMinimumSize(dimension); //new java.awt.Dimension(50,50));
    			 tileArray[i][j].setMaximumSize(dimension); //new java.awt.Dimension(50,50));
    			 
    			 tileArray[i][j].setOpaque(true);  
 
    			 tileArray[i][j].setBackground(bgColor[Math.abs(i - j) % 2]);
                 tileArray[i][j].setEnabled(true);;
    		 }            
    	}       
	}//initGameBoard()	

}//end of class

