package game;

import card.Card;
import card.damage.DamageCard;
import card.damage.Spam;
import card.damage.Virus;
import field.*;
import field.tools.ActionOnLanding;
import field.tools.Direction;
import field.tools.FieldList;
import field.tools.Position;
import server.Server;
import server.protocol.Message;
import server.protocol.aktionen.*;
import server.protocol.chatnachrichten.Error;
import tools.ServerLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * @author David, Melli
 */
public class Robot {

    private int amountCubes;
    private Card[] register = new Card[5];
    private Card[] upgradesPerm = new Card[3];
    private Card[] upgradesTemp = new Card[3];
    private Card[] installedUpgrades = new Card[3];
    private int checkpointToken;
    private Direction lookDir;
    private Player robotPlayer;

    private List<CheckPoint> checkpointsVisited = new ArrayList<>();
    private Position position;
    private DamageCard damageCardRecieved;
    private List<Card> discardPile = new ArrayList<>();
    private Game currentGame;
    private int figure;
    private Movement movementMsg;
    private PlayerTurning playerTurningMsg;
    private DrawDamage drawDamageMsg;
    // private PickDamage pickDamageMsg;
    // private SelectedDamage selectedDamageMsg;
    private Reboot rebootMsg;
    private RebootDirection rebootDirectionMsg;
    private Error errorMsg;

    private ArrayList<String> damageCardName;
    
    private boolean rebootetThisRound = false;
    
    private String currentCourseId;
    
    private Position chosenStartingPointPosition;
    
    private int delay = 500; //delay after movement

    private int currentRegister;
    private CountDownLatch waitForDamage;
    private CountDownLatch waitForReboot;

    private Logger logger = ServerLogger.getLogger();


    public Robot(int amountCubes, int checkpointToken, Direction lookDir, Player robotPlayer) {
        this.amountCubes = amountCubes;
        this.checkpointToken = checkpointToken;
        this.lookDir = lookDir;
        this.robotPlayer = robotPlayer;
        currentGame = this.getRobotPlayer().getCurrentGame();
        this.checkpointsVisited = new ArrayList<>();
    }

    public Robot(int figure) {
        this.figure = figure;
        this.amountCubes = 5;
        this.checkpointToken = 0;
        this.checkpointsVisited = new ArrayList<>();

    }

    /**
     * @author Melanie
     * activate a Robots Laser to shoot other rotbots in its path
     */
    public void activateLaser(Direction direction){
        List<Robot> attackedRobots;
        int pathOfDirectionX = 0 ;
        int pathOfDirectionY = 0 ;
        switch (direction.getDirectionInteger()){
            case 0: pathOfDirectionY = this.getPosition().y();break;
            case 1: pathOfDirectionX = this.getCurrentGame().getGameboard().getColumnCount() - this.getPosition().x();break;
            case 2: pathOfDirectionY = this.getCurrentGame().getGameboard().getRowCount() - this.getPosition().y();break;
            case 3: pathOfDirectionX = this.getPosition().x();break;
        }

        if(direction.getDirectionInteger() == 1 || direction.getDirectionInteger() == 3){
            logger.info(this.robotPlayer.getPlayerID()+" looks In X Richtung distanz: "+pathOfDirectionX);
            scanAndAttack(direction,pathOfDirectionX);
        }else if(direction.getDirectionInteger() == 2 || direction.getDirectionInteger() == 0){
            logger.info(this.robotPlayer.getPlayerID()+" looks In y-Richtung distanz: "+pathOfDirectionY);
            scanAndAttack(direction,pathOfDirectionY);
        }
    }

