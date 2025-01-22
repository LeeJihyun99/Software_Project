package server.protocol.spielzug;
import server.protocol.Message;

import java.util.ArrayList;

public class TimerEnded extends Message {
    private String messageType;
    private MessageBody messageBody;

    public TimerEnded(ArrayList<Integer> clientIDs){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(clientIDs);
    }

    private class MessageBody{
        private ArrayList<Integer> clientIDs;

        public MessageBody(ArrayList<Integer> clientIDs){
            this.clientIDs = clientIDs;
        }

        public ArrayList<Integer> getClientIDs() {
            return clientIDs;
        }
    }

    public ArrayList<Integer> getClientIDs() {
        return messageBody.getClientIDs();
    }

}
