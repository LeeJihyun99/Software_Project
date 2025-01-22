package card.upgrade;

import card.Card;
import card.damage.Spam;
import game.Robot;
import server.protocol.Message;
import server.protocol.spielzug.YourCards;

import java.util.ArrayList;

/**
 * @author Melanie
 */
public class SpamBlocker extends Card {

    private boolean temporary = true;
    private boolean permanent = false;
    @Override
    public String getCardName() {
        return "SpamBlocker";
    }

    @Override
    public void execute(Robot r) {
        ArrayList drawnCards = new ArrayList<>();
        for(Card c : r.getRobotPlayer().getDrawnCards()){
            if(c instanceof Spam){
                r.getDiscardPile().add(c);
                r.getRobotPlayer().getDrawnCards().remove(r.getRobotPlayer().getDrawnCards().indexOf(c));
                r.getRobotPlayer().getDrawnCards().add(r.getRobotPlayer().getProgrammingCardsStack().get(0));
                drawnCards.add(r.getRobotPlayer().getProgrammingCardsStack().get(0));
            }
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
        return 3;
    }
}
