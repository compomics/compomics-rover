package com.compomics.rover.general.enumeration;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Kenny Date: 24-nov-2008 Time: 15:19:27
 * The 'QuantitationMetaTypes ' class was created for typing quantitation related meta information.
 */

/**
 * This enumeration holds different quantitation metatypes. 
 */
public enum QuantitationMetaType {
    /**
     * The filename with the raw information of a RatioGroupCollection.
     */
    FILENAME("Filename"),

    /**
     * The runname of the RatioGroupCollection.
     */
    RUNNAME("Runname"),

    /**
     * The runname of the RatioGroupCollection.
     */
    MASCOTTASKID("MascotTaskId"),

    /**
     * The datfileid
     */
    DATFILEID("Datfileid");


    private String iName;

    private QuantitationMetaType(String aName) {
        iName = aName;
    }


    /**
     * Returns a name for the enumeration type.
     * @return String for the type.
     */
    public String toString() {
        return iName;
    }
}
