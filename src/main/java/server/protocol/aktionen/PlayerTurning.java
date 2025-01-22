package server.protocol.aktionen;


import server.protocol.Message;

public class PlayerTurning extends Message {
    private String messageType;
    private MessageBody messageBody;

    public PlayerTurning(int clientID, String rotation){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID, rotation);
    }

    private class MessageBody{
        private int clientID;
        private String rotation;
        public MessageBody(int clientID, String rotation){
            this.clientID = clientID;
            this.rotation = rotation;
        }

        public int getClientID() {
            return clientID;
        }

        public String getRotation() {
            return rotation;
        }
    }
    public int getClientID() {
        return messageBody.getClientID();
    }

    public String getRotation() {
        return messageBody.getRotation();
    }
}
