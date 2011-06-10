package com.compomics.rover.gui;

import org.apache.log4j.Logger;



import com.compomics.rover.general.quantitation.QuantitativeProtein;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.ReferenceSet;
import com.compomics.rover.general.quantitation.QuantitativePeptideGroup;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatioGroup;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatioGroup;
import com.compomics.rover.general.singelton.Log;
import com.compomics.rover.general.PeptideIdentification.DefaultPeptideIdentification;
import com.compomics.rover.general.PeptideIdentification.DatfilePeptideIdentification;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.util.sun.*;

import javax.swing.*;
import com.compomics.util.sun.SwingWorker;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 21-dec-2008
 * Time: 11:57:59
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class creates a JFrame where different filter are shown. This filters can filter the distiller proteins with quantitave information
 */
public class FilterFrame extends JFrame {
    // Class specific log4j logger for FilterFrame instances.
    private static Logger logger = Logger.getLogger(FilterFrame.class);
    //gui
    private JPanel contentPane;
    private JButton filterButton;
    private JCheckBox chbNumberOfAllIdentifications;
    private JCheckBox chbNumberOfDifferentPeptides;
    private JSpinner spinIdentifications;
    private JSpinner spinDifferentPeptides;
    private JCheckBox chbHasNonValidRatio;
    private JCheckBox chbProteinMeanGreater;
    private JComboBox cmbProteinMeanRatioTypesLarger;
    private JSpinner spinProteinMeanLarger;
    private JCheckBox chbProteinMeanLower;
    private JCheckBox chbUseSingles;
    private JCheckBox chbAccessionSearch;
    private JTextField txtProteinAccession;
    private JCheckBox chbRatioComment;
    private JCheckBox chbDiffRatioAndMean;
    private JSpinner spinnPeptideRatioProteinMeanDiff;
    private JSpinner spinProteinMeanSmaller;
    private JComboBox cmbProteinMeanRatioTypesSmaller;
    private JCheckBox chbPeptideRatioLarger;
    private JCheckBox chbPeptideRatioSmaller;
    private JComboBox cmbPeptideRatioTypesLarger;
    private JComboBox cmbPeptideRatioTypesSmaller;
    private JSpinner spinPeptideRatioLarger;
    private JSpinner spinPeptideRatioSmaller;
    private JComboBox cmbSingleComponentsTarget;
    private JSpinner spinSingle;
    private JComboBox cmbSingleComponent;
    private JCheckBox chbValidated;
    private JLabel jLabelSingle1;
    private JLabel jLabelSingle2;
    private JLabel jLabelSingle3;
    private JCheckBox chbOnlyTrue;
    private JCheckBox chbHuberSignificanceHigher;
    private JSpinner spinHuberSignificanceHigher;
    private JCheckBox chbHuberSignificanceLower;
    private JSpinner spinHuberSignificanceLower;
    private JCheckBox chbProteinComment;
    private JCheckBox chbUnique;
    private JCheckBox chbPeptidesSequence;
    private JTextField txtPeptideSequence;
    private JTextField txtPeptideEnding;
    private JCheckBox chbPeptideEnding;
    private JCheckBox chbPeptideStarting;
    private JTextField txtPeptideStarting;
    private JCheckBox chbPeptideAfter;
    private JTextField txtPeptideAfter;
    private JCheckBox chbPeptideBefore;
    private JTextField txtPeptideBefore;
    private JCheckBox chbNterm;
    private JTextField txtNterm;
    private JCheckBox chbDiffUniqueRazor;
    private JSpinner spinDiffUniquePeptides;
    private JLabel lblMulti1;
    private JLabel lblMulti2;
    private JProgressBar progressBar1;
    private JCheckBox chbExcludeYellow;
    private JComboBox cmbProteinMeanRatioTypesRange1;
    private JCheckBox chbProteinMeanInRange;
    private JSpinner spinMeanInRange;
    private JComboBox cmbProteinMeanRatioTypesRange2;

    /**
     * All the proteins that will be filtered
     */
    private QuantitativeProtein[] iProteinToFilter;
    /**
     * The reference set
     */
    private ReferenceSet iReferenceSet;
    /**
     * The filtered proteins
     */
    private QuantitativeProtein[] iFilteredProteins;
    /**
     * The DistillerQuantitationGUI parent
     */
    private QuantitationValidationGUI iParent;
    /**
     * The different ratio types (ex. L/H , M/H , ...)
     */
    private String[] iTypes;
    /**
     * This distiller validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
    /**
     * The different components (light, heavy, ...)
     */
    private String[] iComponents;
    /**
     * The log
     */
    private Log iLog = Log.getInstance();

