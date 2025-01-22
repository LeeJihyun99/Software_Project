package field.tools;

import field.Field;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.io.File;

/**
 * Generiert aus .txt Files ein Spielfeld
 * @author DavidKulbe
 */
public class BoardGenerator
{
    public Gameboard generateDizzyHighway()
    {
        Gameboard dizzyHighway =  generateGameboardFromString(readFromFile("maps/DizzyHighway.txt"));
        dizzyHighway.setName("DizzyHighway");
        dizzyHighway.clusterIntoCourses();
        return dizzyHighway;
    }
    public Gameboard generateExtraCrispy()
    {
        Gameboard extraCrispy = generateGameboardFromString(readFromFile("maps/ExtraCrispy.txt"));
        extraCrispy.setName("ExtraCrispy");
        extraCrispy.clusterIntoCourses();
        return extraCrispy;
    }
    public Gameboard generateLostBearings()
    {
        Gameboard lostBearings = generateGameboardFromString(readFromFile("maps/LostBearings.txt"));
        lostBearings.setName("LostBearings");
        lostBearings.clusterIntoCourses();
        return lostBearings;
    }
    public Gameboard generateDeathTrap()
    {
        Gameboard deathTrap = generateGameboardFromString(readFromFile("maps/DeathTrap.txt"));
        deathTrap.setName("DeathTrap");
        deathTrap.clusterIntoCourses();
        return deathTrap;
    }

    public Gameboard generateTwister()
    {
        Gameboard twister = generateGameboardFromString(readFromFile("maps/Twister.txt"));
        twister.setName("Twister");
        twister.clusterIntoCourses();
        return twister;
    }
    
    public Gameboard generateTrainingsMap()
    {
        Gameboard training = generateGameboardFromString(readFromFile("maps/Training.txt"));
        training.setName("Training");
        training.clusterIntoCourses();
        return training;
    }
    
    public Gameboard generateGameboardFromString(String abstractedBoard)
    {
        String[] lines = abstractedBoard.split("\n");
        String[] parameters = lines[0].split(",");
        Gameboard gameboard = (Gameboard) identifyParameters(parameters, "gameboard");
        for (int y = 1; y < lines.length; y++)
        {
            interpretLine(gameboard, y-1, lines[y]);
        }
        gameboard.scanAndSetAntennaOnBoard();
        gameboard.initializeStartingPoints();
        return gameboard;
    }
    
    public Board identifyParameters(String[] parameters, String type)
    {
        Board board = null;
        String[] size = parameters[0].split("x");
        int x = Integer.parseInt(size[0]);
        int y = Integer.parseInt(size[1]);
        switch (type)
        {
            case ("gameboard") ->
            {
                board = new Gameboard(x, y);
                if(parameters.length > 1)
                {
                    switch (parameters[1])
                    {
                        case ("Default") -> board.initializeWithDefaultField();
                    }
                }
            }
        }
        return board;
    }
    
    public void interpretLine(Board board, int y, String line)
    {
        int index = 0;
        int x = 0;
        int tileCounter;
        String currentCharacter;
        while (index < line.length())
        {
            tileCounter = 1;
            currentCharacter = String.valueOf(line.charAt(index));
            //Enthält er eine Nummer?
            if(currentCharacter.matches("[0-9]+"))
            {
                if(index+1 < line.length())
                {
                    String currentAndNextCharacter = currentCharacter + line.charAt(index + 1);
                    if (currentAndNextCharacter.matches("[0-9]+"))
                    {
                        currentCharacter = currentAndNextCharacter;
                        index++;
                    }
                }
                tileCounter = Integer.parseInt(currentCharacter);
                currentCharacter = String.valueOf(line.charAt(++index));
            }
            if(currentCharacter.equals("{"))
            {
                String fields = getInterval(line, index, "{", "}");
                index = getIndexOfEndsymbol(line, index, "{", "}");
                for(int i=0; i<tileCounter; i++)
                {
                    board.addFieldsAtPosition(identifyFields(fields,  x,  y), new Position(x++,y));
                }
            }
            else if(index+1 < line.length())
            {
                if (String.valueOf(line.charAt(index + 1)).equals("("))
                {
                String fieldWithParameter = getInterval(line, index, "(", ")");
                index = getIndexOfEndsymbol(line, index, "(", ")");
                for (int i = 0; i < tileCounter; i++)
                {
                    board.addSingleFieldAtPosition(identifyFieldWithParameter(currentCharacter,fieldWithParameter, x, y), new Position(x++, y));
                }
                }
                else
                {
                    x = addSingleFieldToPositionAndGetNextX(board, y, x, tileCounter, currentCharacter);
                }
            }
            else
            {
                x = addSingleFieldToPositionAndGetNextX(board, y, x, tileCounter, currentCharacter);
            }
            index++;
        }
    }
    
    private int addSingleFieldToPositionAndGetNextX(Board board, int y, int x, int tileCounter, String currentCharacter)
    {
        for(int i=0; i<tileCounter; i++)
        {
            if (!currentCharacter.equals("#"))
            {
                board.addSingleFieldAtPosition(identifyField(currentCharacter, x, y), new Position(x++, y));
            }
            else
            {
                board.resetFieldAtPosition(new Position(x++,y));
            }
        }
        return x;
    }
    
