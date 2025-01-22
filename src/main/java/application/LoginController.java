package application;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.animation.ScaleTransition;
import field.reducedFields.ReducedField;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import server.ClientHandler;
import javafx.scene.control.TextField;
import server.PlayerData;
import server.protocol.chatnachrichten.ConnectionUpdate;
import server.protocol.lobby.PlayerValues;
import tools.ClientLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Controller for login and choosing a robot
 * @author Tingyue,Jihyun
 */
public class LoginController {
    @FXML
    private Button selectRobot;
    @FXML
    private Button robot1;

    @FXML
    private Button robot2;

    @FXML
    private Button robot3;

    @FXML
    private Button robot4;

    @FXML
    private Button robot5;

    @FXML
    private Button robot6;

    @FXML
    private ImageView robotHead;

    @FXML
    private ImageView robotImage;

    @FXML
    private Label testLabel;

    @FXML
    private Button next;

    @FXML
    private Label errorMsg;
    @FXML
    private TextField userName;

    @FXML
    private JFXDialog dialog;

    @FXML
    private StackPane stack;

    @FXML
    private JFXDialog smallDialog;

    @FXML
    private Label content;

    @FXML
    private Label header;

    @FXML
    private JFXButton closeBtn;

    @FXML
    private JFXButton closeBtnBig;

    @FXML
    private Label contentBig;

    @FXML
    private Label headerBig;

    private int figur;

    private boolean loginRoomOpen;



    private int finalFigur;
    private String name;
    private ClientHandler clientHandler;
    private ListChangeListener <PlayerData> playersListener;
    private  ChangeListener<ArrayList<ArrayList<ArrayList<ReducedField>>>> gameboardListener;
    private Logger logger = ClientLogger.getLogger();
    /**
     * As soon as the window opens, connect with clientHandler and
     * inti robots that have already been selected are not allowed to be clicked
     */
    public void initialize(){
        loginRoomOpen = true;
        setClientHandler(StartController.getClientHandler());

        //all chosenRobot set disable
        for (PlayerData player :clientHandler.getClientData().getPlayers()) {
            if (player.getFigure() != 0){
                getChooseRobotBtn(player.getFigure()).setDisable(true);
            }
        }

        addListener();
        initRobotHover();
        addHoverEffect(1, 1.1, 150, selectRobot);
        addHoverEffect(1, 1.1, 150, next);

        dialog.setDialogContainer(stack);
        smallDialog.setDialogContainer(stack);
        dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
        smallDialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
    }

