package com.compomics.rover.general.fileio.files;

import org.apache.log4j.Logger;

import psidev.psi.tools.xxindex.index.StandardXpathIndex;
import psidev.psi.tools.xxindex.index.XmlXpathIndexer;
import psidev.psi.tools.xxindex.StandardXpathAccess;

import java.io.*;
import java.util.HashMap;
import java.util.Vector;

import com.compomics.util.interfaces.Flamable;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 16-May-2009
 * Time: 12:11:11
 */

/**
 * This class reads the Census chro file
 */
public class CensusChro {
	// Class specific log4j logger for CensusChro instances.
	 private static Logger logger = Logger.getLogger(CensusChro.class);
    /**
     * The Census chro file
     */
    private File iCensusChro;
    /**
     * Vector with hashmaps with peptide info
     */
    private Vector<HashMap> iPeptideInfos = new Vector<HashMap>();
    /**
     * The flamable
     */
    private Flamable iFlamable;

    /**
     * Constructor
     * @param aCensusChro The census chro file
     * @param aFlamable The Flamable
     */
    public CensusChro(File aCensusChro, Flamable aFlamable){
        this.iCensusChro = aCensusChro;
        this.iFlamable = aFlamable;
        try {
            FileReader freader = new FileReader(iCensusChro);
            LineNumberReader lnreader = new LineNumberReader(freader);
            String line = "";
            HashMap lPeptideInfo = null;
            String lAccession = null;
            while ((line = lnreader.readLine()) != null) {
                //check if it's a peptide
                if(line.trim().startsWith("<peptide")){
                    //it's a peptide
                    //create a new hashmap
                    lPeptideInfo = new HashMap();
                    //get the info and put it in the hashmap
                    lPeptideInfo.put("file",new String(line.substring(line.indexOf("file=") + 6, line.indexOf("\"", line.indexOf("file=") + 6 ))));
                    lPeptideInfo.put("scan",new String(line.substring(line.indexOf("scan=") + 6, line.indexOf("\"", line.indexOf("scan=") + 6 ))));
                    String lChroSequence = new String(line.substring(line.indexOf("seq=") + 5, line.indexOf("\"", line.indexOf("seq=") + 5 )));
                    //if there are "." or "-" we will remove them
                    lChroSequence = lChroSequence.replace(".","");
                    lChroSequence = lChroSequence.replace("-","");
                    lPeptideInfo.put("seq", lChroSequence);
                    lPeptideInfo.put("spC",new String(line.substring(line.indexOf("spC=") + 5, line.indexOf("\"", line.indexOf("spC=") + 5 ))));
                    lPeptideInfo.put("xcorr",new String(line.substring(line.indexOf("xcorr=") + 7, line.indexOf("\"", line.indexOf("xcorr=") + 7 ))));
                    lPeptideInfo.put("charge",new String(line.substring(line.indexOf("charge=") + 8, line.indexOf("\"", line.indexOf("charge=") + 8 ))));
                    lPeptideInfo.put("deltaCN",new String(line.substring(line.indexOf("deltaCN=") + 9, line.indexOf("\"", line.indexOf("deltaCN=") + 9 ))));
                    lPeptideInfo.put("enrichment",new String(line.substring(line.indexOf("enrichment=") + 12, line.indexOf("\"", line.indexOf("enrichment=") + 12 ))));
                    lPeptideInfo.put("lightStartMass",new String(line.substring(line.indexOf("lightStartMass=") + 16, line.indexOf("\"", line.indexOf("lightStartMass=") + 16 ))));
                    lPeptideInfo.put("heavyStartMass",new String(line.substring(line.indexOf("heavyStartMass=") + 16, line.indexOf("\"", line.indexOf("heavyStartMass=") + 16 ))));
                    lPeptideInfo.put("lightAvgMass",new String(line.substring(line.indexOf("lightAvgMass=") + 14, line.indexOf("\"", line.indexOf("lightAvgMass=") + 14 ))));
                    lPeptideInfo.put("heavyAvgMass",new String(line.substring(line.indexOf("heavyAvgMass=") + 14, line.indexOf("\"", line.indexOf("heavyAvgMass=") + 14 ))));
                    lPeptideInfo.put("start_scan",new String(line.substring(line.indexOf("start_scan=") + 12, line.indexOf("\"", line.indexOf("start_scan=") + 12 ))));
                    lPeptideInfo.put("end_scan",new String(line.substring(line.indexOf("end_scan=") + 10, line.indexOf("\"", line.indexOf("end_scan=") + 10 ))));
                    lPeptideInfo.put("accession", lAccession);
                } else if(line.trim().startsWith("<chro")){
                    //it's a chro
                    lPeptideInfo.put("chro",new String(line.substring(line.indexOf("<chro>") + 6, line.indexOf("</chro"))));
                    //we have all the info
                    //add it to the vector
                    iPeptideInfos.add(lPeptideInfo);
                } else if(line.trim().startsWith("<protein")){
                    lAccession =new String(line.substring(line.indexOf("locus=") + 7, line.indexOf("\"", line.indexOf("locus=") + 7 )));
                }
            }

         } catch (FileNotFoundException e) {
            iFlamable.passHotPotato(new Throwable("Problem reading the census chro file"));
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            iFlamable.passHotPotato(new Throwable("Problem reading the census chro file"));
            logger.error(e.getMessage(), e);
        } catch (Exception e){
            iFlamable.passHotPotato(new Throwable("Problem reading the census chro file"));
            logger.error(e.getMessage(), e);
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
