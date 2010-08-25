package com.compomics.rover.gui.multiwizard;

import org.apache.log4j.Logger;

import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.interfaces.WizardPanel;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.quantitation.*;
import com.compomics.rover.general.quantitation.sorters.QuantitativeProteinSorterByRatioGroupNumbers;
import com.compomics.rover.general.quantitation.sorters.QuantitativeProteinSorterByAccession;
import com.compomics.rover.general.quantitation.sorters.RatioSorterByIntensity;
import com.compomics.rover.general.quantitation.sorters.RatioSorterBySummedIntensities;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.sequenceretriever.UniprotSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.IpiSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.NcbiSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.TairSequenceRetriever;
import com.compomics.rover.gui.MatchRatioWithComponent;
import com.compomics.rover.gui.QuantitationValidationGUI;
import com.compomics.util.sun.SwingWorker;
import be.proteomics.statlib.descriptive.BasicStats;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 11-Dec-2009
 * Time: 14:07:38
 * To change this template use File | Settings | File Templates.
 */
public class ProteinCreatorPanel implements WizardPanel {
	// Class specific log4j logger for ProteinCreatorPanel instances.
	 private static Logger logger = Logger.getLogger(ProteinCreatorPanel.class);
    private JPanel jpanContent;
    private JProgressBar progressBar;
    private WizardFrameHolder iParent;
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();

    public ProteinCreatorPanel(WizardFrameHolder aParent) {
        this.iParent = aParent;
    }

    public JPanel getContentPane() {
        return jpanContent;
    }

    public void backClicked() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void nextClicked() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean feasableToProceed() {
        return true;
    }

    public String getNotFeasableReason() {
        return null;
    }

    public void construct() {
        proteinCreation();
    }


    public void proteinCreation() {
        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                //get stuff from the parent
                Vector<String> lProteinAccessions = iParent.getProteinAccessions();
                Vector<String> lRatioTypesList = iParent.getNewRatioTypes();
                String[] lRatioTypes = new String[lRatioTypesList.size()];
                lRatioTypesList.toArray(lRatioTypes);
                Vector<String> lComponentTypesList = iParent.getNewComponentsType();
                String[] lComponentTypes = new String[lComponentTypesList.size()];
                lComponentTypesList.toArray(lComponentTypes);
                Vector<RatioGroupCollection> lCollections = iParent.getCollections();
                Vector<QuantitativeProtein> lQuantProtein = new Vector<QuantitativeProtein>();
                progressBar.setIndeterminate(true);
                progressBar.setStringPainted(true);
                progressBar.setString("Creating proteins ... ");


                //1. create all the distiller proteins
                for (int i = 0; i < lProteinAccessions.size(); i++) {
                    lQuantProtein.add(new QuantitativeProtein(lProteinAccessions.get(i), lRatioTypes));
                }

                //9. couple the distiller ratio groups to the distiller proteins
                for (int i = 0; i < lCollections.size(); i++) {

                    for (int j = 0; j < lCollections.get(i).size(); j++) {
                        //get the ratio group
                        RatioGroup lRatioGroup = lCollections.get(i).get(j);
                        //get all the protein accession linked to this ratiogroup
                        String[] lAccessions = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessions.length; k++) {
                            for (int l = 0; l < lQuantProtein.size(); l++) {
                                if (lAccessions[k].equalsIgnoreCase(lQuantProtein.get(l).getAccession())) {
                                    //add the ratio group to the protein if the accession is the same
                                    lQuantProtein.get(l).addRatioGroup(lRatioGroup);
                                }
                            }
                        }
                    }

                    //ToDo delete me

                    //i = i + 40;

                    //ToDo delete me
                }
                progressBar.setString("Calculating razor peptides ... ");
                calculateRazorPeptides(lQuantProtein);
                iQuantitativeValidationSingelton.setAllProteins(lQuantProtein);

                progressBar.setString("Doing location and scale normalization ... ");
                doNormalization(lQuantProtein, lRatioTypesList);

