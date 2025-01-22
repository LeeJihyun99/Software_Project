package server;

import card.Card;
import field.reducedFields.ReducedField;
import field.tools.Position;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import server.protocol.chatnachrichten.Error;
import server.protocol.chatnachrichten.ReceivedChat;
import tools.ClientLogger;

import java.util.*;
import java.util.logging.Logger;

public class ClientData {
    private ObservableList<Card> upgradeShopContent;
    private ObservableList<Card> boughtUpgradeCards;
    private ArrayList<ReceivedChat> chatmessages;
    private ObjectProperty<ReceivedChat> latestChatMessage;
    private ObjectProperty<Error> latestErrorMessage;
    private StringProperty latestGameMessage;
    private ObservableList<PlayerData> players;
    private ListProperty<String> availableMaps;
    private StringProperty selectedMap;
    private ObjectProperty<ArrayList<ArrayList<ArrayList<ReducedField>>>> chosenMap;
    private IntegerProperty currentPhase;
    private ObjectProperty<ArrayList<Card>> cardsInHand;

    private ObjectProperty<ArrayList<String>> exchangeCards;
    private StringProperty animation;
    private PlayerData yourPlayerData;
    private PlayerData player1Data;
    private PlayerData player2Data;
    private PlayerData player3Data;
    private PlayerData player4Data;
    private PlayerData player5Data;
    private IntegerProperty lastRebooted;
    private int yourClientID;
    private BooleanProperty timerIsRunning;
    private ObjectProperty<ArrayList<Card>> availableDamageCards;
    private IntegerProperty damageCardCount;
    private int round = 0;
    private BooleanProperty gameFinished;
    private IntegerProperty currentRegister;
    private ObjectProperty<ArrayList<Integer>> checkpointPosition;
    private boolean fatalErrorOccured;
    private PlayerData lastDisconnected;
    private Logger logger = ClientLogger.getLogger();
    private String antennaOrientation;
    private Position antennaPosition;
    private BooleanProperty upgradeShopOpen;

    private ObservableMap<Integer, Card> NewlyBoughtCardWithPlayer;


    public ClientData() {
        chatmessages = new ArrayList<>();
        latestChatMessage = new SimpleObjectProperty<>();
        latestErrorMessage = new SimpleObjectProperty<>();
        latestGameMessage = new SimpleStringProperty();
        players = FXCollections.observableArrayList(p ->
                new Observable[]{p.figureProperty(), p.readyProperty(), p.isFirstForReadyProperty(),
                p.currentPlayerProperty(), p.positionProperty(), p.orientationProperty(),
                p.energycountProperty(), p.reachedCheckPointsProperty(), p.cardsProperty(), p.registersFilledProperty()});
        availableMaps = new SimpleListProperty<>();
        selectedMap = new SimpleStringProperty(null);
        chosenMap = new SimpleObjectProperty<>();
        currentPhase = new SimpleIntegerProperty(-1);
        cardsInHand = new SimpleObjectProperty<>();
        animation = new SimpleStringProperty();
        lastRebooted = new SimpleIntegerProperty();
        timerIsRunning = new SimpleBooleanProperty(false);
        gameFinished = new SimpleBooleanProperty(false);
        ArrayList<Card> availableDamage = new ArrayList<>();
        availableDamageCards = new SimpleObjectProperty<>(availableDamage);
        damageCardCount = new SimpleIntegerProperty();
        currentRegister = new SimpleIntegerProperty(0);
        ArrayList <Card> upgradeShopCards = new ArrayList<Card>();
        upgradeShopContent = FXCollections.observableArrayList(c ->
                new Observable[]{});
        fatalErrorOccured = false;
        checkpointPosition = new SimpleObjectProperty<>();
        boughtUpgradeCards = FXCollections.observableArrayList(c->
                new Observable[]{});
        upgradeShopOpen = new SimpleBooleanProperty();
        HashMap<Integer, Card> boughtCards = new HashMap<>();
        NewlyBoughtCardWithPlayer = FXCollections.observableMap(boughtCards);
        exchangeCards = new SimpleObjectProperty<>();
    }

