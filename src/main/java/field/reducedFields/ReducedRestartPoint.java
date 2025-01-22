package field.reducedFields;

import java.util.ArrayList;

public class ReducedRestartPoint extends ReducedField{
    private ArrayList<String> orientations;
    public ReducedRestartPoint(String isOnBoard, ArrayList<String> orientations){
        super("RestartPoint", isOnBoard);
        this.orientations = orientations;
    }
    
    public ArrayList<String> getOrientations()
    {
        return orientations;
    }
}
