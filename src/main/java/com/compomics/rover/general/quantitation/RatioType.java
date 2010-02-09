package com.compomics.rover.general.quantitation;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 02-Oct-2009
 * Time: 10:29:24
 * To change this template use File | Settings | File Templates.
 */
public class RatioType {

    private String iType;
    private String[] iComponents;

    public RatioType(String iType, String[] iComponents) {
        this.iType = iType;
        this.iComponents = iComponents;
    }


    public String getType() {
        return iType;
    }

    public String[] getComponents() {
        return iComponents;
    }
}

