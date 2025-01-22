package field;

import field.reducedFields.ReducedEmpty;
import field.reducedFields.ReducedField;
import field.tools.Position;
import game.Robot;
import image.Image;

/**Standartfeld, ohne Funktion
 */
public class DefaultField extends Field{
    public DefaultField(Position position, String name, boolean passThrough, Image image) {
        super(position, name, passThrough, image);
    }
    
    @Override
    public ReducedField reduce()
    {
        return new ReducedEmpty(getBoardID());
    }
}
