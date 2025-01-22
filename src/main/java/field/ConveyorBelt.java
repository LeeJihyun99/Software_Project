package field;

import field.reducedFields.ReducedConveyorBelt;
import field.reducedFields.ReducedField;
import field.tools.*;
import game.Player;
import game.Robot;
import image.Image;
import server.protocol.aktionen.CheckpointMoved;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Förderbandfeld
 * @author DavidKulbe
 */
public class ConveyorBelt extends Field implements ActionOnActivation
{
    //private final int rotation;
    private final Direction flow_direction_out;
    private final ArrayList<Direction> flow_directions_in;
    private final int speed;
    private Thread thread;

    public ConveyorBelt(Position position, String name, boolean passThrough, Image image,  ArrayList<Direction> directions, int speed) {
        super(position, name, passThrough, image);
        //this.rotation = rotation;
        this.flow_direction_out = directions.get(0);
        this.speed = speed;
        directions.remove(0);
        this.flow_directions_in = directions; //Rest
    }

    @Override
    public void actionOnActivation() {
        CheckPoint checkPoint = getParentList().getCheckPoint();
        Robot robot = getParentList().getCurrentRobot();
        AtomicBoolean exit = new AtomicBoolean(false);
        thread = new Thread(() -> {
            if(robot != null)
            {
                if(getParentList().getGameboard().getName().equals("Twister"))
                {
                    moveCheckPoint(checkPoint);
                }
                if (!movingToSameField(robot))
                {
                    moveRobot(this, robot);
                    rotateRobot(this, robot);
                    if (speed == 2)
                    {
                        if(getParentList().getGameboard().getName().equals("Twister"))
                        {
                            moveCheckPoint(checkPoint);
                        }
                        moveOneFurther(robot);
                    }
                }
            }
            else if(getParentList().getGameboard().getName().equals("Twister"))
            {
                moveCheckPoint(checkPoint);
                try
                {
                    Thread.sleep(500);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                moveCheckPoint(checkPoint);
                try
                {
                    Thread.sleep(500);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
            else if(!getParentList().getGameboard().hasOccupiedConveyorBelt())
            {
                try
                {
                    Thread.sleep(1000);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        //thread.interrupt();
        exit.set(true);
        });
        thread.start();
        while(!exit.get())
        {
            Thread.onSpinWait();
        }
    }

    public void moveCheckPoint(CheckPoint checkPoint)
    {
        if (checkPoint != null)
        {
            if (checkPoint.isMovedThisRound() < 2)
            {
                Position newCheckPointPosition;
                getParentList().getFields().remove(checkPoint);
                newCheckPointPosition = getParentList().getGameboard().getFieldsAtPosition(checkPoint.getPosition()).getConveyorBelt().getFlow_direction_out().getNextPositionInDirection(checkPoint.getPosition().x(), checkPoint.getPosition().y());
                checkPoint.setPosition(newCheckPointPosition);
                getParentList().getGameboard().getFieldsAtPosition(newCheckPointPosition).addField(checkPoint);
                checkPoint.addMovedCounter();
                CheckpointMoved checkpointMovedMessage = new CheckpointMoved(checkPoint.getCheckNum(), checkPoint.getPosition().x(), checkPoint.getPosition().y());
                getParentList().getGameboard().getCurrentGame().getGameServer().broadcast(checkpointMovedMessage);
            }
        }
    }

    /**
     * Bewegt sich eins weiter, wenn das Feld, auf welchem man landet ein ConveyorBelt hat
     * @author David Kulbe
     */
    private void moveOneFurther(Robot robot)
    {
        boolean hasConveyorBelt = robot.getCurrentGame().getGameboard().getFieldsAtPosition(robot.getPosition()).hasConveyorBelt();
        if(hasConveyorBelt)
        {
            ConveyorBelt newConveyorBelt = robot.getCurrentGame().getGameboard().getFieldsAtPosition(robot.getPosition()).getConveyorBelt();
            if(newConveyorBelt != null)
            {
                moveRobot(newConveyorBelt, robot);
                rotateRobot(newConveyorBelt, robot);
            }
        }
    }
    
    /**
     * @param robot
     * Rotate Robot according to Flow_Out and Flow_In Directions
     * @author David
     */
    public void rotateRobot(ConveyorBelt conveyorBelt, Robot robot)
    {
        Position newPosition = robot.getPosition();
        Gameboard gameboard = getParentList().getGameboard();
        ConveyorBelt newConveyorBelt = gameboard.getFieldsAtPosition(newPosition).getConveyorBelt();
        if(newConveyorBelt != null)
        {
                if (newConveyorBelt.getFlow_direction_out().getDirectionInteger() != conveyorBelt.getFlow_direction_out().getDirectionInteger())
                {
                    if (newConveyorBelt.getFlow_direction_out().getDirectionInteger() == (conveyorBelt.flow_direction_out.getDirectionInteger() + 1) % 4)
                    {
                        robot.turn(1); //rotation right
                    }
                    //rotation left
                    else
                    {
                        robot.turn(-1);
                    }
                }
        }
    }
    /**
     * Checks if two robots would end their move (conveyor belt) on the same conveyor belt space
     * @return boolean true = same field
     * @author David
     */
    public boolean movingToSameField(Robot robot)
    {
        Gameboard gameboard = getParentList().getGameboard();
        //initialise lists
        List<Player> playerList = gameboard.getCurrentGame().getPlayerQueue();
        ArrayList<Robot> otherRobots = new ArrayList<>();
        for (Player p: playerList)
        {
            otherRobots.add(p.getRobot());
        }
        otherRobots.remove(robot);
        
        //calculate new position of this roboter
        Position newPositionThisRobot = calculateNewPosition(this,robot);
        
        //calculate new position for other robots and check them with your own
        for (Robot otherRobot: otherRobots)
        {
            Position robotsPosition = otherRobot.getPosition();
            //get its conveyor belt (null when not on a conveyor belt)
            ConveyorBelt itsConveyorBelt = getParentList().getGameboard().getFieldsAtPosition(robotsPosition).getConveyorBelt();
            if(itsConveyorBelt != null)
            {
                Position newPositionOtherRobot = calculateNewPosition(itsConveyorBelt,otherRobot);
                if(newPositionThisRobot.equals(newPositionOtherRobot) && gameboard.getFieldsAtPosition(newPositionOtherRobot).hasConveyorBelt() &&
                        gameboard.getFieldsAtPosition(newPositionThisRobot).hasConveyorBelt())
                {
                    return newPositionThisRobot.equals(newPositionOtherRobot); //same conveyor belt
                }
            }
        }
        
        return false; //no conflicts found
    }
    
    public Position calculateNewPosition(ConveyorBelt conveyorBelt, Robot robot)
    {
        Position newPositionThisRobot;
        if(speed == 2)
        {
            newPositionThisRobot = newPositionFlowOut(conveyorBelt, robot.getPosition());
            boolean hasConveyorBelt = robot.getCurrentGame().getGameboard().getFieldsAtPosition(newPositionThisRobot).hasConveyorBelt();
            if (hasConveyorBelt)
            {
                ConveyorBelt newConveyorBelt = robot.getCurrentGame().getGameboard().getFieldsAtPosition(newPositionThisRobot).getConveyorBelt();
                if (newConveyorBelt.getSpeed() == 2)
                {
                    newPositionThisRobot = newPositionFlowOut(newConveyorBelt, newPositionThisRobot);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        else
        {
            newPositionThisRobot = newPositionFlowOut(conveyorBelt,robot.getPosition());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return newPositionThisRobot;
    }
    
    
    public void moveRobot(ConveyorBelt conveyorBelt, Robot robot)
    {
        //wartet bis das nächste Feld nicht mehr belegt ist
       while(isNextFieldOccupiedAndHasConveyorBelt(conveyorBelt, robot)) {Thread.onSpinWait();}
        if(!isNextFieldOccupied(conveyorBelt, robot))
        {
            robot.move(1, conveyorBelt.flow_direction_out);
            
        }
        else if(isNextFieldOccupiedAndHasConveyorBelt(conveyorBelt, robot)) //falls das nächste conveyor belt doch wieder belegt sein sollte
        {moveRobot(conveyorBelt,robot);}
    }
    
    public boolean isNextFieldOccupiedAndHasConveyorBelt(ConveyorBelt conveyorBelt, Robot robot)
    {
        return isNextFieldOccupied(conveyorBelt, robot) && hasNextFieldConveyorBelt(conveyorBelt, robot);
    }
    
    /**
     * Calculates new position after the movement on the conveyor belt and checks whether it is a non conveyor belt
     * @return new position is not a conveyor belt
     * @author David
     */
    public boolean hasNextFieldConveyorBelt(ConveyorBelt conveyorBelt, Robot robot)
    {
        Position newPosition = newPositionFlowOut(conveyorBelt, robot.getPosition());
        
        if (getParentList().getGameboard().isIndexValid(newPosition.x(), newPosition.y()))
        {
            return getParentList().getGameboard().getFieldsAtPosition(newPosition).hasConveyorBelt();
        }
        else return false;
    }
    
    /**
     * Calculates new position after the movement on the conveyor belt and checks whether it is occupied
     * @return new position is occupied
     * @author David
     */
    public boolean isNextFieldOccupied(ConveyorBelt conveyorBelt, Robot robot)
    {
        Position newPosition = newPositionFlowOut(conveyorBelt, robot.getPosition());
        
        if (getParentList().getGameboard().isIndexValid(newPosition.x(), newPosition.y()))
        {
            return getParentList().getGameboard().getFieldsAtPosition(newPosition).isOccupied();
        }
        else return false;
    }
    
    /**
     * Returns new position after flow out
     * @author David
     */
    private Position newPositionFlowOut(ConveyorBelt conveyorBelt, Position robotsPosition)
    {
        Position newPosition;
        switch (conveyorBelt.flow_direction_out.getDirectionInteger())
        {
            case 0 -> newPosition = new Position(robotsPosition.x(),robotsPosition.y()-1); //oben
            case 1 -> newPosition = new Position(robotsPosition.x()+1,robotsPosition.y()); //rechts
            case 2 -> newPosition = new Position(robotsPosition.x(),robotsPosition.y()+1); //unten
            case 3 -> newPosition = new Position(robotsPosition.x()-1,robotsPosition.y()); //links
            default -> newPosition = robotsPosition; //tritt nicht ein
        }
        return  newPosition;
    }


    /*public int getRotation() {
        return rotation;
    }*/

    public Direction getFlow_direction_out() {
        return flow_direction_out;
    }
    
    public ArrayList<Direction> getFlow_directions_in(){return flow_directions_in;}
    
    @Override
    public ReducedField reduce()
    {
        ArrayList<String> orientations = new ArrayList<>();
        orientations.add(flow_direction_out.getDirectionString());
        for(Direction direction: flow_directions_in)
        {
            orientations.add(direction.getDirectionString());
        }
        return new ReducedConveyorBelt(getBoardID(),speed, orientations);
    }
    
    public int getSpeed()
    {
        return speed;
    }
}
