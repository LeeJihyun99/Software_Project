package server.protocol.verbindungaufbau;

import server.protocol.Message;

public class HelloServer extends Message {
    private String messageType;
    private MessageBody messageBody;

    public HelloServer(String group, boolean isAI, String protocol){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(group, isAI, protocol);
    }


    private class MessageBody{


        private String group;
        private boolean isAI;
        private String protocol;
        public MessageBody(String group, boolean isAI, String protocol){
            this.group = group;
            this.isAI = isAI;
            this.protocol = protocol;
        }

    }
    public String getGroup(){
        return messageBody.group;
    }

    public boolean isAI() {
        return messageBody.isAI;
    }

    public String getProtocol() {
        return messageBody.protocol;
    }

}
