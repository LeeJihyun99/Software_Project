package server.protocol.aktionen;

import server.protocol.DummyMessage;
import server.protocol.Message;

public class Energy extends Message {
    private String messageType;
    private MessageBody messageBody;

    public Energy(int clientID, int count, String source){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID, count, source);
    }

    private class MessageBody{
        private int clientID;
        private int count;
        private String source;

        public MessageBody(int clientID, int count, String source){
            this.clientID = clientID;
            this.count = count;
            this.source = source;
        }

        public int getClientID() {
            return clientID;
        }

        public int getCount() {
            return count;
        }

        public String getSource() {
            return source;
        }
    }

    public int getClientID() {
        return messageBody.getClientID();
    }

    public int getCount() {
        return messageBody.getCount();
    }

    public String getSource() {
        return messageBody.getSource();
    }

}
