package field.reducedFields;

public class ReducedEnergySpace extends ReducedField{
    private int count;
    public ReducedEnergySpace(String isOnBoard, int count) {
        super("EnergySpace", isOnBoard);
        this.count = count;
    }

    public int getCount() {
        return count;
    }


}
