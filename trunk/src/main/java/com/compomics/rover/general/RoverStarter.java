package com.compomics.rover.general;


import com.compomics.software.CompomicsWrapper;
import com.compomics.util.io.PropertiesManager;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;


import java.io.File;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;

/**
 * A wrapper class used to start the jar file with parameters. The parameters
 * are read from the JavaOptions file in the Properties folder.
 *
 * @author Kenny Helsens
 */
public class RoverStarter extends CompomicsWrapper {
    private static final Logger logger = Logger.getLogger(RoverStarter.class);
    
    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     */
    public RoverStarter(String[] args) {
        try {
            PlasticLookAndFeel.setPlasticTheme(new SkyKrupp());
            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            // ignore exception
        }

        try {
            launch(args);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * Launches the jar file with parameters to the jvm.
     *
     * @throws java.lang.Exception
     */
    private void launch(String[] args) throws Exception {

        File jarFile = new File(RoverStarter.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String mainClass = "com.compomics.rover.gui.wizard.WizardFrameHolder";
        launchTool("Rover", jarFile, null, mainClass, args);

    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args
     */
    public static void main(String[] args) {
        new RoverStarter(args);
    }
}
