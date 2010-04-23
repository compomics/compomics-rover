package com.compomics.rover.general.enumeration;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 27-Apr-2009
 * Time: 10:13:30
 */

/**
 * The peptide ratio comments that can be given
 */
public enum RatioComment {
    NO_COMMENT("No comment"), BAD_IDENTIFICATION("Bad identification"), OVELAPPING_PATTENS("Overlapping patterns"),
    XIC_TO_BROAD("XIC to broad"), XIC_ON_WRONG_POSITION("XIC on wrong position"), BAD_CORRELATION("Bad correlation"), EMPTY_SCAN("Empty scan"), IN_NOISE("In noise")
    , CONTAMINATION("Contamination"),REGULATED("Regulated"), SINGLE_LIGHT("Single light"), SINGLE_MEDIUM("Single medium"), SINGLE_HEAVY("Single heavy"), SINGLE("Single");

    private String iTitle;

    RatioComment(String aTitle){
        this.iTitle = aTitle;
    }
    
    public String toString(){
        return iTitle;
    }

}
