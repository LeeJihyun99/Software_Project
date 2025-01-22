package server;

import card.Card;
import field.tools.Direction;
import field.tools.Position;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tools.ClientLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

public class PlayerData {
    private String name;
    private int clientID;
    private boolean isAI = false;
    private int rebootCount;
    private IntegerProperty figure;
    private BooleanProperty ready;

    private BooleanProperty isFirstForReady;
    private BooleanProperty currentPlayer;

    private ObjectProperty<Card> register0;
    private ObjectProperty<Card> register1;
    private ObjectProperty<Card> register2;
    private ObjectProperty<Card> register3;
    private ObjectProperty<Card> register4;
    private ObjectProperty<Position> position;
    private ObjectProperty<Direction> orientation;
    private IntegerProperty energycount;
    private IntegerProperty reachedCheckpoints;
    private IntegerProperty cards;
    private ObservableList<BooleanProperty> registersFilled;
    private Logger logger = ClientLogger.getLogger();
    private ObjectProperty<ArrayList<Card>> drawnDamageCards;
    private ClientData clientData;
    private ObjectProperty<ArrayList<String>> cardsYouGotNow;




    public void setPlayedCards(String[] playedCardsString) {
        if(! playedCardsString[0].equals("")) {
            setCardsYouGotNow(new ArrayList<>(Arrays.asList(playedCardsString)));
        }

        int counter = 0;
        if (register0 == null) {
            register0.set(Card.getCardFromString(playedCardsString[counter]));
            counter++;
        } else if (register1 == null) {
            register1.set(Card.getCardFromString(playedCardsString[counter]));
            counter++;
        } else if (register2 == null) {
            register2.set(Card.getCardFromString(playedCardsString[counter]));
            counter++;
        } else if (register3 == null){
            register3.set(Card.getCardFromString(playedCardsString[counter]));
            counter++;
        } else if (register4 == null) {
            register4.set(Card.getCardFromString(playedCardsString[4]));
        }
    }

    public void setCardsYouGotNow(ArrayList<String> cardsYouGotNow) {
        this.cardsYouGotNow.set(cardsYouGotNow);
    }

    public ArrayList<String> getCardsYouGotNow() {
        return this.cardsYouGotNow.get();
    }

    public ObjectProperty<ArrayList<String>> cardsYouGotNow() {
        return this.cardsYouGotNow;
    }

    public void setOneRegisterFilled (int register, boolean registerFilled){
        registersFilled.get(register).setValue(registerFilled);
        if (register == 0){
        }
//        System.out.println(registersFilled);
    }

    public PlayerData(int clientID, ClientData clientData) {
        figure = new SimpleIntegerProperty();
        ready = new SimpleBooleanProperty(false);
        isFirstForReady = new SimpleBooleanProperty(false);
        currentPlayer = new SimpleBooleanProperty(false);
        position = new SimpleObjectProperty<>();
        orientation = new SimpleObjectProperty<>();
        energycount = new SimpleIntegerProperty(5);
        reachedCheckpoints = new SimpleIntegerProperty(0);
        cards = new SimpleIntegerProperty();
        registersFilled = FXCollections.observableArrayList(b -> new Observable[]{b});
        register0 = new SimpleObjectProperty<>();
        register1 = new SimpleObjectProperty<>();
        register2 = new SimpleObjectProperty<>();
        register3 = new SimpleObjectProperty<>();
        register4 = new SimpleObjectProperty<>();
        registersFilled.addAll(new SimpleBooleanProperty(false), new SimpleBooleanProperty(false), new SimpleBooleanProperty(false), new SimpleBooleanProperty(false), new SimpleBooleanProperty(false));

        ArrayList <Card> drawnDamage = new ArrayList<>();
        drawnDamageCards = new SimpleObjectProperty<>(drawnDamage);
        this.clientID = clientID;
        this.clientData = clientData;
        cardsYouGotNow = new SimpleObjectProperty<>();
    }

    public PlayerData(int clientID, int figure, ClientData clientData){
        this.figure = new SimpleIntegerProperty(figure);
        ready = new SimpleBooleanProperty(false);
        isFirstForReady = new SimpleBooleanProperty(false);
        currentPlayer = new SimpleBooleanProperty(false);
        position = new SimpleObjectProperty<>();
        orientation = new SimpleObjectProperty<>();
        energycount = new SimpleIntegerProperty(5);
        reachedCheckpoints = new SimpleIntegerProperty(0);
        cards = new SimpleIntegerProperty();
        registersFilled = FXCollections.observableArrayList(b -> new Observable[]{b});

        BooleanProperty tmpB = new SimpleBooleanProperty(false);
        registersFilled.addAll(tmpB, tmpB, tmpB, tmpB, tmpB);
        register0 = new SimpleObjectProperty<>();
        register1 = new SimpleObjectProperty<>();
        register2 = new SimpleObjectProperty<>();
        register3 = new SimpleObjectProperty<>();
        register4 = new SimpleObjectProperty<>();

        ArrayList <Card> drawnDamage = new ArrayList<>();
        drawnDamageCards = new SimpleObjectProperty<>(drawnDamage);

        this.clientID = clientID;
        this.clientData = clientData;
        cardsYouGotNow = new SimpleObjectProperty<>();
    }

