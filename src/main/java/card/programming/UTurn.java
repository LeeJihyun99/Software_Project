package card.programming;

import game.Robot;
import image.Image;

public class UTurn extends ProgrammingCard{


    private int orientation;
    private String cardName="UTurn";

    private Image cardImage;

    public UTurn() {
        this.orientation = 0;
    }

    @Override
    public String getCardName() {
        return cardName;
    }

    @Override
    public void execute(Robot r) {
        r.turn(orientation);
    }
}
