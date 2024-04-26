package services;


import exceptions.CustomException;


import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CustomLogger {

    private static final boolean LOGGING_IN_FILE;
    private static final String LOGGING_PATH;
    private FileHandler fh;
    private final Logger logger;

    static{
        Properties props = new Properties();
        try {
            props.load(CustomLogger.class.getClassLoader().getResourceAsStream("application.properties"));
            LOGGING_IN_FILE = Boolean.parseBoolean(props.getProperty("log.in_file"));
            LOGGING_PATH = props.getProperty("log.path");
        } catch (IOException e) {
            throw new CustomException(e);
        }
    }

    public CustomLogger(String name){
        logger = Logger.getLogger(name);
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(true);
    }


    public void log(Level lvl, String log) throws IOException{

        if(LOGGING_IN_FILE && fh == null && lvl == Level.SEVERE){
            File path = new File(LOGGING_PATH);
            boolean existPath = true;
            if(!path.exists()) {
                existPath = path.mkdir();
            }

            if(path.exists() || existPath) {
                fh = new FileHandler(
                         LOGGING_PATH
                                + "log"
                                +System.currentTimeMillis()
                                +".log"
                );
                fh.setFormatter(new SimpleFormatter());
                logger.addHandler(fh);
            }
        }

        logger.log(lvl, log);
    }

}
