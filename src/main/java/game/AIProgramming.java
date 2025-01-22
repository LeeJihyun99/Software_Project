package game;

import card.Card;
import card.programming.MoveI;
import card.programming.MoveIII;
import card.programming.TurnRight;
import com.sun.javafx.css.CalculatedValue;
import field.*;
import field.tools.*;
import org.paukov.combinatorics3.Generator;

import java.io.InputStream;
import java.util.*;

/**
 * Kümmert sich um die Logik der Programmierung der KI.
 * @author DavidKulbe
 */
public class AIProgramming
{
    private Robot robot; //Roboter welcher programmiert werden soll
    private CheckPoint nextCheckPoint; //der nächste CheckPoint, welchen der Roboter ansteuert
    private int rebooted = 0; //rebooted er im Durchlauf? f 0 oder t 1
    private int differenceLookDirectionToCheckPoint = 0;
    private Position currentPosition;
    
    private Position recoverPosition;
    
    private Direction recoverLookDirection;
    private int poweredUp;
    
    private int tookDamage;
    
    private int gotEnergyCubes;
    private Direction lookDirection;
    private Gameboard gameboard;
    private ArrayList<Position> allPositionsAffectedByLaser;
    
    static List<List<Integer>> permutations = new ArrayList<>();
    
    private double[] weights;
    
    private int currentRegister = 0; //the current number of the register, which is simulated
    
    private int wouldReachCheckPoint;
    
    private Card[] bestHand; //best Hand this programming cycle
    
    /**
     * Erstellt ein AI Programming Objekt
     * @param offset offset in beide Richtung
     * @param weights Gewichtung der Flags: weights[0] * distanceToCheckPoint + weights[1]*rebooted + weights[2]*tookDamage + weights[3]*gotEnergyCubes + weights[4]*poweredUp +
     *                 weights[5] * differenceLookDirectionToCheckPoint +  weights[6] * wouldReachCheckPoint;
     * @author David
     */
    /*public AIProgramming(double[] weights, double offset)
    {
        this.weights = weights;
        for (int i = 0; i < weights.length; i++)
        {
            weights[i] = weights[i]-offset + ((weights[i]+offset) - (weights[i]-offset)) * Math.random();
        }
        weights[0] = Math.abs(weights[0]);
    }*/
    
    /**
     * Automatically reads parameters for weights
     * @param offset offset used for top and bottom end of dynamics
     * @author David
     */
    public AIProgramming(double offset)
    {
        double[] readWeights = new double[7];
        int index = 0;
        String fileName = "ai/weights.txt";
        InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
        Scanner sc = new Scanner(in);
        while (sc.hasNextLine())
        {
            readWeights[index] = Double.parseDouble(sc.nextLine());
            index++;
        }
    
        this.weights = readWeights;
        for (int i = 0; i < weights.length; i++)
        {
            weights[i] = weights[i]-offset + ((weights[i]+offset) - (weights[i]-offset)) * Math.random();
        }
        weights[0] = Math.abs(weights[0]);
    }
    
    /**
     * Hauptmethode, nimmt die gezogenen Karten, berechnet alle Kombinationen an Registern, berechnet die beste Konstellation und gibt diese aus
     * @param cards Gezogene Karten
     * @return Gewählte Karten
     * @author David
     */
    public ArrayList<Card> getLoadOut(List<Card> cards, Robot robot, boolean lockRobotPosition)
    {
        this.robot = robot;
        gameboard = robot.getCurrentGame().getGameboard();
        if (currentPosition == null || !lockRobotPosition)
        {
            currentPosition = robot.getPosition();
            lookDirection = robot.getLookDir();
            nextCheckPoint = gameboard.getCheckPointByNumber(robot.getCheckpointToken()+1);
        }
        recoverPosition = currentPosition;
        recoverLookDirection = lookDirection;
        allPositionsAffectedByLaser = getAllPositionsAffectedByLaser();
        ArrayList<Card[]> combinations = calculateAllCombinations(cards);
        bestHand = calculateBestCombination(combinations);
        ArrayList<Card> bestHandAsList = new ArrayList<>();
        bestHandAsList.addAll(Arrays.asList(bestHand));
        resetValues();
        return bestHandAsList;
    }

