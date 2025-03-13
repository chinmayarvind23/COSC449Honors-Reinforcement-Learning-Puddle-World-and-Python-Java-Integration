package ygraph.ai.smartfox.games.rl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

// This class holds the game's state specific to RL puddle world parameters like: state, available actions, rewards and the final state check.
public class RLGameModel {
    private int stateId;
    private int[] availableActions;
    private double[] availableRewards;
    private boolean isTerminal;
    private static final HashMap<String, String> ENV = loadEnv();
    private double cumulativeReward;
    private int stepsThisEpisode;
    private int maxStepsPerEpisode = Integer.parseInt(ENV.getOrDefault("MAX_STEPS", "10"));
    private boolean success;
    private double gamma = Double.parseDouble(ENV.getOrDefault("GAMMA", "0.9"));
    private static final String EPISODE_SEPARATOR = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";

    // Number of episodes completed - for testing purposes, will be cleaned up soon
    private int totalEpisodes = Integer.parseInt(ENV.getOrDefault("EPISODE_COUNT", "2"));

    // Number of episodes possible - for testing purposes, will be cleaned up soon
    private int maxEpisodes = Integer.parseInt(ENV.getOrDefault("EPISODE_COUNT", "2"));

    private int successfulEpisodes = 0;
    public void setSuccessfulEpisodes(int successfulEpisodes) {
        this.successfulEpisodes = successfulEpisodes;
    }

    // Reward threshold to determine the agent succeeded in navigating the world efficiently
    private double successRewardThreshold = Double.parseDouble(ENV.getOrDefault("SUCCESS_REWARD_THRESHOLD", "1.0"));
    private int gridSize = Integer.parseInt(ENV.getOrDefault("GRID_SIZE", "5"));
    private static final int MAX_EPISODES = Integer.parseInt(ENV.getOrDefault("EPISODE_COUNT", "2"));
    private double episodeReward = 0.0;
    private double totalReward = 0.0;
    private int currentEpisode = 0;
    private int totalSteps = 0;
    private boolean episodeComplete = false;
    private boolean trainingComplete = false;
    private static final int MAX_STEPS = Integer.parseInt(ENV.getOrDefault("MAX_STEPS", "10"));
    private boolean isGoalReached = false;
    private int GOAL_STATE = (gridSize * gridSize) - 1;
    private static final double GOAL_REWARD = Double.parseDouble(ENV.getOrDefault("GOAL_REWARD", "10.0"));;

    private RLGamePlayer gamePlayer;

    public void setGamePlayer(RLGamePlayer player) {
        this.gamePlayer = player;
    }

    // Initialize game model with state, available actions, rewards, final state check, cumulative reward, and success check
    public RLGameModel() {
        this.stateId = 0;
        this.availableActions = new int[0];
        this.availableRewards = new double[0];
        this.isTerminal = false;
        this.cumulativeReward = 0.0;
        this.success = false;
    }

    // Loading in .env file into a hashmap for instance variables
    private static HashMap<String, String> loadEnv() {
        HashMap<String, String> env = new HashMap<>();
        String workingDir = System.getProperty("user.dir");
        System.out.println("Current Working Directory: " + workingDir);
        try (BufferedReader br = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) 
                    continue;
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

    // Updates state of agent on client side
    public void updateState(int newStateId) {
        int oldState = this.stateId;
        this.stateId = newStateId;
        
        System.out.println("State transition: " + oldState + " -> " + newStateId);
        
        // Goal state check
        if (newStateId == GOAL_STATE) {
            System.out.println("\n!!! GOAL STATE REACHED !!!");
            this.isGoalReached = true;
            completeEpisode("Goal state reached!");
        }
    }

    // Updates for rewards and available action arrays
    public void updateAvailableActions(int[] availableActions) {
        this.availableActions = availableActions;
    }

    public void updateAvailableRewards(double[] availableRewards) {
        this.availableRewards = availableRewards;
    }

    // Update cumulative reward for training
    public void updateReward(double reward) {
        this.cumulativeReward += Math.pow(gamma, this.stepsThisEpisode - 1) * reward;
    }

    // Setting terminal state and success of the episode
    public void setTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
        if (isTerminal) {
            this.success = this.cumulativeReward >= this.successRewardThreshold;
        }
    }

