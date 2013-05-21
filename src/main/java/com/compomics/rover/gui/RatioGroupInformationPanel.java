package com.compomics.rover.gui;

import com.compomics.rover.general.fileio.readers.LimsMsfInfoReader;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatio;
import com.compomics.rover.general.quantitation.source.thermo_msf.MsfLimsRatio;
import com.compomics.rover.general.quantitation.source.thermo_msf.MsfLimsRatioGroup;
import com.compomics.rover.general.quantitation.source.thermo_msf.ThermoMsfRatio;
import com.compomics.util.gui.spectrum.DefaultSpectrumAnnotation;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import org.apache.log4j.Logger;

import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatioGroup;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatio;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatioGroup;
import com.compomics.rover.general.quantitation.source.Census.CensusRatioGroup;
import com.compomics.rover.general.db.accessors.QuantitationExtension;
import com.compomics.rover.general.interfaces.PeptideIdentification;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.enumeration.RatioComment;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.PeptideIdentification.DefaultPeptideIdentification;
import java.awt.Color;
import java.awt.Dimension;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 22-dec-2008
 * Time: 12:59:45
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class creates a JFrame with information about a ratios and identifications
 * If the data comes from the Mascot Distiller Quantitation toolbox the following graphs will also be created:
 * - a graph with the XIC and absolute intensity values is created
 */
public class RatioGroupInformationPanel extends JFrame {
	// Class specific log4j logger for RatioGroupInformationPanel instances.
	 private static Logger logger = Logger.getLogger(RatioGroupInformationPanel.class);
    //gui stuff
    private JPanel jpanContent;
    private JPanel jpanGraphs;
    private JPanel jpanExtra;
    /**
     * The RatioGroup
     */
    private RatioGroup iRatioGroup;
    /**
     * The connection to ms_lims
     */
    private Connection iConnMsLims;
    /**
     * The QuantitationValidationGUI parent
     */
    private QuantitationValidationGUI iParent;
    /**
     * This distiller validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
    private JTextField lRatioField;

    /**
     * The constructor
     *
     * @param aRatioGroup The RatioGroup
     * @param aConn       The connection to the ms_lims database
     * @param aParent     The QuantitationValidationGUI parent
     */
    public RatioGroupInformationPanel(RatioGroup aRatioGroup, Connection aConn, QuantitationValidationGUI aParent) {
        //set the ratiogroup
        this.iRatioGroup = aRatioGroup;
        //set the connection
        this.iConnMsLims = aConn;
        //set the parent
        this.iParent = aParent;

        createUIComponents();
        setContentPane(new JScrollPane(this.getContentPane()));
        setSize(1200, 750);
        if(aRatioGroup.getParentCollection().getRoverSource() != RoverSource.THERMO_MSF_LIMS && aRatioGroup.getParentCollection().getRoverSource() != RoverSource.THERMO_MSF_FILES && aRatioGroup.getParentCollection().getRoverSource() != RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS && aRatioGroup.getParentCollection().getRoverSource() != RoverSource.DISTILLER_QUANT_TOOLBOX_ROV && aRatioGroup.getParentCollection().getRoverSource() != RoverSource.CENSUS){
            //no graph will be displayed
            this.pack();
        }
    }

    /**
     * This method gives the content panel
     *
     * @return JPanel
     */
    public JPanel getContentPane() {
        return this.jpanContent;
    }

    /**
     * This method creates the panel
     */
    private void createUIComponents() {
        try {

            //create an array with the possible comments
            Vector<String> lCommentVector = new Vector<String>();
            for (RatioComment com : RatioComment.values()){
                lCommentVector.add(com.toString());
            }
            String[] lComments = new String[lCommentVector.size()];
            lCommentVector.toArray(lComments);


            jpanExtra = new JPanel();
            jpanExtra.setLayout(new BoxLayout(jpanExtra, BoxLayout.Y_AXIS));
            jpanExtra.setBackground(Color.white);

            //add correlation and fraction
            jpanExtra.add(Box.createVerticalStrut(5));
            if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) iRatioGroup;
                jpanExtra.add(new JLabel("   Correlation for ratio group : " + lRatioGroup.getCorrelation() + "  fraction for ratio group : " + lRatioGroup.getFraction()));
                jpanExtra.add(Box.createVerticalStrut(5));
            }

