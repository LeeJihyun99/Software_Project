package server;

import card.Card;
import field.CheckPoint;
import field.reducedFields.*;
import field.tools.BoardGenerator;
import field.tools.Position;
import field.tools.Gameboard;
import tools.ClientLogger;


import java.util.ArrayList;
import java.util.logging.Logger;

public class AIData {
    private ArrayList<Position> startPointList = new ArrayList<>();
    private Gameboard gameboard;
    private ArrayList<CheckPoint> checkPointVisited = new ArrayList<>();
    private ArrayList<CheckPoint> allCheckPoints = new ArrayList<>();
    private Card currentCard;
    private Logger logger = ClientLogger.getLogger();

    public void setFieldLists(ArrayList<ArrayList<ArrayList<ReducedField>>> gameBoard){
        for (ArrayList<ArrayList<ReducedField>> gameBoardPart : gameBoard) {
            for (ArrayList<ReducedField> fieldTypes : gameBoardPart){
                Position position = new Position(gameBoard.indexOf(gameBoardPart), gameBoardPart.indexOf(fieldTypes));
                for (ReducedField reducedField: fieldTypes){
                    switch (reducedField.getType()){
                        case "StartPoint": startPointList.add(position); break;

                    }
                }
            }

        }

    }

    public void generateGameBoard(String map){
        BoardGenerator boardGenerator = new BoardGenerator();
        switch (map){
            case "Start: Dizzy Highway": this.gameboard = boardGenerator.generateDizzyHighway(); break;
            case "Dizzy Highway": this.gameboard = boardGenerator.generateDizzyHighway(); break;
            case "Extra Crispy": this.gameboard = boardGenerator.generateExtraCrispy(); break;
            case "DeathTrap": this.gameboard = boardGenerator.generateDeathTrap(); break;
            case "Twister": this.gameboard = boardGenerator.generateTwister(); break;
            default: logger.warning("Check MapSelected-String");
        }
        allCheckPoints = gameboard.getCheckPoints();
        logger.info("Gameboard: " + gameboard.getName());
    }

    public ArrayList<CheckPoint> getCheckPointVisited() {
        return checkPointVisited;
    }

    public ArrayList<CheckPoint> getAllCheckPoints() {
        return allCheckPoints;
    }

    public ArrayList<Position> getStartPointList() {
        return startPointList;
    }

    public Gameboard getGameboard() {
        return gameboard;
    }

    public Card getCurrentCard() {
        return currentCard;
    }

    public void setCurrentCard(Card currentCard) {
        this.currentCard = currentCard;
    }
}
