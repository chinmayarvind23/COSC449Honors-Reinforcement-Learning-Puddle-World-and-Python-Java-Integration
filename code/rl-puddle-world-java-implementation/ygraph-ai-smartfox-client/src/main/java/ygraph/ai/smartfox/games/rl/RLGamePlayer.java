package ygraph.ai.smartfox.games.rl;

import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.IEventListener;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.requests.ExtensionRequest;
// import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.requests.LogoutRequest;
import com.smartfoxserver.v2.exceptions.SFSException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// This class represents an RL agent's core logic and interactions with the Puddle World environment along with handling the connection and message processing to and from the server.
// This would be provided to the students with the connection details filled in, but the core logic would be left to them to implement
public class RLGamePlayer implements IEventListener {

    // Connectio params
    private String userName = "";
    private String password = "";
    private String serverIP = "localhost";
    private int serverPort = 9933;
    private String zoneName;
    private String roomName;

    // Creating a smartfox instance to connect to the server
    private SmartFox smartFox;

    // Current User and Room
    private User currentUser;
    private Room currentRoom;

    // RL Components: model representation of the game, Q-table and V-table
    protected RLGameModel gameModel;
    private Map<Integer, double[]> qTable;
    private Map<Integer, Double> vTable;

    // Learning Parameters: learning rate (prioritizes immediate over future rewards), discount factor (future rewards prioritized over immediate rewards), exploration rate (probability of choosing random action over best action given current knowledge of puddle world)
    // Set in server, so students don't need to worry about this
    private double alpha = 0.1;
    private double gamma = 0.9; 
    private double epsilon = 1;

    // RLGamePlayer constructor that takes in username, password, IP, port, zone name, and room name as params
    public RLGamePlayer(String userName, String password, String serverIP, int serverPort, String zoneName, String roomName) {
        this.userName = userName;
        this.password = password;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.zoneName = zoneName;
        this.roomName = roomName;

        // Initializes RL game model, Q-Table, and V-Table
        this.gameModel = new RLGameModel();
        this.qTable = new HashMap<>();
        this.vTable = new HashMap<>();
        initializeQTable();
        initializeVTable();

        // Initializes the SmartFox client and event listeners for different events and for communication with the server
        this.smartFox = new SmartFox(false);
        this.smartFox.addEventListener(SFSEvent.CONNECTION, this);
        this.smartFox.addEventListener(SFSEvent.LOGIN, this);
        this.smartFox.addEventListener(SFSEvent.LOGIN_ERROR, this);
        this.smartFox.addEventListener(SFSEvent.ROOM_JOIN, this);
        this.smartFox.addEventListener(SFSEvent.ROOM_JOIN_ERROR, this);
        this.smartFox.addEventListener(SFSEvent.EXTENSION_RESPONSE, this);

        // Connects to the server
        this.smartFox.connect(this.serverIP, this.serverPort);
    }

    // Q-table initialization with random values
    // State IDs range: (0, 399) [400 states on 20x20 grid]
    // actions => 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
    // Given to students
    private void initializeQTable() {
        for (int state = 0; state < 400; state++) {
            double[] actions = new double[4];
            for (int a = 0; a < 4; a++) {
                actions[a] = Math.random();
            }
            qTable.put(state, actions);
        }
    }

    // V-table initialized with 0 values
    // Given to students
    private void initializeVTable() {
        for (int state = 0; state < 400; state++) {
            vTable.put(state, 0.0);
        }
    }

    // Handles events from the SmartfoxServer like connection, login, errors, and responses from the server side extension
    // Given to students
    @Override
    public void dispatch(BaseEvent event) throws SFSException {
        String eventType = event.getType();

        switch (eventType) {
            case SFSEvent.CONNECTION:
                handleConnection(event);
                break;
            case SFSEvent.LOGIN:
                handleLogin(event);
                break;
            case SFSEvent.LOGIN_ERROR:
                handleLoginError(event);
                break;
            case SFSEvent.ROOM_JOIN:
                handleRoomJoin(event);
                break;
            case SFSEvent.ROOM_JOIN_ERROR:
                handleRoomJoinError(event);
                break;
            case SFSEvent.EXTENSION_RESPONSE:
                handleExtensionResponse(event);
                break;
            default:
                break;
        }
    }

