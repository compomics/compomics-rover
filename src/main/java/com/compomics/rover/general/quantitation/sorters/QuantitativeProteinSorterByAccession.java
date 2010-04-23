package com.compomics.rover.general.quantitation.sorters;

import org.apache.log4j.Logger;

import com.compomics.rover.general.quantitation.QuantitativeProtein;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 19-dec-2008
 * Time: 8:45:01
 */

/**
 * This implementation of the Comparator class sorts DistillerProteins by accession
 */
public class QuantitativeProteinSorterByAccession implements Comparator<QuantitativeProtein> {
	// Class specific log4j logger for QuantitativeProteinSorterByAccession instances.
	 private static Logger logger = Logger.getLogger(QuantitativeProteinSorterByAccession.class);
    public int compare(QuantitativeProtein o1, QuantitativeProtein o2) {
        return o1.getAccession().compareTo(o2.getAccession());
    }
}
