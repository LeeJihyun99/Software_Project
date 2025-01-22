package card.programming;

import game.Robot;
import image.Image;

public class MoveI extends ProgrammingCard{

    private int amountMove;
    private String cardName;

    private Image cardImage;

    public MoveI() {
        this.cardName =  "MoveI";
        this.amountMove = 1;
    }


    @Override
    public String getCardName() {
        return cardName;
    }

    @Override
    public void execute(Robot r) {
        r.move(amountMove,r.getLookDir());
    }
}
