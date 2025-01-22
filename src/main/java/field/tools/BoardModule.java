package field.tools;

import org.jetbrains.annotations.NotNull;

/**
 * Enthält ein Grundgerüst, für alle Spielbretter, welche einem Modul entsprechen (Kurse und Startfelder)
 * @author DavidKulbe
 */
abstract public class BoardModule extends Board
{
    private final String boardID;
    public BoardModule(int sizeX, int sizeY,String boardID)
    {
        super(sizeX,sizeY);
        this.boardID = boardID;
    }
    
    public String getBoardID()
    {
        return boardID;
    }
    
    @Override
    /**
     * Initalisiert ein Feld mit einem neuen FieldList Objekt, falls die Position null war
     * @param position Position
     * @author DavidKulbe
     */
    protected void initializeFieldAtPosition(@NotNull Position position)
    {
        if(getBoard()[position.y()][position.x()] == null)
        {
            getBoard()[position.y()][position.x()] = new FieldList(boardID);
        }
    }
}
