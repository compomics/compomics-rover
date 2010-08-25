package com.compomics.rover.gui;

import org.apache.log4j.Logger;

import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.quantitation.QuantitativeProtein;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.QuantitativePeptideGroup;
import com.compomics.rover.general.quantitation.sorters.*;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatio;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatioGroup;
import com.compomics.rover.general.singelton.Log;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.fileio.rover.RoverFileReader;
import com.compomics.util.sun.SwingWorker;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 20-dec-2008
 * Time: 21:55:55
 * To change this template use File | Settings | File Templates.
 */


/**
 * This is the main frame of Rover
 */
public class QuantitationValidationGUI extends JFrame {
	// Class specific log4j logger for QuantitationValidationGUI instances.
	 private static Logger logger = Logger.getLogger(QuantitationValidationGUI.class);

    //gui components
    private JTabbedPane tabbedPane;
    private JPanel contentPane;
    private JPanel leftPanel;
    private JList proteinList;
    private JButton filterProteinsButton;
    private JPanel rightPanel;
    private JPanel jpanProteinMeanOptions;
    private JCheckBox useOnlyValidRatiosCheckBox;
    private JPanel jpanProteinGraphWrapper;
    private JPanel jpanProteinRatioGroups;
    private JLabel lblProteinInfo;
    private JPanel jpanExtraProteinInfo;
    private JButton addToSelectionButton;
    private JButton showSelectionButton;
    private JButton goToBrowserButton;
    private JCheckBox log2CheckBox;
    private JButton saveSelectedProteinsToPDFButton;
    private JPanel jpanRatioInRovTab;
    private JPanel tabProtein;
    private JPanel tabRov;
    private JCheckBox chbShowOnlyNonValid;
    private JButton showRovButton;
    private JButton addAllProteinsInButton;
    private JButton deleteAllProteinsInButton;
    private JButton showPossibleIsoformsButton;
    private JButton showAllProteinsButton;
    private JButton saveSelectedProteinsToCSVButton;
    private JButton setValidatedButton;
    private JButton exportButton;
    private JLabel jLabelTotaleNumber;
    private JLabel jLabelSelected;
    private JLabel jLabelValidated;
    private JButton loadRoverFileButton;
    private JPanel jpanGraphButtons;
    private JPanel jpanProteinGraph;
    private JButton graphExtenderRight;
    private JButton graphExtenderLeft;
    private JButton graphExtenderLeftPlus;
    private JButton graphExtenderRightMin;
    private JCheckBox showRealDistributionCheckBox;
    private JCheckBox showHuberEstimatedDistributionCheckBox;
    private JTree treeRovFiles;
    private JButton miscButton;
    private JButton misc_infoButton;
    private JButton logButton;
    private JCheckBox useOnlyUniquelyIdentifiedCheckBox;
    private JButton restartButton;
    private JButton reconstructButton;
    private JComboBox cmbOrder;
    private JComboBox cmbRatioTypes;
    private JLabel lblRatioType;
    private JCheckBox useOriginalCheckBox;
    private JProgressBar progressBar1;

    /**
     * All the proteins (with isoforms) with quantitative information
     */
    private QuantitativeProtein[] iProteins;
    /**
     * The proteins showed in the list, these proteins can be changed by applying a filter
     */
    private Vector<QuantitativeProtein> iFilteredProteins;
    /**
     * The rov files used in the selected proteins
     */
    private Vector<String> iRovFiles = new Vector<String>();
    /**
     * The hits used in the selected rov file for the selected proteins
     */
    private Vector<Integer> iRovFileHits = new Vector<Integer>();
    /**
     * The ratios used in the selected rov file hitfor the selected proteins
     */
    private Vector<DistillerRatioGroup> iHitRatioGroups = new Vector<DistillerRatioGroup>();
    /**
     * The DistillerRatioGroups from the proteins that the user selected
     */
    private Vector<RatioGroup> iSelectedRatioGroups = new Vector<RatioGroup>();
    /**
     * The ratio charts for the different ratio types
     */
    private JFreeChart[] iChartPanels;
    /**
     * The protein bar
     */
    private ProteinBarPanel iProteinBar;
    /**
     * The connection to the ms_lims database
     */
    private Connection iConnMsLims;
    /**
     * This validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
    /**
     * boolean that indicates if the selection is shown
     */
    private boolean iSelectionVizualised = false;
    /**
     * The tree for the different .rov files
     */
    private DefaultMutableTreeNode iRovFileTreeNod;
    /**
     * The protein that is selected
     */
    private QuantitativeProtein iProtein;
    /**
     * Boolean that indicates if this is a stand alone frame
     */
    private boolean iStandAlone;
    /**
     * The log
     */
    private Log iLog = Log.getInstance();

    private boolean iUpdating = false;