    /**
     * @author Stephan
     * adds the hoverEffect to all robot buttons
     */
    private void initRobotHover() {
        double oldScale = 1;
        double scale = 1.2;
        double time = 150;
        addHoverEffect(oldScale, scale, time, robot1);
        addHoverEffect(oldScale, scale, time, robot2);
        addHoverEffect(oldScale, scale, time, robot3);
        addHoverEffect(oldScale, scale, time, robot4);
        addHoverEffect(oldScale, scale, time, robot5);
        addHoverEffect(oldScale, scale, time, robot6);
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

    /**
     * Add Listener to observe changes of PlayerData
     * @author Lea
     */
    private void addListener(){
        playersListener = new ListChangeListener<PlayerData>() {
            @Override
            public void onChanged(Change<? extends PlayerData> change) {
                try {
                    while (change.next()){
                        if (change.wasAdded() || change.wasRemoved()){
                            handleChangesInPlayer();
                        }
                        logger.fine("Change of used robots was added.");
                    }

                } catch (IOException e) {
                    logger.severe("Change in Players could not be handled");
                    throw new RuntimeException(e);
                }
            }
        };

        gameboardListener = (observableValue, arrayLists, t1) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    next.setDisable(true);
//                        openPopUpWindowForGameStarted("The game has started, you will leave the game.");
                    headerBig.setText("Game started");
                    contentBig.setText("The game has started, you will leave the game.");
                    dialog.show();
                    closeBtnBig.setOnAction(ActionEvent-> dialog.close());
                }
            });

        };

        clientHandler.getClientData().getPropertyChosenMap().addListener(gameboardListener);
        clientHandler.getClientData().playersProperty().addListener(playersListener);
    }


    public void openPopUpWindowForGameStarted(String errorMsg) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("PopUpWindow.fxml"));
        Parent root = (Parent)loader.load();
        PopUpWindowController popUpWindowController = loader.getController();
        popUpWindowController.setPopUpWindowForError("Game Started", errorMsg);
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            try {
                ConnectionUpdate connectionUpdate = new ConnectionUpdate(clientHandler.getClientID(), false, "Remove");
                clientHandler.sendMessageSerialized(connectionUpdate);
                backToStart(stage);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    /**
     * event handler for a button to go to the waiting room
     * @param event
     * @throws IOException
     */
    @FXML
    void switchToWaitingRoom(ActionEvent event) throws IOException {
        String uName = userName.getText();

        if(uName.length() != 0 && getFigur() != 0 && getFinalFigur() != 0) {
            logger.fine("WaitingRoom will be opened");
            clientHandler.setName(uName);
            PlayerValues playerValues = new PlayerValues(uName, getFinalFigur());
            clientHandler.sendMessageSerialized(playerValues);

            Stage currStage = (Stage) next.getScene().getWindow();
            openWaitingRoom(currStage);

        }else {
            headerBig.setText("Finish your login");
            contentBig.setText("Please check if you have written your username and chosen a robot.");
            dialog.show();
            closeBtnBig.setOnAction(ActionEvent-> dialog.close());
//            openPopUpWindow(next);
            logger.fine("Waiting Room could not be opened");
        }

    }

    public void openWaitingRoom(Stage stage) throws IOException {
        clientHandler.getClientData().playersProperty().removeListener(playersListener);
        clientHandler.getClientData().getPropertyChosenMap().removeListener(gameboardListener);

        stage.close();
        closeAllPopUp();
        ChatWindowController.setClientHandler(clientHandler);
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("NewWaitingRoom.fxml"));
        Scene scene = new Scene(root);
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Waiting Room");
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
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        Rectangle2D bounds = Screen.getPrimary().getBounds();
        double maxX = bounds.getMaxX();
        double maxY = bounds.getMaxY();
        double font = maxY / 90;
        if(maxY < 900) {
            root.styleProperty().set("-fx-font-size: 10px;");
        } else {
            root.styleProperty().set("-fx-font-size: " + font + "px;");
        }

        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            try {
                primaryStage.close();
                System.exit(0);
                //ConnectionUpdate connectionUpdate = new ConnectionUpdate(clientHandler.getClientID(), false, "Remove");
                //clientHandler.sendMessageSerialized(connectionUpdate);
                //clientHandler.shutdownClient();
                //backToStart(primaryStage);

            } catch (Exception e) {
                logger.severe("Leaving of the client could not be handled.");
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
        stage.close();
        Stage waitingRoomStage = (Stage) next.getScene().getWindow();
        waitingRoomStage.close();
        Parent backToStart = FXMLLoader.load(getClass().getClassLoader().getResource("Start.fxml"));
        Scene scene = new Scene(backToStart);
        Stage startStage = new Stage();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    startStage.getIcons().add(new Image(getClass().getClassLoader().getResource("images/board/blue_1.png").openStream()));
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



    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public Button getChooseRobotBtn(int figur) {
        switch(figur) {
            case 1:
                return robot1;
            case 2:
                return robot2;
            case 3:
                return robot3;
            case 4:
                return robot4;
            case 5:
                return robot5;
        }
        return robot6;
    }

    /**
     * event handler for buttons to see robot images
     * @param actionEvent
     * @throws IOException
     */
    public void chooseRobotColor(ActionEvent actionEvent) throws IOException {
        Node object = (Node) actionEvent.getSource();
        String robotNum = object.getId();
        robotImage.setFitHeight(170);
        robotImage.setPreserveRatio(true);
        robotHead.setFitHeight(170);
        robotHead.setPreserveRatio(true);
        switch(robotNum) {
            case "robot1":
                Image robotb1 = new Image(getClass().getClassLoader().getResource("images/robots/robotbody1.png").openStream());
                Image robotH1 = new Image(getClass().getClassLoader().getResource("images/robots/robothead1.png").openStream());
                robotImage.setImage(robotb1);
                robotHead.setImage(robotH1);
                setFigur(Integer.parseInt(robot1.getText()));
                break;
            case "robot2":
                Image robotb2 = new Image(getClass().getClassLoader().getResource("images/robots/robotbody2.png").openStream());
                Image robotH2 = new Image(getClass().getClassLoader().getResource("images/robots/robothead2.png").openStream());
                robotImage.setImage(robotb2);
                robotHead.setImage(robotH2);
                setFigur(Integer.parseInt(robot2.getText()));
                break;
            case "robot3":
                Image robotb3 = new Image(getClass().getClassLoader().getResource("images/robots/robotbody3.png").openStream());
                Image robotH3 = new Image(getClass().getClassLoader().getResource("images/robots/robothead3.png").openStream());
                robotImage.setImage(robotb3);
                robotHead.setImage(robotH3);
                setFigur(Integer.parseInt(robot3.getText()));
                break;
            case "robot4":
                Image robotb4 = new Image(getClass().getClassLoader().getResource("images/robots/robotbody4.png").openStream());
                Image robotH4 = new Image(getClass().getClassLoader().getResource("images/robots/robothead4.png").openStream());
                robotImage.setImage(robotb4);
                robotHead.setImage(robotH4);
                setFigur(Integer.parseInt(robot4.getText()));
                break;
            case "robot5":
                Image robotb5 = new Image(getClass().getClassLoader().getResource("images/robots/robotbody8.png").openStream());
                Image robotH5 = new Image(getClass().getClassLoader().getResource("images/robots/robothead8.png").openStream());
                robotImage.setImage(robotb5);
                robotHead.setImage(robotH5);
                setFigur(Integer.parseInt(robot5.getText()));
                break;
            case "robot6":
                Image robotb6 = new Image(getClass().getClassLoader().getResource("images/robots/robotbody6.png").openStream());
                Image robotH6 = new Image(getClass().getClassLoader().getResource("images/robots/robothead6.png").openStream());
                robotImage.setImage(robotb6);
                robotHead.setImage(robotH6);
                setFigur(Integer.parseInt(robot6.getText()));
                break;
        }
    }

    /**
     * event handler for select Button to choose the robot
     * @param actionEvent
     * @throws IOException
     */
    public void selectRobotBtnClicked(ActionEvent actionEvent) throws IOException {
        if(getFigur() != 0 ) {
            setFinalFigur(getFigur());
            getChooseRobotBtn(getFinalFigur()).setDisable(true);
            selectRobot.setDisable(true);
            testLabel.setText("Robot " +getFigur()+ " has been selected.");
        }else {
            header.setText("Choose a robot");
            content.setText("Please choose a robot before you click this button.");
            smallDialog.show();
            closeBtn.setOnAction(actionEvent1 -> smallDialog.close());
//            openPopUpWindow2(selectRobot);
        }
    }

    /**
     * a popupwindow when a player clicks choose Button without choosing any robot
     * @param btn
     * @throws IOException
     */
    public void openPopUpWindow2(Button btn) throws IOException { //special popupwindow that appears, only when the player clicks the select button without choosing any robot yet.
        logger.fine("Player chose select-button without choosing a robot");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("PopUpWindow.fxml"));
        Parent root = (Parent) loader.load();
        PopUpWindowController popUpWindowController = loader.getController();
        popUpWindowController.setPopUpClickedBtnFigure(btn);
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
        saveStagesInArray(stage);
        stage.setScene(scene);
        stage.show();
    }

    ArrayList<Stage> arrayOfStages = new ArrayList<Stage>();
    public void saveStagesInArray(Stage stage) {
        arrayOfStages.add(stage);
    }

    public void closeAllPopUp() {
        if(arrayOfStages != null) {
            for (Stage stage : arrayOfStages) {
                stage.close();
            }
        }
    }
    /**
     * a popupwindow when another player has chosen the same robot  as this player has chosen and already went to waiting room
     * @param btn
     * @throws IOException
     */
    public void openPopUpWindow(Button btn) throws IOException {// popupwindow that appears, only when multiple players choose the same robot
        logger.fine("Another player chose the selected robot");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getClassLoader().getResource("PopUpWindow.fxml"));
                    Parent root = (Parent) loader.load();
                    PopUpWindowController popUpWindowController = loader.getController();
                    popUpWindowController.setPopUpClickedBtn(btn);
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
                    saveStagesInArray(stage);
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    public void handleChangesInPlayer() throws IOException {
        ArrayList<PlayerData> currentPlayers = clientHandler.getClientData().getPlayers();
        boolean[] usedFigures = {false, false, false, false, false, false};
        for (PlayerData p : clientHandler.getClientData().getPlayers()) {
            usedFigures[p.getFigure()] = true;
            if (p.getClientID() != clientHandler.getClientID()) {
                //The same robot was selected at the same time
                if (getChooseRobotBtn(p.getFigure()).isDisable()){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            header.setText("Robot already taken");
                            content.setText("This robot has already been chosen. Please choose another robot.");
                            smallDialog.show();
                            closeBtn.setOnAction(ActionEvent-> smallDialog.close());
                        }
                    });
//                    openPopUpWindow(selectRobot);

                    setFigur(0);
                    setFinalFigur(0);
                    selectRobot.setDisable(false);

                }else {
                    getChooseRobotBtn(p.getFigure()).setDisable(true);
                }
            }
        }
        for (int i = 0; i < 6; i++){
            if (usedFigures[i] == false){
                getChooseRobotBtn(i).setDisable(false);
            }
        }
    }

        public int getFigur () {
            return figur;
        }

        public void setFigur ( int figur){
            this.figur = figur;
        }

        public String getName () {
            return name;
        }

        public void setName (String name){
            this.name = name;
        }
        public int getFinalFigur() {
            return finalFigur;
        }

        public void setFinalFigur(int finalFigur) {
            this.finalFigur = finalFigur;
        }

    }

