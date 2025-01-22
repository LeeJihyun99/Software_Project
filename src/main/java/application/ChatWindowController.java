package application;

import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import server.*;
import javafx.scene.control.TextArea;
import server.protocol.chatnachrichten.ReceivedChat;
import tools.ClientLogger;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * @author Stephan Hefele, Tingyue
 */

public class ChatWindowController{

    @FXML
    public Button sendBtn;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField input;
    private static ClientHandler clientHandler;
    private Logger logger = ClientLogger.getLogger();
    public ChatWindowController() {
        super();
    }

    /**
     * Sobald ChatWindow öffnet, beginnt der Client, Nachrichten zu empfangen
     * @author Tingyue
     */
    @FXML
    protected void initialize(){
        //welcome
        chatArea.setStyle("-fx-text-fill: #01035e");

        if(clientHandler.getClientData().getChosenMap() == null){ //in waiting room
            chatArea.appendText("-Welcome " + clientHandler.getName() +" (ID: "+ clientHandler.getClientID()
                    + ") !" + " Here you can chat with other people. "+ "\n" );
            chatArea.appendText("-If you want to chat with someone alone, You can use the following form: @ID xxxxx" + "\n");
            chatArea.appendText("------------------------------------------------------------------------"+ "\n");

        }else {   //in main window
            chatArea.appendText( "-Hi, " + clientHandler.getName() + ", the game has started!" + " Here you can also chat with other people. "+ "\n" );
            chatArea.appendText("-If you want to chat with someone alone, You can use the following form: @ID xxxxx" + "\n");
            chatArea.appendText("-----------------------------------------------------------"+ "\n");
        }

        for (ReceivedChat message : clientHandler.getClientData().getChatmessages()){
            if (!message.getMessage().equals("/first player ready to play")){
                chatArea.appendText(getFormatedChatMessage(message) + "\n");
            }
        }

        clientHandler.getClientData().receivedChatObjectProperty().addListener((c, oldvalue, newvalue) -> {
            try {
                writeInput(newvalue);
                logger.fine("New Message has been added to the Chatwindow: " + newvalue.getMessage());
            } catch (IOException e) {
                logger.severe("Input in the ChatWindow could not be written.");
                throw new RuntimeException(e);
            }
        });

        clientHandler.getClientData().latestGameMessageProperty().addListener((c, oldValue, newValue) -> {
            writeInput(newValue);

        });

        addHoverEffect(1, 1.1, 150, sendBtn);

    }

    /**
     * Write Input for String-Messages (mainly for Game-Messages, which are not otherwise visually displayed)
     */
    private void writeInput(String newValue) {
        String fullText = "";

        fullText = newValue;

        chatArea.setWrapText(true);
        chatArea.appendText(fullText);
    }


    @FXML
    void sendButtonClicked(ActionEvent event) throws IOException {
        String message = input.getText();
        if (!message.equals("")) {
            input.clear();
            try {
                clientHandler.handleMessagesFromUser(message);
            } catch (Exception e) {
                logger.warning("Message from User could not be sent");
                e.printStackTrace();
            }

        }
    }

    public String getFormatedChatMessage(ReceivedChat message){
        return message.getMessage();
    }


    /**
     * Nimmt eine Nachricht entgegen, entschlüsselt ihren Typ und gebt entsprechend davon die Nachricht in Chatfenster aus
     * @param receivedMessage Zu behandelnde Nachricht
     * @author Tingyue
     */
    public void writeInput(ReceivedChat receivedMessage) throws IOException {
        String fullText = "";

        fullText = getFormatedChatMessage(receivedMessage) + "\n";

        if (receivedMessage.getMessage().equals("/first player ready to play")) {
            fullText = "";
        }

        chatArea.setWrapText(true);
        chatArea.appendText(fullText);
    }

    public static void setClientHandler(ClientHandler c) {
        clientHandler = c;
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


