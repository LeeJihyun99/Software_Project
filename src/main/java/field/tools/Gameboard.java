package field.tools;

import field.*;
import game.Game;
import game.Robot;
import org.jetbrains.annotations.NotNull;
import tools.ServerLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Klasse mit dem Spielfeld
 * @author DavidKulbe
 */
public class Gameboard extends Board{
    
    private Antenna antennaOnBoard;
    private RebootToken rebootTokenOnBoard;
    
    private Game currentGame;
    
    ArrayList<String> courseIDs = new ArrayList<>(); //list of all ids of the courses
    
    private HashMap<String, RebootToken> rebootTokens = new HashMap<String, RebootToken>();

   
    //private static final Logger logger = ServerLogger.getLogger();


    public Gameboard(int sizeX, int sizeY)
    {
        super(sizeX, sizeY);
    }

    public FieldList[] getNeighbors(Position position) {
        FieldList bottom = getFieldsAtPosition(new Position(position.x(), position.y() + 1)); //South
        FieldList left = getFieldsAtPosition(new Position(position.x() - 1, position.y())); //East
        FieldList up = getFieldsAtPosition(new Position(position.x(), position.y() - 1)); //North
        FieldList right = getFieldsAtPosition(new Position(position.x() + 1, position.y())); //West
        return new FieldList[] {up, right, bottom, left};
    }

    public void rebootRobotAtToken(Robot robot)
    {
        robot.reboot();
    }

    public Antenna getAntennaOnBoard() {
        return antennaOnBoard;
    }

    public void setAntennaOnBoard(Antenna antennaOnBoard) {
        this.antennaOnBoard = antennaOnBoard;
    }

    public RebootToken getRebootTokenOnBoard(String id) {
        if(rebootTokens.size() == 0)
        {
            clusterIntoCourses();
        }
        return rebootTokens.get(id);
    }

    public void setRebootTokenOnBoard() {
        clusterIntoCourses();
    }
    
    @Override
    protected void initializeFieldAtPosition(@NotNull Position position)
    {
        if(getBoard()[position.y()][position.x()] == null)
        {
            getBoard()[position.y()][position.x()] = new FieldList();
            getBoard()[position.y()][position.x()].setGameboard(this);
        }
    }
    
    public void scanAndSetAntennaOnBoard()
    {
        for (int x = 0; x<getColumnCount(); x++)
        {
            for (int y = 0; y < getRowCount(); y++)
            {
                if (getBoard()[y][x] != null)
                {
                    for (Field field : getBoard()[y][x].getFields())
                    {
                        if (field instanceof Antenna)
                        {
                            setAntennaOnBoard((Antenna) field);
                        }
                    }
                }
            }
        }
    }
    
