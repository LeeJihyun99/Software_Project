package server.protocol.spielzug;

import server.protocol.Message;

public class ShuffleCoding extends Message {
    private String messageType;
    private MessageBody messageBody;

    public ShuffleCoding(int clientID){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID);
    }

    private class MessageBody{
        private int clientID;

        public MessageBody(int clientID){
            this.clientID = clientID;
        }

        public int getClientID() {
            return clientID;
        }
    }

    public int getClientID(){
        return messageBody.getClientID();
    }
}
