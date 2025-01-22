package card.programming.special;

import card.programming.ProgrammingCard;
import game.Robot;
import server.protocol.aktionen.Energy;

public class EnergyRoutine extends ProgrammingCard {
    @Override
    public String getCardName() {
        return "EnergyRoutine";
    }

    @Override
    public void execute(Robot r) {
        r.setAmountCubes(r.getAmountCubes() + 1);
        r.getRobotPlayer().setEnergyReserve(r.getRobotPlayer().getEnergyReserve() + 1);

        Energy energyMsg = new Energy(r.getRobotPlayer().getPlayerID(),1,"special programming card");
        r.getGameServer().broadcast(energyMsg);
    }
}
