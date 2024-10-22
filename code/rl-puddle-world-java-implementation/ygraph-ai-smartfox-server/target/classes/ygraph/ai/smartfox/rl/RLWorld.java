package ygraph.ai.smartfox.rl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.smartfoxserver.v2.entities.User;

// This class defines the internal representation of the Puddle world in the server
// It contains the grid, puddles, state transitions, and reward mechanisms handling capabilities
// The grid itself is represented as a flattened 1D array instead of a 2D array for ease of understanding
public class RLWorld {
    private Map<Integer, double[]> qTable;
    private Map<Integer, Double> vTable;

    // Learning parameters: learning rate, discount factor, and exploration rate
    private double alpha = 0.1;
    private double gamma = 0.9;
    private double epsilon = 1;

    private final int gridSize = 20;
    private final int maxPuddles = 4;
    private final int puddleSize = 2;

    // List of puddle top-left positions to stretch by 2x2 squares for the puddles
    private final List<int[]> puddlePositions;

    private final int goalStateId = gridSize * gridSize - 1; //399 is the terminal state ID

    private int currentStateId;

    private final Random random;

    // Rewards for each state
    private final double defaultReward = 0.01;
    private final double puddleReward = -1.0;
    private final double goalReward = 1.0;

    // Possible actions are up, down, left, and right
    private final String[] actions = {"UP", "DOWN", "LEFT", "RIGHT"};

    // Constructor for RLWorld that initializes the puddle locations, Q and V tables, and resets the environment
    public RLWorld() {
        random = new Random();
        this.currentStateId = 0;
        puddlePositions = new ArrayList<>();
        qTable = new HashMap<>();
        vTable = new HashMap<>();
        initializeQTable();
        initializeVTable();
        initializePuddles();
        reset();
    }

    // Constructor for RLWorld that sets the user, and the learning parameters from defined values, as well as initializing the puddle locations, Q and V tables, and resetting the environment
    public RLWorld(User user, double alpha, double gamma, double epsilon) {
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.random = new Random();
        this.qTable = new HashMap<>();
        this.vTable = new HashMap<>();
        this.puddlePositions = new ArrayList<>();
        initializeQTable();
        initializeVTable();
        initializePuddles();
        reset();
    }

    // Initializes the master Q-table with random values for each state-action pair
    private void initializeQTable() {
        for (int i = 0; i < gridSize * gridSize; i++) {
            double[] actionValues = new double[actions.length];
            for (int j = 0; j < actions.length; j++) {
                actionValues[j] = random.nextDouble();
            }
            qTable.put(i, actionValues);
        }
    }

    // Initialize the master V-table with default values of 0
    private void initializeVTable() {
        for (int i = 0; i < gridSize * gridSize; i++) {
            vTable.put(i, 0.0);
        }
    }

    // Initializes the puddle positions randomly within the grid
    public void initializePuddles() {
        puddlePositions.clear();
        while (puddlePositions.size() < maxPuddles) {
            int row = random.nextInt(gridSize - puddleSize + 1);
            int col = random.nextInt(gridSize - puddleSize + 1);
            int[] puddle = {row, col};
            if (!isOverlapping(puddle)) {
                if (!(row <= (goalStateId / gridSize) && (goalStateId / gridSize) < row + puddleSize &&
                  col <= (goalStateId % gridSize) && (goalStateId % gridSize) < col + puddleSize)) 
                {
                    puddlePositions.add(puddle);
                }
            }
        }
    }

    // Checks if a puddle overlaps with existing puddles or the terminal state and returns a boolean if so
    private boolean isOverlapping(int[] newPuddle) {
        for (int[] existingPuddle : puddlePositions) {
            if (newPuddle[0] < existingPuddle[0] + puddleSize &&
                newPuddle[0] + puddleSize > existingPuddle[0] &&
                newPuddle[1] < existingPuddle[1] + puddleSize &&
                newPuddle[1] + puddleSize > existingPuddle[1]) {
                return true;
            }
        }
        // Goal state cover check
        int goalRow = goalStateId / gridSize;
        int goalCol = goalStateId % gridSize;
        for (int[] puddle : puddlePositions) {
            if (goalRow >= puddle[0] && goalRow < puddle[0] + puddleSize &&
                goalCol >= puddle[1] && goalCol < puddle[1] + puddleSize) {
                return true;
            }
        }
        return false;
    }

