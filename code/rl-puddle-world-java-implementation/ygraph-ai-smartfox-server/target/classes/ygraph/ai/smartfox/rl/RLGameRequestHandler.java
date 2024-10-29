package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import java.util.List;
import java.util.ArrayList;

// This class handles RL-specific client-side requests like making actions and resets of a game
public class RLGameRequestHandler extends BaseClientRequestHandler {

    private RLGameManager gameManager;
    
    public RLGameRequestHandler(RLGameManager gameManager) {
        this.gameManager = gameManager;
    }

    // Handles various client requests like state, action, reset, available actions, available rewards, reward of action chosen, and final state
    @Override
    public void handleClientRequest(User user, ISFSObject params) {
        if (params == null) {
            sendErrorMessage(user, "No parameters provided.");
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
                case RLGameMessage.GAME_ACTION_REWARD:
                    handleActionRewardRequest(user, params, gameManager);
                    break;
                case RLGameMessage.GAME_FINAL_STATE:
                    handleFinalStateRequest(user, params, gameManager);
                    break;
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

        rlUser.updateQTable(action, reward, nextStateId);

        // Sends action reward back to the client
        ISFSObject response = new SFSObject();
        response.putUtfString("messageType", RLGameMessage.GAME_ACTION_REWARD_RESPONSE);
        response.putInt("action", action);
        response.putDouble("reward", reward);
        response.putInt("nextStateId", nextStateId);
        send("rl.action", response, user);
    }

    // Handles Q-Table updates from the client
    private void handleQUpdate(User user, ISFSObject params, RLGameManager gameManager) {
        RLGameMessage msg = new RLGameMessage();
        msg.fromSFSObject(params);
        
        int[] qStateIds = msg.getQStateIds();
        int[] qActionIndices = msg.getQActionIndices();
        double[] qValues = msg.getQValues();
        
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
            world.setQValue(stateId, action, qValue);
        }
        
        System.out.println("Received Q-Table updates from user: " + user.getName());
    }

    // Handles V-Table updates from the client
    private void handleVUpdate(User user, ISFSObject params, RLGameManager gameManager) {
        RLGameMessage msg = new RLGameMessage();
        msg.fromSFSObject(params);
        
        int[] vStateIds = msg.getVStateIds();
        double[] vValues = msg.getVValues();
        
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
            world.setVValue(stateId, vValue);
        }
        