    // Handles the CONNECTION event by sending a login request with the username, password, and zone name to the server
    // with a retry mechanism that retries the connection in 5 seconds
    // Given to students
    private void handleConnection(BaseEvent event) {
        boolean success = (Boolean) event.getArguments().get("success");
        if (success) {
            System.out.println("Connected to SmartFoxServer.");
            LoginRequest loginReq = new LoginRequest(this.userName, this.password, this.zoneName);
            smartFox.send(loginReq);
        } else {
            System.out.println("Connection failed. Retrying...");
            try {
                Thread.sleep(5000);
                this.smartFox.connect(this.serverIP, this.serverPort);
            } catch (InterruptedException e) {
                System.out.println("Reconnection attempt interrupted.");
            }
        }
    }

    private void joinRoom() {
        ISFSObject params = new SFSObject();
        params.putUtfString("messageType", "join");
        params.putUtfString("room.name", this.roomName);
        params.putUtfString("room.password", this.password.trim());
        ExtensionRequest joinReq = new ExtensionRequest("rl.multi", params, null);
        smartFox.send(joinReq);
        
        System.out.println("Sent JOIN request for room: " + this.roomName);
    }

    // Handles the LOGIN event by sending a join request for the current user to a particular room
    private void handleLogin(BaseEvent event) {
        this.currentUser = (User) event.getArguments().get("user");
        System.out.println("Logged in as: " + currentUser.getName());

        // ISFSObject params = new SFSObject();
        // params.putUtfString("messageType", "join");
        // params.putUtfString("room.name", this.roomName);
        // params.putUtfString("room.password", this.password.trim());
        // ExtensionRequest joinReq = new ExtensionRequest("rl.action", params, this.currentRoom);
        // smartFox.send(joinReq);
        
        System.out.println("Sent LOGIN request with password: " + this.password);
        joinRoom();
    }

    // Handles the LOGIN_ERROR event by sending an error message to the console
    private void handleLoginError(BaseEvent event) {
        String errorMessage = (String) event.getArguments().get("errorMessage");
        System.out.println("Login failed: " + errorMessage);
    }

    // Handles the ROOM_JOIN event by logging the user joining a room and starts the RL episode
    private void handleRoomJoin(BaseEvent event) {
        this.currentRoom = (Room) event.getArguments().get("room");
        System.out.println("Joined room: " + currentRoom.getName());

        // // Room joined, now send extension request
        // ISFSObject params = new SFSObject();
        // params.putUtfString("command", "join");
        // params.putUtfString("room.name", this.roomName);
        // params.putUtfString("room.password", this.password);
        // ExtensionRequest joinReq = new ExtensionRequest("rl.action", params, this.currentRoom);
        // smartFox.send(joinReq);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Requesting initial state.....");
        requestInitialState();
    }

    // Handles the ROOM_JOIN_ERROR event by sending an error message to the console
    private void handleRoomJoinError(BaseEvent event) {
        String errorMessage = (String) event.getArguments().get("errorMessage");
        System.out.println("Failed to join room: " + errorMessage);
    }