    public ArrayList<String> getExchangeCards() {
        return exchangeCards.get();
    }

    public ObjectProperty<ArrayList<String>> exchangeCardsProperty() {
        return exchangeCards;
    }

    public void setExchangeCards(ArrayList<String> exchangeCards) {
        this.exchangeCards.set(exchangeCards);
    }
    public void setLastRebooted(int lastRebooted) {
        this.lastRebooted.setValue(lastRebooted);
    }

    public int getLastRebooted() {
        return lastRebooted.get();
    }

    public PlayerData getYourPlayerData(){
        return yourPlayerData;
    }

    public void setYourPlayerData(int id){
        yourPlayerData = new PlayerData(id, this);

    }


    public int getYourClientID() {
        return yourClientID;
    }

    public void setYourClientID(int yourClientID) {
        this.yourClientID = yourClientID;
    }

    public void addPlayer(int clientID){
        PlayerData player = new PlayerData(clientID, this);
        players.add(player);
    }

    public void addPlayer(int clientID, int figure){
        PlayerData player = new PlayerData(clientID, figure, this);
        players.add(player);
    }

    public void addPlayer(int clientID, int figure, String name){
        PlayerData player = new PlayerData(clientID, figure, this);
        players.add(player);
        player.setName(name);
    }

    public void removePlayer(PlayerData player){
        players.remove(player);
    }

    public ArrayList<Integer> getPlayerIDs(){
        ArrayList<Integer> playerIDs = new ArrayList<Integer>();
        for (PlayerData p : players){
            playerIDs.add(p.getClientID());
        }
        return playerIDs;
    }

    public ArrayList<ReceivedChat> getChatmessages() {
        return chatmessages;
    }

    public ReceivedChat getLatestChatMessage() {
        return latestChatMessage.getValue();
    }

    public void setLatestChatMessage(ReceivedChat latestChatMessage) {
        this.latestChatMessage.set(latestChatMessage);
        this.chatmessages.add(latestChatMessage);
    }

    public Error getLatestErrorMessage() {
        return latestErrorMessage.getValue();
    }

    public void setLatestErrorMessage(Error latestErrorMessage) {
        this.latestErrorMessage.set(latestErrorMessage);
    }

    public String getLatestGameMessage() {
        return latestGameMessage.get();
    }

    public StringProperty latestGameMessageProperty(){
        return latestGameMessage;
    }

    public void setLatestGameMessage(String latestGameMessage) {
        this.latestGameMessage.set(latestGameMessage);
    }

    public ArrayList<PlayerData> getPlayers() {
        ArrayList < PlayerData> playerDataArrayList = new ArrayList<>();
        for (PlayerData p : players){
            playerDataArrayList.add(p);
        }
        return playerDataArrayList;
    }

    public ArrayList<String> getAvailableMaps() {
        ArrayList <String> availableMapsArrayList = new ArrayList<>();
        for (String s : availableMaps){
            availableMapsArrayList.add(s);
        }
        return availableMapsArrayList;
    }

    public void setAvailableMaps(ArrayList<String> availableMaps) {
        ObservableList<String> availableMapsObservableList = FXCollections.observableArrayList(availableMaps);
        this.availableMaps.set(availableMapsObservableList);
    }

    public ArrayList<ArrayList<ArrayList<ReducedField>>> getChosenMap() {
        return chosenMap.get();
    }

    public void setChosenMap(ArrayList<ArrayList<ArrayList<ReducedField>>> chosenMap) {
        this.chosenMap.setValue(chosenMap);
    }

    public String getSelectedMap() {
        return selectedMap.get();
    }

    public void setSelectedMap(String selectedMap) {
        this.selectedMap.setValue(selectedMap);
    }

    public int getCurrentPhase() {
        return currentPhase.get();
    }

    public void setCurrentPhase(int currentPhase) {
        this.currentPhase.set(currentPhase);
    }

    public ArrayList<Card> getCardsInHand() {
        return cardsInHand.get();
    }

