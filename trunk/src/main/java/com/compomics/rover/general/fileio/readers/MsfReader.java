package com.compomics.rover.general.fileio.readers;

import com.compomics.rover.general.PeptideIdentification.DefaultPeptideIdentification;
import com.compomics.rover.general.enumeration.DataType;
import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.source.thermo_msf.ThermoMsfRatio;
import com.compomics.thermo_msf_parser.Parser;
import com.compomics.thermo_msf_parser.msf.Peptide;
import com.compomics.thermo_msf_parser.msf.Protein;
import com.compomics.thermo_msf_parser.msf.RatioType;
import com.compomics.thermo_msf_parser.msf.ScoreType;


import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 17-Feb-2011
 * Time: 10:12:44
 */
public class MsfReader {

    private String iMsfFileLocation;
    private Parser iMsfFile;
    private RatioGroupCollection iRatioGroupCollection;
    private int iConfidenceLevel;
    private boolean iOnlyHighestScoring;
    private boolean iOnlyLowestScoring;


    public MsfReader(String lMsfFileLocation, int lConfidenceLevel, boolean lOnlyHighestScoring, boolean lOnlyLowestScoring) throws ClassNotFoundException, SQLException, IOException {
        this.iMsfFileLocation = lMsfFileLocation;
        this.iConfidenceLevel = lConfidenceLevel;
        this.iOnlyHighestScoring = lOnlyHighestScoring;
        this.iOnlyLowestScoring = lOnlyLowestScoring;
        this.iMsfFile = new Parser(iMsfFileLocation, true);
        iRatioGroupCollection = null;
    }

    public MsfReader(Parser lParsedMsfFile, String lMsfFileLocation, int lConfidenceLevel, boolean lOnlyHighestScoring, boolean lOnlyLowestScoring) throws ClassNotFoundException, SQLException, IOException {
        this.iMsfFileLocation = lMsfFileLocation;
        this.iConfidenceLevel = lConfidenceLevel;
        this.iOnlyHighestScoring = lOnlyHighestScoring;
        this.iOnlyLowestScoring = lOnlyLowestScoring;
        this.iMsfFile = lParsedMsfFile;
        iRatioGroupCollection = null;
    }

