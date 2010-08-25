package com.compomics.rover.general.fileio.readers;

import org.apache.log4j.Logger;

import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.source.DatFileiTraq.ITraqRatio;
import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.enumeration.DataType;
import com.compomics.rover.general.PeptideIdentification.MsQuantPeptideIdentification;
import com.compomics.util.interfaces.Flamable;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 3-apr-2009
 * Time: 13:36:33
 */

/**
 * This class will create a RatioGroupCollection for MsQuant result files
 */
public class MsQuantReader {
	// Class specific log4j logger for MsQuantReader instances.
	 private static Logger logger = Logger.getLogger(MsQuantReader.class);
    /**
     * The MsQuant file
     */
    private File iMsQuantFile;

    private Vector<String[]> iPeptideLines = new Vector<String[]>();

    /**
     * HashMap with the header titles as key, the value is the position in the header
     */
    private HashMap iHeaderMap = new HashMap();
    /**
     * The ratiogroup collection
     */
    private RatioGroupCollection iRatioGroupCollection;
    /**
     * The flamable
     */
    private Flamable iFlamable;

    private int lNumberOfIntensities = 0;


    /**
     * Constructor
     * @param aMsQuantFile The MsQuant file
     */
    public MsQuantReader(File aMsQuantFile, Flamable aFlamable){
        this.iFlamable = aFlamable;
        this.iMsQuantFile = aMsQuantFile;
        this.readFile();
    }

