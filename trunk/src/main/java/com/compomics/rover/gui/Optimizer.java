package com.compomics.rover.gui;

import org.apache.log4j.Logger;

import com.compomics.statlib.descriptive.BasicStats;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.fileio.files.MaxQuantEvidenceFile;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.quantitation.*;
import com.compomics.rover.general.quantitation.sorters.QuantitativeProteinSorterByAccession;
import com.compomics.rover.general.quantitation.sorters.QuantitativeProteinSorterByRatioGroupNumbers;
import com.compomics.rover.general.quantitation.sorters.RatioSorterByIntensity;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.util.interfaces.Flamable;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 24-Feb-2010
 * Time: 14:20:09
 * To change this template use File | Settings | File Templates.
 */
public class Optimizer {
	// Class specific log4j logger for Optimizer instances.
	 private static Logger logger = Logger.getLogger(Optimizer.class);


    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();

    public Optimizer() {
        String lEvidenceLocation = "G:\\0520\\raw\\combined\\evidence.txt";
        String lMSMSLocation = "G:\\0520\\raw\\combined\\msms.txt";

        QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
        iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);


        Vector<File> lFiles = new Vector<File>();
        lFiles.add(new File(lEvidenceLocation));
        lFiles.add(new File(lMSMSLocation));

        Vector<RatioGroupCollection> lRatioGroupCollection = new Vector<RatioGroupCollection>();
        for (int i = 0; i < lFiles.size(); i++) {

            //update progress bar
            MaxQuantEvidenceFile lFile;
            if (lFiles.get(i).getName().startsWith("evidence")) {
                lFile = new MaxQuantEvidenceFile(lFiles.get(i), lFiles.get(i + 1), null);
            } else {
                lFile = new MaxQuantEvidenceFile(lFiles.get(i + 1), lFiles.get(i), null);
            }
            i = i + 1;

            RatioGroupCollection lTemp = lFile.getRatioGroupCollection();
            if (lTemp != null) {
                lTemp.setRoverSource(RoverSource.MAX_QUANT);
                lRatioGroupCollection.add(lTemp);
            }

            //_____Do garbage collection______
            System.gc();
        }


        //6. get all the protein accessions from the identifications
        //7.A get the types of the ratios from the first distiller ratio collecion
        Vector<String> lRatioList = lRatioGroupCollection.get(0).getRatioTypes();
        String[] lRatioTypes = new String[lRatioList.size()];
        lRatioList.toArray(lRatioTypes);

        //7.B get the types of the ratios from the first distiller ratio collecion
        List<String> lComponentList = lRatioGroupCollection.get(0).getComponentTypes();
        String[] lComponentTypes = new String[lComponentList.size()];
        lComponentList.toArray(lComponentTypes);

        //8. create all the distiller proteins
        Vector<QuantitativeProtein> lDistillerProtein = new Vector<QuantitativeProtein>();
        HashMap<String, QuantitativeProtein> lProteinMap = new HashMap<String, QuantitativeProtein>();

        if (lRatioGroupCollection.size() == 0) {
            //show gui
            JOptionPane.showMessageDialog(new JFrame(), "No quantitative data could be found!\n The program will close.", "INFO", JOptionPane.INFORMATION_MESSAGE);
        }

        for (int i = 0; i < lRatioGroupCollection.size(); i++) {
            if (i == 0) {
                iQuantitativeValidationSingelton.setRatioTypes(lRatioGroupCollection.get(i).getRatioTypes());
                iQuantitativeValidationSingelton.setComponentTypes(lRatioGroupCollection.get(i).getComponentTypes());
            }


            System.out.println("Found " + lRatioGroupCollection.get(i).size() + " ratio groups!");
            for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);

                if (j % 5000 == 0) {
                    System.out.println("Extracting protein accessions from ratio groups " + (j + 1) + "/" + lRatioGroupCollection.get(i).size() + "(found " + lDistillerProtein.size() + " proteins)");
                }

