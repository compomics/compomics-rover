package com.compomics.rover.gui.wizard;

import org.apache.log4j.Logger;

import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.interfaces.WizardPanel;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 19-mrt-2009
 * Time: 12:12:18
 */

/**
 * This class will create a panel where parameters can be adapted
 */
public class ParameterPanel implements WizardPanel {
    // Class specific log4j logger for ParameterPanel instances.
    private static Logger logger = Logger.getLogger(ParameterPanel.class);
    private JSpinner spinConfidence;
    private JSpinner spinReference;
    private JCheckBox chbValidInReferenceSet;
    private JRadioButton numberOfMostAbundantRadioButton;
    private JRadioButton allProteinsRadioButton;
    private JRadioButton uniprotRadioButton;
    private JRadioButton IPIRadioButton;
    private JRadioButton NCBIRadioButton;
    private JRadioButton nonOfTheAboveRadioButton;
    private JTextField txtCalibratedSD;
    private JPanel jpanContent;
    private JLabel lblConfidence;
    private JLabel selectANumberOfLabel;
    private JRadioButton TAIRRadioButton;
    private JRadioButton MIPSRadioButton1;
    private JLabel lblValid;
    private JRadioButton localRadioButton;
    private JTextField txtFasta;
    private JButton btnFasta;
    private JLabel lblFasta;
    private JCheckBox chbNormalization;
    private JLabel doRatioNormalizationLabel;
    private JCheckBox chbExcludePeptizer;
    private JLabel lblExcludePeptizerInvalidIdentificationsLabel;
    private JLabel msfFilePeptideConfidence;
    private JRadioButton highRadioButton;
    private JRadioButton mediumRadioButton;
    private JRadioButton lowRadioButton;
    private JLabel msfPeptidesLabel;
    private JRadioButton onlyHighestScoringRadioButton;
    private JRadioButton onlyLowesScoringRadioButton;


    /**
     * The wizard frame holder parent
     */
    private WizardFrameHolder iParent;
    /**
     * The rover source
     */
    private RoverSource iRoverSource;
    /**
     * Boolean that indicates if we can go to the next panel
     */
    private boolean iFeasableToProceed = true;
    /**
     * The reason why we cannot go to the next panel
     */
    private String iNotFeasableReason;
    /**
     * The quantitation validation singelton
     */
    private QuantitativeValidationSingelton iQuantitationSingelton = QuantitativeValidationSingelton.getInstance();

