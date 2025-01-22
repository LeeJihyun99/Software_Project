package field;

import field.reducedFields.ReducedField;
import field.reducedFields.ReducedLaser;
import field.tools.ActionOnActivation;
import field.tools.Direction;
import field.tools.Gameboard;
import field.tools.Position;
import game.Robot;
import image.Image;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Realisiert das Laserfeld
 * @author DavidKulbe
 */
public class BoardLaser extends Field implements ActionOnActivation
{
    private final int laserNum;
    public BoardLaser(Position position, String name, boolean passThrough, Image image, int laserNum) {
        super(position, name, passThrough, image);
        this.laserNum = laserNum;
    }

    public int getLaserNum() {
        return laserNum;
    }
    public Direction getLaserOrientation()
    {
        Wall wall = getParentList().getWall();
        Direction direction = wall.getOppositeDirection();
        if(direction == null)
        {
            logger.severe("Fehler bei Initialisierung, keine Wall für den Laser gefunden!");
        }
        return direction;
        
    }
    
    public ArrayList<String> getLaserOrientationStringList()
    {
        ArrayList<String> orientationList = new ArrayList<>();
        orientationList.add(getLaserOrientation().getDirectionString());
        return orientationList;
    }

    public void useLaser()
    {
        Robot robot = checkForRobotInLine();
        if(robot != null)
        {
            for (int i = 0; i<laserNum;i++)
            {
                robot.takeDamage(1);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    

    @Override
    public void actionOnActivation()
    {
        useLaser();
    }
    
    public Robot checkForRobotInLine()
    {
        Robot robot = null;
        Direction orientation = getLaserOrientation();
        Direction oppositeDirection = orientation.getOppositeDirection();
        Position position = getPosition();
        Gameboard gameboard = getParentList().getGameboard();
        while (robot == null)
        {
            if (gameboard.getFieldsAtPosition(position).isOccupied())
            {
                return robot = gameboard.getFieldsAtPosition(position).getCurrentRobot();
            }
            if (gameboard.getFieldsAtPosition(position) != null)
            {
                if (gameboard.getAntennaOnBoard().getPosition().equals(position)) //schaut, ob die Antenne getroffen werden würde
                {
                    return null;
                } else if (gameboard.getFieldsAtPosition(position).hasWall())
                {
                    Wall wall = gameboard.getFieldsAtPosition(position).getWall();
                    if (wall.calculateCollision(gameboard, orientation))
                    {
                        return null;
                    }
                }
                position = orientation.getNextPositionInDirection(position.x(), position.y());
            }
            else return null;
        }
        return robot;
    }
    
    @Override
    public ReducedField reduce()
    {
        return new ReducedLaser(getBoardID(), getLaserOrientationStringList(),laserNum);
    }
}
