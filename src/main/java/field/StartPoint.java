package field;

import field.reducedFields.ReducedField;
import field.reducedFields.ReducedStartPoint;
import field.tools.Position;
import game.Robot;
import image.Image;

public class StartPoint extends Field
{
    
    public StartPoint(Position position, String name, boolean passThrough, Image image)
    {
        super(position, name, passThrough, image);
    }
    
    @Override
    public ReducedField reduce()
    {
        return new ReducedStartPoint(getBoardID());
    }
}
