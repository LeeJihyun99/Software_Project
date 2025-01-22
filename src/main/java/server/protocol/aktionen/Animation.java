package server.protocol.aktionen;

import server.protocol.DummyMessage;
import server.protocol.Message;

public class Animation extends Message {
    private String messageType;
    private MessageBody messageBody;

    public Animation(String type){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(type);
    }

    private class MessageBody{
        private String type;
        public MessageBody(String type){
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
    public String getType() {
        return messageBody.getType();
    }
}
