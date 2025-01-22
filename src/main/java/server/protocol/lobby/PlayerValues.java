package server.protocol.lobby;

import server.protocol.Message;

public class PlayerValues extends Message {
    private String messageType;
    private MessageBody messageBody;

    public PlayerValues(String name, int figure){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(name, figure);
    }

    private class MessageBody{
        private String name;
        private int figure;

        public MessageBody(String name, int figure){
           this.name = name;
           this.figure = figure;
        }

    }
    public int getFigure() {
        return messageBody.figure;
    }
    public String getName() {
        return messageBody.name;
    }

}
