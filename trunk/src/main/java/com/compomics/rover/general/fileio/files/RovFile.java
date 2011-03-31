package com.compomics.rover.general.fileio.files;

import org.apache.log4j.Logger;

import com.compomics.util.interfaces.Flamable;
import com.compomics.mascotdatfile.util.mascot.*;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatioGroup;
import com.compomics.rover.general.quantitation.source.distiller.DistillerPeptide;
import com.compomics.rover.general.fileio.readers.QuantitationXmlReader;
import com.compomics.rover.general.fileio.files.DatFile;
import com.compomics.rover.general.interfaces.PeptideIdentification;

import java.io.*;
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

                BufferedOutputStream out = null;
                ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(iOriginalRovFile)));
                ZipEntry entry;
                while ((entry = in.getNextEntry()) != null) {
                    int count;
                    byte data[] = new byte[1000];

                    // write the files to the disk
                    out = new BufferedOutputStream(new FileOutputStream(lTempUnzippedRovFileFolder.getPath() + "/" + entry.getName()), 1000);

                    while ((count = in.read(data, 0, 1000)) != -1) {
                        out.write(data, 0, count);
                    }
                    out.flush();
                    out.close();
                }
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


}
