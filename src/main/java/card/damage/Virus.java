package card.damage;

import game.Robot;
import image.Image;
import server.protocol.spielzug.ReplaceCard;

import java.util.List;

/**
 * @author Melanie
 * Klasse für Implementierung der Virus Karte
 */
public class Virus extends DamageCard{

    private String cardName = "Virus";

    private Image cardImage;
    @Override
    public String getCardName() {
        return this.cardName;
    }

    /**
     * @author Melanie
     * @param r
     * Effekt der Virus-Karte
     */
    public void execute(Robot r){
        List<Robot> infectRobots = r.scanForRobotRadius(6);
        for(Robot robot : infectRobots){
            robot.takeDamage(1);
        }
        r.getCurrentGame().getVirusCardStack().add(this);

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