                progressBar.setString("Creating reference set ... ");
                //10. create a reference set with the "household" proteins with the most ratiogroups
                ReferenceSet lReferenceSet = new ReferenceSet(new ArrayList<QuantitativeProtein>(), lRatioTypes, lComponentTypes);
                //sort by the ratio group numbers
                Collections.sort(lQuantProtein, new QuantitativeProteinSorterByRatioGroupNumbers());
                //get the reference set size from the singelton
                int lReferenceSetSize = iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet();
                if (iQuantitativeValidationSingelton.getUseAllProteinsForReferenceSet()) {
                    lReferenceSetSize = lQuantProtein.size();
                }
                if (lReferenceSetSize > lQuantProtein.size()) {
                    lReferenceSetSize = lQuantProtein.size();
                }
                for (int i = 0; i < lReferenceSetSize; i++) {
                    lReferenceSet.addReferenceProtein(lQuantProtein.get(i));
                }
                //set the refernce set
                iQuantitativeValidationSingelton.setReferenceSet(lReferenceSet);
                //lReferenceSet.calculateStatisticsByRandomSampling();

                //sort by the protein accession
                Collections.sort(lQuantProtein, new QuantitativeProteinSorterByAccession());
                progressBar.setIndeterminate(false);

                downloadProteinSequences(lQuantProtein);

                //_____Do garbage collection______
                System.gc();
                iParent.setQuantitativeProtein(lQuantProtein);

                progressBar.setIndeterminate(true);
                progressBar.setStringPainted(true);
                

                //_____Do garbage collection______
                System.gc();

                //show gui
                JOptionPane.showMessageDialog(iParent, "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                iQuantitativeValidationSingelton.setAllProteins(iParent.getQuantitativeProtein());
                QuantitationValidationGUI gui = new QuantitationValidationGUI(iParent.getQuantitativeProtein(), iParent.getMs_limsConnection(), iParent.isStandAlone());
                gui.setVisible(true);

                return true;
            }

