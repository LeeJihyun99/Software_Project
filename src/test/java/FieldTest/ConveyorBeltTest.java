package FieldTest;

import card.programming.MoveI;
import card.programming.MoveII;
import field.Antenna;
import field.CheckPoint;
import field.ConveyorBelt;
import field.RebootToken;
import field.tools.*;
import game.ActivationPhase;
import game.Game;
import game.Player;
import game.Robot;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import server.Server;
import server.protocol.aktionen.Reboot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

public class ConveyorBeltTest {

    private Robot robot1;
    private Robot robot2;
    private Player dummyPlayer;
    private List<Player> playerList;
    private Game testGame;
    private Gameboard gameboard;
    private ActivationPhase activationPhase;
    private Thread conveyorBeltThread;
    
    private FieldGenerator fieldGenerator;
    private Game game;
    @Before
    public void setUp(){
        
        fieldGenerator = new FieldGenerator();
        gameboard = new Gameboard(10,10);
        gameboard.initializeWithDefaultField();
    
        ArrayList<Direction> directions1 = new ArrayList<>();
        ArrayList<Direction> directions2 = new ArrayList<>();
        ArrayList<Direction> directions3 = new ArrayList<>();
        ArrayList<Direction> directions4 = new ArrayList<>();
        ArrayList<Direction> directions5 = new ArrayList<>();
        ArrayList<Direction> directions6 = new ArrayList<>();
        ArrayList<Direction> directions7 = new ArrayList<>();
        
        //Den Teil verändern für die Tests:
        Position conveyor1Position = new Position(0,0);
        Position conveyor2Position = new Position(1,0);
        Position conveyor3Position = new Position(2,0);
        Position conveyor4Position = new Position(4,0);
        Position conveyor5Position = new Position(5,0);
        Position conveyor6Position = new Position(2,1);
        Position conveyor7Position = new Position(2,2);
        Position robot1Position = new Position(0,0);
        Position robot2Position = new Position(3,0);
        
        directions1.add(new Direction(1));
        directions1.add(new Direction(3));
        directions2.add(new Direction(1));
        directions2.add(new Direction(3));
        directions3.add(new Direction(1));
        directions3.add(new Direction(3));
        directions4.add(new Direction(1));
        directions4.add(new Direction(3));
        directions5.add(new Direction(0));
        directions5.add(new Direction(3));
        directions6.add(new Direction(0));
        directions6.add(new Direction(2));
        directions7.add(new Direction(0));
        directions7.add(new Direction(2));
        
        gameboard.addSingleFieldAtPosition(fieldGenerator.generateConveyorBelt(conveyor1Position, directions1,2), conveyor1Position);
        gameboard.addSingleFieldAtPosition(fieldGenerator.generateConveyorBelt(conveyor2Position, directions2,2), conveyor2Position);
        gameboard.addSingleFieldAtPosition(fieldGenerator.generateConveyorBelt(conveyor3Position, directions3,2), conveyor3Position);
        gameboard.addSingleFieldAtPosition(fieldGenerator.generateConveyorBelt(conveyor4Position, directions4,1), conveyor4Position);
        gameboard.addSingleFieldAtPosition(fieldGenerator.generateConveyorBelt(conveyor5Position, directions5,1), conveyor5Position);
        gameboard.addSingleFieldAtPosition(fieldGenerator.generateConveyorBelt(conveyor6Position, directions6,2), conveyor6Position);
        gameboard.addSingleFieldAtPosition(fieldGenerator.generateConveyorBelt(conveyor7Position, directions7,2), conveyor7Position);
        ArrayList<String> antennaOrientation = new ArrayList<>();
        antennaOrientation.add("top");
        Antenna antenna = fieldGenerator.generateAntenna(new Position(9,9),antennaOrientation);
        gameboard.addSingleFieldAtPosition(antenna, new Position(9,9));
        RebootToken reboot = fieldGenerator.generateRebootToken(new Position(4,4),new Direction(2));
        gameboard.addSingleFieldAtPosition(reboot, reboot.getPosition());
    
        //STOP
        Player p = new Player(1, "noob", null, false);
        robot1 = new Robot(0,0,new Direction(2),p);
        p.setRobot(robot1);
        ArrayList<Player> playerList = new ArrayList<>();
        playerList.add(p);
        Player p2 = new Player(2, "noobnoob", null, false);
        robot2 = new Robot(0,0,new Direction(2),p2);
        p.setRobot(robot1);
        p.getRobot().getRegister()[0] = new MoveI();
        p2.setRobot(robot2);
        p2.getRobot().getRegister()[0] = new MoveI();
        playerList.add(p2);

        gameboard.setAntennaOnBoard(antenna);
        gameboard.setRebootTokenOnBoard();
        gameboard.setName("");
        game = new Game(gameboard, playerList, new Server());
        game.setUpGame();
        robot2.setCurrentGame(game);
        robot2.occupyField(robot2Position);
        robot1.setCurrentGame(game);
        robot1.occupyField(robot1Position);
        activationPhase = new ActivationPhase(game,0);
        p.setCurrentGame(game);
        p2.setCurrentGame(game);

    }
    
