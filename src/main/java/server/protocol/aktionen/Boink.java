package server.protocol.aktionen;

import server.protocol.DummyMessage;
import server.protocol.Message;

public class Boink extends Message {
    private String messageType;
    private MessageBody messageBody;

    public Boink(String orientation){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(orientation);
    }

    private class MessageBody{
        String orientation;

        public MessageBody(String orientation){
            this.orientation = orientation;
        }

        public String getOrientation() {
            return orientation;
        }
    }

    public String getOrientation(){
        return messageBody.getOrientation();
    }
}