            //give information for every ratio type in the ratio group
            for (int i = 0; i < iRatioGroup.getNumberOfRatios(); i++) {
                final Ratio lRatio = iRatioGroup.getRatio(i);
                JPanel jpanRatio = new JPanel();
                jpanRatio.setLayout(new BoxLayout(jpanRatio, BoxLayout.Y_AXIS));
                jpanRatio.setBackground(Color.white);
                if(lRatio.getInverted()){
                    jpanRatio.setBorder(BorderFactory.createTitledBorder(lRatio.getType()+ " *"));
                } else {
                    jpanRatio.setBorder(BorderFactory.createTitledBorder(lRatio.getType()));
                }

                if (lRatio.getValid()) {
                    //the ratio is valid => it can be set invalid
                    //create a combo box with reasons and a button to set it invalid
                    JPanel lRatioButtonPanel = new JPanel();
                    lRatioButtonPanel.setLayout(new BoxLayout(lRatioButtonPanel, BoxLayout.X_AXIS));
                    lRatioButtonPanel.setBackground(Color.white);
                    //create the JCombobox with reasons
                    final JComboBox lCmbComments = new JComboBox(lComments);
                    lCmbComments.setPreferredSize(new Dimension(120, 22));
                    lCmbComments.setMinimumSize(new Dimension(120, 22));
                    lCmbComments.setMaximumSize(new Dimension(120, 22));
                    final JTextField lCommentField = new JTextField();
                    lCommentField.setPreferredSize(new Dimension(120, 22));
                    lCommentField.setMinimumSize(new Dimension(120, 22));
                    lCommentField.setMaximumSize(new Dimension(120, 22));
                    //create the button
                    JButton lBtnSetInvalid = new JButton("Set comment and invalid");
                    lBtnSetInvalid.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (iQuantitativeValidationSingelton.isDatabaseMode()) {
                                //in database mode
                                //update the db in an other thread
                                com.compomics.util.sun.SwingWorker lUpdateDbThread = new com.compomics.util.sun.SwingWorker() {
                                    public Boolean construct() {

                                            QuantitationExtension lQuant = null;
                                            if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS){
                                                DistillerRatio lDistRatio = (DistillerRatio) lRatio;
                                                lQuant = lDistRatio.getQuantitationStoredInDb();
                                            } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.THERMO_MSF_LIMS){
                                                MsfLimsRatio lDistRatio = (MsfLimsRatio) lRatio;
                                                lQuant = lDistRatio.getQuantitationStoredInDb();
                                            } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS){
                                                MaxQuantRatio lDistRatio = (MaxQuantRatio) lRatio;
                                                lQuant = lDistRatio.getQuantitationStoredInDb();
                                            }
                                            double lRatioDisplayed;
                                            boolean lManuallyChanged = false;
                                            try{
                                                lRatioDisplayed = Double.valueOf(lRatioField.getText());
                                            } catch(NumberFormatException e){
                                                JOptionPane.showMessageDialog(new JFrame(), new String[]{lRatioField.getText(), " is not a correct ratio."}, "ERROR!", JOptionPane.ERROR_MESSAGE);
                                                return false;
                                            }

                                            if(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) != lRatioDisplayed){
                                                int answer = JOptionPane.showConfirmDialog(new JFrame(), "You are going to manually change the ratio value, do you want to continue?","Manually change ratio ? ", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                                                if (answer == JOptionPane.NO_OPTION) {
                                                    //close the validation frame
                                                    return false;
                                                } else {
                                                    //continue
                                                    lManuallyChanged = true;
                                                    lRatio.setRecalculatedRatio(lRatioDisplayed, iQuantitativeValidationSingelton.isLog2());
                                                    if(iQuantitativeValidationSingelton.isLog2()){
                                                        lQuant.setRatio(Math.pow(2,lRatioDisplayed));
                                                    } else {
                                                        lQuant.setRatio(lRatioDisplayed);
                                                    }
                                                }
                                            }
                                            if(lCommentField.getText().length() == 0){
                                                if(lManuallyChanged){
                                                    lQuant.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                    lRatio.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                } else {
                                                    if(lRatio.getComment() != null && lRatio.getComment().startsWith("MANUALLY_CHANGED")){
                                                        lQuant.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                        lRatio.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                    } else {
                                                        lQuant.setComment((String) lCmbComments.getSelectedItem());
                                                        lRatio.setComment((String) lCmbComments.getSelectedItem());
                                                    }
                                                }
                                            } else {
                                                if(lManuallyChanged){
                                                    lQuant.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                    lRatio.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                } else {
                                                    if(lRatio.getComment() != null && lRatio.getComment().startsWith("MANUALLY_CHANGED")){
                                                        lQuant.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                        lRatio.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                    } else {
                                                        lQuant.setComment(lCommentField.getText());
                                                        lRatio.setComment(lCommentField.getText());
                                                    }
                                                }
                                            }
                                            lQuant.setValid(false);
                                            try {
                                                lQuant.updateLowPriority(iConnMsLims);
                                            } catch (SQLException e) {
                                                JOptionPane.showMessageDialog(new JFrame(), new String[]{"An error occurred will changing the valid status: ", e.getMessage()}, "ERROR!", JOptionPane.ERROR_MESSAGE);
                                                logger.error(e.getMessage(), e);
                                            }
                                        return true;
                                    }

                                    public void finished() {
                                        //do nothing when finished
                                    }
                                };
                                lUpdateDbThread.start();
                            } else {
                                //not in database mode
                                iQuantitativeValidationSingelton.addValidatedRatio(lRatio);
                            }
                            if(lCommentField.getText().length() == 0){
                                lRatio.setComment((String) lCmbComments.getSelectedItem());
                            } else {
                                lRatio.setComment((String) lCommentField.getText());
                            }
                            lRatio.setValid(false);

                            iParent.loadProtein(false);
                            close();
                        }
                    });
                    JButton lBtnSetComment = new JButton("Set comment");
                    lBtnSetComment.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (iQuantitativeValidationSingelton.isDatabaseMode()) {
                                //in database mode
                                //update the db in an other thread
                                com.compomics.util.sun.SwingWorker lUpdateDbThread = new com.compomics.util.sun.SwingWorker() {
                                    public Boolean construct() {
                                        QuantitationExtension lQuant = null;
                                        if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS){
                                            DistillerRatio lDistRatio = (DistillerRatio) lRatio;
                                            lQuant = lDistRatio.getQuantitationStoredInDb();
                                        } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.THERMO_MSF_LIMS){
                                            MsfLimsRatio lDistRatio = (MsfLimsRatio) lRatio;
                                            lQuant = lDistRatio.getQuantitationStoredInDb();
                                        } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS){
                                            MaxQuantRatio lDistRatio = (MaxQuantRatio) lRatio;
                                            lQuant = lDistRatio.getQuantitationStoredInDb();
                                        }
                                        double lRatioDisplayed;
                                        boolean lManuallyChanged = false;
                                        try{
                                            lRatioDisplayed = Double.valueOf(lRatioField.getText());
                                        } catch(NumberFormatException e){
                                            JOptionPane.showMessageDialog(new JFrame(), new String[]{lRatioField.getText(), " is not a correct ratio."}, "ERROR!", JOptionPane.ERROR_MESSAGE);
                                            return false;
                                        }

                                        if(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) != lRatioDisplayed){
                                            int answer = JOptionPane.showConfirmDialog(new JFrame(), "You are going to manually change the ratio value, do you want to continue?","Manually change ratio ? ", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                                            if (answer == JOptionPane.NO_OPTION) {
                                                //close the validation frame
                                                return false;
                                            } else {
                                                //continue
                                                lManuallyChanged = true;
                                                lRatio.setRecalculatedRatio(lRatioDisplayed, iQuantitativeValidationSingelton.isLog2());
                                                if(iQuantitativeValidationSingelton.isLog2()){
                                                    lQuant.setRatio(Math.pow(2,lRatioDisplayed));
                                                } else {
                                                    lQuant.setRatio(lRatioDisplayed);
                                                }
                                            }
                                        }
                                        if(lCommentField.getText().length() == 0){
                                            if(lManuallyChanged){
                                                lQuant.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                lRatio.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                            } else {
                                                if(lRatio.getComment() != null && lRatio.getComment().startsWith("MANUALLY_CHANGED")){
                                                    lQuant.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                    lRatio.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                } else {
                                                    lQuant.setComment((String) lCmbComments.getSelectedItem());
                                                    lRatio.setComment((String) lCmbComments.getSelectedItem());
                                                }
                                            }
                                        } else {
                                            if(lManuallyChanged){
                                                lQuant.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                lRatio.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                            } else {
                                                if(lRatio.getComment() != null && lRatio.getComment().startsWith("MANUALLY_CHANGED")){
                                                    lQuant.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                    lRatio.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                } else {
                                                    lQuant.setComment(lCommentField.getText());
                                                    lRatio.setComment(lCommentField.getText());
                                                }
                                            }
                                        }
                                        try {
                                            lQuant.updateLowPriority(iConnMsLims);
                                        } catch (SQLException e) {
                                            JOptionPane.showMessageDialog(new JFrame(), new String[]{"An error occurred will changing the valid status: ", e.getMessage()}, "ERROR!", JOptionPane.ERROR_MESSAGE);
                                            logger.error(e.getMessage(), e);
                                        }
                                        return true;
                                    }

                                    public void finished() {
                                        //do nothing when finished
                                    }
                                };
                                lUpdateDbThread.start();
                            } else {
                                // not in database mode
                                iQuantitativeValidationSingelton.addValidatedRatio(lRatio);
                            }
                            if(lCommentField.getText().length() == 0){
                                lRatio.setComment((String) lCmbComments.getSelectedItem());
                            } else {
                                lRatio.setComment((String) lCommentField.getText());
                            }
                            iParent.loadProtein(false);
                            close();
                        }
                    });

                    //create the ratio label
                    if(iQuantitativeValidationSingelton.isDatabaseMode()){
                        JLabel lRatioLabel = new JLabel("   Ratio: ");
                        lRatioField = new JTextField(String.valueOf(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2())));
                        lRatioField.setPreferredSize(new Dimension(120, 22));
                        lRatioField.setMinimumSize(new Dimension(120, 22));
                        lRatioField.setMaximumSize(new Dimension(120, 22));
                        lRatioLabel.setForeground(Color.GREEN);
                        lRatioButtonPanel.add(lRatioLabel);
                        lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                        lRatioButtonPanel.add(lRatioField);
                        lRatioButtonPanel.add(Box.createHorizontalGlue());
                    } else {
                        JLabel lRatioLabel = new JLabel("   Ratio: " + lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()));
                        lRatioLabel.setForeground(Color.GREEN);
                        lRatioButtonPanel.add(lRatioLabel);
                        lRatioButtonPanel.add(Box.createHorizontalGlue());
                    }
                    if (lRatio.getComment() != null) {
                        //if a comment is found add it
                        JLabel lRatioComment = new JLabel("   Comment: " + lRatio.getComment());
                        lRatioButtonPanel.add(lRatioComment);
                    }

                    lRatioButtonPanel.add(Box.createHorizontalGlue());
                    lRatioButtonPanel.add(lCmbComments);
                    lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                    lRatioButtonPanel.add(lCommentField);
                    lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                    lRatioButtonPanel.add(lBtnSetComment);
                    lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                    lRatioButtonPanel.add(lBtnSetInvalid);
                    lRatioButtonPanel.add(Box.createHorizontalGlue());


                    jpanRatio.add(lRatioButtonPanel);
                } else {
                    //the ratio is invalid => it can be set valid
                    //create a combo box with reasons and a button to set it valid

                    //create the a panel to store the ratio , combobox and button
                    JPanel lRatioButtonPanel = new JPanel();
                    lRatioButtonPanel.setLayout(new BoxLayout(lRatioButtonPanel, BoxLayout.X_AXIS));
                    lRatioButtonPanel.setBackground(Color.white);

                    //create the JCombobox with reasons
                    final JComboBox lCmbComments = new JComboBox(lComments);
                    lCmbComments.setPreferredSize(new Dimension(120, 22));
                    lCmbComments.setMinimumSize(new Dimension(120, 22));
                    lCmbComments.setMaximumSize(new Dimension(120, 22));
                    final JTextField lCommentField = new JTextField();
                    lCommentField.setPreferredSize(new Dimension(120, 22));
                    lCommentField.setMinimumSize(new Dimension(120, 22));
                    lCommentField.setMaximumSize(new Dimension(120, 22));
                    //create the button
                    JButton lBtnSetInvalid = new JButton("Set comment and valid");
                    lBtnSetInvalid.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (iQuantitativeValidationSingelton.isDatabaseMode()) {
                                //update the db in an other thread
                                com.compomics.util.sun.SwingWorker lUpdateDbThread = new com.compomics.util.sun.SwingWorker() {
                                    public Boolean construct() {
                                        QuantitationExtension lQuant = null;
                                        if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS){
                                            DistillerRatio lDistRatio = (DistillerRatio) lRatio;
                                            lQuant = lDistRatio.getQuantitationStoredInDb();
                                        } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.THERMO_MSF_LIMS){
                                            MsfLimsRatio lDistRatio = (MsfLimsRatio) lRatio;
                                            lQuant = lDistRatio.getQuantitationStoredInDb();
                                        } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS){
                                            MaxQuantRatio lDistRatio = (MaxQuantRatio) lRatio;
                                            lQuant = lDistRatio.getQuantitationStoredInDb();
                                        }
                                        double lRatioDisplayed;
                                        boolean lManuallyChanged = false;
                                        try{
                                            lRatioDisplayed = Double.valueOf(lRatioField.getText());
                                        } catch(NumberFormatException e){
                                            JOptionPane.showMessageDialog(new JFrame(), new String[]{lRatioField.getText(), " is not a correct ratio."}, "ERROR!", JOptionPane.ERROR_MESSAGE);
                                            return false;
                                        }

                                        if(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) != lRatioDisplayed){
                                            int answer = JOptionPane.showConfirmDialog(new JFrame(), "You are going to manually change the ratio value, do you want to continue?","Manually change ratio ? ", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                                            if (answer == JOptionPane.NO_OPTION) {
                                                //close the validation frame
                                                return false;
                                            } else {
                                                //continue
                                                lManuallyChanged = true;
                                                lRatio.setRecalculatedRatio(lRatioDisplayed, iQuantitativeValidationSingelton.isLog2());
                                                if(iQuantitativeValidationSingelton.isLog2()){
                                                    lQuant.setRatio(Math.pow(2,lRatioDisplayed));
                                                } else {
                                                    lQuant.setRatio(lRatioDisplayed);
                                                }
                                            }
                                        }
                                        if(lCommentField.getText().length() == 0){
                                            if(lManuallyChanged){
                                                lQuant.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                lRatio.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                            } else {
                                                if(lRatio.getComment() != null && lRatio.getComment().startsWith("MANUALLY_CHANGED")){
                                                    lQuant.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                    lRatio.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                } else {
                                                    lQuant.setComment((String) lCmbComments.getSelectedItem());
                                                    lRatio.setComment((String) lCmbComments.getSelectedItem());
                                                }
                                            }
                                        } else {
                                            if(lManuallyChanged){
                                                lQuant.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                lRatio.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                            } else {
                                                if(lRatio.getComment() != null && lRatio.getComment().startsWith("MANUALLY_CHANGED")){
                                                    lQuant.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                    lRatio.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                } else {
                                                    lQuant.setComment(lCommentField.getText());
                                                    lRatio.setComment(lCommentField.getText());
                                                }
                                            }
                                        }
                                        lQuant.setValid(true);
                                        try {
                                            lQuant.updateLowPriority(iConnMsLims);
                                        } catch (SQLException e) {
                                            JOptionPane.showMessageDialog(new JFrame(), new String[]{"An error occurred will changing the valid status: ", e.getMessage()}, "ERROR!", JOptionPane.ERROR_MESSAGE);
                                            logger.error(e.getMessage(), e);
                                        }
                                        return true;
                                    }

                                    public void finished() {
                                        //do nothing when finished
                                    }
                                };
                                lUpdateDbThread.start();
                            } else {
                                //not in database mode
                                iQuantitativeValidationSingelton.addValidatedRatio(lRatio);
                            }
                            if(lCommentField.getText().length() == 0){
                                lRatio.setComment((String) lCmbComments.getSelectedItem());
                            } else {
                                lRatio.setComment((String) lCommentField.getText());
                            }
                            lRatio.setValid(true);
                            iParent.loadProtein(false);
                            close();
                        }
                    });
                    JButton lBtnSetComment = new JButton("Set comment");
                    lBtnSetComment.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (iQuantitativeValidationSingelton.isDatabaseMode()) {
                                com.compomics.util.sun.SwingWorker lUpdateDbThread = new com.compomics.util.sun.SwingWorker() {
                                    public Boolean construct() {
                                        QuantitationExtension lQuant = null;
                                        if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS){
                                            DistillerRatio lDistRatio = (DistillerRatio) lRatio;
                                            lQuant = lDistRatio.getQuantitationStoredInDb();
                                        } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.THERMO_MSF_LIMS){
                                            MsfLimsRatio lDistRatio = (MsfLimsRatio) lRatio;
                                            lQuant = lDistRatio.getQuantitationStoredInDb();
                                        } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS){
                                            MaxQuantRatio lDistRatio = (MaxQuantRatio) lRatio;
                                            lQuant = lDistRatio.getQuantitationStoredInDb();
                                        }
                                        double lRatioDisplayed;
                                        boolean lManuallyChanged = false;
                                        try{
                                            lRatioDisplayed = Double.valueOf(lRatioField.getText());
                                        } catch(NumberFormatException e){
                                            JOptionPane.showMessageDialog(new JFrame(), new String[]{lRatioField.getText(), " is not a correct ratio."}, "ERROR!", JOptionPane.ERROR_MESSAGE);
                                            return false;
                                        }

                                        if(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) != lRatioDisplayed){
                                            int answer = JOptionPane.showConfirmDialog(new JFrame(), "You are going to manually change the ratio value, do you want to continue?","Manually change ratio ? ", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
                                            if (answer == JOptionPane.NO_OPTION) {
                                                //close the validation frame
                                                return false;
                                            } else {
                                                //continue
                                                lManuallyChanged = true;
                                                lRatio.setRecalculatedRatio(lRatioDisplayed, iQuantitativeValidationSingelton.isLog2());
                                                if(iQuantitativeValidationSingelton.isLog2()){
                                                    lQuant.setRatio(Math.pow(2,lRatioDisplayed));
                                                } else {
                                                    lQuant.setRatio(lRatioDisplayed);
                                                }
                                            }
                                        }
                                        if(lCommentField.getText().length() == 0){
                                            if(lManuallyChanged){
                                                lQuant.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                lRatio.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                            } else {
                                                if(lRatio.getComment() != null && lRatio.getComment().startsWith("MANUALLY_CHANGED")){
                                                    lQuant.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                    lRatio.setComment("MANUALLY_CHANGED_" + (String) lCmbComments.getSelectedItem());
                                                } else {
                                                    lQuant.setComment((String) lCmbComments.getSelectedItem());
                                                    lRatio.setComment((String) lCmbComments.getSelectedItem());
                                                }
                                            }
                                        } else {
                                            if(lManuallyChanged){
                                                lQuant.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                lRatio.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                            } else {
                                                if(lRatio.getComment() != null && lRatio.getComment().startsWith("MANUALLY_CHANGED")){
                                                    lQuant.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                    lRatio.setComment("MANUALLY_CHANGED_" + lCommentField.getText());
                                                } else {
                                                    lQuant.setComment(lCommentField.getText());
                                                    lRatio.setComment(lCommentField.getText());
                                                }
                                            }
                                        }
                                        try {
                                            lQuant.updateLowPriority(iConnMsLims);
                                        } catch (SQLException e) {
                                            JOptionPane.showMessageDialog(new JFrame(), new String[]{"An error occurred will changing the valid status: ", e.getMessage()}, "ERROR!", JOptionPane.ERROR_MESSAGE);
                                            logger.error(e.getMessage(), e);
                                        }
                                        return true;
                                    }

                                    public void finished() {
                                        //do nothing when finished
                                    }
                                };
                                lUpdateDbThread.start();
                            } else {
                                // not in database mode
                                iQuantitativeValidationSingelton.addValidatedRatio(lRatio);
                            }
                            lRatio.setComment((String) lCmbComments.getSelectedItem());
                            iParent.loadProtein(false);
                            close();
                        }
                    });
                    //create the ratio label
                    if(iQuantitativeValidationSingelton.isDatabaseMode()){
                        JLabel lRatioLabel;
                        if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                            DistillerRatio lDistRatio = (DistillerRatio) lRatio;
                            lRatioLabel = new JLabel("   Ratio:   (quality : " + lDistRatio.getQuality() + " )");
                        } else {
                            lRatioLabel = new JLabel("   Ratio: " );
                        }
                        lRatioLabel.setForeground(Color.RED);
                        lRatioField = new JTextField(String.valueOf(lRatio.getRatio(iQuantitativeValidationSingelton.isLog2())));
                        lRatioField.setPreferredSize(new Dimension(120, 22));
                        lRatioField.setMinimumSize(new Dimension(120, 22));
                        lRatioField.setMaximumSize(new Dimension(120, 22));
                        lRatioButtonPanel.add(lRatioLabel);
                        lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                        lRatioButtonPanel.add(lRatioField);
                        lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                    } else {
                        JLabel lRatioLabel;
                        if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                            DistillerRatio lDistRatio = (DistillerRatio) lRatio;
                            lRatioLabel = new JLabel("   Ratio: " + lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) + "   quality ( " + lDistRatio.getQuality() + " )");
                        } else {
                            lRatioLabel = new JLabel("   Ratio: " + lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()));
                        }
                        lRatioLabel.setForeground(Color.RED);
                        lRatioButtonPanel.add(lRatioLabel);
                        lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                    }


                    if (lRatio.getComment() != null) {
                        //if a comment is found add it
                        JLabel lRatioComment = new JLabel("   Comment: " + lRatio.getComment());
                        lRatioButtonPanel.add(lRatioComment);
                    }
                    lRatioButtonPanel.add(Box.createHorizontalGlue());
                    lRatioButtonPanel.add(lCmbComments);
                    lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                    lRatioButtonPanel.add(lCommentField);
                    lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                    lRatioButtonPanel.add(lBtnSetComment);
                    lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                    lRatioButtonPanel.add(lBtnSetInvalid);
                    lRatioButtonPanel.add(Box.createHorizontalStrut(5));

                    //add it to jpanRatio
                    jpanRatio.add(lRatioButtonPanel);
                    //show invalid reasons
                    if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                        DistillerRatio lDistRatio = (DistillerRatio) lRatio;
                        for (int j = 0; j < lDistRatio.getNotValidState().size(); j++) {
                            jpanRatio.add(Box.createVerticalStrut(5));
                            JLabel lRatioLabelExtra = new JLabel("      " + lDistRatio.getNotValidState().get(j) + "   limit: " + lDistRatio.getNotValidExtraInfo().get(j));
                            jpanRatio.add(lRatioLabelExtra);
                            jpanRatio.add(Box.createVerticalStrut(5));
                        }
                    }
                }
                jpanRatio.add(Box.createVerticalGlue());
                jpanExtra.add(jpanRatio);
                jpanExtra.add(Box.createVerticalGlue());
            }


            //get information for every identification
            if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.ITRAQ_DAT || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.ITRAQ_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.ITRAQ_ROV){
                    //it's itraq data, we cannot get the identification for a component because the type of identification is something like "iTraq 4plex"
                    PeptideIdentification lIdentification = iRatioGroup.getIdentification(0);
                    if (lIdentification != null) {
                        // an identification for this type is found
                        // add the information to a panel
                        JPanel jpanIdentfication = new JPanel();
                        jpanIdentfication.setLayout(new BoxLayout(jpanIdentfication, BoxLayout.Y_AXIS));
                        jpanIdentfication.setBackground(Color.white);
                        jpanIdentfication.setBorder(BorderFactory.createTitledBorder(lIdentification.getType() + " component identified"));
                        jpanIdentfication.add(new JLabel("      " + lIdentification.getModified_sequence() + "  score: " + lIdentification.getScore() + "  threshold: " + lIdentification.getIdentitythreshold() + "  homology: " + lIdentification.getHomology() + "  confidence: " + lIdentification.getConfidence()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Spectrum: " + lIdentification.getSpectrumFileName()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Charge: " + lIdentification.getCharge() + "  precursor mass: " + lIdentification.getPrecursor() + "  exp mass: " + lIdentification.getExp_mass() + "  cal mass: " + lIdentification.getCal_mass() + "  ppm: " + Math.abs(lIdentification.getCal_mass().doubleValue() - lIdentification.getExp_mass().doubleValue()) * 1000000.0 / (lIdentification.getExp_mass().doubleValue())));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                            DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) iRatioGroup;
                            jpanIdentfication.add(new JLabel("      Rov file: " + lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME)  + "  hit: " + lRatioGroup.getReferenceOfParentHit()));
                        }

                        //wrap the jpanIdentification in an other panel for a better positioning
                        JPanel jpanIdentificationBetterPositioning = new JPanel();
                        jpanIdentificationBetterPositioning.setLayout(new BoxLayout(jpanIdentificationBetterPositioning, BoxLayout.X_AXIS));
                        jpanIdentificationBetterPositioning.setBackground(Color.white);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));
                        jpanIdentificationBetterPositioning.add(jpanIdentfication);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));

                        //add the identification information to the panel
                        jpanExtra.add(jpanIdentificationBetterPositioning);
                        jpanExtra.add(Box.createVerticalStrut(5));
                    }

            } else if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                //we will show the identification for a specific component type
                for (int i = 0; i < iRatioGroup.getParentCollection().getComponentTypes().size(); i++) {
                    PeptideIdentification lIdentification = iRatioGroup.getIdentificationForType(iRatioGroup.getParentCollection().getComponentTypes().get(i));
                    if (lIdentification != null) {
                        // an identification for this type is found
                        // add the information to a panel
                        JPanel jpanIdentfication = new JPanel();
                        jpanIdentfication.setLayout(new BoxLayout(jpanIdentfication, BoxLayout.Y_AXIS));
                        jpanIdentfication.setBackground(Color.white);
                        jpanIdentfication.setBorder(BorderFactory.createTitledBorder(lIdentification.getType() + " component identified"));
                        jpanIdentfication.add(new JLabel("      " + lIdentification.getModified_sequence() + "  score: " + lIdentification.getScore() + "  threshold: " + lIdentification.getIdentitythreshold() + "  homology: " + lIdentification.getHomology() + "  confidence: " + lIdentification.getConfidence()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Charge: " + lIdentification.getCharge() + "  precursor mass: " + lIdentification.getPrecursor() + "  exp mass: " + lIdentification.getExp_mass() + "  cal mass: " + lIdentification.getCal_mass() + "  ppm: " + Math.abs(lIdentification.getCal_mass().doubleValue() - lIdentification.getExp_mass().doubleValue()) * 1000000.0 / (lIdentification.getExp_mass().doubleValue())));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Filename: " + lIdentification.getSpectrumFileName()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS) {
                            DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) iRatioGroup;
                            jpanIdentfication.add(new JLabel("      Rov file: " + lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME)  + "  hit: " + lRatioGroup.getReferenceOfParentHit()));
                        }

                        //wrap the jpanIdentification in an other panel for a better positioning
                        JPanel jpanIdentificationBetterPositioning = new JPanel();
                        jpanIdentificationBetterPositioning.setLayout(new BoxLayout(jpanIdentificationBetterPositioning, BoxLayout.X_AXIS));
                        jpanIdentificationBetterPositioning.setBackground(Color.white);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));
                        jpanIdentificationBetterPositioning.add(jpanIdentfication);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));

                        //add the identification information to the panel
                        jpanExtra.add(jpanIdentificationBetterPositioning);
                        jpanExtra.add(Box.createVerticalStrut(5));
                    }
                }
            } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_NO_SIGN  ) {
                //it's Max_quant data
                for (int i = 0; i < iRatioGroup.getParentCollection().getComponentTypes().size(); i++) {
                    DefaultPeptideIdentification lIdentification = (DefaultPeptideIdentification) iRatioGroup.getIdentificationForType(iRatioGroup.getParentCollection().getComponentTypes().get(i));

                    if (lIdentification != null) {
                        // an identification for this type is found
                        // add the information to a panel
                        JPanel jpanIdentfication = new JPanel();
                        jpanIdentfication.setLayout(new BoxLayout(jpanIdentfication, BoxLayout.Y_AXIS));
                        jpanIdentfication.setBackground(Color.white);
                        jpanIdentfication.setBorder(BorderFactory.createTitledBorder(lIdentification.getType() + " component identified"));
                        jpanIdentfication.add(new JLabel("      " + lIdentification.getSequence() + " ( modified sequence : '" + lIdentification.getModified_sequence() + "')  score: " + lIdentification.getScore()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Charge: " + lIdentification.getCharge() + "  precursor mass: " + lIdentification.getPrecursor() + "  exp mass: " + Math.round(lIdentification.getExp_mass().doubleValue()*1000.0)/1000.0 + "  cal mass: " + lIdentification.getCal_mass() + "  ppm: " + Math.round(Math.abs((lIdentification.getCal_mass().doubleValue() - lIdentification.getExp_mass().doubleValue()) * 1000000.0 / (lIdentification.getExp_mass().doubleValue()))*1000.0)/1000.0));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Filename: " + lIdentification.getSpectrumFileName() ));
                        jpanIdentfication.add(Box.createVerticalStrut(5));

                        //wrap the jpanIdentification in an other panel for a better positioning
                        JPanel jpanIdentificationBetterPositioning = new JPanel();
                        jpanIdentificationBetterPositioning.setLayout(new BoxLayout(jpanIdentificationBetterPositioning, BoxLayout.X_AXIS));
                        jpanIdentificationBetterPositioning.setBackground(Color.white);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));
                        jpanIdentificationBetterPositioning.add(jpanIdentfication);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));

                        //add the identification information to the panel
                        jpanExtra.add(jpanIdentificationBetterPositioning);
                        jpanExtra.add(Box.createVerticalStrut(5));
                    }

                }
            } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.CENSUS) {
                //it's Max_quant data
                for (int i = 0; i < iRatioGroup.getParentCollection().getComponentTypes().size(); i++) {
                    DefaultPeptideIdentification lIdentification = (DefaultPeptideIdentification) iRatioGroup.getIdentificationForType(iRatioGroup.getParentCollection().getComponentTypes().get(i));

                    if (lIdentification != null) {
                        // an identification for this type is found
                        // add the information to a panel
                        JPanel jpanIdentfication = new JPanel();
                        jpanIdentfication.setLayout(new BoxLayout(jpanIdentfication, BoxLayout.Y_AXIS));
                        jpanIdentfication.setBackground(Color.white);
                        jpanIdentfication.setBorder(BorderFactory.createTitledBorder("Identification"));
                        jpanIdentfication.add(new JLabel("      " + lIdentification.getSequence() + "  xCorr: " + lIdentification.getXcorr()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Charge: " + lIdentification.getCharge()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Filename: " + lIdentification.getSpectrumFileName() ));
                        jpanIdentfication.add(Box.createVerticalStrut(5));

                        //wrap the jpanIdentification in an other panel for a better positioning
                        JPanel jpanIdentificationBetterPositioning = new JPanel();
                        jpanIdentificationBetterPositioning.setLayout(new BoxLayout(jpanIdentificationBetterPositioning, BoxLayout.X_AXIS));
                        jpanIdentificationBetterPositioning.setBackground(Color.white);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));
                        jpanIdentificationBetterPositioning.add(jpanIdentfication);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));

                        //add the identification information to the panel
                        jpanExtra.add(jpanIdentificationBetterPositioning);
                        jpanExtra.add(Box.createVerticalStrut(5));
                    }

                }
            } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.THERMO_MSF_FILES) {
                //it's thermo msf file
                for (int i = 0; i < iRatioGroup.getParentCollection().getComponentTypes().size(); i++) {
                    DefaultPeptideIdentification lIdentification = (DefaultPeptideIdentification) iRatioGroup.getIdentificationForType(iRatioGroup.getParentCollection().getComponentTypes().get(i));

                    if (lIdentification != null) {
                        // an identification for this type is found
                        // add the information to a panel
                        JPanel jpanIdentfication = new JPanel();
                        jpanIdentfication.setLayout(new BoxLayout(jpanIdentfication, BoxLayout.Y_AXIS));
                        jpanIdentfication.setBackground(Color.white);
                        jpanIdentfication.setBorder(BorderFactory.createTitledBorder("Identification"));
                        jpanIdentfication.add(new JLabel("      " + lIdentification.getSequence() + "  Score: " + lIdentification.getScore()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Charge: " + lIdentification.getCharge()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Filename: " + lIdentification.getSpectrumFileName() ));
                        jpanIdentfication.add(Box.createVerticalStrut(5));

                        //wrap the jpanIdentification in an other panel for a better positioning
                        JPanel jpanIdentificationBetterPositioning = new JPanel();
                        jpanIdentificationBetterPositioning.setLayout(new BoxLayout(jpanIdentificationBetterPositioning, BoxLayout.X_AXIS));
                        jpanIdentificationBetterPositioning.setBackground(Color.white);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));
                        jpanIdentificationBetterPositioning.add(jpanIdentfication);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));

                        //add the identification information to the panel
                        jpanExtra.add(jpanIdentificationBetterPositioning);
                        jpanExtra.add(Box.createVerticalStrut(5));
                    }

                }

            } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.THERMO_MSF_LIMS){
                List<String> lComp = (List<String>) ((ArrayList<String>)iRatioGroup.getParentCollection().getComponentTypes()).clone();
                lComp.add("Not defined");
                for (int i = 0; i < iRatioGroup.getParentCollection().getComponentTypes().size(); i++) {
                    PeptideIdentification lIdentification = iRatioGroup.getIdentificationForType(iRatioGroup.getParentCollection().getComponentTypes().get(i));
                    if (lIdentification != null) {
                        // an identification for this type is found
                        // add the information to a panel
                        JPanel jpanIdentfication = new JPanel();
                        jpanIdentfication.setLayout(new BoxLayout(jpanIdentfication, BoxLayout.Y_AXIS));
                        jpanIdentfication.setBackground(Color.white);
                        jpanIdentfication.setBorder(BorderFactory.createTitledBorder(lIdentification.getType() + " component identified"));
                        jpanIdentfication.add(new JLabel("      " + lIdentification.getModified_sequence() + "  score: " + lIdentification.getScore() + "  threshold: " + lIdentification.getIdentitythreshold() + "  homology: " + lIdentification.getHomology() + "  confidence: " + lIdentification.getConfidence()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Charge: " + lIdentification.getCharge() + "  precursor mass: " + lIdentification.getPrecursor() + "  exp mass: " + lIdentification.getExp_mass() + "  cal mass: " + lIdentification.getCal_mass() + "  ppm: " + Math.abs(lIdentification.getCal_mass().doubleValue() - lIdentification.getExp_mass().doubleValue()) * 1000000.0 / (lIdentification.getExp_mass().doubleValue())));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Filename: " + lIdentification.getSpectrumFileName()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS) {
                            DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) iRatioGroup;
                            jpanIdentfication.add(new JLabel("      Rov file: " + lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME)  + "  hit: " + lRatioGroup.getReferenceOfParentHit()));
                        }

                        //wrap the jpanIdentification in an other panel for a better positioning
                        JPanel jpanIdentificationBetterPositioning = new JPanel();
                        jpanIdentificationBetterPositioning.setLayout(new BoxLayout(jpanIdentificationBetterPositioning, BoxLayout.X_AXIS));
                        jpanIdentificationBetterPositioning.setBackground(Color.white);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));
                        jpanIdentificationBetterPositioning.add(jpanIdentfication);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));

                        //add the identification information to the panel
                        jpanExtra.add(jpanIdentificationBetterPositioning);
                        jpanExtra.add(Box.createVerticalStrut(5));
                    }
                }

            } else {
                //it's Ms_quant data
                    PeptideIdentification lIdentification = iRatioGroup.getIdentification(0);
                    if (lIdentification != null) {
                        // an identification for this type is found
                        // add the information to a panel
                        JPanel jpanIdentfication = new JPanel();
                        jpanIdentfication.setLayout(new BoxLayout(jpanIdentfication, BoxLayout.Y_AXIS));
                        jpanIdentfication.setBackground(Color.white);
                        jpanIdentfication.setBorder(BorderFactory.createTitledBorder(lIdentification.getType() + " component identified"));
                        jpanIdentfication.add(new JLabel("      " + lIdentification.getSequence() + " ( modifications : '" + lIdentification.getModified_sequence() + "')  score: " + lIdentification.getScore()));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        jpanIdentfication.add(new JLabel("      Charge: " + lIdentification.getCharge() + "  precursor mass: " + lIdentification.getPrecursor() + "  exp mass: " + lIdentification.getExp_mass() + "  cal mass: " + lIdentification.getCal_mass() + "  ppm: " + Math.abs(lIdentification.getCal_mass().doubleValue() - lIdentification.getExp_mass().doubleValue()) * 1000000.0 / (lIdentification.getExp_mass().doubleValue())));
                        jpanIdentfication.add(Box.createVerticalStrut(5));
                        

                        //wrap the jpanIdentification in an other panel for a better positioning
                        JPanel jpanIdentificationBetterPositioning = new JPanel();
                        jpanIdentificationBetterPositioning.setLayout(new BoxLayout(jpanIdentificationBetterPositioning, BoxLayout.X_AXIS));
                        jpanIdentificationBetterPositioning.setBackground(Color.white);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));
                        jpanIdentificationBetterPositioning.add(jpanIdentfication);
                        jpanIdentificationBetterPositioning.add(Box.createHorizontalStrut(5));

                        //add the identification information to the panel
                        jpanExtra.add(jpanIdentificationBetterPositioning);
                        jpanExtra.add(Box.createVerticalStrut(5));
                    }
            }

            jpanGraphs = new JPanel();
            jpanGraphs.setLayout(new BoxLayout(jpanGraphs, BoxLayout.Y_AXIS));
            if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV ) {
                DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) iRatioGroup;
                ChartPanel lXic = new ChartPanel(lRatioGroup.getXicChart());
                //add the XIC panel
                jpanGraphs.add(Box.createVerticalStrut(5));
                jpanGraphs.add(lXic);
                jpanGraphs.add(Box.createVerticalStrut(5));
                ChartPanel lInt = new ChartPanel(lRatioGroup.getIntensityChart());
                //add the absolute intensity panel
                jpanGraphs.add(lInt);
                jpanGraphs.add(Box.createVerticalStrut(5));
                jpanGraphs.setBackground(Color.white);
            } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_NO_SIGN ){
                MaxQuantRatioGroup lRatioGroup = (MaxQuantRatioGroup) iRatioGroup;
                ChartPanel lInt = new ChartPanel(lRatioGroup.getIntensityChart());
                //add the absolute intensity panel
                jpanGraphs.add(lInt);
                jpanGraphs.add(Box.createVerticalStrut(5));
                jpanGraphs.setBackground(Color.white);
            } else if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.CENSUS) {
                CensusRatioGroup lRatioGroup = (CensusRatioGroup) iRatioGroup;
                ChartPanel lXic = new ChartPanel(lRatioGroup.getXicChart());
                //add the XIC panel
                jpanGraphs.add(Box.createVerticalStrut(5));
                jpanGraphs.add(lXic);
                jpanGraphs.add(Box.createVerticalStrut(5));
                ChartPanel lInt = new ChartPanel(lRatioGroup.getIntensityChart());
                //add the absolute intensity panel
                jpanGraphs.add(lInt);
                jpanGraphs.add(Box.createVerticalStrut(5));
                jpanGraphs.setBackground(Color.white);
            } /* else if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.THERMO_MSF_FILES) {
                try{
                    ThermoMsfRatio lRatio = (ThermoMsfRatio) iRatioGroup.getRatio(0);

                    QuanResult lQuan = lRatio.getQuanResult();
                    //get the quan events
                    Vector<com.compomics.thermo_msf_parser.msf.Event> lQuanEvents = new Vector<com.compomics.thermo_msf_parser.msf.Event>();
                    Vector<Integer> lQuanEventsIds = new Vector<Integer>();
                    Vector<Vector<com.compomics.thermo_msf_parser.msf.Event>> lQuanEventsByPattern = new Vector<Vector<com.compomics.thermo_msf_parser.msf.Event>>();
                    for (int p = 0; p < lQuan.getIsotopePatterns().size(); p++) {
                        Vector<com.compomics.thermo_msf_parser.msf.Event> lIsotopePatternEvents = lQuan.getIsotopePatterns().get(p).getEventsWithQuanResult(lRatio.getConnection());
                        lQuanEventsByPattern.add(lIsotopePatternEvents);
                        for (int j = 0; j < lIsotopePatternEvents.size(); j++) {
                            lQuanEvents.add(lIsotopePatternEvents.get(j));
                            lQuanEventsIds.add(lIsotopePatternEvents.get(j).getEventId());
                        }
                    }

                    //get the quan events
                    Vector<Vector<com.compomics.thermo_msf_parser.msf.Event>> lQuanEventsByPatternWithoutQuanChannel = new Vector<Vector<com.compomics.thermo_msf_parser.msf.Event>>();
                    for (int p = 0; p < lQuan.getIsotopePatterns().size(); p++) {
                        Vector<com.compomics.thermo_msf_parser.msf.Event> lIsotopePatternEvents = lQuan.getIsotopePatterns().get(p).getEventsWithoutQuanResult(lRatio.getConnection());
                        lQuanEventsByPatternWithoutQuanChannel.add(lIsotopePatternEvents);
                        for (int j = 0; j < lIsotopePatternEvents.size(); j++) {
                            lQuanEvents.add(lIsotopePatternEvents.get(j));
                            lQuanEventsIds.add(lIsotopePatternEvents.get(j).getEventId());
                        }
                    }

                    //get the min and max retention and mass
                    double lMinMass = Double.MAX_VALUE;
                    double lMinRT = Double.MAX_VALUE;
                    double lMaxMass = Double.MIN_VALUE;
                    double lMaxRT = Double.MIN_VALUE;

                    for (int i = 0; i < lQuanEvents.size(); i++) {
                        if (lMinMass > lQuanEvents.get(i).getMass()) {
                            lMinMass = lQuanEvents.get(i).getMass();
                        }
                        if (lMaxMass < lQuanEvents.get(i).getMass()) {
                            lMaxMass = lQuanEvents.get(i).getMass();
                        }
                        if (lMinRT > lQuanEvents.get(i).getRetentionTime()) {
                            lMinRT = lQuanEvents.get(i).getRetentionTime();
                        }
                        if (lMaxRT < lQuanEvents.get(i).getRetentionTime()) {
                            lMaxRT = lQuanEvents.get(i).getRetentionTime();
                        }
                    }
                    //calculate the borders
                    double lMassDiff = Math.abs(lMaxMass - lMinMass);
                    if (lMassDiff == 0) {
                        lMassDiff = 15.0;
                    }
                    lMinMass = lMinMass - (lMassDiff / 3.0);
                    lMaxMass = lMaxMass + (lMassDiff / 3.0);
                    lMinRT = lMinRT - 0.5;
                    lMaxRT = lMaxRT + 0.5;

                    Vector<com.compomics.thermo_msf_parser.msf.Event> lBackgroundEvents = com.compomics.thermo_msf_parser.msf.Event.getEventByRetentionTimeLimitMassLimitAndFileIdExcludingIds(lMinRT, lMaxRT, lMinMass, lMaxMass, lQuanEventsIds, lRatio.getFileId(), lRatio.getConnection());


                    double[] lQuanMzValues = new double[lBackgroundEvents.size()];
                    double[] lQuanIntensityValues = new double[lBackgroundEvents.size()];

                    for (int p = 0; p < lBackgroundEvents.size(); p++) {
                        lQuanMzValues[p] = lBackgroundEvents.get(p).getMass();
                        lQuanIntensityValues[p] = lBackgroundEvents.get(p).getIntensity();
                    }


                    // Updating the spectrum panel
                    SpectrumPanel iQuantificationSpectrumPanel = new SpectrumPanel(
                            lQuanMzValues,
                            lQuanIntensityValues,
                            0.0,
                            "RT: " + lMinRT + " - " + lMaxRT,
                            "",50, false, false, false, 1, false);
                    iQuantificationSpectrumPanel.rescale(lMinMass, lMaxMass);
                    iQuantificationSpectrumPanel.setProfileMode(false);
                    iQuantificationSpectrumPanel.setXAxisStartAtZero(false);
                    Vector<DefaultSpectrumAnnotation> lQuanAnnotations = new Vector<DefaultSpectrumAnnotation>();
                    for (int i = 0; i < lQuan.getIsotopePatterns().size(); i++) {
                        double[] lQuanPatternMzValues = new double[lQuanEventsByPattern.get(i).size()];
                        double[] lQuanPatternIntensityValues = new double[lQuanEventsByPattern.get(i).size()];
                        for (int j = 0; j < lQuanEventsByPattern.get(i).size(); j++) {
                            lQuanPatternMzValues[j] = lQuanEventsByPattern.get(i).get(j).getMass();
                            lQuanPatternIntensityValues[j] = lQuanEventsByPattern.get(i).get(j).getIntensity();
                            for (int k = 0; k < lQuan.getIsotopePatterns().get(i).getEventAnnotations().size(); k++) {
                                if (lQuanEventsByPattern.get(i).get(j).getEventId() == lQuan.getIsotopePatterns().get(i).getEventAnnotations().get(k).getEventId()) {
                                    if (lQuan.getIsotopePatterns().get(i).getEventAnnotations().get(k).getQuanChannelId() != -1) {
                                        lQuanAnnotations.add(new DefaultSpectrumAnnotation(lQuanEventsByPattern.get(i).get(j).getMass(), 0.000000000000000000000001, Color.BLACK, "" + lRatio.getQuanChannelNameById(lQuan.getIsotopePatterns().get(i).getEventAnnotations().get(k).getQuanChannelId())));
                                    }
                                }
                            }
                        }
                        if (lQuanPatternMzValues.length > 0) {
                            iQuantificationSpectrumPanel.addAdditionalDataset(lQuanPatternMzValues, lQuanPatternIntensityValues, Color.GREEN, Color.GREEN);
                        }
                    }

                    for (int p = 0; p < lQuan.getIsotopePatterns().size(); p++) {
                        double[] lQuanPatternMzValues = new double[lQuanEventsByPatternWithoutQuanChannel.get(p).size()];
                        double[] lQuanPatternIntensityValues = new double[lQuanEventsByPatternWithoutQuanChannel.get(p).size()];
                        for (int j = 0; j < lQuanEventsByPatternWithoutQuanChannel.get(p).size(); j++) {
                            lQuanPatternMzValues[j] = lQuanEventsByPatternWithoutQuanChannel.get(p).get(j).getMass();
                            lQuanPatternIntensityValues[j] = lQuanEventsByPatternWithoutQuanChannel.get(p).get(j).getIntensity();
                        }
                        if (lQuanPatternMzValues.length > 0) {
                            iQuantificationSpectrumPanel.addAdditionalDataset(lQuanPatternMzValues, lQuanPatternIntensityValues, Color.BLUE, Color.BLUE);
                        }
                    }

                    iQuantificationSpectrumPanel.setAnnotations(lQuanAnnotations);
                    iQuantificationSpectrumPanel.setSize(500,500);
                    //add the XIC panel
                    jpanGraphs = new JPanel();
                    jpanGraphs.setLayout(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.weightx = 1.0;
                    gbc.weighty = 1.0;
                    gbc.fill = GridBagConstraints.BOTH;
                    jpanGraphs.add(iQuantificationSpectrumPanel, gbc);
                    this.jpanGraphs.validate();
                    this.jpanGraphs.repaint();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }*/ else if (iRatioGroup.getParentCollection().getRoverSource() == RoverSource.THERMO_MSF_LIMS) {
                MsfLimsRatio lRatio = (MsfLimsRatio) iRatioGroup.getRatio(0);

                MsfLimsRatioGroup lRatioGroup = (MsfLimsRatioGroup) lRatio.getParentRatioGroup();

                LimsMsfInfoReader.QuantSpectrum lQuanSpectrum = lRatioGroup.getQuantSpectrum();
                if(lQuanSpectrum != null){
                    Vector<LimsMsfInfoReader.Peak> lPeaks = lQuanSpectrum.getPeaks();
                    Vector<Double> lMzValues = new Vector<Double>();
                    Vector<Double> lIntValues = new Vector<Double>();

                    double lMinMass = Double.MAX_VALUE;
                    double lMaxMass = Double.MIN_VALUE;

                    for(int i = 0; i<lPeaks.size(); i ++){
                        if(lPeaks.get(i).getColor() == null){
                            lMzValues.add(lPeaks.get(i).getMass());
                            if(lPeaks.get(i).getMass() > lMaxMass){
                                lMaxMass = lPeaks.get(i).getMass();
                            }
                            if(lPeaks.get(i).getMass() < lMinMass){
                                lMinMass = lPeaks.get(i).getMass();
                            }
                            lIntValues.add(lPeaks.get(i).getIntensity());
                        }
                    }

                    double[] lQuanMzValues = new double[lMzValues.size()];
                    double[] lQuanIntensityValues = new double[lIntValues.size()];
                    for(int i = 0; i<lMzValues.size(); i++){
                        lQuanMzValues[i] = lMzValues.get(i);
                        lQuanIntensityValues[i] = lIntValues.get(i);
                    }

                    // Updating the spectrum panel
                    SpectrumPanel iQuantificationSpectrumPanel = new SpectrumPanel(
                            lQuanMzValues,
                            lQuanIntensityValues,
                            0.0,
                            "" ,
                            "",50, false, false, false, 1, false);

                    iQuantificationSpectrumPanel.rescale(lMinMass, lMaxMass);
                    iQuantificationSpectrumPanel.setProfileMode(false);
                    iQuantificationSpectrumPanel.setXAxisStartAtZero(false);
                    Vector<DefaultSpectrumAnnotation> lQuanAnnotations = new Vector<DefaultSpectrumAnnotation>();

                    Vector<Double> lMzValuesColorAnno = new Vector<Double>();
                    Vector<Double> lIntValuesColorAnno = new Vector<Double>();

                    for(int i = 0; i<lPeaks.size(); i ++){
                        if(lPeaks.get(i).getAnnotation() != null){
                            lMzValuesColorAnno.add(lPeaks.get(i).getMass());
                            if(lPeaks.get(i).getMass() > lMaxMass){
                                lMaxMass = lPeaks.get(i).getMass();
                            }
                            if(lPeaks.get(i).getMass() < lMinMass){
                                lMinMass = lPeaks.get(i).getMass();
                            }
                            lIntValuesColorAnno.add(lPeaks.get(i).getIntensity());
                            lQuanAnnotations.add(new DefaultSpectrumAnnotation(lPeaks.get(i).getMass(), 0.000000000000000000000001, Color.BLACK, "" + lPeaks.get(i).getAnnotation()));
                        }
                    }

                    double[] lQuanMzValuesColorAnno = new double[lMzValuesColorAnno.size()];
                    double[] lQuanIntensityValuesColorAnno = new double[lIntValuesColorAnno.size()];
                    for(int i = 0; i<lMzValuesColorAnno.size(); i++){
                        lQuanMzValuesColorAnno[i] = lMzValuesColorAnno.get(i);
                        lQuanIntensityValuesColorAnno[i] = lIntValuesColorAnno.get(i);
                    }

                    if (lQuanMzValuesColorAnno.length > 0) {
                        iQuantificationSpectrumPanel.addAdditionalDataset(lQuanMzValuesColorAnno, lQuanIntensityValuesColorAnno, Color.GREEN, Color.GREEN);
                    }


                    Vector<Double> lMzValuesColor = new Vector<Double>();
                    Vector<Double> lIntValuesColor = new Vector<Double>();

                    for(int i = 0; i<lPeaks.size(); i ++){
                        if(lPeaks.get(i).getAnnotation() == null && lPeaks.get(i).getColor() != null){
                            lMzValuesColor.add(lPeaks.get(i).getMass());
                            if(lPeaks.get(i).getMass() > lMaxMass){
                                lMaxMass = lPeaks.get(i).getMass();
                            }
                            if(lPeaks.get(i).getMass() < lMinMass){
                                lMinMass = lPeaks.get(i).getMass();
                            }
                            lIntValuesColor.add(lPeaks.get(i).getIntensity());
                            lQuanAnnotations.add(new DefaultSpectrumAnnotation(lPeaks.get(i).getMass(), 0.000000000000000000000001, Color.BLACK, "" + lPeaks.get(i).getAnnotation()));
                        }
                    }

                    double[] lQuanMzValuesColor = new double[lMzValuesColor.size()];
                    double[] lQuanIntensityValuesColor = new double[lIntValuesColor.size()];

                    for(int i = 0; i<lMzValuesColor.size(); i++){
                        lQuanMzValuesColor[i] = lMzValuesColor.get(i);
                        lQuanIntensityValuesColor[i] = lIntValuesColor.get(i);
                    }

                    if (lQuanMzValuesColor.length > 0) {
                        iQuantificationSpectrumPanel.addAdditionalDataset(lQuanMzValuesColor, lQuanIntensityValuesColor, Color.BLUE, Color.BLUE);
                    }

                    iQuantificationSpectrumPanel.setAnnotations(lQuanAnnotations);
                    iQuantificationSpectrumPanel.setSize(500,500);
                    //add the XIC panel
                    jpanGraphs = new JPanel();
                    jpanGraphs.setLayout(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.weightx = 1.0;
                    gbc.weighty = 1.0;
                    gbc.fill = GridBagConstraints.BOTH;
                    jpanGraphs.add(iQuantificationSpectrumPanel, gbc);
                    this.jpanGraphs.validate();
                    this.jpanGraphs.repaint();
                }
            }

            //add everyting to the main panel
            jpanContent = new JPanel();
            jpanContent.setLayout(new BoxLayout(jpanContent, BoxLayout.Y_AXIS));
            jpanContent.add(Box.createVerticalStrut(5));
            jpanContent.add(jpanExtra);
            jpanContent.add(Box.createVerticalStrut(5));
            jpanContent.add(jpanGraphs);
            jpanContent.add(Box.createVerticalStrut(5));
            jpanContent.setBackground(Color.white);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * This method closes this frame
     */
    public void close() {
        this.setVisible(false);
        this.dispose();
    }

}
