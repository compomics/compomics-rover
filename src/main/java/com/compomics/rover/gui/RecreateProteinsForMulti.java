package com.compomics.rover.gui;

import org.apache.log4j.Logger;

import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.QuantitativeProtein;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.ReferenceSet;
import com.compomics.rover.general.quantitation.sorters.QuantitativeProteinSorterByRatioGroupNumbers;
import com.compomics.rover.general.quantitation.sorters.QuantitativeProteinSorterByAccession;
import com.compomics.rover.general.sequenceretriever.UniprotSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.IpiSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.NcbiSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.TairSequenceRetriever;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.util.sun.SwingWorker;

import javax.swing.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.sql.Connection;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 09-Jan-2010
 * Time: 13:31:23
 * To change this template use File | Settings | File Templates.
 */
public class RecreateProteinsForMulti extends JFrame {
	// Class specific log4j logger for RecreateProteinsForMulti instances.
	 private static Logger logger = Logger.getLogger(RecreateProteinsForMulti.class);
    private JPanel jpanSources;
    private JProgressBar progressBar1;
    private JButton startReconstructionButton;
    private JPanel contentPane;

    /**
     * This validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
    private Vector<RoverSource> iRoverSources;
    private Vector<String> iTitles;
    private Vector<JCheckBox> iCheckboxes = new Vector<JCheckBox>();
    private Vector<RatioGroupCollection> iCollections;
    private Connection iConn;
    private boolean iStandAlone;

    public RecreateProteinsForMulti(Connection aConn, boolean aStandAlone) {
        super("Select different sources for the reconstruction");
        this.iConn = aConn;
        this.iStandAlone = aStandAlone;

        $$$setupUI$$$();


        iRoverSources = iQuantitativeValidationSingelton.getOriginalRoverSources();
        iTitles = iQuantitativeValidationSingelton.getTitles();
        iCollections = iQuantitativeValidationSingelton.getOriginalCollections();
        jpanSources.setLayout(new BoxLayout(jpanSources, BoxLayout.Y_AXIS));
        for (int i = 0; i < iTitles.size(); i++) {
            JPanel lTitlePanel = new JPanel();
            lTitlePanel.setLayout(new BoxLayout(lTitlePanel, BoxLayout.X_AXIS));
            JCheckBox lCheck = new JCheckBox();
            lTitlePanel.add(lCheck);
            lCheck.add(Box.createHorizontalGlue());
            lTitlePanel.add(new JLabel(iTitles.get(i)));
            lCheck.add(Box.createHorizontalGlue());
            iCheckboxes.add(lCheck);
            jpanSources.add(lTitlePanel);
            jpanSources.add(Box.createVerticalGlue());
        }

        startReconstructionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final boolean[] lOneIsSelected = new boolean[]{false};

                SwingWorker lStarter = new SwingWorker() {
                    public Boolean construct() {
                        Vector<Boolean> lSelected = new Vector<Boolean>();
                        for (int i = 0; i < iCheckboxes.size(); i++) {
                            if (iCheckboxes.get(i).isSelected()) {
                                lSelected.add(true);
                                lOneIsSelected[0] = true;
                            } else {
                                lSelected.add(false);
                            }
                        }

                        iQuantitativeValidationSingelton.setSelectedIndexes(lSelected);

                        if (lOneIsSelected[0]) {
                            Vector<RatioGroupCollection> lCollections = new Vector<RatioGroupCollection>();
                            for (int i = 0; i < lSelected.size(); i++) {
                                if (lSelected.get(i)) {
                                    for (int j = 0; j < iCollections.size(); j++) {
                                        if (iCollections.get(j).getIndex() == i) {
                                            lCollections.add(iCollections.get(j));
                                        }
                                    }

                                } else {
                                    //this was not selected by the user
                                }
                            }
                            //clear some things from the memory
                            iQuantitativeValidationSingelton.restart();
                            //use the found collections to recreate the proteins
                            //get stuff from the parent
                            Vector<String> lProteinAccessions = iQuantitativeValidationSingelton.getProteinAccessions();
                            Vector<String> lComponentTypesList = iQuantitativeValidationSingelton.getComponentTypes();
                            String[] lComponentTypes = new String[lComponentTypesList.size()];
                            lComponentTypesList.toArray(lComponentTypes);
                            Vector<String> lRatioTypesList = iQuantitativeValidationSingelton.getRatioTypes();
                            String[] lRatioTypes = new String[lRatioTypesList.size()];
                            lRatioTypesList.toArray(lRatioTypes);
                            Vector<QuantitativeProtein> lQuantProtein = new Vector<QuantitativeProtein>();
                            progressBar1.setIndeterminate(true);
                            progressBar1.setStringPainted(true);
                            progressBar1.setString("Creating proteins and reference set ... ");


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
                            Vector<QuantitativeProtein> lFinalProteins = new Vector<QuantitativeProtein>();
                            for (int i = 0; i < lQuantProtein.size(); i++) {
                                if (lQuantProtein.get(i).getNumberOfPeptideGroups() != 0) {
                                    lFinalProteins.add(lQuantProtein.get(i));
                                }
                            }
                            progressBar1.setIndeterminate(false);
                            downloadProteinSequences(lFinalProteins);

                            //_____Do garbage collection______
                            System.gc();

                            //show gui
                            JOptionPane.showMessageDialog(getFrame(), "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                            QuantitationValidationGUI gui = new QuantitationValidationGUI(lFinalProteins, iConn, iStandAlone);
                            gui.setVisible(true);
                        }

                        return true;

                    }

                    public void finished() {
                        if (lOneIsSelected[0]) {
                            closeFrame();
                        } else {
                            //no reconstruction
                            JOptionPane.showMessageDialog(getFrame(), "One source must be selected", "INFO", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }

                };
                lStarter.start();


            }
        });

        //set the frame parameters
        this.setContentPane(contentPane);
        this.setSize(660, 500);
        this.setVisible(true);
    }

    /**
     * This method will close the frame
     */
    public void closeFrame() {
        this.setVisible(false);
        this.dispose();
    }


    public JFrame getFrame() {
        return this;
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
        progressBar1.setString("Downloading protein sequences");
        progressBar1.setStringPainted(true);
        progressBar1.setMaximum(aProteins.size());
        progressBar1.setIndeterminate(false);
        for (int i = 0; i < aProteins.size(); i++) {
            progressBar1.setValue(progressBar1.getValue() + 1);
            QuantitativeProtein lProtein = aProteins.get(i);
            lProtein.setSequence(iQuantitativeValidationSingelton.getProteinSequence(lProtein.getAccession()));

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
            } catch (Exception e) {
                //sequence not found
                //e.printStackTrace();
            }
        }
    }


    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        jpanSources = new JPanel();
        jpanSources.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(jpanSources, gbc);
        progressBar1 = new JProgressBar();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(progressBar1, gbc);
        startReconstructionButton = new JButton();
        startReconstructionButton.setText("Start reconstruction");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(startReconstructionButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
