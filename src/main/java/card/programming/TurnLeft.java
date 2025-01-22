package card.programming;

import game.Robot;
import image.Image;

public class TurnLeft extends ProgrammingCard{


    private int orientation;
    private String cardName = "TurnLeft";

    private Image cardImage;

    public TurnLeft() {
        this.orientation = -1;

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
