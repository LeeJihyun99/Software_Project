package field;

import field.reducedFields.ReducedEnergySpace;
import field.reducedFields.ReducedField;
import field.tools.ActionOnActivation;
import field.tools.ActionOnLanding;
import field.tools.Position;
import game.Robot;
import image.Image;
import server.protocol.aktionen.Animation;
import server.protocol.aktionen.Energy;

/**
 * Realisiert ein Energyfeld
 * @author DavidKulbe
 */
public class EnergySpace extends Field implements ActionOnActivation
{

    private int cubesPresent;

    public EnergySpace(Position position, String name, boolean passThrough, Image image, int cubes) {
        super(position, name, passThrough, image);
        cubesPresent = cubes;
    }

    @Override
    public void actionOnActivation() {
        Robot robot = getParentList().getCurrentRobot();
        if (robot != null)
        {
            if (robot.getCurrentGame().getIterateRegistry() == 5)
            {
                robot.setAmountCubes(robot.getAmountCubes() + 1);
                return;
            }
            if (cubesPresent > 0)
            {
                activate(robot);
            }
            if(cubesPresent == 0) {
                Animation animation = new Animation("EngergySpace");
                robot.getGameServer().broadcast(animation);
            }
    
            Energy energy = new Energy(robot.getRobotPlayer().getPlayerID(), 1, "EnergySpace");
            robot.getGameServer().broadcast(energy);
        }
    }

    public int getCubesPresent() {
        return cubesPresent;
    }

    public void activate(Robot robot)
    {
        robot.setAmountCubes(robot.getAmountCubes()+1);
        cubesPresent--;
    }
    
    @Override
    public ReducedField reduce()
    {
        return new ReducedEnergySpace(getBoardID(), cubesPresent);
    }
}
