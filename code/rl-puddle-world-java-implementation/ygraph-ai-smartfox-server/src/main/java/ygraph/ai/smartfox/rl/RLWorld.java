package ygraph.ai.smartfox.rl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.smartfoxserver.v2.entities.User;

// This class defines the internal representation of the Puddle world on the server side
// It contains the grid, puddles, state transitions, and reward mechanisms handling capabilities
// The puddle world's grid itself is represented as a flattened 1D array, equations to use when computing coordinates in the 1D array:
// stateId = row * gridSize + col
// row = stateId / gridSize
// col = stateId % gridSize
// Puddle positions stored as a List of (row, col) coordinates of the top left of the squares that become the puddles
// Some of this code needs to be refactored to maintain only the logic for moving the RL agent around based on the client's sent actions and respond with valid actions, etc. and store a copy of the Q and V tables (not decide actions by itself), it will be done soon
public class RLWorld {
    private final Map<Integer, double[]> qTable = new ConcurrentHashMap<>();
    private final Map<Integer, Double> vTable = new ConcurrentHashMap<>();
    private static final HashMap<String, String> ENV = loadEnv();

    // Learning parameter - exploration rate
    private double epsilon;

    private final int gridSize = Integer.parseInt(ENV.getOrDefault("GRID_SIZE", "5"));
    private final int maxPuddles;
    private final int puddleSize;

    // List of puddle top-left positions to stretch downward and rightward by puddleSize x puddleSize squares for the puddles
    private final List<int[]> puddlePositions;

    private final int goalStateId = gridSize * gridSize - 1;

    private int currentStateId;

    private final Random random;

    // Rewards for each type of transitions from state to state
    private final double defaultReward;
    private final double puddleReward;
    private final double goalReward;

    // Possible actions are up, down, left, and right
    private final String[] actions = {"UP", "DOWN", "LEFT", "RIGHT"};

    private double lastReward;
    private boolean isTerminal;

    // Constructors
    public RLWorld() {
        random = new Random();
        this.maxPuddles = Integer.parseInt(ENV.getOrDefault("MAX_PUDDLES", "2"));
        this.puddleSize = Integer.parseInt(ENV.getOrDefault("PUDDLE_SIZE", "2"));
        this.epsilon = Double.parseDouble(ENV.getOrDefault("EPSILON", "1"));
        this.defaultReward = Double.parseDouble(ENV.getOrDefault("DEFAULT_REWARD", "-0.01"));
        this.puddleReward = Double.parseDouble(ENV.getOrDefault("PUDDLE_REWARD", "-1.0"));
        this.goalReward = Double.parseDouble(ENV.getOrDefault("GOAL_REWARD", "10.0"));
        puddlePositions = new ArrayList<>();
        initializeQTable();
        initializeVTable();
        initializePuddles();
        reset();
        System.out.println("RLWorld initialized with defaultReward: " + this.defaultReward);
    }

