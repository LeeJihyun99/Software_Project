package server.protocol.chatnachrichten;

import server.protocol.Message;

public class Error extends Message{
    private String messageType;
    private MessageBody messageBody;

    public Error(String error){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(error);
    }

    private class MessageBody{
        private String error;

        public MessageBody(String error){
            this.error = error;
        }

    }
    public String getError() {
        return messageBody.error;
    }
}
