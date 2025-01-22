package server.protocol.spielkarten;

import server.protocol.Message;

public class PlayCard extends Message {
    private String messageType;
    private MessageBody messageBody;

    public PlayCard(String card){
        messageType = getClass().getSimpleName();
        messageBody = new MessageBody(card);
    }

    private class MessageBody{
        private String card;

        public MessageBody(String card){
            this.card = card;
        }

        public String getCard(){
            return card;
        }

    }

    public String getCard(){
        return messageBody.getCard();
    }
}