    public RLWorld(User user, double alpha, double gamma, double epsilon) {
        this.epsilon = epsilon;
        this.maxPuddles = Integer.parseInt(ENV.getOrDefault("MAX_PUDDLES", "2"));
        this.puddleSize = Integer.parseInt(ENV.getOrDefault("PUDDLE_SIZE", "2"));
        this.epsilon = Double.parseDouble(ENV.getOrDefault("EPSILON", "1"));
        this.defaultReward = Double.parseDouble(ENV.getOrDefault("DEFAULT_REWARD", "-0.01"));
        this.puddleReward = Double.parseDouble(ENV.getOrDefault("PUDDLE_REWARD", "-1.0"));
        this.goalReward = Double.parseDouble(ENV.getOrDefault("GOAL_REWARD", "10.0"));
        this.random = new Random();
        this.puddlePositions = new ArrayList<>();
        initializeQTable();
        initializeVTable();
        initializePuddles();
        reset();
        System.out.println("RLWorld initialized with actions: " + String.join(", ", actions));
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

    // Initializes the master Q-table with random values for each state-action pair
    private void initializeQTable() {
        for (int state = 0; state < gridSize * gridSize; state++) {
            double[] actions = new double[4];
            for (int a = 0; a < 4; a++) {
                actions[a] = Math.random();
            }
            qTable.put(state, actions);
        }
    }

    // Initialize the master V-table with default values of 0
    private void initializeVTable() {
        for (int state = 0; state < gridSize * gridSize; state++) {
            vTable.put(state, 0.0);
        }
    }

    // Initializes the puddle positions randomly within the grid
    public void initializePuddles() {
        puddlePositions.clear();
        while (puddlePositions.size() < maxPuddles) {
            int row = random.nextInt(gridSize - puddleSize + 1);
            int col = random.nextInt(gridSize - puddleSize + 1);
            int[] puddle = {row, col};
            if (row == 0 && col == 0) {
                continue;
            }
            if (!isOverlapping(puddle)) {
                if (!(row <= (goalStateId / gridSize) && (goalStateId / gridSize) < row + puddleSize &&
                  col <= (goalStateId % gridSize) && (goalStateId % gridSize) < col + puddleSize)) 
                {
                    puddlePositions.add(puddle);
                    System.out.println("Puddle added at row: " + row + ", col: " + col);
                }
            }
        }
        System.out.println("Total Puddles Initialized: " + puddlePositions.size());
    }    

    // Checks if a puddle overlaps with existing puddles or the terminal state and returns true if so
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
        initializePuddles();
        System.out.println("World reset. Current state set to 0.");
        for (int[] puddle : puddlePositions) {
            System.out.println("Puddle at row: " + puddle[0] + ", col: " + puddle[1]);
        }
        this.currentStateId = 0;
    }

    // Sets Q-value for a given state-action pair for managing client updates to Q-table
    public void setQValue(int stateId, int action, double qValue) {
        double[] currentQ = qTable.get(stateId);
        if (currentQ != null) {
            if (action >= 0 && action < currentQ.length) {
                currentQ[action] = qValue;
                qTable.put(stateId, currentQ);
                System.out.println("Q-Table Updated: StateID=" + stateId + ", Action=" + action + ", QValue=" + qValue);
            } else {
                System.err.println("Invalid action index: " + action + " for StateID: " + stateId);
            }
        } else {
            System.err.println("Invalid stateId: " + stateId + " when setting Q-value.");
        }
    }    

    // Sets V-value for a given state for managing client updates to V-table
    public void setVValue(int stateId, double vValue) {
        if (vTable.containsKey(stateId)) {
            vTable.put(stateId, vValue);
            System.out.println("V-Table Updated: StateID=" + stateId + ", VValue=" + vValue);
        } else {
            System.err.println("Invalid stateId: " + stateId + " when setting V-value.");
        }
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

    // Gets the Q-value for a specific state-action pair
    public double getMasterQValue(int stateId, int actionIndex) {
        double[] qValues = qTable.get(stateId);
        if (qValues != null && actionIndex >= 0 && actionIndex < qValues.length) {
            return qValues[actionIndex];
        }
        System.err.println("Invalid stateId or actionIndex in getMasterQValue: stateId=" + stateId + ", actionIndex=" + actionIndex);
        return 0.0;
    }

    // Gets the V-value for a specific state
    public double getMasterVValue(int stateId) {
        return vTable.getOrDefault(stateId, 0.0);
    }

    // Move the RL agent, update its reward and state
    public int moveAgentWithAction(int stateId, int action) {
        String actionStr = getActionString(action);
        if (actionStr == null) {
            System.err.println("Invalid action index: " + action);
            setLastReward(-0.1);
            setTerminal(true);
            return stateId;
        }
    
        // Get current position
        int row = stateId / gridSize;
        int col = stateId % gridSize;
    
        // Get the new position based on action string
        switch (actionStr) {
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
        }

        int newStateId = row * gridSize + col;
        double reward = defaultReward;
        if (newStateId == goalStateId) {
            reward = goalReward;
        } else if (isPuddle(newStateId)) {
            reward = puddleReward;
        }

        System.out.println("moveAgentWithAction: stateId=" + stateId + ", action=" + action + " (" + actionStr + ")");
        System.out.println("Action '" + actionStr + "' from (" + row + ", " + col + ") leads to newStateId=" + newStateId + " with reward=" + reward);

        setLastReward(reward);
        if (isTerminalState(newStateId)) {
            setTerminal(true);
        } else {
            setTerminal(false);
        }
    
        System.out.println("Moved to state ID: " + newStateId + " with Reward: " + reward);
        this.currentStateId = newStateId;
        return newStateId;
    }

    // Performs a move using the current state ID and action string within the 1D array representing the puddle world
    // Returns the new state ID post action
    public int simulateAction(int stateId, String action) {
        int row = stateId / gridSize;
        int col = stateId % gridSize;
        System.out.println("Simulating action '" + action + "' from state (" + row + ", " + col + ")");
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
                System.out.println("Invalid action string: " + action);
                break;
        }

        int newStateId = row * gridSize + col;
        System.out.println("Action '" + action + "' results in new state ID: " + newStateId);
        return newStateId;
    }

