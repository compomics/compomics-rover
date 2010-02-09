package com.compomics.rover.general.quantitation;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.Vector;
import java.util.HashMap;
import java.util.Arrays;

import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.interfaces.Ratio;
import be.proteomics.statlib.descriptive.BasicStats;
import com.compomics.rover.general.quantitation.QuantitativeProtein;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;


/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 19-dec-2008
 * Time: 8:54:57
 */

/**
 * This class holds the proteins that make the reference set.
 */
public class ReferenceSet {

    /**
     * An ArrayList with QuantiativeProteins that represent the reference set
     */
    private ArrayList<QuantitativeProtein> iReferenceProteins = new ArrayList<QuantitativeProtein>();
    /**
     * String[] with the different ratio types (L/H, M/H, ...)
     */
    private String[] iRatioTypes;
    /**
     * String[] with the different component types (Light, Heavy, ...)
     */
    private String[] iComponents;
    /**
     * An array with DiscriptiveStatistics for every ratio type
     */
    private DescriptiveStatistics[] iDescriptiveStats;
    /**
     * An array of hashmaps with the huber estimators for every ratio type
     */
    private HashMap[] iHuberEstimators;
    /**
     * The log2 status is true when the descriptive statistics is calculated with log2(ratios).
     */
    private boolean iLog2StatusDescriptiveStatistics;
    /**
     * A XYSeries with all the ratios for the reference proteins
     */
    private XYSeries iDensityReal;
    /**
     * A XYSeries with all the ratios for the reference proteins
     */
    private XYSeries iDensityHuberCalculated;
    /**
     * This quantitative validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();

    private int[] iNumberOfRatiosUsedInHuberStatistics;
    /**
     * The value for the heights point in the y-axis in the chart
     */
    private int iHighestYAxisDensity;
    /**
     * The number of proteins used in the reference set
     */
    private int iUsedProteins = 0;
    /**
     * The number of peptides used in the reference set
     */
    private int[] iUsedRatiosByType;

    private Vector<HashMap> iRandomMeansMapVector = new Vector<HashMap>();
    private Vector<HashMap> iRandomSDMapVector = new Vector<HashMap>();
    private Vector<HashMap> iRandomOriginalRatioMeansMapVector = new Vector<HashMap>();
    private Vector<HashMap> iRandomOriginalRatioSDMapVector = new Vector<HashMap>();
    private Vector<HashMap> iRandomIntSDMapVector = new Vector<HashMap>();
    private Vector<HashMap> iRandomIntMeanMapVector = new Vector<HashMap>();
    private Vector<HashMap> iRandomIntMedianMapVector = new Vector<HashMap>();
    private Vector<HashMap> iRandomIntSumMapVector = new Vector<HashMap>();




    /**
     * Construtor
     * @param aReferenceProteins The proteins that make the reference set
     * @param aRatioTypes The different ratio types
     * @param aComponents The different components types
     */
    public ReferenceSet(ArrayList<QuantitativeProtein> aReferenceProteins, String[] aRatioTypes, String[] aComponents) {
        this.iReferenceProteins = aReferenceProteins;
        this.iRatioTypes = aRatioTypes;
        this.iComponents = aComponents;
    }

    /**
     * Add a QuantiativeProtein to the reference set
     * @param aReferenceProtein QuantiativeProtein to add to the reference set
     */
    public void addReferenceProtein(QuantitativeProtein aReferenceProtein){
        iReferenceProteins.add(aReferenceProtein);
        iUsedProteins = iUsedProteins + 1;
    }


    /**
     * This method will calculate the "delta" (differrence between the average and the ratio),
     * "stDev" (Standard deviation of the reference set) and "significance" (the Z-score for this ratio) and store the values
     * in a HashMap
     * @param aType The ratio type
     * @param aRatio The ratio
     * @return HashMap with "delta", "stDev", "significance"
     */
    public HashMap getStatisticalMeasermentForRatio(String aType, Ratio aRatio){
        HashMap mapResult = new HashMap();

        if(iHuberEstimators == null){
            //the huber statistic is not done, do it now
            this.huberStatistics();
        }

        //add huber estimated distrubution
        for(int i = 0; i<iRatioTypes.length; i++){
            if(iRatioTypes[i].equalsIgnoreCase(aType)){
                HashMap mapHuber = iHuberEstimators[i];
                double lStDev = (Double)mapHuber.get("stdev");
                double lMean = (Double)mapHuber.get("mean");
                double delta = Math.round((aRatio.getRatio(true)-lMean)*1000.0)/1000.0;
                mapResult.put("delta", delta);
                mapResult.put("stDev", lStDev);
                mapResult.put("significance", Math.round((delta/lStDev)*1000.0)/1000.0);
            }
        }
        return mapResult;
    }