    /**
     * Hauptmethode, nimmt die gezogenen Karten, berechnet alle Kombinationen an Registern, berechnet die beste Konstellation und gibt diese aus
     * @param cards Gezogene Karten
     * @return Gewählte Karten
     * @author David
     */
    public ArrayList<Card> getLoadOut(List<Card> cards, Robot robot, boolean lockRobotPosition, Gameboard gameBoard)
    {
        this.robot = robot;
        this.gameboard = gameBoard;
        if (currentPosition == null || !lockRobotPosition)
        {
            currentPosition = robot.getPosition();
            lookDirection = robot.getLookDir();
            nextCheckPoint = gameboard.getCheckPointByNumber(robot.getCheckpointToken()+1);
        }
        recoverPosition = currentPosition;
        recoverLookDirection = lookDirection;
        allPositionsAffectedByLaser = getAllPositionsAffectedByLaser();
        ArrayList<Card[]> combinations = calculateAllCombinations(cards);
        bestHand = calculateBestCombination(combinations);
        ArrayList<Card> bestHandAsList = new ArrayList<>();
        bestHandAsList.addAll(Arrays.asList(bestHand));
        resetValues();
        return bestHandAsList;
    }
    
    /**
     * Berechnet alle Kombinationen an Karten
     * @param cards Karten
     * @return Kombinationen (ArrayList mit Arrays, gespeichert Typ ist Card)
     * @author David
     */
    private ArrayList<Card[]> calculateAllCombinations(List<Card> cards)
    {
        ArrayList<Card[]> combinationsAsList = new ArrayList<>();
        //Die Liste beinhaltet Indizes der Karten
        List<List<Integer>> combinations = Generator.combination(0, 1, 2, 3, 4, 5, 6, 7, 8).simple(5).stream().toList();
        for (int i = 0; i<combinations.size(); i++)
        {
            permute(combinations.get(i),0);
        }
        for (int indexList = 0; indexList < permutations.size(); indexList++)
        {
            combinationsAsList.add(new Card[5]);
            for (int indexArray = 0; indexArray < 5; indexArray++)
            {
                combinationsAsList.get(indexList)[indexArray] = cards.get(permutations.get(indexList).get(indexArray));
            }
        }
        return combinationsAsList;
    }
    
    /**
     * Berechnet die beste Kombination an Karten
     * @param combinations Kombinationen der Karten
     * @return Beste Konstellation
     * @author David
     */
    private Card[] calculateBestCombination(ArrayList<Card[]> combinations)
    {
        Card[] hand = new Card[5];
        //der beste Wert ist der kleinste
        double currentMinValue = 10000;
        int indexWithBestValue = 0;

        HashMap<Integer, Double> indexAndValueMap = new HashMap<>(); //Speichert den Wert der berechneten Prognose für eine Kombination
        for (int i = 0; i<combinations.size(); i++)
        {
            indexAndValueMap.put(i, calculateValueOfCombination(combinations.get(i)));
        }
        int length = indexAndValueMap.keySet().size();

        for (int i = 0; i<length; i++)
        {
            if(currentMinValue > indexAndValueMap.get(i))
            {
                //System.out.println(currentMinValue  +  " " + indexAndValueMap.get(i));
                currentMinValue = indexAndValueMap.get(i);
                indexWithBestValue = i;
            }
        }
        calculateValueOfCombination(combinations.get(indexWithBestValue));
        /*for (Card card: combinations.get(indexWithBestValue))
        {
           System.out.println(card.getCardName());
        }*/
        //System.out.println(indexWithBestValue + " "+ currentPosition);
        return combinations.get(indexWithBestValue);
    }

    /**
     * Berechnet den Wert eine Kombination durch Simulation
     * @param combination Kombination an Karten
     * @return Wert als double
     * @author David
     */
    private double calculateValueOfCombination(Card[] combination)
    {
        resetValues();
        double value;
        
        //simulating
        for (int i = 0; i<combination.length; i++)
        {
            if(rebooted != 1)
            {
                currentRegister = i+1;
                simulateCard(combination, combination[i], i);
                simulateField();
            }
        }
        int distanceToCheckPoint = 0;
        if(nextCheckPoint != null)
        {
            distanceToCheckPoint = Math.abs(nextCheckPoint.getPosition().x() - currentPosition.x()) + Math.abs(nextCheckPoint.getPosition().y() - currentPosition.y());
        }
        value = weights[0] * distanceToCheckPoint + weights[1]*rebooted + weights[2]*tookDamage + weights[3]*gotEnergyCubes + weights[4]*poweredUp +
                weights[5] * differenceLookDirectionToCheckPoint +  weights[6] * wouldReachCheckPoint;
        //calculating
        //...
        return value;
    }
    
