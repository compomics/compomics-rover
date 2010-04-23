package com.compomics.rover.gui.multiwizard;

import org.apache.log4j.Logger;

import com.compomics.rover.general.interfaces.WizardPanel;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.interfaces.PeptideIdentification;
import com.compomics.rover.general.quantitation.MergedComponentType;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.MergedRatioType;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatio;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.enumeration.MaxQuantScoreType;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.gui.MatchRatioWithComponent;
import com.compomics.util.sun.SwingWorker;

import javax.swing.*;
import java.util.Vector;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 11-Dec-2009
 * Time: 11:38:29
 * To change this template use File | Settings | File Templates.
 */
public class MatchComponentPanel implements WizardPanel {
    // Class specific log4j logger for MatchComponentPanel instances.
    private static Logger logger = Logger.getLogger(MatchComponentPanel.class);


    private WizardFrameHolder iParent;
    private JPanel jpanContent;
    private JList list1;
    private JLabel lblSet;
    private JLabel lblComponentLabelCount;
    private JLabel lblLabel;
    private JButton linkToSelectedMergedButton;
    private JButton linkToANewlyButton;
    private JTextField newlyCreatedRatioTypeTextField;
    private JProgressBar progressBar1;
    private JPanel panel1;

    private Vector<MergedComponentType> iComponentTypes = new Vector<MergedComponentType>();
    private Vector<String[]> iCollectionsComponents = new Vector<String[]>();
    private int iIndexCounter = 0;
    private int iComponentTypeCounter = 0;
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();


    public void nextRatio() {
        if (iComponentTypeCounter < iCollectionsComponents.get(iIndexCounter).length - 1) {
            //we must select the next ratio type
            iComponentTypeCounter = iComponentTypeCounter + 1;
            lblComponentLabelCount.setText("Component " + (iComponentTypeCounter + 1) + "/" + iCollectionsComponents.get(iIndexCounter).length);
            lblLabel.setText(iCollectionsComponents.get(iIndexCounter)[iComponentTypeCounter]);

        } else {
            //we must select the next index
            iIndexCounter = iIndexCounter + 1;
            if (iIndexCounter == iCollectionsComponents.size()) {
                //no more data sets
                linkToANewlyButton.setVisible(false);
                linkToSelectedMergedButton.setVisible(false);
                newlyCreatedRatioTypeTextField.setVisible(false);
                progressBar1.setVisible(true);
                lblSet.setText("All set");
                lblLabel.setText("\"Renaming ratios ... \"");
                lblComponentLabelCount.setText("");
                renameRatiosAndComponents();


            } else {

                linkToANewlyButton.setEnabled(false);
                linkToSelectedMergedButton.setEnabled(true);
                //we will show the next data set
                iComponentTypeCounter = 0;
                lblSet.setText(iParent.getTitle(iIndexCounter) + " : set " + (iIndexCounter + 1) + "/" + iCollectionsComponents.size());
                lblComponentLabelCount.setText("Component " + (iComponentTypeCounter + 1) + "/" + iCollectionsComponents.get(iIndexCounter).length);
                lblLabel.setText(iCollectionsComponents.get(iIndexCounter)[iComponentTypeCounter]);
            }
        }
    }

