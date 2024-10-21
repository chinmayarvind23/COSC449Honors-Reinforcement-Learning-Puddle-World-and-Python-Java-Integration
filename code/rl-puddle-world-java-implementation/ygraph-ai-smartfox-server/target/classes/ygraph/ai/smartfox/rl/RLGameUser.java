package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.User;

// This class represents a user within in an RL game, and holds their current state, final reward, and terminal state check
public class RLGameUser {

    // User and RLWorlds defined
    private final User user;
    private final RLWorld world;

    // Creating a state ID, a final reward, and a terminal state check
    private int currentStateId;
    private double lastReward;
    private boolean isTerminal;

    private int totalEpisodes = 0;
    private int successfulEpisodes = 0;
    private double cumulativeReward = 0.0;
    private int stepsThisEpisode = 0;

    // Thresholds for evaluation
    private double successRewardThreshold = 1.0;

    // Constructor that associates a User object with an RL world and starts the game
    public RLGameUser(User user, RLWorld world) {
        this.user = user;
        this.world = world;
        this.currentStateId = 0;
        this.lastReward = 0.0;
        this.isTerminal = false;
        initializeGame();
    }

    // Starts the game by resetting the world and sets the initial state for the user in the RL world, a final reward and a terminal state check to false
    public void initializeGame() {
        world.reset();
        currentStateId = world.getCurrentStateId();
        lastReward = 0.0;
        isTerminal = false;
        System.out.println("Game initialized for user: " + user.getName() + " at state: " + currentStateId);
    }

    // Gets an RLWorld instance
    public RLWorld getWorld() {
        return world;
    }

    // Gets a user instance
    public User getUser() {
        return user;
    }

    // Processes an action taken by a user for the following actions: "UP", "DOWN", "LEFT", "RIGHT".
    public void takeAction(String actionStr) {
        // Terminal state check, no further processing needed
        if (isTerminal) {
            System.out.println("User " + user.getName() + " attempted to take action in a terminal state.");
            return;
        }

        // Check if the action string is one of the 4 actions possible in the puddle world
        if (!isValidAction(actionStr)) {
            System.out.println("Invalid action '" + actionStr + "' received from user: " + user.getName());
            return;
        }

        // Performs the action within the RL world and sets the current state ID to the next one
        int nextStateId = world.simulateAction(currentStateId, actionStr);
        lastReward = world.getReward(currentStateId, actionStr, nextStateId);
        currentStateId = nextStateId;

        System.out.println("User " + user.getName() + " performed action '" + actionStr + "'. New state: " + currentStateId + ", Reward: " + lastReward);

        // New state terminal check
        if (world.isTerminalState(currentStateId)) {
            isTerminal = true;
            System.out.println("User " + user.getName() + " has reached a terminal state.");
            resetGame();
        }
    }

    // Checks if the action string is valid and one of the 4 possible actions in the puddle world
    private boolean isValidAction(String action) {
        return action.equals("UP") || action.equals("DOWN") || action.equals("LEFT") || action.equals("RIGHT");
    }

    // Gets the ID of the current state the RL agent is in
    public int getCurrentStateId() {
        return currentStateId;
    }

    // Gets the final reward of an episode for an RL agent
    public double getLastReward() {
        return lastReward;
    }

    // Updates the Q-Table based on action, reward, and next state
    public void updateQTable(int action, double reward, int nextStateId) {
        world.updateQTable(currentStateId, action, reward, nextStateId);
    }

    // Set if the current state is the goal state
    public void setTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
    }

    // Check if current state is the goal state
    public boolean isTerminal() {
        return isTerminal;
    }

    // Resets the game environment post-episode
    public void resetGame() {
        initializeGame();
        cumulativeReward = 0.0;
        stepsThisEpisode = 0;
    }

    // Cleans up the world, done in the RLWorld class when a user leaves a world
    public void cleanup() {
        System.out.println("Cleaning up RLGameUser for user: " + user.getName());
        world.cleanup();
    }

    // Increment total episodes
    public void incrementEpisodes() {
        this.totalEpisodes++;
    }

    // Update cumulative rewards
    public void updateRewards(double reward) {
        this.cumulativeReward += reward;
    }

    // Update steps taken in the current episode
    public void updateSteps(int steps) {
        this.stepsThisEpisode += steps;
    }

    // Increment successful episodes
    public void incrementSuccessfulEpisodes() {
        this.successfulEpisodes++;
    }

    public double getCumulativeReward() {
        return cumulativeReward;
    }

    public int getStepsThisEpisode() {
        return stepsThisEpisode;
    }

    public int getTotalEpisodes() {
        return totalEpisodes;
    }

    public int getSuccessfulEpisodes() {
        return successfulEpisodes;
    }

    public double getSuccessRewardThreshold() {
        return successRewardThreshold;
    }
    
    public void setStateId(int stateId) {
        this.currentStateId = stateId;
    }

    public void updateStepsThisEpisode(int steps) {
        this.stepsThisEpisode += steps;
    }
}