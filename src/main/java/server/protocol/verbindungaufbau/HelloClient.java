package server.protocol.verbindungaufbau;

import server.protocol.Message;

public class HelloClient extends Message {
    private String messageType;
    private MessageBody messageBody;

    public HelloClient(String protocol){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(protocol);
    }

    private class MessageBody{
        private String protocol;
        public MessageBody(String protocol){
            this.protocol = protocol;
        }

        public String getProtocol() {
            return protocol;
        }
    }

    public String getProtocol() {
        return messageBody.protocol;
    }

}
