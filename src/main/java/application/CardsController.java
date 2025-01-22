package application;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import server.ClientHandler;
import tools.ClientLogger;

import java.util.logging.Logger;

public class CardsController extends Application {

    //TODO: ClientHandler initialisieren

    @FXML
    private Button specialProgrammingCardBtn;
    private ClientHandler clientHandler;
    private Logger logger = ClientLogger.getLogger();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
//        clientHandler.getClientData().getPlayers().addListener((ListChangeListener<PlayerData>) c -> {
//            updateCards();
                logger.fine("CardsController updates cards");
//        });

    }

    private void updateCards(){
        //TODO: implement Method
    }
}