    @Test
    public void testSingleMovement()
    {
        Position positionOfAction = new Position(4,0);
        robot1.occupyField(positionOfAction);
        //activationPhase.execute();
        //game.getGameboard().getFieldsAtPosition(positionOfAction).getConveyorBelt().actionOnActivation();
        activateConveyorBelt();
        assertEquals(new Position(5,0), robot1.getPosition());
        
    }
    @Test
    public void testDoubleMovement()
    {
        Position positionOfAction = new Position(0,0);
        robot1.occupyField(positionOfAction);
        //activateConveyorBelt();
        //activationPhase.execute();
        game.getGameboard().getFieldsAtPosition(positionOfAction).getConveyorBelt().actionOnActivation();
        //assertEquals(new Position(5,0), robot2.getPosition());
        assertEquals(new Position(2,0), robot1.getPosition());
    }
    
    @Test
    public void testStopOtherRobotInFrontOffConveyorBelt()
    {
        Position positionOfAction = new Position(2,0);
        robot1.occupyField(positionOfAction);
        robot2.occupyField(new Position(3,0));
        //activationPhase.execute();
        game.getGameboard().getFieldsAtPosition(positionOfAction).getConveyorBelt().actionOnActivation();
        assertEquals(new Position(2,0), robot1.getPosition());
    }
    
    @Test
    public void testStopLeavingConveyorBelt()
    {
        Position positionOfAction = new Position(2,0);
        robot1.occupyField(positionOfAction);
        robot2.occupyField(new Position(8,8));
        //activationPhase.execute();
        game.getGameboard().getFieldsAtPosition(positionOfAction).getConveyorBelt().actionOnActivation();
        assertEquals(new Position(3,0), robot1.getPosition());
    }
    
    @Test
    public void testTurn() //linksdrehung
    {
        Position positionOfAction = new Position(4,0);
        robot1.occupyField(positionOfAction);
        //activationPhase.execute();
        ConveyorBelt conveyorBelt = game.getGameboard().getFieldsAtPosition(positionOfAction).getConveyorBelt();
        conveyorBelt.actionOnActivation();
        assertEquals(new Position(5,0), robot1.getPosition());
        assertEquals(1, robot1.getLookDir().getDirectionInteger());
    }
    
    @Test
    public void testStopMovingToSameField()
    {
        Position positionOfActionRobot1 = new Position(0,0);
        Position positionOfActionRobot2 = new Position(2,2);
        robot1.occupyField(positionOfActionRobot1);
        robot2.occupyField(positionOfActionRobot2);
        //activationPhase.execute();
        game.getGameboard().getFieldsAtPosition(positionOfActionRobot1).getConveyorBelt().actionOnActivation();
        game.getGameboard().getFieldsAtPosition(positionOfActionRobot2).getConveyorBelt().actionOnActivation();
        assertEquals(positionOfActionRobot1, robot1.getPosition());
        assertEquals(positionOfActionRobot2, robot2.getPosition());
    }
    
    /**
    Tests two robots on two conveyor belts in a row, the one blocked by the other gets activated first
     @author David
     */
    @Test
    public void testTwoRobotsOnAConveyorBeltInARow()
    {
        Position positionOfActionRobot1 = new Position(0,0);
        Position positionOfActionRobot2 = new Position(1,0);
        robot1.occupyField(positionOfActionRobot1);
        robot2.occupyField(positionOfActionRobot2);
        //activationPhase.execute();
        activateConveyorBelt();
        assertEquals(new Position(2,0), robot1.getPosition());
        assertEquals(new Position(3,0), robot2.getPosition());
    }
    
