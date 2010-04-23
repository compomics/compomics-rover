package com.compomics.rover.general.quantitation.sorters;

import org.apache.log4j.Logger;

import com.compomics.rover.general.interfaces.Ratio;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 22-Feb-2010
 * Time: 16:49:01
 * To change this template use File | Settings | File Templates.
 */
public class RatioSorterByChargeAndIntensity implements Comparator<Ratio> {
	// Class specific log4j logger for RatioSorterByChargeAndIntensity instances.
	 private static Logger logger = Logger.getLogger(RatioSorterByChargeAndIntensity.class);


    /**
     * The ratio type
     */
    private String iComponent;
    /**
     * The constructor
     *
     */

    public RatioSorterByChargeAndIntensity(String aComponent) {
        this.iComponent = aComponent;
    }


    public int compare(Ratio o1, Ratio o2) {

        Number lInt1 = 0.0;
        Number lInt2 = 0.0;

        lInt1 = o1.getParentRatioGroup().getIdentification(0).getCharge();
        lInt2 = o2.getParentRatioGroup().getIdentification(0).getCharge();

        if(lInt1.doubleValue() > lInt2.doubleValue()){
            return 1;
        } else if(lInt1.doubleValue() < lInt2.doubleValue()){
            return -1;
        }

        double lIntens1 = o1.getParentRatioGroup().getIntensityForComponent(iComponent);
        double lIntens2 = o2.getParentRatioGroup().getIntensityForComponent(iComponent);

        if(lIntens1 > lIntens2){
            return 1;
        } else if(lIntens1 < lIntens2){
            return -1;
        }
        return 0;
    }



}
