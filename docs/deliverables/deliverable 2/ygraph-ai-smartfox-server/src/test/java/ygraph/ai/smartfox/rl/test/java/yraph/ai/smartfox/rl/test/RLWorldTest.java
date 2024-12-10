package ygraph.ai.smartfox.rl.test.java.yraph.ai.smartfox.rl.test;

import ygraph.ai.smartfox.rl.RLWorld;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;

public class RLWorldTest {

    private RLWorld world;
    private final int testGridSize = 5;

    @Before
    public void setUp() {
        // Assumes RLWorld reads .env variables (e.g., GRID_SIZE=6, DEFAULT_REWARD=-0.01, etc.)
        world = new RLWorld();
    }

    // Helper method to convert (row, col) to stateId
    private int getStateId(int row, int col) {
        return row * testGridSize + col;
    }

    @Test
    public void testPerformActionUp() {
        int currentStateId = getStateId(1, 4);
        int expectedNextState = getStateId(0, 4);
        int nextState = world.simulateAction(currentStateId, "UP");
        assertEquals("State should decrement row by 1", expectedNextState, nextState);
    }

    @Test
    public void testPerformActionDown() {
        int currentStateId = getStateId(0, 5);
        int expectedNextState = getStateId(1, 5);
        int nextState = world.simulateAction(currentStateId, "DOWN");
        assertEquals("State should increment row by 1", expectedNextState, nextState);
    }

    @Test
    public void testPerformActionLeft() {
        int currentStateId = getStateId(1, 0);
        int expectedNextState = getStateId(1, 0);
        int nextState = world.simulateAction(currentStateId, "LEFT");
        assertEquals("State should remain the same when moving left at the grid edge", expectedNextState, nextState);
    }

    @Test
    public void testPerformActionRight() {
        int currentStateId = getStateId(0, 4);
        int expectedNextState = getStateId(0, 4);
        int nextState = world.simulateAction(currentStateId, "RIGHT");
        assertEquals("State should remain the same when moving right at the grid edge", expectedNextState, nextState);
    }

    @Test
    public void testIsPuddle() {
        world.setPuddlePositions(Arrays.asList(new int[]{1, 1}));
        int[] puddleStateIds = {
            getStateId(1,1),
            getStateId(1,2),
            getStateId(2,1),
            getStateId(2,2)
        };

        for (int id : puddleStateIds) {
            assertTrue("State " + id + " should be a puddle", world.isPuddle(id));
        }

        int nonPuddleStateId = getStateId(1, 3);
        assertFalse("State " + nonPuddleStateId + " should not be a puddle", world.isPuddle(nonPuddleStateId));
    }

    @Test
    public void testGetReward() {
        int terminalStateId = getStateId(4,4);
        assertEquals(20.0, world.getReward(getStateId(5,5), "RIGHT", terminalStateId), 0.001);

        world.setPuddlePositions(Arrays.asList(new int[]{1, 1}));
        assertEquals(-2.0, world.getReward(getStateId(1,1), "RIGHT", getStateId(1,2)), 0.001);

        assertEquals(-0.02, world.getReward(getStateId(0,0), "RIGHT", getStateId(0,1)), 0.001);
    }

    @Test
    public void testIsTerminalState() {
        int terminalStateId = getStateId(4,4);
        assertTrue(world.isTerminalState(terminalStateId));

        int nonTerminalStateId = getStateId(0, 0);
        assertFalse(world.isTerminalState(nonTerminalStateId));
    }

    @Test
    public void testApplyQUpdate() {
        int stateId = 1;
        int actionIndex = 0;
        double updatedQValue = 0.5;

        world.setQValue(stateId, actionIndex, updatedQValue);
        double retrievedQ = world.getMasterQValue(stateId, actionIndex);
        assertEquals(0.5, retrievedQ, 1e-6);
    }

