package ServerTests;

import field.tools.Direction;
import game.Player;
import game.Robot;
import org.junit.Before;

public class ServerThreatTest {

    @Before
    public void setUp(){
        Player dummy = new Player(0,"Dummy",null,false);
        Robot robot = new Robot(0,0,new Direction(1),dummy);

    }


}
