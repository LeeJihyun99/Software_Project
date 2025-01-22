package server.protocol.chatnachrichten;

import server.protocol.Message;

public class ReceivedChat extends Message {
    private String messageType;
    private MessageBody messageBody;

    public ReceivedChat(String message, int from, boolean isPrivate){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(message, from, isPrivate);
    }

    private class MessageBody{
        private String message;
        private int from;
        private boolean isPrivate;
        public MessageBody(String message, int from, boolean isPrivate){
            this.message = message;
            this.from = from;
            this.isPrivate = isPrivate;
        }

    }

    public String getMessage() {
        return messageBody.message;
    }

    public int getFrom() {
        return messageBody.from;
    }

    public boolean isPrivate() {
        return messageBody.isPrivate;
    }

}
