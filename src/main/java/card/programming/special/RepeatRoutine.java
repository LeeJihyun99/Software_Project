package card.programming.special;

import card.Card;
import card.damage.DamageCard;
import card.programming.ProgrammingCard;
import game.Robot;
import server.protocol.chatnachrichten.Error;

public class RepeatRoutine extends ProgrammingCard {
    @Override
    public String getCardName() {
        return "RepeatRoutine";
    }

    @Override
    public void execute(Robot r) {
        if (r.getCurrentRegister() > 0){
            if (!(r.getRegister()[r.getCurrentRegister()] instanceof DamageCard)){
                r.getRegister()[r.getCurrentRegister() - 1].execute(r);
            }else {
                r.getRobotPlayer().getDrawnCards().get(0).execute(r);
                r.getRobotPlayer().getDrawnCards().remove(0);
            }
        }else {
            Error error = new Error("RepeatRoutine card cannot be played in the first register");
            r.getRobotPlayer().getServerThread().sendMessageSerialized(error);
        }

    }
}
