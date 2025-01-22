package server.protocol.aktionen;

import server.protocol.DummyMessage;
import server.protocol.Message;

public class ChooseRegister extends Message {
    private String messageType;
    private MessageBody messageBody;

    public ChooseRegister(int register){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(register);
    }

    private class MessageBody{
        private int register;

        public MessageBody(int register){
            this.register = register;
        }

        public int getRegister() {
            return register;
        }
    }

    public int getRegister(){
        return messageBody.getRegister();
    }
}
