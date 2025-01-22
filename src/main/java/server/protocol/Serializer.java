package server.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import field.reducedFields.FieldDeserializer;
import field.reducedFields.ReducedField;
import server.protocol.aktionen.*;
import server.protocol.chatnachrichten.ConnectionUpdate;
import server.protocol.chatnachrichten.Error;
import server.protocol.chatnachrichten.ReceivedChat;
import server.protocol.chatnachrichten.SendChat;
import server.protocol.lobby.*;
import server.protocol.spielkarten.CardPlayed;
import server.protocol.spielkarten.PlayCard;
import server.protocol.spielzug.*;
import server.protocol.verbindungaufbau.Alive;
import server.protocol.verbindungaufbau.HelloClient;
import server.protocol.verbindungaufbau.HelloServer;
import server.protocol.verbindungaufbau.Welcome;
import tools.ClientLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;



/**
 * Diese Klasse enthält die Methoden, welche zum (De-)Serialisieren von Nachrichten genutzt werden.
 * @author David, Lea
 * */
public class Serializer
{
    //private static final ArrayList<ClassType> MESSAGE_TYPES = getAllMessageTypes(); //Speichert alle Messagetypen mit messageType und vollqualifiziertem Klassennamen
    private static Gson gson = new Gson();
    /**
     * Gibt eine JSON der server.message.Message in Form eines Strings zurück
     * Zusätzlicher property, um beim Deserialisieren eine Instanz der ursprünglichen Unterklasse von Message.java zu erzeugen
     * @param message zu serialisierende Nachricht
     * @return serialisierte Nachricht als JSON String
     * @author David, Lea
     * */
    public static String serializeMessage(server.protocol.Message message){
        JsonElement jsonElement = gson.toJsonTree(message);
        //System.out.println(jsonElement);
        return gson.toJson(jsonElement);
    }