    // Setters for cumulative reward, steps in this episode, total episodes and getter for steps in this episode
    public void setCumulativeReward(double cumulativeReward) {
        this.cumulativeReward = cumulativeReward;
    }

    public void setStepsThisEpisode(int stepsThisEpisode) {
        this.stepsThisEpisode = stepsThisEpisode;
    }

    public int getStepsThisEpisode() {
        return stepsThisEpisode;
    }

    public void setTotalEpisodes(int totalEpisodes) {
        this.totalEpisodes = totalEpisodes;
    }

    // Resets cumulative reward and success boolean for next episode
    public void resetCumulativeReward() {
        double oldReward = this.cumulativeReward;
        this.cumulativeReward = 0.0;
        this.stepsThisEpisode = 0;
        System.out.println("Reset cumulative reward from " + oldReward + " to 0.0");
    }

    // Getters for state ID, available actions for the step, rewards for the step, and cumulative reward for an episode
    public int getStateId() {
        return stateId;
    }

    public int[] getAvailableActions() {
        return availableActions;
    }

    public double[] getAvailableRewards() {
        return availableRewards;
    }

    public double getCumulativeReward() {
        return cumulativeReward;
    }

    // Add onto cumulative reward for an episode
    public void addToCumulativeReward(double reward) {
        if (!Double.isNaN(reward) && !Double.isInfinite(reward)) {
            double discountedReward = Math.pow(gamma, this.stepsThisEpisode - 1) * reward;
            this.cumulativeReward += discountedReward;
            this.episodeReward += discountedReward;
            System.out.println(String.format(
                "Episode %d/%d - Step %d/%d - Reward: %.2f (Discounted Episode Total: %.2f)", 
                currentEpisode + 1, MAX_EPISODES,
                stepsThisEpisode + 1, MAX_STEPS,
                reward, episodeReward
            ));

            // Check for goal state reward
            if (Math.abs(reward - GOAL_REWARD) < 0.0001) {
                System.out.println("\n!!! GOAL REWARD RECEIVED !!!");
                this.isGoalReached = true;
                completeEpisode("Goal reward received!");
            }
        }
    }

    // Check if state is terminal
    public boolean isTerminal() {
        return isTerminal;
    }

    // Check for episode success
    public boolean isSuccess() {
        return success;
    }

    public int getTotalEpisodes() { return totalEpisodes; }
    public void incrementTotalEpisodes() { totalEpisodes++; }

    public int getMaxEpisodes() { return maxEpisodes; }
    public void setMaxEpisodes(int maxEpisodes) { this.maxEpisodes = maxEpisodes; }

    public int getSuccessfulEpisodes() { return successfulEpisodes; }
    public void incrementSuccessfulEpisodes() { successfulEpisodes++; }

    public double getSuccessRewardThreshold() { 
        return successRewardThreshold; 
    }

    public void setSuccessRewardThreshold(double successRewardThreshold) { 
        this.successRewardThreshold = successRewardThreshold; 
    }

    // Keeps the training of the agent going based on the stop method = 0 (max number of steps), 1 (goal state reached), 2 (probabilistic stopping)
    public void incrementStepsThisEpisode() {
        this.stepsThisEpisode++;
        int stopMethod = Integer.parseInt(ENV.getOrDefault("STOP_METHOD", "0"));
        System.out.println("DEBUG: stopMethod=" + stopMethod);
        if (stopMethod == 0 && this.stepsThisEpisode >= MAX_STEPS && !episodeComplete) {
            completeEpisode("Maximum steps (" + MAX_STEPS + ") reached");
        }
    }

    public void resetStepsThisEpisode() {
        this.stepsThisEpisode = 0;
    }

    public int getMaxStepsPerEpisode() {
        return maxStepsPerEpisode;
    }

