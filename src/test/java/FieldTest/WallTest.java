package FieldTest;

import field.Field;
import field.Wall;
import field.tools.*;
import game.ActivationPhase;
import game.Game;
import game.Player;
import game.Robot;
import org.junit.Before;
import org.junit.Test;
import server.Server;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WallTest
{
    private Robot myRobot;
    private Player dummyPlayer;
    private List<Player> playerList;
    private Game testGame;
    private Gameboard gameboard;
    private ActivationPhase activationPhase;
    
    @Before
    public void setUp(){
        dummyPlayer = new Player(0,"Dummy",null, false);
        myRobot = new Robot(0,0,new Direction(3),dummyPlayer);
        myRobot.setLookDir(new Direction(0));
        dummyPlayer.setRobot(myRobot);
        playerList= new ArrayList<>();
        playerList.add(dummyPlayer);
        
        gameboard = new Gameboard(30,30);
        gameboard.initializeWithDefaultField();
        testGame = new Game(gameboard,playerList, new Server());
        dummyPlayer.setCurrentGame(testGame);
        dummyPlayer.getRobot().setCurrentGame(testGame);
        activationPhase = new ActivationPhase(testGame,1);
        testGame.setActivationPhase(activationPhase);
        myRobot.setPosition(new Position(0,0));
        testGame.setUpGame();
        
        
    }
    
    @Test
    public void testCollision()
    {
        FieldGenerator fg = new FieldGenerator();
        FieldList fl = new FieldList();
        Position p = new Position(11,3);
        fl.addField(fg.generateWall(p,new WallOrientation(false, false, false, true)));
        testGame.getGameboard().addFieldsAtPosition(fl,p);
        fl.clear();
        fl.addField(fg.generateWall(p,new WallOrientation(true, true, false, true)));
        testGame.getGameboard().addFieldsAtPosition(fl,new Position(11,5));
        fl.clear();
        fl.addField(fg.generateWall(p,new WallOrientation(true, true, false, true)));
        testGame.getGameboard().addFieldsAtPosition(fl,new Position(11,2));
        fl.clear();
        fl.addField(fg.generateWall(p,new WallOrientation(true, false, false, true)));
        testGame.getGameboard().addFieldsAtPosition(fl,new Position(10,3));
        fl.clear();
        fl.addField(fg.generateWall(p,new WallOrientation(true, false, false, false)));
        testGame.getGameboard().addFieldsAtPosition(fl,new Position(12,3));
        fl.clear();
        
        myRobot.occupyField(p);
        myRobot.move(2,new Direction(0));
        assertEquals(new Position(11,2), myRobot.getPosition());
        myRobot.occupyField(p);
        myRobot.move(1,new Direction(1));
        assertEquals(new Position(12,3), myRobot.getPosition());
        myRobot.occupyField(p);
        myRobot.move(3,new Direction(2));
        assertEquals(new Position(11, 4), myRobot.getPosition());
        myRobot.occupyField(p);
        myRobot.move(8,new Direction(3));
        assertEquals(p, myRobot.getPosition());
        
    }
}
