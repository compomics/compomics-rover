package com.compomics.rover.gui.wizard;

import com.compomics.rover.general.enumeration.ReferenceSetEnum;
import com.compomics.util.enumeration.CompomicsTools;
import com.compomics.util.io.PropertiesManager;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;
import org.apache.log4j.Logger;

import com.compomics.rover.general.interfaces.WizardPanel;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.util.interfaces.Flamable;
import com.compomics.mslims.db.accessors.Project;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 19-mrt-2009
 * Time: 9:14:54
 */


/**
 * This Class creates a frame with for the wizard
 */
public class WizardFrameHolder extends JFrame implements Flamable {
    // Class specific log4j logger for WizardFrameHolder instances.
    private static Logger logger = Logger.getLogger(WizardFrameHolder.class);

    //gui stuff
    private JButton exitButton;
    private JButton nextButton;
    private JButton previousButton;
    private JPanel jpanContent;
    private JPanel wizardPanel;
    private JButton btnOpenMulti;
    private JLabel lblMemory;

    /**
     * The index of the wizard
     */
    private int iWizardIndex = 0;
    /**
     * The different wizard panels
     */
    private WizardPanel[] iWizardPanels;
    /**
     * The selected wizardpanel
     */
    private WizardPanel iWizardPanel;
    /**
     * The connection to the ms_lims database
     */
    private Connection iConn;
    /**
     * Boolean that indicates if this is a standalone frame
     */
    private boolean iStandAlone;
    /**
     * The rover source
     */
    private RoverSource iRoverSource;
    /**
     * This validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
    /**
     * The selected project
     */
    private Project iProject;
    /**
     * The selected files
     */
    private Vector<File> iFiles = new Vector<File>();
    /**
     * The threshold (ex 0.95)
     */
    private double iThreshold;
    /**
     * The msf peptide confidence level
     */
    private int iMsfPeptideConfidence;
    /**
     * Boolean that indicates if we only need to use the highest scoring peptides
     */
    private boolean iMsfOnlyHighestScoring;

