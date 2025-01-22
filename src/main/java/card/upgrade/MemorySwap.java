package card.upgrade;

import card.Card;
import game.Robot;
import server.protocol.Message;
import server.protocol.spielzug.YourCards;

import java.util.ArrayList;

public class MemorySwap extends Card {
    @Override
    public String getCardName() {
        return "MemorySwap";
    }

    @Override
    public void execute(Robot r) {
        ArrayList<String> drawnCards = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            if (r.getRobotPlayer().getProgrammingCardsStack().size() < 3) {
                r.getRobotPlayer().shuffleCards();
            }
            drawnCards.add(r.getRobotPlayer().getProgrammingCardsStack().get(0).getCardName());
            r.getRobotPlayer().getExchangeCards().add(r.getRobotPlayer().getProgrammingCardsStack().get(0));
            r.getRobotPlayer().getProgrammingCardsStack().remove(0);

        }
        Message yourCards = new YourCards(drawnCards);
        r.getRobotPlayer().getServerThread().sendMessageSerialized(yourCards);

        for(int i=0; i<3; i++){
            if(r.getUpgradesTemp()[i] == this){
                r.getDiscardPile().add(this);
                r.getUpgradesTemp()[i] = null;
            }
        }
    }

    public int getCost(){
        return 1;
    }

    public String getDescription(){
        return "Draw three cards. Then choose three from your hand to put on top of your deck";
    }

    public boolean getPermanent(){
        return false;
    }

    public boolean getTemporary(){
        return true;
    }
}
