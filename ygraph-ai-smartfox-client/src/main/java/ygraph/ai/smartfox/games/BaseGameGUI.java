package ygraph.ai.smartfox.games;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import sfs2x.client.entities.Room;
// import ygraph.ai.smartfox.games.amazons.AmazonsBoard;

/**
 * Base Game GUI - A JFrame with two Panels: the Game Board (Amazons) and the Control Panel.
 * The control panel has a component listing game rooms on the server, together with the number 
 * of players and number of spectators in the rooms. To join a room, single click on the room name.  
 * 
 * 
 * @author Yong Gao (yong.gao@ubc.ca)
 * Nov 7, 2020
 *
 */
 
public class BaseGameGUI extends javax.swing.JFrame{
		 
	private static final long serialVersionUID = -3304116179294383074L;
	
	private GamePlayer gameplayer; 
    private AmazonsBoard gameboard = null;
     
    protected javax.swing.JList<String> roomSelection;
    private JPanel controlPanel;
    
    /**
     * 
     */
    public BaseGameGUI() {
        initComponents();         
    }
    
    /**
     * Create the GUI and attach a GamePlayer
     * @param player --- a GamePlayer 
     */
    public BaseGameGUI(GamePlayer player) {    
        gameplayer = player; 
        initComponents();
    }
    
    
    /**
     * The control panel contain components related to controlling the games
     * Mess around with the JPanel at your own risk
     *    
     */
    public void configureControlPanel(JPanel cPanel) { 
    	
    	roomSelection = new javax.swing.JList<>();
        roomSelection.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3"}));
        roomSelection.setMinimumSize(new java.awt.Dimension(50,20));
        roomSelection.setToolTipText("Select a game room");
        roomSelection.addMouseListener(new RoomSelectionListener());
        
    	javax.swing.GroupLayout layout = new javax.swing.GroupLayout(cPanel);
    	cPanel.setLayout(layout);
    
    	javax.swing.GroupLayout.SequentialGroup g = layout.createSequentialGroup();
    	g.addComponent(this.roomSelection);              
    	layout.setHorizontalGroup(g);
    
    
    	javax.swing.GroupLayout.SequentialGroup gv = 
    			layout.createSequentialGroup(); 
    	gv.addComponent(this.roomSelection);
    	layout.setVerticalGroup(gv);
    } 
    
    /**
     * 
     * @param m
     */
    private void setRoomInformation(DefaultListModel<String> m) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() { 
    		public void run() {
    			roomSelection.removeAll();
    			roomSelection.setModel(m);
    		}
    	});
    }
    
    /**
     * Set or update the room information  and other information for the GUI to use 
     * 
     * @param rooms - a list of Rooms information about game rooms on the server  
     */
    public void setRoomInformation(List<Room> rooms) {   			
    	setTitle("Game of the Amazons (" + gameplayer.getGameGUI().getClass().getSimpleName() + "-" + gameplayer.userName() + ")");
    			
    	DefaultListModel<String> m = new DefaultListModel<>();  
    	rooms.forEach(room -> {m.addElement(room.getName() + "(" + 
    	        			  		room.getUserCount() + "," + 
    	        			  		room.getSpectatorCount() + ")");
    	        			  }
    	);                
    	setRoomInformation(m);  			    			
    }
    

    /**
     * Initialize the game board
     * @param gameS - List of the 10 by 10 array that encodes the initial position of the queens
     */
    public void setGameState(ArrayList<Integer> gameS) {
    	javax.swing.SwingUtilities.invokeLater(
    			new Runnable() { 
    				public void run() {
    					gameboard.setGameState(gameS);
    				}
    			}
    	);
    }
    
 
    /**
     * Update the game board based on a player's move
     * @param queenCurrent --- current position of queen
     * @param queenNew --- new position of queen
     * @param arrow --- position of the arrow
     */
    public void updateGameState(ArrayList<Integer> queenCurrent, 
    							ArrayList<Integer> queenNew, 
    							ArrayList<Integer> arrow) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() { 
    		public void run() {
    			gameboard.updateGameState(queenCurrent, queenNew, arrow);
    		}
    	});    
    }
    
    
    /**
     * Update the game board based on a player's move
     * @param msgDetails --- a Map storing information about the move 
     */
    public void updateGameState(Map<String, Object> msgDetails) {
    	javax.swing.SwingUtilities.invokeLater(new Runnable() { 
    		public void run() {
    			gameboard.updateGameState(msgDetails);
    		}
    	});   
    }
    
    public JPanel getControlPanel() {
    	return this.controlPanel;
    }

    
	protected AmazonsBoard createBoard() {
		return  new AmazonsBoard();	
	}
	
	
 
	//
	private void initComponents(){
	        
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	    setTitle("Game of the Amazons (" + this.getClass().getSimpleName() + ")");
	    gameboard = createBoard();
	    
	    //gamePanel = new JPanel();
	    //gamePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	 
	    controlPanel = new JPanel();
	    controlPanel.setBackground(new java.awt.Color(30, 160, 160));
	    
	    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	    getContentPane().setLayout(layout);
	    
	    layout.setHorizontalGroup(
	        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
	            .addGap(25, 25, 25)
	            .addComponent(gameboard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
	            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 180, Short.MAX_VALUE)
	            .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
	    );
	    
	    layout.setVerticalGroup(
	        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	        .addGroup(layout.createSequentialGroup()
	            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                .addGroup(layout.createSequentialGroup()
	                    .addGap(19, 19, 19)
	                    .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGroup(layout.createSequentialGroup()
	                    . addGap(19, 19, 19)
	                    .addComponent(gameboard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
	                   .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	    ); 
	}//end of initComponents

	/**
	 * 
	 * @author Yong Gao (yong.gao@ubc.ca)
	 * Nov 13, 2020
	 *
	 */
	class RoomSelectionListener extends java.awt.event.MouseAdapter{
	    public void mouseClicked(MouseEvent e) {
	        if (e.getClickCount() == 1) {
	        	//javax.swing.SwingUtilities.invokeLater(new Runnable() { 
	        	//	public void run() {
	        		int index = roomSelection.locationToIndex(e.getPoint());
	        		System.out.println(roomSelection.getSelectedValue());
	        		//System.out.println("Single clicked on Item " + index);            
	        		String rm = (String)roomSelection.getSelectedValue(); 
	        		
	        		gameplayer.getGameClient().leaveCurrentRoom();
	        		
	      	  		if(rm != null) {
	      	  			rm = rm.substring(0, rm.lastIndexOf("("));
	      	  			System.out.println(rm);
	      	  			gameplayer.getGameClient().joinRoom(rm); 
	      	  		}
	        	//}} );
	        }
	    } 
	} 

	/**
	 * 
	 */
	public static void sys_setup() {
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
	}
	
}// end of class
