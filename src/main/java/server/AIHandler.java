package server;


import card.Card;
import field.CheckPoint;
import field.tools.Position;
import game.AIProgramming;
import game.Robot;
import server.protocol.Message;
import server.protocol.Serializer;
import server.protocol.aktionen.*;
import server.protocol.chatnachrichten.ConnectionUpdate;
import server.protocol.lobby.*;
import server.protocol.spielkarten.CardPlayed;
import server.protocol.spielkarten.PlayCard;
import server.protocol.spielzug.*;
import server.protocol.verbindungaufbau.Alive;
import server.protocol.verbindungaufbau.HelloClient;
import server.protocol.verbindungaufbau.HelloServer;
import server.protocol.verbindungaufbau.Welcome;
import server.protocol.chatnachrichten.Error;
import tools.ClientLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

public class AIHandler {
    private Socket socket;
    private BufferedReader inputFromServer;
    private PrintWriter outputForServer;
    private String ip = "127.0.0.1";
    private int port = 8888;
    private final String PROTOCOL_VERSION = "Version 2.0";

    private ClientData clientData;
    private boolean playerValuesSent;
    private boolean playerValuesConfirmed;
    private AIData aiData;


    private Logger logger = ClientLogger.getLogger();

    public AIHandler() {
        clientData = new ClientData();
        playerValuesSent = false;
        playerValuesConfirmed = false;
        aiData = new AIData();
    }

    public void setUpConnection(){
        try {
            socket = new Socket(ip, port);
            //socket = new Socket("sep21.dbs.ifi.lmu.de", 52020);
            socket.isConnected();

            inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputForServer = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
            logger.warning("Initial connection to server could not be established.");
            shutdown();
        }
    }

    public void handleIncomingMessages(){
        while (socket.isConnected()){
            try {
                String serializedMessage = inputFromServer.readLine();
                Message message = getDeserializedMessage(serializedMessage);
                logger.info("Received Message: " + serializedMessage);
                handleNewMessage(message);

            } catch (IOException e) {
                logger.warning("IO-Exception occurred while getting input from server.");
                shutdown();
            }
        }
        logger.warning("Socket is no longer connected to server.");
        shutdown();
    }

    private void handleNewMessage(Message message){
        if (message instanceof HelloClient helloClient){
            handleHelloClientMessage(helloClient);
        } else if (message instanceof Alive){
            handleAliveMessage();
        } else if (message instanceof Welcome welcome){
            handleWelcomeMessage(welcome);
        } else if (message instanceof PlayerAdded playerAdded){
            handlePlayerAddedMessage(playerAdded);
        } else if (message instanceof PlayerStatus playerStatus){
            handlePlayerStatusMessage(playerStatus);
        } else if (message instanceof GameStarted gameStarted){
            handleGameStartedMessage(gameStarted);
        } else if (message instanceof MapSelected mapSelected){
            handleMapSelectedMessage(mapSelected);
        } else if (message instanceof Error error){
            logger.warning(error.getError());
        } else if (message instanceof ConnectionUpdate connectionUpdate){
            handleConnectionUpdateMessage(connectionUpdate);
        } else if (message instanceof CardPlayed cardPlayed){
            handleCardPlayedMessage(cardPlayed);
        } else if (message instanceof CurrentPlayer currentPlayer){
            handleCurrentPlayerMessage(currentPlayer);
        } else if (message instanceof ActivePhase activePhase){
            clientData.setCurrentPhase(activePhase.getPhase());
        } else if (message instanceof StartingPointTaken startingPointTaken){
            handleStartingPointTakenMessage(startingPointTaken);
        } else if (message instanceof YourCards yourCards){
            handleYourCardsMessage(yourCards);
        } else if (message instanceof CurrentCards currentCards){
            handleCurrentCardsMessage(currentCards);
        } else if (message instanceof Movement movement){
            handleMovementMessage(movement);
        } else if (message instanceof PlayerTurning playerTurning){
            handlePlayerTurningMessage(playerTurning);
        } else if (message instanceof PickDamage pickDamage){
            handlePickDamageMessage(pickDamage);
        } else if (message instanceof CheckPointReached checkPointReached){
            handleCheckPointReachedMessage(checkPointReached);
        } else if (message instanceof GameFinished) {
            shutdown();
        } else if (message instanceof CheckpointMoved checkpointMoved){
            handleCheckPointMovedMessage(checkpointMoved);
        }

    }

