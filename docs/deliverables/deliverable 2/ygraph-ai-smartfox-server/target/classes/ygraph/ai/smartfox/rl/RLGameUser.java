package ygraph.ai.smartfox.rl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.smartfoxserver.v2.entities.User;

// This class represents a user within an RL game (server-side representation of the RL agent)
public class RLGameUser {

    // User and RLWorld defined
    private final User user;
    private final RLWorld world;

    private static final HashMap<String, String> ENV = loadEnv();

    // Creating variables for state ID, a final reward, a terminal state check, maximum episodes, and maximum number of steps in an episode
    private int currentStateId;
    private double lastReward;
    private boolean isTerminal;
    private final int maxEpisodes = Integer.parseInt(ENV.getOrDefault("EPISODE_COUNT", "2"));
    private int maxStepsPerEpisode = Integer.parseInt(ENV.getOrDefault("MAX_STEPS", "10"));
    private static final String EPISODE_SEPARATOR = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
    public int getMaxEpisodes() {
        return maxEpisodes;
    }

    // Loading in .env file
    private static HashMap<String, String> loadEnv() {
        HashMap<String, String> env = new HashMap<>();
        String workingDir = System.getProperty("user.dir");
        System.out.println("Current Working Directory: " + workingDir);
        try (BufferedReader br = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
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

    // Getters and setters for the attributes
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

    // Number of max episodes possible, successful episodes, cumulative reward for an episode, steps done in one episode so far
    private int totalEpisodes = 0;
    private int successfulEpisodes = 0;
    private double cumulativeReward = 0.0;
    private int stepsThisEpisode = 0;

    // Threshold for evaluation
    private double successRewardThreshold = Double.parseDouble(ENV.getOrDefault("SUCCESS_REWARD_THRESHOLD", "1.0"));

    // Grid size
    private final int gridSize = Integer.parseInt(ENV.getOrDefault("GRID_SIZE", "5"));

    // Constructor that links a User object with an RL world and starts the game
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
            return;
        }

        int actionIndex = mapActionStringToIndex(actionStr);
        int row = currentStateId / gridSize;
        int col = currentStateId % gridSize;

        if (!isValidPosition(row, col)) {
            System.err.println("Invalid current position: (" + row + ", " + col + ") for user: " + user.getName());
            return;
        }
    
        // Move the agent
        int newStateId = world.moveAgentWithAction(currentStateId, actionIndex);
        double reward = world.getLastReward();
    
        // Update state and last step reward
        this.currentStateId = newStateId;
        this.lastReward = reward;
        stepsThisEpisode++;
        cumulativeReward += Math.pow(world.getGamma(), stepsThisEpisode - 1) * lastReward;

        // New position of agent
        int newRow = newStateId / gridSize;
        int newCol = newStateId % gridSize;
        if (!isValidPosition(newRow, newCol)) {
            System.err.println("Invalid new position: (" + newRow + ", " + newCol + ") for user: " + user.getName());
            return;
        }

        int stopMethod = Integer.parseInt(ENV.getOrDefault("STOP_METHOD", "0"));
        double stopProb = Double.parseDouble(ENV.getOrDefault("STOP_PROB", "0.1"));
        System.out.println("DEBUG (Server): stopMethod=" + stopMethod);

        // For probabilistic stopping, apply only after atleast 1 step has been taken
        if (stopMethod == 2 && stepsThisEpisode > 1) {
            double rnd = Math.random();
            if (rnd < stopProb) {
                isTerminal = true;
                System.out.println("User " + user.getName() + " stopped due to STOP_METHOD=2 random stopping condition.");
            }
        }

        if (world.isTerminalState(currentStateId)) {
            isTerminal = true;
            System.out.println("User " + user.getName() + " has reached the terminal state: " + currentStateId);
        } else if (stopMethod == 0 && stepsThisEpisode >= maxStepsPerEpisode) {
            isTerminal = true;
            System.out.println("User " + user.getName() + " reached maximum steps per episode.");
        }
        System.out.println("Action Taken: " + actionStr + ", New State: " + newStateId + ", Reward: " + reward);
    }    

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < gridSize && col >= 0 && col < gridSize;
    }    

    // Change action strings to action indices
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

    // Episode summary generator and also resets the variables if an episode has ended
    public void concludeEpisode() {
        if (!isTrainingComplete()) {
            totalEpisodes++;
            if (cumulativeReward >= successRewardThreshold) {
                successfulEpisodes++;
                System.out.println("User " + user.getName() + " achieved success in episode " + totalEpisodes);
            }
            System.out.println(EPISODE_SEPARATOR);
            System.out.println("End of Episode Summary:");
            System.out.println(" - Total Episodes: " + totalEpisodes);
            System.out.println(" - Successful Episodes: " + successfulEpisodes);
            System.out.println(" - Steps Taken: " + stepsThisEpisode);
            System.out.println(" - Discounted Episode Reward: " + cumulativeReward);
            System.out.println(EPISODE_SEPARATOR);
            if (isTrainingComplete()) {
                System.out.println("Maximum number of episodes reached. Ending training.");
            } else {
                resetGame();
            }

            this.stepsThisEpisode = 0;
            this.cumulativeReward = 0.0;
            this.isTerminal = false;
            this.world.reset();
            System.out.println("Episode concluded for user: " + user.getName());
        }
    }

    public int getCurrentStateId() {
        return currentStateId;
    }

    public double getLastReward() {
        return lastReward;
    }

    public void setTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    // Resets agent's variables post-episode
    public void resetGame() {
        initializeGame();
        cumulativeReward = 0.0;
        stepsThisEpisode = 0;
        isTerminal = false;
        System.out.println("Game reset for user: " + user.getName() + ". New starting state: " + currentStateId);
    }

    // Clean up the world when a user has been removed from the game room
    public void cleanup() {
        System.out.println("Cleaning up RLGameUser for user: " + user.getName());
        world.cleanup();
    }

    public void incrementEpisodes() {
        this.totalEpisodes++;
    }

    public boolean isTrainingComplete() {
        return totalEpisodes >= maxEpisodes;
    }    

    public void updateRewards(double reward) {
        this.cumulativeReward += reward;
    }

    public void updateSteps(int steps) {
        this.stepsThisEpisode += steps;
    }

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
