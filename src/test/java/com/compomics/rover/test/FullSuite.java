package com.compomics.rover.test;

import com.compomics.rover.test.io.*;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 2-apr-2009
 * Time: 10:53:04
 */
public class FullSuite  extends TestCase {

    public FullSuite() {
        this("Full suite of test for Rover.");
    }

    public FullSuite(String aName) {
        super(aName);
    }

    public static Test suite() {
        TestSuite ts = new TestSuite();

        ts.addTest(new TestSuite(TestDistillerQuantitationFile.class));
        ts.addTest(new TestSuite(TestITraqQuantitationFromDatFile.class));
        ts.addTest(new TestSuite(TestCensusQuantitationFile.class));
        ts.addTest(new TestSuite(TestMsfQuantitationFile.class));
        //ts.addTest(new TestSuite(TestMaxQuantQuantitationFile.class));

        return ts;
    }
}