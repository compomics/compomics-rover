package com.compomics.rover.general.fileio.readers;

import com.compomics.rover.general.PeptideIdentification.DefaultPeptideIdentification;
import com.compomics.rover.general.enumeration.DataType;
import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.fileio.files.CensusChro;
import com.compomics.rover.general.fileio.files.CensusOut;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.source.Census.CensusRatio;
import com.compomics.rover.general.quantitation.source.Census.CensusRatioGroup;
import com.compomics.util.interfaces.Flamable;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 25/03/11
 * Time: 09:24
 */
public class LimsMsfInfoReader {
    // Class specific log4j logger for CensusReader instances.
	 private static Logger logger = Logger.getLogger(CensusReader.class);
    /**
     * The ratio group collection
     */
    private RatioGroupCollection iRatioGroupCollection;
    /**
     * The flamable
     */
    private Flamable iFlamable;
    private File iInfoFile;
    private Vector<QuantSpectrum> iQuanSpectra = new Vector<QuantSpectrum>();

    /**
     * The constructor
     */
    public LimsMsfInfoReader(File aInfoFile, Flamable aFlamable, String aFileName){
        //create the files
        this.iFlamable = aFlamable;
        this.iInfoFile = aInfoFile;
        readFile();
    }


        /**
     * This method will read the file
     */
    public void readFile(){
        try {
            //create the ratiogroup collection
            iRatioGroupCollection = new RatioGroupCollection(DataType.PROTEOME_DISCOVERER);


            FileReader freader = new FileReader(iInfoFile);
            LineNumberReader lnreader = new LineNumberReader(freader);
            String line = "";
            //boolean that indicates if the header is parsed
            boolean lInSpectrum = false;
            int lCurrentQuantId = 0;
            Vector<Peak> lPeaks = new Vector<Peak>();
            while ((line = lnreader.readLine()) != null) {
                //System.out.println(line);
                if(line.startsWith("FILE=")){
                    String lFile = line.substring(line.indexOf("=") + 1);
                    iRatioGroupCollection.putMetaData(QuantitationMetaType.FILENAME, lFile);
                }
                if(line.startsWith("RATIO=")){
                    line = line.substring(line.indexOf("=") + 1);
                    String[] lElements = line.split("\t");
                    Vector<String> lType = new Vector<String>();
                    for(int r= 0; r<lElements.length; r++){
                        lType.add(lElements[r]);
                    }
                    iRatioGroupCollection.setRatioTypes(lType);
                } else if(line.startsWith("COMPONENTS=")){
                    line = line.substring(line.indexOf("=") + 1);
                    String[] lElements = line.split("\t");
                    Vector<String> lComp = new Vector<String>();
                    for(int r= 0; r<lElements.length; r++){
                        lComp.add(lElements[r]);
                    }
                    iRatioGroupCollection.setComponentTypes(lComp);
                }else if(line.startsWith("BEGIN IONS")){
                    lInSpectrum = true;
                }else if(line.startsWith("END IONS")){
                    lInSpectrum = false;
                    iQuanSpectra.add(new QuantSpectrum(lCurrentQuantId, (Vector<Peak>) lPeaks.clone()));
                    lPeaks.removeAllElements();
                }
                if(lInSpectrum){
                    if(line.startsWith("QuanResultId=")){
                        lCurrentQuantId = Integer.valueOf(line.substring(line.indexOf("=") +1));
                    }else if(line.startsWith("BEGIN IONS")){
                        //do nothing
                    } else {
                        String[] lElements = line.split("_");
                        double lMass = Double.valueOf(lElements[0]);
                        double lIntensity = Double.valueOf(lElements[1]);
                        String lColor = null;
                        String lLabel = null;
                        if(lElements.length>2){
                            lColor = lElements[2];
                        }
                        if(lElements.length>3){
                            lLabel = lElements[3];
                        }
                        lPeaks.add(new Peak(lMass, lIntensity,  lColor, lLabel));
                    }
                }

            }
        } catch (FileNotFoundException e) {
            iFlamable.passHotPotato(new Throwable("Problem reading the msf file info"));
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            iFlamable.passHotPotato(new Throwable("Problem reading the msf file info"));
            logger.error(e.getMessage(), e);
        } catch (Exception e){
            iFlamable.passHotPotato(new Throwable("Problem reading the msf file info"));
            logger.error(e.getMessage(), e);
        }
    }

    public Vector<QuantSpectrum> getQuantSpectra() {
        return iQuanSpectra;
    }

    public class Peak{

        private double iMass;
        private double iIntensity;
        private String iColor;
        private String iAnnotation;

        public Peak(double lMass, double lIntensity, String lColor, String lAnnotation){
            this.iMass = lMass;
            this.iIntensity = lIntensity;
            this.iColor = lColor;
            this.iAnnotation = lAnnotation;
        }

        public double getMass() {
            return iMass;
        }

        public double getIntensity() {
            return iIntensity;
        }

        public String getColor() {
            return iColor;
        }

        public String getAnnotation() {
            return iAnnotation;
        }
    }

    public class QuantSpectrum{


        private int iQuantId;
        private Vector<Peak> iPeaks;

        public QuantSpectrum(int lQuantId, Vector<Peak> lPeaks){
            this.iQuantId = lQuantId;
            this.iPeaks = lPeaks;
        }

        public int getQuantId() {
            return iQuantId;
        }

        public Vector<Peak> getPeaks() {
            return iPeaks;
        }
    }

    /**
     * This is the getter for the ratiogroupcollection
     * @return RatioGroupCollection
     */
    public RatioGroupCollection getRatioGroupCollection() {
        return iRatioGroupCollection;
    }
}