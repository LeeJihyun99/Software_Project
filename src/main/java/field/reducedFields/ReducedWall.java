package field.reducedFields;

import java.util.ArrayList;

public class ReducedWall extends ReducedField{

    ArrayList<String> orientations;

    public ReducedWall( String isOnBoard, ArrayList<String> orientations) {
        super("Wall", isOnBoard);
        this.orientations = orientations;
    }

    public ArrayList<String> getOrientations() {
        return orientations;
    }
}
