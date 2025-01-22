package application;

import card.Card;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import server.ClientHandler;
import server.protocol.aktionen.ChooseRegister;
import server.protocol.aktionen.PlayerTurning;
import server.protocol.aktionen.ReturnCards;
import server.protocol.spielkarten.PlayCard;
import server.protocol.spielzug.SelectedCard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

public class PlayerDeckController extends Application {

    @FXML
    private Label permanentLabel;
    @FXML
    private Label installedPermanentLabel;
    @FXML
    private Label temporateLabel;
    @FXML
    private Button Card1 = new Button();
    @FXML
    private Button Card2;
    @FXML
    private Button Card3;
    @FXML
    private Button Card4;
    @FXML
    private Button Card5;
    @FXML
    private Button Card6;
    @FXML
    private Button Card7;
    @FXML
    private Button Card8;
    @FXML
    private Button Card9;
    @FXML
    private ImageView Register0;
    @FXML
    private ImageView Register1;
    @FXML
    private ImageView Register2;
    @FXML
    private ImageView Register3;
    @FXML
    private ImageView Register4;
    @FXML
    private Button checkpoints;
    @FXML
    private Button damageCards;
    @FXML
    private Button energyCubes;
    @FXML
    private Button help;
    @FXML
    private Button upgradeCards;
    @FXML
    private VBox playerDeck;
    @FXML
    private VBox hoverVbox;
    @FXML
    private GridPane registerDeck = new GridPane();
    @FXML
    private VBox cardDeck1;
    @FXML
    private VBox cardDeck2;
    @FXML
    private VBox cardDeck3;
    @FXML
    private Label msg;
    @FXML
    private ImageView checkpointsimg;
    @FXML
    private ImageView energycubesimg;
    @FXML
    private Label numCheckpoints;
    @FXML
    private Label numEnergycubes;
    @FXML
    private ImageView registerBorder;
    @FXML
    private JFXButton upgrade;

    private ClientHandler clientHandler;
    ChangeListener<ArrayList<Card>> cardListener;
    ListChangeListener<BooleanProperty> registerFilledListener;
    ChangeListener<ArrayList<Card>> drawnDamageListener;
    ChangeListener<Number> energyListener;
    ChangeListener<Number> checkpointListener;
    private ChangeListener<Number> currentPhaseForHelpListener;
    private ChangeListener<Number> currentRegisterListener;
    ChangeListener<ArrayList<Card>> availableDamageCardsListener;

    ChangeListener<ArrayList<String>> memorySwapExchangeCardsListener;

    MapChangeListener<Integer,Card> newlyBoughtUpgradeListener;
    ChangeListener<ArrayList<String>> cardsYouGotNowListener;
    ChangeListener<Number> roundListener;

    private Node parentNode;
    List<ImageView> listOfRegisters;
    private ArrayList<JFXButton> adminRegButtons = new ArrayList<>();

    @FXML
    private StackPane stack;
    @FXML
    private JFXDialog dialogUpgrade;
    @FXML
    private JFXButton closeBtnUpgrade;

    @FXML
    private JFXButton btnMemorySwap;

    @FXML
    private JFXButton btnPermanent1;

    @FXML
    private JFXButton btnPermanent2;

    @FXML
    private JFXButton btnPermanent3;

    @FXML
    private JFXButton btnSpamBlocker;

    @FXML
    private StackPane stackupgrade;

    @FXML
    private JFXButton btnUpgradeReg1;

    @FXML
    private JFXButton btnUpgradeReg2;

    @FXML
    private JFXButton btnUpgradeReg3;

    @FXML
    private JFXButton btnUpgradeReg4;

    @FXML
    private JFXButton btnUpgradeReg5;

    private ArrayList<String> boughtUpgradeCardsList;


