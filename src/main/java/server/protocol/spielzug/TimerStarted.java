package server.protocol.spielzug;

import server.protocol.Message;

public class TimerStarted extends Message {
    private String messageType;
    private MessageBody messageBody;

    public TimerStarted(){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody();
    }

    private class MessageBody{

        public MessageBody(){
        }

    }
}
