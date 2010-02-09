package com.compomics.rover.general.quantitation.sorters;

import com.compomics.rover.general.quantitation.QuantitativeProtein;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 18-Jan-2010
 * Time: 09:44:37
 * To change this template use File | Settings | File Templates.
 */
public class QuantitativeProteinSorterByProteinPValue implements Comparator<QuantitativeProtein> {

    /**
     * The ratio type
     */
    private String iType;
    /**
     * The constructor
     *
     * @param aType Ratio type
     */
    public QuantitativeProteinSorterByProteinPValue(String aType) {
        this.iType = aType;
    }
    public int compare(QuantitativeProtein o1, QuantitativeProtein o2) {
        if(o2.getProteinPvalue(iType, -1) - o1.getProteinPvalue(iType, -1) > 0){
            return 1;
        }
        if(o2.getProteinPvalue(iType, -1) - o1.getProteinPvalue(iType, -1) < 0){
            return -1;
        }
        return 0;
    }
}