    /**
     * The constructor
     *
     * @param aProteinsToFilter All the quantitative proteins
     * @param aReferenceSet     The reference set
     * @param aParent           the QuantitationValidationGUI Parent
     * @param aTypes            The different ratio types
     * @param aComponents       The different ratio components
     */
    public FilterFrame(QuantitativeProtein[] aProteinsToFilter, ReferenceSet aReferenceSet, QuantitationValidationGUI aParent, String[] aTypes, String[] aComponents) {
        super("Create a filter");
        this.iProteinToFilter = aProteinsToFilter;
        this.iReferenceSet = aReferenceSet;
        this.iParent = aParent;
        this.iTypes = aTypes;
        this.iComponents = aComponents;

        //create UI
        $$$setupUI$$$();
        progressBar1.setVisible(false);
        if (iQuantitativeValidationSingelton.getRoverSources().size() > 1) {
            //it's a multi source program
            //warn the user
            lblMulti1.setVisible(true);
            lblMulti2.setVisible(true);
        } else {
            lblMulti1.setVisible(false);
            lblMulti2.setVisible(false);
        }

        //action listener
        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                filterProteins();
            }
        });

        if (!iQuantitativeValidationSingelton.isDistillerQuantitation() && !iQuantitativeValidationSingelton.isMaxQuantQuantitation()) {
            //distiller and masquant mode is off
            //no intensity information
            //no single detection
            chbUseSingles.setVisible(false);
            jLabelSingle2.setVisible(false);
            jLabelSingle3.setVisible(false);
            spinSingle.setVisible(false);
            cmbSingleComponent.setVisible(false);
            cmbSingleComponentsTarget.setVisible(false);
        }
        if (!iQuantitativeValidationSingelton.isDistillerQuantitation()) {
            //distiller mode is off
            //no good modified sequences
            chbNterm.setVisible(false);
            txtNterm.setVisible(false);
        }

        //set JFrame parameters
        this.setContentPane(contentPane);
        this.setSize(800, 750);
        this.setVisible(true);
    }

    /**
     * Method the get the filtered proteins
     *
     * @return QuantitativeProtein[] The filtered proteins
     */
    public QuantitativeProtein[] getFilteredProteins() {
        return iFilteredProteins;
    }

    /**
     * This method will filter the proteins
     */
    public void filterProteins() {

        progressBar1.setVisible(true);
        progressBar1.setMaximum(iProteinToFilter.length);
        progressBar1.setValue(0);

        //vector to store the filtered proteins in
        final Vector<QuantitativeProtein> lFiltered = new Vector<QuantitativeProtein>();

        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                boolean useOnlyTrueRatios = chbOnlyTrue.isSelected();
                boolean useUniquePeptide = chbUnique.isSelected();
                boolean useNumberOfAllIdentifications = chbNumberOfAllIdentifications.isSelected();
                boolean useNumberOfDifferentPeptides = chbNumberOfDifferentPeptides.isSelected();
                boolean useNumberOfDifferentUniquePeptides = chbDiffUniqueRazor.isSelected();
                boolean useNonValidRatios = chbHasNonValidRatio.isSelected();
                boolean useExcludeYellow = chbExcludeYellow.isSelected();
                boolean useProteinMeanGreater = chbProteinMeanGreater.isSelected();
                boolean useProteinMeanLower = chbProteinMeanLower.isSelected();
                boolean usePeptideRatioGreater = chbPeptideRatioLarger.isSelected();
                boolean usePeptideRatioLower = chbPeptideRatioSmaller.isSelected();
                boolean useProteinInRange = chbProteinMeanInRange.isSelected();
                boolean useSingles = chbUseSingles.isSelected();
                boolean accessionSearch = chbAccessionSearch.isSelected();
                boolean useRatioComments = chbRatioComment.isSelected();
                boolean usePeptideSequence = chbPeptidesSequence.isSelected();
                boolean usePeptideEnding = chbPeptideEnding.isSelected();
                boolean usePeptideStarting = chbPeptideStarting.isSelected();
                boolean usePeptideAfter = chbPeptideAfter.isSelected();
                boolean usePeptideBefore = chbPeptideBefore.isSelected();
                boolean useNterminalModification = chbNterm.isSelected();
                boolean usePeptideRatioProteinMeanDiff = chbDiffRatioAndMean.isSelected();
                boolean useValidatedProteins = chbValidated.isSelected();
                boolean useHuberSignificanceHigher = chbHuberSignificanceHigher.isSelected();
                boolean useHuberSignificanceLower = chbHuberSignificanceLower.isSelected();
                boolean useProteinComment = chbProteinComment.isSelected();


                //check every protein
                for (int i = 0; i < iProteinToFilter.length; i++) {
                    progressBar1.setValue(i + 1);
                    boolean firstFilter = true;
                    boolean useThisProtein = false;
                    //the protein
                    QuantitativeProtein lProtein = iProteinToFilter[i];


                    //deselect all ratio groups for the not selected proteins
                    for (int j = 0; j < lProtein.getRatioGroups().size(); j++) {
                        if (!lProtein.getSelected()) {
                            lProtein.getRatioGroups().get(j).setSelected(false);
                        }
                    }

                    //Use the filter: The protein must have more than ... identifications
                    if (useNumberOfAllIdentifications) {
                        if (useOnlyTrueRatios) {
                            int lCounter = 0;
                            for (int j = 0; j < lProtein.getRatioGroups().size(); j++) {
                                RatioGroup lRatioGroup = lProtein.getRatioGroups().get(j);
                                for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                    Ratio lRatio = lRatioGroup.getRatio(k);
                                    if (useUniquePeptide) {
                                        if (lRatio.getValid() && lRatioGroup.getProteinAccessions().length == 1) {
                                            lCounter = lCounter + 1;
                                            k = lProtein.getTypes().length;
                                        }
                                    } else {
                                        if (lRatio.getValid()) {
                                            lCounter = lCounter + 1;
                                            k = lProtein.getTypes().length;
                                        }
                                    }

                                }
                            }

                            if (lCounter > (Integer) spinIdentifications.getValue()) {
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
                        } else {
                            if (useUniquePeptide) {
                                if (lProtein.getNumberOfUniquePeptides() > (Integer) spinIdentifications.getValue()) {
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
                            } else {
                                if (lProtein.getRatioGroups().size() > (Integer) spinIdentifications.getValue()) {
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

                            if (lProtein.getRatioGroups().size() > (Integer) spinIdentifications.getValue()) {
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

                    //Use the filter: The protein must have more than ... different peptides identified
                    if (useNumberOfDifferentPeptides) {
                        if (useOnlyTrueRatios) {
                            int lCounter = 0;
                            for (int j = 0; j < lProtein.getPeptideGroups(true).size(); j++) {
                                Vector<RatioGroup> lRatioGroups = lProtein.getPeptideGroups(true).get(j).getRatioGroups();
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

                            if (lCounter > (Integer) spinDifferentPeptides.getValue()) {
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
                        } else {
                            if (useUniquePeptide) {
                                if (lProtein.getNumberOfUniquePeptidesGroups() > (Integer) spinDifferentPeptides.getValue()) {
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
                            } else {
                                if (lProtein.getPeptideGroups(true).size() > (Integer) spinDifferentPeptides.getValue()) {
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
                    }


                    //Use the filter: The protein must have more than ... different unique or razor peptides identified
                    if (useNumberOfDifferentUniquePeptides) {
                        if (useOnlyTrueRatios) {
                            int lCounter = 0;
                            for (int j = 0; j < lProtein.getPeptideGroups(true).size(); j++) {
                                Vector<RatioGroup> lRatioGroups = lProtein.getPeptideGroups(true).get(j).getRatioGroups();
                                for (int l = 0; l < lRatioGroups.size(); l++) {
                                    boolean lValidFound = false;
                                    RatioGroup lRatioGroup = lRatioGroups.get(l);
                                    for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                        if (lRatioGroup.getRatio(k).getValid()) {
                                            lValidFound = true;
                                            if (!lProtein.getPeptideGroups(true).get(j).isLinkedToMoreProteins()) {
                                                //it's a unique peptide; do nothing
                                            } else if (lProtein.getAccession().trim().equalsIgnoreCase(lProtein.getPeptideGroups(true).get(j).getRatioGroups().get(0).getRazorProteinAccession().trim())) {
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

                            if (lCounter > (Integer) spinDiffUniquePeptides.getValue()) {
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
                        } else {
                            int lCounter = 0;
                            for (int j = 0; j < lProtein.getPeptideGroups(true).size(); j++) {
                                boolean lValidFound = true;
                                if (!lProtein.getPeptideGroups(true).get(j).isLinkedToMoreProteins()) {
                                    //it's a unique peptide; do nothing
                                    lValidFound = true;
                                } else if (lProtein.getAccession().trim().equalsIgnoreCase(lProtein.getPeptideGroups(true).get(j).getRatioGroups().get(0).getRazorProteinAccession().trim())) {
                                    //it's a razor peptide; do nothing
                                    lValidFound = true;
                                } else {
                                    //it's an isofrom peptide
                                    lValidFound = false;
                                }

                                if (lValidFound) {
                                    lCounter = lCounter + 1;
                                }

                            }

                            if (lCounter > (Integer) spinDiffUniquePeptides.getValue()) {
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


                    //Use the filter: A protein where one of the ratios is invalid will be selected
                    if (useNonValidRatios && !useOnlyTrueRatios) {
                        boolean notValidFilter = false;
                        Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                        for (int j = 0; j < lRatioGroups.size(); j++) {
                            for (int k = 0; k < lRatioGroups.get(j).getNumberOfRatios(); k++) {
                                Ratio lRatio = lRatioGroups.get(j).getRatio(k);
                                if (!lRatio.getValid()) {
                                    if (useUniquePeptide) {
                                        if (lRatioGroups.get(j).getProteinAccessions().length == 1) {
                                            notValidFilter = true;
                                            lRatioGroups.get(j).setSelected(true);
                                        }
                                    } else {
                                        notValidFilter = true;
                                        lRatioGroups.get(j).setSelected(true);
                                    }
                                }
                            }
                        }
                        if (notValidFilter) {
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

                    if (useExcludeYellow) {
                        boolean notValidFilter = false;
                        boolean lYellowFound = false;
                        for (int j = 0; j < lProtein.getPeptideGroups(true).size(); j++) {
                            Vector<RatioGroup> lRatioGroups = lProtein.getPeptideGroups(true).get(j).getRatioGroups();
                            for (int l = 0; l < lRatioGroups.size(); l++) {

                                RatioGroup lRatioGroup = lRatioGroups.get(l);
                                for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                    if (lRatioGroup.getRatio(k).getValid()) {
                                        if (!lProtein.getPeptideGroups(true).get(j).isLinkedToMoreProteins()) {
                                            //it's a unique peptide; do nothing
                                        } else if (lProtein.getAccession().trim().equalsIgnoreCase(lProtein.getPeptideGroups(true).get(j).getRatioGroups().get(0).getRazorProteinAccession().trim())) {
                                            //it's a razor peptide; do nothing
                                        } else {
                                            //it's an isofrom peptide
                                            lYellowFound = true;
                                        }
                                        k = lProtein.getTypes().length;
                                        l = lRatioGroups.size();
                                    }
                                }

                            }
                        }

                        if (!lYellowFound) {
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

                    //Use the filter: A protein will be selected if the mean is greater than ...
                    if (useProteinMeanGreater) {
                        if (lProtein.getProteinRatio((String) cmbProteinMeanRatioTypesLarger.getSelectedItem()) > (Double) spinProteinMeanLarger.getValue()) {
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

                    //Use the filter: A protein will be selected if the mean is lower than ...
                    if (useProteinMeanLower) {
                        if (lProtein.getProteinRatio((String) cmbProteinMeanRatioTypesSmaller.getSelectedItem()) < (Double) spinProteinMeanSmaller.getValue()) {
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

                    //Use the filter: A protein will be selected if the mean is is in range of another ratio ...
                    if (useProteinInRange) {
                        if (Math.abs(lProtein.getProteinRatio((String) cmbProteinMeanRatioTypesRange1.getSelectedItem()) - lProtein.getProteinRatio((String) cmbProteinMeanRatioTypesRange2.getSelectedItem())) < (Double) spinMeanInRange.getValue()) {
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

                    //Use the filter: A protein will be selected if a peptide of that protein has a ratio that is greater than ...
                    if (usePeptideRatioGreater) {
                        boolean lExtremePeptideRatioFound = false;
                        Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                        for (int j = 0; j < lRatioGroups.size(); j++) {
                            for (int k = 0; k < lRatioGroups.get(j).getNumberOfRatios(); k++) {
                                Ratio lRatio = lRatioGroups.get(j).getRatio(k);
                                if (lRatio.getType().equalsIgnoreCase((String) cmbPeptideRatioTypesLarger.getSelectedItem())) {
                                    //the correct ratio
                                    if (useOnlyTrueRatios) {
                                        if (useUniquePeptide) {
                                            if (lRatioGroups.get(j).getProteinAccessions().length == 1) {
                                                if (lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) > (Double) spinPeptideRatioLarger.getValue() && lRatio.getValid()) {
                                                    lExtremePeptideRatioFound = true;
                                                    lRatioGroups.get(j).setSelected(true);
                                                }
                                            }
                                        } else {
                                            if (lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) > (Double) spinPeptideRatioLarger.getValue() && lRatio.getValid()) {
                                                lExtremePeptideRatioFound = true;
                                                lRatioGroups.get(j).setSelected(true);
                                            }
                                        }
                                    } else {
                                        if (useUniquePeptide) {
                                            if (lRatioGroups.get(j).getProteinAccessions().length == 1) {
                                                if (lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) > (Double) spinPeptideRatioLarger.getValue()) {
                                                    lExtremePeptideRatioFound = true;
                                                    lRatioGroups.get(j).setSelected(true);
                                                }
                                            }
                                        } else {
                                            if (lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) > (Double) spinPeptideRatioLarger.getValue()) {
                                                lExtremePeptideRatioFound = true;
                                                lRatioGroups.get(j).setSelected(true);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        if (lExtremePeptideRatioFound) {
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


                    //Use the filter: A protein will be selected if a peptide of that protein has a ratio that is lower than ...
                    if (usePeptideRatioLower) {
                        boolean lExtremePeptideRatioFound = false;
                        Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                        for (int j = 0; j < lRatioGroups.size(); j++) {
                            for (int k = 0; k < lRatioGroups.get(j).getNumberOfRatios(); k++) {
                                Ratio lRatio = lRatioGroups.get(j).getRatio(k);
                                if (lRatio.getType().equalsIgnoreCase((String) cmbPeptideRatioTypesSmaller.getSelectedItem())) {
                                    //the correct ratio
                                    if (useOnlyTrueRatios) {
                                        if (useUniquePeptide) {
                                            if (lRatioGroups.get(j).getProteinAccessions().length == 1) {
                                                if (lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) < (Double) spinPeptideRatioSmaller.getValue() && lRatio.getValid()) {
                                                    lExtremePeptideRatioFound = true;
                                                    lRatioGroups.get(j).setSelected(true);
                                                }
                                            }
                                        } else {
                                            if (lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) < (Double) spinPeptideRatioSmaller.getValue() && lRatio.getValid()) {
                                                lExtremePeptideRatioFound = true;
                                                lRatioGroups.get(j).setSelected(true);
                                            }
                                        }
                                    } else {
                                        if (useUniquePeptide) {
                                            if (lRatioGroups.get(j).getProteinAccessions().length == 1) {
                                                if (lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) < (Double) spinPeptideRatioSmaller.getValue()) {
                                                    lExtremePeptideRatioFound = true;
                                                    lRatioGroups.get(j).setSelected(true);
                                                }
                                            }
                                        } else {
                                            if (lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) < (Double) spinPeptideRatioSmaller.getValue()) {
                                                lExtremePeptideRatioFound = true;
                                                lRatioGroups.get(j).setSelected(true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (lExtremePeptideRatioFound) {
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

                    //Use the filter: A protein will be where one of the ratios is a single.
                    //A ratio is a single if the highest absolute value is greater than 20 times the lowest absolute value

                    //only in distiller or maxquant mode
                    if (useSingles) {
                        boolean single = false;
                        Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                        for (int j = 0; j < lRatioGroups.size(); j++) {
                            if (lRatioGroups.get(j).getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || lRatioGroups.get(j).getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                                //we're in distiller mode
                                DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) lRatioGroups.get(j);
                                double lGreatestIntesity = 0;
                                double lLowestIntesity = 0;
                                if (useUniquePeptide) {
                                    if (lRatioGroup.getProteinAccessions().length == 1) {
                                        for (int k = 0; k < lRatioGroup.getParentCollection().getComponentTypes().size(); k++) {
                                            if (lRatioGroup.getParentCollection().getComponentTypes().get(k).equalsIgnoreCase((String) cmbSingleComponentsTarget.getSelectedItem())) {
                                                lLowestIntesity = lRatioGroup.getAbsoluteIntensities()[k];
                                            }
                                            if (lRatioGroup.getParentCollection().getComponentTypes().get(k).equalsIgnoreCase((String) cmbSingleComponent.getSelectedItem())) {
                                                lGreatestIntesity = lRatioGroup.getAbsoluteIntensities()[k];
                                            }

                                        }
                                    }
                                } else {
                                    for (int k = 0; k < lRatioGroup.getParentCollection().getComponentTypes().size(); k++) {
                                        if (lRatioGroup.getParentCollection().getComponentTypes().get(k).equalsIgnoreCase((String) cmbSingleComponentsTarget.getSelectedItem())) {
                                            lLowestIntesity = lRatioGroup.getAbsoluteIntensities()[k];
                                        }
                                        if (lRatioGroup.getParentCollection().getComponentTypes().get(k).equalsIgnoreCase((String) cmbSingleComponent.getSelectedItem())) {
                                            lGreatestIntesity = lRatioGroup.getAbsoluteIntensities()[k];
                                        }

                                    }

                                }

                                if (lLowestIntesity < lGreatestIntesity * (Double) spinSingle.getValue()) {
                                    single = true;
                                    lRatioGroups.get(j).setSelected(true);
                                }
                            } else {
                                if (lRatioGroups.get(j).getParentCollection().getRoverSource() == RoverSource.MAX_QUANT || lRatioGroups.get(j).getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_NO_SIGN || lRatioGroups.get(j).getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS) {

                                    //we're in maxquant mode
                                    MaxQuantRatioGroup lRatioGroup = (MaxQuantRatioGroup) lRatioGroups.get(j);
                                    double lGreatestIntesity = 0;
                                    double lLowestIntesity = 0;
                                    if (useUniquePeptide) {
                                        if (lRatioGroup.getProteinAccessions().length == 1) {
                                            for (int k = 0; k < lRatioGroup.getParentCollection().getComponentTypes().size(); k++) {
                                                if (lRatioGroup.getParentCollection().getComponentTypes().get(k).equalsIgnoreCase((String) cmbSingleComponentsTarget.getSelectedItem())) {
                                                    lLowestIntesity = lRatioGroup.getAbsoluteIntensities()[k];
                                                }
                                                if (lRatioGroup.getParentCollection().getComponentTypes().get(k).equalsIgnoreCase((String) cmbSingleComponent.getSelectedItem())) {
                                                    lGreatestIntesity = lRatioGroup.getAbsoluteIntensities()[k];
                                                }

                                            }
                                        }
                                    } else {
                                        for (int k = 0; k < lRatioGroup.getParentCollection().getComponentTypes().size(); k++) {
                                            if (lRatioGroup.getParentCollection().getComponentTypes().get(k).equalsIgnoreCase((String) cmbSingleComponentsTarget.getSelectedItem())) {
                                                lLowestIntesity = lRatioGroup.getAbsoluteIntensities()[k];
                                            }
                                            if (lRatioGroup.getParentCollection().getComponentTypes().get(k).equalsIgnoreCase((String) cmbSingleComponent.getSelectedItem())) {
                                                lGreatestIntesity = lRatioGroup.getAbsoluteIntensities()[k];
                                            }

                                        }
                                    }

                                    if (lLowestIntesity < lGreatestIntesity * (Double) spinSingle.getValue()) {
                                        single = true;
                                        lRatioGroups.get(j).setSelected(true);
                                    }
                                }

                            }


                        }
                        if (single) {
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

                    //Use the filter: A protein with a specific accession will be selected
                    if (accessionSearch) {
                        //split the accessions
                        String[] lAccessions = txtProteinAccession.getText().trim().split(",");
                        boolean lFound = false;
                        for (int a = 0; a < lAccessions.length; a++) {
                            if (lProtein.getAccession().equalsIgnoreCase(lAccessions[a])) {
                                lFound = true;
                            }
                        }
                        if (lFound) {
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

                    //Use the filter: A protein with a ratio that has a comment will be selected
                    if (useRatioComments) {
                        boolean commentFound = false;
                        Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                        for (int j = 0; j < lRatioGroups.size(); j++) {
                            for (int k = 0; k < lRatioGroups.get(j).getNumberOfRatios(); k++) {
                                Ratio lRatio = lRatioGroups.get(j).getRatio(k);
                                if (lRatio.getComment() != null) {
                                    commentFound = true;
                                    lRatioGroups.get(j).setSelected(true);
                                }
                                if (useOnlyTrueRatios && !lRatio.getValid() && commentFound) {
                                    commentFound = false;
                                }
                                if (commentFound) {
                                    k = lRatioGroups.get(j).getNumberOfRatios();
                                }
                            }
                            if (useUniquePeptide && commentFound && lRatioGroups.get(j).getProteinAccessions().length != 1) {
                                commentFound = false;
                            }
                            if (commentFound) {
                                j = lRatioGroups.size();
                            }
                        }
                        if (commentFound) {
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

                    //Use the filter: A peptide with a specific sequence will be selected
                    if (usePeptideSequence && txtPeptideSequence.getText().length() != 0) {
                        boolean peptideSequenceFound = false;
                        Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                        String lSequenceToMatch = txtPeptideSequence.getText();
                        if (useOnlyTrueRatios) {
                            for (int l = 0; l < lRatioGroups.size(); l++) {
                                boolean lValidFound = false;
                                RatioGroup lRatioGroup = lRatioGroups.get(l);
                                for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                    if (lRatioGroup.getRatio(k).getValid()) {
                                        lValidFound = true;
                                        k = lProtein.getTypes().length;
                                    }
                                }
                                if (useUniquePeptide && lValidFound && lRatioGroup.getProteinAccessions().length != 1) {
                                    //it's not unique, set it false
                                    lValidFound = false;
                                }
                                if (lValidFound) {
                                    if (lRatioGroups.get(l).getPeptideSequence().toUpperCase().indexOf(lSequenceToMatch.toUpperCase()) > -1) {
                                        peptideSequenceFound = true;
                                    }
                                }
                            }

                        } else {
                            if (useUniquePeptide) {
                                for (int j = 0; j < lProtein.getPeptideGroups(true).size(); j++) {
                                    for (int l = 0; l < lRatioGroups.size(); l++) {
                                        boolean lValidFound = true;
                                        RatioGroup lRatioGroup = lRatioGroups.get(l);
                                        if (lRatioGroup.getProteinAccessions().length != 1) {
                                            //it's not unique, set it false
                                            lValidFound = false;
                                        }
                                        if (lValidFound) {
                                            if (lRatioGroups.get(l).getPeptideSequence().toUpperCase().indexOf(lSequenceToMatch.toUpperCase()) > -1) {
                                                peptideSequenceFound = true;
                                            }
                                        }
                                    }
                                }
                            } else {
                                for (int j = 0; j < lProtein.getPeptideGroups(true).size(); j++) {
                                    for (int l = 0; l < lRatioGroups.size(); l++) {
                                        if (lRatioGroups.get(l).getPeptideSequence().toUpperCase().indexOf(lSequenceToMatch.toUpperCase()) > -1) {
                                            peptideSequenceFound = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (peptideSequenceFound) {
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


                    //Use the filter: A peptide that ends on a specific amino acid will be used
                    if (usePeptideEnding && txtPeptideEnding.getText().length() != 0) {
                        boolean peptideSequenceFound = false;
                        Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                        String lSequenceToMatch = txtPeptideEnding.getText();
                        if (useOnlyTrueRatios) {
                            for (int l = 0; l < lRatioGroups.size(); l++) {
                                boolean lValidFound = false;
                                RatioGroup lRatioGroup = lRatioGroups.get(l);
                                for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                    if (lRatioGroup.getRatio(k).getValid()) {
                                        lValidFound = true;
                                        k = lProtein.getTypes().length;
                                    }
                                }
                                if (useUniquePeptide && lValidFound && lRatioGroup.getProteinAccessions().length != 1) {
                                    //it's not unique, set it false
                                    lValidFound = false;
                                }
                                if (lValidFound) {
                                    if (lRatioGroups.get(l).getPeptideSequence().toUpperCase().endsWith(lSequenceToMatch.toUpperCase())) {
                                        peptideSequenceFound = true;
                                    }
                                }
                            }

                        } else {
                            if (useUniquePeptide) {
                                for (int j = 0; j < lProtein.getPeptideGroups(true).size(); j++) {
                                    for (int l = 0; l < lRatioGroups.size(); l++) {
                                        boolean lValidFound = true;
                                        RatioGroup lRatioGroup = lRatioGroups.get(l);
                                        if (lRatioGroup.getProteinAccessions().length != 1) {
                                            //it's not unique, set it false
                                            lValidFound = false;
                                        }
                                        if (lValidFound) {
                                            if (lRatioGroups.get(l).getPeptideSequence().toUpperCase().endsWith(lSequenceToMatch.toUpperCase())) {
                                                peptideSequenceFound = true;
                                            }
                                        }
                                    }
                                }
                            } else {
                                for (int j = 0; j < lProtein.getPeptideGroups(true).size(); j++) {
                                    for (int l = 0; l < lRatioGroups.size(); l++) {
                                        if (lRatioGroups.get(l).getPeptideSequence().toUpperCase().endsWith(lSequenceToMatch.toUpperCase())) {
                                            peptideSequenceFound = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (peptideSequenceFound) {
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

                    //Use the filter: A peptide that starts on a specific amino acid will be used
                    if (usePeptideStarting && txtPeptideStarting.getText().length() != 0) {
                        boolean peptideSequenceFound = false;
                        Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                        String lSequenceToMatch = txtPeptideStarting.getText();
                        if (useOnlyTrueRatios) {
                            for (int l = 0; l < lRatioGroups.size(); l++) {
                                boolean lValidFound = false;
                                RatioGroup lRatioGroup = lRatioGroups.get(l);
                                for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                    if (lRatioGroup.getRatio(k).getValid()) {
                                        lValidFound = true;
                                        k = lProtein.getTypes().length;
                                    }
                                }
                                if (useUniquePeptide && lValidFound && lRatioGroup.getProteinAccessions().length != 1) {
                                    //it's not unique, set it false
                                    lValidFound = false;
                                }
                                if (lValidFound) {
                                    if (lRatioGroups.get(l).getPeptideSequence().toUpperCase().startsWith(lSequenceToMatch.toUpperCase())) {
                                        peptideSequenceFound = true;
                                    }
                                }
                            }

                        } else {
                            if (useUniquePeptide) {
                                for (int j = 0; j < lProtein.getPeptideGroups(true).size(); j++) {
                                    for (int l = 0; l < lRatioGroups.size(); l++) {
                                        boolean lValidFound = true;
                                        RatioGroup lRatioGroup = lRatioGroups.get(l);
                                        if (lRatioGroup.getProteinAccessions().length != 1) {
                                            //it's not unique, set it false
                                            lValidFound = false;
                                        }
                                        if (lValidFound) {
                                            if (lRatioGroups.get(l).getPeptideSequence().toUpperCase().startsWith(lSequenceToMatch.toUpperCase())) {
                                                peptideSequenceFound = true;
                                            }
                                        }
                                    }
                                }
                            } else {
                                for (int j = 0; j < lProtein.getPeptideGroups(true).size(); j++) {
                                    for (int l = 0; l < lRatioGroups.size(); l++) {
                                        if (lRatioGroups.get(l).getPeptideSequence().toUpperCase().startsWith(lSequenceToMatch.toUpperCase())) {
                                            peptideSequenceFound = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (peptideSequenceFound) {
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

                    //Use the filter: A peptide that starts after a specific amino acid will be used
                    if (usePeptideAfter && txtPeptideAfter.getText().length() != 0) {
                        boolean peptideSequenceFound = false;
                        Vector<QuantitativePeptideGroup> lPeptideGroups = lProtein.getPeptideGroups(true);
                        String lSequenceToMatch = txtPeptideAfter.getText();
                        if (useOnlyTrueRatios) {
                            for (int l = 0; l < lPeptideGroups.size(); l++) {
                                Vector<RatioGroup> lRatioGroups = lPeptideGroups.get(l).getRatioGroups();
                                for (int m = 0; m < lRatioGroups.size(); m++) {
                                    RatioGroup lRatioGroup = lRatioGroups.get(m);
                                    boolean lValidFound = false;
                                    for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                        if (lRatioGroup.getRatio(k).getValid()) {
                                            lValidFound = true;
                                            k = lProtein.getTypes().length;
                                        }
                                    }
                                    if (useUniquePeptide && lValidFound && lRatioGroup.getProteinAccessions().length != 1) {
                                        //it's not unique, set it false
                                        lValidFound = false;
                                    }
                                    if (lValidFound) {
                                        if (lPeptideGroups.get(l).getPreSequence().toUpperCase().startsWith(lSequenceToMatch.toUpperCase())) {
                                            peptideSequenceFound = true;
                                        }
                                    }
                                }
                            }

                        } else {
                            if (useUniquePeptide) {

                                for (int l = 0; l < lPeptideGroups.size(); l++) {
                                    Vector<RatioGroup> lRatioGroups = lPeptideGroups.get(l).getRatioGroups();
                                    for (int m = 0; m < lRatioGroups.size(); m++) {
                                        RatioGroup lRatioGroup = lRatioGroups.get(m);
                                        boolean lValidFound = false;
                                        if (useUniquePeptide && lValidFound && lRatioGroup.getProteinAccessions().length != 1) {
                                            //it's not unique, set it false
                                            lValidFound = false;
                                        }
                                        if (lValidFound) {
                                            if (lPeptideGroups.get(l).getPreSequence().toUpperCase().startsWith(lSequenceToMatch.toUpperCase())) {
                                                peptideSequenceFound = true;
                                            }
                                        }
                                    }
                                }

                            } else {
                                for (int l = 0; l < lPeptideGroups.size(); l++) {
                                    if (lPeptideGroups.get(l).getPreSequence().toUpperCase().startsWith(lSequenceToMatch.toUpperCase())) {
                                        peptideSequenceFound = true;
                                    }
                                }
                            }
                        }
                        if (peptideSequenceFound) {
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

                    //Use the filter: A peptide that ends before a specific amino acid will be used
                    if (usePeptideBefore && txtPeptideBefore.getText().length() != 0) {
                        boolean peptideSequenceFound = false;
                        Vector<QuantitativePeptideGroup> lPeptideGroups = lProtein.getPeptideGroups(true);
                        String lSequenceToMatch = txtPeptideBefore.getText();
                        if (useOnlyTrueRatios) {
                            for (int l = 0; l < lPeptideGroups.size(); l++) {
                                Vector<RatioGroup> lRatioGroups = lPeptideGroups.get(l).getRatioGroups();
                                for (int m = 0; m < lRatioGroups.size(); m++) {
                                    RatioGroup lRatioGroup = lRatioGroups.get(m);
                                    boolean lValidFound = false;
                                    for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                        if (lRatioGroup.getRatio(k).getValid()) {
                                            lValidFound = true;
                                            k = lProtein.getTypes().length;
                                        }
                                    }
                                    if (useUniquePeptide && lValidFound && lRatioGroup.getProteinAccessions().length != 1) {
                                        //it's not unique, set it false
                                        lValidFound = false;
                                    }
                                    if (lValidFound) {
                                        if (lPeptideGroups.get(l).getPostSequence().toUpperCase().startsWith(lSequenceToMatch.toUpperCase())) {
                                            peptideSequenceFound = true;
                                        }
                                    }
                                }
                            }

                        } else {
                            if (useUniquePeptide) {

                                for (int l = 0; l < lPeptideGroups.size(); l++) {
                                    Vector<RatioGroup> lRatioGroups = lPeptideGroups.get(l).getRatioGroups();
                                    for (int m = 0; m < lRatioGroups.size(); m++) {
                                        RatioGroup lRatioGroup = lRatioGroups.get(m);
                                        boolean lValidFound = false;
                                        if (useUniquePeptide && lValidFound && lRatioGroup.getProteinAccessions().length != 1) {
                                            //it's not unique, set it false
                                            lValidFound = false;
                                        }
                                        if (lValidFound) {
                                            if (lPeptideGroups.get(l).getPostSequence().toUpperCase().startsWith(lSequenceToMatch.toUpperCase())) {
                                                peptideSequenceFound = true;
                                            }
                                        }
                                    }
                                }

                            } else {
                                for (int l = 0; l < lPeptideGroups.size(); l++) {
                                    if (lPeptideGroups.get(l).getPostSequence().toUpperCase().startsWith(lSequenceToMatch.toUpperCase())) {
                                        peptideSequenceFound = true;
                                    }
                                }
                            }
                        }
                        if (peptideSequenceFound) {
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


                    //only in distiller
                    if (useNterminalModification && txtNterm.getText().length() != 0) {
                        boolean lNterminalFound = false;
                        String lNtermToMatch = txtNterm.getText();
                        Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                        for (int j = 0; j < lRatioGroups.size(); j++) {
                            if (lRatioGroups.get(j).getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || lRatioGroups.get(j).getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS) {
                                //we're in distiller mode
                                DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) lRatioGroups.get(j);
                                for (int k = 0; k < lRatioGroup.getNumberOfIdentifications(); k++) {
                                    DatfilePeptideIdentification lIdentification = (DatfilePeptideIdentification) lRatioGroup.getIdentification(k);
                                    String lModifiedSequence = lIdentification.getModified_sequence();
                                    String lNtermMod = lModifiedSequence.substring(0, lModifiedSequence.indexOf("-"));

                                    if (useOnlyTrueRatios) {
                                        boolean lValidFound = false;
                                        for (int l = 0; l < lRatioGroup.getNumberOfRatios(); l++) {
                                            if (lRatioGroup.getRatio(l).getValid()) {
                                                lValidFound = true;
                                                l = lProtein.getTypes().length;
                                            }
                                        }
                                        if (useUniquePeptide && lValidFound && lRatioGroup.getProteinAccessions().length != 1) {
                                            //it's not unique, set it false
                                            lValidFound = false;
                                        }
                                        if (lValidFound) {
                                            if (lNtermMod.toUpperCase().equalsIgnoreCase(lNtermToMatch.toUpperCase())) {
                                                lNterminalFound = true;
                                            }
                                        }
                                    } else {
                                        if (useUniquePeptide) {
                                            boolean lValidFound = false;
                                            if (useUniquePeptide && lValidFound && lRatioGroup.getProteinAccessions().length != 1) {
                                                //it's not unique, set it false
                                                lValidFound = false;
                                            }
                                            if (lValidFound) {
                                                if (lNtermMod.toUpperCase().equalsIgnoreCase(lNtermToMatch.toUpperCase())) {
                                                    lNterminalFound = true;
                                                }
                                            }
                                        } else {
                                            if (lNtermMod.toUpperCase().equalsIgnoreCase(lNtermToMatch.toUpperCase())) {
                                                lNterminalFound = true;
                                            }
                                        }
                                    }
                                    if (lNterminalFound) {
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
                        }
                    }

                    //Use the filter: A protein will be selected if the difference between a peptide ratio and the protein mean is greater than ...
                    if (usePeptideRatioProteinMeanDiff) {
                        String[] lTypes = lProtein.getTypes();
                        for (int l = 0; l < lTypes.length; l++) {
                            double lMean = lProtein.getProteinRatio(lTypes[l]);
                            boolean diffGreaterFound = false;

                            Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                            for (int j = 0; j < lRatioGroups.size(); j++) {
                                for (int k = 0; k < lRatioGroups.get(j).getNumberOfRatios(); k++) {
                                    Ratio lRatio = lRatioGroups.get(j).getRatio(k);
                                    if (useOnlyTrueRatios) {
                                        if (useUniquePeptide) {
                                            if (lRatioGroups.get(j).getProteinAccessions().length == 1) {
                                                if (Math.abs(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) - lMean) > (Double) spinnPeptideRatioProteinMeanDiff.getValue() && lRatio.getValid()) {
                                                    diffGreaterFound = true;
                                                    lRatioGroups.get(j).setSelected(true);
                                                }
                                            }
                                        } else {
                                            if (Math.abs(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) - lMean) > (Double) spinnPeptideRatioProteinMeanDiff.getValue() && lRatio.getValid()) {
                                                diffGreaterFound = true;
                                                lRatioGroups.get(j).setSelected(true);
                                            }
                                        }
                                    } else {
                                        if (useUniquePeptide) {
                                            if (lRatioGroups.get(j).getProteinAccessions().length == 1) {
                                                if (Math.abs(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) - lMean) > (Double) spinnPeptideRatioProteinMeanDiff.getValue()) {
                                                    diffGreaterFound = true;
                                                    lRatioGroups.get(j).setSelected(true);
                                                }
                                            }
                                        } else {
                                            if (Math.abs(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) - lMean) > (Double) spinnPeptideRatioProteinMeanDiff.getValue()) {
                                                diffGreaterFound = true;
                                                lRatioGroups.get(j).setSelected(true);
                                            }
                                        }
                                    }
                                }
                            }
                            if (diffGreaterFound) {
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

                    //Use the filter: The protein must be validated
                    if (useValidatedProteins) {
                        if (lProtein.getValidated()) {
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

                    //Use the filter: Find proteins with ratios that have a z-score (significance) that is higher than ...
                    if (useHuberSignificanceHigher) {
                        boolean lExtremePeptideRatioFound = false;
                        Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                        for (int j = 0; j < lRatioGroups.size(); j++) {
                            for (int k = 0; k < lRatioGroups.get(j).getNumberOfRatios(); k++) {
                                Ratio lRatio = lRatioGroups.get(j).getRatio(k);
                                HashMap lMapStatistics = iQuantitativeValidationSingelton.getReferenceSet().getStatisticalMeasermentForRatio(lRatio.getType(), lRatio);
                                //the correct ratio
                                if (useOnlyTrueRatios) {
                                    if (useUniquePeptide) {
                                        if (lRatioGroups.get(j).getProteinAccessions().length == 1) {
                                            if ((Double) lMapStatistics.get("significance") > (Double) spinHuberSignificanceHigher.getValue() && lRatio.getValid()) {
                                                lExtremePeptideRatioFound = true;
                                                lRatioGroups.get(j).setSelected(true);
                                            }
                                        }
                                    } else {
                                        if ((Double) lMapStatistics.get("significance") > (Double) spinHuberSignificanceHigher.getValue() && lRatio.getValid()) {
                                            lExtremePeptideRatioFound = true;
                                            lRatioGroups.get(j).setSelected(true);
                                        }
                                    }

                                } else {
                                    if (useUniquePeptide) {
                                        if (lRatioGroups.get(j).getProteinAccessions().length == 1) {
                                            if ((Double) lMapStatistics.get("significance") > (Double) spinHuberSignificanceHigher.getValue()) {
                                                lExtremePeptideRatioFound = true;
                                                lRatioGroups.get(j).setSelected(true);
                                            }
                                        }
                                    } else {
                                        if ((Double) lMapStatistics.get("significance") > (Double) spinHuberSignificanceHigher.getValue()) {
                                            lExtremePeptideRatioFound = true;
                                            lRatioGroups.get(j).setSelected(true);
                                        }
                                    }
                                }

                            }
                        }
                        if (lExtremePeptideRatioFound) {
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

                    //Use the filter: Find proteins with ratios that have a z-score (significance) that is lower than ...
                    if (useHuberSignificanceLower) {
                        boolean lExtremePeptideRatioFound = false;
                        Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
                        for (int j = 0; j < lRatioGroups.size(); j++) {
                            for (int k = 0; k < lRatioGroups.get(j).getNumberOfRatios(); k++) {
                                Ratio lRatio = lRatioGroups.get(j).getRatio(k);
                                HashMap lMapStatistics = iQuantitativeValidationSingelton.getReferenceSet().getStatisticalMeasermentForRatio(lRatio.getType(), lRatio);
                                //the correct ratio
                                if (useOnlyTrueRatios) {
                                    if (useUniquePeptide) {
                                        if (lRatioGroups.get(j).getProteinAccessions().length == 1) {
                                            if ((Double) lMapStatistics.get("significance") < (Double) spinHuberSignificanceLower.getValue() && lRatio.getValid()) {
                                                lExtremePeptideRatioFound = true;
                                                lRatioGroups.get(j).setSelected(true);
                                            }
                                        }
                                    } else {
                                        if ((Double) lMapStatistics.get("significance") < (Double) spinHuberSignificanceLower.getValue() && lRatio.getValid()) {
                                            lExtremePeptideRatioFound = true;
                                            lRatioGroups.get(j).setSelected(true);
                                        }
                                    }

                                } else {
                                    if (useUniquePeptide) {
                                        if (lRatioGroups.get(j).getProteinAccessions().length == 1) {
                                            if ((Double) lMapStatistics.get("significance") < (Double) spinHuberSignificanceLower.getValue()) {
                                                lExtremePeptideRatioFound = true;
                                                lRatioGroups.get(j).setSelected(true);
                                            }
                                        }
                                    } else {
                                        if ((Double) lMapStatistics.get("significance") < (Double) spinHuberSignificanceLower.getValue()) {
                                            lExtremePeptideRatioFound = true;
                                            lRatioGroups.get(j).setSelected(true);
                                        }
                                    }
                                }

                            }
                        }
                        if (lExtremePeptideRatioFound) {
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

                    //Use the filter: The protein must be validated
                    if (useProteinComment) {
                        if (lProtein.getProteinComment().length() > 0) {
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

                    if (useThisProtein) {
                        lFiltered.add(lProtein);
                    }

                }

                return true;
            }

            public void finished() {
                //
                progressBar1.setVisible(false);
                //create the filtered proteins array
                iFilteredProteins = new QuantitativeProtein[lFiltered.size()];
                lFiltered.toArray(iFilteredProteins);
                //Set the filtered proteins in the parent
                iParent.setFilteredProteins(lFiltered);
                JOptionPane.showMessageDialog(iParent, "Took " + iFilteredProteins.length + " proteins from " + iProteinToFilter.length + " proteins!", "Info!", JOptionPane.INFORMATION_MESSAGE);
                iLog.addLog("Took " + iFilteredProteins.length + " proteins from " + iProteinToFilter.length + " proteins!");
                close();
            }

        };

        lStarter.start();


    }


    public void close() {
        this.dispose();
    }

    /**
     * This method creates UI components
     */
    private void createUIComponents() {
        spinIdentifications = new JSpinner(new SpinnerNumberModel(2, 0, 20, 1));
        spinDifferentPeptides = new JSpinner(new SpinnerNumberModel(2, 0, 20, 1));
        spinDiffUniquePeptides = new JSpinner(new SpinnerNumberModel(2, 0, 20, 1));
        spinnPeptideRatioProteinMeanDiff = new JSpinner(new SpinnerNumberModel(1.00, -10.0, 10.0, 0.05));
        if (iQuantitativeValidationSingelton.isLog2()) {
            spinProteinMeanLarger = new JSpinner(new SpinnerNumberModel(0.00, -10.0, 10.0, 0.05));
            spinProteinMeanSmaller = new JSpinner(new SpinnerNumberModel(0.00, -10.0, 10.0, 0.05));
            spinPeptideRatioSmaller = new JSpinner(new SpinnerNumberModel(0.00, -10.0, 10.0, 0.05));
            spinPeptideRatioLarger = new JSpinner(new SpinnerNumberModel(0.00, -10.0, 10.0, 0.05));
            spinMeanInRange = new JSpinner(new SpinnerNumberModel(0.00, -10.0, 10.0, 0.05));
        } else {
            spinProteinMeanLarger = new JSpinner(new SpinnerNumberModel(1.00, -10.0, 10.0, 0.05));
            spinProteinMeanSmaller = new JSpinner(new SpinnerNumberModel(1.00, -10.0, 10.0, 0.05));
            spinPeptideRatioSmaller = new JSpinner(new SpinnerNumberModel(1.00, -10.0, 10.0, 0.05));
            spinPeptideRatioLarger = new JSpinner(new SpinnerNumberModel(1.00, -10.0, 10.0, 0.05));
            spinMeanInRange = new JSpinner(new SpinnerNumberModel(1.00, -10.0, 10.0, 0.05));
        }
        spinSingle = new JSpinner(new SpinnerNumberModel(0.05, 0.0, 1.0, 0.01));
        spinHuberSignificanceHigher = new JSpinner(new SpinnerNumberModel(1.96, 0.0, 5.0, 0.01));
        spinHuberSignificanceLower = new JSpinner(new SpinnerNumberModel(-1.96, -5.0, 0.0, 0.01));
        cmbProteinMeanRatioTypesLarger = new JComboBox(iTypes);
        cmbProteinMeanRatioTypesSmaller = new JComboBox(iTypes);
        cmbProteinMeanRatioTypesRange1 = new JComboBox(iTypes);
        cmbProteinMeanRatioTypesRange2 = new JComboBox(iTypes);
        cmbPeptideRatioTypesLarger = new JComboBox(iTypes);
        cmbPeptideRatioTypesSmaller = new JComboBox(iTypes);
        cmbSingleComponent = new JComboBox(iComponents);
        cmbSingleComponentsTarget = new JComboBox(iComponents);

    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel1, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scrollPane1, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        scrollPane1.setViewportView(panel2);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 7;
        gbc.gridheight = 7;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel3, gbc);
        panel3.setBorder(BorderFactory.createTitledBorder(null, "Peptide ratio filters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP, new Font(panel3.getFont().getName(), Font.BOLD, 16)));
        final JLabel label1 = new JLabel();
        label1.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label1, gbc);
        chbNumberOfAllIdentifications = new JCheckBox();
        chbNumberOfAllIdentifications.setSelected(false);
        chbNumberOfAllIdentifications.setText("Number of identifications for protein  >  ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbNumberOfAllIdentifications, gbc);
        chbNumberOfDifferentPeptides = new JCheckBox();
        chbNumberOfDifferentPeptides.setText("Number of different identified peptides for protein  >  ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbNumberOfDifferentPeptides, gbc);
        chbDiffUniqueRazor = new JCheckBox();
        chbDiffUniqueRazor.setText("Number of different unique or razor (blue or red) peptides for protein  >  ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbDiffUniqueRazor, gbc);
        chbUseSingles = new JCheckBox();
        chbUseSingles.setText("Filter proteins with a peptide with absolute intensity for component");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbUseSingles, gbc);
        chbPeptideRatioLarger = new JCheckBox();
        chbPeptideRatioLarger.setText("Peptide ratio for type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbPeptideRatioLarger, gbc);
        chbPeptideRatioSmaller = new JCheckBox();
        chbPeptideRatioSmaller.setText("Peptide ratio for type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbPeptideRatioSmaller, gbc);
        chbDiffRatioAndMean = new JCheckBox();
        chbDiffRatioAndMean.setText("Find proteins with a peptide ratio, protein mean difference greater than");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbDiffRatioAndMean, gbc);
        chbOnlyTrue = new JCheckBox();
        chbOnlyTrue.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(chbOnlyTrue, gbc);
        chbUnique = new JCheckBox();
        chbUnique.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(chbUnique, gbc);
        chbHuberSignificanceHigher = new JCheckBox();
        chbHuberSignificanceHigher.setText("Find proteins with ratios that have a Z-score (significance) that is higher than");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbHuberSignificanceHigher, gbc);
        chbHuberSignificanceLower = new JCheckBox();
        chbHuberSignificanceLower.setText("Find proteins with ratios that have a Z-score (significance) that is lower than");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbHuberSignificanceLower, gbc);
        chbRatioComment = new JCheckBox();
        chbRatioComment.setText("Find ratios with comments");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 15;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbRatioComment, gbc);
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 12;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);
        panel3.add(separator1, gbc);
        final JLabel label2 = new JLabel();
        label2.setFont(new Font(label2.getFont().getName(), Font.ITALIC, label2.getFont().getSize()));
        label2.setText("Use only valid ratios in the following filters");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 5);
        panel3.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setFont(new Font(label3.getFont().getName(), Font.ITALIC, label3.getFont().getSize()));
        label3.setText("Use only uniquely identified (blue) peptides");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 5);
        panel3.add(label3, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(cmbPeptideRatioTypesLarger, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(cmbPeptideRatioTypesSmaller, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(spinIdentifications, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(spinDifferentPeptides, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(spinDiffUniquePeptides, gbc);
        final JLabel label4 = new JLabel();
        label4.setText(">");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 8;
        panel3.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("<");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 9;
        panel3.add(label5, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(spinPeptideRatioLarger, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(spinPeptideRatioSmaller, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(cmbSingleComponentsTarget, gbc);
        jLabelSingle2 = new JLabel();
        jLabelSingle2.setText("is smaller than ");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(jLabelSingle2, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 12;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(spinnPeptideRatioProteinMeanDiff, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 13;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(spinHuberSignificanceHigher, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 14;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(spinHuberSignificanceLower, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 20;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(spinSingle, gbc);
        jLabelSingle3 = new JLabel();
        jLabelSingle3.setHorizontalAlignment(4);
        jLabelSingle3.setText("   of the absolute intensity for component");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.ipadx = 15;
        panel3.add(jLabelSingle3, gbc);
        chbPeptidesSequence = new JCheckBox();
        chbPeptidesSequence.setText("Find peptides with peptide containing");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 16;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbPeptidesSequence, gbc);
        chbPeptideEnding = new JCheckBox();
        chbPeptideEnding.setText("Find peptides with peptide ending on");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 17;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbPeptideEnding, gbc);
        chbPeptideStarting = new JCheckBox();
        chbPeptideStarting.setText("Find peptides with peptide starting on");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 18;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbPeptideStarting, gbc);
        chbPeptideAfter = new JCheckBox();
        chbPeptideAfter.setText("Find peptides with peptide starting after");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 19;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbPeptideAfter, gbc);
        chbPeptideBefore = new JCheckBox();
        chbPeptideBefore.setText("Find peptides with peptide ending before");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 20;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbPeptideBefore, gbc);
        chbNterm = new JCheckBox();
        chbNterm.setText("Find peptides with N-terminal modification");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 21;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(chbNterm, gbc);
        txtPeptideSequence = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 16;
        gbc.gridwidth = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(txtPeptideSequence, gbc);
        txtPeptideEnding = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 17;
        gbc.gridwidth = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(txtPeptideEnding, gbc);
        txtPeptideStarting = new JTextField();
        txtPeptideStarting.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 18;
        gbc.gridwidth = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(txtPeptideStarting, gbc);
        txtPeptideAfter = new JTextField();
        txtPeptideAfter.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 19;
        gbc.gridwidth = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(txtPeptideAfter, gbc);
        txtPeptideBefore = new JTextField();
        txtPeptideBefore.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 20;
        gbc.gridwidth = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(txtPeptideBefore, gbc);
        txtNterm = new JTextField();
        txtNterm.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 21;
        gbc.gridwidth = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(txtNterm, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.ipadx = 50;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(cmbSingleComponent, gbc);
        lblMulti1 = new JLabel();
        lblMulti1.setForeground(new Color(-65536));
        lblMulti1.setText("You are working with more than one data source.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(lblMulti1, gbc);
        lblMulti2 = new JLabel();
        lblMulti2.setForeground(new Color(-65536));
        lblMulti2.setText("Some filters are specific for one data source");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel3.add(lblMulti2, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 7;
        gbc.gridheight = 15;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel4, gbc);
        panel4.setBorder(BorderFactory.createTitledBorder(null, "Protein filters", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP, new Font(panel4.getFont().getName(), Font.BOLD, 16)));
        chbAccessionSearch = new JCheckBox();
        chbAccessionSearch.setText("Find protein with accession(s):");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(chbAccessionSearch, gbc);
        txtProteinAccession = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(txtProteinAccession, gbc);
        chbValidated = new JCheckBox();
        chbValidated.setText("Find validated proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(chbValidated, gbc);
        chbHasNonValidRatio = new JCheckBox();
        chbHasNonValidRatio.setText("Protein has invalid ratios");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(chbHasNonValidRatio, gbc);
        chbExcludeYellow = new JCheckBox();
        chbExcludeYellow.setText("Protein has only unique or razor peptides (exclude protein with yellow peptides)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(chbExcludeYellow, gbc);
        chbProteinMeanGreater = new JCheckBox();
        chbProteinMeanGreater.setText("Protein mean for type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(chbProteinMeanGreater, gbc);
        chbProteinMeanLower = new JCheckBox();
        chbProteinMeanLower.setText("Protein mean for type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(chbProteinMeanLower, gbc);
        chbProteinMeanInRange = new JCheckBox();
        chbProteinMeanInRange.setText("Protein mean for type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(chbProteinMeanInRange, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridheight = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(cmbProteinMeanRatioTypesLarger, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(cmbProteinMeanRatioTypesSmaller, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(cmbProteinMeanRatioTypesRange1, gbc);
        final JLabel label6 = new JLabel();
        label6.setText(">");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(label6, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("<");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(label7, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("in range of ");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(label8, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.gridheight = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(spinProteinMeanLarger, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(spinProteinMeanSmaller, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(spinMeanInRange, gbc);
        chbProteinComment = new JCheckBox();
        chbProteinComment.setText("Find proteins with a comment");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(chbProteinComment, gbc);
        final JLabel label9 = new JLabel();
        label9.setText("protein mean for type");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(label9, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel4.add(cmbProteinMeanRatioTypesRange2, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(spacer1, gbc);
        filterButton = new JButton();
        filterButton.setText("Filter");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(filterButton, gbc);
        progressBar1 = new JProgressBar();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(progressBar1, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
