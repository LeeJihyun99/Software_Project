package server.protocol.lobby;

import server.protocol.Message;

import java.util.ArrayList;

public class MapSelected extends Message {
    private String messageType;
    private MessageBody messageBody;

    public MapSelected(String map){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(map);
    }

    private class MessageBody{

        private String map;
        public MessageBody(String map){
            this.map = map;
        }

    }

    public String getMap() {
        return messageBody.map;
    }

}