    // Handles different responses from the server-side extension (EXTENSION_RESPONSE event)
    // by getting the type of message sent by the server and the params extracted from the event containing the server response data for a specific room
    // Given to students
    private void handleExtensionResponse(BaseEvent event) {
        String cmd = (String) event.getArguments().get("cmd");
        ISFSObject params = (ISFSObject) event.getArguments().get("params");
    
        System.out.println("Received EXTENSION_RESPONSE: cmd=" + cmd);
    
        String messageType = params.getUtfString("messageType");
    
        if ("rl.action".equals(cmd)) {
            // Handle RLGameRequestHandler responses
            switch (messageType) {
                case "GAME_STATE_RESPONSE":
                    processGameState(params);
                    break;
                case "GAME_AVAILABLE_ACTIONS_RESPONSE":
                    processAvailableActions(params);
                    break;
                case "GAME_AVAILABLE_REWARDS_RESPONSE":
                    processAvailableRewards(params);
                    break;
                case "GAME_ACTION_REWARD_RESPONSE":
                    processActionReward(params);
                    break;
                case "GAME_FINAL_STATE_RESPONSE":
                    processFinalState(params);
                    break;
                case "GAME_RESET_RESPONSE":
                    processReset(params);
                    break;
                case "GAME_ERROR":
                    processError(params);
                    break;
                case "GAME_INFO_RESPONSE":
                    processInfo(params);
                    break;
                default:
                    System.out.println("Unknown messageType: " + messageType);
                    break;
            }
        }else if ("rl.multi".equals(cmd)) {
            // Handle RLMultiHandler responses
            switch (messageType) {
                case "join.success":
                    processJoinSuccess(params);
                    break;
                case "disconnect":
                    processDisconnectResponse(params);
                    break;
                default:
                    System.out.println("Unknown messageType in rl.multi: " + messageType);
                    break;
            }
        } else if ("rl.error".equals(cmd)) {
            processError(params);
        } else {
            System.out.println("Unknown cmd: " + cmd);
        }
    }
    
    private void processDisconnectResponse(ISFSObject params) {
        System.out.println("Processed disconnect response.");
    }
    

    private void processJoinSuccess(ISFSObject params) {
        System.out.println("Processed join response.");
        System.out.println("Requesting initial state...");
        requestInitialState();
    }    

    // Handles error messages from the server
    // Given to students
    private void processError(ISFSObject params) {
        RLClientGameMessage msg = new RLClientGameMessage();
        msg.fromSFSObject(params);
        String error = params.getUtfString("error");
        System.out.println("Received GAME_ERROR: " + error);
    }

    // Handles info messages about the game from the server
    // Given to students
    private void processInfo(ISFSObject params) {
        RLClientGameMessage msg = new RLClientGameMessage();
        msg.fromSFSObject(params);
        double cumulativeReward = msg.getCumulativeReward();
        int stepsThisEpisode = msg.getStepsThisEpisode();
        int totalEpisodes = msg.getTotalEpisodes();
        int successfulEpisodes = msg.getSuccessfulEpisodes();
    
        System.out.println("Episode " + totalEpisodes + " Summary:");
        System.out.println(" - Cumulative Reward: " + cumulativeReward);
        System.out.println(" - Steps Taken: " + stepsThisEpisode);
        System.out.println(" - Successful Episodes: " + successfulEpisodes);
        System.out.println(" - Total Episodes: " + totalEpisodes);

        if (totalEpisodes % 100 == 0) {
            sendGameInfoRequest();
        }
    }    

    // Asks for the initial state of the game from the server through an extension request
    // Given to students
    private void requestInitialState() {
        RLClientGameMessage initialStateReq = new RLClientGameMessage(RLClientGameMessage.GAME_STATE);
        initialStateReq.setUserName(this.userName);
        ISFSObject params = initialStateReq.toSFSObject();
        ExtensionRequest req = new ExtensionRequest("rl.action", params, this.currentRoom);
        smartFox.send(req);
        System.out.println("Requested initial state.");
    }

    // Processes the GAME_STATE message by calling the requestAvailable actions method to get the possible states from the current state
    // Partially given to students
    private void processGameState(ISFSObject params) {
        RLClientGameMessage msg = new RLClientGameMessage();
        msg.fromSFSObject(params);
        this.gameModel.updateState(msg.getStateId());
        System.out.println("Current State ID: " + msg.getStateId());
        System.out.println("Requesting available actions for state: " + msg.getStateId());
        requestAvailableActions(msg.getStateId());
    }

