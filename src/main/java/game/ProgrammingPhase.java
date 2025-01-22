package game;

import card.Card;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import server.protocol.Message;
import server.protocol.spielzug.*;
import tools.ServerLogger;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.lang.Thread.*;

public class ProgrammingPhase {

    private Game currGame;
    private List<Player> playerList;
    private List<Card> drawnCards;
    private ArrayList<String> drawnCardsName;
    private List<Card> chosenCards;
    private YourCards yourCardsMsg;
    private NotYourCards notYourCardsMsg;
    private ShuffleCoding shuffleCodingMsg;
    private SelectedCard selectedCardMsg;
    private CardSelected cardSelectedMsg;
    private SelectionFinished selectionFinishedMsg;
    private TimerStarted timerStartedMsg;
    private TimerEnded timerEndedMsg;
    private CardsYouGotNow cardsYouGotNowMsg;
    private boolean cardsCanBeProgrammed;
    private BooleanProperty isRunning;
    private BooleanProperty timerShouldRunNow;
    private int finishedPlayers;
    private CountDownLatch allPlayersFinished;
    private ChangeListener<Boolean> timerShouldRun;
    private Logger logger = ServerLogger.getLogger();
    private CountDownLatch onePlayerFinished;


    public ProgrammingPhase(Game game){
        this.currGame = game;
        cardsCanBeProgrammed = false;
        isRunning = new SimpleBooleanProperty(false);
        timerShouldRunNow = new SimpleBooleanProperty(false);
        timerShouldRun = (c, oldValue, newValue) -> {
            try {
                programmingPhaseLogicAfterOnePlayerHasProgrammedAllRegisters();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * @author Melanie
     * runs the logic of the programming phase so that every player can programm the robot
     * after receiving the programming cards
     */
    public synchronized void execute(){
        setRunning(true);
        finishedPlayers = 0;
        playerList = new ArrayList<>();
        playerList = currGame.getPlayerQueue();

        for(Player p : playerList){
            drawnCards = new ArrayList<Card>();
            drawnCardsName = new ArrayList<String>();
            int amountCards = p.getProgrammingCardsStack().size();
            if(amountCards >= 9) {
               enoughCards(p);
            }else{
               notEnoughCards(p,amountCards);
            }
            p.getRobot().getDiscardPile().addAll(drawnCards);
            if(p.getIsAi()){
                programmingAsAI(p);
            }
        }

        cardsCanBeProgrammed = true;
        onePlayerFinished = new CountDownLatch(1);
        try {
            onePlayerFinished.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            programmingPhaseLogicAfterOnePlayerHasProgrammedAllRegisters();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * @author Melanie
     * @param p - Player Object
     * used when there are enough cards in programming stack
     */
    public void enoughCards(Player p){
        drawnCardsName = new ArrayList<>();
        drawnCards = new ArrayList<>();
        logger.info("Enough Cards");
        for (int i = 0; i < 9; i++) {
            drawnCards.add(i,p.getProgrammingCardsStack().get(p.getProgrammingCardsStack().size() - 1));
            drawnCardsName.add(i,p.getProgrammingCardsStack().get(p.getProgrammingCardsStack().size() - 1).getCardName());
            p.getProgrammingCardsStack().remove(p.getProgrammingCardsStack().get(p.getProgrammingCardsStack().size() - 1));
        }

        for(Player player: currGame.getPlayerQueue()){
            if(p.getPlayerID() == player.getPlayerID()){
                player.setDrawnCards(drawnCards);
            }
        }

        yourCardsMsg = new YourCards(drawnCardsName);
        notYourCardsMsg = new NotYourCards(p.getPlayerID(),drawnCards.size());
        p.getServerThread().sendMessageSerialized(yourCardsMsg);
        this.currGame.gameServer.sendMessageToEveryoneElse(notYourCardsMsg,p.getServerThread());
    }

    /**
     * @author Melanie
     * @param p - Player Object
     * @param restCards - amount of Cards still in programming stack
     * used when there are not enough Cars in programming stack
     */
    public void notEnoughCards(Player p, int restCards){
        drawnCardsName = new ArrayList<>();
        drawnCards = new ArrayList<>();
        logger.info("Not Enough Cards");
        for (int i = 0; i < restCards; i++) {
            drawnCards.add(p.getProgrammingCardsStack().get(p.getProgrammingCardsStack().size() - 1));
            drawnCardsName.add(p.getProgrammingCardsStack().get(p.getProgrammingCardsStack().size() - 1).getCardName());
            p.getProgrammingCardsStack().remove(p.getProgrammingCardsStack().get(p.getProgrammingCardsStack().size() - 1));
        }

        //Nachricht für Restbestand
        yourCardsMsg = new YourCards(drawnCardsName);
        notYourCardsMsg = new NotYourCards(p.getPlayerID(),drawnCards.size());
        p.getServerThread().sendMessageSerialized(yourCardsMsg);
        this.currGame.gameServer.sendMessageToEveryoneElse(notYourCardsMsg,p.getServerThread());

        //Shuffeln + entsprechende Nachricht
        p.shuffleCards();

        for(int j=0; j< 9-restCards; j++){
            drawnCards.add(p.getProgrammingCardsStack().get(p.getProgrammingCardsStack().size() - 1));
            drawnCardsName.add(p.getProgrammingCardsStack().get(p.getProgrammingCardsStack().size() - 1).getCardName());
            p.getProgrammingCardsStack().remove(p.getProgrammingCardsStack().get(p.getProgrammingCardsStack().size() - 1));
        }
        for(Player player: currGame.getPlayerQueue()){
            if(p.getPlayerID() == player.getPlayerID()){
                player.setDrawnCards(drawnCards);
            }
        }

        yourCardsMsg = new YourCards(drawnCardsName);
        notYourCardsMsg = new NotYourCards(p.getPlayerID(),drawnCards.size());
        p.getServerThread().sendMessageSerialized(yourCardsMsg);
        this.currGame.gameServer.sendMessageToEveryoneElse(notYourCardsMsg,p.getServerThread());
    }


    /**  @author Lea
     * Method is executed when one player has filled all registers
     **/
    public void programmingPhaseLogicAfterOnePlayerHasProgrammedAllRegisters() throws InterruptedException {
        logger.config("Timer is running in Thread " + currentThread().getName());
        runTimer();

        ArrayList<Player> notFinishedClients = determineUnfinishedPlayersAndSendMessageWithInformationAboutThem();

        //Remaining Registers of clients who haven't finished in time are filled
        for (Player p : notFinishedClients){
            p.fillRemainingRegisters();

        }

        //All clients are informed about the cards in their registers
        for (Player p : playerList){
            p.sendProgrammedCardsMessage();
        }
        isRunning.removeListener(timerShouldRun);
        isRunning.set(false);
        currGame.setActivePhase(3);
        currGame.gameServer.setTimerRunning(false);



    }

    public ArrayList<Player> determineUnfinishedPlayersAndSendMessageWithInformationAboutThem() {
        ArrayList<Integer> notFinishedClientIDs = new ArrayList<>();
        ArrayList<Player> notFinishedClients = new ArrayList<>();
        for (Player p : playerList){
            p.setDrawnCards(null);
            if (!p.getRegistersFilled()){
                notFinishedClientIDs.add(p.getPlayerID());
                notFinishedClients.add(p);
            }
        }

        Message endTimer = new TimerEnded(notFinishedClientIDs);
        currGame.getGameServer().broadcast(endTimer);

        return notFinishedClients;
    }

    public void runTimer() throws InterruptedException {
        //Timer is started
        Message startTimer = new TimerStarted();
        currGame.getGameServer().broadcast(startTimer);

        int humanPlayerCount = 0;
        boolean oneOrMorePlayersAreHuman = false;

        //get number of players, who still need to finish their selection
        for (Player p : currGame.getPlayerQueue()){
            if (!p.getIsAi()){
                humanPlayerCount++;
                oneOrMorePlayersAreHuman = true;
            }
        }

        //Waits for 30 seconds or until all players have finished their selection
        if (oneOrMorePlayersAreHuman){
            allPlayersFinished = new CountDownLatch(humanPlayerCount - 1);
            allPlayersFinished.await(30, TimeUnit.SECONDS);
        }


        //Evtl. stoppen, wenn alle fertig sind
    }
    
    /**
     * Programmierung eines AI Spielers
     * @author David, Melli
     */
    private void programmingAsAI(Player p){
        AIProgramming programmer = new AIProgramming(0.3);
        ArrayList<Card> loadout = programmer.getLoadOut(p.getDrawnCards(), p.getRobot(), false);
        for (int i=0; i<loadout.size(); i++)
        {
            p.getRobot().getRegister()[i] = loadout.get(i);
            cardSelectedMsg = new CardSelected(p.getPlayerID(), i, true);
            this.currGame.gameServer.broadcast(cardSelectedMsg);
        }
        p.setRegistersFilled(true);
    }

    public boolean isCardsCanBeProgrammed() {
        return cardsCanBeProgrammed;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void setRunning(boolean running) {
        isRunning.set(running);
    }

    public BooleanProperty getRunningProperty(){
        return isRunning;
    }

    public void setDrawnCards(ArrayList<Card> drawnCards){
        this.drawnCards = drawnCards;
    }

    public void countDownNotFinishedPlayers(){
        if (allPlayersFinished != null && allPlayersFinished.getCount() >= 1){
            allPlayersFinished.countDown();
        }
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public boolean isTimerShouldRunNow() {
        return timerShouldRunNow.get();
    }

    public BooleanProperty timerShouldRunNowProperty() {
        return timerShouldRunNow;
    }

    public void setTimerShouldRunNow(boolean timerShouldRunNow) {
        this.timerShouldRunNow.set(timerShouldRunNow);
    }

    public void tryToRunTimer(){
        if (onePlayerFinished!= null){
            onePlayerFinished.countDown();
            logger.config("TimerCountDown" + onePlayerFinished.getCount());
        }
    }
}
