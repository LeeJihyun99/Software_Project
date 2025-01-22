package server.protocol.aktionen;

import server.protocol.DummyMessage;
import server.protocol.Message;

import java.util.ArrayList;

public class SelectedDamage extends Message {
    private String messageType;
    private MessageBody messageBody;

    public SelectedDamage(ArrayList<String> cards){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(cards);
    }

    private class MessageBody{
        private ArrayList<String> cards;

        public MessageBody(ArrayList<String> cards){
            this.cards = cards;
        }

        public ArrayList<String> getCards() {
            return cards;
        }
    }

    public ArrayList<String> getCards(){
        return messageBody.getCards();
    }
}
