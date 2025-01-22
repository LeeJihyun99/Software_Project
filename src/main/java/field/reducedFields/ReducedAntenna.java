package field.reducedFields;

import java.util.ArrayList;

public class ReducedAntenna extends ReducedField{
    private ArrayList<String> orientations;

    public ReducedAntenna(String isOnBoard, ArrayList<String> orientations) {
        super("Antenna", isOnBoard);
        this.orientations = orientations;
    }

    public ArrayList<String> getOrientations() {
        return orientations;
    }

}
