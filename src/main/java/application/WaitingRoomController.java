package application;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import field.reducedFields.ReducedField;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import server.ClientHandler;
import server.PlayerData;
import server.protocol.Message;
import server.protocol.chatnachrichten.ConnectionUpdate;
import server.protocol.chatnachrichten.Error;
import server.protocol.chatnachrichten.ReceivedChat;
import server.protocol.chatnachrichten.SendChat;
import server.protocol.lobby.*;
import server.protocol.verbindungaufbau.HelloServer;
import tools.ClientLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.logging.Logger;

/**
 * Controller for a waiting room
 * @author Jihyun, Tingyue
 */
public class WaitingRoomController {

    @FXML
    public VBox waitingroomVBox;
    @FXML
    private GridPane smallGridPane1;
    @FXML
    private GridPane smallGridPane2;
    @FXML
    private GridPane smallGridPane3;
    @FXML
    private GridPane smallGridPane4;
    @FXML
    private GridPane smallGridPane5;
    @FXML
    private GridPane smallGridPane6;
    @FXML
    private ToggleButton toggleBtn;
    @FXML
    private Button gameStart;
    @FXML
    private ImageView mapImage;
    @FXML
    private Button mapSelectedBtn;
    @FXML
    private ComboBox<String> mapChoiceBox;
    @FXML
    private Label selectedMapName;
    @FXML
    private Button AIbtn;

    @FXML
    private JFXButton closeBtnAI;
    @FXML
    private Label contentAI;

    @FXML
    private StackPane stack;

    @FXML
    private JFXDialog dialogAI;

    @FXML
    private JFXDialog dialogReady;
    @FXML
    private Label contentReady;
    @FXML
    private JFXButton noBtn;
    @FXML
    private JFXButton yesBtn;
    @FXML
    private JFXDialog dialogGameStarted;

    @FXML
    private Label contentGameStarted;
    @FXML
    private JFXButton closeBtnGameStarted;

    private ClientHandler clientHandler;
    private String chosenMap;
    private HashMap<Integer, GridPane> smallGridPaneWithClientID ;
    private Stage currentPopUpWindowStage;
    private GridPane currentFirstPlayerSmallGridPane;
    private ArrayList<GridPane> listOfSmallGridPaneOccupied;
    List<String> chosenMapsFromList=new ArrayList<String>();
    ChangeListener<Error> errorChangeListener;
    ChangeListener<ArrayList<ArrayList<ArrayList<ReducedField>>>> gameboardListener;
    ChangeListener<String> selectedMapListener;
    ChangeListener<ReceivedChat> receivedChatChangeListener;
    ListChangeListener<PlayerData> playersListener;
    private Logger logger = ClientLogger.getLogger();

    /**
     * As soon as the window opens, connect with clientHandler and init all element of pane in this window
     * @throws IOException
     */
    public void initialize() throws IOException {
        //init connection to the clientHandler
        setClientHandler(StartController.getClientHandler());
        dialogAI.setDialogContainer(stack);
        dialogReady.setDialogContainer(stack);
        dialogMapSelect.setDialogContainer(stack);
        dialogStart.setDialogContainer(stack);
        dialogChat.setDialogContainer(stack);
        dialogGameStarted.setDialogContainer(stack);
        gameStart.setDisable(true);
        gameStart.setOpacity(0);
        mapSelectedBtn.setDisable(true);
        AIbtn.setDisable(true);
        //init board element for player
        initPaneElement();
        addListener();

        addHoverEffect(1, 1.1, 150, gameStart);
        addHoverEffect(1, 1.1, 150, AIbtn);
        addHoverEffect(1, 1.1, 150, toggleBtn);
        addHoverEffect(1, 1.1, 150, mapSelectedBtn);

    }

