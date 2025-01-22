package card.programming;

import field.tools.Direction;
import game.Robot;
import image.Image;

public class BackUp extends ProgrammingCard{

    private String cardName = "BackUp";
    private Image cardImage;
    @Override
    public String getCardName() {
        return cardName;
    }

    @Override
    public void execute(Robot r) {
        r.move(-1,r.getLookDir().getOppositeDirection());
    }
}