    /**
     * Simuliert eine Karte und setzt entsprechend Flags
     * @author David
     */
    private void simulateCard(Card[] cardstack, Card card, int indexOfCard)
    {
        switch (card.getCardName())
        {
          case "Again" -> {
            if (indexOfCard > 0)
            {
                simulateCard(cardstack, cardstack[indexOfCard-1], indexOfCard-1);
            }
            }
          case "BackUp" -> simulateMovement(1, 1, lookDirection.getOppositeDirection());
          case "MoveI" -> simulateMovement(1, 1, lookDirection);
          case "MoveII" -> simulateMovement(2, 1, lookDirection);
          case "MoveIII" -> simulateMovement(3, 1, lookDirection);
          case "PowerUp" -> poweredUp = 1;
          case "TurnLeft" -> simulateTurn(-1);
          case "TurnRight" -> simulateTurn(1);
          case "UTurn" -> simulateTurn(0);
        }
    
    }
    
    private void simulateMovement(int distance, int weight, Direction direction)
    {
        for (int i = 1; i <= Math.abs(distance); i++)
        {
        Position newPosition = direction.calculateNewPositionOfAction(currentPosition, weight);
        if (gameboard.isIndexValid(newPosition.x(), newPosition.y()))
        {
            FieldList newFields = gameboard.getFieldsAtPosition(newPosition);
            if (newFields != null)
            {
                if (newFields.isPassable())
                {
                    if (gameboard.getFieldsAtPosition(currentPosition).hasWall())
                    {
                        if (gameboard.getFieldsAtPosition(currentPosition).getWall().calculateCollision(gameboard, direction))
                        {
                            return;
                        }
                    } if (gameboard.getFieldsAtPosition(newPosition).hasWall())
                    {
                        if (gameboard.getFieldsAtPosition(newPosition).getWall().calculateCollision(gameboard, direction.getOppositeDirection()))
                        {
                            return;
                        }
                    }
                    else
                    {
                        currentPosition = newPosition;
                    }
                    if (gameboard.getFieldsAtPosition(currentPosition).hasPit())
                    {
                        reboot();
                        return;
                    }
                } else
                {
                    return;
                }
            } else
            {
                reboot();
                return;
            }//Geht über das Board, fällt runter und rebootet
        } else
        {
            reboot();
            return;
        }//Geht auf ein nicht existierendes Feld auf dem Board, fällt runter und rebootet
        }
    }
    
    private void reboot()
    {
        rebooted = 1;
        tookDamage++;
        if (gameboard.isKeyForTokens(gameboard.getFieldsAtPosition(currentPosition).getIsOnBoard())) //rebootet auf einem Token
        {
            currentPosition = gameboard.getRebootTokenOnBoard(gameboard.getFieldsAtPosition(currentPosition).getIsOnBoard()).getPosition();
            setLookDirectionTowardsCheckPoint();
        }
        else //rebootet beim Startfeld
        {
            currentPosition = robot.getPositionOfChosenStartingPoint();
            setLookDirectionTowardsCheckPoint();
        }
    }
    
    private void setLookDirectionTowardsCheckPoint()
    {
        if (nextCheckPoint != null)
        {
            int xCheckPoint = nextCheckPoint.getPosition().x();
            int yCheckPoint = nextCheckPoint.getPosition().y();
            int xPos = currentPosition.x();
            int yPos = currentPosition.y();
            if (xCheckPoint > xPos)
            {
                if (yCheckPoint >= yPos)
                {
                    lookDirection = new Direction(1);
                } else
                {
                    lookDirection = new Direction(0);
                }
            } else
            {
                if (yCheckPoint >= yPos)
                {
                    lookDirection = new Direction(2);
                } else
                {
                    lookDirection = new Direction(3);
                }
            }
        }
    }
    
    private void simulateTurn(int rotation)
    {
        switch (rotation)
        {
            case 1 -> lookDirection = new Direction((lookDirection.getDirectionInteger()+1) % 4);
            case -1 ->
            {
                if(lookDirection.getDirectionInteger() > 0)
                {
                lookDirection = new Direction((lookDirection.getDirectionInteger() - 1) % 4);
                }
                else lookDirection = new Direction(3);
            }
            case 0 -> lookDirection = new Direction((lookDirection.getDirectionInteger() + 2) % 4);
        }
    }
    
