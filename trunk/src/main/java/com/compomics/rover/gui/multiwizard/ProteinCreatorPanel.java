package com.compomics.rover.gui.multiwizard;

import com.compomics.rover.general.interfaces.WizardPanel;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.quantitation.*;
import com.compomics.rover.general.quantitation.sorters.QuantitativeProteinSorterByRatioGroupNumbers;
import com.compomics.rover.general.quantitation.sorters.QuantitativeProteinSorterByAccession;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.sequenceretriever.UniprotSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.IpiSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.NcbiSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.TairSequenceRetriever;
import com.compomics.rover.gui.QuantitationValidationGUI;
import com.compomics.util.sun.SwingWorker;
import be.proteomics.statlib.descriptive.BasicStats;

import javax.swing.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 11-Dec-2009
 * Time: 14:07:38
 * To change this template use File | Settings | File Templates.
 */
public class ProteinCreatorPanel implements WizardPanel {
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
                Vector<String> lComponentTypesList = iParent.getNewRatioTypes();
                String[] lComponentTypes = new String[lComponentTypesList.size()];
                lComponentTypesList.toArray(lComponentTypes);
                Vector<RatioGroupCollection> lCollections = iParent.getCollections();
                Vector<QuantitativeProtein> lQuantProtein = new Vector<QuantitativeProtein>();
                progressBar.setIndeterminate(true);
                progressBar.setStringPainted(true);
                progressBar.setString("Creating proteins and reference set ... ");


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

                calculateRazorPeptides(lQuantProtein);

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


                //sort by the protein accession
                Collections.sort(lQuantProtein, new QuantitativeProteinSorterByAccession());
                progressBar.setIndeterminate(false);
                downloadProteinSequences(lQuantProtein);

                //_____Do garbage collection______
                System.gc();
                iParent.setQuantitativeProtein(lQuantProtein);

                progressBar.setIndeterminate(true);
                progressBar.setStringPainted(true);
                progressBar.setString("Doing statistics ... ");

                //find for every data set and for every ratio type the stdev and mean
                double[][] lStDevs = new double[iParent.getRoverSources().size()][lRatioTypes.length];
                double[][] lMeans = new double[iParent.getRoverSources().size()][lRatioTypes.length];
                for (int l = 0; l < iParent.getRoverSources().size(); l++) {
                    for (int m = 0; m < lRatioTypes.length; m++) {
                        HashMap<String, Double> lResult = huberStatistics(lQuantProtein, lRatioTypes[m], l);
                        lStDevs[l][m] = lResult.get("stdev");
                        lMeans[l][m] = lResult.get("mean");
                        System.out.println("Set " + (l + 1) + " type: " + lRatioTypes[m] + " StDev: " + lStDevs[l][m] + " Mean: " + lMeans[l][m]);
                    }
                }

                //now calculate the average SD and mean for every ratio type
                double[] lAverageStDevs = new double[lRatioTypes.length];
                double[] lAverageMeans = new double[lRatioTypes.length];

                for (int m = 0; m < lRatioTypes.length; m++) {
                    double lMean = 0.0;
                    double lSD = 0.0;
                    for (int l = 0; l < iParent.getRoverSources().size(); l++) {
                        lMean = lMean + lMeans[l][m];
                        lSD = lSD + lStDevs[l][m];
                    }

                    lAverageMeans[m] = lMean / (double) iParent.getRoverSources().size();
                    lAverageStDevs[m] = lSD / (double) iParent.getRoverSources().size();
                    System.out.println(lRatioTypes[m] + " average StDev: " + lAverageStDevs[m] + " average mean: " + lAverageMeans[m]);
                }


