package server.protocol.aktionen;

import server.protocol.DummyMessage;
import server.protocol.Message;

public class GameFinished extends Message{
    private String messageType;
    private MessageBody messageBody;

    public GameFinished(int clientID){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID);
    }

    private class MessageBody extends Message {
        private int clientID;

        public MessageBody(int clientID){
            this.clientID = clientID;
        }

        public int getClientID() {
            return clientID;
        }
    }

    public int getClientID() {
        return messageBody.getClientID();
    }

}
