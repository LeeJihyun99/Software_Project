package card.programming.special;

import card.programming.ProgrammingCard;
import game.Robot;

public class SpeedRoutine extends ProgrammingCard {
    @Override
    public String getCardName() {
        return "SpeedRoutine";
    }

    @Override
    public void execute(Robot r) {
        r.move(3, r.getLookDir());
    }
}
