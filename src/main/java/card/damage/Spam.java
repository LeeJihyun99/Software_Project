package card.damage;

import game.Robot;
import image.Image;
import server.protocol.spielzug.ReplaceCard;

import java.util.Collections;

public class Spam extends DamageCard{
    private String cardName = "Spam";

    private Image cardImage;
    @Override
    public String getCardName() {
        return cardName;
    }
    @Override
    public void execute(Robot r) {
        r.takeDamage(1);
        r.getCurrentGame().getSpamCardStack().add(this);
        // Oberste Karte des ProgrammingStacks spielen
        if(r.getRobotPlayer().getProgrammingCardsStack().size()-1 < 0){
            r.getRobotPlayer().shuffleCards();
        }
        String newCard = r.getRobotPlayer().getProgrammingCardsStack().get(r.getRobotPlayer().getProgrammingCardsStack().size()-1).getCardName();
        r.getRegister()[r.getCurrentGame().getIterateRegistry()] = r.getRobotPlayer().getProgrammingCardsStack().get(r.getRobotPlayer().getProgrammingCardsStack().size()-1);
        r.getRobotPlayer().getProgrammingCardsStack().remove(r.getRobotPlayer().getProgrammingCardsStack().size()-1);
        ReplaceCard replaceCard = new ReplaceCard(r.getCurrentGame().getIterateRegistry(),newCard,r.getRobotPlayer().getPlayerID());
        r.getCurrentGame().getGameServer().broadcast(replaceCard);
        r.getRegister()[r.getCurrentGame().getIterateRegistry()].execute(r);
    }
}
