package field.tools;

import field.Field;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class StartArea extends BoardModule
{
    public StartArea(String boardID)
    {
        super(3, 10, boardID);
    }
}