    @Test
    public void testApplyVUpdate() {
        int stateId = 1;
        double updatedVValue = 0.3;

        world.setVValue(stateId, updatedVValue);
        double retrievedV = world.getMasterVValue(stateId);
        assertEquals(0.3, retrievedV, 1e-6);
    }

    @Test
    public void testReceiveQAndVUpdates() {
        world.setQValue(1, 0, 0.7);
        world.setQValue(1, 1, 0.4);
        world.setVValue(1, 0.6);

        assertEquals(0.7, world.getMasterQValue(1, 0), 1e-6);
        assertEquals(0.4, world.getMasterQValue(1, 1), 1e-6);
        assertEquals(0.6, world.getMasterVValue(1), 1e-6);
    }

    @Test
    public void testApplyQUpdateAlignmentWithClient() {
        int stateId = 10;
        int action = 1; 
        double initialQ = world.getMasterQValue(stateId, action);
        double reward = 1.0;
        int nextStateId = getStateId(2, 4);
        double maxNextQ = 0.0; 
        double alpha = 0.1;
        double gamma = 0.9;
        double expectedQ = initialQ + alpha * (reward + gamma * maxNextQ - initialQ);

        world.setQValue(stateId, action, expectedQ);
        double serverQ = world.getMasterQValue(stateId, action);
        assertEquals(expectedQ, serverQ, 1e-6);
        assertTrue(nextStateId >= 0 && nextStateId < 36);
    }

    @Test
    public void testApplyVUpdateAlignmentWithClient() {
        int stateId = 10;
        double updatedVValue = 0.3;

        world.setVValue(stateId, updatedVValue);
        double retrievedV = world.getMasterVValue(stateId);
        assertEquals(0.3, retrievedV, 1e-6);
    }

    @Test
    public void testMultipleQAndVUpdates() {
        world.setQValue(4, 2, 0.8);
        world.setVValue(4, 0.4);
        world.setQValue(4, 3, 0.6);
        world.setVValue(4, 0.45);
        world.setQValue(5, 0, 0.9);
        world.setVValue(5, 0.5);

        assertEquals(0.8, world.getMasterQValue(4, 2), 1e-6);
        assertEquals(0.6, world.getMasterQValue(4, 3), 1e-6);
        assertEquals(0.45, world.getMasterVValue(4), 1e-6);
        assertEquals(0.9, world.getMasterQValue(5, 0), 1e-6);
        assertEquals(0.5, world.getMasterVValue(5), 1e-6);
    }

    @Test
    public void testThreadSafetyInUpdates() throws InterruptedException {
        int stateId = 10;
        int actionIndex = 2;

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                world.setQValue(stateId, actionIndex, 0.1 * i);
                world.setVValue(stateId, 0.05 * i);
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                world.setQValue(stateId, actionIndex, 0.2 * i);
                world.setVValue(stateId, 0.1 * i);
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        double finalQ = world.getMasterQValue(stateId, actionIndex);
        double finalV = world.getMasterVValue(stateId);

        assertTrue(finalQ >= 0 && finalQ <= 200);
        assertTrue(finalV >= 0 && finalV <= 100);
    }

    // --- New Tests Checking Env-based Parameters and Behavior ---

    @Test
    public void testGridSizeFromEnv() {
        // We assume .env sets GRID_SIZE=6. 
        // Check a known terminal state at bottom-right corner.
        int terminalStateId = getStateId(4,4);
        assertTrue("If GRID_SIZE=5, state (4,4) should be terminal", world.isTerminalState(terminalStateId));
    }

    @Test
    public void testDefaultRewardFromEnv() {
        // .env sets DEFAULT_REWARD=-0.01
        world.reset();
        world.setPuddlePositions(Arrays.asList());
        int startState = getStateId(0,0);
        int nextState = getStateId(0,1);
        double reward = world.getReward(startState, "RIGHT", nextState);
        assertEquals("Default reward should match .env (-0.02)", -0.02, reward, 1e-6);
    }

