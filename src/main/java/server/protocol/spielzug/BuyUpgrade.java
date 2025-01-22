package server.protocol.spielzug;

import server.protocol.DummyMessage;
import server.protocol.Message;

public class BuyUpgrade extends Message {
    private String messageType;
    private MessageBody messageBody;

    public BuyUpgrade(boolean isBuying, String card){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(isBuying, card);
    }

    private class MessageBody{
        private boolean isBuying;
        private String card;

        public MessageBody(boolean isBuying, String card) {
            this.isBuying = isBuying;
            this.card = card;
        }

        public boolean isBuying() {
            return isBuying;
        }

        public String getCard() {
            return card;
        }
    }

    public boolean isBuying() {
        return messageBody.isBuying();
    }

    public String getCard() {
        return messageBody.getCard();
    }
}
