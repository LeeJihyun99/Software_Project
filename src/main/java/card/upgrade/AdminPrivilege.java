package card.upgrade;

import card.Card;
import game.Player;
import game.Robot;

public class AdminPrivilege extends Card {
    private int cost = 3;
    private boolean temporary = false;
    private boolean permanent = true;
    private String description = "Once per round, you may give your robot priority for one register.";

    @Override
    public String getCardName() {
        return "AdminPrivilege";
    }

    @Override
    public void execute(Robot r) {

    }

    public void addToPriorityList(Player p, int register){

        switch (register){
            case 1:
                p.getCurrentGame().getPriorityRegister0().add(p);
                break;
            case 2:
                p.getCurrentGame().getPriorityRegister1().add(p);
                break;
            case 3:
                p.getCurrentGame().getPriorityRegister2().add(p);
                break;
            case 4:
                p.getCurrentGame().getPriorityRegister3().add(p);
                break;
            case 5:
                p.getCurrentGame().getPriorityRegister4().add(p);
                break;
        }

    }

    public int getCost() {
        return cost;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public String getDescription() {
        return description;
    }
}
