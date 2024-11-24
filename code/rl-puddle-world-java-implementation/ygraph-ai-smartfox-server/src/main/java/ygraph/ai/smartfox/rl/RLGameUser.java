package ygraph.ai.smartfox.rl;

import com.smartfoxserver.v2.entities.User;

// This class represents a user within an RL game and holds their current state, final reward, and terminal state check
public class RLGameUser {

    // User and RLWorld defined
    private final User user;
    private final RLWorld world;

    // Creating a state ID, a final reward, and a terminal state check
    private int currentStateId;
    private double lastReward;
    private boolean isTerminal;
    private final int maxEpisodes = 2;
    private int maxStepsPerEpisode = 10; 
    public int getMaxEpisodes() {
        return maxEpisodes;
    }

    public int getMaxStepsPerEpisode() {
        return maxStepsPerEpisode;
    }

    public void setMaxStepsPerEpisode(int maxStepsPerEpisode) {
        this.maxStepsPerEpisode = maxStepsPerEpisode;
    }

    public void setTotalEpisodes(int totalEpisodes) {
        this.totalEpisodes = totalEpisodes;
    }

    public void setSuccessfulEpisodes(int successfulEpisodes) {
        this.successfulEpisodes = successfulEpisodes;
    }

    public void setCumulativeReward(double cumulativeReward) {
        this.cumulativeReward = cumulativeReward;
    }

    public void setStepsThisEpisode(int stepsThisEpisode) {
        this.stepsThisEpisode = stepsThisEpisode;
    }

    public void setSuccessRewardThreshold(double successRewardThreshold) {
        this.successRewardThreshold = successRewardThreshold;
    }

    public int getGridSize() {
        return gridSize;
    }

    private int totalEpisodes = 0;
    private int successfulEpisodes = 0;
    private double cumulativeReward = 0.0;
    private int stepsThisEpisode = 0;

    // Thresholds for evaluation
    private double successRewardThreshold = 1.0;

    // Grid size (assuming a 5x5 grid)
    private final int gridSize = 5;

    // Constructor that associates a User object with an RL world and starts the game
    public RLGameUser(User user, RLWorld world) {
        this.user = user;
        this.world = world;
        this.currentStateId = 0;
        this.lastReward = 0.0;
        this.isTerminal = false;
        initializeGame();
        System.out.println("RLGameUser initialized for user: " + user.getName() + " with RLWorld instance: " + System.identityHashCode(world));
    }

    // Starts the game by resetting the world and sets the initial state for the user in the RL world, a final reward, and a terminal state check to false
    public void initializeGame() {
        world.reset();
        currentStateId = 0;
        lastReward = 0.0;
        isTerminal = false;
        System.out.println("Game initialized for user: " + user.getName() + " at state: " + currentStateId);
    }

    // Gets an RLWorld instance
    public RLWorld getWorld() {
        if (world == null) {
            System.err.println("RLWorld is null for user: " + user.getName());
        } else {
            System.out.println("RLWorld retrieved for user: " + user.getName() + " - Instance ID: " + System.identityHashCode(world));
        }
        return world;
    }

    // Gets a user instance
    public User getUser() {
        return this.user;
    }

    // Processes an action taken by a user for the following actions: "UP", "DOWN", "LEFT", "RIGHT".
    public void takeAction(String actionStr) {
        if (isTerminal) {
            return; // Episode already terminated
        }
    
        // Map action string to index
        int actionIndex = mapActionStringToIndex(actionStr);
        
        // Use gridSize to map stateId to (row, col)
        int row = currentStateId / gridSize;
        int col = currentStateId % gridSize;
    
        // Validate current position
        if (!isValidPosition(row, col)) {
            System.err.println("Invalid current position: (" + row + ", " + col + ") for user: " + user.getName());
            concludeEpisode();
            return;
        }
    
        // Perform the action in the world using actionIndex
        int newStateId = world.moveAgentWithAction(currentStateId, actionIndex);
        
        // Update last reward
        double reward = world.getLastReward();
        
        // Update user's state and reward
        this.currentStateId = newStateId;
        this.lastReward = reward;
        
        // Increment steps and cumulative reward
        stepsThisEpisode++;
        cumulativeReward += lastReward;
        
        // Calculate new row and col after action
        int newRow = newStateId / gridSize;
        int newCol = newStateId % gridSize;
    
        // Validate new position
        if (!isValidPosition(newRow, newCol)) {
            System.err.println("Invalid new position: (" + newRow + ", " + newCol + ") for user: " + user.getName());
            concludeEpisode();
            return;
        }
    
        // Check for terminal state
        if (world.isTerminalState(currentStateId)) {
            isTerminal = true;
            System.out.println("User " + user.getName() + " has reached the terminal state: " + currentStateId);
            concludeEpisode();
        } else if (stepsThisEpisode >= maxStepsPerEpisode) {
            isTerminal = true;
            System.out.println("User " + user.getName() + " reached maximum steps per episode.");
            concludeEpisode();
        }
        
        // Log the state transition and reward
        System.out.println("Action Taken: " + actionStr + ", New State: " + newStateId + ", Reward: " + reward);
    }
    
    // Helper method to validate position
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < gridSize && col >= 0 && col < gridSize;
    }    

    // Helper method to map action strings to indices
    private int mapActionStringToIndex(String actionStr) {
        switch (actionStr) {
            case "UP":
                return 0;
            case "DOWN":
                return 1;
            case "LEFT":
                return 2;
            case "RIGHT":
                return 3;
            default:
                throw new IllegalArgumentException("Invalid action string: " + actionStr);
        }
    }

    public void addToCumulativeReward(double reward) {
        this.cumulativeReward += reward;
    }

    public void concludeEpisode() {
        totalEpisodes++; // Increment only once
        if (cumulativeReward >= successRewardThreshold) {
            successfulEpisodes++;
            System.out.println("User " + user.getName() + " achieved success in episode " + totalEpisodes);
        }
        
        System.out.println("End of Episode Summary:");
        System.out.println(" - Total Episodes: " + totalEpisodes);
        System.out.println(" - Successful Episodes: " + successfulEpisodes);
        System.out.println(" - Steps Taken: " + stepsThisEpisode);
        System.out.println(" - Cumulative Reward: " + cumulativeReward);
        
        // Check if the maximum number of episodes has been reached
        if (isTrainingComplete()) {
            System.out.println("Maximum number of episodes reached. Ending training.");
            // Do not reset the game; training is complete
        } else {
            resetGame();
        }

        // Reset episode-specific variables
        stepsThisEpisode = 0;
        cumulativeReward = 0.0;
        isTerminal = false;
    }

    // Gets the ID of the current state the RL agent is in
    public int getCurrentStateId() {
        return currentStateId;
    }

    // Gets the final reward of an episode for an RL agent
    public double getLastReward() {
        return lastReward;
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
        isTerminal = false;
        System.out.println("Game reset for user: " + user.getName() + ". New starting state: " + currentStateId);
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

    public boolean isTrainingComplete() {
        return totalEpisodes >= maxEpisodes;
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
