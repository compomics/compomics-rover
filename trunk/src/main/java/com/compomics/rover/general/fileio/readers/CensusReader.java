package com.compomics.rover.general.fileio.readers;

import com.compomics.rover.general.fileio.files.CensusOut;
import com.compomics.rover.general.fileio.files.CensusChro;
import com.compomics.rover.general.PeptideIdentification.DefaultPeptideIdentification;
import com.compomics.rover.general.quantitation.source.Census.CensusRatioGroup;
import com.compomics.rover.general.quantitation.source.Census.CensusRatio;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.enumeration.DataType;
import com.compomics.util.interfaces.Flamable;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 16-May-2009
 * Time: 12:09:56
 */

/**
 * This class will reads census files and will create a ratiogroupcollection
 */
public class CensusReader {
    /**
     * The census out file
     */
    private CensusOut iCensusOut;
    /**
     * The census chro file
     */
    private CensusChro iCensusChro;
    /**
     * The ratio group collection
     */
    private RatioGroupCollection iRatioGroupCollection;
    /**
     * The flamable
     */
    private Flamable iFlamable;

    /**
     * The constructor
     * @param aOutFile The census out file
     * @param aChroFile The census chro file
     */
    public CensusReader(File aOutFile, File aChroFile, Flamable aFlamable){
        //create the files
        this.iFlamable = aFlamable;
        this.iCensusOut = new CensusOut(aOutFile, iFlamable);
        this.iCensusChro = new CensusChro(aChroFile, iFlamable);
        //get the peptide info from the files
        Vector<HashMap> iOutPeptides = iCensusOut.getPeptideInfos();
        Vector<HashMap> iChroPeptides = iCensusChro.getPeptideInfos();

        //create the ratiogroup collection
        iRatioGroupCollection = new RatioGroupCollection(DataType.CENSUS);
        Vector<String> lComp = new Vector<String>();
        Vector<String> lType = new Vector<String>();
        lComp.add("L");
        lComp.add("H");
        lType.add("L/H");
        iRatioGroupCollection.setComponentTypes(lComp);
        iRatioGroupCollection.setRatioTypes(lType);

        try{
            //match the peptides from the two files
            for(int i = 0; i<iOutPeptides.size(); i ++){
                String lOutScore = (String) iOutPeptides.get(i).get("xcorr");
                String lOutSequence = (String) iOutPeptides.get(i).get("sequence");
                for(int j = 0; j<iChroPeptides.size(); j ++){
                    String lChroScore = (String) iChroPeptides.get(j).get("xcorr");
                    String lChroSequence = (String) iChroPeptides.get(j).get("seq");
                    if(lOutScore.equalsIgnoreCase(lChroScore) && lOutSequence.equalsIgnoreCase(lChroSequence)){
                        //we found a match between the two different peptides

                        HashMap lOutMap = iOutPeptides.get(i);
                        HashMap lChroMap = iChroPeptides.get(j);

                        //1.create the peptide identifications
                        DefaultPeptideIdentification lId = new DefaultPeptideIdentification();
                        lId.setAccession((String) lOutMap.get("accession"));
                        lId.setCharge(Integer.valueOf( (String) lChroMap.get("charge")));
                        lId.setXcorr(Double.valueOf(lOutScore));
                        lId.setSequence(lOutSequence);
                        lId.setSpectrumFileName((String) lOutMap.get("filename"));
                        lId.setIsoforms("");
                        lId.setType("L");

                        //2.create the ratio group
                        CensusRatioGroup lRatioGroup = new CensusRatioGroup(iRatioGroupCollection);
                        //set the peptide sequence
                        lRatioGroup.setPeptideSequence(lOutSequence);
                        //set the absolute intensities
                        lRatioGroup.setRatioGroupAbsoluteIntensities(new Double[]{Double.valueOf((String)lOutMap.get("sam_int")),Double.valueOf((String)lOutMap.get("ref_int"))});
                        //set the chro
                        lRatioGroup.setChro((String)lChroMap.get("chro"));

                        //3. create the ratio
                        CensusRatio lRatio = new CensusRatio("L/H", Double.valueOf((String) lOutMap.get("ratio")), Double.valueOf((String) lOutMap.get("regression_factor")), Double.valueOf((String) lOutMap.get("determination_factor")), Double.valueOf((String) lOutMap.get("profile_score")), lRatioGroup);

                        //4.add the identification and the ratio to the ratiogroup
                        lRatioGroup.addIdentification(lId,"L");
                        lRatioGroup.addRatio(lRatio);

                        iRatioGroupCollection.add(lRatioGroup);

                    }
                }
            }
        } catch (Exception e){
            iFlamable.passHotPotato(new Throwable("Problem reading the census chro file"));
            e.printStackTrace();
        }
    }

    /**
     * This is the getter for the ratiogroupcollection
     * @return RatioGroupCollection
     */
    public RatioGroupCollection getRatioGroupCollection() {
        return iRatioGroupCollection;
    }
}
