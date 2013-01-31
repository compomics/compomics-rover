package com.compomics.rover.general.fileio.readers;

import com.compomics.rover.general.PeptideIdentification.DefaultPeptideIdentification;
import com.compomics.rover.general.enumeration.DataType;
import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.interfaces.MsfFileInterface;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.RatioType;
import com.compomics.rover.general.quantitation.source.thermo_msf.ThermoMsfRatio;
import com.compomics.thermo_msf_parser.Parser;
import com.compomics.thermo_msf_parser.gui.MsfFile;
import com.compomics.thermo_msf_parser.msf.Peptide;
import com.compomics.thermo_msf_parser.msf.PeptideLowMem;
import com.compomics.thermo_msf_parser.msf.Protein;
import com.compomics.thermo_msf_parser.msf.ProteinLowMem;
import com.compomics.thermo_msf_parser.msf.ProteinLowMemController;
import com.compomics.thermo_msf_parser.msf.RatioTypeLowMem;
import com.compomics.thermo_msf_parser.msf.RatioTypeLowMemController;
import com.compomics.thermo_msf_parser.msf.RawFileLowMemController;
import com.compomics.thermo_msf_parser.msf.ScoreType;
import com.compomics.thermo_msf_parser.msf.ScoreTypeLowMem;
import com.compomics.thermo_msf_parser.msf.ScoreTypeLowMemController;
import com.compomics.thermo_msf_parser.msf.Spectrum;
import com.compomics.thermo_msf_parser.msf.SpectrumLowMem;
import com.compomics.thermo_msf_parser.msf.SpectrumLowMemController;


import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

    

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 17-Feb-2011
 * Time: 10:12:44
 */
public class MsfReader {

    private String iMsfFileLocation;
    private MsfFile iMsfFile = null;
    private Parser iParsedMsfFile = null;
    private RatioGroupCollection iRatioGroupCollection;
    private int iConfidenceLevel;
    private boolean iOnlyHighestScoring;
    private boolean iOnlyLowestScoring;
    private MsfFileInterface msfFileOfInterest;
    
    //TODO: find out if inner classes can implement interfaces. if so write interface and make msfFileOfInterest a field that is an implementation of the interface

    public MsfReader(String lMsfFileLocation, int lConfidenceLevel, boolean lOnlyHighestScoring, boolean lOnlyLowestScoring) throws ClassNotFoundException, SQLException, IOException {
        this.iMsfFileLocation = lMsfFileLocation;
        this.iConfidenceLevel = lConfidenceLevel;
        this.iOnlyHighestScoring = lOnlyHighestScoring;
        this.iOnlyLowestScoring = lOnlyLowestScoring;
        this.iMsfFile = new MsfFile(new File(iMsfFileLocation));
        iRatioGroupCollection = null;
        msfFileWithoutParser msfFileOfInterest= new msfFileWithoutParser(); 
    }

    public MsfReader(Parser lParsedMsfFile, String lMsfFileLocation, int lConfidenceLevel, boolean lOnlyHighestScoring, boolean lOnlyLowestScoring) throws ClassNotFoundException, SQLException, IOException {
        this.iMsfFileLocation = lMsfFileLocation;
        this.iConfidenceLevel = lConfidenceLevel;
        this.iOnlyHighestScoring = lOnlyHighestScoring;
        this.iOnlyLowestScoring = lOnlyLowestScoring;
        this.iParsedMsfFile = lParsedMsfFile;
        iRatioGroupCollection = null;
        this.msfFileOfInterest = new msfFileWithParser();
    }

    //wut dis do?
    public MsfReader(int lConfidenceLevel, boolean lOnlyHighestScoring, boolean lOnlyLowestScoring){
        this.iConfidenceLevel = lConfidenceLevel;
        this.iOnlyHighestScoring = lOnlyHighestScoring;
        this.iOnlyLowestScoring = lOnlyLowestScoring;
    }

    public RatioGroupCollection getRatioGroupCollection(){
        return msfFileOfInterest.getRatioGroupCollection();
    }
    
    public RatioGroupCollection getRatioGroupCollection(Vector<String> aComponentsVector,Vector<Integer> aChannelIdsVector,Vector ratioTypes,Vector spectraVector) {
        return msfFileOfInterest.getRatioGroupCollection(aComponentsVector,aChannelIdsVector,ratioTypes,spectraVector);
    }
    
    
    public static void main(String[] args) throws Exception {
        new MsfReader("C:\\niklaas\\data\\02_11\\proteomdiscoverer\\V192_RT1_10_training_TOP10_4ppm.msf", 3, true, false);
}
    
