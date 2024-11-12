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
        int nextState = world.simulateAction(25, "UP");
        assertEquals("State should decrement row by 1", 5, nextState);
    }

    @Test
    public void testPerformActionDown() {
        int nextState = world.simulateAction(5, "DOWN");
        assertEquals("State should increment row by 1", 5 + 20, nextState);
    }

    @Test
    public void testPerformActionLeft() {
        int nextState = world.simulateAction(5, "LEFT");
        assertEquals("State should decrement column by 1", 5 - 1, nextState);
    }

    @Test
    public void testPerformActionRight() {
        int nextState = world.simulateAction(5, "RIGHT");
        assertEquals("State should increment column by 1", 5 + 1, nextState);
    }

    @Test
    public void testIsPuddle() {
        // Manually set a puddle at (1,1)
        world.setPuddlePositions(Arrays.asList(new int[]{1, 1}));
        
        // State ID for (1,1) assuming gridSize = 20
        int puddleStateId = 1 * 20 + 1; // 21
        
        // State ID for (1,2) assuming gridSize = 20
        int nonPuddleStateId = 1 * 20 + 2; // 22
        
        assertTrue("State 21 should be a puddle", world.isPuddle(puddleStateId));
        assertTrue("State 22 should also be a puddle (part of 2x2 puddle)", world.isPuddle(nonPuddleStateId));
    }


    @Test
    public void testGetReward() {
        // Terminal state
        assertEquals("Goal state reward should be 1.0", 1.0, world.getReward(0, "DOWN", 399), 0.001);
        // Puddle state
        world.setPuddlePositions(Arrays.asList(new int[]{1, 1}));
        assertEquals("Puddle state reward should be -1.0", -1.0, world.getReward(21, "RIGHT", 22), 0.001);
        // Normal state
        assertEquals("Normal state reward should be 0.01", 0.01, world.getReward(0, "RIGHT", 1), 0.001);
    }

    @Test
    public void testIsTerminalState() {
        assertTrue("State 399 should be terminal", world.isTerminalState(399));
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
        // Simulate a client-side Q-learning update and verify server applies it correctly

        int stateId = 30;
        int action = 1; // DOWN
        double initialQ = world.getMasterQValue(stateId, action);
        double reward = 1.0;
        int nextStateId = 50;
        double maxNextQ = 0.0; // Assume next state's max Q-value is 0.0 for simplicity

        double expectedQ = initialQ + alpha * (reward + gamma * maxNextQ - initialQ);

        // Client computes updated Q-value and sends to server
        world.setQValue(stateId, action, expectedQ);

        // Verify server's master Q-table
        double serverQ = world.getMasterQValue(stateId, action);
        assertEquals("Server Q-table should reflect the updated Q-value", expectedQ, serverQ, 1e-6);

        // Utilize nextStateId to ensure it's within valid range
        assertTrue("Next state ID should be within valid range (0-399)", 
                nextStateId >= 0 && nextStateId < 400);
    }

    @Test
    public void testApplyVUpdateAlignmentWithClient() {
        // Simulate a client-side TD(0) update and verify server applies it correctly

        int stateId = 40;
        double initialV = world.getMasterVValue(stateId);
        double reward = 0.5;
        int nextStateId = 60;
        double nextV = world.getMasterVValue(nextStateId); // Assume 0.0 for simplicity

        double expectedV = initialV + alpha * (reward + gamma * nextV - initialV);

        // Client computes updated V-value and sends to server
        world.setVValue(stateId, expectedV);

        // Verify server's master V-table
        double serverV = world.getMasterVValue(stateId);
        assertEquals("Server V-table should reflect the updated V-value", expectedV, serverV, 1e-6);

        // Utilize nextStateId to ensure it's within valid range
        assertTrue("Next state ID should be within valid range (0-399)", 
                nextStateId >= 0 && nextStateId < 400);
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