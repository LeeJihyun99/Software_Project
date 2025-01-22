package server;

import card.Card;
import card.upgrade.AdminPrivilege;
import card.upgrade.MemorySwap;
import card.upgrade.RearLaser;
import card.upgrade.SpamBlocker;
import field.reducedFields.ReducedField;
import field.tools.*;
import game.Game;
import game.Player;
import server.protocol.Message;
import server.protocol.Serializer;
import server.protocol.aktionen.*;
import server.protocol.chatnachrichten.ConnectionUpdate;
import server.protocol.chatnachrichten.Error;
import server.protocol.chatnachrichten.ReceivedChat;
import server.protocol.chatnachrichten.SendChat;
import server.protocol.lobby.*;
import server.protocol.spielkarten.PlayCard;
import server.protocol.spielzug.*;
import server.protocol.verbindungaufbau.HelloClient;
import server.protocol.verbindungaufbau.HelloServer;
import server.protocol.verbindungaufbau.Welcome;
import tools.ServerLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Diese Klasse wird vom Server verwendet, um Eingaben und Ausgaben des Clients zu verarbeiten
 * @author Tingyue, David, Lea
 */
public class ServerThread extends Server implements Runnable{
    private Socket socket; //zugehöriger Client
    private Server relatedServer; //Der ursprüngliche server. Server, auf den dieser Thread basiert
    private String nickname;
    private int clientID;
    private BufferedReader messagesFromUser;
    private PrintWriter messagesForUser;
    private Thread thread; //Thread vom Server in welchem dieser ServerThread ausgeführt wird (benötigt zum Schließen des Threads)
    private boolean isClientLeave = false;

    private boolean isFirstReadyToPlay = false;

    private Energy energy;
    private Error notEnoughEnergy;
    private UpgradeBought upgradeBought;
    private Error buyUpgradError;
    private Player player; //corresponding player

    private boolean isAI;
    
    private static Game currGame;
    private static int countForNonAI = 0;
    private Logger logger = ServerLogger.getLogger();

    public ServerThread(Socket socket, Server relatedServer) {
        this.socket = socket;
        this.relatedServer = relatedServer;
    }

