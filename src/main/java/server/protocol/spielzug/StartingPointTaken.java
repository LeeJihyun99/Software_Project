package server.protocol.spielzug;

import server.protocol.Message;

public class StartingPointTaken extends Message {
    private String messageType;
    private MessageBody messageBody;

    public StartingPointTaken(int x, int y, int clientID){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(x, y, clientID);
    }

    private class MessageBody{
        private int x;
        private int y;
        private int clientID;

        public MessageBody(int x, int y, int clientID){
            this.x = x;
            this.y = y;
            this.clientID = clientID;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getClientID() {
            return clientID;
        }
    }

    public int getX(){
        return messageBody.getX();
    }

    public int getY(){
        return messageBody.getY();
    }

    public int getClientID(){
        return messageBody.getClientID();
    }
}
