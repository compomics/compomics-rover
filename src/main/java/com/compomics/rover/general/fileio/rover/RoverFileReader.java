package com.compomics.rover.general.fileio.rover;

import org.apache.log4j.Logger;

import com.compomics.rover.general.quantitation.QuantitativePeptideGroup;
import com.compomics.rover.general.quantitation.QuantitativeProtein;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 19-jan-2009
 * Time: 9:10:23
 */

/**
 * This class reads and loads the .rover file
 */
public class RoverFileReader {
	// Class specific log4j logger for RoverFileReader instances.
	 private static Logger logger = Logger.getLogger(RoverFileReader.class);

    /**
     * The .rover file location
     */
    private String iFileLocation;
    /**
     * Indicates if we are reading the protein section
     */
    private boolean iInProteinSection = false;
    /**
     * Indicates if we are reading the ratio section
     */
    private boolean iInRatioSection = false;
    /**
     * Indicates if we are in the not used peptide section
     */
    private boolean iInNotUsedPeptideSection = false;
    /**
     * Vector with found protein in the .rover file
     */
    private Vector<RoverFileProtein> iProteins = new Vector<RoverFileProtein>();
    /**
     * Vector with found ratios in the .rover file
     */
    private Vector<RoverFileRatio> iRatios = new Vector<RoverFileRatio>();
        /**
     * This vector will hold protein accessions for the not used peptides in the calculation of the protein mean
     */
    private Vector<String> iNotUsedProteins = new Vector<String>();
    /**
     * This vector will hold peptide sequences. This ratios linked to the peptide sequences will not be used in the calcultation of the protein mean.
     * (The protein accesion are stored in the iNotUsedProteins vector)
     */
    private Vector<String> iNotUsedPeptides = new Vector<String>();
    /**
     * This validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
    /**
     * All the proteins
     */
    private QuantitativeProtein[] iAllProteins;


