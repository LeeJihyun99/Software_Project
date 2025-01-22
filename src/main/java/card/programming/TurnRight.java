package card.programming;

import game.Robot;
import image.Image;

public class TurnRight extends ProgrammingCard{


    private int orientation;
    private String cardName = "TurnRight";

    private Image cardImage;

    public TurnRight() {
        this.orientation = 1;
    }

    @Override
    public String getCardName() {
        return cardName;
    }

    @Override
    public void execute(Robot r) {
        r.turn(this.orientation);
    }
}
