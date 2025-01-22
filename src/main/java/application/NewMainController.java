package application;

import card.Card;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.events.JFXDialogEvent;
import game.Player;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import server.ActiveCard;
import server.ClientHandler;
import server.PlayerData;
import server.protocol.Message;
import server.protocol.aktionen.*;
import server.protocol.chatnachrichten.ConnectionUpdate;
import server.protocol.chatnachrichten.Error;
import server.protocol.spielzug.BuyUpgrade;
import server.protocol.spielzug.CurrentCards;
import tools.ClientLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NewMainController{

    @FXML
    private GridPane player1RegisterGrid;
    @FXML
    private GridPane player2RegisterGrid;
    @FXML
    private GridPane player3RegisterGrid;
    @FXML
    private GridPane player4RegisterGrid;
    @FXML
    private GridPane player5RegisterGrid;
    @FXML
    private JFXDialog winningDialog;
    @FXML
    private HBox winningLabelBox;
    @FXML
    private ImageView player1reg0;
    @FXML
    private ImageView player1reg1;
    @FXML
    private ImageView player1reg2;
    @FXML
    private ImageView player1reg3;
    @FXML
    private ImageView player1reg4;
    @FXML
    private ImageView player2reg0;
    @FXML
    private ImageView player2reg1;
    @FXML
    private ImageView player2reg2;
    @FXML
    private ImageView player2reg3;
    @FXML
    private ImageView player2reg4;
    @FXML
    private ImageView player3reg0;
    @FXML
    private ImageView player3reg1;
    @FXML
    private ImageView player3reg2;
    @FXML
    private ImageView player3reg3;
    @FXML
    private ImageView player3reg4;
    @FXML
    private ImageView player4reg0;
    @FXML
    private ImageView player4reg1;
    @FXML
    private ImageView player4reg2;
    @FXML
    private ImageView player4reg3;
    @FXML
    private ImageView player4reg4;
    @FXML
    private ImageView player5reg0;
    @FXML
    private ImageView player5reg1;
    @FXML
    private ImageView player5reg2;
    @FXML
    private ImageView player5reg3;
    @FXML
    private ImageView player5reg4;
    @FXML
    private StackPane player3stack;
    @FXML
    private StackPane centerStack;
    @FXML
    private Label congratsLabel;
    @FXML
    private Label winningRound;
    @FXML
    private Label winningDamage;
    @FXML
    private Label winningReboot;
    @FXML
    private Label winningUpgrade;
    @FXML
    private HBox winningButtonHbox;
    @FXML
    private Button winningMapRoom;
    @FXML
    private Button winningQuit;
    @FXML
    private Label winningStats;
    @FXML
    private VBox winningStatsBox;
    @FXML
    private VBox smallgrid1box;
     @FXML
     private VBox smallgrid2box;
    @FXML
    public HBox winningButtonHBox;
    @FXML
    private VBox smallgrid3box;
    @FXML
    private VBox smallgrid4box;
    @FXML
    private VBox smallgrid5box;
    @FXML
    private VBox smallgrid6box;
    @FXML
    private HBox bottomHBox;
    @FXML
    private VBox centerVBox;
    @FXML
    private HBox gameboardHBox;
    @FXML
    private VBox leftVBox;
    @FXML
    private ListView<?> listOfProgress;
    @FXML
    private Label phaseLabel;
    @FXML
    private VBox rightVBox;
    @FXML
    private GridPane smallgridpane1;
    @FXML
    private GridPane smallgridpane2;
    @FXML
    private GridPane smallgridpane3;
    @FXML
    private GridPane smallgridpane4;
    @FXML
    private GridPane smallgridpane5;
    @FXML
    private GridPane smallgridpane6;
    @FXML
    private ProgressIndicator timer;
    @FXML
    private Label timerLabel;
    @FXML
    private Label timername;
    @FXML
    private Label mapName;
    @FXML
    private Label round;
    @FXML
    private HBox titel;
    @FXML
    private VBox winningWindow;
    @FXML
    private StackPane stack;
    private ClientHandler clientHandler;
    private HashMap<Integer, GridPane> smallGridPaneWithClientID;
    private ChangeListener<Boolean> timerIsRunningListener;
    private ChangeListener<Number> currentPhaseListener;
    private ChangeListener<Boolean> gameFinishedListener;
    private ChangeListener<Error> errorChangeListener;
    private ChangeListener<Number> energyListener;
    private ChangeListener<Number> checkpointListener;
    private ListChangeListener<PlayerData> playersListener;
    private ChangeListener<Number> cardCountListener;
    private ChangeListener<Card> register0Listener;
    private ChangeListener<Card> register1Listener;
    private ChangeListener<Card> register2Listener;
    private ChangeListener<Card> register3Listener;
    private ChangeListener<Card> register4Listener;

    private ChangeListener<ArrayList<Card>> availableDamageCardsListener;
    private ChangeListener<Number> lastRebootedListener;
    private ChangeListener<Number> currentRegisterListener;

    private ChangeListener<Boolean> upgradeShopOpenListener;

    private ListChangeListener<Card> availableUpgradeListener;

    private static final Integer STARTTIME = 30;
    private Timeline timeline;
    private IntegerProperty timeSeconds = new SimpleIntegerProperty(STARTTIME);

    private ArrayList<Integer> listOfDamageCardCount;
    private ArrayList<ArrayList<Card>> listOfDamageCardsNewValue;
    private Logger logger = ClientLogger.getLogger();
    double delta;
    double oldTranslate;
    double newTranslate;
    double picSize = 60;
    int playerCount;
    int grid1ID = 0;
    int grid2ID = 0;
    int grid3ID = 0;
    int grid4ID = 0;
    int grid5ID = 0;
    int grid6ID = 0;


    public void initialize() throws IOException {
        setClientHandler(StartController.getClientHandler());
        playerCount = clientHandler.getClientData().getPlayers().size();
        listOfDamageCardCount = new ArrayList<>();
        listOfDamageCardsNewValue = new ArrayList<>();
        setDialogContainer();
        initPaneElement();
        //add listener
        addListenerForPhase();
        addGameFinishedListener();
        addErrorMessageListener();
        addCheckPointListener();
        addEnergyCubeListener();
        addPlayersListener();
        addListenerForDamageCards();
        addListenerForLastRebooted();
        addListenerForTimer();
        addRegisterListeners();

        //listeners about upgradeShop
        addListenerForUpgradeShopOpen();
        addListenerForAvailableUpgrade();
        addListenerForBoughtUpgradeCard();
        //addListenerForCurrentPlayer();

        addHoverEffect(1, 1.1, 150, winningMapRoom);
        addHoverEffect(1, 1.1, 150, winningQuit);
        picSize = Screen.getPrimary().getBounds().getMaxY() * picSize / 1080;
        initializePlayerInfoOnHover();
        addBorderToRegister();
    }

    private HashMap<Integer, Label> playerWithCardLabel;
    private HashMap<Integer, Label> playerWithNameLabel;
    private MapChangeListener<Integer, Card> NewlyBoughtCardListener;
    private int currPlayerID;


    private void addListenerForBoughtUpgradeCard(){
        NewlyBoughtCardListener = change -> {
           Platform.runLater(new Runnable() {
               @Override
               public void run() {
                   //System.out.println("listener for boughtupgradecard listened" + change.getKey()+ change.getValueAdded()); //TODO: löschen
                   if (change.wasAdded() ) {
                       if (change.getValueAdded() != null){
                           Label currentCardLabel = playerWithCardLabel.get(change.getKey());
                           currentCardLabel.setText(change.getValueAdded().getCardName());

                           for (String boughtCard : mapCardWithButton.keySet()) {
                               if (boughtCard.equals(change.getValueAdded().getCardName())) {
                                   mapCardWithButton.get(boughtCard).setGraphic(null);
                                   mapCardWithButton.get(boughtCard).setText(null);
                                   break;
                               }

                           }
                       }

                   }

                   /*if (change.wasRemoved()){
                       playerWithNameLabel.get(change.getKey()).setText("");
                       playerWithCardLabel.get(change.getKey()).setText("");
                       try {
                           searchForNextPlayer();
                       } catch (IOException e) {
                           throw new RuntimeException(e);
                       }
                       //TODO: next turn
                   }*/
               }
           });

       };

       clientHandler.getClientData().getNewlyBoughtCardWithPlayer().addListener(NewlyBoughtCardListener);
    }

    private ChangeListener<Boolean> currentPlayerListener;
    private ChangeListener<Boolean> currentPlayerListener2;
    private void addListenerForCurrentPlayer(){
        int numOfPlayer = clientHandler.getClientData().getPlayers().size();
        currentPlayerListener = (booleanProperty, oldValue, newValue) -> {
            logger.info("newValue: "+newValue.booleanValue());
            if (newValue){
                Platform.runLater(() -> {
                    getBuyButton(numOfPlayer).setDisable(false);
                    getNotBuyButton(numOfPlayer).setDisable(false);
                });
            } else {
                Platform.runLater(() -> {
                    getBuyButton(numOfPlayer).setDisable(true);
                    getNotBuyButton(numOfPlayer).setDisable(true);
                });
            }
        };

        clientHandler.getClientData().getYourPlayerData().currentPlayerProperty().addListener(currentPlayerListener);

        currentPlayerListener2 = (observableValue, aBoolean, t1) -> {
            for (PlayerData p : clientHandler.getClientData().getPlayers()){
                if (p.isCurrentPlayer()){
                    Platform.runLater(() -> {
                        getCurrentPlayerLabel(numOfPlayer).setText("Who's turn:  " + p.getName()+ "(ID: " + p.getClientID() +")");
                        currPlayerID = p.getClientID();
                    });
                }
            }
        };


        for (PlayerData p: clientHandler.getClientData().getPlayers()){
            p.currentPlayerProperty().addListener(currentPlayerListener2);
        }


    }
    private Label getCurrentPlayerLabel(int numOfPlayers){
        switch (numOfPlayers) {
            case 2 -> {
                return currentPlayerFor2;
            }
            case 3 -> {
                return currentPlayerFor3;
            }
            case 4 -> {
                return currentPlayerFor4;
            }
            case 5 -> {
                return currentPlayerFor5;
            }
            case 6 -> {
                return currentPlayerFor6;
            }
            default -> {
                return null;
            }
        }
    }

    private JFXDialog getUpgradeDialog(int numOfPlayers){
        switch (numOfPlayers) {
            case 2 -> {
                return dialogUpgradeFor2;
            }
            case 3 -> {
                return dialogUpgradeFor3;
            }
            case 4 -> {
                return dialogUpgradeFor4;
            }
            case 5 -> {
                return dialogUpgradeFor5;
            }
            case 6 -> {
                return dialogUpgradeFor6;
            }
            default -> {
                return null;
            }
        }
    }

    private Button getBuyButton(int numOfPlayers){
        switch (numOfPlayers) {
            case 2 -> {
                return btnUpgradeBuyFor2;
            }
            case 3 -> {
                return btnUpgradeBuyFor3;
            }
            case 4 -> {
                return btnUpgradeBuyFor4;
            }
            case 5 -> {
                return btnUpgradeBuyFor5;
            }
            case 6 -> {
                return btnUpgradeBuyFor6;
            }
            default -> {
                return null;
            }
        }
    }

    private Button getNotBuyButton(int numOfPlayers){
        switch (numOfPlayers) {
            case 2 -> {
                return btnUpgradeNotBuyFor2;
            }
            case 3 -> {
                return btnUpgradeNotBuyFor3;
            }
            case 4 -> {
                return btnUpgradeNotBuyFor4;
            }
            case 5 -> {
                return btnUpgradeNotBuyFor5;
            }
            case 6 -> {
                return btnUpgradeNotBuyFor6;
            }
            default -> {
                return null;
            }
        }
    }

    private void addListenerForAvailableUpgrade() {
        availableUpgradeListener = change -> {
            //System.out.println("availableupgrades listened");
            while (change.next()){
//                if(change.wasUpdated()){
                    ObservableList<Card> observableArrayListForAvailableUpgrade = FXCollections.observableArrayList(clientHandler.getClientData().getUpgradeShopContent());
                    setAvailableUpgradeCards(observableArrayListForAvailableUpgrade.stream().collect(Collectors.toList()));
//                }
            }
        };

        clientHandler.getClientData().getUpgradeShopContent().addListener(availableUpgradeListener);
    }

    private void addListenerForUpgradeShopOpen() {
        upgradeShopOpenListener = (booleanProperty, oldValue, newValue) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    logger.config("UpGradeShop should open " + newValue);
                    addListenerForCurrentPlayer();
                    if (newValue.equals(true)) {
                        setPlayerListInOrderForUpgradeShop(clientHandler.getClientData().getPlayersInAntennaOrder());
//                        ObservableList<Card> observableArrayListForAvailableUpgrade = FXCollections.observableArrayList(clientHandler.getClientData().getUpgradeShopContent());
//                        setAvailableUpgradeCards(observableArrayListForAvailableUpgrade.stream().collect(Collectors.toList()));
                        try {
                            int numOfPlayers = clientHandler.getClientData().getPlayers().size();
                            switch (numOfPlayers) {
                                case 2 -> openUpgradeShopFor2();
                                case 3 -> openUpgradeShopFor3();
                                case 4 -> openUpgradeShopFor4();
                                case 5 -> openUpgradeShopFor5();
                                case 6 -> openUpgradeShopFor6();
                            };
                            if(clientHandler.getClientData().getYourPlayerData().isCurrentPlayer()){
                                logger.info("Ich bin jetzt spieler");
                                getBuyButton(clientHandler.getClientData().getPlayers().size()).setDisable(false);
                                getNotBuyButton(clientHandler.getClientData().getPlayers().size()).setDisable(false);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }else {
                        int numOfPlayers = clientHandler.getClientData().getPlayers().size();
                        switch (numOfPlayers) {
                            case 2 -> closeUpgradeShop(dialogUpgradeFor2);
                            case 3 -> closeUpgradeShop(dialogUpgradeFor3);
                            case 4 -> closeUpgradeShop(dialogUpgradeFor4);
                            case 5 -> closeUpgradeShop(dialogUpgradeFor5);
                            case 6 -> closeUpgradeShop(dialogUpgradeFor6);
                        }
                        for(PlayerData p: clientHandler.getClientData().getPlayers()){
                            p.currentPlayerProperty().removeListener(currentPlayerListener2);

                        }
                        clientHandler.getClientData().getYourPlayerData().currentPlayerProperty().removeListener(currentPlayerListener);
                    }
                }
            });
        };

        clientHandler.getClientData().upgradeShopOpenProperty().addListener(upgradeShopOpenListener);
    }

    private void closeUpgradeShop(JFXDialog dialogUpgrade) {
        dialogUpgrade.close();
    }



    public List<Card> getAvailableUpgradeCards() {
        return availableUpgradeCards;
    }

    public void setAvailableUpgradeCards(List<Card> availableUpgradeCards) {
        this.availableUpgradeCards = availableUpgradeCards;
    }

    private List<Card> availableUpgradeCards;


    public ArrayList<PlayerData> getPlayerListInOrderForUpgradeShop() {
        return playerListInOrderForUpgradeShop;
    }

    public void setPlayerListInOrderForUpgradeShop(ArrayList<PlayerData> playerListInOrderForUpgradeShop) {
        this.playerListInOrderForUpgradeShop = playerListInOrderForUpgradeShop;
    }


    private ArrayList<PlayerData> playerListInOrderForUpgradeShop;

    private void addListenerForTimer() {
        timerIsRunningListener = (booleanProperty, oldValue, newValue) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (clientHandler.getClientData().isTimerIsRunning()) {
                        try {
                            timerStarts();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        timerStops();
                    }
                }
            });
        };

        clientHandler.getClientData().timerIsRunningProperty().addListener(timerIsRunningListener);
    }
    private void setDialogContainer() {
        dialogError.setDialogContainer(stack);
        dialogDamage.setDialogContainer(stack);
        dialogReboot.setDialogContainer(stack);
        dialogUpgradeFor2.setDialogContainer(stack);
        dialogUpgradeFor3.setDialogContainer(stack);
        dialogUpgradeFor4.setDialogContainer(stack);
        dialogUpgradeFor5.setDialogContainer(stack);
        dialogUpgradeFor6.setDialogContainer(stack);
        dialogUpgradeCardFor2.setDialogContainer(stackUpgradeCardFor2); //small dialog for the description of upgrade cards
        dialogUpgradeCardFor3.setDialogContainer(stackUpgradeCardFor3);
        dialogUpgradeCardFor4.setDialogContainer(stackUpgradeCardFor4);
        dialogUpgradeCardFor5.setDialogContainer(stackUpgradeCardFor5);
        dialogUpgradeCardFor6.setDialogContainer(stackUpgradeCardFor6);
    }

    private void addListenerForLastRebooted(){
        lastRebootedListener = (observableValue, oldValue , newValue) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    //System.out.println("handling LastRebooted "+ newValue.intValue());
                    if(clientHandler.getClientID() == newValue.intValue()) {
                        //System.out.println("opening dialog " + newValue.intValue());
                        clientHandler.getClientData().getYourPlayerData().setOrientation("top");
                        handleLastRebooted(newValue.intValue());
                    } else if(clientHandler.getClientData().getPlayer1Data().getClientID() == newValue.intValue()) {
                        clientHandler.getClientData().getPlayer1Data().setOrientation("top");
                    } else if(clientHandler.getClientData().getPlayer2Data().getClientID() == newValue.intValue()) {
                        clientHandler.getClientData().getPlayer2Data().setOrientation("top");
                    } else if(clientHandler.getClientData().getPlayer3Data().getClientID() == newValue.intValue()) {
                        clientHandler.getClientData().getPlayer3Data().setOrientation("top");
                    } else if(clientHandler.getClientData().getPlayer4Data().getClientID() == newValue.intValue()) {
                        clientHandler.getClientData().getPlayer4Data().setOrientation("top");
                    } else if(clientHandler.getClientData().getPlayer5Data().getClientID() == newValue.intValue()) {
                        clientHandler.getClientData().getPlayer5Data().setOrientation("top");
                    }
                }
                //TODO: send message to everyone in chat which clientID (newValue) is rebooted)
            });
        };

        clientHandler.getClientData().lastRebootedProperty().addListener(lastRebootedListener);
    }

    @FXML
    private JFXDialog dialogReboot;
    @FXML
    private Label rebootContent;
    @FXML
    private Label rebootHeader;
    @FXML
    private JFXButton chooseCloseBtnReboot;
    @FXML
    private ImageView robotImgReboot;
    @FXML
    private JFXButton bottomBtn;
    @FXML
    private JFXButton leftBtn;
    @FXML
    private JFXButton rightBtn;

    @FXML
    private JFXButton topBtn;

    private ArrayList<String> directionBtnClickedList = new ArrayList<String>();

    /**
     * handles when a player gets rebooted (a player can choose the direction of robot when rebooted
     * @param rebootedClientID
     * @author Jihyun
     */
    private void handleLastRebooted(int rebootedClientID) { //TODO: remove parameter later, if it's not needed
        chooseCloseBtnReboot.setText("choose");
        rebootHeader.setText("You are rebooted.");
        rebootContent.setText("Decide in which direction you want your robot to be facing.");
        dialogReboot.show();
        dialogReboot.setOverlayClose(false);

        //when direction button is clicked
        topBtn.setOnAction(ActionEvent-> {
            try {
                directionBtnClicked(topBtn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        bottomBtn.setOnAction(ActionEvent-> {
            try {
                directionBtnClicked(bottomBtn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        rightBtn.setOnAction(ActionEvent-> {
            try {
                directionBtnClicked(rightBtn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        leftBtn.setOnAction(ActionEvent-> {
            try {
                directionBtnClicked(leftBtn);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //when choose/close button is clicked
        chooseCloseBtnReboot.setOnAction(ActionEvent->
        {
            if(chooseCloseBtnReboot.getText().equals("close")) {
                directionBtnClickedList.clear();
                dialogReboot.close();
            }else {
                if(directionBtnClickedList.size() != 0) { //when a player has clicked at least one of the four direction buttons.
                    chooseCloseBtnReboot.setText("close");
                    String lastClickedDirection = directionBtnClickedList.get(directionBtnClickedList.size() - 1);
                    rebootContent.setText("You chose " + lastClickedDirection);
                    directionBtnClickedList.clear();
                    RebootDirection rebootDirection = new RebootDirection(lastClickedDirection);
                    try {
                        clientHandler.sendMessageSerialized(rebootDirection);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else { //when a player didn't click any direction button and click the choose button.
                    rebootContent.setText("Please choose a direction from (top, bottom, right, left) and click the button 'choose'. ");
                }
            }
        });
    }


    private void directionBtnClicked(JFXButton btn) throws IOException {
        rebootContent.setText("Decide in which direction you want your robot to be facing.");
        switch (clientHandler.getClientData().getYourPlayerData().getFigure()) {
            case 1 -> robotImgReboot.setImage(new Image(getClass().getClassLoader().getResource("images/robots/robothead1.png").openStream()));
            case 2 -> robotImgReboot.setImage(new Image(getClass().getClassLoader().getResource("images/robots/robothead2.png").openStream()));
            case 3 -> robotImgReboot.setImage(new Image(getClass().getClassLoader().getResource("images/robots/robothead3.png").openStream()));
            case 4 -> robotImgReboot.setImage(new Image(getClass().getClassLoader().getResource("images/robots/robothead4.png").openStream()));
            case 5 -> robotImgReboot.setImage(new Image(getClass().getClassLoader().getResource("images/robots/robothead5.png").openStream()));
            default -> robotImgReboot.setImage(new Image(getClass().getClassLoader().getResource("images/robots/robothead6.png").openStream()));
        }
        robotImgReboot.setFitHeight(90);
        robotImgReboot.setPreserveRatio(true);
        switch(btn.getText()){
            case "top":
                directionBtnClickedList.add(btn.getText());
                robotImgReboot.setRotate(0);
                break;
            case "bottom":
                directionBtnClickedList.add(btn.getText());
                robotImgReboot.setRotate(180);
                break;
            case "right":
                directionBtnClickedList.add(btn.getText());
                robotImgReboot.setRotate(90);
                break;
            case "left":
                directionBtnClickedList.add(btn.getText());
                robotImgReboot.setRotate(270);
                break;
            default:
                break;
        }
    }
    private void addPlayersListener(){
        playersListener = change -> {
            logger.fine("Change in players-List detected");
            while (change.next()){
                if (change.wasRemoved()){
                    // Get the player who left
                    ArrayList<Integer> removedPlayerIDList = new ArrayList<>();
                    for (PlayerData p: change.getRemoved()) {
                        removedPlayerIDList.add(p.getClientID());
                    }
                    handlePlayerRemoved(removedPlayerIDList);
                }
            }
        };

        clientHandler.getClientData().playersProperty().addListener(playersListener);
    }


    @FXML
    private JFXButton closeBtnDamage;
    @FXML
    private Label contentDamage;
    @FXML
    private Label popUpHeader;
    @FXML
    private JFXButton damageBtn1;

    @FXML
    private JFXButton damageBtn2;

    @FXML
    private JFXButton damageBtn3;
    @FXML
    private JFXDialog dialogDamage;

    private int damageCardCount =0;


    private void addListenerForDamageCards() {
        availableDamageCardsListener = (observableValue, oldValue , newValue) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    damageCardCount = clientHandler.getClientData().getDamageCardCount();
                    listOfDamageCardCount.add(clientHandler.getClientData().getDamageCardCount());
                    closeBtnDamage.setDisable(true);
                    listOfDamageCardsNewValue.add(newValue);
                    if(isDamageCardPopUpOpened.equals(false)) {
                        handleChooseDamageCards(damageCardCount,newValue);
                    }
                }
            });
        };

        clientHandler.getClientData().availableDamageCardsProperty().addListener(availableDamageCardsListener);
    }

    private ArrayList<String> chosenDamageCards = new ArrayList<String>();

    private int firstDamageCardCount = 0;
    private int secondDamageCardCount = 0;
    private int thirdDamageCardCount = 0;

    public Boolean getDamageCardPopUpOpened() {
        return isDamageCardPopUpOpened;
    }

    public void setDamageCardPopUpOpened(Boolean damageCardPopUpOpened) {
        isDamageCardPopUpOpened = damageCardPopUpOpened;
    }

    private Boolean isDamageCardPopUpOpened = false;
    private void handleChooseDamageCards(int numberOfDamageCardsToChoose, ArrayList<Card> newValue) {
        firstDamageCardCount = 0;
        secondDamageCardCount = 0;
        thirdDamageCardCount = 0;

        if(newValue.size() == 3) { //when there are trojan, worm, virus decks
            damageBtn1.setOpacity(1);
            damageBtn2.setOpacity(1);
            damageBtn3.setOpacity(1);
            damageBtn1.setDisable(false);
            damageBtn2.setDisable(false);
            damageBtn3.setDisable(false);

            damageBtn1.setText(newValue.get(0).getCardName());
            damageBtn2.setText(newValue.get(1).getCardName());
            damageBtn3.setText(newValue.get(2).getCardName());

            popUpHeader.setText("Choose "+ numberOfDamageCardsToChoose + " damage cards from: \n [ " + newValue.get(0).getCardName() + ", " + newValue.get(1).getCardName() + ", " + newValue.get(2).getCardName() + "]" );
            contentDamage.setText(newValue.get(0).getCardName() + ": " + firstDamageCardCount + "  " + newValue.get(1).getCardName() + ": " + secondDamageCardCount + "  "+ newValue.get(2).getCardName() + ": " + thirdDamageCardCount);
            dialogDamage.show();
            setDamageCardPopUpOpened(true);
            dialogDamage.setOverlayClose(false);
            closeBtnDamage.setOnAction(ActionEvent-> {
                dialogDamage.close();
                setDamageCardPopUpOpened(false);
                listOfDamageCardsNewValue.remove(0);
                listOfDamageCardCount.remove(0);
                if(listOfDamageCardCount.size() != 0 || listOfDamageCardsNewValue.size() != 0) {
                    handleChooseDamageCards(listOfDamageCardCount.get(0), listOfDamageCardsNewValue.get(0));
                }
            });

            damageBtn1.setOnAction(event -> {
                try {
                    damageCardBtnClicked(newValue, damageBtn1, numberOfDamageCardsToChoose);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            damageBtn2.setOnAction(event -> {
                try {
                    damageCardBtnClicked(newValue, damageBtn2, numberOfDamageCardsToChoose);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            damageBtn3.setOnAction(event -> {
                try {
                    damageCardBtnClicked(newValue, damageBtn3, numberOfDamageCardsToChoose);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }else if (newValue.size() == 2) { //when there are only two of the three card decks(trojan, worm, virus)
            damageBtn1.setOpacity(100);
            damageBtn2.setOpacity(100);
            damageBtn3.setOpacity(0);
            damageBtn3.setDisable(true);
            damageBtn1.setDisable(false);
            damageBtn2.setDisable(false);

            damageBtn1.setText(newValue.get(0).getCardName());
            damageBtn2.setText(newValue.get(1).getCardName());

            popUpHeader.setText("Choose "+ numberOfDamageCardsToChoose + " damage cards from: \n [ " + newValue.get(0).getCardName() + ", " + newValue.get(1).getCardName() + "]" );
            contentDamage.setText(newValue.get(0).getCardName() + ": " + firstDamageCardCount + "  " + newValue.get(1).getCardName() + ": " + secondDamageCardCount);
            dialogDamage.show();
            setDamageCardPopUpOpened(true);
            dialogDamage.setOverlayClose(false);
            closeBtnDamage.setOnAction(ActionEvent-> {
                dialogDamage.close();
                setDamageCardPopUpOpened(false);
                listOfDamageCardsNewValue.remove(0);
                listOfDamageCardCount.remove(0);
                if(listOfDamageCardCount.size() != 0 || listOfDamageCardsNewValue.size() != 0) {
                    handleChooseDamageCards(listOfDamageCardCount.get(0), listOfDamageCardsNewValue.get(0));
                }
            });

            damageBtn1.setOnAction(event -> {
                try {
                    damageCardBtnClicked(newValue, damageBtn1, numberOfDamageCardsToChoose);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            damageBtn3.setOnAction(event -> {
                try {
                    damageCardBtnClicked(newValue, damageBtn3, numberOfDamageCardsToChoose);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }else if (newValue.size() == 1) { // when there is only one of the three card decks(trojan, worm, virus)
            setButtonsOpacityZero();
            setButtonsDisable();
            popUpHeader.setText("Only " + newValue.get(0).getCardName() + " left");
            contentDamage.setText("You get " + numberOfDamageCardsToChoose + " " + newValue.get(0).getCardName() + " damage cards");
            dialogDamage.show();
            setDamageCardPopUpOpened(true);
            dialogDamage.setOverlayClose(false);
            closeBtnDamage.setOnAction(ActionEvent-> {
                dialogDamage.close();
                setDamageCardPopUpOpened(false);
                listOfDamageCardsNewValue.remove(0);
                listOfDamageCardCount.remove(0);
                if(listOfDamageCardCount.size() != 0 || listOfDamageCardsNewValue.size() != 0) {
                    handleChooseDamageCards(listOfDamageCardCount.get(0), listOfDamageCardsNewValue.get(0));
                }
            });

        }else { // when there is no more damage card decks
            setButtonsOpacityZero();
            setButtonsDisable();
            popUpHeader.setText("No more damage cards");
            contentDamage.setText("There is no more damage cards to choose. ");
            dialogDamage.show();
            setDamageCardPopUpOpened(true);
            dialogDamage.setOverlayClose(false);
            closeBtnDamage.setOnAction(ActionEvent-> {
                dialogDamage.close();
                setDamageCardPopUpOpened(false);
                listOfDamageCardsNewValue.remove(0);
                listOfDamageCardCount.remove(0);
                if(listOfDamageCardCount.size() != 0 || listOfDamageCardsNewValue.size() != 0) {
                    handleChooseDamageCards(listOfDamageCardCount.get(0), listOfDamageCardsNewValue.get(0));
                }
            });
        }

    }

    /**
     * draws damage cards that player has chosen from the popup when there is no more spam card to be drawn.
     * @param newValue
     * @param btn
     * @param numOfDamageCardToChoose
     * @throws IOException
     * @author Jihyun
     */
    private void damageCardBtnClicked(ArrayList<Card> newValue, Button btn, int numOfDamageCardToChoose) throws IOException {
        if (chosenDamageCards.size() < numOfDamageCardToChoose) {
            chosenDamageCards.add(btn.getText());
            if (btn.getId().equals("damageBtn1")) {
                firstDamageCardCount++;
                contentDamage.setText(newValue.get(0).getCardName() + ": " + firstDamageCardCount + "  " + newValue.get(1).getCardName() + ": " + secondDamageCardCount + "  " + newValue.get(2).getCardName() + ": " + thirdDamageCardCount);
            } else if (btn.getId().equals("damageBtn2")) {
                secondDamageCardCount++;
                contentDamage.setText(newValue.get(0).getCardName() + ": " + firstDamageCardCount + "  " + newValue.get(1).getCardName() + ": " + secondDamageCardCount + "  " + newValue.get(2).getCardName() + ": " + thirdDamageCardCount);

            } else {
                thirdDamageCardCount++;
                contentDamage.setText(newValue.get(0).getCardName() + ": " + firstDamageCardCount + "  " + newValue.get(1).getCardName() + ": " + secondDamageCardCount + "  " + newValue.get(2).getCardName() + ": " + thirdDamageCardCount);
            }
            if (chosenDamageCards.size() == numOfDamageCardToChoose) { //when the last damage card has been chosen, the selectedDamage msg will be sent to server.
                closeBtnDamage.setDisable(false);
                SelectedDamage selectedDamage = new SelectedDamage(chosenDamageCards);
                clientHandler.sendMessageSerialized(selectedDamage);
                contentDamage.setText("Your chosen damage cards: " + chosenDamageCards + "\n Please click 'close' button.");
                chosenDamageCards.clear();
            }
    }

    }

    private void setButtonsDisable() {
        damageBtn1.setDisable(true);
        damageBtn2.setDisable(true);
        damageBtn3.setDisable(true);

    }
    private void setButtonsOpacityZero() {
        damageBtn1.setOpacity(0);
        damageBtn2.setOpacity(0);
        damageBtn3.setOpacity(0);
    }


    @FXML
    private JFXDialog dialogError;
    @FXML
    private JFXButton closeBtnError;

    @FXML
    private Label contentError;

    private void addErrorMessageListener(){
        errorChangeListener = (observableValue, oldValue, newValue) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    contentError.setText(newValue.getError());
                    dialogError.show();
                    dialogError.setTransitionType(JFXDialog.DialogTransition.TOP);
                    dialogError.setOverlayClose(false);
                    closeBtnError.setOnAction(ActionEvent-> dialogError.close());
                    if (newValue.getError().equals("You are currently the only player, the game cannot be continued.")){
                        bottomHBox.setDisable(true);
                        centerVBox.setDisable(true);
                        leftVBox.setDisable(true);
                        rightVBox.setDisable(true);
                    }
//                    else if(newValue.getError().equals("You already own a Admin Privilege Card!")){
//
//                    }

                    logger.info("The following error was reported to the user: " + newValue.getError());
                }
            });
        };
        clientHandler.getClientData().errorObjectProperty().addListener(errorChangeListener);
    }

    /**
     * @author Stephan
     * opening the winningWindow depending on the Client
     */
    private void addGameFinishedListener() {
        winningDialog.setDialogContainer(centerStack);

        winningDialog.setOnDialogOpened(new EventHandler<JFXDialogEvent>() {
            @Override
            public void handle(JFXDialogEvent jfxDialogEvent) {
            }
        });



        gameFinishedListener = ((observableValue, oldValue, newValue) -> {
            try {
                GameFinished message = (GameFinished) clientHandler.getCopyOfNewestMessage();
                openWinningWindow(message.getClientID(), clientHandler.getClientID() == message.getClientID());
            } catch (Exception ignored) {
                try {
                    CheckPointReached message = (CheckPointReached) clientHandler.getCopyOfNewestMessage();
                    openWinningWindow(message.getClientID(), clientHandler.getClientID() == message.getClientID());
                } catch (Exception ignored1) {
                }
            }

        });
        clientHandler.getClientData().gameFinished().addListener(gameFinishedListener);
    }

    /**
     * @author Stephan
     * @param clientID
     * @param isWinner
     */
    private void openWinningWindow(int clientID, boolean isWinner) {
        if(Screen.getPrimary().getBounds().getMaxY() < 900) {
            double x = Screen.getPrimary().getVisualBounds().getMaxY() /1.5;
            winningLabelBox.setMinWidth(x);
            winningStatsBox.setMinWidth(x);
            winningButtonHBox.setMinWidth(x);
            winningDialog.setMaxWidth(x);
            winningDialog.setMaxHeight(Screen.getPrimary().getVisualBounds().getMaxY() / 2);
        } else {
            double x = Screen.getPrimary().getVisualBounds().getMaxY() / 2;
            winningLabelBox.setMinWidth(x);
            winningStatsBox.setMinWidth(x);
            winningButtonHBox.setMinWidth(x);
            winningDialog.setMaxWidth(x);
            winningDialog.setMaxHeight(Screen.getPrimary().getVisualBounds().getMaxY() / 2.5);
        }

        String name = "";
        for(PlayerData playerData: clientHandler.getClientData().getPlayers()) {
            if (playerData.getClientID() == clientID) {
                name = playerData.getName();
            }
        }

        String finalName = name;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                congratsLabel.setText(finalName + "(ID: " + clientID + ") won the Game!");
                if(isWinner) {
                    congratsLabel.setText("Congratulations for Winning!");
                }
                winningStats.setText("Game Stats");
                winningRound.setText("Rounds: " + clientHandler.getClientData().getRound());
                winningReboot.setText("Amount of Reboots: " + clientHandler.getClientData().getYourPlayerData().getRebootCount());
                winningDamage.setText("Damage taken: ");
                winningUpgrade.setText("Amount of Upgrades: ");
                winningDialog.show();
            }
        });

    }



    /**
     * adds the active phase listener
     * @author Tingyue
     */
    private void addListenerForPhase(){
        currentPhaseListener =  (booleanProperty, oldValue, newValue) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    //update round
                    if (newValue.intValue() == 2){
                        round.setText("Round: " + clientHandler.getClientData().getRound());
                    }
                    //update phase
                    String phase;
                    switch(clientHandler.getClientData().getCurrentPhase()) {
                        case 1 -> {
                            phase = "Current Phase: Upgrade Phase";
                            if(!dialogReboot.isOverlayClose()) {
                                dialogReboot.close();
                            }
                            //setPlayerListInOrderForUpgradeShop(clientHandler.getClientData().getPlayersInAntennaOrder());
                            ObservableList<Card> observableArrayListForAvailableUpgrade = FXCollections.observableArrayList(clientHandler.getClientData().getUpgradeShopContent());
                            setAvailableUpgradeCards(observableArrayListForAvailableUpgrade.stream().collect(Collectors.toList()));


                            //TODO: open the popupwindow for the upgrade shop for players to buy upgrade card
                        }
                        case 2 -> {
                            phase = "Current Phase: Programming Phase";
                            if(!dialogReboot.isOverlayClose()) {
                                dialogReboot.close();
                            }
                            //try {
                            //    openUpgradeShop(); //TODO: löschen wenn logic fertig
                            //} catch (IOException e) {
                            //    throw new RuntimeException(e);
                            //}
                        }
                        case 3 -> {
                            phase = "Current Phase: Activation Phase";
                            timerLabel.setOpacity(0);
                            timer.setOpacity(0);
                            timername.setOpacity(0);
                        }

                        default -> phase = "";
                    }
                    phaseLabel.setText(phase);
                }
            });

        };
        clientHandler.getClientData().currentPhaseProperty().addListener(currentPhaseListener);
    }


    private void initPlayerBoxInUpgradeShop(int numPlayers){
        playerWithNameLabel = new HashMap<>();
        playerWithCardLabel = new HashMap<>();
        GridPane currentGridPane = getGridPaneInUpgradeShop(numPlayers);

        for (int i = 0; i < numPlayers; i++){
            int paneIndex = 2*i;
            Label playerName = (Label) currentGridPane.getChildren().get(paneIndex);
            playerName.setText("(ID:" + playerListInOrderForUpgradeShop.get(i).getClientID()+") "+playerListInOrderForUpgradeShop.get(i).getName());
            playerWithNameLabel.put(playerListInOrderForUpgradeShop.get(i).getClientID(), playerName);

            Label boughtUpgradeCard = (Label) currentGridPane.getChildren().get(paneIndex + 1);
            boughtUpgradeCard.setText("null");
            playerWithCardLabel.put(playerListInOrderForUpgradeShop.get(i).getClientID(),boughtUpgradeCard);

        }

        currPlayerID = clientHandler.getClientData().getPlayersInAntennaOrder().get(0).getClientID();
    }

    private GridPane getGridPaneInUpgradeShop(int numPlayers){
        return switch (numPlayers){
            case 2 -> gridpanePlayerListUpgradeFor2;
            case 3 -> gridpanePlayerListUpgradeFor3;
            case 4 -> gridpanePlayerListUpgradeFor4;
            case 5 -> gridpanePlayerListUpgradeFor5;
            case 6 -> gridpanePlayerListUpgradeFor6;
            default -> null;
        };
    }

    @FXML
    private Label currentPlayerFor2;
    @FXML
    private JFXDialog dialogUpgradeFor2; //dialog for 2 players for the whole upgrade shop
    @FXML
    private ImageView upgradeShopImgFor2; //an imageview where energy shop image will be show next to the title of the upgradeshop
    @FXML
    private JFXDialog dialogUpgradeCardFor2; //dialog for description of each upgrade card
    @FXML
    private StackPane stackUpgradeCardFor2; //stackpane for a dialog where description of each upgrade card will be shown.
    @FXML
    private GridPane gridpanePlayerListUpgradeFor2;
    @FXML
    private JFXButton btnUpgradeBuyFor2;
    @FXML
    private JFXButton btnUpgradeNotBuyFor2;
    @FXML
    private JFXButton upgrade1For2;
    @FXML
    private ImageView upgrade1ImgFor2;
    @FXML
    private JFXButton upgrade2For2;
    @FXML
    private ImageView upgrade2ImgFor2;

    @FXML
    private Label currentPlayerFor3;
    @FXML
    private JFXDialog dialogUpgradeFor3;
    @FXML
    private ImageView upgradeShopImgFor3;
    @FXML
    private JFXDialog dialogUpgradeCardFor3;
    @FXML
    private StackPane stackUpgradeCardFor3;
    @FXML
    private GridPane gridpanePlayerListUpgradeFor3;
    @FXML
    private JFXButton btnUpgradeBuyFor3;
    @FXML
    private JFXButton btnUpgradeNotBuyFor3;
    @FXML
    private JFXButton upgrade1For3;
    @FXML
    private ImageView upgrade1ImgFor3;
    @FXML
    private JFXButton upgrade2For3;
    @FXML
    private ImageView upgrade2ImgFor3;
    @FXML
    private JFXButton upgrade3For3;
    @FXML
    private ImageView upgrade3ImgFor3;

    @FXML
    private Label currentPlayerFor4;
    @FXML
    private JFXDialog dialogUpgradeFor4;
    @FXML
    private ImageView upgradeShopImgFor4;
    @FXML
    private JFXDialog dialogUpgradeCardFor4;
    @FXML
    private StackPane stackUpgradeCardFor4;
    @FXML
    private GridPane gridpanePlayerListUpgradeFor4;
    @FXML
    private JFXButton btnUpgradeBuyFor4;
    @FXML
    private JFXButton btnUpgradeNotBuyFor4;

    @FXML
    private JFXButton upgrade1For4;
    @FXML
    private ImageView upgrade1ImgFor4;
    @FXML
    private JFXButton upgrade2For4;
    @FXML
    private ImageView upgrade2ImgFor4;
    @FXML
    private JFXButton upgrade3For4;
    @FXML
    private ImageView upgrade3ImgFor4;
    @FXML
    private JFXButton upgrade4For4;
    @FXML
    private ImageView upgrade4ImgFor4;

    @FXML
    private Label currentPlayerFor5;
    @FXML
    private JFXDialog dialogUpgradeFor5;
    @FXML
    private ImageView upgradeShopImgFor5;
    @FXML
    private JFXDialog dialogUpgradeCardFor5;
    @FXML
    private StackPane stackUpgradeCardFor5;
    @FXML
    private GridPane gridpanePlayerListUpgradeFor5;
    @FXML
    private JFXButton btnUpgradeBuyFor5;
    @FXML
    private JFXButton btnUpgradeNotBuyFor5;
    @FXML
    private JFXButton upgrade1For5;
    @FXML
    private ImageView upgrade1ImgFor5;
    @FXML
    private JFXButton upgrade2For5;
    @FXML
    private ImageView upgrade2ImgFor5;
    @FXML
    private JFXButton upgrade3For5;
    @FXML
    private ImageView upgrade3ImgFor5;
    @FXML
    private JFXButton upgrade4For5;
    @FXML
    private ImageView upgrade4ImgFor5;
    @FXML
    private JFXButton upgrade5For5;
    @FXML
    private ImageView upgrade5ImgFor5;

    @FXML
    private Label currentPlayerFor6;
    @FXML
    private JFXDialog dialogUpgradeFor6;
    @FXML
    private ImageView upgradeShopImgFor6;
    @FXML
    private JFXDialog dialogUpgradeCardFor6;
    @FXML
    private StackPane stackUpgradeCardFor6;
    @FXML
    private GridPane gridpanePlayerListUpgradeFor6;
    @FXML
    private JFXButton btnUpgradeBuyFor6;
    @FXML
    private JFXButton btnUpgradeNotBuyFor6;
    @FXML
    private JFXButton upgrade1For6;
    @FXML
    private ImageView upgrade1ImgFor6;
    @FXML
    private JFXButton upgrade2For6;
    @FXML
    private ImageView upgrade2ImgFor6;
    @FXML
    private JFXButton upgrade3For6;
    @FXML
    private ImageView upgrade3ImgFor6;
    @FXML
    private JFXButton upgrade4For6;
    @FXML
    private ImageView upgrade4ImgFor6;
    @FXML
    private JFXButton upgrade5For6;
    @FXML
    private ImageView upgrade5ImgFor6;
    @FXML
    private JFXButton upgrade6For6;
    @FXML
    private ImageView upgrade6ImgFor6;
    @FXML
    private ImageView registerBorder1 = new ImageView();
    @FXML
    private ImageView registerBorder2 = new ImageView();
    @FXML
    private ImageView registerBorder3 = new ImageView();
    @FXML
    private ImageView registerBorder4 = new ImageView();
    @FXML
    private ImageView registerBorder5 = new ImageView();

    private void skipAI(PlayerData playerData) throws IOException {
        if (currPlayerID == playerData.getClientID() && playerData.isAI()){
            BuyUpgrade NotBuyUpgrade = new BuyUpgrade(false, "null");
            clientHandler.sendMessageSerialized(NotBuyUpgrade);
        }
    }

    private void initButtonInUpgradeShop(int numOfPlayers){
        //init buy and not buy button
        getBuyButton(numOfPlayers).setDisable(true);

        if (currPlayerID == clientHandler.getClientID()){
            getNotBuyButton(numOfPlayers).setDisable(false);
        }else {
            getNotBuyButton(numOfPlayers).setDisable(true);
        }
    }

    /**
     * opens upgradeshop when there are two players
     * @throws IOException
     * @author Jihyun
     */
    private void openUpgradeShopFor2() throws IOException {
        initPlayerBoxInUpgradeShop(2);
        labelChosenCardFor2.setText(" ");
        PlayerData firstPlayer = clientHandler.getClientData().getPlayersInAntennaOrder().get(0);
        // TODO: will be updated whenever boughtupgradecard with the player's id is listened. (add to 3,4,5,6)
        currentPlayerFor2.setText("Who's turn:  " + firstPlayer.getName() + "(ID: " + firstPlayer.getClientID() +")");

        showAvailableUpgrade(2, availableUpgradeCards);

        dialogUpgradeFor2.show();
        dialogUpgradeFor2.setOverlayClose(false);


        showUpgradeCardDescriptions(upgrade1For2, dialogUpgradeCardFor2);
        showUpgradeCardDescriptions(upgrade2For2, dialogUpgradeCardFor2);


        upgrade1For2.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade1For2.getText(),dialogUpgradeFor2, upgrade1For2));
        upgrade2For2.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade2For2.getText(),dialogUpgradeFor2, upgrade2For2));

        btnUpgradeBuyFor2.setOnAction(ActionEvent-> {
            try {
                handleBuyBtnClicked(dialogUpgradeFor2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        btnUpgradeNotBuyFor2.setOnAction(ActionEvent-> {
            try {
                handleNotBuyBtnClicked(dialogUpgradeFor2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void openUpgradeShopFor3() throws IOException {
        initPlayerBoxInUpgradeShop(3);

        PlayerData firstPlayer = clientHandler.getClientData().getPlayersInAntennaOrder().get(0);
        currentPlayerFor3.setText("Who's turn:  " + firstPlayer.getName() + "(ID: " + firstPlayer.getClientID() +")");

        initButtonInUpgradeShop(3);

        showAvailableUpgrade(3, availableUpgradeCards);
        dialogUpgradeFor3.show();
        dialogUpgradeFor3.setOverlayClose(false);

        showUpgradeCardDescriptions(upgrade1For3, dialogUpgradeCardFor3);
        showUpgradeCardDescriptions(upgrade2For3, dialogUpgradeCardFor3);
        showUpgradeCardDescriptions(upgrade3For3, dialogUpgradeCardFor3);


        upgrade1For3.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade1For3.getText(),dialogUpgradeFor3, upgrade1For3));
        upgrade2For3.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade2For3.getText(),dialogUpgradeFor3, upgrade2For3));
        upgrade3For3.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade3For3.getText(),dialogUpgradeFor3, upgrade3For3));

        btnUpgradeBuyFor3.setOnAction(ActionEvent-> {
            try {
                handleBuyBtnClicked(dialogUpgradeFor3);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        btnUpgradeNotBuyFor3.setOnAction(ActionEvent-> {
            try {
                handleNotBuyBtnClicked(dialogUpgradeFor3);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void openUpgradeShopFor4() throws IOException {
        initPlayerBoxInUpgradeShop(4);

        PlayerData firstPlayer = clientHandler.getClientData().getPlayersInAntennaOrder().get(0);
        currentPlayerFor4.setText("Who's turn:  " + firstPlayer.getName() + "(ID: " + firstPlayer.getClientID() +")");

        initButtonInUpgradeShop(4);

        showAvailableUpgrade(4, availableUpgradeCards);
        btnUpgradeBuyFor4.setDisable(true);
        dialogUpgradeFor4.show();
        dialogUpgradeFor4.setOverlayClose(false);

        showUpgradeCardDescriptions(upgrade1For4, dialogUpgradeCardFor4);
        showUpgradeCardDescriptions(upgrade2For4, dialogUpgradeCardFor4);
        showUpgradeCardDescriptions(upgrade3For4, dialogUpgradeCardFor4);
        showUpgradeCardDescriptions(upgrade4For4, dialogUpgradeCardFor4);

        upgrade1For4.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade1For4.getText(), dialogUpgradeFor4,upgrade1For4));
        upgrade2For4.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade2For4.getText(), dialogUpgradeFor4, upgrade2For4));
        upgrade3For4.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade3For4.getText(), dialogUpgradeFor4, upgrade3For4));
        upgrade4For4.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade3For4.getText(), dialogUpgradeFor4, upgrade4For4));

        btnUpgradeBuyFor4.setOnAction(ActionEvent-> {
            try {
                handleBuyBtnClicked(dialogUpgradeFor4);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        btnUpgradeNotBuyFor4.setOnAction(ActionEvent-> {
            try {
                handleNotBuyBtnClicked(dialogUpgradeFor4);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void openUpgradeShopFor5() throws IOException {
        initPlayerBoxInUpgradeShop(5);

        PlayerData firstPlayer = clientHandler.getClientData().getPlayersInAntennaOrder().get(0);
        currentPlayerFor5.setText("Who's turn:  " + firstPlayer.getName() + "(ID: " + firstPlayer.getClientID() +")");

        initButtonInUpgradeShop(5);

        showAvailableUpgrade(5, availableUpgradeCards);
        btnUpgradeBuyFor5.setDisable(true);
        dialogUpgradeFor5.show();
        dialogUpgradeFor5.setOverlayClose(false);

        showUpgradeCardDescriptions(upgrade1For5, dialogUpgradeCardFor5);
        showUpgradeCardDescriptions(upgrade2For5, dialogUpgradeCardFor5);
        showUpgradeCardDescriptions(upgrade3For5, dialogUpgradeCardFor5);
        showUpgradeCardDescriptions(upgrade4For5, dialogUpgradeCardFor5);
        showUpgradeCardDescriptions(upgrade5For5, dialogUpgradeCardFor5);

        upgrade1For5.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade1For5.getText(), dialogUpgradeFor5, upgrade1For5));
        upgrade2For5.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade2For5.getText(), dialogUpgradeFor5, upgrade2For5));
        upgrade3For5.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade3For5.getText(), dialogUpgradeFor5, upgrade3For5));
        upgrade4For5.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade4For5.getText(), dialogUpgradeFor5, upgrade4For5));
        upgrade5For5.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade5For5.getText(), dialogUpgradeFor5, upgrade5For5));

        btnUpgradeBuyFor5.setOnAction(ActionEvent-> {
            try {
                handleBuyBtnClicked(dialogUpgradeFor5);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        btnUpgradeNotBuyFor5.setOnAction(ActionEvent-> {
            try {
                handleNotBuyBtnClicked(dialogUpgradeFor5);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void openUpgradeShopFor6() throws IOException {
        initPlayerBoxInUpgradeShop(6);

        PlayerData firstPlayer = clientHandler.getClientData().getPlayersInAntennaOrder().get(0);
        currentPlayerFor6.setText("Who's turn:  " + firstPlayer.getName() + "(ID: " + firstPlayer.getClientID() +")");

        initButtonInUpgradeShop(6);

        showAvailableUpgrade(6, availableUpgradeCards);
        btnUpgradeBuyFor6.setDisable(true);
        dialogUpgradeFor6.show();
        dialogUpgradeFor6.setOverlayClose(false);

        showUpgradeCardDescriptions(upgrade1For6, dialogUpgradeCardFor6);
        showUpgradeCardDescriptions(upgrade2For6, dialogUpgradeCardFor6);
        showUpgradeCardDescriptions(upgrade3For6, dialogUpgradeCardFor6);
        showUpgradeCardDescriptions(upgrade4For6, dialogUpgradeCardFor6);
        showUpgradeCardDescriptions(upgrade5For6, dialogUpgradeCardFor6);
        showUpgradeCardDescriptions(upgrade6For6, dialogUpgradeCardFor6);

        upgrade1For6.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade1For6.getText(),dialogUpgradeFor6, upgrade1For6));
        upgrade2For6.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade2For6.getText(), dialogUpgradeFor6, upgrade2For6));
        upgrade3For6.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade3For6.getText(),  dialogUpgradeFor6, upgrade3For6));
        upgrade4For6.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade4For6.getText(), dialogUpgradeFor6, upgrade4For6));
        upgrade5For6.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade5For6.getText(), dialogUpgradeFor6, upgrade5For6));
        upgrade6For6.setOnMouseClicked(mouseEvent -> handleChooseUpgradeCard(upgrade6For6.getText(), dialogUpgradeFor6, upgrade6For6));

        btnUpgradeBuyFor6.setOnAction(ActionEvent-> {
            try {
                handleBuyBtnClicked(dialogUpgradeFor6);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        btnUpgradeNotBuyFor6.setOnAction(ActionEvent-> {
            try {
                handleNotBuyBtnClicked(dialogUpgradeFor6);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void showUpgradeCardDescriptions(JFXButton upgradeBtn, JFXDialog dialog) {
        upgradeBtn.setOnMouseEntered(mouseEvent -> handleUpgradeCardDialog(upgradeBtn.getText(), dialog));
        upgradeBtn.setOnMouseExited(mouseEvent -> dialog.close());
    }

    private LinkedHashMap<String, JFXButton> mapCardWithButton = new LinkedHashMap<>();
    private void showAvailableUpgrade(int numOfPlayers, List<Card> availableUpgradeCards) throws IOException {
        switch(numOfPlayers) {
            case 2:
                upgrade1ImgFor2.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(0).getCardName())+ ".png").openStream()));
                upgrade1For2.setGraphic(upgrade1ImgFor2);
                upgrade1For2.setText(availableUpgradeCards.get(0).getCardName());
                mapCardWithButton.put(upgrade1For2.getText(), upgrade1For2);
                upgrade2ImgFor2.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(1).getCardName())+ ".png").openStream()));
                upgrade2For2.setGraphic(upgrade2ImgFor2);
                upgrade2For2.setText(availableUpgradeCards.get(1).getCardName());
                mapCardWithButton.put(upgrade2For2.getText(), upgrade2For2);
                break;
            case 3:
                upgrade1ImgFor3.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(0).getCardName())+ ".png").openStream()));
                upgrade1For3.setGraphic(upgrade1ImgFor3);
                upgrade1For3.setText(availableUpgradeCards.get(0).getCardName());
                mapCardWithButton.put(upgrade1For3.getText(), upgrade1For3);
                upgrade2ImgFor3.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(1).getCardName())+ ".png").openStream()));
                upgrade2For3.setGraphic(upgrade2ImgFor3);
                upgrade2For3.setText(availableUpgradeCards.get(1).getCardName());
                mapCardWithButton.put(upgrade2For3.getText(), upgrade2For3);
                upgrade3ImgFor3.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(2).getCardName())+ ".png").openStream()));
                upgrade3For3.setGraphic(upgrade3ImgFor3);
                upgrade3For3.setText(availableUpgradeCards.get(2).getCardName());
                mapCardWithButton.put(upgrade3For3.getText(), upgrade3For3);
                break;
            case 4:
                upgrade1ImgFor4.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(0).getCardName())+ ".png").openStream()));
                upgrade1For4.setGraphic(upgrade1ImgFor4);
                upgrade1For4.setText(availableUpgradeCards.get(0).getCardName());
                mapCardWithButton.put(upgrade1For4.getText(), upgrade1For4);
                upgrade2ImgFor4.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(1).getCardName())+ ".png").openStream()));
                upgrade2For4.setGraphic(upgrade2ImgFor4);
                upgrade2For4.setText(availableUpgradeCards.get(1).getCardName());
                mapCardWithButton.put(upgrade2For4.getText(), upgrade2For4);
                upgrade3ImgFor4.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(2).getCardName())+ ".png").openStream()));
                upgrade3For4.setGraphic(upgrade3ImgFor4);
                upgrade3For4.setText(availableUpgradeCards.get(2).getCardName());
                mapCardWithButton.put(upgrade3For4.getText(), upgrade3For4);
                upgrade4ImgFor4.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(3).getCardName())+ ".png").openStream()));
                upgrade4For4.setGraphic(upgrade4ImgFor4);
                upgrade4For4.setText(availableUpgradeCards.get(3).getCardName());
                mapCardWithButton.put(upgrade4For4.getText(), upgrade4For4);
                break;
            case 5:
                upgrade1ImgFor5.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(0).getCardName())+ ".png").openStream()));
                upgrade1For5.setGraphic(upgrade1ImgFor5);
                upgrade1For5.setText(availableUpgradeCards.get(0).getCardName());
                mapCardWithButton.put(upgrade1For5.getText(), upgrade1For5);
                upgrade2ImgFor5.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(1).getCardName())+ ".png").openStream()));
                upgrade2For5.setGraphic(upgrade2ImgFor5);
                upgrade2For5.setText(availableUpgradeCards.get(1).getCardName());
                mapCardWithButton.put(upgrade2For5.getText(), upgrade2For5);
                upgrade3ImgFor5.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(2).getCardName())+ ".png").openStream()));
                upgrade3For5.setGraphic(upgrade3ImgFor5);
                upgrade3For5.setText(availableUpgradeCards.get(2).getCardName());
                mapCardWithButton.put(upgrade3For5.getText(), upgrade3For5);
                upgrade4ImgFor5.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(3).getCardName())+ ".png").openStream()));
                upgrade4For5.setGraphic(upgrade4ImgFor5);
                upgrade4For5.setText(availableUpgradeCards.get(3).getCardName());
                mapCardWithButton.put(upgrade4For5.getText(), upgrade4For5);
                upgrade5ImgFor5.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(4).getCardName())+ ".png").openStream()));
                upgrade5For5.setGraphic(upgrade5ImgFor5);
                upgrade5For5.setText(availableUpgradeCards.get(4).getCardName());
                mapCardWithButton.put(upgrade5For5.getText(), upgrade5For5);
                break;
            case 6:
                upgrade1ImgFor6.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(0).getCardName())+ ".png").openStream()));
                upgrade1For6.setGraphic(upgrade1ImgFor6);
                upgrade1For6.setText(availableUpgradeCards.get(0).getCardName());
                mapCardWithButton.put(upgrade1For6.getText(), upgrade1For6);
                upgrade2ImgFor6.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(1).getCardName())+ ".png").openStream()));
                upgrade2For6.setGraphic(upgrade2ImgFor6);
                upgrade2For6.setText(availableUpgradeCards.get(1).getCardName());
                mapCardWithButton.put(upgrade2For6.getText(), upgrade2For6);
                upgrade3ImgFor6.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(2).getCardName())+ ".png").openStream()));
                upgrade3For6.setGraphic(upgrade3ImgFor6);
                upgrade3For6.setText(availableUpgradeCards.get(2).getCardName());
                mapCardWithButton.put(upgrade3For6.getText(), upgrade3For6);
                upgrade4ImgFor6.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(3).getCardName())+ ".png").openStream()));
                upgrade4For6.setGraphic(upgrade4ImgFor6);
                upgrade4For6.setText(availableUpgradeCards.get(3).getCardName());
                mapCardWithButton.put(upgrade4For6.getText(), upgrade4For6);
                upgrade5ImgFor6.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(4).getCardName())+ ".png").openStream()));
                upgrade5For6.setGraphic(upgrade5ImgFor6);
                upgrade5For6.setText(availableUpgradeCards.get(4).getCardName());
                mapCardWithButton.put(upgrade5For6.getText(), upgrade5For6);
                upgrade6ImgFor6.setImage(new Image(getClass().getClassLoader().getResource("images/cards/upgradeCards/" + getImgNameFromCardName(availableUpgradeCards.get(5).getCardName())+ ".png").openStream()));
                upgrade6For6.setGraphic(upgrade6ImgFor6);
                upgrade6For6.setText(availableUpgradeCards.get(5).getCardName());
                mapCardWithButton.put(upgrade6For6.getText(), upgrade6For6);
                break;
            default:

        }
    }

    private String getImgNameFromCardName(String upgradeCardName) {
        switch (upgradeCardName) {
            case "AdminPrivilege":
                return "adminPrivilege";
            case "MemorySwap":
                return "memorySwap";
            case "RearLaser":
                return "realLaser";
            case "SpamBlocker":
                return "spamBlocker";
            default:
                return null;
        }
    }

    @FXML
    private Label labelUpgradeCardNameFor2;
    @FXML
    private Label labelUpgradeCardDescriptionFor2;
    @FXML
    private Label labelUpgradeCardNameFor3;
    @FXML
    private Label labelUpgradeCardDescriptionFor3;
    @FXML
    private Label labelUpgradeCardNameFor4;
    @FXML
    private Label labelUpgradeCardDescriptionFor4;
    @FXML
    private Label labelUpgradeCardNameFor5;
    @FXML
    private Label labelUpgradeCardDescriptionFor5;
    @FXML
    private Label labelUpgradeCardNameFor6;
    @FXML
    private Label labelUpgradeCardDescriptionFor6;

    /**
     * shows the number of energy cubes required and descriptions of each upgrade card, when a player puts mouse on the upgrade card image.
     * @param cardName
     * * @author Jihyun, Lu
     */
    private void handleUpgradeCardDialog(String cardName, JFXDialog dialogUpgradeCard) { //TODO: change parameter to String (cardname) and add switch to change content of the dialog depending on the upgrade card
        labelUpgradeCardNameFor2.setText(cardName);
        labelUpgradeCardNameFor3.setText(cardName);
        labelUpgradeCardNameFor4.setText(cardName);
        labelUpgradeCardNameFor5.setText(cardName);
        labelUpgradeCardNameFor6.setText(cardName);
        if(cardName != null) {
            switch (cardName) {
                case "AdminPrivilege":
                    labelUpgradeCardDescriptionFor2.setText("permanent\n" + "3 energy cubes required\n" + "Once per round, you may give your robot priority for one register.");
                    labelUpgradeCardDescriptionFor3.setText("permanent\n" + "3 energy cubes required\n" + "Once per round, you may give your robot priority for one register.");
                    labelUpgradeCardDescriptionFor4.setText("permanent\n" + "3 energy cubes required\n" + "Once per round, you may give your robot priority for one register.");
                    labelUpgradeCardDescriptionFor5.setText("permanent\n" + "3 energy cubes required\n" + "Once per round, you may give your robot priority for one register.");
                    labelUpgradeCardDescriptionFor6.setText("permanent\n" + "3 energy cubes required\n" + "Once per round, you may give your robot priority for one register.");
                    break;
                case "MemorySwap":
                    labelUpgradeCardDescriptionFor2.setText("temporary\n" + "1 energy cubes required\n" + "Draw three cards. Then choose three from your hand to put on top of your deck");
                    labelUpgradeCardDescriptionFor3.setText("temporary\n" + "1 energy cubes required\n" + "Draw three cards. Then choose three from your hand to put on top of your deck");
                    labelUpgradeCardDescriptionFor4.setText("temporary\n" + "1 energy cubes required\n" + "Draw three cards. Then choose three from your hand to put on top of your deck");
                    labelUpgradeCardDescriptionFor5.setText("temporary\n" + "1 energy cubes required\n" + "Draw three cards. Then choose three from your hand to put on top of your deck");
                    labelUpgradeCardDescriptionFor6.setText("temporary\n" + "1 energy cubes required\n" + "Draw three cards. Then choose three from your hand to put on top of your deck");
                    break;
                case "RearLaser":
                    labelUpgradeCardDescriptionFor2.setText("permanent\n" + "2 energy cubes required\n" + "Your robot may shoot backward as well as forward.");
                    labelUpgradeCardDescriptionFor3.setText("permanent\n" + "2 energy cubes required\n" + "Your robot may shoot backward as well as forward.");
                    labelUpgradeCardDescriptionFor4.setText("permanent\n" + "2 energy cubes required\n" + "Your robot may shoot backward as well as forward.");
                    labelUpgradeCardDescriptionFor5.setText("permanent\n" + "2 energy cubes required\n" + "Your robot may shoot backward as well as forward.");
                    labelUpgradeCardDescriptionFor6.setText("permanent\n" + "2 energy cubes required\n" + "Your robot may shoot backward as well as forward.");
                    break;
                case "SpamBlocker":
                    labelUpgradeCardDescriptionFor2.setText("temporary\n" + "3 energy cubes required\n" + "Replace each SPAM damage card in your hand with a card from the top of your deck.");
                    labelUpgradeCardDescriptionFor3.setText("temporary\n" + "3 energy cubes required\n" + "Replace each SPAM damage card in your hand with a card from the top of your deck.");
                    labelUpgradeCardDescriptionFor4.setText("temporary\n" + "3 energy cubes required\n" + "Replace each SPAM damage card in your hand with a card from the top of your deck.");
                    labelUpgradeCardDescriptionFor5.setText("temporary\n" + "3 energy cubes required\n" + "Replace each SPAM damage card in your hand with a card from the top of your deck.");
                    labelUpgradeCardDescriptionFor6.setText("temporary\n" + "3 energy cubes required\n" + "Replace each SPAM damage card in your hand with a card from the top of your deck.");
                    break;
                default:
                    break;
            }
        }
        dialogUpgradeCard.show();
    }

    public String getMostCurrentlyChosenUpgradeCard() {
        return mostCurrentlyChosenUpgradeCard;
    }

    public void setMostCurrentlyChosenUpgradeCard(String chosenUpgradeCard) {
        this.mostCurrentlyChosenUpgradeCard = chosenUpgradeCard;
    }

    private String mostCurrentlyChosenUpgradeCard;

    public JFXButton getMostCurrentlyClickedUpgradeBtn() {
        return mostCurrentlyClickedUpgradeBtn;
    }

    public void setMostCurrentlyClickedUpgradeBtn(JFXButton mostCurrentlyClickedUpgradeBtn) {
        this.mostCurrentlyClickedUpgradeBtn = mostCurrentlyClickedUpgradeBtn;
    }

    private JFXButton mostCurrentlyClickedUpgradeBtn;

    @FXML
    private Label labelChosenCardFor2;
    @FXML
    private Label labelChosenCardFor3;
    @FXML
    private Label labelChosenCardFor4;
    @FXML
    private Label labelChosenCardFor5;
    @FXML
    private Label labelChosenCardFor6;
    private void handleChooseUpgradeCard(String cardName, JFXDialog upgradeDialog, JFXButton clickedBtn) {
        labelChosenCardFor2.setText("You chose: " + cardName+".  Click Buy button to buy this card.");
        labelChosenCardFor3.setText("You chose: " + cardName+".  Click Buy button to buy this card.");
        labelChosenCardFor4.setText("You chose: " + cardName+".  Click Buy button to buy this card.");
        labelChosenCardFor5.setText("You chose: " + cardName+".  Click Buy button to buy this card.");
        labelChosenCardFor6.setText("You chose: " + cardName+".  Click Buy button to buy this card.");
        setMostCurrentlyChosenUpgradeCard(cardName);
        setMostCurrentlyClickedUpgradeBtn(clickedBtn);
//        switch(upgradeDialog.getId()){
//            case "dialogUpgradeFor2":
//                btnUpgradeBuyFor2.setDisable(false);
//                break;
//            case "dialogUpgradeFor3":
//                btnUpgradeBuyFor3.setDisable(false);
//
//                break;
//            case "dialogUpgradeFor4":
//                btnUpgradeBuyFor4.setDisable(false);
//
//                break;
//            case "dialogUpgradeFor5":
//                btnUpgradeBuyFor5.setDisable(false);
//
//                break;
//            case "dialogUpgradeFor6":
//                btnUpgradeBuyFor6.setDisable(false);
//                break;
//            default:
//                break;
//        }
    }


    /**
     * handles when a player wants to buy an upgrade card
     * * @author Jihyun
     */
    private void handleBuyBtnClicked(JFXDialog dialogUpgrade) throws IOException {
        labelChosenCardFor2.setText("You bought" + mostCurrentlyChosenUpgradeCard +". Please wait for other players to finish.");
        labelChosenCardFor3.setText("You bought" + mostCurrentlyChosenUpgradeCard +". Please wait for other players to finish.");
        labelChosenCardFor4.setText("You bought" + mostCurrentlyChosenUpgradeCard +". Please wait for other players to finish.");
        labelChosenCardFor5.setText("You bought" + mostCurrentlyChosenUpgradeCard +". Please wait for other players to finish.");
        labelChosenCardFor6.setText("You bought" + mostCurrentlyChosenUpgradeCard +". Please wait for other players to finish.");
        BuyUpgrade buyUpgrade = new BuyUpgrade(true, mostCurrentlyChosenUpgradeCard);
        clientHandler.sendMessageSerialized(buyUpgrade);
        mostCurrentlyChosenUpgradeCard = null;
       /* for(Card boughtCard: availableUpgradeCards) {
            if(boughtCard.getCardName().equals(mostCurrentlyChosenUpradeCard)){
//                System.out.println("before someone buys a card: " + availableUpgradeCards);
                availableUpgradeCards.remove(boughtCard);
//                System.out.println("after someone bought a card: " + availableUpgradeCards);
            }
        }*/
    }

    /**
     * handles when a player does not want to buy any upgrade card for the current round.
     * @author Jihyun
     */
    private void handleNotBuyBtnClicked(JFXDialog dialogUpgrade) throws IOException {
        labelChosenCardFor2.setText("You decided to skip buying any upgrade card this round. Please wait for other players to finish.");
        labelChosenCardFor3.setText("You decided to skip buying any upgrade card this round. Please wait for other players to finish.");
        labelChosenCardFor4.setText("You decided to skip buying any upgrade card this round. Please wait for other players to finish.");
        labelChosenCardFor5.setText("You decided to skip buying any upgrade card this round. Please wait for other players to finish.");
        labelChosenCardFor6.setText("You decided to skip buying any upgrade card this round. Please wait for other players to finish.");
        BuyUpgrade NotBuyUpgrade = new BuyUpgrade(false, "null");
        clientHandler.sendMessageSerialized(NotBuyUpgrade);
        mostCurrentlyChosenUpgradeCard = null;
    }

    private void timerStops() {
        try {
            timeline.stop();
        } catch (Exception ignored) {
        }
        timername.setText("Time is up!");
        timerLabel.setOpacity(0);
        timer.setOpacity(0);
    }


    private void timerStarts() throws InterruptedException {
        timerLabel.setOpacity(1);
        timername.setOpacity(1);
        timer.setOpacity(1);
        timername.setText("Time is running!   ");
        timerLabel.textProperty().bind(timeSeconds.asString());
        timer.progressProperty().bind(timeSeconds.multiply(0.0333));
        if (timeline != null) {
            timeline.stop();
        }
        timeSeconds.set(STARTTIME);
        timeline = new Timeline();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(STARTTIME + 1),
                        new KeyValue(timeSeconds, -1)));
        timeline.playFromStart();
    }

    public void initPaneElement() throws IOException {
        //timer
        timerLabel.setOpacity(0);
        timername.setOpacity(0);
        timer.setOpacity(0);
        //titel
        round.setText("Round:   " + 1);
        mapName.setText("Map Name:   " + clientHandler.getClientData().getSelectedMap());

        if (clientHandler.getClientData().getCurrentPhase() == 0){
            phaseLabel.setText("Current Phase: Setup Phase");
        }

        //right playerList
        smallGridPaneWithClientID = new HashMap<>();
        for(int i = 0; i < 6; i++) {
            getSmallGridPane(i).setOpacity(0);
            getSmallGridPane(i).setDisable(true);
        }
        initPlayersInGridPane();
    }

    public void initPlayersInGridPane() throws IOException {
        int i = 0;
        for (PlayerData p : clientHandler.getClientData().getPlayers()) {
            addPlayersInTheGridPane(getSmallGridPane(i), p.getName(), p.getClientID(), p.getFigure());
            getSmallGridPane(i).setOpacity(1);
            getSmallGridPane(i).setDisable(false);
            //speichern smallerGridPane in a hashmap, index is clientID

            smallGridPaneWithClientID.put(p.getClientID(), getSmallGridPane(i));
            i++;
        }

    }

    /**
     * display player name and robot in player list which is on the right side of the main window
     * @author Tingyue, Lu
     */
    public void addPlayersInTheGridPane(GridPane playerSmallGridPane, String name, int clientID, int figure) throws IOException {
        //set name
        Label playerName = (Label) playerSmallGridPane.getChildren().get(0);
        playerName.setText(name + " (ID: " + clientID + ")");
        if(grid1ID == 0) {
            grid1ID = clientID;
        } else if(grid2ID == 0) {
            grid2ID = clientID;
        } else if(grid3ID == 0) {
            grid3ID = clientID;
        } else if(grid4ID == 0) {
            grid4ID = clientID;
        } else if(grid5ID == 0) {
            grid5ID = clientID;
        } else {
            grid6ID = clientID;
        }
        //set robot image
        VBox robotVBox = (VBox) playerSmallGridPane.getChildren().get(1);
        ImageView setRobotImage = (ImageView) robotVBox.getChildren().get(0);;
        setRobotImage.setImage(getRobotHeadImag(figure));
        //set current player info
        VBox robotInfoVBox = (VBox) playerSmallGridPane.getChildren().get(2);
        HBox robotInfoHBox = (HBox) robotInfoVBox.getChildren().get(0);
        VBox checkpointEnergyInfo = (VBox) robotInfoHBox.getChildren().get(0);
        Label checkPointInfo = (Label) checkpointEnergyInfo.getChildren().get(0);
        Label energyCubeInfo = (Label) checkpointEnergyInfo.getChildren().get(1);

        for (PlayerData p: clientHandler.getClientData().getPlayers()) {
            if (p.getClientID() == clientID){
                checkPointInfo.setText("Check Points: " + p.getReachedCheckpoints());
                energyCubeInfo.setText("Energy Cubes: " + p.getEnergycount());
            }
        }
    }

    /**
     * the check point listener
     * @author Lu
     */
    private void addCheckPointListener(){
        checkpointListener = (observableValue, oldValue, newValue) ->{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (PlayerData playerData: clientHandler.getClientData().getPlayers()) {
                            Integer playerID = playerData.getClientID();
                            getCheckPointLabel(smallGridPaneWithClientID.get(playerID)).
                                    setText("Check Points: " + playerData.getReachedCheckpoints());
                        }
                    }catch (Exception e){
                        //System.out.println("null point exception in check points ");
                    }

                }
            });
        };

        for (PlayerData playerData: clientHandler.getClientData().getPlayers()) {
            playerData.reachedCheckPointsProperty().addListener(checkpointListener);
        }

    }

    /**
     * the energy cube listener
     * @author Lu
     */
    private void addEnergyCubeListener(){
        energyListener = (observableValue, oldValue, newValue) ->{
            Platform.runLater(new Runnable() {
                @Override
                public void run(){
                    try {
                        for (PlayerData playerData: clientHandler.getClientData().getPlayers()){
                            Integer playerID = playerData.getClientID();

                            getEnergyCubeLabel(smallGridPaneWithClientID.get(playerID)).
                                    setText("Energy Cubes: " + playerData.getEnergycount());
                        }
                    }catch (Exception e){
                        //System.out.println("null point exception in energy cube ");
                    }

                }
            });
        };

        for (PlayerData playerData: clientHandler.getClientData().getPlayers()) {
            playerData.energycountProperty().addListener(energyListener);
        }

    }

    private Label getCheckPointLabel(GridPane pane) {
        VBox robotInfoVBox = (VBox) pane.getChildren().get(2);
        HBox robotInfoHBox = (HBox) robotInfoVBox.getChildren().get(0);
        VBox checkpointEnergyInfo = (VBox) robotInfoHBox.getChildren().get(0);
        Label checkPointLabel = (Label) checkpointEnergyInfo.getChildren().get(0);
        return checkPointLabel;
    }

    private Label getEnergyCubeLabel(GridPane pane) {
        VBox robotInfoVBox = (VBox) pane.getChildren().get(2);
        HBox robotInfoHBox = (HBox) robotInfoVBox.getChildren().get(0);
        VBox checkpointEnergyInfo = (VBox) robotInfoHBox.getChildren().get(0);
        Label energyCubeLabel = (Label) checkpointEnergyInfo.getChildren().get(1);
        return energyCubeLabel;
    }

    private Image getRobotHeadImag(int figure) throws IOException {
        return switch (figure) {
            case 1 -> new Image(getClass().getClassLoader().getResource("images/robots/robothead1.png").openStream());
            case 2 -> new Image(getClass().getClassLoader().getResource("images/robots/robothead2.png").openStream());
            case 3 -> new Image(getClass().getClassLoader().getResource("images/robots/robothead3.png").openStream());
            case 4 -> new Image(getClass().getClassLoader().getResource("images/robots/robothead4.png").openStream());
            case 5 -> new Image(getClass().getClassLoader().getResource("images/robots/robothead5.png").openStream());
            case 6 -> new Image(getClass().getClassLoader().getResource("images/robots/robothead6.png").openStream());
            default -> null;
        };
    }

    private GridPane getSmallGridPane(int i) {
        return switch (i) {
            case 0 -> smallgridpane1;
            case 1 -> smallgridpane2;
            case 2 -> smallgridpane3;
            case 3 -> smallgridpane4;
            case 4 -> smallgridpane5;
            default -> smallgridpane6;
        };
    }

    public void setClientHandler(ClientHandler clientHandler) throws IOException {
        this.clientHandler = clientHandler;
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
     * @author Stephan
     * adding a Transition to the small gridpanes
     * @param node
     */
    private void addTranslate(Node node) {
        Node node2;
        switch(node.getId()) {
            case "smallgridpane1" -> node2 = smallgrid1box;
            case "smallgridpane2" -> node2 = smallgrid2box;
            case "smallgridpane3" -> node2 = smallgrid3box;
            case "smallgridpane4" -> node2 = smallgrid4box;
            case "smallgridpane5" -> node2 = smallgrid5box;
            default -> node2 = smallgrid6box;
        }

        node.setOnMouseEntered(mouseEvent -> {
            delta = smallgridpane1.getWidth();
            oldTranslate = node.getTranslateX();
            newTranslate = node.getTranslateX() - delta;

            TranslateTransition transition = new TranslateTransition(Duration.millis(200));
            transition.setFromX(oldTranslate);
            transition.setToX(newTranslate);
            transition.setNode(node2);
            FadeTransition fade = new FadeTransition(Duration.millis(400));
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setNode(node2);
            ParallelTransition parallelTransition = new ParallelTransition(transition, fade);
            parallelTransition.play();
            parallelTransition.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    node2.setOpacity(1);
                }
            });
        });
        node.setOnMouseExited(mouseEvent -> {
            TranslateTransition transition = new TranslateTransition(Duration.millis(200));
            transition.setFromX(newTranslate);
            transition.setToX(newTranslate + delta);
            transition.setNode(node2);

            FadeTransition fade = new FadeTransition(Duration.millis(200));
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setNode(node2);
            ParallelTransition parallelTransition = new ParallelTransition(transition, fade);
            parallelTransition.play();
            transition.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    node2.setOpacity(0);
                }
            });
        });
    }

    /**
     * @author Stephan
     * setting the default images to the gridpanes
     * @throws IOException
     */
    private void initializePlayerInfoOnHover() throws IOException {
        smallgrid1box.setOpacity(0);
        smallgrid2box.setOpacity(0);
        smallgrid3box.setOpacity(0);
        smallgrid4box.setOpacity(0);
        smallgrid5box.setOpacity(0);
        smallgrid6box.setOpacity(0);
        //addTranslate(smallgridpane1);
        addTranslate(smallgridpane2);
        addTranslate(smallgridpane3);
        addTranslate(smallgridpane4);
        addTranslate(smallgridpane5);
        addTranslate(smallgridpane6);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    setImage(player1reg0);
                    setImage(player1reg1);
                    setImage(player1reg2);
                    setImage(player1reg3);
                    setImage(player1reg4);
                    if(playerCount >= 3) {
                        setImage(player2reg0);
                        setImage(player2reg1);
                        setImage(player2reg2);
                        setImage(player2reg3);
                        setImage(player2reg4);
                    }
                    if(playerCount >= 4) {
                        setImage(player3reg0);
                        setImage(player3reg1);
                        setImage(player3reg2);
                        setImage(player3reg3);
                        setImage(player3reg4);
                    }
                    if(playerCount >= 5) {
                        setImage(player4reg0);
                        setImage(player4reg1);
                        setImage(player4reg2);
                        setImage(player4reg3);
                        setImage(player4reg4);
                    }
                    if(playerCount >= 6) {
                        setImage(player5reg0);
                        setImage(player5reg1);
                        setImage(player5reg2);
                        setImage(player5reg3);
                        setImage(player5reg4);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    private void setImage(ImageView imageview) throws IOException {
        Image defaultIMG = new Image(getClass().getClassLoader().getResource("images/cards/defaultCard.png").openStream());
        imageview.setImage(defaultIMG);
        imageview.setPreserveRatio(true);
        imageview.setFitWidth(picSize);
    }

    /**
     * @author Stephan
     * adding borders to the current registers
     * @throws IOException
     */
    private void addBorderToRegister() throws IOException {
        loadRegisterBorder(registerBorder1);
        if(playerCount >= 3) {
            loadRegisterBorder(registerBorder2);
        }
        if(playerCount >= 4) {
            loadRegisterBorder(registerBorder3);
        }
        if(playerCount >= 5) {
            loadRegisterBorder(registerBorder4);
        }
        if(playerCount >= 6) {
            loadRegisterBorder(registerBorder5);
        }

        currentRegisterListener = ((observableValue, oldRegister, newRegister) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    player1RegisterGrid.getChildren().remove(registerBorder1);
                    if(playerCount >= 3) {
                        player2RegisterGrid.getChildren().remove(registerBorder2);
                    }
                    if(playerCount >= 4) {
                        player3RegisterGrid.getChildren().remove(registerBorder3);
                    }
                    if(playerCount >= 5) {
                        player4RegisterGrid.getChildren().remove(registerBorder4);
                    }
                    if(playerCount >= 6) {
                        player5RegisterGrid.getChildren().remove(registerBorder5);
                    }

                    if(newRegister.intValue() != 0) {
                        player1RegisterGrid.add(registerBorder1, newRegister.intValue()-1, 0);
                        if(playerCount >= 3) {
                            player2RegisterGrid.add(registerBorder2, newRegister.intValue()-1, 0);
                        }
                        if(playerCount >= 4) {
                            player3RegisterGrid.add(registerBorder3, newRegister.intValue()-1, 0);
                        }
                        if(playerCount >= 5) {
                            player4RegisterGrid.add(registerBorder4, newRegister.intValue()-1, 0);
                        }
                        if(playerCount >= 6) {
                            player5RegisterGrid.add(registerBorder5, newRegister.intValue()-1, 0);
                        }
                    }
                }
            });
        });

        clientHandler.getClientData().currentRegisterProperty().addListener(currentRegisterListener);
    }

    private void loadRegisterBorder(ImageView imageview) throws IOException {
        imageview.setImage(new Image(getClass().getClassLoader().getResource("images/cards/border3.png").openStream()));
        imageview.setPreserveRatio(true);
        imageview.setFitWidth(picSize);
    }

    private void addRegisterListeners() {
        register0Listener = (observable, oldValue, newValue) -> {
            updateRegister(newValue);
        };
        register1Listener = (observable, oldValue, newValue) -> {
            updateRegister(newValue);
        };
        register2Listener = (observable, oldValue, newValue) -> {
            updateRegister(newValue);
        };
        register3Listener = (observable, oldValue, newValue) -> {
            updateRegister(newValue);
        };
        register4Listener = (observable, oldValue, newValue) -> {
            updateRegister(newValue);
        };

        if(playerCount >= 2) {
            clientHandler.getClientData().getPlayer1Data().register0Property().addListener(register0Listener);
            clientHandler.getClientData().getPlayer1Data().register1Property().addListener(register1Listener);
            clientHandler.getClientData().getPlayer1Data().register2Property().addListener(register2Listener);
            clientHandler.getClientData().getPlayer1Data().register3Property().addListener(register3Listener);
            clientHandler.getClientData().getPlayer1Data().register4Property().addListener(register4Listener);
        }
        if(playerCount >= 3) {
            clientHandler.getClientData().getPlayer2Data().register0Property().addListener(register0Listener);
            clientHandler.getClientData().getPlayer2Data().register1Property().addListener(register1Listener);
            clientHandler.getClientData().getPlayer2Data().register2Property().addListener(register2Listener);
            clientHandler.getClientData().getPlayer2Data().register3Property().addListener(register3Listener);
            clientHandler.getClientData().getPlayer2Data().register4Property().addListener(register4Listener);
        }
        if(playerCount >= 4) {
            clientHandler.getClientData().getPlayer3Data().register0Property().addListener(register0Listener);
            clientHandler.getClientData().getPlayer3Data().register1Property().addListener(register1Listener);
            clientHandler.getClientData().getPlayer3Data().register2Property().addListener(register2Listener);
            clientHandler.getClientData().getPlayer3Data().register3Property().addListener(register3Listener);
            clientHandler.getClientData().getPlayer3Data().register4Property().addListener(register4Listener);
        }
        if(playerCount >= 5) {
            clientHandler.getClientData().getPlayer4Data().register0Property().addListener(register0Listener);
            clientHandler.getClientData().getPlayer4Data().register1Property().addListener(register1Listener);
            clientHandler.getClientData().getPlayer4Data().register2Property().addListener(register2Listener);
            clientHandler.getClientData().getPlayer4Data().register3Property().addListener(register3Listener);
            clientHandler.getClientData().getPlayer4Data().register4Property().addListener(register4Listener);
        }
        if(playerCount == 6) {
            clientHandler.getClientData().getPlayer5Data().register0Property().addListener(register0Listener);
            clientHandler.getClientData().getPlayer5Data().register1Property().addListener(register1Listener);
            clientHandler.getClientData().getPlayer5Data().register2Property().addListener(register2Listener);
            clientHandler.getClientData().getPlayer5Data().register3Property().addListener(register3Listener);
            clientHandler.getClientData().getPlayer5Data().register4Property().addListener(register4Listener);
        }
    }

    /**
     * @author Stephan
     * updating the Registers
     */
    private void updateRegister(Card newCard) {
        int register = clientHandler.getClientData().getCurrentRegister();
        ArrayList<ActiveCard> activeCards = null;
        ArrayList<Message> log = clientHandler.getLogOfMessages();
        int size = log.size();
        for (int i = 1; size - i > 0; i++) {
            if (log.get(size - i).toString().startsWith("CurrentCards", 25)) {
                CurrentCards currentCards = (CurrentCards) log.get(size - i);
                activeCards = currentCards.getCurrentCards();
                i = size;
            }
        }
        if (activeCards != null && newCard != null) {
            ArrayList<ActiveCard> finalActiveCards = activeCards;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        for(int i = 0; i < finalActiveCards.size(); i++) {
                            if (finalActiveCards.get(i).getClientID() == grid2ID) {
                                switch (register) {
                                    case 0 -> player1reg0.setImage(getImage(finalActiveCards.get(i).getCard()));
                                    case 1 -> player1reg1.setImage(getImage(finalActiveCards.get(i).getCard()));
                                    case 2 -> player1reg2.setImage(getImage(finalActiveCards.get(i).getCard()));
                                    case 3 -> player1reg3.setImage(getImage(finalActiveCards.get(i).getCard()));
                                    case 4 -> player1reg4.setImage(getImage(finalActiveCards.get(i).getCard()));
                                }
                                i = finalActiveCards.size();
                            }
                        }
                        if(playerCount >= 3) {
                            for(int i = 0; i < finalActiveCards.size(); i++) {
                                if (finalActiveCards.get(i).getClientID() == grid3ID) {
                                    switch (register) {
                                        case 0 -> player2reg0.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 1 -> player2reg1.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 2 -> player2reg2.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 3 -> player2reg3.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 4 -> player2reg4.setImage(getImage(finalActiveCards.get(i).getCard()));
                                    }
                                    i = finalActiveCards.size();
                                }
                            }
                        }
                        if(playerCount >= 4) {
                            for(int i = 0; i < finalActiveCards.size(); i++) {
                                if (finalActiveCards.get(i).getClientID() == grid4ID) {
                                    switch (register) {
                                        case 0 -> player3reg0.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 1 -> player3reg1.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 2 -> player3reg2.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 3 -> player3reg3.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 4 -> player3reg4.setImage(getImage(finalActiveCards.get(i).getCard()));
                                    }
                                    i = finalActiveCards.size();
                                }
                            }
                        }
                        if(playerCount >= 5) {
                            for(int i = 0; i < finalActiveCards.size(); i++) {
                                if (finalActiveCards.get(i).getClientID() == grid5ID) {
                                    switch (register) {
                                        case 0 -> player4reg0.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 1 -> player4reg1.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 2 -> player4reg2.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 3 -> player4reg3.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 4 -> player4reg4.setImage(getImage(finalActiveCards.get(i).getCard()));
                                    }
                                    i = finalActiveCards.size();
                                }
                            }
                        }
                        if(playerCount >= 6) {
                            for(int i = 0; i < finalActiveCards.size(); i++) {
                                if (finalActiveCards.get(i).getClientID() == grid6ID) {
                                    switch (register) {
                                        case 0 -> player5reg0.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 1 -> player5reg1.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 2 -> player5reg2.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 3 -> player5reg3.setImage(getImage(finalActiveCards.get(i).getCard()));
                                        case 4 -> player5reg4.setImage(getImage(finalActiveCards.get(i).getCard()));
                                    }
                                    i = finalActiveCards.size();
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else if (newCard == null) {
            try {
                setImage(player1reg0);
                setImage(player1reg1);
                setImage(player1reg2);
                setImage(player1reg3);
                setImage(player1reg4);
                if(playerCount >= 3) {
                    setImage(player2reg0);
                    setImage(player2reg1);
                    setImage(player2reg2);
                    setImage(player2reg3);
                    setImage(player2reg4);
                } if(playerCount >= 4) {
                    setImage(player3reg0);
                    setImage(player3reg1);
                    setImage(player3reg2);
                    setImage(player3reg3);
                    setImage(player3reg4);
                }
                if(playerCount >= 5) {
                    setImage(player4reg0);
                    setImage(player4reg1);
                    setImage(player4reg2);
                    setImage(player4reg3);
                    setImage(player4reg4);
                }
                if(playerCount >= 6) {
                    setImage(player5reg0);
                    setImage(player5reg1);
                    setImage(player5reg2);
                    setImage(player5reg3);
                    setImage(player5reg4);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private Image getImage(String cardName) throws IOException {
        if(cardName != null) {
            return switch (cardName) {
                case "MoveI" -> new Image(getClass().getClassLoader().getResource("images/cards/move1.png").openStream());
                case "MoveII" -> new Image(getClass().getClassLoader().getResource("images/cards/move2.png").openStream());
                case "MoveIII" -> new Image(getClass().getClassLoader().getResource("images/cards/move3.png").openStream());
                case "Again" -> new Image(getClass().getClassLoader().getResource("images/cards/again.png").openStream());
                case "BackUp" -> new Image(getClass().getClassLoader().getResource("images/cards/moveback.png").openStream());
                case "PowerUp" -> new Image(getClass().getClassLoader().getResource("images/cards/powerup.png").openStream());
                case "TurnRight" -> new Image(getClass().getClassLoader().getResource("images/cards/rightturn.png").openStream());
                case "TurnLeft" -> new Image(getClass().getClassLoader().getResource("images/cards/leftturn.png").openStream());
                case "UTurn" -> new Image(getClass().getClassLoader().getResource("images/cards/uturn.png").openStream());
                case "Spam" -> new Image(getClass().getClassLoader().getResource("images/cards/spam.png").openStream());
                case "Virus" -> new Image(getClass().getClassLoader().getResource("images/cards/virus.png").openStream());
                case "Worm" -> new Image(getClass().getClassLoader().getResource("images/cards/worm.png").openStream());
                case "Trojan" -> new Image(getClass().getClassLoader().getResource("images/cards/trojan.png").openStream());
                default -> new Image(getClass().getClassLoader().getResource("images/cards/defaultCard.png").openStream());
            };
        } else return new Image(getClass().getClassLoader().getResource("images/cards/defaultCard.png").openStream());
    }


    public void switchToMapRoom(ActionEvent actionEvent) throws IOException {
        initClientDataAfterGame();

        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        //stage.close();
        ChatWindowController.setClientHandler(clientHandler);
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("NewWaitingRoom.fxml"));
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
        stage.setHeight(670);
        stage.setWidth(884);
        stage.setResizable(false);
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
        stage.centerOnScreen();
        stage.setOnCloseRequest(event -> {
            try {
                stage.close();
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
     * if game finished and the player still want to keep playing the game,
     * reinitialize data in clientData and PlayerData
     * if there are AIs, send message to remove AIs
     * @author Tingyue
     */
    private void initClientDataAfterGame() throws IOException {
        //reset client and player data
        if (getAISize() != 0){
            for (PlayerData p : clientHandler.getClientData().getPlayers()) {
                if (p.isAI()){
                    ConnectionUpdate connectionUpdate = new ConnectionUpdate(p.getClientID(), true,"Remove");
                    clientHandler.sendMessageSerialized(connectionUpdate);
                }
            }
        }

        clientHandler.getClientData().setSelectedMap(null);
        clientHandler.getClientData().setChosenMap(null);
        clientHandler.getClientData().setRound(0);
        for (PlayerData p : clientHandler.getClientData().getPlayers()) {
            p.setEnergycount(5);
            p.setReachedCheckpoints(0);
        }

    }

    /**
     * Calculate how many AI's there are
     * @author Tingyue
     */
    private int getAISize(){
        int count = 0;
        for (PlayerData p : clientHandler.getClientData().getPlayers()) {
            if (p.isAI()){
                count++;
            }
        }
        return count;
    }

    public void quitGame(ActionEvent actionEvent) throws IOException {
        Stage currStg = (Stage) winningQuit.getScene().getWindow();
        currStg.close();
        System.exit(0);
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

                        //set name
                        Label playerName = (Label) removedPlayerGridPane.getChildren().get(0);
                        playerName.setText("");
                        //set robot image
                        VBox robotVBox = (VBox) removedPlayerGridPane.getChildren().get(1);
                        ImageView setRobotImage = (ImageView) robotVBox.getChildren().get(0);;
                        setRobotImage.setImage(null);
                        //set current player info
                        getCheckPointLabel(removedPlayerGridPane).setText("");
                        getEnergyCubeLabel(removedPlayerGridPane).setText("");

                        smallGridPaneWithClientID.remove(removedPlayerID);
                    }
                }
            }
        });
    }

}
