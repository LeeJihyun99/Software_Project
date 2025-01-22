package game;

import card.Card;
import card.damage.*;
import card.programming.*;
import card.programming.special.*;
import card.upgrade.AdminPrivilege;
import card.upgrade.MemorySwap;
import card.upgrade.RearLaser;
import card.upgrade.SpamBlocker;
import field.CheckPoint;
import field.Field;
import field.tools.BoardGenerator;
import field.tools.Gameboard;
import field.tools.Position;
import image.Image;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import server.Server;
import server.protocol.Message;
import server.protocol.aktionen.GameFinished;
import server.protocol.spielzug.ActivePhase;
import server.protocol.spielzug.CurrentPlayer;
import tools.ServerLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class Game implements Runnable{

    private IntegerProperty activePhase;
    private Gameboard gameboard;
    private List<Player> playerQueue;
    private Player currentPlayer;
    private List<Spam> spamStack;
    private List<Virus> virusStack;
    private List<Trojan> trojanHorseCardsStack;
    private List<Worm> wormStack;
    private List<Card> allProgrammingCards;
    private List<AdminPrivilege> adminPrivilegesStack;
    private List<RearLaser> rearLasersStack;
    private List<MemorySwap> memorySwapsStack;
    private List<SpamBlocker> spamBlockersStack;
    private List<Card> upgradeCards;
    private List<ProgrammingCard> specialProgrammingCards;
    Server gameServer;
    private ProgrammingPhase programmingPhase;
    private ActivationPhase activationPhase;
    private UpgradeShop upgradeShop;
    private List<CheckPoint> allCheckPoints;
    private CheckPoint finalCheckPoint;
    private boolean gameActive = true;
    private int round = 1;

    private int iterateRegistry;
    private ActivePhase activePhaseMsg;
    private GameFinished gameFinishedMsg;
    private Player winner;
    private  CountDownLatch waitForPoint;

    private ArrayList<Player> priorityRegister0;
    private ArrayList<Player> priorityRegister1;
    private ArrayList<Player> priorityRegister2;
    private ArrayList<Player> priorityRegister3;
    private ArrayList<Player> priorityRegister4;
    private Card[] upgradeShopRegister;
    private Thread gameThread;

    private Logger logger = ServerLogger.getLogger();


    public Game(Gameboard gameboard, List<Player> playerQueue, Server server) {
        this.gameboard = gameboard;
        gameboard.setCurrentGame(this);
        this.playerQueue = playerQueue;
        this.currentPlayer = playerQueue.get(0);
        this.gameServer = server;
        this.activePhase = new SimpleIntegerProperty();

        this.priorityRegister0 = new ArrayList<>();
        this.priorityRegister1 = new ArrayList<>();
        this.priorityRegister2 = new ArrayList<>();
        this.priorityRegister3 = new ArrayList<>();
        this.priorityRegister4 = new ArrayList<>();

    }


   /**
     * @author Melanie
     * runs the logic of the Game
     */
    public void runGame(){
        logger.info("Game starts now in Thread (" + Thread.currentThread().getName() + ")");

        iterateRegistry = 1;
        activePhase.set(0);
        upgradeShopRegister =  new Card[this.getPlayerQueue().size()];
        activePhase.addListener((c, oldValue, newValue) -> {
            logger.config("Active Phase is now " + activePhase.get() + " in Thread " + Thread.currentThread().getName());
            gameThread = Thread.currentThread();
            if (gameActive){
                Message activePhaseMsg = new ActivePhase(newValue.intValue());
                gameServer.broadcast(activePhaseMsg);
                if (newValue.equals(1)){
                    //Add updatePhase
                    runUpgradeShop();
                    setActivePhase(2);
                } else if (newValue.equals(2)){
                    logger.info("Game continues now in Thread (" + Thread.currentThread().getName() + ") after UpgradeShop");
                    runProgPhase();
                } else if (newValue.equals(3)){
                    iterateRegistry = 0;
                    while(iterateRegistry != 5){
                        currentPlayer.getRobot().setCurrentRegister(iterateRegistry);
                        runActivationPhase(iterateRegistry);
                        //checkForWinner
                        winner = checkRobotCheckPoints();
    
                        if(winner != null){
                            gameFinishedMsg = new GameFinished(winner.getPlayerID());
                            this.gameServer.endGame();
                            this.gameServer.broadcast(gameFinishedMsg);
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        iterateRegistry++;
                    }
                    //Emptying registers for next round,
                    for(Player p: this.getPlayerQueue()){
                        p.getRobot().setRegister(new Card[5]);//Register leeren
                        p.setRegistersFilled(false);
                        p.setDrawnCards(new ArrayList<Card>());

                    }

                    winner = checkRobotCheckPoints();
                    //set Active Phase to updatePhase

                    if(winner != null){
                        gameFinishedMsg = new GameFinished(winner.getPlayerID());
                        this.gameServer.endGame();
                        this.gameServer.broadcast(gameFinishedMsg);
                    }else{
                        round++;
                        setActivePhase(1);
                    }

                }
            }
        });

        /*try {
            logger.warning("Current active Phase " + getActivePhase());
            Thread.sleep(200);
            setActivePhase(1);
            logger.warning("Set active Phase" + getActivePhase());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

         */
        setActivePhase(1);


    }

    /**
     * @author Melanie
     * Basic SetUp Methods for Cards and Field Information
     */
    public void setUpGame(){
        activePhase.set(0);
        activePhaseMsg = new ActivePhase(activePhase.get());
        this.gameServer.broadcast(activePhaseMsg);

        //Create all Card-Stacks
        this.setVirusCardStack();
        this.setTrojanHorseCardsStack();
        this.setWormCardStack();
        this.setSpamCardStack();

        this.setAllProgrammingCards();
        this.setSpecialProgrammingCards();

        this.setUpgradeCards();

        //SetUp for separate Players
        for(Player p: playerQueue){
            p.setEnergyReserve(5);
            p.getRobot().setAmountCubes(0);
        }
        collectAllCheckPoints();
        setFinalCheckPoint();
        logger.info("Game is set up, and can start now");
    }

    /**
     * @author Melanie
     */
    public void setFinalCheckPoint(){
        //Dummy CheckPoint
        finalCheckPoint = new CheckPoint(new Position(0,0), "Dummy CheckPoint", true,new Image(),0);
        for(CheckPoint cp: this.getAllCheckPoints()){
            if(cp.getCheckNum() > finalCheckPoint.getCheckNum()){
                finalCheckPoint = cp;
            }
        }
    }


    /**
     * @author Melanie, David
     * Interate through Board and collect all CheckPoints of Course
     */
    public void collectAllCheckPoints(){
        allCheckPoints = new ArrayList<>();
          for(int i = 0; i <  this.getGameboard().getRowCount();i++){
            for(int j = 0; j < this.getGameboard().getColumnCount() ;j++){
                if(this.getGameboard().getBoard()[i][j] != null)
                {
                    List<Field> fields = this.getGameboard().getBoard()[i][j].getFields();
                    for (Field field : fields)
                    {
                        if (field instanceof CheckPoint)
                        {
                            allCheckPoints.add((CheckPoint) field);
                        }
                    }
                }
            }
        }
          if (allCheckPoints.isEmpty())
          {
              logger.info("Couldn't find any CheckPoints");
          }
    }

    public Player checkRobotCheckPoints(){
        for(Player p: playerQueue){
            if(p.getRobot().getPosition().equals(this.finalCheckPoint.getPosition())){
               for(int i=0; i<p.getRobot().getCheckpointsVisited().size();i++){
                  if(p.getRobot().getCheckpointsVisited().get(i).equals(this.allCheckPoints.get(i))){
                      if(this.allCheckPoints.get(i).getCheckNum() == finalCheckPoint.getCheckNum()){
                          gameActive = false;
                          return p;
                      }
                  }else{
                      gameActive = true;
                  }
               }
            }
        }
        return null;
    }
    public void runUpgradeShop(){
        upgradeShop = new UpgradeShop(this);
        upgradeShop.execute();

    }

    public void runProgPhase(){

        priorityRegister0.clear();
        priorityRegister1.clear();
        priorityRegister2.clear();
        priorityRegister3.clear();
        priorityRegister4.clear();
        programmingPhase = new ProgrammingPhase(this);
        programmingPhase.execute();
    }

    public void runActivationPhase(int registryIndex){
        activePhaseMsg = new ActivePhase(activePhase.get());
        this.gameServer.broadcast(activePhaseMsg);
        activationPhase = new ActivationPhase(this,registryIndex);
        activationPhase.execute();

    }

    public Card[] getUpgradeShopRegister() {
        return upgradeShopRegister;
    }

    public void setUpgradeShopRegister(Card[] upgradeShopRegister) {
        this.upgradeShopRegister = upgradeShopRegister;
    }

    public List<Player> getPlayerQueue() {
        return playerQueue;
    }

    public void setPlayerQueue(List<Player> playerQueue) {
        this.playerQueue = playerQueue;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public void setAllProgrammingCards() {

        for(Player p : playerQueue){
            this.allProgrammingCards = new ArrayList<Card>();
            int MAX_MOVE1 = 5;
            for(int i = 1; i <= MAX_MOVE1; i++){
                MoveI m1 = new MoveI();
                this.allProgrammingCards.add(m1);
            }

            int MAX_MOVE2 = 3;
            for(int i = 1; i <= MAX_MOVE2; i++){
                MoveII m2 = new MoveII();
                this.allProgrammingCards.add(m2);
            }

            MoveIII m3 = new MoveIII();
            this.allProgrammingCards.add(m3);

            int MAX_TURN = 3;
            for(int i = 1; i <= MAX_TURN; i++){
                TurnLeft turnL = new TurnLeft();
                this.allProgrammingCards.add(turnL);
            }

            for(int i = 1; i <= MAX_TURN; i++){
                TurnRight turnR = new TurnRight();
                this.allProgrammingCards.add(turnR);
            }

            BackUp backUp = new BackUp();
            this.allProgrammingCards.add(backUp);

            PowerUpCard pUp = new PowerUpCard();
            this.allProgrammingCards.add(pUp);

            int MAX_AGAIN = 2;
            for(int i = 1; i <= MAX_AGAIN; i++){
                AgainCard againCard = new AgainCard();
                this.allProgrammingCards.add(againCard);
            }

            UTurn uTurn = new UTurn();
            this.allProgrammingCards.add(uTurn);
            Collections.shuffle(allProgrammingCards);
            p.setProgrammingCardsStack(allProgrammingCards);
        }
    }

    public List<Spam> getSpamCardStack() {
        return spamStack;
    }

    public void setSpamCardStack() {
        spamStack = new ArrayList<>();
        for(int i=1; i <= 38;i++){
            Spam spam = new Spam();
            this.spamStack.add(spam);
        }
    }

    public List<Virus> getVirusCardStack() {
        return virusStack;
    }

    public void setVirusCardStack() {
        virusStack = new ArrayList<>();
        for(int i=1; i <= 18;i++){
            Virus virus = new Virus();
            this.virusStack.add(virus);
        }
    }

    public List<Trojan> getTrojanHorseCardsStack() {
        return trojanHorseCardsStack;
    }

    public void setTrojanHorseCardsStack() {
        trojanHorseCardsStack = new ArrayList<>();
        for(int i=1; i <= 12;i++){
            Trojan trojan = new Trojan();
            this.trojanHorseCardsStack.add(trojan);
        }
    }

    public List<Worm> getWormCardStack() {
        return wormStack;
    }

    public void setWormCardStack() {
        wormStack = new ArrayList<>();
        for(int i=1; i <= 6;i++){
            Worm worm = new Worm();
            this.wormStack.add(worm);
        }
    }

    public int getIterateRegistry(){
        return this.iterateRegistry;
    }

    public void setIterateRegistry(int index){
        this.iterateRegistry = index;
    }

    public Gameboard getGameboard() {
        return gameboard;
    }

    public void setGameboard(Gameboard gameboard) {
        this.gameboard = gameboard;
        gameboard.setCurrentGame(this);
    }

    public int getActivePhase() {
        return activePhase.get();
    }

    public void setActivePhase(int activePhase) {
        this.activePhase.set(activePhase);
    }

    public void setGameboard(String selectedMap) {
        BoardGenerator boardGenerator = new BoardGenerator();
        this.gameboard = boardGenerator.generateDizzyHighway();
    }

    public List<CheckPoint> getAllCheckPoints(){
        return this.allCheckPoints;
    }

    public CheckPoint getFinalCheckPoint(){
        return this.finalCheckPoint;
    }

    public ActivationPhase getActivationPhase(){
        return this.activationPhase;
    }

    public void setActivationPhase(ActivationPhase activationPhase) {
        this.activationPhase = activationPhase;
    }

    public Server getGameServer(){
        return gameServer;
    }

    public ProgrammingPhase getProgrammingPhase() {
        return programmingPhase;
    }


    public List<ProgrammingCard> getSpecialProgrammingCards() {
        return specialProgrammingCards;
    }

    public void setSpecialProgrammingCards() {
        this.specialProgrammingCards = new ArrayList<>();

        specialProgrammingCards.add(new EnergyRoutine());
        specialProgrammingCards.add(new RepeatRoutine());
        specialProgrammingCards.add(new SpamFolder());
        specialProgrammingCards.add(new WeaselRoutine());
        specialProgrammingCards.add(new SandboxRoutine());
        specialProgrammingCards.add(new SpeedRoutine());
    }

     public List<Card> getUpgradeCards() {
        return upgradeCards;
    }

    public void setUpgradeCards() {
        this.upgradeCards = new ArrayList<>();
        //Spam-Blockers
        for(int i=0; i< 10; i++){
            upgradeCards.add(new SpamBlocker());
        }
        //Rear-Laser
        for(int i=0; i< 10; i++){
            upgradeCards.add(new RearLaser());
        }
        // AdminPrivilege
        for(int i=0; i< 10; i++){
            upgradeCards.add(new AdminPrivilege());
        }
        //MemorySwap
        for(int i=0; i< 10; i++){
            upgradeCards.add(new MemorySwap());
        }
        Collections.shuffle(upgradeCards);
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round){
        this.round = round;
    }

    public ArrayList<Player> getPriorityRegister0() {
        return priorityRegister0;
    }

    public ArrayList<Player> getPriorityRegister1() {
        return priorityRegister1;
    }

    public ArrayList<Player> getPriorityRegister2() {
        return priorityRegister2;
    }

    public ArrayList<Player> getPriorityRegister3() {
        return priorityRegister3;
    }

    public ArrayList<Player> getPriorityRegister4() {
        return priorityRegister4;
    }

    public UpgradeShop getUpgradeShop() {
        return upgradeShop;
    }

    public void setUpgradeShop(UpgradeShop upgradeShop) {
        this.upgradeShop = upgradeShop;
    }

    @Override
    public void run() {
        runGame();
    }
}
