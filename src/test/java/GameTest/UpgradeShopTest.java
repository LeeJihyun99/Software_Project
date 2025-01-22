package GameTest;

import card.Card;
import card.programming.MoveI;
import card.programming.MoveII;
import card.programming.ProgrammingCard;
import card.upgrade.RearLaser;
import field.tools.BoardGenerator;
import field.tools.Gameboard;
import field.tools.Position;
import game.*;
import org.junit.Before;
import org.junit.Test;
import server.Server;
import server.ServerThread;
import server.protocol.spielzug.BuyUpgrade;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UpgradeShopTest {
    private Player player1;
    private Player player2;
    private Player player3AI;
    private ArrayList<Player> players;
    private Robot robot;
    private Robot robot2;
    private ServerThread serverThread;
    private Game testGame;
    private Gameboard gameboard;
    private UpgradeShop upgradeShop;
    private Server server;
    @Before
    public void setUp(){
        robot = new Robot(1);
        robot2 = new Robot(2);
        server = new Server();
        Socket serverSocket = null;
        serverThread = new ServerThread(null, server);
        player1 = new Player(0, "Player1", robot, false);
        player2 = new Player(1,"Player2",robot2,false);
        players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        player1.setServerThread(serverThread);
        player2.setServerThread(serverThread);

        gameboard = new BoardGenerator().generateDizzyHighway();
        testGame = new Game(gameboard, players, new Server());
        player1.getRobot().setCurrentGame(testGame);
        player2.getRobot().setCurrentGame(testGame);
        upgradeShop = new UpgradeShop(testGame);
        testGame.setUpgradeShop(upgradeShop);
        testGame.setUpGame();
        testGame.setUpgradeShopRegister(new Card[testGame.getPlayerQueue().size()]);
        for(int i=0; i < testGame.getPlayerQueue().size();i++){
            testGame.getUpgradeShopRegister()[i] = new RearLaser();
        }
        testGame.setRound(2);

    }
    @Test
    public void testRefresh(){
        int isEmpty = 0;
        testGame.getUpgradeShopRegister()[testGame.getPlayerQueue().size()-1] = null;
        for(int i =0; i < testGame.getPlayerQueue().size(); i++){
            if(testGame.getUpgradeShopRegister()[i] == null){
                isEmpty++;
            }
        }
        assertTrue(testGame.getPlayerQueue().size() > isEmpty);
        int isNotEmpty = 0;
        testGame.getUpgradeShop().refreshShop();
        for(int i =0; i < testGame.getPlayerQueue().size(); i++){
            if(testGame.getUpgradeShopRegister()[i] != null){
                isNotEmpty++;
            }
        }
        assertEquals(testGame.getPlayerQueue().size(),isNotEmpty);
    }

    @Test
    public void testUpgradeShopSort(){
        player2.getRobot().occupyField(new Position(2,4));
        player1.getRobot().occupyField(new Position(3,2));

        ArrayList<Player> sortedPlayers = testGame.getUpgradeShop().getOrganizedPlayers(players);
        assertTrue(sortedPlayers.get(0).equals(player2) && sortedPlayers.get(1).equals(player1));

    }

    // NOTE: Nachrichten Versenden in ServerThreat muss auskommentiert werden ansonsten Fehler
    @Test
    public void testReceivingUpgrade(){
        player1.getServerThread().setGame(testGame);
        player1.getServerThread().setPlayer(player1);
        player1.getRobot().occupyField(new Position(3,2));
        player2.getRobot().occupyField(new Position(7,8));

        testGame.getUpgradeShop().execute();

        BuyUpgrade buyUpgrade = new BuyUpgrade(true, "RearLaser");
        player1.getServerThread().handleBuyUpgrade(buyUpgrade);

        assertTrue(player1.getRobot().getUpgrades() != null && player1.getServerThread().getUpgradeBought() != null && player1.getServerThread().getEnergy() != null);
    }
    @Test
    public void toLittleEnergy(){
        player1.getServerThread().setGame(testGame);
        player1.getServerThread().setPlayer(player1);
        player1.getRobot().occupyField(new Position(3,2));
        player2.getRobot().occupyField(new Position(7,8));
        testGame.getUpgradeShop().execute();
        BuyUpgrade buyUpgrade = new BuyUpgrade(true, "RearLaser");
        player1.setEnergyReserve(1);
        player1.getServerThread().handleBuyUpgrade(buyUpgrade);
        assertTrue(player1.getServerThread().getNotEnoughEnergy() != null);
    }
    @Test
    public void testUpgradeCardWrongInput(){
        player1.getServerThread().setGame(testGame);
        player1.getServerThread().setPlayer(player1);
        player1.getRobot().occupyField(new Position(3,2));
        player2.getRobot().occupyField(new Position(7,8));
        testGame.getUpgradeShop().execute();
        BuyUpgrade buyUpgrade = new BuyUpgrade(true, "Lol");
        player1.setEnergyReserve(1);
        player1.getServerThread().handleBuyUpgrade(buyUpgrade);
        assertTrue(player1.getServerThread().getBuyUpgradError() != null);
    }



}
