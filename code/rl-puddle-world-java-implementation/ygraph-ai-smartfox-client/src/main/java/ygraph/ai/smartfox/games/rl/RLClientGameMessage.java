package ygraph.ai.smartfox.games.rl;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// This class creates and manages attributes of a message sent between the client and server
public class RLClientGameMessage {

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

    // Server to Client messages
    public static final String GAME_STATE_RESPONSE = "GAME_STATE_RESPONSE";
    public static final String GAME_AVAILABLE_ACTIONS_RESPONSE = "GAME_AVAILABLE_ACTIONS_RESPONSE";
    public static final String GAME_AVAILABLE_REWARDS_RESPONSE = "GAME_AVAILABLE_REWARDS_RESPONSE";
    public static final String GAME_ACTION_REWARD_RESPONSE = "GAME_ACTION_REWARD_RESPONSE";
    public static final String GAME_FINAL_STATE_RESPONSE = "GAME_FINAL_STATE_RESPONSE";
    public static final String GAME_RESET_RESPONSE = "GAME_RESET_RESPONSE";
    public static final String GAME_INFO_RESPONSE = "GAME_INFO_RESPONSE";
    public static final String GAME_ERROR = "GAME_ERROR";
    
    // Message Fields
    private String messageType;
    String userName;
    private int stateId;
    private int action;
    private double reward;
    private int nextStateId;
    private int[] availableActions;
    private double[] availableRewards;
    private boolean isTerminal;
    private int[] qStateIds;
    private int[] qActionIndices;
    private double[] qValues;
    private int[] vStateIds;
    private double[] vValues;
    private double cumulativeReward;
    private int stepsThisEpisode;
    private int totalEpisodes;
    private int successfulEpisodes;

    // Multiple constructors to make communication easier
    public RLClientGameMessage() {
        this.messageType = "";
        this.stateId = 0;
        // this.action = -1;
        this.reward = 0.0;
        // this.nextStateId = 0;
        this.availableActions = new int[0];
        this.availableRewards = new double[0];
        this.isTerminal = false;
    }

    public RLClientGameMessage(int[] qStateIds, int[] qActionIndices, double[] qValues) {
        this.messageType = GAME_Q_UPDATE;
        this.qStateIds = qStateIds;
        this.qActionIndices = qActionIndices;
        this.qValues = qValues;
    }

    public RLClientGameMessage(int[] vStateIds, double[] vValues) {
        this.messageType = GAME_V_UPDATE;
        this.vStateIds = vStateIds;
        this.vValues = vValues;
    }

    public RLClientGameMessage(String messageType) {
        this();
        this.messageType = messageType;
    }

    public RLClientGameMessage(int stateId) {
        this(GAME_STATE);
        this.stateId = stateId;
    }

    public RLClientGameMessage(int[] availableActions) {
        this(GAME_AVAILABLE_ACTIONS);
        this.availableActions = availableActions;
    }

    public RLClientGameMessage(double[] availableRewards) {
        this(GAME_AVAILABLE_REWARDS);
        this.availableRewards = availableRewards;
    }

    public RLClientGameMessage(int action, int stateId) {
        this(GAME_ACTION_MOVE);
        this.action = action;
        this.stateId = stateId;
    }

    public RLClientGameMessage(int action, double reward, int nextStateId) {
        this(GAME_ACTION_REWARD);
        this.action = action;
        this.reward = reward;
        this.nextStateId = nextStateId;
    }

    public RLClientGameMessage(boolean isTerminal, double cumulativeReward, int stepsThisEpisode) {
        this.messageType = GAME_FINAL_STATE;
        this.isTerminal = isTerminal;
        this.cumulativeReward = cumulativeReward;
        this.stepsThisEpisode = stepsThisEpisode;
    }

    public RLClientGameMessage(boolean isTerminal) {
        this(GAME_FINAL_STATE);
        this.isTerminal = isTerminal;
    }

    public static RLClientGameMessage resetMessage(String userName) {
        RLClientGameMessage msg = new RLClientGameMessage(GAME_RESET);
        msg.setUserName(userName);
        return msg;
    }

