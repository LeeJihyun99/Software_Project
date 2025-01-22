package server.protocol.lobby;

import server.protocol.Message;

public class PlayerAdded extends Message {
    private String messageType;
    private MessageBody messageBody;

    public PlayerAdded(int clientID, String name, int figure){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(clientID, name, figure);
    }

    private class MessageBody{


        private int clientID;
        private String name;

        private int figure;
        public MessageBody(int clientID, String name, int figure){
            this.clientID = clientID;
            this.name = name;
            this.figure = figure;
        }

    }
    public int getClientID() {
        return messageBody.clientID;
    }
    public int getFigure() {
        return messageBody.figure;
    }
    public String getName() {
        return messageBody.name;
    }

}
