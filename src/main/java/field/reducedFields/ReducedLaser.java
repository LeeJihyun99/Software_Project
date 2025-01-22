package field.reducedFields;

import java.util.ArrayList;

public class ReducedLaser extends ReducedField{
    private ArrayList<String> orientations;
    private int count;

    public ReducedLaser(String isOnBoard, ArrayList<String> orientations, int count) {
        super("Laser", isOnBoard);
        this.orientations = orientations;
        this.count = count;
    }

    public ArrayList<String> getOrientations() {
        return orientations;
    }

    public int getCount() {
        return count;
    }


}
