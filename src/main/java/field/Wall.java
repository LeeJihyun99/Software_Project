package field;

import field.reducedFields.ReducedField;
import field.reducedFields.ReducedWall;
import field.tools.*;
import game.Robot;
import image.Image;

import java.util.ArrayList;

/**
 * Realisiert eine Wand
 * @author DavidKulbe
 */
public class Wall extends Field{
    private WallOrientation wallOrientation;
    public Wall(Position position, String name, boolean passThrough, Image image, WallOrientation wallOrientation) {
        super(position, name, passThrough, image);
        this.wallOrientation = wallOrientation;
    }

    //TODO testen

    /**
     * Rechnet aus, ob es eine Kollision gibt, wenn man sich auf ein Feld mit einer Wand bewegt
     * @param gameboard Spielbrett
     * @param direction Richtung, in welche der Spieler sich bewegen will
     * @return Ob es eine Kollision gibt
     * @author DavidKulbe
     */
    public boolean calculateCollision(Gameboard gameboard, Direction direction)
    {
        FieldList[] neighbors = gameboard.getNeighbors(getPosition());
        switch (direction.getDirectionInteger()) {
            case (0) -> {
                for (Field field : neighbors[0].getFields()) {
                    if (field instanceof Wall) {
                        return wallOrientation.top() || ((Wall) field).wallOrientation.bottom();
                    }
                } return wallOrientation.top();
            }
            case (1) -> {
                for (Field field : neighbors[1].getFields()) {
                    if (field instanceof Wall) {
                        return wallOrientation.right() || ((Wall) field).wallOrientation.left();
                    }
                }
                return wallOrientation.right();
            }
            case (2) -> {
                for (Field field: neighbors[2].getFields()) {
                    if (field instanceof Wall) {
                        return wallOrientation.bottom() || ((Wall) field).wallOrientation.top();
                    }
                }
                return wallOrientation.bottom();
            }
            case (3) -> {
                for (Field field : neighbors[3].getFields()) {
                        if (field instanceof Wall) {
                            return wallOrientation.left() || ((Wall) field).wallOrientation.right();
                        }
                    }
                    return wallOrientation.left();
            }
        }
        return false;
    }
    
    public WallOrientation getWallOrientation()
    {
        return wallOrientation;
    }
    
    public Direction getSingleDirection()
    {
        int wallCounter = 0;
        int wallOrientationIndex = 0;
        for(int i = 0; i<4; i++)
        {
            if(wallOrientation.getOrientationVector()[i])
            {
                wallCounter++;
                wallOrientationIndex = i;
            }
        }
        if(wallCounter == 1)
        {
           return new Direction(wallOrientationIndex);
        }
        else
        {
            logger.info("Mehr als eine oder keine Wand gefunden, null wird zurückgegeben!");
            return null;
        }
    }
    
    public ArrayList<String> getOrientationsAsString()
    {
        ArrayList<String> wallNames = new ArrayList<>();
        for(int i = 0; i<4; i++)
        {
            if(wallOrientation.getOrientationVector()[i])
            {
                Direction direction = new Direction(i);
                wallNames.add(direction.getDirectionString());
            }
        }
        if (wallNames.size() == 0)
        {
            logger.info("Keine Wand gefunden!");
        }
        return wallNames;
    }
    
    /**
     * Gibt die gegenüberliegende Richtung der Mauer an
     */
    public Direction getOppositeDirection()
    {
        Direction oppositeDirection = new Direction(0);
        switch((this.getSingleDirection().getDirectionInteger()+2)%4) {
            case 0:
                oppositeDirection = new Direction(0);
                break;
            case 1:oppositeDirection = new Direction(1);
                break;
            case 2:oppositeDirection = new Direction(2);
                break;
            case 3:oppositeDirection = new Direction(3);
                break;
        }
        return oppositeDirection;
        
    }
    
    @Override
    public ReducedField reduce()
    {
        return new ReducedWall(getBoardID(), getOrientationsAsString());
    }
}
