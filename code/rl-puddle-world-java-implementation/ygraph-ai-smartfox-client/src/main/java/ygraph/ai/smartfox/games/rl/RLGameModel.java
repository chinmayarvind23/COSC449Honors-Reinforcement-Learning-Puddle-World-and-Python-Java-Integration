package ygraph.ai.smartfox.games.rl;

import java.util.Arrays;

// This class holds the game's state specific to RL puddle world parameters like: state, available actions, rewards and the final state check.
public class RLGameModel {
    private int stateId;
    private int[] availableActions;
    private double[] availableRewards;
    private boolean isTerminal;
    
    private double cumulativeReward;
    private int stepsThisEpisode;
    private int maxStepsPerEpisode = 50;
    private boolean success;
    private int totalEpisodes = 0;
    private int maxEpisodes = 10;
    private int successfulEpisodes = 0;
    public void setSuccessfulEpisodes(int successfulEpisodes) {
        this.successfulEpisodes = successfulEpisodes;
    }

    // Reward threshold to determine the agent succeeded in navigating the world efficiently
    private double successRewardThreshold = 1.0;
    
    private double episodeReward = 0.0;
    private double totalReward = 0.0;
    private int currentEpisode = 0;
    private boolean episodeComplete = false;
    private boolean trainingComplete = false;
    private static final int MAX_EPISODES = 10;
    private static final int MAX_STEPS = 50;
    private boolean isGoalReached = false;
    private static final int GOAL_STATE = 24;
    private static final double GOAL_REWARD = 10.0;

    private RLGamePlayer gamePlayer;

    public void setGamePlayer(RLGamePlayer player) {
        this.gamePlayer = player;
    }

    // Initialize game model with state, available actions, rewards, and final state check
    public RLGameModel() {
        this.stateId = 0;
        this.availableActions = new int[0];
        this.availableRewards = new double[0];
        this.isTerminal = false;
        this.cumulativeReward = 0.0;
        this.success = false;
    }

    // Update methods for all the attributes
    public void updateState(int newStateId) {
        int oldState = this.stateId;
        this.stateId = newStateId;
        
        System.out.println("State transition: " + oldState + " -> " + newStateId);
        
        // Check if goal state reached
        if (newStateId == GOAL_STATE) {
            System.out.println("\n!!! GOAL STATE REACHED !!!");
            this.isGoalReached = true;
            this.successfulEpisodes++;
            completeEpisode("Goal state reached!");
        }
    }

    public void updateAvailableActions(int[] availableActions) {
        this.availableActions = availableActions;
    }

    public void updateAvailableRewards(double[] availableRewards) {
        this.availableRewards = availableRewards;
    }

    public void updateReward(double reward) {
        this.cumulativeReward += reward;
    }

