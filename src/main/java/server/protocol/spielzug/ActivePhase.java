package server.protocol.spielzug;

import server.protocol.Message;

public class ActivePhase extends Message {
    private String messageType;
    private MessageBody messageBody;

    public ActivePhase(int phase){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(phase);
    }

    private class MessageBody{
        private int phase;

        public MessageBody(int phase){
            this.phase = phase;
        }

        public int getPhase() {
            return phase;
        }
    }
    public int getPhase(){
        return messageBody.getPhase();
    }


}
