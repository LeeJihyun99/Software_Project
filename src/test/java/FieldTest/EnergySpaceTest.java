package FieldTest;

import field.EnergySpace;
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

import static org.junit.Assert.assertEquals;

public class EnergySpaceTest {

    private Robot myRobot;
    private Player dummyPlayer;
    private List<Player> playerList;
    private Game testGame;
    private Gameboard gameboard;
    private ActivationPhase activationPhase;

    @Before
    public void setUp(){
        dummyPlayer = new Player(0,"Dummy",null, false);
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
    public void testEnergySpaceActivation5thRegistry(){
        testGame.setIterateRegistry(5);
        List<FieldList> landOn = new ArrayList<>();
        dummyPlayer.getRobot().setPosition(new Position(4,2));
        dummyPlayer.getRobot().setLookDir(new Direction(2));
        int oldAmountEngery = dummyPlayer.getRobot().getAmountCubes();
        landOn.add(dummyPlayer.getCurrentGame().getGameboard().getFieldsAtPosition(dummyPlayer.getRobot().getPosition()));
        for(FieldList fl : landOn){
            testGame.getActivationPhase().sortFields(fl);
        }
        for(EnergySpace es :activationPhase.getOccupiedEnergySpaces() ){
            es.getParentList().setCurrentRobot(dummyPlayer.getRobot());
            es.actionOnActivation();
        }
        assertEquals(oldAmountEngery+1,dummyPlayer.getRobot().getAmountCubes());
    }

     @Test
    public void testEnergySpaceActivationOtherRegistry(){
        testGame.setIterateRegistry(3);
        List<FieldList> landOn = new ArrayList<>();
        dummyPlayer.getRobot().setPosition(new Position(4,2));
        dummyPlayer.getRobot().setLookDir(new Direction(2));
        int oldAmountEnergy = dummyPlayer.getRobot().getAmountCubes();
        landOn.add(dummyPlayer.getCurrentGame().getGameboard().getFieldsAtPosition(dummyPlayer.getRobot().getPosition()));
        for(FieldList fl : landOn){
            testGame.getActivationPhase().sortFields(fl);
        }
        for(EnergySpace es :activationPhase.getOccupiedEnergySpaces() ){
            es.getParentList().setCurrentRobot(dummyPlayer.getRobot());
            es.actionOnActivation();
        }
        assertEquals(oldAmountEnergy+1,dummyPlayer.getRobot().getAmountCubes());
    }
}
