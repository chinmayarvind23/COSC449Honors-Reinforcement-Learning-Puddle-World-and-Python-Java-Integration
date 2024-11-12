package ygraph.ai.smartfox.rl.test.java.yraph.ai.smartfox.rl.test;

import ygraph.ai.smartfox.rl.RLWorld;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;

public class RLWorldTest {

    private RLWorld world;

    @Before
    public void setUp() {
        world = new RLWorld();
    }

    private final double alpha = 0.1; // Learning rate
    private final double gamma = 0.9; // Discount factor

    // @Test
    // public void testInitialState() {
    //     assertEquals("Initial state should be 0", 0, world.getCurrentStateId());
    // }

    @Test
    public void testPerformActionUp() {
        int currentStateId = 10; // Corresponds to (2,0) in a 5x5 grid
        int expectedNextState = 5; // Corresponds to (1,0)
        int nextState = world.simulateAction(currentStateId, "UP");
        assertEquals("State should decrement row by 1", expectedNextState, nextState);
    }


    @Test
    public void testPerformActionDown() {
        int currentStateId = 5; // Corresponds to (1,0)
        int expectedNextState = 10; // Corresponds to (2,0)
        int nextState = world.simulateAction(currentStateId, "DOWN");
        assertEquals("State should increment row by 1", expectedNextState, nextState);
    }


    @Test
    public void testPerformActionLeft() {
        int currentStateId = 6; // Corresponds to (1,1)
        int expectedNextState = 5; // Corresponds to (1,0)
        int nextState = world.simulateAction(currentStateId, "LEFT");
        assertEquals("State should decrement column by 1", expectedNextState, nextState);
    }


    @Test
    public void testPerformActionRight() {
        int currentStateId = 5; // Corresponds to (1,0)
        int expectedNextState = 6; // Corresponds to (1,1)
        int nextState = world.simulateAction(currentStateId, "RIGHT");
        assertEquals("State should increment column by 1", expectedNextState, nextState);
    }


    @Test
    public void testIsPuddle() {
        // Manually set a puddle at (1,1)
        world.setPuddlePositions(Arrays.asList(new int[]{1, 1}));
        
        // In a 5x5 grid, a 2x2 puddle at (1,1) covers state IDs 6, 7, 11, 12
        int[] puddleStateIds = {6, 7, 11, 12};
        
        for(int id : puddleStateIds){
            assertTrue("State " + id + " should be a puddle", world.isPuddle(id));
        }
        
        // Check a state not in the puddle
        int nonPuddleStateId = 8; // Corresponds to (1,3)
        assertFalse("State " + nonPuddleStateId + " should not be a puddle", world.isPuddle(nonPuddleStateId));
    }



    @Test
    public void testGetReward() {
        // Terminal state: (4,4) corresponds to stateId=24
        assertEquals("Goal state reward should be 10.0", 10.0, world.getReward(19, "DOWN", 24), 0.001);
        
        // Puddle state: (1,1) corresponds to stateId=6, action "RIGHT" leads to stateId=7
        world.setPuddlePositions(Arrays.asList(new int[]{1, 1}));
        assertEquals("Puddle state reward should be -1.0", -1.0, world.getReward(6, "RIGHT", 7), 0.001);
        
        // Normal state: (0,0) corresponds to stateId=0, action "RIGHT" leads to stateId=1
        assertEquals("Normal state reward should be -0.01", -0.01, world.getReward(0, "RIGHT", 1), 0.001);
    }


    @Test
    public void testIsTerminalState() {
        assertTrue("State 24 should be terminal", world.isTerminalState(24));
        assertFalse("State 0 should not be terminal", world.isTerminalState(0));
    }


    @Test
    public void testApplyQUpdate() {
        int stateId = 1;
        int actionIndex = 0; // Assuming 0 corresponds to "UP"
        double updatedQValue = 0.5;

        world.setQValue(stateId, actionIndex, updatedQValue);
        double retrievedQ = world.getMasterQValue(stateId, actionIndex);
        assertEquals("Master Q-value should be updated to 0.5", 0.5, retrievedQ, 1e-6);
    }

