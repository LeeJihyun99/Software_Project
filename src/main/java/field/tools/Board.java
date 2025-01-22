package field.tools;

import field.DefaultField;
import field.Field;
import field.reducedFields.ReducedField;
import org.jetbrains.annotations.NotNull;
import tools.ClientLogger;
import tools.ServerLogger;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Enthält das Grundgerüst, für alle Spielbretter, welche eine Sammlung an Feldern enthalten
 * @author DavidKulbe
 */
abstract public class Board
{
    /**
     * Array, in welchem das Spielfeld gespeichert wird
     * Referenziert über [y][x], damit y die Reihen und x die Spalten ist
     */
    private FieldList[][] board;
    private int columnCount;
    private int rowCount;
    
    private String name;
    
    private ArrayList<Position> positionsOfAvailableStartingPoints = new ArrayList<>();
    
    private final Logger logger = ServerLogger.getLogger();
    
    public Board(int sizeX, int sizeY)
    {
        board = new FieldList[sizeY][sizeX];
        columnCount = board[0].length;
        rowCount = board.length;
    }
    
    public boolean hasDefaultField(FieldList fieldList)
    {
        for (Field field: fieldList.getFields())
        {
            if (field instanceof DefaultField)
            {
                return true;
            }
        }
        return false;
    }
    
    public void resetFieldAtPosition(Position position)
    {
        board[position.y()][position.x()] = null;
    }
    
    /**
     * Fügt ein einzelnes Feld einer Position zu
     * @param field Hinzufügendes Feld
     * @param position Position
     * @author DavidKulbe
     */
    public void addSingleFieldAtPosition(Field field, @NotNull Position position)
    {
        if(isIndexValid(position.x(), position.y())) {
            initializeFieldAtPosition(position);
            if(!(field instanceof DefaultField && hasDefaultField(board[position.y()][position.x()])))
            {
                board[position.y()][position.x()].addField(field);
            }
        }
        else logger.severe("Index ungültig: x: " + position.x() +" y: " +  position.y());
    }
    
    /**
     * Erlaubt es Felder an einer Position hinzuzufügen. Erlaubt nur gültige Indizes.
     * @param fields Die hinzuzufügenden Felder
     * @param position Die betroffende Position
     * @author David Kulbe
     */
    public void addFieldsAtPosition(FieldList fields, @NotNull Position position)
    {
        if(isIndexValid(position.x(), position.y())) {
            initializeFieldAtPosition(position);
            board[position.y()][position.x()].addFieldArrayList(fields.getFields());
        }
        else logger.severe("Index ungültig: x: " + position.x() +" y: " +  position.y());
    }
    
    public FieldList getFieldsAtPosition(@NotNull Position position) {
        if(isIndexValid(position.x(), position.y())) {return board[position.y()][position.x()];}
        else return null;
    }
    
    /**
     * Checkt, ob ein Index in den Grenzen des Spielfeldes liegt
     * @param x x Koordinate
     * @param y y Koordinate
     * @return Ob der Index gültig ist (in-bounds ist)
     * @author DavidKulbe
     */
    public boolean isIndexValid(int x, int y)
    {
        return (x >= 0 && x<columnCount && y >= 0 && y < rowCount);
    }
    public int getColumnCount() {
        return columnCount;
    }
    
    public int getRowCount() {
        return rowCount;
    }
    
    
    public void initializeWithDefaultField()
    {
        FieldGenerator fieldGenerator = new FieldGenerator();
        for (int y=0; y<rowCount; y++)
        {
            for (int x=0; x<columnCount; x++)
            {
                Position newPosition = new Position(x,y);
                initializeFieldAtPosition(newPosition);
                board[y][x].addField(fieldGenerator.generateDefaultField(newPosition));
            }
        }
    }
    
    
    abstract void initializeFieldAtPosition(@NotNull Position position);
    public FieldList[][] getBoard() {
        return board;
    }
    
    public void setBoard(FieldList[][] board) {
        this.board = board;
    }
    
    /**
     * Wandelt ein Board zu einem "reduzierten" Board um, welches nur für das Protokoll relevante Informationen über die Felder enthält
     * @return
     */
    public ArrayList<ReducedField>[][] convertToReducedBoard()
    {
        return new BoardConverter().convertToReducedBoard(this);
    }
    
    public ArrayList<ArrayList<ArrayList<ReducedField>>> convertToReducedList()
    {
        return new BoardConverter().convertToReducedList(this);
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void initializeStartingPoints()
    {
        for (int y=0; y<rowCount; y++)
        {
            for (int x=0; x<columnCount; x++)
            {
                Position currentPosition = new Position(x,y);
                if (getFieldsAtPosition(currentPosition) != null)
                {
                    if (getFieldsAtPosition(currentPosition).hasStartingPoint())
                    {
                        positionsOfAvailableStartingPoints.add(currentPosition);
                    }
                }
            }
        }
    }
    
    public ArrayList<Position> getPositionsOfAvailableStartingPoints()
    {
        return positionsOfAvailableStartingPoints;
    }
    
    public void removePositionFromAvailableStartingPoints(Position position)
    {
        positionsOfAvailableStartingPoints.remove(position);
    }
    
}
