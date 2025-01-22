package server;

import field.reducedFields.ReducedField;
import field.tools.BoardConverter;
import game.Game;
import game.Player;
import game.Robot;
import server.protocol.chatnachrichten.ConnectionUpdate;
import server.protocol.chatnachrichten.Error;
import server.protocol.chatnachrichten.SendChat;
import server.protocol.lobby.*;
import server.protocol.verbindungaufbau.Alive;
import server.protocol.*;
import tools.ServerLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Server, an welchem sich die Clients anmelden und das Spiel läuft
 */
public class Server {
    private static final int CONNECT_PORT = 8888;
    public ArrayList<ServerThread> connections;
    private ArrayList<Thread> threads;

    private HashMap<Integer, Integer> chosenRobots = new HashMap<>();  //String: clientID of player;  int: figure of robot

    private ArrayList<Integer> playerReadyToPlay = new ArrayList<>();
    private ArrayList<String> listOfMaps = new ArrayList<String>();
    ;  //availableMaps
    private String selectedMap = null;

    private Game game;

    private boolean onePlayerIsReady;

    private List<Player> playerQueue;

    private static ArrayList<Integer> figureList;
    private ArrayList<Integer> AIQueue;
    private static int aiCount = 1; //counts the numbers of ai players beginning with 1 (relevant for their name)
    private Boolean timerRunning = false;

    private final int minPlayer = 2;
    private Logger logger = ServerLogger.getLogger();
    private int clientIDFlag;

    public Server() {
        connections = new ArrayList<>();
        threads = new ArrayList<>();
        onePlayerIsReady = false;
        initializeGameMap();
        initFigureList();
        AIQueue = new ArrayList<>();
        clientIDFlag = 1;
    }

    /**
     * Der server.Server stellt eine Verbindung mit dem server.Client her.
     * Erstellen mehrerer Threads, um den server.Client für die Antwort zu bearbeiten.
     *
     * @throws IOException ioexception
     * @author David, Tingyue
     */
    public void startServer() throws IOException {

        ServerSocket serverSocket = new ServerSocket(CONNECT_PORT);

        try {
            while (!serverSocket.isClosed()) {
                Socket connectedSocket = serverSocket.accept();
                if (connectedSocket.isConnected()) {    //TODO: löschen
                    logger.info("isConnected");
                }

                //Create new threads to handle client input and output
                ServerThread serverThread;
                Thread thread = new Thread(serverThread = new ServerThread(connectedSocket, this));
                threads.add(thread);

                //assign clientID
                serverThread.setClientID(clientIDFlag++);

                threads.get(threads.indexOf(thread)).start();
                serverThread.setThread(thread);

                sendAliveMessage(thread, serverThread);
            }
        } catch (Exception e) {
            logger.severe("Problem with Serversocket");
            e.printStackTrace();
            serverSocket.close();

        }
    }

    public void initializeGameMap() {
        listOfMaps.add("Start: Dizzy Highway");
        //listOfMaps.add("Beginner: Risky Crossing");
        //listOfMaps.add("Beginner: High Octane");
        //listOfMaps.add("Beginner: Sprint Cramp");
        //listOfMaps.add("Beginner: Corridor Blitz");
        //listOfMaps.add("Beginner: Fractionation");
        //listOfMaps.add("Intermediate: Burnout");
        listOfMaps.add("Intermediate: Lost Bearings");
        //listOfMaps.add("Intermediate: Passing Lanes");
        listOfMaps.add("Intermediate: Twister");
        //listOfMaps.add("Advanced: Dodge This");
        //listOfMaps.add("Advanced: Chop Shop Challenge");
        //listOfMaps.add("Advanced:Undertow");
        //listOfMaps.add("Advanced: Heavy Merge Area");
        listOfMaps.add("Advanced: Death Trap");
        //listOfMaps.add("RobotsMustDie: Pilgrimage");
        //listOfMaps.add("RobotsMustDie: Gear Stripper");
        listOfMaps.add("RobotsMustDie: Extra Crispy");
        //listOfMaps.add("RobotsMustDie: Burn Run");
    }

