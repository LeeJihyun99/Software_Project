package server;

import card.Card;
import field.reducedFields.ReducedAntenna;
import field.reducedFields.ReducedField;
import field.tools.Position;
import server.protocol.DummyMessage;
import server.protocol.Message;
import server.protocol.Serializer;
import server.protocol.aktionen.*;
import server.protocol.chatnachrichten.ConnectionUpdate;
import server.protocol.chatnachrichten.Error;
import server.protocol.chatnachrichten.ReceivedChat;
import server.protocol.chatnachrichten.SendChat;
import server.protocol.lobby.*;
import server.protocol.spielkarten.CardPlayed;
import server.protocol.spielkarten.PlayCard;
import server.protocol.spielzug.*;
import server.protocol.verbindungaufbau.Alive;
import server.protocol.verbindungaufbau.HelloClient;
import server.protocol.verbindungaufbau.HelloServer;
import server.protocol.verbindungaufbau.Welcome;
import tools.ClientLogger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Kümmert sich um die Kommunikation von der GUI zum Server
 * @author Tingyue
 */
public class ClientHandler {
    private Socket socket;
    private String name = "";
    private int clientID;
    private BufferedReader inputFromServer;
    private BufferedReader inputFromUser;
    private PrintWriter messagesForServer;
    private String ip = "127.0.0.1";
    //private String ip = "sep21.dbs.ifi.lmu.de";
    private int port = 8888;
    //private int port = 52020;

    private Message copyOfNewestMessage = null;
    
    private ArrayList<Message> logOfMessages = new ArrayList<>();
    private PropertyChangeSupport messageSupport;
    private Thread thread;
    private ClientData clientData;
    private boolean isAI;
    private Logger logger = ClientLogger.getLogger();

    public ClientHandler() {
        clientData = new ClientData();
        messageSupport = new PropertyChangeSupport(this);
    }

