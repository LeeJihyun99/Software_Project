package field;

import field.reducedFields.ReducedAntenna;
import field.reducedFields.ReducedField;
import field.tools.Direction;
import field.tools.Position;
import game.Robot;
import image.Image;

import java.util.ArrayList;

/**
 * Realsisiert eine Antenne
 * @author DavidKulbe
 */
public class Antenna extends Field{
    private ArrayList<String> orientation;
    public Antenna(Position position, String name, boolean passThrough, Image image, ArrayList<String> orientation) {
        super(position, name, passThrough, image);
        this.orientation = orientation;
    }
    
    public String getOrientation()
    {
        return orientation.get(0);
    }
    
    @Override
    public ReducedField reduce()
    {
        return new ReducedAntenna(getParentList().getIsOnBoard(), orientation);
    }
}