    public void setCurrentGame(Game game)
    {
        currentGame = game;
    }
    public Game getCurrentGame()
    {
        return currentGame;
    }
    
    
    /**
     * Clustert das Gameboard in die einzelnen Kurse, indem es den Feldern ihre BoardID zuweist
     * @author David
     */
    public void clusterIntoCourses()
    {
        if(getName() != null)
        {
            switch (getName())
            {
                case ("DizzyHighway"):
                {
                    setBoardIDCluster(0, 2, 0, 9, "A");
                    setBoardIDCluster(3, 12, 0, 9, "5B");
                    rebootTokens.put("5B", getBoard()[3][7].getRebootToken());
                    courseIDs.add("5B");
                    break;
                }
        
                case ("ExtraCrispy"):
                {
                    setBoardIDCluster(0, 2, 0, 9, "A");
                    setBoardIDCluster(3, 12, 0, 9, "4A");
                    rebootTokens.put("4A", getBoard()[0][0].getRebootToken());
                    rebootTokens.put("A", getBoard()[0][0].getRebootToken());
                    courseIDs.add("4A");
                    break;
                }
        
                case ("LostBearings"):
                {
                    setBoardIDCluster(0, 2, 0, 9, "A");
                    setBoardIDCluster(3, 12, 0, 9, "1A");
                    rebootTokens.put("1A", getBoard()[0][0].getRebootToken());
                    courseIDs.add("1A");
                    break;
                }
        
                case ("DeathTrap"):
                {
                    setBoardIDCluster(0, 9, 0, 9, "2A");
                    setBoardIDCluster(10, 12, 0, 9, "A");
                    rebootTokens.put("2A", getBoard()[9][12].getRebootToken());
                    rebootTokens.put("A", getBoard()[9][12].getRebootToken());
                    courseIDs.add("2A");
                    break;
                }

                case ("Twister"):
                {
                    setBoardIDCluster(0, 2, 0, 9, "A");
                    setBoardIDCluster(3, 12, 0, 9, "6B");
                    rebootTokens.put("6B", getBoard()[7][0].getRebootToken());
                    courseIDs.add("6B");
                    break;
                }
                
                case ("Training"):
                {
                    setBoardIDCluster(3,12,0,9, "2A"); //Death Trap
                    setBoardIDCluster(13,22,0,9, "4A"); //Extra Crispy
                    setBoardIDCluster(0,2,10,19, "A"); //Startboard
                    setBoardIDCluster(3,12,10,19, "5B"); //Dizzy Highway
                    setBoardIDCluster(13,22,10,19, "1A"); //Lost Bearings
                    
                    //reboot token deathtrap
                    Position positionRebootTokenDeathTrap = new Position(12,9);
                    addSingleFieldAtPosition(new FieldGenerator().generateRebootToken(positionRebootTokenDeathTrap,new Direction(3)), positionRebootTokenDeathTrap);
                    rebootTokens.put("2A", getBoard()[positionRebootTokenDeathTrap.y()][positionRebootTokenDeathTrap.x()].getRebootToken());
                    //reboot token extra crispy
                    Position positionOfRebootTokenExtraCrispy = new Position(13,0);
                    addSingleFieldAtPosition(new FieldGenerator().generateRebootToken(positionOfRebootTokenExtraCrispy,new Direction(1)), positionOfRebootTokenExtraCrispy);
                    rebootTokens.put("4A", getBoard()[positionOfRebootTokenExtraCrispy.y()][positionOfRebootTokenExtraCrispy.x()].getRebootToken());
                    //reboot token dizzy highway
                    rebootTokens.put("5B", getBoard()[13][7].getRebootToken());
                    //reboot token lost bearings
                    Position positionOfRebootTokenLostBearings = new Position(13,10);
                    addSingleFieldAtPosition(new FieldGenerator().generateRebootToken(positionOfRebootTokenLostBearings,new Direction(1)), positionOfRebootTokenLostBearings);
                    rebootTokens.put("1A", getBoard()[positionOfRebootTokenLostBearings.y()][positionOfRebootTokenLostBearings.x()].getRebootToken());
                }
            }
        }
        else //eigenes Spielfeld ohne einen Namen
        {
            setBoardIDCluster(0, getColumnCount()-1, 0, getRowCount()-1, "Default");
            RebootToken rebootToken = null;
            for (int x = 0; x<getColumnCount(); x++)
            {
                for (int y = 0; y < getRowCount(); y++)
                {
                    if (getBoard()[y][x] != null)
                    {
                        if (getBoard()[y][x].hasRebootToken())
                        {
                            rebootToken = getBoard()[y][x].getRebootToken();
                        }
                    }
                }
            }
            rebootTokens.put("Default", rebootToken);
            courseIDs.add("Default");
        }
    }
    
    /**
     * Sets the index in an interval (start and end bounds are inclusive)
     * @author David
     */
    private void setBoardIDCluster(int x_start, int x_end,int y_start, int y_end,String id)
    {
        for (int x = x_start; x<=x_end; x++)
        {
            for (int y = y_start; y <= y_end; y++)
            {
                if (getBoard()[y][x] != null)
                {
                    getBoard()[y][x].setIsOnBoard(id);
                }
            }
        }
    }
    
    /**
     * Gibt eine Liste aller RebootTokens aus
     * @author David
     */

    public ArrayList<RebootToken> getRebootToken()
    {
        ArrayList<RebootToken> rebootTokens = new ArrayList<>();
        for (Field field: getListOfFieldType("rebootToken"))
        {
            rebootTokens.add((RebootToken) field);
        }
        return rebootTokens;
    }
    
    public ArrayList<BoardLaser> getLasers()
    {
        ArrayList<BoardLaser> laser = new ArrayList<>();
        for (Field field: getListOfFieldType("boardlaser"))
        {
            laser.add((BoardLaser) field);
        }
        return laser;
    }
    
    public ArrayList<ConveyorBelt> getBlueConveyorBelts()
    {
        ArrayList<ConveyorBelt> conveyorBelts = new ArrayList<>();
        for (Field field: getListOfFieldType("conveyorbelt"))
        {
            ConveyorBelt belt = (ConveyorBelt) field;
            if (belt.getSpeed() == 2)
            {
                conveyorBelts.add(belt);
            }
        }
        return conveyorBelts;
    }
    
