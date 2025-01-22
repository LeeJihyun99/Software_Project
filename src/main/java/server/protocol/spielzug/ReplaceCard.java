package server.protocol.spielzug;

import server.protocol.Message;

public class ReplaceCard extends Message {
    private String messageType;
    private MessageBody messageBody;

    public ReplaceCard(int register, String newCard, int clientID){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(register, newCard, clientID);
    }

    private class MessageBody{
        private int register;
        private String newCard;
        private int clientID;

        public MessageBody(int register, String newCard, int clientID){
            this.register = register;
            this.clientID = clientID;
            this.newCard = newCard;
        }

        public int getClientID() {
            return clientID;
        }

        public int getRegister() {
            return register;
        }

        public String getNewCard() {
            return newCard;
        }
    }

    public int getClientID() {
        return messageBody.getClientID();
    }

    public int getRegister() {
        return messageBody.getRegister();
    }

    public String getNewCard() {
        return messageBody.getNewCard();
    }
}
