package FieldTest;

import field.Antenna;
import field.BoardLaser;
import field.ConveyorBelt;
import field.Wall;
import field.tools.*;
import game.ActivationPhase;
import game.Game;
import game.Player;
import game.Robot;
import image.Image;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BoardLaserTest
{
    Gameboard gameboard;
    FieldGenerator fieldGenerator;
    Game game;
    BoardLaser boardLaser;
    @Before
    public void setUp(){
        fieldGenerator = new FieldGenerator();
        gameboard = new BoardGenerator().generateDizzyHighway();
        //Den Teil verändern für die Tests:
        Position laserPosition = new Position(0,0);
        Position wallPositionObstacle = new Position(0,2);
        Position robotPosition = new Position(6,3);
        Position antennaPosition = new Position(0,3);
        //STOP
        //gameboard.addSingleFieldAtPosition(fieldGenerator.generateWall(wallPositionObstacle, new WallOrientation(false, true, false, false)), wallPositionObstacle);
        //gameboard.addSingleFieldAtPosition(fieldGenerator.generateWall(laserPosition, new WallOrientation(true, false, false, false)), laserPosition);
        //boardLaser = fieldGenerator.generateBoardLaser(laserPosition,1);
        //gameboard.addSingleFieldAtPosition(boardLaser, laserPosition);
        ArrayList<String> orientation = new ArrayList<>();
        orientation.add("top");
        Antenna antenna = fieldGenerator.generateAntenna(antennaPosition,orientation);
        gameboard.addSingleFieldAtPosition(antenna, antennaPosition);
        gameboard.setAntennaOnBoard(antenna);
        Player p = new Player(1, "noob", null, false);
        Robot r = new Robot(0,0,new Direction(2),p);
        p.setRobot(r);
        ArrayList<Player> playerList = new ArrayList<>();
        playerList.add(p);
        game = new Game(gameboard, playerList, null);
        r.setCurrentGame(game);
        r.occupyField(robotPosition);
    }
    
    @Test
    public void testLaserDetectionObstacle(){
       boardLaser = gameboard.getFieldsAtPosition(new Position(6,4)).getLaser();
       Robot r = boardLaser.checkForRobotInLine();
       assertNotNull(r);
    }
}
