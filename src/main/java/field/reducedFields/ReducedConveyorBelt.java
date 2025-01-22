package field.reducedFields;

import java.util.ArrayList;

public class ReducedConveyorBelt extends ReducedField{
    private int speed;
    private ArrayList <String> orientations;

    public ReducedConveyorBelt(String isOnBoard, int speed, ArrayList<String> orientations) {
        super("ConveyorBelt", isOnBoard);
        this.speed = speed;
        this.orientations = orientations;
    }

    public ArrayList<String> getOrientations() {
        return orientations;
    }

    public int getSpeed() {
        return speed;
    }
}
