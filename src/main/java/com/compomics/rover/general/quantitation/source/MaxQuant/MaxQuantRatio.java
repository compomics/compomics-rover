package com.compomics.rover.general.quantitation.source.MaxQuant;

import com.compomics.rover.general.db.accessors.QuantitationExtension;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.enumeration.MaxQuantScoreType;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 20-Apr-2009
 * Time: 08:51:46
 */

/**
 * The MaxQuant ratio
 */
public class MaxQuantRatio implements Ratio {
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
     * The normalized ratio from MaxQuant
     */
    private Double iRatioNorm;
    /**
     * The significance A ratio from MaxQuant
     */
    private Double iRatioSignA;
    /**
     * The significance A ratio from MaxQuant
     */
    private Double iRatioSignB;

    private double iOriginalRatio;
    private int iPartNumber = 0;
    private double iPreNormMAD;
    private double iNormMAD;


    /**
      * Constructs a new DistillerRatio instance.
      *
      * @param aRatio   The Ratio measurement
      * @param aType    The ratio type
      * @param aValid   The valid status for this ratio
      * @param aParentRatioGroup The parent RatioGroup
      */
     public MaxQuantRatio(Double aRatio, Double aRatioNorm, Double aRatioSignA, Double aRatioSignB, String aType, boolean aValid, RatioGroup aParentRatioGroup) {
         this.iRatio = aRatio;
         this.iOriginalRatio = aRatio;
         this.iRatioNorm = aRatioNorm;
         this.iRatioSignA = aRatioSignA;
         this.iRatioSignB = aRatioSignB;
         this.iType = aType;
         this.iValid = aValid;
         this.iParentRatioGroup = aParentRatioGroup;
     }

     /**
      * {@inheritDoc}
      */
     public double getRatio(boolean aLog2Ratio) {
         double aRatio = 0.0;
         if(iQuantitativeValidationSingelton.getMaxQuantScoreType() == MaxQuantScoreType.RATIO){
             aRatio = iRatio;
         } else if(iQuantitativeValidationSingelton.getMaxQuantScoreType() == MaxQuantScoreType.NORMALIZED){
             aRatio = iRatioNorm;
         } else if(iQuantitativeValidationSingelton.getMaxQuantScoreType() == MaxQuantScoreType.SIGN_A){
             aRatio = iRatioSignA;
         } else if(iQuantitativeValidationSingelton.getMaxQuantScoreType() == MaxQuantScoreType.SIGN_B){
             aRatio = iRatioSignB;
         }

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

    public void setType(String lType) {
        iType = lType;
    }


     /**
      * Getter for the parent RatioGroup
      * @return RatioGroup
      */
     public RatioGroup getParentRatioGroup() {
         return iParentRatioGroup;
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
    }

    public void setRecalculatedNormRatio(double lNewRatio) {
        iRatioNorm = Math.pow(2,lNewRatio);
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

 }