    public ParameterPanel(WizardFrameHolder aParent) {
        this.iParent = aParent;
        $$$setupUI$$$();
        localRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (localRadioButton.isSelected()) {
                    lblFasta.setVisible(true);
                    txtFasta.setVisible(true);
                    btnFasta.setVisible(true);
                }
            }
        });
        btnFasta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //open file chooser
                JFileChooser fc = new JFileChooser();
                //create the file filter to choose
                FileFilter lFilter = new FastaFilter();

                if (iQuantitationSingelton.getFileLocationOpener() != null) {
                    fc.setCurrentDirectory(new File(iQuantitationSingelton.getFileLocationOpener()));
                }
                fc.setFileFilter(lFilter);
                fc.showOpenDialog(iParent);
                File lFile = fc.getSelectedFile();
                txtFasta.setText(lFile.getAbsolutePath());
                iQuantitationSingelton.setFileLocationOpener(lFile.getAbsolutePath());
            }
        });
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
        jpanContent = new JPanel();
        jpanContent.setLayout(new GridBagLayout());
        final JPanel spacer1 = new JPanel();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 26;
        gbc.fill = GridBagConstraints.VERTICAL;
        jpanContent.add(spacer1, gbc);
        lblConfidence = new JLabel();
        lblConfidence.setFont(new Font("Tahoma", lblConfidence.getFont().getStyle(), lblConfidence.getFont().getSize()));
        lblConfidence.setText("- Set peptide identification confidence level");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblConfidence, gbc);
        selectANumberOfLabel = new JLabel();
        selectANumberOfLabel.setFont(new Font("Tahoma", selectANumberOfLabel.getFont().getStyle(), selectANumberOfLabel.getFont().getSize()));
        selectANumberOfLabel.setText("- Select a number of proteins to build the reference set");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(selectANumberOfLabel, gbc);
        numberOfMostAbundantRadioButton = new JRadioButton();
        numberOfMostAbundantRadioButton.setFont(new Font("Tahoma", numberOfMostAbundantRadioButton.getFont().getStyle(), numberOfMostAbundantRadioButton.getFont().getSize()));
        numberOfMostAbundantRadioButton.setSelected(false);
        numberOfMostAbundantRadioButton.setText("Number of most abundant proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(numberOfMostAbundantRadioButton, gbc);
        lblValid = new JLabel();
        lblValid.setFont(new Font("Tahoma", lblValid.getFont().getStyle(), lblValid.getFont().getSize()));
        lblValid.setText("- Ratios from the reference proteins must be true");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblValid, gbc);
        lblExcludePeptizerInvalidIdentificationsLabel = new JLabel();
        lblExcludePeptizerInvalidIdentificationsLabel.setFont(new Font("Tahoma", lblExcludePeptizerInvalidIdentificationsLabel.getFont().getStyle(), lblExcludePeptizerInvalidIdentificationsLabel.getFont().getSize()));
        lblExcludePeptizerInvalidIdentificationsLabel.setText("- Exclude Peptizer invalid identifications");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblExcludePeptizerInvalidIdentificationsLabel, gbc);
        msfFilePeptideConfidence = new JLabel();
        msfFilePeptideConfidence.setFont(new Font("Tahoma", msfFilePeptideConfidence.getFont().getStyle(), msfFilePeptideConfidence.getFont().getSize()));
        msfFilePeptideConfidence.setText("- Msf file peptide confidence level");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(msfFilePeptideConfidence, gbc);
        msfPeptidesLabel = new JLabel();
        msfPeptidesLabel.setFont(new Font("Tahoma", msfPeptidesLabel.getFont().getStyle(), msfPeptidesLabel.getFont().getSize()));
        msfPeptidesLabel.setText("- Msf peptides: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(msfPeptidesLabel, gbc);
        doRatioNormalizationLabel = new JLabel();
        doRatioNormalizationLabel.setFont(new Font("Tahoma", doRatioNormalizationLabel.getFont().getStyle(), doRatioNormalizationLabel.getFont().getSize()));
        doRatioNormalizationLabel.setText("- Do ratio normalization");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(doRatioNormalizationLabel, gbc);
        final JLabel label1 = new JLabel();
        label1.setFont(new Font("Tahoma", label1.getFont().getStyle(), label1.getFont().getSize()));
        label1.setText("- Set protein database type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridheight = 17;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setFont(new Font("Tahoma", label2.getFont().getStyle(), label2.getFont().getSize()));
        label2.setText("- Set the calibrated standard deviation for log2 scale ratios for 1/1 ratio mixtures on the mass spectrometer");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 25;
        gbc.gridwidth = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label2, gbc);
        allProteinsRadioButton = new JRadioButton();
        allProteinsRadioButton.setFont(new Font("Tahoma", allProteinsRadioButton.getFont().getStyle(), allProteinsRadioButton.getFont().getSize()));
        allProteinsRadioButton.setSelected(true);
        allProteinsRadioButton.setText("all proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(allProteinsRadioButton, gbc);
        chbValidInReferenceSet = new JCheckBox();
        chbValidInReferenceSet.setSelected(true);
        chbValidInReferenceSet.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(chbValidInReferenceSet, gbc);
        chbExcludePeptizer = new JCheckBox();
        chbExcludePeptizer.setSelected(true);
        chbExcludePeptizer.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(chbExcludePeptizer, gbc);
        chbNormalization = new JCheckBox();
        chbNormalization.setSelected(false);
        chbNormalization.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(chbNormalization, gbc);
        final JSeparator separator1 = new JSeparator();
        separator1.setOrientation(1);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 15;
        gbc.gridwidth = 2;
        gbc.gridheight = 10;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);
        jpanContent.add(separator1, gbc);
        uniprotRadioButton = new JRadioButton();
        uniprotRadioButton.setFont(new Font("Tahoma", uniprotRadioButton.getFont().getStyle(), uniprotRadioButton.getFont().getSize()));
        uniprotRadioButton.setSelected(true);
        uniprotRadioButton.setText("Uniprot");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 15;
        gbc.gridwidth = 12;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(uniprotRadioButton, gbc);
        localRadioButton = new JRadioButton();
        localRadioButton.setFont(new Font("Tahoma", localRadioButton.getFont().getStyle(), localRadioButton.getFont().getSize()));
        localRadioButton.setText("Local fasta database");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 21;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(localRadioButton, gbc);
        final JSeparator separator2 = new JSeparator();
        separator2.setOrientation(1);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);
        jpanContent.add(separator2, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(spinConfidence, gbc);
        lblFasta = new JLabel();
        lblFasta.setFont(new Font("Tahoma", lblFasta.getFont().getStyle(), lblFasta.getFont().getSize()));
        lblFasta.setText("Select location of FASTA protein database:");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 22;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblFasta, gbc);
        txtFasta = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 23;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(txtFasta, gbc);
        nonOfTheAboveRadioButton = new JRadioButton();
        nonOfTheAboveRadioButton.setFont(new Font("Tahoma", nonOfTheAboveRadioButton.getFont().getStyle(), nonOfTheAboveRadioButton.getFont().getSize()));
        nonOfTheAboveRadioButton.setText("Non of the above");
        gbc = new GridBagConstraints();
        gbc.gridx = 18;
        gbc.gridy = 21;
        gbc.gridwidth = 26;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(nonOfTheAboveRadioButton, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 41;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(spinReference, gbc);
        btnFasta = new JButton();
        btnFasta.setText("Open");
        gbc = new GridBagConstraints();
        gbc.gridx = 41;
        gbc.gridy = 23;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(btnFasta, gbc);
        txtCalibratedSD = new JTextField();
        txtCalibratedSD.setHorizontalAlignment(4);
        txtCalibratedSD.setText("0.14277725");
        gbc = new GridBagConstraints();
        gbc.gridx = 41;
        gbc.gridy = 25;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(txtCalibratedSD, gbc);
        IPIRadioButton = new JRadioButton();
        IPIRadioButton.setFont(new Font("Tahoma", IPIRadioButton.getFont().getStyle(), IPIRadioButton.getFont().getSize()));
        IPIRadioButton.setText("IPI");
        gbc = new GridBagConstraints();
        gbc.gridx = 41;
        gbc.gridy = 15;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(IPIRadioButton, gbc);
        NCBIRadioButton = new JRadioButton();
        NCBIRadioButton.setFont(new Font("Tahoma", NCBIRadioButton.getFont().getStyle(), NCBIRadioButton.getFont().getSize()));
        NCBIRadioButton.setText("NCBI");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 16;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(NCBIRadioButton, gbc);
        TAIRRadioButton = new JRadioButton();
        TAIRRadioButton.setFont(new Font("Tahoma", TAIRRadioButton.getFont().getStyle(), TAIRRadioButton.getFont().getSize()));
        TAIRRadioButton.setText("TAIR");
        gbc = new GridBagConstraints();
        gbc.gridx = 41;
        gbc.gridy = 16;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(TAIRRadioButton, gbc);
        MIPSRadioButton1 = new JRadioButton();
        MIPSRadioButton1.setFont(new Font("Tahoma", MIPSRadioButton1.getFont().getStyle(), MIPSRadioButton1.getFont().getSize()));
        MIPSRadioButton1.setText("mips CYGD");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 19;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(MIPSRadioButton1, gbc);
        highRadioButton = new JRadioButton();
        highRadioButton.setSelected(true);
        highRadioButton.setText("High");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(highRadioButton, gbc);
        mediumRadioButton = new JRadioButton();
        mediumRadioButton.setText("Medium");
        gbc = new GridBagConstraints();
        gbc.gridx = 42;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(mediumRadioButton, gbc);
        lowRadioButton = new JRadioButton();
        lowRadioButton.setText("Low");
        gbc = new GridBagConstraints();
        gbc.gridx = 43;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lowRadioButton, gbc);
        onlyHighestScoringRadioButton = new JRadioButton();
        onlyHighestScoringRadioButton.setSelected(true);
        onlyHighestScoringRadioButton.setText("Only highest scoring");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(onlyHighestScoringRadioButton, gbc);
        onlyLowesScoringRadioButton = new JRadioButton();
        onlyLowesScoringRadioButton.setText("Only lowest scoring");
        gbc = new GridBagConstraints();
        gbc.gridx = 42;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(onlyLowesScoringRadioButton, gbc);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(allProteinsRadioButton);
        buttonGroup.add(numberOfMostAbundantRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(uniprotRadioButton);
        buttonGroup.add(IPIRadioButton);
        buttonGroup.add(NCBIRadioButton);
        buttonGroup.add(TAIRRadioButton);
        buttonGroup.add(MIPSRadioButton1);
        buttonGroup.add(localRadioButton);
        buttonGroup.add(nonOfTheAboveRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(highRadioButton);
        buttonGroup.add(mediumRadioButton);
        buttonGroup.add(lowRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(onlyHighestScoringRadioButton);
        buttonGroup.add(onlyLowesScoringRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }

    /**
     * A .fasta file filter
     */
    class FastaFilter extends FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt") || f.getName().toLowerCase().endsWith(".fasta") || f.getName().toLowerCase().endsWith(".fas");
        }

        public String getDescription() {
            return ".txt, .fasta, .fas files";
        }
    }

    /**
     * {@inheritDoc}
     */
    public JPanel getContentPane() {
        return jpanContent;
    }

    /**
     * {@inheritDoc}
     */
    public void backClicked() {
        /*iParent.setDatabaseType(null);
        iParent.setUseAllProteinsForReferenceSet(true);
        iParent.setReferenceSetSize(0);
        iParent.setCalibratedStdev(0.0);
        iParent.setRatioValidInReferenceSet(true);
         */
        iFeasableToProceed = true;
        iNotFeasableReason = null;
    }

    /**
     * {@inheritDoc}
     */
    public void nextClicked() {
        //set all the feasibility back to normal
        iFeasableToProceed = true;
        iNotFeasableReason = null;

        //Set the database type
        if (uniprotRadioButton.isSelected()) {
            iParent.setDatabaseType(ProteinDatabaseType.UNIPROT);
        }
        if (IPIRadioButton.isSelected()) {
            iParent.setDatabaseType(ProteinDatabaseType.IPI);
        }
        if (NCBIRadioButton.isSelected()) {
            iParent.setDatabaseType(ProteinDatabaseType.NCBI);
        }
        if (TAIRRadioButton.isSelected()) {
            iParent.setDatabaseType(ProteinDatabaseType.TAIR);
        }
        if (MIPSRadioButton1.isSelected()) {
            iParent.setDatabaseType(ProteinDatabaseType.MIPS_CYGD);
        }
        if (localRadioButton.isSelected()) {
            File lFasta = new File(txtFasta.getText());
            if (!lFasta.exists()) {
                //fasta file was not found
                iFeasableToProceed = false;
                iNotFeasableReason = "Fasta protein database not found.";
                return;
            } else {
                iParent.setFastaDatabase(txtFasta.getText());
                iParent.setDatabaseType(ProteinDatabaseType.LOCAL);
            }
        }
        if (nonOfTheAboveRadioButton.isSelected()) {
            iParent.setDatabaseType(ProteinDatabaseType.UNKNOWN);
        }
        //get the reference set size
        int lReferenceSetSize = (Integer) spinReference.getValue();
        boolean lUseAllProteins = allProteinsRadioButton.isSelected();
        iParent.setUseAllProteinsForReferenceSet(lUseAllProteins);
        iParent.setReferenceSetSize(lReferenceSetSize);
        if (!iParent.getUseMs_lims()) {
            iParent.setThreshold((Double) spinConfidence.getValue());
        }
        //set RatioValidInReferenceSet
        iParent.setRatioValidInReferenceSet(chbValidInReferenceSet.isSelected());
        //set the ratio normalization boolean
        iParent.setRatioNormalization(chbNormalization.isSelected());
        //set the peptizer status
        iParent.setPeptizerStatus(chbExcludePeptizer.isSelected());
        //set the msf peptide confidence level
        if (iParent.getRoverSource() != RoverSource.THERMO_MSF_FILES) {
            if (highRadioButton.isSelected()) {
                iParent.setMsfPeptideConfidence(3);
            } else if (mediumRadioButton.isSelected()) {
                iParent.setMsfPeptideConfidence(2);
            } else if (lowRadioButton.isSelected()) {
                iParent.setMsfPeptideConfidence(1);
            }
            if (onlyHighestScoringRadioButton.isSelected()) {
                iParent.setMsfOnlyHighesScoring(true);
            } else {
                iParent.setMsfOnlyHighesScoring(false);
            }
        }

        //Set the calibrated SD
        double lCalibratedSD = 0.0;
        try {
            //check if the calibrated SD is a double
            lCalibratedSD = Double.valueOf(txtCalibratedSD.getText());
            iParent.setCalibratedStdev(lCalibratedSD);
        } catch (Exception e) {
            iFeasableToProceed = false;
            iNotFeasableReason = "\"" + txtCalibratedSD.getText() + "\" is not a correct standard deviation.";
            return;
        }


    }

    /**
     * {@inheritDoc}
     */
    public boolean feasableToProceed() {
        return iFeasableToProceed;
    }

    /**
     * {@inheritDoc}
     */
    public String getNotFeasableReason() {
        return iNotFeasableReason;
    }

    /**
     * {@inheritDoc}
     */
    public void construct() {
        if (iParent.getUseMs_lims()) {
            //if we use ms_lims we don't need to set the confidence
            lblConfidence.setVisible(false);
            spinConfidence.setVisible(false);
            lblExcludePeptizerInvalidIdentificationsLabel.setVisible(true);
            chbExcludePeptizer.setVisible(true);
            msfFilePeptideConfidence.setVisible(false);
            highRadioButton.setVisible(false);
            mediumRadioButton.setVisible(false);
            lowRadioButton.setVisible(false);
            msfPeptidesLabel.setVisible(false);
            onlyHighestScoringRadioButton.setVisible(false);
            onlyLowesScoringRadioButton.setVisible(false);
        } else {
            lblExcludePeptizerInvalidIdentificationsLabel.setVisible(false);
            chbExcludePeptizer.setVisible(false);
            if (iParent.getRoverSource() != RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS && iParent.getRoverSource() != RoverSource.MAX_QUANT_NO_SIGN && iParent.getRoverSource() != RoverSource.MAX_QUANT && iParent.getRoverSource() != RoverSource.MS_QUANT && iParent.getRoverSource() != RoverSource.CENSUS) {
                lblConfidence.setVisible(true);
                spinConfidence.setVisible(true);
            } else {
                lblValid.setVisible(false);
                chbValidInReferenceSet.setVisible(false);
                lblConfidence.setVisible(false);
                spinConfidence.setVisible(false);
            }
            if (iParent.getRoverSource() != RoverSource.THERMO_MSF_FILES) {
                msfFilePeptideConfidence.setVisible(false);
                highRadioButton.setVisible(false);
                mediumRadioButton.setVisible(false);
                lowRadioButton.setVisible(false);
                msfPeptidesLabel.setVisible(false);
                onlyHighestScoringRadioButton.setVisible(false);
                onlyLowesScoringRadioButton.setVisible(false);
            } else {
                msfFilePeptideConfidence.setVisible(true);
                highRadioButton.setVisible(true);
                mediumRadioButton.setVisible(true);
                lowRadioButton.setVisible(true);
                msfPeptidesLabel.setVisible(true);
                onlyHighestScoringRadioButton.setVisible(true);
                onlyLowesScoringRadioButton.setVisible(true);
                lblValid.setVisible(false);
                chbValidInReferenceSet.setVisible(false);
                lblConfidence.setVisible(false);
                spinConfidence.setVisible(false);
            }
        }
        lblFasta.setVisible(false);
        txtFasta.setEditable(false);
        txtFasta.setVisible(false);
        btnFasta.setVisible(false);
        doRatioNormalizationLabel.setVisible(false);
        chbNormalization.setVisible(false);
    }

    public void createUIComponents() {
        spinConfidence = new JSpinner(new SpinnerNumberModel(0.99, 0.50, 0.995, 0.005));
        spinReference = new JSpinner(new SpinnerNumberModel(100, 20, 300, 1));
    }

}
