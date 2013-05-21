package com.compomics.rover.gui;

import org.apache.log4j.Logger;

import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.quantitation.RatioType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.*;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 04-Feb-2010
 * Time: 07:56:53
 * To change this template use File | Settings | File Templates.
 */
public class MatchRatioWithComponent extends JFrame {
	// Class specific log4j logger for MatchRatioWithComponent instances.
	 private static Logger logger = Logger.getLogger(MatchRatioWithComponent.class);
    private JLabel lblRatioType;
    private JButton matchButtonButton;
    private JPanel jpanComponent;
    private JPanel jpanContent;
    private JPanel jpanUnregulatedComponents;
    private JTextField medianTextField;

    private Vector<String> iRatios;
    private List<String> iComponents;
    private Vector<JCheckBox> iCheckBoxes = new Vector<JCheckBox>();
    private Vector<JCheckBox> iUnregulatedCheckBoxes = new Vector<JCheckBox>();
    private int iRatioCount = 0;
    private int iSetCount = 0;
    /**
     * This validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();

    public MatchRatioWithComponent(boolean lStandAlone) {


        $$$setupUI$$$();
        iRatios = iQuantitativeValidationSingelton.getRatioTypes();
        iComponents = iQuantitativeValidationSingelton.getComponentTypes();

        for (int i = 0; i < iComponents.size(); i++) {
            JCheckBox lTemp = new JCheckBox(iComponents.get(i));
            if (iComponents.size() == 2) {
                lTemp.setSelected(true);
            }
            jpanComponent.add(Box.createVerticalGlue());
            jpanComponent.add(lTemp);
            iCheckBoxes.add(lTemp);
        }

        ButtonGroup lGroup = new ButtonGroup();
        for (int i = 0; i < iComponents.size(); i++) {
            JCheckBox lTemp = new JCheckBox(iComponents.get(i));
            if (i == 0) {
                lTemp.setSelected(true);
            }
            lGroup.add(lTemp);
            jpanUnregulatedComponents.add(Box.createVerticalGlue());
            jpanUnregulatedComponents.add(lTemp);
            iUnregulatedCheckBoxes.add(lTemp);
        }

        if (iQuantitativeValidationSingelton.isMultipleSources()) {
            lblRatioType.setText(iQuantitativeValidationSingelton.getTitles().get(0) + "=>" + iRatios.get(0));
        } else {
            lblRatioType.setText(iRatios.get(0));
        }


        //create the jframe
        if (lStandAlone) {
            this.setContentPane(jpanContent);
            this.setSize(600, 300);
            this.setLocation(250, 250);
            this.setVisible(true);
        } else {
            jpanContent.setSize(600, 300);
        }

        matchButtonButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //check the median value
                double lMedian = 1.0;
                try {
                    lMedian = Double.valueOf(medianTextField.getText());
                } catch (NumberFormatException f) {
                    JOptionPane.showMessageDialog(new JFrame(), "\"" + medianTextField.getText() + "\" is not a valid median", "ERROR", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                //count the selected
                int lCount = 0;
                for (int i = 0; i < iCheckBoxes.size(); i++) {
                    if (iCheckBoxes.get(i).isSelected()) {
                        lCount = lCount + 1;
                    }
                }

                if (lCount == 2) {
                    String lRatio = iRatios.get(iRatioCount);
                    String[] lComponents = new String[2];
                    String lUnregulatedComponent = "";
                    for (int i = 0; i < iCheckBoxes.size(); i++) {
                        if (iCheckBoxes.get(i).isSelected()) {
                            if (lComponents[0] == null) {
                                lComponents[0] = iCheckBoxes.get(i).getText();
                            } else {
                                lComponents[1] = iCheckBoxes.get(i).getText();
                            }
                        }
                    }
                    for (int i = 0; i < iUnregulatedCheckBoxes.size(); i++) {
                        if (iUnregulatedCheckBoxes.get(i).isSelected()) {
                            lUnregulatedComponent = iUnregulatedCheckBoxes.get(i).getText();
                        }
                    }

                    //check if the unregulated component is part of the ratio
                    boolean lComponentFound = false;
                    if (lComponents[0].equalsIgnoreCase(lUnregulatedComponent)) {
                        lComponentFound = true;
                    } else if (lComponents[1].equalsIgnoreCase(lUnregulatedComponent)) {
                        lComponentFound = true;
                    }


                    if (lComponentFound) {

                        if (iQuantitativeValidationSingelton.isMultipleSources()) {
                            if (iSetCount == 0) {
                                RatioType lType = new RatioType(lRatio, lComponents, lUnregulatedComponent, lMedian);
                                iQuantitativeValidationSingelton.addMatchedRatioTypes(lType);
                            } else {
                                RatioType lRatioType = iQuantitativeValidationSingelton.getMatchedRatioTypes().get(iRatioCount);
                                lRatioType.addUnregulatedComponentForSet(lUnregulatedComponent);
                            }
                        } else {
                            RatioType lType = new RatioType(lRatio, lComponents, lUnregulatedComponent, lMedian);
                            iQuantitativeValidationSingelton.addMatchedRatioTypes(lType);
                        }

                        iRatioCount = iRatioCount + 1;
                        if (iQuantitativeValidationSingelton.isMultipleSources()) {
                            if (iRatioCount >= iRatios.size()) {
                                if (iSetCount + 1 >= iQuantitativeValidationSingelton.getTitles().size()) {
                                    //close this frame
                                    close();
                                } else {
                                    iRatioCount = 0;
                                    iSetCount = iSetCount + 1;
                                    //show the next ratio
                                    lblRatioType.setText(iQuantitativeValidationSingelton.getTitles().get(iSetCount) + " => " + iRatios.get(iRatioCount));
                                    if (iCheckBoxes.size() != 2) {
                                        for (int i = 0; i < iCheckBoxes.size(); i++) {
                                            iCheckBoxes.get(i).setSelected(false);
                                        }
                                    }
                                }
                            } else {
                                //show the next ratio
                                lblRatioType.setText(iQuantitativeValidationSingelton.getTitles().get(iSetCount) + " => " + iRatios.get(iRatioCount));
                                if (iCheckBoxes.size() != 2) {
                                    for (int i = 0; i < iCheckBoxes.size(); i++) {
                                        iCheckBoxes.get(i).setSelected(false);
                                    }
                                }
                            }
                        } else {
                            if (iRatioCount >= iRatios.size()) {
                                //close this frame
                                close();
                            } else {
                                //show the next ratio
                                lblRatioType.setText(iRatios.get(iRatioCount));
                                if (iCheckBoxes.size() != 2) {
                                    for (int i = 0; i < iCheckBoxes.size(); i++) {
                                        iCheckBoxes.get(i).setSelected(false);
                                    }
                                }
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(new JFrame(), "The unregulated component " + lUnregulatedComponent + " is not " + lComponents[0] + " or " + lComponents[1] + " !", "ERROR", JOptionPane.WARNING_MESSAGE);
                    }


                } else {
                    if (iRatioCount == iRatios.size()) {
                        if (lCount == 1) {

                        } else {
                            //the user must select two checkboxes
                            JOptionPane.showMessageDialog(new JFrame(), "One unregulated component must be selected!", "ERROR", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        //the user must select two checkboxes
                        JOptionPane.showMessageDialog(new JFrame(), "Two components must be selected!", "ERROR", JOptionPane.WARNING_MESSAGE);
                    }
                }

            }
        });


    }

    public JPanel getContentPane() {
        return jpanContent;
    }


    private void close() {
        this.setVisible(false);
        this.dispose();
    }

    private void createUIComponents() {
        jpanComponent = new JPanel();
        jpanComponent.setLayout(new BoxLayout(jpanComponent, BoxLayout.Y_AXIS));

        jpanUnregulatedComponents = new JPanel();
        jpanUnregulatedComponents.setLayout(new BoxLayout(jpanUnregulatedComponents, BoxLayout.Y_AXIS));
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
        lblRatioType = new JLabel();
        lblRatioType.setText("LABEL");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblRatioType, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(jpanComponent, gbc);
        matchButtonButton = new JButton();
        matchButtonButton.setText("Match components to ratio type!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(matchButtonButton, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        jpanContent.add(jpanUnregulatedComponents, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Selected 2 components linked to the ratio");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Selected the most unregulated component");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Give the expected ratio median (not Log 2)");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label3, gbc);
        medianTextField = new JTextField();
        medianTextField.setHorizontalAlignment(4);
        medianTextField.setText("1.0");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(medianTextField, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }
}
