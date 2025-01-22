package tools;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class ServerLogger {
    private static Logger logger = Logger.getLogger("ClientLogger");
    private static Handler consoleHandler = new ConsoleHandler();
    private static Handler fileHandler;
    private static boolean loggerAlreadySetUp = false;

    private static final Level DEFAULT_LEVEL = Level.CONFIG;

    private static void setUpLogger(){
        if (!loggerAlreadySetUp) {
            logger.setLevel(DEFAULT_LEVEL);
            consoleHandler.setLevel(DEFAULT_LEVEL);
            consoleHandler.setFormatter(new ConsoleFormatter());
            logger.setUseParentHandlers(false);
            logger.addHandler(consoleHandler);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
            String path;
            if(String.valueOf(ServerLogger.class.getClassLoader().getResource("logFiles/").toString()).contains("jar")){
                path = System.getProperty("user.dir")+"/logFiles/";
            }else{
                path = "src/resources/logFiles/";
            }
            File file = new File(path,df.format(new Date()) + "serverLog.lck");
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                if(String.valueOf(ServerLogger.class.getClassLoader().getResource("logFiles/").toString()).contains("jar")){
                    fileHandler = new FileHandler("logFiles/"+df.format(new Date()) + "serverLog.lck");
                }else{
                    fileHandler = new FileHandler("src/resources/logFiles/"+df.format(new Date()) + "serverLog.lck");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.addHandler(fileHandler);
            fileHandler.setLevel(DEFAULT_LEVEL);
            fileHandler.setFormatter(new FileFormatter());
            loggerAlreadySetUp = true;
        }
    }

    private static class FileFormatter extends SimpleFormatter{
        @Override
        public synchronized String format(LogRecord record){
            StringBuilder ret = new StringBuilder();

            ret.append(record.getLevel() + ": ");
            ret.append("Class: " + record.getSourceMethodName() + ", ");
            ret.append("Method: " + record.getSourceMethodName() +"\n");

            ret.append(record.getLevel());
            ret.append(": ");
            ret.append(record.getMessage());
            ret.append("\n");

            return ret.toString();
        }
    }

    private static class ConsoleFormatter extends SimpleFormatter{
        @Override
        public synchronized String format(LogRecord record){
            StringBuilder ret = new StringBuilder();
            if (record.getLevel().intValue() <= Level.INFO.intValue()){
                ret.append("\u001B[37m");
            } else {
                ret.append(record.getLevel() + ": ");
                ret.append("Class: " + record.getSourceMethodName() + ", ");
                ret.append("Method: " + record.getSourceMethodName() +"\n");
            }
            ret.append(record.getLevel());
            ret.append(": ");
            ret.append(record.getMessage());
            ret.append("\n");
            if (record.getLevel().intValue() <= Level.INFO.intValue()){
                ret.append("\u001B[0m");
            }
            return ret.toString();
        }
    }

    public void setLevel(String level){
        switch (level){
            case "SEVERE": setLevel(Level.SEVERE); break;
            case "WARNING": setLevel(Level.WARNING); break;
            case "INFO": setLevel(Level.INFO); break;
            case "CONFIG": setLevel(Level.CONFIG); break;
            case "FINE": setLevel(Level.FINE); break;
            case "FINER": setLevel(Level.FINER); break;
            case "FINEST": setLevel(Level.FINEST); break;
            default: setLevel(DEFAULT_LEVEL);
        }

    }

    public void setLevel(Level level){
        logger.setLevel(level);
        consoleHandler.setLevel(level);
        fileHandler.setLevel(level);
    }

    public static Logger getLogger() {
        setUpLogger();
        return logger;
    }
}
