package server.protocol.lobby;

import server.protocol.Message;

public class PlayerStatus extends Message {
    private String messageType;
    private MessageBody messageBody;

    public PlayerStatus(int clientID, boolean ready){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(clientID, ready);
    }


    private class MessageBody{
        private int clientID;

        private boolean ready;

        public MessageBody(int clientID, boolean ready){
            this.clientID = clientID;
            this.ready = ready;
        }

    }
    public int getClientID() {
        return messageBody.clientID;
    }
    public boolean getReady() {
        return messageBody.ready;
    }
}
