package server.protocol.verbindungaufbau;

import server.protocol.Message;

public class Welcome extends Message {
    private String messageType;
    private MessageBody messageBody;

    public Welcome(int clientID){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(clientID);
    }

    private class MessageBody{


        private int clientID;
        public MessageBody(int clientID){
            this.clientID = clientID;
        }

    }
    public int getClientID() {
        return messageBody.clientID;
    }

}
