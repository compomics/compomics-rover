package com.compomics.rover.gui.multiwizard;

import org.apache.log4j.Logger;

import com.compomics.rover.general.interfaces.WizardPanel;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.gui.multiwizard.WizardFrameHolder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 19-mrt-2009
 * Time: 9:46:52
 */

/**
 * This class creates a panel where the type of the sources can be set
 */
public class RoverSourcePanel implements WizardPanel {
    // Class specific log4j logger for RoverSourcePanel instances.
    private static Logger logger = Logger.getLogger(RoverSourcePanel.class);

    //gui stuff
    private JRadioButton distillerQuantitationToolboxRovRadioButton;
    private JRadioButton distillerQuantitationToolboxMsLimsRadioButton;
    private JPanel jpanContent;
    private JRadioButton msQuantTxtFilesRadioButton;
    private JRadioButton maxQuantRadioButton;
    private JRadioButton censusOutTxtAndRadioButton;
    private JTextField txtTitle;
    private JRadioButton maxQuantMsLims;


    /**
     * The wizard frame holder parent
     */
    private WizardFrameHolder iParent;
    /**
     * Boolean that indicates if we can go to the next panel
     */
    private boolean iFeasableToProceed = true;
    /**
     * The reason why we cannot go to the next panel
     */
    private String iNotFeasableReason;

