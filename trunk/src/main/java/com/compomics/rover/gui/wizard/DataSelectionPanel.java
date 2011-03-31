package com.compomics.rover.gui.wizard;

import com.compomics.util.enumeration.CompomicsTools;
import com.compomics.util.io.PropertiesManager;
import org.apache.log4j.Logger;

import com.compomics.mslims.db.accessors.Project;
import com.compomics.mslims.db.accessors.Protocol;
import com.compomics.util.gui.dialogs.ConnectionDialog;
import com.compomics.util.interfaces.Connectable;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.interfaces.WizardPanel;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;
import java.util.Properties;
import java.util.Vector;


/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 19-mrt-2009
 * Time: 10:25:19
 */

/**
 * This class will create a panel where the data can be selected.
 */

public class DataSelectionPanel implements Connectable, WizardPanel {
    // Class specific log4j logger for DataSelectionPanel instances.
    private static Logger logger = Logger.getLogger(DataSelectionPanel.class);

    //gui stuff
    private JPanel jpanContent;
    private JPanel ms_limsPanel;
    private JPanel filePanel;
    private JTextArea txtDescription;
    private JLabel lblUser;
    private JLabel lblProtocol;
    private JLabel lblCreationdate;
    private JLabel lblTitle;
    private JComboBox cmbProjects;
    private JButton openButton;
    private JList fileList;

    //_______________________ms-lims stuff
    /**
     * The connection to the ms_lims database
     */
    private Connection iConn;
    /**
     * All the ms_lims projects
     */
    private Project[] iProjects = new Project[0];
    /**
     * The selected project
     */
    private Project iProject;
    /**
     * The protocols from the database
     */
    private Protocol[] iProtocols;
    /**
     * Date-time format String.
     */
    private static final String iDateTimeFormat = "dd/MM/yyyy - HH:mm:ss";
    /**
     * The SimpleDateFormat formatter to display creationdates.
     */
    private static SimpleDateFormat iSDF = new SimpleDateFormat(iDateTimeFormat);

    //_______________________file stuff
    /**
     * The select files
     */
    private Vector<File> iFiles = new Vector<File>();

    /**
     * The wizard frame holder parent
     */
    private WizardFrameHolder iParent;
    /**
     * The rover source
     */
    private RoverSource iRoverSource;
    /**
     * Boolean that indicates if we can go to the next panel
     */
    private boolean iFeasableToProceed = true;
    /**
     * The reason why we cannot go to the next panel
     */
    private String iNotFeasableReason;
    /**
     * The quantitation validation singelton
     */
    private QuantitativeValidationSingelton iQuantitationSingelton = QuantitativeValidationSingelton.getInstance();

