package com.compomics.rover.gui;

import org.apache.log4j.Logger;


import com.compomics.rover.general.quantitation.QuantitativeProtein;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.QuantitativePeptideGroup;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.sequenceretriever.UniprotSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.IpiSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.NcbiSequenceRetriever;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 9-jan-2009
 * Time: 8:42:05
 */


/**
 * This class will create a panel wiht a protein bar on it
 */
public class ProteinBarPanel extends JPanel {
	// Class specific log4j logger for ProteinBarPanel instances.
	 private static Logger logger = Logger.getLogger(ProteinBarPanel.class);

    /**
     * The protein
     */
    private QuantitativeProtein iProtein;
    /**
     * The height of the panel
     */
    private int iHeight;
    /**
     * The width of the panel
     */
    private int iWidth;
    /**
     * The number of bars that must be painted (for every ratio type a bar will be painted)
     */
    private int iNumberOfBoxes;
    /**
     * The sequence of the protein
     */
    private String iSequence;
    /**
     * This distiller validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
    /**
     * The protein sequence length
     */
    private int iSequenceLength = 0;

    /**
     * The constructor
     *
     * @param aProtein The protein
     */
    public ProteinBarPanel(QuantitativeProtein aProtein) {
        this.iProtein = aProtein;
        this.iHeight = this.getHeight();
        this.iWidth = this.getWidth();
        this.setBackground(Color.white);

        this.iNumberOfBoxes = iProtein.getTypes().length;
        try {
            if (iProtein.getSequence() == null) {
                if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.UNIPROT)) {
                    this.iSequence = (new UniprotSequenceRetriever(iProtein.getAccession())).getSequence();
                    this.iSequenceLength = iSequence.length();
                    iProtein.setSequence(iSequence);
                } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.IPI)) {
                    this.iSequence = (new IpiSequenceRetriever(iProtein.getAccession())).getSequence();
                    this.iSequenceLength = iSequence.length();
                    iProtein.setSequence(iSequence);
                } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.NCBI)) {
                    this.iSequence = (new NcbiSequenceRetriever(iProtein.getAccession())).getSequence();
                    this.iSequenceLength = iSequence.length();
                    iProtein.setSequence(iSequence);
                }
            } else {
                this.iSequenceLength = iProtein.getSequenceLength();
                iSequence = iProtein.getSequence();
            }
        } catch (Exception e) {
            //sequence not found
            iSequence = null;
        }
    }


    /**
     * This method creates and paint the protein bar
     */
    protected void paintComponent(Graphics g) {

        this.iHeight = this.getHeight();
        this.iWidth = this.getWidth();

        //get the peptide groups linked to this protien
        Vector<QuantitativePeptideGroup> lPeptideGroups = iProtein.getPeptideGroups(true);

        this.setBackground(Color.white);
        //only go through with this if the height is minimal 40;
        if (iHeight < 40) {
            return;
        }

        //draw white backgrouns
        g.setColor(Color.white);
        g.fillRect(0, 0, iWidth, iHeight);
        g.setColor(Color.black);

        if (iSequence == null) {
            //not sequence found
            g.setFont(new Font("Times New Roman", Font.PLAIN, 20));
            g.drawString("No sequence found", iWidth / 20 - 40, iHeight / 2);
            //don't paint the rest
            return;
        }

        //calculate the height and widht of one box
        int lBoxHeight = (iHeight - 25 - (iNumberOfBoxes - 1) * 5) / iNumberOfBoxes;
        int lBoxWidth = iWidth - 95;

        if (lBoxHeight < 5) {
            return;
        }

        //draw the gradient rectangle
        Graphics2D g2d = (Graphics2D) g;
        Color lRed = Color.red;
        Color lBlack = Color.black;
        GradientPaint gradient = new GradientPaint(15, 5, lRed, 15, iHeight / 2, lBlack, false);
        g2d.setPaint(gradient);
        g2d.fillRect(15, 5, 30, (iHeight - 10) / 2);
        Color lGreen = Color.green;
        GradientPaint gradient1 = new GradientPaint(15, iHeight / 2, lBlack, 15, iHeight - 5, lGreen, false);
        g2d.setPaint(gradient1);
        g2d.fillRect(15, iHeight / 2, 30, (iHeight - 10) / 2);
        g2d.setColor(Color.black);
        //paint markers on gradient
        g.setColor(Color.white);
        g.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        if(iQuantitativeValidationSingelton.isLog2()){
            g.drawString(String.valueOf(iQuantitativeValidationSingelton.getLeftGraphBorder()),27,15+12);
            g.drawString("0",30,iHeight/2 + 6);
            g.drawString(String.valueOf(iQuantitativeValidationSingelton.getRightGraphBorder()),30,iHeight - 15 - 2);
        } else {
            g.drawString(String.valueOf(iQuantitativeValidationSingelton.getLeftGraphBorder()),27,15+12);
            g.drawString("1",30,iHeight/2 + 6);
            g.drawString(String.valueOf(iQuantitativeValidationSingelton.getRightGraphBorder()),30,iHeight - 15 - 2);
        }
        g.setColor(Color.black);

        //paint the boxes        
        for (int i = 0; i < iNumberOfBoxes; i++) {
            int lYstart = 5 + i * (5 + lBoxHeight);
            g.drawRect(80, lYstart, lBoxWidth, lBoxHeight);

            //paint the box ratio type
            g.setFont(new Font("Times New Roman", Font.PLAIN, 12));
            g.drawString(iProtein.getTypes()[i] + " : ", 50, lYstart + lBoxHeight / 2);

            //paint the ratio boxes (grouped by peptides)

            for (int j = 0; j < lPeptideGroups.size(); j++) {
                if(lPeptideGroups.get(j).isUsedInCalculations()){
                    boolean lCreateBox = true;
                    if(iQuantitativeValidationSingelton.isUseOnlyValidRatioForProteinMean()){
                        //check if it is valid
                        if(lPeptideGroups.get(j).getNumberOfValidRatiosForType(iProtein.getTypes()[i]) == 0){
                            //it's not valid, don't create it
                            lCreateBox = false;
                        }
                    }
                    if(iQuantitativeValidationSingelton.isUseOnlyUniqueRatioForProteinMean()){
                        //check if it is unique
                        if(lPeptideGroups.get(j).getRatioGroups().get(0).getProteinAccessions().length != 1){
                            //it's not unique don't create it
                            lCreateBox = false;
                        }
                    }
                    if(lCreateBox){

                        int lPeptideStart =lPeptideGroups.get(j).getStartPosition();
                        int lPeptideEnd = lPeptideGroups.get(j).getEndPosition();

                        if (lPeptideStart > -1) {
                            //only show it if it is found
                            int lXratioBoxStart = (int) Math.round((double) lBoxWidth * ((double) lPeptideStart / (double) iSequenceLength));
                            int lXratioBoxEnd = (int) Math.round((double) lBoxWidth * ((double) lPeptideEnd / (double) iSequenceLength));
                            Color lColor;
                            if (iQuantitativeValidationSingelton.isLog2()) {
                                lColor = calculateColor(lPeptideGroups.get(j).getMeanRatioForGroup(iProtein.getTypes()[i]), 0.0 , (double) iQuantitativeValidationSingelton.getRightGraphBorder(), (double) iQuantitativeValidationSingelton.getLeftGraphBorder());
                            } else {
                                lColor = calculateColor(lPeptideGroups.get(j).getMeanRatioForGroup(iProtein.getTypes()[i]), 1.0, (double) iQuantitativeValidationSingelton.getRightGraphBorder(), (double) iQuantitativeValidationSingelton.getLeftGraphBorder());
                            }

                            //paint box
                            g.setColor(lColor);
                            g.fillRect(80 + lXratioBoxStart, lYstart, lXratioBoxEnd - lXratioBoxStart, lBoxHeight);

                            //paint identifier
                            g.setColor(Color.gray);
                            g.setFont(new Font("Times New Roman", Font.BOLD, 20));
                            g.drawString(String.valueOf(j + 1), 80 + (lXratioBoxStart + lXratioBoxEnd) / 2 - 5, 10 + lBoxHeight / 2 + i * (5 + lBoxHeight));
                            g.setColor(Color.black);
                        }
                    }
                }
            }
        }


        //paint the bottom box
        //this is a box with orange and blue squares
        //the square is orange if the corresponding peptide is linked to multiple proteins

        g.drawRect(80, iHeight - 15, lBoxWidth, 10);

        //paint the colored peptide boxes
        for (int j = 0; j < lPeptideGroups.size(); j++) {
            RatioGroup lRatioGroup = lPeptideGroups.get(j).getRatioGroups().get(0);

            boolean lRatioGroupedLinkedToMultipleProteins = lPeptideGroups.get(j).isLinkedToMoreProteins(); 

            int lPeptideStart = lPeptideGroups.get(j).getStartPosition();
            int lPeptideEnd = lPeptideStart + lRatioGroup.getPeptideSequence().length();

            if (lPeptideStart > -1) {
                //only show it if it is found
                int lXratioBoxStart = (int) Math.round((double) lBoxWidth * ((double) lPeptideStart / (double) iSequenceLength));
                int lXratioBoxEnd = (int) Math.round((double) lBoxWidth * ((double) lPeptideEnd / (double) iSequenceLength));
                Color lColor;
                if(iProtein.getAccession().trim().equalsIgnoreCase(lPeptideGroups.get(j).getRazorAccession()) && lRatioGroupedLinkedToMultipleProteins){
                    lColor = Color.RED;
                } else if (lRatioGroupedLinkedToMultipleProteins) {
                    lColor = Color.ORANGE;
                } else {
                    lColor = Color.BLUE;
                }

                //paint box
                g.setColor(lColor);
                g.fillRect(80 + lXratioBoxStart, iHeight - 15, lXratioBoxEnd - lXratioBoxStart, 10 );

                //set color back to black
                g.setColor(Color.black);
            }
        }
    }

    /**
     * This method calculates the color
     *
     * @param lRatio      The ratio
     * @param aMiddle     The ratio where the color must be black
     * @param aUpperLimit The ratio where the color must be green
     * @param aLowerLimit The ratio where the color must be red
     * @return A string that represents the colof
     */
    private Color calculateColor(double lRatio, double aMiddle, double aUpperLimit, double aLowerLimit) {
        Color lColor = null;
        if (lRatio > aMiddle) {
            //color it more green
            //green gradient is from aMiddle to aUpperLimit
            lRatio = lRatio - aMiddle;
            //calculate the ratio percentage
            double lRatioPercentage = lRatio / (aUpperLimit - aMiddle);
            int lRed = 0;
            int lGreen = 255;
            int lBlue = 0;
            if (lRatioPercentage > 1.0) {
                //the ratio is greater than the upperlimit, color it green
                lGreen = 255;
            } else {
                lGreen = (int) (lGreen * lRatioPercentage);
            }

            lColor = new Color(lRed, lGreen, lBlue);

        } else {
            //color it more red
            //red gradient is from aLowerLimit to aMiddle
            lRatio = lRatio - aMiddle;
            double lRatioPercentage = -lRatio / (aMiddle - aLowerLimit);
            int lRed = 255;
            int lGreen = 0;
            int lBlue = 0;
            if (lRatioPercentage > 1.0) {
                //the ratio is lower than the lowerlimit, color it red
                lRed = 255;
            } else {
                lRed = (int) (lRed * lRatioPercentage);
            }

            lColor = new Color(lRed, lGreen, lBlue);

        }
        return lColor;
    }

    /**
     * This method resizes the protein bar to an A4 paper width
     */
    public void resizeToA4Width() {
        this.iWidth = 550;
        this.paintComponent(this.getGraphics());
        this.update(this.getGraphics());
    }
}
