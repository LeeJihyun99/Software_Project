package game;

import card.Card;
import card.upgrade.RearLaser;
import field.*;
import field.tools.FieldList;
import server.protocol.aktionen.Animation;
import server.protocol.aktionen.CheckPointReached;
import server.protocol.spielkarten.CardPlayed;
import server.protocol.spielkarten.PlayCard;
import server.protocol.spielzug.CurrentCards;
import server.protocol.spielzug.CurrentPlayer;
import tools.ServerLogger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ActivationPhase {

    private Game currGame;
    private List<Player> playerList;
    private List<Player> organizedForRound;
    private Map<Player,Integer> distanceResults;

    private List<ConveyorBelt> occupiedBlueConveyorBelt = new ArrayList<>();
    private List<ConveyorBelt> occupiedGreenConveyorBelt = new ArrayList<>();
    private List<PushPanel> occupiedPushPanels = new ArrayList<>();
    private List<Gear> occupiedGears = new ArrayList<>();
    private List<BoardLaser> occupiedBoardLaser = new ArrayList<>();
    private List<EnergySpace> occupiedEnergySpaces = new ArrayList<>();
    private List<CheckPoint> occupiedCheckPoints = new ArrayList<>();

    private List<Integer> numResults = new ArrayList<>();
    private int iteration;
    private List<FieldList>  landOn = new ArrayList<>();
    private List<Card> playedCards;

    private CurrentCards currentCardsMsg;
    private CurrentPlayer currentPlayerMsg;
    private CardPlayed cardPlayedMsg;
    private PlayCard playCardMsg;
    private CheckPointReached checkPointReachedMsg;
    private Thread conveyorBeltThread;

    private Logger logger = ServerLogger.getLogger();
    

    public ActivationPhase(Game game, int iteration){
        this.currGame = game;
        this.iteration = iteration;
    }

    /**
     * @author Melanie, David
     * run the ActivationPhase
     */
    public void execute(){
        sortPlayers();
        playedCards = new ArrayList<>();
        currentCardsMsg = new CurrentCards();

        for(Player p: organizedForRound){
            currentCardsMsg.addActiveCard(p.getPlayerID(),p.getRobot().getRegister()[iteration].getCardName());
        }

        this.currGame.gameServer.broadcast(currentCardsMsg);
        for(Player p: organizedForRound){
            
            if(iteration == 0)
            {
                p.getRobot().setRebootetThisRound(false);
            }
            
            if (!p.getRobot().hasRebootetThisRound())
            {
                currentPlayerMsg = new CurrentPlayer(p.getPlayerID());
                this.currGame.gameServer.broadcast(currentPlayerMsg);

                Card currCard = p.getRobot().getRegister()[iteration];
                playedCards.add(currCard);
                currCard.execute(p.getRobot());
    
                cardPlayedMsg = new CardPlayed(p.getPlayerID(), currCard.getCardName());

                this.currGame.gameServer.broadcast(cardPlayedMsg);

                this.currGame.gameServer.sendMessageToEveryoneElse(cardPlayedMsg, p.getServerThread());
    
                landOn.add(p.getCurrentGame().getGameboard().getFieldsAtPosition(p.getRobot().getPosition()));
                landOn.get(landOn.size() - 1).setCurrentRobot(p.getRobot());
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        for(FieldList fieldList : landOn){
            sortFields(fieldList);
        }

        Animation animation = new Animation("BlueConveyorBelt");
        currGame.getGameServer().broadcast(animation);
        activateConveyorBelt(currGame.getGameboard().getBlueConveyorBelts());
        activateConveyorBelt(currGame.getGameboard().getGreenConveyorBelts());
        for(PushPanel pp : currGame.getGameboard().getPushPanels()){
            if(onlyPushPanelsForThisRegestry(pp)){
                pp.actionOnActivation();
            }
        }
        for(Gear g : currGame.getGameboard().getGears()){
            g.actionOnActivation();
        }
        for(BoardLaser bl : currGame.getGameboard().getLasers()){
            bl.actionOnActivation();
        }
        for(Player p: playerList){
            p.getRobot().activateLaser(p.getRobot().getLookDir());
            for(int i=0; i<3;i++){
                if(p.getRobot().getUpgradesPerm()[i] instanceof RearLaser){
                    p.getRobot().getUpgradesPerm()[i].execute(p.getRobot());
                }
            }
        }
        for(EnergySpace es : currGame.getGameboard().getEnergySpaces()){
            es.actionOnActivation();
        }
        for(CheckPoint cp : currGame.getGameboard().getCheckPoints()){
            cp.actionOnActivation();
            cp.setMovedThisRound(0);
        }

    }
    
    /**
     * Activates ConveyorBelts in Threads
     * @author David
     */
    private void activateConveyorBelt(List<ConveyorBelt> occupiedConveyorBelt)
    {
        ArrayList<AtomicBoolean> endings = new ArrayList<>();
        for(ConveyorBelt cb: occupiedConveyorBelt){
            AtomicBoolean endOfThisThread = new AtomicBoolean(false);
            endings.add(endOfThisThread);
            int indexOfThisSemaphor = endings.toArray().length-1;
            conveyorBeltThread = new Thread(() ->
            {
                cb.actionOnActivation();
                endings.get(indexOfThisSemaphor).set(true);
        
            });
            conveyorBeltThread.start();
        }
        while (!allThreadsEnded(endings))
        {
            Thread.onSpinWait();
        }
        if(allThreadsEnded(endings)) {
            Animation animation2 = new Animation("GreenConveyorBelt");
            currGame.getGameServer().broadcast(animation2);
        }
    }
    
    
    /**
     * @author Melanie
     * sort players according to antenna-distance and admin-privilege
     */
    public void sortPlayers(){
        organizedForRound = new ArrayList<>();
        playerList = this.currGame.getPlayerQueue();
        distanceResults = new HashMap<>();
        numResults = new ArrayList<>();

        //determine distance to antenna
        for(Player p : playerList){
            distanceResults.put(p,p.getRobot().getDistanceToAntenna());
            numResults.add(p.getRobot().getDistanceToAntenna());
        }
        Collections.sort(numResults);

        //add Players with priority first to list
        switch (iteration){
            case 0: organizedForRound.addAll(currGame.getPriorityRegister0()); break;
            case 1: organizedForRound.addAll(currGame.getPriorityRegister1()); break;
            case 2: organizedForRound.addAll(currGame.getPriorityRegister2()); break;
            case 3: organizedForRound.addAll(currGame.getPriorityRegister3()); break;
            case 4: organizedForRound.addAll(currGame.getPriorityRegister4()); break;
        }

        for(int i=0; i<numResults.size();i++) {
            for(Player p: playerList){
                if (numResults.get(i).equals(distanceResults.get(p)) && !(organizedForRound.contains(p)) && !organizedForRound.contains(p)) {
                    organizedForRound.add(p);
                }
            }
        }

    }
    public void sortFields(FieldList fl){
        List<Field> fields = fl.getFields();
        for (Field field : fields) {
            //Is Field a ConveyorBelt? (blue/green)
            if (field instanceof ConveyorBelt) {
                if (((ConveyorBelt) field).getSpeed() == 2) {
                    occupiedBlueConveyorBelt.add((ConveyorBelt) field);
                } else {
                    occupiedGreenConveyorBelt.add((ConveyorBelt) field);
                }
            } else if (field instanceof PushPanel) {
                occupiedPushPanels.add((PushPanel) field);
            } else if (field instanceof Gear) {
                occupiedGears.add((Gear) field);
            } else if (field instanceof BoardLaser) {
                occupiedBoardLaser.add((BoardLaser) field);
            } else if (field instanceof EnergySpace) {
                occupiedEnergySpaces.add((EnergySpace) field);
            } else if (field instanceof CheckPoint) {
                occupiedCheckPoints.add((CheckPoint) field);
            }
        }
    }

    public boolean onlyPushPanelsForThisRegestry(PushPanel pp){
        for(Integer i: pp.getRegNumbers()){
            if(i==this.currGame.getIterateRegistry()){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gibt an, ob alle Threads beendet wurden
     * @return boolean
     * @author David
     */
    private boolean allThreadsEnded(ArrayList<AtomicBoolean> semaphors)
    {
        for (AtomicBoolean atomicBoolean: semaphors)
        {
           if (!atomicBoolean.get())
           {
               return false; //mindestens ein Thread läuft noch
           }
        }
        return true; //alle beendet
    }

    public List<PushPanel> getOccupiedPushPanels(){
        return occupiedPushPanels;
    }

    public List<ConveyorBelt> getOccupiedBlueConveyorBelt() {
        return occupiedBlueConveyorBelt;
    }
    public List<ConveyorBelt> getOccupiedGreenConveyorBelt() {
        return occupiedGreenConveyorBelt;
    }

    public List<EnergySpace> getOccupiedEnergySpaces() {
        return occupiedEnergySpaces;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }



}
