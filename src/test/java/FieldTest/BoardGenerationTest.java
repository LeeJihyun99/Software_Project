package FieldTest;

import field.CheckPoint;
import field.ConveyorBelt;
import field.Field;
import field.RebootToken;
import field.reducedFields.ReducedCheckPoint;
import field.reducedFields.ReducedConveyorBelt;
import field.reducedFields.ReducedField;
import field.reducedFields.ReducedLaser;
import field.tools.BoardGenerator;
import field.tools.Direction;
import field.tools.Gameboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class BoardGenerationTest
{
    
    protected static final Logger logger = LogManager.getLogger(BoardGenerationTest.class);
    @Test
    public void testInterval()
    {
        String line = "s3.c(2,d).{w(l)|(4)}{w(r)-(4)}.{w(d)|(3)}.c(2,t).";
        int index = line.indexOf("(")+1;
        int indexOfClosedBracket = line.indexOf(")", index);
        String fields = line.substring(index,indexOfClosedBracket);
        index = indexOfClosedBracket;
        assertEquals("l", getInterval(line, 8, "(", ")"));
        assertEquals(line.indexOf("l")+1, getIndexAfterInterval(line, 8, "(", ")"));
    }
    
    @Test
    public void testConvertGameboardToList()
    {
        Gameboard dizzyHighway = new BoardGenerator().generateDizzyHighway();
        ArrayList<ArrayList<ArrayList<ReducedField>>> convertedReducedList = dizzyHighway.convertToReducedList();
        Gameboard lostBearings = new BoardGenerator().generateLostBearings();
        convertedReducedList = dizzyHighway.convertToReducedList();
        Gameboard deathTrap = new BoardGenerator().generateDeathTrap();
        convertedReducedList = dizzyHighway.convertToReducedList();
        Gameboard extraCrispy = new BoardGenerator().generateExtraCrispy();
        convertedReducedList = dizzyHighway.convertToReducedList();
    }
    
    @Test
    public void containsConveyorBelt()
    {
        Gameboard dizzyHighway = new BoardGenerator().generateDizzyHighway();
        boolean containsConveyor = false;
        for (Field field: dizzyHighway.getBoard()[0][5].getFields())
        {
            if (field instanceof ConveyorBelt)
            {
                containsConveyor = true;
                break;
            }
        }
        assertTrue(""+containsConveyor, containsConveyor);
    }
    
    @Test
    public void checkDirectionOfConveyorBelt()
    {
        Gameboard dizzyHighway = new BoardGenerator().generateDizzyHighway();
        ConveyorBelt conveyorBelt = null;
        ArrayList <String> checkOut = new ArrayList<>();
        ArrayList <String> checkIn = new ArrayList<>();
        int x = 4;
        int y = 7;
        checkOut.add("bottom");
        checkIn.add("top");
        checkIn.add("left");
        for (Field field: dizzyHighway.getBoard()[y][x].getFields())
        {
            if (field instanceof ConveyorBelt)
            {
                conveyorBelt = (ConveyorBelt) field;
                break;
            }
        }
        assertEquals(conveyorBelt.getFlow_direction_out().getDirectionString(), checkOut.get(0));
        assertEquals(conveyorBelt.getFlow_directions_in().get(0).getDirectionString(), checkIn.get(0));
        assertEquals(conveyorBelt.getFlow_directions_in().get(1).getDirectionString(), checkIn.get(1));
    
        conveyorBelt = null;
        checkOut.clear();
        checkIn.clear();
        x = 11;
        y = 2;
        checkOut.add("top");
        checkIn.add("bottom");
        checkIn.add("right");
        for (Field field: dizzyHighway.getBoard()[y][x].getFields())
        {
            if (field instanceof ConveyorBelt)
            {
                conveyorBelt = (ConveyorBelt) field;
                break;
            }
        }
        assertEquals(conveyorBelt.getFlow_direction_out().getDirectionString(), checkOut.get(0));
        assertEquals(conveyorBelt.getFlow_directions_in().get(0).getDirectionString(), checkIn.get(0));
        assertEquals(conveyorBelt.getFlow_directions_in().get(1).getDirectionString(), checkIn.get(1));
        
    }
    
    @Test
    public void testLaserDirection()
    {
        Gameboard dizzyHighway = new BoardGenerator().generateDizzyHighway();
        Gameboard extraCrsipy = new BoardGenerator().generateExtraCrispy();
        
        ArrayList<ArrayList<ArrayList<ReducedField>>> reducedListHighway = dizzyHighway.convertToReducedList();
        ArrayList<ArrayList<ArrayList<ReducedField>>> reducedListCrispy = extraCrsipy.convertToReducedList();
        
        for(ReducedField rf: reducedListHighway.get(6).get(4))
        {
            if (rf instanceof ReducedLaser laser)
            {
                System.out.println(laser.getOrientations().get(0));
            }
        }
    
        for(ReducedField rf: reducedListCrispy.get(5).get(7))
        {
            if (rf instanceof ReducedLaser laser)
            {
                System.out.println(laser.getOrientations().get(0));
            }
        }
    }
    
    @Test
    public void checkDirectionOfConveyorBeltInList()
    {
        Gameboard dizzyHighway = new BoardGenerator().generateDizzyHighway();
        ArrayList<ArrayList<ArrayList<ReducedField>>> list = dizzyHighway.convertToReducedList();
        ReducedConveyorBelt conveyorBelt = null;
        ArrayList <String> checkOut = new ArrayList<>();
        ArrayList <String> checkIn = new ArrayList<>();
        int x = 4;
        int y = 7;
        checkOut.add("bottom");
        checkIn.add("top");
        checkIn.add("left");
        for (ReducedField field: list.get(x).get(y))
        {
            if (field instanceof ReducedConveyorBelt)
            {
                conveyorBelt = (ReducedConveyorBelt) field;
                break;
            }
        }
        assertEquals(conveyorBelt.getOrientations().get(0), checkOut.get(0));
        assertEquals(conveyorBelt.getOrientations().get(1), checkIn.get(0));
        assertEquals(conveyorBelt.getOrientations().get(2), checkIn.get(1));
    }
    @Test
    public void testBoardGenerationDizzyHighway()
    {
        BoardGenerator boardGenerator = new BoardGenerator();
        Gameboard gameboard = boardGenerator.generateDizzyHighway();
        gameboard.convertToReducedList();
        assertNotNull(gameboard);
    }
    @Test
    public void testBoardGenerationLostBearings()
    {
        BoardGenerator boardGenerator = new BoardGenerator();
        Gameboard gameboard = boardGenerator.generateLostBearings();
        gameboard.convertToReducedList();
        assertNotNull(gameboard);
    }
    @Test
    public void testBoardGenerationExtraCrispy()
    {
        BoardGenerator boardGenerator = new BoardGenerator();
        Gameboard gameboard = boardGenerator.generateExtraCrispy();
        gameboard.convertToReducedList();
        assertNotNull(gameboard);
    }
    @Test
    public void testBoardGenerationDeathTrap()
    {
        BoardGenerator boardGenerator = new BoardGenerator();
        Gameboard gameboard = boardGenerator.generateDeathTrap();
        gameboard.convertToReducedList();
        assertNotNull(gameboard);
    }

    @Test
    public void testBoardGenerationTwister()
    {
        BoardGenerator boardGenerator = new BoardGenerator();
        Gameboard gameboard = boardGenerator.generateTwister();
        gameboard.convertToReducedList();
        assertNotNull(gameboard);
    }
    
    @Test
    public void testBoardGenerationFromFile()
    {
        BoardGenerator boardGenerator = new BoardGenerator();
        String abstractedDizzyHighway = boardGenerator.readFromFile("maps/DizzyHighway.txt");
        assertNotEquals(abstractedDizzyHighway, "");
        Gameboard gameboard = boardGenerator.generateGameboardFromString(abstractedDizzyHighway);
        assertNotNull(gameboard);
    }
    
    @Test
    public void mapContainsCheckpoint()
    {
        BoardGenerator boardGenerator = new BoardGenerator();
        Gameboard gameboard = boardGenerator.generateDizzyHighway();
        boolean checkpointInBoard = false;
        boolean checkpointInList = false;
        ArrayList<CheckPoint> checkpoints = new ArrayList<>();
        for(int x = 0; x<gameboard.getColumnCount();x++)
        {
            for(int y = 0; y<gameboard.getRowCount();y++)
            {
                for (Field field: gameboard.getBoard()[y][x].getFields())
                {
                    if (field instanceof CheckPoint)
                    {
                        checkpointInBoard = true;
                        checkpoints.add((CheckPoint) field);
                    }
                }
            }
        }
        for (Field checkpoint: checkpoints)
        {
            System.out.println("x:" + checkpoint.getPosition().x() + " y:" + checkpoint.getPosition().y());
        }
        assertTrue(checkpointInBoard);
    
        ArrayList<ArrayList<ArrayList<ReducedField>>> list = gameboard.convertToReducedList();
        for(int x = 0; x<gameboard.getColumnCount();x++)
        {
            for(int y = 0; y<gameboard.getRowCount();y++)
            {
                for (ReducedField field : list.get(x).get(y))
                {
                    if (field instanceof ReducedCheckPoint)
                    {
                        checkpointInList = true;
                        break;
                    }
                }
            }
        }
        assertTrue(checkpointInList);
    }
    
    @Test
    public void testAvailableStartingPoints()
    {
        Gameboard dizzyHighway = new BoardGenerator().generateDizzyHighway();
        System.out.println(dizzyHighway.getPositionsOfAvailableStartingPoints());
        assertTrue(dizzyHighway.getPositionsOfAvailableStartingPoints().size() > 0);
    }
    
    @Test
    public void rebootTokens()
    {
        BoardGenerator boardGenerator = new BoardGenerator();
        ArrayList<RebootToken> rebootTokens = new ArrayList<>();
        ArrayList<Gameboard> boards = new ArrayList<>();
        boards.add(boardGenerator.generateDizzyHighway());
        boards.add(boardGenerator.generateExtraCrispy());
        boards.add(boardGenerator.generateLostBearings());
        boards.add(boardGenerator.generateDeathTrap());
        boards.add(boardGenerator.generateTwister());
        
        for (Gameboard gameboard: boards)
        {
            rebootTokens.add(gameboard.getRebootToken().get(0));
        }
        for (RebootToken token: rebootTokens)
        {
            System.out.println(token.getPosition());
        }
    }
    
    @Test
    public void checkPoints()
    {
        BoardGenerator boardGenerator = new BoardGenerator();
        ArrayList<CheckPoint> rebootTokens = new ArrayList<>();
        ArrayList<Gameboard> boards = new ArrayList<>();
        boards.add(boardGenerator.generateDizzyHighway());
        boards.add(boardGenerator.generateExtraCrispy());
        boards.add(boardGenerator.generateLostBearings());
        boards.add(boardGenerator.generateDeathTrap());
        boards.add(boardGenerator.generateTwister());
        
        for (Gameboard gameboard: boards)
        {
            rebootTokens.addAll(gameboard.getCheckPoints());
        }
        for (CheckPoint point: rebootTokens)
        {
            System.out.println(point.getBoardID() + " " + point.getCheckNum());
        }
    }
    
    private String getInterval(String fullLine, int startIndex, String startSymbol, String endSymbol)
    {
        String line = fullLine.substring(startIndex);
        int newIndex = line.indexOf(startSymbol)+1;
        int indexOfClosedBracket = line.indexOf(endSymbol, newIndex);
        return line.substring(newIndex,indexOfClosedBracket);
    }
    
    private int getIndexAfterInterval(String fullLine, int startIndex, String startSymbol, String endSymbol)
    {
        String line = fullLine.substring(startIndex);
        int newIndex = line.indexOf(startSymbol)+1;
        return line.indexOf(endSymbol, newIndex)+(fullLine.length()-line.length());
    }
}
