package server;

public class ActiveCard {
    private int clientID;
    private String card;

    public ActiveCard(int clientID, String card) {
        this.clientID = clientID;
        this.card = card;
    }

    public String getCard() {
        return card;
    }

    public int getClientID() {
        return clientID;
    }
}
