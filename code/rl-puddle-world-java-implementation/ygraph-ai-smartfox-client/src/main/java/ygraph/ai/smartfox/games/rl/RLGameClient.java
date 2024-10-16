package ygraph.ai.smartfox.games.rl;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.entities.Room;

import java.util.HashMap;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class RLGameClient {
    private SmartFox sfs;

    public RLGameClient() {
        // Initialize SmartFox client
        sfs = new SmartFox();

        // Add event listeners
        sfs.addEventListener(SFSEvent.CONNECTION, this::onConnection);
        sfs.addEventListener(SFSEvent.LOGIN, this::onLogin);
        sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, this::onExtensionResponse);

        // Connect to SmartFoxServer
        sfs.connect("localhost", 9933);
    }

    // Handle connection success or failure
    private void onConnection(BaseEvent evt) {
        if ((Boolean) evt.getArguments().get("success")) {
            System.out.println("Connected to SmartFoxServer!");

            // Send a login request (replace with your zone name and username)
            sfs.send(new LoginRequest("TestUser", "pass1", "cosc322-2"));
        } else {
            System.out.println("Failed to connect.");
        }
    }

    // Handle successful login
    private void onLogin(BaseEvent evt) {
        System.out.println("Successfully logged in!");

        // Join the RLRoom after login (replace with your room name)
        Room room = sfs.getRoomByName("RLRoom");
        if (room != null) {
            // Send a test request to the RL extension
            SFSObject data = new SFSObject();
            data.putUtfString("action", "testAction");
            sfs.send(new ExtensionRequest("rlRequest", data, room));
        } else {
            System.out.println("Room not found.");
        }
    }

    // Handle responses from the extension
    private void onExtensionResponse(BaseEvent evt) {
        String cmd = (String) evt.getArguments().get("cmd");
        @SuppressWarnings("unchecked")
        HashMap<String, Object> params = (HashMap<String, Object>) evt.getArguments().get("params");

        if (cmd.equals("rl.action")) {
            System.out.println("Received response from extension: " + params.toString());
        }
    }

    public static void main(String[] args) {
        new RLGameClient();
    }
}