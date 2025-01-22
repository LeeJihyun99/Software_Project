package field.reducedFields;

public class ReducedCheckPoint extends ReducedField{
    private int count;
    public ReducedCheckPoint(String isOnBoard, int count) {
        super("CheckPoint", isOnBoard);
        this.count = count;
    }

    public int getCount() {
        return count;
    }

}
