package com.compomics.rover.general.fileio.rover;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 4-mrt-2009
 * Time: 9:51:35
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class has a protein accession and two booleans who indicate if this protein was selected and validated in the .rover file
 */
public class RoverFileProtein {
	// Class specific log4j logger for RoverFileProtein instances.
	 private static Logger logger = Logger.getLogger(RoverFileProtein.class);

    /**
     * The protein accession
     */
    private String iProteinAccession;
    /**
     * Boolean that indicates if this protein is selected
     */
    private boolean iSelected;
    /**
     * Boolean that indicates if this protein is validated
     */
    private boolean iValidated;
    /**
     * String with the comment
     */
    private String iComment;

    /**
     * Constructor
     * @param iProteinAccession String with the protein accession
     * @param iSelected boolean with selected status
     * @param iValidated boolean with validated status
     */
    public RoverFileProtein(String iProteinAccession, boolean iSelected, boolean iValidated, String aComment) {
        this.iProteinAccession = iProteinAccession;
        this.iSelected = iSelected;
        this.iValidated = iValidated;
        this.iComment = aComment;
    }

    /**
     * Getter for the protein accession
     * @return String with the protein accession
     */
    public String getProteinAccession() {
        return iProteinAccession;
    }

    /**
     * Getter for the boolean with the selected status
     * @return boolean with the selected status
     */
    public boolean isSelected() {
        return iSelected;
    }

    /**
     * Getter for the boolean with the validated status
     * @return boolean with the validated status
     */
    public boolean isValidated() {
        return iValidated;
    }

    /**
     * Getter for comment
     * @return String with the comment
     */
    public String getComment() {
        return iComment;
    }
}