    // Setting terminal state and success of the episode
    public void setTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
        if (isTerminal) {
            this.success = this.cumulativeReward >= this.successRewardThreshold;
        }
    }

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

    // Getters for all the attributes
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

    public void addToCumulativeReward(double reward) {
        if (!Double.isNaN(reward) && !Double.isInfinite(reward)) {
            this.episodeReward += reward;
            
            System.out.println(String.format(
                "Episode %d/%d - Step %d/%d - Reward: %.2f (Episode Total: %.2f)", 
                currentEpisode + 1, MAX_EPISODES,
                stepsThisEpisode + 1, MAX_STEPS,
                reward, episodeReward
            ));

            // Check for goal reward
            if (Math.abs(reward - GOAL_REWARD) < 0.0001) {
                System.out.println("\n!!! GOAL REWARD RECEIVED !!!");
                this.isGoalReached = true;
                this.successfulEpisodes++;
                completeEpisode("Goal reward received!");
            }
        }
    }

    // Check for terminal state
    public boolean isTerminal() {
        return isTerminal;
    }

    // Determines success/failure of episode
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

    public void incrementStepsThisEpisode() {
        this.stepsThisEpisode++;
        if (this.stepsThisEpisode >= MAX_STEPS && !episodeComplete) {
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

    // Debugging purposes only
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

    public void resetForNewEpisode() {
        if (currentEpisode + 1 >= MAX_EPISODES) {
            return;
        }
        
        this.stateId = 0;
        this.episodeReward = 0.0;
        this.stepsThisEpisode = 0;
        this.isTerminal = false;
        this.currentEpisode++;
        this.episodeComplete = false;
        this.isGoalReached = false;

        System.out.println("\n=== Starting Episode " + (currentEpisode + 1) + "/" + MAX_EPISODES + " ===");
        System.out.println("Initial State: " + stateId);
        System.out.println("Steps: 0/" + MAX_STEPS);
        System.out.println("Episode Reward: 0.0");
        System.out.println("===========================\n");

        if (gamePlayer != null) {
            gamePlayer.requestInitialState();
        }
    }

    public void resetEpisodeStats() {
        this.totalEpisodes = 0;
        this.successfulEpisodes = 0;
        this.cumulativeReward = 0.0;
        this.stepsThisEpisode = 0;
        System.out.println("Reset all episode statistics");
    }

    private void completeEpisode(String reason) {
        if (!episodeComplete) {
            this.episodeComplete = true;
            
            System.out.println("\n=== Episode " + (currentEpisode + 1) + "/" + MAX_EPISODES + " Complete ===");
            System.out.println("Reason: " + reason);
            System.out.println("Steps Taken: " + stepsThisEpisode + "/" + MAX_STEPS);
            System.out.println("Episode Reward: " + String.format("%.2f", episodeReward));
            System.out.println("Successful Episodes: " + successfulEpisodes + "/" + (currentEpisode + 1));
            System.out.println("Goal Reached: " + (isGoalReached ? "Yes!" : "No"));
            System.out.println("================================\n");

            if (currentEpisode + 1 < MAX_EPISODES) {
                resetForNewEpisode();
            } else if (!trainingComplete) {
                trainingComplete = true;
                System.out.println("\n=== Training Complete ===");
                System.out.println("Total Episodes: " + (currentEpisode + 1));
                System.out.println("Successful Episodes: " + successfulEpisodes);
                System.out.println("Final Episode Reward: " + String.format("%.2f", episodeReward));
                System.out.println("======================\n");
                
                if (gamePlayer != null) {
                    gamePlayer.sendTrainingCompleteMessage();
                }
            }
        }
    }

    public void handleTerminalState(double finalReward, boolean success) {
        if (!episodeComplete) {
            this.addToCumulativeReward(finalReward);
            if (success) {
                this.successfulEpisodes++;
            }
            completeEpisode(success ? "Goal reached" : "Terminal state reached");
        }
    }

    // Update getters and setters
    public double getEpisodeReward() {
        return episodeReward;
    }

    public double getTotalReward() {
        return totalReward;
    }

    public int getCurrentEpisode() {
        return currentEpisode;
    }

    public void resetAllStats() {
        this.currentEpisode = 0;
        this.successfulEpisodes = 0;
        this.totalReward = 0.0;
        this.episodeReward = 0.0;
        this.stepsThisEpisode = 0;
        this.episodeComplete = false;
        System.out.println("Reset all statistics");
        resetForNewEpisode();
    }

    public boolean isEpisodeComplete() {
        return episodeComplete;
    }

    public boolean isTrainingComplete() {
        return trainingComplete || (currentEpisode + 1 >= MAX_EPISODES && 
               (stepsThisEpisode >= MAX_STEPS || isGoalReached));
    }

    public void handleStateError() {
        System.out.println("State mismatch detected - Requesting resync...");
        if (gamePlayer != null) {
            gamePlayer.requestInitialState();
        }
    }

    public boolean isGoalReached() {
        return isGoalReached;
    }
}