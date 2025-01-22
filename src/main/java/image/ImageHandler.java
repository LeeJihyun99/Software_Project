package image;

import field.tools.WallOrientation;

import java.util.ArrayList;

/**
 * Klasse, in welcher alle Images generiert werden können
 * @author DavidKulbe
 */
public class ImageHandler {
    //Felder
    public Image generateAntenna()
    {
        return new Image();
    }
    public Image generateBoardLaser()
    {
        return new Image();
    }
    public Image generateBoardLaserEnd()
    {
        return new Image();
    }
    public Image generateCheckpoint()
    {
        return new Image();
    }
    public Image generateConveyorBelt(int speed)
    {
        if(speed == 1) {return new Image();}
        else return new Image();
    }
    public Image generateDefaultField()
    {
        return new Image();
    }
    public Image generateEnergySpaces()
    {
        return new Image();
    }
    public Image generateGears()
    {
        return new Image();
    }
    public Image generatePit()
    {
        return new Image();
    }
    public Image generatePushPanel(ArrayList<Integer> regNumbers)
    {
        return new Image();
    }
    public Image generateRebootToken()
    {
        return new Image();
    }
    public Image generateWall(WallOrientation wallOrientation)
    {
        return new Image();
    }
    
    public Image generateStartField()
    {
        return new Image();
    }

    //Robot
    public Image generateRobot()
    {
        return new Image();
    }

    //Cards
}
