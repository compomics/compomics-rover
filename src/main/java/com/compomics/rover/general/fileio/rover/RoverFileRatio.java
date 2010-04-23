package com.compomics.rover.general.fileio.rover;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 5-mrt-2009
 * Time: 8:17:57
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class stores the parameters of a ratio that was stored in the .rover file
 */
public class RoverFileRatio {
	// Class specific log4j logger for RoverFileRatio instances.
	 private static Logger logger = Logger.getLogger(RoverFileRatio.class);

    /**
     * The ratio type (L/H, M/H, ...)
     */
    private String iRatioType;

    /**
     * The ratio
     */
    private double iRatio;

    /**
     * The proteins
     */
    private String iProteins;

    /**
     * This boolean indicates if this ratio is valid
     */
    private boolean iValid;

    /**
     * The comment
     */
    private String iComment;
    /**
     * The peptide sequence of the rover ratio
     */
    private String iPeptideSequence;

    /**
     * Constructor
     * @param aSequence The peptide sequence
     * @param iRatioType The ratio type
     * @param iRatio The ratio
     * @param iProteins The proteins that this ratio is linked to
     * @param iValid The validated status
     * @param iComment The comment
     */
    public RoverFileRatio(String aSequence, String iRatioType, double iRatio, String iProteins, boolean iValid, String iComment) {
        this.iRatioType = iRatioType;
        this.iPeptideSequence = aSequence;
        this.iRatio = iRatio;
        this.iProteins = iProteins;
        this.iValid = iValid;
        this.iComment = iComment;
    }

    /**
     * Getter for the ratiotype
     * @return String with the ratiotype
     */
    public String getRatioType() {
        return iRatioType;
    }

    /**
     * Getter for the ratio
     * @return double with the ratio
     */
    public double getRatio() {
        return iRatio;
    }

    /**
     * Getter for the proteins
     * @return String with the protein accessions seperated by a comma (',')
     */
    public String getProteins() {
        return iProteins;
    }

    /**
     * Getter for the valid status
     * @return boolean that indicates if it's valid
     */
    public boolean isValid() {
        return iValid;
    }

    /**
     * Getter for the comment
     * @return String with the comment
     */
    public String getComment() {
        return iComment;
    }

    /**
     * Getter for the peptide sequence
     * @return String with the peptide sequence
     */
    public String getPeptideSequence() {
        return iPeptideSequence;
    }
}
