package com.compomics.rover.general.quantitation.source.Census;

import org.apache.log4j.Logger;

import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.db.accessors.QuantitationExtension;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 16-May-2009
 * Time: 14:20:10
 */

/**
 * The census ratio
 */
public class CensusRatio implements Ratio {
	// Class specific log4j logger for CensusRatio instances.
	 private static Logger logger = Logger.getLogger(CensusRatio.class);
    /**
      * The ratio itself.
      */
     private double iRatio;
     /**
      * The type of the Ratio.
      * ex: The type 'L/H' shows a ratio between a 'Light' and 'Heavy' component.
      */
     private String iType;
     /**
      * The valid status of the Ratio by MascotDistiller.
      */
     private boolean iValid;
     /**
      * The quantitation stored in the database and linked to this DistillerRatio.
      */
     private QuantitationExtension iQuantitationStoredInDb;
     /**
      * A comment on the valid status of the ratio
      */
     private String iComment;
     /**
      * This distiller validation singelton holds information for the calculation of the ratio
      */
     private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
     /**
      * The Parent RatioGroup for this ratio
      */
     private RatioGroup iParentRatioGroup;
    /**
     * The reference factor
     */
    private double iReferenceFactor;
    /**
     * The determination factor
     */
    private double iDeterminationFactor;
    /**
     * The profile score
     */
    private double iProfileScore;
    /**
     * The original ratio
     */
    private double iOriginalRatio;
    /**
     * The number of the part in the list of the intensity sorted ratios
     */
    private int iPartNumber = 0;
    /**
     * The MAD before normalization
     */
    private double iPreNormMAD;
    /**
     * The MAD after normalization
     */
    private double iNormMAD;
    /**
     * The number of times the ratio is updated
     */
    private int iUpdates = 0;
    /**
     * The index of the data source
     */
    private double iIndex = -1.0;
    /**
     * The original source index
     */
    private double iOriginalIndex = -1.0;
    /**
     * boolean that indicates if the ratio was inverted
     */
    private boolean iInverted = false;

    /**
     * CONSTRUCTOR
     * @param aRatio The ratio
     * @param aRf The reference Factor
     * @param aDf The determination factor
     * @param aPs The profile score
     */
    public CensusRatio(String aType, double aRatio, double aRf, double aDf, double aPs, RatioGroup aParent){
        //set the parameters
        this.iType = aType;
        this.iRatio = aRatio;
        this.iOriginalRatio = aRatio;
        this.iReferenceFactor = aRf;
        this.iDeterminationFactor = aDf;
        this.iProfileScore = aPs;
        this.iParentRatioGroup = aParent;

        //set valid true
        iValid = true;
    }

    /**
      * {@inheritDoc}
      */
     public double getRatio(boolean aLog2Ratio) {
         double aRatio = iRatio;
          if(iQuantitativeValidationSingelton.isUseOriginalRatio()){
              aRatio = iOriginalRatio;
          }
          if (aLog2Ratio) {
              return Math.log(aRatio) / Math.log(2);
          } else {
              return aRatio;
          }
     }

     /**
      * {@inheritDoc}
      */
     public String getType() {
         return iType;
     }

     //getters


     /**
      * {@inheritDoc}
      */
     public boolean getValid() {
         return iValid;
     }

     /**
      * {@inheritDoc}
      */
     public String toString() {
         return "" + iRatio + "@" + iValid;
     }

     /**
      * A setter for the QuantitationStoredInDb property
      *
      * @param lQuant The Quantitation found in the db that is linked to this ratio
      */
     public void setQuantitationStoredInDb(QuantitationExtension lQuant) {
         iQuantitationStoredInDb = lQuant;
     }

     /**
      * This method gives the Quantitation found in the db that is linked to this ratio
      *
      * @return QuantitationExtension The Quantitation found in the db linked to this ratio
      */
     public QuantitationExtension getQuantitationStoredInDb() {
         return iQuantitationStoredInDb;
     }

     /**
      * Setter for the Valid property
      *
      * @param aValid
      */
     public void setValid(boolean aValid) {
         iValid = aValid;
     }

     /**
      * Setter for the comment property
      *
      * @param aComment
      */
     public void setComment(String aComment) {
         iComment = aComment;
     }

     /**
      * Getter for the comment property. If we are in the database mode, it will get the comment from the
      * QuantitationExtension linked to this Ratio.
      *
      * @return String with the comment on the valid status of the ratio
      */
     public String getComment() {
         if (iQuantitativeValidationSingelton.isDatabaseMode()) {
             //in database mode
             //get the comment linked to the Quantitation stored in the db
             return this.getQuantitationStoredInDb().getComment();
         }
         //not in database mode
         //return the comment to linked to this DistillerRatio
         return iComment;
     }


     /**
      * Getter for the parent RatioGroup
      * @return RatioGroup
      */
     public RatioGroup getParentRatioGroup() {
         return iParentRatioGroup;
     }

    public void setType(String lType) {
        iType = lType;
    }

    public double getOriginalRatio(boolean aLog2Ratio) {
        if (aLog2Ratio) {
            return Math.log(iOriginalRatio) / Math.log(2);
        } else {
            return iOriginalRatio;
        }
    }


    public void setRecalculatedRatio(double lNewRatio) {
        iRatio = Math.pow(2,lNewRatio);
        iUpdates = iUpdates + 1;
    }

        public void setRecalculatedRatio(double lNewRatio, boolean lLog2) {
        if(lLog2){
            iRatio = Math.pow(2,lNewRatio);
        } else {
            iRatio = lNewRatio;
        }
        iUpdates = iUpdates + 1;
    }

    public void setOriginalRatio(double lOriginalRatio){
        iOriginalRatio = Math.pow(2,lOriginalRatio);
    }

    /**
     * Getter for the reference factor
     * @return double with the reference factor
     */
    public double getReferenceFactor() {
        return iReferenceFactor;
    }

    /**
     * Getter for the determination factor
     * @return double with the determination factor
     */
    public double getDeterminationFactor() {
        return iDeterminationFactor;
    }

    /**
     * Getter for the profile score
     * @return double with the profile score
     */
    public double getProfileScore() {
        return iProfileScore;
    }

    public void setNormalizationPart(int lNumber){
        this.iPartNumber = lNumber;
    }

    public int getNormatlizationPart(){
        return this.iPartNumber;
    }

    public double getPreNormalizedMAD(){
        return this.iPreNormMAD;
    }

    public void setPreNormalizedMAD(double lPreMAD){
        this.iPreNormMAD = lPreMAD;
    }

    public double getNormalizedMAD(){
        return this.iNormMAD;
    }

    public void setNormalizedMAD(double lMAD){
        this.iNormMAD = lMAD;
    }

    public int getNumberOfRatioUpdates(){
        return iUpdates;
    }

    public void setIndex(double v){
        iIndex = v;
    }

    public void setOriginalIndex(double v){
        iOriginalIndex = v;
    }

    public double getIndex(){
        return iIndex;
    }

    public double getOriginalIndex(){
        return iOriginalIndex;
    }

    public void setInverted(boolean lInverted){
        this.iInverted = lInverted;
    }

    public boolean getInverted(){
        return this.iInverted;
    }
}
