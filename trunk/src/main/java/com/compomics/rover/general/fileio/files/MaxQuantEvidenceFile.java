package com.compomics.rover.general.fileio.files;

import org.apache.log4j.Logger;

import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatio;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatioGroup;
import com.compomics.rover.general.enumeration.DataType;
import com.compomics.rover.general.PeptideIdentification.DefaultPeptideIdentification;
import com.compomics.util.interfaces.Flamable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 17-Apr-2009
 * Time: 13:51:00
 */

/**
 * This class reads the maxquant evidence and maxquant msms files
 */
public class MaxQuantEvidenceFile {
	// Class specific log4j logger for MaxQuantEvidenceFile instances.
	 private static Logger logger = Logger.getLogger(MaxQuantEvidenceFile.class);

    /**
     * The evidence file
     */
    private File iEvidenceFile;
    /**
     * The msms file
     */
    private File iMsmsFile;
    /**
     * The flamable
     */
    private Flamable iFlamable;

    private boolean tripleSilac = false;
    private final Locale locale;

    /**
     * Constructor
     *
     * @param aEvidenceFile The evidence file
     */
    public MaxQuantEvidenceFile(File aEvidenceFile, File aMsmsFile, Flamable aFlamable) {
        this.iFlamable = aFlamable;
        this.iEvidenceFile = aEvidenceFile;
        this.iMsmsFile = aMsmsFile;
        this.locale = new Locale(System.getProperty("user.language"));
    }