    /**
     * Searches robots in sight and attacks them
     * @author Melli, David
     */
    public void scanAndAttack(Direction lookingDirection, int distance){
        List<Robot> attackedRobots = new ArrayList<>();
        switch (lookingDirection.getDirectionInteger()){
            case 0:
                for(int i=0; i <= distance;i++){
                    FieldList fieldToCheck = getCurrentGame().getGameboard().getFieldsAtPosition(new Position(this.position.x(),this.position.y()-i));
                    if(fieldToCheck.isOccupied() && fieldToCheck.getCurrentRobot() != this){
                        if (fieldToCheck.hasWall())
                        {
                            if (fieldToCheck.getWall().getWallOrientation().bottom())
                            {
                                logger.info("Wand im Weg");
                                return;
                            }
                        }else{
                            logger.info("Robot gefunden");
                            attackedRobots.add(fieldToCheck.getCurrentRobot());
                            doAttackOnRobot(attackedRobots);
                            return;
                        }
                    }else{
                        for(Field field : fieldToCheck.getFields()){
                            if(field instanceof Wall){
                                if(((Wall) field).getWallOrientation().top() || ((Wall) field).getWallOrientation().bottom()){
                                    logger.info("Wand im Weg (ohne Roboter auf Feld)");
                                    return;
                                }else if(!field.getPassThrough()){
                                    logger.info("Antenne im Weg");
                                    return;
                                }
                            }
                        }
                    }
                }break;
            case 1:
                for(int i=0; i < distance;i++){
                    FieldList fieldToCheck = getCurrentGame().getGameboard().getFieldsAtPosition(new Position(this.position.x()+i,this.position.y()));
                    if(fieldToCheck.isOccupied() && fieldToCheck.getCurrentRobot() != this){
                        if (fieldToCheck.hasWall())
                        {
                            if (fieldToCheck.getWall().getWallOrientation().left())
                            {
                                logger.info("Wand im Weg");
                                return;
                            }
                        }else{
                            logger.info("Robot gefunden");
                            attackedRobots.add(fieldToCheck.getCurrentRobot());
                            doAttackOnRobot(attackedRobots);
                            return;
                        }
                    }else{
                        for(Field field : fieldToCheck.getFields()){
                            if(field instanceof Wall){
                                if(((Wall) field).getWallOrientation().right() || ((Wall) field).getWallOrientation().left()){
                                    logger.info("Wand im Weg (ohne Roboter auf Feld)");
                                    return;
                                }else if(!field.getPassThrough()){
                                    logger.info("Antenne im Weg");
                                    return;
                                }
                            }
                        }
                    }
                }break;
            case 2:
                for(int i=1; i < distance;i++){
                FieldList fieldToCheck = getCurrentGame().getGameboard().getFieldsAtPosition(new Position(this.position.x(),this.position.y()+i));
                if(fieldToCheck.isOccupied() && fieldToCheck.getCurrentRobot() != this){
                    if (fieldToCheck.hasWall())
                    {
                        if (fieldToCheck.getWall().getWallOrientation().top())
                        {
                            logger.info("Wand im Weg");
                            return;
                        }
                    }else{
                            logger.info("Robot gefunden");
                            attackedRobots.add(fieldToCheck.getCurrentRobot());
                            doAttackOnRobot(attackedRobots);
                            return;
                        }
                    }
                else{
                    for(Field field : fieldToCheck.getFields()){
                        if(field instanceof Wall){
                            if(((Wall) field).getWallOrientation().top() || ((Wall) field).getWallOrientation().bottom()){
                                logger.info("Wand im Weg (ohne Roboter auf Feld)");
                                return;
                            }else if(!field.getPassThrough()){
                                logger.info("Antenne im Weg");
                                return;
                            }
                        }
                    }
                }
            }break;
            case 3:
                for(int i=0; i <= distance;i++){
                FieldList fieldToCheck = getCurrentGame().getGameboard().getFieldsAtPosition(new Position(this.position.x()-i,this.position.y()));
                if(fieldToCheck.isOccupied() && fieldToCheck.getCurrentRobot() != this){
                    if (fieldToCheck.hasWall())
                    {
                        if (fieldToCheck.getWall().getWallOrientation().top())
                        {
                            logger.info("Wand im Weg");
                            return;
                        }
                    }else{
                        logger.info("Robot gefunden");
                        attackedRobots.add(fieldToCheck.getCurrentRobot());
                        doAttackOnRobot(attackedRobots);
                        return;
                    }
                }else{
                    for(Field field : fieldToCheck.getFields()){
                        if(field instanceof Wall){
                            if(((Wall) field).getWallOrientation().right() || ((Wall) field).getWallOrientation().left()){
                                logger.info("Wand im Weg (ohne Roboter auf Feld)");
                                return;
                            }else if(!field.getPassThrough()){
                                logger.info("Antenne im Weg");
                                return;
                            }
                        }
                    }
                }
            }break;
            default: logger.warning("Received Wrong Orientation or null! Value: "+lookingDirection.getDirectionString());
        }
    }
    
    /**
     * Scannt nach Robotern und schießt auf diese, falls welche gefunden werden konnten
     * @param pathOfDirection Länge der Richtung
     * @author Melli, David
     */
    private void scanAndAttack(int pathOfDirection, Direction direction)
    {
        List<Robot> attackedRobots;
        if(!this.isPathFree(position, 0, pathOfDirection, direction)){
            attackedRobots = scanForRobotLine(pathOfDirection,direction);
            ArrayList<Wall> wallsInPath;
            wallsInPath = this.getWallsInPath(pathOfDirection,direction);
            logger.info("Blickrichtung: "+direction.getDirectionInteger());
            switch (direction.getDirectionInteger()){
                case 0:
                    logger.info("Wände in Blickrichtung: "+wallsInPath.size()+ " gefundene Robots: "+attackedRobots.size());
                    if(wallsInPath.size() == 0 && attackedRobots.size() > 0){
                        logger.info(this.figure+": Laser activated!");
                        doAttackOnRobot(attackedRobots);
                    }else if(wallsInPath.size() > 0 && attackedRobots.size() > 0){
                        for (int i = 0; i < wallsInPath.size(); i++) {
                            if (wallsInPath.get(i).getPosition().y() < attackedRobots.get(0).getPosition().y() && wallsInPath.get(i).getOrientationsAsString().equals(this.getLookDir().getDirectionString())) {
                                logger.info(this.figure+": Laser activated!");
                                doAttackOnRobot(attackedRobots);
                            }else{
                               logger.info("Wand im Weg");
                            }
                        }
                    }else{
                        logger.info("Keine Roboter gefunden");
                    }
                break;
                case 1:
                    logger.info("Wände in Blickrichtung: "+wallsInPath.size()+ " gefundene Robots: "+attackedRobots.size());
                    if(wallsInPath.size() == 0 && attackedRobots.size() > 0){
                        logger.info(this.figure+": Laser activated!");
                        doAttackOnRobot(attackedRobots);
                    }else if(attackedRobots.size() > 0 && wallsInPath.size() > 0){
                        for(int i=0; i< wallsInPath.size();i++){
                            if(wallsInPath.get(i).getPosition().x() > attackedRobots.get(0).getPosition().x() && wallsInPath.get(i).getOrientationsAsString().equals(this.getLookDir().getDirectionString())){
                                logger.info(this.figure+": Laser activated!");
                                doAttackOnRobot(attackedRobots);
                            }else{
                                logger.info("Wand im Weg");
                            }
                        }
                    }else{
                        logger.info("Kein Robot gefunden");
                    }

                break;
                case 2:
                    logger.info("Wände in Blickrichtung: "+wallsInPath.size()+ " gefundene Robots: "+attackedRobots.size());
                    if(wallsInPath.size() == 0 && attackedRobots.size() > 0){
                        logger.info(this.figure+": Laser activated!");
                        doAttackOnRobot(attackedRobots);
                    }else if(attackedRobots.size() > 0 && wallsInPath.size() > 0){
                        for(int i=0; i< wallsInPath.size();i++){
                            if(wallsInPath.get(i).getPosition().y() > attackedRobots.get(0).getPosition().y() && wallsInPath.get(i).getOrientationsAsString().equals(this.getLookDir().getDirectionString())){
                                logger.info(this.figure+": Laser activated!");
                                doAttackOnRobot(attackedRobots);
                            }else{
                                logger.info("Wand im Weg");
                            }
                        }
                    }else{
                        logger.info("Kein Robot gefunden");
                    }
                    break;
                case 3:
                    logger.info("Wände in Blickrichtung: "+wallsInPath.size()+ " gefundene Robots: "+attackedRobots.size());
                    if(wallsInPath.size() == 0 && attackedRobots.size() > 0){
                        logger.info(this.figure+": Laser activated!");
                        doAttackOnRobot(attackedRobots);
                    }else if(attackedRobots.size() > 0 && wallsInPath.size() > 0){
                        for(int i=0; i< wallsInPath.size();i++){
                            if(wallsInPath.get(i).getPosition().x() < attackedRobots.get(0).getPosition().x() && wallsInPath.get(i).getOrientationsAsString().equals(this.getLookDir().getDirectionString())){
                                logger.info(this.figure+": Laser activated!");
                                doAttackOnRobot(attackedRobots);
                            }else{
                                logger.info("Wand im Weg");
                            }
                        }
                    }else{
                        logger.info("Kein Robot gefunden");
                    }
                    break;
                default: logger.warning("Ungültige Direction oder null");
            }
        }
    }

