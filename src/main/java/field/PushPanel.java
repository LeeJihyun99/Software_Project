package field;

import field.reducedFields.ReducedField;
import field.reducedFields.ReducedPushPanel;
import field.tools.ActionOnActivation;
import field.tools.Direction;
import field.tools.Position;
import field.tools.WallOrientation;
import game.Robot;
import image.Image;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Klaase zu dem Pushpanel Feld
 * @author DavidKulbe
 */
public class PushPanel extends Field implements ActionOnActivation
{
    private final ArrayList<Integer> regNumbers; //betreffende Registernummern

    public PushPanel(Position position, String name, boolean passThrough, Image image, ArrayList<Integer> regNumbers) {
        super(position, name, passThrough, image);
        this.regNumbers = regNumbers;
    }



   
    public void push(Robot robot)
    {
        if (robot != null)
        {
            robot.move(1, this.getPanelOrientation());
        }
    }

    @Override
    public void actionOnActivation() {
        push(getParentList().getCurrentRobot());
    }

    public ArrayList<Integer> getRegNumbers()
    {
        return regNumbers;
    }
    
    public Direction getPanelOrientation()
    {
        Wall wall = getParentList().getWall();
        Direction direction = wall.getOppositeDirection();
        if(direction == null)
        {
            logger.severe("Fehler bei Initialisierung, keine Wall für das Pushpanel gefunden!");
        }
        return direction;
        
    }
    
    public ArrayList<String> getPanelOrientationStringList()
    {
        ArrayList<String> orientationList = new ArrayList<>();
        orientationList.add(getPanelOrientation().getDirectionString());
        return orientationList;
    }
    
    
    @Override
    public ReducedField reduce()
    {
        return new ReducedPushPanel(getBoardID(), getPanelOrientationStringList(),regNumbers);
    }
}
