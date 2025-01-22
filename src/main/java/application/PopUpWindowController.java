package application;

import card.Card;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import server.ClientHandler;
import server.protocol.aktionen.SelectedDamage;
import server.protocol.lobby.SetStatus;
import tools.ClientLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Controller for popupwindows
 * @author Jihyun
 */

public class PopUpWindowController extends Application {

    private Button PopUpClickedBtn;

    private ToggleButton PopUpClickedToggleBtn;

    @FXML
    private Label content;

    @FXML
    private Label popUpHeader;

    @FXML
    private Button yesBtn;

    @FXML
    private Button noBtn;

    @FXML
    private Button damageCardBtn;

    private ClientHandler clientHandler;
    private Logger logger = ClientLogger.getLogger();

    /**
     * initializes the popupwindow
     * @throws IOException
     */
    public void initialize() throws IOException {
        //init connection to the clientHandler
        setClientHandler(StartController.getClientHandler());
    }

    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void setPopUpClickedBtn(Button popUpClickedBtn) {
        PopUpClickedBtn = popUpClickedBtn;
        changeContent();
    }

    public void setPopUpClickedBtnFigure(Button popUpClickedBtn) { //only when the player clicks the select button without choosing any robot.
        PopUpClickedBtn = popUpClickedBtn;
        changeContentFigure();
    }

    public void setPopUpClickedToggleBtn(ToggleButton toggleBtn) {
        PopUpClickedToggleBtn = toggleBtn;
        changeContentToggleBtn(toggleBtn);
    }

    public void setPopUpClickedStartBtn(Button btn, int sizeofReadyPlayers) {
        PopUpClickedBtn = btn;
        changeContentStartBtn(sizeofReadyPlayers);
    }

    private ArrayList<Button> ButtonsList = new ArrayList<Button>();

    private ArrayList<String> chosenDamageCards = new ArrayList<String>();

