package application;

import field.reducedFields.*;
import field.tools.Direction;
import field.tools.Position;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import server.ClientHandler;
import server.PlayerData;
import server.protocol.Message;
import server.protocol.aktionen.PlayerTurning;
import server.protocol.aktionen.Reboot;
import server.protocol.spielzug.SetStartingPoint;
import server.protocol.spielzug.StartingPointTaken;
import tools.ClientLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static javafx.scene.paint.Color.RED;

/**
 * @author Stephan
 * Controller of the GameBoard (Map)
 */
public class GameBoardController implements Initializable {
    private ClientHandler clientHandler;
    private double picSize = 42;
    @FXML
    private GridPane gameBoardGrid;
    @FXML
    private VBox gameBoardVBox;
    @FXML
    private HBox clip;
    @FXML
    private ImageView robot1view;
    @FXML
    private ImageView robot2view;
    @FXML
    private ImageView robot3view;
    @FXML
    private ImageView robot4view;
    @FXML
    private ImageView robot5view;
    @FXML
    private ImageView robot6view;
    @FXML
    private ImageView robot7view;
    @FXML
    private ImageView robot8view;
    @FXML
    private ImageView checkpoint1view;
    @FXML
    private ImageView checkpoint2view;
    @FXML
    private ImageView checkpoint3view;
    @FXML
    private ImageView checkpoint4view;
    @FXML
    private ImageView checkpoint5view;
    @FXML
    private ImageView checkpoint6view;
    private ArrayList<ArrayList<Integer>> laserList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> wallList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> startList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> checkpointList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> energyList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> beltList = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> animatedBeltList = new ArrayList<>();
    private boolean canSetRobot;
    private Thread thread;
    ChangeListener<Position> positionListener;
    ChangeListener<Direction> directionListener;
    ListChangeListener<PlayerData> playersListener;
    ChangeListener<ArrayList<Integer>> checkpointListener;
    ChangeListener<String> animationListener;
    private Logger logger = ClientLogger.getLogger();
    private boolean isAnimationActive = false;
    private boolean firstAnimation = true;
    private int beltPosition = 0;
    private int newBeltPosition = 1;
    private Position startPosition;
    private int startPosX;
    private int startPosY;


    /**
     * @author Stephan, Lea
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //loads the chosen map
        setClientHandler(StartController.getClientHandler());
        if(clientHandler.getClientData().getChosenMap() != null) {
            try {
                loadmap(clientHandler.getClientData().getChosenMap());
                logger.fine("Map is loaded to GUI");
            } catch (IOException e) {
                logger.severe("GameMap could not be loaded.");
                throw new RuntimeException(e);
            }
        }

        //make sure that the gameboard is never bigger than it should be while zoom
        double rectangesize = 700;
        rectangesize = Screen.getPrimary().getBounds().getMaxY() * rectangesize / 900;

        Rectangle rectangle = new Rectangle(rectangesize, rectangesize);
        clip.setClip(rectangle);
        clip.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
            rectangle.setWidth(newValue.getWidth() * 1.2);
            rectangle.setHeight(newValue.getHeight() * 1.2);
        });
        rectangle.setTranslateX(rectangle.getWidth() * -0.1);
        rectangle.setTranslateY(rectangle.getHeight() * -0.05);

        this.zoom(gameBoardVBox);
        this.addDrag(gameBoardVBox);


        canSetRobot = true;
        //waitForStartingTaken();
        setupStartPoints();

        addPlayersListener();
        addCheckpointListener();
        addAnimationListener();

        checkForMovement();
        checkforRotation();

        try {
            initializeRobotsAndCheckpoints();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //loadAnimatedBelts();
        beltAnimation();
    }


    private void addCheckpointListener() {
        checkpointListener = ((observableValue, oldValue, newValue) -> {
            try {
                handleCheckpoint(newValue);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        clientHandler.getClientData().checkPointProperty().addListener(checkpointListener);
    }

    private void handleCheckpoint(ArrayList<Integer> checkpointPos) throws IOException {
        int posX = checkpointPos.get(0);
        int posY = checkpointPos.get(1);
        int checkpointID = checkpointPos.get(2);

        for(int i = 0; i < checkpointList.size(); i++) {
            if(checkpointList.get(i).get(2) == checkpointID) {

                removeCheckpoint(checkpointID);
                setImageViewToGrid("checkpoint"+checkpointID, posX, posY);
                checkpointList.add(checkpointPos);
                checkpointList.remove(i);
                i = checkpointList.size();
            }
        }
    }

    /**
     * @author Stephan
     * @param gameBoardVBox
     * adds MouseEvents so you can drag the map around
     */
    private void addDrag(VBox gameBoardVBox) {
        final double[] deltaX = new double[1];
        final double[] deltaY = new double[1];
        gameBoardVBox.setOnMousePressed(mouseEvent -> {
            deltaX[0] = gameBoardVBox.getTranslateX() - mouseEvent.getSceneX();
            deltaY[0] = gameBoardVBox.getTranslateY() - mouseEvent.getSceneY();
        });
        gameBoardVBox.setOnMouseDragged(mouseEvent -> {
            gameBoardVBox.setTranslateX(mouseEvent.getSceneX() + deltaX[0]);
            gameBoardVBox.setTranslateY(mouseEvent.getSceneY() + deltaY[0]);
        });
    }

    /**
     * @author Stephan
     * @throws IOException
     */
    private void initializeRobotsAndCheckpoints() throws IOException {
        robot1view = new ImageView(getRobotPic("robothead1"));
        robot1view.setFitHeight(picSize * 0.9);
        robot1view.setPreserveRatio(true);

        robot2view = new ImageView(getRobotPic("robothead2"));
        robot2view.setFitHeight(picSize * 0.9);
        robot2view.setPreserveRatio(true);

        robot3view = new ImageView(getRobotPic("robothead3"));
        robot3view.setFitHeight(picSize * 0.9);
        robot3view.setPreserveRatio(true);

        robot4view = new ImageView(getRobotPic("robothead4"));
        robot4view.setFitHeight(picSize * 0.9);
        robot4view.setPreserveRatio(true);

        robot5view = new ImageView(getRobotPic("robothead5"));
        robot5view.setFitHeight(picSize * 0.9);
        robot5view.setPreserveRatio(true);

        robot6view = new ImageView(getRobotPic("robothead6"));
        robot6view.setFitHeight(picSize * 0.9);
        robot6view.setPreserveRatio(true);

        robot7view = new ImageView(getRobotPic("robothead7"));
        robot7view.setFitHeight(picSize * 0.9);
        robot7view.setPreserveRatio(true);

        robot8view = new ImageView(getRobotPic("robothead8"));
        robot8view.setFitHeight(picSize * 0.9);
        robot8view.setPreserveRatio(true);

        checkpoint1view = new ImageView(getPicture("checkpoint1"));
        checkpoint1view.setFitHeight(picSize);
        checkpoint1view.setPreserveRatio(true);

        checkpoint2view = new ImageView(getPicture("checkpoint2"));
        checkpoint2view.setFitHeight(picSize);
        checkpoint2view.setPreserveRatio(true);

        checkpoint3view = new ImageView(getPicture("checkpoint3"));
        checkpoint3view.setFitHeight(picSize);
        checkpoint3view.setPreserveRatio(true);

        checkpoint4view = new ImageView(getPicture("checkpoint4"));
        checkpoint4view.setFitHeight(picSize);
        checkpoint4view.setPreserveRatio(true);

        checkpoint5view = new ImageView(getPicture("checkpoint5"));
        checkpoint5view.setFitHeight(picSize);
        checkpoint5view.setPreserveRatio(true);

        checkpoint6view = new ImageView(getPicture("checkpoint6"));
        checkpoint6view.setFitHeight(picSize);
        checkpoint6view.setPreserveRatio(true);
    }

