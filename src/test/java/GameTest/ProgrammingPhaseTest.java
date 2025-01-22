package GameTest;

import card.Card;
import card.programming.MoveI;
import card.programming.MoveII;
import card.programming.ProgrammingCard;
import field.tools.BoardGenerator;
import field.tools.Gameboard;
import game.Game;
import game.Player;
import game.ProgrammingPhase;
import game.Robot;
import org.junit.Before;
import org.junit.Test;
import server.Server;
import server.ServerThread;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ProgrammingPhaseTest {
    private Player player1;
    private Player player2;
    private Player player3AI;
    private ArrayList<Player> players;
    private Robot robot;
    private ServerThread serverThread;
    private Game testGame;
    private Gameboard gameboard;
    private ProgrammingPhase programmingPhase;
    private Server server;



    @Before
    public void setUp(){
        robot = new Robot(1);
        server = new Server();
        Socket serverSocket = null;
        serverThread = new ServerThread(null, server);
        player1 = new Player(0, "Player1", robot, false);
        player2 = new Player(1, "Player2", null, false);
        player3AI = new Player(2, "Player 3 (AI)", null, true);
        players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        players.add(player3AI);
        player1.setServerThread(serverThread);

        gameboard = new BoardGenerator().generateDizzyHighway();
        testGame = new Game(gameboard, players, new Server());

        programmingPhase = new ProgrammingPhase(testGame);

        List<Card> cardStack = new ArrayList<>();
        List<Card> discardedStack = new ArrayList<>();

        ProgrammingCard moveI = new MoveI();
        cardStack.add(moveI);
        cardStack.add(moveI);
        cardStack.add(moveI);
        cardStack.add(moveI);

        ProgrammingCard moveII = new MoveII();
        discardedStack.add(moveII);
        discardedStack.add(moveII);
        discardedStack.add(moveII);
        discardedStack.add(moveII);
        discardedStack.add(moveII);
        discardedStack.add(moveII);

        player1.setProgrammingCardsStack(cardStack);
        player1.getRobot().setDiscardPile(discardedStack);
        player1.setDrawnCards(null);
    }

    @Test
    public void enoughCardsTest(){
        testGame.setAllProgrammingCards();
        assertEquals(0, player1.getDrawnCards().size());

        programmingPhase.enoughCards(player1);
        assertEquals(9, player1.getDrawnCards().size());


    }

    @Test
    public void notEnoughCardsTest(){
        ArrayList<Card> tmp = new ArrayList<>();
        programmingPhase.setDrawnCards(tmp);

        assertEquals(4, player1.getProgrammingCardsStack().size());
        assertEquals(6, player1.getRobot().getDiscardPile().size());

        programmingPhase.notEnoughCards(player1, 4);

        assertEquals(9, player1.getDrawnCards().size());
        boolean containsMoveI = false;
        boolean containsMoveII = false;
        for (Card c : player1.getDrawnCards()){
            if (c instanceof MoveI){
                containsMoveI = true;
            } else if (c instanceof MoveII){
                containsMoveII = true;
            }
        }
        assertTrue(containsMoveI);
        assertTrue(containsMoveII);
        System.out.println(player1.getProgrammingCardsStack().get(0));
        assertTrue(player1.getProgrammingCardsStack().get(0) instanceof MoveII);

    }

    @Test
    public void programmingAsAITest(){

    }

    @Test
    public void executeTest(){

    }

    @Test
    public void setOneRegisterTest(){
        Card card = new MoveI();
        player1.getRobot().setOneRegister(card, 0);
        assertEquals(card, player1.getRobot().getRegister()[0]);
    }

    @Test
    public void controllAllRegistersFilledTestNotFull(){
        player1.getRobot().controlAllRegistersFilled();
        assertFalse(player1.getRegistersFilled());

    }

    @Test
    public void runTimerTest(){

    }

    @Test
    public void determineUnfinishedPlayersAndSendMessageWithInformationAboutThemTest(){

    }

    @Test
    public void fillRemainingRegistersTest(){

    }

    @Test
    public void programmingPhaseLogicAfterOnePlayerHasProgrammedAllRegisters(){

    }
}
