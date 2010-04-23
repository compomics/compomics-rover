package com.compomics.rover.gui.multiwizard;

import org.apache.log4j.Logger;

import com.compomics.rover.general.interfaces.WizardPanel;
import com.compomics.rover.general.quantitation.MergedRatioType;
import com.compomics.rover.gui.multiwizard.WizardFrameHolder;

import javax.swing.*;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 11-Dec-2009
 * Time: 10:33:05
 * To change this template use File | Settings | File Templates.
 */
public class MatchRatiosPanel implements WizardPanel {
	// Class specific log4j logger for MatchRatiosPanel instances.
	 private static Logger logger = Logger.getLogger(MatchRatiosPanel.class);


    private WizardFrameHolder iParent;
    private JPanel jpanContent;
    private JList list1;
    private JLabel lblSet;
    private JLabel lblRatioLabelCount;
    private JLabel lblLabel;
    private JButton linkToSelectedMergedButton;
    private JButton linkToANewlyButton;
    private JTextField newlyCreatedRatioTypeTextField;
    private JCheckBox invertRatioCheckBox;

    private Vector<MergedRatioType> iRatioTypes = new Vector<MergedRatioType>();
    private Vector<String[]> iCollectionsRatios = new Vector<String[]>();
    private Vector<String[]> iCollectionsComponents = new Vector<String[]>();
    private int iIndexCounter = 0;
    private int iRatioTypeCounter = 0;


    public void nextRatio() {
        if (iRatioTypeCounter < iCollectionsRatios.get(iIndexCounter).length - 1) {
            //we must select the next ratio type
            iRatioTypeCounter = iRatioTypeCounter + 1;
            lblRatioLabelCount.setText("Ratio type " + (iRatioTypeCounter + 1) + "/" + iCollectionsRatios.get(iIndexCounter).length);
            lblLabel.setText(iCollectionsRatios.get(iIndexCounter)[iRatioTypeCounter]);
            invertRatioCheckBox.setSelected(false);

        } else {
            //we must select the next index
            iIndexCounter = iIndexCounter + 1;
            if (iIndexCounter == iCollectionsRatios.size()) {
                //no more data sets
                linkToANewlyButton.setVisible(false);
                linkToSelectedMergedButton.setVisible(false);
                newlyCreatedRatioTypeTextField.setVisible(false);
                invertRatioCheckBox.setVisible(false);
                iParent.setNextButtonEnabled(true);
                lblSet.setText("All set");
                lblLabel.setText("Click next");
                lblRatioLabelCount.setText("");
                iParent.clickNextButton();

            } else {

                linkToANewlyButton.setEnabled(false);
                linkToSelectedMergedButton.setEnabled(true);
                //we will show the next data set
                iRatioTypeCounter = 0;
                lblSet.setText(iParent.getTitle(iIndexCounter) + " : set " + (iIndexCounter + 1) + "/" + iCollectionsRatios.size());
                lblRatioLabelCount.setText("Ratio type " + (iRatioTypeCounter + 1) + "/" + iCollectionsRatios.get(iIndexCounter).length);
                lblLabel.setText(iCollectionsRatios.get(iIndexCounter)[iRatioTypeCounter]);
            }
        }
    }