    /**
     * This method will return a hashmap with the huber estimations for a specific ratio type.
     * The keys in the hashmap are: "mean","stdev", "iterations".
     * @param aType String with the ratio type
     * @return HashMap with the huber estimators
     */
    public HashMap getHuberEstimatorsForType(String aType){
        //check if the calculation is already done
        if(iHuberEstimators == null){
            this.huberStatistics();
        }
        for(int i = 0; i<iRatioTypes.length; i++){
            if(iRatioTypes[i].equalsIgnoreCase(aType)){
                return iHuberEstimators[i];
            }
        }
        return null;
    }

    /**
     * This method will do the huber statistics for this reference set an for every ratio type
     */
    public void huberStatistics(){

        //create the int[] to store the number of used ratios
        if(iUsedRatiosByType == null){
            iUsedRatiosByType = new int[iRatioTypes.length];
            for(int i = 0; i<iUsedRatiosByType.length; i ++){
                iUsedRatiosByType[i] = 0;
            }
        } else {
            for(int i = 0; i<iUsedRatiosByType.length; i ++){
                iUsedRatiosByType[i] = 0;
            }
        }

        // Create a HashMap for every ratio type
        this.iHuberEstimators = new HashMap[iRatioTypes.length];
        // Create an array where the number of ratios used for a Huber estimation will be stored
        this.iNumberOfRatiosUsedInHuberStatistics = new int[iRatioTypes.length];

        // Okay, do the stats on the log2 ratios.
        for(int t = 0; t<iRatioTypes.length; t ++){
            Vector<Double> lLog2Ratios = new Vector<Double>();
            //we will first look for the ratios where we want to do statistics on
            for (int i = 0; i < iReferenceProteins.size(); i++) {
                Vector<RatioGroup> lRatioGroups = iReferenceProteins.get(i).getRatioGroups();
                for(int j = 0; j<lRatioGroups.size(); j ++){
                    Ratio lRatio  = lRatioGroups.get(j).getRatioByType(iRatioTypes[t]);
                    if(lRatio != null){
                        if(iQuantitativeValidationSingelton.isRatioValidInReferenceSet()){
                            //check if the ratio is valid
                            if(lRatio.getValid()){
                                if(!Double.isNaN(lRatio.getRatio(true)) && !Double.isInfinite(lRatio.getRatio(true))){
                                    lLog2Ratios.add(lRatio.getRatio(true));
                                    iUsedRatiosByType[t] = iUsedRatiosByType[t] + 1;
                                }
                            }
                        } else {
                            if(!Double.isNaN(lRatio.getRatio(true)) && !Double.isInfinite(lRatio.getRatio(true))){
                                lLog2Ratios.add(lRatio.getRatio(true));
                                iUsedRatiosByType[t] = iUsedRatiosByType[t] + 1;
                            }
                        }
                    }
                }
            }
            //we have the ratios to do the statistics on
            double[] log2Ratios = new double[lLog2Ratios.size()];
            for(int i = 0; i<lLog2Ratios.size(); i ++){
                log2Ratios[i] = lLog2Ratios.get(i);
            }
            //do the statistics
            double[] estimators = BasicStats.hubers(log2Ratios, 1e-06, false);
            //correct the StDev with the instrument calibrated SD
            double lCorrectedStDev = Math.sqrt(Math.pow(estimators[1],2) + Math.pow(iQuantitativeValidationSingelton.getCalibratedStdev(),2));
            iHuberEstimators[t] = new HashMap();
            iHuberEstimators[t].put("mean", estimators[0]);
            iHuberEstimators[t].put("stdev", lCorrectedStDev);
            iHuberEstimators[t].put("iterations", estimators[2]);
            iNumberOfRatiosUsedInHuberStatistics[t] = log2Ratios.length;
        }
    }