    public void setMaxStepsPerEpisode(int maxSteps) {
        this.maxStepsPerEpisode = maxSteps;
    }

    // For debugging
    @Override
    public String toString() {
        return "RLGameModel{" +
                "stateId=" + stateId +
                ", availableActions=" + Arrays.toString(availableActions) +
                ", availableRewards=" + Arrays.toString(availableRewards) +
                ", isTerminal=" + isTerminal +
                ", cumulativeReward=" + cumulativeReward +
                ", success=" + success +
                '}';
    }

    // Resetting episode variables for new episode
    public void resetForNewEpisode() {
        if (currentEpisode + 1 >= MAX_EPISODES) {
            return;
        }
        
        this.episodeReward = 0.0;
        this.stepsThisEpisode = 0;
        this.isTerminal = false;
        this.currentEpisode++;
        this.episodeComplete = false;
        this.isGoalReached = false;

        System.out.println("\n=== Starting Episode " + (currentEpisode + 1) + "/" + MAX_EPISODES + " ===");
        System.out.println("Steps: 0/" + MAX_STEPS);
        System.out.println("Discounted Episode Reward: 0.0");
        System.out.println("===========================\n");

        if (gamePlayer != null) {
            gamePlayer.requestInitialState();
        }
    }

    // Episode termination based on the stopping methods
    private void completeEpisode(String reason) {
        int stopMethod = Integer.parseInt(ENV.getOrDefault("STOP_METHOD", "0"));
        // If the termination condition is reaching goal state, then ignore the max steps and exit out of this code
        if (stopMethod == 1 && reason.contains("Maximum steps")) {
            System.out.println("Ignoring max steps end condition for STOP_METHOD=1");
            return;
        }

        System.out.println(EPISODE_SEPARATOR);

        // Episode terminated and summary printed if the episode wasn't terminated
        if (!episodeComplete) {
            this.episodeComplete = true;
            System.out.println("\n=== Episode " + (currentEpisode + 1) + " Information ===");
            System.out.println("Reason: " + reason);
            System.out.println("Goal Reached: " + (isGoalReached ? "Yes!" : "No"));
            System.out.println("================================\n");
            if (gamePlayer != null) {
                gamePlayer.sendFinalStateMessage();
            }

            if (currentEpisode + 1 < MAX_EPISODES) {
                resetForNewEpisode();
            } else if (!trainingComplete) {
                trainingComplete = true;
                System.out.println("\n=== Training Complete ===");
                System.out.println("Total Episodes: " + (currentEpisode + 1));
                System.out.println("======================\n");
                
                if (gamePlayer != null) {
                    gamePlayer.sendTrainingCompleteMessage();
                }
            }
        }

        System.out.println(EPISODE_SEPARATOR);
    }

    public double getEpisodeReward() {
        return episodeReward;
    }

    public double getTotalReward() {
        return totalReward;
    }

    public int getCurrentEpisode() {
        return currentEpisode;
    }

    public boolean isEpisodeComplete() {
        return episodeComplete;
    }

    public boolean isTrainingComplete() {
        int stopMethod = Integer.parseInt(ENV.getOrDefault("STOP_METHOD", "0"));
        // Ignore max steps if stop method is 1, and stop if goal state is reached within the permitted number of episodes
        if (stopMethod == 1) {
            return trainingComplete || (currentEpisode + 1 >= MAX_EPISODES && isGoalReached);
        } 
        // Else if stop methods are 0 or 2, then keep going till the goal state is reached or max number of steps (probabilistic stopping handled on server side)
        else {
            return trainingComplete || (currentEpisode + 1 >= MAX_EPISODES && (stepsThisEpisode >= MAX_STEPS || isGoalReached));
        }
    }

    public boolean isGoalReached() {
        return isGoalReached;
    }

    // Add to total steps in the episode and get the total steps in the episode that have been completed
    public void addTotalSteps(int steps) {
        this.totalSteps += steps;
    }

    public int getTotalSteps() {
        return this.totalSteps;
    }
}