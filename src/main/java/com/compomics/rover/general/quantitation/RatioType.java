package com.compomics.rover.general.quantitation;

import org.apache.log4j.Logger;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 02-Oct-2009
 * Time: 10:29:24
 * To change this template use File | Settings | File Templates.
 */
public class RatioType {
	// Class specific log4j logger for RatioType instances.
	 private static Logger logger = Logger.getLogger(RatioType.class);

    private String iType;
    private String[] iComponents;
    private String iUnregulatedComponent;
    private Vector<String> iUnregulatedComponentsBySet = new Vector<String>();
    private double iMedian;

    public RatioType(String iType, String[] iComponents, String aUnregulatedComponent, double aMedian) {
        this.iType = iType;
        this.iComponents = iComponents;
        this.iUnregulatedComponent = aUnregulatedComponent;
        this.iUnregulatedComponentsBySet.add(aUnregulatedComponent);
        this.iMedian =  aMedian;
    }


    public String getType() {
        return iType;
    }

    public String[] getComponents() {
        return iComponents;
    }

    public String getUnregulatedComponent() {
        return iUnregulatedComponent;
    }

    public void addUnregulatedComponentForSet(String lUnregulatedComponent) {
        iUnregulatedComponentsBySet.add(lUnregulatedComponent);
    }

    public Vector<String> getUnregulatedComponentsBySet() {
        return iUnregulatedComponentsBySet;
    }

    public double getMedian() {
        return iMedian;
    }
}