    public RatioGroupCollection getRatioGroupCollection() {

        RatioGroupCollection lCollection = new RatioGroupCollection(DataType.MAXQUANT);

        //boolean that indicates if we have read the header
        boolean lEvidenceHeaderParsed = false;
        //hashmap with the header titles and their positions
        HashMap<String,Integer> lHeaderMap = new HashMap<String,Integer>();
        //boolean that indicates if we are using triple silac


        //we will read the file line by line, on every line their is a peptide identification
        HashMap<Integer,DefaultPeptideIdentification> lIdentificationsMap = getIdentifications();
        try {

            //get the identifications

            int lCounter = 0;

            Runtime r = Runtime.getRuntime();
            String[] lColumns;

            //2.read the evidence file
            //create reader
            FileReader lReader = new FileReader(iEvidenceFile);
            LineNumberReader lnreader = new LineNumberReader(lReader);
            boolean lRatioSignificanceAFound = false;

            String line = "";
            //read it
            while ((line = lnreader.readLine()) != null) {
                if (!lEvidenceHeaderParsed) {
                    //we will parse the header
                    String[] lHeader = line.split("\t");
                    for (int i = 0; i < lHeader.length; i++) {
                        if (lHeader[i].equalsIgnoreCase("INTENSITY M")) {
                            tripleSilac = true;
                        }
                        lHeaderMap.put(lHeader[i].toUpperCase(locale), i);
                    }
                    //create the different types and components and add them to the RatioGroupCollection
                    Vector<String> lTypes = new Vector<String>();
                    Vector<String> lComponents = new Vector<String>();
                    lTypes.add("H/L");
                    lComponents.add("Light");
                    lComponents.add("Heavy");
                    if (tripleSilac) {
                        lTypes.add("M/L");
                        lTypes.add("H/M");
                        lComponents.add("Medium");
                    }
                    lCollection.setComponentTypes(lComponents);
                    lCollection.setRatioTypes(lTypes);
                    //Check if we can find Significance A
                    if (lHeaderMap.get("RATIO H/L SIGNIFICANCE(A)") != null) {
                        lRatioSignificanceAFound = true;
                        //QuantitativeValidationSingelton.getInstance().setRoverDataType(RoverSource.MAX_QUANT_NO_SIGN);
                        lCollection.setRatioGroupCollectionType(DataType.MAXQUANT_NO_SIGN);
                    }
                    //set the header parsed true
                    lEvidenceHeaderParsed = true;
                } else {
                    try {
                        lCounter++;
                        if (lCounter % 50000 == 0) {
                            logger.info("Found " + lCounter + " evidence lines!");
                            r.gc();
                        }

                        lColumns = line.split("\t");


                        //set the type
                        boolean lSILAC = true;
                        if (lHeaderMap.containsKey("SILAC STATE")){
                            Integer lTypeInt = lHeaderMap.get("SILAC STATE");
                            if(lTypeInt!= null){
                                String lType = lColumns[lTypeInt];
                                if (lType.length() == 0) {
                                    lSILAC = false;
                                }
                            } else {
                                Integer lRatioInt = lHeaderMap.get("RATIO H/L");
                                if(lRatioInt>= lColumns.length){
                                    lSILAC = false;
                                } else {
                                    String lRatio = lColumns[lRatioInt];
                                    if (lRatio.length() == 0) {
                                        lSILAC = false;
                                    }
                                }
                            }
                        } else if (lHeaderMap.containsKey("LABELING STATE")) {
                                lSILAC = true;
                        }
                        //only if we found a silac identification we will create  a ratio group
                        if (lSILAC && ( lHeaderMap.get("INTENSITY H")).intValue() < lColumns.length) {

                            //only add a ratio if we find any
                            if (!lColumns[( lHeaderMap.get("INTENSITY L")).intValue()].equalsIgnoreCase("")) {
                                MaxQuantRatioGroup lGroup = new MaxQuantRatioGroup(lCollection, Double.valueOf(lColumns[(lHeaderMap.get("PEP")).intValue()]), Integer.valueOf(lColumns[( lHeaderMap.get("ID")).intValue()]));
                                lGroup.setPeptideSequence(lColumns[(lHeaderMap.get("SEQUENCE")).intValue()]);
                                //get the different identification ids
                                String lMsmsIdsString =lColumns[(lHeaderMap.get("MS/MS IDS")).intValue()];
                                String[] lMsmss = lMsmsIdsString.split(";");
                                for (int i = 0; i < lMsmss.length; i++) {
                                    int lMsms = Integer.valueOf(lMsmss[i]);
                                    DefaultPeptideIdentification lIdentification = lIdentificationsMap.get(lMsms);
                                    if(lIdentification != null){
                                        lGroup.addIdentification(lIdentification, lIdentification.getType());
                                    } else {
                                        System.out.println("Error"+ lMsmss[i]);
                                    }
                                }

                                //create a vector with the absolute intensities
                                Vector<Double> lAbsoluteIntensityVector = new Vector<Double>();
                                if (lColumns[(lHeaderMap.get("INTENSITY L")).intValue()].length() > 0) {
                                    lAbsoluteIntensityVector.add(Double.valueOf(lColumns[( lHeaderMap.get("INTENSITY L")).intValue()]));
                                    lAbsoluteIntensityVector.add(Double.valueOf(lColumns[( lHeaderMap.get("INTENSITY H")).intValue()]));
                                } else {
                                    lAbsoluteIntensityVector.add(0.0);
                                    lAbsoluteIntensityVector.add(0.0);
                                }

                                //get the ratio(s)
                                if((lColumns[(lHeaderMap.get("RATIO H/L")).intValue()]).length() != 0){
                                    MaxQuantRatio lHLRatio;
                                    if (lRatioSignificanceAFound) {
                                        lHLRatio = new MaxQuantRatio(Double.valueOf(lColumns[( lHeaderMap.get("RATIO H/L")).intValue()]), Double.valueOf(lColumns[(lHeaderMap.get("RATIO H/L NORMALIZED")).intValue()]), "H/L", true, lGroup);
                                    } else {
                                        lHLRatio = new MaxQuantRatio(Double.valueOf(lColumns[( lHeaderMap.get("RATIO H/L")).intValue()]), Double.valueOf(lColumns[(lHeaderMap.get("RATIO H/L NORMALIZED")).intValue()]), "H/L", true, lGroup);
                                    }
                                    lGroup.addRatio(lHLRatio);
                                }
                                //find the other ratios if it's triple silac
                                if (tripleSilac) {
                                    if((lColumns[( lHeaderMap.get("RATIO H/M")).intValue()]).length() != 0){
                                        MaxQuantRatio lHMRatio;
                                        if (lRatioSignificanceAFound) {
                                            lHMRatio = new MaxQuantRatio(Double.valueOf(lColumns[(lHeaderMap.get("RATIO H/M")).intValue()]), Double.valueOf(lColumns[( lHeaderMap.get("RATIO H/M NORMALIZED")).intValue()]), "H/M", true, lGroup);
                                        } else {
                                            lHMRatio = new MaxQuantRatio(Double.valueOf(lColumns[( lHeaderMap.get("RATIO H/M")).intValue()]), Double.valueOf(lColumns[(lHeaderMap.get("RATIO H/M NORMALIZED")).intValue()]), "H/M", true, lGroup);
                                        }
                                        lGroup.addRatio(lHMRatio);
                                    }

                                    if((lColumns[(lHeaderMap.get("RATIO M/L")).intValue()]).length() != 0){
                                        MaxQuantRatio lMLRatio;
                                        if (lRatioSignificanceAFound) {
                                            lMLRatio = new MaxQuantRatio(Double.valueOf(lColumns[(lHeaderMap.get("RATIO M/L")).intValue()]), Double.valueOf(lColumns[( lHeaderMap.get("Ratio M/L Normalized")).intValue()]), "M/L", true, lGroup);
                                        } else {
                                            lMLRatio = new MaxQuantRatio(Double.valueOf(lColumns[(lHeaderMap.get("RATIO M/L")).intValue()]), Double.valueOf(lColumns[(lHeaderMap.get("Ratio M/L Normalized")).intValue()]), "M/L", true, lGroup);
                                        }
                                        lGroup.addRatio(lMLRatio);
                                    }

                                    lAbsoluteIntensityVector.add(Double.valueOf(lColumns[(lHeaderMap.get("INTENSITY M")).intValue()]));
                                }

                                //set the absolute intensities
                                Double[] lAbsoluteIntensities = new Double[lAbsoluteIntensityVector.size()];
                                lAbsoluteIntensityVector.toArray(lAbsoluteIntensities);
                                lGroup.setRatioGroupAbsoluteIntensities(lAbsoluteIntensities);
                                lGroup.setRazorProteinAccession(lColumns[(lHeaderMap.get("LEADING RAZOR PROTEIN")).intValue()]);

                                //System.out.println(lCollection.size() + " " + Integer.valueOf(lColumns[(Integer) lHeaderMap.get("id")]) + " " + lCounter + " " + lAddedCounter);
                                //add the ratiogroup to the collection
                                lCollection.add(lGroup);
                            }
                        }
                    } catch (NumberFormatException e) {
                        logger.info("Problem extracting information from the following lineid (No ratio could be detected):" + line.substring(0, line.indexOf("\t")));
                    }
                }

            }
            lnreader.close();

        } catch (FileNotFoundException e) {
            iFlamable.passHotPotato(new Throwable("Problem reading the maxquant files"));
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            iFlamable.passHotPotato(new Throwable("Problem reading the maxquant files"));
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            iFlamable.passHotPotato(new Throwable("Problem reading the maxquant files"));
            logger.error(e.getMessage(), e);
        }


        return lCollection;
    }