            public void finished() {
                iParent.closeFrame();
            }

        };
        lStarter.start();
    }


    public void doNormalization(Vector<QuantitativeProtein> lProteins, Vector<String> lTypes) {
        iQuantitativeValidationSingelton.setNormalization(true);
        for (int i = 0; i < lTypes.size(); i++) {

            String lUnregulatedComponent = null;


            double lStDevOld = Double.MAX_VALUE;
            double lCoefVarOld = Double.MAX_VALUE;
            double lStDevNew = 0.0;
            double lCoefVarNew = 0.0;
            double lCoefVarDiff = 1.0;
            int lCycles = 0;
            Vector<Vector<Double>> lAllCalculatedMADs = new Vector<Vector<Double>>();

            //get the ratios
            Vector<Vector<Ratio>> lValidUniqueRatios = new Vector<Vector<Ratio>>();
            Vector<Vector<Ratio>> lAllRatios = new Vector<Vector<Ratio>>();
            Vector<Vector<Boolean>> lAllRatiosTakenForNormalizationVector = new Vector<Vector<Boolean>>();
            for (int l = 0; l < iParent.getRoverSources().size(); l++) {
                Vector<RatioType> lRatioTypes = iQuantitativeValidationSingelton.getMatchedRatioTypes();
                String lRatioType = "";
                for (int j = 0; j < lRatioTypes.size(); j++) {
                    if (lRatioTypes.get(j).getType().equalsIgnoreCase(lTypes.get(i))) {
                        lUnregulatedComponent = lRatioTypes.get(j).getUnregulatedComponentsBySet().get(l);
                        lRatioType = lRatioTypes.get(j).getType();
                    }
                }
                //get the ratios
                Vector<Ratio> lValidUniqueRatiosForSource = new Vector<Ratio>();
                Vector<Ratio> lAllRatiosForSource = new Vector<Ratio>();
                Vector<Boolean> lAllRatiosTakenForNormalizationVectorForSource = new Vector<Boolean>();

                lAllRatios.add(lAllRatiosForSource);
                lAllRatiosTakenForNormalizationVector.add(lAllRatiosTakenForNormalizationVectorForSource);
                lValidUniqueRatios.add(lValidUniqueRatiosForSource);

                for (int j = 0; j < lProteins.size(); j++) {
                    for (int k = 0; k < lProteins.get(j).getRatioGroups().size(); k++) {
                        Ratio lRatio = lProteins.get(j).getRatioGroups().get(k).getRatioByType(lTypes.get(i));
                        if (lRatio != null) {
                            if (lRatio.getParentRatioGroup().getParentCollection().getIndex() == l) {
                                if (!lAllRatios.get(l).contains(lRatio)) {
                                    lAllRatiosForSource.add(lRatio);
                                    lAllRatiosTakenForNormalizationVectorForSource.add(false);
                                }
                                if (lRatio.getValid()) {
                                    if (!lValidUniqueRatios.get(l).contains(lRatio)) {
                                        lValidUniqueRatiosForSource.add(lRatio);
                                    }
                                }
                            }
                        }
                    }
                }


                //sort the ratios by intesities
                RatioSorterByIntensity lSorter = new RatioSorterByIntensity(lUnregulatedComponent, lRatioType);
                Collections.sort(lAllRatiosForSource, lSorter);
                Collections.sort(lValidUniqueRatiosForSource, lSorter);
            }
            try {

                //Calendar now = Calendar.getInstance();
                //PrintWriter out = new PrintWriter(new FileWriter("C:\\" + "multiRoverOut" + now.getTimeInMillis() + ".csv"));
                //out.println("Normalization cycles\tOld MAD Sd\tOld MAD mean\tOld Coef of var\tNew MAD SD\tNew MAD mean\tNew coef of var");


                while (lCoefVarDiff > 0.0005) {
                    //while (lCycles < 20) {


                    //Create the holders for the divided ratio vectors
                    Vector<Vector<Ratio>> lDividedValidUniqueRatios = new Vector<Vector<Ratio>>();
                    Vector<Vector<Ratio>> lDividedValidUniqueSlidingWindowRatios = new Vector<Vector<Ratio>>();
                    Vector<Double> lDividedValidUniqueMADS = new Vector<Double>();
                    Vector<Double> lScalingFactor = new Vector<Double>();
                    Vector<Vector<Ratio>> lDividedAllRatios = new Vector<Vector<Ratio>>();
                    DescriptiveStatistics lOldMADs = new DescriptiveStatistics();
                    DescriptiveStatistics lNewMADs = new DescriptiveStatistics();
                    Vector<Double> lIntensities = new Vector<Double>();


                    for (int r = 0; r < iParent.getRoverSources().size(); r++) {
                        Vector<RatioType> lRatioTypes = iQuantitativeValidationSingelton.getMatchedRatioTypes();
                        String lRatioTypeTitle = "";
                        RatioType lRatioType = null;
                        for (int j = 0; j < lRatioTypes.size(); j++) {
                            if (lRatioTypes.get(j).getType().equalsIgnoreCase(lTypes.get(i))) {
                                lUnregulatedComponent = lRatioTypes.get(j).getUnregulatedComponentsBySet().get(r);
                                lRatioTypeTitle = lRatioTypes.get(j).getType();
                                lRatioType = lRatioTypes.get(j);
                            }
                        }
                        for (int p = 0; p < lAllRatiosTakenForNormalizationVector.get(r).size(); p++) {
                            lAllRatiosTakenForNormalizationVector.get(r).set(p, false);
                        }


                        if (lCycles == 0) {
                            //only do a normalization if its the first cycle
                            //calculate log 2 median
                            double[] lRatios = new double[lValidUniqueRatios.get(r).size()];
                            for (int m = 0; m < lValidUniqueRatios.get(r).size(); m++) {
                                lRatios[m] = lValidUniqueRatios.get(r).get(m).getRatio(true);
                            }
                            double lMedian = BasicStats.median(lRatios, false);
                            //calculate the wanted log 2 median
                            double lWantedMedian = lRatioType.getMedian();
                            lWantedMedian = Math.log(lWantedMedian) / Math.log(2);

                            //use this median to correct every ratio
                            for (int m = 0; m < lAllRatios.get(r).size(); m++) {
                                Ratio lRatio = lAllRatios.get(r).get(m);
                                double lRatioValue = lRatio.getRatio(true);
                                lRatioValue = lRatioValue + (lWantedMedian - lMedian);
                                lRatio.setRecalculatedRatio(lRatioValue);
                            }

                        }


                        //calculate the size of each of the 20 groups
                        int lHalfGroupSize = 200;
                        if (lCycles != 0) {
                            lHalfGroupSize = (int) (lHalfGroupSize * ((lCycles) / 0.6));
                        }
                        int lJumpFactor = 50;
                        int lGroupSize = lHalfGroupSize * 2 + lJumpFactor;
                        int lNumberOfGroups = (int) (lValidUniqueRatios.get(r).size() / Double.valueOf(lJumpFactor));
                        int lLastIndexAdded = -1;
                        int lMaximumIndexToAdd = 0;

                        //divide the ratios in to groups
                        double lUpperLast = Double.MIN_VALUE;
                        for (int j = 0; j < lNumberOfGroups; j++) {
                            Vector<Ratio> lTempRatiosSlidingGroupUnique = new Vector<Ratio>();
                            Vector<Ratio> lTempRatiosSmallGroupUnique = new Vector<Ratio>();
                            Vector<Ratio> lTempRatiosAll = new Vector<Ratio>();
                            for (int k = 0; k < lGroupSize; k++) {
                                int lIndex = k + (j * lJumpFactor) - ((lGroupSize - lJumpFactor) / 2);
                                if (lIndex >= 0 && lIndex < lValidUniqueRatios.get(r).size()) {
                                    lTempRatiosSlidingGroupUnique.add(lValidUniqueRatios.get(r).get(lIndex));
                                }
                                if (k >= ((lGroupSize - lJumpFactor) / 2) && k < ((lGroupSize - lJumpFactor) / 2) + lJumpFactor) {
                                    lTempRatiosSmallGroupUnique.add(lValidUniqueRatios.get(r).get(lIndex));
                                    lTempRatiosAll.add(lValidUniqueRatios.get(r).get(lIndex));
                                    lAllRatiosTakenForNormalizationVector.get(r).set(lIndex, true);
                                    lMaximumIndexToAdd = lAllRatios.indexOf(lValidUniqueRatios.get(r).get(lIndex));
                                }
                            }
                            //we sorted the valid unique ratio, now we want to use the lower and upper intensities to create a subset of all ratios
                            Ratio o2 = lTempRatiosSmallGroupUnique.get(lTempRatiosSmallGroupUnique.size() - 1);

                            double lUpper = o2.getParentRatioGroup().getIntensityForComponent(lUnregulatedComponent);
                            if (lUpper == 0.0) {
                                lUpper = o2.getParentRatioGroup().getSummedIntensityForRatioType(lRatioTypeTitle);
                            }
                            lIntensities.add(lUpper);
                            boolean lAddingStarted = false;
                            if (j == lNumberOfGroups - 1) {
                                lUpper = Double.MAX_VALUE;
                                lMaximumIndexToAdd = lAllRatios.get(r).size() - 1;
                            }
                            for (int k = lLastIndexAdded + 1; k < lMaximumIndexToAdd; k++) {
                                if (!lAllRatiosTakenForNormalizationVector.get(r).get(k)) {
                                    double lInt = lAllRatios.get(r).get(k).getParentRatioGroup().getIntensityForComponent(lUnregulatedComponent);
                                    if (lInt == 0.0) {
                                        lInt = lAllRatios.get(r).get(k).getParentRatioGroup().getSummedIntensityForRatioType(lRatioTypeTitle);
                                    }
                                    if (lUpperLast <= lInt && lInt < lUpper) {
                                        if (!lTempRatiosAll.contains(lAllRatios.get(r).get(k))) {
                                            //it's not yet added anywhere
                                            lTempRatiosAll.add(lAllRatios.get(r).get(k));
                                            lAllRatiosTakenForNormalizationVector.get(r).set(k, true);
                                            lAddingStarted = true;
                                            lLastIndexAdded = k;
                                        }
                                    } else {
                                        //if we already added some things and now not anymore, we can stop this loop
                                        if (lAddingStarted) {
                                            k = lAllRatios.get(r).size();
                                        }
                                    }
                                }
                            }
                            lUpperLast = lUpper;
                            lLastIndexAdded = lAllRatios.indexOf(lTempRatiosAll.get(lTempRatiosAll.size() - 1));

                            lDividedAllRatios.add(lTempRatiosAll);
                            lDividedValidUniqueRatios.add(lTempRatiosSmallGroupUnique);
                            lDividedValidUniqueSlidingWindowRatios.add(lTempRatiosSlidingGroupUnique);
                            double lMad = calculateMAD(lTempRatiosSlidingGroupUnique);
                            lOldMADs.addValue(lMad);
                            lDividedValidUniqueMADS.add(lMad);
                        }
                    }

                    if (lCycles == 0) {
                        lAllCalculatedMADs.add(lIntensities);
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
                    for (int j = 0; j < lScalingFactor.size(); j++) {
                        double lNewMAD = 0.0;
                        lNewMAD = calculateMAD(lDividedValidUniqueSlidingWindowRatios.get(j));
                        lNewMADs.addValue(lNewMAD);
                        lTempMADs.add(lNewMAD);
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

                    
                    //out.println("|   " + (lCycles + 1) + "   |   " + lStDevOld + "   |   " + lOldMADs.getMean() + "   |   " + lCoefVarOld + "   |   " + lStDevNew + "   |   " + lNewMADs.getMean() + "   |   " + lCoefVarNew + "   |");

                    lAllCalculatedMADs.add(lTempMADs);

                    lCycles = lCycles + 1;
                }

                /*
                for (int y = 0; y < lAllCalculatedMADs.get(0).size(); y++) {
                    //System.out.print("\n");
                    out.print("\n");
                    for (int x = 0; x < lAllCalculatedMADs.size(); x++) {
                        //System.out.print(lAllCalculatedMADs.get(x).get(y) + ",");
                        out.print(lAllCalculatedMADs.get(x).get(y) + ",");
                    }
                }
                out.flush();
                out.close();      */
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public double calculateMAD(Vector<Ratio> lRatios) {
        double[] lRatioDoubles = new double[lRatios.size()];
        for (int i = 0; i < lRatios.size(); i++) {
            lRatioDoubles[i] = lRatios.get(i).getRatio(true);
        }
        return BasicStats.mad(lRatioDoubles, false);
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

    /**
     * This method will download all the protein sequences for the given proteins
     *
     * @param aProteins
     */
    public void downloadProteinSequences(Vector<QuantitativeProtein> aProteins) {
        progressBar.setString("Downloading protein sequences");
        progressBar.setStringPainted(true);
        progressBar.setMaximum(aProteins.size());
        progressBar.setIndeterminate(false);
        Vector<QuantitativeProtein> lProteins = (Vector<QuantitativeProtein>) aProteins.clone();
        if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.LOCAL)) {
            progressBar.setString("Finding protein sequences in local FASTA file");
            //find the sequences in the fasta file
            try {
                //create the reader
                FileReader fDbReader = new FileReader(iQuantitativeValidationSingelton.getFastaDatabaseLocation());
                //create the line reader
                LineNumberReader lnreader = new LineNumberReader(fDbReader);
                //the current line
                String lLine = "";
                //create the header and the sequence strings
                String lHeader = "";
                String lSequence = "";
                int lCounter = 0;

                while ((lLine = lnreader.readLine()) != null) {
                    lCounter = lCounter + 1;
                    if (lLine.startsWith(">")) {
                        //find the previous one
                        if (lHeader.length() != 0 && lSequence.length() != 0) {
                            for (int i = 0; i < lProteins.size(); i++) {
                                if (lHeader.indexOf(lProteins.get(i).getAccession()) >= 0) {
                                    progressBar.setValue(progressBar.getValue() + 1);
                                    QuantitativeProtein lProtein = lProteins.get(i);
                                    lProtein.setSequence(lSequence);
                                    lProtein.setSequenceLength(lSequence.length());
                                    lProtein.getPeptideGroups(true);
                                    lProteins.remove(lProtein);
                                    i = lProteins.size();
                                }
                            }
                        }
                        lSequence = "";
                        lHeader = lLine;
                    } else if (!lLine.equalsIgnoreCase("\n")) {
                        lSequence = lSequence + lLine.replace("\n", "");
                    }
                }


            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

        } else {

            for (int i = 0; i < lProteins.size(); i++) {
                progressBar.setValue(progressBar.getValue() + 1);
                QuantitativeProtein lProtein = lProteins.get(i);

                try {
                    if (lProtein.getSequence() == null) {
                        if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.UNIPROT)) {
                            lProtein.setSequence((new UniprotSequenceRetriever(lProtein.getAccession())).getSequence());
                        } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.IPI)) {
                            lProtein.setSequence((new IpiSequenceRetriever(lProtein.getAccession())).getSequence());
                        } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.NCBI)) {
                            lProtein.setSequence((new NcbiSequenceRetriever(lProtein.getAccession())).getSequence());
                        } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.TAIR)) {
                            lProtein.setSequence((new TairSequenceRetriever(lProtein.getAccession())).getSequence());
                        }
                    }
                    if (lProtein.getSequence() != null && lProtein.getSequence().length() > 0) {
                        lProtein.getPeptideGroups(true);
                        lProtein.setSequenceLength(lProtein.getSequence().length());
                        iQuantitativeValidationSingelton.addProteinSequence(lProtein.getAccession(), lProtein.getSequence());
                        lProtein.setSequence("");
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }


    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        jpanContent = new JPanel();
        jpanContent.setLayout(new GridBagLayout());
        progressBar = new JProgressBar();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(progressBar, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        jpanContent.add(spacer1, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }
}
