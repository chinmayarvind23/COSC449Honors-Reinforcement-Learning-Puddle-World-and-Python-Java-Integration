package ygraph.ai.smartfox.games.rl;

import java.util.Arrays;

// This class holds the game's state specific to RL puddle world parameters like: state, available actions, rewards and the final state check.
public class RLGameModel {
    private int stateId;
    private int[] availableActions;
    private double[] availableRewards;
    private boolean isTerminal;
    

    // For testing purposes
    private double cumulativeReward;
    private int stepsThisEpisode;
    private boolean success;

    // Reward threshold to determine the agent succeeded in navigating the world efficiently
    private double successRewardThreshold = 60.0;

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
    public void updateState(int stateId) {
        this.stateId = stateId;
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


    // Resets cumulative reward and success boolean for next episode
    public void resetCumulativeReward() {
        this.cumulativeReward = 0.0;
        this.success = false;
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

    // Check for terminal state
    public boolean isTerminal() {
        return isTerminal;
    }

    // Determines success/failure of episode
    public boolean isSuccess() {
        return success;
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
}