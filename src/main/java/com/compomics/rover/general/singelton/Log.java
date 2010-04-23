/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 27-Apr-2009
 * Time: 11:20:00
 */
package com.compomics.rover.general.singelton;

import org.apache.log4j.Logger;

import java.util.Observable;


/**
 * The log
 */
public class Log extends Observable {
	// Class specific log4j logger for Log instances.
	 private static Logger logger = Logger.getLogger(Log.class);

    /**
     * The log that will be appended
     */
    public String iLog = "";

    private static Log ourInstance = new Log();

    public static Log getInstance() {
        return ourInstance;
    }

    private Log() {
    }

    /**
     * Method to add a string to the log in a new line
     * @param aLogToAdd String to add
     */
    public void addLog(String aLogToAdd){
        iLog = iLog + aLogToAdd + "\n";
        this.setChanged();
        this.notifyObservers();
    }

    /**
     * Getter for the log
     * @return String with the log
     */
    public String getLog(){
        return iLog;
    }
}
