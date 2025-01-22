package field;

import field.reducedFields.ReducedField;
import field.reducedFields.ReducedPit;
import field.tools.ActionOnLanding;
import field.tools.Position;
import game.Robot;
import image.Image;

/**
 * Realisiert ein Grubenfeld
 * @author DavidKulbe
 */
public class Pit extends Field implements ActionOnLanding
{
    public Pit(Position position, String name, boolean passThrough, Image image) {
        super(position, name, passThrough,  image);
    }

    @Override
    public void actionOnLanding() {fall(getParentList().getCurrentRobot());}


    public void fall(Robot robot)
    {robot.reboot();}
    
    @Override
    public ReducedField reduce()
    {
        return new ReducedPit(getBoardID());
    }
}
