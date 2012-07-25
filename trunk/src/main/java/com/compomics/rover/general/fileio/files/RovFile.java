package com.compomics.rover.general.fileio.files;

import com.compomics.util.enumeration.CompomicsTools;
import com.compomics.util.io.PropertiesManager;
import org.apache.log4j.Logger;

import com.compomics.util.interfaces.Flamable;
import com.compomics.mascotdatfile.util.mascot.*;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatioGroup;
import com.compomics.rover.general.quantitation.source.distiller.DistillerPeptide;
import com.compomics.rover.general.fileio.readers.QuantitationXmlReader;
import com.compomics.rover.general.fileio.files.DatFile;
import com.compomics.rover.general.interfaces.PeptideIdentification;

import javax.swing.*;
import java.io.*;
import java.util.logging.Level;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 17-jan-2009
 * Time: 16:21:22
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class holds the different elements of the unzipped rov.file (Mascot dat file, the quantitation xml file, ...)
 */
public class RovFile {
    // Class specific log4j logger for RovFile instances.
    private static Logger logger = Logger.getLogger(RovFile.class);

    /**
     * The original rov file
     */
    private File iOriginalRovFile;
    /**
     * The flamable
     */
    private Flamable iFlamable;
    /**
     * The quantitation xml file
     */
    private File iQuantitationXmlFile;
    /**
     * The mascot dat file with the identifications
     */
    private DatFile iMascotIdentificationFile;
    /**
     * The mascot dat file object
     */
    private MascotDatfile_Index iMascotDatFile;
    /**
     * The RatioGroupCollection
     */
    private RatioGroupCollection iRatioGroupCollection;
    /**
     * The threshold
     */
    private double iThreshold = 0.05;
    /**
     * The file path to the orignal file
     */
    private String iFilePath;


     /*
     * Static block that deletes the temp folder on startup.
     */
    static {
        // Only run this on class load!
        try {
            File lTempfolder = File.createTempFile("temp", "temp").getParentFile();
            File lTempRovFolder = new File(lTempfolder, "rover");
            logger.info("Deleting temporary files from '" + lTempRovFolder + "'...");
            if (lTempRovFolder.exists()) {
                deleteDir(lTempRovFolder);
            }
            logger.info("Deleted temporary files from '" + lTempRovFolder + "'.");
        } catch(IOException ioe) {
            logger.warn("Error deleting temporary files!", ioe);
        }
    }


    /**
     * The constructor
     *
     * @param aOriginalRovFile The rov file made by Mascot Distiller Quantitation toolbox
     */
    public RovFile(File aOriginalRovFile) {
        this.iOriginalRovFile = aOriginalRovFile;
        this.iFilePath = iOriginalRovFile.getAbsolutePath();
    }

    /**
     * This method sets the flamable
     *
     * @param aFl The flamable
     */
    public void setFlamable(Flamable aFl) {
        this.iFlamable = aFl;
    }

    /**
     * This method unzippes the original rov file to the temp folder.
     * It checks if the quantitative and peptide identification files are in the rov file.
     *
     * @return boolean True if the needed files are found in the unzipped rov file.
     */
    public boolean unzipRovFile() {
        boolean lNeededFilesFound = false;

        try {
            File lTempfolder = File.createTempFile("temp", "temp").getParentFile();
            File lTempRovFolder = new File(lTempfolder, "rover");

            if (lTempRovFolder.exists() == false) {
                lTempRovFolder.mkdir();
            }

            File lTempUnzippedRovFileFolder = new File(lTempRovFolder, iOriginalRovFile.getName());

            lTempUnzippedRovFileFolder.deleteOnExit();

            if (!lTempUnzippedRovFileFolder.exists()) {
                // Folder does not exist yet.
                if (!lTempUnzippedRovFileFolder.mkdir()) {
                    // Making of folder failed, quit!
                    iFlamable.passHotPotato(new Throwable("Unable to create temporary directory ' " + lTempUnzippedRovFileFolder.getName() + "' for distiller rov project '" + iOriginalRovFile.getName() + "'!!"));
                    // If temporary dir could not be created, return null to stop the process.
                    return false;
                }

                // Unzip the files in the new temp folder

                BufferedWriter out = null;
                ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(iOriginalRovFile)));
                InputStreamReader isr = new InputStreamReader(in);
                ZipEntry entry;
                while ((entry = in.getNextEntry()) != null) {
                    int count;
                    char data[] = new char[1000];

                    // write the files to the disk
                    out = new BufferedWriter(new FileWriter(lTempUnzippedRovFileFolder.getPath() + "/" + entry.getName()), 1000);

                    while ((count = isr.read(data, 0, 1000)) != -1) {
                        out.write(data, 0, count);
                    }
                    out.flush();
                    out.close();
                }
                isr.close();
                in.close();
            }

