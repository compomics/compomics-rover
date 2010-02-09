package com.compomics.rover.general.quantitation;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 11-Dec-2009
 * Time: 11:43:51
 * To change this template use File | Settings | File Templates.
 */
public class MergedComponentType {

    private String iName;
    private HashMap<Integer, String> iIndexLinks = new HashMap<Integer, String>();

    public MergedComponentType(String iName) {
        this.iName = iName;
    }

    public void addComponentType(int aIndex, String aName){
        iIndexLinks.put(aIndex, aName);
    }

    public String toString() {
        return iName;
    }

    public String getOriginalForIndex(int lIndex) {
        return iIndexLinks.get(lIndex);
    }
}
