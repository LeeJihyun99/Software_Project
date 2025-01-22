package server.protocol.aktionen;

import server.protocol.Message;

public class RebootDirection extends Message {
    private String messageType;
    private MessageBody messageBody;

    public RebootDirection(String direction){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(direction);
    }

    private class MessageBody{
        private String direction;
        public MessageBody(String direction){
            this.direction = direction;
        }

        public String getDirection() {
            return direction;
        }
    }

    public String getDirection(){
        return messageBody.getDirection();
    }
}
