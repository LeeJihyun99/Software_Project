package card.programming;

import card.Card;
import game.Robot;
import image.Image;

public class AgainCard extends ProgrammingCard{

    private String cardName = "Again";
    private Image cardImage;

    public void repeat(Robot r){
        for(int i=0; i < 5;i++){
            if(r.getRegister()[i].equals(this)){
                if(i>0){
                    r.getRegister()[i-1].execute(r);
                }else{
                    return; //Karte kann nicht im ersten Register gespielt werden - Sonderfall für Again Card in Reg pos 0
                }
            }
        }
    }

    @Override
    public String getCardName() {
        return cardName;
    }



    @Override
    public void execute(Robot r) {
        repeat(r);
    }
}