            // Ok, all files should have been unzipped  in the lTempUnzippedRovFileFolder by now.
            // Try to find the distiller xml file..
            File[] lUnzippedRovFiles = lTempUnzippedRovFileFolder.listFiles();
            boolean lQuantFound = false;
            boolean lDatFileFound = false;
            for (int i = 0; i < lUnzippedRovFiles.length; i++) {
                File lUnzippedRovFile = lUnzippedRovFiles[i];
                if (lUnzippedRovFile.getName().toLowerCase().indexOf("rover_data+bb8") != -1) {
                    lQuantFound = true;
                    iQuantitationXmlFile = lUnzippedRovFile;
                }
                if (lUnzippedRovFile.getName().toLowerCase().indexOf("rover_data+bb9") != -1) {
                    lQuantFound = true;
                    iQuantitationXmlFile = lUnzippedRovFile;
                }
                if (lUnzippedRovFile.getName().toLowerCase().indexOf("mdro_search_status+1") != -1) {
                    lDatFileFound = true;
                    iMascotIdentificationFile = new DatFile(lUnzippedRovFile, iFlamable);
                }
            }

            if (lQuantFound && lDatFileFound) {
                //we found both files, yipee
                lNeededFilesFound = true;
            }
        } catch (IOException e) {
            iFlamable.passHotPotato(new Throwable("Error in parsing the .rov file"));
            logger.error(e.getMessage(), e);
        }

        return lNeededFilesFound;
    }

    /**
     * This method reads the quantitation xml file.
     */
    public void readQuantitationXmlFile() {
        QuantitationXmlReader lReader = new QuantitationXmlReader(iQuantitationXmlFile, iFlamable, iOriginalRovFile.getName());
        iRatioGroupCollection = lReader.getRatioGroupCollection();
    }


    public File getQuantitationXmlFile() {
        return iQuantitationXmlFile;
    }

    public void setThreshold(double iThreshold) {
        this.iThreshold = iThreshold;
    }

    /**
     * This method matches ratio with identifications
     */
    public void match() {
        //System.out.println("Start reading file");
        HashMap<Integer, PeptideIdentification> lIds = iMascotIdentificationFile.extractDatfilePeptideIdentification(iThreshold);
        //System.out.println("Matching ids: " + lIds.size() + " with ratios: " + iRatioGroupCollection.size());
        int lCounter = 0;
        for (int i = 0; i < iRatioGroupCollection.size(); i++) {
            DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) iRatioGroupCollection.get(i);
            //1. Get the DistillerPeptides from the parent hit.
            Vector<DistillerPeptide> lDistillerPeptides = lRatioGroup.getParentHit().getDistillerPeptides();


            //2. Check if the query number from distiller peptides is the same as the query linked to the identification, the sequence of the ratio must be the same as the sequence of the identfication
            //2.1 the query must also be linked to the RatioGroup and not only to the hit
            for (int k = 0; k < lRatioGroup.getDatfileQueries().length; k++) {
                PeptideIdentification lIdent = lIds.get(lRatioGroup.getDatfileQueries()[k]);
                if(lIdent != null){
                    for (int p = 0; p < lDistillerPeptides.size(); p++) {
                        if (lDistillerPeptides.get(p).getQuery() == lRatioGroup.getDatfileQueries()[k]){
                            if(lDistillerPeptides.get(p).getQuery() == lIdent.getDatfile_query() && lRatioGroup.getPeptideSequence().equalsIgnoreCase(lIdent.getSequence())) {
                                lRatioGroup.addIdentification(lIdent, lDistillerPeptides.get(p).getComposition());
                                lCounter = lCounter + 1;
                            }
                        }
                    }
                }
            }
            if(lCounter%1000 == 0){
                System.out.print(".");
            }

        }
        //System.out.println("Matching done ");
        System.gc();
        System.gc();
    }

    /**
     * Getter for the file path of the .rov file
     *
     * @return String with the path of the .rov file
     */
    public String getRovFilePath() {
        return iFilePath;
    }

    /**
     * Getter for the RatioGroupCollection
     *
     * @return RatioGroupCollection
     */
    public RatioGroupCollection getRatioGroupCollection() {
        return iRatioGroupCollection;
    }

    /**
     * toString method
     *
     * @return String with the rov file name
     */
    public String toString() {
        return iOriginalRovFile.getName();
    }

    /**
     * This method will delete every file in a directory
     *
     * @param dir Directory to delete
     * @return
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
    public void editRovFile(File lTempRovFolder){
        String distillerlocation = mcdChecker();
        int exitval = -1;
        try{
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("\"" + distillerlocation + "\" \"" + iOriginalRovFile.getAbsolutePath() + "\" -batch -saveQuantXml -quantout \""  + lTempRovFolder.getParent() + "\\rover_data+bb8_edited\"");
            streamGobbler errorGobbler = new streamGobbler(proc.getErrorStream(), "ERROR");

            // any output
            streamGobbler outputGobbler = new streamGobbler(proc.getInputStream(), "OUTPUT");

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // any error
            exitval = proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //if editing succeeds rezip the archive and add file
        if (exitval == 0) {
            iQuantitationXmlFile = new File(lTempRovFolder.getAbsoluteFile()+"/rover_data+bb8_edited");
        }
        else { logger.error("there was a problem with mascot distiller processing the rov files");}
    }
    private String mcdChecker(){
        Properties props = PropertiesManager.getInstance().getProperties(CompomicsTools.ROVER,"rover.properties");
        boolean checkChosen = false;
        JFileChooser fc = new JFileChooser("C:\\Program Files\\Matrix Science\\Mascot Distiller");
        mcdFileFilter mcdfilter = new mcdFileFilter();
        fc.setFileFilter(mcdfilter);
        if(props.getProperty("distillerlocation") == null) {
            while (!checkChosen) {
                JOptionPane.showMessageDialog(null, "It seems you are using rov files generated with Mascot Distiller 2.4. \n To work with the new files, please select the location of the Mascot Distiller executable");
                fc.showOpenDialog(new JFrame());
                if (fc.getSelectedFile().exists()) {
                    props.put("distillerlocation", fc.getSelectedFile().getAbsolutePath());
                    PropertiesManager.getInstance().updateProperties(CompomicsTools.ROVER,"rover.properties",props);
                    checkChosen = true;
                    return fc.getSelectedFile().getAbsolutePath();
                } else {
                    int ClosePane = JOptionPane.showConfirmDialog(null, "Do you want to stop selecting Mascot Distiller?", "warning", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
                    if (ClosePane == JOptionPane.NO_OPTION) {
                    } else if (ClosePane == JOptionPane.YES_OPTION) {
                        return null;
                    }
                }
            }
        } else {
            String value = props.getProperty("distillerlocation");
            if (!(new File(value)).exists()) {
                JOptionPane.showMessageDialog(null,"the location of Mascot Distiller does not seem to exist anymore, please select the new location of Mascot Distiller");
                if (fc.getSelectedFile().exists()) {
                    props.put("distillerlocation",fc.getSelectedFile().getAbsolutePath());
                    PropertiesManager.getInstance().updateProperties(CompomicsTools.ROVER,"rover.properties",props);
                    return fc.getSelectedFile().getAbsolutePath();
                }
            }
        }
        return props.getProperty("distillerlocation");

    }
    private class streamGobbler extends Thread {

        private InputStream is;
        private String type;
        private OutputStream os;

        streamGobbler(InputStream is, String type) {
            this(is, type, null);
        }

        streamGobbler(InputStream is, String type, OutputStream redirect) {
            this.is = is;
            this.type = type;
            this.os = redirect;
        }
    }
    private class mcdFileFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".exe");
        }

        @Override
        public String getDescription() {
            return ".exe files";
        }
    }
}