    public DataSelectionPanel(WizardFrameHolder aParent) {
        this.iParent = aParent;
        $$$setupUI$$$();
        //add a listener to the open button
        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectFiles();
            }
        });
    }

    /**
     * This method let the user selects the rov files and updates the selected rov file list
     */
    private void selectFiles() {
        //open file chooser
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        //create the file filter to choose
        FileFilter lFilter = null;
        if (RoverSource.ITRAQ_DAT == iRoverSource) {
            lFilter = new DatFileFilter();
        } else if (RoverSource.TMT_DAT == iRoverSource) {
            lFilter = new DatFileFilter();
        } else if (RoverSource.ITRAQ_ROV == iRoverSource) {
            lFilter = new MgfRovFileFilter();
        } else if (RoverSource.DISTILLER_QUANT_TOOLBOX_ROV == iRoverSource) {
            lFilter = new RovFileFilter();
        } else if (RoverSource.MS_QUANT == iRoverSource) {
            lFilter = new TextFileFilter();
        } else if (RoverSource.MAX_QUANT == iRoverSource) {
            lFilter = new TextFileFilter();
        } else if (RoverSource.CENSUS == iRoverSource) {
            lFilter = new CensusFilter();
        } else if (RoverSource.THERMO_MSF_FILES == iRoverSource) {
            lFilter = new MsfFileFilter();
        }
        if (iQuantitationSingelton.getFileLocationOpener() != null) {
            fc.setCurrentDirectory(new File(iQuantitationSingelton.getFileLocationOpener()));
        }
        fc.setFileFilter(lFilter);
        fc.showOpenDialog(iParent);
        File[] lFiles = fc.getSelectedFiles();
        for (int i = 0; i < lFiles.length; i++) {
            if (i == 0) {
                iQuantitationSingelton.setFileLocationOpener(lFiles[i].getParent());
            }
            iFiles.add(lFiles[i]);
        }
        fileList.updateUI();
    }


    /**
     * This method attempts to load the protocol types from the DB.
     */
    private void loadProjects() {
        try {
            iProjects = Project.getAllProjects(iConn);
        } catch (SQLException e) {
            iParent.passHotPotato(e, "Unable to load projects from DB!");
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * This method attempts to load the protocol types from the DB.
     */
    private void loadProtocol() {
        try {
            iProtocols = Protocol.getAllProtocols(iConn);
        } catch (SQLException e) {
            iParent.passHotPotato(e, "Unable to load protocol types from DB!");
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * This method can set the connection
     *
     * @param connection Database connection to ms_lims
     * @param s          Database name
     */
    public void passConnection(Connection connection, String s) {
        iConn = connection;
        this.iParent.setMs_limsConnection(iConn);
    }

    /**
     * {@inheritDoc}
     */
    public JPanel getContentPane() {
        return this.jpanContent;
    }

    /**
     * {@inheritDoc}
     */
    public void backClicked() {
        //check if we are using ms_lims
        /*if (iParent.getUseMs_lims()) {
            iParent.setSelectedProject(null);
        } else {
            iParent.setFiles(null);
            iFiles.removeAllElements();
            fileList.updateUI();
        } */

        iFeasableToProceed = true;
        iNotFeasableReason = null;
        iQuantitationSingelton.removeLastRoverDataType();
    }

    /**
     * {@inheritDoc}
     */
    public void nextClicked() {
        //check if we are using ms_lims
        if (iParent.getUseMs_lims()) {
            //we are using ms_lims
            if (iProject == null) {
                iFeasableToProceed = false;
                iNotFeasableReason = "No project was selected";
                return;
            } else {
                iFeasableToProceed = true;
                iNotFeasableReason = null;
            }
            iParent.setSelectedProject(iProject);
        } else {
            //we are using files
            Vector<File> lSelectedFiles = new Vector<File>();
            //for the moment we may proceed
            iFeasableToProceed = true;
            iNotFeasableReason = null;
            //check the files if the RoverSource is ITRAQ_ROV because we must have for every .mgf file the corresponding .rov file
            if (RoverSource.ITRAQ_ROV == iRoverSource) {
                Vector<File> lMgfFiles = new Vector<File>();
                Vector<File> lRovFiles = new Vector<File>();
                for (int i = 0; i < iFiles.size(); i++) {
                    if (iFiles.get(i).getAbsolutePath().endsWith(".mgf")) {
                        lMgfFiles.add(iFiles.get(i));
                    } else if (iFiles.get(i).getAbsolutePath().endsWith(".rov")) {
                        lRovFiles.add(iFiles.get(i));
                    }
                }
                //check if you have a .rov file for every mgf file
                for (int i = 0; i < lMgfFiles.size(); i++) {
                    boolean lRovFound = false;
                    String lMgfFileName = lMgfFiles.get(i).getName().substring(0, lMgfFiles.get(i).getName().indexOf("."));
                    for (int j = 0; j < lRovFiles.size(); j++) {
                        String lRovFileName = lRovFiles.get(j).getName().substring(0, lRovFiles.get(j).getName().indexOf("."));
                        if (lRovFileName.indexOf(lMgfFileName) > -1) {
                            lRovFound = true;
                            //add the .rov file
                            lSelectedFiles.add(lRovFiles.get(j));
                            //add the .mgf file
                            lSelectedFiles.add(lMgfFiles.get(i));
                            j = lRovFiles.size();
                        }
                    }
                    if (!lRovFound) {
                        //no rov file was found for this mgf file
                        iFeasableToProceed = false;
                        if (iNotFeasableReason == null) {
                            iNotFeasableReason = "Could not find the corresponding .rov file for the mgf file '" + lMgfFileName + "'.";
                        } else {
                            iNotFeasableReason = iNotFeasableReason + "\nCould not find the corresponding .rov file for the mgf file '" + lMgfFileName + "'.";
                        }
                    }
                }
                if (!iFeasableToProceed) {
                    return;
                }

            } else {
                lSelectedFiles = iFiles;
            }
            //check if some files were selected
            if (lSelectedFiles.size() == 0) {
                //no files were selected
                iFeasableToProceed = false;
                if (RoverSource.ITRAQ_ROV == iRoverSource) {
                    iNotFeasableReason = "No correct (.mgf and .rov) files were selected.";
                } else if (RoverSource.MS_QUANT == iRoverSource) {
                    iNotFeasableReason = "No correct (.txt) files were selected.";
                } else if (RoverSource.MAX_QUANT == iRoverSource) {
                    iNotFeasableReason = "No correct (.txt) files were selected.";
                } else if (RoverSource.CENSUS == iRoverSource) {
                    iNotFeasableReason = "No correct .txt and .xml file was selected.";
                } else if (RoverSource.ITRAQ_DAT == iRoverSource || RoverSource.TMT_DAT == iRoverSource) {
                    iNotFeasableReason = "No correct .dat files were selected.";
                } else if (RoverSource.THERMO_MSF_FILES == iRoverSource) {
                    iNotFeasableReason = "No correct .msf files were selected.";
                } else {
                    iNotFeasableReason = "No correct (.rov) files were selected.";
                }
                return;
            }
            //if it's maxquant we must find a evidence and a msms file
            if (RoverSource.MAX_QUANT == iRoverSource) {
                boolean lEvidenceFound = false;
                boolean lMsMsFound = false;
                for (int i = 0; i < lSelectedFiles.size(); i++) {
                    if (lSelectedFiles.get(i).getName().startsWith("evidence")) {
                        lEvidenceFound = true;
                    }
                    if (lSelectedFiles.get(i).getName().equalsIgnoreCase("msms.txt")) {
                        lMsMsFound = true;
                    }
                }

                if (!lEvidenceFound && !lMsMsFound) {
                    iFeasableToProceed = false;
                    iNotFeasableReason = "The evidence.txt or the msms.txt file could not be found";
                    return;
                }
            }

            //if it's census we must find a .txt and a .xml file
            if (RoverSource.CENSUS == iRoverSource) {
                boolean lXmlFound = false;
                boolean lTxtFound = false;
                for (int i = 0; i < lSelectedFiles.size(); i++) {
                    if (lSelectedFiles.get(i).getName().endsWith(".xml")) {
                        lXmlFound = true;
                    }
                    if (lSelectedFiles.get(i).getName().endsWith(".txt")) {
                        lTxtFound = true;
                    }
                }

                if (!lTxtFound && !lXmlFound) {
                    iFeasableToProceed = false;
                    iNotFeasableReason = "No .xml AND .txt file could be found";
                    return;
                }
            }
            if (lSelectedFiles.size() != 2 && RoverSource.MAX_QUANT == iRoverSource || RoverSource.CENSUS == iRoverSource) {
                iFeasableToProceed = false;
                iNotFeasableReason = "You have to select two files!";
            }


            //set the files to the parent
            iParent.setFiles(lSelectedFiles);
            iFeasableToProceed = true;
            iNotFeasableReason = null;

        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean feasableToProceed() {
        return iFeasableToProceed;
    }

    /**
     * {@inheritDoc}
     */
    public String getNotFeasableReason() {
        return iNotFeasableReason;
    }

    /**
     * {@inheritDoc}
     */
    public void construct() {
        if (iRoverSource != null && iRoverSource != iParent.getRoverSource()) {
            iParent.setFiles(null);
            iFiles.removeAllElements();
            fileList.updateUI();
        }
        this.iRoverSource = iParent.getRoverSource();

        //check if we must get data from ms_lims
        if (iParent.getUseMs_lims()) {
            //we must use ms_lims
            //set the correct panels visible
            ms_limsPanel.setVisible(true);
            filePanel.setVisible(false);
            //check if we have a connection
            iConn = iParent.getMs_limsConnection();
            if (iConn == null) {
                Properties lConnectionProperties = PropertiesManager.getInstance().getProperties(CompomicsTools.MSLIMS, "ms-lims.properties");
                ConnectionDialog cd = new ConnectionDialog(iParent, this, "Establish DB connection for ms_lims", lConnectionProperties);
                cd.setVisible(true);
            }
            if (iConn == null) {
                iParent.getPreviousButton().doClick();
                iQuantitationSingelton.removeLastRoverDataType();
                return;
            } else {
                //check if we are working with a 7.1 ms_lims version or earlier
                try {
                    String query = "select * from quantitation_group where quantitation_groupid = 1";

                    PreparedStatement ps = iConn.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    rs.close();
                    ps.close();
                    iQuantitationSingelton.setMsLimsPre7_2(false);
                } catch (SQLException e) {
                    iQuantitationSingelton.setMsLimsPre7_2(true);
                }
            }
            this.loadProjects();
            this.loadProtocol();

            ms_limsPanel.remove(cmbProjects);
            cmbProjects = new JComboBox(iProjects);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 5;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 0);
            ms_limsPanel.add(cmbProjects, gbc);


            //add listener to the projects combobox
            cmbProjects.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        iProject = (Project) e.getItem();
                        lblCreationdate.setText(iSDF.format(iProject.getCreationdate()));
                        lblUser.setText(iProject.getUsername());
                        for (int i = 0; i < iProtocols.length; i++) {
                            if (iProtocols[i].getProtocolid() == iProject.getL_protocolid()) {
                                lblProtocol.setText(iProtocols[i].getType());
                            }
                        }
                        txtDescription.setText(iProject.getDescription());
                        lblTitle.setText(iProject.getTitle());
                    }
                }
            });
            //select the newest project
            iProject = iProjects[0];
            lblCreationdate.setText(iSDF.format(iProject.getCreationdate()));
            lblUser.setText(iProject.getUsername());
            for (int i = 0; i < iProtocols.length; i++) {
                if (iProtocols[i].getProtocolid() == iProject.getL_protocolid()) {
                    lblProtocol.setText(iProtocols[i].getType());
                }
            }
            txtDescription.setText(iProject.getDescription());
            lblTitle.setText(iProject.getTitle());

        } else {
            //we will not use ms_lims
            ms_limsPanel.setVisible(false);
            filePanel.setVisible(true);
        }


    }

    private void createUIComponents() {
        cmbProjects = new JComboBox(iProjects);

        fileList = new JList(iFiles);
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
        ms_limsPanel = new JPanel();
        ms_limsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        jpanContent.add(ms_limsPanel, gbc);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 0, 5, 0);
        ms_limsPanel.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder(null, "Project description", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, new Font("Tahoma", Font.BOLD, 12)));
        final JLabel label1 = new JLabel();
        label1.setFont(new Font("Tahoma", label1.getFont().getStyle(), label1.getFont().getSize()));
        label1.setText("User:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setFont(new Font("Tahoma", label2.getFont().getStyle(), label2.getFont().getSize()));
        label2.setText("Protocol:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setFont(new Font("Tahoma", label3.getFont().getStyle(), label3.getFont().getSize()));
        label3.setText("Creation date:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setFont(new Font("Tahoma", label4.getFont().getStyle(), label4.getFont().getSize()));
        label4.setText("Description:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(label4, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scrollPane1, gbc);
        txtDescription = new JTextArea();
        txtDescription.setFont(new Font("Tahoma", txtDescription.getFont().getStyle(), txtDescription.getFont().getSize()));
        scrollPane1.setViewportView(txtDescription);
        lblUser = new JLabel();
        lblUser.setText(" ");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(lblUser, gbc);
        lblProtocol = new JLabel();
        lblProtocol.setText(" ");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(lblProtocol, gbc);
        lblCreationdate = new JLabel();
        lblCreationdate.setText(" ");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(lblCreationdate, gbc);
        final JLabel label5 = new JLabel();
        label5.setFont(new Font("Tahoma", label5.getFont().getStyle(), label5.getFont().getSize()));
        label5.setText("Title:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(label5, gbc);
        lblTitle = new JLabel();
        lblTitle.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(lblTitle, gbc);
        final JLabel label6 = new JLabel();
        label6.setFont(new Font("Tahoma", label6.getFont().getStyle(), label6.getFont().getSize()));
        label6.setText("Select your project: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 5);
        ms_limsPanel.add(label6, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 0);
        ms_limsPanel.add(cmbProjects, gbc);
        filePanel = new JPanel();
        filePanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        jpanContent.add(filePanel, gbc);
        final JLabel label7 = new JLabel();
        label7.setFont(new Font("Tahoma", label7.getFont().getStyle(), label7.getFont().getSize()));
        label7.setText("Select your files: ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 5);
        filePanel.add(label7, gbc);
        openButton = new JButton();
        openButton.setText("Open");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 0);
        filePanel.add(openButton, gbc);
        final JLabel label8 = new JLabel();
        label8.setFont(new Font("Tahoma", label8.getFont().getStyle(), label8.getFont().getSize()));
        label8.setHorizontalAlignment(0);
        label8.setText("Selected files");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 0, 5, 0);
        filePanel.add(label8, gbc);
        final JScrollPane scrollPane2 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 0, 5, 0);
        filePanel.add(scrollPane2, gbc);
        fileList.setFont(new Font("Tahoma", fileList.getFont().getStyle(), fileList.getFont().getSize()));
        scrollPane2.setViewportView(fileList);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }


    //_____________________some file filters

    /**
     * A .rov file filter
     */
    class RovFileFilter extends FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".rov");
        }

        public String getDescription() {
            return ".rov files";
        }
    }

    /**
     * A .txt file filter
     */
    class TextFileFilter extends FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
        }

        public String getDescription() {
            return ".txt files";
        }
    }

    /**
     * A .txt and .xml file filter
     */
    class CensusFilter extends FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt") || f.getName().toLowerCase().endsWith(".xml");
        }

        public String getDescription() {
            return ".txt or .xml files";
        }
    }

    /**
     * A .dat file filter
     */
    class DatFileFilter extends FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".dat");
        }

        public String getDescription() {
            return ".dat files";
        }
    }

    /**
     * A .rov or .mgf file filter
     */
    class MgfRovFileFilter extends FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".mgf") || f.getName().toLowerCase().endsWith(".rov");
        }

        public String getDescription() {
            return ".mgf or .rov files";
        }
    }

    /**
     * A .msf file filter
     */
    class MsfFileFilter extends FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".msf");
        }

        public String getDescription() {
            return ".msf";
        }
    }
}