    public ServerThread(Server server){
        this.relatedServer = server;
        this.clientID = relatedServer.getClientIDFlag();
        relatedServer.setClientIDFlag(clientID + 1);
        this.isAI = true;
        //assign figure to AI
        int AIFigure = relatedServer.getFigureList().get(0);
        //set name
        setNickname("AI" + relatedServer.generateNewAINameNumber());

        PlayerValues aiValues = new PlayerValues(getNickname(),AIFigure);
        try {
            handlePlayerValuesMessage(aiValues);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        relatedServer.getAIQueue().add(getClientID());
        relatedServer.getConnections().add(this);

        Message playerStatus = new PlayerStatus(clientID, true);
        relatedServer.broadcast(playerStatus);
    }

    /**
     * Liest verschiedene Typen von Nachrichten ein, die den Client betreffen, und verarbeitet sie entsprechend
     * @author Tingyue
     */
    @Override
    public void run() {
        try {
            logger.fine("Start of run-Methode");

            messagesFromUser = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            messagesForUser = new PrintWriter(socket.getOutputStream(), true);

            //Der Server sendet seine Protokoll version an den Client
            logger.fine("Welcome-Message is going to be sent");
            HelloClient helloClient = new HelloClient("Version 2.0");
            sendMessageSerialized(helloClient);

            logger.fine("Welcome-Message was sent");


            while (socket.isConnected()) {
                String serializedMessage = messagesFromUser.readLine();
                Message message = getDeserializedMessage(serializedMessage);
                logger.info("Received Message (" + clientID + "): " + serializedMessage);

                if (message instanceof HelloServer helloServer) {
                    if (helloServer.isAI()){
                        handleHelloServerMessageAI(helloServer, helloClient);
                    } else {
                        handleHelloServerMessage(helloServer, helloClient);
                    }

                } else if (message instanceof PlayerValues playerValues) {
                    handlePlayerValuesMessage(playerValues);

                } else if (message instanceof SendChat sendChatMessage) {
                    handleSendChatMessage(sendChatMessage);

                } else if (message instanceof SetStatus setStatus) {
                    handleSetStatusMessage(setStatus);

                } else if (message instanceof MapSelected mapSelected) {
                    relatedServer.setSelectedMap(mapSelected.getMap());
                    relatedServer.broadcast(new MapSelected(mapSelected.getMap()));

                } else if (message instanceof SetStartingPoint setStartingPoint) {
                    handleStartingPoint(setStartingPoint);

                } else if (message instanceof SelectedCard selectedCard) {
                    handleSelectedCard(selectedCard);

                } else if (message instanceof ConnectionUpdate connectionUpdate) {
                    if (connectionUpdate.isConnected()){
                        ServerThread AILeft = relatedServer.getServerThreadViaClientID(connectionUpdate.getClientID());
                        relatedServer.shutdownAIConnection(AILeft);
                    }else {
                        handleConnectionUpdate(connectionUpdate);
                    }
                } else if (message instanceof GameStarted gameStarted) {
                    createGame();
                } else if (message instanceof SelectedDamage selectedDamage) {
                    handleSelectedDamage(selectedDamage);
                } else if (message instanceof RebootDirection rebootDirection) {
                    player.getRobot().setLookDir(new Direction(rebootDirection.getDirection()));
                    PlayerTurning turningClockwise = new PlayerTurning(player.getPlayerID(), "clockwise");
                    switch(rebootDirection.getDirection()) {
                        case "right" -> relatedServer.broadcast(turningClockwise);
                        case "bottom" -> {
                            relatedServer.broadcast(turningClockwise);
                            relatedServer.broadcast(turningClockwise);
                        }
                        case "left" -> relatedServer.broadcast(new PlayerTurning(player.getPlayerID(), "counterclockwise"));
                    }
                    logger.config("Sent PlayerTurning from Reboot Direction ID: " + player.getPlayerID() + " direction: " + rebootDirection.getDirection());
                    player.getRobot().didReboot();
                } else if (message instanceof ReturnCards returnCards) {
                    handleMemorySwap(returnCards);
                } else if( message instanceof BuyUpgrade buyUpgrade){
                    handleBuyUpgrade(buyUpgrade);
                } else if(message instanceof ChooseRegister chooseRegister){
                    handlePriority(chooseRegister);
                } else if(message instanceof PlayCard playCard){
                    if(playCard.getCard().equals("SpamBlocker")){
                        handleSpamBlocker();
                    }else if(playCard.getCard().equals("MemorySwap")){
                        //Neuen Karten ziehen
                        for(int i=0; i<3;i++){
                            if(player.getRobot().getUpgradesTemp()[i] instanceof MemorySwap){
                                player.getRobot().getUpgradesTemp()[i].execute(player.getRobot());
                            }
                        }
                    }
                }
            }


        } catch (IOException e) {
            //falls Verbindung plötzlich abgebrochen wird.
            if (!isClientLeave()){logger.warning("Connection to client " + clientID + " is lost, connection is shutdown");
                ConnectionUpdate connectionUpdate = new ConnectionUpdate(getClientID(),false,"Remove");
                handleConnectionUpdate(connectionUpdate);

            }

        } catch (InterruptedException e) {
            logger.warning("Connection to client " + clientID + " is lost, connection is shutdown");
            throw new RuntimeException(e);
        }
    }

    public void handleSpamBlocker(){
        for(int i =0; i< 3;i++){
            if(player.getRobot().getUpgradesTemp()[i] instanceof SpamBlocker){
                player.getRobot().getUpgradesTemp()[i].execute(player.getRobot());
                logger.info("Spam Blocker wurde aktiviert bei Client "+player.getPlayerID());
            }
        }
    }

    public void handlePriority(ChooseRegister chooseRegister){

        for(int i=0; i<3;i++){
            if(player.getRobot().getUpgradesPerm()[i] instanceof AdminPrivilege){
                ((AdminPrivilege) player.getRobot().getUpgradesPerm()[i]).addToPriorityList(player,chooseRegister.getRegister());
                break;
            }
        }
        RegisterChosen registerChosen = new RegisterChosen(player.getPlayerID(),chooseRegister.getRegister());
        relatedServer.broadcast(registerChosen);
    }

    public void handleBuyUpgrade(BuyUpgrade buyUpgrade){
            logger.config(Integer.toString(player.getPlayerID()));
            if(relatedServer.getGame().getUpgradeShop().getPlayersWithoutAISorted().indexOf(player) > relatedServer.getGame().getUpgradeShop().getPlayersWithoutAISorted().indexOf(relatedServer.getGame().getCurrentPlayer())) {
                Error error = new Error("Please wait until it is your turn!");
                this.relatedServer.sendMessageSerializedToSomeOne(error, player.getPlayerID());
                return;
            }
        if(buyUpgrade.getCard() != null) {
            switch (buyUpgrade.getCard()) {
                case "AdminPrivilege":
                    if (player.getEnergyReserve() < new AdminPrivilege().getCost()) {
                        notEnoughEnergy = new Error("You only have " + player.getEnergyReserve() + " Energy but need " + new AdminPrivilege().getCost());
                        relatedServer.sendMessageSerializedToSomeOne(notEnoughEnergy, player.getPlayerID());
                    } else {
                        for (int i = 0; i < 3; i++) {
                            if (player.getRobot().getUpgradesPerm()[i] instanceof AdminPrivilege) {
                                buyUpgradError = new Error("You already own a Admin Privilege Card!");
                                relatedServer.sendMessageSerializedToSomeOne(buyUpgradError, player.getPlayerID());
                                return;
                            }
                        }
                        for(int j=0; j < relatedServer.getPlayerQueue().size();j++){
                            if (currGame.getUpgradeShopRegister()[j] instanceof AdminPrivilege) {
                                        player.getRobot().setUpgradesPerm(currGame.getUpgradeShopRegister()[j]);
                                        upgradeBought = new UpgradeBought(player.getPlayerID(), currGame.getUpgradeShopRegister()[j].getCardName());
                                        relatedServer.broadcast(upgradeBought);
                                        currGame.getUpgradeShopRegister()[j] = null;
                                        player.setChooseUpgrade(true);
                                        player.setEnergyReserve(player.getEnergyReserve() - new AdminPrivilege().getCost());
                                        energy = new Energy(player.getPlayerID(), new AdminPrivilege().getCost(), "AdminPrivilege");
                                        relatedServer.broadcast(energy);
                                        break;
                            }
                        }
                    }
                    break;
                case "MemorySwap":
                    if (player.getEnergyReserve() < new MemorySwap().getCost()) {
                        notEnoughEnergy = new Error("You only have " + player.getEnergyReserve() + " Energy but need " + new MemorySwap().getCost());
                        relatedServer.sendMessageSerializedToSomeOne(notEnoughEnergy, player.getPlayerID());
                    } else {
                        for (int i = 0; i < 3; i++) {
                            if (player.getRobot().getUpgradesTemp()[i] instanceof MemorySwap) {
                                buyUpgradError = new Error("You already own a Memory Swap Card!");
                                relatedServer.sendMessageSerializedToSomeOne(buyUpgradError, player.getPlayerID());
                                return;
                            }
                        }
                        for(int j=0; j<relatedServer.getPlayerQueue().size();j++){
                            if (currGame.getUpgradeShopRegister()[j] instanceof MemorySwap) {
                                        player.getRobot().setUpgradesTemp(currGame.getUpgradeShopRegister()[j]);
                                        upgradeBought = new UpgradeBought(player.getPlayerID(), currGame.getUpgradeShopRegister()[j].getCardName());
                                        relatedServer.broadcast(upgradeBought);
                                        currGame.getUpgradeShopRegister()[j] = null;
                                        player.setChooseUpgrade(true);
                                        player.setEnergyReserve(player.getEnergyReserve() - new MemorySwap().getCost());
                                        energy = new Energy(player.getPlayerID(), new MemorySwap().getCost(), "MemorySwap");
                                        relatedServer.broadcast(energy);
                                        break;
                            }
                        }
                    }
                    break;
                case "RearLaser":
                    if (player.getEnergyReserve() < new RearLaser().getPrice()) {
                        notEnoughEnergy = new Error("You only have " + player.getEnergyReserve() + " Energy but need " + new RearLaser().getPrice());
                        relatedServer.sendMessageSerializedToSomeOne(notEnoughEnergy, player.getPlayerID());
                    } else {
                        for (int i = 0; i < 3; i++) {
                            System.out.println("Rear Laser vorhanden: " + (player.getRobot().getUpgradesPerm()[i] instanceof RearLaser));
                            if (player.getRobot().getUpgradesPerm()[i] instanceof RearLaser) {
                                buyUpgradError = new Error("You already own a Rear Laser Card!");
                                relatedServer.sendMessageSerializedToSomeOne(buyUpgradError, player.getPlayerID());
                                return;
                            }
                        }
                        for (int j = 0; j < relatedServer.getPlayerQueue().size(); j++) {
                            if (currGame.getUpgradeShopRegister()[j] instanceof RearLaser) {
                                player.getRobot().setUpgradesPerm(currGame.getUpgradeShopRegister()[j]);
                                upgradeBought = new UpgradeBought(player.getPlayerID(), currGame.getUpgradeShopRegister()[j].getCardName());
                                relatedServer.broadcast(upgradeBought);
                                currGame.getUpgradeShopRegister()[j] = null;
                                player.setChooseUpgrade(true);
                                player.setEnergyReserve(player.getEnergyReserve() - new RearLaser().getPrice());
                                energy = new Energy(player.getPlayerID(), new RearLaser().getPrice(), "RearLaser");
                                relatedServer.broadcast(energy);
                                break;
                            }

                        }
                    }

                    break;
                case "SpamBlocker":
                    if (player.getEnergyReserve() < new SpamBlocker().getCost()) {
                        notEnoughEnergy = new Error("You only have " + player.getEnergyReserve() + " Energy but need " + new SpamBlocker().getCost());
                        relatedServer.sendMessageSerializedToSomeOne(notEnoughEnergy, player.getPlayerID());
                    } else {

                        for (int i = 0; i < 3; i++) {
                            if (player.getRobot().getUpgradesTemp()[i] instanceof SpamBlocker) {
                                buyUpgradError = new Error("You already own a Spam Blocker Card!");
                                relatedServer.sendMessageSerializedToSomeOne(buyUpgradError, player.getPlayerID());
                                return;
                            }
                        }
                        for(int j=0; j < relatedServer.getPlayerQueue().size();j++){
                            if (currGame.getUpgradeShopRegister()[j] instanceof SpamBlocker) {
                                        player.getRobot().setUpgradesTemp(currGame.getUpgradeShopRegister()[j]);
                                        upgradeBought = new UpgradeBought(player.getPlayerID(),currGame.getUpgradeShopRegister()[j].getCardName());
                                        relatedServer.broadcast(upgradeBought);
                                        currGame.getUpgradeShopRegister()[j] = null;
                                        player.setChooseUpgrade(true);
                                        player.setEnergyReserve(player.getEnergyReserve() - new SpamBlocker().getCost());
                                        energy = new Energy(player.getPlayerID(), new SpamBlocker().getCost(), "SpamBlocker");
                                        relatedServer.broadcast(energy);
                                        break;
                            }
                        }
                    }
                    break;
                case "null":
                    upgradeBought = new UpgradeBought(player.getPlayerID(), "null");
                    relatedServer.broadcast(upgradeBought);
                    logger.info("User choose not to Buy an Upgrade.");
                    player.setChooseUpgrade(true); //Choose not to buy One
                    break;
                default:
                    buyUpgradError = new Error(buyUpgrade.getCard() + " is not a valid Card Name!");
                    relatedServer.sendMessageSerializedToSomeOne(buyUpgradError, player.getPlayerID());
                    break;
            }
        }

        logger.info("Spieler "+player.getPlayerID()+" hat ein Upgrade gewählt oder sich enthalten");
        List<Player> playerList = relatedServer.getGame().getUpgradeShop().getPlayersWithoutAISorted();
        for(Player p: playerList){
            if(p.getPlayerID() == relatedServer.getGame().getCurrentPlayer().getPlayerID() && playerList.indexOf(p) < playerList.size()-1 ){
                if(!playerList.get((playerList.indexOf(p)+1)).getIsAi()){
                    CurrentPlayer currentPlayer = new CurrentPlayer(playerList.get((playerList.indexOf(p)+1)).getPlayerID());
                    relatedServer.broadcast(currentPlayer);
                    relatedServer.getGame().setCurrentPlayer(playerList.get((playerList.indexOf(p)+1)));
                    logger.info("New currentPlayer: "+playerList.get((playerList.indexOf(p)+1)).getName());
                }
                break;
            }
        }
        getRelatedServer().getGame().getUpgradeShop().selectedUpgrade();
    }

    public void handleSelectedDamage(SelectedDamage selectedDamage){
        if(selectedDamage == null ||selectedDamage.getCards().isEmpty()){
            logger.warning("Keine Karten erhalten!");
        }else{
            for(Player p: currGame.getPlayerQueue()){
                if(p.getPlayerID() == clientID){
                    for(String cardName: selectedDamage.getCards()){
                        p.getRobot().takeDifferentDamage(cardName);
                        logger.config(clientID + " Took Damage");
                        p.getRobot().tookDamage();
                    }
                }
            }
        }
    }

    public void handleConnectionUpdate(ConnectionUpdate connectionUpdate){
        setClientLeave(true);
        relatedServer.connections.remove(this);
        if (relatedServer.connections.size() > 0){
            relatedServer.broadcast(connectionUpdate);
        }
        relatedServer.removePlayerDataBeforeGameStarted(this);
        //after game started
        relatedServer.removePlayer(clientID);
        if (!isAI()){
            relatedServer.stopGameIfNotEnoughPlayersArePresent();
        }
        shutdownConnection();
    }

    /**
     * @author Lea, Melanie, Jihyun
     * @param selectedCard
     * manage the selected Card in Drag and Drop of Gameboard
     */
    private void handleSelectedCard(SelectedCard selectedCard) throws InterruptedException {

        if(selectedCard.getCard().equals("null")){
            Message CardSelected = new CardSelected(clientID, selectedCard.getRegister(), false);
            relatedServer.broadcast(CardSelected);
            Card card = null;
            player.getDrawnCards().add(player.getRobot().getRegister()[selectedCard.getRegister()]);
            player.getRobot().getRegister()[selectedCard.getRegister()] = null;
        } else {
            Card card = null;
            Message CardSelected = new CardSelected(clientID, selectedCard.getRegister(), true);
            relatedServer.broadcast(CardSelected);

            for(Player p: currGame.getPlayerQueue()){
                if(p.getPlayerID() == clientID){
                    card = p.getDrawnCardsSpecificCard(selectedCard.getCard());
                    player.setDrawnCards(p.getDrawnCards());
                }
            }

            if(card == null){
               logger.info("Card could not be identified");
            }

            if (player.getDrawnCards().contains(card) && relatedServer.getGame().getProgrammingPhase().isCardsCanBeProgrammed()) {
                player.getDrawnCards().remove(card);
                if (player.getRobot().getRegister()[selectedCard.getRegister()] != null) {
                    player.getDrawnCards().add(player.getRobot().getRegister()[selectedCard.getRegister()]);
                }
                player.getRobot().setOneRegister(card, selectedCard.getRegister());
                player.getRobot().controlAllRegistersFilled();
                if (player.getRegistersFilled()) {
                    Message SelectionFinished = new SelectionFinished(clientID);
                    relatedServer.getGame().getProgrammingPhase().countDownNotFinishedPlayers();
                    relatedServer.broadcast(SelectionFinished);
                    currGame.getProgrammingPhase().tryToRunTimer();
                    logger.config("Timer should run now");

                }
            } else {
                Message error = new Error("A mistake has occurred when you chose the card. The card could not be identified");
                sendMessageSerialized(error);
            }
        }
    }


    private void handleStartingPoint(SetStartingPoint sPoint) {
        for(Player p: relatedServer.getPlayerQueue()){
            if(p.getPlayerID() < player.getPlayerID() && !p.isPositionSet() && !p.getIsAi()) {
                Error error = new Error("you need to wait with choosing a Starting Position!");
                relatedServer.sendMessageSerializedToSomeOne(error, player.getPlayerID());
                return;
            }
        }
        for(Player p: relatedServer.getPlayerQueue()){
            if(clientID == p.getPlayerID()){
                player.setServerThread(this);
                player.setCurrentGame(currGame);
                player.getRobot().setRobotPlayer(player);
                player.setPositionSet(true);
                p.setServerThread(this);
                p.setCurrentGame(currGame);
                p.getRobot().setRobotPlayer(p);
            }
        }


        if(currGame == null)
            logger.info("Kein Spiel gesetzt");

        player.getRobot().setCurrentGame(currGame);

        Position chosenPosition = new Position(sPoint.getX(),sPoint.getY());
        if(!relatedServer.getGame().getGameboard().getFieldsAtPosition(chosenPosition).isOccupied()){
            player.getRobot().occupyField(chosenPosition);
            currGame.getGameboard().removePositionFromAvailableStartingPoints(chosenPosition);
            player.getRobot().setPositionOfChosenStartingPoint(chosenPosition);
            StartingPointTaken startingPointTakenMsg = new StartingPointTaken(sPoint.getX(),sPoint.getY(),clientID);
            relatedServer.broadcast(startingPointTakenMsg);
            countForNonAI++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for(Player p: relatedServer.getPlayerQueue()){
                if(p.getPlayerID() == relatedServer.getGame().getCurrentPlayer().getPlayerID() && relatedServer.getPlayerQueue().indexOf(p) < relatedServer.getPlayerQueue().size()-1 ){
                    if(!relatedServer.getPlayerQueue().get((relatedServer.getPlayerQueue().indexOf(p)+1)).getIsAi()){
                        CurrentPlayer currentPlayer = new CurrentPlayer(relatedServer.getPlayerQueue().get((relatedServer.getPlayerQueue().indexOf(p)+1)).getPlayerID());
                        relatedServer.broadcast(currentPlayer);
                        relatedServer.getGame().setCurrentPlayer(relatedServer.getPlayerQueue().get((relatedServer.getPlayerQueue().indexOf(p)+1)));
                        logger.info("New currentPlayer: "+relatedServer.getPlayerQueue().get((relatedServer.getPlayerQueue().indexOf(p)+1)).getName());
                    }
                    break;
                }
            }

        }else{
            Error error = new Error("This Starting Position is already occupied! Please choose a different one.");
            relatedServer.sendMessageSerializedToSomeOne(error,this.clientID);
        }
       
        int nonAIPlayer = 0;
        for(Player p: currGame.getPlayerQueue()){
            if(!p.getIsAi()){
                nonAIPlayer++;
            }
        }

        if(countForNonAI == nonAIPlayer){
            for(Player p: relatedServer.getPlayerQueue()){
                if(p.getIsAi() && p.getRobot().getPosition() == null){
                    p.setCurrentGame(currGame);
                    p.getRobot().setCurrentGame(currGame);
                    p.getRobot().setRobotPlayer(p);
                    Random random = new Random();
                    int countPositions = currGame.getGameboard().getPositionsOfAvailableStartingPoints().size();
                    CurrentPlayer currentPlayer = new CurrentPlayer(p.getPlayerID());
                    relatedServer.getGame().setCurrentPlayer(p);
                    relatedServer.broadcast(currentPlayer);
                    Position positionOfChosenStartingPoint = currGame.getGameboard().getPositionsOfAvailableStartingPoints().get(random.nextInt(countPositions));
                    p.getRobot().occupyField(positionOfChosenStartingPoint);
                    p.getRobot().setPositionOfChosenStartingPoint(positionOfChosenStartingPoint);
                    currGame.getGameboard().removePositionFromAvailableStartingPoints(positionOfChosenStartingPoint);
                    StartingPointTaken startingPointTaken  = new StartingPointTaken(p.getRobot().getPosition().x(), p.getRobot().getPosition().y(), p.getPlayerID());
                    relatedServer.broadcast(startingPointTaken);
                }
            }

            Thread thread = new Thread(currGame);
            thread.setName("Game");
            thread.start();

            //currGame.setActivePhase(1);
            //logger.info("ActivePhase is now" + currGame.getActivePhase());

            //currGame.runGame();
        }
        boolean allPlayersHaveTakenAStartingPoint = true;

        for (Player p : currGame.getPlayerQueue()){
            if (p.getRobot().getPosition() == null){
                allPlayersHaveTakenAStartingPoint = false;
            }
        }

        if (allPlayersHaveTakenAStartingPoint){
            currGame.setActivePhase(1);
        }
    }

    /**
     * If this message typ HelloServer type is received, send message typ welcome
     * and then receive other player information
     * @param helloServer,
     * @param helloClient
     * @author Tingyue
     */
    public void handleHelloServerMessage(HelloServer helloServer, HelloClient helloClient) throws IOException {
        if (helloServer.getProtocol().equals(helloClient.getProtocol())){
            relatedServer.connections.add(this);
            sendMessageSerialized(new Welcome(getClientID()));
            relatedServer.informAllPlayerStatus(this);

        }else {
            String errorInfo = "The server does not support the protocol version of the group , the connection will be terminated. ";
            sendMessageSerialized(new Error(errorInfo));
        }
    }

    private void handleHelloServerMessageAI(HelloServer helloServer, HelloClient helloClient){
        logger.info("AI is requested to be added");
        ServerThread aiThread = new ServerThread(relatedServer);
        Thread thread = new Thread(aiThread);
        relatedServer.getThreads().add(thread);
        aiThread.setThread(thread);
    }

    /**
     * If this message typ PlayerValues type is received, send message typ PlayerAdded and messages about map
     * @param playerValues Nachrichtentyp
     * @author Tingyue
     */
    public void handlePlayerValuesMessage(PlayerValues playerValues) throws IOException {
        int figure = playerValues.getFigure();
        relatedServer.getFigureList().remove((Integer) figure);

        boolean isChosenRobot= relatedServer.getChosenRobots().values().contains(figure);
        if (!isChosenRobot){
            setNickname(playerValues.getName());

            //send PlayerAdded
            relatedServer.getChosenRobots().put(getClientID(), figure);
            PlayerAdded playerAdded = new PlayerAdded(getClientID(), getNickname(), figure);
            relatedServer.broadcast(playerAdded);

            //send selectMap
            sendMapMessage();

        }else{
            Error error = new Error("Robot " + figure + " has been chosen.");
            sendMessageSerialized(error);
        }
        
    }

    /**
     * handles Messages to set priority for Upgrade card AdminPrivilege
     */
    private void handleAdminPrivilege(ChooseRegister chooseRegister) {
        switch (chooseRegister.getRegister()){
            case 0: currGame.getPriorityRegister0().add(player); break;
            case 1: currGame.getPriorityRegister1().add(player); break;
            case 2: currGame.getPriorityRegister2().add(player); break;
            case 3: currGame.getPriorityRegister3().add(player); break;
            case 4: currGame.getPriorityRegister4().add(player); break;
        }
    }

    /**
     * send message typ map and if map is already selected, send message typ MapSelected
     * @author Tingyue
     */
    public void sendMapMessage(){
        if (relatedServer.getListOfMaps().isEmpty()){
            relatedServer.initializeGameMap();
        }

        SelectMap selectMap = new SelectMap(relatedServer.getListOfMaps());
        sendMessageSerialized(selectMap);

        //inform new play, if map already chosen
        if (relatedServer.getSelectedMap() != null){
            sendMessageSerialized(new MapSelected(relatedServer.getSelectedMap()));
        }
    }

    /**
     /**
     * Falls Server SetStatus Nachricht empfangen hat, schickt diese Nachrichten zuerst an allen Spieler
     * dann überprüft, ob player ready ist
     * @author Tingyue
     */
    public void handleSetStatusMessage(SetStatus setStatus){
        PlayerStatus playerStatus = new PlayerStatus(getClientID(), setStatus.isReady() );
        relatedServer.broadcast(playerStatus);

        if (playerStatus.getReady()){
            if (!isAI){
                relatedServer.getPlayerReadyToPlay().add(getClientID());
            }

            //search for first player who is ready to play
            sendFirstPlayerMessage();

        }else {
            relatedServer.getPlayerReadyToPlay().remove((Object) getClientID());

            //The first ready player set status not ready, so search for next player to choose map
            if (isFirstReadyToPlay && !relatedServer.getPlayerReadyToPlay().isEmpty()){
                relatedServer.searchNextPlayerToChooseMap();
                //es gibt nur KI, wählt ein zufällig Map

            }
            setFirstReadyToPlay(false);

            if (relatedServer.getPlayerReadyToPlay().isEmpty() && relatedServer.getAIQueue().size() > 0 && getSelectedMap() == null){
                relatedServer.assignMapRandomly();
                relatedServer.broadcast(new MapSelected(relatedServer.getSelectedMap()));
            }
        }
    }


    private void handleMemorySwap(ReturnCards returnCards) {
        ArrayList<String> newCards = new ArrayList<>();
        for (String cardName : returnCards.getCards()){
            logger.info("Diese Karte will ich ersetzte: "+cardName+". (Durch MemorySwap)");
            Card cardToExchange = player.getDrawnCardsSpecificCard(cardName);

            if(player.getDrawnCards().remove(cardToExchange)){
                logger.info("Karte wurde entfernt. (Durch MemorySwap)");
            }
            player.getProgrammingCardsStack().add(cardToExchange);
            logger.info("Karte wurde auf Programming Stack geworfen. (Durch MemorySwap)");
        }

        System.out.println("amount drawncards " + player.getDrawnCards().size());
        System.out.println(player.getDrawnCards().toString());
        for(int i = 0; i < 3;i++){
            player.getDrawnCards().add(player.getExchangeCards().get(0));
            logger.info("Neue Karte "+player.getExchangeCards().get(0).getCardName()+" hinzugefügt. (Durch MemorySwap)");

            player.getExchangeCards().remove(0);
        }
        for(Card c: player.getDrawnCards()){
            newCards.add(c.getCardName());
        }
        YourCards yourCards = new YourCards(newCards);
        relatedServer.sendMessageSerializedToSomeOne(yourCards,player.getPlayerID());
    }

    /**
     /**
     * Determine who is the first player to be ready and send this message
     * @author Tingyue
     */
    public void sendFirstPlayerMessage(){
        int firstPlayer = relatedServer.getPlayerReadyToPlay().get(0);
        if (firstPlayer == getClientID()) {
            String messageForMe = "/first player ready to play";
            sendMessageSerialized(new SendChat(messageForMe, getClientID()));
            setFirstReadyToPlay(true);
        }else {
            ReceivedChat receivedChat = new ReceivedChat("/first player ready to play", firstPlayer,false);
            sendMessageSerialized(receivedChat);
        }
    }

    /**
     /**
     * Falls Server SendChat Nachricht empfangen hat, dann überprüft er, ob "to" von SendChat Nachricht -1 ist.
     * Falls true, schickt er ReceivedChat Nachricht für alle; Falls falsch, schickt er private Nachricht an jemanden.
     * @author Tingyue
     */
    public void handleSendChatMessage(SendChat sendChatMessage){
        int toSomeOne = sendChatMessage.getTo();

        if( toSomeOne == -1){
            if (sendChatMessage.getMessage().equals("/Start game")){
                //add AI in players list
                relatedServer.getPlayerReadyToPlay().addAll(relatedServer.getAIQueue());
                createGame();


            } else {
                String messageContent;
                if (sendChatMessage.getMessage().equals("/first player ready to play")){
                    messageContent = "/first player ready to play";
                }else {
                    String senderName = relatedServer.getNameViaClientID(getClientID());
                    messageContent = senderName + "(ID: " + getClientID() + "): " +sendChatMessage.getMessage();
                }
                ReceivedChat receivedChat = new ReceivedChat(messageContent, getClientID(),false);
                relatedServer.broadcast(receivedChat);
            }

        }else {
            handlePersonalChatMessage(sendChatMessage);
        }
    }



    /**
     * Liest aus der Privatnachricht den Empfänger raus, wobei dieser mit allen möglichen clientID vom Server abgeglichen wird
     * Falls es nur ein möglich clientID gibt, dann schickt message zu diesem Player.
     * @author David, Tingyue
     */
    private void handlePersonalChatMessage(SendChat message) {
        int recipientClientID = message.getTo();

        ArrayList<Integer> possibleRecipients = new ArrayList<>();//mögliche recipients, alle Mitglieder des Servers

        for (ServerThread connection: this.relatedServer.connections) {
            possibleRecipients.add(connection.getClientID());
        }

        possibleRecipients.removeIf(clientID -> recipientClientID != clientID); //prüft ob clientID eines Recipients nach dem @ steht und entfernt clientID,

        switch (possibleRecipients.size()) {
            case 0 -> {
                Error error = new Error("The Player you want to send a private message to cannot be found. Please check if the player is still in the room or not.");
                sendMessageSerialized(error);
            }
            case 1 -> { //Der Name ist das erste und einzige Element
                if(!relatedServer.getServerThreadViaClientID(recipientClientID).isAI()){
                    String senderName = relatedServer.getNameViaClientID(getClientID());
                    String messageContent = senderName + "(ID: "+ getClientID()+ ")" + " an dich: " + message.getMessage();
                    ReceivedChat receivedChat = new ReceivedChat(messageContent,getClientID(),true);
                    relatedServer.sendMessageSerializedToSomeOne(receivedChat, recipientClientID);
                }else {
                    Error error = new Error("The Player you want to send a private message to is an AI. You cannot chat with an AI.");
                    sendMessageSerialized(error);
                }
            }
            default -> {}
        }
    }

    /**
     * @author Melanie
     * Erstelle Spiel und dazugehörige Daten
     */
    public void createGame(){

        if(relatedServer.getGame() == null){
            Gameboard gameboard = null;
            switch (relatedServer.getSelectedMap()){
                case "Start: Dizzy Highway":  gameboard = new BoardGenerator().generateDizzyHighway();
                    logger.info("Dizzy Highway erzeugt");
                    break;
                case "Advanced: Death Trap":  gameboard = new BoardGenerator().generateDeathTrap();
                    break;
                case "RobotsMustDie: Extra Crispy": gameboard = new BoardGenerator().generateExtraCrispy();
                    break;
                case "Intermediate: Lost Bearings": gameboard = new BoardGenerator().generateLostBearings();
                    break;
                case "Intermediate: Twister" : gameboard = new BoardGenerator().generateTwister();
                    break;
                //TODO: Weiter Karten Hinzufügen wenn verfügbar
            }

            relatedServer.initializePlayerQueue();

            Game currentGame = new Game(gameboard, relatedServer.getPlayerQueue(), relatedServer);
            relatedServer.setGame(currentGame);
            currGame = currentGame;
            //currentGame = currGame;

            BoardConverter boardConverter = new BoardConverter();
            ArrayList<ArrayList<ArrayList<ReducedField>>> gameBoard = boardConverter.convertToReducedList(relatedServer.getGame().getGameboard());
            GameStarted gameStarted = new GameStarted(gameBoard);
            relatedServer.broadcast(gameStarted);

            currGame.setUpGame();
            countForNonAI = 0;
            relatedServer.getGame().setCurrentPlayer(player);
            CurrentPlayer currentPlayer = new CurrentPlayer(player.getPlayerID());
            relatedServer.broadcast(currentPlayer);
        }
    }

    /**
     * Trennen die Verbindung und informieren andere darüber, dass du gegangen bist.
     * close BufferedRead, PrintWriter, socket und Thread
     * @author David, Lea, Tingyue
     */
    public void shutdownConnection()  {
        try {
            messagesFromUser.close();
            messagesForUser.close();

            //thread.interrupt();
            //thread = null;
            socket.close();

        } catch (Exception e){

        }
    }



    /**
     * Versendet eine Nachricht serialisiert an den korrespondierenden Client
     * @param message Nachricht vom Typ Message
     * @author David
     */
    public void sendMessageSerialized(Message message) {
        if (!isAI) {
            String serializedMessage = Serializer.serializeMessage(message);
            logger.info("Sent Message (" + clientID + "): " + serializedMessage);
            sendMessage(serializedMessage);
        }
    }

    public void sendMessage(String message){
        try {
            messagesForUser.println(message); //Gibt den Inhalt (String) aus
        } catch (Exception e) {
            ConnectionUpdate connectionUpdate = new ConnectionUpdate(getClientID(), false, "Remove");
            handleConnectionUpdate(connectionUpdate);
        }

    }


    /**
     * Deserialisiert Nachrichten
     * @param serializedMessage Serialisierte Nachricht (JSON String)
     * @return Message
     * @author David
     */

    public Message getDeserializedMessage(String serializedMessage) {
        return Serializer.deserializeMessage(serializedMessage);
    }
    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getNickname (){
        return nickname;
    }

    public boolean isClientLeave() {
        return isClientLeave;
    }

    public void setClientLeave(boolean clientLeave) {
        isClientLeave = clientLeave;
    }


    public boolean isFirstReadyToPlay() {
        return isFirstReadyToPlay;
    }

    public void setFirstReadyToPlay(boolean firstReadyToPlay) {
        isFirstReadyToPlay = firstReadyToPlay;
    }

    public Server getRelatedServer() {
        return relatedServer;
    }

    public Socket getSocket() {
        return socket;
    }


    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isAI() {
        return isAI;
    }

    public Energy getEnergy() {
        return energy;
    }

    public Error getNotEnoughEnergy() {
        return notEnoughEnergy;
    }

    public UpgradeBought getUpgradeBought() {
        return upgradeBought;
    }

    public Error getBuyUpgradError(){
        return buyUpgradError;
    }

    public Thread getThread(){
        return thread;
    }
}

