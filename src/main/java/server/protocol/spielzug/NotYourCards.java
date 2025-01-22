package server.protocol.spielzug;

import server.protocol.Message;

public class NotYourCards extends Message {
    private String messageType;
    private MessageBody messageBody;

    public NotYourCards(int clientID, int cardsInHand){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID, cardsInHand);
    }

    private class MessageBody{
        private int clientID;
        private int cardsInHand;

        public MessageBody(int clientID, int cardsInHand){
            this.clientID = clientID;
            this.cardsInHand = cardsInHand;
        }

        public int getClientID() {
            return clientID;
        }

        public int getCardsInHand() {
            return cardsInHand;
        }
    }

    public int getClientID(){
        return messageBody.getClientID();
    }

    public int getCardsInHand(){
        return messageBody.getCardsInHand();
    }
}
