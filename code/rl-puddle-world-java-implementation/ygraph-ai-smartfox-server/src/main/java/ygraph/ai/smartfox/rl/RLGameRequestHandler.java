package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

// This class handles RL-specific client-side requests like making actions and resets of a game
public class RLGameRequestHandler extends BaseClientRequestHandler {

    private RLGameManager gameManager;
    private static final HashMap<String, String> ENV = loadEnv();
    
    public RLGameRequestHandler(RLGameManager gameManager) {
        this.gameManager = gameManager;
    }

     private static HashMap<String, String> loadEnv() {
        HashMap<String, String> env = new HashMap<>();
        String workingDir = System.getProperty("user.dir");
        System.out.println("Current Working Directory: " + workingDir);
        try (BufferedReader br = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    env.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read .env file: " + e.getMessage());
        }
        return env;
    }

    // Handles various client requests like state, action, reset, available actions, available rewards, reward of action chosen, and final state
    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        if (params == null) {
            sendErrorMessage(user, "No parameters provided.");
            return;
        }

        Room rlRoom = getParentExtension().getParentZone().getRoomByName("RLRoom");

        // Check if the Room exists and if the user is joined in the Room
        if (rlRoom == null || !user.isJoinedInRoom(rlRoom)) {
            sendErrorMessage(user, "User is not in RLRoom.");
            return;
        }
    
        String messageType = params.getUtfString("messageType");
        if (messageType == null) {
            sendErrorMessage(user, "Missing message type.");
            return;
        }
    
        if (gameManager == null) {
            sendErrorMessage(user, "GameManager is not initialized.");
            return;
        }
    
        RLGameUser rlUser = gameManager.getUser(user);
        if (rlUser == null) {
            System.out.println("RLGameUser not found for user: " + user.getName() + ". Adding now.");
            boolean added = gameManager.addUser(user);
            if (!added) {
                sendErrorMessage(user, "Failed to add user to game.");
                return;
            }
    
            rlUser = gameManager.getUser(user);
            if (rlUser == null) {
                sendErrorMessage(user, "User not found after adding.");
                return;
            }
        }  

        if (rlUser.isTrainingComplete()) {
            System.out.println("User " + user.getName() + " has completed training.");
            sendTrainingCompleteMessage(user);
            return; // Do not process further requests
        }

