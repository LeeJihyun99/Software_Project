package server.protocol.chatnachrichten;

import server.protocol.Message;

public class SendChat extends Message {
    private String messageType;
    private MessageBody messageBody;

    public SendChat(String message, int to){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(message, to);
    }

    private class MessageBody{

        private String message;
        private int to;

        public MessageBody(String message, int to){
            this.message = message;
            this.to = to;
        }

    }
    public String getMessage(){
       return messageBody.message;
    }

    public int getTo(){
        return messageBody.to;
    }

}