    /**
     * Constructor
     * @param aFileLocation String with the file location of the .rover file
     */
    public RoverFileReader(String aFileLocation, QuantitativeProtein[] aAllProteins) {

        //set the proteins
        this.iAllProteins = aAllProteins;
        //set the file location
        this.iFileLocation = aFileLocation;

        try {
            FileReader freader = new FileReader(iFileLocation);
            LineNumberReader lnreader = new LineNumberReader(freader);
            String line = "";
            while ((line = lnreader.readLine()) != null) {
                if (line.equalsIgnoreCase("//protein accession, selected, validated")) {
                    iInProteinSection = true;
                    iInRatioSection = false;
                    iInNotUsedPeptideSection = false;
                } else if(line.equalsIgnoreCase("//ratio type, ratio, sequence, proteins, valid, comment")) {
                    //ratio section header
                    iInRatioSection = true;
                    iInProteinSection = false;
                    iInNotUsedPeptideSection = false;
                } else if(line.equalsIgnoreCase("//protein, peptide")) {
                    iInRatioSection = false;
                    iInProteinSection = false;
                    iInNotUsedPeptideSection = true;
                } else if (iInRatioSection) {
                    //read the ratio
                    String lRatioType =  line.substring(0, line.indexOf(","));
                    int lEndReadPosition = line.indexOf(",");
                    double lRatio = Double.valueOf(line.substring(lEndReadPosition + 1, line.indexOf(",", lEndReadPosition + 1)));
                    lEndReadPosition = line.indexOf(",", lEndReadPosition + 1);
                    String lSequence =line.substring(lEndReadPosition + 1, line.indexOf(",", lEndReadPosition + 1));
                    lEndReadPosition = line.indexOf(",", lEndReadPosition + 1);
                    String lProteins =line.substring(lEndReadPosition + 1, line.indexOf("|,", lEndReadPosition + 1));
                    lEndReadPosition = line.indexOf("|,", lEndReadPosition + 1);
                    boolean lValid = Boolean.valueOf(line.substring(lEndReadPosition + 2, line.indexOf(",", lEndReadPosition + 2)));
                    lEndReadPosition = line.indexOf(",", lEndReadPosition + 2);
                    String lComment =line.substring(lEndReadPosition + 1);
                    iRatios.add(new RoverFileRatio(lSequence, lRatioType, lRatio, lProteins, lValid, lComment));
                } else if (iInProteinSection) {
                    //read proteins
                    boolean lSelected = false;
                    boolean lValidated = false;
                    int lRunningIndex = line.indexOf(",");
                    String lSelectedString = line.substring(lRunningIndex + 1, lRunningIndex + 2 );
                    if (lSelectedString.equalsIgnoreCase("1")) {
                        lSelected = true;
                    }
                    lRunningIndex = line.indexOf(",",lRunningIndex + 1);
                    String lValidatedString = line.substring(lRunningIndex + 1, lRunningIndex + 2 );
                    if (lValidatedString.equalsIgnoreCase("1")) {
                        lValidated = true;
                    }
                    lRunningIndex = line.indexOf(",",lRunningIndex + 1);
                    String lComment = line.substring(lRunningIndex + 1);
                    iProteins.add(new RoverFileProtein(line.substring(0, line.indexOf(",")), lSelected, lValidated, lComment));
                } else if (iInNotUsedPeptideSection){
                    iNotUsedProteins.add(line.substring(0, line.indexOf(",")));
                    iNotUsedPeptides.add(line.substring(line.indexOf(",") + 1));

                }

            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        //now load the data
        this.loadReadedData();
    }

    /**
     * Getter for the Vector with proteins that were stored in the .rover file
     * @return Vector<RoverFileProtein> with proteins
     */
    public Vector<RoverFileProtein> getProteins() {
        return iProteins;
    }

    /**
     * Getter for the Vector with ratios that were stored in the .rover file
     * @return Vector<RoverFileRatio> with ratios
     */
    public Vector<RoverFileRatio> getRatios() {
        return iRatios;
    }

    /**
     * Getter for the Vector with accession of the peptides that are not used in the calculation of the protein mean that were stored in the .rover file
     * @return Vector<String> with accession
     */
    public Vector<String> getNotUsedProteinAccessions() {
        return iNotUsedProteins;
    }
    /**
     * Getter for the Vector with peptides that are not used in the calculation of the protein mean that were stored in the .rover file
     * @return Vector<String> with peptide sequences
     */
    public Vector<String> getNotUsedPeptides() {
        return iNotUsedPeptides;
    }

    /**
     * This method will set the data that has been read.
     */
    public void loadReadedData(){
        Vector<RoverFileProtein> aProteins = this.getProteins();
        Vector<RoverFileRatio> aRatios = this.getRatios();
        Vector<String> lNotUsedProteinAccession = this.getNotUsedProteinAccessions();
        Vector<String> lNotUsedPeptides = this.getNotUsedPeptides();

        //add the not used peptides to the iQuantitativeValidationSingelton
        for (int p = 0; p < lNotUsedPeptides.size(); p++) {
            iQuantitativeValidationSingelton.addNotUsedPeptide(lNotUsedProteinAccession.get(p), lNotUsedPeptides.get(p));
        }
        //now find them in the proteins
        for (int p = 0; p < iAllProteins.length; p++) {
            for (int a = 0; a < lNotUsedProteinAccession.size(); a++) {
                if (lNotUsedProteinAccession.get(a).equalsIgnoreCase(iAllProteins[p].getAccession())) {
                    //protein found, set the peptides not used
                    Vector<QuantitativePeptideGroup> lPeptideGroups = iAllProteins[p].getPeptideGroups(true);
                    for (int b = 0; b < lPeptideGroups.size(); b++) {
                        if (lPeptideGroups.get(b).getSequence().equalsIgnoreCase(lNotUsedPeptides.get(a))) {
                            lPeptideGroups.get(b).setUsedInCalculation(false);
                        }
                    }
                }
            }
        }


        for (int p = 0; p < iAllProteins.length; p++) {
            QuantitativeProtein lProtein = iAllProteins[p];
            for (int v = 0; v < aProteins.size(); v++) {
                if (lProtein.getAccession().equalsIgnoreCase(aProteins.get(v).getProteinAccession())) {
                    //found a saved protein
                    if (aProteins.get(v).isSelected()) {
                        //this is a selected protein
                        if (!lProtein.getSelected()) {
                            //it's not selected
                            lProtein.setSelected(true);
                            iQuantitativeValidationSingelton.addSelectedProtein(lProtein);
                        }
                    }
                    if (aProteins.get(v).isValidated()) {
                        //this is a validated protein
                        if (!lProtein.getValidated()) {
                            //it's not validated
                            lProtein.setValidated(true);
                            iQuantitativeValidationSingelton.addValidatedProtein(lProtein);
                        }
                    }
                    if (aProteins.get(v).getComment().length() != 0) {
                        if (lProtein.getProteinComment().length() == 0) {
                            lProtein.setProteinComment(aProteins.get(v).getComment());
                            iQuantitativeValidationSingelton.addCommentedProtein(lProtein);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < iAllProteins.length; i++) {
            QuantitativeProtein lProtein = iAllProteins[i];
            for (int j = 0; j < lProtein.getRatioGroups().size(); j++) {
                RatioGroup lRatioGroup = lProtein.getRatioGroups().get(j);
                for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                    Ratio lRatio = lRatioGroup.getRatio(k);
                    for (int r = 0; r < aRatios.size(); r++) {
                        RoverFileRatio lRoverRatio = aRatios.get(r);
                        if (lRoverRatio.getPeptideSequence().equalsIgnoreCase(lRatio.getParentRatioGroup().getPeptideSequence()) && lRoverRatio.getRatio() == lRatio.getRatio(false) && lRoverRatio.getRatioType().equalsIgnoreCase(lRatio.getType()) && lRoverRatio.getProteins().equalsIgnoreCase(lRatio.getParentRatioGroup().getProteinAccessionsAsString())) {
                            lRatio.setValid(lRoverRatio.isValid());
                            lRatio.setComment(lRoverRatio.getComment());
                        }
                    }
                }
            }
        }
    }

}