        System.out.println("Received V-Table updates from user: " + user.getName());
    }
    
    private void handleFinalStateRequest(User user, ISFSObject params, RLGameManager gameManager) {
        RLGameMessage msg = new RLGameMessage();
        msg.fromSFSObject(params);
        boolean isTerminal = msg.isTerminal();
        double cumulativeReward = msg.getCumulativeReward();
        int stepsThisEpisode = msg.getStepsThisEpisode();
    
        RLGameUser rlUser = gameManager.getUser(user);
        if (rlUser == null) {
            System.out.println("RLGameUser not found for user: " + user.getName());
            sendErrorMessage(user, "User not found.");
            return;
        }
    
        rlUser.setTerminal(isTerminal);
        if (isTerminal) {
            rlUser.incrementEpisodes();
            rlUser.updateRewards(cumulativeReward);
            rlUser.updateStepsThisEpisode(stepsThisEpisode);
    
            if (rlUser.getCumulativeReward() >= rlUser.getSuccessRewardThreshold()) {
                rlUser.incrementSuccessfulEpisodes();
                System.out.println("Episode " + rlUser.getTotalEpisodes() + " was successful!");
            }
    
            rlUser.concludeEpisode();
    
            // Respond with GAME_INFO_RESPONSE
            RLGameMessage infoResponseMsg = new RLGameMessage();
            infoResponseMsg.setMessageType(RLGameMessage.GAME_INFO_RESPONSE);
            infoResponseMsg.setCumulativeReward(rlUser.getCumulativeReward());
            infoResponseMsg.setStepsThisEpisode(rlUser.getStepsThisEpisode());
            infoResponseMsg.setTotalEpisodes(rlUser.getTotalEpisodes());
            infoResponseMsg.setSuccessfulEpisodes(rlUser.getSuccessfulEpisodes());
            ISFSObject infoResponse = infoResponseMsg.toSFSObject();
            send("rl.action", infoResponse, user);
        }
    }    

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
        if (stateId != rlUser.getWorld().getCurrentStateId()) {
            System.out.println("State ID mismatch for user: " + user.getName() + ". Expected: " 
                + rlUser.getWorld().getCurrentStateId() + ", Received: " + stateId);
            ISFSObject errorResponse = new SFSObject();
            errorResponse.putUtfString("messageType", RLGameMessage.GAME_ERROR);
            errorResponse.putUtfString("error", "State ID mismatch.");
            send("rl.action", errorResponse, user);
            return;
        }

        // Perform action
        String actionStr = mapActionIndexToString(action);
        if (actionStr != null) {
            rlUser.takeAction(actionStr);
        } else {
            System.out.println("Invalid action index received: " + action);
            sendErrorMessage(user, "Invalid action index: " + action);
            return;
        }

        // Sends GAME_ACTION_REWARD_RESPONSE
        ISFSObject actionRewardResponse = new SFSObject();
        actionRewardResponse.putUtfString("messageType", RLGameMessage.GAME_ACTION_REWARD_RESPONSE);
        actionRewardResponse.putInt("action", action);
        actionRewardResponse.putDouble("reward", rlUser.getLastReward());
        actionRewardResponse.putInt("nextStateId", rlUser.getWorld().getCurrentStateId());
        send("rl.action", actionRewardResponse, user);

        // Sends GAME_STATE_RESPONSE
        ISFSObject stateResponse = new SFSObject();
        stateResponse.putUtfString("messageType", RLGameMessage.GAME_STATE_RESPONSE);
        stateResponse.putInt("stateId", rlUser.getWorld().getCurrentStateId());
        send("rl.action", stateResponse, user);

        // Sends GAME_AVAILABLE_ACTIONS_RESPONSE
        String[] availableActions = rlUser.getWorld().getAvailableActions(rlUser.getWorld().getCurrentStateId());
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
            String actionString = availableActions[i];
            int nextStateId = rlUser.getWorld().simulateAction(rlUser.getWorld().getCurrentStateId(), actionString);
            availableRewards[i] = rlUser.getWorld().getReward(rlUser.getWorld().getCurrentStateId(), actionString, nextStateId);

        }

        ISFSObject rewardsResponse = new SFSObject();
        rewardsResponse.putUtfString("messageType", RLGameMessage.GAME_AVAILABLE_REWARDS_RESPONSE);
        rewardsResponse.putDoubleArray("availableRewards", convertDoubleArrayToList(availableRewards));
        send("rl.action", rewardsResponse, user);

        // Goal state check
        if (rlUser.isTerminal()) {
            ISFSObject finalStateResponse = new SFSObject();
            finalStateResponse.putUtfString("messageType", RLGameMessage.GAME_FINAL_STATE_RESPONSE);
            finalStateResponse.putBool("isTerminal", true);
            finalStateResponse.putDouble("cumulativeReward", rlUser.getCumulativeReward());
            finalStateResponse.putInt("stepsThisEpisode", rlUser.getStepsThisEpisode()); 
            send("rl.action", finalStateResponse, user);
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

        // Respond with GAME_RESET_RESPONSE
        ISFSObject resetResponse = new SFSObject();
        resetResponse.putUtfString("messageType", RLGameMessage.GAME_RESET_RESPONSE);
        send("rl.action", resetResponse, user);

        // Send initial state, available actions, and rewards back to client
        handleGameStateRequest(user, params, gameManager);
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
        ISFSObject response = new SFSObject();
        response.putUtfString("messageType", RLGameMessage.GAME_ERROR);
        response.putUtfString("error", message);
        send("rl.error", response, user);
    }
}