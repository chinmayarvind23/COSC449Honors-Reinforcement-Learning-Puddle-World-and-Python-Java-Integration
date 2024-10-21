package ygraph.ai.smartfox.games.rl;
import java.util.ArrayList;
import java.util.List;

import com.smartfoxserver.v2.entities.data.ISFSObject;

// Subclassed from RLGamePlayer for easier testing - not used, only for testing individual client methods
public class RLGameTest extends RLGamePlayer {

    // Metrics to evaluate the RL agent's performance
    private int totalEpisodes = 0;
    private int successfulEpisodes = 0;
    private List<Double> rewardsPerEpisode = new ArrayList<>();
    private List<Integer> stepsPerEpisode = new ArrayList<>();
    private double cumulativeReward = 0.0;
    private int stepsThisEpisode = 0;
    private int maxEpisodes = 100; // Training limit for test

    // Thresholds for evaluation
    private double successRewardThreshold = 1.0;
    private int stepLimitPerEpisode = 400;

    // Creating a client-side test of the program's functionality with the relevant connection details such as:
    // username, password, IP, port, zone name, and room name
    public RLGameTest(String userName, String passwd, String serverIP, int serverPort, String zoneName, String roomName) {
        super(userName, passwd, serverIP, serverPort, zoneName, roomName);
    }

    // Checking final state to make sure the agent reached the final state for evaluation
    @Override
    protected void processFinalState(ISFSObject params) {
        super.processFinalState(params);
        System.out.println("Evaluation: Agent has reached the terminal state.");

        // Update Evaluation Metrics for each episode
        totalEpisodes++;
        cumulativeReward += gameModel.getCumulativeReward();
        rewardsPerEpisode.add(gameModel.getCumulativeReward());
        stepsPerEpisode.add(stepsThisEpisode);

        // Check if the episode was successful
        if (gameModel.getCumulativeReward() >= successRewardThreshold) {
            successfulEpisodes++;
            System.out.println("Episode " + totalEpisodes + " was successful!");
        } else {
            System.out.println("Episode " + totalEpisodes + " ended unsuccessfully.");
        }

        // Print episode summary to console
        System.out.println("Episode " + totalEpisodes + " Summary:");
        System.out.println(" - Cumulative Reward: " + gameModel.getCumulativeReward());
        System.out.println(" - Steps Taken: " + stepsThisEpisode);

        // Reset Episode-specific Variables
        gameModel.resetCumulativeReward();
        stepsThisEpisode = 0;

        // Print metrics to console
        logMetrics();

        // Calculate average reward over all episodes
        double averageReward = rewardsPerEpisode.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // Check for training termination
        if (totalEpisodes >= maxEpisodes || averageReward >= successRewardThreshold) {
            System.out.println("Training terminated based on defined conditions.");
            disconnect();
        }
    }

    // Overrides sendAction from GamePlayer and allows for tracking steps in the current episode
    @Override
    protected void sendAction(int action, int stateId) {
        super.sendAction(action, stateId);
        stepsThisEpisode++;

        // Check if step limit was exceeded
        if (stepsThisEpisode >= stepLimitPerEpisode) {
            System.out.println("Step limit exceeded. Terminating episode.");
            resetEnvironment();
        }
    }

    // Logs success rate, average reward and average steps, with an overall summary to console
    private void logMetrics() {
        // Calculate Success Rate
        double successRate = totalEpisodes > 0 ? ((double) successfulEpisodes / totalEpisodes) * 100 : 0;

        // Calculate Average Reward over all episodes
        double averageReward = rewardsPerEpisode.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // Calculate Average Steps
        double averageSteps = stepsPerEpisode.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        // Print Overall Evaluation Summary
        System.out.println("=== Overall Evaluation Summary ===");
        System.out.println("Total Episodes: " + totalEpisodes);
        System.out.println("Successful Episodes: " + successfulEpisodes);
        System.out.printf("Success Rate: %.2f%%\n", successRate);
        System.out.printf("Average Reward per Episode: %.2f\n", averageReward);
        System.out.printf("Total Cumulative Reward: %.2f\n", cumulativeReward);
        System.out.printf("Average Steps per Episode: %.2f\n", averageSteps);
        System.out.println("==================================");
    }

    // Main method to initialize the test
    // Type in the following arguments into command line: <username> <password> <serverIP> <serverPort> <zoneName> <roomName>
    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Usage: java RLGameTest <username> <password> <serverIP> <serverPort> <zoneName> <roomName>");
            return;
        }

        String username = args[0];
        String password = args[1];
        String serverIP = args[2];
        int serverPort = Integer.parseInt(args[3]);
        String zoneName = args[4];
        String roomName = args[5];

        RLGameTest testAgent = new RLGameTest(username, password, serverIP, serverPort, zoneName, roomName);

        // While loop to keep the agent listening for events
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                testAgent.disconnect();
                break;
            }
        }
    }
}
