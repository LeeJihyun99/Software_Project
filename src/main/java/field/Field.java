package field;

import field.tools.FieldList;
import field.tools.Position;
import field.tools.ReduceField;
import game.Robot;
import image.Image;

import tools.ServerLogger;

import java.util.logging.Logger;

/**
 * Abstrakte Klasse, von welcher die einzelnen Felarten erben
 * @author DavidKulbe
 */
public abstract class Field implements ReduceField
{
    private Position position;
    private final String name;
    private boolean occupied; //ist das Feld durch einen Roboter belegt
    private final boolean passThrough; //kann durch dieses Feld geschossen oder laufen werden
    private Image image;
    private Robot currentRobot = null; //Roboter, der aktuell auf diesem Feld steht
    
    private FieldList parentList;
    
    protected Logger logger = ServerLogger.getLogger();

    public Field(Position position, String name, boolean passThrough, Image image)
    {
        occupied = false;
        this.position = position;
        this.name = name;
        this.passThrough = passThrough;
        this.image = image;
    }

    public void setCurrentRobot(Robot currentRobot) {
        this.currentRobot = currentRobot;
    }

    public void removeCurrentRobot()
    {
        this.currentRobot = null;
    }
    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public boolean getOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public boolean getPassThrough() {
        return passThrough;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }


    public Robot getCurrentRobot() {
        return currentRobot;
    }
    
    public void setParentList(FieldList fieldList)
    {
        parentList = fieldList;
    }
    
    public FieldList getParentList()
    {
        return parentList;
    }
    
    public String getBoardID()
    {
        return parentList.getIsOnBoard();
    }
    


}
