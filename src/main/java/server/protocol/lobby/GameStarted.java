package server.protocol.lobby;

import field.reducedFields.ReducedField;
import server.protocol.Message;

import java.util.ArrayList;

public class GameStarted extends Message {
    private String messageType;
    private MessageBody messageBody;

    public GameStarted(ArrayList<ArrayList<ArrayList<ReducedField>>> gameBoard){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(gameBoard);
    }

    private class MessageBody{
        ArrayList<ArrayList<ArrayList<ReducedField>>> gameMap;
        public MessageBody(ArrayList<ArrayList<ArrayList<ReducedField>>> gameBoard){
            this.gameMap = gameBoard;
        }

        public ArrayList<ArrayList<ArrayList<ReducedField>>> getGameMap() {
            return gameMap;
        }
    }

    public ArrayList<ArrayList<ArrayList<ReducedField>>> getGameMap() {
        return messageBody.getGameMap();
    }
}
