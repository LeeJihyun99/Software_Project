package card.upgrade;

import card.Card;
import field.tools.Direction;
import game.Robot;

public class RearLaser extends Card {

    private boolean permanent;
    private boolean temporary;
    private int price;

    public RearLaser(){
        this.permanent = true;
        this.temporary = false;
        this.price = 2;
    }

    @Override
    public String getCardName() {
        return "RearLaser";
    }

    public int getPrice(){return this.price;}

    @Override
    public void execute(Robot r) {
        Direction rearLaser;

        if(r.getLookDir().getDirectionInteger() == 1 || r.getLookDir().getDirectionInteger() == 0 ){
            rearLaser = new Direction(r.getLookDir().getDirectionInteger()+2);
        }else{
            rearLaser = new Direction(r.getLookDir().getDirectionInteger()-2);
        }
        r.activateLaser(rearLaser);
    }
}
