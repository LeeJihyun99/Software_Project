package field.reducedFields;

public abstract class ReducedField {
    private String type;
    private String isOnBoard;

    public ReducedField(String type, String isOnBoard){
        this.type = type;
        this.isOnBoard = isOnBoard;
    }

    public String getType(){
        return type;
    }

    public String getIsOnBoard() {
        return isOnBoard;
    }
}
