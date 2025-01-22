package GameTest;

import card.damage.Spam;
import card.upgrade.RearLaser;
import field.Antenna;
import field.RebootToken;
import field.Wall;
import field.tools.*;
import game.Game;
import game.Player;
import game.Robot;
import image.Image;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import server.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class RobotTest {

    Game testGame;
    Player dummy;
    Player targetPlayer;
    Robot testRobot;
    Robot target;
    Position posAntenna;
    Antenna testAntenna;

    RebootToken reboot;
    FieldGenerator fieldGenerator = new FieldGenerator();

    @Before
    public void setUp(){

        dummy = new Player(0,"Test",testRobot, false);
        List<Player> playerList = new ArrayList<>();
        playerList.add(dummy);

        testGame = new Game(new Gameboard(10,10),playerList, new Server());
        dummy.setCurrentGame(testGame);

        testGame.setGameboard(new BoardGenerator().generateDizzyHighway());
        //testGame.setGameboard(new BoardGenerator().generateDeathTrap());

        testRobot = new Robot(0,0,new Direction(1),dummy);
        Position testPosForRobot = new Position(7,7);
        testRobot.setPosition(testPosForRobot);
        dummy.setRobot(testRobot);

        targetPlayer= new Player(1,"Target",target,false);
        target = new Robot(5,0,new Direction(2),targetPlayer);

        targetPlayer.setRobot(target);
        target.setCurrentGame(testGame);
        target.occupyField(new Position(5,7));
        playerList.add(targetPlayer);

        testGame.setUpGame();
        /*
        posAntenna = new Position(0,6);
        ArrayList<String> antennaOrientation = (ArrayList<String>) Collections.singletonList("top");
        testAntenna = new Antenna(posAntenna,"Antenna", false,new Image(), antennaOrientation);
        testGame.getGameboard().setAntennaOnBoard(testAntenna);
*/
        Position posReboot = new Position(8,7);
        reboot = new RebootToken(posReboot,"Reboot",true, new Image(), new Direction(0));
        testGame.getGameboard().setRebootTokenOnBoard();


    }

    @Test
    public void testDistanceAntenna(){
        testRobot.setPosition(new Position(3,3));
        int distance = testRobot.getDistanceToAntenna();
        assertTrue("Distance Funktion stimmt nicht: "+distance+" Position Antenne: "+testGame.getGameboard().getAntennaOnBoard().getPosition().x()+" , "+testGame.getGameboard().getAntennaOnBoard().getPosition().y()+" Antennen  distanz: "+testGame.getGameboard().getAntennaOnBoard().getOrientation(), distance == 11);
    }
    @Test
    public void testReboot(){
        testRobot.reboot();
        assertTrue("Robot is not at Reboot location", testRobot.getPosition().equals(reboot.getPosition()));
    }

    @Test
    public void testNotOnConveyorBelt(){
        Robot otherRobot = new Robot(0,0,new Direction(1), testRobot.getRobotPlayer());
        otherRobot.setPosition(new Position(5,5));
        testRobot.setPosition(new Position(5,4));
        testRobot.setLookDir(new Direction(1));
        assertFalse("otherRobot is on a ConveyorBelt/testRobot is not on ConveyorBelt",testRobot.notOnConveyorBelt(otherRobot));
    }

    @Test
    public void testFallOfBoard(){
        Robot otherRobot = new Robot(0,0,new Direction(1), testRobot.getRobotPlayer());
        otherRobot.setPosition(new Position(9,14));
        testRobot.setLookDir(new Direction(2));
        assertTrue("Robot will not fall off of Board, Result: "+ testRobot.fallOfBoard(otherRobot), testRobot.fallOfBoard(otherRobot));
    }

    @Test
    public void testScanLine(){
        Robot targetRobot = new Robot(0,0,new Direction(2), testRobot.getRobotPlayer());
        Position searchedPosition = new Position(6,5);
        targetRobot.setPosition(searchedPosition);

        testRobot.setPosition(new Position(5,5));
        testRobot.setLookDir(new Direction(1));

        List<Robot> foundRobots = testRobot.scanForRobotLine(6, testRobot.getLookDir());

        assertTrue("Keinen Roboter gefunden!", foundRobots.size()!= 0);
    }
   @Test
    public void testScanForRobots(){
        Robot r1 = new Robot(5,0,new Direction(1),dummy);
        Robot r2 = new Robot(5,0,new Direction(1),dummy);

        r1.setPosition(new Position(3,4));
        r2.setPosition(new Position(2,7));

        List<Robot> foundRobots = testRobot.scanForRobotRadius(6);

        assertTrue("Nicht alle Roboter gefunden! Gefunden: "+foundRobots.size(), foundRobots.size() == 2);
    }
    /**
     * Checks whether a path is passable (no collision) and free (not occupied)
    @author David
     */
    @Test
    public void testPathFree()
    {
        Player player = new Player(0,"Test",testRobot, false);
        List<Player> playerList = new ArrayList<>();
        playerList.add(player);
    
        Game game = new Game(new Gameboard(10,10),playerList, new Server());
        player.setCurrentGame(game);
    
        //testGame.setGameboard(new BoardGenerator().generateDizzyHighway());
        game.setGameboard(new Gameboard(10,10));
        game.getGameboard().initializeWithDefaultField();
    
        Robot robot = new Robot(0,0,new Direction(1),player);
        robot.setPosition(new Position(0,0));
        player.setRobot(robot);
    
        game.setUpGame();
        
        //no obstacles
        Assert.assertTrue(robot.isPathPassable(robot.getPosition(),0,3, robot.getLookDir()));
        Assert.assertTrue(robot.isPathFree(robot.getPosition(),0,3, robot.getLookDir()));
        Assert.assertFalse(robot.isPathFree(robot.getPosition(),0,12, robot.getLookDir()));
        //walls but no robots
        game.getGameboard().addSingleFieldAtPosition(fieldGenerator.generateWall(new Position(3,0), new WallOrientation(true, false, true, false)),new Position(3,0));
        game.getGameboard().addSingleFieldAtPosition(fieldGenerator.generateWall(new Position(6,0), new WallOrientation(true, true, true, false)),new Position(6,0));
       
        Assert.assertTrue(robot.isPathPassable(robot.getPosition(),0,4, robot.getLookDir()));
        Assert.assertTrue(robot.isPathFree(robot.getPosition(),0,4, robot.getLookDir()));
        Assert.assertTrue(robot.isPathPassable(robot.getPosition(),0,6, robot.getLookDir()));
        Assert.assertTrue(robot.isPathFree(robot.getPosition(),0,6, robot.getLookDir()));
        Assert.assertFalse(robot.isPathPassable(robot.getPosition(),0,8, robot.getLookDir()));
        
    }
    
    /**
     @author David
     */
    @Test
    public void testPush()
    {
        Player player1 = new Player(0,"Test",null, false);
        Player player2 = new Player(1,"Test",null, false);
        Player player3 = new Player(2,"Test",null, false);
        Player player4 = new Player(3,"Test",null, false);
        List<Player> playerList = new ArrayList<>();
        playerList.add(player1);
        playerList.add(player2);
        playerList.add(player3);
        playerList.add(player4);
    
        Game game = new Game(new Gameboard(10,10),playerList, new Server());
        player1.setCurrentGame(game);
        player2.setCurrentGame(game);
        player3.setCurrentGame(game);
        player4.setCurrentGame(game);
    
        //testGame.setGameboard(new BoardGenerator().generateDizzyHighway());
        Position positionRebootToken = new Position(5,5);
        RebootToken rebootToken = fieldGenerator.generateRebootToken(positionRebootToken,new Direction(0));
        game.getGameboard().initializeWithDefaultField();
        game.getGameboard().setRebootTokenOnBoard();
    
        Robot robot1 = new Robot(0,0,new Direction(1),player1);
        robot1.setPosition(new Position(0,0));
        Robot robot2 = new Robot(0,0,new Direction(1),player2);
        robot2.setPosition(new Position(1,0));
        Robot robot3 = new Robot(0,0,new Direction(1),player3);
        robot3.setPosition(new Position(2,0));
        Robot robot4 = new Robot(0,0,new Direction(1),player4);
        robot4.setPosition(new Position(5,5));
        player1.setRobot(robot1);
        player2.setRobot(robot2);
        player3.setRobot(robot3);
        player4.setRobot(robot4);
    
        game.setUpGame();
        //without obstacle
        robot1.move(1,robot1.getLookDir());
        Assert.assertEquals(robot1.getPosition(), new Position(1, 0));
        //with wall
        robot1.setPosition(new Position(0,0));
        robot2.setPosition(new Position(1,0));
        robot3.setPosition(new Position(2,0));
        robot4.setPosition(new Position(5,5));
        Position wallPosition = new Position(3,0);
        game.getGameboard().addSingleFieldAtPosition(fieldGenerator.generateWall(wallPosition, new WallOrientation(true,true,true,true)),wallPosition);
        robot1.move(1,robot1.getLookDir());
        Assert.assertEquals(robot1.getPosition(), new Position(0, 0));
        
    }
    @Test
    public void testRearLaserPresent(){
        boolean present = false;
        dummy.getRobot().getUpgradesPerm()[2] = new RearLaser();
        for(int i=0; i<3;i++){
            if(dummy.getRobot().getUpgradesPerm()[i] instanceof RearLaser){
                dummy.getRobot().getUpgradesPerm()[i].execute(dummy.getRobot());
                present = true;
            }
        }
        assertTrue("Keine RearLaser Karte gefunden",present);
    }

    @Test
    public void testRearLaserFunction(){
        RearLaser rearLaser = new RearLaser();
        int amountSpamBefore = 0;
        for(int i=0; i < target.getDiscardPile().size();i++){
            if(target.getDiscardPile().get(i) instanceof Spam){
                amountSpamBefore++;
            }
        }
        rearLaser.execute(dummy.getRobot());
        int amountSpamAfter = 0;
        for(int i=0; i < target.getDiscardPile().size();i++){
            if(target.getDiscardPile().get(i) instanceof Spam){
                amountSpamAfter++;
            }
        }
        assertTrue("before: "+amountSpamBefore+" after: "+amountSpamAfter,amountSpamBefore < amountSpamAfter);

    }
    
    /**
     * Erfolgreich, wenn es nicht abstürzt/kein StackOverflow gibt
     * @author David
     */
    @Test
    public void testPushLookAtEachOther()
    {
        Gameboard dizzyHighway = new BoardGenerator().generateDizzyHighway();
        Player player1 = new Player(0,"p1", null,false);
        Player player2 = new Player(1, "p2", null, false);
        Robot robot1 = new Robot(0,0,new Direction(0),player1);
        Robot robot2 = new Robot(0,0,new Direction(2),player2);
        ArrayList<Player> playerList = new ArrayList<>();
        playerList.add(player1);
        playerList.add(player2);
        Game game = new Game(dizzyHighway, playerList,new Server());
        player1.setRobot(robot1);
        player2.setRobot(robot2);
        robot1.setCurrentGame(game);
        robot2.setCurrentGame(game);
        robot1.setPosition(new Position(1,3));
        robot2.setPosition(new Position(1,2));
        robot1.move(3,robot1.getLookDir());
        robot2.move(4,robot2.getLookDir());
    }
    
    
}
