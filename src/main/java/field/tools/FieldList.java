package field.tools;

import field.*;
import game.Robot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Diese Klasse kümmert sich darum, dass mehrere Felder übereinander gespeichert werden können und behandelt alle Abfragen an diese Liste
 * Zudem wird einem ermöglicht diese Felder zu besetzen
 * @author DavidKulbe
 */
public class FieldList {


    private ArrayList<Field> fields = new ArrayList<>();
    private boolean occupied;
    private Robot currentRobot;
    private String isOnBoard;
    
    
    private Gameboard gameboard;
    
    public FieldList(String boardID)
    {
        isOnBoard = boardID;
    }
    
    public FieldList()
    {
    }

    public void initializeFields()
    {
        fields = new ArrayList<>();
    }

    public void clear()
    {
        fields.clear();
    }

    public void setFieldsNull()
    {
        fields = null;
    }

    public void removeField(Field field)
    {
        fields.remove(field);
    }

    public void addField(Field field)
    {
        if(fields == null)
        {
            initializeFields();
        }
        fields.add(field);
        field.setParentList(this);
    }

    public void addFieldArrayList(@NotNull ArrayList<Field> fields)
    {
        for(Field field: fields)
        {
            addField(field);
        }
    }

    public ArrayList<Field> getFields() {
        return fields;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public Robot getCurrentRobot() {
        return currentRobot;
    }

    public void setCurrentRobot(Robot currentRobot) {
        this.currentRobot = currentRobot;
    }
    
    public boolean hasFieldType(String typeName)
    {
        for (Field field: fields)
        {
            if(field.getName().equals(typeName))
            {
                return true;
            }
        }
        return false;
    }
    
    public Field getFieldType(String typeName)
    {
        if(hasFieldType(typeName))
        {
            for(Field field: fields)
            {
                if(field.getName().equals(typeName))
                {
                    return field;
                }
            }
        }
        return null;
    }

    public boolean hasConveyorBelt()
    {
        return getConveyorBelt() != null;
    }
    
    public ConveyorBelt getConveyorBelt()
    {
        return (ConveyorBelt) getFieldType("conveyorbelt");
    }

    public boolean hasWall()
    {
        return getWall() != null;
    }

    public Wall getWall()
    {
        return (Wall) getFieldType("wall");
    }
    
    public boolean hasPit()
    {
        return getWall() != null;
    }
    
    public Pit getPit()
    {
        return (Pit) getFieldType("pit");
    }
    
    public boolean hasRebootToken()
    {
        return getRebootToken() !=null ;
    }
    
    public RebootToken getRebootToken()
    {
        return (RebootToken) getFieldType("rebootToken");
    }
    
    public BoardLaser getLaser()
    {
        return (BoardLaser) getFieldType("boardlaser");
    }
    
    public boolean hasLaser()
    {
        return getLaser() != null;
    }
    
    public PushPanel getPushPanel()
    {
        return (PushPanel) getFieldType("pushpanel");
    }
    
    public boolean hasPushPanel()
    {
        return getPushPanel() != null;
    }
    
    public Gear getGear()
    {
        return (Gear) getFieldType("gear");
    }
    
    public boolean hasGear()
    {
        return getGear() != null;
    }
    
    public EnergySpace getEnergySpace()
    {
        return (EnergySpace) getFieldType("energyspace");
    }
    
    public boolean hasEnergySpace()
    {
        return getEnergySpace() != null;
    }
    
    public CheckPoint getCheckPoint()
    {
        return (CheckPoint) getFieldType("checkpoint");
    }
    
    public boolean hasCheckPoint()
    {
        return getCheckPoint() != null;
    }
    
    public boolean hasStartingPoint()
    {
        return hasFieldType("startpoint");
    }

    public boolean isPassable()
    {
        boolean isPassable = true;
        for(Field field: fields)
        {
            isPassable = field.getPassThrough();
            if (!isPassable) break;
        }
        return isPassable;
    }
    
    public void setIsOnBoard(String boardID)
    {
        isOnBoard = boardID;
    }
    
    public String getIsOnBoard()
    {
        return isOnBoard;
    }
    public Gameboard getGameboard()
    {
        return gameboard;
    }
    
    public void setGameboard(Gameboard gameboard)
    {
        this.gameboard = gameboard;
    }
}