    // Asks for the available states from the current state through an extension request
    // Partially given to students
    private void requestAvailableActions(int stateId) {
        RLClientGameMessage actionsReq = new RLClientGameMessage(RLClientGameMessage.GAME_AVAILABLE_ACTIONS);
        actionsReq.setStateId(stateId);
        actionsReq.setUserName(this.userName);
        ISFSObject params = actionsReq.toSFSObject();
        ExtensionRequest req = new ExtensionRequest("rl.action", params, this.currentRoom);
        smartFox.send(req);
        System.out.println("Requested available actions for state " + stateId);
    }    

    // Processes the available actions from the server and decides which action to take next by calling the decideAction() helper method that decides the next action based on an epsilon-greedy policy
    // And then sends the chosen action back to the server
    // Given to students
    private void processAvailableActions(ISFSObject params) {
        RLClientGameMessage msg = new RLClientGameMessage();
        msg.fromSFSObject(params);
        this.gameModel.updateAvailableActions(msg.getAvailableActions());
        System.out.println("Available Actions: " + Arrays.toString(msg.getAvailableActions()));
        if (msg.getAvailableActions().length == 0) {
            System.err.println("Received empty available actions from server.");
        }
        requestAvailableRewards(this.gameModel.getStateId());
        int action = decideAction(this.gameModel.getStateId(), msg.getAvailableActions());
        System.out.println("Chosen Action: " + action);
        sendAction(action, this.gameModel.getStateId());
    }
    
    // Requests the available rewards for the available states
    // Given to students
    private void requestAvailableRewards(int stateId) {
        RLClientGameMessage rewardsReq = new RLClientGameMessage(RLClientGameMessage.GAME_AVAILABLE_REWARDS);
        rewardsReq.setStateId(stateId);
        rewardsReq.setUserName(this.userName);
        ISFSObject params = rewardsReq.toSFSObject();
        ExtensionRequest req = new ExtensionRequest("rl.action", params, this.currentRoom);
        smartFox.send(req);
        System.out.println("Requested available rewards for state " + stateId);
    }    

    // Processes the GAME_AVAILABLE_REWARDS message and updates the available rewards of the states after getting the available rewards from the server
    // Given to students
    private void processAvailableRewards(ISFSObject params) {
        RLClientGameMessage msg = new RLClientGameMessage();
        msg.fromSFSObject(params);
        this.gameModel.updateAvailableRewards(msg.getAvailableRewards());
        System.out.println("Available Rewards: " + Arrays.toString(msg.getAvailableRewards()));
    }

    // Processes the GAME_ACTION_REWARD message from the server by getting the reward, the next state, the action taken
    // Then it updates the Q and V tables based on the received reward and next state, and updates the current to the next state and gets possible actions from the next state
    // Partially given to students
    private void processActionReward(ISFSObject params) {
        RLClientGameMessage msg = new RLClientGameMessage();
        msg.fromSFSObject(params);
        double reward = msg.getReward();
        int nextStateId = msg.getNextStateId();
        int action = msg.getAction();
        System.out.println("Received Reward: " + reward + ", Next State ID: " + nextStateId);
        updateQTable(this.gameModel.getStateId(), action, reward, nextStateId);
        updateVTable(this.gameModel.getStateId(), reward, nextStateId);
        this.gameModel.updateState(nextStateId);
        System.out.println("Updated Current State ID to: " + nextStateId);
        requestAvailableActions(nextStateId);
    }

    // Processes the GAME_FINAL_STATE message from the server by resetting the environment by checking if the final state has been reached
    // Given to students
    protected void processFinalState(ISFSObject params) {
        RLClientGameMessage msg = new RLClientGameMessage();
        msg.fromSFSObject(params);
        boolean isTerminal = msg.isTerminal();
        double cumulativeReward = msg.getCumulativeReward();
        int stepsThisEpisode = msg.getStepsThisEpisode();

        this.gameModel.setTerminal(isTerminal);
        this.gameModel.setCumulativeReward(cumulativeReward);
        this.gameModel.setStepsThisEpisode(stepsThisEpisode);
        System.out.println("Is Terminal State: " + isTerminal);
        System.out.println("Received Cumulative Reward: " + cumulativeReward + ", Steps Taken: " + stepsThisEpisode);

        if (isTerminal) {
            System.out.println("Episode terminated. Resetting environment...");
            resetEnvironment();
        }
    }