    /**
     * The constructor
     *
     * @param aProteins   The proteins that have quantitave information
     * @param aConn       The connection to the ms_lims database
     * @param aStandAlone Boolean that indicates if it's standalone
     */
    public QuantitationValidationGUI(Vector<QuantitativeProtein> aProteins, Connection aConn, boolean aStandAlone) {
        //set title
        super("Rover");
        //set icon
        if (iQuantitativeValidationSingelton.isMultipleSources()) {
            this.setIconImage(new ImageIcon(getClass().getResource("/mutliRover.png")).getImage());
        } else {
            this.setIconImage(new ImageIcon(getClass().getResource("/rover.png")).getImage());
        }
        //set the proteins
        this.iProteins = new QuantitativeProtein[aProteins.size()];
        aProteins.toArray(iProteins);
        this.iFilteredProteins = aProteins;
        //set stand alond
        this.iStandAlone = aStandAlone;
        //set the connection
        this.iConnMsLims = aConn;
        //create ui
        $$$setupUI$$$();
        //add a closing window listener
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                String lQuestion = "Do you want to close the validation? \nDo not forget to save your validated and seleted proteins in a .rover file!";
                if (iStandAlone) {
                    lQuestion = lQuestion + "\nThis is the primary Rover window. If you close this, all other opened Rover windows will close.";
                }
                int answer = JOptionPane.showConfirmDialog(new JFrame(), lQuestion, "Close Validation ? ", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    //close the validation frame
                    if (iStandAlone) {
                        if (iQuantitativeValidationSingelton.isDatabaseMode()) {
                            try {
                                logger.info("Closing the db connection");
                                iConnMsLims.close();
                            } catch (SQLException e) {
                                logger.info("Unable to close database connection!");
                            }
                        }
                        System.exit(0);
                    } else {
                        dispose();
                        setVisible(false);
                    }
                } else {
                    //do not close this frame
                }

            }
        });

        //set these checkboxes selected (depending on the status in the validation singelton)
        iQuantitativeValidationSingelton.setLog2(true);
        log2CheckBox.setSelected(iQuantitativeValidationSingelton.isLog2());
        useOnlyValidRatiosCheckBox.setSelected(iQuantitativeValidationSingelton.isUseOnlyValidRatioForProteinMean());
        useOnlyUniquelyIdentifiedCheckBox.setSelected(iQuantitativeValidationSingelton.isUseOnlyUniqueRatioForProteinMean());

        //set the frame parameters
        this.setContentPane(contentPane);
        this.setSize(1250, 750);
        //set the action listeners
        this.setActionListeners();

        //gui changes
        addToSelectionButton.setVisible(false);
        goToBrowserButton.setVisible(false);
        setValidatedButton.setVisible(false);
        showPossibleIsoformsButton.setVisible(false);
        progressBar1.setVisible(false);
        //create labels
        jLabelTotaleNumber.setText("# proteins : " + iProteins.length);
        jLabelSelected.setText("# selected proteins : " + iQuantitativeValidationSingelton.getSelectedProteins().size());
        jLabelValidated.setText("# validated proteins : " + iQuantitativeValidationSingelton.getValidatedProteins().size());
        //buttons adjustment
        addToSelectionButton.setText("");

        //only show the rov button if it is a Distiller project
        if (!iQuantitativeValidationSingelton.isDistillerQuantitation()) {
            showRovButton.setVisible(false);
            tabRov.setVisible(false);
            tabbedPane.remove(1);
        }

        //don't show the showRovButton
        if (iQuantitativeValidationSingelton.isMultipleSources()) {
            //we've got a multi project
            //some features will not be here
            showRovButton.setVisible(false);
            tabRov.setVisible(false);
            if (tabbedPane.getComponents().length > 1) {
                tabbedPane.remove(1);
            }
        }
        reconstructButton.setVisible(false);

        //show the normalization checkbox if needed
        if (iQuantitativeValidationSingelton.isNormalization()) {
            useOriginalCheckBox.setVisible(true);
        } else {
            useOriginalCheckBox.setVisible(false);
        }

        //show the ratio type selection combobox if needed
        if (iQuantitativeValidationSingelton.getRatioTypes().size() > 0) {
            lblRatioType.setVisible(true);
            cmbRatioTypes.setVisible(true);
        } else {
            lblRatioType.setVisible(false);
            cmbRatioTypes.setVisible(false);
        }

        //do not show the go to browser button if a local fasta db was used
        if (iQuantitativeValidationSingelton.getDatabaseType() == ProteinDatabaseType.LOCAL || iQuantitativeValidationSingelton.getDatabaseType() == ProteinDatabaseType.UNKNOWN) {
            goToBrowserButton.setVisible(false);
        }


    }

    /**
     * This method will close the frame and the program
     */
    public void closeFrame() {
        this.setVisible(false);
        this.dispose();
    }


    /**
     * This method will set all the action listeners
     */
    public void setActionListeners() {
        reconstructButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String lQuestion = "Do you want to close and reconstruct the validation? \nDo not forget to save your validated and seleted proteins in a .rover file!";
                int answer = JOptionPane.showConfirmDialog(new JFrame(), lQuestion, "Close Validation ? ", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    RecreateProteinsForMulti lHolder = new RecreateProteinsForMulti(iConnMsLims, iStandAlone);
                    closeFrame();
                } else {
                    //do not close this frame
                }
            }
        });

        logButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LogGui lLogGui = new LogGui();
            }
        });
        filterProteinsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FilterFrame filter = new FilterFrame(iProteins, iQuantitativeValidationSingelton.getReferenceSet(), getFrame(), iQuantitativeValidationSingelton.getReferenceSet().getTypes(), iQuantitativeValidationSingelton.getReferenceSet().getComponents());
            }
        });
        useOnlyValidRatiosCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(useOnlyValidRatiosCheckBox.isSelected());
                loadProtein(false);
            }
        });
        useOnlyUniquelyIdentifiedCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iQuantitativeValidationSingelton.setUseOnlyUniqueRatioForProteinMean(useOnlyUniquelyIdentifiedCheckBox.isSelected());
                loadProtein(false);
            }
        });
        log2CheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iQuantitativeValidationSingelton.setLog2(log2CheckBox.isSelected());
                if (iQuantitativeValidationSingelton.isLog2()) {
                    iQuantitativeValidationSingelton.setLeftGraphBorder(-2);
                    iQuantitativeValidationSingelton.setRightGraphBorder(2);
                } else {
                    iQuantitativeValidationSingelton.setLeftGraphBorder(0);
                    iQuantitativeValidationSingelton.setRightGraphBorder(3);
                }
                loadProtein(false);
            }
        });

        addToSelectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ImageIcon addIcon = new ImageIcon(getClass().getResource("/addSelection.gif"));
                ImageIcon deleteIcon = new ImageIcon(getClass().getResource("/deleteSelection.gif"));
                if (iProtein.getSelected()) {
                    //it's in the selection, delete it
                    iProtein.setSelected(false);
                    iQuantitativeValidationSingelton.getSelectedProteins().remove(iProtein);
                    if (iSelectionVizualised) {
                        iFilteredProteins.removeAllElements();

                        for (int i = 0; i < iQuantitativeValidationSingelton.getSelectedProteins().size(); i++) {
                            iFilteredProteins.add(iQuantitativeValidationSingelton.getSelectedProteins().get(i));
                        }
                        iSelectionVizualised = true;
                        proteinList.setSelectedIndex(2);
                        proteinList.updateUI();
                        jpanProteinGraph.removeAll();
                        jpanProteinRatioGroups.removeAll();
                        jpanExtraProteinInfo.removeAll();

                        iChartPanels = null;
                        iProteinBar = null;
                        addToSelectionButton.setVisible(false);
                        goToBrowserButton.setVisible(false);
                        showPossibleIsoformsButton.setVisible(false);
                        setValidatedButton.setVisible(false);

                        lblProteinInfo.setText("No protein selected");
                        update(getGraphics());
                    }
                    addToSelectionButton.setIcon(addIcon);
                    addToSelectionButton.setToolTipText("Add this protein to the selection");
                    writeToLog("Deselected protein : " + iProtein);

                } else {
                    //it's not in the selection add it
                    iProtein.setSelected(true);
                    iQuantitativeValidationSingelton.getSelectedProteins().add(iProtein);
                    addToSelectionButton.setIcon(deleteIcon);
                    addToSelectionButton.setToolTipText("Delete this protein from the selection");
                    writeToLog("Selected protein : " + iProtein);
                }
                proteinList.updateUI();
                jLabelSelected.setText("# selected proteins : " + iQuantitativeValidationSingelton.getSelectedProteins().size());
                jLabelValidated.setText("# validated proteins : " + iQuantitativeValidationSingelton.getValidatedProteins().size());
            }
        });

        showSelectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iFilteredProteins.removeAllElements();

                for (int i = 0; i < iQuantitativeValidationSingelton.getSelectedProteins().size(); i++) {
                    iFilteredProteins.add(iQuantitativeValidationSingelton.getSelectedProteins().get(i));
                }
                iSelectionVizualised = true;
                proteinList.setSelectedIndex(2);
                proteinList.updateUI();
                jpanProteinGraph.removeAll();
                jpanProteinRatioGroups.removeAll();
                jpanExtraProteinInfo.removeAll();

                iChartPanels = null;
                iProteinBar = null;
                addToSelectionButton.setVisible(false);
                goToBrowserButton.setVisible(false);
                showPossibleIsoformsButton.setVisible(false);
                setValidatedButton.setVisible(false);

                lblProteinInfo.setText("No protein selected");
                update(getGraphics());
            }
        });
        goToBrowserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.UNIPROT)) {
                    showInBrowser("http://www.uniprot.org/uniprot/" + iProtein.getAccession());
                } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.IPI)) {
                    showInBrowser("http://srs.ebi.ac.uk/srsbin/cgi-bin/wgetz?-id+5X1021Z8Dfm+[ipi-AccNumber:" + iProtein.getAccession() + "]+-e");
                } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.NCBI)) {
                    showInBrowser("http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein&id=" + iProtein.getAccession());
                }
            }
        });
        chbShowOnlyNonValid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadRovFileInfo();
            }
        });
        showRovButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadRovFileInfo();
            }
        });
        addAllProteinsInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int addCounter = 0;
                for (int i = 0; i < iFilteredProteins.size(); i++) {
                    QuantitativeProtein lProtein = iFilteredProteins.get(i);
                    //check if this protein is already in the selection
                    boolean lProteinInSelection = lProtein.getSelected();
                    if (!lProteinInSelection) {
                        //it's not in the selection , add it
                        iQuantitativeValidationSingelton.getSelectedProteins().add(lProtein);
                        lProtein.setSelected(true);
                        writeToLog("Selected protein : " + lProtein);
                        addCounter = addCounter + 1;
                    }
                }
                writeToLog("Added " + addCounter + " proteins to the selection.");
                jLabelSelected.setText("# selected proteins : " + iQuantitativeValidationSingelton.getSelectedProteins().size());
                jLabelValidated.setText("# validated proteins : " + iQuantitativeValidationSingelton.getValidatedProteins().size());
                proteinList.updateUI();
            }
        });
        deleteAllProteinsInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int deleteCounter = 0;
                for (int i = 0; i < iFilteredProteins.size(); i++) {
                    QuantitativeProtein lProtein = iFilteredProteins.get(i);
                    //check if this protein is already in the selection
                    boolean lProteinInSelection = lProtein.getSelected();
                    if (lProteinInSelection) {
                        //it's in the selection , delete it
                        iQuantitativeValidationSingelton.getSelectedProteins().remove(lProtein);
                        lProtein.setSelected(false);
                        writeToLog("Deselected protein : " + lProtein);
                        deleteCounter = deleteCounter + 1;
                    }
                }
                writeToLog("Deleted " + deleteCounter + " proteins from the selection.");
                jLabelSelected.setText("# selected proteins : " + iQuantitativeValidationSingelton.getSelectedProteins().size());
                jLabelValidated.setText("# validated proteins : " + iQuantitativeValidationSingelton.getValidatedProteins().size());
                proteinList.updateUI();
            }
        });
        showPossibleIsoformsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Create a vector to store all the possible isoform accessions in
                Vector<String> lAccessions = new Vector<String>();
                Vector<RatioGroup> lRatioGroups = iProtein.getRatioGroups();
                for (int i = 0; i < lRatioGroups.size(); i++) {
                    RatioGroup lRatioGroup = lRatioGroups.get(i);
                    String[] lRatioGroupAccessions = lRatioGroup.getProteinAccessions();
                    for (int j = 0; j < lRatioGroupAccessions.length; j++) {
                        //check if it's a new accessions
                        boolean lNewAccession = true;
                        for (int k = 0; k < lAccessions.size(); k++) {
                            if (lAccessions.get(k).equalsIgnoreCase(lRatioGroupAccessions[j])) {
                                lNewAccession = false;
                            }
                        }
                        if (lNewAccession) {
                            lAccessions.add(lRatioGroupAccessions[j]);
                        }

                    }
                }

                //we found the possible isoform accessions, now filter all the proteins and select the possible isoforms
                Vector<QuantitativeProtein> lFilteredProteins = new Vector<QuantitativeProtein>();
                for (int i = 0; i < iProteins.length; i++) {
                    QuantitativeProtein lProteinToCheck = iProteins[i];
                    for (int j = 0; j < lAccessions.size(); j++) {
                        if (lAccessions.get(j).equalsIgnoreCase(lProteinToCheck.getAccession())) {
                            lFilteredProteins.add(lProteinToCheck);
                        }
                    }
                }

                setFilteredProteins(lFilteredProteins);
            }
        });
        showAllProteinsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Vector<QuantitativeProtein> lFilteredProteins = new Vector<QuantitativeProtein>();
                for (int i = 0; i < iProteins.length; i++) {
                    QuantitativeProtein lProteinToCheck = iProteins[i];
                    lFilteredProteins.add(lProteinToCheck);
                }
                setFilteredProteins(lFilteredProteins);
            }
        });
        setValidatedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!iProtein.getValidated()) {
                    iProtein.setValidated(true);
                    iQuantitativeValidationSingelton.addValidatedProtein(iProtein);
                    setValidatedButton.setToolTipText("Set not validated");
                    setValidatedButton.setIcon(new ImageIcon(getClass().getResource("/setNotValidated.gif")));
                    writeToLog("Validated protein : " + iProtein);
                } else {
                    iProtein.setValidated(false);
                    iQuantitativeValidationSingelton.removeValidatedProtein(iProtein);
                    setValidatedButton.setToolTipText("Set validated");
                    setValidatedButton.setIcon(new ImageIcon(getClass().getResource("/setValidated.gif")));
                    writeToLog("Devalidated protein : " + iProtein);
                }
                jLabelSelected.setText("# selected proteins : " + iQuantitativeValidationSingelton.getSelectedProteins().size());
                jLabelValidated.setText("# validated proteins : " + iQuantitativeValidationSingelton.getValidatedProteins().size());
                proteinList.updateUI();
            }
        });
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ExportGui lExportGui = new ExportGui(getFrame(), iProtein, iProteins);
            }
        });
        loadRoverFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //open file chooser
                JFileChooser fc = new JFileChooser();
                if (iQuantitativeValidationSingelton.getFileLocationOpener() != null) {
                    fc.setCurrentDirectory(new File(iQuantitativeValidationSingelton.getFileLocationOpener()));
                }
                fc.showOpenDialog(getFrame());
                File iRoverFile = fc.getSelectedFile();
                if (iRoverFile != null) {
                    iQuantitativeValidationSingelton.setFileLocationOpener(iRoverFile.getParent());
                    RoverFileReader lRoverReader = new RoverFileReader(iRoverFile.getAbsolutePath(), iProteins);

                    proteinList.updateUI();
                    loadProtein(false);
                    //set labels
                    jLabelTotaleNumber.setText("# proteins : " + iProteins.length);
                    jLabelSelected.setText("# selected proteins : " + iQuantitativeValidationSingelton.getSelectedProteins().size());
                    jLabelValidated.setText("# validated proteins : " + iQuantitativeValidationSingelton.getValidatedProteins().size());
                }
            }
        });
        graphExtenderRight.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iQuantitativeValidationSingelton.setRightGraphBorder(iQuantitativeValidationSingelton.getRightGraphBorder() + 1);
                loadProtein(false);
            }
        });
        graphExtenderLeft.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iQuantitativeValidationSingelton.setLeftGraphBorder(iQuantitativeValidationSingelton.getLeftGraphBorder() - 1);
                loadProtein(false);
            }
        });
        graphExtenderRightMin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (iQuantitativeValidationSingelton.isLog2()) {
                    if (iQuantitativeValidationSingelton.getRightGraphBorder() - 1 > 0) {
                        iQuantitativeValidationSingelton.setRightGraphBorder(iQuantitativeValidationSingelton.getRightGraphBorder() - 1);
                    }
                } else {
                    if (iQuantitativeValidationSingelton.getRightGraphBorder() - 1 > 1) {
                        iQuantitativeValidationSingelton.setRightGraphBorder(iQuantitativeValidationSingelton.getRightGraphBorder() - 1);
                    }
                }
                loadProtein(false);
            }
        });
        graphExtenderLeftPlus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (iQuantitativeValidationSingelton.isLog2()) {
                    if (iQuantitativeValidationSingelton.getLeftGraphBorder() + 1 < 0) {
                        iQuantitativeValidationSingelton.setLeftGraphBorder(iQuantitativeValidationSingelton.getLeftGraphBorder() + 1);
                    }
                } else {
                    if (iQuantitativeValidationSingelton.getLeftGraphBorder() + 1 < 1) {
                        iQuantitativeValidationSingelton.setLeftGraphBorder(iQuantitativeValidationSingelton.getLeftGraphBorder() + 1);
                    }
                }
                loadProtein(false);
            }
        });
        miscButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ReferenceSetParameterFrame lReferenceFrame = new ReferenceSetParameterFrame(iProteins, getFrame());
            }
        });

        ActionListener statisticTypeListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadProtein(false);
            }
        };
        showRealDistributionCheckBox.addActionListener(statisticTypeListener);
        showHuberEstimatedDistributionCheckBox.addActionListener(statisticTypeListener);

        proteinList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent me) {
                if (me.getButton() == 1) {
                    loadProtein(true);
                }
            }
        });

        proteinList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    loadProtein(true);
                }
            }

        });


        misc_infoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ReferenceSetInfoFrame lReferenceFrame = new ReferenceSetInfoFrame();
            }
        });

        cmbOrder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                progressBar1.setIndeterminate(true);
                progressBar1.setVisible(true);
                SwingWorker lStarter = new SwingWorker() {
                    public Boolean construct() {
                        String lOrderType = (String) cmbOrder.getSelectedItem();
                        if (lOrderType.equalsIgnoreCase("alphabetical")) {
                            //sort by the protein accession
                            Collections.sort(iFilteredProteins, new QuantitativeProteinSorterByAccession());
                            proteinList.updateUI();
                        }
                        if (lOrderType.equalsIgnoreCase("# peptide ratios")) {
                            //sort by number of ratio groups
                            Collections.sort(iFilteredProteins, new QuantitativeProteinSorterByRatioGroupNumbers());
                            proteinList.updateUI();
                        }
                        if (lOrderType.equalsIgnoreCase("protein ratio")) {
                            //sort by the protein ratio
                            Collections.sort(iFilteredProteins, new QuantitativeProteinSorterByProteinMean((String) cmbRatioTypes.getSelectedItem()));
                            proteinList.updateUI();

                        }
                        if (lOrderType.equalsIgnoreCase("ratio diff between sources")) {
                            //sort by the protein ratio
                            Collections.sort(iFilteredProteins, new QuantitativeProteinSorterByDiffProteinMeans((String) cmbRatioTypes.getSelectedItem()));
                            proteinList.updateUI();

                        }

                        if (lOrderType.equalsIgnoreCase("protein Z-score")) {
                            //sort by the protein ratio
                            Collections.sort(iFilteredProteins, new QuantitativeProteinSorterByProteinZscore((String) cmbRatioTypes.getSelectedItem()));
                            proteinList.updateUI();
                        }

                        return true;
                    }

                    public void finished() {
                        //
                        progressBar1.setIndeterminate(true);
                        progressBar1.setVisible(false);
                    }

                };

                lStarter.start();


            }
        });
        cmbRatioTypes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                progressBar1.setIndeterminate(true);
                progressBar1.setVisible(true);
                SwingWorker lStarter = new SwingWorker() {
                    public Boolean construct() {
                        //check if protein ratio is selected in the cmbOrder
                        String lOrderType = (String) cmbOrder.getSelectedItem();
                        if (lOrderType.equalsIgnoreCase("protein ratio")) {
                            //sort by the protein ratio
                            Collections.sort(iFilteredProteins, new QuantitativeProteinSorterByProteinMean((String) cmbRatioTypes.getSelectedItem()));
                            proteinList.updateUI();

                        }
                        if (lOrderType.equalsIgnoreCase("ratio diff between sources")) {
                            //sort by the protein ratio
                            Collections.sort(iFilteredProteins, new QuantitativeProteinSorterByDiffProteinMeans((String) cmbRatioTypes.getSelectedItem()));
                            proteinList.updateUI();

                        }

                        if (lOrderType.equalsIgnoreCase("protein Z-score")) {
                            //sort by the protein ratio
                            Collections.sort(iFilteredProteins, new QuantitativeProteinSorterByProteinZscore((String) cmbRatioTypes.getSelectedItem()));
                            proteinList.updateUI();
                        }

                        return true;
                    }

                    public void finished() {
                        //
                        progressBar1.setIndeterminate(true);
                        progressBar1.setVisible(false);
                    }

                };

                lStarter.start();
            }
        });
        useOriginalCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iQuantitativeValidationSingelton.setUseOriginalRatio(useOriginalCheckBox.isSelected());
                iQuantitativeValidationSingelton.getReferenceSet().clearCalculateReferenceSet();
                loadProtein(false);
            }
        });

    }


    /**
     * This methods loads the ratio information panel, protein bar and the graphs
     *
     * @param lForceUpdate An update will be done if the panels are not empty or if this forced update boolean is true.
     */
    public void loadProtein(boolean lForceUpdate) {


        final boolean aForceUpdate = lForceUpdate;
        if (!iUpdating) {
            iUpdating = true;

            progressBar1.setVisible(true);
            progressBar1.setIndeterminate(true);

            SwingWorker lStarter = new SwingWorker() {
                public Boolean construct() {

                    if ((iChartPanels != null || aForceUpdate)) {
                        //get the protein from the list
                        iProtein = (QuantitativeProtein) proteinList.getSelectedValue();
                        //check if this protein is in the user selection
                        boolean lProteinInSelection = false;
                        for (int i = 0; i < iQuantitativeValidationSingelton.getSelectedProteins().size(); i++) {
                            if (iQuantitativeValidationSingelton.getSelectedProteins().get(i).getAccession().equalsIgnoreCase(iProtein.getAccession())) {
                                lProteinInSelection = true;
                            }
                        }
                        ImageIcon addIcon = new ImageIcon(getClass().getResource("/addSelection.gif"));
                        ImageIcon deleteIcon = new ImageIcon(getClass().getResource("/deleteSelection.gif"));
                        if (lProteinInSelection) {
                            //it's in the selection, it can be deleted from that selection
                            addToSelectionButton.setIcon(deleteIcon);
                            addToSelectionButton.setToolTipText("Delete this protein from the selection");

                        } else {
                            //it's not in the selection, it can be added
                            addToSelectionButton.setIcon(addIcon);
                            addToSelectionButton.setToolTipText("Add this protein to the selection");
                        }
                        if (iProtein.getValidated()) {
                            setValidatedButton.setToolTipText("Set not validated");
                            setValidatedButton.setIcon(new ImageIcon(getClass().getResource("/setNotValidated.gif")));
                        } else {
                            setValidatedButton.setToolTipText("Set validated");
                            setValidatedButton.setIcon(new ImageIcon(getClass().getResource("/setValidated.gif")));
                        }
                        addToSelectionButton.setVisible(true);
                        if (iQuantitativeValidationSingelton.getDatabaseType() != ProteinDatabaseType.LOCAL && iQuantitativeValidationSingelton.getDatabaseType() != ProteinDatabaseType.UNKNOWN) {
                            goToBrowserButton.setVisible(true);
                        }
                        showPossibleIsoformsButton.setVisible(true);
                        setValidatedButton.setVisible(true);

                        //set the protein accession as "title"
                        lblProteinInfo.setText(iProtein.toString());
                        //get the different ratio types for this protein
                        String[] lTypes = iProtein.getTypes();
                        //create a chartpanel array
                        iChartPanels = new JFreeChart[lTypes.length];
                        //remove everyting from the chart panel
                        jpanProteinGraph.removeAll();
                        jpanProteinGraph.setBackground(Color.white);
                        //now add the new charts to the empty panel
                        for (int i = 0; i < lTypes.length; i++) {
                            //get the chart from the protein
                            iChartPanels[i] = iProtein.getChart(iQuantitativeValidationSingelton.getReferenceSet(), lTypes[i], showHuberEstimatedDistributionCheckBox.isSelected(), showRealDistributionCheckBox.isSelected());
                            jpanProteinGraph.add(Box.createVerticalStrut(5));
                            jpanProteinGraph.add(new ChartPanel(iChartPanels[i]));
                            jpanProteinGraph.add(Box.createVerticalStrut(5));
                        }

                        //remove everything from the ratio group information panel
                        jpanProteinRatioGroups.removeAll();
                        jpanProteinRatioGroups.add(Box.createVerticalStrut(5));
                        jpanProteinRatioGroups.setBackground(Color.white);
                        //now add the new info to the empty panel

                        //general protein information

                        JPanel lProteinInfoPanel = new JPanel();
                        lProteinInfoPanel.setLayout(new BoxLayout(lProteinInfoPanel, BoxLayout.Y_AXIS));
                        lProteinInfoPanel.setBackground(Color.WHITE);

                        //1.create the header
                        //1.1 create label
                        JLabel lProteinInfoLabel = new JLabel("Protein info: ");
                        lProteinInfoLabel.setFont(new Font("sansserif", Font.BOLD, 18));
                        lProteinInfoPanel.add(lProteinInfoLabel);
                        lProteinInfoPanel.add(new JLabel("   Protein accession : " + iProtein.getAccession()));
                        lProteinInfoPanel.add(Box.createVerticalStrut(5));
                        for (int i = 0; i < lTypes.length; i++) {
                            lProteinInfoPanel.add(Box.createVerticalStrut(5));
                            lProteinInfoPanel.add(new JLabel("   Protein mean for " + lTypes[i] + " : " + Math.round(iProtein.getProteinRatio(lTypes[i]) * 1000.0) / 1000.0));
                            lProteinInfoPanel.add(Box.createVerticalStrut(5));
                            lProteinInfoPanel.add(new JLabel("   Peptide grouped protein mean for " + lTypes[i] + " : " + Math.round(iProtein.getGroupedProteinRatio(lTypes[i]) * 1000.0) / 1000.0));
                            lProteinInfoPanel.add(Box.createVerticalStrut(5));
                            lProteinInfoPanel.add(new JLabel("   Peptide SD " + lTypes[i] + " : " + Math.round(iProtein.getProteinRatioStandardDeviationForType(lTypes[i]) * 1000.0) / 1000.0));
                            lProteinInfoPanel.add(Box.createVerticalStrut(5));
                            lProteinInfoPanel.add(new JLabel("   Protein Z-score " + lTypes[i] + " : " + (Math.round(iProtein.getProteinZScore(lTypes[i], -1) * 1000.0) / 1000.0)));
                            lProteinInfoPanel.add(Box.createVerticalStrut(5));
                        }
                        //add the label to a temp panel with X axis layout
                        JPanel lTemp = new JPanel();
                        lTemp.setBackground(Color.WHITE);
                        lTemp.setLayout(new BoxLayout(lTemp, BoxLayout.X_AXIS));
                        lTemp.setBackground(Color.WHITE);
                        lTemp.add(Box.createHorizontalStrut(5));
                        lTemp.add(lProteinInfoPanel);
                        lTemp.add(Box.createHorizontalGlue());
                        jpanProteinRatioGroups.add(lTemp);
                        jpanProteinRatioGroups.add(Box.createVerticalStrut(2));

                        //1.2 create a panel with the set comment button
                        JButton setCommentButton = new JButton("");
                        setCommentButton.setIcon(new ImageIcon(getClass().getResource("/pencil.png")));
                        setCommentButton.setToolTipText("Set protein comment");
                        setCommentButton.setContentAreaFilled(false);
                        final JTextField txtComment = new JTextField();
                        txtComment.setText(iProtein.getProteinComment());
                        setCommentButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                iProtein.setProteinComment(txtComment.getText());
                                iQuantitativeValidationSingelton.addCommentedProtein(iProtein);
                                loadProtein(false);
                            }
                        });

                        final JButton hideButton = new JButton("");
                        hideButton.setFocusPainted(false);

                        if (iProtein.isAllPeptideCollapsedStatus()) {
                            hideButton.setIcon(new ImageIcon(getClass().getResource("/show.png")));
                            hideButton.setToolTipText("Show all the peptide ratios");
                        } else {
                            hideButton.setIcon(new ImageIcon(getClass().getResource("/hide.png")));
                            hideButton.setToolTipText("Hide all the peptide ratios");
                        }
                        hideButton.setContentAreaFilled(false);
                        hideButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                if (iProtein.isAllPeptideCollapsedStatus()) {
                                    iProtein.setAllPeptideGroupsCollapsed(false);
                                } else {
                                    iProtein.setAllPeptideGroupsCollapsed(true);
                                }
                                loadProtein(false);
                            }
                        });

                        JPanel lCommentPanel = new JPanel();
                        lCommentPanel.setBackground(Color.WHITE);
                        lCommentPanel.setLayout(new BoxLayout(lCommentPanel, BoxLayout.X_AXIS));
                        lCommentPanel.add(Box.createHorizontalStrut(5));
                        lCommentPanel.add(hideButton);
                        lCommentPanel.add(Box.createHorizontalStrut(5));
                        lCommentPanel.add(setCommentButton);
                        lCommentPanel.add(Box.createHorizontalStrut(5));
                        lCommentPanel.add(txtComment);
                        lCommentPanel.add(Box.createHorizontalGlue());
                        lCommentPanel.setMaximumSize(new Dimension(900, 20));
                        jpanProteinRatioGroups.add(lCommentPanel);
                        jpanProteinRatioGroups.add(Box.createVerticalStrut(2));


                        //2.add a JSeparator
                        jpanProteinRatioGroups.add(new JSeparator());
                        jpanProteinRatioGroups.add(Box.createVerticalStrut(2));


                        //3. information for every peptide sequence grouped ratio
                        //get the peptide groups linked to this protien
                        final Vector<QuantitativePeptideGroup> lPeptideGroups = iProtein.getPeptideGroups(true);

                        progressBar1.setIndeterminate(false);
                        progressBar1.setMaximum(lPeptideGroups.size());
                        progressBar1.setValue(0);

                        for (int i = 0; i < lPeptideGroups.size(); i++) {
                            progressBar1.setValue(i + 1);
                            jpanProteinRatioGroups.add(Box.createVerticalStrut(5));
                            //create the title button
                            JButton lGroupTitleButton = new JButton((i + 1) + ". " + lPeptideGroups.get(i).getFullSequence());
                            lGroupTitleButton.setBorderPainted(false);
                            lGroupTitleButton.setContentAreaFilled(false);
                            lGroupTitleButton.setFocusPainted(false);
                            lGroupTitleButton.setFont(new Font("sansserif", Font.BOLD, 18));
                            if (!lPeptideGroups.get(i).isLinkedToMoreProteins()) {
                                lGroupTitleButton.setForeground(Color.BLUE);
                            } else if (iProtein.getAccession().trim().equalsIgnoreCase(lPeptideGroups.get(i).getRazorAccession().trim())) {
                                lGroupTitleButton.setForeground(Color.RED);
                                lGroupTitleButton.setToolTipText(lPeptideGroups.get(i).getProteinsLinkedToGroupAsString());
                            } else {
                                lGroupTitleButton.setForeground(Color.ORANGE);
                                lGroupTitleButton.setToolTipText(lPeptideGroups.get(i).getProteinsLinkedToGroupAsString());
                            }
                            //Create a checkbox. This checkbox will show if the ratios linked to this sequence will be used in the calculation of the protein mean
                            final JCheckBox lUsePeptides = new JCheckBox();
                            lUsePeptides.setBackground(Color.WHITE);
                            lUsePeptides.setSelected(lPeptideGroups.get(i).isUsedInCalculations());
                            if (iQuantitativeValidationSingelton.isUseOnlyUniqueRatioForProteinMean()) {
                                if (lPeptideGroups.get(i).isLinkedToMoreProteins()) {
                                    lUsePeptides.setEnabled(false);
                                }
                            }

                            final int i1 = i;
                            lUsePeptides.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    if (lUsePeptides.isSelected()) {
                                        lPeptideGroups.get(i1).setUsedInCalculation(true);
                                        iQuantitativeValidationSingelton.deleteNotUsedPeptide(iProtein.getAccession(), lPeptideGroups.get(i1).getSequence());
                                        loadProtein(false);
                                    } else {
                                        lPeptideGroups.get(i1).setUsedInCalculation(false);
                                        iQuantitativeValidationSingelton.addNotUsedPeptide(iProtein.getAccession(), lPeptideGroups.get(i1).getSequence());
                                        loadProtein(false);
                                    }
                                }
                            });

                            //add the label to a temp panel with X axis layout
                            JPanel lTempRatioGroupInfo = new JPanel();
                            lTempRatioGroupInfo.setLayout(new BoxLayout(lTempRatioGroupInfo, BoxLayout.X_AXIS));
                            lTempRatioGroupInfo.setBackground(Color.WHITE);
                            lTempRatioGroupInfo.add(Box.createHorizontalStrut(5));
                            lTempRatioGroupInfo.add(lGroupTitleButton);
                            lTempRatioGroupInfo.add(Box.createHorizontalGlue());
                            lTempRatioGroupInfo.add(lUsePeptides);
                            lTempRatioGroupInfo.add(Box.createHorizontalStrut(5));

                            jpanProteinRatioGroups.add(lTempRatioGroupInfo);

                            for (int j = 0; j < lTypes.length; j++) {
                                Double lMean = lPeptideGroups.get(i).getMeanRatioForGroup(lTypes[j]);
                                if (lMean == null) {
                                    jpanProteinRatioGroups.add(new JLabel("     Peptide group ratio mean (" + lTypes[j] + "): /"));
                                    jpanProteinRatioGroups.add(Box.createVerticalStrut(5));
                                } else {
                                    jpanProteinRatioGroups.add(new JLabel("     Peptide group ratio mean (" + lTypes[j] + "): " + (Math.round(lMean * 1000.0)) / 1000.0));
                                    jpanProteinRatioGroups.add(Box.createVerticalStrut(5));

                                }
                            }

                            //create panel to show the ratiogroups on
                            final JPanel jpanRatioGroup = new JPanel();
                            jpanRatioGroup.setLayout(new BoxLayout(jpanRatioGroup, BoxLayout.Y_AXIS));
                            jpanRatioGroup.setBackground(Color.WHITE);
                            //add the different ratio groups to the panel
                            for (int j = 0; j < lPeptideGroups.get(i).getRatioGroups().size(); j++) {
                                jpanRatioGroup.add(Box.createVerticalStrut(5));
                                //create the ratio group panel
                                RatioGroupPanel lPanel = new RatioGroupPanel(lPeptideGroups.get(i).getRatioGroups().get(j), iConnMsLims, getFrame());
                                //add the created panel to the vector
                                jpanRatioGroup.add(lPanel.getContentPane());
                            }
                            //check if these ratiogroups must be shown
                            if (lPeptideGroups.get(i).isCollapsed()) {
                                jpanRatioGroup.setVisible(false);
                            }
                            jpanProteinRatioGroups.add(jpanRatioGroup);

                            final int i2 = i;
                            lGroupTitleButton.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    jpanRatioGroup.setVisible(!jpanRatioGroup.isVisible());
                                    lPeptideGroups.get(i2).setCollapsed(!lPeptideGroups.get(i2).isCollapsed());
                                }
                            });
                        }

                        final QuantitativeProtein lProteinForWorker = iProtein;
                        //create the protein bar in a new thread because the sequence retrieving can take some time
                        SwingWorker lProteinBarThread = new SwingWorker() {
                            public Boolean construct() {
                                iProteinBar = new ProteinBarPanel(lProteinForWorker);
                                jpanExtraProteinInfo.removeAll();
                                jpanExtraProteinInfo.add(iProteinBar);
                                return true;
                            }

                            public void finished() {
                                iProteinBar.updateUI();
                            }
                        };
                        lProteinBarThread.start();
                        jpanProteinRatioGroups.updateUI();
                        jpanExtraProteinInfo.updateUI();
                        jpanProteinGraph.updateUI();
                    }
                    return true;
                }

                public void finished() {
                    //
                    progressBar1.setIndeterminate(true);
                    progressBar1.setVisible(false);
                    iUpdating = false;
                }

            };

            lStarter.start();
        }
    }

    /**
     * This method finds the rov file names (and puts them in the iRovFiles Vector)
     * that hold ratios and ratio groups linked to the selected proteins
     * Works only in distiller mode
     */
    private void loadRovFileInfo() {
        //only when its in distiller mode
        //remove everything first
        //remove everything from the tree
        iRovFileTreeNod.removeAllChildren();
        //remove everything from the jpanRatioInRovTab panel
        jpanRatioInRovTab.removeAll();
        jpanRatioInRovTab.updateUI();

        //1. Find the rov files
        //select the second tab
        tabbedPane.setSelectedIndex(1);
        //create a vector with all the RatioGroups from the selected proteins
        iSelectedRatioGroups.removeAllElements();
        for (int i = 0; i < iQuantitativeValidationSingelton.getSelectedProteins().size(); i++) {
            QuantitativeProtein lProtein = iQuantitativeValidationSingelton.getSelectedProteins().get(i);
            //get the ratio groups linked to this protein
            Vector<RatioGroup> lRatioGroups = lProtein.getRatioGroups();
            for (int j = 0; j < lRatioGroups.size(); j++) {
                RatioGroup lRatioGroup = lRatioGroups.get(j);
                //check if it's already in the vector
                boolean found = false;
                for (int k = 0; k < iSelectedRatioGroups.size(); k++) {
                    if (iSelectedRatioGroups.get(k).equals(lRatioGroup)) {
                        found = true;
                    }
                }
                if (!found) {
                    if (chbShowOnlyNonValid.isSelected()) {
                        //check if a ratio group linked to this rov file has a ratio that is not valid
                        boolean nonValidFound = false;
                        for (int l = 0; l < lRatioGroup.getNumberOfRatios(); l++) {
                            Ratio lRatio = (Ratio) lRatioGroup.getRatio(l);
                            if (!lRatio.getValid()) {
                                nonValidFound = true;
                            }
                        }
                        if (nonValidFound) {
                            //add it
                            iSelectedRatioGroups.add(lRatioGroups.get(j));
                        }
                    } else {
                        //add it
                        iSelectedRatioGroups.add(lRatioGroups.get(j));
                    }
                }
            }
        }

        //create a vector with rov file titles
        iRovFiles.removeAllElements();
        //try to find to rov files used in the selected DistillerRatioGroups
        for (int i = 0; i < iSelectedRatioGroups.size(); i++) {
            RatioGroup lRatioGroup = iSelectedRatioGroups.get(i);
            //get the rov file name from the ratiogroup
            String lRovFileName = (String) lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME);

            //check if it's a new filename
            boolean found = false;
            for (int j = 0; j < iRovFiles.size(); j++) {
                if (iRovFiles.get(j).equalsIgnoreCase(lRovFileName)) {
                    found = true;
                }
            }
            if (!found) {
                //it's a new rov file name
                iRovFiles.add(lRovFileName);
            }
        }


        //check if we found some rov files
        if (iRovFiles.size() == 0) {
            //nothing rov file found
            //tell this to the user
            JOptionPane.showMessageDialog(this, "No rov files could be found for " + iQuantitativeValidationSingelton.getSelectedProteins().size() + " selected proteins!", "WARNING", JOptionPane.WARNING_MESSAGE);
            iRovFileHits.removeAllElements();
            treeRovFiles.updateUI();
            jpanRatioInRovTab.removeAll();
            return;
        }

        for (int i = 0; i < iRovFiles.size(); i++) {
            DefaultMutableTreeNode rovFile = new DefaultMutableTreeNode(iRovFiles.get(i));
            iRovFileTreeNod.add(rovFile);

            //2. find hits linked to this rov file
            //create a vector with the hits
            iRovFileHits.removeAllElements();
            //try to find to rov files used in the selected DistillerRatioGroups
            for (int j = 0; j < iSelectedRatioGroups.size(); j++) {
                DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) iSelectedRatioGroups.get(j);
                //get the rov file name from the ratiogroup
                String lRovFileName = (String) lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME);

                if (iRovFiles.get(i).equalsIgnoreCase(lRovFileName)) {
                    //this ratio group belongs to this rov file
                    int lHit = lRatioGroup.getReferenceOfParentHit();
                    //check if it's a new hit
                    boolean lOldHit = false;
                    for (int k = 0; k < iRovFileHits.size(); k++) {
                        if (iRovFileHits.get(k) == lHit) {
                            lOldHit = true;
                        }
                    }
                    if (!lOldHit) {
                        //it's a new hit
                        if (chbShowOnlyNonValid.isSelected()) {
                            //check if one of the ratio groups linked to this hit has a ratio that is not valid
                            boolean nonValidFound = false;
                            for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                DistillerRatio lRatio = (DistillerRatio) lRatioGroup.getRatio(k);
                                if (!lRatio.getValid()) {
                                    nonValidFound = true;
                                }
                            }
                            if (nonValidFound) {
                                //add it
                                iRovFileHits.add(lHit);
                            }
                        } else {
                            //add it
                            iRovFileHits.add(lHit);
                        }

                    }
                }
            }
            for (int j = 0; j < iRovFileHits.size(); j++) {
                //3. find the ratiogroups linked to this hit
                //add the hit to the rov file
                DefaultMutableTreeNode hit = new DefaultMutableTreeNode(iRovFileHits.get(j));
                rovFile.add(hit);
                //create a vector with the different ratiogroups linked to this hit
                iHitRatioGroups.removeAllElements();
                //try to find to rov files used in the selected DistillerRatioGroups
                for (int k = 0; k < iSelectedRatioGroups.size(); k++) {
                    DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) iSelectedRatioGroups.get(k);

                    if (lRatioGroup.getReferenceOfParentHit() == iRovFileHits.get(j)) {
                        //this ratio group belongs to this hit
                        iHitRatioGroups.add(lRatioGroup);
                    }
                }
                for (int k = 0; k < iHitRatioGroups.size(); k++) {
                    DefaultMutableTreeNode ratioGroup = new DefaultMutableTreeNode(iHitRatioGroups.get(k));
                    hit.add(ratioGroup);
                }
            }
        }
        this.treeRovFiles.updateUI();
    }

    /**
     * This method gives the JFrame
     *
     * @return JFrame
     */
    private QuantitationValidationGUI getFrame() {
        return this;
    }

    /**
     * This method creates UI components
     */
    private void createUIComponents() {
        //create the order cmb
        String[] lOrderOptions;
        if (iQuantitativeValidationSingelton.isMultipleSources()) {
            lOrderOptions = new String[]{"alphabetical", "# peptide ratios", "protein ratio", "protein Z-score", "ratio diff between sources"};
        } else {
            //lOrderOptions = new String[]{"alphabetical", "# peptide ratios", "protein P value", "protein ratio"};
            lOrderOptions = new String[]{"alphabetical", "# peptide ratios", "protein ratio", "protein Z-score"};
        }
        cmbOrder = new JComboBox(lOrderOptions);
        //create type cmb
        cmbRatioTypes = new JComboBox(iQuantitativeValidationSingelton.getRatioTypes());
        cmbRatioTypes.setSelectedIndex(0);

        //create the protein list
        proteinList = new JList(iFilteredProteins);
        proteinList.setCellRenderer(new ProteinCellRenderer());

        iRovFileTreeNod = new DefaultMutableTreeNode("Rov files");
        treeRovFiles = new JTree(iRovFileTreeNod);
        treeRovFiles.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        TreeSelectionModel treeRovFilesSelectionModel = treeRovFiles.getSelectionModel();
        treeRovFilesSelectionModel.addTreeSelectionListener(new RovFilesTreeSelectionHandler());


        //create jpanels
        jpanProteinGraph = new JPanel();
        jpanProteinGraph.setLayout(new BoxLayout(jpanProteinGraph, BoxLayout.Y_AXIS));
        jpanProteinRatioGroups = new JPanel();
        jpanProteinRatioGroups.setLayout(new BoxLayout(jpanProteinRatioGroups, BoxLayout.Y_AXIS));
        jpanExtraProteinInfo = new JPanel();
        jpanExtraProteinInfo.setLayout(new BoxLayout(jpanExtraProteinInfo, BoxLayout.Y_AXIS));
        jpanRatioInRovTab = new JPanel();
        jpanRatioInRovTab.setLayout(new BoxLayout(jpanRatioInRovTab, BoxLayout.Y_AXIS));
        jpanRatioInRovTab.setBackground(Color.WHITE);

        //create labels
        jLabelTotaleNumber = new JLabel("# proteins : " + iProteins.length);
        jLabelSelected = new JLabel("# selected proteins : " + iQuantitativeValidationSingelton.getSelectedProteins().size());
        jLabelValidated = new JLabel("# validated proteins : " + iQuantitativeValidationSingelton.getValidatedProteins().size());

    }

    /**
     * This method sets the filtered proteins. The protein list is automaticly updated
     *
     * @param aFilteredProteins Vector with "selected" proteins by a filter
     */
    public void setFilteredProteins(Vector<QuantitativeProtein> aFilteredProteins) {
        this.iFilteredProteins.removeAllElements();

        for (int i = 0; i < aFilteredProteins.size(); i++) {
            iFilteredProteins.add(aFilteredProteins.get(i));
        }
        iSelectionVizualised = false;
        this.proteinList.updateUI();
        jpanProteinGraph.removeAll();
        jpanProteinRatioGroups.removeAll();
        jpanExtraProteinInfo.removeAll();

        iChartPanels = null;
        iProteinBar = null;
        addToSelectionButton.setVisible(false);
        goToBrowserButton.setVisible(false);
        showPossibleIsoformsButton.setVisible(false);
        setValidatedButton.setVisible(false);

        lblProteinInfo.setText("No protein selected");
        jLabelSelected.setText("# selected proteins : " + iQuantitativeValidationSingelton.getSelectedProteins().size());
        jLabelValidated.setText("# validated proteins : " + iQuantitativeValidationSingelton.getValidatedProteins().size());
        proteinList.setSelectedIndex(-1);
        this.update(this.getGraphics());

    }


    /**
     * This method loads the ratio groups for the selected hit and rov file
     * Works only in distiller mode
     */
    private void loadRatioGroup(DistillerRatioGroup aRatioGroup) {

        //remove everything from the jpanRatioInRovTab
        jpanRatioInRovTab.removeAll();
        jpanRatioInRovTab.add(Box.createVerticalStrut(5));
        RatioGroupInformationPanel lPanel = new RatioGroupInformationPanel(aRatioGroup, iConnMsLims, getFrame());
        jpanRatioInRovTab.add(lPanel.getContentPane());
        update(getGraphics());
    }

    /**
     * This is the getter for the protein list
     *
     * @return JList
     */
    public JList getProteinList() {
        return proteinList;
    }

    /**
     * This is the getter for the protein bar panel
     *
     * @return ProteinBarPanel
     */
    public ProteinBarPanel getProteinBar() {
        return iProteinBar;
    }

    /**
     * This is the getter for the ChartPanel
     *
     * @return JFreeChart[]
     */
    public JFreeChart[] getChartPanels() {
        return iChartPanels;
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
        contentPane.setBackground(new Color(-1));
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(-1));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(tabbedPane, gbc);
        tabProtein = new JPanel();
        tabProtein.setLayout(new GridBagLayout());
        tabbedPane.addTab("Protein", tabProtein);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setContinuousLayout(true);
        splitPane1.setDividerLocation(172);
        splitPane1.setEnabled(true);
        splitPane1.setOneTouchExpandable(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        tabProtein.add(splitPane1, gbc);
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setBackground(new Color(-1));
        splitPane1.setLeftComponent(leftPanel);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 9;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        leftPanel.add(scrollPane1, gbc);
        proteinList.setSelectionMode(0);
        scrollPane1.setViewportView(proteinList);
        showSelectionButton = new JButton();
        showSelectionButton.setBorderPainted(true);
        showSelectionButton.setContentAreaFilled(false);
        showSelectionButton.setFocusPainted(false);
        showSelectionButton.setIcon(new ImageIcon(getClass().getResource("/viewSelection.gif")));
        showSelectionButton.setIconTextGap(0);
        showSelectionButton.setLabel("");
        showSelectionButton.setText("");
        showSelectionButton.setToolTipText("Show all selected proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        leftPanel.add(showSelectionButton, gbc);
        jLabelTotaleNumber.setEnabled(true);
        jLabelTotaleNumber.setText("# proteins: 99999");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 9;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        leftPanel.add(jLabelTotaleNumber, gbc);
        jLabelValidated.setText("validated");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 9;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        leftPanel.add(jLabelValidated, gbc);
        showAllProteinsButton = new JButton();
        showAllProteinsButton.setContentAreaFilled(false);
        showAllProteinsButton.setFocusPainted(false);
        showAllProteinsButton.setIcon(new ImageIcon(getClass().getResource("/viewAll.gif")));
        showAllProteinsButton.setIconTextGap(0);
        showAllProteinsButton.setLabel("");
        showAllProteinsButton.setText("");
        showAllProteinsButton.setToolTipText("Show all proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        leftPanel.add(showAllProteinsButton, gbc);
        jLabelSelected.setText("# selected proteins : 99999");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 9;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        leftPanel.add(jLabelSelected, gbc);
        addAllProteinsInButton = new JButton();
        addAllProteinsInButton.setContentAreaFilled(false);
        addAllProteinsInButton.setFocusPainted(false);
        addAllProteinsInButton.setIcon(new ImageIcon(getClass().getResource("/plus.gif")));
        addAllProteinsInButton.setIconTextGap(0);
        addAllProteinsInButton.setLabel("");
        addAllProteinsInButton.setText("");
        addAllProteinsInButton.setToolTipText("Add proteins from list to selection");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        leftPanel.add(addAllProteinsInButton, gbc);
        deleteAllProteinsInButton = new JButton();
        deleteAllProteinsInButton.setContentAreaFilled(false);
        deleteAllProteinsInButton.setFocusPainted(false);
        deleteAllProteinsInButton.setIcon(new ImageIcon(getClass().getResource("/min.gif")));
        deleteAllProteinsInButton.setIconTextGap(0);
        deleteAllProteinsInButton.setLabel("");
        deleteAllProteinsInButton.setText("");
        deleteAllProteinsInButton.setToolTipText("Delete proteins in list from selection");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        leftPanel.add(deleteAllProteinsInButton, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 9;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        leftPanel.add(cmbOrder, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.gridwidth = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        leftPanel.add(cmbRatioTypes, gbc);
        lblRatioType = new JLabel();
        lblRatioType.setText("Select type:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        leftPanel.add(lblRatioType, gbc);
        rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBackground(new Color(-1));
        splitPane1.setRightComponent(rightPanel);
        lblProteinInfo = new JLabel();
        lblProteinInfo.setText("No protein selected");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        rightPanel.add(lblProteinInfo, gbc);
        final JSplitPane splitPane2 = new JSplitPane();
        splitPane2.setContinuousLayout(false);
        splitPane2.setDividerLocation(95);
        splitPane2.setDividerSize(6);
        splitPane2.setOneTouchExpandable(true);
        splitPane2.setOrientation(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(splitPane2, gbc);
        jpanExtraProteinInfo.setBackground(new Color(-1));
        splitPane2.setLeftComponent(jpanExtraProteinInfo);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        splitPane2.setRightComponent(panel1);
        final JSplitPane splitPane3 = new JSplitPane();
        splitPane3.setDividerLocation(453);
        splitPane3.setOneTouchExpandable(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(splitPane3, gbc);
        final JScrollPane scrollPane2 = new JScrollPane();
        splitPane3.setLeftComponent(scrollPane2);
        jpanProteinRatioGroups.setBackground(new Color(-1));
        scrollPane2.setViewportView(jpanProteinRatioGroups);
        jpanProteinGraphWrapper = new JPanel();
        jpanProteinGraphWrapper.setLayout(new GridBagLayout());
        splitPane3.setRightComponent(jpanProteinGraphWrapper);
        jpanGraphButtons = new JPanel();
        jpanGraphButtons.setLayout(new GridBagLayout());
        jpanGraphButtons.setBackground(new Color(-1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        jpanProteinGraphWrapper.add(jpanGraphButtons, gbc);
        graphExtenderLeft = new JButton();
        graphExtenderLeft.setContentAreaFilled(false);
        graphExtenderLeft.setFocusPainted(false);
        graphExtenderLeft.setIcon(new ImageIcon(getClass().getResource("/1leftarrow.png")));
        graphExtenderLeft.setLabel("");
        graphExtenderLeft.setMargin(new Insets(3, 3, 3, 3));
        graphExtenderLeft.setText("");
        graphExtenderLeft.setToolTipText("Move the left graph border to the left");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        jpanGraphButtons.add(graphExtenderLeft, gbc);
        graphExtenderRight = new JButton();
        graphExtenderRight.setContentAreaFilled(false);
        graphExtenderRight.setFocusPainted(false);
        graphExtenderRight.setIcon(new ImageIcon(getClass().getResource("/1rightarrow.png")));
        graphExtenderRight.setLabel("");
        graphExtenderRight.setMargin(new Insets(3, 3, 3, 3));
        graphExtenderRight.setText("");
        graphExtenderRight.setToolTipText("Move the right graph border to the right");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        jpanGraphButtons.add(graphExtenderRight, gbc);
        graphExtenderLeftPlus = new JButton();
        graphExtenderLeftPlus.setContentAreaFilled(false);
        graphExtenderLeftPlus.setFocusPainted(false);
        graphExtenderLeftPlus.setIcon(new ImageIcon(getClass().getResource("/1rightarrow.png")));
        graphExtenderLeftPlus.setLabel("");
        graphExtenderLeftPlus.setMargin(new Insets(3, 3, 3, 3));
        graphExtenderLeftPlus.setText("");
        graphExtenderLeftPlus.setToolTipText("Move the left graph border to the right");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanGraphButtons.add(graphExtenderLeftPlus, gbc);
        graphExtenderRightMin = new JButton();
        graphExtenderRightMin.setContentAreaFilled(false);
        graphExtenderRightMin.setFocusPainted(false);
        graphExtenderRightMin.setIcon(new ImageIcon(getClass().getResource("/1leftarrow.png")));
        graphExtenderRightMin.setLabel("");
        graphExtenderRightMin.setMargin(new Insets(3, 3, 3, 3));
        graphExtenderRightMin.setText("");
        graphExtenderRightMin.setToolTipText("Move the right graph border to the left");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanGraphButtons.add(graphExtenderRightMin, gbc);
        showRealDistributionCheckBox = new JCheckBox();
        showRealDistributionCheckBox.setBackground(new Color(-1));
        showRealDistributionCheckBox.setSelected(true);
        showRealDistributionCheckBox.setText("Show \"real\" distribution");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        jpanGraphButtons.add(showRealDistributionCheckBox, gbc);
        showHuberEstimatedDistributionCheckBox = new JCheckBox();
        showHuberEstimatedDistributionCheckBox.setBackground(new Color(-1));
        showHuberEstimatedDistributionCheckBox.setSelected(true);
        showHuberEstimatedDistributionCheckBox.setText("Show Huber estimated distribution");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        jpanGraphButtons.add(showHuberEstimatedDistributionCheckBox, gbc);
        jpanProteinGraph.setBackground(new Color(-1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        jpanProteinGraphWrapper.add(jpanProteinGraph, gbc);
        addToSelectionButton = new JButton();
        addToSelectionButton.setContentAreaFilled(false);
        addToSelectionButton.setFocusPainted(false);
        addToSelectionButton.setIcon(new ImageIcon(getClass().getResource("/addSelection.gif")));
        addToSelectionButton.setMargin(new Insets(3, 3, 3, 3));
        addToSelectionButton.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        rightPanel.add(addToSelectionButton, gbc);
        goToBrowserButton = new JButton();
        goToBrowserButton.setContentAreaFilled(false);
        goToBrowserButton.setFocusPainted(false);
        goToBrowserButton.setIcon(new ImageIcon(getClass().getResource("/homepage.png")));
        goToBrowserButton.setLabel("");
        goToBrowserButton.setMargin(new Insets(3, 3, 3, 3));
        goToBrowserButton.setText("");
        goToBrowserButton.setToolTipText("Open this protein in your default browser");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        rightPanel.add(goToBrowserButton, gbc);
        jpanProteinMeanOptions = new JPanel();
        jpanProteinMeanOptions.setLayout(new GridBagLayout());
        jpanProteinMeanOptions.setBackground(new Color(-1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(jpanProteinMeanOptions, gbc);
        showRovButton = new JButton();
        showRovButton.setContentAreaFilled(false);
        showRovButton.setFocusPainted(false);
        showRovButton.setIcon(new ImageIcon(getClass().getResource("/edu_science.png")));
        showRovButton.setMargin(new Insets(3, 3, 3, 3));
        showRovButton.setText("");
        showRovButton.setToolTipText("Show rov file info for selected proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridheight = 45;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanProteinMeanOptions.add(showRovButton, gbc);
        filterProteinsButton = new JButton();
        filterProteinsButton.setContentAreaFilled(false);
        filterProteinsButton.setFocusPainted(false);
        filterProteinsButton.setIcon(new ImageIcon(getClass().getResource("/wizard.png")));
        filterProteinsButton.setLabel("");
        filterProteinsButton.setMargin(new Insets(3, 3, 3, 3));
        filterProteinsButton.setText("");
        filterProteinsButton.setToolTipText("Filter proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 45;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanProteinMeanOptions.add(filterProteinsButton, gbc);
        miscButton = new JButton();
        miscButton.setContentAreaFilled(false);
        miscButton.setFocusPainted(false);
        miscButton.setIcon(new ImageIcon(getClass().getResource("/misc.png")));
        miscButton.setLabel("");
        miscButton.setMargin(new Insets(3, 3, 3, 3));
        miscButton.setText("");
        miscButton.setToolTipText("Reference set parameters");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 45;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanProteinMeanOptions.add(miscButton, gbc);
        misc_infoButton = new JButton();
        misc_infoButton.setContentAreaFilled(false);
        misc_infoButton.setFocusPainted(false);
        misc_infoButton.setIcon(new ImageIcon(getClass().getResource("/misc_info.png")));
        misc_infoButton.setLabel("");
        misc_infoButton.setMargin(new Insets(3, 3, 3, 3));
        misc_infoButton.setText("");
        misc_infoButton.setToolTipText("Reference set info");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 45;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanProteinMeanOptions.add(misc_infoButton, gbc);
        logButton = new JButton();
        logButton.setContentAreaFilled(false);
        logButton.setFocusPainted(false);
        logButton.setIcon(new ImageIcon(getClass().getResource("/log.png")));
        logButton.setLabel("");
        logButton.setMargin(new Insets(3, 3, 3, 3));
        logButton.setText("");
        logButton.setToolTipText("View log");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridheight = 45;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanProteinMeanOptions.add(logButton, gbc);
        useOnlyValidRatiosCheckBox = new JCheckBox();
        useOnlyValidRatiosCheckBox.setBackground(new Color(-1));
        useOnlyValidRatiosCheckBox.setText("Use only valid ratios");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 43;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanProteinMeanOptions.add(useOnlyValidRatiosCheckBox, gbc);
        useOnlyUniquelyIdentifiedCheckBox = new JCheckBox();
        useOnlyUniquelyIdentifiedCheckBox.setBackground(new Color(-1));
        useOnlyUniquelyIdentifiedCheckBox.setText("Use only uniquely identified (blue) peptides");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanProteinMeanOptions.add(useOnlyUniquelyIdentifiedCheckBox, gbc);
        log2CheckBox = new JCheckBox();
        log2CheckBox.setBackground(new Color(-1));
        log2CheckBox.setText("log 2");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanProteinMeanOptions.add(log2CheckBox, gbc);
        useOriginalCheckBox = new JCheckBox();
        useOriginalCheckBox.setBackground(new Color(-1));
        useOriginalCheckBox.setText("Use original ratios");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 22;
        gbc.gridheight = 22;
        gbc.weightx = 0.1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanProteinMeanOptions.add(useOriginalCheckBox, gbc);
        exportButton = new JButton();
        exportButton.setContentAreaFilled(false);
        exportButton.setFocusPainted(false);
        exportButton.setFocusTraversalPolicyProvider(true);
        exportButton.setIcon(new ImageIcon(getClass().getResource("/filesave.png")));
        exportButton.setLabel("");
        exportButton.setMargin(new Insets(3, 3, 3, 3));
        exportButton.setText("");
        exportButton.setToolTipText("Export proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 0;
        gbc.gridheight = 44;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanProteinMeanOptions.add(exportButton, gbc);
        loadRoverFileButton = new JButton();
        loadRoverFileButton.setContentAreaFilled(false);
        loadRoverFileButton.setFocusPainted(false);
        loadRoverFileButton.setIcon(new ImageIcon(getClass().getResource("/rover.gif")));
        loadRoverFileButton.setLabel("");
        loadRoverFileButton.setMargin(new Insets(3, 3, 3, 3));
        loadRoverFileButton.setText("");
        loadRoverFileButton.setToolTipText("Import a .rover file");
        gbc = new GridBagConstraints();
        gbc.gridx = 12;
        gbc.gridy = 0;
        gbc.gridheight = 44;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanProteinMeanOptions.add(loadRoverFileButton, gbc);
        reconstructButton = new JButton();
        reconstructButton.setContentAreaFilled(false);
        reconstructButton.setIcon(new ImageIcon(getClass().getResource("/recon.png")));
        reconstructButton.setMargin(new Insets(3, 3, 3, 3));
        reconstructButton.setText("");
        reconstructButton.setToolTipText("Reconstruct protein from different sources");
        gbc = new GridBagConstraints();
        gbc.gridx = 11;
        gbc.gridy = 0;
        gbc.gridheight = 44;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanProteinMeanOptions.add(reconstructButton, gbc);
        progressBar1 = new JProgressBar();
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 0;
        gbc.gridheight = 44;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanProteinMeanOptions.add(progressBar1, gbc);
        showPossibleIsoformsButton = new JButton();
        showPossibleIsoformsButton.setContentAreaFilled(false);
        showPossibleIsoformsButton.setFocusPainted(false);
        showPossibleIsoformsButton.setIcon(new ImageIcon(getClass().getResource("/isoform.gif")));
        showPossibleIsoformsButton.setMargin(new Insets(3, 3, 3, 3));
        showPossibleIsoformsButton.setText("");
        showPossibleIsoformsButton.setToolTipText("Show possible isoforms");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        rightPanel.add(showPossibleIsoformsButton, gbc);
        setValidatedButton = new JButton();
        setValidatedButton.setContentAreaFilled(false);
        setValidatedButton.setFocusPainted(false);
        setValidatedButton.setMargin(new Insets(3, 3, 3, 3));
        setValidatedButton.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        rightPanel.add(setValidatedButton, gbc);
        tabRov = new JPanel();
        tabRov.setLayout(new GridBagLayout());
        tabbedPane.addTab("Rov file", tabRov);
        final JSplitPane splitPane4 = new JSplitPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        tabRov.add(splitPane4, gbc);
        splitPane4.setRightComponent(jpanRatioInRovTab);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        panel2.setBackground(new Color(-1));
        splitPane4.setLeftComponent(panel2);
        final JScrollPane scrollPane3 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(scrollPane3, gbc);
        scrollPane3.setViewportView(treeRovFiles);
        chbShowOnlyNonValid = new JCheckBox();
        chbShowOnlyNonValid.setBackground(new Color(-1));
        chbShowOnlyNonValid.setText("Show only false ratios");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(chbShowOnlyNonValid, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }


    class ProteinCellRenderer extends DefaultListCellRenderer {

        final ImageIcon upGreenIcon = new ImageIcon(getClass().getResource("/thumbsUpGreen.gif"));
        final ImageIcon upRedIcon = new ImageIcon(getClass().getResource("/thumbsUpRed.gif"));
        final ImageIcon downGreenIcon = new ImageIcon(getClass().getResource("/thumbsDownGreen.gif"));
        final ImageIcon downRedIcon = new ImageIcon(getClass().getResource("/thumbsDownRed.gif"));


        public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf) {
            QuantitativeProtein lProtein = (QuantitativeProtein) value;
            JLabel lLabel = null;

            if (lProtein.getValidated()) {
                //is validated so green
                if (iQuantitativeValidationSingelton.getSelectedProteins().contains(lProtein)) {
                    //selected
                    lLabel = new JLabel(lProtein.toString(), upGreenIcon, JLabel.LEFT);
                } else {
                    //not selected
                    lLabel = new JLabel(lProtein.toString(), downGreenIcon, JLabel.LEFT);
                }
            } else {
                //not validated so red
                if (iQuantitativeValidationSingelton.getSelectedProteins().contains(lProtein)) {
                    //selected
                    lLabel = new JLabel(lProtein.toString(), upRedIcon, JLabel.LEFT);
                } else {
                    //not selected
                    lLabel = new JLabel(lProtein.toString(), downRedIcon, JLabel.LEFT);
                }
            }
            lLabel.setOpaque(true);
            if (index == 0) {
                lLabel.setBackground(Color.WHITE);
            } else {
                if (index % 2 == 0) {
                    lLabel.setBackground(Color.WHITE);
                } else {
                    lLabel.setBackground(new Color(199, 208, 209));
                }
            }
            if (iss || chf) {
                lLabel.setBackground(new Color(118, 145, 239));
            }

            return lLabel;
        }
    }


    /**
     * This method opens the default browser on a given webpage
     *
     * @param url String with the url
     * @return boolean False if an error occured
     */
    private boolean showInBrowser(String url) {

        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        try {
            if (os.indexOf("win") >= 0) {
                String[] cmd = new String[4];
                cmd[0] = "cmd.exe";
                cmd[1] = "/C";
                cmd[2] = "start";
                cmd[3] = url;
                rt.exec(cmd);
            } else if (os.indexOf("mac") >= 0) {
                rt.exec("open " + url);
            } else {
                //prioritized 'guess' of users' preference
                String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror",
                        "netscape", "opera", "links", "lynx"};

                StringBuffer cmd = new StringBuffer();
                for (int i = 0; i < browsers.length; i++)
                    cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + url + "\" ");

                rt.exec(new String[]{"sh", "-c", cmd.toString()});
                //rt.exec("firefox http://www.google.com");
                //System.out.println(cmd.toString());

            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(new JFrame(), "\n\n The system failed to invoke your default web browser while attempting to access: \n\n " + url + "\n\n", "Browser Error", JOptionPane.WARNING_MESSAGE);
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }


    private class RovFilesTreeSelectionHandler implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeRovFiles.getLastSelectedPathComponent();
            if (node == null) {
                return;
            }
            Object nodeInfo = node.getUserObject();
            if (node.isLeaf() && nodeInfo.getClass() == DistillerRatioGroup.class) {
                DistillerRatioGroup ratioGroup = (DistillerRatioGroup) nodeInfo;
                loadRatioGroup(ratioGroup);
            }

        }
    }

    /**
     * This method will write a string to the log
     *
     * @param aLog String to write
     */
    public void writeToLog(String aLog) {
        iLog.addLog(aLog);
    }
}