    /**
     * This method will read the file
     */
    public void readFile(){
        try {
            FileReader freader = new FileReader(iMsQuantFile);
            LineNumberReader lnreader = new LineNumberReader(freader);
            String line = "";
            //boolean that indicates if the header is parsed
            boolean lHeaderParsed = false;
            while ((line = lnreader.readLine()) != null) {
                //check if the file has tabs
                if(line.startsWith("Dishes")){
                    lNumberOfIntensities = Integer.valueOf(line.substring(line.indexOf(":") + 2).trim());   
                } else if(line.length() != 0 && line.indexOf("\t")>0){
                    //check if it could be the headet
                    if(line.indexOf("Accession") > 0 && line.indexOf("Sequence") > 0){
                        //it's the header
                        if(!lHeaderParsed){
                            String [] lHeader = line.split("\t");
                            for(int i = 0; i<lHeader.length; i ++){
                                iHeaderMap.put(lHeader[i], i);
                            }
                            lHeaderParsed = true;
                        }
                    } else {
                        //it will be a peptide line if it's after we parsed the header
                        if(lHeaderParsed){
                            if(line.startsWith("PROTEIN") || line.indexOf("Accession") > 0){
                                //this is a protein line or a header line, we are not interested
                            } else {
                                iPeptideLines.add(line.split("\t"));
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            iFlamable.passHotPotato(new Throwable("Problem reading the msquant file"));
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            iFlamable.passHotPotato(new Throwable("Problem reading the msquant file"));
            logger.error(e.getMessage(), e);
        } catch (Exception e){
            iFlamable.passHotPotato(new Throwable("Problem reading the msquant file"));
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Getter for the RatioGroupCollection
     * @return RatioGroupCollection
     */
    public RatioGroupCollection getRatioGroupCollection(){
        iRatioGroupCollection = new RatioGroupCollection(DataType.MSQUANT);
        Vector<String> lComp = new Vector<String>();
        if(lNumberOfIntensities == 2){
            lComp.add("light");
            lComp.add("heavy");
        } else if (lNumberOfIntensities == 3){
            lComp.add("light");
            lComp.add("medium");
            lComp.add("heavy");
        } else if (lNumberOfIntensities == 4){
            lComp.add("1");
            lComp.add("2");
            lComp.add("3");
            lComp.add("4");
        }
        Vector<String> lType = new Vector<String>();
        if(lNumberOfIntensities == 2){
            lType.add("L/H");
        } else if (lNumberOfIntensities == 3){
            lType.add("L/H");
            lType.add("L/M");
            lType.add("M/H");
        } else if (lNumberOfIntensities == 4){
            lType.add("2/1");
            lType.add("3/1");
            lType.add("4/1");
        }
        iRatioGroupCollection.setComponentTypes(lComp);
        iRatioGroupCollection.setRatioTypes(lType);
        int lOne = (Integer) iHeaderMap.get("Intensity 1");
        int lTwo = (Integer) iHeaderMap.get("Intensity 2");
        int lThree = 0;
        int lFour = 0;
        try{
            lThree = (Integer) iHeaderMap.get("Intensity 3");
            lFour = (Integer) iHeaderMap.get("Intensity 4");
        } catch(Exception e){
            // no problem if it is not found
        }
        //set the filename
        iRatioGroupCollection.putMetaData(QuantitationMetaType.FILENAME, iMsQuantFile.getName());

        //create for every peptide a ratiogroup and add it to the ratiogroup collection
        for(int i = 0; i<iPeptideLines.size(); i ++){

            if( i == 0){
                logger.info("Reading results for: " + iPeptideLines.get(i)[((Integer)iHeaderMap.get("Result file")).intValue()]  + " (" + iMsQuantFile.getName() + ")");
            }
            //create the ratiogroup
            RatioGroup lRatioGroup = new RatioGroup(iRatioGroupCollection);
            if(lNumberOfIntensities == 2){
                //create a ratio
                double lCalRatio = Math.round(Double.valueOf(iPeptideLines.get(i)[lOne]) / Double.valueOf(iPeptideLines.get(i)[lTwo])*1000.0)/1000.0;
                ITraqRatio lRatio = new ITraqRatio(lCalRatio, "L/H", true, lRatioGroup);
                //add the ratio to the ratiogroup
                lRatio.setValid(new Boolean (iPeptideLines.get(i)[((Integer)iHeaderMap.get("Used in protein quantitation")).intValue()]));
                lRatioGroup.addRatio(lRatio);
            } else if (lNumberOfIntensities == 3){

                //create a ratio
                double lCalRatio = Math.round(Double.valueOf(iPeptideLines.get(i)[lOne]) / Double.valueOf(iPeptideLines.get(i)[lTwo])*1000.0)/1000.0;
                ITraqRatio lRatio = new ITraqRatio(lCalRatio, "L/M", true, lRatioGroup);
                //add the ratio to the ratiogroup
                lRatio.setValid(new Boolean (iPeptideLines.get(i)[((Integer)iHeaderMap.get("Used in protein quantitation")).intValue()]));
                lRatioGroup.addRatio(lRatio);

                //create a ratio
                double lCalRatioLH = Math.round(Double.valueOf(iPeptideLines.get(i)[lOne]) / Double.valueOf(iPeptideLines.get(i)[lThree])*1000.0)/1000.0;
                ITraqRatio lRatioLH = new ITraqRatio(lCalRatioLH, "L/H", true, lRatioGroup);
                //add the ratio to the ratiogroup
                lRatioLH.setValid(new Boolean (iPeptideLines.get(i)[((Integer)iHeaderMap.get("Used in protein quantitation")).intValue()]));
                lRatioGroup.addRatio(lRatioLH);

                //create a ratio
                double lCalRatioMH = Math.round(Double.valueOf(iPeptideLines.get(i)[lTwo]) / Double.valueOf(iPeptideLines.get(i)[lThree])*1000.0)/1000.0;
                ITraqRatio lRatioMH = new ITraqRatio(lCalRatioMH, "M/H", true, lRatioGroup);
                //add the ratio to the ratiogroup
                lRatioMH.setValid(new Boolean (iPeptideLines.get(i)[((Integer)iHeaderMap.get("Used in protein quantitation")).intValue()]));
                lRatioGroup.addRatio(lRatioMH);
            } else if (lNumberOfIntensities == 4){

                //create a ratio
                double lCalRatio = Math.round(Double.valueOf(iPeptideLines.get(i)[lTwo]) / Double.valueOf(iPeptideLines.get(i)[lOne])*1000.0)/1000.0;
                ITraqRatio lRatio = new ITraqRatio(lCalRatio, "2/1", true, lRatioGroup);
                //add the ratio to the ratiogroup
                lRatio.setValid(new Boolean (iPeptideLines.get(i)[((Integer)iHeaderMap.get("Used in protein quantitation")).intValue()]));
                lRatioGroup.addRatio(lRatio);

                //create a ratio
                double lCalRatioLH = Math.round(Double.valueOf(iPeptideLines.get(i)[lThree]) / Double.valueOf(iPeptideLines.get(i)[lOne])*1000.0)/1000.0;
                ITraqRatio lRatioLH = new ITraqRatio(lCalRatioLH, "3/1", true, lRatioGroup);
                //add the ratio to the ratiogroup
                lRatioLH.setValid(new Boolean (iPeptideLines.get(i)[((Integer)iHeaderMap.get("Used in protein quantitation")).intValue()]));
                lRatioGroup.addRatio(lRatioLH);

                //create a ratio
                double lCalRatioMH = Math.round(Double.valueOf(iPeptideLines.get(i)[lFour]) / Double.valueOf(iPeptideLines.get(i)[lOne])*1000.0)/1000.0;
                ITraqRatio lRatioMH = new ITraqRatio(lCalRatioMH, "4/1", true, lRatioGroup);
                //add the ratio to the ratiogroup
                lRatioMH.setValid(new Boolean (iPeptideLines.get(i)[((Integer)iHeaderMap.get("Used in protein quantitation")).intValue()]));
                lRatioGroup.addRatio(lRatioMH);
            }


                                                                            
            //create an identification and add it to the ratio
            HashMap lIdentificationParameters = new HashMap();
            String lScore = iPeptideLines.get(i)[((Integer)iHeaderMap.get("Score")).intValue()];
            if(lScore.indexOf(".")>0){
                lScore = lScore.substring(0, lScore.indexOf("."));
            }
            lIdentificationParameters.put("SCORE", Long.valueOf(lScore));
            lIdentificationParameters.put("MODIFIED_SEQUENCE", iPeptideLines.get(i)[((Integer)iHeaderMap.get("Modifications")).intValue()]);
            lIdentificationParameters.put("CAL_MASS", Double.valueOf(iPeptideLines.get(i)[((Integer)iHeaderMap.get("Measured mass [Da]")).intValue()]));
            lIdentificationParameters.put("EXP_MASS", Double.valueOf(iPeptideLines.get(i)[((Integer)iHeaderMap.get("Mascot calculated mass [Da]")).intValue()]));
            lIdentificationParameters.put("SEQUENCE",iPeptideLines.get(i)[((Integer)iHeaderMap.get("Sequence")).intValue()]);
            lIdentificationParameters.put("VALID", new Integer(1).intValue());
            lIdentificationParameters.put("PRECURSOR",  Double.valueOf(iPeptideLines.get(i)[((Integer)iHeaderMap.get("MCR (Mass charge ratio) [Th]")).intValue()]));
            lIdentificationParameters.put("CHARGE", Integer.valueOf(iPeptideLines.get(i)[((Integer)iHeaderMap.get("Charge")).intValue()]));
            lIdentificationParameters.put("TITLE", null);
            lIdentificationParameters.put("DATFILE_QUERY", new Long(iPeptideLines.get(i)[((Integer)iHeaderMap.get("Query number")).intValue()]));
            lIdentificationParameters.put("ACCESSION", iPeptideLines.get(i)[((Integer)iHeaderMap.get("Accession number")).intValue()]);
            lIdentificationParameters.put("END", new Long (iPeptideLines.get(i)[((Integer)iHeaderMap.get("End position in protein")).intValue()]));
            lIdentificationParameters.put("START", new Long (iPeptideLines.get(i)[((Integer)iHeaderMap.get("Start position in protein")).intValue()]));
            lIdentificationParameters.put("DESCRIPTION",iPeptideLines.get(i)[((Integer)iHeaderMap.get("Description")).intValue()]);
            lIdentificationParameters.put("ISOFORMS", "");

            MsQuantPeptideIdentification lid = new MsQuantPeptideIdentification(lIdentificationParameters);

            lRatioGroup.addIdentification(lid, "light");
            lRatioGroup.setPeptideSequence(lid.getSequence());
            iRatioGroupCollection.add(lRatioGroup);

        }

        return iRatioGroupCollection;
    }
}