    /**
     * Simuliert das Feld, auf dem der Roboter aktuell steht und setzt entsprechend Flags
     * @author David
     */
    private void simulateField()
    {
        FieldList fieldsAtCurrentPosition = gameboard.getFieldsAtPosition(currentPosition);
        if (fieldsAtCurrentPosition != null)
        {
            if (fieldsAtCurrentPosition.hasConveyorBelt())
            {
                    simulateConveyorBelt();
            }
            if (fieldsAtCurrentPosition.hasPushPanel())
            {
                PushPanel pushPanel = fieldsAtCurrentPosition.getPushPanel();
                if (pushPanel.getRegNumbers().contains(currentRegister))
                {
                    simulateMovement(1, 1, pushPanel.getPanelOrientation().getOppositeDirection());
                }
            }
            if (fieldsAtCurrentPosition.hasGear())
            {
                if (fieldsAtCurrentPosition.getGear().getClockwise()) simulateTurn(1);
                else simulateTurn(-1);
            }
            if (allPositionsAffectedByLaser.contains(currentPosition))
            {
                tookDamage++;
            }
            if (fieldsAtCurrentPosition.hasEnergySpace())
            {
                if (currentRegister == 5 || fieldsAtCurrentPosition.getEnergySpace().getCubesPresent() > 0)
                {
                    gotEnergyCubes++;
                }
            }
            if (fieldsAtCurrentPosition.hasCheckPoint())
            {
                if (nextCheckPoint != null)
                {
                    if (fieldsAtCurrentPosition.getCheckPoint().getCheckNum() == nextCheckPoint.getCheckNum())
                    {
                        //nextCheckPoint = gameboard.getCheckPointByNumber(nextCheckPoint.getCheckNum()+1);
                        wouldReachCheckPoint = 1;
                    }
                }
            }
        }
    }
    
    /**
     * Simulate the current ConveyorBelt, includes movement and rotation
     * @author David
     */
    private void simulateConveyorBelt()
    {
        ConveyorBelt currentConveyorBelt = gameboard.getFieldsAtPosition(currentPosition).getConveyorBelt();
        if (currentConveyorBelt != null)
        {
            Position nextPosition = currentConveyorBelt.getFlow_direction_out().getNextPositionInDirection(currentPosition.x(), currentPosition.y());
            ConveyorBelt newConveyorBelt = null;
            if (gameboard.isIndexValid(nextPosition.x(), nextPosition.y()))
            {
                if (gameboard.getFieldsAtPosition(nextPosition) != null)
                {
                    newConveyorBelt = gameboard.getFieldsAtPosition(nextPosition).getConveyorBelt();
                }
            }
            //movement
            simulateMovement(1,1,currentConveyorBelt.getFlow_direction_out());
            //rotation
            if(newConveyorBelt != null)
            {
                if (newConveyorBelt.getFlow_direction_out().getDirectionInteger() != currentConveyorBelt.getFlow_direction_out().getDirectionInteger())
                {
                    if (newConveyorBelt.getFlow_direction_out().getDirectionInteger() == (currentConveyorBelt.getFlow_direction_out().getDirectionInteger() + 1) % 4)
                    {
                        simulateTurn(1); //rotation right
                    }
                    //rotation left
                    else
                    {
                        simulateTurn(-1);
                    }
                }
                if (currentConveyorBelt.getSpeed() == 2) //second conveyor belt movement
                {
                    nextPosition = currentConveyorBelt.getFlow_direction_out().getNextPositionInDirection(currentPosition.x(), currentPosition.y());
                    newConveyorBelt = null;
                    if (gameboard.isIndexValid(nextPosition.x(), nextPosition.y()))
                    {
                        if (gameboard.getFieldsAtPosition(nextPosition) != null)
                        {
                            newConveyorBelt = gameboard.getFieldsAtPosition(nextPosition).getConveyorBelt();
                        }
                    }
                    //movement
                    simulateMovement(1,1,currentConveyorBelt.getFlow_direction_out());
                    //rotation
                    if(newConveyorBelt != null)
                    {
                        if (newConveyorBelt.getFlow_direction_out().getDirectionInteger() != currentConveyorBelt.getFlow_direction_out().getDirectionInteger())
                        {
                            if (newConveyorBelt.getFlow_direction_out().getDirectionInteger() == (currentConveyorBelt.getFlow_direction_out().getDirectionInteger() + 1) % 4)
                            {
                                simulateTurn(1); //rotation right
                            }
                            //rotation left
                            else
                            {
                                simulateTurn(-1);
                            }
                        }
        
                    }
                }
            }
        }
    }
    
    
    /**
     * Gibt eine Liste aller Positionen aus, welche von einem Laser betroffen sind
     * @author David
     */
    private ArrayList<Position> getAllPositionsAffectedByLaser()
    {
        ArrayList<BoardLaser> lasers = gameboard.getLasers();
        ArrayList<Position> affectedPositions = new ArrayList<>();
        boolean shouldStop;
        for (BoardLaser laser: lasers)
        {
            shouldStop = false;
            Direction orientation = laser.getLaserOrientation();
            Position position = laser.getPosition();
            while (gameboard.isIndexValid(position.x(), position.y()))
            {
                if (gameboard.getAntennaOnBoard().getPosition().equals(position)) //schaut, ob die Antenne getroffen werden würde
                {
                    break;
                } else if (gameboard.getFieldsAtPosition(position).hasWall())
                {
                    Wall wall = gameboard.getFieldsAtPosition(position).getWall();
                    if (position != laser.getPosition())
                    {
                        if (wall.calculateCollision(gameboard, orientation.getOppositeDirection()))
                        {
                            break;
                        }
                        if(wall.calculateCollision(gameboard, orientation))
                        {
                            shouldStop = true;
                        }
                    }
                }
                affectedPositions.add(position);
                if (shouldStop)
                {
                    break;
                }
                position = orientation.getNextPositionInDirection(position.x(),position.y());
            }
        }
        return affectedPositions;
    }
    
