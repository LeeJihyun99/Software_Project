package FieldTest;

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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PitTest
{
    private Robot myRobot;
    private Player dummyPlayer;
    private List<Player> playerList;
    private Game testGame;
    private Gameboard gameboard;
    private ActivationPhase activationPhase;
    private FieldGenerator fieldGenerator;
    
    @Before
    public void setUp(){
        fieldGenerator = new FieldGenerator();
        dummyPlayer = new Player(0,"Dummy",null, false);
        myRobot = new Robot(0,0,new Direction(3),dummyPlayer);
        myRobot.setLookDir(new Direction(1));
        dummyPlayer.setRobot(myRobot);
        playerList= new ArrayList<>();
        playerList.add(dummyPlayer);
    
        gameboard = new Gameboard(30,30);
        gameboard.initializeWithDefaultField();
        RebootToken rebootToken = fieldGenerator.generateRebootToken(new Position(5,5),new Direction(0));
        gameboard.addSingleFieldAtPosition(rebootToken,new Position(5,5));
        gameboard.setRebootTokenOnBoard();
        testGame = new Game(gameboard,playerList, new Server());
        dummyPlayer.setCurrentGame(testGame);
        dummyPlayer.getRobot().setCurrentGame(testGame);
        activationPhase = new ActivationPhase(testGame,1);
        testGame.setActivationPhase(activationPhase);
        myRobot.setPosition(new Position(0,0));
        testGame.setUpGame();
        
        
    }
    
    @Test
    public void testPitFallMovement()
    {
        gameboard.addSingleFieldAtPosition(fieldGenerator.generatePit(new Position(4,0)),new Position(4,0));
        myRobot.move(2,myRobot.getLookDir());
        assertNotEquals(myRobot.getPosition(),gameboard.getRebootTokenOnBoard(myRobot.getCurrentCourseId()).getPosition());
        myRobot.move(5, myRobot.getLookDir());
        assertEquals(new Position(8,5), myRobot.getPosition());
        myRobot.occupyField(new Position(3,0));
        myRobot.move(1,myRobot.getLookDir());
        assertEquals(gameboard.getRebootTokenOnBoard(myRobot.getCurrentCourseId()).getPosition(), myRobot.getPosition());
    }
    
}