    private void addPlayersListener(){
        playersListener = change -> {
            while (change.next()){
                if (change.wasRemoved()){
                    // Get the player who left
                    //ArrayList<Integer> removedPlayerIDList = new ArrayList<>();
                    for (PlayerData p: change.getRemoved()) {
                        //removedPlayerIDList.add(p.getClientID());
                        removeRobot(p.getClientID());
                    }
                }
            }
        };
        clientHandler.getClientData().playersProperty().addListener(playersListener);
    }

    /**
     * @author Stephan
     * Thread that waits until the newest Message is a StartingPointTaken Message
     */
    private void waitForStartingTaken() {
        AtomicBoolean end = new AtomicBoolean(false);
        AtomicInteger count = new AtomicInteger();

        thread = new Thread(() -> {
            while(!(end.get())) {
                if(clientHandler.getCopyOfNewestMessage() != null) {
                    while (!(clientHandler.getCopyOfNewestMessage().toString().startsWith("Starting", 25))) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            logger.warning("Thread was interrupted by something.");
                            throw new RuntimeException(e);
                        }
                    }
                    StartingPointTaken newestMessage = (StartingPointTaken) clientHandler.getCopyOfNewestMessage();

                    if(newestMessage.getClientID() == clientHandler.getClientID()) {
                        canSetRobot = false;
                    }
                    count.getAndIncrement();
                    //clientHandler.resetNewestMessage();
                    if(clientHandler.getClientData().getPlayers().size() == count.get()) {
                        end.set(true);
                    }
                }
            }
            thread.interrupt();
        });
        thread.start();
    }

    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    /**
     * @param gameBoardVBox zooms into the gameBoard relative to the mouse position
     * @author Stephan
     */
    private void zoom(VBox gameBoardVBox) {
        gameBoardVBox.setOnScroll(
                new EventHandler<ScrollEvent>() {
                    @Override
                    public void handle(ScrollEvent Scrollevent) {
                        double zoomFactor = 1.1;

                        if (Scrollevent.getDeltaY() < 0) {
                            zoomFactor = 0.9;
                        }
                        Scale newScale = new Scale();
                        newScale.setPivotX(Scrollevent.getX());
                        newScale.setPivotY(Scrollevent.getY());
                        newScale.setX(gameBoardVBox.getScaleX() * zoomFactor);
                        newScale.setY(gameBoardVBox.getScaleY() * zoomFactor);
                        gameBoardVBox.getTransforms().add(newScale);

                        Scrollevent.consume();
                    }
                });
    }

    /**
     * @param name is the name of the wanted picture
     * @return Image object with the wanted picture
     * @throws IOException
     * @author Stephan
     */
    public Image getPicture(String name) throws IOException {
        return switch (name) {
            case "field" -> new Image(getClass().getClassLoader().getResource("images/board/field.png").openStream());
            case "green_arrow1" -> new Image(getClass().getClassLoader().getResource("images/board/green_1.png").openStream());
            case "green_arrow2" -> new Image(getClass().getClassLoader().getResource("images/board/green_2.png").openStream());
            case "green_arrow3" -> new Image(getClass().getClassLoader().getResource("images/board/green_3.png").openStream());
            case "green_arrow4" -> new Image(getClass().getClassLoader().getResource("images/board/green_4.png").openStream());

            case "blue_arrow1" -> new Image(getClass().getClassLoader().getResource("images/board/blue_1.png").openStream());
            case "blue_arrow2" -> new Image(getClass().getClassLoader().getResource("images/board/blue_2.png").openStream());
            case "blue_arrow3" -> new Image(getClass().getClassLoader().getResource("images/board/blue_3.png").openStream());
            case "blue_arrow4" -> new Image(getClass().getClassLoader().getResource("images/board/blue_4.png").openStream());
            case "startpoint" -> new Image(getClass().getClassLoader().getResource("images/board/startpoint.png").openStream());
            case "restartpoint" -> new Image(getClass().getClassLoader().getResource("images/board/restartpoint.png").openStream());
            case "wall" -> new Image(getClass().getClassLoader().getResource("images/board/wall.png").openStream());
            case "wall_corner" -> new Image(getClass().getClassLoader().getResource("images/board/wall_corner.png").openStream());
            case "laser1" -> new Image(getClass().getClassLoader().getResource("images/board/laser1.png").openStream());
            case "laser2" -> new Image(getClass().getClassLoader().getResource("images/board/laser2.png").openStream());
            case "laser3" -> new Image(getClass().getClassLoader().getResource("images/board/laser3.png").openStream());
            case "laserstrahl1" -> new Image(getClass().getClassLoader().getResource("images/board/laserstrahl1.png").openStream());
            case "laserstrahl2" -> new Image(getClass().getClassLoader().getResource("images/board/laserstrahl2.png").openStream());
            case "laserstrahl3" -> new Image(getClass().getClassLoader().getResource("images/board/laserstrahl3.png").openStream());
            case "energyspace_full" -> new Image(getClass().getClassLoader().getResource("images/board/energyspace_full.png").openStream());
            case "energyspace_empty" -> new Image(getClass().getClassLoader().getResource("images/board/energyspace_empty.png").openStream());
            case "pusher24" -> new Image(getClass().getClassLoader().getResource("images/board/pusher24.png").openStream());
            case "pusher135" -> new Image(getClass().getClassLoader().getResource("images/board/pusher135.png").openStream());
            case "pit" -> new Image(getClass().getClassLoader().getResource("images/board/pit.png").openStream());
            case "gear" -> new Image(getClass().getClassLoader().getResource("images/board/gear.png").openStream());
            case "gear_counter" -> new Image(getClass().getClassLoader().getResource("images/board/gear_counter.png").openStream());
            case "antenna" -> new Image(getClass().getClassLoader().getResource("images/board/antenna.png").openStream());
            case "checkpoint1" -> new Image(getClass().getClassLoader().getResource("images/board/checkpoint_1.png").openStream());
            case "checkpoint2" -> new Image(getClass().getClassLoader().getResource("images/board/checkpoint_2.png").openStream());
            case "checkpoint3" -> new Image(getClass().getClassLoader().getResource("images/board/checkpoint_3.png").openStream());
            case "checkpoint4" -> new Image(getClass().getClassLoader().getResource("images/board/checkpoint_4.png").openStream());
            case "checkpoint5" -> new Image(getClass().getClassLoader().getResource("images/board/checkpoint_5.png").openStream());
            case "checkpoint6" -> new Image(getClass().getClassLoader().getResource("images/board/checkpoint_6.png").openStream());
            case "laserende1" -> new Image(getClass().getClassLoader().getResource("images/board/laserende1.png").openStream());
            case "laserende2" -> new Image(getClass().getClassLoader().getResource("images/board/laserende2.png").openStream());
            case "laserende3" -> new Image(getClass().getClassLoader().getResource("images/board/laserende3.png").openStream());

            default -> null;
        };
    }

    /**
     * @author Stephan
     * @param reducedMapList
     * @throws IOException
     * calls handleField for every field in the reducedMapList
     * sets the picSize depending on the size of the gameboard
     */
    public void loadmap(ArrayList<ArrayList<ArrayList<ReducedField>>> reducedMapList) throws IOException {
        int ColumnCount = reducedMapList.size();
        int RowCount = 0;

        for (int i = 0; i < ColumnCount; i++) {
            if (reducedMapList.get(i) == null) {
                RowCount = 0;
            } else {
                RowCount = reducedMapList.get(i).size();
                i = ColumnCount;
            }
        }

        int size = Math.max(RowCount, ColumnCount);
        if (size <= 13) {
            picSize = 42;
        } else if (size <= 23) {
            picSize = 24;
        } else if (size > 24) {
            picSize = 16;
        }
        picSize = Screen.getPrimary().getBounds().getMaxY() * picSize / 1000;

        for (int x = 0; x < ColumnCount; x++) {
            for (int y = 0; y < RowCount; y++) {
                    handleField(reducedMapList.get(x).get(y), x, y);
            }
        }
    }

    /**
     * @author Stephan
     * @param fieldList
     * @param posX
     * @param posY
     * @throws IOException
     * checks which type the field is and sets the pictures in the gridpane according to the fieldtype, rotation and whether it's mirrored
     */
    private void handleField(ArrayList<ReducedField> fieldList, int posX, int posY) throws IOException {
        String fieldType = "";
        int rotation = 0;
        boolean isMirrored = false;

        if(fieldList!= null)
        {
            for (int i = 0; i < fieldList.size(); i++)
            {
                if (fieldList.get(i) != null)
                {
                    switch (fieldList.get(i).getType())
                    {
                        case "Empty" -> fieldType = "field";
                        case "Antenna" ->
                        {
                            fieldType = "antenna";
                            ReducedAntenna antenna = (ReducedAntenna) fieldList.get(i);
                            ArrayList<String> orient = antenna.getOrientations();
                            String o0 = orient.get(0);
                            if (o0.equals("right"))
                            {
                                rotation = 90;
                            } else if (o0.equals("bottom"))
                            {
                                rotation = 180;
                            } else if (o0.equals("left"))
                            {
                                rotation = 270;
                            }
                        }
                        case "CheckPoint" ->
                        {
                            ReducedCheckPoint checkpoint = (ReducedCheckPoint) fieldList.get(i);
                            fieldType = "checkpoint" + checkpoint.getCount();
                            ArrayList<Integer> checkpointPos = new ArrayList<>();
                            checkpointPos.add(posX);
                            checkpointPos.add(posY);
                            checkpointPos.add(checkpoint.getCount());
                            checkpointList.add(checkpointPos);
                            setImageViewToGrid(fieldType, posX, posY);
                            beltPosition--;

                        }
                        case "ConveyorBelt" ->
                        {
                            ReducedConveyorBelt belt = (ReducedConveyorBelt) fieldList.get(i);
                            ArrayList<String> orient = belt.getOrientations();
                            String o0 = orient.get(0);
                            String o1 = orient.get(1);
                            String o2 = "";
                            if (orient.size() == 3)
                            {
                                o2 = orient.get(2);
                            }
                    
                            //arrow1
                            if (orient.size() == 2)
                            {
                                if (o0.equals("top") && o1.equals("bottom"))
                                {
                                    fieldType = "arrow1";
                                } else if (o0.equals("right") && o1.equals("left"))
                                {
                                    fieldType = "arrow1";
                                    rotation = 90;
                                } else if (o0.equals("bottom") && o1.equals("top"))
                                {
                                    fieldType = "arrow1";
                                    rotation = 180;
                                } else if (o0.equals("left") && o1.equals("right"))
                                {
                                    fieldType = "arrow1";
                                    rotation = 270;
                                }
                        
                                //arrow2
                                else if (o0.equals("right") && o1.equals("bottom"))
                                {
                                    fieldType = "arrow2";
                                } else if (o0.equals("bottom") && o1.equals("left"))
                                {
                                    fieldType = "arrow2";
                                    rotation = 90;
                                } else if (o0.equals("left") && o1.equals("top"))
                                {
                                    fieldType = "arrow2";
                                    rotation = 180;
                                } else if (o0.equals("top") && o1.equals("right"))
                                {
                                    fieldType = "arrow2";
                                    rotation = 270;
                                } else if (o0.equals("left") && o1.equals("bottom"))
                                {
                                    fieldType = "arrow2";
                                    rotation = 180;
                                    isMirrored = true;
                                } else if (o0.equals("bottom") && o1.equals("right"))
                                {
                                    fieldType = "arrow2";
                                    rotation = 90;
                                    isMirrored = true;
                                } else if (o0.equals("right") && o1.equals("top"))
                                {
                                    fieldType = "arrow2";
                                    isMirrored = true;
                                } else if (o0.equals("top") && o1.equals("left"))
                                {
                                    fieldType = "arrow2";
                                    rotation = 270;
                                    isMirrored = true;
                                }
                            }
                            //arrow 3
                            else if (o0.equals("bottom") && ((o1.equals("right") && o2.equals("top")) || (o1.equals("top") && o2.equals("right"))))
                            {
                                fieldType = "arrow3";
                            } else if (o0.equals("left") && ((o1.equals("bottom") && o2.equals("right")) || (o1.equals("right") && o2.equals("bottom"))))
                            {
                                fieldType = "arrow3";
                                rotation = 90;
                            } else if (o0.equals("top") && ((o1.equals("left") && o2.equals("bottom")) || (o1.equals("bottom") && o2.equals("left"))))
                            {
                                fieldType = "arrow3";
                                rotation = 180;
                            } else if (o0.equals("right") && ((o1.equals("top") && o2.equals("left")) || (o1.equals("left") && o2.equals("top"))))
                            {
                                fieldType = "arrow3";
                                rotation = 270;
                            } else if (o0.equals("bottom") && ((o1.equals("left") && o2.equals("top")) || (o1.equals("top") && o2.equals("left"))))
                            {
                                fieldType = "arrow3";
                                isMirrored = true;
                                rotation = 180;
                            } else if (o0.equals("right") && ((o1.equals("bottom") && o2.equals("left")) || (o1.equals("left") && o2.equals("bottom"))))
                            {
                                fieldType = "arrow3";
                                rotation = 90;
                                isMirrored = true;
                            } else if (o0.equals("top") && ((o1.equals("right") && o2.equals("bottom")) || (o1.equals("bottom") && o2.equals("right"))))
                            {
                                fieldType = "arrow3";
                                //rotation = 180;
                                isMirrored = true;
                            } else if (o0.equals("left") && ((o1.equals("top") && o2.equals("right")) || (o1.equals("right") && o2.equals("top"))))
                            {
                                fieldType = "arrow3";
                                rotation = 270;
                                isMirrored = true;
                            }
                    
                            //arrow4
                            else if (o0.equals("right") && ((o1.equals("top") && o2.equals("bottom")) || (o1.equals("bottom") && o2.equals("top"))))
                            {
                                fieldType = "arrow4";
                            } else if (o0.equals("bottom") && ((o1.equals("right") && o2.equals("left")) || (o1.equals("left") && o2.equals("right"))))
                            {
                                fieldType = "arrow4";
                                rotation = 90;
                            } else if (o0.equals("left") && ((o1.equals("top") && o2.equals("bottom")) || (o1.equals("bottom") && o2.equals("top"))))
                            {
                                fieldType = "arrow4";
                                rotation = 180;
                            } else if (o0.equals("top") && ((o1.equals("right") && o2.equals("left")) || (o1.equals("left") && o2.equals("right"))))
                            {
                                fieldType = "arrow4";
                                rotation = 270;
                            }
                    
                            //green or blue belt depending on the speed
                            String colour;
                            if (belt.getSpeed() == 1)
                            {
                                colour = "green";
                            } else colour = "blue";
                            fieldType = colour + "_" + fieldType;

                            //ArrayList<Integer> oneBelt = new ArrayList<>();
                            //int mirrored = 0;
                            //if(isMirrored) {
                            //    mirrored = 1;
                            //}
                            //oneBelt.add(belt.getSpeed());
                            //oneBelt.add(posX);
                            //oneBelt.add(posY);
                            //oneBelt.add(rotation);
                            //oneBelt.add(mirrored);
                            //oneBelt.add(Integer.valueOf(fieldType.substring(fieldType.length() -1)));
                            //oneBelt.add(beltPosition);
                            //beltList.add(oneBelt);
                        }
                        case "EnergySpace" ->
                        {
                            ReducedEnergySpace energySpace = (ReducedEnergySpace) fieldList.get(i);
                            if(energySpace.getCount() >= 1) {
                                fieldType = "energyspace_full";
                            } else fieldType = "energyspace_empty";
                            ArrayList<Integer> energyPos = new ArrayList<>();
                            energyPos.add(posX);
                            energyPos.add(posY);
                            energyList.add(energyPos);
                        }
                        case "Gear" ->
                        {
                            ReducedGear gear = (ReducedGear) fieldList.get(i);
                            fieldType = "gear";
                            if (gear.getOrientations().get(0).equals("counterclockwise"))
                            {
                                fieldType = "gear_counter";
                            }
                        }
                        case "Laser" ->
                        {
                            ReducedLaser laser = (ReducedLaser) fieldList.get(i);
                            fieldType = "laser" + laser.getCount();
                            switch (laser.getOrientations().get(0))
                            {
                                case "bottom" -> rotation = 90;
                                case "left" -> rotation = 180;
                                case "top" -> rotation = 270;
                                default ->
                                {
                                }
                            }
                    
                            ArrayList<Integer> laserPos = new ArrayList<>();
                            laserPos.add(posX);
                            laserPos.add(posY);
                            laserList.add(laserPos);
                            Platform.runLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        handleLaser(laser, posX, posY);
                                    } catch (IOException e)
                                    {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                    
                        }
                        case "Pit" -> fieldType = "pit";
                        case "PushPanel" ->
                        {
                            ReducedPushPanel pushPanel = (ReducedPushPanel) fieldList.get(i);
                            String o0 = pushPanel.getOrientations().get(0);
                            switch (o0)
                            {
                                case "left" -> rotation = 180;
                                case "top" -> rotation = 270;
                                case "bottom" -> rotation = 90;
                                default ->
                                {
                                }
                            }
                            ArrayList<Integer> registers = pushPanel.getRegisters();
                            fieldType = "pusher";
                            for (int j = 0; j < registers.size(); j++)
                            {
                                fieldType = fieldType + registers.get(j);
                            }
                        }
                        case "RestartPoint" ->
                        {
                            fieldType = "restartpoint";
                            ReducedRestartPoint restartpoint = (ReducedRestartPoint) fieldList.get(i);
                            switch (restartpoint.getOrientations().get(0))
                            {
                                case "right" -> rotation = 90;
                                case "bottom" -> rotation = 180;
                                case "left" -> rotation = 270;
                                default ->
                                {
                                }
                            }
                        }
                        case "StartPoint" ->
                        {
                            fieldType = "startpoint";
                            ArrayList<Integer> startPos = new ArrayList<>();
                            startPos.add(posX);
                            startPos.add(posY);
                            startList.add(startPos);
                        }
                        case "Wall" ->
                        {
                            ReducedWall wall = (ReducedWall) fieldList.get(i);
                            ArrayList<String> orientation = wall.getOrientations();
                            String o0 = orientation.get(0);
                            String o1 = "";
                            if (orientation.size() == 1)
                            {
                                fieldType = "wall";
                                ArrayList<Integer> wallPos = new ArrayList<>();
                                wallPos.add(posX);
                                wallPos.add(posY);
                                wallList.add(wallPos);
                            } else
                            {
                                o1 = orientation.get(1);
                                fieldType = "wall_corner";
                            }
                            if (orientation.size() == 1)
                            {
                                switch (o0)
                                {
                                    case "top" -> rotation = 90;
                                    case "right" -> rotation = 180;
                                    case "bottom" -> rotation = 270;
                                    default ->
                                    {
                                    }
                                }
                            } else
                            {
                                if ((o0.equals("left") && o1.equals("top")) || (o0.equals("top") && o1.equals("left")))
                                {
                                    rotation = 90;
                                } else if ((o0.equals("right") && o1.equals("top")) || (o0.equals("top") && o1.equals("right")))
                                {
                                    rotation = 180;
                                } else if ((o0.equals("right") && o1.equals("bottom")) || (o0.equals("bottom") && o1.equals("right")))
                                {
                                    rotation = 270;
                                }
                            }
                        }
                    }
                } else
                {
                    fieldType = null;
                }

                if(!fieldType.startsWith("checkpoint")) {
                    setPictureNew(fieldType, posX, posY, rotation, isMirrored);
                }
                if(fieldType.startsWith("blue_arrow") || fieldType.startsWith("green_arrow")) {
                    setGifToGameBoard(fieldType, posX, posY, rotation, isMirrored);
                    ArrayList<Integer> oneBelt = new ArrayList<>();
                    beltPosition++;
                    oneBelt.add(beltPosition);
                    animatedBeltList.add(oneBelt);
                }
                fieldType = "";
                rotation = 0;
                isMirrored = false;
                beltPosition++;
            }
        }
    }

    /**
     * @author Stephan
     * @param laser
     * @param posX
     * @param posY
     * @throws IOException
     * sets pictures of the laser beams between 2 lasers /  between a laser and a wall
     */
    private void handleLaser(ReducedLaser laser, int posX, int posY) throws IOException {
        for (int i = 0; i < wallList.size(); i++) {
            switch (laser.getOrientations().get(0)) {
                case "top" -> {
                    int j = 1;
                    if(posX == wallList.get(i).get(0) && wallList.get(i).get(1) < posY) {
                        while(posY-j != wallList.get(i).get(1)) {
                            int x = posY-j;
                            setPictureNew("laserstrahl" + laser.getCount(), posX, posY-j, 90, false);
                            j++;
                        }
                        setLaserEnd(laser.getCount(), posX, posY-j, 90, false);
                        //setPictureNew("laserende" + laser.getCount(), posX, posY-j, 90, false);

                        i = wallList.size();
                    }
                }
                case "bottom" -> {
                    int j = 1;
                    if(posX == wallList.get(i).get(0) && wallList.get(i).get(1) > posY) {
                        while(posY+j != wallList.get(i).get(1)) {
                            int x = posY+j;
                            setPictureNew("laserstrahl" + laser.getCount(), posX, posY+j, 90, false);
                            j++;
                        }
                        setLaserEnd(laser.getCount(), posX, posY+j, 270, false);
                        //setPictureNew("laserende" + laser.getCount(), posX, posY+j, 270, false);

                        i = wallList.size();
                    }
                }
                case "left" -> {
                    int j = 1;
                    if(posY == wallList.get(i).get(1) && wallList.get(i).get(0) < posX) {
                        while(posX-j != wallList.get(i).get(0)) {
                            int x = posX-j;
                            setPictureNew("laserstrahl" + laser.getCount(), posX-j, posY, 0, false);
                            j++;
                        }
                        setLaserEnd(laser.getCount(), posX-j, posY, 0, false);
                        //setPictureNew("laserende" + laser.getCount(), posX-j, posY, 0, false);
                        i = wallList.size();
                    }
                }
                case "right" -> {
                    int j = 1;
                    if(posY == wallList.get(i).get(1) && wallList.get(i).get(0) > posX) {
                        while(posX+j != wallList.get(i).get(0)) {
                            int x = posX+j;
                            setPictureNew("laserstrahl" + laser.getCount(), posX+j, posY, 0, false);
                            j++;
                        }
                        setLaserEnd(laser.getCount(), posX+j, posY, 180, false);
                        //setPictureNew("laserende" + laser.getCount(), posX+j, posY, 180, false);
                        i = wallList.size();
                    }
                }
            }
        }
    }

    /**
     * @author Stephan
     * @param lasercount
     * @param posX
     * @param posY
     * @param rotation
     * @param isMirrored
     * @throws IOException
     * checks which image to set at the end of a laserbeam
     */
    private void setLaserEnd(int lasercount, int posX, int posY, int rotation, boolean isMirrored) throws IOException {
        for(int i = 0; i < checkpointList.size(); i++) {
            if (posX == checkpointList.get(i).get(0) && posY == checkpointList.get(i).get(1)) {
                    setPictureNew("checkpoint" + checkpointList.get(i).get(2), posX, posY, rotation, isMirrored);
                    return;
            }
        }

        for(int i = 0; i < laserList.size(); i++) {
            if (posX == laserList.get(i).get(0) && posY == laserList.get(i).get(1)) {
                //setPictureNew("laser" + lasercount, posX, posY, rotation, isMirrored);
                return;
            }
        }
        setPictureNew("laserende" + lasercount, posX, posY, rotation, isMirrored);
    }

    /**
     * @author Stephan
     * adds a rectangle to all fields that are a startingPoint and adds a MouseEvent to them that sends a SetStartingPoint Message if the player didn't set a robot before
     */
    private void setupStartPoints() {
        for(int i  = 0; i < startList.size(); i++) {
            int posX = startList.get(i).get(0);
            int posY = startList.get(i).get(1);

            Rectangle startField = new Rectangle();
            startField.setHeight(picSize);
            startField.setWidth(picSize);
            startField.setFill(RED);
            startField.setOpacity(0);
            gameBoardGrid.add(startField, posX, posY);

            startField.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if(canSetRobot) {
                        logger.config("clicked X: " +  posX + " Y: " + posY);
                        try {
                            startPosX = posX;
                            startPosY = posY;
                            SetStartingPoint message = new SetStartingPoint(posX, posY);
                            clientHandler.sendMessageSerialized(message);
                            //canSetRobot = false;
                        } catch (IOException e) {
                            logger.warning("StartingPoint-Message could not be sent");
                            throw new RuntimeException(e);
                        }
                        mouseEvent.consume();
                    }
                }
            });
        }
    }

    /**
     * @author Stephan
     * @param imageviewName
     * @param posX
     * @param posY
     * adds the imageView to the wanted position with the right colour
     */
    private void setImageViewToGrid(String imageviewName, int posX, int posY) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch(imageviewName) {
                    case "robothead1" -> gameBoardGrid.add(robot1view, posX, posY);
                    case "robothead2" -> gameBoardGrid.add(robot2view, posX, posY);
                    case "robothead3" -> gameBoardGrid.add(robot3view, posX, posY);
                    case "robothead4" -> gameBoardGrid.add(robot4view, posX, posY);
                    case "robothead5" -> gameBoardGrid.add(robot5view, posX, posY);
                    case "robothead6" -> gameBoardGrid.add(robot6view, posX, posY);
                    case "robothead7" -> gameBoardGrid.add(robot7view, posX, posY);
                    case "robothead8" -> gameBoardGrid.add(robot8view, posX, posY);
                    case "checkpoint1" -> gameBoardGrid.add(checkpoint1view, posX, posY);
                    case "checkpoint2" -> gameBoardGrid.add(checkpoint2view, posX, posY);
                    case "checkpoint3" -> gameBoardGrid.add(checkpoint3view, posX, posY);
                    case "checkpoint4" -> gameBoardGrid.add(checkpoint4view, posX, posY);
                    case "checkpoint5" -> gameBoardGrid.add(checkpoint5view, posX, posY);
                    case "checkpoint6" -> gameBoardGrid.add(checkpoint6view, posX, posY);
                }
            }
        });
    }

    private void setRobotToGrid(int robotID, int posX, int posY) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch(robotID) {
                    case 1 -> gameBoardGrid.add(robot1view, posX, posY);
                    case 2 -> gameBoardGrid.add(robot2view, posX, posY);
                    case 3 -> gameBoardGrid.add(robot3view, posX, posY);
                    case 4 -> gameBoardGrid.add(robot4view, posX, posY);
                    case 5 -> gameBoardGrid.add(robot5view, posX, posY);
                    case 6 -> gameBoardGrid.add(robot6view, posX, posY);
                    case 7 -> gameBoardGrid.add(robot7view, posX, posY);
                    case 8 -> gameBoardGrid.add(robot8view, posX, posY);
                }
            }
        });
    }

    /**
     * @author Stephan
     * @param name
     * @return the Image of the wanted robot
     * @throws IOException
     */
    private Image getRobotPic(String name) throws IOException {
        return switch(name) {
            case "robothead1" -> new Image(getClass().getClassLoader().getResource("images/robots/robothead1.png").openStream());
            case "robothead2" -> new Image(getClass().getClassLoader().getResource("images/robots/robothead2.png").openStream());
            case "robothead3" -> new Image(getClass().getClassLoader().getResource("images/robots/robothead3.png").openStream());
            case "robothead4" -> new Image(getClass().getClassLoader().getResource("images/robots/robothead4.png").openStream());
            case "robothead5" -> new Image(getClass().getClassLoader().getResource("images/robots/robothead5.png").openStream());
            case "robothead6" -> new Image(getClass().getClassLoader().getResource("images/robots/robothead6.png").openStream());
            case "robothead7" -> new Image(getClass().getClassLoader().getResource("images/robots/robothead7.png").openStream());
            case "robothead8" -> new Image(getClass().getClassLoader().getResource("images/robots/robothead8.png").openStream());

            default -> null;
        };
    }

    /**
     * @author Stephan
     * @param posX
     * @param posY
     * @return the Node on the wanted position
     */

    private Node getNode(int posX, int posY) {
        for(Node node : gameBoardGrid.getChildren()) {
            if(GridPane.getColumnIndex(node) == posX && GridPane.getRowIndex(node) == posY) {
                return node;
            }
        }
        return null;
    }

    private Node getNode(int i) {
        return gameBoardGrid.getChildren().get(i);
    }

    /**
     * @author Stephan
     * @param robot
     * @return Position of the robot
     */
    private Position getRobotPosition(ImageView robot) {
        int x = GridPane.getColumnIndex(robot);
        int y = GridPane.getRowIndex(robot);
        return new Position(x, y);
    }

    /**
     * @author Stephan
     * @param robotID
     * @return Position of the robot
     */
    private Position getRobotPosition(int robotID) {
        ImageView robot;
        if(robotID == 1) {
            robot = robot1view;
        } else if(robotID == 2) {
            robot = robot2view;
        } else if(robotID == 3) {
            robot = robot3view;
        } else if(robotID == 4) {
            robot = robot4view;
        } else if(robotID == 5) {
            robot = robot5view;
        } else {
            robot = robot6view;
        }
        int x = GridPane.getColumnIndex(robot);
        int y = GridPane.getRowIndex(robot);
        return new Position(x, y);
    }

    /**
     * @author Stephan
     * @param name
     * @param posX
     * @param posY
     * @param rotation
     * @param isMirrored
     * @throws IOException
     * sets the picture in the gridpane
     */
    public void setPictureNew(String name, int posX, int posY, int rotation, boolean isMirrored) throws IOException {
        if(name != null) {
            ImageView imageview;
            imageview = new ImageView(getPicture(name));
            imageview.setFitHeight(picSize);
            imageview.setPreserveRatio(true);
            imageview.setRotate(rotation);
            if (isMirrored) {
                imageview.setScaleY(-1);
            }
            gameBoardGrid.add(imageview, posX, posY);
        }
    }

    private void checkForStartPointTaken(){
        Position yourStartPosition = clientHandler.getClientData().getYourPlayerData().getPosition();
        if (yourStartPosition != null && canSetRobot){
            if (yourStartPosition.x() == startPosX && yourStartPosition.y() == startPosY){
                canSetRobot = false;
            }
        }

    }
    /**
     * @author Stephan
     * adds the Listeners to the position of the players
     */
    public void checkForMovement() {

        positionListener = (observableValue, oldValue, newValue) -> {
            try {
                checkForStartPointTaken();
                handleMovement(newValue);

            } catch (IOException e) {

                throw new RuntimeException(e);
            }
        };
        clientHandler.getClientData().getYourPlayerData().positionProperty().addListener(positionListener);
        int playerCount = clientHandler.getClientData().getPlayers().size();
        if(playerCount >= 2) {
            clientHandler.getClientData().getPlayer1Data().positionProperty().addListener(positionListener);
        }
        if(playerCount >= 3) {
            clientHandler.getClientData().getPlayer2Data().positionProperty().addListener(positionListener);
        }
        if(playerCount >= 4) {
            clientHandler.getClientData().getPlayer3Data().positionProperty().addListener(positionListener);
        }
        if(playerCount >= 5) {
            clientHandler.getClientData().getPlayer4Data().positionProperty().addListener(positionListener);
        }
        if(playerCount == 6) {
            clientHandler.getClientData().getPlayer5Data().positionProperty().addListener(positionListener);
        }
    }

    /**
     * @author Stephan
     * adds the Listeners to the orientation of the players
     */
    private void checkforRotation() {
        directionListener = ((observableValue, oldValue, newValue) -> {
            handleRotation(newValue);
        });
        clientHandler.getClientData().getYourPlayerData().orientationProperty().addListener(directionListener);
        int playerCount = clientHandler.getClientData().getPlayers().size();
        if(playerCount >= 2) {
            clientHandler.getClientData().getPlayer1Data().orientationProperty().addListener(directionListener);
        }
        if(playerCount >= 3) {
            clientHandler.getClientData().getPlayer2Data().orientationProperty().addListener(directionListener);
        }
        if(playerCount >= 4) {
            clientHandler.getClientData().getPlayer3Data().orientationProperty().addListener(directionListener);
        }
        if(playerCount >= 5) {
            clientHandler.getClientData().getPlayer4Data().orientationProperty().addListener(directionListener);
        }
        if(playerCount == 6) {
            clientHandler.getClientData().getPlayer5Data().orientationProperty().addListener(directionListener);
        }
    }

    /**
     * @author Stephan
     * @param newPos
     * @throws IOException
     * first removes the robot on the old position and then adds it again on the new position
     */
    private void handleMovement(Position newPos) throws IOException {
        int robotID = 0;
        if(clientHandler.getClientData().getYourPlayerData().getPosition() == newPos) {
            robotID = clientHandler.getClientData().getYourPlayerData().getFigure();
        } else if(clientHandler.getClientData().getPlayer1Data() != null && clientHandler.getClientData().getPlayer1Data().getPosition() == newPos) {
            robotID = clientHandler.getClientData().getPlayer1Data().getFigure();
        } else if(clientHandler.getClientData().getPlayer2Data() != null && clientHandler.getClientData().getPlayer2Data().getPosition() == newPos) {
            robotID = clientHandler.getClientData().getPlayer2Data().getFigure();
        } else if(clientHandler.getClientData().getPlayer3Data() != null && clientHandler.getClientData().getPlayer3Data().getPosition() == newPos) {
            robotID = clientHandler.getClientData().getPlayer3Data().getFigure();
        } else if(clientHandler.getClientData().getPlayer4Data() != null && clientHandler.getClientData().getPlayer4Data().getPosition() == newPos) {
            robotID = clientHandler.getClientData().getPlayer4Data().getFigure();
        } else if(clientHandler.getClientData().getPlayer5Data() != null && clientHandler.getClientData().getPlayer5Data().getPosition() == newPos) {
            robotID = clientHandler.getClientData().getPlayer5Data().getFigure();
        }

        removeRobot(robotID);

        String name = "robothead" + robotID;
        setImageViewToGrid(name, newPos.x(), newPos.y());
    }

    /**
     * @author Stephan
     * @param selectedMap
     * @return the starting rotation depending on the selected map
     */
    private int getFirstOrientation(String selectedMap) {
        return switch (selectedMap) {
            case "Start: Dizzy Highway", "Intermediate: Lost Bearings", "RobotsMustDie: Extra Crispy" -> 90;
            case "Advanced: Death Trap" -> 270;
            default -> 0;
        };
    }


    private int lastRebootedID;
    /**
     * @author Stephan
     * @param newRotation
     * checks for the ClientID and rotates the robot of that Client
     */
    private void handleRotation(Direction newRotation) {
        int clientID = 0;
        ArrayList<Message> log = clientHandler.getLogOfMessages();
        int size = log.size();
        for (int i = 1; size - i > 0; i++) {
            if(i > 20) {
                i = size;
            } else if (log.get(size - i).toString().startsWith("PlayerTurning", 25)) {
                PlayerTurning message = (PlayerTurning) log.get(size - i);
                clientID = message.getClientID();
                i = size;
            } else if (log.get(size - i).toString().startsWith("Reboot", 25)) {
                if (!log.get(size - i).toString().startsWith("RebootDirection", 25)) {
                    Reboot message = (Reboot) log.get(size - i);
                    clientID = message.getClientID();
                    //lastRebootedID = clientID;
                    i = size;
                //} else {
                }
            //} if(log.get(size - i).toString().startsWith("RebootDirection", 25)) {
            //    clientID = lastRebootedID;
            //    i = size;
            } else if (log.get(size - i).toString().startsWith("Starting", 25)) {
                StartingPointTaken message = (StartingPointTaken) log.get(size - i);
                clientID = message.getClientID();
                i = size;
            }
        }


        int robotID = 0;
        if (clientHandler.getClientID() == clientID) {
            robotID = clientHandler.getClientData().getYourPlayerData().getFigure();
        } else if (clientHandler.getClientData().getPlayer1Data() != null && clientHandler.getClientData().getPlayer1Data().getClientID() == clientID) {
            robotID = clientHandler.getClientData().getPlayer1Data().getFigure();
        } else if (clientHandler.getClientData().getPlayer2Data() != null && clientHandler.getClientData().getPlayer2Data().getClientID() == clientID) {
            robotID = clientHandler.getClientData().getPlayer2Data().getFigure();
        } else if (clientHandler.getClientData().getPlayer3Data() != null && clientHandler.getClientData().getPlayer3Data().getClientID() == clientID) {
            robotID = clientHandler.getClientData().getPlayer3Data().getFigure();
        } else if (clientHandler.getClientData().getPlayer4Data() != null && clientHandler.getClientData().getPlayer4Data().getClientID() == clientID) {
            robotID = clientHandler.getClientData().getPlayer4Data().getFigure();
        } else if (clientHandler.getClientData().getPlayer5Data() != null && clientHandler.getClientData().getPlayer5Data().getClientID() == clientID) {
            robotID = clientHandler.getClientData().getPlayer5Data().getFigure();
        }
        rotateRobot(robotID, newRotation.getDirectionInteger());
    }

    /**
     * @author Stephan
     * @param robotID
     * @param newRotation
     * rotates the robot
     */
    private void rotateRobot(int robotID, int newRotation) {
        int rotation = 0;

        if(newRotation <= 4) {
            switch (newRotation) {
                case 1 -> rotation = 90;
                case 2 -> rotation = 180;
                case 3 -> rotation = 270;
            }
        } else rotation = newRotation;

        int finalRotation = rotation;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch (robotID) {
                    case 1 ->
                        robot1view.setRotate(finalRotation);
                    case 2 ->
                        robot2view.setRotate(finalRotation);
                    case 3 ->
                        robot3view.setRotate(finalRotation);
                    case 4 ->
                        robot4view.setRotate(finalRotation);
                    case 5 ->
                        robot5view.setRotate(finalRotation);
                    case 6 ->
                        robot6view.setRotate(finalRotation);
                    case 7 ->
                        robot7view.setRotate(finalRotation);
                    case 8 ->
                        robot8view.setRotate(finalRotation);
                }
            }
        });

    }

    /**
     * @author Stephan
     * @param robotID
     * removes the imageview of the robot from the gridpane
     */
    private void removeRobot(int robotID) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch (robotID) {
                    case 1 -> gameBoardGrid.getChildren().remove(robot1view);
                    case 2 -> gameBoardGrid.getChildren().remove(robot2view);
                    case 3 -> gameBoardGrid.getChildren().remove(robot3view);
                    case 4 -> gameBoardGrid.getChildren().remove(robot4view);
                    case 5 -> gameBoardGrid.getChildren().remove(robot5view);
                    case 6 -> gameBoardGrid.getChildren().remove(robot6view);
                    case 7 -> gameBoardGrid.getChildren().remove(robot7view);
                    case 8 -> gameBoardGrid.getChildren().remove(robot8view);
                }
            }
        });
    }

    /**
     * @author Stephan
     * removes the imageview of the robot from the gridpane
     */
    private void removeCheckpoint(int checkPointID) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                switch (checkPointID) {
                    case 1 -> gameBoardGrid.getChildren().remove(checkpoint1view);
                    case 2 -> gameBoardGrid.getChildren().remove(checkpoint2view);
                    case 3 -> gameBoardGrid.getChildren().remove(checkpoint3view);
                    case 4 -> gameBoardGrid.getChildren().remove(checkpoint4view);
                }
            }
        });
    }

    private void addAnimationListener() {
        animationListener = ((observableValue, oldValue, newValue) -> {
            if(newValue.equals("EngergySpace")) {
                for(PlayerData playerData : clientHandler.getClientData().getPlayers()) {
                    for(int i = 0; i < energyList.size(); i++) {
                        if(playerData.getPosition().x() == energyList.get(i).get(0) && playerData.getPosition().y() == energyList.get(i).get(1)) {
                            //ImageView energyspace_empty = new ImageView(new Image(String.valueOf(new File(getClass().getResource("/images/gif/energyspace_empty_gif.gif").toString()))));
                            ImageView energyspace_empty = new ImageView(new Image(String.valueOf(new File(getClass().getClassLoader().getResource("images/gif/energyspace_empty_gif.gif").toString()))));

                            energyspace_empty.setFitHeight(picSize);
                            energyspace_empty.setPreserveRatio(true);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    gameBoardGrid.add(energyspace_empty, playerData.getPosition().x(), playerData.getPosition().y());
                                    removeRobot(playerData.getFigure());
                                    setImageViewToGrid("robothead" + playerData.getFigure(), playerData.getPosition().x(), playerData.getPosition().y());
                                }
                            });
                        }
                    }
                }
                clientHandler.getClientData().setAnimation("");
            } else if(newValue.equals("BlueConveyorBelt")) {
                isAnimationActive = true;
                beltAnimation();
                try {
                    handleAnimation();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if(newValue.equals("GreenConveyorBelt")) {
                isAnimationActive = false;
                beltAnimation();
                try {
                    handleAnimation();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        clientHandler.getClientData().animationProperty().addListener(animationListener);
    }

    private void handleAnimation() throws IOException {
        for(int i = 0; i < beltList.size(); i++) {

            int posX = beltList.get(i).get(1);
            int posY = beltList.get(i).get(2);
            int rotation = beltList.get(i).get(3);
            boolean isMirrored = false;
            if(beltList.get(i).get(4) == 1) {
                isMirrored = true;
            }
            int arrowType = beltList.get(i).get(5);
            String name = "blue_arrow" + arrowType;
            if(beltList.get(i).get(0) == 1) {
                name = "green_arrow" + arrowType;
            }

            String finalName = name;
            boolean finalIsMirrored = isMirrored;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if(isAnimationActive) {
                        try {
                            //if(firstAnimation) {
                                gameBoardGrid.getChildren().remove(getNode(posX, posY));
                            //    firstAnimation = false;
                            //}
                            setGifToGameBoard(finalName, posX, posY, rotation, finalIsMirrored);
                            for(PlayerData playerData : clientHandler.getClientData().getPlayers()) {
                                Position pos = getRobotPosition(playerData.getFigure());
                                removeRobot(playerData.getFigure());
                                setRobotToGrid(playerData.getFigure(), pos.x(), pos.y());
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        try {
                            //if(firstAnimation) {
                                gameBoardGrid.getChildren().remove(getNode(posX, posY));
                            //    firstAnimation = false;
                            //}
                            setPictureNew(finalName, posX, posY, rotation, finalIsMirrored);
                            for(PlayerData playerData : clientHandler.getClientData().getPlayers()) {
                                Position pos = getRobotPosition(playerData.getFigure());
                                removeRobot(playerData.getFigure());
                                setRobotToGrid(playerData.getFigure(), pos.x(), pos.y());
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }
    }

    private void setGifToGameBoard(String name, int posX, int posY, int rotation, boolean isMirrored) throws IOException {
        if(name != null) {
            ImageView imageview;
            imageview = new ImageView(getGif(name));
            imageview.setFitHeight(picSize);
            imageview.setPreserveRatio(true);
            imageview.setRotate(rotation);
            if (isMirrored) {
                imageview.setScaleY(-1);
            }
            gameBoardGrid.add(imageview, posX, posY);
        }
    }

    private Image getGif(String name) throws IOException {
        String green1 = String.valueOf(new File(getClass().getClassLoader().getResource("images/gif/green_1_gif.gif").toString()));
        String green2 = String.valueOf(new File(getClass().getClassLoader().getResource("images/gif/green_2_gif.gif").toString()));
        String blue1 = String.valueOf(new File(getClass().getClassLoader().getResource("images/gif/blue_1_gif.gif").toString()));
        String blue2 = String.valueOf(new File(getClass().getClassLoader().getResource("images/gif/blue_2_gif.gif").toString()));
        String blue3 = String.valueOf(new File(getClass().getClassLoader().getResource("images/gif/blue_3_gif.gif").toString()));
        String blue4 = String.valueOf(new File(getClass().getClassLoader().getResource("images/gif/blue_4_gif.gif").toString()));
        return switch (name) {
            case "blue_arrow1" -> new Image(blue1.replace("\\","/"));
            case "blue_arrow2" -> new Image(blue2.replace("\\","/"));
            case "blue_arrow3" -> new Image(blue3.replace("\\","/"));
            case "blue_arrow4" -> new Image(blue4.replace("\\","/"));
            case "green_arrow1" -> new Image(green1.replace("\\","/"));
            case "green_arrow2" -> new Image(green2.replace("\\","/"));
            case "green_arrow3" -> new Image(getClass().getClassLoader().getResource("images/board/green_3.png").openStream());
            case "green_arrow4" -> new Image(getClass().getClassLoader().getResource("images/board/green_4.png").openStream());

            default -> null;
        };
    }

    private void loadAnimatedBelts() {
        for(int i = 0; i < beltList.size(); i++) {

            int posX = beltList.get(i).get(1);
            int posY = beltList.get(i).get(2);
            int rotation = beltList.get(i).get(3);
            boolean isMirrored = false;
            if(beltList.get(i).get(4) == 1) {
                isMirrored = true;
            }
            int arrowType = beltList.get(i).get(5);
            String name = "blue_arrow" + arrowType;
            if(beltList.get(i).get(0) == 1) {
                name = "green_arrow" + arrowType;
            }

            String finalName = name;
            boolean finalIsMirrored = isMirrored;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        setGifToGameBoard(finalName, posX, posY, rotation, finalIsMirrored);
                        newBeltPosition++;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    private void beltAnimation() {
        if(isAnimationActive) {
            for(int i = 0; i < animatedBeltList.size(); i++){
                getNode(animatedBeltList.get(i).get(0)).setOpacity(1);
            }
        } else {
            for(int i = 0; i < animatedBeltList.size(); i++){
                getNode(animatedBeltList.get(i).get(0)).setOpacity(0);
            }
        }
    }
}


