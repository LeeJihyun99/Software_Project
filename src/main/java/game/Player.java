package game;

import card.Card;
import server.ServerThread;
import server.protocol.Message;
import server.protocol.spielzug.CardsYouGotNow;
import server.protocol.spielzug.ShuffleCoding;
import tools.ServerLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class Player {

    private int playerID;
    private String name;
    private Robot robot;
    private int energyReserve;
    private Game currentGame;

    // Spieler erhält den gesamten Stapel Karten und der Roboter nur die die er ausführt
    private List<Card> programmingCardsStack;
    // List with cards for programming phase
    private List<Card> drawnCards;
    private boolean positionSet = false;
    private ServerThread serverThread;
    private boolean registersFilled;
    private boolean chooseUpgrade = false;
    private boolean isAI;

    private List<Card> exchangeCards = new ArrayList<>();
    private Logger logger = ServerLogger.getLogger();

    public Player(int playerID, String name, Robot robot, boolean isAI){
        this.playerID = playerID;
        this.name = name;
        this.robot = robot;
        this.isAI = isAI;

        drawnCards = new ArrayList<>();
    }

    public Player(int playerID, String name, Robot robot, boolean isAI, ServerThread serverThread){
        this.playerID = playerID;
        this.name = name;
        this.robot = robot;
        this.serverThread = serverThread;
        this.isAI = isAI;
        registersFilled = false;

        drawnCards = new ArrayList<>();
    }

    public List<Card> getExchangeCards() {
        return exchangeCards;
    }

    public void setExchangeCards(List<Card> exchangeCards) {
        this.exchangeCards = exchangeCards;
    }

    public List<Card> getProgrammingCardsStack() {
        return programmingCardsStack;
    }

    public Card getDrawnCardsSpecificCard(String s) {
        for(int i =0; i < this.getDrawnCards().size(); i++){
            if(drawnCards.get(i).getCardName().equals(s)){
                return this.getDrawnCards().get(i);
            }
        }
        return null;
    }

    public boolean isPositionSet() {
        return positionSet;
    }

    public void setPositionSet(boolean positionSet) {
        this.positionSet = positionSet;
    }

    public void setProgrammingCardsStack(List<Card> programmingCardsStack) {
        this.programmingCardsStack = programmingCardsStack;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public Robot getRobot() {
        return robot;
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    public int getEnergyReserve() {
        return energyReserve;
    }

    public void setEnergyReserve(int energyReserve) {
        this.energyReserve = energyReserve;
    }

    public Game getCurrentGame() {
        return currentGame;
    }
    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

    public int getPlayerID() {
        return playerID;
    }

    public ServerThread getServerThread(){
        return this.serverThread;
    }

    public void setServerThread(ServerThread serverThread) {
        this.serverThread = serverThread;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public boolean getRegistersFilled() {
        return registersFilled;
    }

    public void setRegistersFilled(boolean registersFilled) {
        this.registersFilled = registersFilled;
    }
    
    public boolean getIsAi(){
        return isAI;
    }
    
    public void setIsAI(boolean isAI)
    {
        this.isAI = isAI;
    }

    public void sendProgrammedCardsMessage() {
        ArrayList<String> cardNames = new ArrayList<>();
        for (Card i : robot.getRegister()){
            cardNames.add(i.getCardName());
        }

        Message cardsYouGotNow = new CardsYouGotNow(cardNames);
    }


    /**
     * @Author Lea
     * Fills remaining registers with cards after timer has ended in programming phase
     */
    public void fillRemainingRegisters() {
        int numberOfNotFilledRegisters = 0;

        //Gets the number of not filled registers
        for (Card c : robot.getRegister()){
            if (c == null){
                numberOfNotFilledRegisters++;
            }
        }

        //Gets the necessary amount of cards for filling the registers or the remaining cards from the Programming Stack
        ArrayList<Card>cardsForRegisters = new ArrayList<>();

        //If the Programming Stack has not enough cards, the discard pile is shuffled
        if (cardsForRegisters.size() < numberOfNotFilledRegisters){
            shuffleCards();
        }

        for (int i = 0; programmingCardsStack.size() > 0 && i < numberOfNotFilledRegisters; i++){
            cardsForRegisters.add(programmingCardsStack.get(programmingCardsStack.size() - 1));
            programmingCardsStack.remove(programmingCardsStack.size() - 1);
        }

        ArrayList<String> cardsYouGot = new ArrayList<>();

        //Cards for the registers are shuffled and then distributed on the registers
        Collections.shuffle(cardsForRegisters);
        for (int i = 0; i <= 4; i++){
            if (robot.getRegister()[i] == null){
                robot.getRegister()[i] = cardsForRegisters.get(0);
                cardsForRegisters.remove(0);
                cardsYouGot.add(robot.getRegister()[i].getCardName());
            }
        }

        Message cardsYouGotNowMsg = new CardsYouGotNow(cardsYouGot);
        getServerThread().sendMessageSerialized(cardsYouGotNowMsg);
    }
    
    public void executeProgrammingPhaseAsAI(){
    
    }

    public List<Card> getDrawnCards() {
        return drawnCards;
    }

    public void setDrawnCards(List<Card> drawnCards) {
        this.drawnCards = drawnCards;
    }

    public void shuffleCards(){
        if (programmingCardsStack.size() > 0){
            logger.info("Programming Stack is shuffled while it is is not empty");
        }else{
            List<Card> newProgrammingPile = robot.getDiscardPile();
            robot.setDiscardPile(new ArrayList<>());
            setProgrammingCardsStack(newProgrammingPile);
        }
        Collections.shuffle(programmingCardsStack);
        Message shuffleMsg = new ShuffleCoding(playerID);
        getServerThread().sendMessageSerialized(shuffleMsg);
    }

    public boolean isChooseUpgrade() {
        return chooseUpgrade;
    }

    public void setChooseUpgrade(boolean chooseUpgrade) {
        this.chooseUpgrade = chooseUpgrade;
    }
}
