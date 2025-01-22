package server.protocol.spielzug;

import server.protocol.Message;

import java.util.ArrayList;

public class CardsYouGotNow extends Message {
    private String messageType;
    private MessageBody messageBody;

    public CardsYouGotNow(ArrayList<String> cards){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(cards);
    }

    private class MessageBody{
        ArrayList<String> cards;

        public MessageBody(ArrayList<String> cards){
            this.cards = cards;
        }

        public ArrayList<String> getCards() {
            return cards;
        }
    }

    public ArrayList<String> getCards() {
        return messageBody.getCards();
    }

}
