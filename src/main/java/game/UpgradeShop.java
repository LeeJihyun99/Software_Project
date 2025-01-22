package game;

import server.PlayerData;
import server.protocol.spielzug.CurrentPlayer;
import server.protocol.spielzug.ExchangeShop;
import server.protocol.spielzug.RefillShop;
import tools.ServerLogger;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class UpgradeShop {

    private List<Player> playersWithoutAI;
    private List<Player> playersWithoutAISorted;
    private Game currentGame;
    private CountDownLatch waitForUpgrade;
    private Logger logger = ServerLogger.getLogger();

    public UpgradeShop(Game game) {
        this.currentGame = game;
    }

    public void execute(){
        playersWithoutAI = new ArrayList<>();
        playersWithoutAISorted = new ArrayList<>();
        for(Player p: this.currentGame.getPlayerQueue()){
            if(!p.getIsAi()){
                playersWithoutAI.add(p);
            }
        }
        playersWithoutAISorted = getOrganizedPlayers(playersWithoutAI);
        refreshShop();
        CurrentPlayer currentPlayer = new CurrentPlayer(playersWithoutAISorted.get(0).getPlayerID());
        currentGame.getGameServer().broadcast(currentPlayer);
        currentGame.setCurrentPlayer(playersWithoutAISorted.get(0));
        waitForUpgrade = new CountDownLatch(getPlayersWithoutAISorted().size());
        try {
            waitForUpgrade.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        logger.config("Spiel sollte weitergehen");
    }

    public ArrayList<Player> getOrganizedPlayers(List<Player> unorganizedList){

        /*ArrayList<Player> organizedForRound = new ArrayList<>();
        Map<Player,Integer> distanceResults = new HashMap<>();
        ArrayList<Integer> numResults = new ArrayList<>();
        for(Player p : unorganizedList){
            distanceResults.put(p,p.getRobot().getDistanceToAntenna());
            logger.info("Client "+p.getPlayerID()+" ist "+p.getRobot().getDistanceToAntenna()+" Felder von Antenne entfernt");
            numResults.add(p.getRobot().getDistanceToAntenna());
        }
        Collections.sort(numResults);
        for(int i=0; i<numResults.size();i++) {
            for(Player p: unorganizedList){
                if (numResults.get(i).equals(distanceResults.get(p)) && !(organizedForRound.contains(p))) {
                    System.out.println("///////////////////////////////organized player in upgrade shop " + p.getPlayerID()); //TODO
                    organizedForRound.add(p);
                }
            }
        }
        return organizedForRound;*/

        ArrayList<Player> players = new ArrayList<>(unorganizedList);
        players.sort(new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                if (p1.getRobot().getDistanceToAntenna() > p2.getRobot().getDistanceToAntenna()) {
                    return 1;
                } else if (Objects.equals(p1.getRobot().getDistanceToAntenna(), p2.getRobot().getDistanceToAntenna()) &&
                        p1.getPlayerID() > p2.getPlayerID()) {
                    return 1;
                }

                return -1;
            }
        });
        return players;
    }

    public void refreshShop(){
        boolean wasEmptied = false;
        ArrayList <String> newUpgrades = new ArrayList<>();
        for(int i=0; i<this.currentGame.getPlayerQueue().size(); i++){
            if(this.currentGame.getUpgradeShopRegister()[i] == null){
                wasEmptied = true;
            }
        }
        if(!wasEmptied && this.currentGame.getRound() != 1){
            for (int i=0; i<this.currentGame.getPlayerQueue().size(); i++){
                this.currentGame.getUpgradeShopRegister()[i] = this.currentGame.getUpgradeCards().get(0);
                this.currentGame.getUpgradeCards().remove(0);
                newUpgrades.add(this.currentGame.getUpgradeShopRegister()[i].getCardName());
            }
            ExchangeShop exchangeShop;
            exchangeShop = new ExchangeShop(newUpgrades);
            this.currentGame.gameServer.broadcast(exchangeShop);
        }else{
            for(int i=0; i<this.currentGame.getPlayerQueue().size(); i++){
                if(this.currentGame.getUpgradeShopRegister()[i] == null){
                    this.currentGame.getUpgradeShopRegister()[i] = this.currentGame.getUpgradeCards().get(0);
                    this.currentGame.getUpgradeCards().remove(0);
                    newUpgrades.add(this.currentGame.getUpgradeShopRegister()[i].getCardName());
                }
            }
            RefillShop refillShop;
            refillShop = new RefillShop(newUpgrades);
            this.currentGame.gameServer.broadcast(refillShop);


        }
    }

    public List<Player> getPlayersWithoutAISorted() {
        return playersWithoutAISorted;
    }

    public void setPlayersWithoutAISorted(List<Player> playersWithoutAISorted) {
        this.playersWithoutAISorted = playersWithoutAISorted;
    }

    public void selectedUpgrade(){
        if (waitForUpgrade != null){
            waitForUpgrade.countDown();
            logger.config("WaitForDamage CountDownLatch" + waitForUpgrade.getCount());
        }
    }

}
