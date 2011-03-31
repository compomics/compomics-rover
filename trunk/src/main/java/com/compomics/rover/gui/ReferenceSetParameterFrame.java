package com.compomics.rover.gui;

import com.compomics.rover.general.enumeration.ReferenceSetEnum;
import org.apache.log4j.Logger;

import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.quantitation.ReferenceSet;
import com.compomics.rover.general.quantitation.QuantitativeProtein;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 26-mrt-2009
 * Time: 13:09:58
 */
public class ReferenceSetParameterFrame extends JFrame {
    // Class specific log4j logger for ReferenceSetParameterFrame instances.
    private static Logger logger = Logger.getLogger(ReferenceSetParameterFrame.class);
    private JPanel jpanContent;
    private JRadioButton useAllProteinsRadioButton;
    private JRadioButton useMostAbundantProteinsRadioButton;
    private JSpinner spinner1;
    private JCheckBox checkBox1;
    private JButton createReferenceSetButton;
    private JRadioButton useAllProteinsWithRadioButton;
    private JTextField txtAccessionsOnly;
    private JTextField txtAllAccessionsExcept;
    private JRadioButton useAllProteinsExceptRadioButton;
    /**
     * This validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
    /**
     * All the proteins
     */
    private QuantitativeProtein[] iProteins;
    /**
     * The parent frame
     */
    private QuantitationValidationGUI iParent;