    private int firstDamageCardCount = 0;
    private int secondDamageCardCount = 0;
    private int thirdDamageCardCount = 0;
    public void changeContentForDamageCard(ArrayList<Card> availableDamageCards, int numOfDamageCardsToChoose) {
        ButtonsList.add(yesBtn);
        ButtonsList.add(damageCardBtn);
        ButtonsList.add(noBtn);

        if(availableDamageCards.size() == 3) { //when there are trojan, worm, virus decks
            yesBtn.setOpacity(100);
            damageCardBtn.setOpacity(100);
            noBtn.setOpacity(100);
            yesBtn.setText(availableDamageCards.get(0).getCardName());
            damageCardBtn.setText(availableDamageCards.get(1).getCardName());
            noBtn.setText(availableDamageCards.get(2).getCardName());

            popUpHeader.setText("Choose "+ numOfDamageCardsToChoose + " damage cards from: \n [ " + availableDamageCards.get(0).getCardName() + ", " + availableDamageCards.get(1).getCardName() + ", " + availableDamageCards.get(2).getCardName() + "]" );
            content.setText(availableDamageCards.get(0).getCardName() + ": " + firstDamageCardCount + "  " + availableDamageCards.get(1).getCardName() + ": " + secondDamageCardCount + "  "+ availableDamageCards.get(2).getCardName() + ": " + thirdDamageCardCount);

            yesBtn.setOnAction(event -> {
                try {
                    damageCardBtnClicked(yesBtn, numOfDamageCardsToChoose);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            damageCardBtn.setOnAction(event -> {
                try {
                    damageCardBtnClicked(damageCardBtn, numOfDamageCardsToChoose);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            noBtn.setOnAction(event -> {
                try {
                    damageCardBtnClicked(noBtn, numOfDamageCardsToChoose);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }else if (availableDamageCards.size() == 2) { //when there are only two of the three card decks(trojan, worm, virus)
            yesBtn.setOpacity(100);
            damageCardBtn.setOpacity(100);
            noBtn.setOpacity(0);
            noBtn.setDisable(true);
            yesBtn.setText(availableDamageCards.get(0).getCardName());
            damageCardBtn.setText(availableDamageCards.get(1).getCardName());

            popUpHeader.setText("Choose "+ numOfDamageCardsToChoose + " damage cards from: \n [ " + availableDamageCards.get(0).getCardName() + ", " + availableDamageCards.get(1).getCardName() + "]" );
            content.setText(availableDamageCards.get(0).getCardName() + ": " + firstDamageCardCount + "  " + availableDamageCards.get(1).getCardName() + ": " + secondDamageCardCount);

            yesBtn.setOnAction(event -> {
                try {
                    damageCardBtnClicked(yesBtn, numOfDamageCardsToChoose);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            damageCardBtn.setOnAction(event -> {
                try {
                    damageCardBtnClicked(noBtn, numOfDamageCardsToChoose);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }else if (availableDamageCards.size() ==1) { // when there is only one of the three card decks(trojan, worm, virus)
            setButtonsOpacityZero();
            setButtonsDisable();
            popUpHeader.setText("Only " + availableDamageCards.get(0).getCardName() + "left");
            content.setText("You get " + numOfDamageCardsToChoose + " " + availableDamageCards.get(0).getCardName() + " damage cards");

        }else { // when there is no more damage card decks
            setButtonsOpacityZero();
            setButtonsDisable();
            popUpHeader.setText("No more damage cards");
            content.setText("There is no more damage cards to choose. ");
        }
        
    }

    private void damageCardBtnClicked(Button btn, int numOfDamageCardToChoose) throws IOException {
        if(chosenDamageCards.size() < numOfDamageCardToChoose) {
            chosenDamageCards.add(btn.getText());
            if(btn.getId().equals("yesBtn")) {
                firstDamageCardCount++;
            }else if (btn.getId().equals("damageCardBtn")) {
                secondDamageCardCount++;
            }else {
                thirdDamageCardCount++;
            }

            if(chosenDamageCards.size() == numOfDamageCardToChoose) { //when the last damage card has been chosen, the selectedDamage msg will be sent to server.
                SelectedDamage selectedDamage = new SelectedDamage(chosenDamageCards);
                clientHandler.sendMessageSerialized(selectedDamage);
                setButtonsDisable();
                content.setText("Your chosen damage cards: " + chosenDamageCards + "\n Please close this window.");
            }
        }
    }
    /**
     *  for all error message
     */
    public void setPopUpWindowForError(String titel, String errorMsg) {
        setButtonsOpacityZero();
        popUpHeader.setText(titel);
        content.setText(errorMsg);
        content.setWrapText(true);
    }


    /**
     * 'not enough players to start a game' popupwindow
     * @param sizeOfReadyPlayers
     */
    public void changeContentStartBtn(int sizeOfReadyPlayers) {
        if(PopUpClickedBtn.getId().equals("gameStart")) {
            setButtonsOpacityZero();
            setButtonsDisable();

            popUpHeader.setText("Not enough players to start a game");
            content.setText("Only " + sizeOfReadyPlayers + " player is ready to play." +
                   "\nPlease wait for more players ready to start a game.");
        }
    }

    /**
     * 'Notification for the first player ' popupwindow: the first player decides whether or not he/she stays as the first player to choose a map
     * @param btn
     */
    public void changeContentToggleBtn(ToggleButton btn) {
        if(PopUpClickedToggleBtn.getId().equals("toggleBtn")) {
            noBtn.setOpacity(100);
            yesBtn.setOpacity(100);
            damageCardBtn.setOpacity(0);
            damageCardBtn.setDisable(true);
            popUpHeader.setText("You are the first player");
            content.setText("You cannot choose a map. Are you sure you want to continue?");
            yesBtn.setOnAction(event -> {
                SetStatus setStatus = new SetStatus(false);
                try {
                    clientHandler.sendMessageSerialized(setStatus);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                btn.setSelected(false);
                btn.setText("Ready");
                Node node = (Node) event.getSource();
                Stage thisStage = (Stage) node.getScene().getWindow();
                thisStage.close();
            });
            noBtn.setOnAction(event -> {
                Node node = (Node) event.getSource();
                Stage thisStage = (Stage) node.getScene().getWindow();
                thisStage.close();
            });
        }
    }

    /**
     * 'choose a robot' popupwindow: when a player clicks select Button without choosing any robot
     */
    public void changeContentFigure() {
        if (PopUpClickedBtn.getId().equals("selectRobot")) {
            setButtonsOpacityZero();
            setButtonsDisable();
            popUpHeader.setText("Choose a robot");
            content.setText("Please choose a robot before you click this button.");
        }
    }

    /**
     * changes contents of the popupwindow according to the text of the button
     */
    public void changeContent() {
        if (PopUpClickedBtn != null) {
            if (PopUpClickedBtn.getId().equals("mapSelectedBtn")) {
                setButtonsOpacityZero();
                setButtonsDisable();
                popUpHeader.setText("Choose a map");
                content.setText("Please choose a map from the choicebox before you click this button.");
            } else if (PopUpClickedBtn.getId().equals("next")) {
                setButtonsOpacityZero();
                setButtonsDisable();
                popUpHeader.setText("Finish your login");
                content.setText("Please check if you have written your username and chosen a robot.");
            } else if (PopUpClickedBtn.getId().equals("selectRobot")) {
                setButtonsOpacityZero();
                setButtonsDisable();
                popUpHeader.setText("Robot already taken");
                content.setText("This robot has already been chosen. Please choose another robot.");
            } else if (PopUpClickedBtn.getId().equals("AIbtn")){
                setButtonsOpacityZero();
                setButtonsDisable();
                popUpHeader.setText("All 6 robots are taken");
                content.setText("ALl robots are already taken and no more AI can be added.");
            }
        }
    }

    private void setButtonsDisable() {
        yesBtn.setDisable(true);
        damageCardBtn.setDisable(true);
        noBtn.setDisable(true);

    }
    private void setButtonsOpacityZero() {
        yesBtn.setOpacity(0);
        damageCardBtn.setOpacity(0);
        noBtn.setOpacity(0);
    }
    public static void main(String[] args) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("PopUpWindow.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Alert");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

}