    /**
     * Consturctor
     *
     * @param aParent The parent
     */
    public RoverSourcePanel(WizardFrameHolder aParent) {
        this.iParent = aParent;
        $$$setupUI$$$();
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iParent.setNextButtonEnabled(true);
            }
        };
        distillerQuantitationToolboxRovRadioButton.addActionListener(listener);
        distillerQuantitationToolboxMsLimsRadioButton.addActionListener(listener);
        msQuantTxtFilesRadioButton.addActionListener(listener);
        maxQuantRadioButton.addActionListener(listener);
        censusOutTxtAndRadioButton.addActionListener(listener);
        maxQuantMsLims.addActionListener(listener);
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
        //set the datatype on the parent as null
        //iParent.setRoverSource(null);
        //set feasable to proceed
        iFeasableToProceed = true;
        iNotFeasableReason = null;
    }

    /**
     * {@inheritDoc}
     */
    public void nextClicked() {
        //get the rover source
        RoverSource lSource = null;
        if (distillerQuantitationToolboxMsLimsRadioButton.isSelected()) {
            lSource = RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS;
        } else if (distillerQuantitationToolboxRovRadioButton.isSelected()) {
            lSource = RoverSource.DISTILLER_QUANT_TOOLBOX_ROV;
        } else if (msQuantTxtFilesRadioButton.isSelected()) {
            lSource = RoverSource.MS_QUANT;
        } else if (maxQuantRadioButton.isSelected()) {
            lSource = RoverSource.MAX_QUANT;
        } else if (censusOutTxtAndRadioButton.isSelected()) {
            lSource = RoverSource.CENSUS;
        } else if (maxQuantMsLims.isSelected()) {
            lSource = RoverSource.MAX_QUANT_MS_LIMS;
        }

        //check if anything was selected
        if (lSource == null) {
            //nothing is selected, warn the user
            iFeasableToProceed = false;
            iNotFeasableReason = "No source was selected";
            return;
        } else {
            if (txtTitle.getText().length() == 0) {
                iNotFeasableReason = "Set source title";
                iFeasableToProceed = false;
                return;
            } else {
                iFeasableToProceed = true;
                iNotFeasableReason = null;
            }
        }


        //set the data type
        iParent.setCurrentRoverSource(lSource);
        iParent.addRoverSource(lSource);
        iParent.addTitle(txtTitle.getText());
        txtTitle.setText("");

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
        logger.info("Multiple source method started");
        iParent.setNextButtonEnabled(false);
        if (iParent.getRoverSources().size() != 0) {
            if (iParent.getUseMs_lims()) {
                distillerQuantitationToolboxRovRadioButton.setEnabled(false);
                msQuantTxtFilesRadioButton.setEnabled(false);
                maxQuantRadioButton.setEnabled(false);
                censusOutTxtAndRadioButton.setEnabled(false);
            } else {
                distillerQuantitationToolboxMsLimsRadioButton.setEnabled(false);
                maxQuantMsLims.setEnabled(false);
            }
        }
        if (distillerQuantitationToolboxMsLimsRadioButton.isSelected()) {
            iParent.setNextButtonEnabled(true);
        } else if (distillerQuantitationToolboxRovRadioButton.isSelected()) {
            iParent.setNextButtonEnabled(true);
        } else if (msQuantTxtFilesRadioButton.isSelected()) {
            iParent.setNextButtonEnabled(true);
        } else if (maxQuantRadioButton.isSelected()) {
            iParent.setNextButtonEnabled(true);
        } else if (censusOutTxtAndRadioButton.isSelected()) {
            iParent.setNextButtonEnabled(true);
        } else if (maxQuantMsLims.isSelected()) {
            iParent.setNextButtonEnabled(true);
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
        jpanContent = new JPanel();
        jpanContent.setLayout(new GridBagLayout());
        jpanContent.setFont(new Font("Tahoma", jpanContent.getFont().getStyle(), jpanContent.getFont().getSize()));
        distillerQuantitationToolboxRovRadioButton = new JRadioButton();
        distillerQuantitationToolboxRovRadioButton.setFont(new Font("Tahoma", distillerQuantitationToolboxRovRadioButton.getFont().getStyle(), distillerQuantitationToolboxRovRadioButton.getFont().getSize()));
        distillerQuantitationToolboxRovRadioButton.setText("Mascot distiller quantitation toolbox (.rov) files");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(distillerQuantitationToolboxRovRadioButton, gbc);
        distillerQuantitationToolboxMsLimsRadioButton = new JRadioButton();
        distillerQuantitationToolboxMsLimsRadioButton.setFont(new Font("Tahoma", distillerQuantitationToolboxMsLimsRadioButton.getFont().getStyle(), distillerQuantitationToolboxMsLimsRadioButton.getFont().getSize()));
        distillerQuantitationToolboxMsLimsRadioButton.setText("Mascot distiller quantitation toolbox quantitation from ms_lims");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(distillerQuantitationToolboxMsLimsRadioButton, gbc);
        final JLabel label1 = new JLabel();
        label1.setFont(new Font("Tahoma", label1.getFont().getStyle(), label1.getFont().getSize()));
        label1.setHorizontalAlignment(4);
        label1.setText("Select the information source:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 9;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.ipadx = 20;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label1, gbc);
        msQuantTxtFilesRadioButton = new JRadioButton();
        msQuantTxtFilesRadioButton.setFont(new Font("Tahoma", msQuantTxtFilesRadioButton.getFont().getStyle(), msQuantTxtFilesRadioButton.getFont().getSize()));
        msQuantTxtFilesRadioButton.setText("MsQuant (.txt) files");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(msQuantTxtFilesRadioButton, gbc);
        maxQuantRadioButton = new JRadioButton();
        maxQuantRadioButton.setFont(new Font("Tahoma", maxQuantRadioButton.getFont().getStyle(), maxQuantRadioButton.getFont().getSize()));
        maxQuantRadioButton.setText("MaxQuant evidence.txt and msms.txt files");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(maxQuantRadioButton, gbc);
        maxQuantMsLims = new JRadioButton();
        maxQuantMsLims.setFont(new Font("Tahoma", maxQuantMsLims.getFont().getStyle(), maxQuantMsLims.getFont().getSize()));
        maxQuantMsLims.setText("MaxQuant quantifications from ms_lims");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(maxQuantMsLims, gbc);
        censusOutTxtAndRadioButton = new JRadioButton();
        censusOutTxtAndRadioButton.setFont(new Font("Tahoma", censusOutTxtAndRadioButton.getFont().getStyle(), censusOutTxtAndRadioButton.getFont().getSize()));
        censusOutTxtAndRadioButton.setText("Census out (.txt) and Census chro (.xml) files");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(censusOutTxtAndRadioButton, gbc);
        final JLabel label2 = new JLabel();
        label2.setHorizontalAlignment(11);
        label2.setText("Title:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.ipadx = 20;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label2, gbc);
        txtTitle = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(txtTitle, gbc);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(distillerQuantitationToolboxRovRadioButton);
        buttonGroup.add(distillerQuantitationToolboxMsLimsRadioButton);
        buttonGroup.add(msQuantTxtFilesRadioButton);
        buttonGroup.add(maxQuantRadioButton);
        buttonGroup.add(maxQuantMsLims);
        buttonGroup.add(censusOutTxtAndRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }
}
