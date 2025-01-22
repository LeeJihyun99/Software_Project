package field.tools;

import field.*;
import image.ImageHandler;

import java.util.ArrayList;

/**
 * Eine Klasse, welches einem erlaubt einfacher Felder zu erstellen
 * @author DavidKulbe
 */
public class FieldGenerator {
    private static final ImageHandler imageHandler = new ImageHandler();
    public Antenna generateAntenna(Position position, ArrayList<String> orientation)
    {
        return new Antenna(position,"antenna", false, imageHandler.generateAntenna(), orientation);
    }
    public BoardLaser generateBoardLaser(Position position, int laserNum)
    {
        return new BoardLaser(position,"boardlaser", true, imageHandler.generateBoardLaser(), laserNum);
    }
    public CheckPoint generateCheckpoint(Position position, int checkPointNum)
    {
        return new CheckPoint(position, "checkpoint", true, imageHandler.generateCheckpoint(), checkPointNum);
    }
    public ConveyorBelt generateConveyorBelt(Position position, ArrayList<Direction> directions, int speed)
    {
        return new ConveyorBelt(position, "conveyorbelt", true,  imageHandler.generateConveyorBelt(speed), directions, speed);
    }
    public DefaultField generateDefaultField(Position position)
    {
        return new DefaultField(position, "defaultfield", true, imageHandler.generateDefaultField());
    }
    public EnergySpace generateEnergySpaces(Position position, int cubes)
    {
        return new EnergySpace(position, "energyspace", true,  imageHandler.generateEnergySpaces(), cubes);
    }
    public Gear generateGears(Position position, boolean clockwise)
    {
        return new Gear(position, "gear", true, imageHandler.generateGears(), clockwise);
    }
    public Pit generatePit(Position position)
    {
        return new Pit(position, "pit", true,  imageHandler.generatePit());
    }
    public PushPanel generatePushPanel(Position position, ArrayList<Integer> regNumbers)
    {
        return new PushPanel(position, "pushpanel", true,  imageHandler.generatePushPanel(regNumbers), regNumbers);
    }
    public RebootToken generateRebootToken(Position position, Direction direction)
    {
        return new RebootToken(position, "rebootToken", true, imageHandler.generateRebootToken(), direction);
    }
    public Wall generateWall(Position position, WallOrientation wallOrientation)
    {
        return new Wall(position, "wall", true,  imageHandler.generateWall(wallOrientation), wallOrientation);
    }
    
    public StartPoint generateStartPoint (Position position)
    {
        return new StartPoint(position, "startpoint", true, imageHandler.generateStartField());
    }




}