    /**
     * Getter for the RatioGroupCollection
     *
     * @return RatioGroupCollection
     */
    public RatioGroupCollection getRatioGroupCollection() {
        if(iRatioGroupCollection == null){

            iRatioGroupCollection = new RatioGroupCollection(DataType.PROTEOME_DISCOVERER);

            Vector<String> lComp = iMsfFile.getComponents();
            Vector<Integer> lChannelIds = iMsfFile.getChannelIds();
            Vector<String> lType = new Vector<String>();
            for (int i = 0; i < iMsfFile.getRatioTypes().size(); i++) {
                lType.add(iMsfFile.getRatioTypes().get(i).getRatioType());
            }
            iRatioGroupCollection.setComponentTypes(lComp);
            iRatioGroupCollection.setRatioTypes(lType);

            //set the filename
            String lFileName = (new File(iMsfFileLocation)).getName();
            iRatioGroupCollection.putMetaData(QuantitationMetaType.FILENAME, lFileName);

            Vector<ScoreType> lScores = iMsfFile.getScoreTypes();
            Vector<ScoreType> lScoreTypes = new Vector<ScoreType>();
            for (int i = 0; i < lScores.size(); i++) {
                if (lScores.get(i).getIsMainScore() == 1) {
                    lScoreTypes.add(lScores.get(i));
                }
            }

            Vector<RatioType> lRatioTypes = iMsfFile.getRatioTypes();

            //create for every peptide a ratiogroup and add it to the ratiogroup collection
            for (int i = 0; i < iMsfFile.getSpectra().size(); i++) {

                for (int p = 0; p < iMsfFile.getSpectra().get(i).getPeptides().size(); p++) {

                    //check if spectrum has good peptide identification
                    Peptide lPeptide = iMsfFile.getSpectra().get(i).getPeptides().get(p);
                    boolean lHighestOrLowestScoring = false;
                    if(iOnlyHighestScoring){
                        if (lPeptide.getParentSpectrum().isHighestScoring(lPeptide, lScoreTypes)){
                            lHighestOrLowestScoring = true;
                        }
                    } else if(iOnlyLowestScoring){
                        if (lPeptide.getParentSpectrum().isLowestScoring(lPeptide, lScoreTypes)){
                            lHighestOrLowestScoring = true;
                        }
                    } else {
                        lHighestOrLowestScoring = true;
                    }


                    if (lHighestOrLowestScoring && lPeptide.getConfidenceLevel() >= iConfidenceLevel) {

                        boolean lUse = false;
                        if (lPeptide != null && iMsfFile.getSpectra().get(i).getQuanResult() != null && lPeptide.getProteins().size() > 0) {
                            for (int t = 0; t < lRatioTypes.size(); t++) {
                                if (iMsfFile.getSpectra().get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) != null) {
                                    //this peptide has a ratio
                                    lUse = true;
                                }
                            }


                        }

                        if (lUse) {

                            //create the ratiogroup
                            RatioGroup lRatioGroup = new RatioGroup(iRatioGroupCollection);
                            for (int t = 0; t < lRatioTypes.size(); t++) {
                                if (iMsfFile.getSpectra().get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) != null) {
                                    double lCalRatio = (Math.round(iMsfFile.getSpectra().get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) * 1000.0)) / 1000.0;
                                    ThermoMsfRatio lRatio = new ThermoMsfRatio(lCalRatio, lType.get(t), true, lRatioGroup, iMsfFile.getSpectra().get(i).getQuanResult(), iMsfFile.getSpectra().get(i).getConnection(), iMsfFile.getSpectra().get(i).getFileId(), iMsfFile.getSpectra().get(i).getParser().getComponents(), iMsfFile.getSpectra().get(i).getParser().getChannelIds());
                                    lRatio.setNumeratorIntensity(iMsfFile.getSpectra().get(i).getQuanResult().getNumeratorByRatioType(lRatioTypes.get(t)));
                                    lRatio.setDenominatorIntensity(iMsfFile.getSpectra().get(i).getQuanResult().getDenominatorByRatioType(lRatioTypes.get(t)));
                                    lRatio.setValid(true);
                                    lRatioGroup.addRatio(lRatio);
                                }
                            }

                            //create an identification and add it to the ratio
                            HashMap lIdentificationParameters = new HashMap();
                            String lScore = String.valueOf(lPeptide.getMainScore());
                            if (lScore.indexOf(".") > 0) {
                                lScore = lScore.substring(0, lScore.indexOf("."));
                            }
                            DefaultPeptideIdentification lid = new DefaultPeptideIdentification();

                            lid.setScore(Long.valueOf(lScore));
                            lid.setModified_sequence(lPeptide.getModifiedPeptide());
                            lid.setSequence(lPeptide.getSequence());
                            lid.setValid(new Integer(1).intValue());
                            lid.setPrecursor(iMsfFile.getSpectra().get(i).getMz());
                            lid.setCharge(Integer.valueOf(iMsfFile.getSpectra().get(i).getCharge()));
                            lid.setSpectrumFileName(iMsfFile.getSpectra().get(i).getSpectrumTitle());
                            Vector<Protein> lProteins = lPeptide.getProteins();

                            String lIsoforms = "";
                            if (lProteins.size() > 1) {
                                for (int j = 1; j < lProteins.size(); j++) {
                                    lIsoforms = lIsoforms + lProteins.get(j).getUtilAccession() + " ,";
                                }
                            }
                            lid.setIsoforms(lIsoforms);
                            if(lProteins.get(0).getUtilAccession() == null){
                                lid.setAccession(lProteins.get(0).getDescription());
                            } else {
                                lid.setAccession(lProteins.get(0).getUtilAccession());
                            }


                            if (lPeptide.getChannelId() != 0) {
                                for (int v = 0; v < lComp.size(); v++) {
                                    if (lPeptide.getChannelId() == lChannelIds.get(v)) {
                                        lRatioGroup.addIdentification(lid, lComp.get(v));
                                        lRatioGroup.setPeptideSequence(lid.getSequence());
                                    }
                                }
                            } else {
                                //System.out.println(lPeptide.getScoreByScoreType(lScoreTypes));
                            }
                            iRatioGroupCollection.add(lRatioGroup);
                        }
                    }
                }

            }
        }

        return iRatioGroupCollection;
    }



    public static void main(String[] args) throws Exception {
        new MsfReader("C:\\niklaas\\data\\02_11\\proteomdiscoverer\\V192_RT1_10_training_TOP10_4ppm.msf", 3, true, false);
    }
}
