package field.tools;

import field.Field;
import field.reducedFields.ReducedField;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Klasse, in welcher die Methode zum converten von Boards ausgelagert sind
 * @author DavidKulbe
 */
public class BoardConverter
{
    /**
     * Wandelt ein Board zu einem "reduzierten" Board um, welches nur für das Protokoll relevante Informationen über die Felder enthält
     * @return reduziertes Board, bestehend aus ArrayList<ReducedField>[][]
     * @author DavidKulbe
     */
    public ArrayList<ReducedField>[][] convertToReducedBoard(@NotNull Board originalBoard)
    {
        ArrayList<ReducedField>[][] reducedBoard = new ArrayList[originalBoard.getRowCount()][originalBoard.getColumnCount()];
        for (int x = 0; x < originalBoard.getColumnCount(); x++)
        {
            for (int y = 0; y < originalBoard.getRowCount(); y++)
            {
                FieldList currentFieldList = originalBoard.getBoard()[y][x];
                ArrayList<ReducedField> reducedFields = new ArrayList<>();
                if (currentFieldList != null)
                {
                    for(Field field: currentFieldList.getFields())
                    {
                        reducedFields.add(field.reduce());
                    }
                }
                reducedBoard[y][x] = reducedFields;
            }
        }
        return reducedBoard;
    }
    
    /**
     * Nimmt ein Spielfeld, reduziert es und wandelt dieses in eine verschachtelte List der Form x<y<Fields<ReducedField>>> um
     * @param originalBoard zu reduzierendes Board
     * @return reduzierte Liste
     * @author DavidKulbe
     */
    public ArrayList<ArrayList<ArrayList<ReducedField>>> convertToReducedList(@NotNull Board originalBoard)
    {
        ArrayList<ReducedField>[][] reducedBoard = convertToReducedBoard(originalBoard);
        ArrayList<ArrayList<ArrayList<ReducedField>>> reducedList = new ArrayList<>();
        for (int x = 0; x < originalBoard.getColumnCount(); x++)
        {
            ArrayList<ArrayList<ReducedField>> reducedListAtY = new ArrayList<>();
            for (int y = 0; y < originalBoard.getRowCount(); y++)
            {
                reducedListAtY.add(reducedBoard[y][x]);
            }
            reducedList.add(reducedListAtY);
        }
        return reducedList;
    }
}