    // Processes the GAME_RESET message from the server by requesting the initial state of the game
    // Given to students
    private void processReset(ISFSObject params) {
        RLClientGameMessage msg = new RLClientGameMessage();
        msg.fromSFSObject(params);
        String resetUserName = msg.userName;
        System.out.println("Environment reset for user: " + resetUserName);
        requestInitialState();
    }

    // Method that decides the next action to be taken based on an epsilon-greedy policy
    // Takes the current state, and the available actions and if the probability generated is < epsilon, then choose a random available action, else choose the action with the highest Q-value
    // Students must implement decideAction(), getRandomAvailableAction(), and getBestAvailableAction() or an action selection policy of their own
    private int decideAction(int stateId, int[] availableActions) {
        if (availableActions == null || availableActions.length == 0) {
            System.err.println("No available actions for the current state.");
            // Handle case where no available actions exist (perhaps return a default or throw an error)
            resetEnvironment();
            throw new IllegalArgumentException("No available actions for the current state.");
        }
        if (Math.random() < epsilon) {
            return getRandomAvailableAction(availableActions);
        } else {
            return getBestAvailableAction(stateId, availableActions);
        }
    }

    // Gets a random action from the available actions
    // Students must implement this
    private int getRandomAvailableAction(int[] availableActions) {
        if (availableActions == null || availableActions.length == 0) {
            throw new IllegalArgumentException("No available actions to choose from.");
        }
        int index = (int) (Math.random() * availableActions.length);
        return availableActions[index];
    }

    // Gets the best possible action for the RL agent to take based on the maximum Q-value in the Q-table for a particular state-action pair
    // Students must implement this
    private int getBestAvailableAction(int stateId, int[] availableActions) {
        if (availableActions.length == 0) {
            throw new IllegalArgumentException("No available actions to choose from.");
        }
        double[] actions = qTable.get(stateId);
        if (actions == null) {
            throw new IllegalStateException("Q-Table not initialized for stateId: " + stateId);
        }
        double maxQ = Double.NEGATIVE_INFINITY;
        int bestAction = availableActions[0];
        for (int action : availableActions) {
            if (action < 0 || action >= actions.length) {
                System.err.println("Invalid action index: " + action + " for stateId: " + stateId);
                continue;
            }
            if (actions[action] > maxQ) {
                maxQ = actions[action];
                bestAction = action;
            }
        }
        return bestAction;
    }    

    // Sends the action chosen by the RL agent to the server via the extension request
    // Given to students
    protected void sendAction(int action, int stateId) {
        RLClientGameMessage actionMsg = new RLClientGameMessage(RLClientGameMessage.GAME_ACTION_MOVE);
        actionMsg.setAction(action);
        actionMsg.setStateId(stateId);
        actionMsg.setUserName(this.userName);
        ISFSObject params = actionMsg.toSFSObject();
        ExtensionRequest actionReq = new ExtensionRequest("rl.action", params, this.currentRoom);
        smartFox.send(actionReq);
        System.out.println("Sent Action: " + action + " for State: " + stateId);
    }

    // Sends Q-Table updates to the server
    private void sendQUpdate(int[] qStateIds, int[] qActionIndices, double[] qValues) {
        RLClientGameMessage qUpdateMsg = new RLClientGameMessage(qStateIds, qActionIndices, qValues);
        ISFSObject params = qUpdateMsg.toSFSObject();
        ExtensionRequest qUpdateReq = new ExtensionRequest("rl.action", params, this.currentRoom);
        smartFox.send(qUpdateReq);
        System.out.println("Sent Q-Table updates to the server.");
    }

