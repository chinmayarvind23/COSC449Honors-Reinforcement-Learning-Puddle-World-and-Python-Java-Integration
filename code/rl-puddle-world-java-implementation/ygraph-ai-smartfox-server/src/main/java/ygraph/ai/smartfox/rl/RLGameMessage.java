package ygraph.ai.smartfox.rl;

import java.util.ArrayList;
import java.util.List;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

// This class defines the messages for server-side communication
public class RLGameMessage {
    // Client to server messages
    public static final String GAME_STATE = "GAME_STATE";
    public static final String GAME_AVAILABLE_ACTIONS = "GAME_AVAILABLE_ACTIONS";
    public static final String GAME_AVAILABLE_REWARDS = "GAME_AVAILABLE_REWARDS";
    public static final String GAME_ACTION_MOVE = "GAME_ACTION_MOVE";
    public static final String GAME_ACTION_REWARD = "GAME_ACTION_REWARD";
    public static final String GAME_FINAL_STATE = "GAME_FINAL_STATE";
    public static final String GAME_RESET = "GAME_RESET";
    public static final String GAME_Q_UPDATE = "GAME_Q_UPDATE";
    public static final String GAME_V_UPDATE = "GAME_V_UPDATE";
    public static final String GAME_INFO = "GAME_INFO";
    public static final String FORWARD_ACTION = "FORWARD_ACTION";

    // Server to Client messages
    public static final String GAME_STATE_RESPONSE = "GAME_STATE_RESPONSE";
    public static final String GAME_AVAILABLE_ACTIONS_RESPONSE = "GAME_AVAILABLE_ACTIONS_RESPONSE";
    public static final String GAME_AVAILABLE_REWARDS_RESPONSE = "GAME_AVAILABLE_REWARDS_RESPONSE";
    public static final String GAME_ACTION_REWARD_RESPONSE = "GAME_ACTION_REWARD_RESPONSE";
    public static final String GAME_FINAL_STATE_RESPONSE = "GAME_FINAL_STATE_RESPONSE";
    public static final String GAME_RESET_RESPONSE = "GAME_RESET_RESPONSE";
    public static final String GAME_ERROR = "GAME_ERROR";
    public static final String GAME_INFO_RESPONSE = "GAME_INFO_RESPONSE";
    public static final String GAME_TRAINING_COMPLETE = "GAME_TRAINING_COMPLETE";

    // Fields for Q-Table updates
    private int[] qStateIds;
    private int[] qActionIndices;
    private double[] qValues;
    private String messageType;
    String userName;
    private int stateId;
    private int action;
    private double reward; // reward for action x
    private int nextStateId;
    private int[] availableActions;
    private double[] availableRewards;
    private boolean isTerminal;
    private int steps;
    private double cumulativeReward;
    private int stepsThisEpisode;
    private int totalEpisodes;
    private int successfulEpisodes;

    // Fields for V-Table updates
    private int[] vStateIds;
    private double[] vValues;

    private String errorMessage;

    public RLGameMessage() {
        this.messageType = "";
        this.userName = "";
        this.stateId = 0;
        // this.action = -1;
        this.reward = 0.0;
        // this.nextStateId = -1;
        this.isTerminal = false;
        this.qStateIds = new int[0];
        this.qActionIndices = new int[0];
        this.qValues = new double[0];
        this.vStateIds = new int[0];
        this.vValues = new double[0];
        this.availableActions = new int[0];
        this.availableRewards = new double[0];
        this.steps = 0;
    }

    // Constructor for Q-Table Update
    public RLGameMessage(int[] qStateIds, int[] qActionIndices, double[] qValues) {
        this.messageType = GAME_Q_UPDATE;
        this.qStateIds = qStateIds;
        this.qActionIndices = qActionIndices;
        this.qValues = qValues;
    }

    // Constructor for V-Table Update
    public RLGameMessage(int[] vStateIds, double[] vValues) {
        this.messageType = GAME_V_UPDATE;
        this.vStateIds = vStateIds;
        this.vValues = vValues;
    }

    public RLGameMessage(boolean isTerminal, double cumulativeReward, int stepsThisEpisode) {
        this.messageType = GAME_FINAL_STATE;
        this.isTerminal = isTerminal;
        this.cumulativeReward = cumulativeReward;
        this.stepsThisEpisode = stepsThisEpisode;
    }

