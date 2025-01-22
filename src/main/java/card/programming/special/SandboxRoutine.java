package card.programming.special;

import card.Card;
import card.programming.*;
import game.Robot;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import server.protocol.spielzug.YourCards;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SandboxRoutine extends ProgrammingCard {

    private ObjectProperty<Card> chosenCard;
    private Thread waitThread;
    AtomicBoolean waitingEnded = new AtomicBoolean(false);

    @Override
    public String getCardName() {
        return "SandboxRoutine";
    }

    @Override
    public void execute(Robot r) {
        chosenCard = new SimpleObjectProperty<>();
        sendAvailableCardToChose(r);
        chosenCard.addListener((c, oldValue, newValue) -> {
            waitingEnded.set(true);
            newValue.execute(r);
        });

        waitForCardChosen();

    }

    private void sendAvailableCardToChose(Robot r){
        ArrayList<String> availableCards = new ArrayList<>();
        availableCards.add(new MoveI().getCardName());
        availableCards.add(new MoveII().getCardName());
        availableCards.add(new MoveIII().getCardName());
        availableCards.add(new BackUp().getCardName());
        availableCards.add(new TurnLeft().getCardName());
        availableCards.add(new TurnRight().getCardName());
        availableCards.add(new UTurn().getCardName());

        YourCards sandboxRoutineCard = new YourCards(availableCards);
        r.getRobotPlayer().getServerThread().sendMessageSerialized(sandboxRoutineCard);

    }
    public Card getChosenCard() {
        return chosenCard.get();
    }

    public void setChosenCard(Card chosenCard) {
        this.chosenCard.set(chosenCard);
    }

    private void waitForCardChosen() {
        waitThread = new Thread(()->{
            while (!waitingEnded.get()) {
                Thread.onSpinWait();
            }
            waitThread.interrupt();
        });
        waitThread.start();
    }
}
