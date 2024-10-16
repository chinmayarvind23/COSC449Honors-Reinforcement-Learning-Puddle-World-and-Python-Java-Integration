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

    // Server to Client messages
    public static final String GAME_STATE_RESPONSE = "GAME_STATE_RESPONSE";
    public static final String GAME_AVAILABLE_ACTIONS_RESPONSE = "GAME_AVAILABLE_ACTIONS_RESPONSE";
    public static final String GAME_AVAILABLE_REWARDS_RESPONSE = "GAME_AVAILABLE_REWARDS_RESPONSE";
    public static final String GAME_ACTION_REWARD_RESPONSE = "GAME_ACTION_REWARD_RESPONSE";
    public static final String GAME_FINAL_STATE_RESPONSE = "GAME_FINAL_STATE_RESPONSE";
    public static final String GAME_RESET_RESPONSE = "GAME_RESET_RESPONSE";
    public static final String GAME_ERROR = "GAME_ERROR";
    public static final String GAME_INFO = "GAME_INFO";

    // Fields for Q-Table updates
    private int[] qStateIds;
    private int[] qActionIndices;
    private double[] qValues;
    private String messageType;
    String userName;
    private int stateId;
    private int action;
    private double reward;
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

    public RLGameMessage() {
        this.messageType = "";
        this.userName = "";
        this.stateId = -1;
        this.action = -1;
        this.reward = 0.0;
        this.nextStateId = -1;
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

    public ISFSObject toSFSObject() {
        ISFSObject params = new SFSObject();
        params.putUtfString("messageType", this.messageType);
        if (this.userName != null && !this.userName.isEmpty()) {
            params.putUtfString("userName", this.userName);
        }
    
        switch (this.messageType) {
            case GAME_STATE:
                params.putInt("stateId", this.stateId);
                break;
            case GAME_AVAILABLE_ACTIONS:
                List<Integer> actionList = new ArrayList<>();
                for (int action : availableActions) {
                    actionList.add(action);
                }
                params.putIntArray("availableActions", actionList);
                break;
            case GAME_AVAILABLE_REWARDS:
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
            case GAME_ACTION_REWARD:
                params.putInt("action", this.action);
                params.putDouble("reward", this.reward);
                params.putInt("nextStateId", this.nextStateId);
                break;
            case GAME_FINAL_STATE:
                params.putBool("isTerminal", this.isTerminal);
                params.putDouble("reward", this.reward);
                params.putInt("steps", this.steps);
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
            case GAME_INFO:
                params.putDouble("cumulativeReward", this.cumulativeReward);
                params.putInt("stepsThisEpisode", this.stepsThisEpisode);
                params.putInt("totalEpisodes", this.totalEpisodes);
                params.putInt("successfulEpisodes", this.successfulEpisodes);
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
                this.stateId = params.getInt("stateId");
                break;
            case GAME_AVAILABLE_ACTIONS:
                List<Integer> availableActionsList = (List<Integer>) params.getIntArray("availableActions");
                if (availableActionsList != null) {
                    this.availableActions = availableActionsList.stream().mapToInt(Integer::intValue).toArray();
                } else {
                    this.availableActions = new int[0];
                }
                break;
            case GAME_AVAILABLE_REWARDS:
                List<Double> availableRewardsList = (List<Double>) params.getDoubleArray("availableRewards");
                if (availableRewardsList != null) {
                    this.availableRewards = availableRewardsList.stream().mapToDouble(Double::doubleValue).toArray();
                } else {
                    this.availableRewards = new double[0];
                }
                break;
            case GAME_ACTION_MOVE:
                this.action = params.getInt("action");
                this.stateId = params.getInt("stateId");
                break;
            case GAME_ACTION_REWARD:
                this.action = params.getInt("action");
                this.reward = params.getDouble("reward");
                this.nextStateId = params.getInt("nextStateId");
                break;
            case GAME_FINAL_STATE:
                this.isTerminal = params.getBool("isTerminal");
                this.reward = params.getDouble("reward"); // Assuming 'reward' is sent
                this.steps = params.getInt("steps"); // Assuming 'steps' is sent
                break;
            case GAME_RESET:
                // No fields to set
                break;
            case GAME_Q_UPDATE:
                List<Integer> qStateList = (List<Integer>) params.getIntArray("qStateIds");
                if (qStateList != null) {
                    this.qStateIds = qStateList.stream().mapToInt(Integer::intValue).toArray();
                } else {
                    this.qStateIds = new int[0];
                }
    
                List<Integer> qActionList = (List<Integer>) params.getIntArray("qActionIndices");
                if (qActionList != null) {
                    this.qActionIndices = qActionList.stream().mapToInt(Integer::intValue).toArray();
                } else {
                    this.qActionIndices = new int[0];
                }
    
                List<Double> qValueList = (List<Double>) params.getDoubleArray("qValues");
                if (qValueList != null) {
                    this.qValues = qValueList.stream().mapToDouble(Double::doubleValue).toArray();
                } else {
                    this.qValues = new double[0];
                }
                break;
            case GAME_V_UPDATE:
                List<Integer> vStateList = (List<Integer>) params.getIntArray("vStateIds");
                if (vStateList != null) {
                    this.vStateIds = vStateList.stream().mapToInt(Integer::intValue).toArray();
                } else {
                    this.vStateIds = new int[0];
                }
    
                List<Double> vValueList = (List<Double>) params.getDoubleArray("vValues");
                if (vValueList != null) {
                    this.vValues = vValueList.stream().mapToDouble(Double::doubleValue).toArray();
                } else {
                    this.vValues = new double[0];
                }
                break;
            case GAME_INFO:
                this.cumulativeReward = params.getDouble("cumulativeReward");
                this.steps = params.getInt("stepsThisEpisode");
                this.stateId = params.getInt("totalEpisodes");
                this.action = params.getInt("successfulEpisodes");
                break;
            default:
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
}