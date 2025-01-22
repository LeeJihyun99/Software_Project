package card.programming;

import game.Robot;
import image.Image;

public class MoveII extends ProgrammingCard{

    private int amountMove;
    private String cardName;

    private Image cardImage;

    public MoveII() {
        this.cardName =  "MoveII";
        this.amountMove = 2;
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