    private void buy(){} //TODO
    
    public static void permute(List<Integer> intArray, int start) {
        for(int i = start; i < intArray.size(); i++){
            int temp = intArray.get(start);
            intArray.set(start,intArray.get(i));
            intArray.set(i,temp);
            permute(intArray, start + 1);
            intArray.set(i,intArray.get(start));
            intArray.set(start,temp);
        }
        if (start == intArray.size() - 1) {
            permutations.add(intArray);
        }
    }
    
    public boolean hasWon()
    {
        return nextCheckPoint == null;
    }
    
    public double[] getWeights()
    {
        return weights;
    }
    
    public void updateNextCheckPoint()
    {
        if (nextCheckPoint != null)
        {
            //System.out.println(currentPosition + " " + nextCheckPoint.getPosition());
            if (isOnCheckPoint())
            {
                nextCheckPoint = gameboard.getCheckPointByNumber(nextCheckPoint.getCheckNum() + 1);
            }
        }
    }
    
    public boolean isOnCheckPoint()
    {
        return nextCheckPoint.getPosition().equals(currentPosition);
    }
    
    private void resetValues()
    {
        //initializing
        rebooted = 0;
        differenceLookDirectionToCheckPoint = 0;
        currentPosition = recoverPosition;
        lookDirection = recoverLookDirection;
        poweredUp = 0;
        tookDamage = 0;
        gotEnergyCubes = 0;
        currentRegister = 0;
        wouldReachCheckPoint = 0;
    }
    
    public void trainingsSimulation(int regNum)
    {
        simulateCard(bestHand,bestHand[regNum],regNum);
        simulateField();
        updateNextCheckPoint();
    }
    
    public static void main(String[] args)
    {
       /* AIProgramming programming = new AIProgramming(new double[]{1.688965670457948, 2.4144220367075806, 0.6560726642937948, -0.30245716577640763, -0.19275233404825853, 0.11073246491668759, -3.167816020112563},0.0);
        programming.gameboard = new BoardGenerator().generateDizzyHighway();
        programming.currentPosition = new Position(1,8);
        programming.lookDirection = new Direction(1);
        programming.nextCheckPoint = programming.gameboard.getCheckPoints().get(0);
        ArrayList<Card> programmingCardSelection = new ArrayList<>();
        programmingCardSelection.add(new MoveIII());
        programmingCardSelection.add(new MoveIII());
        programmingCardSelection.add(new MoveIII());
        programmingCardSelection.add(new TurnRight());
        programmingCardSelection.add(new MoveI());
        Card[] hand = programmingCardSelection.toArray(new Card[0]);
        System.out.println(programming.calculateValueOfCombination(hand));
        System.out.println(programming.currentPosition);*/
    }
}
