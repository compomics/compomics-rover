package com.compomics.rover.general.quantitation.sorters;

import org.apache.log4j.Logger;

import com.compomics.rover.general.quantitation.QuantitativeProtein;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 21-dec-2008
 * Time: 10:52:31
 */


/**
 * This implementation of the Comparator class sorts DistillerProteins by protein mean for a specific ratio type
 */
public class QuantitativeProteinSorterByProteinMean implements Comparator<QuantitativeProtein> {
	// Class specific log4j logger for QuantitativeProteinSorterByProteinMean instances.
	 private static Logger logger = Logger.getLogger(QuantitativeProteinSorterByProteinMean.class);

    /**
     * The ratio type
     */
    private String iType;
    /**
     * The constructor
     *
     * @param aType Ratio type
     */
    public QuantitativeProteinSorterByProteinMean(String aType) {
        this.iType = aType;
    }
    public int compare(QuantitativeProtein o1, QuantitativeProtein o2) {
        if(o2.getProteinRatio(iType) - o1.getProteinRatio(iType) > 0){
            return 1;
        }
        if(o2.getProteinRatio(iType) - o1.getProteinRatio(iType) < 0){
            return -1;
        }
        return 0;
    }
}
