package field.reducedFields;

import java.util.ArrayList;

public class ReducedGear extends ReducedField{

    private ArrayList <String> orientations;

    public ReducedGear(String isOnBoard, ArrayList<String> orientations) {
        super("Gear", isOnBoard);
        this.orientations = orientations;
    }

    public ArrayList<String> getOrientations() {
        return orientations;
    }


}
