package field.reducedFields;

import java.util.ArrayList;

public class ReducedPushPanel extends ReducedField{
    private ArrayList<String> orientations;
    private ArrayList<Integer> registers;

    public ReducedPushPanel(String isOnBoard, ArrayList<String> orientations, ArrayList<Integer>registers) {
        super("PushPanel", isOnBoard);
        this.orientations = orientations;
        this.registers = registers;
    }

    public ArrayList<String> getOrientations() {
        return orientations;
    }

    public ArrayList<Integer> getRegisters() {
        return registers;
    }

}
