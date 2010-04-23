package com.compomics.rover.general.quantitation.sorters;

import org.apache.log4j.Logger;

import com.compomics.rover.general.quantitation.QuantitativeProtein;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 18-Jan-2010
 * Time: 09:44:37
 * To change this template use File | Settings | File Templates.
 */
public class QuantitativeProteinSorterByProteinZscore implements Comparator<QuantitativeProtein> {
	// Class specific log4j logger for QuantitativeProteinSorterByProteinZscore instances.
	 private static Logger logger = Logger.getLogger(QuantitativeProteinSorterByProteinZscore.class);

    /**
     * The ratio type
     */
    private String iType;
    /**
     * The constructor
     *
     * @param aType Ratio type
     */
    public QuantitativeProteinSorterByProteinZscore(String aType) {
        this.iType = aType;
    }

    public int compare(QuantitativeProtein o1, QuantitativeProtein o2) {
        if(o2.getProteinZScore(iType, -1) - o1.getProteinZScore(iType, -1) > 0){
            return 1;
        }
        if(o2.getProteinZScore(iType, -1) - o1.getProteinZScore(iType, -1) < 0){
            return -1;
        }
        return 0;
    }
}