                //now recalculate the ratios
                for (int i = 0; i < lCollections.size(); i++) {

                    for (int j = 0; j < lCollections.get(i).size(); j++) {
                        //get the ratio group
                        RatioGroup lRatioGroup = lCollections.get(i).get(j);
                        for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                            Ratio lRatio = lRatioGroup.getRatio(k);
                            String lRatioType = lRatio.getType();
                            for (int m = 0; m < lRatioTypes.length; m++) {
                                if (lRatioTypes[m].equalsIgnoreCase(lRatioType)) {
                                    //change the ratio
                                    double lTempRatio = lRatio.getRatio(true);
                                    int lIndex = lCollections.get(i).getIndex();
                                    double lZscore = (lTempRatio - lMeans[lIndex][m]) / lStDevs[lIndex][m];
                                    //reset the SD
                                    lTempRatio = lAverageMeans[m] + (lAverageStDevs[m] * lZscore);
                                    //System.out.println(lRatio.getRatio(true) +  " z: " + lZscore + " aveM: " + lAverageMeans[m] + " aveSD: " + lAverageStDevs[m] + " => " + lTempRatio);
                                    lRatio.setRecalculatedRatio(lTempRatio);
                                    //System.out.println(lRatio.getRatio(true) + " " + lRatio.getRatio(false));
                                }
                            }

                        }

                    }
                }

                //_____Do garbage collection______
                System.gc();

                //show gui
                JOptionPane.showMessageDialog(iParent, "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                iQuantitativeValidationSingelton.setAllProteins(iParent.getQuantitativeProtein());
                QuantitationValidationGUI gui = new QuantitationValidationGUI(iParent.getQuantitativeProtein(), null, iParent.isStandAlone());
                gui.setVisible(true);

                return true;
            }

            public void finished() {
                iParent.closeFrame();
            }

        };
        lStarter.start();
    }


    /**
     * This method will do the huber statistics for this reference set an for every ratio type
     */
    public HashMap<String, Double> huberStatistics(Vector<QuantitativeProtein> iReferenceProteins, String lType, int lIndex) {

        int iUsedRatios = 0;

        // Okay, do the stats on the log2 ratios.

        Vector<Double> lLog2Ratios = new Vector<Double>();
        //we will first look for the ratios where we want to do statistics on
        for (int i = 0; i < iReferenceProteins.size(); i++) {
            Vector<RatioGroup> lRatioGroups = iReferenceProteins.get(i).getRatioGroups();
            for (int j = 0; j < lRatioGroups.size(); j++) {
                if (lRatioGroups.get(j).getParentCollection().getIndex() == lIndex) {
                    if (iQuantitativeValidationSingelton.isRatioValidInReferenceSet()) {
                        //check if the ratio is valid
                        Ratio lRatio = lRatioGroups.get(j).getRatioByType(lType);
                        if (lRatio != null) {
                            if (lRatio.getValid()) {
                                if (!Double.isNaN(lRatio.getRatio(true)) && !Double.isInfinite(lRatio.getRatio(true))) {
                                    lLog2Ratios.add(lRatio.getRatio(true));
                                    iUsedRatios = iUsedRatios + 1;
                                }
                            }
                        }
                    } else {
                        Ratio lRatio = lRatioGroups.get(j).getRatioByType(lType);
                        if (lRatio != null) {
                            if (!Double.isNaN(lRatio.getRatio(true)) && !Double.isInfinite(lRatio.getRatio(true))) {
                                lLog2Ratios.add(lRatio.getRatio(true));
                                iUsedRatios = iUsedRatios + 1;
                            }
                        }
                    }
                }
            }
        }
        //we have the ratios to do the statistics on
        double[] log2Ratios = new double[lLog2Ratios.size()];
        for (int i = 0; i < lLog2Ratios.size(); i++) {
            log2Ratios[i] = lLog2Ratios.get(i);
        }
        //do the statistics
        double[] estimators = BasicStats.hubers(log2Ratios, 1e-06, false);

        HashMap<String, Double> lResult = new HashMap();
        lResult.put("mean", estimators[0]);
        lResult.put("stdev", estimators[1]);
        lResult.put("iterations", estimators[2]);
        return lResult;
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
        for (int i = 0; i < aProteins.size(); i++) {
            progressBar.setValue(progressBar.getValue() + 1);
            QuantitativeProtein lProtein = aProteins.get(i);

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
                    iQuantitativeValidationSingelton.addProteinSequence(lProtein.getAccession(), lProtein.getSequence());
                }
            } catch (Exception e) {
                //sequence not found
                //e.printStackTrace();
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
