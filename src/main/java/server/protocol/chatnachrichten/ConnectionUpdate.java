package server.protocol.chatnachrichten;

import server.protocol.Message;

public class ConnectionUpdate extends Message {
    private String messageType;
    private MessageBody messageBody;

    public ConnectionUpdate(int clientID, boolean isConnected, String action){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(clientID, isConnected, action);
    }

    private class MessageBody{
        private int clientID;

        private boolean isConnected;

        private String action;
        public MessageBody(int clientID, boolean isConnected, String action){
            this.clientID = clientID;
            this.isConnected = isConnected;
            this.action = action;
        }

    }

    public int getClientID() {
        return messageBody.clientID;
    }

    public boolean isConnected() {
        return messageBody.isConnected;
    }

    public String getAction() {
        return messageBody.action;
    }
}
