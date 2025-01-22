package server.protocol.spielzug;

import server.protocol.Message;

public class UpgradeBought extends Message {
    private String messageType;
    private MessageBody messageBody;

    public UpgradeBought(int clientID, String card){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID, card);
    }

    private class MessageBody{
        private int clientID;
        private String card;

        public MessageBody(int clientID, String card) {
            this.clientID = clientID;
            this.card = card;
        }

        public int getClientID() {
            return clientID;
        }

        public String getCard() {
            return card;
        }
    }

    public int getClientID() {
        return messageBody.getClientID();
    }

    public String getCard() {
        return messageBody.getCard();
    }
}