    public HashMap<Integer,DefaultPeptideIdentification> getIdentifications(){
        InputStreamReader lMsmsReader = null;
        HashMap<Integer,DefaultPeptideIdentification> lIdentificationsMap = new HashMap<Integer,DefaultPeptideIdentification>(); 
        try {
            lMsmsReader = new InputStreamReader(new FileInputStream(iMsmsFile),Charset.defaultCharset());
            LineNumberReader lnMsmsreader = new LineNumberReader(lMsmsReader);
            //boolean that indicates if we have read the header
            boolean lMsMsHeaderParsed = false;
            //hashmap with the header titles and their positions
            HashMap<String,Integer> lMsmsHeaderMap = new HashMap<String,Integer>();
            int lCounter = 0;
            String[] lColumns;
            String lMsmsLine = "";
            //read it
            while ((lMsmsLine = lnMsmsreader.readLine()) != null) {
                if (!lMsMsHeaderParsed) {
                    //we will parse the header
                    String[] lHeader = lMsmsLine.split("\t");
                    for (int i = 0; i < lHeader.length; i++) {
                        lMsmsHeaderMap.put(lHeader[i].toUpperCase(), i);
                    }
                    //set the header parsed true
                    lMsMsHeaderParsed = true;
                } else {
                    lCounter++;
                    lColumns = lMsmsLine.split("\t");
                    //first create the DefaultPeptideIdentification
                    DefaultPeptideIdentification lIdentification = new DefaultPeptideIdentification();

                    //set the id
                    lIdentification.setId(Integer.valueOf(lColumns[lMsmsHeaderMap.get("ID")]));

                    //set the accession and possible isoforms
                    String lAccessions = (lColumns[lMsmsHeaderMap.get("PROTEINS")]);
                    String lIsoforms = "";
                    if (lAccessions.indexOf(";") > 0) {
                        //we found some isoforms
                        String[] lTempIsoforms = lAccessions.split(";");
                        //get the main identification accession
                        lAccessions = lTempIsoforms[0];
                        for (int i = 1; i < lTempIsoforms.length; i++) {
                            lIsoforms = lIsoforms + lTempIsoforms[i] + " ,";
                        }
                    }
                    //set the identifications
                    lIdentification.setAccession(lAccessions);
                    //set the isoforms
                    lIdentification.setIsoforms(lIsoforms);

                    //set the (modified) sequence
                    lIdentification.setSequence(lColumns[ lMsmsHeaderMap.get("SEQUENCE")]);
                    
                    lIdentification.setModified_sequence(lColumns[ lMsmsHeaderMap.get("MODIFIED SEQUENCE")]);

                    //set the charge
                    lIdentification.setCharge(Integer.valueOf(lColumns[ lMsmsHeaderMap.get("CHARGE")]));

                    //set the masses
                    lIdentification.setPrecursor(Double.valueOf(lColumns[ lMsmsHeaderMap.get("M/Z")]));
                    //get the mass error
                    String lMassErrorString = lColumns[lMsmsHeaderMap.get("SIMPLE MASS ERROR [PPM]")];
                    double lMassError = 0.0;
                    if (lMassErrorString.indexOf("-") > -1) {
                        lMassError = Double.valueOf(lMassErrorString.substring(1));
                    } else {
                        lMassError = Double.valueOf(lMassErrorString);
                    }
                    //get the cal mass
                    double lCalMass = Double.valueOf(lColumns[ lMsmsHeaderMap.get("MASS")]);
                    //recalculate the obsereved mass
                    double lExpMass = (lCalMass * 1000000.0) / (lMassError + 1000000.0);
                    //set exp and cal mass
                    lIdentification.setExp_mass(lExpMass);
                    lIdentification.setCal_mass(lCalMass);

                    //set the score
                    if(lMsmsHeaderMap.get("MASCOT SCORE") != null){
                        lIdentification.setScore((Double.valueOf(lColumns[ lMsmsHeaderMap.get("MASCOT SCORE")]).longValue()));
                    } else {
                        lIdentification.setScore((Double.valueOf(lColumns[ lMsmsHeaderMap.get("SCORE")]).longValue()));
                    }

                    lIdentification.setPep(Double.valueOf(lColumns[lMsmsHeaderMap.get("PEP")]));

                    //set the type
                    if (lMsmsHeaderMap.containsKey("SILAC STATE")) {
                        String lType = lColumns[lMsmsHeaderMap.get("SILAC STATE")];
                        boolean lSILAC = true;
                        if (lType.length() == 0) {
                            lSILAC = false;
                        }
                        lIdentification.setType(lType);
                    } else if (lMsmsHeaderMap.containsKey("LABELING STATE") && !tripleSilac) {
                            if (Integer.parseInt(lColumns[lMsmsHeaderMap.get("LABELING STATE")])== 0){
                                lIdentification.setType("LIGHT");
                            } else if (Integer.parseInt(lColumns[lMsmsHeaderMap.get("LABELING STATE")]) == 1){
                                lIdentification.setType("HEAVY");
                            }
                            boolean lSILAC = true;
                    } else if (lMsmsHeaderMap.containsKey("LABELING STATE") && tripleSilac){
                        if (Integer.parseInt(lColumns[lMsmsHeaderMap.get("LABELING STATE")]) == 0){
                            lIdentification.setType("LIGHT");
                        } else if (Integer.parseInt(lColumns[lMsmsHeaderMap.get("LABELING STATE")]) == 1){
                            lIdentification.setType("MEDIUM");
                        } else if (Integer.parseInt(lColumns[lMsmsHeaderMap.get("LABELING STATE")]) == 2){
                            lIdentification.setType("HEAVY");
                        }
                        boolean lSILAC = true;
                    }
                    //set the title
                    lIdentification.setSpectrumFileName((lColumns[ lMsmsHeaderMap.get("RAW FILE")]) + ".MQ." +lColumns[ lMsmsHeaderMap.get("SCAN NUMBER")] + "." + lColumns[ lMsmsHeaderMap.get("CHARGE")]);

                    lIdentificationsMap.put(lIdentification.getId(), lIdentification);

                    if (lCounter % 50000 == 0) {
                        logger.info("Found " + lCounter + " MSMS lines!");
                        Runtime r = Runtime.getRuntime();
                        r.gc();
                    }
                }

            }
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(MaxQuantEvidenceFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ioex) {
            java.util.logging.Logger.getLogger(MaxQuantEvidenceFile.class.getName()).log(Level.SEVERE, null, ioex);
        }
        finally {
            try {
                lMsmsReader.close();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(MaxQuantEvidenceFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lIdentificationsMap;
    }
}
