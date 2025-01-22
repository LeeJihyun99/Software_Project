package application;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import field.reducedFields.ReducedField;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import server.ClientHandler;
import server.protocol.Message;
import server.protocol.chatnachrichten.Error;
import tools.ClientLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Jihyun, Tingyue
 * Start window that is shown when a user opens the game
 */
public class StartController{
    @FXML
    public HBox buttonHBox;
    @FXML
    public Label startRobo;
    @FXML
    public Label startRally;
    @FXML
    private Button startBtn;
    @FXML
    private Button quitBtn;
    @FXML
    private Label errorMsg;

    @FXML
    private JFXButton closeBtn;

    @FXML
    private Label content;

    @FXML
    private JFXDialog dialog;

    @FXML
    private StackPane stack;

    private static ClientHandler clientHandler;
    private Message copyOfNewestMessage;
    private ChangeListener<Error> errorChangeListener;
    private ChangeListener<ArrayList<ArrayList<ArrayList<ReducedField>>>> gameboardListener;

    private Logger logger = ClientLogger.getLogger();

    /**
     * Sobald Login fenster öffnet, beginnt der Client, Verbindung aufzubauen.
     * @author Tingyue
     */
    public void initialize(){
        clientHandler = new ClientHandler();
        clientHandler.setAI(false);
        dialog.setDialogContainer(stack);
        try {
            clientHandler.setUpConnection();
        } catch (IOException e) {
            logger.severe("Connection to Server could not be established");
            throw new RuntimeException(e);
        }

        errorChangeListener = (observableValue, oldValue, newValue) -> {
            System.out.println("error msg comes");
            try {
                logger.fine("Change in Error-Property was detected");
                handleErrorMessage(newValue);
            } catch (IOException e) {
                logger.severe("Error Message could be displayed");
                throw new RuntimeException(e);
            }
        };
        clientHandler.getClientData().errorObjectProperty().addListener(errorChangeListener);
        addGameBoardListener();

        clientHandler.handleIncomingChatInThread();

        addHoverEffect(1, 1.1, 150, startBtn);
        addHoverEffect(1, 1.1, 150, quitBtn);
    }

    private void addGameBoardListener(){
        gameboardListener = (observableValue, arrayLists, t1) -> {
            System.out.println("game started");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    startBtn.setDisable(true);
//                        openPopUpWindowForGameStarted("The game has started, you cannot enter the game.");
                    content.setText("The game has started, you cannot enter the game.");
                    dialog.show();
                    closeBtn.setOnAction(ActionEvent-> dialog.close());
                }
            });

        };

        clientHandler.getClientData().getPropertyChosenMap().addListener(gameboardListener);
    }

    public void openPopUpWindowForGameStarted(String content) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("PopUpWindow.fxml"));
        Parent root = (Parent)loader.load();
        PopUpWindowController popUpWindowController = loader.getController();
        popUpWindowController.setPopUpWindowForError("Game Started", content);
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
    }

    private void handleErrorMessage(Error error) throws IOException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (clientHandler.getClientData().isFatalErrorOccured()){
                    startBtn.setDisable(true);
                }
                //startBtn.setDisable(true);   // man kann nicht mehr login button clicked.
                //                    openPopUpWindowPlayersFull(error.getError());

                content.setText(error.getError());
                dialog.show();
                closeBtn.setOnAction(ActionEvent-> dialog.close());
               /* try {
                    openPopUpWindowForError(error.getError());
                } catch (IOException e) {
                    logger.getLogger().severe("Error-Message could not be displayed.");
                    throw new RuntimeException(e);
                }*/
            }
        });

    }

    public void openPopUpWindowForError(String errorMsg) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("PopUpWindow.fxml"));
        Parent root = (Parent)loader.load();
        PopUpWindowController popUpWindowController = loader.getController();
        popUpWindowController.setPopUpWindowForError("Error", errorMsg);
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
    }
    /**
     * event handler for a next button to go to login window
     * @author Jihyun, Tingyue
     * @param actionEvent
     * @throws IOException
     */
    public void switchToLogIn(ActionEvent actionEvent) throws IOException {

        clientHandler.getClientData().errorObjectProperty().removeListener(errorChangeListener);
        clientHandler.getClientData().getPropertyChosenMap().removeListener(gameboardListener);

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("NewLogin.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
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
        stage.setMinHeight(500);
        stage.setMinWidth(900);
        stage.setMaxHeight(500);
        stage.setMaxWidth(900);


        Rectangle2D bounds = Screen.getPrimary().getBounds();
        double maxX = bounds.getMaxX();
        double maxY = bounds.getMaxY();
        double font = maxY / 90;
        if(maxY < 900) {
            root.styleProperty().set("-fx-font-size: 10px;");
        } else {
            root.styleProperty().set("-fx-font-size: " + font + "px;");
        }


        stage.show();
        stage.setOnCloseRequest(event -> {
            try {
                stage.close();
                System.exit(0);
                //backToStart(stage);
                logger.info("Switch to LogIn-Stage was made");

            } catch (Exception e) {
                logger.severe("Switch to LogIn was not possible");
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * goes back to start window, when the current window is closed
     * @param stage
     * @throws IOException
     */
    public void backToStart(Stage stage) throws IOException {
        stage.close();


        Parent backToStart = FXMLLoader.load(getClass().getClassLoader().getResource("Start.fxml"));
        Scene scene = new Scene(backToStart);
        Stage startStage = new Stage();
        startStage.setTitle("Welcome back");
        startStage.setScene(scene);
        startStage.show();
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
        startStage.setOnCloseRequest(event ->{
            startStage.close();
            System.exit(0);
        });

    }
    /**
     * @author Jihyun
     * @param actionEvent a user can quit the came by clicking the button 'quit'
     */
    public void QuitTheGame(ActionEvent actionEvent) throws IOException {
        Stage currStg = (Stage) quitBtn.getScene().getWindow();
        currStg.close();

        System.exit(0); //stop programm
    }


    public static ClientHandler getClientHandler() {
        return clientHandler;
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

