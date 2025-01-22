package game;

import card.Card;
import card.programming.*;
import field.tools.BoardGenerator;
import field.tools.Direction;
import field.tools.Gameboard;
import field.tools.Position;
import server.Server;
import tools.ServerLogger;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

public class AITrainer
{
    private int roundsNeeded = 0;
    private ArrayList<AIProgramming> ais = new ArrayList<>();
    //private double[] weights = {0.8,0.2,0.1,-0.05,-0.05,-0.1, -3.3};
    //private double[] weights = {1.0,0.0,0.0,0.0,0.0,0.0,-5.0};
    //private double[] weights = {0.5989019470107113, -0.6907194647278154, 1.4626636312098464, 0.5467212366688265, -0.8220650014341349, 0.1782855054464387, -4.261377137081776};
    private double[] weights = {1.688965670457948, 2.4144220367075806, 0.6560726642937948, -0.30245716577640763, -0.19275233404825853, 0.11073246491668759, -3.167816020112563};
    
    
    private double offset = 0.3;
    
    private ArrayList<Card> allProgrammingCards = new ArrayList<>();
    protected Logger logger = ServerLogger.getLogger();
    
    private void train(int robots, int aisCounter, int length)
    {
        double roundsNeededAll = 0;
        double times = 0;
        //random starting point on map
        Random random = new Random();
        Gameboard trainingsmap = new BoardGenerator().generateDizzyHighway();
        int countPositions = trainingsmap.getPositionsOfAvailableStartingPoints().size();
        Position positionOfChosenStartingPoint = trainingsmap.getPositionsOfAvailableStartingPoints().get(random.nextInt(countPositions));
        Game game = setUpGame(robots, positionOfChosenStartingPoint);
        for (int i = 0; i<length; i++)
        {
            this.ais = new ArrayList<>();
            boolean end = false;
            //System.out.println("Round: "+ i);
            roundsNeeded = 0;
            for (int aiCounter = 0; aiCounter<aisCounter; aiCounter++)
            {
                //System.out.println(aiCounter);
                AIProgramming ai = new AIProgramming(offset);
                this.ais.add(ai);
            }
            //simulation
            while (!end)
            {
                times++;
                ArrayList<Card> availableCards = get9Cards();
                for (AIProgramming ai : ais)
                {
                        if (robots == aisCounter)
                        {
                            ai.getLoadOut(availableCards, game.getPlayerQueue().get(ais.indexOf(ai)).getRobot(), true);
                        } else ai.getLoadOut(availableCards, game.getPlayerQueue().get(0).getRobot(), true);
                }
                for (int register = 0; register < 5; register++)
                {
                    for (AIProgramming ai : ais)
                    {
                        ai.trainingsSimulation(register);
                        //System.out.println(ais.indexOf(ai));
                        if (ai.hasWon() && !end)
                        {
                            end = true;
                            weights = ai.getWeights();
                            //System.out.println("Rounds needed: " + roundsNeeded);
                            logger.info("Trainingsround finished! Rounds needed: " + roundsNeeded + " Winner-weight: " + Arrays.toString(weights));
                            break;
                        }
                    }
                }
                roundsNeeded++;
                roundsNeededAll += roundsNeeded;
                //System.out.println("Current round: " + roundsNeeded);
            }
        }
        logger.info("Training ended!");
    }
    
    private void setUpProgrammingStack()
    {
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
    }
    
    private ArrayList<Card> get9Cards()
    {
        Collections.shuffle(allProgrammingCards);
        ArrayList<Card> programmingCardSelection = new ArrayList<>();
        for (int i = 0; i<9; i++)
        {
            programmingCardSelection.add(allProgrammingCards.get(i));
        }
        return programmingCardSelection;
    }
    
    private Game setUpGame(int robots, Position startPosition)
    {
        Gameboard map = new BoardGenerator().generateDizzyHighway();
        ArrayList<Player> playerList = new ArrayList<>();
        for (int i = 0; i<robots; i++)
        {
            Player newPlayer = new Player(i,"ai"+i, null,true);
            Robot newRobot = new Robot(5,0,new Direction(1),newPlayer);
            newPlayer.setRobot(newRobot);
            playerList.add(newPlayer);
        }
        Game game = new Game(map, playerList,new Server());
        for (Player p: playerList)
        {
            p.getRobot().setCurrentGame(game);
            p.getRobot().setPosition(startPosition);
            p.getRobot().setPositionOfChosenStartingPoint(startPosition);
        }
        return game;
    }
    
    public static void main(String[] args)
    {
        AITrainer trainer = new AITrainer();
        trainer.setUpProgrammingStack();
        trainer.train(1,1,50);
    }
}
