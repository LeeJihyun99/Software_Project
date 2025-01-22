package field.tools;

/**
 * Gibt die Richtung eines Feldes oder einer Aktion an
 * @author DavidKulbe, Melli
 */
public class Direction {
    /**
     * 0 = top
     * 1 = right
     * 2 = bottom
     * 3 = left
     */
    private int directionInteger;
    
    private String directionString;
    
    public Direction(int direction)
    {
        this.directionInteger = direction;
        switch (direction)
        {
            case (0) -> directionString = "top";
            case (1) -> directionString = "right";
            case (2) -> directionString = "bottom";
            case (3) -> directionString = "left";
        }
    }
    public Direction(String direction)
    {
        this.directionString = direction;
        switch (direction)
        {
            case ("top") -> directionInteger = 0;
            case ("right") -> directionInteger = 1;
            case ("bottom") -> directionInteger = 2;
            case ("left") -> directionInteger = 3;
        }
    }

    /**
     * Berechnet basierend auf der Richtung eines Felds oder einer Aktion und der Position des Akteurs eine neue Position.
     * Neue Position muss überprüft werden, ob diese gültig/inbounds ist
     * @param position alte Position
     * @param actionWeight Gewicht einer Aktion (momentan, wie viele Felder etwas bewegt werden soll)
     * @return neue Position
     * @author David
     */
    public Position calculateNewPositionOfAction(Position position, int actionWeight)
    {
        switch (directionInteger)
        {
            case (0): return new Position(position.x(), position.y()-actionWeight);
            case (1): return new Position(position.x()+actionWeight, position.y());
            case (2): return new Position(position.x(), position.y()+actionWeight);
            case (3): return new Position(position.x()-actionWeight, position.y());
            default: return position;
        }
    }



    public int getDirectionInteger() {
        return directionInteger;
    }

    public void setDirectionInteger(int direction) {
        this.directionInteger = direction;
        //Update
        switch (direction)
        {
            case (0) -> directionString = "top";
            case (1) -> directionString = "right";
            case (2) -> directionString = "bottom";
            case (3) -> directionString = "left";
        }
        
    }
    
    public String getDirectionString() {
        return directionString;
    }
    
    public void setDirectionString(String direction) {
        this.directionString = direction;
        //Update
        switch (direction)
        {
            case ("top") -> directionInteger = 0;
            case ("right") -> directionInteger = 1;
            case ("bottom") -> directionInteger = 2;
            case ("left") -> directionInteger = 3;
        }
    }

    public String changeDirectionCounterClockwise(){
        directionInteger = (directionInteger + 3) % 4;
        switch (directionInteger){
            case (0) -> directionString = "top";
            case (1) -> directionString = "right";
            case (2) -> directionString = "bottom";
            case (3) -> directionString = "left";
        }
        return directionString;
    }

    public String changeDirectionClockwise(){
        directionInteger = (directionInteger + 1) % 4;
        switch (directionInteger){
            case (0) -> directionString = "top";
            case (1) -> directionString = "right";
            case (2) -> directionString = "bottom";
            case (3) -> directionString = "left";
        }
        return directionString;
    }
    
    public Direction getOppositeDirection()
    {
        return new Direction((directionInteger + 2) % 4);
    }
    
    public Position getNextPositionInDirection(int x, int y)
    {
        switch (directionInteger)
        {
            case (0) -> {return new Position(x,y-1);}
            case (1) -> {return new Position(x+1,y);}
            case (2) -> {return new Position(x,y+1);}
            case (3) -> {return new Position(x-1,y);}
        }
        return null;
    }
}