    private void handleCheckPointMovedMessage(CheckpointMoved checkpointMoved) {
        for (CheckPoint checkPoint : aiData.getAllCheckPoints()){
            if (checkPoint.getCheckNum() == checkpointMoved.getCheckpointID()){
                Position position = new Position(checkpointMoved.getX(), checkpointMoved.getY());
                checkPoint.setPosition(position);
            }
        }
        for (CheckPoint checkPoint : aiData.getCheckPointVisited()){
            if (checkPoint.getCheckNum() == checkpointMoved.getCheckpointID()){
                Position position = new Position(checkpointMoved.getX(), checkpointMoved.getY());
                checkPoint.setPosition(position);
            }
        }
    }


    private void handleCheckPointReachedMessage(CheckPointReached checkPointReached) {
        if (clientData.getYourClientID() == checkPointReached.getClientID()){
            for (CheckPoint checkPoint : aiData.getAllCheckPoints()){
                if (checkPoint.getCheckNum() == checkPointReached.getNumber()){
                    aiData.getCheckPointVisited().add(checkPoint);
                }
            }
        }
    }

    private void handlePickDamageMessage(PickDamage pickDamage) {
        ArrayList<String> selectedDamage = new ArrayList<>();
        for (int i = 0; i < pickDamage.getCount(); i++){
            selectedDamage.add(pickDamage.getAvailablePiles().get(0));
        }
        Message selectedDamageMessage = new SelectedDamage(selectedDamage);
        sendMessage(selectedDamageMessage);
    }

    private void handlePlayerTurningMessage(PlayerTurning playerTurning) {
        if (clientData.getYourClientID() == playerTurning.getClientID()){
            clientData.getYourPlayerData().setOrientation(playerTurning.getRotation());
        }
    }

    private void handleMovementMessage(Movement movement) {
        if (clientData.getYourClientID() == movement.getClientID()) {
            Position position = new Position(movement.getX(), movement.getY());
            clientData.getYourPlayerData().setPosition(position);
        }
    }

    private void handleCurrentCardsMessage(CurrentCards currentCards) {
        for (ActiveCard card : currentCards.getCurrentCards()){
            if (card.getClientID() == clientData.getYourClientID()){
                aiData.setCurrentCard(Card.getCardFromString(card.getCard()));
            }
        }
    }

    private void handleMapSelectedMessage(MapSelected mapSelected) {
        aiData.generateGameBoard(mapSelected.getMap());
    }

    private void handleYourCardsMessage(YourCards yourCards) {
        clientData.setCardsInHand(yourCards.getCardsInHand());
        AIProgramming programmer = new AIProgramming(0.3);
        Robot robot = new Robot(clientData.getYourPlayerData().getFigure());
        robot.setPosition(clientData.getYourPlayerData().getPosition());
        robot.setLookDir(clientData.getYourPlayerData().orientationProperty().get());
        robot.setCheckpointsVisited(aiData.getCheckPointVisited());
        ArrayList<Card> loadOut = programmer.getLoadOut(clientData.getCardsInHand(), robot ,false, aiData.getGameboard() );
        logger.info("LoadOut: " + loadOut);
        for (int i=0; i < loadOut.size(); i++){
            robot.getRegister()[i] = loadOut.get(i);
            Message selectCard = new SelectedCard(robot.getRegister()[i].getCardName(), i);
            sendMessage(selectCard);
        }
    }

    private void handleStartingPointTakenMessage(StartingPointTaken startingPointTaken) {
        Position position = new Position(startingPointTaken.getX(), startingPointTaken.getY());
        Position toRemove = null;
        for (Position startPoint : aiData.getStartPointList()){
            if (startPoint.x() == position.x() && startPoint.y() == position.y()){
                toRemove = startPoint;
            }
        }
        if (toRemove != null){
            aiData.getStartPointList().remove(toRemove);
        }
    }

