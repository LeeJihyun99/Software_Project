package CardTest;

import card.damage.Spam;
import card.damage.Trojan;
import card.damage.Virus;
import card.damage.Worm;
import card.programming.*;
import field.RebootToken;
import field.tools.BoardGenerator;
import field.tools.Direction;
import field.tools.Gameboard;
import field.tools.Position;
import game.Game;
import game.Player;
import game.Robot;
import image.Image;
import org.junit.Before;
import org.junit.Test;
import server.Server;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class CardEffektTest {

    Game testGame;
    Player dummy;
    Robot testRobot;

    Trojan thCard;
    Spam spam;
    Virus virus;
    Worm worm;

    TurnLeft turn1;
    TurnRight turn2;
    UTurn turn3;

    MoveI move1;
    MoveII move2;
    MoveIII move3;

    RebootToken reboot;

    @Before
    public void setUp(){

        dummy = new Player(0,"Test", null,false);
        List<Player> playerList = new ArrayList<>();
        playerList.add(dummy);

        testRobot = new Robot(0,0,new Direction(1),dummy);
        dummy.setRobot(testRobot);

        testGame = new Game(new Gameboard(10,10),playerList, new Server());
        testGame.setGameboard(new BoardGenerator().generateDizzyHighway());

        dummy.setCurrentGame(testGame);
        testRobot.setCurrentGame(testGame);

        Position posReboot = new Position(8,7);
        reboot = new RebootToken(posReboot, "Reboot", true, new Image(), new Direction(0));
        testGame.getGameboard().setRebootTokenOnBoard();

        testRobot.setPosition(new Position(6,3));
        testGame.setUpGame();




    }

    /**
     * @author Melanie
     */
    @Test
    public void testTorjanHorse(){
        thCard = new Trojan();
        int before = testRobot.getDiscardPile().size();
        int stackBefore = testRobot.getCurrentGame().getTrojanHorseCardsStack().size();
        thCard.execute(testRobot);
        int after = testRobot.getDiscardPile().size();
        int stackAfter = testRobot.getCurrentGame().getTrojanHorseCardsStack().size();
        assertTrue("discard pile is not size+2 of before discard pile", after == before+2);
        assertTrue("Card was not returned to pile", stackBefore < stackAfter );
    }

    /**
     * @author Melanie
     */
    @Test
    public void testSpam(){
        spam = new Spam();
        int discardPileBefore = testRobot.getDiscardPile().size();
        int spamStackBefore = testGame.getSpamCardStack().size();
        spam.execute(testRobot);
        int discardPileAfter = testRobot.getDiscardPile().size();
        int spamStackAfter = testGame.getSpamCardStack().size();

        assertTrue("No new Card was pulled. "+discardPileAfter, discardPileAfter == discardPileBefore+1);
        assertTrue("Card was not returnd to SpamStack. "+spamStackAfter, spamStackBefore+1 == spamStackAfter);

    }
    @Test
    public void testVirus(){
        virus = new Virus();
        int amountVirusCardsBefore = testGame.getVirusCardStack().size();
        virus.execute(testRobot);
        assertEquals(amountVirusCardsBefore+1, testGame.getVirusCardStack().size());

    }

    @Test
    public void testWorm(){
        worm = new Worm();
        worm.execute(testRobot);
        assertTrue("Aktuelle Position: x="+testRobot.getPosition().x()+" y="+testRobot.getPosition().y()+" Erwartet: x="+reboot.getPosition().x()+" y="+reboot.getPosition().y(),testRobot.getPosition().equals(reboot.getPosition()));
    }

    /**
     * @author Melanie
     */
    @Test
    public void testTurnCard(){
        turn1 = new TurnLeft();
        turn2 = new TurnRight();
        turn3 = new UTurn();
        Direction beforeTurn1 = testRobot.getLookDir();

        // Left Turn 1->2
        turn1.execute(testRobot);
        Direction afterTurn1 = testRobot.getLookDir();
        assertTrue("Robot did not turn to the left, current Dir: "+ testRobot.getLookDir().getDirectionInteger(),beforeTurn1.getDirectionInteger()+1 == afterTurn1.getDirectionInteger());

        // Right Turn 2->1
        turn2.execute(testRobot);
        Direction afterTurn2 = testRobot.getLookDir();
        assertTrue("Robot did not turn to the right, current Dir: "+ testRobot.getLookDir().getDirectionInteger(),beforeTurn1.getDirectionInteger() == afterTurn2.getDirectionInteger());

        // U-Turn
        turn3.execute(testRobot);
        Direction afterTurn3 = testRobot.getLookDir();
        assertTrue("Robot did not perform u-turn, current Dir: "+ testRobot.getLookDir().getDirectionInteger(), beforeTurn1.getDirectionInteger()+2 == afterTurn3.getDirectionInteger());
    }


    /**
     * @author Melanie
     */
    @Test
    public void testMoveCard(){
        move1 = new MoveI();
        move2 = new MoveII();
        move3 = new MoveIII();
        Position start = new Position(4,4);
        testRobot.setPosition(start);

        int beforePosX = testRobot.getPosition().x();
        int beforePosY = testRobot.getPosition().y();

        //Move 1 - Dir: 1 => 4-> 5 (X)
        move1.execute(testRobot);
        int afterMoveX1 = testRobot.getPosition().x();
        int afterMoveY1 = testRobot.getPosition().y();

        assertTrue("Robot did not move 1 to the right, curr Pos: "+testRobot.getPosition().x()+" gewollt: "+beforePosX+1, beforePosX+1 == afterMoveX1);
        assertTrue("Robot moved into Y direction: "+ testRobot.getPosition().y(),beforePosY == afterMoveY1);

        // Move 2 - Dir: 2 => 4 -> 6 (Y)
        testRobot.setLookDir(new Direction(2));
        move2.execute(testRobot);
        int afterMoveY2 = testRobot.getPosition().y();
        int afterMoveX2 = testRobot.getPosition().x();
        assertTrue("Robot did not move 2 down, curr Pos: "+testRobot.getPosition().y(), beforePosY + 2 == afterMoveY2);
        assertTrue("Robot moved into Y direction: "+ testRobot.getPosition().x(),afterMoveX1 == afterMoveX2);

        // Move 3 - Dir: 3 => 3 -> 6 (X)
        testRobot.setLookDir(new Direction(3));
        move3.execute(testRobot);
        int afterMoveX3 = testRobot.getPosition().x();
        int afterMoveY3 = testRobot.getPosition().y();
        assertTrue("Robot did not move 3 down, curr Pos: "+testRobot.getPosition().x(), afterMoveX1 - 3 == afterMoveX3);
        assertTrue("Robot moved into X direction: "+ testRobot.getPosition().y(),afterMoveY2 == afterMoveY3);

    }

    @Test
    public void backUpTest(){
        BackUp backUp = new BackUp();
        testRobot.setPosition(new Position(3,2));
        Position oldPosition = new Position(testRobot.getPosition().x(),testRobot.getPosition().y());
        backUp.execute(testRobot);
        Position newPosition = new Position(testRobot.getPosition().x(),testRobot.getPosition().y());

        assertTrue(oldPosition!=newPosition);
        assertTrue(newPosition.x() == oldPosition.x()+1);
        assertTrue(newPosition.y()==oldPosition.y());
    }
    
    @Test
    public void againCardsNotEqualTest()
    {
        AgainCard againCard1 = new AgainCard();
        AgainCard againCard2 = new AgainCard();
        assertNotEquals(againCard1, againCard2);
    }
}
