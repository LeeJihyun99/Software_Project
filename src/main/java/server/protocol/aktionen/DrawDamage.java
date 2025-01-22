package server.protocol.aktionen;

import server.protocol.DummyMessage;
import server.protocol.Message;

import java.util.ArrayList;

public class DrawDamage extends Message {
    private String messageType;
    private MessageBody messageBody;

    public DrawDamage(int clientID, ArrayList<String> cards){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID, cards);
    }

    private class MessageBody{
        private int clientID;
        private ArrayList<String> cards;

        public MessageBody(int clientID, ArrayList<String> cards) {
            this.clientID = clientID;
            this.cards = cards;
        }

        public int getClientID() {
            return clientID;
        }

        public ArrayList<String> getCards() {
            return cards;
        }
    }

    public int getClientID(){
        return messageBody.getClientID();
    }

    public ArrayList<String> getCards(){
        return messageBody.getCards();
    }
}

