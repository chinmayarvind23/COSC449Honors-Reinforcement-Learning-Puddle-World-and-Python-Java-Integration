// package ygraph.ai.smartfox;


// import com.smartfoxserver.v2.extensions.SFSExtension;
 
// /**
//  *Tutorials on project setup and extension deployment can be found at 
//  *<ol>
//  *<li>http://docs2x.smartfoxserver.com/DevelopmentBasics/writing-extensions)
//  *<li>(http://docs2x.smartfoxserver.com/AdvancedTopics/server-side-extensions) 
//  *</ol>	
//  *Java Compiler Version Used Java 1.15 (Compatible with Java 1.8)				  
//  *Yong Gao(yong.gao@ubc.ca)
//  */
// public class COSC322Extension extends SFSExtension{

// 	public static final String zoneName = "cosc322-2";
	
// 	public COSC322Extension() {}

// 	@Override
// 	public void init() {
// 		trace("Server extension for COSC 322 (V2) at UBC Okanagan");
		
// 		this.addRequestHandler(GameMessage.GAME_STATE_JOIN, new COSC322MultiHandler(this));
// 	}
// }