    public void initialize() throws IOException {
        setClientHandler(StartController.getClientHandler());
        listOfRegisters = Arrays.asList(Register0, Register1, Register2, Register3, Register4);
        dialogUpgrade.setDialogContainer(stack);
        dialogUpgradeDescription.setDialogContainer(stackupgrade);
        dialogMemorySwap.setDialogContainer(stackMemory);

        handleButtonOnHover(damageCards);
        handleButtonOnHover(help);
        hoverStageForDamageCards = openHoverWindowForDamageCards(damageCards);
        hoverStageForHelp = openHoverWindowForHelp(help);
        mouseOnHoverWindowForHelp.changeContentWithPhaseForHelp("Setup Phase");
        iniCardsList();
        cardDeck1.setDisable(true);
        cardDeck2.setDisable(true);
        cardDeck3.setDisable(true);
        cardDeck1.setOpacity(0);
        cardDeck2.setOpacity(0);
        cardDeck3.setOpacity(0);

        for(ImageView register: listOfRegisters) {
            removeCardFromRegister(register);
        }
        Image energycubes = new Image(getClass().getClassLoader().getResource("images/playerDeck/energycube.png").openStream());
        energycubesimg.setImage(energycubes);
        numEnergycubes.setText(String.valueOf(clientHandler.getClientData().getYourPlayerData().getEnergycount()));
        Image checkpoints = new Image(getClass().getClassLoader().getResource("images/playerDeck/checkpoint.png").openStream());
        checkpointsimg.setImage(checkpoints);

        //upgrade cards with descriptions
        upgradeBtnAdminPrivilege.setOnAction(ActionEvent-> showUpgradesWithDescriptions(upgradeBtnAdminPrivilege.getText()));
        upgradeBtnMemorySwap.setOnAction(ActionEvent-> showUpgradesWithDescriptions(upgradeBtnMemorySwap.getText()));
        upgradeBtnRearLaser.setOnAction(ActionEvent-> showUpgradesWithDescriptions(upgradeBtnRearLaser.getText()));
        upgradeBtnSpamBlocker.setOnAction(ActionEvent-> showUpgradesWithDescriptions(upgradeBtnSpamBlocker.getText()));

        upgrade.setOnAction(ActionEvent-> dialogUpgrade.show());
        closeBtnUpgrade.setOnAction(ActionEvent-> dialogUpgrade.close());

        installedPermanentUpgradeList = new ArrayList<>(); // a list of bought permanent upgrade cards(buttons)
        boughtUpgradeCardsList = new ArrayList<>(); // a list of bought upgrade cards (string) (both permanent and temporary)


        btnUpgradeReg1.setDisable(true);
        btnUpgradeReg2.setDisable(true);
        btnUpgradeReg3.setDisable(true);
        btnUpgradeReg4.setDisable(true);
        btnUpgradeReg5.setDisable(true);


        //adminRegisterButtons list
        adminRegButtons.add(btnUpgradeReg1);
        adminRegButtons.add(btnUpgradeReg2);
        adminRegButtons.add(btnUpgradeReg3);
        adminRegButtons.add(btnUpgradeReg4);
        adminRegButtons.add(btnUpgradeReg5);

        //set adminRegisterbuttons with " " text
        btnUpgradeReg1.setText(" ");
        btnUpgradeReg2.setText(" ");
        btnUpgradeReg3.setText(" ");
        btnUpgradeReg4.setText(" ");
        btnUpgradeReg5.setText(" ");

        //bought upgrade cards
        btnPermanent1.setDisable(true);
        btnPermanent2.setDisable(true);
        btnPermanent3.setDisable(true);
        btnMemorySwap.setDisable(true);
        btnSpamBlocker.setDisable(true);


        btnPermanent1.setText(" ");
        btnPermanent2.setText(" ");
        btnPermanent3.setText(" ");

        updateCards();
        updateRegisterFilled();
        updateDamageCards();

        addListenerForCheckpoint();
        addListenerForEnergyCube();
//        addListenerForDamageCards();
        addListenerForcurrentPhaseForHelp();
        addListenerForNewlyBoughtUpgrade();
        addListenerForExchangeCards();

        clientHandler.getClientData().getYourPlayerData().registersFilledProperty().addListener(registerFilledListener);
        clientHandler.getClientData().getYourPlayerData().drawnDamageCardsProperty().addListener(drawnDamageListener);
        clientHandler.getClientData().cardProperty().addListener(cardListener);
        clientHandler.getClientData().currentPhaseProperty().addListener((c, oldValue, newValue) -> {
            if(newValue.intValue() == 1) {
                clearRegisters();
            }
        });

        addHoverEffect(1, 1.1, 150, Card1);
        addHoverEffect(1, 1.1, 150, Card2);
        addHoverEffect(1, 1.1, 150, Card3);
        addHoverEffect(1, 1.1, 150, Card4);
        addHoverEffect(1, 1.1, 150, Card5);
        addHoverEffect(1, 1.1, 150, Card6);
        addHoverEffect(1, 1.1, 150, Card7);
        addHoverEffect(1, 1.1, 150, Card8);
        addHoverEffect(1, 1.1, 150, Card9);

        addBorderToRegister();
        cardsYouGotNowListener();
    }