    @Test
    public void testApplyVUpdate() {
        int stateId = 1;
        double updatedVValue = 0.3;

        world.setVValue(stateId, updatedVValue);
        double retrievedV = world.getMasterVValue(stateId);
        assertEquals("Master V-value should be updated to 0.3", 0.3, retrievedV, 1e-6);
    }

    @Test
    public void testReceiveQAndVUpdates() {
        // Simulate receiving Q and V updates from client
        world.setQValue(1, 0, 0.7); // "UP" action
        world.setQValue(1, 1, 0.4); // "DOWN" action
        world.setVValue(1, 0.6);

        // Verify Q-table updates
        assertEquals(0.7, world.getMasterQValue(1, 0), 1e-6);
        assertEquals(0.4, world.getMasterQValue(1, 1), 1e-6);

        // Verify V-table update
        assertEquals(0.6, world.getMasterVValue(1), 1e-6);
    }

    @Test
    public void testApplyQUpdateAlignmentWithClient() {
        int stateId = 10; // Valid stateId within 0-24
        int action = 1; // DOWN
        double initialQ = world.getMasterQValue(stateId, action);
        double reward = 1.0;
        int nextStateId = 15; // Simulate "DOWN" from stateId=10 -> stateId=15
        double maxNextQ = 0.0; // Assume next state's max Q-value is 0.0 for simplicity

        double expectedQ = initialQ + alpha * (reward + gamma * maxNextQ - initialQ);

        // Client computes updated Q-value and sends to server
        world.setQValue(stateId, action, expectedQ);

        // Verify server's master Q-table
        double serverQ = world.getMasterQValue(stateId, action);
        assertEquals("Server Q-table should reflect the updated Q-value", expectedQ, serverQ, 1e-6);

        // Utilize nextStateId to ensure it's within valid range
        assertTrue("Next state ID should be within valid range (0-24)", 
                nextStateId >= 0 && nextStateId < 25);
    }


    @Test
    public void testApplyVUpdateAlignmentWithClient() {
        int stateId = 10; // Valid stateId within 0-24
        double updatedVValue = 0.3;

        world.setVValue(stateId, updatedVValue);
        double retrievedV = world.getMasterVValue(stateId);
        assertEquals("Server V-table should reflect the updated V-value", 0.3, retrievedV, 1e-6);
    }



    @Test
    public void testMultipleQAndVUpdates() {
        // Simulate multiple Q and V updates and verify cumulative correctness

        // Update 1
        world.setQValue(4, 2, 0.8); // State 4, Action "LEFT"
        world.setVValue(4, 0.4);

        // Update 2
        world.setQValue(4, 3, 0.6); // State 4, Action "RIGHT"
        world.setVValue(4, 0.45);

        // Update 3
        world.setQValue(5, 0, 0.9); // State 5, Action "UP"
        world.setVValue(5, 0.5);

        // Verify updates for State 4
        assertEquals(0.8, world.getMasterQValue(4, 2), 1e-6);
        assertEquals(0.6, world.getMasterQValue(4, 3), 1e-6);
        assertEquals(0.45, world.getMasterVValue(4), 1e-6);

        // Verify updates for State 5
        assertEquals(0.9, world.getMasterQValue(5, 0), 1e-6);
        assertEquals(0.5, world.getMasterVValue(5), 1e-6);
    }

    @Test
    public void testThreadSafetyInUpdates() throws InterruptedException {
        // Simulate concurrent updates to Q and V tables

        int stateId = 10;
        int actionIndex = 2; // "LEFT"

        // Define two threads that update the same state-action pair and state
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

        // Start both threads
        thread1.start();
        thread2.start();

        // Wait for both threads to finish
        thread1.join();
        thread2.join();

        // Since the threads are running concurrently, the final value is non-deterministic
        // However, we can check that the values are within expected bounds
        double finalQ = world.getMasterQValue(stateId, actionIndex);
        double finalV = world.getMasterVValue(stateId);

        assertTrue("Final Q-value should be between 0 and 0.2*999", finalQ >= 0 && finalQ <= 199.8);
        assertTrue("Final V-value should be between 0 and 0.1*999", finalV >= 0 && finalV <= 99.9);
    }
}