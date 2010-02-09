package com.compomics.rover.general.quantitation.sorters;

import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.QuantitativeProtein;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatioGroup;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatioGroup;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.interfaces.Ratio;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 04-Feb-2010
 * Time: 11:43:01
 * To change this template use File | Settings | File Templates.
 */
public class RatioSorterByIntensities implements Comparator<Ratio> {

    /**
     * The ratio type
     */
    private String iType;
    /**
     * The constructor
     *
     * @param aType Ratio type
     */

    public RatioSorterByIntensities(String aType) {
        this.iType = aType;
    }


    public int compare(Ratio o1, Ratio o2) {

        double lInt1 = 0.0;
        double lInt2 = 0.0;
        lInt1 = o1.getParentRatioGroup().getSummedIntensityForRatioType(iType);
        lInt2 = o2.getParentRatioGroup().getSummedIntensityForRatioType(iType);

        if(lInt1 > lInt2){
            return 1;
        } else if(lInt1 < lInt2){
            return -1;
        }
        return 0;
    }



}