    /**
     * Erstellen socket, bufferedReader(für Eingabe) und PrintWriter(für Ausgabe)
     * @throws IOException ioexception
     * @author Tingyue, Lea
     */
    public void setUpConnection() throws IOException {
        socket = new Socket(ip, port);

        socket.isConnected(); //TODO: löschen

        inputFromUser = new BufferedReader(new InputStreamReader(System.in));
        messagesForServer = new PrintWriter(socket.getOutputStream(), true);
        inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * verarbeitet eingehender Nachrichten von server
     * @throws IOException ioexception
     * @author David, Tingyue
     */
    public void handleIncomingChatInThread() {
        AtomicBoolean end = new AtomicBoolean(false);
        synchronized (this){
           thread = new Thread(() -> {
               try {
                   int number = 0;
                   while (socket.isConnected()){
                       String serializedMessage = inputFromServer.readLine();
                       logger.info("Received Message(" + number + "): " + serializedMessage);
                       number++;
                       Message message = getDeserializedMessage(serializedMessage);

                       if (message instanceof HelloClient){
                           HelloServer helloServer = new HelloServer("EdleEisbecher", isAI() , "Version 2.0");
                           //HelloServer helloServer = new HelloServer("EdleEisbecher", false, "Version 0.2"); //falsche Protocol Version
                           sendMessageSerialized(helloServer);
                       } else if (message instanceof Alive) {
                           sendMessageSerialized(new Alive());

                       } else if (message instanceof Welcome welcome){
                           setClientID(((Welcome) message).getClientID());
                           clientData.setYourPlayerData(welcome.getClientID());
                           clientData.setYourClientID(welcome.getClientID());
                           clientData.addPlayer(clientData.getYourPlayerData());

                       }  else if (message instanceof PlayerAdded playerAdded){

                           if (playerAdded.getClientID() == getClientID()){
                               clientData.getYourPlayerData().setName(playerAdded.getName());
                               clientData.getYourPlayerData().setFigure(playerAdded.getFigure());

                               if (playerAdded.getName().startsWith("AI")){
                                   clientData.getYourPlayerData().setAI(true);
                               }

                               //send default status true
                               SetStatus setStatus = new SetStatus(true);
                               sendMessageSerialized(setStatus);

                           }else {
                               PlayerData newPlayer = new PlayerData(playerAdded.getClientID(), playerAdded.getFigure(), clientData);
                               newPlayer.setName(playerAdded.getName());
                               clientData.addPlayer(newPlayer);

                               if (clientData.getPlayer1Data() == null){
                                   clientData.setPlayer1Data(newPlayer);

                                   if (playerAdded.getName().startsWith("AI")){
                                       clientData.getPlayer1Data().setAI(true);
                                   }
                               } else if (clientData.getPlayer2Data() == null){
                                   clientData.setPlayer2Data(newPlayer);

                                   if (playerAdded.getName().startsWith("AI")){
                                       clientData.getPlayer2Data().setAI(true);
                                   }
                               } else if (clientData.getPlayer3Data() == null){
                                   clientData.setPlayer3Data(newPlayer);

                                   if (playerAdded.getName().startsWith("AI")){
                                       clientData.getPlayer3Data().setAI(true);
                                   }
                               } else if (clientData.getPlayer4Data() == null){
                                   clientData.setPlayer4Data(newPlayer);

                                   if (playerAdded.getName().startsWith("AI")){
                                       clientData.getPlayer4Data().setAI(true);
                                   }
                               } else if (clientData.getPlayer5Data() == null){
                                   clientData.setPlayer5Data(newPlayer);

                                   if (playerAdded.getName().startsWith("AI")){
                                       clientData.getPlayer5Data().setAI(true);
                                   }
                               } else {
                                   Error error = new Error("The number of players has reached its maximum. ");
                                   clientData.setLatestErrorMessage(error);
                                   clientData.setFatalErrorOccured(true);
                               }

                           }

                       } else if (message instanceof PlayerStatus){
                           setNewMessage(message);
                           for (PlayerData p :clientData.getPlayers()){
                               if (((PlayerStatus) message).getClientID() == p.getClientID()){
                                   p.setReady(((PlayerStatus) message).getReady());
                               }
                           }

                       } else if (message instanceof SelectMap selectMap ){
                           clientData.setAvailableMaps(selectMap.getAvailableMaps());

                       } else if (message instanceof GameStarted gameStarted){
                          for (PlayerData p: clientData.getPlayers()){
                              if (!p.isReady()){
                                  clientData.removePlayer(p);
                              }
                          }
                          clientData.setChosenMap(((GameStarted) message).getGameMap());
                          for (ArrayList<ArrayList<ReducedField>> arrayLists: gameStarted.getGameMap()){
                              for (ArrayList<ReducedField> arrayList : arrayLists){
                                  for (ReducedField field : arrayList){
                                      if (field instanceof ReducedAntenna antenna){
                                          clientData.setAntennaOrientation(antenna.getOrientations().get(0));
                                          Position position = new Position(gameStarted.getGameMap().indexOf(arrayLists), arrayLists.indexOf(arrayList));
                                          clientData.setAntennaPosition(position);
                                      }
                                  }
                              }
                           }
                          setNewMessage(message);

                       } else if(message instanceof SendChat sendChat){
                        //   waitInThread();
                           setNewMessage(message);

                           if (sendChat.getMessage().equals("/first player ready to play")) {
                               setFirstPlayerForReady(message);
                           }

                       } else if (message instanceof ReceivedChat receivedChat) {
                           setNewMessage(message);
                           clientData.setLatestChatMessage(receivedChat);

                           if (receivedChat.getMessage().equals("/first player ready to play")) {
                               setFirstPlayerForReady(message);
                           }

                       } else if (message instanceof Error){
                           setNewMessage(message);
                           clientData.setLatestErrorMessage((Error) message);

                       } else if (message instanceof ConnectionUpdate connectionUpdate) {
                           for (PlayerData p: clientData.getPlayers()) {
                               if (connectionUpdate.getClientID() == p.getClientID()){
                                   clientData.setLastDisconnected(p);
                                   clientData.removePlayer(p);
                                   Error error = new Error(p.getName() + "(ID: " + p.getClientID() + ") left the game.");
                                   clientData.setLatestErrorMessage(error);

                                   if (clientData.getPlayer1Data() != null ){
                                       if (clientData.getPlayer1Data().getClientID() == p.getClientID()){
                                           clientData.setPlayer1Data(null);
                                       }
                                   }else if (clientData.getPlayer2Data() != null){
                                       if (clientData.getPlayer2Data().getClientID() == p.getClientID()){
                                           clientData.setPlayer2Data(null);
                                       }
                                   }else if (clientData.getPlayer3Data() != null){
                                       if (clientData.getPlayer3Data().getClientID() == p.getClientID()){
                                           clientData.setPlayer3Data(null);
                                       }
                                   }else if (clientData.getPlayer4Data() != null){
                                       if (clientData.getPlayer4Data().getClientID() == p.getClientID()){
                                           clientData.setPlayer4Data(null);
                                       }
                                   }else if (clientData.getPlayer5Data() != null){
                                       if (clientData.getPlayer5Data().getClientID() == p.getClientID()){
                                           clientData.setPlayer5Data(null);
                                       }
                                   }
                               }
                           }


                       } else if (message instanceof MapSelected mapSelected){
                           clientData.setSelectedMap(mapSelected.getMap());
                           setNewMessage(message);

                       } else if (message instanceof CardPlayed cardPlayed){
                           setNewMessage(message);
                           for (PlayerData p : clientData.getPlayers()){
                               if (p.getClientID() == cardPlayed.getClientID()){
                                   //p.addPlayedCard(cardPlayed.getCard());
                               }
                           }
                       } else if(message instanceof PlayCard playCard){

                       } else if (message instanceof CurrentPlayer currentPlayer){
                           for (PlayerData p : clientData.getPlayers()){
                               if (p.getClientID() == currentPlayer.getClientID()){
                                   p.setCurrentPlayer(true);
                               } else {
                                   p.setCurrentPlayer(false);
                               }
                               logger.config("Current Player: " + p.getClientID() + p.isCurrentPlayer());
                           }
                       } else if (message instanceof ActivePhase activePhase){
                           if(activePhase.getPhase() == 1){
                               clientData.roundCounter();
                               String[] resetRegistry = new String[5];
//                               if (clientData.getNewlyBoughtCardWithPlayer() != null){
//                                   clientData.setNewlyBoughtCardWithPlayer(null);
//                               }

                               for(PlayerData p: clientData.getPlayers()){
                                   for(int i=0; i< 5; i++){
                                       p.setOneRegisterFilled(i,false);
                                       resetRegistry[i] = "";
                                   }
                                   p.setPlayedCards(resetRegistry);
                                   p.getRegistersFilled().clear();
                                   p.setRegister0(null);
                                   p.setRegister1(null);
                                   p.setRegister2(null);
                                   p.setRegister3(null);
                                   p.setRegister4(null);
                               }
                               clientData.setCurrentRegister(0);
                           } else if (activePhase.getPhase() == 2) {
                               clientData.setUpgradeShopOpen(false);
                           }
                           clientData.setCurrentPhase(activePhase.getPhase());

                       } else if (message instanceof StartingPointTaken startingPointTaken){
                           setNewMessage(message);
                           for (PlayerData p : clientData.getPlayers()){
                               if (p.getClientID() == startingPointTaken.getClientID()){
                                   Position position= new Position (startingPointTaken.getX(), startingPointTaken.getY());
                                   p.setPosition(position);
                                   p.setOrientation("top");
                                   switch (clientData.getSelectedMap())
                                   {
                                       case "Start: Dizzy Highway", "Intermediate: Lost Bearings", "RobotsMustDie: Extra Crispy", "Intermediate: Twister" -> {
                                           p.setOrientation("right");
                                       }
                                       case "Advanced: Death Trap" -> {
                                           p.setOrientation("left");
                                       }
                                   }
                               }
                           }
                       } else if (message instanceof YourCards yourCards){
                           if(yourCards.getCardsInHand().size()==9) {
                               clientData.setCardsInHand(yourCards.getCardsInHand());
                               for (PlayerData p : clientData.getPlayers()){
                                   if (p.getClientID() == clientData.getYourClientID()){
                                       p.setCards(yourCards.getCardsInHand().size());
                                   }
                               }
                           }else if(yourCards.getCardsInHand().size()==3) {//for memoryswap yourcards with 3 new cards
                               clientData.setExchangeCards(yourCards.getCardsInHand());
                           }

                       } else if (message instanceof NotYourCards notYourCards){
                            setNewMessage(notYourCards);
                           for (PlayerData p : clientData.getPlayers()){
                               if (p.getClientID() == notYourCards.getClientID()){
                                   p.setCards(notYourCards.getCardsInHand());
                               }
                           }
                       } else if (message instanceof ShuffleCoding shuffleCoding){
                           for (PlayerData p : clientData.getPlayers()){
                               if (p.getClientID() == shuffleCoding.getClientID()){
                                   clientData.setLatestGameMessage("The player "+ p.getName() + " ("+ p.getClientID() + ") has shuffled their cards");
                               }
                           }
                       } else if (message instanceof CardSelected cardSelected){
                           for (PlayerData p : clientData.getPlayers()){
                               if (p.getClientID() == cardSelected.getClientID()){
                                   p.setOneRegisterFilled(cardSelected.getRegister(), cardSelected.getFilled());
                               }
                           }
                       } else if (message instanceof SelectionFinished selectionFinished){
                           for (PlayerData p : clientData.getPlayers()){
                               if (p.getClientID() == selectionFinished.getClientID()){
                                   clientData.setLatestGameMessage("The player "+ p.getName() + " ("+ p.getClientID() + ") has finished the selection of cards of their register.\n");
                               }
                           }
                       } else if (message instanceof TimerStarted){
                           clientData.setLatestGameMessage("The timer has started, only 30 seconds are left to select your cards.\n");
                           clientData.setTimerIsRunning(true);
                       } else if (message instanceof TimerEnded timerEnded){
                           clientData.setTimerIsRunning(false);
                           ArrayList <PlayerData> notFinishedPlayers = new ArrayList<>();
                           for (PlayerData p : clientData.getPlayers()){
                               for (int i : timerEnded.getClientIDs()){
                                   if (p.getClientID() == i){
                                       notFinishedPlayers.add(p);
                                   }
                               }
                           }
                           if (notFinishedPlayers.size()==0){
                               clientData.setLatestGameMessage("The timer has ended. All players have finished their selection in time.\n");
                           } else if(notFinishedPlayers.size() == 1){
                               clientData.setLatestGameMessage("The timer has ended. Only " + notFinishedPlayers.get(0).getName()
                                       + " (" + notFinishedPlayers.get(0).getClientID() + ") has not finished in time.\n");
                           } else {
                               String gameMessage = "The timer has ended. The players";
                               for (PlayerData p : notFinishedPlayers){
                                   if (notFinishedPlayers.size() == 1){
                                       gameMessage = gameMessage + "and " + p.getName() + " (" + p.getClientID() + ") ";
                                       notFinishedPlayers.remove(p);
                                   } else {
                                       gameMessage = gameMessage + p.getName() + " (" + p.getClientID() + ") ";
                                   }
                               }
                               gameMessage = gameMessage + " have not finished in time\n";
                               clientData.setLatestGameMessage(gameMessage);
                           }
                       } else if (message instanceof CardsYouGotNow cardsYouGotNow){
                           clientData.getYourPlayerData().setPlayedCards(cardsYouGotNow.getCards().toArray(new String[5]));

                           //clientData.setCardsInHand(cardsYouGotNow.getCards());
                       } else if (message instanceof CurrentCards currentCards){
                           setNewMessage(currentCards);
                           int currentRegister = clientData.getCurrentRegister();
                           for (ActiveCard a : currentCards.getCurrentCards()){
                               for (PlayerData p : clientData.getPlayers()){
                                   if (a.getClientID() == p.getClientID()){
                                       p.setPlayedCard(clientData.getCurrentRegister(), a.getCard());
                                   }
                               }
                           }
                           currentRegister++;
                           clientData.setCurrentRegister(currentRegister);
                           StringBuilder string1 = new StringBuilder("Spielerreihenfolge: ");
                           for (PlayerData p : clientData.getPlayersInAntennaOrder()){
                               string1.append(p.getClientID() + ", ");
                           }
                           logger.info(string1.toString());
                           /*
                           System.out.println("Current Register: " + clientData.getCurrentRegister());
                           for (PlayerData p : clientData.getPlayers()){
                               StringBuilder stringBuilder = new StringBuilder("");
                               stringBuilder.append(p.getClientID() + ": ");
                               if (p.getRegister0() != null){
                                   stringBuilder.append("Register 0: " + p.getRegister0().getCardName());
                               } else {
                                   stringBuilder.append("Register 0: null");
                               }
                               if (p.getRegister1() != null){
                                   stringBuilder.append(", Register 1: " + p.getRegister1().getCardName());
                               } else {
                                   stringBuilder.append(", Register 1: null");
                               }
                               if (p.getRegister2() != null){
                                   stringBuilder.append(", Register 2: " + p.getRegister2().getCardName());
                               } else {
                                   stringBuilder.append(", Register 2: null");
                               }
                               if (p.getRegister3() != null){
                                   stringBuilder.append(", Register 3: " + p.getRegister3().getCardName());
                               } else {
                                   stringBuilder.append(", Register 3: null" );
                               }
                               if (p.getRegister4() != null){
                                   stringBuilder.append(", Register 4: " + p.getRegister4().getCardName());
                               } else {
                                   stringBuilder.append(", Register 4: null" );
                               }
                               System.out.println(stringBuilder.toString());
                           }

                            */
                       } else if (message instanceof ReplaceCard replaceCard){
                           for (PlayerData p : clientData.getPlayers()){
                               if (replaceCard.getClientID() == p.getClientID()){
                                   p.setPlayedCard(replaceCard.getRegister(), replaceCard.getNewCard());
                               }
                           }
                       } else if (message instanceof Movement movement){
                           boolean positionChanged = false;
                           for (PlayerData p : clientData.getPlayers()){
                               if (p.getClientID() == movement.getClientID()){
                                   Position position = new Position (movement.getX(), movement.getY());
                                   p.setPosition(position);
                                   positionChanged = true;
                               }
                           }
                           assert(positionChanged = true);
                       } else if (message instanceof PlayerTurning playerTurning){
                           setNewMessage(message);
                           boolean orientationChanged = false;
                           //System.out.println(this.getClass()+": instanceOf vor for-Schleife");
                           for (PlayerData p : clientData.getPlayers()){
                               //System.out.println(this.getClass()+": instanceOf in for-Schleife");
                               if (p.getClientID() == playerTurning.getClientID()){
                                   if (playerTurning.getRotation().equals("counterclockwise")){
                                       // p.getDirection().changeDirectionCounterClockwise();
                                       p.changeOrientationCounterclockwise();

                                   } else if (playerTurning.getRotation().equals("clockwise")){
                                       p.changeOrientationClockwise();
                                       //p.getDirection().changeDirectionClockwise();
                                   } else {
                                       logger.warning("Direction could not be changed");
                                   }

                                   orientationChanged = true;
                               }
                           }
                           assert(orientationChanged = true);
                       } else if (message instanceof Animation animation){
                           clientData.setAnimation(animation.getType());
                       } else if (message instanceof Reboot reboot){
                           for (PlayerData p : clientData.getPlayers()){
                               if (p.getClientID() == reboot.getClientID()){
                                   p.increaseRebootCount();
                               }
                           }
                           setNewMessage(message);
                           clientData.setLastRebooted(reboot.getClientID());
                           //TODO Möglichkeit, die Positionen zu ändern, aus dem Game klauen
                       } else if (message instanceof RebootDirection rebootDirection){
                           setNewMessage(message);
                           for (PlayerData p : clientData.getPlayers()){
                               if (p.getClientID() == clientData.getLastRebooted()){
                                   p.setOrientation(rebootDirection.getDirection());
                                   sendMessageSerialized(rebootDirection);
                               }
                           }
                       } else if (message instanceof Energy energy){
                           handleEnergyMessage(energy);

                       } else if(message instanceof CheckPointReached checkPointReached){
                           setNewMessage(checkPointReached);
                           for (PlayerData p : clientData.getPlayers()){
                               if (p.getClientID() == checkPointReached.getClientID()){
                                   p.setReachedCheckpoints(checkPointReached.getNumber());
                               }
                           }

                       } else if(message instanceof GameFinished gameFinished){
                           setNewMessage(gameFinished);
                           clientData.setGameFinished(true);
                           for (PlayerData p : clientData.getPlayers()) {
                               if (p.getClientID() == gameFinished.getClientID()) {
                                   clientData.setLatestGameMessage(p.getName() + " (" + p.getClientID() + ") has won the game.");
                                   p.setFirstForReady(false);
                               }
                           }


                       } else if (message instanceof DrawDamage drawnDamage){
                           for (PlayerData p : clientData.getPlayers()) {
                               if (p.getClientID() == drawnDamage.getClientID()){
                                   String tmp = "";
                                   ArrayList<Card> drawnDamageCards = new ArrayList<>();
                                   for (String s : drawnDamage.getCards()){
                                       drawnDamageCards.add(Card.getCardFromString(s));
                                       if (!tmp.equals("")) {
                                           tmp += ", ";
                                       }
                                       tmp += s;
                                   }
                                   p.setDrawnDamageCards(drawnDamageCards);
                                   //TODO: Delete when another reaction is available in GUI
                                   clientData.setLatestGameMessage("Player " + p.getClientID() + " has drawn the following DamageCards: " + tmp + "\n");
                               }
                           }

                       } else if (message instanceof PickDamage pickDamage){
                           ArrayList<Card> availableDamageCards = new ArrayList<>();
                           String tmp = "";
                           for (String s : pickDamage.getAvailablePiles()){
                               availableDamageCards.add(Card.getCardFromString(s));
                               if (!tmp.equals("")) {
                                   tmp += ", ";
                               }
                               tmp += s;

                           }
                           clientData.setAvailableDamageCards(availableDamageCards);
                           clientData.setDamageCardCount(pickDamage.getCount());
                           //TODO: Delete when other reaction is finished in GUI
                           clientData.setLatestGameMessage("You have to pick the following amount of damage: " +
                                   pickDamage.getCount() + ". \n Please select from the following available Piles: " + tmp + "\n");

                       } else if (message instanceof RefillShop refillShop){
                           ArrayList<Card> cards = new ArrayList<>();
                           for (String cardName : refillShop.getCards()){
                               Card card = Card.getCardFromString(cardName);
                               cards.add(card);
                           }
                           clientData.addUpgradeShopContent(cards);
                           logger.config("UpgradeShop State: " + clientData.upgradeShopOpenProperty().get());
                           clientData.setUpgradeShopOpen(true);
                           logger.config("UpgradeShop State: " + clientData.upgradeShopOpenProperty().get());
                           for (Card card : clientData.getUpgradeShopContent()){
                               logger.config("Karte im UpgradeShop: " + card.getCardName());
                           }

                       } else if (message instanceof ExchangeShop exchangeShop){
                           /*
                           for (int i = 0; i < clientData.getUpgradeShopContent().size(); i++){
                               clientData.getUpgradeShopContent().remove(i);
                           }

                            */

                           clientData.getUpgradeShopContent().clear();

                           for (Card card : clientData.getUpgradeShopContent()){
                               logger.config("Karte im UpgradeShop: " + card.getCardName());
                           }

                           for (String cardName : exchangeShop.getCards()){
                               Card card = Card.getCardFromString(cardName);
                               clientData.getUpgradeShopContent().add(card);
                           }
                           logger.config("UpgradeShop State: " + clientData.upgradeShopOpenProperty().get());
                           clientData.setUpgradeShopOpen(true);
                           logger.config("UpgradeShop State: " + clientData.upgradeShopOpenProperty().get());

                           for (Card card : clientData.getUpgradeShopContent()){
                               logger.config("Karte im UpgradeShop: " + card.getCardName());
                           }

                       } else if (message instanceof UpgradeBought upgradeBought) {
                           if (upgradeBought.getCard() != null) {
                               Card card = Card.getCardFromString(upgradeBought.getCard());
                               int cardIndexToDelete = -1;
                               for (int i = 0; i < clientData.getUpgradeShopContent().size(); i++){
                                   if ( clientData.getUpgradeShopContent().get(i).getCardName().equals(upgradeBought.getCard())){
                                       cardIndexToDelete = i;
                                   }
                               }

                               if (cardIndexToDelete != -1){
                                   clientData.getUpgradeShopContent().remove(cardIndexToDelete);
                               }

                               for (Card c: clientData.getUpgradeShopContent()){
                                   logger.config("Card in UpgradeShop " + c.getCardName());
                               }
                               clientData.setLatestGameMessage("Client " + upgradeBought.getClientID() + " bought the UpgradeCard: " +
                                       upgradeBought.getCard() + ".\n");
                               if (clientID == upgradeBought.getClientID()) {
                                   clientData.getBoughtUpgradeCards().add(card);
                               }
                               clientData.addNewlyBoughtCard(upgradeBought.getClientID(), card);
                           } else {
                               clientData.addNewlyBoughtCard(upgradeBought.getClientID(), null);
                           }


                       }else if(message instanceof CheckpointMoved checkpointMoved) {
                            clientData.setCheckpointPosition(checkpointMoved.getX(), checkpointMoved.getY(), checkpointMoved.getCheckpointID());
                       } else {
                           //TODO: Problem finden
                           //logger.getLogger().warning("Message of type " + message.getClass() + " could not be processed");
                       }
                   }

               } catch (IOException e) {
                   /*try {
                       //logger.getLogger().info("Client (" + clientID + ") closed");
                       shutdownClient();
                   } catch (IOException ex) {
                       logger.getLogger().severe("IOException: Client (" + clientID + ") closed");
                       throw new RuntimeException(ex);
                   }
                   e.printStackTrace();

                    */
               }
           });
           thread.start();
        }
    }

    private void handleEnergyMessage(Energy energy){
        String source = energy.getSource();
        for (PlayerData p : clientData.getPlayers()){
            if (p.getClientID() == energy.getClientID()){
                switch (source){
                    case "AdminPrivilege","MemorySwap", "RearLaser", "SpamBlocker":
                        p.energyCounter(energy.getCount(), "minus");
                        break;
                    default:
                        p.energyCounter(energy.getCount(), "plus");
                }
            }
        }
    }


    /**
     * If the first player message is received, update client data
     * @author Tingyue
     */
   public void setFirstPlayerForReady(Message message) throws IOException {
       if (message instanceof SendChat sendChat) {
           SendChat firstPlayerInfo = new SendChat(sendChat.getMessage(), -1);
           sendMessageSerialized(firstPlayerInfo);

       }else if (message instanceof ReceivedChat receivedChat){
           for (PlayerData p :clientData.getPlayers()){
               if (receivedChat.getFrom() == p.getClientID()){
                   p.setFirstForReady(true);
               }else{
                   p.setFirstForReady(false);
               }
           }
       }
   }


    /**
     * Gibt die Kopie der neuesten empfangenen Nachricht aus
     * @return aktuellste Nachricht
     */
    public Message getCopyOfNewestMessage() {
        return copyOfNewestMessage;
    }

    /**
     * verarbeitet ausgehender Nachrichten von server
     * @throws IOException
     * @author Tingyue
     */
    public void handleMessagesFromUser(String chatMessage) throws IOException {
        if (chatMessage.charAt(0) == '@') {
            String content = chatMessage.substring(chatMessage.indexOf(" ")+1);
            try {
                String to = chatMessage.substring(chatMessage.indexOf("@")+1, chatMessage.indexOf(" "));
                SendChat personalChatMessage= new SendChat(content, Integer.parseInt(to));
                sendMessageSerialized(personalChatMessage);

            }catch (NumberFormatException | StringIndexOutOfBoundsException e){
                SendChat groupChatMessage = new SendChat(chatMessage, -1);
                sendMessageSerialized(groupChatMessage);
            }

        } else {
            SendChat groupChatMessage = new SendChat(chatMessage, -1);
            sendMessageSerialized(groupChatMessage);

        }
    }


    /**
     * close BufferedRead, PrintWriter, socket und Thread
     * @throws IOException
     * @author David, Tingyue
     */

    public void shutdownClient() throws IOException {
        inputFromUser.close();
        inputFromServer.close();
        messagesForServer.close();

        thread.interrupt();

        socket.close();


    }

    public void sendMessageSerialized(Message message) throws IOException {
        try {
            Serializer serializer = new Serializer();
            //logger.getLogger().info(serializer.serializeMessage(message));
            String serializedMessage = serializer.serializeMessage(message);
            messagesForServer.println(serializedMessage); //Gibt den Inhalt (String) aus
            logger.info("Sent Message: " + serializedMessage);

        }catch (Exception e){
            shutdownClient();
        }

    }

    /**
     * Deserialisiert die Nachricht
     * @param serializedMessage serialisierte Nachricht
     * @return deserialisierte Nachricht vom Typ Message
     * @author David
     */
    public Message getDeserializedMessage(String serializedMessage) throws IOException {
        try{
            Serializer serializer = new Serializer();
            return serializer.deserializeMessage(serializedMessage);
         }catch (Exception e){
           // logger.getLogger().warning("Message could not be deserialized in clienthandler");
            return new DummyMessage();
        }
    }

    public void addMessageSupport(PropertyChangeListener listener){
        messageSupport.addPropertyChangeListener(listener);
    }

    public void removeMessageSupport(PropertyChangeListener listener){
        messageSupport.removePropertyChangeListener(listener);
    }

    public void setNewMessage(Message message){
        messageSupport.firePropertyChange("New Chatmessage", this.copyOfNewestMessage, message);
        this.copyOfNewestMessage = message;
        logOfMessages.add(copyOfNewestMessage);
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ClientData getClientData() {
        return clientData;
    }

    public void setClientData(ClientData clientData) {
        this.clientData = clientData;
    }

    public boolean isAI() {
        return isAI;
    }

    public void setAI(boolean AI) {
        isAI = AI;
    }


    public void resetNewestMessage() {
        copyOfNewestMessage = null;
    }
    
    public ArrayList<Message> getLogOfMessages()
    {
        return logOfMessages;
    }

}