    public void setCardsInHand(ArrayList<String> cardsInHandString) {
        ArrayList <Card> cardsInHand = new ArrayList<>();
        for (String s : cardsInHandString){
            Card card = Card.getCardFromString(s);
            cardsInHand.add(card);
        }
        this.cardsInHand.setValue(cardsInHand);
    }

    public String getAnimation() {
        return animation.get();
    }

    public void setAnimation(String animation) {
        this.animation.set(animation);
    }
    public StringProperty animationProperty() {
        return animation;
    }

    public void addPlayer(PlayerData yourPlayerData) {
        players.add(yourPlayerData);
    }

    public ObjectProperty<ArrayList<ArrayList<ArrayList<ReducedField>>>> getPropertyChosenMap(){
        return chosenMap;
    }

    public ObjectProperty<ReceivedChat> receivedChatObjectProperty(){
        return latestChatMessage;
    }

    public ObservableList<PlayerData> playersProperty(){
        return players;
    }

    public ObjectProperty<Error> errorObjectProperty(){
        return latestErrorMessage;
    }

    public StringProperty selectedMapProperty(){
        return selectedMap;
    }

    public ObjectProperty<ArrayList<Card>> cardProperty() {
        return cardsInHand;
    }

    public PlayerData getPlayer1Data() {
        return player1Data;
    }

    public void setPlayer1Data(PlayerData player1Data) {
        this.player1Data = player1Data;
    }

    public PlayerData getPlayer2Data() {
        return player2Data;
    }

    public void setPlayer2Data(PlayerData player2Data) {
        this.player2Data = player2Data;
    }

    public PlayerData getPlayer3Data() {
        return player3Data;
    }

    public void setPlayer3Data(PlayerData player3Data) {
        this.player3Data = player3Data;
    }

    public PlayerData getPlayer4Data() {
        return player4Data;
    }

    public void setPlayer4Data(PlayerData player4Data) {
        this.player4Data = player4Data;
    }

    public PlayerData getPlayer5Data() {
        return player5Data;
    }

    public void setPlayer5Data(PlayerData player5Data) {
        this.player5Data = player5Data;
    }

    public boolean isTimerIsRunning() {
        return timerIsRunning.get();
    }

    public BooleanProperty timerIsRunningProperty() {
        return timerIsRunning;
    }

    public void setTimerIsRunning(boolean timerIsRunning) {
        this.timerIsRunning.set(timerIsRunning);
    }

    public ArrayList<Card> getAvailableDamageCards() {
        return availableDamageCards.get();
    }

    public ObjectProperty<ArrayList<Card>> availableDamageCardsProperty() {
        return availableDamageCards;
    }

    public void setAvailableDamageCards(ArrayList<Card> availableDamageCards) {
        this.availableDamageCards.set(availableDamageCards);
    }

    public int getDamageCardCount() {
        return damageCardCount.get();
    }

    public IntegerProperty damageCardCountProperty() {
        return damageCardCount;
    }

    public IntegerProperty currentPhaseProperty(){ return currentPhase;}

    public IntegerProperty lastRebootedProperty() {return lastRebooted;}
    public void setDamageCardCount(int damageCardCount) {
        this.damageCardCount.set(damageCardCount);
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;

    }
    public void roundCounter(){
        this.round++ ;
    }

    public void setGameFinished(boolean gamefinished) {
        this.gameFinished.set(gamefinished);
    }

    public BooleanProperty gameFinished() {
        return gameFinished;
    }

    public boolean isGameFinished() {
        return gameFinished.get();
    }

    public int getCurrentRegister(){
        return this.currentRegister.get();
    }

    public void setCurrentRegister(int currentRegister){
        this.currentRegister.set(currentRegister);
    }
    public IntegerProperty currentRegisterProperty() {
        return this.currentRegister;
    }

    public ObservableList<Card> getUpgradeShopContent() {
        return upgradeShopContent;
    }

    public void addUpgradeShopContent(ArrayList<Card> cards){
        this.upgradeShopContent.addAll(cards);
    }

