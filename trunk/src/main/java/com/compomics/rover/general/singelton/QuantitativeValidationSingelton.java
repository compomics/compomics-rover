/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 5-jan-2009
 * Time: 7:31:33
 */
package com.compomics.rover.general.singelton;

import com.compomics.rover.general.quantitation.QuantitativeProtein;
import com.compomics.rover.general.quantitation.RatioType;
import com.compomics.rover.general.quantitation.ReferenceSet;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatio;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.enumeration.MaxQuantScoreType;

import java.util.Vector;
import java.util.HashMap;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math.special.Erf;
import org.apache.commons.math.MathException;
import sun.net.www.protocol.http.AuthCache;


/**
 * This singelton holds some information that is used the QuantitationValidationGui, ProteinBarCodePanel, QuantiativeProtein ... classes
 */
public class QuantitativeValidationSingelton {
    /**
     * Singelton instance
     */
    private static QuantitativeValidationSingelton ourInstance = new QuantitativeValidationSingelton();
    /**
     * Boolean that says if non valid ratios should be used in the calculation of the protein mean
     */
    private boolean iUseOnlyValidRatioForProteinMean = false;
     /**
     * Boolean that says if only uniquely identified peptide ratios should be used in the calculation of the protein mean
     */
    private boolean iUseOnlyUniqueRatioForProteinMean = false;
    private boolean iUseOriginalRatio = false;
    /**
     * Boolean that says if a ratio or the log2 of that ratio should be given
     */
    private boolean iLog2 = false;
    /**
     * This vector with Ratio stores the validated ratios
     */
    private Vector<Ratio> iValidatedRatios = new Vector<Ratio>();
    /**
     * This vector holds the selected QuantiativeProtein
     */
    private Vector<QuantitativeProtein> iSelectedProteins = new Vector<QuantitativeProtein>();
        /**
     * This vector holds the commented QuantiativeProtein
     */
    private Vector<QuantitativeProtein> iCommentedProteins = new Vector<QuantitativeProtein>();
    /**
     * This vector holds the validated QuantiativeProtein
     */
    private Vector<QuantitativeProtein> iValidatedProteins = new Vector<QuantitativeProtein>();
    /**
     * This vector holds the validated QuantiativeProtein
     */
    private Vector<QuantitativeProtein> iAllProteins = new Vector<QuantitativeProtein>();
    /**
     * This is the database type (Uniprot, ipi, ncbi)
     */
    private ProteinDatabaseType iDatabaseType;
    /**
     * This boolean says if the ratios used in the reference set must be valid. If it's true, the ratios must be valid
     */
    private boolean iRatioValidInReferenceSet = false;
    /**
     * The right border of the graph. This is also the end for the colored gradient in the protein bar
     */
    private int iRightGraphBorder = 3;
    /**
     * The left border of the graph. This is also the start for the colored gradient in the protein bar
     */
    private int iLeftGraphBorder = 0;
    /**
     * The reference set
     */
    private ReferenceSet iReferenceSet;
    /**
     * The calibrated standard deviation for log2 scale ratios for 1/1
     * ratio mixtures on the mass spectrometer.
     */
    private double iCalibratedStdev = 0.238714;
    /**
     * This vector will hold protein accessions for the not used peptides in the calculation of the protein mean
     */
    private Vector<String> iNotUsedProteins = new Vector<String>();
    /**
     * This vector will hold peptide sequences. This ratios linked to the peptide sequences will not be used in the calcultation of the protein mean.
     * (The protein accesion are stored in the iNotUsedProteins vector)
     */
    private Vector<String> iNotUsedPeptides = new Vector<String>();
    /**
     * Boolean that indicates if we must use all the proteins in the reference set
     */
    private boolean iUseAllProteinsForReferenceSet;
    /**
     * The number of proteins that will be used for the creation of the reference set
     */
    private int iNumberOfProteinsInReferenceSet;
    /**
     * The rover sources
     */
    private Vector<RoverSource> iRoverSources = new Vector<RoverSource>();
    /**
     * The max quant score type
     */
    private MaxQuantScoreType iMaxQuantScoreType = MaxQuantScoreType.RATIO;
    /**
     * The location where the file chooser should open
     */
    private String iFileLocationOpener;
    private Vector<String> iRatioTypes;
    private Vector<String> iComponentsTypes;
    private Vector<RatioType> iMatchedRatioTypes = new Vector<RatioType>();
    private Vector<RoverSource> iOriginalRoverSources;
    private boolean iMultipleSources = false;
    private Vector<String> iTitlesForDifferentSources;
    private HashMap iSequenceMap = new HashMap();
    private Vector<RatioGroupCollection> iOriginalCollections;
    private Vector<String> iProteinAccessions;
    private Vector<Boolean> iSelectedIndexes;


