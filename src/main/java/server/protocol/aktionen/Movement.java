package server.protocol.aktionen;


import server.protocol.Message;

public class Movement extends Message {
    private String messageType;
    private MessageBody messageBody;

    public Movement(int clientID, int x, int y){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID, x, y);
    }

    private class MessageBody{
        private int clientID;
        private int x;
        private int y;
        public MessageBody(int clientID, int x, int y){
            this.clientID = clientID;
            this.x = x;
            this.y = y;
        }

        public int getClientID() {
            return clientID;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

    }

    public int getClientID() {
        return messageBody.getClientID();
    }

    public int getX() {
        return messageBody.getX();
    }

    public int getY() {
        return messageBody.getY();
    }
}
