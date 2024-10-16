package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class RLGameTestRequestHandler extends BaseClientRequestHandler {

    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        String action = params.getUtfString("action");

        // Respond to the client
        if ("testAction".equals(action)) {
            SFSObject response = new SFSObject();
            response.putUtfString("message", "Test action received and processed successfully!");

            // Send response back to the client
            send("rl.action", response, user);
        }
    }
}