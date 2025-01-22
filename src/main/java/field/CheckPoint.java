package field;

import field.reducedFields.ReducedCheckPoint;
import field.reducedFields.ReducedField;
import field.tools.ActionOnActivation;
import field.tools.ActionOnLanding;
import field.tools.Position;
import game.Robot;
import image.Image;
import server.protocol.aktionen.CheckPointReached;

/**
 * Realisiert ein Checkpointfeld
 * @author DavidKulbe
 */
public class CheckPoint extends Field implements ActionOnActivation
{

    private int checkNum; //Nummer des Checkpoints
    
    private int timesMovedThisRound = 0;

    public CheckPoint(Position position, String name, boolean passThrough, Image image, int checkNum) {
        super(position, name, passThrough, image);
        this.checkNum = checkNum;
    }

    public int getCheckNum() {
        return checkNum;
    }

    @Override
    public void actionOnActivation()
    {
        Robot robot = getParentList().getCurrentRobot();
        if (robot != null)
        {
        //prüft, ob der letzte Checkpoint eins kleiner, als dieser ist und im Falle von true wird der Checkpoint des Roboters auf this aktualisiert
        if (robot.getCheckpointToken() + 1 == checkNum)
        {
            robot.addToCheckpointsVisited(this);
            robot.setCheckpointToken(checkNum);
            CheckPointReached checkPointReached = new CheckPointReached(robot.getRobotPlayer().getPlayerID(), checkNum);
            robot.getCurrentGame().getGameServer().broadcast(checkPointReached);
        }else{
            logger.info("Checkpoint was not added, because another needs to be reached");
        }

    }
    }
    
    @Override
    public ReducedField reduce()
    {
        return new ReducedCheckPoint(getBoardID(), checkNum);
    }
    
    public int isMovedThisRound()
    {
        return timesMovedThisRound;
    }
    
    public void setMovedThisRound(int movedThisRound)
    {
        this.timesMovedThisRound = movedThisRound;
    }
    
    public void addMovedCounter()
    {
        timesMovedThisRound++;
    }
}
