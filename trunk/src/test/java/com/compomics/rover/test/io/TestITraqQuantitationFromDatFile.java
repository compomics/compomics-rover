package com.compomics.rover.test.io;

import junit.TestCaseLM;
import junit.framework.Assert;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.fileio.readers.Mdf_iTraqReader;
import com.compomics.rover.general.interfaces.PeptideIdentification;
import com.compomics.mascotdatfile.util.mascot.MascotDatfile;

import java.util.ArrayList;
import java.util.Vector;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 2-apr-2009
 * Time: 10:58:29
 */
public class TestITraqQuantitationFromDatFile  extends TestCaseLM {

    public TestITraqQuantitationFromDatFile() {
        this("test case for the iTRAQ quantitations from a mascot result file.");
    }

    public TestITraqQuantitationFromDatFile(String aName) {
        super(aName);
    }

    /**
     * This method simply test the creation of a Mascot generic file.
     */
    public void testItraqFromDatFile() {

        MascotDatfile lMdf = null;

        String lITRAQTestFile = getFullFilePath("F001657.dat");
        Mdf_iTraqReader lReader = new Mdf_iTraqReader(new File(lITRAQTestFile), null, null );
        lReader.setThreshold(0.95);

        RatioGroupCollection lRatioGroupCollection = lReader.getRatioGroupCollection();

        Assert.assertEquals(lRatioGroupCollection.getMetaData(QuantitationMetaType.FILENAME), "F001657.dat");

        Assert.assertEquals(lRatioGroupCollection.size(), 135);

        Vector lComponentTypes = lRatioGroupCollection.getComponentTypes();
        Assert.assertEquals(lComponentTypes.size(), 4);
        Assert.assertEquals("114", lComponentTypes.get(0));
        Assert.assertEquals("115", lComponentTypes.get(1));
        Assert.assertEquals("116", lComponentTypes.get(2));
        Assert.assertEquals("117", lComponentTypes.get(3));

        Vector lRatioTypes = lRatioGroupCollection.getRatioTypes();
        Assert.assertEquals(lRatioTypes.size(),3);
        Assert.assertEquals("115/114", lRatioTypes.get(0));
        Assert.assertEquals("116/114", lRatioTypes.get(1));
        Assert.assertEquals("117/114", lRatioTypes.get(2));

        // Test the RatioGroups themselve!
        // Fence posts : first and last.
        RatioGroup lRatioGroup = lRatioGroupCollection.get(0);
        Assert.assertEquals(1, lRatioGroup.getNumberOfIdentifications());
        Assert.assertEquals(3, lRatioGroup.getNumberOfRatios());
        Assert.assertEquals("EIMAR", lRatioGroup.getPeptideSequence());
        Assert.assertEquals(0.9468, (lRatioGroup.getRatio(0)).getRatio(false));
        Assert.assertEquals("115/114", (lRatioGroup.getRatio(0)).getType());
        Assert.assertEquals(true, (lRatioGroup.getRatio(0)).getValid());

        lRatioGroup = lRatioGroupCollection.get(lRatioGroupCollection.size()-1);
        Assert.assertEquals(1, lRatioGroup.getNumberOfIdentifications());
        Assert.assertEquals(3, lRatioGroup.getNumberOfRatios());
        Assert.assertEquals("DSLDENEATMCQAYEER", lRatioGroup.getPeptideSequence());
        Assert.assertEquals(0.4626, (lRatioGroup.getRatio(2)).getRatio(false));
        Assert.assertEquals("117/114", (lRatioGroup.getRatio(2)).getType());
        PeptideIdentification lId = lRatioGroupCollection.get(lRatioGroupCollection.size() - 1).getIdentification(0);
        Assert.assertEquals("LZTS2_HUMAN", lId.getAccession());
        Assert.assertEquals(551.9439, lId.getPrecursor());

    }

}