    /**
     * Method that creates the DescriptiveStatistics for every ratio type
     */
    public void createDescriptiveStatistics(){

        //create the int[] to store the number of used ratios
        if(iUsedRatiosByType == null){
            iUsedRatiosByType = new int[iRatioTypes.length];
            for(int i = 0; i<iUsedRatiosByType.length; i ++){
                iUsedRatiosByType[i] = 0;
            }
        } else {
            for(int i = 0; i<iUsedRatiosByType.length; i ++){
                iUsedRatiosByType[i] = 0;
            }
        }

        if(iQuantitativeValidationSingelton.isLog2()){
            iLog2StatusDescriptiveStatistics = true;
        }else{
            iLog2StatusDescriptiveStatistics = false;
        }
        //create a descriptive statistics for every ratio type
        this.iDescriptiveStats = new DescriptiveStatistics[iRatioTypes.length];
        for(int t = 0; t<iRatioTypes.length; t ++){
            DescriptiveStatistics stat = new DescriptiveStatistics();
            for (int i = 0; i < iReferenceProteins.size(); i++) {
                Vector<RatioGroup> lRatioGroups = iReferenceProteins.get(i).getRatioGroups();
                for(int j = 0; j<lRatioGroups.size(); j ++){
                    Ratio lRatio = lRatioGroups.get(j).getRatioByType(iRatioTypes[t]);
                    if(lRatio != null){
                        if(iQuantitativeValidationSingelton.isRatioValidInReferenceSet()){
                            //check if the ratio is valid
                            if(lRatio.getValid()){
                                if(!Double.isNaN(lRatio.getRatio(true)) && !Double.isInfinite(lRatio.getRatio(true))){
                                    stat.addValue(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()));
                                    iUsedRatiosByType[t] = iUsedRatiosByType[t] + 1;
                                }
                            }
                        } else {
                            if(!Double.isNaN(lRatio.getRatio(true)) && !Double.isInfinite(lRatio.getRatio(true))){
                                stat.addValue(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()));
                                iUsedRatiosByType[t] = iUsedRatiosByType[t] + 1;
                            }
                        }
                    }

                }
            }
            iDescriptiveStats[t] = stat;
        }
    }

    /**
     * This method creates an XYSeriesCollection. This collection is used to create a "histogram"-like presentation of the referenceset.
     * A series of intervals will be created between the lower and upper limit. All the ratios of the reference proteins will be assinged to
     * one of the intervals. So, in the end you can know how many ratios of the reference set are in a specific interval.
     * @param aType Ratio type
     * @param aNumberOfParts The number of intervals
     * @param aLowerLimit The lower limit
     * @param aUpperlimit The upper limit
     * @param showHuberDistribution A boolean that indicates if we have to show the huber calculated distribution
     * @param showRealDistribution A boolean that indicates if we have to show the real distribution
     * @return XYSeriesCollection
     */
    public XYSeriesCollection getChartDataSet(String aType, int aNumberOfParts, int aLowerLimit , int aUpperlimit, boolean showHuberDistribution, boolean showRealDistribution){

        //creat the data set
        XYSeriesCollection lDataset = new XYSeriesCollection();
        //create the distributions
        if(iQuantitativeValidationSingelton.isLog2()){
            iDensityReal = new XYSeries("Ratio distribution (log 2) in reference proteins");
            iDensityHuberCalculated = new XYSeries("Normal distribution (log 2) in reference proteins calcutaled by huber estimator");
        } else {
            iDensityReal = new XYSeries("Ratio distribution in reference proteins");
            iDensityHuberCalculated = new XYSeries("Distribution in reference proteins calcutaled by huber estimator");
        }
        //add the wanted distributions
        if(showHuberDistribution){
            lDataset.addSeries(iDensityHuberCalculated);
        }
        if(showRealDistribution){
            lDataset.addSeries(iDensityReal);
        }

        //add "real" distribution
        int lHighestDensity = 0;
        if(iDescriptiveStats == null || iQuantitativeValidationSingelton.isLog2() != iLog2StatusDescriptiveStatistics){
            this.createDescriptiveStatistics();
        }
        if(iHuberEstimators == null){
            this.huberStatistics();
        }
        for(int i = 0; i<iRatioTypes.length; i++){
            if(iRatioTypes[i].equalsIgnoreCase(aType)){
                DescriptiveStatistics stat = iDescriptiveStats[i];
                double lLowest = aLowerLimit;
                double lHighest = aUpperlimit;
                double lDiff = lHighest - lLowest;
                double lInterval = lDiff / aNumberOfParts;
                double[] lRatios = stat.getSortedValues();

                int[] lWeight = new int[aNumberOfParts];
                double[] lValue = new double[aNumberOfParts];
                for(int j = 0; j<lWeight.length; j++){
                    lWeight[j] = 0;
                    lValue[j] =  lLowest + (j * lInterval);
                }

                for(int j = 0; j<lRatios.length; j ++){
                    for(int k = 0; k<lWeight.length; k++){
                        double lLowerLimit = lLowest + k * lInterval - lInterval/2;
                        if(lLowerLimit <= lRatios[j] && lRatios[j] < lLowerLimit + lInterval){
                            lWeight[k] = lWeight[k] + 1;
                            if(lWeight[k]>lHighestDensity){
                                lHighestDensity = lWeight[k];
                            }
                        }
                    }
                }
                iHighestYAxisDensity = lHighestDensity;
                for(int j = 0; j<lWeight.length; j ++){
                    iDensityReal.add(lValue[j], lWeight[j]);
                }
            }
        }


        //add huber estimated distrubution
        for(int i = 0; i<iRatioTypes.length; i++){
            if(iRatioTypes[i].equalsIgnoreCase(aType)){
                HashMap map = iHuberEstimators[i];
                double lLowest = aLowerLimit;
                double lHighest = aUpperlimit;
                double lDiff = lHighest - lLowest;
                double lInterval = lDiff / aNumberOfParts;
                double lStDev = (Double)map.get("stdev");
                double lMean = (Double)map.get("mean");

                double[] lValue = new double[aNumberOfParts];
                for(int j = 0; j<lValue.length; j++){
                    lValue[j] =  lLowest + (j * lInterval);
                }

                //calcutate the height for the mean with the probabilty distribution function
                double lMeanDenom = lStDev * Math.sqrt(2*3.1415926539);
                double lMeanHeight = 1/lMeanDenom * Math.exp((-0.5) * Math.pow(((lMean-lMean)/lStDev), 2));
                //calculate the correction for the height (the height must be the same as the maximun height in the "real" distribution
                double lCorrection = (double)lHighestDensity/lMeanHeight;

                for(int k = 0; k<lValue.length; k++){
                    double lValueToCalculateDensityFor = lValue[k];
                    if(!iQuantitativeValidationSingelton.isLog2()){
                        //we are not in the log2 mode
                        //the values (for which the density will be calculated) must first be made log 2
                        lValueToCalculateDensityFor = Math.log(lValue[k])/Math.log(2);
                    }
                    double denom = lStDev * Math.sqrt(2*3.1415926539);
                    double y = 1/denom * Math.exp((-0.5) * Math.pow(((lValueToCalculateDensityFor-lMean)/lStDev), 2));
                    iDensityHuberCalculated.add(lValue[k], (y*lCorrection));
                }

            }
        }

        return lDataset;
    }


    /**
     * This method clears (deletes) the calculated referenec set
     */
    public void clearCalculateReferenceSet(){
        iHuberEstimators = null;
        iDescriptiveStats = null;
        iUsedRatiosByType = null;
    }

    /**
     * This method gives the ratio for a specific percentile (in the real distribution) and ratio type
     * @param aType Ratio type
     * @param aPercentile Percentile
     * @return double with the ratio for that percentile and type
     */
    public double getPercentileReal(String aType, double aPercentile){
        double lPercentile = 0.0;
        if(iDescriptiveStats == null || iQuantitativeValidationSingelton.isLog2() != iLog2StatusDescriptiveStatistics){
            this.createDescriptiveStatistics();
        }
        for(int i = 0; i<iRatioTypes.length; i++){
            if(iRatioTypes[i].equalsIgnoreCase(aType)){
                DescriptiveStatistics stat = iDescriptiveStats[i];
                lPercentile = stat.getPercentile(aPercentile);
            }
        }
        return lPercentile;
    }

    /**
     * This method gives the ratio for a specific percentile (in the huber estimation) and ratio type
     * @param aType Ratio type
     * @param aZscore The z score (1.96 for 95%)
     * @return double with the ratio for that percentile and type
     */
    public double getPercentileHuber(String aType, double aZscore){
        double lPercentile = 0.0;
        if(iHuberEstimators == null ){
            this.huberStatistics();
        }
        for(int i = 0; i<iRatioTypes.length; i++){
            if(iRatioTypes[i].equalsIgnoreCase(aType)){
                HashMap map = iHuberEstimators[i];
                double lStDev = (Double)map.get("stdev");
                double lMean = (Double)map.get("mean");

                lPercentile = lMean + lStDev*aZscore;
            }
        }
        if(iQuantitativeValidationSingelton.isLog2()){
            return lPercentile;
        }
        //not log2 mode
        return Math.pow(2, (lPercentile));
    }

    /**
     * Getter for the different ratio types
     * @return String[] with the different ratio types
     */
    public String[] getTypes(){
        return iRatioTypes;
    }

     /**
     * Getter for the different component types
     * @return String[] with the different ratio types
     */
    public String[] getComponents() {
        return iComponents;
    }

    /**
     * Getter for the HighesYAxisDensity parameter
     * @return int with the highest y-axis value
     */
    public int getHighestYAxisValue(){
        return iHighestYAxisDensity;
    }

    /**
     * Get the number of used proteins in this reference set
     * @return int with the number of used proteins
     */
    public int getUsedProteinsNumber() {
        return iUsedProteins;
    }

    /**
     * Get the number of used ratios for every ratiotype
     * @return int[] with the number of ratio used for every type
     */
    public int[] getUsedRatiosByType() {
        if(iUsedRatiosByType == null){
            this.createDescriptiveStatistics();
            this.huberStatistics();
        }
        return iUsedRatiosByType;
    }

    
    public void calculateStatisticsByRandomSampling(){
        Vector<QuantitativeProtein> lAllProteins = iQuantitativeValidationSingelton.getAllProteins();

        //find which samples sizes must be calculated
        int lMaxRatiosForProtein = 0;
        for(int i = 0; i<lAllProteins.size(); i ++){
            if(lMaxRatiosForProtein < lAllProteins.get(i).getNumberOfRatioGroups()){
                lMaxRatiosForProtein = lAllProteins.get(i).getNumberOfRatioGroups();
            }
        }

        //calculate the statistics for every ratio type
        //we only want the unique peptide ratios
        boolean lUnique = iQuantitativeValidationSingelton.isUseOnlyUniqueRatioForProteinMean();
        boolean lValid = iQuantitativeValidationSingelton.isUseOnlyValidRatioForProteinMean();
        iQuantitativeValidationSingelton.setUseOnlyUniqueRatioForProteinMean(true);
        iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);
        for(int i = 0; i<iQuantitativeValidationSingelton.getRatioTypes().size(); i ++){
            //gather all the unique and valid peptide ratios
            Vector<Double> lAllRatios = new Vector<Double>();
            Vector<Double> lAllOriginalRatios = new Vector<Double>();
            Vector<Double> lAllIntensities = new Vector<Double>();
            for(int j = 0; j<lAllProteins.size(); j ++){
                for(int k = 0; k<lAllProteins.get(j).getPeptideGroups(false).size(); k ++){
                    Vector<Double> lTempRatios =  lAllProteins.get(j).getPeptideGroups(false).get(k).getRatiosForType(iQuantitativeValidationSingelton.getRatioTypes().get(i),true, -1);
                    iQuantitativeValidationSingelton.setUseOriginalRatio(true);
                    Vector<Double> lTempOriginalRatios =  lAllProteins.get(j).getPeptideGroups(false).get(k).getRatiosForType(iQuantitativeValidationSingelton.getRatioTypes().get(i),true, -1);
                    iQuantitativeValidationSingelton.setUseOriginalRatio(false);
                    Vector<Double> lTempInts =  lAllProteins.get(j).getPeptideGroups(false).get(k).getIntensitiesForType(iQuantitativeValidationSingelton.getRatioTypes().get(i),true, -1);

                    for(int l = 0; l<lTempRatios.size(); l ++){
                        lAllRatios.add(lTempRatios.get(l));
                        lAllOriginalRatios.add(lTempOriginalRatios.get(l));
                    }
                    for(int l = 0; l<lTempInts.size(); l ++){
                        lAllIntensities.add(lTempInts.get(l));
                    }
                }
            }

            //create the hashmap to store the different statistics in
            HashMap lRatioMeansMap = new HashMap();
            HashMap lRatioSDMap = new HashMap();
            HashMap lOriginalRatioMeansMap = new HashMap();
            HashMap lOriginalRatioSDMap = new HashMap();
            HashMap lIntensityMeansMap = new HashMap();
            HashMap lIntensityMedianMap = new HashMap();
            HashMap lIntensitySumsMap = new HashMap();
            HashMap lIntensitySDMap = new HashMap();
            for(int j = 1; j<= lMaxRatiosForProtein; j ++){
                System.out.println("Random sampling " + (j+1) + "/" + lMaxRatiosForProtein);
                DescriptiveStatistics lStatMean = new DescriptiveStatistics();
                DescriptiveStatistics lStatSD = new DescriptiveStatistics();
                DescriptiveStatistics lStatOriginalMean = new DescriptiveStatistics();
                DescriptiveStatistics lStatOriginalSD = new DescriptiveStatistics();
                DescriptiveStatistics lStatIntSD = new DescriptiveStatistics();
                DescriptiveStatistics lStatIntSum = new DescriptiveStatistics();
                DescriptiveStatistics lStatIntMean = new DescriptiveStatistics();
                DescriptiveStatistics lStatIntMedian = new DescriptiveStatistics();
                for(int k = 0; k<1000; k ++){
                    DescriptiveStatistics lTempRatio = new DescriptiveStatistics();
                    DescriptiveStatistics lTempOriginalRatio = new DescriptiveStatistics();
                    DescriptiveStatistics lTempIntensity = new DescriptiveStatistics();
                    for(int l = 0; l<j; l ++){
                        //get random ratio and put it in temp
                        int lRandom = (int) (Math.random() * lAllRatios.size());
                        int lRandom2 = (int) (Math.random() * lAllIntensities.size());
                        int lRandom3 = (int) (Math.random() * lAllOriginalRatios.size());
                        lTempRatio.addValue(lAllRatios.get(lRandom));
                        lTempOriginalRatio.addValue(lAllOriginalRatios.get(lRandom3));
                        lTempIntensity.addValue(lAllIntensities.get(lRandom2));
                    }
                    double[] lvalues = lTempIntensity.getValues();
                    double lMedianInt = BasicStats.median(lvalues, false);
                    lStatMean.addValue(lTempRatio.getMean());
                    lStatSD.addValue(lTempRatio.getStandardDeviation());
                    lStatOriginalMean.addValue(lTempOriginalRatio.getMean());
                    lStatOriginalSD.addValue(lTempOriginalRatio.getStandardDeviation());
                    lStatIntSD.addValue(lTempIntensity.getStandardDeviation());
                    lStatIntMean.addValue(lTempIntensity.getMean());
                    lStatIntMedian.addValue(lMedianInt);
                    lStatIntSum.addValue(lTempIntensity.getSum());
                }

                lRatioMeansMap.put(j,lStatMean);
                lRatioSDMap.put(j,lStatSD);
                lOriginalRatioMeansMap.put(j,lStatOriginalMean);
                lOriginalRatioSDMap.put(j,lStatOriginalSD);
                lIntensityMeansMap.put(j,lStatIntMean);
                lIntensityMedianMap.put(j,lStatIntMedian);
                lIntensitySDMap.put(j,lStatIntSD);
                lIntensitySumsMap.put(j,lStatIntSum);

            }

            iRandomMeansMapVector.add(lRatioMeansMap);
            iRandomSDMapVector.add(lRatioSDMap);
            iRandomOriginalRatioMeansMapVector.add(lOriginalRatioMeansMap);
            iRandomOriginalRatioSDMapVector.add(lOriginalRatioSDMap);
            iRandomIntMeanMapVector.add(lIntensityMeansMap);
            iRandomIntMedianMapVector.add(lIntensityMedianMap);
            iRandomIntSDMapVector.add(lIntensitySDMap);
            iRandomIntSumMapVector.add(lIntensitySumsMap);
        }

        iQuantitativeValidationSingelton.setUseOnlyUniqueRatioForProteinMean(lUnique);
        iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(lValid);

    }

    public double getZscoreForRatioMean(double lMean, int lNumberOfRatios, String lType){
        double lZScore = 0;
        if(lNumberOfRatios != 0){
            for(int i = 0; i< iQuantitativeValidationSingelton.getRatioTypes().size(); i ++){
                if(lType.equalsIgnoreCase(iQuantitativeValidationSingelton.getRatioTypes().get(i))){
                    DescriptiveStatistics lStat = (DescriptiveStatistics) iRandomMeansMapVector.get(i).get(lNumberOfRatios);
                    if(iQuantitativeValidationSingelton.isUseOriginalRatio()){
                        lStat = (DescriptiveStatistics) iRandomOriginalRatioMeansMapVector.get(i).get(lNumberOfRatios);
                    }
                    lZScore = (lMean -  lStat.getMean())/lStat.getStandardDeviation();
                }
            }
        }
        return lZScore;
    }

    public double getZscoreForRatioSd(double lSd, int lNumberOfRatios, String lType){
        double lZScore = 0;
        if(lNumberOfRatios != 0){
            for(int i = 0; i< iQuantitativeValidationSingelton.getRatioTypes().size(); i ++){
                if(lType.equalsIgnoreCase(iQuantitativeValidationSingelton.getRatioTypes().get(i))){
                    DescriptiveStatistics lStat = (DescriptiveStatistics) iRandomSDMapVector.get(i).get(lNumberOfRatios);
                    if(iQuantitativeValidationSingelton.isUseOriginalRatio()){
                        lStat = (DescriptiveStatistics) iRandomOriginalRatioSDMapVector.get(i).get(lNumberOfRatios);
                    }
                    lZScore = (lSd -  lStat.getMean())/lStat.getStandardDeviation();
                }
            }
        }
        return lZScore;
    }

    public double getZscoreForIntensityMean(double lMean, int lNumberOfRatios, String lType){
        double lZScore = 0;
        if(lNumberOfRatios != 0){
            for(int i = 0; i< iQuantitativeValidationSingelton.getRatioTypes().size(); i ++){
                if(lType.equalsIgnoreCase(iQuantitativeValidationSingelton.getRatioTypes().get(i))){
                    DescriptiveStatistics lStat = (DescriptiveStatistics) iRandomIntMeanMapVector.get(i).get(lNumberOfRatios);
                    lZScore = (lMean -  lStat.getMean())/lStat.getStandardDeviation();
                }
            }
        }
        return lZScore;
    }

    public double getZscoreForIntensityMedian(double lMedian, int lNumberOfRatios, String lType){
        double lZScore = 0;
        if(lNumberOfRatios != 0){
            for(int i = 0; i< iQuantitativeValidationSingelton.getRatioTypes().size(); i ++){
                if(lType.equalsIgnoreCase(iQuantitativeValidationSingelton.getRatioTypes().get(i))){
                    DescriptiveStatistics lStat = (DescriptiveStatistics) iRandomIntMedianMapVector.get(i).get(lNumberOfRatios);
                    lZScore = (lMedian -  lStat.getMean())/lStat.getStandardDeviation();
                }
            }
        }
        return lZScore;
    }

    public double getZscoreForIntensitySd(double lSd, int lNumberOfRatios, String lType){
        double lZScore = 0;
        if(lNumberOfRatios != 0){
            for(int i = 0; i< iQuantitativeValidationSingelton.getRatioTypes().size(); i ++){
                if(lType.equalsIgnoreCase(iQuantitativeValidationSingelton.getRatioTypes().get(i))){
                    DescriptiveStatistics lStat = (DescriptiveStatistics) iRandomIntSDMapVector.get(i).get(lNumberOfRatios);
                    lZScore = (lSd -  lStat.getMean())/lStat.getStandardDeviation();
                }
            }
        }
        return lZScore;
    }

    public double getZscoreForIntensitySum(double lSum, int lNumberOfRatios, String lType){
        double lZScore = 0;
        if(lNumberOfRatios != 0){
            for(int i = 0; i< iQuantitativeValidationSingelton.getRatioTypes().size(); i ++){
                if(lType.equalsIgnoreCase(iQuantitativeValidationSingelton.getRatioTypes().get(i))){
                    DescriptiveStatistics lStat = (DescriptiveStatistics) iRandomIntSumMapVector.get(i).get(lNumberOfRatios);
                    lZScore = (lSum -  lStat.getMean())/lStat.getStandardDeviation();
                }
            }
        }
        return lZScore;
    }

}
