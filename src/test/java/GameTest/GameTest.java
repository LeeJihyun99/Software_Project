package GameTest;

import field.CheckPoint;
import field.tools.*;
import game.ActivationPhase;
import game.Game;
import game.Player;
import game.Robot;
import image.Image;
import org.jetbrains.annotations.TestOnly;
import org.junit.Before;
import org.junit.Test;
import server.Server;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameTest {

    private Player dummyPlayer;
    private Robot testRobot;
    private Game testGame;
    private List<Player> playerList;
    private Gameboard gameboard;
    private ActivationPhase activationPhase;
    private CheckPoint cp;
    @Before
    public void setUp(){
        dummyPlayer = new Player(0,"Dummy",null,false);
        testRobot = new Robot(5,0,new Direction(1),dummyPlayer);
        dummyPlayer.setRobot(testRobot);
        playerList = new ArrayList<>();
        playerList.add(dummyPlayer);
        Player dummyPlayer2 = new Player(1,"Dummy2",testRobot,false);
        playerList.add(dummyPlayer2);
        gameboard = new BoardGenerator().generateDeathTrap();
        testGame = new Game(gameboard,playerList, new Server());
        dummyPlayer.setCurrentGame(testGame);
        activationPhase = new ActivationPhase(testGame,1);
        testGame.setActivationPhase(activationPhase);
        testRobot.setCurrentGame(testGame);

    }

    @Test
    public void CheckpointCollectionTest(){
        testGame.collectAllCheckPoints();
        assertEquals(5,testGame.getAllCheckPoints().size());
    }

    @Test
    public void testFindFinalCheckPoint(){
        testGame.collectAllCheckPoints();
        testGame.setFinalCheckPoint();
        assertTrue(testGame.getFinalCheckPoint().getCheckNum()==5); //DizzyHighway hat nur einen CheckPoint
    }

    @Test
    public void testSortFieldForBlueConveyorBelts(){
        List<FieldList> landOn = new ArrayList<>();
        dummyPlayer.getRobot().setPosition(new Position(4,7)); //Roboter sitzt auf Blauem Conveyor Belt
        landOn.add(dummyPlayer.getCurrentGame().getGameboard().getFieldsAtPosition(dummyPlayer.getRobot().getPosition()));
        for(FieldList fl : landOn){
            testGame.getActivationPhase().sortFields(fl);
        }
        assertEquals(1,testGame.getActivationPhase().getOccupiedBlueConveyorBelt().size());

    }
    @Test
    public void testStartGame(){
        testGame.setUpGame();
        for(Player p: testGame.getPlayerQueue()){
            assertTrue("Programming Stack ist nicht gesetzt", p.getProgrammingCardsStack()!= null);
            assertTrue("Programming Stack ist leer", p.getProgrammingCardsStack().size() > 0);
        }

    }
}