    public ArrayList<ConveyorBelt> getGreenConveyorBelts()
    {
        ArrayList<ConveyorBelt> conveyorBelts = new ArrayList<>();
        for (Field field: getListOfFieldType("conveyorbelt"))
        {
            ConveyorBelt belt = (ConveyorBelt) field;
            if (belt.getSpeed() == 1)
            {
                conveyorBelts.add(belt);
            }
        }
        return conveyorBelts;
    }
    
    public ArrayList<PushPanel> getPushPanels()
    {
        ArrayList<PushPanel> fields = new ArrayList<>();
        for (Field field: getListOfFieldType("pushpanel"))
        {
            fields.add((PushPanel) field);
        }
        return fields;
    }
    
    public ArrayList<Gear> getGears()
    {
        ArrayList<Gear> fields = new ArrayList<>();
        for (Field field: getListOfFieldType("gear"))
        {
            fields.add((Gear) field);
        }
        return fields;
    }
    
    public ArrayList<EnergySpace> getEnergySpaces()
    {
        ArrayList<EnergySpace> fields = new ArrayList<>();
        for (Field field: getListOfFieldType("energyspace"))
        {
            fields.add((EnergySpace) field);
        }
        return fields;
    }
    
    public ArrayList<CheckPoint> getCheckPoints()
    {
        ArrayList<CheckPoint> checkpoints = new ArrayList<>();
        for (Field field: getListOfFieldType("checkpoint"))
        {
            checkpoints.add((CheckPoint) field);
        }
        return checkpoints;
    }

    public CheckPoint getCheckPointByNumber(int number)
    {
        for(CheckPoint checkPoint: getCheckPoints())
        {
            if (checkPoint.getCheckNum() == number)
            {
                return checkPoint;
            }
        }
        return null;
    }
    
    /**
     * Gibt eine Liste aller Felder eines Typus aus
     * @param fieldType Name des Feldes
     * @return Liste, mit Field type (muss noch gecastet werden)
     * @author David
     */
    private ArrayList<Field> getListOfFieldType(String fieldType)
    {
        ArrayList<Field> fields = new ArrayList<>();
        for (int x = 0; x<getColumnCount(); x++)
        {
            for (int y = 0; y < getRowCount(); y++)
            {
                if (getBoard()[y][x] != null)
                {
                    switch (fieldType)
                    {
                        case ("conveyorbelt"):
                        {
                            if (getBoard()[y][x].hasConveyorBelt())
                            {
                                fields.add(getBoard()[y][x].getConveyorBelt());
                            }
                            break;
                        }
                        case ("boardlaser"):
                        {
                            if (getBoard()[y][x].hasLaser())
                            {
                                fields.add(getBoard()[y][x].getLaser());
                            }
                            break;
                        }
                        case ("pushpanel"):
                        {
                            if (getBoard()[y][x].hasPushPanel())
                            {
                                fields.add(getBoard()[y][x].getPushPanel());
                            }
                            break;
                        }
                        case ("gear"):
                        {
                            if (getBoard()[y][x].hasGear())
                            {
                                fields.add(getBoard()[y][x].getGear());
                            }
                            break;
                        }
                        case ("energyspace"):
                        {
                            if (getBoard()[y][x].hasEnergySpace())
                            {
                                fields.add(getBoard()[y][x].getEnergySpace());
                            }
                            break;
                        }
                        case ("checkpoint"):
                        {
                            if (getBoard()[y][x].hasCheckPoint())
                            {
                                fields.add(getBoard()[y][x].getCheckPoint());
                            }
                            break;
                        }
                        case ("rebootToken"):
                        {
                            if (getBoard()[y][x].hasRebootToken())
                            {
                                fields.add(getBoard()[y][x].getRebootToken());
                            }
                            break;
                        }
                    }
                }
            }
        }
        return fields;
    }

    /**
     * Gibt an, ob eine ID ein Key für ein RebootToken ist
     */
    public boolean isKeyForTokens(String key)
    {
        return rebootTokens.containsKey(key);
    }
    
    public boolean hasOccupiedConveyorBelt()
    {
        for (ConveyorBelt conveyorBelt: getBlueConveyorBelts())
        {
            if (conveyorBelt.getParentList().isOccupied())
            {
                return true;
            }
        }
        for (ConveyorBelt conveyorBelt: getGreenConveyorBelts())
        {
            if (conveyorBelt.getParentList().isOccupied())
            {
                return true;
            }
        }
        return false;
    }
    
}
