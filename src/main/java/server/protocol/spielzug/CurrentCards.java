package server.protocol.spielzug;


import server.ActiveCard;
import server.protocol.Message;

import java.util.ArrayList;

public class CurrentCards extends Message {

    private String messageType;
    private MessageBody messageBody;

    public CurrentCards(){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody();
    }

    public ArrayList<ActiveCard> getCurrentCards() {
        return messageBody.activeCards;
    }

    private class MessageBody{
        private ArrayList<ActiveCard> activeCards;

        public MessageBody(){
            this.activeCards = new ArrayList<>();
        }

        public ArrayList<ActiveCard> getActiveCards() {
            return activeCards;
        }
    }

    public void addActiveCard(int clientID, String card){
        messageBody.activeCards.add(new ActiveCard(clientID, card));
    }


}