    // Resets the RLWorld to the initial state with current state starting with state ID = 0 and initialize the puddles
    public void reset() {
        currentStateId = 0;
        initializePuddles();
        System.out.println("World reset. Current state set to 0.");
    }

    // Sets Q-value for a given state-action pair for managing client updates to Q-table
    public void setQValue(int stateId, int action, double qValue) {
        double[] currentQ = qTable.get(stateId);
        if (currentQ != null && action >= 0 && action < currentQ.length) {
            currentQ[action] = qValue;
            qTable.put(stateId, currentQ);
        }
    }

    // Sets V-value for a given state for managing client updates to V-table
    public void setVValue(int stateId, double vValue) {
        vTable.put(stateId, vValue);
    }


    // Completes an action selection using epsilon-greedy policy and updates the current state ID to the next state ID by calling the helper method moveAgentWithAction that takes the current state and action
    public int moveAgent(int stateId) {
        int action;
        if (random.nextDouble() < epsilon) {
            // random action chosen -> exploration
            action = getRandomAction();
        } else {
            // max Q-value action chosen -> exploitation (using the Q-table)
            action = getBestAction(stateId);
        }
        return moveAgentWithAction(stateId, action);
    }

    // Gets a random action index to select a random action
    private int getRandomAction() {
        return random.nextInt(actions.length);
    }

    // Finds the best action's index based on maximum Q-value and returns that action
    private int getBestAction(int stateId) {
        double[] qValues = qTable.get(stateId);
        double maxQValue = Double.NEGATIVE_INFINITY;
        int bestAction = 0;

        for (int i = 0; i < qValues.length; i++) {
            if (qValues[i] > maxQValue) {
                maxQValue = qValues[i];
                bestAction = i;
            }
        }

        return bestAction;
    }

    // Performs an action and updates the current state to the next state
    public int moveAgentWithAction(int stateId, int action) {
        String actionStr = getActionString(action);
        if (actionStr == null) {
            return currentStateId;
        }
    
        int newStateId = simulateAction(stateId, actionStr);
    
        // Check if the new state is a puddle
        if (isPuddle(newStateId)) {
            // Apply penalty but don't move
            double puddleReward = getReward(stateId, actionStr, newStateId);  // -1.0 reward
            updateQTable(stateId, action, puddleReward, stateId);  // Agent stays in the same state
            return stateId;  // Agent remains in the current state
        } else {
            currentStateId = newStateId;
            return currentStateId;
        }
    }    

    // Perform a move using the current state ID and action string within the 1D array representing the puddle world
    // Returns the new state ID post action
    public int simulateAction(int stateId, String action) {
        int row = stateId / gridSize;
        int col = stateId % gridSize;

        switch (action) {
            case "UP":
                row = Math.max(row - 1, 0);
                break;
            case "DOWN":
                row = Math.min(row + 1, gridSize - 1);
                break;
            case "LEFT":
                col = Math.max(col - 1, 0);
                break;
            case "RIGHT":
                col = Math.min(col + 1, gridSize - 1);
                break;
            default:
                // No movement
                break;
        }

        return row * gridSize + col;
    }

    // Gets the reward from moving from a certain state ID to the next state ID via some action
    public double getReward(int stateId, String action, int nextStateId) {
        if (nextStateId == goalStateId) {
            return goalReward;
        }

        if (isPuddle(nextStateId)) {
            return puddleReward;
        }

        return defaultReward;
    }

    // Checks if the given state is within a puddle
    public boolean isPuddle(int stateId) {
        int row = stateId / gridSize; // each row has gridSize elements, so index/gridSize = row
        int col = stateId % gridSize; // each row has gridSize elements, so the remainder gives position in the row (which is the column)

        for (int[] puddle : puddlePositions) {
            if (row >= puddle[0] && row < puddle[0] + puddleSize &&
                col >= puddle[1] && col < puddle[1] + puddleSize) {
                return true;
            }
        }
        return false;
    }

    // Gets the list of possible actions from a current state
    public String[] getAvailableActions(int stateId) {
        return actions;
    }

