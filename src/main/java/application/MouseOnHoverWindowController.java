package application;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tools.ClientLogger;

import java.io.IOException;
import java.util.logging.Logger;


public class MouseOnHoverWindowController extends Application {

    @FXML
    private Label Nr1;

    @FXML
    private Label Nr2;

    @FXML
    private Label Nr3;

    @FXML
    private Label Nr4;


    @FXML
    private Pane damagePane;

    @FXML
    private Pane helpPane;

    @FXML
    private Pane upgradePane;
    @FXML
    private ImageView img1;

    @FXML
    private ImageView img2;

    @FXML
    private ImageView img3;

    @FXML
    private ImageView img4;

    @FXML
    private Label title;

    @FXML
    private AnchorPane parent;


    @FXML
    private Label explanation;
    private Button currentButton;
    private Logger logger = ClientLogger.getLogger();



    public void initialize() throws IOException {
        damagePane.setOpacity(0);
        upgradePane.setOpacity(0);
        helpPane.setOpacity(0);


    }
    public void setButtonHovered(Button button) throws IOException {
        currentButton = button;
        changeContent();
    }

    public void changeContentWithPhaseForHelp(String currentPhase) {
        switch(currentPhase) {
            case "Setup Phase":
                title.setText(currentPhase);
                explanation.setOpacity(100);
                explanation.setText("Choose one starting point to place your robot from the gameboard.");
                break;
            case "Programming Phase":
                title.setText(currentPhase);
                explanation.setOpacity(100);
                explanation.setText("Draw cards from your programming deck (9 programming cards), and arrange them on registers to play the moves you want your robot to make.\n" +
                        "* You can remove the card from a register by clicking the register. \n" +
                        "* Also you can replace the card with another card by dragging the card you want and drop it onto the register. \n" +
                        "* Don't take too long programming! The timer starts, when one of the players has placed the fifth card on a register. Then you only have 30 second to finish programming! \n");
                break;
            case "Activation Phase":
                title.setText(currentPhase);
                explanation.setOpacity(100);
                explanation.setText("The programming cards you placed in your registers during the programming phase activate. \n" +
                        "Programming cards activate one register at a time.\n" +
                        "For every register, after all players have activated their programming,\n" +
                        "board elements and robot lasers activate before the next register begins. \n" +
                        "(* Board elements activate and lasers fire.)\n" +
                        "After the fifth register is complete, then play returns to the upgrade phase.");

                break;
            default:
                title.setText(" ");
                explanation.setOpacity(0);
                break;
        }
    }

    public Label getNumberOfCards(String cardName) {
        switch(cardName){
            case "Spam":
                return Nr1;
            case "Trojan":
                return Nr2;
            case "Worm":
                return Nr3;
            case "Virus":
                return Nr4;
        }
        return Nr4;
    }

    public void changeContent() throws IOException {
        System.out.println(currentButton.getId());
        if(currentButton.getId().equals("damageCards")) {
            title.setText("Damage Cards");
            damagePane.setOpacity(100);
            damagePane.toFront();
            Image spam = new Image(getClass().getClassLoader().getResource("images/cards/spam.png").openStream());
            img1.setImage(spam);
            Image trodjan = new Image(getClass().getClassLoader().getResource("images/cards/trojan.png").openStream());
            img2.setImage(trodjan);
            Image worm = new Image(getClass().getClassLoader().getResource("images/cards/worm.png").openStream());
            img3.setImage(worm);
            Image virus = new Image(getClass().getClassLoader().getResource("images/cards/virus.png").openStream());
            img4.setImage(virus);
        }else if (currentButton.getId().equals("help")) {
            title.setText("Help");
            damagePane.setOpacity(0);
            helpPane.setOpacity(100);
            upgradePane.setOpacity(0);
            //TODO: remove all the default elements in the changingcontent pane and add an image with all the information of elements.
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MouseOnHoverWindow.fxml"));
        Scene scene = new Scene(root);
        stage.initStyle(StageStyle.UNDECORATED);
        scene.setFill(Color.TRANSPARENT);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
//        setCurrentHoverStage(stage);
        stage.show();
    }

}
