package com.compomics.rover.general.quantitation.sorters;

import org.apache.log4j.Logger;

import com.compomics.rover.general.quantitation.QuantitativeProtein;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 19-dec-2008
 * Time: 8:47:11
 */

/**
 * This implementation of the Comparator class sorts DistillerProteins by the number of RatioGroups linked to the proteins
 */
public class QuantitativeProteinSorterByRatioGroupNumbers implements Comparator<QuantitativeProtein> {
	// Class specific log4j logger for QuantitativeProteinSorterByRatioGroupNumbers instances.
	 private static Logger logger = Logger.getLogger(QuantitativeProteinSorterByRatioGroupNumbers.class);
    public int compare(QuantitativeProtein o1, QuantitativeProtein o2) {
        return o2.getNumberOfRatioGroups() - o1.getNumberOfRatioGroups();
    }
}
