package server.protocol.spielzug;

import server.protocol.Message;

public class SelectedCard extends Message {
    private String messageType;
    private MessageBody messageBody;

    public SelectedCard(String card, int register){
        this.messageType = this.getClass().getSimpleName();
        this.messageBody = new MessageBody(card, register);
    }

    private class MessageBody{
        private String card;
        private int register;

        public MessageBody(String card, int register){
            this.card = card;
            this.register = register;
        }

        public int getRegister() {
            return register;
        }

        public String getCard() {
            return card;
        }
    }
    public int getRegister() {
        return messageBody.getRegister();
    }

    public String getCard() {
        return messageBody.getCard();
    }

}