   //check if inner classes can add additional imports
    private class msfFileWithoutParser implements MsfFileInterface{
        
        private ScoreTypeLowMemController scoreTypeInstance = new ScoreTypeLowMemController(); 
        private RatioTypeLowMemController ratioTypeLowMemInstance = new RatioTypeLowMemController();
        private SpectrumLowMemController spectrumLowMemInstance = new SpectrumLowMemController();
        private ProteinLowMemController proteinLowMemInstance = new ProteinLowMemController();
        private RawFileLowMemController rawFileLowMemInstance = new RawFileLowMemController();
        
        public RatioGroupCollection getRatioGroupCollection() {
            if(iRatioGroupCollection == null){
                    Vector<String> lComp = new Vector<String>();
                    Vector<Integer> lChannelIds = new Vector<Integer>();
                    Vector<String> lType = new Vector<String>();
                    Vector<RatioTypeLowMem> lRatioTypes = ratioTypeLowMemInstance.parseRatioTypes(iMsfFile.getConnection());
                    iRatioGroupCollection = new RatioGroupCollection(DataType.PROTEOME_DISCOVERER);
                    Vector<ScoreTypeLowMem> lScores = scoreTypeInstance.getScoreTypes(iMsfFile.getConnection());
                    for (RatioTypeLowMem aRatioType : lRatioTypes){
                        lComp.addAll(aRatioType.getComponents());
                        lChannelIds.addAll(aRatioType.getChannelIds());
                    }
                    iRatioGroupCollection.setComponentTypes(lComp);
                    iRatioGroupCollection.setRatioTypes(lType);

                    //set the filename
                    String lFileName = (new File(iMsfFileLocation)).getName();
                    iRatioGroupCollection.putMetaData(QuantitationMetaType.FILENAME, lFileName);

                    Vector<ScoreTypeLowMem> lScoreTypes = new Vector<ScoreTypeLowMem>();
                    for (int i = 0; i < lScores.size(); i++) {
                        if (lScores.get(i).getIsMainScore() == 1) {
                            lScoreTypes.add(lScores.get(i));
                        }
                    }
                   
                    Vector<SpectrumLowMem> spectra = spectrumLowMemInstance.getAllSpectra(iMsfFile.getConnection());
                    //create for every peptide a ratiogroup and add it to the ratiogroup collection
                    for (int i = 0; i < spectra.size(); i++) {

                        for (int p = 0; p < spectra.get(i).getPeptides().size(); p++) {

                            //check if spectrum has good peptide identification
                            PeptideLowMem lPeptide = spectra.get(i).getPeptides().get(p);
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
                                if (lPeptide != null && spectra.get(i).getQuanResult() != null && lPeptide.getProteins().size() > 0) {
                                    for (int t = 0; t < lRatioTypes.size(); t++) {
                                        if (spectra.get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) != null) {
                                            //this peptide has a ratio
                                            lUse = true;
                                        }
                                    }
                                }

                                if (lUse) {

                                    //create the ratiogroup
                                    RatioGroup lRatioGroup = new RatioGroup(iRatioGroupCollection);
                                    for (int t = 0; t < lRatioTypes.size(); t++) {
                                        if (spectra.get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) != null) {
                                            Double lCalRatio = (Math.round(spectra.get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) * 1000.0)) / 1000.0;
                                            ThermoMsfRatio lRatio = new ThermoMsfRatio(lCalRatio, lType.get(t), true, lRatioGroup, spectra.get(i).getQuanResult(), spectra.get(i).getConnection(), spectra.get(i).getFileId(), lComp, lChannelIds);
                                            lRatio.setNumeratorIntensity(spectra.get(i).getQuanResult().getNumeratorByRatioType(lRatioTypes.get(t)));
                                            lRatio.setDenominatorIntensity(spectra.get(i).getQuanResult().getDenominatorByRatioType(lRatioTypes.get(t)));
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
                                    lid.setPrecursor(spectra.get(i).getMz());
                                    lid.setCharge(Integer.valueOf(spectra.get(i).getCharge()));
                                    lid.setSpectrumFileName(rawFileLowMemInstance.getRawFileNameForFileID(spectra.get(i).getFileId(), iMsfFile.getConnection()));
                                    Vector<ProteinLowMem> lProteins = lPeptide.getProteins();

                                    String lIsoforms = "";
                                    if (lProteins.size() > 1) {
                                        for (int j = 1; j < lProteins.size(); j++) {
                                            lIsoforms = lIsoforms + proteinLowMemInstance.getUtilProteinForProteinID(lProteins.get(j).getProteinID(), iMsfFile.getConnection()).getHeader().getAccession()+ " ,";
                                        }
                                    }
                                    lid.setIsoforms(lIsoforms);
                                    if(proteinLowMemInstance.getAccessionFromProteinID(lProteins.get(0).getProteinID(),iMsfFile.getConnection()) == null){
                                        lid.setAccession(proteinLowMemInstance.getUtilProteinForProteinID(lProteins.get(0).getProteinID(), iMsfFile.getConnection()).getHeader().getDescription());
                                    } else {
                                        lid.setAccession(proteinLowMemInstance.getAccessionFromProteinID(lProteins.get(0).getProteinID(), iMsfFile.getConnection()));
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

    public RatioGroupCollection getRatioGroupCollection(Vector<String> aComponentsVector,Vector<Integer> aChannelIdsVector,Vector ratioTypes,Vector spectraVector) {
        if(iRatioGroupCollection == null){
                iRatioGroupCollection = new RatioGroupCollection(DataType.PROTEOME_DISCOVERER);
                Vector<ScoreTypeLowMem> lScores = scoreTypeInstance.getScoreTypes(iMsfFile.getConnection());    
                Vector<RatioTypeLowMem> ratiotypes = ratioTypes; 
                Vector<String> lComp = aComponentsVector;
                Vector<String> lType = new Vector<String>();
                for (int i = 0; i < ratioTypes.size(); i++) {
                    lType.add(ratiotypes.get(i).getRatioType());
                }
                iRatioGroupCollection.setComponentTypes(lComp);
                iRatioGroupCollection.setRatioTypes(lType);

                //set the filename
                String lFileName = (new File(iMsfFileLocation)).getName();
                iRatioGroupCollection.putMetaData(QuantitationMetaType.FILENAME, lFileName);
                Vector<ScoreTypeLowMem> lScoreTypes = new Vector<ScoreTypeLowMem>();
                for (int i = 0; i < lScores.size(); i++) {
                    if (lScores.get(i).getIsMainScore() == 1) {
                        lScoreTypes.add(lScores.get(i));
                    }
                }

                Vector<RatioTypeLowMem> lRatioTypes = ratioTypes;
                Vector<SpectrumLowMem> lSpectrumVector = spectraVector;
                //create for every peptide a ratiogroup and add it to the ratiogroup collection
                for (int i = 0; i < lSpectrumVector.size(); i++) {

                    for (int p = 0; p < lSpectrumVector.get(i).getPeptides().size(); p++) {

                        //check if spectrum has good peptide identification
                        PeptideLowMem lPeptide = lSpectrumVector.get(i).getPeptides().get(p);
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
                            if (lPeptide != null && lSpectrumVector.get(i).getQuanResult() != null && lPeptide.getProteins().size() > 0) {
                                for (int t = 0; t < lRatioTypes.size(); t++) {
                                    if (lSpectrumVector.get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) != null) {
                                        //this peptide has a ratio
                                        lUse = true;
                                    }
                                }


                            }

                            if (lUse) {

                                //create the ratiogroup
                                RatioGroup lRatioGroup = new RatioGroup(iRatioGroupCollection);
                                for (int t = 0; t < lRatioTypes.size(); t++) {
                                    if (lSpectrumVector.get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) != null) {
                                        double lCalRatio = (Math.round(lSpectrumVector.get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) * 1000.0)) / 1000.0;
                                        ThermoMsfRatio lRatio = new ThermoMsfRatio(lCalRatio, lType.get(t), true, lRatioGroup, lSpectrumVector.get(i).getQuanResult(), lSpectrumVector.get(i).getConnection(), lSpectrumVector.get(i).getFileId(),lComp, aChannelIdsVector);
                                        lRatio.setNumeratorIntensity(lSpectrumVector.get(i).getQuanResult().getNumeratorByRatioType(lRatioTypes.get(t)));
                                        lRatio.setDenominatorIntensity(lSpectrumVector.get(i).getQuanResult().getDenominatorByRatioType(lRatioTypes.get(t)));
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
                                lid.setPrecursor(lSpectrumVector.get(i).getMz());
                                lid.setCharge(Integer.valueOf(lSpectrumVector.get(i).getCharge()));
                                lid.setSpectrumFileName(rawFileLowMemInstance.getRawFileNameForFileID(lSpectrumVector.get(i).getFileId(), iMsfFile.getConnection()));
                                Vector<ProteinLowMem> lProteins = lPeptide.getProteins();

                                String lIsoforms = "";
                                if (lProteins.size() > 1) {
                                    for (int j = 1; j < lProteins.size(); j++) {
                                        lIsoforms = lIsoforms + proteinLowMemInstance.getUtilProteinForProteinID(lProteins.get(j).getProteinID(),iMsfFile.getConnection()).getHeader().getAccession() + " ,";
                                    }
                                }
                                lid.setIsoforms(lIsoforms);
                                if(proteinLowMemInstance.getUtilProteinForProteinID(lProteins.get(0).getProteinID(),iMsfFile.getConnection()).getHeader().getAccession() == null){
                                    lid.setAccession(proteinLowMemInstance.getUtilProteinForProteinID(lProteins.get(0).getProteinID(),iMsfFile.getConnection()).getHeader().getDescription());
                                } else {
                                    lid.setAccession(proteinLowMemInstance.getUtilProteinForProteinID(lProteins.get(0).getProteinID(),iMsfFile.getConnection()).getHeader().getAccession());
                                }


                                if (lPeptide.getChannelId() != 0) {
                                    for (int v = 0; v < lComp.size(); v++) {
                                        if (lPeptide.getChannelId() == aChannelIdsVector.get(v)) {
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
        
     
    }
    
 private class msfFileWithParser implements MsfFileInterface {
 
     
        public RatioGroupCollection getRatioGroupCollection() {
        if(iRatioGroupCollection == null){

            iRatioGroupCollection = new RatioGroupCollection(DataType.PROTEOME_DISCOVERER);

            Vector<String> lComp = iParsedMsfFile.getComponents();
            Vector<Integer> lChannelIds = iParsedMsfFile.getChannelIds();
            Vector<String> lType = new Vector<String>();
            for (int i = 0; i < iParsedMsfFile.getRatioTypes().size(); i++) {
                lType.add(iParsedMsfFile.getRatioTypes().get(i).getRatioType());
            }
            iRatioGroupCollection.setComponentTypes(lComp);
            iRatioGroupCollection.setRatioTypes(lType);

            //set the filename
            String lFileName = (new File(iMsfFileLocation)).getName();
            iRatioGroupCollection.putMetaData(QuantitationMetaType.FILENAME, lFileName);

            Vector<ScoreType> lScores = iParsedMsfFile.getScoreTypes();
            Vector<ScoreType> lScoreTypes = new Vector<ScoreType>();
            for (int i = 0; i < lScores.size(); i++) {
                if (lScores.get(i).getIsMainScore() == 1) {
                    lScoreTypes.add(lScores.get(i));
                }
            }

            Vector<com.compomics.thermo_msf_parser.msf.RatioType> lRatioTypes = iParsedMsfFile.getRatioTypes();

            //create for every peptide a ratiogroup and add it to the ratiogroup collection
            for (int i = 0; i < iParsedMsfFile.getSpectra().size(); i++) {

                for (int p = 0; p < iParsedMsfFile.getSpectra().get(i).getPeptides().size(); p++) {

                    //check if spectrum has good peptide identification
                    Peptide lPeptide = iParsedMsfFile.getSpectra().get(i).getPeptides().get(p);
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
                        if (lPeptide != null && iParsedMsfFile.getSpectra().get(i).getQuanResult() != null && lPeptide.getProteins().size() > 0) {
                            for (int t = 0; t < lRatioTypes.size(); t++) {
                                if (iParsedMsfFile.getSpectra().get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) != null) {
                                    //this peptide has a ratio
                                    lUse = true;
                                }
                            }


                        }

                        if (lUse) {

                            //create the ratiogroup
                            RatioGroup lRatioGroup = new RatioGroup(iRatioGroupCollection);
                            for (int t = 0; t < lRatioTypes.size(); t++) {
                                if (iParsedMsfFile.getSpectra().get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) != null) {
                                    Double lCalRatio = (Math.round(iParsedMsfFile.getSpectra().get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) * 1000.0)) / 1000.0;
                                    ThermoMsfRatio lRatio = new ThermoMsfRatio(lCalRatio, lType.get(t), true, lRatioGroup, iParsedMsfFile.getSpectra().get(i).getQuanResult(), iParsedMsfFile.getSpectra().get(i).getConnection(), iParsedMsfFile.getSpectra().get(i).getFileId(), iParsedMsfFile.getSpectra().get(i).getParser().getComponents(), iParsedMsfFile.getSpectra().get(i).getParser().getChannelIds());
                                    lRatio.setNumeratorIntensity(iParsedMsfFile.getSpectra().get(i).getQuanResult().getNumeratorByRatioType(lRatioTypes.get(t)));
                                    lRatio.setDenominatorIntensity(iParsedMsfFile.getSpectra().get(i).getQuanResult().getDenominatorByRatioType(lRatioTypes.get(t)));
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
                            lid.setPrecursor(iParsedMsfFile.getSpectra().get(i).getMz());
                            lid.setCharge(Integer.valueOf(iParsedMsfFile.getSpectra().get(i).getCharge()));
                            lid.setSpectrumFileName(iParsedMsfFile.getSpectra().get(i).getSpectrumTitle());
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

    public RatioGroupCollection getRatioGroupCollection(Vector<String> aComponentsVector,Vector<Integer> aChannelIdsVector,Vector ratioTypes,Vector spectraVector) {
        if(iRatioGroupCollection == null){

            iRatioGroupCollection = new RatioGroupCollection(DataType.PROTEOME_DISCOVERER);
            Vector<RatioType> ratiotypes = ratioTypes;
            Vector<String> lComp = aComponentsVector;
            Vector<String> lType = new Vector<String>();
            for (int i = 0; i < ratioTypes.size(); i++) {
                lType.add(ratiotypes.get(i).getType());
            }
            iRatioGroupCollection.setComponentTypes(lComp);
            iRatioGroupCollection.setRatioTypes(lType);

            //set the filename
            String lFileName = (new File(iMsfFileLocation)).getName();
            iRatioGroupCollection.putMetaData(QuantitationMetaType.FILENAME, lFileName);

            Vector<ScoreType> lScores = iParsedMsfFile.getScoreTypes();
            Vector<ScoreType> lScoreTypes = new Vector<ScoreType>();
            for (int i = 0; i < lScores.size(); i++) {
                if (lScores.get(i).getIsMainScore() == 1) {
                    lScoreTypes.add(lScores.get(i));
                }
            }

            Vector<com.compomics.thermo_msf_parser.msf.RatioType> lRatioTypes = ratioTypes;
            Vector<Spectrum> lSpectrumVector = spectraVector;
            //create for every peptide a ratiogroup and add it to the ratiogroup collection
            for (int i = 0; i < lSpectrumVector.size(); i++) {

                for (int p = 0; p < lSpectrumVector.get(i).getPeptides().size(); p++) {

                    //check if spectrum has good peptide identification
                    Peptide lPeptide = lSpectrumVector.get(i).getPeptides().get(p);
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
                        if (lPeptide != null && lSpectrumVector.get(i).getQuanResult() != null && lPeptide.getProteins().size() > 0) {
                            for (int t = 0; t < lRatioTypes.size(); t++) {
                                if (lSpectrumVector.get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) != null) {
                                    //this peptide has a ratio
                                    lUse = true;
                                }
                            }
                        }

                        if (lUse) {

                            //create the ratiogroup
                            RatioGroup lRatioGroup = new RatioGroup(iRatioGroupCollection);
                            for (int t = 0; t < lRatioTypes.size(); t++) {
                                if (lSpectrumVector.get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) != null) {
                                    double lCalRatio = (Math.round(lSpectrumVector.get(i).getQuanResult().getRatioByRatioType(lRatioTypes.get(t)) * 1000.0)) / 1000.0;
                                    ThermoMsfRatio lRatio = new ThermoMsfRatio(lCalRatio, lType.get(t), true, lRatioGroup, lSpectrumVector.get(i).getQuanResult(), lSpectrumVector.get(i).getConnection(), lSpectrumVector.get(i).getFileId(),lComp, aChannelIdsVector);
                                    lRatio.setNumeratorIntensity(lSpectrumVector.get(i).getQuanResult().getNumeratorByRatioType(lRatioTypes.get(t)));
                                    lRatio.setDenominatorIntensity(lSpectrumVector.get(i).getQuanResult().getDenominatorByRatioType(lRatioTypes.get(t)));
                                    lRatio.setValid(true);
                                    lRatioGroup.addRatio(lRatio);
                                }
                            }

                            //create an identification and add it to the ratio
                            String lScore = String.valueOf(lPeptide.getMainScore());
                            if (lScore.indexOf(".") > 0) {
                                lScore = lScore.substring(0, lScore.indexOf("."));
                            }
                            DefaultPeptideIdentification lid = new DefaultPeptideIdentification();

                            lid.setScore(Long.valueOf(lScore));
                            lid.setModified_sequence(lPeptide.getModifiedPeptide());
                            lid.setSequence(lPeptide.getSequence());
                            lid.setValid(new Integer(1).intValue());
                            lid.setPrecursor(lSpectrumVector.get(i).getMz());
                            lid.setCharge(Integer.valueOf(lSpectrumVector.get(i).getCharge()));
                            lid.setSpectrumFileName(lSpectrumVector.get(i).getSpectrumTitle());
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
                                    if (lPeptide.getChannelId() == aChannelIdsVector.get(v)) {
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
        
    }
}
