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
                if(line.length() != 0 && line.indexOf("\t")>0){
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
        lComp.add("light");
        lComp.add("heavy");
        Vector<String> lType = new Vector<String>();
        lType.add("L/H");
        iRatioGroupCollection.setComponentTypes(lComp);
        iRatioGroupCollection.setRatioTypes(lType);

        //set the filename
        iRatioGroupCollection.putMetaData(QuantitationMetaType.FILENAME, iMsQuantFile.getName());

        //create for every peptide a ratiogroup and add it to the ratiogroup collection
        for(int i = 0; i<iPeptideLines.size(); i ++){

            if( i == 0){
                logger.info("Reading results for: " + iPeptideLines.get(i)[(Integer)iHeaderMap.get("Result file")]  + " (" + iMsQuantFile.getName() + ")");
            }
            //create the ratiogroup
            RatioGroup lRatioGroup = new RatioGroup(iRatioGroupCollection);
            //create a ratio
            double lCalRatio = Math.round(Double.valueOf(iPeptideLines.get(i)[(Integer) iHeaderMap.get("Intensity 1")]) / Double.valueOf(iPeptideLines.get(i)[(Integer)iHeaderMap.get("Intensity 2")])*1000.0)/1000.0;
            ITraqRatio lRatio = new ITraqRatio(lCalRatio, "L/H", true, lRatioGroup);
            //add the ratio to the ratiogroup
            lRatio.setValid(new Boolean (iPeptideLines.get(i)[(Integer)iHeaderMap.get("Used in protein quantitation")]));
            lRatioGroup.addRatio(lRatio);
                                                                            
            //create an identification and add it to the ratio
            HashMap lIdentificationParameters = new HashMap();
            lIdentificationParameters.put("SCORE", Long.valueOf(iPeptideLines.get(i)[(Integer)iHeaderMap.get("Score")].substring(0,iPeptideLines.get(i)[(Integer)iHeaderMap.get("Score")].indexOf(".") )));
            lIdentificationParameters.put("MODIFIED_SEQUENCE", iPeptideLines.get(i)[(Integer)iHeaderMap.get("Modifications")]);
            lIdentificationParameters.put("CAL_MASS", Double.valueOf(iPeptideLines.get(i)[(Integer)iHeaderMap.get("Measured mass [Da]")]));
            lIdentificationParameters.put("EXP_MASS", Double.valueOf(iPeptideLines.get(i)[(Integer)iHeaderMap.get("Mascot calculated mass [Da]")]));
            lIdentificationParameters.put("SEQUENCE",iPeptideLines.get(i)[(Integer)iHeaderMap.get("Sequence")]);
            lIdentificationParameters.put("VALID", new Integer(1));
            lIdentificationParameters.put("PRECURSOR",  Double.valueOf(iPeptideLines.get(i)[(Integer)iHeaderMap.get("MCR (Mass charge ratio) [Th]")]));
            lIdentificationParameters.put("CHARGE", Integer.valueOf(iPeptideLines.get(i)[(Integer)iHeaderMap.get("Charge")]));
            lIdentificationParameters.put("TITLE", null);
            lIdentificationParameters.put("DATFILE_QUERY", new Long(iPeptideLines.get(i)[(Integer)iHeaderMap.get("Query number")]));
            lIdentificationParameters.put("ACCESSION", iPeptideLines.get(i)[(Integer)iHeaderMap.get("Accession number")]);
            lIdentificationParameters.put("END", new Long (iPeptideLines.get(i)[(Integer)iHeaderMap.get("End position in protein")]));
            lIdentificationParameters.put("START", new Long (iPeptideLines.get(i)[(Integer)iHeaderMap.get("Start position in protein")]));
            lIdentificationParameters.put("DESCRIPTION",iPeptideLines.get(i)[(Integer)iHeaderMap.get("Description")]);
            lIdentificationParameters.put("ISOFORMS", "");

            MsQuantPeptideIdentification lid = new MsQuantPeptideIdentification(lIdentificationParameters);

            lRatioGroup.addIdentification(lid, "light");
            lRatioGroup.setPeptideSequence(lid.getSequence());
            iRatioGroupCollection.add(lRatioGroup);

        }

        return iRatioGroupCollection;
    }
}