    // Converting RLClientGameMessage into an ISFSObject for sending data to the server
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
                params.putInt("stateId", this.stateId);
                List<Integer> actionList = new ArrayList<>();
                for (int action : availableActions) {
                    actionList.add(action);
                }
                params.putIntArray("availableActions", actionList);
                break;
            case GAME_AVAILABLE_REWARDS:
                params.putInt("stateId", this.stateId);
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
            case GAME_INFO:
                if (this.cumulativeReward != 0.0 || this.stepsThisEpisode != 0 || this.totalEpisodes != 0 || this.successfulEpisodes != 0) {
                    params.putDouble("cumulativeReward", this.cumulativeReward);
                    params.putInt("stepsThisEpisode", this.stepsThisEpisode);
                    params.putInt("totalEpisodes", this.totalEpisodes);
                    params.putInt("successfulEpisodes", this.successfulEpisodes);
                }
                break;
            case GAME_ERROR:
                if (this.userName != null && !this.userName.isEmpty()) {
                    params.putUtfString("error", this.userName);
                }
                break;
            default:
                break;
        }

        return params;
    }

    // Populate the fields from an ISFSObject received from the server
    public void fromSFSObject(ISFSObject params) {
        this.messageType = params.getUtfString("messageType");
        this.userName = params.getUtfString("userName");
    
        switch (this.messageType) {
            case GAME_STATE_RESPONSE:
                this.stateId = params.getInt("stateId");
                break;
            case GAME_AVAILABLE_ACTIONS_RESPONSE:
                List<Integer> availableActionsList = (List<Integer>) params.getIntArray("availableActions");
                if (availableActionsList != null) {
                    this.availableActions = availableActionsList.stream().mapToInt(Integer::intValue).toArray();
                } else {
                    this.availableActions = new int[0];
                }
                break;
            case GAME_AVAILABLE_REWARDS_RESPONSE:
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
            case GAME_ACTION_REWARD_RESPONSE:
                this.action = params.getInt("action");
                this.reward = params.getDouble("reward");
                this.nextStateId = params.getInt("nextStateId");
                break;
            case GAME_FINAL_STATE_RESPONSE:
                this.isTerminal = params.getBool("isTerminal");
                this.cumulativeReward = params.getDouble("cumulativeReward");
                this.stepsThisEpisode = params.getInt("stepsThisEpisode");
                break;
            case GAME_RESET:
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
                this.stepsThisEpisode = params.getInt("stepsThisEpisode");
                this.totalEpisodes = params.getInt("totalEpisodes");
                this.successfulEpisodes = params.getInt("successfulEpisodes");
                break;
            case GAME_ERROR:
                if (params.containsKey("error")) {
                    this.userName = params.getUtfString("error");
                } else {
                    System.err.println("Missing 'error' field in GAME_ERROR message.");
                }
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

    // toString for debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RLClientGameMessage { ");
        sb.append("messageType: ").append(messageType);

        switch (messageType) {
            case GAME_STATE:
                sb.append(", stateId: ").append(stateId);
                break;
            case GAME_AVAILABLE_ACTIONS:
                sb.append(", availableActions: ").append(Arrays.toString(availableActions));
                break;
            case GAME_AVAILABLE_REWARDS:
                sb.append(", availableRewards: ").append(Arrays.toString(availableRewards));
                break;
            case GAME_ACTION_MOVE:
                sb.append(", action: ").append(action);
                sb.append(", stateId: ").append(stateId);
                break;
            case GAME_ACTION_REWARD:
                sb.append(", action: ").append(action);
                sb.append(", reward: ").append(reward);
                sb.append(", nextStateId: ").append(nextStateId);
                break;
            case GAME_FINAL_STATE:
                sb.append(", isTerminal: ").append(isTerminal);
                break;
            case GAME_RESET:
                sb.append(", userName: ").append(userName);
                break;
            case GAME_INFO:
                sb.append(", cumulativeReward: ").append(cumulativeReward);
                sb.append(", stepsThisEpisode: ").append(stepsThisEpisode);
                sb.append(", totalEpisodes: ").append(totalEpisodes);
                sb.append(", successfulEpisodes: ").append(successfulEpisodes);
                break;
            case GAME_ERROR:
                sb.append(", error: ").append(userName);
                break;
            default:
                sb.append(", Unknown message type");
                break;
        }

        sb.append(" }");
        return sb.toString();
    }
}
