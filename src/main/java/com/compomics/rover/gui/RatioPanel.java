package com.compomics.rover.gui;

import org.apache.log4j.Logger;

import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatio;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatioGroup;
import com.compomics.rover.general.quantitation.source.Census.CensusRatio;
import com.compomics.rover.general.enumeration.RoverSource;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 9-mrt-2009
 * Time: 9:49:11
 * To change this template use File | Settings | File Templates.
 */
public class RatioPanel {
	// Class specific log4j logger for RatioPanel instances.
	 private static Logger logger = Logger.getLogger(RatioPanel.class);
    private JLabel lblRatioType;
    private JLabel lblRatio;
    private JLabel lblQualityText;
    private JLabel lblQualityNumbers;
    private JLabel lblZScoreText;
    private JLabel lblZScoreNumbers;
    private JLabel lblPValueText;
    private JLabel lblPValueNumbers;
    private JLabel lblError0;
    private JLabel lblError1;
    private JLabel lblError2;
    private JLabel lblError3;
    private JLabel lblError4;
    private JPanel jpanContent;
    private JLabel lblComment;

    /**
     * This validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();


    public RatioPanel(Ratio aRatio) {

        $$$setupUI$$$();
        //set background white
        jpanContent.setBackground(Color.WHITE);

        //set some labels not shown
        lblComment.setVisible(false);
        lblError0.setVisible(false);
        lblError1.setVisible(false);
        lblError2.setVisible(false);
        lblError3.setVisible(false);
        lblError4.setVisible(false);
        lblQualityText.setVisible(false);
        lblQualityNumbers.setVisible(false);

        //set the labels

        if (aRatio.getInverted()) {
            lblRatioType.setText(aRatio.getType() + " *");
        } else {
            lblRatioType.setText(aRatio.getType());
        }
        lblRatio.setText(String.valueOf(Math.round(aRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) * 1000.0) / 1000.0));
        if (aRatio.getValid()) {
            lblRatioType.setForeground(Color.GREEN);
        } else {
            lblRatioType.setForeground(Color.RED);
            //show the quality if it is not valid , and if it's in distiller mode
            //show the errors found by distiller
            if (aRatio.getParentRatioGroup().getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || aRatio.getParentRatioGroup().getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS) {
                //Show the quality
                DistillerRatio lDistillerRatio = (DistillerRatio) aRatio;
                lblQualityText.setVisible(true);
                lblQualityNumbers.setVisible(true);
                lblQualityNumbers.setText(String.valueOf(Math.round(lDistillerRatio.getQuality() * 1000.0) / 1000.0));
                //show the errors
                //create an array with the error labels
                JLabel[] lErrorLabels = new JLabel[]{lblError0, lblError1, lblError2, lblError3, lblError4};
                ArrayList<String> lErrors = lDistillerRatio.getNotValidState();
                for (int i = 0; i < lErrors.size(); i++) {
                    lErrorLabels[i].setVisible(true);
                    lErrorLabels[i].setText(lErrors.get(i));
                }
            }
        }

        if (aRatio.getParentRatioGroup().getParentCollection().getRoverSource() == RoverSource.CENSUS) {
            CensusRatio lCensusRatio = (CensusRatio) aRatio;
            lblQualityText.setVisible(true);
            lblQualityText.setText("Determination factor (R^2)");
            lblQualityNumbers.setVisible(true);
            lblQualityNumbers.setText(String.valueOf(lCensusRatio.getDeterminationFactor()));

            lblError0.setVisible(true);
            lblError0.setText("XIC profile score: " + lCensusRatio.getProfileScore());
        }

        if (aRatio.getParentRatioGroup().getParentCollection().getRoverSource() == RoverSource.MAX_QUANT || aRatio.getParentRatioGroup().getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_NO_SIGN || aRatio.getParentRatioGroup().getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS) {
            lblQualityText.setText("PEP: ");
            lblQualityText.setVisible(true);
            lblQualityNumbers.setVisible(true);
            MaxQuantRatioGroup lGroup = (MaxQuantRatioGroup) aRatio.getParentRatioGroup();
            lblQualityNumbers.setText(String.valueOf(lGroup.getPEP()));
        }

        HashMap lStatMeas = iQuantitativeValidationSingelton.getReferenceSet().getStatisticalMeasermentForRatio(aRatio.getType(), aRatio);
        lblZScoreNumbers.setText(String.valueOf(Math.round((Double) lStatMeas.get("significance") * 1000.0) / 1000.0));
        lblPValueNumbers.setText(String.valueOf(Math.round(iQuantitativeValidationSingelton.calculateTwoSidedPvalueForZvalue((Double) lStatMeas.get("significance")) * 1000.0) / 1000.0));
        if (aRatio.getComment() != null) {
            lblComment.setVisible(true);
            lblComment.setText(" " + aRatio.getComment() + " ");
        }


    }


    /**
     * Getter for the jpanContent
     *
     * @return JPanel
     */
    public JPanel getContentPane() {
        return this.jpanContent;
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
        lblRatioType = new JLabel();
        lblRatioType.setText("Ratio type: ");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 2, 5, 2);
        jpanContent.add(lblRatioType, gbc);
        lblRatio = new JLabel();
        lblRatio.setText("ratio");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 2, 5, 2);
        jpanContent.add(lblRatio, gbc);
        lblZScoreText = new JLabel();
        lblZScoreText.setText("Z-score: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 12;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 2, 5, 2);
        jpanContent.add(lblZScoreText, gbc);
        lblZScoreNumbers = new JLabel();
        lblZScoreNumbers.setText("zScore");
        gbc = new GridBagConstraints();
        gbc.gridx = 13;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 2, 5, 2);
        jpanContent.add(lblZScoreNumbers, gbc);
        lblPValueText = new JLabel();
        lblPValueText.setText("P-value: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 14;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 2, 5, 2);
        jpanContent.add(lblPValueText, gbc);
        lblPValueNumbers = new JLabel();
        lblPValueNumbers.setText("pValue");
        gbc = new GridBagConstraints();
        gbc.gridx = 15;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 2, 5, 2);
        jpanContent.add(lblPValueNumbers, gbc);
        lblQualityText = new JLabel();
        lblQualityText.setText("Quality: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 2, 5, 2);
        jpanContent.add(lblQualityText, gbc);
        lblQualityNumbers = new JLabel();
        lblQualityNumbers.setText("qual");
        gbc = new GridBagConstraints();
        gbc.gridx = 11;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 2, 5, 2);
        jpanContent.add(lblQualityNumbers, gbc);
        lblError0 = new JLabel();
        lblError0.setFont(new Font(lblError0.getFont().getName(), lblError0.getFont().getStyle(), 11));
        lblError0.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 15;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblError0, gbc);
        lblError2 = new JLabel();
        lblError2.setFont(new Font(lblError2.getFont().getName(), lblError2.getFont().getStyle(), 11));
        lblError2.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 15;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblError2, gbc);
        lblError3 = new JLabel();
        lblError3.setFont(new Font(lblError3.getFont().getName(), lblError3.getFont().getStyle(), 11));
        lblError3.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 15;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblError3, gbc);
        lblError4 = new JLabel();
        lblError4.setFont(new Font(lblError4.getFont().getName(), lblError4.getFont().getStyle(), 11));
        lblError4.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 15;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblError4, gbc);
        lblError1 = new JLabel();
        lblError1.setFont(new Font(lblError1.getFont().getName(), lblError1.getFont().getStyle(), 11));
        lblError1.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 15;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblError1, gbc);
        lblComment = new JLabel();
        lblComment.setFont(new Font(lblComment.getFont().getName(), Font.ITALIC, lblComment.getFont().getSize()));
        lblComment.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 15;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(lblComment, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }
}
