package com.compomics.rover.general.quantitation.sorters;

import org.apache.log4j.Logger;

import com.compomics.rover.general.quantitation.QuantitativeProtein;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 14-Jan-2010
 * Time: 10:07:36
 * To change this template use File | Settings | File Templates.
 */
public class QuantitativeProteinSorterByDiffProteinMeans implements Comparator<QuantitativeProtein> {
	// Class specific log4j logger for QuantitativeProteinSorterByDiffProteinMeans instances.
	 private static Logger logger = Logger.getLogger(QuantitativeProteinSorterByDiffProteinMeans.class);

    /**
     * The ratio type
     */
    private String iType;
    /**
     * The constructor
     *
     * @param aType Ratio type
     */
    public QuantitativeProteinSorterByDiffProteinMeans(String aType) {
        this.iType = aType;
    }
    public int compare(QuantitativeProtein o1, QuantitativeProtein o2) {
        if(o2.getDiffProteinRatios(iType) - o1.getDiffProteinRatios(iType) > 0){
            return 1;
        }
        if(o2.getDiffProteinRatios(iType) - o1.getDiffProteinRatios(iType) < 0){
            return -1;
        }
        return 0;
    }
}
