package field.reducedFields;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import field.PushPanel;
import server.protocol.Message;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;


/**
 * Diese Klasse enthält die Methoden, welche zum (De-)Serialisieren von Nachrichten genutzt werden.
 *
 * @author David, Lea
 */
public class FieldDeserializer {
    //private static final ArrayList<ClassType> FIELD_TYPES = getAllFieldTypes(); //Speichert alle Messagetypen mit messageType und vollqualifiziertem Klassennamen

    /**
     * Wandelt einen JSON String in richtigen Nachrichtentyp zurück
     *
     * @param serializedMessage Serialisierte Nachricht als JSON Object
     * @return deserialisierte Nachricht
     * @author David, Lea
     */
    public static ArrayList<ArrayList<ArrayList<ReducedField>>> deserializeMessage(JsonObject serializedMessage) {
        Gson gson = new Gson();
        ArrayList<ArrayList<ArrayList<ReducedField>>> gameboard = new ArrayList<ArrayList<ArrayList<ReducedField>>>();
        JsonArray wholeGameBoard = serializedMessage.get("gameMap").getAsJsonArray();
        for (JsonElement yElements : wholeGameBoard) {
            ArrayList<ArrayList<ReducedField>> listY = new ArrayList<ArrayList<ReducedField>>();
            JsonArray yArray = yElements.getAsJsonArray();
            for (JsonElement fieldElements : yArray) {
                ArrayList<ReducedField> deserializedFieldElements = new ArrayList<ReducedField>();
                JsonArray fieldElementsArray = fieldElements.getAsJsonArray();
                if (fieldElementsArray.size() == 0) {
                    listY.add(null);
                }else {
                    for (JsonElement serializedField : fieldElementsArray) {
                        ReducedField deserializedField = null;
                        try {
                            String fieldType = serializedField.getAsJsonObject().get("type").getAsString();
                            switch (fieldType) {
                                case "Antenna":
                                    deserializedField = gson.fromJson(serializedField, ReducedAntenna.class); break;
                                case "CheckPoint":
                                    deserializedField = gson.fromJson(serializedField, ReducedCheckPoint.class); break;
                                case "ConveyorBelt":
                                    deserializedField = gson.fromJson(serializedField, ReducedConveyorBelt.class); break;
                                case "Empty":
                                    deserializedField = gson.fromJson(serializedField, ReducedEmpty.class); break;
                                case "EnergySpace":
                                    deserializedField = gson.fromJson(serializedField, ReducedEnergySpace.class); break;
                                case "Gear":
                                    deserializedField = gson.fromJson(serializedField, ReducedGear.class); break;
                                case "Laser":
                                    deserializedField = gson.fromJson(serializedField, ReducedLaser.class); break;
                                case "Pit":
                                    deserializedField = gson.fromJson(serializedField, ReducedPit.class); break;
                                case "PushPanel":
                                    deserializedField = gson.fromJson(serializedField, ReducedPushPanel.class); break;
                                case "RestartPoint":
                                    deserializedField = gson.fromJson(serializedField, ReducedRestartPoint.class); break;
                                case "StartPoint":
                                    deserializedField = gson.fromJson(serializedField, ReducedStartPoint.class); break;
                                case "Wall":
                                    deserializedField = gson.fromJson(serializedField, ReducedWall.class); break;
                                default:
                                    deserializedField = null;
                            }
                            /*
                            for (ClassType m : FIELD_TYPES) {
                                if (fieldType.equals(m.getSimpleName())) {
                                    deserializedField = (ReducedField) gson.fromJson(serializedField, m.getClassPath());
                                }
                            }

                             */
                        } catch (Exception e) {

                        } finally {
                            if (deserializedField.equals(null)) {
                                deserializedFieldElements.add(null);
                            } else {
                                deserializedFieldElements.add(deserializedField);
                            }
                        }
                    }
                    listY.add(deserializedFieldElements);

                }
            }
            gameboard.add(listY);
        }
        return gameboard;
    }

    public static ArrayList<ClassType> getAllFieldTypes() {
        ArrayList<ClassType> classTypes = new ArrayList<>();
        ArrayList<File> dirs = new ArrayList();
        String packageName = FieldDeserializer.class.getPackageName();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        assert classLoader != null;
        String path = packageName.replace('.', '/');

        try {
            Enumeration resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = (URL) resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            for (File directory : dirs) {
                classTypes.addAll(findClasses(directory, packageName));
            }
        } catch (IOException e) {}
        return classTypes;

    }

    private static ArrayList<ClassType> findClasses(File directory, String packageName) {
        ArrayList<ClassType> classTypes = new ArrayList<>();

        if (!directory.exists()) {
            return classTypes;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                classTypes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")
                    && !file.getName().equals("FieldDeserializer.class")) {
                try {
                    Class aClass = (Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                    String className = file.getName().substring(7, file.getName().length() - 6);
                    ClassType classType = new ClassType(className, aClass);
                    classTypes.add(classType);
                } catch (ClassNotFoundException e) {}
            }
        }
        return classTypes;
    }


    static class ClassType {
        private String simpleName;
        private Class classPath;

        public ClassType(String simpleName, Class classPath) {
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