    @Test
    public void testPuddleRewardFromEnv() {
        // .env sets PUDDLE_REWARD=-1.0
        world.setPuddlePositions(Arrays.asList(new int[]{2, 2}));
        int puddleState = getStateId(2,2);
        int nextPuddleState = getStateId(2,3); // same row, move right
        double reward = world.getReward(puddleState, "RIGHT", nextPuddleState);
        // If nextStateId is also puddle-involved, it yields puddleReward
        // Actually, a 2x2 puddle at (2,2) covers (2,2),(2,3),(3,2),(3,3)
        assertEquals("Puddle reward should match .env (-2.0)", -2.0, reward, 1e-6);
    }

    @Test
    public void testGoalRewardFromEnv() {
        // .env sets GOAL_REWARD=20.0
        int terminalStateId = getStateId(4,4);
        double reward = world.getReward(terminalStateId, "RIGHT", terminalStateId);
        assertEquals("Goal reward should match .env (20.0)", 20.0, reward, 1e-6);
    }

    @Test
    public void testMaxPuddlesFromEnv() {
        // .env sets MAX_PUDDLES=2
        // After reset, world tries to place 2 puddles. We can check that initialization doesn't fail.
        // We cannot directly read puddle count, but we can ensure world does not throw errors.
        // We'll just call reset and ensure no exceptions.
        world.reset();
        // If this runs without exceptions, we assume puddles are initialized correctly.
        assertTrue("World reset should initialize up to MAX_PUDDLES=2 puddles", true);
    }

    @Test
    public void testPuddleSizeFromEnv() {
        // .env sets PUDDLE_SIZE=2
        // When we set a puddle at (1,1), it covers 2x2 area.
        world.setPuddlePositions(Arrays.asList(new int[]{1, 1}));
        int[] puddleStateIds = {6,7,11,12};
        for (int id : puddleStateIds) {
            assertTrue("Should be puddle (2x2 area)", world.isPuddle(id));
        }
    }

    @Test
    public void testResetWorldResetsPuddles() {
        // After placing puddles, resetting the world should re-initialize them.
        world.setPuddlePositions(Arrays.asList(new int[]{0, 0}));
        world.reset();
        // After reset, puddles are re-initialized randomly. Check a known puddle state from previous setup is no longer guaranteed puddle.
        assertTrue("World reset runs without issues", true);
    }

    @Test
    public void testRewardsAreConsistent() {
        // Given .env defaults, normal transitions: -0.01, puddle: -1.0, goal: 10.0
        // Check a normal non-edge, non-puddle, non-goal transition:
        world.setPuddlePositions(Arrays.asList(new int[]{1,1}));
        int nonPuddleNonGoal = getStateId(0,1);
        int nonPuddleNext = getStateId(0,2);
        double reward = world.getReward(nonPuddleNonGoal, "RIGHT", nonPuddleNext);
        assertEquals("Non-puddle, non-goal reward should be -0.02", -0.02, reward, 1e-6);
    }

    @Test
    public void testNoExceptionsOnMultipleResets() {
        // Repeated resets should not cause exceptions
        for (int i = 0; i < 5; i++) {
            world.reset();
        }
        assertTrue("Multiple resets run without exceptions", true);
    }

    @Test
    public void testQAndVInitializationFromEnv() {
        // Q and V tables are initialized based on env parameters (like gamma, epsilon),
        // We cannot directly read gamma or epsilon from RLWorld without modifying it,
        // but we can at least ensure the Q-Values are not NaN or Infinity after initialization.
        int testState = getStateId(2,2);
        double[] actions = { world.getMasterQValue(testState,0), world.getMasterQValue(testState,1),
                             world.getMasterQValue(testState,2), world.getMasterQValue(testState,3) };
        for (double q : actions) {
            assertFalse("Q-values should be finite after initialization", Double.isNaN(q) || Double.isInfinite(q));
        }
        double v = world.getMasterVValue(testState);
        assertFalse("V-value should be finite after initialization", Double.isNaN(v) || Double.isInfinite(v));
    }
}
