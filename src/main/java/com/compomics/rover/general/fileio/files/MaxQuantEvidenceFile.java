package com.compomics.rover.general.fileio.files;

import org.apache.log4j.Logger;

import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatio;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatioGroup;
import com.compomics.rover.general.enumeration.DataType;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.PeptideIdentification.DefaultPeptideIdentification;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.util.interfaces.Flamable;

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;

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

    /**
     * Constructor
     *
     * @param aEvidenceFile The evidence file
     */
    public MaxQuantEvidenceFile(File aEvidenceFile, File aMsmsFile, Flamable aFlamable) {
        this.iFlamable = aFlamable;
        this.iEvidenceFile = aEvidenceFile;
        this.iMsmsFile = aMsmsFile;
    }

    public RatioGroupCollection getRatioGroupCollection() {

        RatioGroupCollection lCollection = new RatioGroupCollection(DataType.MAXQUANT);

        //boolean that indicates if we have read the header
        boolean lEvidenceHeaderParsed = false;
        //hashmap with the header titles and their positions
        HashMap lHeaderMap = new HashMap();
        //boolean that indicates if we are using triple silac


        //we will read the file line by line, on every line their is a peptide identification
        try {

            //get the identifications
            HashMap<Integer,DefaultPeptideIdentification> lIdentificationsMap = getIdentifications();

            int lCounter = 0;
            int lAddedCounter = 0;

            Runtime r = Runtime.getRuntime();
            r.gc();
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
                        if (lHeader[i].equalsIgnoreCase("Intensity M")) {
                            tripleSilac = true;
                        }
                        lHeaderMap.put(lHeader[i], i);
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
                    if ((Integer) lHeaderMap.get("Ratio H/L Significance(A)") != null) {
                        lRatioSignificanceAFound = true;
                        //QuantitativeValidationSingelton.getInstance().setRoverDataType(RoverSource.MAX_QUANT_NO_SIGN);
                        lCollection.setRatioGroupCollectionType(DataType.MAXQUANT_NO_SIGN);
                    }
                    //set the header parsed true
                    lEvidenceHeaderParsed = true;
                } else {
                    try {
                        lCounter = lCounter + 1;
                        if (lCounter % 50000 == 0) {
                            logger.info("Found " + lCounter + " evidence lines!");
                            r.gc();
                        }

                        lColumns = line.split("\t");


                        //set the type
                        boolean lSILAC = true;
                        if (lHeaderMap.containsKey("SILAC State")){
                            Integer lTypeInt = (Integer) lHeaderMap.get("SILAC State");
                            if(lTypeInt!= null){
                                String lType = new String(lColumns[lTypeInt]);
                                if (lType.length() == 0) {
                                    lSILAC = false;
                                }
                            } else {
                                Integer lRatioInt = (Integer) lHeaderMap.get("Ratio H/L");
                                if(lRatioInt>= lColumns.length){
                                    lSILAC = false;
                                } else {
                                    String lRatio = new String(lColumns[lRatioInt]);
                                    if (lRatio.length() == 0) {
                                        lSILAC = false;
                                    }
                                }
                            }
                        } else if (lHeaderMap.containsKey("Labeling State")) {
                             if (lHeaderMap.containsKey("Labeling State") && !tripleSilac) {
                                if (Integer.parseInt(lColumns[(Integer)lHeaderMap.get("Labeling State")])== 0){
                                    String lType = "Light";
                                } else if (Integer.parseInt(lColumns[(Integer)lHeaderMap.get("Labeling State")]) == 1){
                                    String lType = "Heavy";
                                }
                                lSILAC = true;
                            } else if (lHeaderMap.containsKey("Labeling State") && tripleSilac){
                                if (Integer.parseInt(lColumns[(Integer)lHeaderMap.get("Labeling State")]) == 0){
                                    String lType = "Light";
                                } else if (Integer.parseInt(lColumns[(Integer)lHeaderMap.get("Labeling State")]) == 1){
                                    String lType = "Medium";
                                } else if (Integer.parseInt(lColumns[(Integer)lHeaderMap.get("Labeling State")]) == 2){
                                    String lType = "Heavy";
                                }
                                lSILAC = true;
                            }
                        }
                        //only if we found a silac identification we will create  a ratio group
                        if (lSILAC && ((Integer) lHeaderMap.get("Intensity H")).intValue() < lColumns.length) {

                            //only add a ratio if we find any
                            if (!lColumns[((Integer) lHeaderMap.get("Intensity L")).intValue()].equalsIgnoreCase("")) {
                                MaxQuantRatioGroup lGroup = new MaxQuantRatioGroup(lCollection, Double.valueOf(lColumns[((Integer) lHeaderMap.get("PEP")).intValue()]), Integer.valueOf(lColumns[((Integer) lHeaderMap.get("id")).intValue()]));
                                lGroup.setPeptideSequence(new String(lColumns[((Integer) lHeaderMap.get("Sequence")).intValue()]));
                                //get the different identification ids
                                String lMsmsIdsString = new String(lColumns[((Integer) lHeaderMap.get("MS/MS IDs")).intValue()]);
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
                                if (lColumns[((Integer) lHeaderMap.get("Intensity L")).intValue()].length() > 0) {
                                    lAbsoluteIntensityVector.add(Double.valueOf(lColumns[((Integer) lHeaderMap.get("Intensity L")).intValue()]));
                                    lAbsoluteIntensityVector.add(Double.valueOf(lColumns[((Integer) lHeaderMap.get("Intensity H")).intValue()]));
                                } else {
                                    lAbsoluteIntensityVector.add(0.0);
                                    lAbsoluteIntensityVector.add(0.0);
                                }

                                //get the ratio(s)
                                if(((String)lColumns[((Integer) lHeaderMap.get("Ratio H/L")).intValue()]).length() != 0){
                                    MaxQuantRatio lHLRatio;
                                    if (lRatioSignificanceAFound) {
                                        lHLRatio = new MaxQuantRatio(Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio H/L")).intValue()]), Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio H/L Normalized")).intValue()]), "H/L", true, lGroup);
                                    } else {
                                        lHLRatio = new MaxQuantRatio(Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio H/L")).intValue()]), Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio H/L Normalized")).intValue()]), "H/L", true, lGroup);
                                    }
                                    lGroup.addRatio(lHLRatio);
                                }
                                //find the other ratios if it's triple silac
                                if (tripleSilac) {
                                    if(((String)lColumns[((Integer) lHeaderMap.get("Ratio H/M")).intValue()]).length() != 0){
                                        MaxQuantRatio lHMRatio;
                                        if (lRatioSignificanceAFound) {
                                            lHMRatio = new MaxQuantRatio(Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio H/M")).intValue()]), Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio H/M Normalized")).intValue()]), "H/M", true, lGroup);
                                        } else {
                                            lHMRatio = new MaxQuantRatio(Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio H/M")).intValue()]), Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio H/M Normalized")).intValue()]), "H/M", true, lGroup);
                                        }
                                        lGroup.addRatio(lHMRatio);
                                    }

                                    if(((String)lColumns[((Integer) lHeaderMap.get("Ratio M/L")).intValue()]).length() != 0){
                                        MaxQuantRatio lMLRatio;
                                        if (lRatioSignificanceAFound) {
                                            lMLRatio = new MaxQuantRatio(Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio M/L")).intValue()]), Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio M/L Normalized")).intValue()]), "M/L", true, lGroup);
                                        } else {
                                            lMLRatio = new MaxQuantRatio(Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio M/L")).intValue()]), Double.valueOf(lColumns[((Integer) lHeaderMap.get("Ratio M/L Normalized")).intValue()]), "M/L", true, lGroup);
                                        }
                                        lGroup.addRatio(lMLRatio);
                                    }

                                    lAbsoluteIntensityVector.add(Double.valueOf(lColumns[((Integer) lHeaderMap.get("Intensity M")).intValue()]));
                                }

                                //set the absolute intensities
                                Double[] lAbsoluteIntensities = new Double[lAbsoluteIntensityVector.size()];
                                lAbsoluteIntensityVector.toArray(lAbsoluteIntensities);
                                lGroup.setRatioGroupAbsoluteIntensities(lAbsoluteIntensities);
                                lGroup.setRazorProteinAccession(new String(lColumns[((Integer) lHeaderMap.get("Leading Razor Protein")).intValue()]));

                                lAddedCounter = lAddedCounter + 1;
                                //System.out.println(lCollection.size() + " " + Integer.valueOf(lColumns[(Integer) lHeaderMap.get("id")]) + " " + lCounter + " " + lAddedCounter);
                                //add the ratiogroup to the collection
                                lCollection.add(lGroup);
                            }
                        }
                    } catch (NumberFormatException e) {
                        //logger.info("Problem extracting information from the following lineid (No ratio could be detected):" + line.substring(0, line.indexOf("\t")));
                    }
                }

            }
            lnreader.close();
            lIdentificationsMap = null;

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


    public HashMap<Integer,DefaultPeptideIdentification> getIdentifications() throws IOException {
            //1.read the msms file
            //create reader
            FileReader lMsmsReader = new FileReader(iMsmsFile);
            LineNumberReader lnMsmsreader = new LineNumberReader(lMsmsReader);
            //boolean that indicates if we have read the header
            boolean lMsMsHeaderParsed = false;
            //hashmap with the header titles and their positions
            HashMap lMsmsHeaderMap = new HashMap();

            HashMap<Integer,DefaultPeptideIdentification> lIdentificationsMap = new HashMap<Integer,DefaultPeptideIdentification>();
            int lCounter = 0;
            String[] lColumns;
            String lMsmsLine = "";
            //read it
            while ((lMsmsLine = lnMsmsreader.readLine()) != null) {
                if (!lMsMsHeaderParsed) {
                    //we will parse the header
                    String[] lHeader = lMsmsLine.split("\t");
                    for (int i = 0; i < lHeader.length; i++) {
                        lMsmsHeaderMap.put(lHeader[i], i);
                    }
                    //set the header parsed true
                    lMsMsHeaderParsed = true;
                } else {
                    lCounter = lCounter + 1;
                    lColumns = lMsmsLine.split("\t");
                    //first create the DefaultPeptideIdentification
                    DefaultPeptideIdentification lIdentification = new DefaultPeptideIdentification();

                    //set the id
                    lIdentification.setId(Integer.valueOf(lColumns[(Integer) lMsmsHeaderMap.get("id")]));

                    //set the accession and possible isoforms
                    String lAccessions = new String(lColumns[(Integer) lMsmsHeaderMap.get("Proteins")]);
                    String lIsoforms = "";
                    if (lAccessions.indexOf(";") > 0) {
                        //we found some isoforms
                        String[] lTempIsoforms = lAccessions.split(";");
                        //get the main identification accession
                        lAccessions = new String(lTempIsoforms[0]);
                        for (int i = 1; i < lTempIsoforms.length; i++) {
                            lIsoforms = lIsoforms + lTempIsoforms[i] + " ,";
                        }
                    }
                    //set the identifications
                    lIdentification.setAccession(lAccessions);
                    //set the isoforms
                    lIdentification.setIsoforms(lIsoforms);

                    //set the (modified) sequence
                    lIdentification.setSequence(new String(lColumns[(Integer) lMsmsHeaderMap.get("Sequence")]));
                    lIdentification.setModified_sequence(new String(lColumns[(Integer) lMsmsHeaderMap.get("Modified Sequence")]));

                    //set the charge
                    lIdentification.setCharge(Integer.valueOf(lColumns[(Integer) lMsmsHeaderMap.get("Charge")]));

                    //set the masses
                    lIdentification.setPrecursor(Double.valueOf(lColumns[(Integer) lMsmsHeaderMap.get("m/z")]));
                    //get the mass error
                    String lMassErrorString = new String(lColumns[(Integer) lMsmsHeaderMap.get("Simple Mass Error [ppm]")]);
                    double lMassError = 0.0;
                    if (lMassErrorString.indexOf("-") > -1) {
                        lMassError = Double.valueOf(lMassErrorString.substring(1));
                    } else {
                        lMassError = Double.valueOf(lMassErrorString);
                    }
                    //get the cal mass
                    double lCalMass = Double.valueOf(lColumns[(Integer) lMsmsHeaderMap.get("Mass")]);
                    //recalculate the obsereved mass
                    double lExpMass = (lCalMass * 1000000.0) / (lMassError + 1000000.0);
                    //set exp and cal mass
                    lIdentification.setExp_mass(lExpMass);
                    lIdentification.setCal_mass(lCalMass);

                    //set the score
                    if(lMsmsHeaderMap.get("Mascot Score") != null){
                        lIdentification.setScore((Double.valueOf(lColumns[(Integer) lMsmsHeaderMap.get("Mascot Score")]).longValue()));
                    } else {
                        lIdentification.setScore((Double.valueOf(lColumns[(Integer) lMsmsHeaderMap.get("Score")]).longValue()));
                    }

                    lIdentification.setPep(Double.valueOf(lColumns[(Integer) lMsmsHeaderMap.get("PEP")]));

                    //set the type
                    if (lMsmsHeaderMap.containsKey("SILAC State")) {
                        String lType = lColumns[(Integer) lMsmsHeaderMap.get("SILAC State")];
                        boolean lSILAC = true;
                        if (lType.length() == 0) {
                            lSILAC = false;
                        }
                        lIdentification.setType(lType);
                    } else if (lMsmsHeaderMap.containsKey("Labeling State") && !tripleSilac) {
                            if (Integer.parseInt(lColumns[(Integer)lMsmsHeaderMap.get("Labeling State")])== 0){
                                lIdentification.setType("Light");
                            } else if (Integer.parseInt(lColumns[(Integer)lMsmsHeaderMap.get("Labeling State")]) == 1){
                                lIdentification.setType("Heavy");
                            }
                            boolean lSILAC = true;
                    } else if (lMsmsHeaderMap.containsKey("Labeling State") && tripleSilac){
                        if (Integer.parseInt(lColumns[(Integer)lMsmsHeaderMap.get("Labeling State")]) == 0){
                            lIdentification.setType("Light");
                        } else if (Integer.parseInt(lColumns[(Integer)lMsmsHeaderMap.get("Labeling State")]) == 1){
                            lIdentification.setType("Medium");
                        } else if (Integer.parseInt(lColumns[(Integer)lMsmsHeaderMap.get("Labeling State")]) == 2){
                            lIdentification.setType("Heavy");
                        }
                        boolean lSILAC = true;
                    }
                    //set the title
                    lIdentification.setSpectrumFileName(new String(lColumns[(Integer) lMsmsHeaderMap.get("Raw File")]) + ".MQ." + new String(lColumns[(Integer) lMsmsHeaderMap.get("Scan Number")]) + "." + lColumns[(Integer) lMsmsHeaderMap.get("Charge")]);

                    lIdentificationsMap.put(lIdentification.getId(), lIdentification);

                    if (lCounter % 50000 == 0) {
                        logger.info("Found " + lCounter + " MSMS lines!");
                        Runtime r = Runtime.getRuntime();
                        r.gc();
                    }
                }

            }
        lMsmsReader.close();
        return lIdentificationsMap;
    }
}
