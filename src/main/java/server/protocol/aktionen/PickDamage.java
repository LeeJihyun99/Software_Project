package server.protocol.aktionen;

import server.protocol.DummyMessage;
import server.protocol.Message;

import java.util.ArrayList;

public class PickDamage extends Message {
    private String messageType;
    private MessageBody messageBody;

    public PickDamage(int count, ArrayList<String> availablePiles){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(count, availablePiles);
    }

    private class MessageBody{

        private int  count;
        private ArrayList<String> availablePiles;
        public MessageBody(int count, ArrayList<String> availablePiles){
            this.count = count;
            this.availablePiles = availablePiles;
        }

        public ArrayList<String> getAvailablePiles() {
            return availablePiles;
        }

        public int getCount() {
            return count;
        }
    }

    public int getCount(){
        return messageBody.getCount();
    }

    public  ArrayList<String> getAvailablePiles(){
        return messageBody.getAvailablePiles();
    }
}