    /**
     * before game started, init all ready player inclusive AI
     *
     * @author Tingyue
     */
    public void initializePlayerQueue() {
        if (playerQueue == null) {
            playerQueue = new ArrayList<>();
        }

        for (int clientID : getPlayerReadyToPlay()) {
            ServerThread currentServerThread = getServerThreadViaClientID(clientID);
            Robot robot = new Robot(chosenRobots.get(clientID));

            Player player;
            if (currentServerThread.isAI()) {
                player = new Player(clientID, currentServerThread.getNickname(), robot, true);
                player.setServerThread(currentServerThread);
            } else {
                player = new Player(clientID, currentServerThread.getNickname(), robot, false);
            }
            robot.setRobotPlayer(player);
            playerQueue.add(player);
            currentServerThread.setPlayer(player);
        }
    }

    public void initFigureList() {
        if (figureList == null) {
            figureList = new ArrayList<>();
            for (int i = 1; i < 7; i++) {
                figureList.add(i);
            }

        }
    }

    public void endGame() {
        this.game = null;
        this.selectedMap = null;
        this.playerQueue = new ArrayList<>();
        this.AIQueue = new ArrayList<>();
        resetAICount();

        for (ServerThread s: connections) {
            SetStatus setStatusFalse = new SetStatus(false);
            s.handleSetStatusMessage(setStatusFalse);
        }
    }