    public void addPlayedCard(String cardName){
        Card card = Card.getCardFromString(cardName);
        boolean cardSaved = false;
        if (register0.get() == null && cardSaved == false){
            register0.set(card);
            cardSaved = true;
        } else if (register1.get() == null && cardSaved == false){
            register1.set(card);
            cardSaved = true;
        } else if (register2.get() == null && cardSaved == false){
            register2.set(card);
            cardSaved = true;
        } else if (register3.get() == null && cardSaved == false){
            register3.set(card);
            cardSaved = true;
        } else if (register4.get() == null && cardSaved == false) {
            register4.set(card);
            cardSaved = true;
        }
        assert(cardSaved == true);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public int getFigure() {
        return figure.get();
    }

    public void setFigure(int figure) {
        this.figure.setValue(figure);
    }

    public boolean isReady() {
        return ready.get();
    }

    public void setReady(boolean ready) {
        this.ready.setValue(ready);
    }

    public boolean isFirstForReady() {
        return isFirstForReady.get();
    }

    public void setFirstForReady(boolean firstForReady) {
        isFirstForReady.setValue(firstForReady);
    }

    public boolean isCurrentPlayer() {
        return currentPlayer.get();
    }

    public void setCurrentPlayer(boolean currentPlayer) {
        this.currentPlayer.setValue(currentPlayer);
    }


    public Position getPosition() {
        return position.get();
    }

    public void setPosition(Position position) {
        this.position.set(position);
    }

    public String getOrientation() {
        return orientation.getValue().getDirectionString();
    }

    public void setOrientation(String orientation) {
        Direction direction = new Direction(orientation);
        this.orientation.setValue(direction);
    }

    public void changeOrientationCounterclockwise(){
        //orientation.get().changeDirectionCounterClockwise();
        this.setOrientation(orientation.get().changeDirectionCounterClockwise());
    }

    public void changeOrientationClockwise(){
        //orientation.get().changeDirectionClockwise();
        this.setOrientation(orientation.get().changeDirectionClockwise());
    }
    public int getEnergycount() {
        return energycount.get();
    }

    public void setEnergycount(int energycount) {
        this.energycount.setValue(energycount);
    }
    public void energyCounter(int energycount, String operation){
        if (operation.equals("plus")){
            this.energycount.setValue(getEnergycount() + energycount);
        }else {
            this.energycount.setValue(getEnergycount() - energycount);
        }

    }

    public int getReachedCheckpoints() {
        return reachedCheckpoints.get();
    }

    public void setReachedCheckpoints(int reachedCheckpoints) {
        this.reachedCheckpoints.setValue(reachedCheckpoints);
    }

    public int getCards() {
        return cards.get();
    }

    public void setCards(int cards) {
        this.cards.setValue(cards);
    }

    public ArrayList<Boolean> getRegistersFilled() {
        ArrayList<Boolean> registers = new ArrayList<Boolean>();
        for (BooleanProperty register : registersFilled){
            Boolean b = register.getValue();
            registers.add(b);
        }
        return  registers;
    }


    public void setPlayedCard(int register, String cardName) {
        Card card = Card.getCardFromString(cardName);
        if (register == 0){
            register0.set(card);
        } else if (register == 1){
            register1.set(card);
        } else if (register == 2){
            register2.set(card);
        } else if (register == 3){
            register3.set(card);
        } else if (register == 4){
            register4.set(card);
        }
    }

    public void setOrientationString(String orientationString){
        orientation.get().setDirectionString(orientationString);
    }

    public IntegerProperty figureProperty() {
        return  figure;
    }

    public BooleanProperty readyProperty(){
        return ready;
    }

    public BooleanProperty isFirstForReadyProperty(){
        return isFirstForReady;
    }

    public BooleanProperty currentPlayerProperty(){
        return currentPlayer;
    }


    public ObjectProperty<Position>  positionProperty(){
        return position;
    }

    public ObjectProperty<Direction> orientationProperty(){
        return orientation;
    }

    public IntegerProperty energycountProperty(){
        return energycount;
    }

    public IntegerProperty reachedCheckPointsProperty(){
        return reachedCheckpoints;
    }

    public IntegerProperty cardsProperty(){
        return cards;
    }

    public ObservableList<BooleanProperty> registersFilledProperty(){
        return registersFilled;
    }

    public boolean isAI() {
        return isAI;
    }

    public void setAI(boolean AI) {
        isAI = AI;
    }

    public ArrayList<Card> getDrawnDamageCards() {
        return drawnDamageCards.get();
    }

    public ObjectProperty<ArrayList<Card>> drawnDamageCardsProperty() {
        return drawnDamageCards;
    }

    public void setDrawnDamageCards(ArrayList<Card> drawnDamageCards) {
        this.drawnDamageCards.set(drawnDamageCards);
    }

    public Card getRegister0() {
        return register0.get();
    }

    public ObjectProperty<Card> register0Property() {
        return register0;
    }

    public void setRegister0(Card register0) {
        this.register0.set(register0);
    }

    public Card getRegister1() {
        return register1.get();
    }

    public ObjectProperty<Card> register1Property() {
        return register1;
    }

    public void setRegister1(Card register1) {
        this.register1.set(register1);
    }

    public Card getRegister2() {
        return register2.get();
    }

    public ObjectProperty<Card> register2Property() {
        return register2;
    }

    public void setRegister2(Card register2) {
        this.register2.set(register2);
    }

    public Card getRegister3() {
        return register3.get();
    }

    public ObjectProperty<Card> register3Property() {
        return register3;
    }

    public void setRegister3(Card register3) {
        this.register3.set(register3);
    }

    public Card getRegister4() {
        return register4.get();
    }

    public ObjectProperty<Card> register4Property() {
        return register4;
    }

    public void setRegister4(Card register4) {
        this.register4.set(register4);
    }

    public int getRebootCount() {
        return this.rebootCount;
    }

    public void increaseRebootCount() {
        this.rebootCount++;
    }

    public Integer getDistanceToAntenna() {
        String orientation = clientData.getAntennaOrientation();
        int xLocationAntenna = clientData.getAntennaPosition().x();
        int yLocationAntenna = clientData.getAntennaPosition().y();

        int xLocationRobot = this.getPosition().x();
        int yLocationRobot = this.getPosition().y();

        int distance = 0;
        switch (orientation){
            case "top":
                yLocationAntenna = yLocationRobot-1; // obere Feld von Antenne
                distance = distance+1;
                if(xLocationRobot > xLocationAntenna){
                    for(int i = xLocationAntenna ; i < xLocationRobot ;i++){
                        distance = distance + 1;
                    }
                }else if(xLocationRobot < xLocationAntenna) {

                    for(int i = xLocationAntenna ; i > xLocationRobot ;i--){
                        distance = distance + 1;
                    }
                }else{
                    logger.info("Keine Änderung an Distanz ");
                }
                if(yLocationRobot > yLocationAntenna){
                    for(int i = yLocationAntenna; i <= yLocationRobot; i++){
                        distance = distance + 1;
                    }
                }else if(yLocationRobot < yLocationAntenna){
                    for(int i = yLocationAntenna; i >= yLocationRobot; i--){
                        distance = distance + 1;
                    }
                }
                break;
            case "bottom":
                yLocationAntenna = yLocationRobot+1; // untere Feld von Antenne
                distance = distance+1;
                if(xLocationRobot > xLocationAntenna){
                    for(int i = xLocationAntenna ; i < xLocationRobot ;i++){
                        distance = distance + 1;
                    }
                }else if(xLocationRobot < xLocationAntenna){
                    for(int i = xLocationAntenna ; i > xLocationRobot ;i--){
                        distance = distance + 1;
                    }
                }
                if(yLocationRobot > yLocationAntenna){
                    for(int i = yLocationAntenna; i <= yLocationRobot; i++){
                        distance = distance + 1;
                    }
                }else if(yLocationRobot < yLocationAntenna){
                    for(int i = yLocationAntenna; i >= yLocationRobot; i--){
                        distance = distance + 1;
                    }
                }
                break;
            case "right":
                xLocationAntenna = xLocationRobot+1; // rechte Feld von Antenne
                distance = distance+1;
                if(xLocationRobot > xLocationAntenna){
                    for(int i = xLocationAntenna ; i <= xLocationRobot ;i++){
                        distance = distance + 1;
                    }
                }else if(xLocationRobot < xLocationAntenna){
                    for(int i = xLocationAntenna ; i >= xLocationRobot ;i--){
                        distance = distance + 1;
                    }
                }
                if(yLocationRobot > yLocationAntenna){
                    for(int i = yLocationAntenna; i < yLocationRobot; i++){
                        distance = distance + 1;
                    }
                }else if(yLocationRobot < yLocationAntenna){
                    for(int i = yLocationAntenna; i > yLocationRobot; i--){
                        distance = distance + 1;
                    }
                }
                break;
            case "left":
                xLocationAntenna = xLocationAntenna-1; // linke Feld von Antenne
                distance = distance+1;

                if(xLocationRobot > xLocationAntenna){
                    for(int i = xLocationAntenna ; i <= xLocationRobot ;i++){
                        distance = distance + 1;
                    }
                }else if(xLocationRobot < xLocationAntenna){
                    for(int i = xLocationAntenna ; i > xLocationRobot ;i--){
                        distance = distance + 1;
                    }
                }
                if(yLocationRobot > yLocationAntenna){
                    for(int i = yLocationAntenna; i < yLocationRobot; i++){
                        distance = distance + 1;
                    }
                }else if(yLocationRobot < yLocationAntenna){
                    for(int i = yLocationAntenna; i > yLocationRobot; i--){
                        distance = distance + 1;
                    }
                }
                break;
            default: logger.warning("Wrong Orientation from Antenna. Expected: top,bottom,left or right. Got: "+orientation);
        }
        return distance;
    }

}

