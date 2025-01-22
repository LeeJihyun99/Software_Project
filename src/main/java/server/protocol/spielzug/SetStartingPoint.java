package server.protocol.spielzug;

import server.protocol.Message;

public class SetStartingPoint extends Message {
    private String messageType;
    private MessageBody messageBody;

    public SetStartingPoint(int x, int y){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(x, y);
    }

    private class MessageBody{
        private int x;
        private int y;
        public MessageBody(int x, int y){
            this.x = x;
            this.y = y;

        }
        public int getX(){
            return x;
        }

        public int getY(){
            return y;
        }

    }

    public int getX(){
        return messageBody.getX();
    }

    public int getY(){
        return messageBody.getY();
    }
}
