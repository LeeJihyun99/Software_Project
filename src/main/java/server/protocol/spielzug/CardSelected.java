package server.protocol.spielzug;


import server.protocol.Message;

public class CardSelected extends Message {

    private String messageType;
    private MessageBody messageBody;

    public CardSelected(int clientID, int register, boolean filled){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientID, register, filled);
    }


    private class MessageBody{
        private int clientID;
        private int register;
        private boolean filled;

        public MessageBody(int clientID, int register, boolean filled){
            this.clientID = clientID;
            this.register = register;
            this.filled = filled;
        }

        public int getRegister() {
            return register;
        }

        public int getClientID() {
            return clientID;
        }

        public boolean isFilled() {
            return filled;
        }
    }

    public int getRegister() {
        return messageBody.getRegister();
    }

    public int getClientID() {
        return messageBody.getClientID();
    }

    public boolean getFilled() {
        return messageBody.isFilled();
    }

}