    /**
     * Wandelt einen JSON String in richtigen Nachrichtentyp zurück
     * @param serializedMessage Serialisierte Nachricht als JSON String
     * @return deserialisierte Nachricht
     * @author David, Lea
     * */
    public static Message deserializeMessage(String serializedMessage) {
        Message message = null;
        JsonObject jsonObject = new Gson().fromJson(serializedMessage, JsonObject.class);
        String typeOfSerializedMessage = jsonObject.get("messageType").getAsString();
        //System.out.println(serializedMessage);

        switch (typeOfSerializedMessage) {
            //Verbindungsaufbau
            case "HelloClient":
                message = gson.fromJson(jsonObject, HelloClient.class); break;
            case "Alive":
                message = gson.fromJson(jsonObject, Alive.class); break;
            case "HelloServer":
                message = gson.fromJson(jsonObject, HelloServer.class); break;
            case "Welcome":
                message = gson.fromJson(jsonObject, Welcome.class); break;
            case "PlayerValues":
                message = gson.fromJson(jsonObject, PlayerValues.class); break;
            case "PlayerAdded":
                message = gson.fromJson(jsonObject, PlayerAdded.class); break;
            case "SetStatus":
                message = gson.fromJson(jsonObject, SetStatus.class); break;
            case "PlayerStatus":
                message = gson.fromJson(jsonObject, PlayerStatus.class); break;
            case "SelectMap":
                message = gson.fromJson(jsonObject, SelectMap.class); break;
            case "MapSelected":
                message = gson.fromJson(jsonObject, MapSelected.class); break;
            case "GameStarted":
                JsonObject messageBody = jsonObject.get("messageBody").getAsJsonObject();
                ArrayList<ArrayList<ArrayList<ReducedField>>> gameBoard = FieldDeserializer.deserializeMessage(messageBody);
                message = new GameStarted(gameBoard); break;
            case "SendChat":
                message = gson.fromJson(jsonObject, SendChat.class); break;
            case "ReceivedChat":
                message = gson.fromJson(jsonObject, ReceivedChat.class); break;
            case "Error":
                message = gson.fromJson(jsonObject, Error.class); break;
            case "ConnectionUpdate":
                message = gson.fromJson(jsonObject, ConnectionUpdate.class); break;
            case "PlayCard":
                message = gson.fromJson(jsonObject, PlayCard.class); break;
            case "CardPlayed":
                message = gson.fromJson(jsonObject, CardPlayed.class); break;
            case "CurrentPlayer":
                message = gson.fromJson(jsonObject, CurrentPlayer.class); break;
            case "ActivePhase":
                message = gson.fromJson(jsonObject, ActivePhase.class); break;
            case "SetStartingPoint":
                message = gson.fromJson(jsonObject, SetStartingPoint.class); break;
            case "StartingPointTaken":
                message = gson.fromJson(jsonObject, StartingPointTaken.class); break;
            case "RefillShop":
                message = gson.fromJson(jsonObject, RefillShop.class); break;
            case "ExchangeShop":
                message = gson.fromJson(jsonObject, ExchangeShop.class); break;
            case "BuyUpgrade":
                message = gson.fromJson(jsonObject, BuyUpgrade.class); break;
            case "UpgradeBought":
                message = gson.fromJson(jsonObject, UpgradeBought.class); break;
            case "YourCards":
                message = gson.fromJson(jsonObject, YourCards.class); break;
            case "NotYourCards":
                message = gson.fromJson(jsonObject, NotYourCards.class); break;
            case "ShuffleCoding":
                message = gson.fromJson(jsonObject, ShuffleCoding.class); break;
            case "SelectedCard":
                message = gson.fromJson(jsonObject, SelectedCard.class); break;
            case "CardSelected":
                message = gson.fromJson(jsonObject, CardSelected.class); break;
            case "SelectionFinished":
                message = gson.fromJson(jsonObject, SelectionFinished.class); break;
            case "TimerStarted":
                message = gson.fromJson(jsonObject, TimerStarted.class); break;
            case "TimerEnded":
                message = gson.fromJson(jsonObject, TimerEnded.class); break;
            case "CardsYouGotNow":
                message = gson.fromJson(jsonObject, CardsYouGotNow.class); break;
            case "CurrentCards":
                message = gson.fromJson(jsonObject, CurrentCards.class); break;
            case "ReplaceCard":
                message = gson.fromJson(jsonObject, ReplaceCard.class); break;
            case "Movement":
                message = gson.fromJson(jsonObject, Movement.class); break;
            case "PlayerTurning":
                message = gson.fromJson(jsonObject, PlayerTurning.class); break;
            case "DrawDamage":
                message = gson.fromJson(jsonObject, DrawDamage.class); break;
            case "PickDamage":
                message = gson.fromJson(jsonObject, PickDamage.class); break;
            case "SelectedDamage":
                message = gson.fromJson(jsonObject, SelectedDamage.class); break;
            case "Animation":
                message = gson.fromJson(jsonObject, Animation.class); break;
            case "Reboot":
                message = gson.fromJson(jsonObject, Reboot.class); break;
            case "RebootDirection":
                message = gson.fromJson(jsonObject, RebootDirection.class); break;
            case "Energy":
                message = gson.fromJson(jsonObject, Energy.class); break;
            case "CheckPointReached":
                message = gson.fromJson(jsonObject, CheckPointReached.class); break;
            case "GameFinished":
                message = gson.fromJson(jsonObject, GameFinished.class); break;
            case "DiscardSome":
                message = gson.fromJson(jsonObject, DiscardSome.class); break;
            case "Boink":
                message = gson.fromJson(jsonObject, Boink.class); break;
            case "CheckpointMoved":
                message = gson.fromJson(jsonObject, CheckpointMoved.class); break;
            case "ChooseRegister":
                message = gson.fromJson(jsonObject, ChooseRegister.class); break;
            case "RegisterChosen":
                message = gson.fromJson(jsonObject, RegisterChosen.class); break;
            case "ReturnCards":
                message = gson.fromJson(jsonObject, ReturnCards.class); break;
            default:
                message = gson.fromJson(jsonObject, DummyMessage.class);
        }
        /*
        if (typeOfSerializedMessage.equals("GameStarted")){
            JsonObject messageBody = jsonObject.get("messageBody").getAsJsonObject();
            ArrayList<ArrayList<ArrayList<ReducedField>>> gameBoard = FieldDeserializer.deserializeMessage(messageBody);
            message = new GameStarted(gameBoard);

        } else {
            for (ClassType m : MESSAGE_TYPES) {
                if (typeOfSerializedMessage.equals(m.getSimpleName())) {
                    message = (Message) gson.fromJson(jsonObject, m.getClassPath());

                }
            }
        }

         */
        //TODO: Logging message != null
        return message;
    }

    public static ArrayList <ClassType> getAllMessageTypes() {
        ArrayList <ClassType> classTypes = new ArrayList<>();
        ArrayList <File>  dirs = new ArrayList();
        String packageName = Serializer.class.getPackageName();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        assert classLoader != null;
        String path = packageName.replace('.', '/');

        try {
            Enumeration resources = classLoader.getResources(path);
            while (resources.hasMoreElements()){
                URL resource = (URL) resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            for (File directory : dirs){
                classTypes.addAll(findClasses(directory, packageName));
            }
        } catch (IOException e) {
        }
        return classTypes;

    }

    private static ArrayList<ClassType> findClasses(File directory, String packageName) {
        ArrayList <ClassType> classTypes = new ArrayList<>();

        if (!directory.exists()){
            return classTypes;
        }

        File[] files = directory.listFiles();
        for (File file : files){

            if (file.isDirectory()){
                classTypes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")
                    && !(file.getName().equals("Message.class") || file.getName().equals("Serializer.class"))){
                try {
                    Class aClass = (Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    ClassType classType = new ClassType(className, aClass);
                    classTypes.add(classType);
                } catch (ClassNotFoundException e) {
                }
            }
        }
        return classTypes;
    }

    static class ClassType{
        private String simpleName;
        private Class classPath;

        public ClassType (String simpleName, Class classPath){
            this.simpleName = simpleName;
            this.classPath = classPath;
        }

        public Class getClassPath() {
            return classPath;
        }

        public String getSimpleName() {
            return simpleName;
        }
    }
}