    /**
     * @param aStandAlone
     * @param aConn
     */
    public WizardFrameHolder(boolean aStandAlone, Connection aConn) {
        this.iStandAlone = aStandAlone;
        this.iConn = aConn;

        $$$setupUI$$$();

        //before we start we will delete all the files and folders in the temp/rover
        try {
            File lTempfolder = File.createTempFile("temp", "temp").getParentFile();
            File lTempRovFolder = new File(lTempfolder, "rover");

            if (lTempRovFolder.exists() == true) {
                deleteDir(lTempRovFolder);
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        //create the panel array
        iWizardPanels = new WizardPanel[]{new RoverSourcePanel(this), new DataSelectionPanel(this), new ParameterPanel(this), new LoadingPanel(this)};

        //create action listeners
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                close();
            }
        });
        //action listener for the exit button
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        //action listener for the mutli button
        btnOpenMulti.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                com.compomics.rover.gui.multiwizard.WizardFrameHolder launch = new com.compomics.rover.gui.multiwizard.WizardFrameHolder(true, null);
                closeFrame();
            }
        });

        //action listener for the next button
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //perform the next method on the current wizardpanel
                if (iWizardPanel == null) {
                    iWizardIndex = 0;
                    iWizardPanel = iWizardPanels[iWizardIndex];
                    iWizardPanel.construct();
                    setTitle("Rover wizard - step " + (iWizardIndex + 1));
                    //visualize the panel
                    wizardPanel.removeAll();
                    wizardPanel.setLayout(new BoxLayout(wizardPanel, BoxLayout.X_AXIS));
                    wizardPanel.add(iWizardPanel.getContentPane());
                    wizardPanel.updateUI();
                } else {
                    if (iWizardIndex == iWizardPanels.length - 1) {
                        //it's the last panel
                        //that means that something is loading
                        //set the buttons disabled
                        previousButton.setEnabled(false);
                        nextButton.setEnabled(false);
                    }
                    iWizardPanel.nextClicked();
                    //check if it's ok to proceed
                    if (iWizardPanel.feasableToProceed() && iWizardIndex != iWizardPanels.length - 1) {
                        //set the next index
                        iWizardIndex = iWizardIndex + 1;
                        //get the next panel
                        iWizardPanel = iWizardPanels[iWizardIndex];
                        iWizardPanel.construct();
                        setTitle("Rover wizard - step " + (iWizardIndex + 1));
                        //visualize the panel
                        wizardPanel.removeAll();
                        wizardPanel.setLayout(new BoxLayout(wizardPanel, BoxLayout.X_AXIS));
                        wizardPanel.add(iWizardPanel.getContentPane());
                        wizardPanel.updateUI();
                        if (iWizardIndex == iWizardPanels.length - 1) {
                            nextButton.setIcon(new ImageIcon(getClass().getResource("/finish.png")));
                            nextButton.setText("Start");
                            nextButton.setToolTipText("Start");
                        } else {
                            nextButton.setIcon(new ImageIcon(getClass().getResource("/forward.png")));
                            nextButton.setText("");
                            nextButton.setToolTipText("next");
                        }
                    } else {
                        if (iWizardIndex != iWizardPanels.length - 1) {
                            //it's not ok to proceed
                            passHotPotato(new Throwable(iWizardPanel.getNotFeasableReason()));
                        }
                    }
                }

            }
        });
        //action listener for the previous button
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (iWizardPanel == null) {
                    iWizardIndex = 0;
                    iWizardPanel = iWizardPanels[iWizardIndex];
                    iWizardPanel.construct();
                    setTitle("Rover wizard - step " + (iWizardIndex + 1));
                    //visualize the panel
                    wizardPanel.removeAll();
                    wizardPanel.setLayout(new BoxLayout(wizardPanel, BoxLayout.X_AXIS));
                    wizardPanel.add(iWizardPanel.getContentPane());
                    wizardPanel.updateUI();
                } else {
                    //perform the previous method on the current wizardpanel
                    iWizardPanel.backClicked();
                    //check if it's ok to proceed
                    if (iWizardPanel.feasableToProceed() && iWizardIndex != 0) {
                        //set the previous index
                        iWizardIndex = iWizardIndex - 1;
                        //get the previous panel
                        iWizardPanel = iWizardPanels[iWizardIndex];
                        iWizardPanel.construct();
                        setTitle("Rover wizard - step " + (iWizardIndex + 1));
                        //visualize the panel
                        wizardPanel.removeAll();
                        wizardPanel.setLayout(new BoxLayout(wizardPanel, BoxLayout.X_AXIS));
                        wizardPanel.add(iWizardPanel.getContentPane());
                        wizardPanel.updateUI();
                    } else {
                        //it's not ok to proceed
                        if (iWizardIndex != 0) {
                            passHotPotato(new Throwable(iWizardPanel.getNotFeasableReason()));
                        }
                    }
                    if (iWizardIndex == iWizardPanels.length - 1) {
                        nextButton.setIcon(new ImageIcon(getClass().getResource("/finish.png")));
                        nextButton.setText("Start");
                        nextButton.setToolTipText("Start");
                    } else {
                        nextButton.setIcon(new ImageIcon(getClass().getResource("/forward.png")));
                        nextButton.setText("");
                        nextButton.setToolTipText("next");
                    }
                }
            }
        });

        //create JFrame parameters
        this.setTitle("Rover wizard");
        this.setContentPane(jpanContent);
        this.setSize(850, 450);
        this.setLocation(150, 150);
        this.setVisible(true);
        this.setIconImage(new ImageIcon(getClass().getResource("/rover.png")).getImage());
        this.lblMemory.setText(String.valueOf((Runtime.getRuntime().maxMemory() / 1024.0) / 1024.0));
        lblMemory.setVisible(false);
        update(getGraphics());

    }

    public void setNextButtonEnabled(boolean aEnabled) {
        this.nextButton.setEnabled(aEnabled);
    }

    /**
     * This method will close the frame
     */
    public void closeFrame() {
        this.setVisible(false);
        this.dispose();
    }

    public JButton getPreviousButton() {
        return previousButton;
    }

    /**
     * This method will be done when the close button is clicked
     */
    public void close() {
        if (iStandAlone) {
            if (iConn != null) {
                //close db connection
                try {
                    logger.info("Closing db connection");
                    iConn.close();
                } catch (SQLException e) {
                    logger.info("Unable to close database connection!");
                }
            }
            //exit the program
            System.exit(0);
        } else {
            this.closeFrame();
        }
    }

    //GETTERS AND SETTERS


    /**
     * This method gets the 'Database mode' Parameter in the quantitationValidationSingelton
     *
     * @return iUseMs_lims
     */
    public boolean getUseMs_lims() {
        return iQuantitativeValidationSingelton.isDatabaseMode();
    }

    /**
     * This method gets the 'iTraqData mode' Parameter in the quantitationValidationSingelton
     *
     * @return boolean
     */
    public boolean isITraqData() {
        return iQuantitativeValidationSingelton.isITraqData();
    }

    /**
     * This method gets the 'iRoverSource' Parameter
     *
     * @return iRoverSource
     */
    public RoverSource getRoverSource() {
        return iRoverSource;
    }

    /**
     * This method sets the 'iRoverSource' Parameter
     *
     * @param aRoverSource The roversource
     */
    public void setRoverSource(RoverSource aRoverSource) {
        iQuantitativeValidationSingelton.setRoverDataType(aRoverSource);
        this.iRoverSource = aRoverSource;
    }

    /**
     * This method gets the 'iConn' Parameter
     *
     * @return iConn
     */
    public Connection getMs_limsConnection() {
        return iConn;
    }

    /**
     * This method sets the 'iConn' Parameter
     *
     * @param aConn The ms_lims connection
     */
    public void setMs_limsConnection(Connection aConn) {
        this.iConn = aConn;
    }

    /**
     * This method sets the 'iProject' Parameter
     *
     * @param aProject the selected project
     */
    public void setSelectedProject(Project aProject) {
        this.iProject = aProject;
    }

    /**
     * This method gets the 'iProject' Parameter
     *
     * @return iProject
     */
    public Project getSelectedProject() {
        return iProject;
    }

    /**
     * This method gets the 'iFiles' Parameter
     *
     * @return iFiles
     */
    public Vector<File> getFiles() {
        return iFiles;
    }

    /**
     * This method sets the 'iFiles' Parameter
     *
     * @param aFiles the selected files
     */
    public void setFiles(Vector<File> aFiles) {
        this.iFiles = aFiles;
    }

    /**
     * This method takes care of any unrecoverable exception or error, thrown by a child thread.
     *
     * @param aThrowable Throwable that represents the unrecoverable error or exception.
     */
    public void passHotPotato(Throwable aThrowable) {
        this.passHotPotato(aThrowable, aThrowable.getMessage());
        logger.warn(aThrowable.getMessage(), aThrowable);
    }

    /**
     * This method takes care of any unrecoverable exception or error, thrown by a child thread.
     *
     * @param aThrowable Throwable that represents the unrecoverable error or exception.
     * @param aMessage   String with an extra message to display.
     */
    public void passHotPotato(Throwable aThrowable, String aMessage) {
        JOptionPane.showMessageDialog(this, new String[]{"An error occurred while attempting to process your data:", aMessage}, "Error occurred!", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method sets the 'ProteinDatabaseType' in the iQuantitativeValidationSingelton
     *
     * @param lType The selected database type
     */
    public void setDatabaseType(ProteinDatabaseType lType) {
        iQuantitativeValidationSingelton.setDatabaseType(lType);
    }

    /**
     * This method gets the 'ProteinDatabaseType' from the iQuantitativeValidationSingelton
     *
     * @return ProteinDatabaseType
     */
    public ProteinDatabaseType getDatabaseType() {
        return iQuantitativeValidationSingelton.getDatabaseType();
    }

    /**
     * This method sets the 'UseAllProteinsReferenceSet' in the iQuantitativeValidationSingelton
     *
     * @param lUseAllProteins
     */
    public void setUseAllProteinsForReferenceSet(boolean lUseAllProteins) {
        if (lUseAllProteins) {
            iQuantitativeValidationSingelton.setReferenceSetEnum(ReferenceSetEnum.ALL);
        } else {
            iQuantitativeValidationSingelton.setReferenceSetEnum(ReferenceSetEnum.MOST_ABUNDANT);
        }
    }

    /**
     * This method sets the 'NumberOfProteinsInReferenceSet' in the iQuantitativeValidationSingelton
     *
     * @param lReferenceSetSize
     */
    public void setReferenceSetSize(int lReferenceSetSize) {
        iQuantitativeValidationSingelton.setNumberOfProteinsInReferenceSet(lReferenceSetSize);
    }

    /**
     * This method sets the 'iCalibratedSD' in the iQuantitativeValidationSingelton
     *
     * @param lCalibratedSD
     */
    public void setCalibratedStdev(double lCalibratedSD) {
        iQuantitativeValidationSingelton.setCalibratedStdev(lCalibratedSD);
    }

    public void setRatioValidInReferenceSet(boolean lValidInReferenceSet) {
        iQuantitativeValidationSingelton.setRatioValidInReferenceSet(lValidInReferenceSet);
    }

    public boolean isStandAlone() {
        return iStandAlone;
    }

    public static void main(String[] args) {
        PropertiesManager.getInstance().updateLog4jConfiguration(logger, CompomicsTools.ROVER);
        logger.info("Rover started");
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            // ignore exception
        }
        WizardFrameHolder launch = new WizardFrameHolder(true, null);
    }

    public void setThreshold(double aThreshold) {
        this.iThreshold = aThreshold;
    }

    public double getThreshold() {
        return iThreshold;
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public void setFastaDatabase(String text) {
        iQuantitativeValidationSingelton.setFastaDatabaseLocation(text);
    }

    public void setRatioNormalization(boolean selected) {
        iQuantitativeValidationSingelton.setNormalization(selected);
    }

    public void setPeptizerStatus(boolean selected) {
        iQuantitativeValidationSingelton.setExcludePeptizerUnvalid(selected);
    }


    public void setMsfPeptideConfidence(int i) {
        this.iMsfPeptideConfidence = i;
    }

    public void setMsfOnlyHighesScoring(boolean b) {
        this.iMsfOnlyHighestScoring = b;
    }

    public int getMsfPeptideConfidence() {
        return iMsfPeptideConfidence;
    }

    public boolean isMsfOnlyHighestScoring() {
        return iMsfOnlyHighestScoring;
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
        wizardPanel = new JPanel();
        wizardPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(wizardPanel, gbc);
        final JLabel label1 = new JLabel();
        label1.setFont(new Font("Tahoma", Font.ITALIC, 26));
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setText(" Welcome to the Rover wizard ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        wizardPanel.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Combine quantitative data from different sources:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        wizardPanel.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setFont(new Font(label3.getFont().getName(), Font.ITALIC, 10));
        label3.setText("Please cite: Colaert et. al. Rover: a tool to visualize and validate quantitative proteomics data from different sources. Proteomics  2010 Mar;10(6):1226-9.");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        wizardPanel.add(label3, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        wizardPanel.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        wizardPanel.add(spacer2, gbc);
        btnOpenMulti = new JButton();
        btnOpenMulti.setBorderPainted(true);
        btnOpenMulti.setContentAreaFilled(true);
        btnOpenMulti.setFocusPainted(false);
        btnOpenMulti.setIcon(new ImageIcon(getClass().getResource("/mutliRover.png")));
        btnOpenMulti.setText("");
        btnOpenMulti.setToolTipText("Combine different quantitative projects");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        wizardPanel.add(btnOpenMulti, gbc);
        lblMemory = new JLabel();
        lblMemory.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        wizardPanel.add(lblMemory, gbc);
        exitButton = new JButton();
        exitButton.setContentAreaFilled(true);
        exitButton.setFocusPainted(false);
        exitButton.setIcon(new ImageIcon(getClass().getResource("/exit.png")));
        exitButton.setText("");
        exitButton.setToolTipText("exit");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(exitButton, gbc);
        nextButton = new JButton();
        nextButton.setBorderPainted(true);
        nextButton.setContentAreaFilled(true);
        nextButton.setFocusPainted(false);
        nextButton.setIcon(new ImageIcon(getClass().getResource("/forward.png")));
        nextButton.setText("");
        nextButton.setToolTipText("next");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(nextButton, gbc);
        previousButton = new JButton();
        previousButton.setContentAreaFilled(true);
        previousButton.setFocusPainted(false);
        previousButton.setIcon(new ImageIcon(getClass().getResource("/back.png")));
        previousButton.setText("");
        previousButton.setToolTipText("previous");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(previousButton, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer3, gbc);
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 5, 2, 5);
        jpanContent.add(separator1, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }
}
