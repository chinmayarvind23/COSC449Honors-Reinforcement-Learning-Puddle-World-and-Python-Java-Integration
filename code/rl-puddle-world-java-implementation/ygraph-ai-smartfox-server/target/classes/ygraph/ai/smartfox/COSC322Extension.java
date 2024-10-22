package ygraph.ai.smartfox;

import com.smartfoxserver.v2.extensions.SFSExtension;
import ygraph.ai.smartfox.rl.RLMultiHandler;

// Handles RLMultiHandler events by delegating to RLMultiHandler
public class COSC322Extension extends SFSExtension {

    public static final String zoneName = "cosc322-2";

    @Override
    public void init() {
        System.out.println("Server extension for COSC 322 (V2) at UBC Okanagan");

        // Registering RL MultiHandler with null RLGameManager
        RLMultiHandler rlMultiHandler = new RLMultiHandler(null);
        addRequestHandler("rl.multi", rlMultiHandler);
    }

    @Override
    public void destroy() {
        System.out.println("COSC322Extension destroyed for Zone level.");
        super.destroy();
    }
}
