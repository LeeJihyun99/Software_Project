package server.protocol;


public class DummyMessage extends Message {
    private String messageType;
    private MessageBody messageBody;

    public DummyMessage(){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody();
    }

    private class MessageBody{

        public MessageBody(){
        }

    }


}