    /**
     * Sendet der Server Alive Nachricht regelmäßig alle 5 Sekunden
     *
     * @param thread       und serverThread  current thread , zu überprüfen immer ob diese thread alive ist
     * @param serverThread entsprechenden serverThread, um alive message zu schicken
     * @author Tingyue
     */
    public void sendAliveMessage(Thread thread, ServerThread serverThread) {

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);
        executor.scheduleAtFixedRate((new Runnable() {
            @Override
            public void run() {
                if (thread.isAlive() && serverThread.getSocket().isConnected() && !serverThread.isClientLeave() && !serverThread.isAI()) {
                    Alive alive = new Alive();
                    String serializedMessage = Serializer.serializeMessage(alive);
                    serverThread.sendMessage(serializedMessage);

                } else {
                    executor.shutdown();
                }
            }
        }), 0, 5, TimeUnit.SECONDS);

    }


    /**
     * Sendet eine Nachricht serialisiert an einen bestimmten ServerThread, welcher per clientID identifiziert werden kann
     *
     * @param clientID Recipient
     * @param message  Nachricht
     * @author David. Tingyue
     */
    public void sendMessageSerializedToSomeOne(Message message, int clientID) {
        getServerThreadViaClientID(clientID).sendMessageSerialized(message);
    }

    /**
     * Sucht einen ServeThread via ClientID raus und gibt diesen zurück
     *
     * @param clientID clientID des gesuchten ServerThreads
     * @return Gesuchter ServerThread
     * @author David, Tingyue
     */
    public ServerThread getServerThreadViaClientID(int clientID) {
        for (ServerThread connection : connections) {
            if (connection.getClientID() == clientID) {
                return connection;
            }
        }
        return null;
    }

    /**
     * Sucht nach dem entsprechenden Namen für ClientID und gibt diesen zurück
     *
     * @param clientID clientID des gesuchten ServerThreads
     * @return String gesuchte Name von Client
     * @author Tingyue
     */
    public String getNameViaClientID(int clientID) {
        for (ServerThread connection : connections) {
            if (connection.getClientID() == clientID) {
                return connection.getNickname();
            }
        }
        return null;
    }

    /**
     * Veranlasst <b>alle</b> verbundenen ServerThreads Nachrichten an die Clients zu schicken
     *
     * @param message zu verschickende Nachricht (noch unserialisiert)
     * @author Lea
     */
    public void broadcast(Message message) {
        String serializedMessage = Serializer.serializeMessage(message);
        logger.info("Sent message to All: " + serializedMessage);
        for (ServerThread c : connections) {
            if (c != null && !c.isAI()) {
                c.sendMessage(serializedMessage);
            }
        }
    }

    /**
     * Nachricht wird an alle anderen Chatteilnehmer außer dem Teilnehmer, der zum übergebenen serverThread gehört
     *
     * @param message      zu verschickende Nachricht (noch unserialisiert)
     * @param serverThread serverThread, der keine Nachricht versenden soll
     * @author Lea
     */
    public void sendMessageToEveryoneElse(Message message, ServerThread serverThread) {
        String serializedMessage = Serializer.serializeMessage(message);
        logger.info("Sent Message to everyone except " + serverThread.getClientID() + ": " + serializedMessage);
        for (ServerThread c : connections) {
            if (c != null && !(c.equals(serverThread)) && !c.isAI()) {
                c.sendMessage(serializedMessage);
            }

        }
    }

    /**
     * If a new player joins, tell him about the robots that has been selected and the players that is ready
     *
     * @param serverThread current ServerThread
     * @author Tingyue
     */
    public void informAllPlayerStatus(ServerThread serverThread) {
        Iterator<Map.Entry<Integer, Integer>> entries = chosenRobots.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry<Integer, Integer> entry = entries.next();
            PlayerAdded playerAdded = new PlayerAdded(entry.getKey(), getNameViaClientID(entry.getKey()), entry.getValue());
            serverThread.sendMessageSerialized(playerAdded);
        }

        for (int clientID : playerReadyToPlay) {
            PlayerStatus playerStatus = new PlayerStatus(clientID, true);
            serverThread.sendMessageSerialized(playerStatus);

        }

        for (int id : AIQueue) {
            PlayerStatus playerStatus = new PlayerStatus(id, true);
            serverThread.sendMessageSerialized(playerStatus);
        }

        if (game != null) {
            BoardConverter boardConverter = new BoardConverter();
            ArrayList<ArrayList<ArrayList<ReducedField>>> gameBoard = boardConverter.convertToReducedList(getGame().getGameboard());
            GameStarted gameStarted = new GameStarted(gameBoard);
            serverThread.sendMessageSerialized(gameStarted);
        }

    }


    /**
     * Falls der erste Spieler die Bereitschaft widerruft oder das Spiel verlässt, ist der nächste Spieler an der Reihe
     *
     * @author Tingyue
     */
    public void searchNextPlayerToChooseMap() {
        int currentFirstID = getPlayerReadyToPlay().get(0);
        getServerThreadViaClientID(currentFirstID).setFirstReadyToPlay(true);
        sendMessageSerializedToSomeOne(new SendChat("/first player ready to play", currentFirstID), currentFirstID);

    }

    public void assignMapRandomly() {
        Random random = new Random();
        int randomNum = random.nextInt(5) + 1;
        switch (randomNum) {
            case 1 -> setSelectedMap("Start: Dizzy Highway");
            case 2 -> setSelectedMap("Advanced: Death Trap");
            case 3 -> setSelectedMap("RobotsMustDie: Extra Crispy");
            case 4 -> setSelectedMap("Intermediate: Lost Bearings");
            case 5 -> setSelectedMap("Intermediate: Twister");
        }

    }

    public ArrayList<Integer> getPlayerReadyToPlay() {
        return playerReadyToPlay;
    }

    public HashMap<Integer, Integer> getChosenRobots() {
        return chosenRobots;
    }

    public ArrayList<String> getListOfMaps() {
        return listOfMaps;
    }

    public String getSelectedMap() {
        return selectedMap;
    }

    public void setSelectedMap(String selectedMap) {
        this.selectedMap = selectedMap;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }


    public List<Player> getPlayerQueue() {
        return playerQueue;
    }

    public boolean isOnePlayerIsReady() {
        return onePlayerIsReady;
    }

    public void setOnePlayerIsReady(boolean onePlayerIsReady) {
        this.onePlayerIsReady = onePlayerIsReady;
    }


    public static ArrayList<Integer> getFigureList() {
        return figureList;
    }

    public static void setFigureList(ArrayList<Integer> figureList) {
        Server.figureList = figureList;
    }

    public ArrayList<Integer> getAIQueue() {
        return AIQueue;
    }

    public Boolean isTimerRunning() {
        return timerRunning;
    }

    public void setTimerRunning(boolean timerRunning) {
        this.timerRunning = timerRunning;
    }

    public int generateNewAINameNumber() {
        return aiCount++;
    }

    /**
     * If the player leaves before game started, remove his information from all relevant lists
     * @author Tingyue
     */
    public void removePlayerDataBeforeGameStarted(ServerThread serverThread){
        //Remove the player from the list of prepared players
        getPlayerReadyToPlay().remove((Object) serverThread.getClientID());

        //If he is the first player to be ready, set the next player as the first player
        if (!getPlayerReadyToPlay().isEmpty() && serverThread.isFirstReadyToPlay() && game == null){
            searchNextPlayerToChooseMap();
        }

        if (getPlayerReadyToPlay().size() == 0){
            setSelectedMap(null);
        }

        //Remove from the list of selected robots
        getFigureList().add(getChosenRobots().get(serverThread.getClientID()));
        getChosenRobots().remove(serverThread.getClientID());

        //shut down all connections of AI, if only AI there
        if (connections.size() == AIQueue.size()){
            for (int id: AIQueue){
                shutdownAIConnection(getServerThreadViaClientID(id));
            }
            AIQueue.clear();
        }
    }
    /**
     * If only AIs left, remove theirs information from all relevant lists and shut down connection
     * @author Tingyue
     */
    public void shutdownAIConnection(ServerThread serverThread){
        if (getPlayerReadyToPlay().contains(serverThread.getClientID())){
            getPlayerReadyToPlay().remove((Object) serverThread.getClientID());
        }

        getFigureList().add(getChosenRobots().get(serverThread.getClientID()));
        getChosenRobots().remove(serverThread.getClientID());

        this.connections.remove(serverThread);
        if (this.connections.size() > 0){
            broadcast(new ConnectionUpdate(serverThread.getClientID(),false,"Remove"));
        }

        serverThread.shutdownConnection();
    }
    /**
     * remove player from list in class Game and ProgrammingPhase and ActivationPhase
     *
     * @author Tingyue
     */
    protected void removePlayer(int clientID) {
        if (game != null && playerQueue != null) {
            try {
                for (Player p : game.getPlayerQueue()) {
                    if (p.getPlayerID() == clientID) {
                        try {
                            playerQueue.remove(p);
                            game.getPlayerQueue().remove(p);
                            if (game.getProgrammingPhase() != null) {
                                game.getProgrammingPhase().getPlayerList().remove(p);
                            }

                            if (game.getActivationPhase() != null) {
                                game.getActivationPhase().getPlayerList().remove(p);
                            }


                        } catch (ConcurrentModificationException e) {
                            logger.info("Player was removed, while someone is looping");
                        }
                    }
                }
            } catch (ConcurrentModificationException e) {
                logger.info("Player was removed, while someone is looping");
            }
        }

    }

    /**
     * stop game if not enough player are present
     * @author Tingyue Lea
     */

    public void stopGameIfNotEnoughPlayersArePresent() {
        if (game != null){
            if (connections.size() == 1 && AIQueue.isEmpty()) {
                Error error = new Error("You are currently the only player, the game cannot be continued.");
                broadcast(error);

            } else if (connections.size() == AIQueue.size()){
                for (int id: AIQueue){
                    getServerThreadViaClientID(id).handleConnectionUpdate(new ConnectionUpdate(id,false,"Remove"));
                }
                endGame();
                logger.warning("The current game is stopped because only AIs are present");
            }
        }

    }

    public int getClientIDFlag() {
        return clientIDFlag;
    }

    public void setClientIDFlag(int clientIDFlag) {
        this.clientIDFlag = clientIDFlag;
    }

    public ArrayList<ServerThread> getConnections() {
        return connections;
    }
    
    public void resetAICount()
    {
        aiCount = 1;
    }

    public ArrayList<Thread> getThreads() {
        return threads;
    }
}

    /**
     * Die Klasse server. MainServer wird verwendet, um den server.
     * Server zu erstellen und das Programm zu starten.
     * @author Tingyue
     */
    class MainServer {
        public static void main(String[] args) throws IOException {
            Server server = new Server();
            server.startServer();
        }
    }