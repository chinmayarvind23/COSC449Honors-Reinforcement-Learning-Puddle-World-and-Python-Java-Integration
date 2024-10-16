package ygraph.ai.smartfox.rl;

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

    // Server to Client messages
    public static final String GAME_STATE_RESPONSE = "GAME_STATE_RESPONSE";
    public static final String GAME_AVAILABLE_ACTIONS_RESPONSE = "GAME_AVAILABLE_ACTIONS_RESPONSE";
    public static final String GAME_AVAILABLE_REWARDS_RESPONSE = "GAME_AVAILABLE_REWARDS_RESPONSE";
    public static final String GAME_ACTION_REWARD_RESPONSE = "GAME_ACTION_REWARD_RESPONSE";
    public static final String GAME_FINAL_STATE_RESPONSE = "GAME_FINAL_STATE_RESPONSE";
    public static final String GAME_RESET_RESPONSE = "GAME_RESET_RESPONSE";
    public static final String GAME_ERROR = "GAME_ERROR";
    public static final String GAME_INFO = "GAME_INFO";
}