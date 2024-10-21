package ygraph.ai.smartfox.rl;

// import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.extensions.SFSExtension;

// This class creates the RL environment within the server and sets up request handlers for client logic
public class RLGameExtension extends SFSExtension {

    private RLGameManager gameManager;

    // Instantiation of RLGameManager
    public RLGameManager getGameManager() {
        return gameManager;
    }

    @Override
    public void init() {
        System.out.println("RLGameExtension initialized for COSC 322.");

        // Initialize the RLGameManager
        gameManager = new RLGameManager();
        System.out.println("RLGameManager initialized.");

        // Register request handlers
        addRequestHandler("rl.action", new RLGameRequestHandler(gameManager));
        addRequestHandler("rl.multi", new RLMultiHandler(gameManager));
    }

    @Override
    public void destroy() {
        System.out.println("RLGameExtension destroyed for COSC 322.");
        super.destroy();
    }
}