    public static QuantitativeValidationSingelton getInstance() {
        return ourInstance;
    }

    private QuantitativeValidationSingelton() {
    }

    /**
     * Getter for useOnlyValidRatioForProteinMean
     * @return boolean
     */
    public boolean isUseOnlyValidRatioForProteinMean() {
        return iUseOnlyValidRatioForProteinMean;
    }

    /**
     * Getter for useOnlyUniqueRatioForProteinMean
     * @return boolean
     */
    public boolean isUseOnlyUniqueRatioForProteinMean() {
        return iUseOnlyUniqueRatioForProteinMean;
    }

    /**
     * Setter for useOnlyValidRatioForProteinMean
     * @param aUseOnlyValidRatioForProteinMean
     */
    public void setUseOnlyValidRatioForProteinMean(boolean aUseOnlyValidRatioForProteinMean) {
        this.iUseOnlyValidRatioForProteinMean = aUseOnlyValidRatioForProteinMean;
    }

        /**
     * Setter for useOnlyUniqueRatioForProteinMean
     * @param aUseOnlyUniqueRatioForProteinMean
     */
    public void setUseOnlyUniqueRatioForProteinMean(boolean aUseOnlyUniqueRatioForProteinMean) {
        this.iUseOnlyUniqueRatioForProteinMean = aUseOnlyUniqueRatioForProteinMean;
    }

    /**
     * Getter for log2
     * @return boolean
     */
    public boolean isLog2() {
        return iLog2;
    }

    /**
     * Setter for log2 boolean
     * @param aLog2
     */
    public void setLog2(boolean aLog2) {
        this.iLog2 = aLog2;
    }

    /**
     * Method that indicates if we are connected to ms_lims
     * @return boolean
     */
    public boolean isDatabaseMode() {

        boolean lDatabaseMode = false;
        for(int i = 0;i<iRoverSources.size(); i ++){
            RoverSource lRoverSource = iRoverSources.get(i);
            if(lRoverSource ==  RoverSource.ITRAQ_MS_LIMS || lRoverSource == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS){
                lDatabaseMode = true;
            }
        }
        return lDatabaseMode;
    }

    /**
     * This method will add a validate ratio
     * @param aRatio Ratio to add
     */
    public void addValidatedRatio(Ratio aRatio){
        if(!iValidatedRatios.contains(aRatio)){
            iValidatedRatios.add(aRatio);
        }
    }

    /**
     * Getter for the validated ratios
     * @return Vector<Ratio>
     */
    public Vector<Ratio> getValidatedRatios(){
        return iValidatedRatios;
    }

    /**
     * This method will add a selected protein
     * @param aProtein
     */
    public void addSelectedProtein(QuantitativeProtein aProtein){
        if(!iSelectedProteins.contains(aProtein)){
            iSelectedProteins.add(aProtein);
        }
    }

    /**
     * Getter for the selected proteins
     * @return Vector<QuantitativeProtein>
     */
    public Vector<QuantitativeProtein> getSelectedProteins(){
        return iSelectedProteins;
    }

    /**
     * This method will remove all the selected proteins
     */
    public void removeAllSelectedProteins(){
        iSelectedProteins.removeAllElements();
    }

    /**
     * This method will add a validated 
     * @param aProtein
     */
    public void addValidatedProtein(QuantitativeProtein aProtein){
        if(!iValidatedProteins.contains(aProtein)){
            iValidatedProteins.add(aProtein);
        }
    }

    /**
     * Getter for the validated proteins
     * @return Vector<QuantitativeProtein>
     */
    public Vector<QuantitativeProtein> getValidatedProteins(){
        return iValidatedProteins;
    }

    /**
     * Remove a protein from the validated proteins
     * @param aProtein The protein that will be removed from the validated vector
     */
    public void removeValidatedProtein(QuantitativeProtein aProtein){
        iValidatedProteins.remove(aProtein);
    }

