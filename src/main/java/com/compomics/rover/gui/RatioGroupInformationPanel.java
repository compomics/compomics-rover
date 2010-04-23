package com.compomics.rover.gui;

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
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
        if(aRatioGroup.getParentCollection().getRoverSource() != RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS && aRatioGroup.getParentCollection().getRoverSource() != RoverSource.DISTILLER_QUANT_TOOLBOX_ROV && aRatioGroup.getParentCollection().getRoverSource() != RoverSource.CENSUS){
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
                    //create the button
                    JButton lBtnSetInvalid = new JButton("Set comment and invalid");
                    lBtnSetInvalid.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (iQuantitativeValidationSingelton.isDatabaseMode()) {
                                //in database mode
                                //update the db in an other thread
                                com.compomics.util.sun.SwingWorker lUpdateDbThread = new com.compomics.util.sun.SwingWorker() {
                                    public Boolean construct() {
                                        DistillerRatio lDistRatio = (DistillerRatio) lRatio;
                                        QuantitationExtension lQuant = lDistRatio.getQuantitationStoredInDb();
                                        lQuant.setValid(false);
                                        lQuant.setComment((String) lCmbComments.getSelectedItem());
                                        lRatio.setComment((String) lCmbComments.getSelectedItem());
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
                            lRatio.setComment((String) lCmbComments.getSelectedItem());
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
                                        DistillerRatio lDistRatio = (DistillerRatio) lRatio;
                                        QuantitationExtension lQuant = lDistRatio.getQuantitationStoredInDb();
                                        lQuant.setComment((String) lCmbComments.getSelectedItem());
                                        lRatio.setComment((String) lCmbComments.getSelectedItem());
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
                    JLabel lRatioLabel = new JLabel("   Ratio: " + lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()));
                    lRatioLabel.setForeground(Color.GREEN);
                    lRatioButtonPanel.add(lRatioLabel);
                    lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                    if (lRatio.getComment() != null) {
                        //if a comment is found add it
                        JLabel lRatioComment = new JLabel("   Comment: " + lRatio.getComment());
                        lRatioButtonPanel.add(lRatioComment);
                    }

                    lRatioButtonPanel.add(Box.createHorizontalGlue());
                    lRatioButtonPanel.add(lCmbComments);
                    lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                    lRatioButtonPanel.add(lBtnSetComment);
                    lRatioButtonPanel.add(Box.createHorizontalStrut(5));
                    lRatioButtonPanel.add(lBtnSetInvalid);
                    lRatioButtonPanel.add(Box.createHorizontalStrut(5));


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

                    //create the button
                    JButton lBtnSetInvalid = new JButton("Set comment and valid");
                    lBtnSetInvalid.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (iQuantitativeValidationSingelton.isDatabaseMode()) {
                                //update the db in an other thread
                                com.compomics.util.sun.SwingWorker lUpdateDbThread = new com.compomics.util.sun.SwingWorker() {
                                    public Boolean construct() {
                                        DistillerRatio lDistRatio = (DistillerRatio) lRatio;
                                        QuantitationExtension lQuant = lDistRatio.getQuantitationStoredInDb();
                                        lQuant.setValid(true);
                                        lQuant.setComment((String) lCmbComments.getSelectedItem());
                                        lRatio.setComment((String) lCmbComments.getSelectedItem());
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
                            lRatio.setComment((String) lCmbComments.getSelectedItem());
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
                                        DistillerRatio lDistRatio = (DistillerRatio) lRatio;
                                        QuantitationExtension lQuant = lDistRatio.getQuantitationStoredInDb();
                                        lQuant.setComment((String) lCmbComments.getSelectedItem());
                                        lRatio.setComment((String) lCmbComments.getSelectedItem());
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
                    if (lRatio.getComment() != null) {
                        //if a comment is found add it
                        JLabel lRatioComment = new JLabel("   Comment: " + lRatio.getComment());
                        lRatioButtonPanel.add(lRatioComment);
                    }
                    lRatioButtonPanel.add(Box.createHorizontalGlue());
                    lRatioButtonPanel.add(lCmbComments);
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
                jpanRatio.add(Box.createVerticalStrut(5));
                jpanExtra.add(jpanRatio);
                jpanExtra.add(Box.createVerticalStrut(5));
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
            } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_NO_SIGN  ) {
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
            } else if(iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT || iRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_NO_SIGN ){
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