    public ISFSObject toSFSObject() {
        ISFSObject params = new SFSObject();
        params.putUtfString("messageType", this.messageType);
        if (this.userName != null && !this.userName.isEmpty()) {
            params.putUtfString("userName", this.userName);
        }
    
        switch (this.messageType) {
            case GAME_STATE_RESPONSE:
                params.putInt("stateId", this.stateId);
                break;
            case GAME_AVAILABLE_ACTIONS_RESPONSE:
                List<Integer> actionList = new ArrayList<>();
                for (int action : availableActions) {
                    actionList.add(action);
                }
                params.putIntArray("availableActions", actionList);
                break;
            case GAME_AVAILABLE_REWARDS_RESPONSE:
                List<Double> rewardList = new ArrayList<>();
                for (double reward : availableRewards) {
                    rewardList.add(reward);
                }
                params.putDoubleArray("availableRewards", rewardList);
                break;
            case GAME_ACTION_MOVE:
                params.putInt("action", this.action);
                params.putInt("stateId", this.stateId);
                break;
            case GAME_ACTION_REWARD_RESPONSE:
                params.putInt("action", this.action);
                params.putDouble("reward", this.reward);
                params.putInt("nextStateId", this.nextStateId);
                break;
            case GAME_FINAL_STATE_RESPONSE:
                params.putBool("isTerminal", this.isTerminal);
                params.putDouble("cumulativeReward", this.cumulativeReward);
                params.putInt("stepsThisEpisode", this.stepsThisEpisode);
                break;
            case GAME_RESET:
                params.putUtfString("userName", this.userName);
                break;
            case GAME_Q_UPDATE:
                List<Integer> qStateList = new ArrayList<>();
                for (int state : qStateIds) qStateList.add(state);
                params.putIntArray("qStateIds", qStateList);
    
                List<Integer> qActionList = new ArrayList<>();
                for (int action : qActionIndices) qActionList.add(action);
                params.putIntArray("qActionIndices", qActionList);
    
                List<Double> qValueList = new ArrayList<>();
                for (double q : qValues) qValueList.add(q);
                params.putDoubleArray("qValues", qValueList);
                break;
            case GAME_V_UPDATE:
                List<Integer> vStateList = new ArrayList<>();
                for (int state : vStateIds) vStateList.add(state);
                params.putIntArray("vStateIds", vStateList);
    
                List<Double> vValueList = new ArrayList<>();
                for (double v : vValues) vValueList.add(v);
                params.putDoubleArray("vValues", vValueList);
                break;
            case GAME_INFO_RESPONSE:
                params.putDouble("cumulativeReward", this.cumulativeReward);
                params.putInt("stepsThisEpisode", this.stepsThisEpisode);
                params.putInt("totalEpisodes", this.totalEpisodes);
                params.putInt("successfulEpisodes", this.successfulEpisodes);
                break;
            case GAME_ERROR:
                if (this.userName != null && !this.userName.isEmpty()) {
                    params.putUtfString("errorMessage", this.userName);
                }
                break;
            default:
                break;
        }
    
        return params;
    }    