    /**
     * Add Listeners to observe changes in Model
     * @author Lea
     */
    private void addListener(){
        errorChangeListener = (observableValue, oldValue, newValue) -> handleError(newValue);
        gameboardListener = (observableValue, arrayLists, t1) -> handleGameStartedMessage();
        selectedMapListener = (observableValue, oldValue, newValue) -> handleMapSelectedMessage(newValue);
        receivedChatChangeListener = (observableValue, oldValue, newValue) -> handleFirstPlayerMessage(newValue);
        playersListener = change -> {
            logger.fine("Change in players-List detected");
            while (change.next()){
                if (change.wasAdded()){
                    handlePlayerAdded();
                } else if (change.wasUpdated()){
                    handlePlayerStatus();
                } else if (change.wasRemoved()){
                    // Get the player who left
                    ArrayList<Integer> removedPlayerIDList = new ArrayList<>();
                    for (PlayerData p: change.getRemoved()) {
                        removedPlayerIDList.add(p.getClientID());
                    }
                    handlePlayerRemoved(removedPlayerIDList);
                }
            }
        };

        clientHandler.getClientData().errorObjectProperty().addListener(errorChangeListener);
        clientHandler.getClientData().getPropertyChosenMap().addListener(gameboardListener);
        clientHandler.getClientData().selectedMapProperty().addListener(selectedMapListener);
        clientHandler.getClientData().receivedChatObjectProperty().addListener(receivedChatChangeListener);
        clientHandler.getClientData().playersProperty().addListener(playersListener);

    }