    // Gets the reward from moving from a certain state ID to the next state ID via some action
    public double getReward(int stateId, String action, int nextStateId) {
        if (nextStateId == goalStateId) {
            System.out.println("Reached goal state: " + goalStateId + ". Assigning reward: " + goalReward);
            return goalReward;
        }
    
        if (isPuddle(nextStateId)) {
            System.out.println("Entered puddle state: " + nextStateId + ". Assigning reward: " + puddleReward);
            return puddleReward;
        }
    
        System.out.println("Normal state transition. Assigning reward: " + defaultReward);
        return defaultReward;
    }    

    // Checks if the current state is a puddle
    public boolean isPuddle(int stateId) {
        int row = stateId / gridSize; // each row has gridSize elements, so index/gridSize = row
        int col = stateId % gridSize; // each row has gridSize elements, so the remainder gives position in the row (which is the column)
        boolean inPuddle = false;
        for (int[] puddle : puddlePositions) {
            if (row >= puddle[0] && row < puddle[0] + puddleSize &&
                col >= puddle[1] && col < puddle[1] + puddleSize) {
                inPuddle = true;
                System.out.println("State " + stateId + " is a puddle at row: " + row + ", col: " + col);
                break;
            }
        }
    
        if (!inPuddle) {
            System.out.println("State " + stateId + " is not a puddle.");
        }
    
        return inPuddle;
    }

    // Gets the list of possible actions from the current state
    public String[] getAvailableActions(int stateId) {
        if (actions == null) {
            System.err.println("Error: Actions array is null for stateId: " + stateId);
            return new String[0];
        }

        List<String> available = new ArrayList<>(Arrays.asList(actions));
        int row = stateId / gridSize;
        int col = stateId % gridSize;
        
        if (row == 0) {
            available.remove("UP");
        }
        if (row == gridSize - 1) {
            available.remove("DOWN");
        }
        if (col == 0) {
            available.remove("LEFT");
        }
        if (col == gridSize - 1) {
            available.remove("RIGHT");
        }
        
        System.out.println("Available Actions for state " + stateId + ": " + String.join(", ", available));
        return available.toArray(new String[0]);
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

    // Checks if a given state is a terminal state
    public boolean isTerminalState(int stateId) {
        int terminalStateId = (gridSize * gridSize) - 1;
        return stateId == terminalStateId;
    }

    // Converts an action index to its corresponding action string
    private String getActionString(int actionIndex) {
        if (actionIndex >= 0 && actionIndex < actions.length) {
            return actions[actionIndex];
        }
        return null;
    }

    // Gets the current state ID of the agent
    public int getCurrentStateId() {
        return currentStateId;
    }

    // Resets the world to its initial state, and clears puddle positions
    public void cleanup() {
        reset();
        puddlePositions.clear();
        System.out.println("RL World has been cleaned up.");
    }

    // Check for puddle states overlapping with the goal state
    private boolean isOverlappingGoal(int row, int col) {
        int goalRow = goalStateId / gridSize;
        int goalCol = goalStateId % gridSize;
        
        return (row <= goalRow && goalRow < row + puddleSize) &&
            (col <= goalCol && goalCol < col + puddleSize);
    }

    // Check for if a puddle overlaps with an existing puddle
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
            
            // Adding puddle position
            this.puddlePositions.add(new int[]{row, col});
        }
    }
    
    public double getLastReward() {
        return lastReward;
    }

    public void setLastReward(double reward) {
        this.lastReward = reward;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public void setTerminal(boolean terminal) {
        this.isTerminal = terminal;
    }
}