    public void fromSFSObject(ISFSObject params) {
        this.messageType = params.getUtfString("messageType");
        this.userName = params.getUtfString("userName");
    
        switch (this.messageType) {
            case GAME_STATE:
                if (params.containsKey("stateId")) {
                    this.stateId = params.getInt("stateId");
                } else {
                    System.err.println("Missing 'stateId' in GAME_STATE message.");
                }
                break;
            case GAME_AVAILABLE_ACTIONS:
                List<Integer> availableActionsList = (List<Integer>) params.getIntArray("availableActions");
                if (availableActionsList != null) {
                    this.availableActions = availableActionsList.stream().mapToInt(Integer::intValue).toArray();
                } else {
                    System.err.println("Missing 'availableActions' in GAME_AVAILABLE_ACTIONS message.");
                    this.availableActions = new int[0];
                }
                break;
            case GAME_AVAILABLE_REWARDS:
                List<Double> availableRewardsList = (List<Double>) params.getDoubleArray("availableRewards");
                if (availableRewardsList != null) {
                    this.availableRewards = availableRewardsList.stream().mapToDouble(Double::doubleValue).toArray();
                } else {
                    System.err.println("Missing 'availableRewards' in GAME_AVAILABLE_REWARDS message.");
                    this.availableRewards = new double[0];
                }
                break;
            case GAME_ACTION_MOVE:
                if (params.containsKey("action") && params.containsKey("stateId")) {
                    this.action = params.getInt("action");
                    this.stateId = params.getInt("stateId");
                } else {
                    System.err.println("Missing 'action' or 'stateId' in GAME_ACTION_MOVE message.");
                }
                break;
            case GAME_ACTION_REWARD:
                if (params.containsKey("action") && params.containsKey("reward") && params.containsKey("nextStateId")) {
                    this.action = params.getInt("action");
                    this.reward = params.getDouble("reward");
                    this.nextStateId = params.getInt("nextStateId");
                } else {
                    System.err.println("Missing fields in GAME_ACTION_REWARD message.");
                }
                break;
            case GAME_FINAL_STATE_RESPONSE:
                if (params.containsKey("isTerminal") && params.containsKey("cumulativeReward") && params.containsKey("stepsThisEpisode")) {
                    this.isTerminal = params.getBool("isTerminal");
                    this.cumulativeReward = params.getDouble("cumulativeReward");
                    this.steps = params.getInt("stepsThisEpisode");
                } else {
                    System.err.println("Missing fields in GAME_FINAL_STATE_RESPONSE message.");
                }
                break;
            case GAME_RESET:
                break;
            case GAME_Q_UPDATE:
                List<Integer> qStateList = (List<Integer>) params.getIntArray("qStateIds");
                if (qStateList != null) {
                    this.qStateIds = qStateList.stream().mapToInt(Integer::intValue).toArray();
                } else {
                    System.err.println("Missing 'qStateIds' in GAME_Q_UPDATE message.");
                    this.qStateIds = new int[0];
                }
    
                List<Integer> qActionList = (List<Integer>) params.getIntArray("qActionIndices");
                if (qActionList != null) {
                    this.qActionIndices = qActionList.stream().mapToInt(Integer::intValue).toArray();
                } else {
                    System.err.println("Missing 'qActionIndices' in GAME_Q_UPDATE message.");
                    this.qActionIndices = new int[0];
                }
    
                List<Double> qValueList = (List<Double>) params.getDoubleArray("qValues");
                if (qValueList != null) {
                    this.qValues = qValueList.stream().mapToDouble(Double::doubleValue).toArray();
                } else {
                    System.err.println("Missing 'qValues' in GAME_Q_UPDATE message.");
                    this.qValues = new double[0];
                }
                break;
            case GAME_V_UPDATE:
                List<Integer> vStateList = (List<Integer>) params.getIntArray("vStateIds");
                if (vStateList != null) {
                    this.vStateIds = vStateList.stream().mapToInt(Integer::intValue).toArray();
                } else {
                    System.err.println("Missing 'vStateIds' in GAME_V_UPDATE message.");
                    this.vStateIds = new int[0];
                }
    
                List<Double> vValueList = (List<Double>) params.getDoubleArray("vValues");
                if (vValueList != null) {
                    this.vValues = vValueList.stream().mapToDouble(Double::doubleValue).toArray();
                } else {
                    System.err.println("Missing 'vValues' in GAME_V_UPDATE message.");
                    this.vValues = new double[0];
                }
                break;
            case GAME_INFO_RESPONSE:
                if (params.containsKey("cumulativeReward") && params.containsKey("stepsThisEpisode")
                    && params.containsKey("totalEpisodes") && params.containsKey("successfulEpisodes")) {
                    this.cumulativeReward = params.getDouble("cumulativeReward");
                    this.stepsThisEpisode = params.getInt("stepsThisEpisode");
                    this.totalEpisodes = params.getInt("totalEpisodes");
                    this.successfulEpisodes = params.getInt("successfulEpisodes");
                } else {
                    System.err.println("Missing fields in GAME_INFO_RESPONSE message.");
                }
                break;
            default:
                System.err.println("Unhandled message type in fromSFSObject: " + this.messageType);
                break;
        }
    }        
    
    // Getters and Setters for all fields
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public double getReward() {
        return reward;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public int getNextStateId() {
        return nextStateId;
    }

    public void setNextStateId(int nextStateId) {
        this.nextStateId = nextStateId;
    }

    public int[] getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(int[] availableActions) {
        this.availableActions = availableActions;
    }

    public double[] getAvailableRewards() {
        return availableRewards;
    }

    public void setAvailableRewards(double[] availableRewards) {
        this.availableRewards = availableRewards;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public void setTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    public int[] getQStateIds() {
        return qStateIds;
    }
    
    public void setQStateIds(int[] qStateIds) {
        this.qStateIds = qStateIds;
    }
    
    public int[] getQActionIndices() {
        return qActionIndices;
    }
    
    public void setQActionIndices(int[] qActionIndices) {
        this.qActionIndices = qActionIndices;
    }
    
    public double[] getQValues() {
        return qValues;
    }
    
    public void setQValues(double[] qValues) {
        this.qValues = qValues;
    }
    
    // Getters and Setters for V-Table Updates
    public int[] getVStateIds() {
        return vStateIds;
    }
    
    public void setVStateIds(int[] vStateIds) {
        this.vStateIds = vStateIds;
    }
    
    public double[] getVValues() {
        return vValues;
    }
    
    public void setVValues(double[] vValues) {
        this.vValues = vValues;
    }
    
    public void setSteps(int steps) {
        this.steps = steps;
    }
    
    public int getSteps() {
        return steps;
    }

    public double getCumulativeReward() {
        return cumulativeReward;
    }
    
    public void setCumulativeReward(double cumulativeReward) {
        this.cumulativeReward = cumulativeReward;
    }
    
    public int getStepsThisEpisode() {
        return stepsThisEpisode;
    }
    
    public void setStepsThisEpisode(int stepsThisEpisode) {
        this.stepsThisEpisode = stepsThisEpisode;
    }
    
    public int getTotalEpisodes() {
        return totalEpisodes;
    }
    
    public void setTotalEpisodes(int totalEpisodes) {
        this.totalEpisodes = totalEpisodes;
    }
    
    public int getSuccessfulEpisodes() {
        return successfulEpisodes;
    }
    
    public void setSuccessfulEpisodes(int successfulEpisodes) {
        this.successfulEpisodes = successfulEpisodes;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}