        synchronized (rlUser) {
            switch (messageType) {
                case RLGameMessage.GAME_STATE:
                    handleGameStateRequest(user, params, gameManager);
                    break;
                case RLGameMessage.GAME_ACTION_MOVE:
                    handleActionMove(user, params, gameManager);
                    break;
                case RLGameMessage.GAME_RESET:
                    handleGameReset(user, params, gameManager);
                    break;
                case RLGameMessage.GAME_AVAILABLE_ACTIONS:
                    handleAvailableActionsRequest(user, params, gameManager);
                    break;
                case RLGameMessage.GAME_AVAILABLE_REWARDS:
                    handleAvailableRewardsRequest(user, params, gameManager);
                    break;
                case RLGameMessage.GAME_TRAINING_COMPLETE:
                    handleTrainingComplete(user, params, gameManager);
                    break;
                case RLGameMessage.GAME_ACTION_REWARD:
                    handleActionRewardRequest(user, params, gameManager);
                    break;
                // case RLGameMessage.GAME_FINAL_STATE:
                //     handleFinalStateRequest(user, params, gameManager);
                //     break;
                case RLGameMessage.GAME_Q_UPDATE:
                    handleQUpdate(user, params, gameManager);
                    break;
                case RLGameMessage.GAME_V_UPDATE:
                    handleVUpdate(user, params, gameManager);
                    break;
                case RLGameMessage.GAME_INFO:
                    handleInfoRequest(user, params, gameManager);
                    break;
                default:
                    System.out.println("Unknown message type: " + messageType);
                    sendErrorMessage(user, "Unknown message type: " + messageType);
                    break;
            }
        }
    }

    private void sendTrainingCompleteMessage(User user) {
        ISFSObject response = new SFSObject();
        response.putUtfString("messageType", RLGameMessage.GAME_TRAINING_COMPLETE);
        response.putUtfString("message", "Training completed. Maximum number of episodes reached.");
        send("rl.action", response, user);
    }

    // Sends the initial state and available actions/rewards to the client to start a new episode
    private void sendInitialState(User user, RLGameUser rlUser) {
        // Reset the RLWorld for the new episode
        rlUser.resetGame();

        int initialStateId = rlUser.getCurrentStateId();

        // Send GAME_STATE_RESPONSE
        RLGameMessage stateMsg = new RLGameMessage();
        stateMsg.setMessageType(RLGameMessage.GAME_STATE_RESPONSE);
        stateMsg.setStateId(initialStateId);
        ISFSObject stateResponse = stateMsg.toSFSObject();
        send("rl.action", stateResponse, user);
        System.out.println("Sent GAME_STATE_RESPONSE for new episode");

        // Send GAME_AVAILABLE_ACTIONS_RESPONSE
        String[] availableActions = rlUser.getWorld().getAvailableActions(initialStateId);
        int[] actionIndices = new int[availableActions.length];
        for (int i = 0; i < availableActions.length; i++) {
            actionIndices[i] = mapActionStringToIndex(availableActions[i]);
        }        
        ISFSObject actionsResponse = new SFSObject();
        actionsResponse.putUtfString("messageType", RLGameMessage.GAME_AVAILABLE_ACTIONS_RESPONSE);
        actionsResponse.putIntArray("availableActions", convertIntArrayToList(actionIndices));
        send("rl.action", actionsResponse, user);
        System.out.println("Sent GAME_AVAILABLE_ACTIONS_RESPONSE for new episode");

        // Send GAME_AVAILABLE_REWARDS_RESPONSE
        double[] availableRewards = new double[availableActions.length];
        for (int i = 0; i < availableActions.length; i++) {
            String actionString = availableActions[i];
            int nextStateId = rlUser.getWorld().simulateAction(initialStateId, actionString);
            availableRewards[i] = rlUser.getWorld().getReward(initialStateId, actionString, nextStateId);
        }

        ISFSObject rewardsResponse = new SFSObject();
        rewardsResponse.putUtfString("messageType", RLGameMessage.GAME_AVAILABLE_REWARDS_RESPONSE);
        rewardsResponse.putDoubleArray("availableRewards", convertDoubleArrayToList(availableRewards));
        send("rl.action", rewardsResponse, user);
        System.out.println("Sent GAME_AVAILABLE_REWARDS_RESPONSE for new episode");
    }


    // Handles the GAME_STATE request from the client by sending back the current state, available actions and available rewards
    private void handleGameStateRequest(User user, ISFSObject params, RLGameManager gameManager) {
        RLGameUser rlUser = gameManager.getUser(user);
        if (rlUser == null) {
            System.out.println("RLGameUser not found for user: " + user.getName());
            sendErrorMessage(user, "User not found.");
            return;
        }

        if (rlUser.getWorld() == null) {
            System.out.println("RLWorld is null for user: " + user.getName());
            sendErrorMessage(user, "Game world not initialized.");
            return;
        }

        int stateId = rlUser.getCurrentStateId();

        // Sends GAME_STATE_RESPONSE
        System.out.println("Server is sending initial game state..." + stateId);
        ISFSObject stateResponse = new SFSObject();
        stateResponse.putUtfString("messageType", RLGameMessage.GAME_STATE_RESPONSE);
        stateResponse.putInt("stateId", stateId);
        send("rl.action", stateResponse, user);

        // Sends GAME_AVAILABLE_ACTIONS_RESPONSE
        String[] availableActions = rlUser.getWorld().getAvailableActions(stateId);
        int[] actionIndices = new int[availableActions.length];
        for (int i = 0; i < availableActions.length; i++) {
            actionIndices[i] = mapActionStringToIndex(availableActions[i]);
        }        
        ISFSObject actionsResponse = new SFSObject();
        actionsResponse.putUtfString("messageType", RLGameMessage.GAME_AVAILABLE_ACTIONS_RESPONSE);
        actionsResponse.putIntArray("availableActions", convertIntArrayToList(actionIndices));
        send("rl.action", actionsResponse, user);

        // Sends GAME_AVAILABLE_REWARDS_RESPONSE
        double[] availableRewards = new double[availableActions.length];
        for (int i = 0; i < availableActions.length; i++) {
            String action = availableActions[i];
            int nextStateId = rlUser.getWorld().simulateAction(stateId, action);
            availableRewards[i] = rlUser.getWorld().getReward(stateId, action, nextStateId);
        }
        ISFSObject rewardsResponse = new SFSObject();
        rewardsResponse.putUtfString("messageType", RLGameMessage.GAME_AVAILABLE_REWARDS_RESPONSE);
        rewardsResponse.putDoubleArray("availableRewards", convertDoubleArrayToList(availableRewards));
        send("rl.action", rewardsResponse, user);
    }

    private void handleAvailableActionsRequest(User user, ISFSObject params, RLGameManager gameManager) {
        try {
            RLGameUser rlUser = gameManager.getUser(user);
            if (rlUser == null) {
                System.out.println("RLGameUser not found for user: " + user.getName());
                sendErrorMessage(user, "User not found.");
                return;
            }
    
            RLWorld world = rlUser.getWorld();
            if (world == null) {
                System.err.println("RLWorld is null for user: " + user.getName());
                sendErrorMessage(user, "Game world not initialized.");
                return;
            }
    
            int stateId = params.getInt("stateId");
            System.out.println("Handling Available Actions Request for stateId: " + stateId);
    
            String[] availableActions = world.getAvailableActions(stateId);
    
            if (availableActions == null) {
                System.err.println("Available Actions is null for stateId: " + stateId);
                sendErrorMessage(user, "Available actions are null.");
                return;
            }
    
            if (availableActions.length == 0) {
                System.err.println("No available actions for stateId: " + stateId);
                sendErrorMessage(user, "No available actions for the current state.");
                return;
            }
    
            System.out.println("Available Actions Length: " + availableActions.length);
            System.out.println("Available Actions: " + String.join(", ", availableActions));
    
            int[] actionIndices = new int[availableActions.length];
            for (int i = 0; i < availableActions.length; i++) {
                int actionIndex = mapActionStringToIndex(availableActions[i]);
                if (actionIndex == -1) {
                    System.err.println("Invalid action string: " + availableActions[i]);
                    sendErrorMessage(user, "Invalid action received: " + availableActions[i]);
                    return;
                }
                actionIndices[i] = actionIndex;
            }
    
            List<Integer> actionsList = convertIntArrayToList(actionIndices);
            if (actionsList == null) {
                System.err.println("convertIntArrayToList returned null.");
                sendErrorMessage(user, "Internal server error: Failed to process actions.");
                return;
            }
    
            System.out.println("Available Actions Indices: " + actionsList);
            ISFSObject actionsResponse = new SFSObject();
            actionsResponse.putUtfString("messageType", RLGameMessage.GAME_AVAILABLE_ACTIONS_RESPONSE);
            actionsResponse.putIntArray("availableActions", actionsList);
            send("rl.action", actionsResponse, user);
            System.out.println("Sent Available Actions Response to user: " + user.getName());
        } catch (Exception e) {
            System.err.println("Exception in handleAvailableActionsRequest: " + e.getMessage());
            e.printStackTrace();
            sendErrorMessage(user, "An unexpected error occurred.");
        }
    }            

    private void handleAvailableRewardsRequest(User user, ISFSObject params, RLGameManager gameManager) {
        try {
            RLGameUser rlUser = gameManager.getUser(user);
            if (rlUser == null) {
                System.out.println("RLGameUser not found for user: " + user.getName());
                sendErrorMessage(user, "User not found.");
                return;
            }
    
            RLWorld world = rlUser.getWorld();
            if (world == null) {
                System.err.println("RLWorld is null for user: " + user.getName());
                sendErrorMessage(user, "Game world not initialized.");
                return;
            }
    
            int stateId = params.getInt("stateId");
            System.out.println("Handling Available Rewards Request for stateId: " + stateId);
    
            String[] availableActions = world.getAvailableActions(stateId);
    
            if (availableActions == null) {
                System.err.println("Available Actions is null for stateId: " + stateId);
                sendErrorMessage(user, "Available actions are null.");
                return;
            }
    
            if (availableActions.length == 0) {
                System.err.println("No available actions for stateId: " + stateId);
                sendErrorMessage(user, "No available actions for the current state.");
                return;
            }
    
            System.out.println("Available Actions Length: " + availableActions.length);
            System.out.println("Available Actions: " + String.join(", ", availableActions));
    
            double[] availableRewards = new double[availableActions.length];
            System.out.println("State ID: " + stateId);
            System.out.println("Available rewards array initialized with length: " + availableRewards.length);
    
            for (int i = 0; i < availableActions.length; i++) {
                String actionStr = availableActions[i];
                if (actionStr == null || actionStr.isEmpty()) {
                    System.err.println("Invalid action string at index " + i);
                    sendErrorMessage(user, "Invalid action received at index " + i + ".");
                    return;
                }
                System.out.println("Processing Action: " + actionStr);
                int nextStateId = world.simulateAction(stateId, actionStr);
                double reward = world.getReward(stateId, actionStr, nextStateId);
                availableRewards[i] = reward;
                System.out.println("Action '" + actionStr + "' leads to state " + nextStateId + " with reward " + reward);
                if (Double.isNaN(reward)) {
                    System.err.println("Received invalid reward for action: " + actionStr);
                    sendErrorMessage(user, "Invalid reward received for action: " + actionStr);
                    return;
                }
            }
    
            List<Double> rewardsList = convertDoubleArrayToList(availableRewards);
            if (rewardsList == null) {
                System.err.println("convertDoubleArrayToList returned null.");
                sendErrorMessage(user, "Internal server error: Failed to process rewards.");
                return;
            }
    
            System.out.println("Available Rewards: " + rewardsList);
            ISFSObject rewardsResponse = new SFSObject();
            rewardsResponse.putUtfString("messageType", RLGameMessage.GAME_AVAILABLE_REWARDS_RESPONSE);
            rewardsResponse.putDoubleArray("availableRewards", rewardsList);
            send("rl.action", rewardsResponse, user);
            System.out.println("Sent Available Rewards Response to user: " + user.getName());
        } catch (Exception e) {
            System.err.println("Exception in handleAvailableRewardsRequest: " + e.getMessage());
            e.printStackTrace();
            sendErrorMessage(user, "An unexpected error occurred.");
        }
    }               
    
    private void handleActionRewardRequest(User user, ISFSObject params, RLGameManager gameManager) {
        RLGameUser rlUser = gameManager.getUser(user);
        if (rlUser == null) {
            System.out.println("RLGameUser not found for user: " + user.getName());
            sendErrorMessage(user, "User not found.");
            return;
        }

        int action = params.getInt("action");
        double reward = params.getDouble("reward");
        int nextStateId = params.getInt("nextStateId");

        // rlUser.updateQTable(action, reward, nextStateId);

        // Sends action reward back to the client
        ISFSObject response = new SFSObject();
        response.putUtfString("messageType", RLGameMessage.GAME_ACTION_REWARD_RESPONSE);
        response.putInt("action", action);
        response.putDouble("reward", reward);
        response.putInt("nextStateId", nextStateId);
        send("rl.action", response, user);
    }

    private void handleTrainingComplete(User user, ISFSObject params, RLGameManager gameManager) {
        RLGameUser rlUser = gameManager.getUser(user);
        if (rlUser != null) {
            System.out.println("Received GAME_TRAINING_COMPLETE from user: " + user.getName());
            // Perform any necessary cleanup
            rlUser.cleanup();
            gameManager.removeUser(user);
        } else {
            System.out.println("RLGameUser not found for user: " + user.getName());
        }
    }    

    // Handles Q-Table updates from the client
    private void handleQUpdate(User user, ISFSObject params, RLGameManager gameManager) {
        try {
            RLGameMessage msg = new RLGameMessage();
            msg.fromSFSObject(params);
            
            int[] qStateIds = msg.getQStateIds();
            int[] qActionIndices = msg.getQActionIndices();
            double[] qValues = msg.getQValues();
            
            if (qStateIds == null || qActionIndices == null || qValues == null ||
                qStateIds.length != qActionIndices.length || qStateIds.length != qValues.length) {
                sendErrorMessage(user, "Invalid Q-Update data received.");
                return;
            }
            
            RLGameUser rlUser = gameManager.getUser(user);
            if (rlUser == null) {
                System.out.println("RLGameUser not found for user: " + user.getName());
                sendErrorMessage(user, "User not found.");
                return;
            }
            
            RLWorld world = rlUser.getWorld();
            for (int i = 0; i < qStateIds.length; i++) {
                int stateId = qStateIds[i];
                int action = qActionIndices[i];
                double qValue = qValues[i];
                
                if (!isValidStateAction(stateId, action, world)) {
                    System.err.println("Invalid stateId or action index: StateId=" + stateId + ", Action=" + action);
                    continue;
                }
                
                world.setQValue(stateId, action, qValue);
            }
            
            System.out.println("Applied Q-Table updates from user: " + user.getName());
        } catch (Exception e) {
            System.err.println("Exception in handleQUpdate: " + e.getMessage());
            e.printStackTrace();
            sendErrorMessage(user, "Failed to process Q-Update.");
        }
    }
    
    // Helper method to validate stateId and action index
    private boolean isValidStateAction(int stateId, int action, RLWorld world) {
        final int gridSize =  Integer.parseInt(ENV.getOrDefault("GRID_SIZE", "5"));
        if (stateId < 0 || stateId >= gridSize * gridSize) {
            return false;
        }
        if (action < 0 || action >= 4) {
            return false;
        }
        return true;
    }    

    // Handles V-Table updates from the client
    private void handleVUpdate(User user, ISFSObject params, RLGameManager gameManager) {
        try {
            RLGameMessage msg = new RLGameMessage();
            msg.fromSFSObject(params);
            
            int[] vStateIds = msg.getVStateIds();
            double[] vValues = msg.getVValues();
            
            if (vStateIds == null || vValues == null || vStateIds.length != vValues.length) {
                sendErrorMessage(user, "Invalid V-Update data received.");
                return;
            }
            
            RLGameUser rlUser = gameManager.getUser(user);
            if (rlUser == null) {
                System.out.println("RLGameUser not found for user: " + user.getName());
                sendErrorMessage(user, "User not found.");
                return;
            }
            
            RLWorld world = rlUser.getWorld();
            for (int i = 0; i < vStateIds.length; i++) {
                int stateId = vStateIds[i];
                double vValue = vValues[i];
                
                if (!isValidState(stateId, world)) {
                    System.err.println("Invalid stateId: " + stateId);
                    continue;
                }
                
                world.setVValue(stateId, vValue);
            }
            
            System.out.println("Applied V-Table updates from user: " + user.getName());
        } catch (Exception e) {
            System.err.println("Exception in handleVUpdate: " + e.getMessage());
            e.printStackTrace();
            sendErrorMessage(user, "Failed to process V-Update.");
        }
    }
    
    // Helper method to validate stateId
    private boolean isValidState(int stateId, RLWorld world) {
        final int gridSize =  Integer.parseInt(ENV.getOrDefault("GRID_SIZE", "5"));
        return stateId >= 0 && stateId < gridSize * gridSize;
    }    
    
    // private void handleFinalStateRequest(User user, ISFSObject params, RLGameManager gameManager) {
    //     RLGameMessage msg = new RLGameMessage();
    //     msg.fromSFSObject(params);
    //     boolean isTerminal = msg.isTerminal();
    //     double cumulativeReward = msg.getCumulativeReward();
    //     int stepsThisEpisode = msg.getStepsThisEpisode();
    
    //     RLGameUser rlUser = gameManager.getUser(user);
    //     if (rlUser == null) {
    //         System.out.println("RLGameUser not found for user: " + user.getName());
    //         sendErrorMessage(user, "User not found.");
    //         return;
    //     }
    
    //     rlUser.setTerminal(isTerminal);
    //     rlUser.updateRewards(cumulativeReward);
    //     rlUser.updateStepsThisEpisode(stepsThisEpisode);
    
    //     if (rlUser.getCumulativeReward() >= rlUser.getSuccessRewardThreshold()) {
    //         rlUser.incrementSuccessfulEpisodes();
    //         System.out.println("Episode " + rlUser.getTotalEpisodes() + " was successful!");
    //     }
    
    //     rlUser.concludeEpisode();
    
    //     // Construct GAME_FINAL_STATE_RESPONSE using RLGameMessage
    //     RLGameMessage finalStateMsg = new RLGameMessage();
    //     finalStateMsg.setMessageType(RLGameMessage.GAME_FINAL_STATE_RESPONSE);
    //     finalStateMsg.setTotalEpisodes(rlUser.getTotalEpisodes());
    //     finalStateMsg.setStepsThisEpisode(rlUser.getStepsThisEpisode());
    //     finalStateMsg.setCumulativeReward(rlUser.getCumulativeReward());
    //     finalStateMsg.setSuccessfulEpisodes(rlUser.getSuccessfulEpisodes());
    //     finalStateMsg.setTerminal(rlUser.isTerminal());
    
    //     // Send GAME_FINAL_STATE_RESPONSE to the client
    //     ISFSObject finalStateResponse = finalStateMsg.toSFSObject();
    //     send("rl.action", finalStateResponse, user);
    //     System.out.println("Sent GAME_FINAL_STATE_RESPONSE to user: " + user.getName());
    
    //     // Check if training is complete
    //     if (rlUser.isTrainingComplete()) {
    //         sendTrainingCompleteMessage(user);
    //     }
    // }            

    // Handles the GAME_INFO request by sending a summary of the RL agent's training over x episodes
    private void handleInfoRequest(User user, ISFSObject params, RLGameManager gameManager) {
        RLGameUser rlUser = gameManager.getUser(user);
        if (rlUser == null) {
            System.out.println("RLGameUser not found for user: " + user.getName());
            sendErrorMessage(user, "User not found.");
            return;
        }

        // Retrieve metrics from RLGameUser
        double cumulativeReward = rlUser.getCumulativeReward();
        int stepsThisEpisode = rlUser.getStepsThisEpisode();
        int totalEpisodes = rlUser.getTotalEpisodes();
        int successfulEpisodes = rlUser.getSuccessfulEpisodes();

        // Log metrics
        System.out.println("Episode " + totalEpisodes + " Summary:");
        System.out.println(" - Cumulative Reward: " + cumulativeReward);
        System.out.println(" - Steps Taken: " + stepsThisEpisode);
        System.out.println(" - Successful Episodes: " + successfulEpisodes);
        System.out.println(" - Total Episodes: " + totalEpisodes);

        // GAME_INFO response message
        RLGameMessage infoResponseMsg = new RLGameMessage();
        infoResponseMsg.setMessageType(RLGameMessage.GAME_INFO_RESPONSE);
        infoResponseMsg.setCumulativeReward(cumulativeReward);
        infoResponseMsg.setStepsThisEpisode(stepsThisEpisode);
        infoResponseMsg.setTotalEpisodes(totalEpisodes);
        infoResponseMsg.setSuccessfulEpisodes(successfulEpisodes);
        ISFSObject infoResponse = infoResponseMsg.toSFSObject();
        send("rl.action", infoResponse, user);
    }

    // Handles the GAME_ACTION_MOVE request by performing the action, updating the state, calculating the reward and responding with the reward, state, available actions and available rewards
    // RLGameRequestHandler.java
    private void handleActionMove(User user, ISFSObject params, RLGameManager gameManager) {
        RLGameUser rlUser = gameManager.getUser(user);
        if (rlUser == null) {
            System.out.println("RLGameUser not found for user: " + user.getName());
            sendErrorMessage(user, "User not found.");
            return;
        }
    
        int action = params.getInt("action");
        int stateId = params.getInt("stateId");
    
        // Validate the state ID
        if (stateId != rlUser.getCurrentStateId()) {
            // System.out.println("State ID mismatch for user: " + user.getName() + ". Expected: " 
            //     + rlUser.getCurrentStateId() + ", Received: " + stateId);
            sendErrorMessage(user, "State ID mismatch.");
            return;
        }
    
        // Perform the action
        String actionStr = mapActionIndexToString(action);
        if (actionStr == null) {
            System.out.println("Invalid action index received: " + action);
            sendErrorMessage(user, "Invalid action index: " + action);
            return;
        }
        rlUser.takeAction(actionStr);
    
        // Retrieve updated state and reward
        int updatedStateId = rlUser.getCurrentStateId();
        double reward = rlUser.getLastReward();

        // Send GAME_ACTION_REWARD_RESPONSE
        ISFSObject actionRewardResponse = new SFSObject();
        actionRewardResponse.putUtfString("messageType", RLGameMessage.GAME_ACTION_REWARD_RESPONSE);
        actionRewardResponse.putInt("action", action);
        actionRewardResponse.putDouble("reward", reward);
        actionRewardResponse.putInt("nextStateId", updatedStateId);
        send("rl.action", actionRewardResponse, user);
        System.out.println("Sent GAME_ACTION_REWARD_RESPONSE with action: " + action + ", reward: " + reward + ", nextStateId: " + updatedStateId);
    
        // Check if the episode has ended
        if (rlUser.isTerminal() || rlUser.getStepsThisEpisode() >= rlUser.getMaxStepsPerEpisode()) {
            // Send GAME_FINAL_STATE_RESPONSE
            RLGameMessage finalStateMsg = new RLGameMessage();
            finalStateMsg.setMessageType(RLGameMessage.GAME_FINAL_STATE_RESPONSE);
            finalStateMsg.setCumulativeReward(rlUser.getCumulativeReward());
            finalStateMsg.setStepsThisEpisode(rlUser.getStepsThisEpisode());
            finalStateMsg.setTotalEpisodes(rlUser.getTotalEpisodes());
            finalStateMsg.setSuccessfulEpisodes(rlUser.getSuccessfulEpisodes());
            finalStateMsg.setTerminal(rlUser.isTerminal());
            ISFSObject finalStateResponse = finalStateMsg.toSFSObject();
            send("rl.action", finalStateResponse, user);
            System.out.println("Sent GAME_FINAL_STATE_RESPONSE");
    
            // Conclude the episode
            rlUser.concludeEpisode();
    
            // Check if training is complete
            if (rlUser.isTrainingComplete()) {
                // Send GAME_TRAINING_COMPLETE
                sendTrainingCompleteMessage(user);
            } else {
                // Start a new episode by sending initial state and available actions/rewards
                sendInitialState(user, rlUser);
            }
        } else {    
            // Send GAME_AVAILABLE_ACTIONS_RESPONSE
            String[] availableActions = rlUser.getWorld().getAvailableActions(updatedStateId);
            int[] actionIndices = new int[availableActions.length];
            for (int i = 0; i < availableActions.length; i++) {
                actionIndices[i] = mapActionStringToIndex(availableActions[i]);
            }        
            ISFSObject actionsResponse = new SFSObject();
            actionsResponse.putUtfString("messageType", RLGameMessage.GAME_AVAILABLE_ACTIONS_RESPONSE);
            actionsResponse.putIntArray("availableActions", convertIntArrayToList(actionIndices));
            send("rl.action", actionsResponse, user);
            System.out.println("Sent GAME_AVAILABLE_ACTIONS_RESPONSE");
    
            // Send GAME_AVAILABLE_REWARDS_RESPONSE
            double[] availableRewards = new double[availableActions.length];
            for (int i = 0; i < availableActions.length; i++) {
                String actionString = availableActions[i];
                int nextStateId = rlUser.getWorld().simulateAction(updatedStateId, actionString);
                availableRewards[i] = rlUser.getWorld().getReward(updatedStateId, actionString, nextStateId);
            }
    
            ISFSObject rewardsResponse = new SFSObject();
            rewardsResponse.putUtfString("messageType", RLGameMessage.GAME_AVAILABLE_REWARDS_RESPONSE);
            rewardsResponse.putDoubleArray("availableRewards", convertDoubleArrayToList(availableRewards));
            send("rl.action", rewardsResponse, user);
            System.out.println("Sent GAME_AVAILABLE_REWARDS_RESPONSE");
        }        
    }       

    // Handles GAME_RESET request by client by resetting RL world
    private void handleGameReset(User user, ISFSObject params, RLGameManager gameManager) {
        RLGameUser rlUser = gameManager.getUser(user);
        if (rlUser == null) {
            System.out.println("RLGameUser not found for user: " + user.getName());
            sendErrorMessage(user, "User not found.");
            return;
        }
    
        rlUser.resetGame();
    
        // Sent GAME_RESET_RESPONSE
        ISFSObject resetResponse = new SFSObject();
        resetResponse.putUtfString("messageType", RLGameMessage.GAME_RESET_RESPONSE);
        resetResponse.putInt("stateId", rlUser.getCurrentStateId());
        send("rl.action", resetResponse, user);
        System.out.println("Sent GAME_RESET_RESPONSE with stateId: " + rlUser.getCurrentStateId());
    
        // Sent GAME_AVAILABLE_ACTIONS_RESPONSE
        String[] availableActions = rlUser.getWorld().getAvailableActions(rlUser.getCurrentStateId());
        int[] actionIndices = new int[availableActions.length];
        for (int i = 0; i < availableActions.length; i++) {
            actionIndices[i] = mapActionStringToIndex(availableActions[i]);
        }
        ISFSObject actionsResponse = new SFSObject();
        actionsResponse.putUtfString("messageType", RLGameMessage.GAME_AVAILABLE_ACTIONS_RESPONSE);
        actionsResponse.putIntArray("availableActions", convertIntArrayToList(actionIndices));
        send("rl.action", actionsResponse, user);
        System.out.println("Sent GAME_AVAILABLE_ACTIONS_RESPONSE");
    
        // Sent GAME_AVAILABLE_REWARDS_RESPONSE
        double[] availableRewards = new double[availableActions.length];
        for (int i = 0; i < availableActions.length; i++) {
            String action = availableActions[i];
            int nextStateId = rlUser.getWorld().simulateAction(rlUser.getCurrentStateId(), action);
            availableRewards[i] = rlUser.getWorld().getReward(rlUser.getCurrentStateId(), action, nextStateId);
        }
        ISFSObject rewardsResponse = new SFSObject();
        rewardsResponse.putUtfString("messageType", RLGameMessage.GAME_AVAILABLE_REWARDS_RESPONSE);
        rewardsResponse.putDoubleArray("availableRewards", convertDoubleArrayToList(availableRewards));
        send("rl.action", rewardsResponse, user);
        System.out.println("Sent GAME_AVAILABLE_REWARDS_RESPONSE");

        RLGameMessage infoResponseMsg = new RLGameMessage();
        infoResponseMsg.setMessageType(RLGameMessage.GAME_INFO_RESPONSE);
        infoResponseMsg.setCumulativeReward(rlUser.getCumulativeReward());
        infoResponseMsg.setStepsThisEpisode(rlUser.getStepsThisEpisode());
        infoResponseMsg.setTotalEpisodes(rlUser.getTotalEpisodes());
        infoResponseMsg.setSuccessfulEpisodes(rlUser.getSuccessfulEpisodes());
        ISFSObject infoResponse = infoResponseMsg.toSFSObject();
        send("rl.action", infoResponse, user);
        System.out.println("Sent GAME_INFO_RESPONSE");
    }
    

    // Converts an integer array to a list of integers
    private List<Integer> convertIntArrayToList(int[] array) {
        if (array == null) return null;
        List<Integer> list = new ArrayList<>();
        for (int num : array) {
            list.add(num);
        }
        return list;
    }

    // Converts an array of doubles into a list of doubles
    private List<Double> convertDoubleArrayToList(double[] array) {
        if (array == null) return null;
        List<Double> list = new ArrayList<>();
        for (double num : array) {
            list.add(num);
        }
        return list;
    }

    // Maps an action's index to its respective action string
    private String mapActionIndexToString(int index) {
        switch (index) {
            case 0:
                return "UP";
            case 1:
                return "DOWN";
            case 2:
                return "LEFT";
            case 3:
                return "RIGHT";
            default:
                return null;
        }
    }

    // Maps an action's string to its respective index
    private int mapActionStringToIndex(String action) {
        switch (action) {
            case "UP":
                return 0;
            case "DOWN":
                return 1;
            case "LEFT":
                return 2;
            case "RIGHT":
                return 3;
            default:
                return -1;
        }
    }

    // Sends an error message to the user
    private void sendErrorMessage(User user, String message) {
        RLGameMessage msg = new RLGameMessage();
        msg.setMessageType(RLGameMessage.GAME_ERROR);
        msg.setErrorMessage(message);
        ISFSObject response = msg.toSFSObject();
        send("rl.error", response, user);
    }    
}