    public ReferenceSetParameterFrame(QuantitativeProtein[] aProteins, QuantitationValidationGUI aParent) {
        this.iProteins = aProteins;
        this.iParent = aParent;


        $$$setupUI$$$();

        //set the checbox selected
        checkBox1.setSelected(iQuantitativeValidationSingelton.isRatioValidInReferenceSet());
        //set the use all proteins selected
        if (iQuantitativeValidationSingelton.getReferenceSetEnum() == ReferenceSetEnum.ALL) {
            useAllProteinsRadioButton.setSelected(true);
        } else if (iQuantitativeValidationSingelton.getReferenceSetEnum() == ReferenceSetEnum.MOST_ABUNDANT) {
            useMostAbundantProteinsRadioButton.setSelected(true);
        } else if (iQuantitativeValidationSingelton.getReferenceSetEnum() == ReferenceSetEnum.ONLY_ACCESSIONS) {
            useAllProteinsWithRadioButton.setSelected(true);
            txtAccessionsOnly.setText(iQuantitativeValidationSingelton.getReferenceSetSpecialAccessions());
        } else if (iQuantitativeValidationSingelton.getReferenceSetEnum() == ReferenceSetEnum.ALL_EXCEPT_ACCESSIONS) {
            useAllProteinsExceptRadioButton.setSelected(true);
            txtAllAccessionsExcept.setText(iQuantitativeValidationSingelton.getReferenceSetSpecialAccessions());
        }
        //set the number of proteins to use
        spinner1.setValue(iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet());

        this.setTitle("Reference set parameters");
        this.setContentPane(jpanContent);
        this.pack();
        this.setLocation(400, 400);
        this.setVisible(true);
        createReferenceSetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //set the changed things
                iQuantitativeValidationSingelton.setNumberOfProteinsInReferenceSet((Integer) spinner1.getValue());
                iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(checkBox1.isSelected());
                String lSpecialAccessions = "";
                if (useAllProteinsRadioButton.isSelected()) {
                    iQuantitativeValidationSingelton.setReferenceSetEnum(ReferenceSetEnum.ALL);
                } else if (useMostAbundantProteinsRadioButton.isSelected()) {
                    iQuantitativeValidationSingelton.setReferenceSetEnum(ReferenceSetEnum.MOST_ABUNDANT);
                } else if (useAllProteinsWithRadioButton.isSelected()) {
                    iQuantitativeValidationSingelton.setReferenceSetEnum(ReferenceSetEnum.ONLY_ACCESSIONS);
                    iQuantitativeValidationSingelton.setReferenceSetSpecialAccessions(txtAccessionsOnly.getText());
                    lSpecialAccessions = "," + txtAccessionsOnly.getText().trim() + ",";
                    if (lSpecialAccessions.length() == 0) {
                        JOptionPane.showMessageDialog(new JFrame(), "No accessions were found, give the accessions in the correct text field", "No accessions found!", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                } else if (useAllProteinsExceptRadioButton.isSelected()) {
                    iQuantitativeValidationSingelton.setReferenceSetEnum(ReferenceSetEnum.ALL_EXCEPT_ACCESSIONS);
                    iQuantitativeValidationSingelton.setReferenceSetSpecialAccessions(txtAllAccessionsExcept.getText());
                    lSpecialAccessions = "," + txtAllAccessionsExcept.getText().trim() + ",";
                    if (lSpecialAccessions.length() == 0) {
                        JOptionPane.showMessageDialog(new JFrame(), "No accessions were found, give the accessions in the correct text field", "No accessions found!", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                //create a reference set
                ReferenceSet lReferenceSet = new ReferenceSet(new ArrayList<QuantitativeProtein>(), iQuantitativeValidationSingelton.getReferenceSet().getTypes(), iQuantitativeValidationSingelton.getReferenceSet().getComponents());

                int lReferenceSetSize = iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet();
                if (iQuantitativeValidationSingelton.getReferenceSetEnum() == ReferenceSetEnum.ALL || iQuantitativeValidationSingelton.getReferenceSetEnum() == ReferenceSetEnum.ONLY_ACCESSIONS || iQuantitativeValidationSingelton.getReferenceSetEnum() == ReferenceSetEnum.ALL_EXCEPT_ACCESSIONS) {
                    lReferenceSetSize = iProteins.length;
                }
                for (int i = 0; i < lReferenceSetSize; i++) {
                    if (iQuantitativeValidationSingelton.getReferenceSetEnum() == ReferenceSetEnum.ONLY_ACCESSIONS) {
                        if (lSpecialAccessions.indexOf("," + iProteins[i].getAccession() + ",") >= 0) {
                            //we've found it
                            lReferenceSet.addReferenceProtein(iProteins[i]);
                        }

                    } else if (iQuantitativeValidationSingelton.getReferenceSetEnum() == ReferenceSetEnum.ALL_EXCEPT_ACCESSIONS) {
                        if (lSpecialAccessions.indexOf("," + iProteins[i].getAccession() + ",") == -1) {
                            //we didn't  found it
                            lReferenceSet.addReferenceProtein(iProteins[i]);
                        }
                    } else {
                        lReferenceSet.addReferenceProtein(iProteins[i]);
                    }
                }
                if (lReferenceSet.getUsedProteinsNumber() == 0) {
                    JOptionPane.showMessageDialog(new JFrame(), "No proteins were added to the new reference set.\nPlease select a different option.", "Reference set creation error!", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                iQuantitativeValidationSingelton.setReferenceSet(lReferenceSet);
                iParent.loadProtein(false);
                //close
                setVisible(false);
                dispose();
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
        jpanContent = new JPanel();
        jpanContent.setLayout(new GridBagLayout());
        final JLabel label1 = new JLabel();
        label1.setText("Select a number of proteins to build the reference set");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label1, gbc);
        useAllProteinsRadioButton = new JRadioButton();
        useAllProteinsRadioButton.setSelected(true);
        useAllProteinsRadioButton.setText("Use all proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(useAllProteinsRadioButton, gbc);
        useMostAbundantProteinsRadioButton = new JRadioButton();
        useMostAbundantProteinsRadioButton.setText("Use most abundant proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(useMostAbundantProteinsRadioButton, gbc);
        spinner1 = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(spinner1, gbc);
        final JLabel label2 = new JLabel();
        label2.setFont(new Font("Tahoma", label2.getFont().getStyle(), label2.getFont().getSize()));
        label2.setText("Ratios from the reference proteins must be true");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label2, gbc);
        checkBox1 = new JCheckBox();
        checkBox1.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(checkBox1, gbc);
        createReferenceSetButton = new JButton();
        createReferenceSetButton.setText("Create reference set");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(createReferenceSetButton, gbc);
        useAllProteinsWithRadioButton = new JRadioButton();
        useAllProteinsWithRadioButton.setText("Use all proteins with accessions (comma seperated)");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(useAllProteinsWithRadioButton, gbc);
        txtAccessionsOnly = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(txtAccessionsOnly, gbc);
        txtAllAccessionsExcept = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(txtAllAccessionsExcept, gbc);
        useAllProteinsExceptRadioButton = new JRadioButton();
        useAllProteinsExceptRadioButton.setText("Use all proteins except accessions (comma seperated)");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(useAllProteinsExceptRadioButton, gbc);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(useAllProteinsRadioButton);
        buttonGroup.add(useMostAbundantProteinsRadioButton);
        buttonGroup.add(useAllProteinsWithRadioButton);
        buttonGroup.add(useAllProteinsExceptRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }
}
