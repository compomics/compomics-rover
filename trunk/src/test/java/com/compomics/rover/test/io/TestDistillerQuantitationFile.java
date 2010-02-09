package com.compomics.rover.test.io;

import junit.TestCaseLM;
import junit.framework.Assert;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatioGroup;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatio;
import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.fileio.files.RovFile;
import com.compomics.util.interfaces.Flamable;

import java.util.ArrayList;
import java.util.Vector;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 2-apr-2009
 * Time: 10:54:12
 */
/**
 * This class implements the full test scenario for the MascotQuantitationProcessor class.
 *
 * @author Kenny Helsens
 */

public class TestDistillerQuantitationFile extends TestCaseLM {

    public TestDistillerQuantitationFile() {
        this("test case for the MascotGenericFile class.");
    }

    public TestDistillerQuantitationFile(String aName) {
        super(aName);
    }

    /**
     * This method simply test the creation of a Mascot generic file.
     */
    public void testDistillerQuantiation_1() {

        RovFile lRovFile = new RovFile(new File(getFullFilePath("test_distiller_quantitation_1.rov")));
        Assert.assertEquals(true, lRovFile.unzipRovFile());
        lRovFile.unzipRovFile();
        lRovFile.readQuantitationXmlFile();
        lRovFile.match();


        RatioGroupCollection lRatioGroupCollection = lRovFile.getRatioGroupCollection();

        Assert.assertEquals(lRatioGroupCollection.getMetaData(QuantitationMetaType.FILENAME), "test_distiller_quantitation_1.rov");
        Assert.assertEquals(lRatioGroupCollection.getMetaData(QuantitationMetaType.RUNNAME), "test");
        // Verify!
        Assert.assertEquals(lRatioGroupCollection.size(), 29);

        Vector lComponentTypes = lRatioGroupCollection.getComponentTypes();
        Assert.assertEquals(lComponentTypes.size(), 2);
        Assert.assertEquals("light", lComponentTypes.get(0));
        Assert.assertEquals("heavy", lComponentTypes.get(1));

        Vector lRatioTypes = lRatioGroupCollection.getRatioTypes();
        Assert.assertEquals(lRatioTypes.size(),1);
        Assert.assertEquals("L/H", lRatioTypes.get(0));

        // Test the RatioGroups themselve!
        // Fence posts : first and last.
        DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) lRatioGroupCollection.get(0);
        Assert.assertEquals(3, lRatioGroup.getNumberOfIdentifications());
        Assert.assertEquals(1, lRatioGroup.getNumberOfRatios());
        Assert.assertEquals("ESVVFVQTDK", lRatioGroup.getPeptideSequence());
        Assert.assertEquals(0.244488, ((DistillerRatio)lRatioGroup.getRatio(0)).getRatio(false));
        Assert.assertEquals("L/H", ((DistillerRatio)lRatioGroup.getRatio(0)).getType());
        Assert.assertEquals(0.0957103, ((DistillerRatio)lRatioGroup.getRatio(0)).getQuality());
        Assert.assertEquals(true, ((DistillerRatio)lRatioGroup.getRatio(0)).getValid());

        lRatioGroup = (DistillerRatioGroup) lRatioGroupCollection.get(lRatioGroupCollection.size()-1);
        Assert.assertEquals(1, lRatioGroup.getNumberOfIdentifications());
        Assert.assertEquals(1, lRatioGroup.getNumberOfRatios());
        Assert.assertEquals("MMKAGGTEIGK", lRatioGroup.getPeptideSequence());
        Assert.assertEquals(0.000948086, ((DistillerRatio)lRatioGroup.getRatio(0)).getRatio(false));
        Assert.assertEquals("L/H", ((DistillerRatio)lRatioGroup.getRatio(0)).getType());
        Assert.assertEquals(0.393534, ((DistillerRatio)lRatioGroup.getRatio(0)).getQuality());
        Assert.assertEquals(false, ((DistillerRatio)lRatioGroup.getRatio(0)).getValid());


        // Find the RatioGroups that should have matched two Identification instances.
        lRatioGroup = (DistillerRatioGroup) lRatioGroupCollection.get(10);
        Assert.assertEquals(1, lRatioGroup.getNumberOfRatios());
        Assert.assertEquals(3, lRatioGroup.getNumberOfTypes());
        Assert.assertEquals("SVIVEPEGIEK", lRatioGroup.getPeptideSequence());

        Assert.assertEquals(0.53943, ((DistillerRatio)lRatioGroup.getRatio(0)).getRatio(false));

        Assert.assertEquals("L/H", ((DistillerRatio)lRatioGroup.getRatio(0)).getType());
        Assert.assertEquals(0.148147, ((DistillerRatio)lRatioGroup.getRatio(0)).getQuality());
        Assert.assertEquals(false, ((DistillerRatio)lRatioGroup.getRatio(0)).getValid());

        Assert.assertEquals(3, lRatioGroup.getNumberOfIdentifications());
        Assert.assertEquals(360, lRatioGroup.getIdentification(0).getDatfile_query());
        Assert.assertEquals(365, lRatioGroup.getIdentification(1).getDatfile_query());


        // Find the RatioGroups that should have matched one Identification instance.
        lRatioGroup = (DistillerRatioGroup) lRatioGroupCollection.get(11);
        Assert.assertEquals(1, lRatioGroup.getNumberOfRatios());
        Assert.assertEquals(1, lRatioGroup.getNumberOfTypes());
        Assert.assertEquals("VTQTFGENMQK", lRatioGroup.getPeptideSequence());

        Assert.assertEquals(1.33237, ((DistillerRatio)lRatioGroup.getRatio(0)).getRatio(false));
        Assert.assertEquals("L/H", ((DistillerRatio)lRatioGroup.getRatio(0)).getType());
        Assert.assertEquals(0.0603027, ((DistillerRatio)lRatioGroup.getRatio(0)).getQuality());
        Assert.assertEquals(true, ((DistillerRatio)lRatioGroup.getRatio(0)).getValid());

        Assert.assertEquals(1, lRatioGroup.getNumberOfIdentifications());
        Assert.assertEquals(425, lRatioGroup.getIdentification(0).getDatfile_query());

    }

    /**
     * This method simply test the creation of a Mascot generic file.
     */
    public void testDistillerQuantiation_2() {

        final boolean[] lFlamableThrown = new boolean[]{false};
        final String[] lFlamableMessage = new String[]{"test"};

        // Simulate the Flamable instance.
        Flamable lFlamable = new Flamable() {
            public void passHotPotato(final Throwable aThrowable) {
                lFlamableThrown[0] = true;
                lFlamableMessage[0] = aThrowable.getMessage();
            }

            public void passHotPotato(final Throwable aThrowable, final String aMessage) {
                // Empty.
            }
        };

        RovFile lRovFile = new RovFile(new File(getFullFilePath("test_distiller_quantitation_2.rov")));
        Assert.assertEquals(true, lRovFile.unzipRovFile());
        lRovFile.setFlamable(lFlamable);
        lRovFile.unzipRovFile();
        lRovFile.readQuantitationXmlFile();
        lRovFile.match();

        Assert.assertTrue("Flamable for empty file was not thrown!!", true);
        Assert.assertTrue("Flamable message was incorrect!!", lFlamableMessage[0].indexOf("No information could be extracted") > -1);
    }
}
