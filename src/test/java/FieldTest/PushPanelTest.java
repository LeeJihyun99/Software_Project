package FieldTest;

import field.PushPanel;
import field.tools.*;
import game.ActivationPhase;
import game.Game;
import game.Player;
import game.Robot;
import org.junit.Before;
import org.junit.Test;
import server.Server;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class PushPanelTest {

    private Robot myRobot;
    private Player dummyPlayer;
    private List<Player> playerList;
    private Game testGame;
    private Gameboard gameboard;
    private ActivationPhase activationPhase;

    @Before
    public void setUp(){
        dummyPlayer = new Player(0,"Dummy",null,false);
        myRobot = new Robot(0,0,new Direction(1),dummyPlayer);
        myRobot.setLookDir(new Direction(1));
        dummyPlayer.setRobot(myRobot);
        playerList= new ArrayList<>();
        playerList.add(dummyPlayer);
        gameboard = new BoardGenerator().generateDeathTrap();
        testGame = new Game(gameboard,playerList, new Server());
        dummyPlayer.setCurrentGame(testGame);
        dummyPlayer.getRobot().setCurrentGame(testGame);
        activationPhase = new ActivationPhase(testGame,1);
        testGame.setActivationPhase(activationPhase);

        testGame.setUpGame();


    }

    @Test
    public void testPushPanelActivationProcess(){
        boolean nowActivate = false;
        testGame.setIterateRegistry(2);
        List<FieldList> landOn = new ArrayList<>();
        dummyPlayer.getRobot().setPosition(new Position(4,3)); //Roboter sitzt auf PushPanel
        landOn.add(dummyPlayer.getCurrentGame().getGameboard().getFieldsAtPosition(dummyPlayer.getRobot().getPosition()));
        for(FieldList fl : landOn){
            testGame.getActivationPhase().sortFields(fl);
        }
        for(PushPanel pp:activationPhase.getOccupiedPushPanels() ){
            nowActivate = activationPhase.onlyPushPanelsForThisRegestry(pp);
        }
        assertTrue("Pushpanel is not for registry: "+testGame.getIterateRegistry(),nowActivate);

    }

    @Test
    public void testPushPanelFunction(){
        testGame.setIterateRegistry(2);
        List<FieldList> landOn = new ArrayList<>();
        dummyPlayer.getRobot().setPosition(new Position(4,3)); //Roboter sitzt auf PushPanel
        dummyPlayer.getRobot().setLookDir(new Direction(2));
        landOn.add(dummyPlayer.getCurrentGame().getGameboard().getFieldsAtPosition(dummyPlayer.getRobot().getPosition()));
        for(FieldList fl : landOn){
            testGame.getActivationPhase().sortFields(fl);
        }
        for(PushPanel pp:activationPhase.getOccupiedPushPanels() ){
            pp.getParentList().setCurrentRobot(dummyPlayer.getRobot());
            pp.actionOnActivation();
        }
        assertTrue("Current Position: x="+dummyPlayer.getRobot().getPosition().x()+" y="+dummyPlayer.getRobot().getPosition().y(),dummyPlayer.getRobot().getPosition().equals(new Position(4,2)));
    }


}
