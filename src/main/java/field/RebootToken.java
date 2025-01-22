package field;

import field.reducedFields.ReducedField;
import field.reducedFields.ReducedRestartPoint;
import field.tools.Direction;
import field.tools.Position;
import game.Robot;
import image.Image;

import java.util.ArrayList;

/**
 * Implements a reboot token
 * @author DavidKulbe
 */
public class RebootToken extends Field{
    
    private final Direction direction;
    public RebootToken(Position position, String name, boolean passThrough, Image image, Direction direction) {
        super(position, name, passThrough, image);
        this.direction = direction;
    }
    public Direction getDirection()
    {
        return direction;
    }
    
    @Override
    public ReducedField reduce()
    {
        ArrayList<String> orientation = new ArrayList<>();
        orientation.add(direction.getDirectionString());
        return new ReducedRestartPoint(getBoardID(), orientation);
    }
}
