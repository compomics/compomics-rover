package com.compomics.rover.general.quantitation.sorters;

import com.compomics.rover.general.quantitation.source.thermo_msf.ThermoMsfRatio;
import org.apache.log4j.Logger;

import com.compomics.rover.general.interfaces.Ratio;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 16-Feb-2010
 * Time: 16:24:24
 * To change this template use File | Settings | File Templates.
 */
public class RatioSorterByIntensity implements Comparator<Ratio> {
	// Class specific log4j logger for RatioSorterByIntensity instances.
	 private static Logger logger = Logger.getLogger(RatioSorterByIntensity.class);

    /**
     * The ratio type
     */
    private String iComponent;
    private String iRatioType;

    /**
     * The constructor
     *
     * @param aComponent Ratio type
     */

    public RatioSorterByIntensity(String aComponent, String aRatioType) {
        this.iComponent = aComponent;
        this.iRatioType = aRatioType;
    }


    public int compare(Ratio o1, Ratio o2) {

        double lInt1 = 0.0;
        double lInt2 = 0.0;
        lInt1 = o1.getParentRatioGroup().getIntensityForComponent(iComponent);
        if(lInt1 == 0.0){
            lInt1 = o1.getParentRatioGroup().getSummedIntensityForRatioType(iRatioType);
        }
        if(o1 instanceof ThermoMsfRatio){
            lInt1 = ((ThermoMsfRatio) o1).getNumeratorIntensity();
        }
        lInt2 = o2.getParentRatioGroup().getIntensityForComponent(iComponent);
        if(lInt2 == 0){
            lInt2 = o2.getParentRatioGroup().getSummedIntensityForRatioType(iRatioType);
        }
        if(o2 instanceof ThermoMsfRatio){
            lInt2 = ((ThermoMsfRatio) o2).getNumeratorIntensity();
        }
        if(lInt1 > lInt2){
            return 1;
        } else if(lInt1 < lInt2){
            return -1;
        }
        return 0;
    }



}