    public Field identifyField(String parameter, int x, int y)
    {
        FieldGenerator fieldGenerator = new FieldGenerator();
        String type = String.valueOf(parameter.charAt(0));
        Position position = new Position(x, y);
        switch (type)
        {
            case "." -> {
            return fieldGenerator.generateDefaultField(position);
            }
            case "s" -> {
                return fieldGenerator.generateStartPoint(position);
            }
            case "p" ->
            {
                return fieldGenerator.generatePit(position);
            }
        }
        return null;
    }
    
    public Field identifyFieldWithParameter(String fieldType, String parameter, int x, int y)
    {
        FieldGenerator fieldGenerator = new FieldGenerator();
        String type = fieldType;
        parameter = parameter.replace("[","");
        parameter = parameter.replace("]","");
        String[] constructorParameters = parameter.split(",");
        Position position = new Position(x, y);
        switch(type)
        {
            case ("c") -> {
                int speed = Integer.parseInt(constructorParameters[0]);
                ArrayList<Direction> directions = new ArrayList<>();
                for (int i = 1; i<constructorParameters.length; i++)
                {
                    directions.add(generateDirection(constructorParameters[i]));
                }
            return fieldGenerator.generateConveyorBelt(position,directions,speed);
            }
            case ("e") -> {
                int cubes = Integer.parseInt(constructorParameters[0]);
                return fieldGenerator.generateEnergySpaces(position, cubes);
            }
            case ("w") -> {
                ArrayList<Direction> directions = new ArrayList<>();
                boolean[] wallOrientation = {false, false, false, false};
                for (String constructorParameter : constructorParameters)
                {
                    directions.add(generateDirection(constructorParameter));
                    Direction direction = directions.get(directions.size()-1);
                    switch (direction.getDirectionString())
                    {
                        case "top" -> wallOrientation[0] = true;
                        case "right" -> wallOrientation[1] = true;
                        case "bottom" -> wallOrientation[2] = true;
                        case "left" -> wallOrientation[3] = true;
                    }
                }
                return fieldGenerator.generateWall(position,new WallOrientation(wallOrientation[0], wallOrientation[1], wallOrientation[2], wallOrientation[3]));
            }
            case ("!") -> {
                int number = Integer.parseInt(constructorParameters[0]);
                return fieldGenerator.generateCheckpoint(position,number);
            }
            case ("r") -> {
                Direction direction = generateDirection(constructorParameters[0]);
                return fieldGenerator.generateRebootToken(position,direction);
            }
            case "a" -> {
                Direction direction = generateDirection(constructorParameters[0]);
                ArrayList<String> orientation = new ArrayList<>();
                orientation.add(direction.getDirectionString());
                return fieldGenerator.generateAntenna(position, orientation);
            }
            case ("-") -> {
                int laserNum = Integer.parseInt(constructorParameters[0]);
                return fieldGenerator.generateBoardLaser(position, laserNum);
            }
            case "g" ->
            {
                if(constructorParameters[0].equals(">"))
                {
                    return fieldGenerator.generateGears(position,true);
                }
                else return fieldGenerator.generateGears(position,false);
            }
            case "|" ->
            {
                ArrayList<Integer> registers = new ArrayList<>();
                for (int i = 0; i<constructorParameters.length; i++)
                {
                    registers.add(Integer.parseInt(constructorParameters[i]));
                }
                return fieldGenerator.generatePushPanel(position,registers);
            }
        }
        return null;
    }
    
    public FieldList identifyFields(String parameter, int x, int y)
    {
        FieldList fieldList = new FieldList();
        int index = 0;
        while (index < parameter.length())
        {
            String characterAtIndex = String.valueOf(parameter.charAt(index));
            if(index+1 < parameter.length())
            {
                if (String.valueOf(parameter.charAt(index + 1)).equals("("))
                {
                    String fieldWithParameter = getInterval(parameter, index, "(", ")");
                    index = getIndexOfEndsymbol(parameter, index, "(", ")");
                    fieldList.addField(identifyFieldWithParameter(characterAtIndex,fieldWithParameter, x, y));
                }
                else
                {
                    String currentCharacter = String.valueOf(parameter.charAt(index));
                    fieldList.addField(identifyField(currentCharacter, x,  y));
                }
            }
            else
            {
                String currentCharacter = String.valueOf(parameter.charAt(index));
                fieldList.addField(identifyField(currentCharacter, x,  y));
            }
            index++;
        }
        return fieldList;
    }
    
    private String getInterval(String fullLine, int startIndex, String startSymbol, String endSymbol)
    {
        String line = fullLine.substring(startIndex);
        int newIndex = line.indexOf(startSymbol)+1;
        int indexOfClosedBracket = line.indexOf(endSymbol, newIndex);
        return line.substring(newIndex,indexOfClosedBracket);
    }
    
    private int getIndexOfEndsymbol(String fullLine, int startIndex, String startSymbol, String endSymbol)
    {
        String line = fullLine.substring(startIndex);
        int newIndex = line.indexOf(startSymbol)+1;
        return line.indexOf(endSymbol, newIndex)+(fullLine.length()-line.length());
    }
    
    private Direction generateDirection(String directionLetter)
    {
        switch (directionLetter)
        {
            case "t" -> {
            return new Direction("top");
            }
            case "r" -> {
                return new Direction("right");
            }
            case "b" -> {
                return new Direction("bottom");
            }
            case "l" -> {
                return new Direction("left");
            }
        }
        return null;
    }
    
    public String readFromFile(String fileName)
    {
        StringBuilder abstractedMap = new StringBuilder();
        InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
        Scanner sc = new Scanner(in);
        while (sc.hasNextLine())
        {
            abstractedMap.append(sc.nextLine()).append("\n");
        }
        return abstractedMap.toString();
    }
}