                String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                    //check if it's a new accession
                    QuantitativeProtein lTempProtein = lProteinMap.get(lAccessionsForRatioGroup[k]);
                    if (lTempProtein != null) {
                        lTempProtein.addRatioGroup(lRatioGroup);
                    } else {
                        QuantitativeProtein lProtein = new QuantitativeProtein(lAccessionsForRatioGroup[k], lRatioTypes);
                        lProtein.addRatioGroup(lRatioGroup);
                        lDistillerProtein.add(lProtein);
                        lProteinMap.put(lAccessionsForRatioGroup[k], lProtein);
                    }
                }
            }
        }


        calculateRazorPeptides(lDistillerProtein);
        iQuantitativeValidationSingelton.setAllProteins(lDistillerProtein);
        iQuantitativeValidationSingelton.setLog2(true);
        iQuantitativeValidationSingelton.setUseOriginalRatio(true);
        iQuantitativeValidationSingelton.addMatchedRatioTypes(new RatioType("H/L", new String[]{"Light", "Heavy"}, "Heavy", 1.0));
        boolean useOnlyTrueRatios = true;
        boolean useNumberOfDifferentUniquePeptides = true;
        boolean useNumberOfDifferentPeptides = true;
        boolean useUniquePeptide = false;

        int[] lJump = new int[]{150,10};
        //int[] lJump = new int[]{150,100,50,25,15,10};
        //int[] lJump = new int[]{100,25,10};

        for(int y = 0; y < lJump.length; y ++){
            try{

                PrintWriter out = new PrintWriter(new FileWriter("G:\\0520\\raw\\combined\\" + "optimumJump" + lJump[y] + ".csv"));

                for (int i = 10; i < 400; i  = i + 10) {
                    System.out.println("\n" + lJump[y] + "  " + i);
                    for(double  j = 0.1; j < 2.5; j = j + 0.1 ){
                        System.gc();
                        System.out.print(".");
                        iQuantitativeValidationSingelton.setUseOriginalRatio(false);
                        int lCycles = 0;
                        lCycles  = doNormalization(lDistillerProtein, lRatioList, i, j, lJump[y]);
                        DescriptiveStatistics lNorm = new DescriptiveStatistics();
                        DescriptiveStatistics lOrig = new DescriptiveStatistics();
                        DescriptiveStatistics lDiffMAD = new DescriptiveStatistics();
                        DescriptiveStatistics lDiffSD = new DescriptiveStatistics();
                        for (int z = 0; z < lDistillerProtein.size(); z++) {

                            boolean useThisProtein = false;
                            boolean firstFilter = true;
                            QuantitativeProtein lProtein = lDistillerProtein.get(z);
                            //do filter
                            //Use the filter: The protein must have more than ... different peptides identified
                            if (useNumberOfDifferentPeptides) {
                                if (useOnlyTrueRatios) {
                                    int lCounter = 0;
                                    for (int q = 0; q < lProtein.getPeptideGroups(true).size(); q++) {
                                        Vector<RatioGroup> lRatioGroups = lProtein.getPeptideGroups(true).get(q).getRatioGroups();
                                        for (int l = 0; l < lRatioGroups.size(); l++) {
                                            boolean lValidFound = false;
                                            RatioGroup lRatioGroup = lRatioGroups.get(l);
                                            for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                                if (lRatioGroup.getRatio(k).getValid()) {
                                                    lValidFound = true;
                                                    k = lProtein.getTypes().length;
                                                    l = lRatioGroups.size();
                                                }
                                            }
                                            if (useUniquePeptide && lValidFound && lRatioGroup.getProteinAccessions().length != 1) {
                                                //it's not unique, set it false
                                                lValidFound = false;
                                            }
                                            if (lValidFound) {
                                                lCounter = lCounter + 1;
                                            }
                                        }
                                    }

                                    if (lCounter > 1) {
                                        if (firstFilter) {
                                            useThisProtein = true;
                                            firstFilter = false;
                                        } else {
                                            // it is not the first filter
                                            // if it's already true don't change it
                                            // if it's false don't change it either because all the filters must be true
                                        }
                                    } else {
                                        firstFilter = false;
                                        useThisProtein = false;
                                    }
                                }


                            }

                            //Use the filter: The protein must have more than ... different unique or razor peptides identified
                            if (useNumberOfDifferentUniquePeptides) {
                                if (useOnlyTrueRatios) {
                                    int lCounter = 0;
                                    for (int q = 0; q < lProtein.getPeptideGroups(true).size(); q++) {
                                        Vector<RatioGroup> lRatioGroups = lProtein.getPeptideGroups(true).get(q).getRatioGroups();
                                        for (int l = 0; l < lRatioGroups.size(); l++) {
                                            boolean lValidFound = false;
                                            RatioGroup lRatioGroup = lRatioGroups.get(l);
                                            for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                                if (lRatioGroup.getRatio(k).getValid()) {
                                                    lValidFound = true;
                                                    if (!lProtein.getPeptideGroups(true).get(q).isLinkedToMoreProteins()) {
                                                        //it's a unique peptide; do nothing
                                                    } else if (lProtein.getAccession().trim().equalsIgnoreCase(lProtein.getPeptideGroups(true).get(q).getRatioGroups().get(0).getRazorProteinAccession().trim())) {
                                                        //it's a razor peptide; do nothing
                                                    } else {
                                                        //it's an isofrom peptide
                                                        lValidFound = false;
                                                    }
                                                    k = lProtein.getTypes().length;
                                                    l = lRatioGroups.size();
                                                }
                                            }
                                            if (useUniquePeptide && lValidFound && lRatioGroup.getProteinAccessions().length != 1) {
                                                //it's not unique, set it false
                                                lValidFound = false;
                                            }
                                            if (lValidFound) {
                                                lCounter = lCounter + 1;
                                            }
                                        }
                                    }

                                    if (lCounter > 1) {
                                        if (firstFilter) {
                                            useThisProtein = true;
                                            firstFilter = false;
                                        } else {
                                            // it is not the first filter
                                            // if it's already true don't change it
                                            // if it's false don't change it either because all the filters must be true
                                        }
                                    } else {
                                        firstFilter = false;
                                        useThisProtein = false;
                                    }
                                }
                            }


                            if (useThisProtein) {
                                iQuantitativeValidationSingelton.setUseOriginalRatio(false);
                                double lNormMADValue = lProtein.getProteinRatioMADForType(lRatioTypes[0]);
                                double lNormSDValue = lProtein.getProteinRatioStandardDeviationForType(lRatioTypes[0]);
                                iQuantitativeValidationSingelton.setUseOriginalRatio(true);
                                double lOrigMADValue = lProtein.getProteinRatioMADForType(lRatioTypes[0]);
                                double lOrigSDValue = lProtein.getProteinRatioStandardDeviationForType(lRatioTypes[0]);
                                iQuantitativeValidationSingelton.setUseOriginalRatio(false);
                                lDiffMAD.addValue(lNormMADValue - lOrigMADValue);
                                lDiffSD.addValue(lNormSDValue - lOrigSDValue);
                            }
                        }

                        String lMedian = lDiffMAD.toString();
                        lMedian = lMedian.substring(lMedian.indexOf("median:") + 7, lMedian.indexOf("\nskew"));

                        String lMedianSD = lDiffSD.toString();
                        lMedianSD = lMedianSD.substring(lMedianSD.indexOf("median:") + 7, lMedianSD.indexOf("\nskew"));
                        if(j == 0.1){
                            if(i == 10){
                                for(double  x = 0.1; x < 2.5; x = x + 0.1 ){
                                    if(j == 0.1){
                                        //out.print("smallWindow\tcycles\tMADmedian\tMAD")
                                    } else{

                                    }

                                }
                            }
                            out.print("\n" + i + "\t" + lCycles + "\t" + lMedian + "\t" + lDiffMAD.getMean() + "\t" + lDiffMAD.getStandardDeviation() + "\t"+ lMedianSD + "\t" + lDiffSD.getMean() + "\t" + lDiffSD.getStandardDeviation() + "\t");
                            out.flush();
                        } else {
                            out.print( lCycles + "\t" + lMedian + "\t" + lDiffMAD.getMean() + "\t" + lDiffMAD.getStandardDeviation() + "\t" + lMedianSD + "\t" + lDiffSD.getMean() + "\t" + lDiffSD.getStandardDeviation() + "\t");
                        }
                    }
                }
                out.flush();
                out.close();

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


    }

    public int doNormalization(Vector<QuantitativeProtein> lProteins, Vector<String> lTypes, int lStart, double lChangingValue, int lJumpFactor) {

        int lCycles = 0;

        for (int i = 0; i < lTypes.size(); i++) {

            String lUnregulatedComponent = null;
            String lRatioTypeTitle = null;
            RatioType lRatioType = null;

            Vector<RatioType> lRatioTypes = iQuantitativeValidationSingelton.getMatchedRatioTypes();
            for (int j = 0; j < lRatioTypes.size(); j++) {
                if (lRatioTypes.get(j).getType().equalsIgnoreCase(lTypes.get(i))) {
                    lUnregulatedComponent = lRatioTypes.get(j).getUnregulatedComponent();
                    lRatioTypeTitle = lRatioTypes.get(j).getType();
                    lRatioType = lRatioTypes.get(j);
                }
            }

            double lStDevOld = Double.MAX_VALUE;
            double lCoefVarOld = Double.MAX_VALUE;
            double lStDevNew = 0.0;
            double lCoefVarNew = 0.0;
            double lCoefVarDiff = 1.0;
            Vector<Vector<Double>> lAllCalculatedMADs = new Vector<Vector<Double>>();

            //get the ratios
            Vector<Ratio> lValidUniqueRatios = new Vector<Ratio>();
            Vector<Ratio> lAllRatios = new Vector<Ratio>();
            Vector<Boolean> lAllRatiosTakenForNormalizationVector = new Vector<Boolean>();

            for (int j = 0; j < lProteins.size(); j++) {
                for (int k = 0; k < lProteins.get(j).getRatioGroups().size(); k++) {
                    Ratio lRatio = lProteins.get(j).getRatioGroups().get(k).getRatioByType(lTypes.get(i));
                    if (lRatio != null) {
                        lRatio.setRecalculatedRatio(Math.log(lRatio.getOriginalRatio(false)) / Math.log(2));
                        if (!lAllRatios.contains(lRatio)) {
                            lAllRatios.add(lRatio);
                            lAllRatiosTakenForNormalizationVector.add(false);
                        }
                        if (lRatio.getValid()) {
                            if (!lValidUniqueRatios.contains(lRatio)) {
                                lValidUniqueRatios.add(lRatio);
                            }
                        }
                    }
                }
            }


            //sort the ratios by intesities
            RatioSorterByIntensity lSorter = new RatioSorterByIntensity(lUnregulatedComponent, lRatioTypeTitle);
            Collections.sort(lAllRatios, lSorter);
            Collections.sort(lValidUniqueRatios, lSorter);

            //System.out.println("^Normalization cycles^Old MAD Sd^Old MAD mean^Old Coef of var^New MAD SD^New MAD mean^New coef of var^");

            //while (lCycles < 10) {
            while (lCoefVarDiff > 0.0005 && lCycles < 11) {

                if (lCycles == 0) {
                    //only do a normalization if its the first cycle
                    //calculate log 2 median
                    double[] lRatios = new double[lValidUniqueRatios.size()];
                    for (int m = 0; m < lValidUniqueRatios.size(); m++) {
                        lRatios[m] = lValidUniqueRatios.get(m).getRatio(true);
                    }
                    double lMedian = BasicStats.median(lRatios, false);
                    //calculate the wanted log 2 median
                    double lWantedMedian = lRatioType.getMedian();
                    lWantedMedian = Math.log(lWantedMedian) / Math.log(2);

                    //use this median to correct every ratio
                    for (int m = 0; m < lAllRatios.size(); m++) {
                        Ratio lRatio = lAllRatios.get(m);
                        double lRatioValue = lRatio.getRatio(true);
                        lRatioValue = lRatioValue + (lWantedMedian - lMedian);
                        lRatio.setRecalculatedRatio(lRatioValue);
                    }

                }

                for (int p = 0; p < lAllRatiosTakenForNormalizationVector.size(); p++) {
                    lAllRatiosTakenForNormalizationVector.set(p, false);
                }


                //Create the holders for the divided ratio vectors
                Vector<Vector<Ratio>> lDividedValidUniqueRatios = new Vector<Vector<Ratio>>();
                Vector<Vector<Ratio>> lDividedValidUniqueSlidingWindowRatios = new Vector<Vector<Ratio>>();
                Vector<Double> lDividedValidUniqueMADS = new Vector<Double>();
                Vector<Double> lScalingFactor = new Vector<Double>();
                Vector<Vector<Ratio>> lDividedAllRatios = new Vector<Vector<Ratio>>();
                DescriptiveStatistics lOldMADs = new DescriptiveStatistics();
                DescriptiveStatistics lNewMADs = new DescriptiveStatistics();
                Vector<Double> lIntensities = new Vector<Double>();

                //calculate the size of each of the 20 groups
                int lHalfGroupSize = lStart;
                if(lCycles != 0){
                    lHalfGroupSize = (int) (lHalfGroupSize * ((lCycles) / lChangingValue));
                }
                //int lJumpFactor = 10;
                int lGroupSize = lHalfGroupSize * 2 + lJumpFactor;
                int lNumberOfGroups = (int) (lValidUniqueRatios.size() / Double.valueOf(lJumpFactor));
                int lLastIndexAdded = -1;
                int lMaximumIndexToAdd = 0;

                //divide the ratios in to groups
                Vector<Double> lGroupMedian = new Vector<Double>();
                Vector<Double> lSlidingGroupMedian = new Vector<Double>();
                double lUpperLast = Double.MIN_VALUE;
                for (int j = 0; j < lNumberOfGroups; j++) {
                    Vector<Ratio> lTempRatiosSlidingGroupUnique = new Vector<Ratio>();
                    Vector<Ratio> lTempRatiosSmallGroupUnique = new Vector<Ratio>();
                    Vector<Ratio> lTempRatiosAll = new Vector<Ratio>();
                    for (int k = 0; k < lGroupSize; k++) {
                        int lIndex = k + (j * lJumpFactor) - ((lGroupSize - lJumpFactor) / 2);
                        if (lIndex >= 0 && lIndex < lValidUniqueRatios.size()) {
                            lTempRatiosSlidingGroupUnique.add(lValidUniqueRatios.get(lIndex));
                        }
                        if (k >= ((lGroupSize - lJumpFactor) / 2) && k < ((lGroupSize - lJumpFactor) / 2) + lJumpFactor) {
                            lTempRatiosSmallGroupUnique.add(lValidUniqueRatios.get(lIndex));
                            lTempRatiosAll.add(lValidUniqueRatios.get(lIndex));
                            lAllRatiosTakenForNormalizationVector.set(lIndex, true);
                            lMaximumIndexToAdd = lAllRatios.indexOf(lValidUniqueRatios.get(lIndex));
                        }
                    }
                    //we sorted the valid unique ratio, now we want to use the lower and upper intensities to create a subset of all ratios
                    Ratio o2 = lTempRatiosSmallGroupUnique.get(lTempRatiosSmallGroupUnique.size() - 1);

                    double lUpper = o2.getParentRatioGroup().getIntensityForComponent(lUnregulatedComponent);


                    lUpperLast = lUpper;
                    lLastIndexAdded = lAllRatios.indexOf(lTempRatiosAll.get(lTempRatiosAll.size() - 1));

                    lDividedAllRatios.add(lTempRatiosAll);
                    lDividedValidUniqueRatios.add(lTempRatiosSmallGroupUnique);
                    lDividedValidUniqueSlidingWindowRatios.add(lTempRatiosSlidingGroupUnique);
                    double lMad = calculateMAD(lTempRatiosSlidingGroupUnique);
                    lOldMADs.addValue(lMad);
                    lDividedValidUniqueMADS.add(lMad);
                    lGroupMedian.add(calculateMedian(lTempRatiosSmallGroupUnique));
                    lSlidingGroupMedian.add(calculateMedian(lTempRatiosSlidingGroupUnique));
                }
                if (lCycles == 0) {
                    lAllCalculatedMADs.add(lIntensities);
                    lAllCalculatedMADs.add(lGroupMedian);
                    lAllCalculatedMADs.add(lSlidingGroupMedian);
                    lAllCalculatedMADs.add(lDividedValidUniqueMADS);
                }

                //calculate the scaling factor
                double lRootedMADProduct = 0.0;
                double lTempProduct = 1.0;
                double lGroupCounts = (double) lDividedValidUniqueMADS.size();
                for (int j = 0; j < lDividedValidUniqueMADS.size(); j++) {
                    if (lDividedValidUniqueMADS.get(j) != 0.0) {
                        lTempProduct = lTempProduct * Math.pow(lDividedValidUniqueMADS.get(j), (1.0 / lGroupCounts));
                    }
                }

                lRootedMADProduct = lTempProduct;

                //System.out.println(lRootedMADProduct);

                lScalingFactor = new Vector<Double>();
                for (int j = 0; j < lDividedValidUniqueMADS.size(); j++) {
                    if (lDividedValidUniqueMADS.get(j) != 0.0) {
                        lScalingFactor.add(lDividedValidUniqueMADS.get(j) / lRootedMADProduct);
                    } else {
                        lScalingFactor.add(1.0);
                    }
                }

                //use the new scaling factors
                for (int j = 0; j < lScalingFactor.size(); j++) {
                    for (int k = 0; k < lDividedAllRatios.get(j).size(); k++) {
                        Ratio lRatio = lDividedAllRatios.get(j).get(k);
                        double lRatioValue = lRatio.getRatio(true);
                        lRatioValue = lRatioValue / lScalingFactor.get(j);
                        lRatio.setRecalculatedRatio(lRatioValue);
                        lRatio.setNormalizationPart(j);
                        if (lCycles == 0) {
                            lRatio.setPreNormalizedMAD(lDividedValidUniqueMADS.get(j));
                        }
                    }
                }
                //calculate the new MADs
                Vector<Double> lTempMADs = new Vector<Double>();
                Vector<Double> lTempGroupMedians = new Vector<Double>();
                Vector<Double> lTempSlidingMedians = new Vector<Double>();
                for (int j = 0; j < lScalingFactor.size(); j++) {
                    double lNewMAD = 0.0;
                    lNewMAD = calculateMAD(lDividedValidUniqueSlidingWindowRatios.get(j));
                    lNewMADs.addValue(lNewMAD);
                    lTempMADs.add(lNewMAD);
                    lTempGroupMedians.add(calculateMedian(lDividedValidUniqueRatios.get(j)));
                    lTempSlidingMedians.add(calculateMedian(lDividedValidUniqueSlidingWindowRatios.get(j)));
                    for (int k = 0; k < lDividedAllRatios.get(j).size(); k++) {
                        Ratio lRatio = lDividedAllRatios.get(j).get(k);
                        lRatio.setNormalizedMAD(lNewMAD);
                    }
                }

                lStDevNew = lNewMADs.getStandardDeviation();
                lStDevOld = lOldMADs.getStandardDeviation();


                lCoefVarNew = Math.abs(lStDevNew / lNewMADs.getMean());
                lCoefVarOld = Math.abs(lStDevOld / lOldMADs.getMean());
                lCoefVarDiff = Math.abs(lCoefVarNew - lCoefVarOld);

                /*
                if (lCoefVarDiff < 0.0005) {
                   //set the ratios from the previous run
                   //since this cycle was not good
                   for (int j = 0; j < lScalingFactor.size(); j++) {
                       for (int k = 0; k < lDividedAllRatios.get(j).size(); k++) {
                           Ratio lRatio = lDividedAllRatios.get(j).get(k);
                           if(lCycles == 0){
                               lRatio.setRecalculatedRatio(lRatio.getOriginalRatio(true));
                               lRatio.setNormalizedMAD(lRatio.getPreNormalizedMAD());
                           } else {
                               lRatio.setRecalculatedRatio(lPreviousRatios.get(j).get(k));
                               lRatio.setNormalizedMAD(lPreviousMADs.get(j));
                           }
                       }
                   }


               } else {
                   //this cycle was good since the new SD is smaller than the previous one
                    System.out.println((lCycles+1) + "\t" + lStDevOld + "\t"+ lOldMADs.getMean()+ "\t"+ lCoefVarOld + "\t" + lStDevNew  + "\t" + lNewMADs.getMean()+ "\t"+ lCoefVarNew );
                    //lPreviousRatios = lTemp;
                    //lPreviousMADs = lTempMADs;
                }*/
                //System.out.println("|" + (lCycles + 1) + "|" + lStDevOld + "|" + lOldMADs.getMean() + "|" + lCoefVarOld + "|" + lStDevNew + "|" + lNewMADs.getMean() + "|" + lCoefVarNew + "|");
                lAllCalculatedMADs.add(lTempMADs);
                lAllCalculatedMADs.add(lTempGroupMedians);
                lAllCalculatedMADs.add(lTempSlidingMedians);
                lCycles = lCycles + 1;
            }
            /*
               try{

                   PrintWriter out = new PrintWriter(new FileWriter("C:\\niklaas\\work\\temp\\" + "medianTest10_50" + ".csv"));

                   System.out.println(lTypes.get(i) + "\n");
                   for (int y = 0; y < lAllCalculatedMADs.get(0).size(); y++) {
                       //System.out.print("\n");
                       out.print("\n");
                       for (int x = 0; x < lAllCalculatedMADs.size(); x++) {
                           //System.out.print(lAllCalculatedMADs.get(x).get(y) + ",");
                           out.print(lAllCalculatedMADs.get(x).get(y) + ",");
                       }
                   }
                   out.flush();
                   out.close();
               } catch (IOException e) {
                   e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
               }
            */

        }
        return lCycles;
    }


    public double calculateMAD(Vector<Ratio> lRatios) {
        double[] lRatioDoubles = new double[lRatios.size()];
        for (int i = 0; i < lRatios.size(); i++) {
            lRatioDoubles[i] = lRatios.get(i).getRatio(true);
        }
        return BasicStats.mad(lRatioDoubles, false);
    }


    public double calculateMADFromRatios(Vector<Double> lRatios) {
        double[] lRatioDoubles = new double[lRatios.size()];
        for (int i = 0; i < lRatios.size(); i++) {
            lRatioDoubles[i] = lRatios.get(i);
        }
        return BasicStats.mad(lRatioDoubles, false);
    }

    public double calculateMedian(Vector<Ratio> lRatios) {
        double[] lRatioDoubles = new double[lRatios.size()];
        for (int i = 0; i < lRatios.size(); i++) {
            lRatioDoubles[i] = lRatios.get(i).getRatio(true);
        }
        return BasicStats.median(lRatioDoubles, false);
    }


    /**
     * This method will calculate the razor accession for every ratiogroup linked to the given proteins
     *
     * @param aProteins
     */
    public void calculateRazorPeptides(Vector<QuantitativeProtein> aProteins) {
        //create a hashmap with the protein accession and the number of peptide groups linked to the protein
        HashMap lProteinsPeptideNumber = new HashMap();
        HashMap lProteinsIdentificationNumber = new HashMap();
        for (int i = 0; i < aProteins.size(); i++) {
            lProteinsPeptideNumber.put(aProteins.get(i).getAccession().trim(), aProteins.get(i).getNumberOfPeptideGroups());
            lProteinsIdentificationNumber.put(aProteins.get(i).getAccession().trim(), aProteins.get(i).getNumberOfIdentifications());
        }
        //we will get the razor accession for every ratio group
        for (int i = 0; i < aProteins.size(); i++) {
            for (int j = 0; j < aProteins.get(i).getPeptideGroups(false).size(); j++) {
                for (int k = 0; k < aProteins.get(i).getPeptideGroups(false).get(j).getRatioGroups().size(); k++) {
                    RatioGroup lRatioGroup = aProteins.get(i).getPeptideGroups(false).get(j).getRatioGroups().get(k);
                    if (lRatioGroup.getRazorProteinAccession() == null) {
                        //the razor accession in not set yet
                        int lPeptideGroupsLinked = 0;
                        int lIdentficationsLinked = 0;
                        String lRazorAccession = null;
                        for (int l = 0; l < lRatioGroup.getProteinAccessions().length; l++) {
                            if (lPeptideGroupsLinked < (Integer) lProteinsPeptideNumber.get(lRatioGroup.getProteinAccessions()[l].trim())) {
                                lRazorAccession = lRatioGroup.getProteinAccessions()[l].trim();
                                lPeptideGroupsLinked = (Integer) lProteinsPeptideNumber.get(lRatioGroup.getProteinAccessions()[l].trim());
                                lIdentficationsLinked = (Integer) lProteinsIdentificationNumber.get(lRatioGroup.getProteinAccessions()[l].trim());
                            } else if (lPeptideGroupsLinked == (Integer) lProteinsPeptideNumber.get(lRatioGroup.getProteinAccessions()[l].trim())) {
                                if (lIdentficationsLinked < (Integer) lProteinsIdentificationNumber.get(lRatioGroup.getProteinAccessions()[l].trim())) {
                                    lRazorAccession = lRatioGroup.getProteinAccessions()[l].trim();
                                    lPeptideGroupsLinked = (Integer) lProteinsPeptideNumber.get(lRatioGroup.getProteinAccessions()[l].trim());
                                    lIdentficationsLinked = (Integer) lProteinsIdentificationNumber.get(lRatioGroup.getProteinAccessions()[l].trim());
                                }
                            }
                        }
                        lRatioGroup.setRazorProteinAccession(lRazorAccession);
                    }
                }
            }
        }


    }


    public static void main(String[] args) {
        Optimizer lOp = new Optimizer();
    }
}