    public MatchComponentPanel(WizardFrameHolder aParent) {
        this.iParent = aParent;
        $$$setupUI$$$();
        linkToANewlyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String lName = newlyCreatedRatioTypeTextField.getText();
                if (lName.equalsIgnoreCase("Newly created component type name")) {
                    //the given name was the default name
                    JOptionPane.showMessageDialog(iParent, "Set a valid name for the newly created component type!", "ERROR", JOptionPane.WARNING_MESSAGE);
                    newlyCreatedRatioTypeTextField.setText("");
                    newlyCreatedRatioTypeTextField.requestFocus();
                } else {
                    //check if the name does not exists
                    boolean lAlreadyUsed = false;
                    for (int i = 0; i < iComponentTypes.size(); i++) {
                        if (iComponentTypes.get(i).toString().equalsIgnoreCase(lName)) {
                            lAlreadyUsed = true;
                        }
                    }
                    if (lAlreadyUsed) {
                        JOptionPane.showMessageDialog(iParent, "Set a valid name for the newly created component type!\n" + lName + " was already used.", "ERROR", JOptionPane.WARNING_MESSAGE);
                        newlyCreatedRatioTypeTextField.setText("");
                        newlyCreatedRatioTypeTextField.requestFocus();
                    } else {
                        MergedComponentType lMerged = new MergedComponentType(lName);
                        lMerged.addComponentType(iIndexCounter, lblLabel.getText());
                        iComponentTypes.add(lMerged);
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
                MergedComponentType lMerged = (MergedComponentType) list1.getSelectedValue();
                if (lMerged != null) {
                    lMerged.addComponentType(iIndexCounter, lblLabel.getText());
                    nextRatio();
                } else {
                    JOptionPane.showMessageDialog(iParent, "No component was selected!", "ERROR", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    public void renameRatiosAndComponents() {
        iParent.setComponentTypes(iComponentTypes);

        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                Vector<MergedComponentType> lComponentsTypes = iParent.getComponentTypes();
                Vector<String> lNewComponentTypes = new Vector<String>();
                for (int i = 0; i < lComponentsTypes.size(); i++) {
                    lNewComponentTypes.add(lComponentsTypes.get(i).toString());
                }
                iParent.setNewCompentTypes(lNewComponentTypes);
                Vector<MergedRatioType> lRatioTypes = iParent.getRatioTypes();
                Vector<String> lNewRatioTypes = new Vector<String>();
                for (int i = 0; i < lRatioTypes.size(); i++) {
                    lNewRatioTypes.add(lRatioTypes.get(i).toString());
                }
                iParent.setNewRatioTypes(lNewRatioTypes);
                MatchRatioWithComponent lMatch = new MatchRatioWithComponent(true);
                while (lNewRatioTypes.size() > iQuantitativeValidationSingelton.getMatchedRatioTypes().size()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //sleep failed
                    }
                }
                while (iQuantitativeValidationSingelton.getMatchedRatioTypes().get(iQuantitativeValidationSingelton.getMatchedRatioTypes().size() - 1).getUnregulatedComponentsBySet().size() < iQuantitativeValidationSingelton.getRoverSources().size()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //sleep failed
                    }
                }

                Vector<RatioGroupCollection> lCollections = iParent.getCollections();
                progressBar1.setMaximum(lCollections.size());
                for (int i = 0; i < lCollections.size(); i++) {
                    progressBar1.setValue(i + 1);
                    int lIndex = lCollections.get(i).getIndex();
                    RatioGroupCollection lCollection = lCollections.get(i);
                    //set the new types to the ratiogroupcollection
                    lCollection.setRatioTypes(lNewRatioTypes);
                    lCollection.setComponentTypes(lNewComponentTypes);
                    for (int j = 0; j < lCollection.size(); j++) {
                        //set the peptide types
                        RatioGroup lRatioGroup = lCollection.get(j);
                        //set these on the peptide identifications
                        for (int k = 0; k < lRatioGroup.getNumberOfIdentifications(); k++) {
                            PeptideIdentification lPeptide = lRatioGroup.getIdentification(k);
                            String lType = lPeptide.getType();
                            String lResult = getMergedComponentForOriginal(lComponentsTypes, lIndex, lType);
                            lPeptide.setType(lResult);
                        }
                        ArrayList<String> lPeptideTypes = lRatioGroup.getAllPeptideTypes();
                        ArrayList<String> lConvertedTypes = new ArrayList<String>();
                        for (int k = 0; k < lPeptideTypes.size(); k++) {
                            String lResult = getMergedComponentForOriginal(lComponentsTypes, lIndex, lPeptideTypes.get(k));
                            lConvertedTypes.add(lResult);
                        }
                        lRatioGroup.setAllPeptideTypes(lConvertedTypes);
                        //set the ratio types
                        for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                            Ratio lRatio = lRatioGroup.getRatio(k);
                            String lType = lRatio.getType();
                            String lResult = getMergedRatioTypeForOriginal(lRatioTypes, lIndex, lType);
                            boolean lMustBeInverted = getInvertedStatusForOriginal(lRatioTypes, lIndex, lType);
                            if (lMustBeInverted) {
                                //get the not log2 ratio
                                iQuantitativeValidationSingelton.setMaxQuantScoreType(MaxQuantScoreType.RATIO);
                                double lTempRatio = lRatio.getRatio(false);
                                //inverted it
                                lTempRatio = Math.pow(lTempRatio, -1);
                                //set it as a log 2 value
                                lRatio.setRecalculatedRatio(Math.log(lTempRatio) / Math.log(2));
                                //this inverted ratio resembles the original ratio
                                lRatio.setOriginalRatio(Math.log(lTempRatio) / Math.log(2));
                                //set that the ratio was inverted
                                lRatio.setInverted(true);
                            }
                            lRatio.setType(lResult);
                        }
                    }
                }

                iQuantitativeValidationSingelton.setMaxQuantScoreType(MaxQuantScoreType.RATIO);
                return true;
            }

            public void finished() {
                iParent.setNextButtonEnabled(true);
                iParent.clickNextButton();
            }

        };
        lStarter.start();
    }