    // Gets the index of the action in the actions array from the action string
    public int getActionIndex(String action) {
        for (int i = 0; i < actions.length; i++) {
            if (actions[i].equals(action)) {
                return i;
            }
        }
        return -1;
    }

    // Gets the list of state IDs in the state space of the puddle world
    public List<Integer> getStateSpace() {
        List<Integer> states = new ArrayList<>();
        for (int i = 0; i < gridSize * gridSize; i++) {
            states.add(i);
        }
        return states;
    }

    // Gets the list of available actions in the actions array
    // public String[] getActionSpace() {
    //     return actions;
    // }

    // Checks if a given state is a terminal state
    public boolean isTerminalState(int stateId) {
        return stateId == goalStateId;
    }

    // Converts an action index to its corresponding action string
    private String getActionString(int actionIndex) {
        if (actionIndex >= 0 && actionIndex < actions.length) {
            return actions[actionIndex];
        }
        return null;
    }

    // Helper method to get the maximum Q-value for a given state's action values
    private double getMaxQ(double[] qValues) {
        double max = Double.NEGATIVE_INFINITY;
        for (double q : qValues) {
            if (q > max) {
                max = q;
            }
        }
        return max;
    }

    // Gets the current state ID of the agent
    public int getCurrentStateId() {
        return currentStateId;
    }

    // Resets the world to its initial state, clears puddle positions
    public void cleanup() {
        reset();
        puddlePositions.clear();
        System.out.println("RL World has been cleaned up.");
    }

    // Updates the Q-Table with the current state, action, reward, and next state as params
    public void updateQTable(int stateId, int action, double reward, int nextStateId) {
        double[] currentQ = qTable.get(stateId);
        double[] nextQ = qTable.get(nextStateId);
        double maxNextStateQ = getMaxQ(nextQ);
        
        // Update Q-value for current state-action pair
        currentQ[action] = currentQ[action] + alpha * (reward + gamma * maxNextStateQ - currentQ[action]);
        qTable.put(stateId, currentQ);
    }

    // Check for puddle states overlapping with goal state
    private boolean isOverlappingGoal(int row, int col) {
        int goalRow = goalStateId / gridSize;
        int goalCol = goalStateId % gridSize;
        
        return (row <= goalRow && goalRow < row + puddleSize) &&
            (col <= goalCol && goalCol < col + puddleSize);
    }

    // Check if a puddle overlaps with any existing puddle
    private boolean isOverlappingExistingPuddle(int row, int col) {
        for (int[] existingPuddle : puddlePositions) {
            int existingRow = existingPuddle[0];
            int existingCol = existingPuddle[1];
            boolean rowOverlap = row < existingRow + puddleSize && existingRow < row + puddleSize;
            boolean colOverlap = col < existingCol + puddleSize && existingCol < col + puddleSize;
            if (rowOverlap && colOverlap) {
                return true;
            }
        }
        return false;
    }

    // Setting puddle positions for testing
    public void setPuddlePositions(List<int[]> puddles) {
        if (puddles == null) {
            throw new IllegalArgumentException("Puddle positions list cannot be null.");
        }
        
        this.puddlePositions.clear();
        
        // Adding new puddle positions
        for (int[] puddle : puddles) {
            if (puddle == null || puddle.length != 2) {
                throw new IllegalArgumentException("Each puddle position must be an array of two integers [row, col].");
            }
            
            int row = puddle[0];
            int col = puddle[1];
            
            // Boundary checks for rows and columns
            if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) {
                throw new IllegalArgumentException("Puddle position out of grid bounds: (" + row + ", " + col + ")");
            }
            
            // Goal state overlap check for puddles inserted
            if (isOverlappingGoal(row, col)) {
                throw new IllegalArgumentException("Puddle position overlaps with the goal state at (" 
                    + (goalStateId / gridSize) + ", " + (goalStateId % gridSize) + ")");
            }
            
            // Existing puddle state overlap check for puddles inserted
            if (isOverlappingExistingPuddle(row, col)) {
                throw new IllegalArgumentException("Puddle position overlaps with an existing puddle at (" 
                    + row + ", " + col + ")");
            }
            
            // Adding the validated puddle position
            this.puddlePositions.add(new int[]{row, col});
        }
    }        
}