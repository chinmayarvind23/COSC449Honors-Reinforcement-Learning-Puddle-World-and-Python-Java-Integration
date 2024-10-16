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

    // Server to Client messages
    public static final String GAME_STATE_RESPONSE = "GAME_STATE_RESPONSE";
    public static final String GAME_AVAILABLE_ACTIONS_RESPONSE = "GAME_AVAILABLE_ACTIONS_RESPONSE";
    public static final String GAME_AVAILABLE_REWARDS_RESPONSE = "GAME_AVAILABLE_REWARDS_RESPONSE";
    public static final String GAME_ACTION_REWARD_RESPONSE = "GAME_ACTION_REWARD_RESPONSE";
    public static final String GAME_FINAL_STATE_RESPONSE = "GAME_FINAL_STATE_RESPONSE";
    public static final String GAME_RESET_RESPONSE = "GAME_RESET_RESPONSE";
    public static final String GAME_ERROR = "GAME_ERROR";
    public static final String GAME_INFO = "GAME_INFO";
    

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

    // Multiple constructors to make communication easier
    public RLClientGameMessage() {
        this.messageType = "";
        this.stateId = -1;
        this.action = -1;
        this.reward = 0.0;
        this.nextStateId = -1;
        this.availableActions = new int[0];
        this.availableRewards = new double[0];
        this.isTerminal = false;
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
                break;
            case GAME_RESET:
                params.putUtfString("userName", this.userName);
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
                break;
            case GAME_RESET:
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
            default:
                sb.append(", Unknown message type");
                break;
        }

        sb.append(" }");
        return sb.toString();
    }
}
