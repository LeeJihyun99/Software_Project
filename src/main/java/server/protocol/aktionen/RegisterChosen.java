package server.protocol.aktionen;

import server.protocol.Message;

public class RegisterChosen extends Message {
    private String messageType;
    private MessageBody messageBody;

    public RegisterChosen(int clientID, int register){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID, register);
    }

    private class MessageBody{
        private int clientID;
        private int register;

        public MessageBody(int clientID, int register){
            this.clientID = clientID;
            this.register = register;
        }

        public int getClientID() {
            return clientID;
        }

        public int getRegister() {
            return register;
        }
    }

    public int getClientID() {
        return messageBody.getClientID();
    }

    public int getRegister() {
        return messageBody.getRegister();
    }
}
