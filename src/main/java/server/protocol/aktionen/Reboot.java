package server.protocol.aktionen;


import server.protocol.Message;

public class Reboot extends Message {
    private String messageType;
    private MessageBody messageBody;

    public Reboot(int clientID){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID);
    }

    private class MessageBody{
        private int clientID;

        public MessageBody(int clientID){
            this.clientID = clientID;
        }

        public int getClientID() {
            return clientID;
        }
    }

    public int getClientID(){
        return messageBody.getClientID();
    }
}