    /**
     * Getter for the commented proteins
     * @return Vector<QuantitativeProtein>
     */
    public Vector<QuantitativeProtein> getAllProteins(){
        return iAllProteins;
    }

     /**
     * Setter for the commented proteins
     */
    public void setAllProteins(Vector<QuantitativeProtein> lAllProteins){
        iAllProteins = lAllProteins;
    }


    /**
     * Getter for the commented proteins
     * @return Vector<QuantitativeProtein>
     */
    public Vector<QuantitativeProtein> getCommentedProteins(){
        return iCommentedProteins;
    }

    /**
     * Remove a protein from the commented proteins
     * @param aProtein The protein that will be removed from the commented vector
     */
    public void removeCommentedProtein(QuantitativeProtein aProtein){
        iCommentedProteins.remove(aProtein);
    }

    /**
     * Add a commented protein
     * @param aProtein That will be added
     */
    public void addCommentedProtein(QuantitativeProtein aProtein){
        if(!iCommentedProteins.contains(aProtein)){
            iCommentedProteins.add(aProtein);
        }
    }

    /**
     * This method will save a .ROVER file
     * @param aFileLocation Location to save to
     * @throws IOException
     */
    public void saveRoverFile(String aFileLocation) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(aFileLocation));
        out.write("//protein accession, selected, validated\n");
        //write selected proteins
        for(int i = 0; i<iSelectedProteins.size(); i ++){
            String validated = "0";
            if(iSelectedProteins.get(i).getValidated()){
                validated = "1";
            }
            out.write(iSelectedProteins.get(i).getAccession() + ",1," +validated + "," + iSelectedProteins.get(i).getProteinComment() + "\n" );
        }
        //write validated proteins
        for(int i = 0; i<iValidatedProteins.size(); i ++){
            //check if it is not in the selected proteins
            boolean found = false;
            for(int j = 0; j<iSelectedProteins.size(); j ++){
                if(iSelectedProteins.get(j).getAccession().equalsIgnoreCase(iValidatedProteins.get(i).getAccession())){
                    found = true;
                }
            }
            if(!found){
                out.write(iValidatedProteins.get(i).getAccession() + ",0,1," + iSelectedProteins.get(i).getProteinComment() + "\n" );
            }
        }
        //write commented proteins
        for(int i = 0; i<iCommentedProteins.size(); i ++){
            //the validated and selected proteins are already written
            if(!iCommentedProteins.get(i).getSelected() && !iCommentedProteins.get(i).getValidated()){
                out.write(iCommentedProteins.get(i).getAccession() + ",0,0," + iCommentedProteins.get(i).getProteinComment() + "\n" );
            }
        }
        out.write("//ratio type, ratio, filename, hit, valid, comment\n");
        for(int i = 0; i<iValidatedRatios.size(); i ++){
            if(this.isDistillerQuantitation()){
                DistillerRatio lRatio = (DistillerRatio) iValidatedRatios.get(i);
                out.write(iValidatedRatios.get(i).getType() + "," + iValidatedRatios.get(i).getRatio(false) + ","  + lRatio.getParentRatioGroup().getParentCollection().getMetaData(QuantitationMetaType.FILENAME)+","+ lRatio.getParentRatioGroup().getParentHit().getDistillerHitNumber() + "," + lRatio.getValid() + "," + lRatio.getComment() +"\n");
            }

        }
        out.write("//protein, peptide\n");
        for(int i = 0; i<iNotUsedPeptides.size(); i ++){
            out.write(iNotUsedProteins.get(i) + "," + iNotUsedPeptides.get(i) +"\n");
        }
        out.close();
    }

    /**
     * Is the loaded data from Mascot Distiller
     * @return boolean that indicates the status
     */
    public boolean isDistillerQuantitation() {
        boolean lDistillerQuantitation = false;
        for(int i = 0;i<iRoverSources.size(); i ++){
            RoverSource lRoverSource = iRoverSources.get(i);
            if(lRoverSource == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || lRoverSource == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV ){
                lDistillerQuantitation = true;
            }
        }
        return lDistillerQuantitation;
    }

    /**
     * Is the loaded data from MaxQuant
     * @return boolean that indicates the status
     */
    public boolean isMaxQuantQuantitation() {
        boolean lMaxQuantQuantitation = false;
        for(int i = 0;i<iRoverSources.size(); i ++){
            RoverSource lRoverSource = iRoverSources.get(i);
            if(lRoverSource == RoverSource.MAX_QUANT || lRoverSource == RoverSource.MAX_QUANT_NO_SIGN){
                lMaxQuantQuantitation = true;
            }
        }
        return lMaxQuantQuantitation;
    }

    /**
     * Is the loaded data from MaxQuant without Sign A and Sign B
     * @return boolean that indicates the status
     */
    public boolean isMaxQuantQuantitationWithoutSign() {
        boolean lMaxQuantQuantitation = false;

        for(int i = 0;i<iRoverSources.size(); i ++){
            RoverSource lRoverSource = iRoverSources.get(i);
            if(lRoverSource == RoverSource.MAX_QUANT_NO_SIGN){
                lMaxQuantQuantitation = true;
            }
        }
        return lMaxQuantQuantitation;
    }

    /**
     * Getter for the database type
     * @return ProteinDatabaseType
     */
    public ProteinDatabaseType getDatabaseType() {
        return iDatabaseType;
    }

    /**
     * Setter for the database type
     * @param aDatabaseType The database type
     */
    public void setDatabaseType(ProteinDatabaseType aDatabaseType) {
        this.iDatabaseType = aDatabaseType;
    }

    /**
     * Getter for the boolean that indicates if the calculations in the reference set only takes valid ratios
     * @return boolean
     */
    public boolean isRatioValidInReferenceSet() {
        return iRatioValidInReferenceSet;
    }

    /**
     * Setter for the status RatioValidInReferenceSet
     * @param aRatioValidInReferenceSet
     */
    public void setRatioValidInReferenceSet(boolean aRatioValidInReferenceSet){
        iRatioValidInReferenceSet = aRatioValidInReferenceSet;
    }

    /**
     * Getter for the right graph border
     * @return int with the right border
     */
    public int getRightGraphBorder() {
        return iRightGraphBorder;
    }

    /**
     * Setter for the right graph border
     * @param iRightGraphBorder Int with the border value
     */
    public void setRightGraphBorder(int iRightGraphBorder) {
        this.iRightGraphBorder = iRightGraphBorder;
    }

    /**
     * Getter for the left  graph border
     * @return int with the left border
     */
    public int getLeftGraphBorder() {
        return iLeftGraphBorder;
    }


    /**
     * Setter for the left graph border
     * @param iLeftGraphBorder Int with the border value
     */public void setLeftGraphBorder(int iLeftGraphBorder) {
        this.iLeftGraphBorder = iLeftGraphBorder;
    }

    /**
     * Getter for the reference set
     * @return ReferenceSet
     */
    public ReferenceSet getReferenceSet() {
        return iReferenceSet;
    }

    /**
     * Setter for the reference set
     * @param aReferenceSet Reference set tot set
     */
    public void setReferenceSet(ReferenceSet aReferenceSet) {
        this.iReferenceSet = aReferenceSet;
    }

    /**
     * Getter for the calibrated stDev
     * @return double with the calibrated stDev
     */
    public double getCalibratedStdev() {
        return iCalibratedStdev;
    }

    /**
     * Setter for the calibrated stDev
     * @param aCalibratedStdev double with the stDev to set
     */
    public void setCalibratedStdev(double aCalibratedStdev) {
        this.iCalibratedStdev = aCalibratedStdev;
    }

    /**
     * This method will delete the protein and peptide from the not used in protein mean calculation vector.
     * @param accession String with the accession
     * @param peptideSequence String with the peptide sequence
     */
    public void deleteNotUsedPeptide(String accession, String peptideSequence) {
        for(int i = 0; i<iNotUsedProteins.size(); i++){
            if(iNotUsedProteins.get(i).equalsIgnoreCase(accession) && iNotUsedPeptides.get(i).equalsIgnoreCase(peptideSequence)){
                iNotUsedPeptides.remove(i);
                iNotUsedProteins.remove(i);
            }
        }
    }

    /**
     * This method will add a protein and peptide to the not used in protein mean calculation vector.
     * @param accession String with the accession
     * @param peptideSequence String with the peptide sequence
     */
    public void addNotUsedPeptide(String accession, String peptideSequence) {
        iNotUsedPeptides.add(peptideSequence);
        iNotUsedProteins.add(accession);
    }

    /**
     * Getter for the not used proteins
     * @return Vector<String> with protein accessions
     */
    public Vector<String> getNotUsedProteins(){
        return iNotUsedProteins;
    }

    /**
     * Getter for the not used proteins
     * @return Vector<String> with peptide sequences
     */
    public Vector<String> getNotUsedPeptides(){
        return iNotUsedPeptides;
    }

    /**
     * Setter for the boolean that indicates if all the proteins or just a subset of the proteins are used in the reference set
     * @param lUseAllProteins boolean to set
     */
    public void setUseAllProteinsForReferenceSet(boolean lUseAllProteins) {
        this.iUseAllProteinsForReferenceSet = lUseAllProteins;
    }

    /**
     * This method gives a boolean that indicates if all the proteins or just a subset of the proteins are used in the reference set
     * @return boolean
     */
    public boolean getUseAllProteinsForReferenceSet(){
        return iUseAllProteinsForReferenceSet;
    }

    /**
     * Getter for the number of proteins used in the reference set
     * @return int with the number of proteins used
     */
    public int getNumberOfProteinsInReferenceSet() {
        return iNumberOfProteinsInReferenceSet;
    }

    /**
     * Setter for the number of proteins used in the reference set
     * @param aNumberOfProteinsInReferenceSet int with the number to set
     */
    public void setNumberOfProteinsInReferenceSet(int aNumberOfProteinsInReferenceSet) {
        this.iNumberOfProteinsInReferenceSet = aNumberOfProteinsInReferenceSet;
    }


    /**
     * This method will give a boolean that indicates if we are working with iTRAQ data
     * @return boolean that indicates the status
     */
    public boolean isITraqData() {
        boolean lItraq = false;
        for(int i = 0;i<iRoverSources.size(); i ++){
            RoverSource lRoverSource = iRoverSources.get(i);
            if(lRoverSource == RoverSource.ITRAQ_DAT || lRoverSource == RoverSource.ITRAQ_MS_LIMS || lRoverSource == RoverSource.ITRAQ_ROV){
                lItraq = true;
            }
        }
        return lItraq;
    }

    /**
     * Setter for the RoverSource
     * @param aRoverSource The RoverSource to set
     */
    public void setRoverDataType(RoverSource aRoverSource) {
        this.iRoverSources.add(aRoverSource);
    }

    /**
     * Getter for the MaxQuant Score type
     * @return MaxQuantScoreType
     */
    public MaxQuantScoreType getMaxQuantScoreType() {
        return iMaxQuantScoreType;
    }

    /**
     * Setter for the MaxQuantScoreType
     * @param aMaxQuantScoreType MaxQuantScoreType to set
     */
    public void setMaxQuantScoreType(MaxQuantScoreType aMaxQuantScoreType) {
        this.iMaxQuantScoreType = aMaxQuantScoreType;
    }

    /**
     * This method calculates the p value for a Z-value
     * Example: From 1.96 (Z-score) to 0.95 % (P-value)
     *
     * @param lZvalue the Z-value
     * @return double a p value
     */
    public double calculateTwoSidedPvalueForZvalue(double lZvalue) {
        lZvalue = lZvalue / Math.sqrt(2.0);
        double lPvalue = 0.0;
        try {
            lPvalue = Erf.erf(lZvalue);
        } catch (MathException e) {
            //e.printStackTrace();
            //Maximal number of iterations (10,000) exceeded
            //The Z value is to big or to small
            // P value will be 0.0, this is ok!
        }
        return Math.round((Math.abs(lPvalue)) * 1000000.0) / 1000000.0;
    }

        /**
     * This method calculates the p value for a Z-value
     * Example: From 1.96 (Z-score) to 0.975 % (P-value)
     *
     * @param lZvalue the Z-value
     * @return double a p value
     */
    public double calculateOneSidedPvalueForZvalue(double lZvalue) {
        lZvalue = lZvalue / Math.sqrt(2.0);
        double lPvalue = 0.0;
        try {
            lPvalue = Erf.erf(lZvalue);
        } catch (MathException e) {
            //e.printStackTrace();
            //Maximal number of iterations (10,000) exceeded
            //The Z value is to big or to small
            // P value will be 0.0, this is ok!
        }
        lPvalue = (1 - ((1-lPvalue) /2 ));
        return Math.round((lPvalue) * 1000000.0) / 1000000.0;
    }

    /**
     * This method will give a boolean that indicates if we are working with Census data
     * @return boolean that indicates the status
     */
    public boolean isCensusQuantitation() {
        for(int i = 0;i<iRoverSources.size(); i ++){
            RoverSource lRoverSource = iRoverSources.get(i);
            if(lRoverSource == RoverSource.CENSUS){
                return true;
            }
        }
        return false;
    }

    public String getFileLocationOpener() {
        return iFileLocationOpener;
    }

    public void setFileLocationOpener(String iFileLocationOpener) {
        this.iFileLocationOpener = iFileLocationOpener;
    }

    public void setRatioTypes(Vector<String> lRatioTypes) {
        this.iRatioTypes = lRatioTypes;
    }

    public Vector<String> getRatioTypes() {
        return iRatioTypes;
    }

    public void setComponentTypes(Vector<String> lComponentsTypes) {
        this.iComponentsTypes = lComponentsTypes;
    }

    public Vector<String> getComponentTypes() {
        return iComponentsTypes;
    }
    public Vector<RoverSource> getRoverSources() {
        return iRoverSources;
    }

    public void setRoverSources(Vector<RoverSource> iRoverSources) {
        this.iRoverSources = iRoverSources;
    }

    public void setOriginalRoverSources(Vector<RoverSource> iRoverSources) {
        this.iOriginalRoverSources = iRoverSources;
    }

    public void setMultipleSources(boolean multipleSources) {
        this.iMultipleSources = multipleSources;
    }

    public boolean isMultipleSources() {
        return iMultipleSources;
    }

    public void setTitles(Vector<String> titles) {
        this.iTitlesForDifferentSources = titles;
    }

    public Vector<String> getTitles() {
        return iTitlesForDifferentSources;
    }

    public void addProteinSequence(String aAccession, String aSequence){
        this.iSequenceMap.put(aAccession,aSequence);
    }

    public String getProteinSequence(String aAccession){
        String aSequence = (String) iSequenceMap.get(aAccession);
        return aSequence;
    }

    public Vector<RoverSource> getOriginalRoverSources() {
        return iOriginalRoverSources;
    }

    public void setOriginalCollections(Vector<RatioGroupCollection> originalCollections) {
        this.iOriginalCollections = originalCollections;
    }

    public Vector<RatioGroupCollection> getOriginalCollections() {
        return iOriginalCollections;
    }

    public void setProteinAccessions(Vector<String> proteinAccessions) {
        this.iProteinAccessions = proteinAccessions;
    }

    public Vector<String> getProteinAccessions() {
        return iProteinAccessions;
    }

    public void restart() {
        iCommentedProteins = new Vector<QuantitativeProtein>();
        iNotUsedPeptides = new Vector<String>();
        iNotUsedProteins = new Vector<String>();
        iSelectedProteins = new Vector<QuantitativeProtein>();
        iValidatedProteins = new Vector<QuantitativeProtein>();
        iValidatedRatios = new Vector<Ratio>();
    }

    public void setSelectedIndexes(Vector<Boolean> selectedIndexes) {
        this.iSelectedIndexes = selectedIndexes;
    }

    public Vector<Boolean> getSelectedIndexes() {
        if(iSelectedIndexes == null){
            //if it doesn't exist, every index will be used
            Vector<Boolean> lResult = new Vector<Boolean>();
            for(int i = 0; i<iTitlesForDifferentSources.size(); i ++){
                lResult.add(true);
            }
            return lResult;
        }
        return iSelectedIndexes;
    }

    public Vector<RatioType> getMatchedRatioTypes() {
        return iMatchedRatioTypes;
    }

    public void addMatchedRatioTypes(RatioType lMatchedRatioType) {
        this.iMatchedRatioTypes.add(lMatchedRatioType);
    }

    public boolean isUseOriginalRatio() {
        return iUseOriginalRatio;
    }

    public void setUseOriginalRatio(boolean iUseOriginalRatio) {
        this.iUseOriginalRatio = iUseOriginalRatio;
    }
}
