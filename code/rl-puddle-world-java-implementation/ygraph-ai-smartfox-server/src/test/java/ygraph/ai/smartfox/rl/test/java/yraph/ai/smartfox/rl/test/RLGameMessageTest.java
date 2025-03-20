// package ygraph.ai.smartfox.rl.test.java.yraph.ai.smartfox.rl.test;

// import org.junit.Before;
// import org.junit.Test;
// import static org.junit.Assert.*;

// import ygraph.ai.smartfox.rl.RLGameMessage;

// public class RLGameMessageTest {

//     private RLGameMessage msg;

//     @Before
//     public void setUp() {
//         msg = new RLGameMessage();
//     }

//     @Test
//     public void testSetAndGetMessageType() {
//         msg.setMessageType(RLGameMessage.GAME_STATE);
//         assertEquals(RLGameMessage.GAME_STATE, msg.getMessageType());
//     }

//     @Test
//     public void testSetAndGetStateId() {
//         msg.setStateId(10);
//         assertEquals(10, msg.getStateId());
//     }

//     @Test
//     public void testSetAndGetAction() {
//         msg.setAction(2);
//         assertEquals(2, msg.getAction());
//     }

//     @Test
//     public void testSetAndGetReward() {
//         msg.setReward(5.0);
//         assertEquals(5.0, msg.getReward(), 1e-6);
//     }

//     @Test
//     public void testSetAndGetNextStateId() {
//         msg.setNextStateId(15);
//         assertEquals(15, msg.getNextStateId());
//     }

//     @Test
//     public void testSetAndGetIsTerminal() {
//         msg.setTerminal(true);
//         assertTrue(msg.isTerminal());
//         msg.setTerminal(false);
//         assertFalse(msg.isTerminal());
//     }

//     @Test
//     public void testSetAndGetCumulativeReward() {
//         msg.setCumulativeReward(10.5);
//         assertEquals(10.5, msg.getCumulativeReward(), 1e-6);
//     }

//     @Test
//     public void testSetAndGetStepsThisEpisode() {
//         msg.setStepsThisEpisode(20);
//         assertEquals(20, msg.getStepsThisEpisode());
//     }

//     @Test
//     public void testSetAndGetTotalEpisodes() {
//         msg.setTotalEpisodes(3);
//         assertEquals(3, msg.getTotalEpisodes());
//     }

//     @Test
//     public void testSetAndGetSuccessfulEpisodes() {
//         msg.setSuccessfulEpisodes(1);
//         assertEquals(1, msg.getSuccessfulEpisodes());
//     }

//     @Test
//     public void testQUpdateFields() {
//         int[] qStateIds = {1, 2};
//         int[] qActions = {0, 1};
//         double[] qValues = {0.5, 0.6};

//         msg = new RLGameMessage(qStateIds, qActions, qValues);
//         assertArrayEquals(qStateIds, msg.getQStateIds());
//         assertArrayEquals(qActions, msg.getQActionIndices());
//         assertArrayEquals(qValues, msg.getQValues(), 1e-6);
//     }

//     @Test
//     public void testVUpdateFields() {
//         int[] vStateIds = {3, 4};
//         double[] vVals = {0.3, 0.4};

//         msg = new RLGameMessage(vStateIds, vVals);
//         assertArrayEquals(vStateIds, msg.getVStateIds());
//         assertArrayEquals(vVals, msg.getVValues(), 1e-6);
//     }
// }