    public boolean isFatalErrorOccured() {
        return fatalErrorOccured;
    }

    public void setFatalErrorOccured(boolean fatalErrorOccured) {
        this.fatalErrorOccured = fatalErrorOccured;
    }

    public void setCheckpointPosition(int posX, int posY, int checkpointID) {
        ArrayList<Integer> checkPointList = new ArrayList<>();
        checkPointList.add(posX);
        checkPointList.add(posY);
        checkPointList.add(checkpointID);
        this.checkpointPosition.set(checkPointList);
    }

    public ArrayList<Integer> getCheckpointPosition() {
        return this.checkpointPosition.get();
    }

    public ObjectProperty<ArrayList<Integer>> checkPointProperty() {
        return this.checkpointPosition;
    }

    public void setLastDisconnected(PlayerData lastDisconnected) {
        this.lastDisconnected = lastDisconnected;
    }
    public PlayerData getLastDisconnected() {
        return this.lastDisconnected;
    }

    public String getAntennaOrientation() {
        return antennaOrientation;
    }

    public void setAntennaOrientation(String antennaOrientation) {
        this.antennaOrientation = antennaOrientation;
    }

    public Position getAntennaPosition() {
        return antennaPosition;
    }

    public void setAntennaPosition(Position antennaPosition) {
        this.antennaPosition = antennaPosition;
    }

    public ArrayList<PlayerData> getPlayersInAntennaOrder(){
       /* ArrayList<PlayerData> organizedPlayers = new ArrayList<>();
        HashMap<PlayerData, Integer> distanceResults = new HashMap<>();
        ArrayList<Integer> distance = new ArrayList<>();*/

        /*Collections.sort(distance);

        for (int i = 0; i < distance.size(); i++){
            for (PlayerData p : players){
                if (distance.get(i).equals(distanceResults.get(p))&& !(organizedPlayers.contains(p))){
                    System.out.println("///////////////////" + p.getClientID() + p.getName());//TODO
                    organizedPlayers.add(p);
                }
            }
        }

        return organizedPlayers;*/
        ArrayList<PlayerData> organizedPlayers = getPlayers();
        organizedPlayers.sort(new Comparator<PlayerData>() {
            @Override
            public int compare(PlayerData p1, PlayerData p2) {
                if (p1.getDistanceToAntenna() > p2.getDistanceToAntenna()) {
                    return 1;
                } else if (Objects.equals(p1.getDistanceToAntenna(), p2.getDistanceToAntenna()) &&
                        p1.getClientID() > p2.getClientID()) {
                    return 1;
                }

                return -1;
            }
        });
        return organizedPlayers;

    }

    public ObservableList<Card> getBoughtUpgradeCards() {
        return boughtUpgradeCards;
    }

    public boolean isUpgradeShopOpen() {
        return upgradeShopOpen.get();
    }

    public BooleanProperty upgradeShopOpenProperty() {
        return upgradeShopOpen;
    }

    public void setUpgradeShopOpen(boolean upgradeShopOpen) {
        this.upgradeShopOpen.set(upgradeShopOpen);
    }


    public ObservableMap<Integer, Card> getNewlyBoughtCardWithPlayer() {
        return NewlyBoughtCardWithPlayer;
    }

    public void setNewlyBoughtCardWithPlayer(ObservableMap<Integer, Card> newlyBoughtCardWithPlayer) {
        this.NewlyBoughtCardWithPlayer = newlyBoughtCardWithPlayer;
    }

    public void addNewlyBoughtCard(int clientId, Card upgradeCard){
        System.out.println(getNewlyBoughtCardWithPlayer());
        this.NewlyBoughtCardWithPlayer.put(clientId,upgradeCard);
        System.out.println(getNewlyBoughtCardWithPlayer());
    }

    public void removeFromBoughtCard(String cardName) {
        for(Card card: this.getBoughtUpgradeCards()){
            if(card.getCardName().equals(cardName)) {
                this.getBoughtUpgradeCards().remove(card);
            }
        }
    }
}
