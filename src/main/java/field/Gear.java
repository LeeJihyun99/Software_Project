package field;

import field.reducedFields.ReducedField;
import field.reducedFields.ReducedGear;
import field.tools.ActionOnActivation;
import field.tools.FieldGenerator;
import field.tools.Position;
import game.Robot;
import image.Image;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Klasse zum Gearsfeld
 * @author DavidKulbe
 */
public class Gear extends Field implements ActionOnActivation
{
    boolean clockwise; //bestimmt die Drehrichtung
    public Gear(Position position, String name, boolean passThrough, Image image, boolean clockwise) {
        super(position, name, passThrough,image);
        this.clockwise = clockwise;
    }

    @Override
    public void actionOnActivation() {rotate(getParentList().getCurrentRobot());}


    public void rotate(Robot robot)
    {
        if (robot != null)
        {
            if (clockwise) robot.turn(1);
            else robot.turn(-1);
        }
    }

    public boolean getClockwise()
    {
        return clockwise;
    }
    
    @Override
    public ReducedField reduce()
    {
        ArrayList<String> orientationList = new ArrayList<>();
        if(clockwise)
        {
            orientationList.add("clockwise");
        }
        else
        {
            orientationList.add("counterclockwise");
        }
        return new ReducedGear(getBoardID(),orientationList);
    }
}