    @Test
    public void testRotationOnConveyorBelt(){
        //Setup
        Gameboard DizzyHighway = new BoardGenerator().generateDizzyHighway();
        Player p = new Player(1, "noob", null, false);
        Robot robot = new Robot(0,0,new Direction(1),p);
        p.setRobot(robot);
        ArrayList<Player> playerList = new ArrayList<>();
        playerList.add(p);
        Position positionRobot = new Position(11,2);
    
        
        Game newGame = new Game(DizzyHighway, playerList, new Server());
        newGame.setUpGame();
        robot.setCurrentGame(newGame);
        robot.occupyField(positionRobot);
        p.setCurrentGame(newGame);
        robot.setLookDir(DizzyHighway.getFieldsAtPosition(robot.getPosition()).getConveyorBelt().getFlow_direction_out());
        //Test
        for (int i = 1; i < new Random().nextInt(40);i++)
        {
            for (int x = 1; x < new Random().nextInt(50);x++)
            {
                DizzyHighway.getFieldsAtPosition(robot.getPosition()).getConveyorBelt().actionOnActivation();
            }
            assertEquals(DizzyHighway.getFieldsAtPosition(robot.getPosition()).getConveyorBelt().getFlow_direction_out().getDirectionInteger(), robot.getLookDir().getDirectionInteger());
        }
        robot.occupyField(new Position(9,8));
        robot.setLookDir(DizzyHighway.getFieldsAtPosition(robot.getPosition()).getConveyorBelt().getFlow_direction_out());
        DizzyHighway.getFieldsAtPosition(robot.getPosition()).getConveyorBelt().actionOnActivation();
        assertEquals(DizzyHighway.getFieldsAtPosition(robot.getPosition()).getConveyorBelt().getFlow_direction_out().getDirectionInteger(), robot.getLookDir().getDirectionInteger());
    }
    @Test
    public void testMovementCheckPoints()
    {
        Gameboard twister = new BoardGenerator().generateTwister();
        Position exampleConveyorBeltPosition = new Position(10,1);
        CheckPoint checkPoint = twister.getFieldsAtPosition(exampleConveyorBeltPosition).getCheckPoint();
        twister.getFieldsAtPosition(exampleConveyorBeltPosition).getConveyorBelt().moveCheckPoint(checkPoint);
        twister.getFieldsAtPosition(exampleConveyorBeltPosition).getConveyorBelt().moveCheckPoint(checkPoint);
        Position newPositionCheckPoint = new Position(11,2);
        Assert.assertTrue(twister.getFieldsAtPosition(newPositionCheckPoint).hasCheckPoint());
    }
    private void activateConveyorBelt()
    {
        ArrayList<AtomicBoolean> endings = new ArrayList<>();
        ArrayList<ConveyorBelt> conveyorBelts = new ArrayList<>();
        conveyorBelts.add(game.getGameboard().getFieldsAtPosition(new Position(1,0)).getConveyorBelt());
        conveyorBelts.add(game.getGameboard().getFieldsAtPosition(new Position(0,0)).getConveyorBelt());
        conveyorBelts.add(game.getGameboard().getFieldsAtPosition(new Position(4,0)).getConveyorBelt());
        for(ConveyorBelt cb: conveyorBelts){
            AtomicBoolean endOfThisThread = new AtomicBoolean(false);
            endings.add(endOfThisThread);
            int indexOfThisSemaphor = endings.toArray().length-1;
            conveyorBeltThread = new Thread(() ->
            {
                cb.actionOnActivation();
                endings.get(indexOfThisSemaphor).set(true);
                
            });
            conveyorBeltThread.start();
        }
        while (!allThreadsEnded(endings))
        {
            Thread.onSpinWait();
        }
    }
    
    private boolean allThreadsEnded(ArrayList<AtomicBoolean> semaphors)
    {
        for (AtomicBoolean atomicBoolean: semaphors)
        {
            if (!atomicBoolean.get())
            {
                return false; //mindestens ein Thread läuft noch
            }
        }
        return true; //alle beendet
    }
}
