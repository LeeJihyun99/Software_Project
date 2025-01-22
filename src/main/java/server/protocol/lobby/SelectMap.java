package server.protocol.lobby;

import server.protocol.Message;

import java.util.ArrayList;

public class SelectMap extends Message {
    private String messageType;
    private MessageBody messageBody;

    public SelectMap(ArrayList<String> availableMaps){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(availableMaps);
    }

    private class MessageBody{
        private ArrayList<String> availableMaps;
        public MessageBody(ArrayList<String> availableMaps){
            this.availableMaps = availableMaps;
        }

    }
    public ArrayList<String> getAvailableMaps() {
        return messageBody.availableMaps;
    }
}
