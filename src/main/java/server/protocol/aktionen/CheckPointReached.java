package server.protocol.aktionen;

import server.protocol.Message;

public class CheckPointReached extends Message {
    private String messageType;
    private MessageBody messageBody;

    public CheckPointReached(int clientID, int number){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID, number);
    }

    private class MessageBody{
        private int clientID;
        private int number;

        public MessageBody(int clientID, int number){
            this.clientID = clientID;
            this.number = number;
        }

        public int getClientID() {
            return clientID;
        }

        public int getNumber() {
            return number;
        }
    }

    public int getClientID() {
        return messageBody.getClientID();
    }

    public int getNumber() {
        return messageBody.getNumber();
    }

}
