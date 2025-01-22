package card.damage;

import game.Robot;
import image.Image;
import server.protocol.spielzug.ReplaceCard;

/**
 * @author Melanie
 * Imlementierung der Tojan Horse Karte
 */
public class Trojan extends DamageCard{
    private String cardName = "Trojan";

    private Image cardImage;
    @Override
    public String getCardName() {
        return cardName;
    }

    /**
     * @author Melanie
     * @param r
     * Effekt der Trojan Horse Karte
     */
    public void execute(Robot r){
        r.takeDamage(2);
        r.getCurrentGame().getTrojanHorseCardsStack().add(this);

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
