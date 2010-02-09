package com.compomics.rover.general.fileio.files;

import com.compomics.util.interfaces.Flamable;

import java.io.*;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 16-May-2009
 * Time: 12:10:49
 */

/**
 * This class reads the census out file
 */
public class CensusOut {
    /**
     * The census out file
     */
    private File iCensusOutFile;
    /**
     * Vector with peptide info hashmaps
     */
    private Vector<HashMap> iPeptideInfos = new Vector<HashMap>();
    /**
     * The flamable
     */
    private Flamable iFlamable;


    /**
     * CONSTRUCTOR
     * @param aCensusOutFile The census out file
     * @param aFlamable The Flamable
     */
    public CensusOut(File aCensusOutFile, Flamable aFlamable){
        this.iFlamable = aFlamable;
        this.iCensusOutFile = aCensusOutFile;

        int lUnknownAccessionCounter = 1;

        //read the file line by line
        try{
            FileReader freader = new FileReader(iCensusOutFile);
            LineNumberReader lnreader = new LineNumberReader(freader);
            String line = "";
            String lAccession = "";
            while ((line = lnreader.readLine()) != null) {
                //split the line
                String[] lElements = line.split("\t");
                //check if it's a protein line
                if(line.startsWith("P")){
                    if(lElements[1].length() != 0){
                        if(lElements[1].trim().equalsIgnoreCase("*")){
                            lAccession = "UnknownAccession" + lUnknownAccessionCounter;
                            lUnknownAccessionCounter = lUnknownAccessionCounter + 1;
                        } else {
                            lAccession = line.substring(line.indexOf("\t") + 1, line.indexOf("\t",line.indexOf("\t") + 1 ));
                        }
                    }
                } else if(line.startsWith("S")){
                    //it's a peptide ratio line

                    //create the hashmap
                    HashMap lPeptide = new HashMap();
                    //add the protein accession
                    lPeptide.put("accession", lAccession);
                    //set unique
                    if(lElements[1].equalsIgnoreCase("U")){
                        lPeptide.put("unique", true);
                    } else {
                        lPeptide.put("unique", false);
                    }
                    //set the peptide sequence
                    String lSequence = lElements[2];
                    //if there are "." or "-" we will remove them
                    lSequence = lSequence.replace(".","");
                    lSequence = lSequence.replace("-","");
                    lPeptide.put("sequence", lSequence);
                    //FILE_NAME	SCAN	CS
                    //set the ratio
                    lPeptide.put("ratio", lElements[3]);
                    //set the regression factor
                    lPeptide.put("regression_factor", lElements[4]);
                    //set the determination factor
                    lPeptide.put("determination_factor", lElements[5]);
                    //set xcorr
                    lPeptide.put("xcorr", lElements[6]);
                    //set deltaCN
                    lPeptide.put("detlaCN", lElements[7]);
                    //set SAM_INT
                    lPeptide.put("sam_int", lElements[8]);
                    //set REF_INT
                    lPeptide.put("ref_int", lElements[9]);
                    //set area_ratio
                    lPeptide.put("area_ratio", lElements[10]);
                    //set profile score
                    lPeptide.put("profile_score", lElements[11]);
                    //set filename
                    lPeptide.put("filename", lElements[12]);
                    //set scan
                    lPeptide.put("scan", lElements[13]);
                    //set cn
                    lPeptide.put("cn", lElements[14]);

                    //add the hashmap to the vector
                    iPeptideInfos.add(lPeptide);
                }
            }
        } catch (FileNotFoundException e1) {
            iFlamable.passHotPotato(new Throwable("Problem reading the census out file"));
            e1.printStackTrace();
        } catch (IOException e1) {
            iFlamable.passHotPotato(new Throwable("Problem reading the census out file"));
            e1.printStackTrace();
        } catch (Exception e){
            iFlamable.passHotPotato(new Throwable("Problem reading the census out file"));
            e.printStackTrace();
        }
    }

    /**
     * This is the getter for the vector with the peptide info hashmaps
     * @return
     */
    public Vector<HashMap> getPeptideInfos() {
        return iPeptideInfos;
    }

}