    /**
     * @author Stephan
     * setting the pictures of the cardsYouGotNow message
     */
    private void cardsYouGotNowListener() {
        cardsYouGotNowListener = ((observableValue, oldValue, newValue) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        Register0.setImage(new Image(getClass().getClassLoader().getResource("images/cards/" + getImageName(newValue.get(0)) + ".png").openStream()));
                        Register1.setImage(new Image(getClass().getClassLoader().getResource("images/cards/" + getImageName(newValue.get(1)) + ".png").openStream()));
                        Register2.setImage(new Image(getClass().getClassLoader().getResource("images/cards/" + getImageName(newValue.get(2)) + ".png").openStream()));
                        Register3.setImage(new Image(getClass().getClassLoader().getResource("images/cards/" + getImageName(newValue.get(3)) + ".png").openStream()));
                        Register4.setImage(new Image(getClass().getClassLoader().getResource("images/cards/" + getImageName(newValue.get(4)) + ".png").openStream()));

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
        });
        clientHandler.getClientData().getYourPlayerData().cardsYouGotNow().addListener(cardsYouGotNowListener);
    }

    private void addListenerForExchangeCards(){
        memorySwapExchangeCardsListener = ((observableValue, oldValue, newValue) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        cardsInMyHand.clear();
                        mostCurrentlyClickedCardFor1 = null;
                        mostCurrentlyClickedCardFor2 = null;
                        mostCurrentlyClickedCardFor3 = null;
                        handleExchangeCards(newValue);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
        clientHandler.getClientData().exchangeCardsProperty().addListener(memorySwapExchangeCardsListener);
    }
    @FXML
    private StackPane stackMemory;
    @FXML
    private JFXDialog dialogMemorySwap;
    @FXML
    private Label labelNewCard1;

    @FXML
    private Label labelNewCard2;

    @FXML
    private Label labelNewCard3;
    @FXML
    private ImageView imageViewArrow1;

    @FXML
    private ImageView imageViewArrow2;

    @FXML
    private ImageView imageViewArrow3;

    @FXML
    private JFXButton btnExchange1;

    @FXML
    private JFXButton btnExchange2;

    @FXML
    private JFXButton btnExchange3;

    @FXML
    private ComboBox<String> comboBoxExchange1;

    @FXML
    private ComboBox<String> comboBoxExchange2;

    @FXML
    private ComboBox<String> comboBoxExchange3;
    @FXML
    private Label labelChosenCard1;
    @FXML
    private Label labelChosenCard2;
    @FXML
    private Label labelChosenCard3;
    private void handleExchangeCards(ArrayList<String> exchangeCardsList) throws IOException {
        imageViewArrow1.setImage(new Image(getClass().getClassLoader().getResource("images/arrow.png").openStream()));
        imageViewArrow1.setRotate(180);
        imageViewArrow2.setImage(new Image(getClass().getClassLoader().getResource("images/arrow.png").openStream()));
        imageViewArrow2.setRotate(180);
        imageViewArrow3.setImage(new Image(getClass().getClassLoader().getResource("images/arrow.png").openStream()));
        imageViewArrow3.setRotate(180);
        labelNewCard1.setText(exchangeCardsList.get(0));
        labelNewCard2.setText(exchangeCardsList.get(1));
        labelNewCard3.setText(exchangeCardsList.get(2));
        updateComboBoxes(comboBoxExchange1);
        btnExchange2.setDisable(true);
        btnExchange3.setDisable(true);
        comboBoxExchange2.setDisable(true);
        comboBoxExchange3.setDisable(true);
        listenerForComboBox1(comboBoxExchange1);
        listenerForComboBox2(comboBoxExchange2);
        listenerForComboBox3(comboBoxExchange3);

        dialogMemorySwap.show();
        dialogMemorySwap.setOverlayClose(false);

        btnExchange1.setOnAction(ActionEvent->{
            cardsInMyHand.remove(mostCurrentlyClickedCardFor1);
            comboBoxExchange2.setDisable(false);
            labelChosenCard1.setText(mostCurrentlyClickedCardFor1);
            exchangeBtn1Clicked(btnExchange1, mostCurrentlyClickedCardFor1);
            btnExchange1.setDisable(true);
            btnExchange2.setDisable(false);
            comboBoxExchange1.setDisable(true);
        });

        btnExchange2.setOnAction(ActionEvent->{
            comboBoxExchange3.setDisable(false);
            cardsInMyHand.remove(mostCurrentlyClickedCardFor2);
            labelChosenCard2.setText(mostCurrentlyClickedCardFor2);
            exchangeBtn2Clicked(btnExchange2, mostCurrentlyClickedCardFor2);
            btnExchange2.setDisable(true);
            btnExchange3.setDisable(false);
            comboBoxExchange2.setDisable(true);
        });

        btnExchange3.setOnAction(ActionEvent->{
            comboBoxExchange3.setDisable(true);
            cardsInMyHand.remove(mostCurrentlyClickedCardFor3);
            labelChosenCard3.setText(mostCurrentlyClickedCardFor3);
            try {
                exchangeBtn3Clicked(btnExchange3, mostCurrentlyClickedCardFor3);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            btnExchange3.setDisable(true);
            comboBoxExchange3.setDisable(true);
        });
    }

    private void exchangeBtn3Clicked(JFXButton exchangeBtn, String chosenCard) throws IOException {
                ArrayList<String> chosen3CardsFromMyHand = new ArrayList<>();
                chosen3CardsFromMyHand.add(mostCurrentlyClickedCardFor1);
                chosen3CardsFromMyHand.add(mostCurrentlyClickedCardFor2);
                chosen3CardsFromMyHand.add(mostCurrentlyClickedCardFor3);
                clearRegisters();

                ReturnCards returnCards = new ReturnCards(chosen3CardsFromMyHand);
                try {
                    clientHandler.sendMessageSerialized(returnCards);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                clientHandler.getClientData().removeFromBoughtCard(mostCurrentlyClickedCardFor3);

                mostCurrentlyClickedCardFor1 = null;
                mostCurrentlyClickedCardFor2 = null;
                mostCurrentlyClickedCardFor3 = null;
//                        for(Card usedCard: clientHandler.getClientData().getBoughtUpgradeCards()) {
//                            if(usedCard.getCardName().equals("MemorySwap")) {
//                                System.out.println("before removed used memoryswap card: " + clientHandler.getClientData().getBoughtUpgradeCards() );
//
//                                clientHandler.getClientData().getBoughtUpgradeCards().remove(usedCard);
////                                System.out.println("temporary card " + usedCard.getCardName() + "is removed.");
//                            }
//                            System.out.println("after removed used memoryswap card: " + clientHandler.getClientData().getBoughtUpgradeCards() );
//                        }
        dialogMemorySwap.close();

    }
    private void exchangeBtn2Clicked(JFXButton exchangeBtn, String chosenCard){
        comboBoxExchange3.getItems().addAll(cardsInMyHand);

    }
    private void exchangeBtn1Clicked(JFXButton exchangeBtn, String chosenCard) {
        comboBoxExchange2.getItems().addAll(cardsInMyHand);
    }
    public void setMostCurrentlyClickedCardFor1(String mostCurrentlyClickedCardFor1) {
        this.mostCurrentlyClickedCardFor1 = mostCurrentlyClickedCardFor1;
    }

    private String mostCurrentlyClickedCardFor1;

    public void setMostCurrentlyClickedCardFor2(String mostCurrentlyClickedCardFor2) {
        this.mostCurrentlyClickedCardFor2 = mostCurrentlyClickedCardFor2;
    }

    private String mostCurrentlyClickedCardFor2;

    public void setMostCurrentlyClickedCardFor3(String mostCurrentlyClickedCardFor3) {
        this.mostCurrentlyClickedCardFor3 = mostCurrentlyClickedCardFor3;
    }

    private String mostCurrentlyClickedCardFor3;
    private void listenerForComboBox1(ComboBox<String> comboBoxExchange) {
        comboBoxExchange.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            setMostCurrentlyClickedCardFor1(newValue);

        });
    }

    private void listenerForComboBox2(ComboBox<String> comboBoxExchange) {
        comboBoxExchange.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            setMostCurrentlyClickedCardFor2(newValue);

        });
    }

    private void listenerForComboBox3(ComboBox<String> comboBoxExchange) {
        comboBoxExchange.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            setMostCurrentlyClickedCardFor3(newValue);

        });
    }
    private ArrayList<String> cardsInMyHand =new ArrayList<>();
    private void updateComboBoxes(ComboBox<String> comboBoxExchange) {
        for(Card myCard: clientHandler.getClientData().getCardsInHand()) {
            comboBoxExchange.getItems().add(myCard.getCardName());
            cardsInMyHand.add(myCard.getCardName());
        }
    }
    private void upgradeRegBtnClicked() {
        handleUpgradeRegisterBtnclicked(btnUpgradeReg1);
        handleUpgradeRegisterBtnclicked(btnUpgradeReg2);
        handleUpgradeRegisterBtnclicked(btnUpgradeReg3);
        handleUpgradeRegisterBtnclicked(btnUpgradeReg4);
        handleUpgradeRegisterBtnclicked(btnUpgradeReg5);
    }
    private int getRegNumFromButton(JFXButton btn){
        switch (btn.getId()) {
            case "btnUpgradeReg1":
                return 1;
            case "btnUpgradeReg2":
                return 2;
            case "btnUpgradeReg3":
                return 3;
            case "btnUpgradeReg4":
                return 4;
            case "btnUpgradeReg5":
                return 5;
            default:
                return 0;
        }
    }

    /**
     * handles when a player clicks one of the small buttons which are placed on top of all 5 registers to choose a specific register where the player wants to activate adminprivilege permanent upgrade card.
     * @author Jihyun
     * @param btnUpgradeReg
     */
    private void handleUpgradeRegisterBtnclicked(JFXButton btnUpgradeReg) {
        btnUpgradeReg.setOnAction(ActionEvent->{
            for(Card card: clientHandler.getClientData().getBoughtUpgradeCards()) {
                if(card.getCardName().equals("AdminPrivilege")) {
                    ChooseRegister chooseRegister = new ChooseRegister(getRegNumFromButton(btnUpgradeReg));
                    try {
                        clientHandler.sendMessageSerialized(chooseRegister);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    btnUpgradeReg.setText("AdminPrivilege");
                    btnUpgradeReg1.setDisable(true);
                    btnUpgradeReg2.setDisable(true);
                    btnUpgradeReg3.setDisable(true);
                    btnUpgradeReg4.setDisable(true);
                    btnUpgradeReg5.setDisable(true);
                }
            }

            });
    }
    /**
     * a listener when a player buys a upgrade card.
     * @author Jihyun
     */
    private void addListenerForNewlyBoughtUpgrade(){
        newlyBoughtUpgradeListener = change -> {
            //System.out.println("newlyboughtupgrade listened");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if(change.wasAdded() && change.getKey() == clientHandler.getClientID()){
                        if (change.getValueAdded() != null){
                            try {
                                handleNewlyBoughtUpgrade(change.getValueAdded());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
//                            boughtUpgradeCardsList.add(change.getValueAdded().getCardName());
                        }
                    }
                }
            });
        };

        clientHandler.getClientData().getNewlyBoughtCardWithPlayer().addListener(newlyBoughtUpgradeListener);
    }

    @FXML
    private JFXDialog dialogUpgradeDescription;
    @FXML
    private Label upgradeDescription;

    @FXML
    private Label upgradeName;
    @FXML
    private Label upgradeCardTyp;

    private ArrayList<JFXButton> installedPermanentUpgradeList;

    /**
     * shows on each playerdeck which upgrade card a player has bought and connects to execute methodes according to the type of upgrade card.
     * @author Jihyun
     * @param newlyBoughtUpgrade
     */
    private void handleNewlyBoughtUpgrade(Card newlyBoughtUpgrade) throws IOException {
        switch(newlyBoughtUpgrade.getCardName()) {
            case "AdminPrivilege":
                if(installedPermanentUpgradeList.size() == 0){
                    btnPermanent1.setDisable(false);
                    btnPermanent1.setText("AdminPrivilege");
                    installedPermanentUpgradeList.add(btnPermanent1);
                    btnUpgradeReg1.setDisable(false);
                    btnUpgradeReg2.setDisable(false);
                    btnUpgradeReg3.setDisable(false);
                    btnUpgradeReg4.setDisable(false);
                    btnUpgradeReg5.setDisable(false);
                    upgradeRegBtnClicked();
                }else if(installedPermanentUpgradeList.size() ==1 ){
                    btnPermanent2.setDisable(false);
                    btnPermanent2.setText("AdminPrivilege");
                    installedPermanentUpgradeList.add(btnPermanent2);
                    btnUpgradeReg1.setDisable(false);
                    btnUpgradeReg2.setDisable(false);
                    btnUpgradeReg3.setDisable(false);
                    btnUpgradeReg4.setDisable(false);
                    btnUpgradeReg5.setDisable(false);
                }else{
                    break;
                }
                break;
            case "MemorySwap": //temporary
                btnMemorySwap.setDisable(false);
                btnMemorySwap.setOnAction(ActionEvent->memorySwapBtnClicked(btnMemorySwap));
                break;
            case "RearLaser":
                if(installedPermanentUpgradeList.size() == 0){
                    btnPermanent1.setText("RearLaser");
                    btnPermanent1.setDisable(false);
                    installedPermanentUpgradeList.add(btnPermanent1);
                }else if(installedPermanentUpgradeList.size() ==1 ){
                    btnPermanent2.setText("RearLaser");
                    btnPermanent2.setDisable(false);
                    installedPermanentUpgradeList.add(btnPermanent2);
                }
                else{
                    break;
                }

                break;
            case "SpamBlocker": //temporary
                btnSpamBlocker.setDisable(false);
                btnSpamBlocker.setOnAction(ActionEvent-> {
                    try {
                        spamBlockerBtnClicked(btnSpamBlocker);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                break;
        }
    }

    public void setSpamBlocker(String spamBlocker) {
        this.spamBlockerCard = spamBlocker;
    }

    private String spamBlockerCard;
    private void spamBlockerBtnClicked(JFXButton upgradeCard) throws IOException {
        btnSpamBlocker.setDisable(true);
        setSpamBlocker(upgradeBtnSpamBlocker.getText());
        PlayCard spamBlocker = new PlayCard("SpamBlocker");
        try {
            clientHandler.sendMessageSerialized(spamBlocker);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        clientHandler.getClientData().removeFromBoughtCard(spamBlockerCard);
    }
    private void memorySwapBtnClicked(JFXButton upgradeCard) {
        upgradeCard.setOnAction(ActionEvent-> {
            btnMemorySwap.setDisable(true);
            PlayCard memorySwapCard = new PlayCard("MemorySwap");
            try {
                clientHandler.sendMessageSerialized(memorySwapCard);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

//    /**
//     * handles temporary cards, when a player clicks one of the temporary card buttons(memorysway/spamblocker).
//     * @author Jihyun
//     * @param upgradeCardName
//     */
//    private void handleUpgrade(String upgradeCardName) throws IOException {
//        switch(upgradeCardName) {
//            case "MemorySwap":
//                handleMemorySwap();
//                btnMemorySwap.setDisable(true);
//                break;
//            case "SpamBlocker":
//                handleSpamBlocker();
//                btnSpamBlocker.setDisable(true);
//                break;
//            default:
//                break;
//        }
//    }
    private void handleMemorySwap() throws IOException {

//        System.out.println("memoryswap has been used.");
        PlayCard memorySwapCard = new PlayCard("MemorySwap");
        clientHandler.sendMessageSerialized(memorySwapCard);
//        boughtUpgradeCardsList.remove("MemorySwap");
    }
    private void handleSpamBlocker() throws IOException {
//        System.out.println("spamblocker has been used.");

//        boughtUpgradeCardsList.remove("SpamBlocker");
//        for(Card card: clientHandler.getClientData().getBoughtUpgradeCards()){
//            Platform.runLater(new Runnable() {
//                @Override
//                public void run() {
//                    if(card.getCardName().equals("SpamBlocker")) {
//                        clientHandler.getClientData().getBoughtUpgradeCards().remove(card);
//                    }
//                }
//            });
//        }

    }

    @FXML
    private JFXButton upgradeBtnAdminPrivilege;

    @FXML
    private JFXButton upgradeBtnMemorySwap;

    @FXML
    private JFXButton upgradeBtnRearLaser;

    @FXML
    private JFXButton upgradeBtnSpamBlocker;
    private void showUpgradesWithDescriptions(String upgradeCardName) {
            switch(upgradeCardName) {
                case "AdminPrivilege":
                    upgradeName.setText("AdminPrivilege");
                    upgradeDescription.setText("Once per round, you may give your robot priority for one register.");
                    upgradeCardTyp.setText("permanent");
                    dialogUpgradeDescription.show();
                    break;
                case "MemorySwap":
                    upgradeName.setText("MemorySwap");
                    upgradeDescription.setText("Draw three cards. Then choose three from your hand to put on top of your deck");
                    upgradeCardTyp.setText("temporary");
                    dialogUpgradeDescription.show();
                    break;
                case "RearLaser":
                    upgradeName.setText("RearLaser");
                    upgradeDescription.setText("Your robot may shoot backward as well as forward.");
                    upgradeCardTyp.setText("permanent");
                    dialogUpgradeDescription.show();
                    break;
                case "SpamBlocker":
                    upgradeName.setText("SpamBlocker");
                    upgradeDescription.setText("Replace each SPAM damage card in your hand with a card from the top of your deck.");
                    upgradeCardTyp.setText("temporary");
                    dialogUpgradeDescription.show();
                    break;
                default:
                    break;
            }
    }

    /**
     * @author
     * adding a Border around the current register and sending the playCard message
     * @throws IOException
     */
    private void addBorderToRegister() throws IOException {
        registerBorder = new ImageView();
        registerBorder.setImage(new Image(getClass().getClassLoader().getResource("images/cards/border3.png").openStream()));

        currentRegisterListener = ((observableValue, oldRegister, newRegister) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    registerDeck.getChildren().remove(registerBorder);
                    if(newRegister.intValue() != 0) {
                        registerDeck.add(registerBorder, newRegister.intValue(), 0);
                    }
                }
            });
            if(newRegister.intValue() != 0) {
                String currentCard;
                switch (newRegister.intValue()) {
                    case 1 -> currentCard = clientHandler.getClientData().getYourPlayerData().getRegister0().getCardName();
                    case 2 -> currentCard = clientHandler.getClientData().getYourPlayerData().getRegister1().getCardName();
                    case 3 -> currentCard = clientHandler.getClientData().getYourPlayerData().getRegister2().getCardName();
                    case 4 -> currentCard = clientHandler.getClientData().getYourPlayerData().getRegister3().getCardName();
                    default -> currentCard = clientHandler.getClientData().getYourPlayerData().getRegister4().getCardName();
                }
                PlayCard playCard = new PlayCard(currentCard);
                try {
                    clientHandler.sendMessageSerialized(playCard);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        clientHandler.getClientData().currentRegisterProperty().addListener(currentRegisterListener);
    }

    private void iniCardsList() {
        ArrayList<Button> cardsList = new ArrayList<Button>();
        cardsList.add(Card1);
        cardsList.add(Card2);
        cardsList.add(Card3);
        cardsList.add(Card4);
        cardsList.add(Card5);
        cardsList.add(Card6);
        cardsList.add(Card7);
        cardsList.add(Card8);
        cardsList.add(Card9);

        for(Node card: cardsList) {
            for(ImageView register: listOfRegisters) {
                setOnDragAndDrop((Button) card, register);
            }
        }
    }



    /**
     * a small guideline with some explanation for players for each phase the player is in (set up, upgrade, programming, activation phases)
     * @author Jihyun
     */
    private void addListenerForcurrentPhaseForHelp() {
        currentPhaseForHelpListener = ((observableValue, oldValue, newValue) -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    switch(clientHandler.getClientData().getCurrentPhase()) {

                        case 0:
                            for(JFXButton btn: adminRegButtons) {
                                if(!btn.getText().equals(" ")) {
                                    btn.setText(" ");
                                }
                            }
                            btnSpamBlocker.setDisable(true);
                            btnUpgradeReg1.setDisable(true);
                            btnUpgradeReg2.setDisable(true);
                            btnUpgradeReg3.setDisable(true);
                            btnUpgradeReg4.setDisable(true);
                            btnUpgradeReg5.setDisable(true);

                            mouseOnHoverWindowForHelp.changeContentWithPhaseForHelp("Setup Phase");
                            break;
                        case 1:
                            for(JFXButton btn: adminRegButtons) {
                                if(!btn.getText().equals(" ")) {
                                    btn.setText(" ");
                                }
                            }
                            btnSpamBlocker.setDisable(true);
                            btnUpgradeReg1.setDisable(true);
                            btnUpgradeReg2.setDisable(true);
                            btnUpgradeReg3.setDisable(true);
                            btnUpgradeReg4.setDisable(true);
                            btnUpgradeReg5.setDisable(true);
                            mouseOnHoverWindowForHelp.changeContentWithPhaseForHelp("Upgrade Phase");
                            break;
                        case 2:
                            for(Card card: clientHandler.getClientData().getBoughtUpgradeCards()){
                                if(card != null && card.getCardName().equals("AdminPrivilege")){
                                    btnUpgradeReg1.setDisable(false);
                                    btnUpgradeReg2.setDisable(false);
                                    btnUpgradeReg3.setDisable(false);
                                    btnUpgradeReg4.setDisable(false);
                                    btnUpgradeReg5.setDisable(false);
                                }
                                else if(card != null && card.getCardName().equals("SpamBlocker")) {
                                    btnSpamBlocker.setDisable(false);
                                }
                            }
                            mouseOnHoverWindowForHelp.changeContentWithPhaseForHelp("Programming Phase");
                            break;
                        case 3:
                            btnSpamBlocker.setDisable(true);
                            for(Card card: clientHandler.getClientData().getBoughtUpgradeCards()) {
                                if (card != null && card.getCardName().equals("AdminPrivilege")) {
                                    btnUpgradeReg1.setDisable(false);
                                    btnUpgradeReg2.setDisable(false);
                                    btnUpgradeReg3.setDisable(false);
                                    btnUpgradeReg4.setDisable(false);
                                    btnUpgradeReg5.setDisable(false);
                                }
                            }
                            mouseOnHoverWindowForHelp.changeContentWithPhaseForHelp("Activation Phase");
                            break;
                        default:
                            btnSpamBlocker.setDisable(true);
                            btnMemorySwap.setDisable(true);
                            break;

                    }
                }
            });
        });
        clientHandler.getClientData().currentPhaseProperty().addListener(currentPhaseForHelpListener);
    }
    /**
     * adds the check point listener
     * @author Tingyue
     */
    private void addListenerForCheckpoint(){
        checkpointListener = (observableValue, oldValue, newValue) ->{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    numCheckpoints.setText(String.valueOf(clientHandler.getClientData().getYourPlayerData().getReachedCheckpoints()));
                }
            });

        };
        clientHandler.getClientData().getYourPlayerData().reachedCheckPointsProperty().addListener(checkpointListener);
    }
    /**
     * adds the energy cube listener
     * @author Tingyue
     */
    private void addListenerForEnergyCube(){
        energyListener = (observableValue, oldValue, newValue) ->{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    numEnergycubes.setText(String.valueOf(clientHandler.getClientData().getYourPlayerData().getEnergycount()));
                }
            });

        };
        clientHandler.getClientData().getYourPlayerData().energycountProperty().addListener(energyListener);
    }


    public void updateRegisterFilled(){
        registerFilledListener = change -> {
            while(change.next()) {
                if(change.wasUpdated()) {
                    int start = change.getFrom();
                    int end = change.getTo();
                    for(int i=start; i < end; i++) {
                        handleRegistersFilled(i);
                    }
                }
            }
        };
    }

    public void updateDamageCards() {
        drawnDamageListener = (observableValue, oldValue, newValue) -> handleDrawnDamageCards(newValue);
    }

    /**
     * handles programming phase when player drags and drops cards onto their registers
     * @author Jihyun
     * @param register
     */
    private void handleRegistersFilled(int register) {
        ArrayList<Boolean> registerList = clientHandler.getClientData().getYourPlayerData().getRegistersFilled();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!isCardRemoved && !registerList.get(register) && !isCardDropped) { //for empty register (register is set to default(false))
                    getRegisterFromNumber(register).setImage(null);
                } else if(isCardRemoved && !registerList.get(register) && !isCardDropped) { //card is removed from a register
                    removeCardImgFromRegister(register);
                    setCardRemoved(false);
                } else if(!isCardRemoved && registerList.get(register) && isCardDropped) { //card is added to register
                    setCardImgsOnRegisters();
                    setCardDropped(false);
                }
            }
        });

    }

    /**
     * sets card images on registers
     * @author Jihyun
     */
    public void setCardImgsOnRegisters() {
        for(int register: mapOfRegistersWithCards.keySet()) {
            Image programmingCard = null;
            Button cardName = mapOfRegistersWithCards.get(register);
            try {
                programmingCard = new Image(getClass().getClassLoader().getResource("images/cards/" + getImageName(cardName.getText()) + ".png").openStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            getRegisterFromNumber(register).setImage(programmingCard);
            cardName.setOpacity(0);
            cardName.setDisable(true);
        }

    }

    /**
     * removes a card image from register when a player clicks the register
     * @author Jihyun
     * @param register
     */
    public void removeCardImgFromRegister(int register) {
        for(int cardImg: mapOfRegistersWithCards.keySet()) {
            if(getRegisterFromNumber(register).getId().equals(getRegisterFromNumber(cardImg).getId())) {
                mapOfRegistersWithCards.get(cardImg).setOpacity(1);
                mapOfRegistersWithCards.get(cardImg).setDisable(false);
                getRegisterFromNumber(register).setImage(null);
                mapOfRegistersWithCards.remove(register);
            }else {continue;}
            break;
        }
    }
        public ImageView getRegisterFromNumber(int i) {
            switch(i) {
                case 0:
                    return Register0;
                case 1:
                    return Register1;
                case 2:
                    return Register2;
                case 3:
                    return Register3;
            }
            return Register4;
        }

    /**
     * handles drawn damage cards. The number of the drawm damage card will be changed on their playerdeck.
     * @author Jihyun
     * @param newValue
     */
    public void handleDrawnDamageCards(ArrayList<Card> newValue) {
            ArrayList<Card> drawnDamageCardsList = new ArrayList<Card>(newValue);
            //System.out.println("a damage card has been drawn.");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    for(Card damageCard : drawnDamageCardsList) {
                        switch(damageCard.getCardName()) {
                            case "Spam":
                                Label numForSpam = mouseOnHoverWindowForDamage.getNumberOfCards("Spam");
                                numForSpam.setText(String.valueOf((Integer.parseInt(numForSpam.getText())+1)));
                                break;
                            case "Trojan":
                                Label numForTrojan = mouseOnHoverWindowForDamage.getNumberOfCards("Trojan");
                                numForTrojan.setText(String.valueOf((Integer.parseInt(numForTrojan.getText())+1)));
                                break;
                            case "Virus":
                                Label numForVirus = mouseOnHoverWindowForDamage.getNumberOfCards("Virus");
                                numForVirus.setText(String.valueOf((Integer.parseInt(numForVirus.getText())+1)));
                                break;
                            case "Worm":
                                Label numForWorm = mouseOnHoverWindowForDamage.getNumberOfCards("Worm");
                                numForWorm.setText(String.valueOf((Integer.parseInt(numForWorm.getText())+1)));
                                break;
                            default:
                                break;
                        }
                    }
                }
            });

        }
    /**
     * @author Stephan
     * adds the cardListener
     */
    private void updateCards() {
        cardListener = (observableValue, oldValue, newValue) -> {
            if(newValue.size() < 9) {
                //if(cardTemp1.isEmpty()) {
                //    for (int i = 0; i < newValue.size(); i++) {
                //        cardTemp1.add(newValue.get(i));
                //    }
                //} else {
                //    for (int i = 0; i < newValue.size(); i++) {
                //        cardTemp2.add(newValue.get(i));
                //    }
                //    for(int j = 0; j < cardTemp2.size(); j++) {
                //        cardTemp1.add(cardTemp2.get(j));
                //    }
                //    //cardTemp1.addAll(cardTemp2);
                //    handleNewCards(cardTemp1);
                //}
            } else {
                //System.out.println("updated 9 cards!"+ newValue);
                handleNewCards(newValue);
                //if(!cardTemp1.isEmpty()) {
                //    cardTemp1.clear();
                //}
                //if(!cardTemp2.isEmpty()) {
                //    cardTemp2.clear();
                //}
            }
            clearRegisters();
            cardDeck1.setDisable(false);
            cardDeck2.setDisable(false);
            cardDeck3.setDisable(false);
            cardDeck1.setOpacity(1);
            cardDeck2.setOpacity(1);
            cardDeck3.setOpacity(1);
        };
    }

    /**
     * @author Stephan
     */
    private void clearRegisters() {
        removeCardImgFromRegister(0);
        removeCardImgFromRegister(1);
        removeCardImgFromRegister(2);
        removeCardImgFromRegister(3);
        removeCardImgFromRegister(4);
    }

    /**
     * @author Stephan
     * sets the new text of the buttons according to the new values
     */
    private void handleNewCards(ArrayList<Card> cardList) {
        //ArrayList<Card> cardList = clientHandler.getClientData().getCardsInHand();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Card1.setText(cardList.get(0).getCardName());
                Card1.setOpacity(1);
                Card2.setText(cardList.get(1).getCardName());
                Card2.setOpacity(1);
                Card3.setText(cardList.get(2).getCardName());
                Card3.setOpacity(1);
                Card4.setText(cardList.get(3).getCardName());
                Card4.setOpacity(1);
                Card5.setText(cardList.get(4).getCardName());
                Card5.setOpacity(1);
                Card6.setText(cardList.get(5).getCardName());
                Card6.setOpacity(1);
                Card7.setText(cardList.get(6).getCardName());
                Card7.setOpacity(1);
                Card8.setText(cardList.get(7).getCardName());
                Card8.setOpacity(1);
                Card9.setText(cardList.get(8).getCardName());
                Card9.setOpacity(1);
            }
        });
    }


    private Button currentButton = new Button();
    private ImageView currentRegister = new ImageView();

    public void saveCurrentCardButton(Button btn){
        currentButton = btn;
    }

    public void saveCurrentRegister(ImageView image) {
        currentRegister = image;
    }

    HashMap<Integer, Button> mapOfRegistersWithCards = new HashMap<Integer, Button>();

    /**
     * handles the drag and drop events during programming phase
     * @author Jihyun
     * @param btn
     * @param image
     */
    public void setOnDragAndDrop(Button btn,ImageView image) {
        btn.setOnDragDetected((MouseEvent event) -> {
            saveCurrentCardButton(btn);
            //System.out.println("Card drag detected");
            Dragboard db = btn.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(btn.getText());
            db.setContent(content);
        });

        btn.setOnMouseDragged((MouseEvent event) -> {
            event.setDragDetect(true);
        });

        saveCurrentRegister(image);
        image.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                if (event.getGestureSource() != image && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            }
        });

        image.setOnDragDropped((DragEvent event) -> {
            if(image.getImage() == null) {
                msg.setText("Click the register to cancel the card.");
                saveCurrentRegister(image);
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    setCardDropped(true);
                    //System.out.println("Dropped: " + db.getString() + "in" + image.getId());
                    SelectedCard selectedCard = new SelectedCard(db.getString(),getNumFromRegister(image));
                    try {
                        clientHandler.sendMessageSerialized(selectedCard);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    mapOfRegistersWithCards.put(getNumFromRegister(image), currentButton);
                    event.setDropCompleted(true);
                } else {
                    event.setDropCompleted(false);
                }
                event.consume();
            }else {
                mapOfRegistersWithCards.get(getNumFromRegister(image)).setOpacity(100);// old card which is replaced by another card
                mapOfRegistersWithCards.get(getNumFromRegister(image)).setDisable(false);
                currentButton.setOpacity(0); //replaced card button
                currentButton.setDisable(true);
                mapOfRegistersWithCards.replace(getNumFromRegister(image), mapOfRegistersWithCards.get(getNumFromRegister(image)),currentButton); //save the replaced card on the register the replaced card is dropped on.
                Image programmingCard = null;
                Button cardName = mapOfRegistersWithCards.get(getNumFromRegister(image));
                try {
                    programmingCard = new Image(getClass().getClassLoader().getResource("images/cards/" + getImageName(cardName.getText()) + ".png").openStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                image.setImage(programmingCard);
                SelectedCard selectedCard = new SelectedCard(currentButton.getText(),getNumFromRegister(image));
                try {
                    clientHandler.sendMessageSerialized(selectedCard);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                msg.setText("The card on register " + getNumFromRegister(image) + " is replaced to "+ currentButton.getText());
//                msg.setText("The register is already occupied. Choose another Register or click the register to cancel the card.");
            }
        });

    }

    public Boolean getCardDropped() {
        return isCardDropped;
    }

    public void setCardDropped(Boolean cardDropped) {
        isCardDropped = cardDropped;
    }

    private Boolean isCardDropped =false;

    public int getNumFromRegister(ImageView img) {
        switch(img.getId()) {
            case "Register0":
                return 0;
            case "Register1":
                return 1;
            case "Register2":
                return 2;
            case "Register3":
                return 3;

        }
        return 4;
    }

    public Boolean getCardRemoved() {
        return isCardRemoved;
    }

    public void setCardRemoved(Boolean cardRemoved) {
        isCardRemoved = cardRemoved;
    }

    private Boolean isCardRemoved = false;
    public void removeCardFromRegister(ImageView register) {
            register.setOnMouseClicked(event -> {
                setCardRemoved(true);
                SelectedCard selectedCard = new SelectedCard("null",getNumFromRegister(register));
                try {
                    clientHandler.sendMessageSerialized(selectedCard);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }
    public String getImageName(String cardName) {
        return switch (cardName) {
            case "MoveI" -> "move1";
            case "MoveII" -> "move2";
            case "MoveIII" -> "move3";
            case "Again" -> "again";
            case "BackUp" -> "moveback";
            case "PowerUp" -> "powerup";
            case "TurnRight" -> "rightturn";
            case "TurnLeft" -> "leftturn";
            case "UTurn" -> "uturn";
            case "Spam" -> "spam";
            case "Virus" -> "virus";
            case "Worm" -> "worm";
            default -> "trojan";
        };
    }
    Stage hoverStageForDamageCards;
    Stage hoverStageForUpgradeCards;

    Stage hoverStageForHelp;


    /**
     * handles the popups when the mouse hovers over them (for help and damage cards)
     * @author Jihyun
     * @param btn
     */
    @FXML
    private void handleButtonOnHover(Button btn) {
        btn.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                switch (btn.getId()) {
                    case "damageCards":
                        //                            hoverStageForDamageCards = openHoverWindow(damageCards);
                        hoverStageForDamageCards.show();
                        setWindowPosition(hoverStageForDamageCards,damageCards);
                        break;
//                    case "upgradeCards":
//                        //                            hoverStageForUpgradeCards = openHoverWindow(upgradeCards);
//                        hoverStageForUpgradeCards.show();
//                        setWindowPosition(hoverStageForUpgradeCards,upgradeCards);
//                        break;
                    case "help":
                        //                            hoverStageForHelp = openHoverWindow(help);
                        hoverStageForHelp.show();
                        setWindowPosition(hoverStageForHelp, help);
                        break;
                }
                ScaleTransition scalet = new ScaleTransition(Duration.millis(150), btn);
                scalet.setFromX(1);
                scalet.setFromY(1);
                scalet.setToX(1.2);
                scalet.setToY(1.2);
                scalet.play();
            }
        });
        btn.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hoverStageForDamageCards.hide();
//                hoverStageForUpgradeCards.hide();
                hoverStageForHelp.hide();
                ScaleTransition scalet = new ScaleTransition(Duration.millis(150), btn);
                scalet.setFromX(1.2);
                scalet.setFromY(1.2);
                scalet.setToX(1);
                scalet.setToY(1);
                scalet.play();
            }
        });
    }


    public void setWindowPosition(Stage stage, Button btn) {
        Bounds boundsInScreen = btn.localToScreen(btn.getBoundsInLocal());
        double x = boundsInScreen.getMaxX() -stage.getWidth() + 150;
        double y = boundsInScreen.getMinY() - stage.getHeight();
        stage.setX(x);
        stage.setY(y);
    }

    MouseOnHoverWindowController mouseOnHoverWindowForDamage;
    MouseOnHoverWindowController mouseOnHoverWindowForUpgrade;
    MouseOnHoverWindowController mouseOnHoverWindowForHelp;
    public Stage openHoverWindowForDamageCards(Button btn) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("MouseOnHoverWindow.fxml"));
        Parent root = (Parent) loader.load();
        MouseOnHoverWindowController mouseOnHoverWindowControllerforDamageCards = loader.getController();
        mouseOnHoverWindowForDamage = mouseOnHoverWindowControllerforDamageCards;
        mouseOnHoverWindowControllerforDamageCards.setButtonHovered(btn);
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getScene().setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        return stage;
    }


    public Stage openHoverWindowForHelp(Button btn) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("MouseOnHoverWindow.fxml"));
        Parent root = (Parent) loader.load();
        MouseOnHoverWindowController mouseOnHoverWindowController = loader.getController();
        mouseOnHoverWindowForHelp = mouseOnHoverWindowController;
        mouseOnHoverWindowController.setButtonHovered(btn);
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getScene().setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        return stage;
    }


    @FXML
    void changeSize(Stage stage) {
        stage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double f = newValue.doubleValue()/1920;
                registerDeck.setPrefWidth(510*f);
                for(int register: mapOfRegistersWithCards.keySet()) {
                    getRegisterFromNumber(register).setFitWidth(94*f);
                    ///registerBorder.setFitWidth(94*f);
                }
                Card1.setPrefWidth(175*f);
                /*
                Card2.setPrefWidth(175*f);
                Card3.setPrefWidth(175*f);
                Card4.setPrefWidth(175*f);
                Card5.setPrefWidth(175*f);
                Card6.setPrefWidth(175*f);
                Card7.setPrefWidth(175*f);
                Card8.setPrefWidth(175*f);
                Card9.setPrefWidth(175*f);
                 */
            }
        });

        stage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double f = newValue.doubleValue()/1080;
                registerDeck.setPrefHeight(139*f);
                for(int register: mapOfRegistersWithCards.keySet()) {
                    getRegisterFromNumber(register).setFitHeight(128*f);
                }
                Card1.setPrefHeight(22*f);

                /*
                Card2.setPrefHeight(22*f);
                Card3.setPrefHeight(22*f);
                Card4.setPrefHeight(22*f);
                Card5.setPrefHeight(22*f);
                Card6.setPrefHeight(22*f);
                Card7.setPrefHeight(22*f);
                Card8.setPrefHeight(22*f);
                Card9.setPrefHeight(22*f);
                 */
            }
        });
    }

    //this method is just for testing
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("PlayerDeck.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("playerDeck");
        primaryStage.setScene(scene);


        Rectangle2D bounds = Screen.getPrimary().getBounds();
        double maxX = bounds.getMaxX();
        double maxY = bounds.getMaxY();
        double font = maxY / 90;
        if(maxY < 900) {
            root.styleProperty().set("-fx-font-size: 10px;");
        } else {
            root.styleProperty().set("-fx-font-size: " + font + "px;");
        }
            changeSize(primaryStage);//test for resizing
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

    public void setClientHandler(ClientHandler clientHandler) {
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


}