    public void doAttackOnRobot(List<Robot> attackedRobots){
        if(!attackedRobots.isEmpty())
        {
            attackedRobots.get(0).takeDamage(1);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    
    /**
     * @author: Melanie
     * Ziehe eine Spamkarte, um Schaden zu erhalten
     */
    public void takeDamage(int amount){
        damageCardName = new ArrayList<>();
        if(getCurrentGame().getSpamCardStack().size() >= amount){
            for(int i = 0; i < amount;i++){
                damageCardRecieved = this.getCurrentGame().getSpamCardStack().get(this.getCurrentGame().getSpamCardStack().size()-1);
                this.discardPile.add(damageCardRecieved);
                this.currentGame.getSpamCardStack().remove(this.getCurrentGame().getSpamCardStack().size()-1);
                damageCardName.add(damageCardRecieved.getCardName());
            }
            drawDamageMsg = new DrawDamage(this.getRobotPlayer().getPlayerID(),damageCardName);
            currentGame.gameServer.broadcast(drawDamageMsg);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }else{
            selectMessageForDamage(amount);
        }
    }

    public synchronized void selectMessageForDamage(int amount){
        ArrayList<String> damageCards = new ArrayList<>();

        if(this.getRobotPlayer().getIsAi()){ //wenn AI ohne PickDamage Message

            if(this.getCurrentGame().getVirusCardStack().size() >= amount){
                takeDifferentDamage("Virus");
            }else if(this.getCurrentGame().getWormCardStack().size() >= amount){
                takeDifferentDamage("Worm");
            }else if(this.getCurrentGame().getTrojanHorseCardsStack().size() >= amount){
                takeDifferentDamage("Trojan");
            }

        }else{
            if(this.getCurrentGame().getVirusCardStack().size() >= amount){
                if(damageCards.add("Virus")){
                    logger.info("Virus hinzugefügt");
                }
            }
            if(this.getCurrentGame().getWormCardStack().size() >= amount){
                if(damageCards.add("Worm")){
                    logger.info("Worm hinzugefügt");
                }
            }
            if(this.getCurrentGame().getTrojanHorseCardsStack().size() >= amount){
                if(damageCards.add("Trojan")){
                    logger.info("Trojan hinzugefügt");
                }
            }
            PickDamage pickDamage = new PickDamage(amount,damageCards);
            robotPlayer.getServerThread().sendMessageSerialized(pickDamage);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            waitForDamage = new CountDownLatch(1);

                try {
                    waitForDamage.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }



            // getGameServer().pauseGame(this.getRobotPlayer().getServerThread());
        }

    }

    /**
     * @author Melanie
     * @param cardName
     */
    public void takeDifferentDamage(String cardName){
        damageCardName = new ArrayList<>();
        switch (cardName){
            case "Worm":
                damageCardRecieved = this.getCurrentGame().getWormCardStack().get(this.getCurrentGame().getWormCardStack().size()-1);
                this.discardPile.add(damageCardRecieved);
                this.currentGame.getWormCardStack().remove(this.getCurrentGame().getWormCardStack().size()-1);
                damageCardName.add(damageCardRecieved.getCardName());
                drawDamageMsg = new DrawDamage(this.getRobotPlayer().getPlayerID(),damageCardName);
                currentGame.gameServer.broadcast(drawDamageMsg);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "Virus":
                damageCardRecieved = this.getCurrentGame().getVirusCardStack().get(this.getCurrentGame().getVirusCardStack().size()-1);
                this.discardPile.add(damageCardRecieved);
                this.currentGame.getVirusCardStack().remove(this.getCurrentGame().getVirusCardStack().size()-1);
                damageCardName.add(damageCardRecieved.getCardName());
                drawDamageMsg = new DrawDamage(this.getRobotPlayer().getPlayerID(),damageCardName);
                currentGame.gameServer.broadcast(drawDamageMsg);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;

            case "Trojan":
                damageCardRecieved = this.getCurrentGame().getTrojanHorseCardsStack().get(this.getCurrentGame().getTrojanHorseCardsStack().size()-1);
                this.discardPile.add(damageCardRecieved);
                this.currentGame.getTrojanHorseCardsStack().remove(this.getCurrentGame().getTrojanHorseCardsStack().size()-1);
                damageCardName.add(damageCardRecieved.getCardName());
                drawDamageMsg = new DrawDamage(this.getRobotPlayer().getPlayerID(),damageCardName);
                currentGame.gameServer.broadcast(drawDamageMsg);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
        }

    }

    public void reboot(){
        leaveField();
        takeDamage(2);


        for(Card c : this.getRegister()){
            this.discardPile.add(c);
        }
        String courseID = currentCourseId; //nötig, da sich current courseID beim reboot verändert
        if (this.getCurrentGame().getGameboard().isKeyForTokens(courseID)) //rebootet auf einem Token
        {
            FieldList fieldsWithRebootToken = this.getCurrentGame().getGameboard().getFieldsAtPosition(this.getCurrentGame().getGameboard().getRebootTokenOnBoard(courseID).getPosition());
            if (fieldsWithRebootToken.isOccupied())
            {
                this.pushInDirection(fieldsWithRebootToken.getCurrentRobot(),
                        this.getCurrentGame().getGameboard().getRebootTokenOnBoard(courseID).getDirection(), 1);
            }
            this.occupyField(this.getCurrentGame().getGameboard().getRebootTokenOnBoard(courseID).getPosition());
        }
        else //rebootet beim Startfeld
        {
            if (getCurrentGame().getGameboard().getFieldsAtPosition(chosenStartingPointPosition).isOccupied())
            {
                this.pushInDirection(getCurrentGame().getGameboard().getFieldsAtPosition(chosenStartingPointPosition).getCurrentRobot(),
                        lookDir, 1);
            }
            this.occupyField(chosenStartingPointPosition);
        }
        rebootetThisRound = true;

        rebootMsg = new Reboot(this.getRobotPlayer().getPlayerID());
        movementMsg = new Movement(this.getRobotPlayer().getPlayerID(), position.x(), position.y());
        this.getGameServer().broadcast(movementMsg);

        this.getGameServer().broadcast(rebootMsg);
        try
        {
            Thread.sleep(1000);
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    
        if (!robotPlayer.getIsAi())
        {
            waitForReboot = new CountDownLatch(1);
            try
            {
                waitForReboot.await();
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * @author Melanie
     * @param distance
     * @param direction
     * @return
     */
    public List<Robot> scanForRobotLine(int distance, Direction direction){
        List<Robot> foundRobots = new ArrayList<>();
        FieldList checkFields;
        Position start = this.getPosition();
        Position check;
        int maxDistance = 0;
        if(direction.getDirectionInteger() == 1 || direction.getDirectionInteger() == 3){
            maxDistance = this.getCurrentGame().getGameboard().getColumnCount();
        }else if(direction.getDirectionInteger() == 2 || direction.getDirectionInteger() == 0){
            maxDistance = this.getCurrentGame().getGameboard().getRowCount();
        }

        //Distanz kontrollieren
        if(this.lookDir.getDirectionInteger() == 1 ){
            if(start.x() + distance >= maxDistance){
                int toMuch = (start.x()+distance)-maxDistance;
                distance = distance-toMuch-1;
            }
        }else if(this.lookDir.getDirectionInteger() == 3) {
            if (start.x() - distance < 0) {
                int toMuch = (start.x() - distance);
                distance = distance + toMuch;
            }
        }else if(this.lookDir.getDirectionInteger()==2) {
            if(start.y() + distance >= maxDistance){
                int toMuch = (start.y()+distance)-maxDistance;
                distance = distance-toMuch-1;
            }
        }else{
            if(start.y() - distance < 0){
                int toMuch = (start.y()-distance);
                distance = distance+toMuch;
            }
        }

        switch (direction.getDirectionInteger()){
                case 1:
                    for(int i = 1; i <= distance; i++){
                    check = new Position(start.x()+i, start.y());
                    checkFields = this.getCurrentGame().getGameboard().getFieldsAtPosition(check);
                    if (checkFields.isOccupied()) {
                        foundRobots.add(checkFields.getCurrentRobot());
                    }
                    }break;
                case 2:
                    for(int i = 1; i <= distance; i++){
                        check = new Position(start.x(), start.y()+i);
                        checkFields = this.getCurrentGame().getGameboard().getFieldsAtPosition(check);
                        if (checkFields.isOccupied()) {
                            foundRobots.add(checkFields.getCurrentRobot());
                        }
                    }break;
                case 3:
                    for(int i = 1; i <= distance; i++){
                        check = new Position(start.x()-i, start.y());
                        checkFields = this.getCurrentGame().getGameboard().getFieldsAtPosition(check);
                        if (checkFields.isOccupied()) {
                            foundRobots.add(checkFields.getCurrentRobot());
                        }
                    }break;
                case 0:
                    for(int i = 1; i <= distance; i++){
                        check = new Position(start.x(), start.y()-i);
                        checkFields = this.getCurrentGame().getGameboard().getFieldsAtPosition(check);
                        if (checkFields.isOccupied()) {
                            foundRobots.add(checkFields.getCurrentRobot());
                        }
                    }break;
            }
        return foundRobots;
    }

    /**
     * @author Melanie
     * @param radius
     * @return
     */
    public List<Robot> scanForRobotRadius(int radius){
        List<Robot> foundRobots = new ArrayList<>();
        Position center = this.getPosition();
        int radiusX = radius;
        int radiusY = radius;
        int maxX = this.getCurrentGame().getGameboard().getColumnCount();
        int maxY = this.getCurrentGame().getGameboard().getRowCount();
        int toMuch = 0;

        //Zu wei in X-Richtung (Columns)
        if (center.x() + radiusX > maxX-1) {
            toMuch = center.x() + radiusX - maxX;
            radiusX = radiusX - toMuch-1;
        }
        //Zu weit in Y-Richtung (Rows)
        if(center.y() + radiusY > maxY ){
            toMuch = center.y() + radiusY - maxY;
            radiusY = radiusY - toMuch-1;
        }
        // weniger als 0 in X-Richtung
        if(center.x() - radiusX < 0){
            toMuch = center.x() - radiusX;
            radiusX = radiusX + toMuch;
        }
        // weniger als 0 in Y-Richtung
        if(center.y() - radiusY < 0){
            toMuch = center.y() - radiusY;
            radiusY = radiusY + toMuch;
        }

        Position check;
        for(int i = 1; i <= radiusX; i++){
            for(int j = 0; j < radiusY; j++){
                check = new Position(center.x()+i,center.y()+j);
                FieldList checkFields = this.getCurrentGame().getGameboard().getFieldsAtPosition(check);
                    if (checkFields.isOccupied()) {
                        foundRobots.add(checkFields.getCurrentRobot());
                }
            }
        }
        for(int i = 1; i <= radiusX; i++){
            for(int j = 0; j < radiusY; j++){
                check = new Position(center.x()-i,center.y()-j);
                FieldList checkFields = this.getCurrentGame().getGameboard().getFieldsAtPosition(check);
                if (checkFields.isOccupied()) {
                    foundRobots.add(checkFields.getCurrentRobot());
                }
            }
        }
        for(int j = 1; j <= radiusX; j++){
            for(int i = 0; i < radiusY; i++){
                check = new Position(center.x()-j,center.y()+i);
                FieldList checkFields = this.getCurrentGame().getGameboard().getFieldsAtPosition(check);
                if (checkFields.isOccupied()) {
                    foundRobots.add(checkFields.getCurrentRobot());
                }
            }
        }
        for(int j = 1; j <= radiusY; j++){
            for(int i = 0; i <= radiusX; i++){
                check = new Position(center.x()+i,center.y()-j);
                FieldList checkFields = this.getCurrentGame().getGameboard().getFieldsAtPosition(check);
                if (checkFields.isOccupied()) {
                    foundRobots.add(checkFields.getCurrentRobot());
                }
            }
        }
        return foundRobots;
    }

    public void pushInDirection(Robot otherRobot, Direction direction, int moveAmount) //benötigt bei push bei einem reboot token Feld
    {
        otherRobot.move(moveAmount,direction);
    }

    /**
     * Dreht den Roboter in eine Richtung
     * @param rotation Roboter
     * @author Melanie, David
     */
    public void turn(int rotation)
    {
        switch (rotation)
        {
            case 1:
                setLookDir(new Direction((lookDir.getDirectionInteger()+1) % 4));
                playerTurningMsg = new PlayerTurning(this.getRobotPlayer().getPlayerID(),"clockwise");
                this.currentGame.gameServer.broadcast(playerTurningMsg);
                try
                {
                    Thread.sleep(delay);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                break;
            case -1:
                if(lookDir.getDirectionInteger() > 0)
                {
                    setLookDir(new Direction((lookDir.getDirectionInteger() - 1) % 4));
                }
                else setLookDir(new Direction(3));
                playerTurningMsg = new PlayerTurning(this.getRobotPlayer().getPlayerID(),"counterclockwise");
                this.currentGame.gameServer.broadcast(playerTurningMsg);
                try
                {
                    Thread.sleep(delay);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                break;
            case 0:
                setLookDir(new Direction((getLookDir().getDirectionInteger() + 2)%4));
                playerTurningMsg = new PlayerTurning(this.getRobotPlayer().getPlayerID(),"clockwise");
                this.currentGame.gameServer.broadcast(playerTurningMsg);
                try
                {
                    Thread.sleep(delay);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                playerTurningMsg = new PlayerTurning(this.getRobotPlayer().getPlayerID(),"clockwise");
                this.currentGame.gameServer.broadcast(playerTurningMsg);
                break;
            default:
                errorMsg = new Error("Invalid Rotation value!");
                this.getGameServer().broadcast(errorMsg);
                logger.warning("Invalid Rotation value! Received: "+rotation+" expected: 1, -1 or 0");
        }
    }

    /**
     * Bewegt den Roboter und behandelt Randfälle
     * @param distance Zu laufende Entfernung positiv oder negativ ist egal (rein visueller Unterschied im Code)
     * @param dir zu laufende Richtung (bestimmt im Gegensatz zu Distanz vorwärts/rückwärts)
     * @author David, Melanie
     */
    public void move(int distance, Direction dir)
    {
        for (int i = 1; i <= Math.abs(distance); i++)
        {
            if (doMovement(dir)){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return;//Bewegung von einem unpassierbaren Feld (z.B. Antenna) gestört, beende restlich Bewegung
            }

        }
    }
    
    /**
     * Handelt die Bewegung von einem Schritt ab
     * @param dir Richtung
     * @return boolean, ob die Bewegung (true) gescheitert ist
     * @author David
     */
    private boolean doMovement(Direction dir)
    {
        Position newPosition = dir.calculateNewPositionOfAction(this.position, 1);
        if (getCurrentGame().getGameboard().isIndexValid(newPosition.x(), newPosition.y()))
        {
            FieldList newFields = getCurrentGame().getGameboard().getFieldsAtPosition(newPosition);
            if (newFields != null)
            {
                if (newFields.isPassable())
                {
                    if (getCurrentGame().getGameboard().getFieldsAtPosition(position).hasWall())
                    {
                        if (getCurrentGame().getGameboard().getFieldsAtPosition(position).getWall().calculateCollision(getCurrentGame().getGameboard(), dir))
                        {
                            logger.info("Kollision mit Wand" + getRobotPlayer().getPlayerID());
                            return true;
                        }
                    } else if (getCurrentGame().getGameboard().getFieldsAtPosition(newPosition).hasWall())
                    {
                        if (getCurrentGame().getGameboard().getFieldsAtPosition(newPosition).getWall().calculateCollision(getCurrentGame().getGameboard(), dir.getOppositeDirection()))
                        {
                            logger.info("Kollision mit Wand");
                            return true;
                        }
                    }
                    if(newFields.isOccupied())
                    {
                        logger.info(getRobotPlayer().getPlayerID() + " pushed " + newFields.getCurrentRobot().getRobotPlayer().getPlayerID() + " @ " + newPosition);
                        pushInDirection(newFields.getCurrentRobot(),lookDir,1);
                        if(newFields.isOccupied())
                        {
                            logger.info("Andere Roboter konnte nicht verschoben werden");
                            return true;
                        }
                        else occupyField(newPosition);
                    }
                    else
                    {
                        occupyField(newPosition);
                    }
                    movementMsg = new Movement(this.getRobotPlayer().getPlayerID(), newPosition.x(), newPosition.y());
                    this.getGameServer().broadcast(movementMsg);
                    try
                    {
                        Thread.sleep(delay);
                    } catch (InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                    runActionsOnLandedFields(position);
                } else
                {
                    logger.info("Robot cant move forward, Robot will remain at its current position. Reason: Field unpassable");
                    return true;
                }
            } else
            {
                reboot();
                return true;
            }//Geht über das Board, fällt runter und rebootet
        } else
        {
            reboot();
            return true;
        }//Geht auf ein nicht existierendes Feld auf dem Board, fällt runter und rebootet
        return false;
    }
    
    /**
     * Gibt alle Wände in einer Richtung an
     * @return ArrayList der Walls
     * @author David
     */
    public ArrayList<Wall> getWallsInPath(int distance, Direction direction)
    {
        ArrayList<Wall> walls = new ArrayList<>();
        Position p = position;
        for (int i=0; i<distance; i++)
        {
            if(getCurrentGame().getGameboard().getFieldsAtPosition(p) != null)
            {
                if(getCurrentGame().getGameboard().getFieldsAtPosition(p).hasWall())
                {
                    walls.add(getCurrentGame().getGameboard().getFieldsAtPosition(p).getWall());
                }
                p = direction.calculateNewPositionOfAction(p, 1);
            }
        }
        return walls;
    }
    
    /**
     * Triggered die actionsOnLanding der neu betretenen Felder
     * @author David
     */
    private void runActionsOnLandedFields(Position p)
    {
        if(currentGame.getGameboard().isIndexValid(p.x(),p.y()))
        {
            for (Field field : currentGame.getGameboard().getFieldsAtPosition(p).getFields())
            {
                if (field instanceof ActionOnLanding)
                {
                    ((ActionOnLanding) field).actionOnLanding();
                }
            }
        }
    }

    /**
     * @author Melanie
     * @param robot
     * @return true if robot would fall off of board
     * Abfrage ob der weggeschobene Roboter von Board fallen würde
     */
    public boolean fallOfBoard(Robot robot){

        int maxValueX = this.currentGame.getGameboard().getRowCount();
        int maxValueY = this.currentGame.getGameboard().getColumnCount();

        switch (this.lookDir.getDirectionInteger()){
            case 1:
                if(robot.position.x()+1 >= maxValueX){
                    return true;
                }
                return false;
            case 2:
                if(robot.position.y()+1 >= maxValueY){
                    return true;
                }
                return false;
            case 3:
                if(robot.position.x()-1 < 0 ){
                    return true;
                }
                return false;
            case 0:
                if(robot.position.y()-1 < 0 ){
                    return true;
                }
                return false;
            default: errorMsg = new Error("Couldn't determine the looking Direction of Robot!");
            this.currentGame.gameServer.broadcast(errorMsg);
            return false;
        }
    }

    /**
     * @author Melanie
     * @param otherRobot
     * @return boolean
     * true if otherRobot is not on ConveyorBelt and pushing Robot is on ConveyorBelt
     * false if other Robot is on ConveyorBelt and/or pushing Robot is not on ConveyorBelt
     */
    public boolean notOnConveyorBelt(Robot otherRobot){

        List<Field> otherRobotField = otherRobot.currentGame.getGameboard().getFieldsAtPosition(new Position(otherRobot.position.x(), otherRobot.position.y())).getFields();
        List<Field> myRobotField = otherRobot.currentGame.getGameboard().getFieldsAtPosition(new Position(this.position.x(), this.position.y())).getFields();
        
        for(Field f1 : otherRobotField){
            for(Field f2: myRobotField){
                if(!(f1 instanceof ConveyorBelt) && f2 instanceof ConveyorBelt){
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }
    
    /**
     * Betrete ein neues Feld und verlasse das alte Feld
     * @author David
     */
    public void occupyField(Position newPosition)
    {
        if (lookDir == null)
        {
            setLookDirectionAccordingToBoard();
        }
        leaveField();
        setPosition(newPosition);
        FieldList newFields = currentGame.getGameboard().getFieldsAtPosition(position);
        newFields.setOccupied(true);
        newFields.setCurrentRobot(this);
    }
    
    public void setLookDirectionAccordingToBoard()
    {
        switch (getCurrentGame().getGameboard().getName())
            {
                case "DizzyHighway", "LostBearings", "ExtraCrispy", "Twister" -> lookDir = new Direction(1);
                case "DeathTrap" -> lookDir = new Direction(3);
            }
    }
    
    /**
     * Verlasse das alte Feld
     * @author David
     */
    private void leaveField()
    {
        if(position != null)
        {
            FieldList oldFields = currentGame.getGameboard().getFieldsAtPosition(this.getPosition());
            oldFields.setOccupied(false);
            if (oldFields.getCurrentRobot() == this)
            {
                oldFields.setCurrentRobot(null);
            }
        }
    }

    /**
     * Gibt an, ob ein Weg in Blickrichtung komplett frei ist
     * @param counter muss mit 0 initialisiert werden
     * @param distance Zu laufende Entfernung
     * @param direction Richtung in welche geguckt wird
     * @return Ob ein Weg frei ist
     * @author DavidKulbe
     */
    public boolean isPathFree(Position position, int counter, int distance, Direction direction)
    {
        if(isPathPassable(position, counter, distance, direction))
        {
            if (counter == distance)
            {
                return true;
            } else
            {
                Position newPos = direction.getNextPositionInDirection(position.x(), position.y());
                FieldList currentFields = currentGame.getGameboard().getFieldsAtPosition(newPos);
                if (currentFields != null)
                {
                    if (currentFields.isOccupied())
                    {
                        return false;
                    } else return isPathFree(newPos, ++counter, distance, direction);
                } else return false; //über das Brett hinaus
            }
        }
        else return false;
    }
    
    /**
     * Gibt an, ob ein Weg in Blickrichtung komplett passierbar ist (inkl. Kollisionen mit Wänden)
     * @param counter muss mit 0 initialisiert werden
     * @param distance Zu laufende Entfernung
     * @param direction Richtung in welche geguckt wird
     * @return Ob ein Weg frei ist
     * @author DavidKulbe
     */
    public boolean isPathPassable(Position position, int counter, int distance, Direction direction)
    {
        if(counter == distance)
        {
            return true;
        }
        else
        {
            Position newPos = direction.getNextPositionInDirection(position.x(), position.y());
            FieldList newFields = currentGame.getGameboard().getFieldsAtPosition(newPos);
            if (newFields != null)
            {
                if (!newFields.isPassable())
                {
                    return false;
                }
                if(getCurrentGame().getGameboard().getFieldsAtPosition(position).hasWall())
                {
                    if (getCurrentGame().getGameboard().getFieldsAtPosition(position).getWall().calculateCollision(getCurrentGame().getGameboard(), direction))
                    {
                        logger.info("Kollision mit Wand");
                        return false;
                    }
                }
                else if(getCurrentGame().getGameboard().getFieldsAtPosition(newPos).hasWall())
                {
                    if (getCurrentGame().getGameboard().getFieldsAtPosition(newPos).getWall().calculateCollision(getCurrentGame().getGameboard(), direction.getOppositeDirection()))
                    {
                        logger.info("Kollision mit Wand");
                        return false;
                    }
                }
                return isPathPassable(newPos,++counter,distance, direction);
            }
            else return false; //über das Brett hinaus
        }
    }

    /**
     * @author Melanie
     * @return Distance to Antenna of a Robot
     * Calculation: Consider Direction of Antenna
     */
    public int getDistanceToAntenna(){

        String orientation = this.getCurrentGame().getGameboard().getAntennaOnBoard().getOrientation();
        int xLocationAntenna = this.getCurrentGame().getGameboard().getAntennaOnBoard().getPosition().x();
        int yLocationAntenna =  this.getCurrentGame().getGameboard().getAntennaOnBoard().getPosition().y();

        int xLocationRobot = this.getPosition().x();
        int yLocationRobot = this.getPosition().y();

        int distance = 0;
        switch (orientation){
            case "top":
                    yLocationAntenna = yLocationRobot-1; // obere Feld von Antenne
                    distance = distance+1;
                    if(xLocationRobot > xLocationAntenna){
                        for(int i = xLocationAntenna ; i < xLocationRobot ;i++){
                            distance = distance + 1;
                        }
                    }else if(xLocationRobot < xLocationAntenna) {

                        for(int i = xLocationAntenna ; i > xLocationRobot ;i--){
                            distance = distance + 1;
                        }
                    }else{
                        logger.info("Keine Änderung an Distanz ");
                    }
                    if(yLocationRobot > yLocationAntenna){
                        for(int i = yLocationAntenna; i <= yLocationRobot; i++){
                            distance = distance + 1;
                        }
                    }else if(yLocationRobot < yLocationAntenna){
                        for(int i = yLocationAntenna; i >= yLocationRobot; i--){
                            distance = distance + 1;
                        }
                    }
                    break;
            case "bottom":
                    yLocationAntenna = yLocationRobot+1; // untere Feld von Antenne
                    distance = distance+1;
                    if(xLocationRobot > xLocationAntenna){
                        for(int i = xLocationAntenna ; i < xLocationRobot ;i++){
                            distance = distance + 1;
                        }
                    }else if(xLocationRobot < xLocationAntenna){
                        for(int i = xLocationAntenna ; i > xLocationRobot ;i--){
                            distance = distance + 1;
                        }
                    }
                    if(yLocationRobot > yLocationAntenna){
                        for(int i = yLocationAntenna; i <= yLocationRobot; i++){
                            distance = distance + 1;
                        }
                    }else if(yLocationRobot < yLocationAntenna){
                        for(int i = yLocationAntenna; i >= yLocationRobot; i--){
                            distance = distance + 1;
                        }
                    }
                    break;
            case "right":
                    xLocationAntenna = xLocationRobot+1; // rechte Feld von Antenne
                    distance = distance+1;
                    if(xLocationRobot > xLocationAntenna){
                        for(int i = xLocationAntenna ; i <= xLocationRobot ;i++){
                            distance = distance + 1;
                        }
                    }else if(xLocationRobot < xLocationAntenna){
                        for(int i = xLocationAntenna ; i >= xLocationRobot ;i--){
                            distance = distance + 1;
                        }
                    }
                    if(yLocationRobot > yLocationAntenna){
                        for(int i = yLocationAntenna; i < yLocationRobot; i++){
                            distance = distance + 1;
                        }
                    }else if(yLocationRobot < yLocationAntenna){
                        for(int i = yLocationAntenna; i > yLocationRobot; i--){
                            distance = distance + 1;
                        }
                    }
                    break;
            case "left":
                    xLocationAntenna = xLocationAntenna-1; // linke Feld von Antenne
                    distance = distance+1;

                    if(xLocationRobot > xLocationAntenna){
                        for(int i = xLocationAntenna ; i <= xLocationRobot ;i++){
                            distance = distance + 1;
                        }
                    }else if(xLocationRobot < xLocationAntenna){
                        for(int i = xLocationAntenna ; i > xLocationRobot ;i--){
                            distance = distance + 1;
                        }
                    }
                    if(yLocationRobot > yLocationAntenna){
                        for(int i = yLocationAntenna; i < yLocationRobot; i++){
                            distance = distance + 1;
                        }
                    }else if(yLocationRobot < yLocationAntenna){
                        for(int i = yLocationAntenna; i > yLocationRobot; i--){
                            distance = distance + 1;
                        }
                    }
                    break;
            default: logger.severe("Wrong Orientation from Antenna. Expected: top,bottom,left or right. Got: "+orientation);
        }
        return distance;
    }

   public int calculatePathTopBottom(int xLocationRobot, int yLocationRobot,int xLocationAntenna, int yLocationAntenna, int distance){

        return distance;
    }
    
    public void setPositionOfChosenStartingPoint(Position position)
    {
        chosenStartingPointPosition = position;
    }
    
    public Position getPositionOfChosenStartingPoint()
    {
        return chosenStartingPointPosition;
    }

    public Direction getLookDir() {
        return lookDir;
    }

    public void setLookDir(Direction lookDir) {
        this.lookDir = lookDir;
    }

    public int getCheckpointToken() {
        return checkpointToken;
    }

    public void setCheckpointToken(int checkpointToken) {
        this.checkpointToken = checkpointToken;
    }

    public int getAmountCubes() {
        return amountCubes;
    }

    public void setAmountCubes(int amountCubes) {
        this.amountCubes = amountCubes;
    }

    public Card[] getRegister() {
        return register;
    }

    public void setRegister(Card[] register) {
        this.register = register;
    }

    public Player getRobotPlayer() {
        return robotPlayer;
    }

    public Game getCurrentGame(){
        return this.currentGame;
    }

    public void setCurrentGame(Game currentGame){
        this.currentGame = currentGame;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        leaveField();
        
            this.position = position;
            if (getCurrentGame().getGameboard().isIndexValid(position.x(),position.y()))
            {
                currentCourseId = currentGame.getGameboard().getBoard()[position.y()][position.x()].getIsOnBoard();
                this.getCurrentGame().getGameboard().getFieldsAtPosition(position).setOccupied(true);
                this.getCurrentGame().getGameboard().getFieldsAtPosition(position).setCurrentRobot(this);
            }
        
    }

    public List<Card> getDiscardPile() {
        return discardPile;
    }

    public void setDiscardPile(List<Card> discardPile) {
        this.discardPile = discardPile;
    }

    public void addToCheckpointsVisited(CheckPoint cp){

        if(checkpointsVisited.isEmpty()){
            this.checkpointsVisited.add(cp);
        }else{
            for(int i=0; i<checkpointsVisited.size();i++){
                if(!checkpointsVisited.get(i).equals(cp)){
                    this.checkpointsVisited.add(cp);
                    return;
                }else{
                    logger.info("Dieser Checkpoint wurde bereits erreicht!");
                }
            }
        }



    }

    public List<CheckPoint> getCheckpointsVisited(){
        return this.checkpointsVisited;
    }

    public Server getGameServer(){
        return this.currentGame.gameServer;
    }

    public void setRobotPlayer(Player player){
        this.robotPlayer = player;
    }

    public void setOneRegister(Card card, int i){
        register[i] = card;
    }

    public void controlAllRegistersFilled(){
        boolean allFilled = true;
        for (Card i : register){
            if (i == null){
                allFilled = false;
            }
        }
        robotPlayer.setRegistersFilled(allFilled);
    }
    
    public boolean hasRebootetThisRound()
    {
        return rebootetThisRound;
    }
    
    public void setRebootetThisRound(boolean rebootetThisRound)
    {
        this.rebootetThisRound = rebootetThisRound;
    }
    
    public String getCurrentCourseId()
    {
        return currentCourseId;
    }
    
    public void setCurrentCourseId(String currentCourseId)
    {
        this.currentCourseId = currentCourseId;
    }

    public Card[] getUpgradesPerm() {
        return upgradesPerm;
    }

    public void setUpgradesPerm(Card newUpgrade) {

        for(int i =0; i<3;i++){
            if(getUpgradesPerm()[i] == null){
                this.upgradesPerm[i] = newUpgrade;
            }
        }
    }
    public Card[] getUpgradesTemp() {
        return upgradesTemp;
    }

    public void setUpgradesTemp(Card newUpgrade) {

        for(int i =0; i<3;i++){
            if(getUpgradesTemp()[i] == null){
                this.upgradesTemp[i] = newUpgrade;
            }
        }
    }

    public Card[] getInstalledUpgrades() {
        return installedUpgrades;
    }

    public void setInstalledUpgrades(Card newUpgrade) {

        for(int i =0; i<3;i++){
            if(getInstalledUpgrades()[i] == null){
                this.installedUpgrades[i] = newUpgrade;
            }
        }
    }

    public int getCurrentRegister() {
        return currentRegister;
    }

    public void setCurrentRegister(int currentRegister) {
        this.currentRegister = currentRegister;
    }

    public ArrayList<String> getDamageCardName() {
        return damageCardName;
    }

    public void tookDamage(){
        if (waitForDamage != null){
            waitForDamage.countDown();

        }
    }

    public void didReboot(){
        if (waitForReboot != null){
            waitForReboot.countDown();

        }
    }

    public void setCheckpointsVisited(List<CheckPoint> checkpointsVisited) {
        this.checkpointsVisited = checkpointsVisited;
    }
}
