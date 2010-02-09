package com.compomics.rover.gui.wizard;

import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.interfaces.WizardPanel;

import javax.swing.*;
import java.awt.*;

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

    public ParameterPanel(WizardFrameHolder aParent) {
        this.iParent = aParent;
        $$$setupUI$$$();
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
        } else {
            if (iParent.getRoverSource() != RoverSource.MAX_QUANT_NO_SIGN && iParent.getRoverSource() != RoverSource.MAX_QUANT && iParent.getRoverSource() != RoverSource.MS_QUANT && iParent.getRoverSource() != RoverSource.CENSUS) {
                lblConfidence.setVisible(true);
                spinConfidence.setVisible(true);
            } else {
                lblValid.setVisible(false);
                chbValidInReferenceSet.setVisible(false);
                lblConfidence.setVisible(false);
                spinConfidence.setVisible(false);
            }
        }
    }

    public void createUIComponents() {
        spinConfidence = new JSpinner(new SpinnerNumberModel(0.95, 0.50, 0.99, 0.01));
        spinReference = new JSpinner(new SpinnerNumberModel(100, 20, 300, 1));
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
        gbc.gridy = 19;
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
        numberOfMostAbundantRadioButton.setSelected(true);
        numberOfMostAbundantRadioButton.setText("Number of most abundant proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(numberOfMostAbundantRadioButton, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 19;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(spinReference, gbc);
        lblValid = new JLabel();
        lblValid.setFont(new Font("Tahoma", lblValid.getFont().getStyle(), lblValid.getFont().getSize()));
        lblValid.setText("- Ratios from the reference proteins must be true");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblValid, gbc);
        final JLabel label1 = new JLabel();
        label1.setFont(new Font("Tahoma", label1.getFont().getStyle(), label1.getFont().getSize()));
        label1.setText("- Set protein database type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridheight = 14;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label1, gbc);
        txtCalibratedSD = new JTextField();
        txtCalibratedSD.setHorizontalAlignment(4);
        txtCalibratedSD.setText("0.14277725");
        gbc = new GridBagConstraints();
        gbc.gridx = 19;
        gbc.gridy = 18;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(txtCalibratedSD, gbc);
        final JLabel label2 = new JLabel();
        label2.setFont(new Font("Tahoma", label2.getFont().getStyle(), label2.getFont().getSize()));
        label2.setText("- Set the calibrated standard deviation for log2 scale ratios for 1/1 ratio mixtures on the mass spectrometer");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 18;
        gbc.gridwidth = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label2, gbc);
        allProteinsRadioButton = new JRadioButton();
        allProteinsRadioButton.setFont(new Font("Tahoma", allProteinsRadioButton.getFont().getStyle(), allProteinsRadioButton.getFont().getSize()));
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
        final JSeparator separator1 = new JSeparator();
        separator1.setOrientation(1);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.gridheight = 7;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);
        jpanContent.add(separator1, gbc);
        uniprotRadioButton = new JRadioButton();
        uniprotRadioButton.setFont(new Font("Tahoma", uniprotRadioButton.getFont().getStyle(), uniprotRadioButton.getFont().getSize()));
        uniprotRadioButton.setSelected(true);
        uniprotRadioButton.setText("Uniprot");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 11;
        gbc.gridwidth = 12;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(uniprotRadioButton, gbc);
        IPIRadioButton = new JRadioButton();
        IPIRadioButton.setFont(new Font("Tahoma", IPIRadioButton.getFont().getStyle(), IPIRadioButton.getFont().getSize()));
        IPIRadioButton.setText("IPI");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 12;
        gbc.gridwidth = 24;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(IPIRadioButton, gbc);
        NCBIRadioButton = new JRadioButton();
        NCBIRadioButton.setFont(new Font("Tahoma", NCBIRadioButton.getFont().getStyle(), NCBIRadioButton.getFont().getSize()));
        NCBIRadioButton.setText("NCBI");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 14;
        gbc.gridwidth = 24;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(NCBIRadioButton, gbc);
        TAIRRadioButton = new JRadioButton();
        TAIRRadioButton.setFont(new Font("Tahoma", TAIRRadioButton.getFont().getStyle(), TAIRRadioButton.getFont().getSize()));
        TAIRRadioButton.setText("TAIR");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 15;
        gbc.gridwidth = 24;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(TAIRRadioButton, gbc);
        MIPSRadioButton1 = new JRadioButton();
        MIPSRadioButton1.setFont(new Font("Tahoma", MIPSRadioButton1.getFont().getStyle(), MIPSRadioButton1.getFont().getSize()));
        MIPSRadioButton1.setText("mips CYGD");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 16;
        gbc.gridwidth = 24;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(MIPSRadioButton1, gbc);
        nonOfTheAboveRadioButton = new JRadioButton();
        nonOfTheAboveRadioButton.setFont(new Font("Tahoma", nonOfTheAboveRadioButton.getFont().getStyle(), nonOfTheAboveRadioButton.getFont().getSize()));
        nonOfTheAboveRadioButton.setText("Non of the above");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 17;
        gbc.gridwidth = 24;
        gbc.anchor = GridBagConstraints.WEST;
        jpanContent.add(nonOfTheAboveRadioButton, gbc);
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
        buttonGroup.add(nonOfTheAboveRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }
}
