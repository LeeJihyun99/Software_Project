package card.programming;

import game.Robot;
import image.Image;

public class MoveIII extends ProgrammingCard{

    private int amountMove;
    private String cardName;

    private Image cardImage;

    public MoveIII() {
        this.cardName =  "MoveIII";
        this.amountMove = 3;
    }

    @Override
    public String getCardName() {
        return cardName;
    }

    @Override
    public void execute(Robot r) {
        r.move(amountMove, r.getLookDir());
    }
}
