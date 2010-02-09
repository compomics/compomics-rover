package com.compomics.rover.general.quantitation;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 11-Dec-2009
 * Time: 11:12:23
 * To change this template use File | Settings | File Templates.
 */
public class MergedRatioType {


    private String iName;
    private HashMap<Integer, String> iIndexLinks = new HashMap<Integer, String>();
    private HashMap<Integer, Boolean> iIndexInverted = new HashMap<Integer, Boolean>();

    public MergedRatioType(String iName) {
        this.iName = iName;
    }

    public void addRatioType(int aIndex, String aName, boolean aInverted){
        iIndexLinks.put(aIndex, aName);
        iIndexInverted.put(aIndex, aInverted);
    }

    public String toString() {
        return iName;
    }

    public String getOriginalForIndex(int lIndex) {
        return iIndexLinks.get(lIndex);
    }

    public boolean isInverted(int lIndex){
        return iIndexInverted.get(lIndex);
    }
}
