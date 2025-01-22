package card;

import card.damage.Spam;
import card.damage.Trojan;
import card.damage.Virus;
import card.damage.Worm;
import card.programming.*;
import card.programming.special.*;
import card.upgrade.AdminPrivilege;
import card.upgrade.MemorySwap;
import card.upgrade.RearLaser;
import card.upgrade.SpamBlocker;
import game.Robot;
import image.Image;

public abstract class Card {

    private String cardName;

    private Image image;

    public abstract String getCardName();

    public abstract void execute(Robot r);

    public static Card getCardFromString(String cardName){
        Card card;

        switch (cardName){
            case("null") -> {card = null; break;}
            // DamageCards
            case ("Spam") -> {card = new Spam(); break;}
            case ("Trojan") -> {card = new Trojan(); break;}
            case ("Virus") -> {card = new Virus(); break;}
            case ("Worm") -> {card = new Worm(); break;}
            //ProgrammingCards
            case ("Again") -> {card = new AgainCard();  break;}
            case ("BackUp") -> {card = new BackUp(); break;}
            case ("MoveI") -> {card = new MoveI();  break;}
            case ("MoveII") -> {card = new MoveII();  break;}
            case ("MoveIII") -> {card = new MoveIII(); break;}
            case ("PowerUp") -> {card = new PowerUpCard();  break;}
            case ("TurnLeft") -> {card = new TurnLeft();  break;}
            case ("TurnRight") -> {card = new TurnRight();  break;}
            case ("UTurn") -> {card = new UTurn(); break;}
            //Special Upgrade Karten
            case ("EnergyRoutine") -> {card = new EnergyRoutine(); break;}
            case ("RepeatRoutine") -> {card = new RepeatRoutine(); break;}
            case ("SandboxRoutine") -> {card = new SandboxRoutine(); break;}
            case ("SpamFolder") -> {card = new SpamFolder(); break;}
            case ("SpeedRoutine") -> {card = new SpeedRoutine(); break;}
            case ("WeaselRoutine") -> {card = new WeaselRoutine(); break;}
            //Upgrade Karten
            case ("AdminPrivilege") -> {card = new AdminPrivilege(); break;}
            case ("MemorySwap") -> {card = new MemorySwap(); break;}
            case ("RearLaser") -> {card = new RearLaser(); break;}
            case ("SpamBlocker") -> {card = new SpamBlocker(); break;}
            //If cardtype cannot be determined
            default -> {card = null;}
        }
        return card;
    }



}
