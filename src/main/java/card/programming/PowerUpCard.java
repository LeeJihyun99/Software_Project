package card.programming;

import game.Robot;
import image.Image;
import server.protocol.aktionen.Energy;

public class PowerUpCard extends ProgrammingCard{
    private String cardName = "PowerUp";


    private Image cardImage;
    /**
     * @author Melanie
     * @param r - Robot which will be charged
     * Removes on Energycube from the robot to the players energy reserve
     */
    public void charge(Robot r){
        r.setAmountCubes(r.getAmountCubes()-1);
        r.getRobotPlayer().setEnergyReserve(r.getRobotPlayer().getEnergyReserve()+1);
        Energy energyMsg = new Energy(r.getRobotPlayer().getPlayerID(),1,"PowerUp Card");
        r.getGameServer().broadcast(energyMsg);
    }

    @Override
    public String getCardName() {
        return cardName;
    }

    @Override
    public void execute(Robot r) {
        charge(r);
    }
}
