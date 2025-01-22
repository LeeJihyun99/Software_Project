package server.protocol.spielzug;


import server.protocol.Message;

import java.util.ArrayList;

public class YourCards extends Message {
    private String messageType;
    private MessageBody messageBody;

    public YourCards(ArrayList<String> cardsInHand){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(cardsInHand);
    }

    private class MessageBody{
        private ArrayList <String> cardsInHand;
        public MessageBody(ArrayList<String> cardsInHand){
            this.cardsInHand = cardsInHand;
        }

        public ArrayList<String> getCardsInHand(){
            return cardsInHand;
        }
    }

    public ArrayList<String> getCardsInHand(){
        return messageBody.getCardsInHand();
    }
}
