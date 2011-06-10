package com.compomics.rover.general.quantitation.source.thermo_msf;

import com.compomics.rover.general.db.accessors.QuantitationExtension;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.thermo_msf_parser.msf.QuanResult;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 16-Mar-2011
 * Time: 08:55:42
 */

public class ThermoMsfRatio implements Ratio {
    // Class specific log4j logger for ITraqRatio instances.
    private static Logger logger = Logger.getLogger(ThermoMsfRatio.class);
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
     * The valid status of the Ratio.
     */
    private boolean iValid;
    /**
     * The quantitation stored in the database and linked to this Ratio.
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
     * The quan result
     */
    private QuanResult iQuanResult;
    /**
     * The connection to the msf file
     */
    private Connection iConnection;
    /**
     * The file id from this ratio in the msf file
     */
    private int iFileId;
    /**
     * The quantification components
     */
    private Vector<String> iComponents = new Vector<String>();
    /**
     * The channel ids of the quantification components
     */
    private Vector<Integer> iChannelIds = new Vector<Integer>();

    private double iNumeratorIntensity;
    private double iDenominatorIntensity;


    /**
     * Constructs a new DistillerRatio instance.
     *
     * @param aRatio            The Ratio measurement
     * @param aType             The ratio type
     * @param aValid            The valid status for this ratio
     * @param aParentRatioGroup The parent RatioGroup
     */
    public ThermoMsfRatio(Double aRatio, String aType, boolean aValid, RatioGroup aParentRatioGroup, QuanResult lQuanResult, Connection lConn, int lFileId, Vector<String> lComp, Vector<Integer> lChannels) {
        this.iRatio = aRatio;
        this.iOriginalRatio = aRatio;
        this.iType = aType;
        this.iValid = aValid;
        this.iParentRatioGroup = aParentRatioGroup;
        this.iQuanResult = lQuanResult;
        this.iConnection = lConn;
        this.iFileId = lFileId;
        this.iComponents = lComp;
        this.iChannelIds = lChannels;
    }

    /**
     * This getter gives the name of the channel for a given channelid
     * @param lChannelId Int with the channelid
     * @return String with the name of the channel
     */
    public String getQuanChannelNameById(int lChannelId){
        for(int i = 0; i<iChannelIds.size(); i ++){
            if(iChannelIds.get(i)==lChannelId){
                return iComponents.get(i);
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public double getRatio(boolean aLog2Ratio) {
        double aRatio = iRatio;
        if (iQuantitativeValidationSingelton.isUseOriginalRatio()) {
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
     *
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
        iRatio = Math.pow(2, lNewRatio);
        iUpdates = iUpdates + 1;
    }

    public void setRecalculatedRatio(double lNewRatio, boolean lLog2) {
        if (lLog2) {
            iRatio = Math.pow(2, lNewRatio);
        } else {
            iRatio = lNewRatio;
        }
        iUpdates = iUpdates + 1;
    }

    public void setOriginalRatio(double lOriginalRatio) {
        iOriginalRatio = Math.pow(2, lOriginalRatio);
    }

    public void setNormalizationPart(int lNumber) {
        this.iPartNumber = lNumber;
    }

    public int getNormatlizationPart() {
        return this.iPartNumber;
    }

    public double getPreNormalizedMAD() {
        return this.iPreNormMAD;
    }

    public void setPreNormalizedMAD(double lPreMAD) {
        this.iPreNormMAD = lPreMAD;
    }

    public double getNormalizedMAD() {
        return this.iNormMAD;
    }

    public void setNormalizedMAD(double lMAD) {
        this.iNormMAD = lMAD;
    }

    public int getNumberOfRatioUpdates() {
        return iUpdates;
    }

    public void setIndex(double v) {
        iIndex = v;
    }

    public void setOriginalIndex(double v) {
        iOriginalIndex = v;
    }

    public double getIndex() {
        return iIndex;
    }

    public double getOriginalIndex() {
        return iOriginalIndex;
    }

    public void setInverted(boolean lInverted) {
        this.iInverted = lInverted;
    }

    public boolean getInverted() {
        return this.iInverted;
    }

    public QuanResult getQuanResult() {
        return iQuanResult;
    }

    public Connection getConnection(){
        return iConnection;
    }

    public int getFileId() {
        return iFileId;
    }

    public double getNumeratorIntensity() {
        return iNumeratorIntensity;
    }

    public void setNumeratorIntensity(double aNominatorIntensity) {
        iNumeratorIntensity = aNominatorIntensity;
    }

    public double getDenominatorIntensity() {
        return iDenominatorIntensity;
    }

    public void setDenominatorIntensity(double aDenominatorIntensity) {
        iDenominatorIntensity = aDenominatorIntensity;
    }
}

