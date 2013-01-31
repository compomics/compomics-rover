package com.compomics.rover.general;


import com.compomics.util.enumeration.CompomicsTools;
import com.compomics.util.io.PropertiesManager;


import java.io.File;
import java.io.IOException;
import java.util.Properties;


/**
 * A wrapper class used to start the jar file with parameters. The parameters are read from the JavaOptions file in the
 * Properties folder.
 *
 * @author Kenny Helsens
 */
public class RoverStarter {


    /**
     * Starts the launcher by calling the launch method. Use this as the main class in the jar file.
     */
    public RoverStarter() {

        try {
            launch();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Launches the jar file with parameters to the jvm.
     *
     * @throws java.lang.Exception
     */
    private void launch() throws Exception {

        // get the version number set in the pom file
        Properties lProperties = PropertiesManager.getInstance().getProperties(CompomicsTools.ROVER, "rover.properties");

        /**
         * The name of the rover parser jar file. Must be equal to the name
         * given in the pom file.
         */
        String jarFileName = "rover-" + lProperties.get("version") + ".jar";

        // Get the jarFile path.
        String path;
        path = this.getClass().getResource("RoverStarter.class").getPath();
        path = path.substring(5, path.indexOf(jarFileName));
        path = path.replace("%20", " ");

        // Get Java vm options.
        String options = lProperties.get("java").toString();


        String quote = "";
        if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
            quote = "\"";
        }

        String javaHome = System.getProperty("java.home") + File.separator +
                "bin" + File.separator;

        String cmdLine = javaHome + "java " + options + " -cp " + quote
                + new File(path, jarFileName).getAbsolutePath()
                + quote + " com.compomics.rover.gui.wizard.WizardFrameHolder";

        try {
            // Run the process!
            System.out.println(cmdLine);
            Runtime.getRuntime().exec(cmdLine);
            Thread.sleep(10000);

        } catch (IOException e1) {
            System.err.println(e1.getMessage());
            e1.printStackTrace();
        } catch (Throwable t) {
            System.err.println(t.getMessage());
            t.printStackTrace();
        }

        finally {
            //System.exit(0);
        }
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main class in the jar file.
     *
     * @param args
     */
    public static void main(String[] args) {
        new RoverStarter();
    }
}