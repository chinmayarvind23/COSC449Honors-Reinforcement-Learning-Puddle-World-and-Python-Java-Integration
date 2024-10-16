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

    @Test
    public void testInitialState() {
        assertEquals("Initial state should be 0", 0, world.getCurrentStateId());
    }

    @Test
    public void testPerformActionUp() {
        int nextState = world.performAction(25, "UP");
        assertEquals("State should decrement row by 1", 5, nextState);
    }

    @Test
    public void testPerformActionDown() {
        int nextState = world.performAction(5, "DOWN");
        assertEquals("State should increment row by 1", 5 + 20, nextState);
    }

    @Test
    public void testPerformActionLeft() {
        int nextState = world.performAction(5, "LEFT");
        assertEquals("State should decrement column by 1", 5 - 1, nextState);
    }

    @Test
    public void testPerformActionRight() {
        int nextState = world.performAction(5, "RIGHT");
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
}