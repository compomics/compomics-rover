package com.compomics.rover.general.quantitation.source.thermo_msf;

import com.compomics.rover.general.db.accessors.IdentificationExtension;
import com.compomics.rover.general.fileio.readers.LimsMsfInfoReader;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 25/03/11
 * Time: 09:52
 * To change this template use File | Settings | File Templates.
 */
public class MsfLimsRatioGroup  extends RatioGroup {
	// Class specific log4j logger for MaxQuantRatioGroup instances.
	 private static Logger logger = Logger.getLogger(MsfLimsRatioGroup.class);

     /**
     * This distiller validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();


    private long iQuanGroupId;
    private String iQuanFileRef;
    private LimsMsfInfoReader.QuantSpectrum iQuantSpectrum;

    public MsfLimsRatioGroup(final RatioGroupCollection aRatioGroupCollection, long lQuanGroupId, String lQuanFileRef){
        super(aRatioGroupCollection);
        this.iQuanGroupId = lQuanGroupId;
        this.iQuanFileRef = lQuanFileRef;
    }



    public long getGroupId() {
        return iQuanGroupId;
    }

    public String getQuanFileRef() {
        return iQuanFileRef;
    }

    /**
     * This method will link identifications to the ratios. This is done by comparing the query number.
     * These identifications were found in the database for a specific datfile linked to the mascot distiller rov file.
     * @param aIdentifications Identifications to match. <b>Hence, the final aim is to link ms_lims Identifications to Quantitation information</b>
     */
    public void linkIdentificationsAndQueries(IdentificationExtension[] aIdentifications){

        for(int i = 0; i<aIdentifications.length; i ++){

            if(aIdentifications[i].getQuantitationGroupId() == iQuanGroupId){
                //the file ref id is correct
                this.addIdentification(aIdentifications[i], aIdentifications[i].getType());
                this.setPeptideSequence(aIdentifications[i].getSequence());
            }
        }
    }

    /**
     * To String method
     * @return String
     */
    public String toString(){
        String lTitle = "";
        // row keys...
        Vector<String> lTypes = iParentCollection.getRatioTypes();
        for(int i = 0; i<lTypes.size(); i ++){
            Ratio lRatio = getRatioByType(lTypes.get(i));
            if(lRatio !=  null){
                lTitle = lTitle + " " + lTypes.get(i) + ": " + this.getRatioByType(lTypes.get(i)).getRatio(iQuantitativeValidationSingelton.isLog2());
            } else {
                lTitle = lTitle + " " + lTypes.get(i) + ": /";
            }
        }
        return lTitle;
    }


    public void setQuantSpectrum(LimsMsfInfoReader.QuantSpectrum quantSpectrum) {
        this.iQuantSpectrum = quantSpectrum;
    }

    public LimsMsfInfoReader.QuantSpectrum getQuantSpectrum() {
        return iQuantSpectrum;
    }
}
