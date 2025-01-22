package card.programming.special;

import card.Card;
import card.damage.Spam;
import card.programming.ProgrammingCard;
import game.Robot;

public class SpamFolder extends ProgrammingCard {
    @Override
    public String getCardName() {
        return "SpamFolder";
    }

    @Override
    public void execute(Robot r) {
        for (Card c : r.getDiscardPile()) {
            if (c instanceof Spam){
                r.getDiscardPile().remove(c);
                r.getDamageCardName().remove(c.getCardName());
                r.getCurrentGame().getSpamCardStack().add((Spam) c);
                break;
            }
        }
    }
}