    private void handleCurrentPlayerMessage(CurrentPlayer currentPlayer) {
        for (PlayerData player : clientData.getPlayers()){
            if (player.getClientID() == currentPlayer.getClientID()){
                player.setCurrentPlayer(true);
            } else {
                player.setCurrentPlayer(false);
            }
        }
        if (clientData.getYourClientID() == currentPlayer.getClientID()){
            if (clientData.getCurrentPhase() == 0){
                Message setStartingPoint = new SetStartingPoint(aiData.getStartPointList().get(0).x(), aiData.getStartPointList().get(0).y());
                sendMessage(setStartingPoint);
            } else if (clientData.getCurrentPhase() == 1) {
                Message buyUpgrade = new BuyUpgrade(false, "null");
                sendMessage(buyUpgrade);
            } else if (clientData.getCurrentPhase() == 3) {
                Message playCard = new PlayCard(aiData.getCurrentCard().toString());
                sendMessage(playCard);
                //TODO: richtige Karte einfügen
            }
        }
    }

    private void handleCardPlayedMessage(CardPlayed cardPlayed) {
        for (PlayerData player : clientData.getPlayers()){
            if (player.getClientID() == cardPlayed.getClientID()){
                player.addPlayedCard(cardPlayed.getCard());
            }
        }
    }

    private void handleConnectionUpdateMessage(ConnectionUpdate connectionUpdate) {
        if (connectionUpdate.getAction().equals("Remove")) {
            for (PlayerData player : clientData.getPlayers()) {
                if (player.getClientID() == connectionUpdate.getClientID()) {
                    clientData.getPlayers().remove(player);
                }
            }
        } else if (connectionUpdate.getAction().equals("AIControl")){
            for (PlayerData player : clientData.getPlayers()) {
                if (player.getClientID() == connectionUpdate.getClientID()) {
                    player.setAI(true);
                }
            }
        }
    }

    private void handleGameStartedMessage(GameStarted gameStarted) {
        clientData.setChosenMap(gameStarted.getGameMap());
        aiData.setFieldLists(gameStarted.getGameMap());
    }


    private void handlePlayerStatusMessage(PlayerStatus playerStatus) {
        for (PlayerData player : clientData.getPlayers()){
            if (player.getClientID() == playerStatus.getClientID()){
                player.setReady(playerStatus.getReady());
            }
        }
    }

    private void handlePlayerAddedMessage(PlayerAdded playerAdded) {
        if (playerAdded.getClientID() == clientData.getYourClientID()){
            playerValuesConfirmed = true;
            Message setStatus = new SetStatus(true);
            sendMessage(setStatus);
            clientData.setYourPlayerData(playerAdded.getClientID());
            clientData.getYourPlayerData().setFigure(playerAdded.getFigure());
            clientData.getYourPlayerData().setName(playerAdded.getName());
            clientData.getPlayers().add(clientData.getYourPlayerData());
        } else {
            clientData.addPlayer(playerAdded.getClientID(), playerAdded.getFigure(), playerAdded.getName());
        }

        //Gives Server Time to send all PlayerAdded-Messages
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Resend PlayerValues if something went wrong
        if (playerValuesSent && !playerValuesConfirmed){
            sendPlayerValues();
        }
    }

    private void handleWelcomeMessage(Welcome welcome) {
        clientData.setYourClientID(welcome.getClientID());
        sendPlayerValues();
    }

    private void sendPlayerValues() {
        //Gives server Time to send PlayerAdded-Messages
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String name = "AI" + clientData.getYourClientID();

        int figure = 0;
        boolean figureAlreadyChosen;
        do {
            figureAlreadyChosen = false;
            figure++;
            for (PlayerData player : clientData.getPlayers()){
                if (player.getFigure() == figure){
                    figureAlreadyChosen = true;
                }
            }
        } while (figureAlreadyChosen);

        Message playerValues = new PlayerValues(name, figure);
        sendMessage(playerValues);
        playerValuesSent = true;
    }

    private void handleAliveMessage() {
        Message alive = new Alive();
        sendMessage(alive);
    }

    private void handleHelloClientMessage (HelloClient helloClient) {
        Message helloServer = new HelloServer("EdleEisbecher", false,PROTOCOL_VERSION );
        sendMessage(helloServer);
    }


    private void sendMessage(Message message){
        try {
            String serializedMessage = Serializer.serializeMessage(message);
            outputForServer.println(serializedMessage); //Gibt den Inhalt (String) aus
            logger.info("Sent Message: " + serializedMessage);

        }catch (Exception e){
            shutdown();
        }
    }

    private Message getDeserializedMessage(String serializedMessage) {
        return Serializer.deserializeMessage(serializedMessage);
    }

    private void shutdown(){
        logger.warning("AI is shutdown.");
        try {
            inputFromServer.close();
            outputForServer.close();

            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.exit(0);

    }
}
