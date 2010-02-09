package com.compomics.rover.gui.multiwizard;

import com.compomics.mslims.db.accessors.Project;
import com.compomics.rover.general.interfaces.WizardPanel;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.quantitation.MergedComponentType;
import com.compomics.rover.general.quantitation.MergedRatioType;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.QuantitativeProtein;

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
import java.util.ArrayList;

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

    //gui stuff
    private JButton exitButton;
    private JButton nextButton;
    private JButton previousButton;
    private JPanel jpanContent;
    private JPanel wizardPanel;

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
    private Vector<RoverSource> iRoverSource = new Vector<RoverSource>();
    private Vector<String> iTitles = new Vector<String>();
    /**
     * This validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
    /**
     * The selected project
     */
    private Vector<Project> iProject = new Vector<Project>();
    /**
     * The selected files
     */
    private Vector<Vector<File>> iFiles = new Vector<Vector<File>>();
    /**
     * The threshold (ex 0.95)
     */
    private Vector<Double> iThreshold = new Vector<Double>();
    /**
     * The current rover source
     */
    private RoverSource iCurrentRoverSource;
    /**
     * The current project
     */
    private Project iCurrentProject;
    private Vector<String[]> iCollectionsRatios;
    private Vector<String[]> iCollectionsComponents;
    private Vector<MergedComponentType> iComponentTypes = new Vector<MergedComponentType>();
    private Vector<MergedRatioType> iRatioTypes = new Vector<MergedRatioType>();
    private Vector<RatioGroupCollection> iCollections;
    private Vector<String> iProteinAccessions;
    private Vector<String> iNewComponentsType;
    private Vector<String> iNewRatioTypes;
    private Vector<QuantitativeProtein> iQuantitativeProtein;

    /**
     * @param aStandAlone
     * @param aConn
     */
    public WizardFrameHolder(boolean aStandAlone, Connection aConn) {
        this.iStandAlone = aStandAlone;
        this.iConn = aConn;

        $$$setupUI$$$();

       previousButton.setVisible(false);
        iQuantitativeValidationSingelton.setMultipleSources(true);

        //before we start we will delete all the files and folders in the temp/rover
        try {
            File lTempfolder = File.createTempFile("temp", "temp").getParentFile();
            File lTempRovFolder = new File(lTempfolder, "rover");

            if (lTempRovFolder.exists() == true) {
                deleteDir(lTempRovFolder);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //create the panel array
        iWizardPanels = new WizardPanel[]{new RoverSourcePanel(this), new DataSelectionPanel(this), new ParameterPanel(this), new OverviewPanel(this), new LoadingPanel(this), new MatchRatiosPanel(this), new MatchComponentPanel(this), new ProteinCreatorPanel(this)};

        //create action listeners
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                close();
            }
        });
        //action listener for the exit button
        //action listener for the previous button
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
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
                            nextButton.setVisible(false);
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
        this.setSize(800, 400);
        this.setLocation(150, 150);
        this.setVisible(true);
        this.setIconImage(new ImageIcon(getClass().getResource("/rover.png")).getImage());

        update(getGraphics());

    }

    public void goToPanel(int aPanelNumber) {
        //set the next index
        iWizardIndex = aPanelNumber;
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

    /**
     * This method will be done when the close button is clicked
     */
    public void close() {
        if (iStandAlone) {
            if (iConn != null) {
                //close db connection
                try {
                    System.out.println("Closing db connection");
                    iConn.close();
                } catch (SQLException e) {
                    System.out.println("Unable to close database connection!");
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
    public RoverSource getRoverSource(int iIndex) {
        return iRoverSource.get(iIndex);
    }

    /**
     * This method gets the title for a specific index
     *
     * @return String with the title
     */
    public String getTitle(int iIndex) {
        return iTitles.get(iIndex);
    }

    /**
     * This method sets the 'iRoverSource' Parameter
     *
     * @param aRoverSource The roversource
     */
    public void addRoverSource(RoverSource aRoverSource) {
        //iQuantitativeValidationSingelton.setRoverDataType(aRoverSource);
        this.iRoverSource.add(aRoverSource);
    }

    /**
     * This method adds a title
     *
     * @param aTitle
     */
    public void addTitle(String aTitle) {
        this.iTitles.add(aTitle);
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
    public void addSelectedProject(Project aProject) {
        this.iCurrentProject = aProject;
        this.iProject.add(aProject);
    }

    /**
     * This method gets the 'iProject' Parameter
     *
     * @return iProject
     */
    public Project getProject(int iIndex) {
        return iProject.get(iIndex);
    }

    /**
     * This method gets the 'iFiles' Parameter
     *
     * @return iFiles
     */
    public Vector<File> getFiles(int iIndex) {
        return iFiles.get(iIndex);
    }

    /**
     * This method sets the 'iFiles' Parameter
     *
     * @param aFiles the selected files
     */
    public void addFiles(Vector<File> aFiles) {
        this.iFiles.add(aFiles);
    }

    /**
     * This method takes care of any unrecoverable exception or error, thrown by a child thread.
     *
     * @param aThrowable Throwable that represents the unrecoverable error or exception.
     */
    public void passHotPotato(Throwable aThrowable) {
        this.passHotPotato(aThrowable, aThrowable.getMessage());
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
        iQuantitativeValidationSingelton.setUseAllProteinsForReferenceSet(lUseAllProteins);
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
        WizardFrameHolder launch = new WizardFrameHolder(true, null);
    }

    public void addThreshold(double aThreshold) {
        this.iThreshold.add(aThreshold);
    }

    public double getThreshold(int iIndex) {
        return iThreshold.get(iIndex);
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


    public void setCurrentRoverSource(RoverSource lSource) {
        this.iCurrentRoverSource = lSource;
    }

    public RoverSource getCurrentRoverSource() {
        return iCurrentRoverSource;
    }

    public Project getCurrentProject() {
        return iCurrentProject;
    }

    public Vector<RoverSource> getRoverSources() {
        return iRoverSource;
    }

    public Vector<String> getTitles() {
        return iTitles;
    }

    public void setCollectionsRatios(Vector<String[]> collectionsRatios) {
        this.iCollectionsRatios = collectionsRatios;
    }

    public Vector<String[]> getCollectionsRatios() {
        return iCollectionsRatios;
    }

    public void setCollectionsComponents(Vector<String[]> collectionsComponents) {
        this.iCollectionsComponents = collectionsComponents;
    }

    public Vector<String[]> getCollectionsComponents() {
        return iCollectionsComponents;
    }

    public Vector<MergedComponentType> getComponentTypes() {
        return iComponentTypes;
    }

    public void setComponentTypes(Vector<MergedComponentType> iComponentTypes) {
        this.iComponentTypes = iComponentTypes;
    }

    public Vector<MergedRatioType> getRatioTypes() {
        return iRatioTypes;
    }

    public void setRatioTypes(Vector<MergedRatioType> iRatioTypes) {
        this.iRatioTypes = iRatioTypes;
    }

    public void setCollections(Vector<RatioGroupCollection> collection) {
        this.iCollections = collection;
    }

    public Vector<RatioGroupCollection> getCollections() {
        return iCollections;
    }

    public void setProteinAccessions(Vector<String> proteinAccessions) {
        this.iProteinAccessions = proteinAccessions;
        iQuantitativeValidationSingelton.setProteinAccessions(proteinAccessions);
    }

    public Vector<String> getProteinAccessions() {
        return iProteinAccessions;
    }

    public void setNewCompentTypes(Vector<String> lNewComponentTypes) {
        this.iNewComponentsType = lNewComponentTypes;
        iQuantitativeValidationSingelton.setComponentTypes(lNewComponentTypes);
    }

    public void setNewRatioTypes(Vector<String> newRatioTypes) {
        this.iNewRatioTypes = newRatioTypes;
        iQuantitativeValidationSingelton.setRatioTypes(newRatioTypes);
    }

    public Vector<String> getNewRatioTypes() {
        return iNewRatioTypes;
    }

    public Vector<String> getNewComponentsType() {
        return iNewComponentsType;
    }

    public void setQuantitativeProtein(Vector<QuantitativeProtein> quantitativeProtein) {
        this.iQuantitativeProtein = quantitativeProtein;
    }

    public Vector<QuantitativeProtein> getQuantitativeProtein() {
        return iQuantitativeProtein;
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
        label1.setFont(new Font("Tahoma", Font.ITALIC, 22));
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setText(" Welcome to the multi sources Rover wizard ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        wizardPanel.add(label1, gbc);
        exitButton = new JButton();
        exitButton.setContentAreaFilled(false);
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
        nextButton.setContentAreaFilled(false);
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
        previousButton.setContentAreaFilled(false);
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
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer1, gbc);
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