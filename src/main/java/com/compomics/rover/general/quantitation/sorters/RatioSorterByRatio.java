package com.compomics.rover.general.quantitation.sorters;

import org.apache.log4j.Logger;

import com.compomics.rover.general.interfaces.Ratio;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 01-Apr-2010
 * Time: 16:06:20
 * To change this template use File | Settings | File Templates.
 */
public class RatioSorterByRatio implements Comparator<Ratio> {
	// Class specific log4j logger for RatioSorterByRatio instances.
	 private static Logger logger = Logger.getLogger(RatioSorterByRatio.class);

    /**
     * The constructor
     *
     */

    public RatioSorterByRatio() {

    }


    public int compare(Ratio o1, Ratio o2) {

        double lInt1 = 0.0;
        double lInt2 = 0.0;
        lInt1 = o1.getRatio(true);
        lInt2 = o2.getRatio(true);
        if(lInt1 > lInt2){
            return 1;
        } else if(lInt1 < lInt2){
            return -1;
        }
        return 0;
    }



}