    // Sends V-Table updates to the server
    private void sendVUpdate(int[] vStateIds, double[] vValues) {
        RLClientGameMessage vUpdateMsg = new RLClientGameMessage(vStateIds, vValues);
        ISFSObject params = vUpdateMsg.toSFSObject();
        ExtensionRequest vUpdateReq = new ExtensionRequest("rl.action", params, this.currentRoom);
        smartFox.send(vUpdateReq);
        System.out.println("Sent V-Table updates to the server.");
    }

    // Updates the Q-table based on the update formula and sends updates to the server
    private void updateQTable(int stateId, int action, double reward, int nextStateId) {
        double[] currentQ = qTable.get(stateId);
        double[] nextQ = qTable.get(nextStateId);
        double maxNextQ = getMaxQ(nextQ);
        currentQ[action] = currentQ[action] + alpha * (reward + gamma * maxNextQ - currentQ[action]);
        qTable.put(stateId, currentQ);

        // Send Q-Table update to the server
        int[] qStateIds = {stateId};
        int[] qActionIndices = {action};
        double[] qValues = {currentQ[action]};
        sendQUpdate(qStateIds, qActionIndices, qValues);
    }

    // Updates the V-table based on TD error and sends updates to the server
    private void updateVTable(int stateId, double reward, int nextStateId) {
        double currentV = vTable.get(stateId);
        double nextV = vTable.get(nextStateId);
        double updatedV = currentV + alpha * (reward + gamma * nextV - currentV);
        vTable.put(stateId, updatedV);

        // Send V-Table update to the server
        int[] vStateIds = {stateId};
        double[] vValues = {updatedV};
        sendVUpdate(vStateIds, vValues);
    }

    // Sends a GAME_INFO request to the server
    private void sendGameInfoRequest() {
        RLClientGameMessage infoRequest = new RLClientGameMessage(RLClientGameMessage.GAME_INFO);
        infoRequest.setUserName(this.userName);
        ISFSObject params = infoRequest.toSFSObject();
        ExtensionRequest infoReq = new ExtensionRequest("rl.action", params, this.currentRoom);
        smartFox.send(infoReq);
        System.out.println("Sent GAME_INFO request to the server.");
    }

    // Gets the maximum Q-value from the array of Q-values for an action-state pair
    // Students must implement this
    private double getMaxQ(double[] qValues) {
        double max = Double.NEGATIVE_INFINITY;
        for (double q : qValues) {
            if (q > max) {
                max = q;
            }
        }
        return max;
    }

    // Resets the puddle world environment with an extension request to the server
    // Given to students
    protected void resetEnvironment() {
        RLClientGameMessage resetMsg = RLClientGameMessage.resetMessage(this.userName);
        ISFSObject params = resetMsg.toSFSObject();
        ExtensionRequest resetReq = new ExtensionRequest("rl.action", params, this.currentRoom);
        smartFox.send(resetReq);
        System.out.println("Sent GAME_RESET message to the server.");
    }

    // Disconnect client from the SmartFoxServer
    // Given to students
    public void disconnect() {
        if (smartFox.isConnected()) {
            smartFox.send(new LogoutRequest());
            smartFox.disconnect();
            System.out.println("Disconnected from SmartFoxServer.");
        }
    }

    // Main method to start the RLGamePlayer 
    // The following arguments needed to be typed into command line: <username> <password> <serverIP> <serverPort> <zoneName> <roomName>
    // Given to students
    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Usage: java RLGamePlayer <username> <password> <serverIP> <serverPort> <zoneName> <roomName>");
            return;
        }

        String username = args[0];
        String password = args[1];
        String serverIP = args[2];
        int serverPort = Integer.parseInt(args[3]);
        String zoneName = args[4];
        String roomName = args[5];

        RLGamePlayer agent = new RLGamePlayer(username, password, serverIP, serverPort, zoneName, roomName);

        // Keeps the agent running to listen for events
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                agent.disconnect();
                break;
            }
        }
    }
}