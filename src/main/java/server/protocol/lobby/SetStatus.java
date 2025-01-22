package server.protocol.lobby;

import server.protocol.Message;

public class SetStatus extends Message {
    private String messageType;
    private MessageBody messageBody;

    public SetStatus(boolean ready){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(ready);
    }

    private class MessageBody{
        private boolean ready;
        public MessageBody(boolean ready){
            this.ready = ready;
        }

    }
    public boolean isReady() {
        return messageBody.ready;
    }
}