    /**
     * event handler when first ready player clicks an add AI button
     * @param event
     * @throws IOException
     */
    @FXML
    public void addAI(ActionEvent event) throws IOException {
        System.out.println("size"+ clientHandler.getClientData().getPlayers().size());
        if (clientHandler.getClientData().getPlayers().size() < 6){
            Message helloServer = new HelloServer("EdleEisbecher", true, "Version 2.0");
            try {
                clientHandler.sendMessageSerialized(helloServer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    contentAI.setText("ALl robots are already taken and no more AI can be added.");
                    dialogAI.show();
                    closeBtnAI.setOnAction(ActionEvent -> dialogAI.close());
                }
            });
        }
    }

    /**
     * initialises all the elements in the waiting room.
     * @throws IOException
     */
    public void initPaneElement() throws IOException {
        smallGridPaneWithClientID = new HashMap<>();
        initPlayersInGridPane();
        execute(clientHandler.getClientData().getAvailableMaps());

        //set ready button
        if (clientHandler.getClientData().getYourPlayerData().isReady()){
            toggleBtn.setSelected(true);
        }else {
            toggleBtn.setSelected(false);
            toggleBtn.setText("Ready");
        }

        //if the map is already selected
        if (clientHandler.getClientData().getSelectedMap() != null){
            selectedMapName.setText("Selected: " + clientHandler.getClientData().getSelectedMap());
        }
        if (clientHandler.getClientData().getYourPlayerData().isFirstForReady()){
            mapSelectedBtn.setDisable(false);
            AIbtn.setDisable(false);
        }
    }

    /**
     * initialises specifically the panes in which players who successfully logged in will be included with their information(chosen robot, username with ID, ready, first player)
     * @throws IOException
     */
    public void initPlayersInGridPane() throws IOException {
        int i = 0;
        listOfSmallGridPaneOccupied = new ArrayList<>();
        for (PlayerData p : clientHandler.getClientData().getPlayers()) {
            addPlayersInTheGridPane(getSmallGridPane(i), p.getName(), p.getClientID(), p.getFigure(),p.isReady());
            listOfSmallGridPaneOccupied.add(getSmallGridPane(i));
            logger.info(" " + p.getClientID() + " " + p.isFirstForReady());
            //speichern smallerGridPane in a hashmap, index is clientID
            smallGridPaneWithClientID.put(p.getClientID(),getSmallGridPane(i));

            if( p.isFirstForReady()) {
                setFirstPlayerCrown(getSmallGridPane(i));
            }
            i++;
        }
    }

    public GridPane getSmallGridPane(int i) {
        return switch (i) {
            case 0 -> smallGridPane1;
            case 1 -> smallGridPane2;
            case 2 -> smallGridPane3;
            case 3 -> smallGridPane4;
            case 4 -> smallGridPane5;
            default -> smallGridPane6;
        };
    }

    public String getReadyToString(Boolean ready) {
        if(ready) {
            return "Ready";
        }
        return "Not Ready";
    }

    /**
     * visualises players' information added in the gridpane
     * @param playerSmallGridPane
     * @param name
     * @param clientID
     * @param figure
     * @param isReady
     */
    public void addPlayersInTheGridPane(GridPane playerSmallGridPane, String name, int clientID, int figure, boolean isReady) {
            Node username = playerSmallGridPane.getChildren().get(0);
            Label setPlayerName = (Label) username;
            setPlayerName.setText(name + " (ID: " + clientID + ")");
            // set Ready
            Node readyNotReady = playerSmallGridPane.getChildren().get(1);
            Label setReadyNotReady = (Label) readyNotReady;
            setReadyNotReady.setText(getReadyToString(isReady));

            Node robotColor = playerSmallGridPane.getChildren().get(2);
            Pane setRobotColor = (Pane) robotColor;
            setRobotColor.setStyle("-fx-background-color:" + getRobotColor(figure));
        }

    /**
     * event handler when a player clicks a ready/not ready button
     * @param actionEvent
     * @throws IOException
     */
    public void toggleBtnClicked(javafx.event.ActionEvent actionEvent) throws IOException {
        if (toggleBtn.getText().equals("Not ready")) {
            if (clientHandler.getClientData().getYourPlayerData().isFirstForReady()) { //if the player is the first player
//                openPopUpWindow(toggleBtn);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        contentReady.setText("You cannot choose a map. Are you sure you want to continue?");
                        dialogReady.show();
                        yesBtn.setOnAction(event -> {
                            SetStatus setStatus = new SetStatus(false);
                            try {
                                clientHandler.sendMessageSerialized(setStatus);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            toggleBtn.setSelected(false);
                            toggleBtn.setText("Ready");
                            dialogReady.close();
                        });
                        noBtn.setOnAction(event -> {
                            dialogReady.close();
                        });

                    }
                });
            }else { //if the player is not the first player
                SetStatus setStatus = new SetStatus(false);
                clientHandler.sendMessageSerialized(setStatus);
                toggleBtn.setSelected(false);
                toggleBtn.setText("Ready");
            }
        }else {
            SetStatus setStatus = new SetStatus(true);
            clientHandler.sendMessageSerialized(setStatus);
            toggleBtn.setSelected(true);
            toggleBtn.setText("Not ready");
        }
    }


    public String getRobotColor(int figure) {
        switch(figure) {
            case 1:
                return "orange";
            case 2:
                return "#22B14C";
            case 3:
                return "#00A2E8";
            case 4:
                return "#A349A4";
            case 5:
                return " #817F26";
        }
        return " #7F7F7F";
    }

    /**
     * places a crown image on the current first player
     * @param playerGridPane
     * @throws IOException
     */
    public void setFirstPlayerCrown(GridPane playerGridPane) throws IOException {
        Image crown = new Image(getClass().getClassLoader().getResource("images/crown.png").openStream());
        Node CrownImgView = playerGridPane.getChildren().get(3); //get the firstplayer's smallgridpane(parameter) and add here.
        ImageView addCrownImg = (ImageView)CrownImgView;
        addCrownImg.setImage(crown);
        setFirstPlayerSmallGridPane(playerGridPane);
    }

    /**
     * remove the crown image from the old first player(when the first player has been switched to another)
     * @param oldFirstPlayerPane
     */
    public void removeOldFirstPlayerCrown(GridPane oldFirstPlayerPane) {
        Node CrownImgView = oldFirstPlayerPane.getChildren().get(3); //get the firstplayer's smallgridpane(parameter) and add here.
        ImageView removeCrownImg = (ImageView)CrownImgView;
        removeCrownImg.setImage(null);

    }

    /**
     * listner for choicebox, in which all the avaiable maps are listed
     * @param listOfMaps
     * @throws IOException
     */
    public void execute(ArrayList<String> listOfMaps) throws IOException {
        mapChoiceBox.getItems().addAll(listOfMaps);
        mapChoiceBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
        String selectedMap = newValue;
        setChosenMap(newValue);
        chosenMapsFromList.add(getChosenMap());

            switch(selectedMap) {
                case "Start: Dizzy Highway" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/dizzyhighway.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Beginner: Risky Crossing" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/riskycrossing.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Beginner: High Octane" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/highoctane.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Beginner: Sprint Cramp" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/sprintcramp.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Beginner: Corridor Blitz" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/corridorblitz.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Beginner: Fractionation" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/fractionation.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Intermediate: Burnout" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/burnout.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Intermediate: Lost Bearings" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/lostbearings.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Intermediate: Passing Lanes" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/passinglanes.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Intermediate: Twister" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/twister.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Advanced: Dodge This" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/dodgethis.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Advanced: Chop Shop Challenge" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/chopshopchallenge.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Advanced:Undertow" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/undertow.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Advanced: Heavy Merge Area" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/heavymergearea.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "Advanced: Death Trap" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/deathtrap.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "RobotsMustDie: Pilgrimage" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/pilgrimage.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "RobotsMustDie: Gear Stripper" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/gearstripper.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "RobotsMustDie: Extra Crispy" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/extracrispy.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case "RobotsMustDie: Burn Run" :
                    try {
                        mapImage.setImage(new Image(getClass().getClassLoader().getResource("images/courses/burnrun.png").openStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
            }
        });
    }

    @FXML
    private JFXDialog dialogMapSelect;
    @FXML
    private JFXButton closeBtnMapSelect;
    @FXML
    private Label contentMapSelect;
    /**
     * event handler, when a player clicks the select (for map) Button.
     * @param actionEvent
     * @throws IOException
     */
    public void mapSelectedBtnClicked(ActionEvent actionEvent) throws IOException {
        String finalChosenMap;
        if(chosenMapsFromList.size()==0) {
            contentMapSelect.setText("Please choose a map from the choicebox before you click this button.");
            dialogMapSelect.show();
            closeBtnMapSelect.setOnAction(ActionEvent-> dialogMapSelect.close());

        }else {
            finalChosenMap = chosenMapsFromList.get(chosenMapsFromList.size()-1);
            selectedMapName.setText("Selected: " + finalChosenMap);
            clientHandler.sendMessageSerialized(new MapSelected((finalChosenMap)));
            gameStart.setDisable(false);
            gameStart.setOpacity(1);
        }
    }

    /**
     * If one player comes, add his information to the GUI
     */
    public void handlePlayerAdded(){
        GridPane playerAddedSmallGridPane = getSmallGridPane(getFirstUnoccupiedSmallGridPane());
        PlayerData addedPlayer = clientHandler.getClientData().getPlayers()
                .get(clientHandler.getClientData().getPlayers().size() - 1);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                addPlayersInTheGridPane(playerAddedSmallGridPane,addedPlayer.getName(),addedPlayer.getClientID(),addedPlayer.getFigure(),addedPlayer.isReady());
                logger.fine("Player was added to grid pane");
            }
        });
        smallGridPaneWithClientID.put(addedPlayer.getClientID(), playerAddedSmallGridPane);
        listOfSmallGridPaneOccupied.add(playerAddedSmallGridPane);

    }

    /**
     * handles changes in PlayerStatus
     */
    public void handlePlayerStatus(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (PlayerData player : clientHandler.getClientData().getPlayers()){
                    GridPane currentPlayerPane = smallGridPaneWithClientID.get(player.getClientID());
                    Label setReadyNotReady = (Label) currentPlayerPane.getChildren().get(1);
                    setReadyNotReady.setText(getReadyToString(player.isReady()));
                }

                if(currentFirstPlayerSmallGridPane != null && isNoPlayerSetReady()) {
                    removeOldFirstPlayerCrown(currentFirstPlayerSmallGridPane);
                    mapSelectedBtn.setDisable(true);
                    gameStart.setDisable(true);
                    AIbtn.setDisable(true);
                }
                logger.fine("Player status is changed in grid pane");
            }
        });

    }

    /**
     * handles specifically FirstPlayer message
     * @param receivedChat
     */
    public void handleFirstPlayerMessage(ReceivedChat receivedChat){
        if (receivedChat.getMessage().equals("/first player ready to play")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (currentFirstPlayerSmallGridPane != null) {
                        removeOldFirstPlayerCrown(currentFirstPlayerSmallGridPane);
                    }
                    GridPane smallGridPane = smallGridPaneWithClientID.get(receivedChat.getFrom());
                    try {
                        setFirstPlayerCrown(smallGridPane);
                    } catch (IOException e) {
                        logger.warning("Crown of first player could not be added");
                        throw new RuntimeException(e);
                    }
                }
            });

            if (clientHandler.getClientID() != receivedChat.getFrom()) {
                mapSelectedBtn.setDisable(true);
                gameStart.setDisable(true);
                AIbtn.setDisable(true);
            } else {
                mapSelectedBtn.setDisable(false);
                AIbtn.setDisable(false);
                if (clientHandler.getClientData().getSelectedMap() != null){
                    gameStart.setDisable(false);
                    gameStart.setOpacity(1);
               }
            }
        }
    }


    /**
     * handles specifically MapSelected message from clienthandler
     * @param mapSelected
     */
    public void handleMapSelectedMessage(String mapSelected){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                selectedMapName.setText("Selected: " + mapSelected);
            }
        });

    }

    /**
     * handles specifically GameStart message from clienthandler
     */
    public void handleGameStartedMessage(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Vor If");
                    if (clientHandler.getClientData().getYourPlayerData().isReady()){
                        System.out.println("In if");
                        openMain();
                        logger.info("Main GUI has been opened");

                    }else {
                        contentGameStarted.setText("The game has started, you will leave the game.");
                        dialogGameStarted.show();
                        closeBtnGameStarted.setOnAction(ActionEvent-> dialogGameStarted.close());
                        waitingroomVBox.setDisable(true);
                        //openPopUpWindowForGameStarted("The game has started, you will leave the game.");
                        logger.info("Player (" +clientHandler.getClientID() + ") was not ready when game "
                                + " and therefore will not take part in the game");
                    }

                } catch (IOException e) {

                    logger.severe("A problem occurred when the game was started");
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * open a popupwindow to not ready player when the game has started
     * @param errorMsg
     * @throws IOException
     */
    public void openPopUpWindowForGameStarted(String errorMsg) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("PopUpWindow.fxml"));
        Parent root = (Parent)loader.load();
        PopUpWindowController popUpWindowController = loader.getController();
        popUpWindowController.setPopUpWindowForError("Game Started", errorMsg);
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    stage.getIcons().add(new Image(getClass().getClassLoader().getResource("images/board/blue_1.png").openStream()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            ConnectionUpdate connectionUpdate = new ConnectionUpdate(clientHandler.getClientID(), false, "Remove");
            try {
                clientHandler.sendMessageSerialized(connectionUpdate);
                backToStart(stage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * goes back to start window when this window is closed
     * @param stage
     * @throws IOException
     */
    public void backToStart(Stage stage) throws IOException {
        Stage window = (Stage) gameStart.getScene().getWindow();
        window.close();
        stage.close();
        Parent backToStart = FXMLLoader.load(getClass().getClassLoader().getResource("Start.fxml"));
        Scene scene = new Scene(backToStart);
        Stage startStage = new Stage();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    stage.getIcons().add(new Image(getClass().getClassLoader().getResource("images/board/blue_1.png").openStream()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        startStage.setTitle("Welcome back");
        startStage.setScene(scene);
        startStage.show();

        startStage.setOnCloseRequest(event -> {
            startStage.close();
            System.exit(0);
        });

    }

    @FXML
    private JFXDialog dialogChat;
    @FXML
    private JFXButton closeBtnChat;

    @FXML
    private Label contentChat;
    /**
     * handles specifically Error message from clienthandler
     */
    private void handleError(Error error){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                contentChat.setText(error.getError());
                dialogChat.show();
                closeBtnChat.setOnAction(ActionEvent-> dialogChat.close());
                logger.info("The following error was reported to the user: " + error.getError());
            }
        });

    }

    /**
     * If the player leaves, remove his information from the GUI
     */
    private void handlePlayerRemoved(ArrayList<Integer> removedPlayerIDList) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (int removedPlayerID: removedPlayerIDList) {
                    if (smallGridPaneWithClientID.containsKey(removedPlayerID)){
                        GridPane removedPlayerGridPane = smallGridPaneWithClientID.get(removedPlayerID);

                        Label setPlayerName = (Label) removedPlayerGridPane.getChildren().get(0);
                        setPlayerName.setText("");

                        Label setReadyNotReady = (Label) removedPlayerGridPane.getChildren().get(1);
                        setReadyNotReady.setText("");

                        Pane setRobotColor = (Pane) removedPlayerGridPane.getChildren().get(2);
                        setRobotColor.setStyle("");

                        removeOldFirstPlayerCrown(removedPlayerGridPane);

                        smallGridPaneWithClientID.remove(removedPlayerID);
                        listOfSmallGridPaneOccupied.remove(removedPlayerGridPane);
                    }
                }
            }
        });

    }


    public boolean isNoPlayerSetReady(){
        for (PlayerData p: clientHandler.getClientData().getPlayers()) {
            if (p.isReady() && !p.isAI()){
                return false;
            }
        }
        return true;
    }

    @FXML
    private JFXDialog dialogStart;
    @FXML
    private JFXButton closeBtnStart;
    @FXML
    private Label contentStart;

    /**
     * event handler for a button to go to main
     * @param event
     * @throws IOException
     */
    @FXML
    void switchToMain(ActionEvent event) throws IOException {
        if (getSizeOfPlayersReadyToPlay() >= 2 ){
            clientHandler.getClientData().getPlayers();
            clientHandler.sendMessageSerialized(new SendChat("/Start game", -1));
        }else {
            contentStart.setText("Not enough players to start a game");
            dialogStart.show();
            closeBtnStart.setOnAction(ActionEvent-> dialogStart.close());
        }


    }


    /**
     *
     * @return size of arraylist of players who are ready to play
     */
    int getSizeOfPlayersReadyToPlay(){
        ArrayList<PlayerData> playersReadyToPlay = new ArrayList<>();
        for (PlayerData p: clientHandler.getClientData().getPlayers()) {
            if (p.isReady()){
                playersReadyToPlay.add(p);
            }
        }
        return playersReadyToPlay.size();
    }

    Rectangle2D bounds = Screen.getPrimary().getBounds();
    double screenWidth = bounds.getWidth();
    double screenHeight = bounds.getHeight();

    private void listenToSizeInitialization(ObservableDoubleValue size, DoubleConsumer handler) {
        ChangeListener<Number> listener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs, Number oldSize, Number newSize) {
                if (newSize.doubleValue() != Double.NaN) {
                    handler.accept(newSize.doubleValue());
                    size.removeListener(this);
                }
            }
        };
        size.addListener(listener);
    }


    /**
     * open main window
     * @throws IOException
     */
    void openMain() throws IOException {
        System.out.println("In openMain");
        Stage currStg = (Stage) gameStart.getScene().getWindow();
        System.out.println("stage erhalten");
        currStg.close();
        System.out.println("aktuelle Geschlossen");
        System.out.println("Neue FXML: "+getClass().getResource("/NewMain.fxml").toString());
        Parent root = FXMLLoader.load(getClass().getResource("/NewMain.fxml"));
        System.out.println("neues Root");
        Scene scene = new Scene(root);
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Game Starts");
        primaryStage.setScene(scene);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("images/board/blue_1.png").openStream()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        if(screenHeight < 900) {
            primaryStage.setHeight(screenHeight);
            primaryStage.setWidth(screenWidth);
            primaryStage.setMaxWidth(screenWidth);
            root.styleProperty().set("-fx-font-size: 10px;");
        } else {
           listenToSizeInitialization(primaryStage.widthProperty(),
                   w -> primaryStage.setWidth(screenWidth / 1.2));
           listenToSizeInitialization(primaryStage.heightProperty(),
                   h -> primaryStage.setHeight(screenHeight / 1.2));
            Rectangle2D bounds = Screen.getPrimary().getBounds();
            double maxX = bounds.getMaxX();
            double maxY = bounds.getMaxY();
            double font = maxY / 90;
            root.styleProperty().set("-fx-font-size: " + font + "px;");
        }

        primaryStage.show();

        clientHandler.getClientData().errorObjectProperty().removeListener(errorChangeListener);
        clientHandler.getClientData().getPropertyChosenMap().removeListener(gameboardListener);
        clientHandler.getClientData().selectedMapProperty().removeListener(selectedMapListener);
        clientHandler.getClientData().receivedChatObjectProperty().removeListener(receivedChatChangeListener);
        clientHandler.getClientData().playersProperty().removeListener(playersListener);

        primaryStage.setOnCloseRequest(event -> {
            try {
                primaryStage.close();
                System.exit(0);
                //ConnectionUpdate connectionUpdate = new ConnectionUpdate(clientHandler.getClientID(), false, "Remove");
                //clientHandler.sendMessageSerialized(connectionUpdate);
                //backToStart(primaryStage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }
    public void setFirstPlayerSmallGridPane(GridPane gridpane) {
        currentFirstPlayerSmallGridPane = gridpane;
    }
    public void getStage(Stage stage) {
        currentPopUpWindowStage = stage;
    }
    public int getFirstUnoccupiedSmallGridPane() {
        return listOfSmallGridPaneOccupied.size();
    }

    public String getChosenMap() {
        return chosenMap;
    }

    public void setChosenMap(String chosenMap) {
        this.chosenMap = chosenMap;
    }

    /**
     * @author Stephan
     * adds an animation to the node when the mouse hovers over them
     */
    private void addHoverEffect(double oldScale, double scale, double time, Node node) {
        node.setOnMouseEntered(mouseEvent -> {
            ScaleTransition scalet = new ScaleTransition(Duration.millis(time), node);
            scalet.setFromX(oldScale);
            scalet.setFromY(oldScale);
            scalet.setToX(scale);
            scalet.setToY(scale);
            scalet.play();
        });
        node.setOnMouseExited(mouseEvent -> {
            ScaleTransition scalet = new ScaleTransition(Duration.millis(time), node);
            scalet.setFromX(scale);
            scalet.setFromY(scale);
            scalet.setToX(oldScale);
            scalet.setToY(oldScale);
            scalet.play();
        });
    }

}
