package ygraph.ai.smartfox;

import com.smartfoxserver.v2.extensions.SFSExtension;
import ygraph.ai.smartfox.rl.RLGameManager;
// import ygraph.ai.smartfox.rl.RLGameRequestHandler;
import ygraph.ai.smartfox.rl.RLMultiHandler;

/**
 * Server extension for COSC 322 (V2) at UBC Okanagan
 * This class forwards requests to room-level RLGameExtension.
 */
public class COSC322Extension extends SFSExtension {

    public static final String zoneName = "cosc322-2";

    @Override
    public void init() {
        System.out.println("Server extension for COSC 322 (V2) at UBC Okanagan");

        // Initialize the RLGameManager
        RLGameManager gameManager = new RLGameManager();
        RLMultiHandler rlMultiHandler = new RLMultiHandler(gameManager);

        // Register RLMultiHandler for join and disconnect
        addRequestHandler("rl.multi", rlMultiHandler);
    }

    @Override
    public void destroy() {
        System.out.println("COSC322Extension destroyed for Zone level.");
        super.destroy();
    }
}
