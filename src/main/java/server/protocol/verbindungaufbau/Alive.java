package server.protocol.verbindungaufbau;

import server.protocol.Message;

public class Alive extends Message {
    private String messageType;
    private MessageBody messageBody;

    public Alive(){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody();
    }

    private class MessageBody{

        public MessageBody(){}

    }

}