    public String getMergedComponentForOriginal(Vector<MergedComponentType> lComponentsTypes, int lIndex, String lType) {
        String lResult = "";
        for (int i = 0; i < lComponentsTypes.size(); i++) {
            MergedComponentType lMerged = lComponentsTypes.get(i);
            String lOriginal = lMerged.getOriginalForIndex(lIndex);
            if (lOriginal.equalsIgnoreCase(lType)) {
                lResult = lMerged.toString();
            }
        }
        return lResult;
    }

    public String getMergedRatioTypeForOriginal(Vector<MergedRatioType> lRatioTypes, int lIndex, String lType) {
        String lResult = "";
        for (int i = 0; i < lRatioTypes.size(); i++) {
            MergedRatioType lMerged = lRatioTypes.get(i);
            String lOriginal = lMerged.getOriginalForIndex(lIndex);
            if (lOriginal.equalsIgnoreCase(lType)) {
                lResult = lMerged.toString();
            }
        }
        return lResult;
    }


    public boolean getInvertedStatusForOriginal(Vector<MergedRatioType> lRatioTypes, int lIndex, String lType) {
        boolean lMustBeInverted = false;
        for (int i = 0; i < lRatioTypes.size(); i++) {
            MergedRatioType lMerged = lRatioTypes.get(i);
            String lOriginal = lMerged.getOriginalForIndex(lIndex);
            if (lOriginal.equalsIgnoreCase(lType)) {
                lMustBeInverted = lMerged.isInverted(lIndex);
            }
        }
        return lMustBeInverted;
    }

    public JPanel getContentPane() {
        return jpanContent;
    }

    public void backClicked() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void nextClicked() {
        iParent.setComponentTypes(iComponentTypes);
    }

    public boolean feasableToProceed() {
        return true;
    }

    public String getNotFeasableReason() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void construct() {
        iCollectionsComponents = iParent.getCollectionsComponents();
        lblLabel.setText(iCollectionsComponents.get(0)[0]);
        lblSet.setText(iParent.getTitle(iIndexCounter) + " : set " + (iIndexCounter + 1) + iCollectionsComponents.size());
        lblComponentLabelCount.setText("Component 1/" + iCollectionsComponents.get(0).length);
        linkToSelectedMergedButton.setEnabled(false);
        iParent.setNextButtonEnabled(false);
        progressBar1.setVisible(false);
    }

    private void createUIComponents() {
        list1 = new JList(iComponentTypes);
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
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        jpanContent = new JPanel();
        jpanContent.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(jpanContent, gbc);
        panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 9;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        jpanContent.add(panel1, gbc);
        final JLabel label1 = new JLabel();
        label1.setFont(new Font(label1.getFont().getName(), Font.BOLD, 16));
        label1.setText("Merge component types from different sources");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(label1, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 3;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(scrollPane1, gbc);
        scrollPane1.setViewportView(list1);
        lblSet = new JLabel();
        lblSet.setText("Set 1");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(lblSet, gbc);
        lblComponentLabelCount = new JLabel();
        lblComponentLabelCount.setText("Component 1");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(lblComponentLabelCount, gbc);
        lblLabel = new JLabel();
        lblLabel.setFont(new Font(lblLabel.getFont().getName(), Font.BOLD, 20));
        lblLabel.setHorizontalAlignment(0);
        lblLabel.setHorizontalTextPosition(0);
        lblLabel.setText("Light");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel1.add(lblLabel, gbc);
        linkToSelectedMergedButton = new JButton();
        linkToSelectedMergedButton.setText("Link to selected merged component type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(linkToSelectedMergedButton, gbc);
        linkToANewlyButton = new JButton();
        linkToANewlyButton.setText("Link to a newly created merged component type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(linkToANewlyButton, gbc);
        newlyCreatedRatioTypeTextField = new JTextField();
        newlyCreatedRatioTypeTextField.setText("Newly created component type name");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(newlyCreatedRatioTypeTextField, gbc);
        progressBar1 = new JProgressBar();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(progressBar1, gbc);
    }
}