    public MatchRatiosPanel(WizardFrameHolder aParent) {
        this.iParent = aParent;
        $$$setupUI$$$();
        linkToANewlyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String lName = newlyCreatedRatioTypeTextField.getText();
                if (lName.equalsIgnoreCase("Newly created ratio type name")) {
                    //the given name was the default name
                    JOptionPane.showMessageDialog(iParent, "Set a valid name for the newly created ratio type!", "ERROR", JOptionPane.WARNING_MESSAGE);
                    newlyCreatedRatioTypeTextField.setText("");
                    newlyCreatedRatioTypeTextField.requestFocus();
                } else {
                    //check if the name does not exists
                    boolean lAlreadyUsed = false;
                    for (int i = 0; i < iRatioTypes.size(); i++) {
                        if (iRatioTypes.get(i).toString().equalsIgnoreCase(lName)) {
                            lAlreadyUsed = true;
                        }
                    }
                    if (lAlreadyUsed) {
                        JOptionPane.showMessageDialog(iParent, "Set a valid name for the newly created ratio type!\n" + lName + " was already used.", "ERROR", JOptionPane.WARNING_MESSAGE);
                        newlyCreatedRatioTypeTextField.setText("");
                        newlyCreatedRatioTypeTextField.requestFocus();
                    } else {
                        MergedRatioType lMerged = new MergedRatioType(lName);
                        if (invertRatioCheckBox.isSelected()) {
                            lMerged.addRatioType(iIndexCounter, iCollectionsRatios.get(iIndexCounter)[iRatioTypeCounter], true);
                        } else {
                            lMerged.addRatioType(iIndexCounter, lblLabel.getText(), false);
                        }
                        iRatioTypes.add(lMerged);
                        list1.updateUI();
                        nextRatio();
                        newlyCreatedRatioTypeTextField.setText("");
                        newlyCreatedRatioTypeTextField.requestFocus();
                    }
                }
            }
        });
        linkToSelectedMergedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MergedRatioType lMerged = (MergedRatioType) list1.getSelectedValue();
                if (lMerged != null) {
                    if (invertRatioCheckBox.isSelected()) {
                        lMerged.addRatioType(iIndexCounter, iCollectionsRatios.get(iIndexCounter)[iRatioTypeCounter], true);
                    } else {
                        lMerged.addRatioType(iIndexCounter, lblLabel.getText(), false);
                    }
                    nextRatio();
                } else {
                    JOptionPane.showMessageDialog(iParent, "No ratio type was selected!", "ERROR", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        invertRatioCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (invertRatioCheckBox.isSelected()) {
                    String lLabelText = lblLabel.getText();
                    lLabelText = "(" + lLabelText + ")^-1";
                    lblLabel.setText(lLabelText);
                } else {
                    lblLabel.setText(iCollectionsRatios.get(iIndexCounter)[iRatioTypeCounter]);
                }
            }
        });
    }

    public JPanel getContentPane() {
        return jpanContent;
    }

    public void backClicked() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void nextClicked() {
        iParent.setRatioTypes(iRatioTypes);
    }

    public boolean feasableToProceed() {
        return true;
    }

    public String getNotFeasableReason() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void construct() {
        iCollectionsComponents = iParent.getCollectionsComponents();
        iCollectionsRatios = iParent.getCollectionsRatios();
        lblLabel.setText(iCollectionsRatios.get(0)[0]);
        lblSet.setText(iParent.getTitle(iIndexCounter) + " : set " + (iIndexCounter + 1) + "/" + iCollectionsRatios.size());
        lblRatioLabelCount.setText("Ratio type 1/" + iCollectionsRatios.get(0).length);
        linkToSelectedMergedButton.setEnabled(false);
        iParent.setNextButtonEnabled(false);
    }

    private void createUIComponents() {
        list1 = new JList(iRatioTypes);
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
        jpanContent.setDoubleBuffered(false);
        final JLabel label1 = new JLabel();
        label1.setDoubleBuffered(false);
        label1.setFont(new Font(label1.getFont().getName(), Font.BOLD, 16));
        label1.setText("Merge ratio types from different sources");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(label1, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setDoubleBuffered(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 4;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(scrollPane1, gbc);
        list1.setDoubleBuffered(false);
        scrollPane1.setViewportView(list1);
        lblSet = new JLabel();
        lblSet.setDoubleBuffered(false);
        lblSet.setText("Set 1");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblSet, gbc);
        lblRatioLabelCount = new JLabel();
        lblRatioLabelCount.setDoubleBuffered(false);
        lblRatioLabelCount.setText("Ratio type 1");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblRatioLabelCount, gbc);
        lblLabel = new JLabel();
        lblLabel.setDoubleBuffered(false);
        lblLabel.setFont(new Font(lblLabel.getFont().getName(), Font.BOLD, 20));
        lblLabel.setHorizontalAlignment(0);
        lblLabel.setHorizontalTextPosition(0);
        lblLabel.setText("L/H");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblLabel, gbc);
        linkToSelectedMergedButton = new JButton();
        linkToSelectedMergedButton.setDoubleBuffered(false);
        linkToSelectedMergedButton.setText("Link to selected merged ratio type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(linkToSelectedMergedButton, gbc);
        linkToANewlyButton = new JButton();
        linkToANewlyButton.setDoubleBuffered(false);
        linkToANewlyButton.setText("Link to a newly created merged ratio type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(linkToANewlyButton, gbc);
        newlyCreatedRatioTypeTextField = new JTextField();
        newlyCreatedRatioTypeTextField.setDoubleBuffered(false);
        newlyCreatedRatioTypeTextField.setText("Newly created ratio type name");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(newlyCreatedRatioTypeTextField, gbc);
        invertRatioCheckBox = new JCheckBox();
        invertRatioCheckBox.setText("invert ratio");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(invertRatioCheckBox, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }
}
