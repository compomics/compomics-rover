package com.compomics.rover.gui.wizard;

import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatio;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatioGroup;
import org.apache.log4j.Logger;

import com.compomics.rover.general.interfaces.WizardPanel;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.quantitation.sorters.*;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.db.accessors.IdentificationExtension;
import com.compomics.rover.general.db.accessors.QuantitationExtension;
import com.compomics.rover.general.quantitation.*;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatioGroup;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatio;
import com.compomics.rover.general.quantitation.source.DatFileiTraq.ITraqRatio;
import com.compomics.rover.general.fileio.readers.QuantitationXmlReader;
import com.compomics.rover.general.fileio.readers.Mdf_iTraqReader;
import com.compomics.rover.general.fileio.readers.MsQuantReader;
import com.compomics.rover.general.fileio.readers.CensusReader;
import com.compomics.rover.general.fileio.files.DatFile;
import com.compomics.rover.general.fileio.files.RovFile;
import com.compomics.rover.general.fileio.files.MaxQuantEvidenceFile;
import com.compomics.rover.general.sequenceretriever.*;
import com.compomics.rover.gui.QuantitationValidationGUI;
import com.compomics.rover.gui.MatchRatioWithComponent;
import com.compomics.util.interfaces.Flamable;
import com.compomics.util.sun.SwingWorker;
import com.compomics.mslims.db.accessors.Identification_to_quantitation;
import com.compomics.mslims.db.accessors.Quantitation_file;
import be.proteomics.statlib.descriptive.BasicStats;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.io.*;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 23-mrt-2009
 * Time: 10:17:55
 */

/**
 * This class will create a panel that will show the status of the loaded data
 */
public class LoadingPanel implements WizardPanel {
    // Class specific log4j logger for LoadingPanel instances.
    private static Logger logger = Logger.getLogger(LoadingPanel.class);

    //gui stuff
    private JLabel lblCheckFiles;
    private JProgressBar progressBar;
    private JPanel jpanCheck;
    private JPanel jpanId;
    private JPanel jpanQuant;
    private JPanel jpanMatch;
    private JPanel jpanCreate;
    private JPanel jpanContent;
    private JLabel lblId;
    private JLabel lblQuant;
    private JLabel lblMatch;
    private JLabel lblCreate;
    private JPanel jpanDownload;
    private JLabel lblDownload;

    /**
     * The wizard frame holder parent
     */
    private WizardFrameHolder iParent;
    /**
     * Boolean that indicates if we can go to the next panel
     */
    private boolean iFeasableToProceed = true;
    /**
     * The reason why we cannot go to the next panel
     */
    private String iNotFeasableReason;
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
    private Flamable iFlamable;


    public LoadingPanel(WizardFrameHolder aParent) {
        this.iParent = aParent;
        this.iFlamable = iParent;
        $$$setupUI$$$();
        setIconOnPanel(jpanCheck, "empty.png", 3, 1);
        setIconOnPanel(jpanId, "empty.png", 3, 2);
        setIconOnPanel(jpanQuant, "empty.png", 3, 3);
        setIconOnPanel(jpanMatch, "empty.png", 3, 4);
        setIconOnPanel(jpanCreate, "empty.png", 3, 5);
        setIconOnPanel(jpanDownload, "empty.png", 3, 6);
    }

    /**
     * {@inheritDoc}
     */
    public JPanel getContentPane() {
        return jpanContent;
    }

    /**
     * {@inheritDoc}
     */
    public void backClicked() {
        //back cannot be clicked since this is the last panel
    }

    /**
     * {@inheritDoc}
     */
    public void nextClicked() {
        if (iParent.getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
            startFileRov();
        } else if (iParent.getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS) {
            startMs_limsRov();
        } else if (iParent.getRoverSource() == RoverSource.ITRAQ_DAT) {
            startFileDat();
        } else if (iParent.getRoverSource() == RoverSource.TMT_DAT) {
            startFileDat();
        } else if (iParent.getRoverSource() == RoverSource.ITRAQ_MS_LIMS) {
            startMs_limsiTraq();
        } else if (iParent.getRoverSource() == RoverSource.ITRAQ_ROV) {
            startITraqRov();
        } else if (iParent.getRoverSource() == RoverSource.MS_QUANT) {
            startMsQuant();
        } else if (iParent.getRoverSource() == RoverSource.MAX_QUANT || iParent.getRoverSource() == RoverSource.MAX_QUANT_NO_SIGN) {
            startMaxQuant();
        } else if (iParent.getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS) {
            startMs_limsMaxQuant();
        } else if (iParent.getRoverSource() == RoverSource.CENSUS) {
            startCensus();
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
        if (iParent.getUseMs_lims()) {
            //we are not using ms_lims, not files
            //do not show the file check lbl and panel
            lblCheckFiles.setVisible(false);
            jpanCheck.setVisible(false);
        }
    }


    /**
     * This method set an icon on a panel
     *
     * @param aPanel     The JPanel
     * @param aIconName  String with the filename of the icon
     * @param aXlocation The x location in the GridBagConstraints
     * @param aYlocation The y location in the GridBagConstraints
     */
    public void setIconOnPanel(JPanel aPanel, String aIconName, int aXlocation, int aYlocation) {
        //remove old icon
        jpanContent.remove(aPanel);
        aPanel.removeAll();
        aPanel.updateUI();
        aPanel.validate();
        aPanel.repaint();

        //add new
        aPanel.add(new ImagePanel(new ImageIcon(getClass().getResource("/" + aIconName)).getImage()));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = aXlocation;
        gbc.gridy = aYlocation;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(aPanel, gbc);
        jpanContent.validate();
        jpanContent.updateUI();
        jpanContent.repaint();
    }


    /**
     * This method start the data acquisition process for quantitative iTraq data in a ms_lims project
     */
    public void startMs_limsiTraq() {
        //create a new swing worker
        final Flamable lFlamable = iParent;
        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                try {

                    QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
                    iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

                    //update progress bar
                    progressBar.setIndeterminate(true);
                    //set id loading
                    lblId.setEnabled(true);
                    setIconOnPanel(jpanId, "clock.png", 3, 2);

                    //1. get IdentificationExtension for a specific project
                    IdentificationExtension[] lQuantPeptides;
                    lQuantPeptides = IdentificationExtension.getIdentificationExtensionsforProject(iParent.getMs_limsConnection(), iParent.getSelectedProject().getProjectid(), null);
                    if (lQuantPeptides.length == 0) {
                        lFlamable.passHotPotato(new Throwable("No identifications found for this project!"));
                        return false;
                    }
                    //create a string with the identificationsids seperated by ","
                    String lIdentificationsIds = "";
                    for (int i = 0; i < lQuantPeptides.length; i++) {
                        lIdentificationsIds = lIdentificationsIds + lQuantPeptides[i].getIdentificationid() + " , ";
                    }
                    lIdentificationsIds = lIdentificationsIds.substring(0, lIdentificationsIds.lastIndexOf(","));

                    //2. get all the quantitation linkers (identification_to_quantitation)
                    //set id done
                    setIconOnPanel(jpanId, "apply.png", 3, 2);
                    //set quant loading
                    lblQuant.setEnabled(true);
                    setIconOnPanel(jpanQuant, "clock.png", 3, 3);

                    Identification_to_quantitation[] lQuantLinkers = Identification_to_quantitation.getIdentification_to_quantitationForIdentificationIds(iParent.getMs_limsConnection(), lIdentificationsIds);
                    //now add them to the correct quantitative peptide
                    for (int i = 0; i < lQuantLinkers.length; i++) {
                        for (int j = 0; j < lQuantPeptides.length; j++) {
                            if (lQuantPeptides[j].getIdentificationid() == lQuantLinkers[i].getL_identificationid()) {
                                lQuantPeptides[j].addIdentification_to_quantitation(lQuantLinkers[i]);
                            }
                        }
                    }
                    if (lQuantLinkers.length == 0) {
                        lFlamable.passHotPotato(new Throwable("No quantitations found for this project!"));
                        return false;
                    }


                    //3.get for all the QuantPeptides the original dat file name
                    ArrayList<Long> lDatFileIds = new ArrayList<Long>();
                    String query = "select i.identificationid, d.filename, d.datfileid from identification as i, datfile as d where i.identificationid in (" + lIdentificationsIds + ") and d.datfileid = i.l_datfileid";
                    PreparedStatement ps = iParent.getMs_limsConnection().prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        long lId = rs.getLong(1);
                        String lDatFileName = rs.getString(2);
                        long lDatFileId = rs.getLong(3);
                        //get a list of all the rov files
                        boolean lDatRovFile = true;
                        for (int r = 0; r < lDatFileIds.size(); r++) {
                            if (lDatFileId == lDatFileIds.get(r)) {
                                lDatRovFile = false;
                            }
                        }
                        if (lDatRovFile) {
                            lDatFileIds.add(lDatFileId);
                        }
                        //attach the dat file name to the quant peptides
                        for (int j = 0; j < lQuantPeptides.length; j++) {
                            if (lQuantPeptides[j].getIdentificationid() == lId) {
                                lQuantPeptides[j].setQuantitationFileName(lDatFileName);
                            }
                        }
                    }
                    rs.close();
                    ps.close();

                    //update progress bar
                    progressBar.setIndeterminate(false);
                    progressBar.setMaximum(lDatFileIds.size() + 1);
                    progressBar.setValue(0);
                    progressBar.setStringPainted(true);

                    //4.store all the rov files used for this project in the temp folder
                    //4.1 create the temp/ms_lims folder
                    File lTempfolder = File.createTempFile("temp", "temp").getParentFile();
                    File lTempRoverFolder = new File(lTempfolder, "rover");
                    lTempRoverFolder.deleteOnExit();
                    if (lTempRoverFolder.exists() == false) {
                        lTempRoverFolder.mkdir();
                    }
                    //4.2 get all the dat files
                    RatioGroupCollection[] lRatioGroupCollection = new RatioGroupCollection[lDatFileIds.size()];
                    for (int i = 0; i < lDatFileIds.size(); i++) {

                        //update progress bar
                        progressBar.setValue(progressBar.getValue() + 1);
                        progressBar.setString("Storing .dat file number " + (i + 1) + " of " + lDatFileIds.size() + " mascot result files");

                        PreparedStatement prepDat = null;
                        prepDat = iParent.getMs_limsConnection().prepareStatement("select * from datfile where datfileid = ?");
                        Long id = lDatFileIds.get(i);
                        prepDat.setLong(1, id);
                        ResultSet rsDat = prepDat.executeQuery();
                        ResultSetMetaData rsmd = rsDat.getMetaData();
                        int columns = rsmd.getColumnCount();
                        int columnName = 0;
                        int columnFile = 0;
                        for (int j = 1; j <= columns; j++) {
                            if (rsmd.getColumnName(j).equalsIgnoreCase("filename")) {
                                columnName = j;
                            }
                            if (rsmd.getColumnName(j).equalsIgnoreCase("file")) {
                                columnFile = j;
                            }
                        }

                        while (rsDat.next()) {
                            byte[] zipped = rsDat.getBytes(columnFile);
                            ByteArrayInputStream bais = new ByteArrayInputStream(zipped);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            BufferedInputStream bis = new BufferedInputStream(new GZIPInputStream(bais));
                            BufferedOutputStream bos = new BufferedOutputStream(baos);
                            int read = -1;
                            while ((read = bis.read()) != -1) {
                                bos.write(read);
                            }
                            bos.flush();
                            baos.flush();
                            byte[] result = baos.toByteArray();
                            bos.close();
                            bis.close();
                            bais.close();
                            baos.close();

                            //store the file
                            PrintWriter out = new PrintWriter(new FileWriter(new File(lTempRoverFolder.getPath(), rsDat.getString(columnName))));
                            out.write(new String(result));
                            out.close();
                            out.flush();
                            //create an xml reader
                            lRatioGroupCollection[i] = new Mdf_iTraqReader(new File(lTempRoverFolder.getPath(), rsDat.getString(columnName)), null, iParent, iParent.getMs_limsConnection(), rsDat.getLong("datfileid")).getRatioGroupCollection();
                            lRatioGroupCollection[i].setRoverSource(iParent.getRoverSource());
                        }
                        prepDat.close();
                        rsDat.close();


                        //_____Do garbage collection______
                        System.gc();

                        //ToDo delete me

                        //i = i + 40;
                        //progressBar.setValue(progressBar.getValue() + 40);

                        //ToDo delete me


                    }

                    //_____Do garbage collection______
                    System.gc();
                    //update progress bar
                    progressBar.setString("");
                    progressBar.setIndeterminate(true);

                    //Show in the gui that we found all the rov files
                    setIconOnPanel(jpanQuant, "apply.png", 3, 3);
                    //set match  loading
                    lblMatch.setEnabled(true);
                    setIconOnPanel(jpanMatch, "clock.png", 3, 4);

                    //5. couple the quantitative peptides to the ratio groups
                    //get all the quantitations from the db
                    //these will be used to link to the Ratios

                    //update progress bar
                    progressBar.setString("Getting quantitations from the db!");
                    progressBar.setStringPainted(true);

                    Vector<QuantitationExtension> lQuantitations = QuantitationExtension.getQuantitationForIdentifications(lIdentificationsIds, iParent.getMs_limsConnection());

                    //update progress bar
                    progressBar.setString("");
                    progressBar.setIndeterminate(false);
                    progressBar.setMaximum(lDatFileIds.size() + 1);
                    progressBar.setValue(0);
                    progressBar.setStringPainted(true);

                    for (int i = 0; i < lRatioGroupCollection.length; i++) {
                        if (i == 0) {
                            iQuantitativeValidationSingelton.setRatioTypes(lRatioGroupCollection[i].getRatioTypes());
                            iQuantitativeValidationSingelton.setComponentTypes(lRatioGroupCollection[i].getComponentTypes());
                        }
                        //update progress bar
                        progressBar.setValue(progressBar.getValue() + 1);
                        progressBar.setString("Matching identifications and ratios to .dat file number " + (i + 1) + " of " + lDatFileIds.size() + " files");

                        //only give the IdentificationExtension for a specific rovFile
                        Vector<IdentificationExtension> lQuantPeptideForRovFileVector = new Vector<IdentificationExtension>();
                        for (int j = 0; j < lQuantPeptides.length; j++) {
                            String lPeptideRovFileName = lQuantPeptides[j].getQuantitationFileName();
                            String lCollectionRovFileName = ((String) lRatioGroupCollection[i].getMetaData(QuantitationMetaType.FILENAME)).substring(((String) lRatioGroupCollection[i].getMetaData(QuantitationMetaType.FILENAME)).lastIndexOf("\\") + 1);
                            if (lPeptideRovFileName != null) {
                                if (lPeptideRovFileName.equalsIgnoreCase(lCollectionRovFileName)) {
                                    lQuantPeptideForRovFileVector.add(lQuantPeptides[j]);
                                }
                            }
                        }

                        IdentificationExtension[] lQuantPeptideForRovFile = new IdentificationExtension[lQuantPeptideForRovFileVector.size()];
                        lQuantPeptideForRovFileVector.toArray(lQuantPeptideForRovFile);


                        //now match the identifications found for this rov file to the ratio groups
                        for (int j = 0; j < lRatioGroupCollection[i].size(); j++) {
                            RatioGroup lRatioGroup = lRatioGroupCollection[i].get(j);

                            //match the quantitation with the ratios
                            for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                ITraqRatio lRatio = (ITraqRatio) lRatioGroup.getRatio(k);
                                BigDecimal lRatioValue = new BigDecimal(lRatio.getRatio(false));
                                lRatioValue = lRatioValue.setScale(5, BigDecimal.ROUND_HALF_DOWN);


                                for (int l = 0; l < lQuantitations.size(); l++) {
                                    QuantitationExtension lQuant = lQuantitations.get(l);

                                    //check if the filename, the hitnumber, the ratio and the ratio type is the same
                                    String lQuantFileName = lQuant.getQuantitationFileName().substring(lQuant.getQuantitationFileName().lastIndexOf("\\") + 1);

                                    if (lRatioValue.doubleValue() == lQuant.getRatio() && lRatio.getType().equalsIgnoreCase(lQuant.getType()) && lQuant.getFile_ref().equalsIgnoreCase(String.valueOf(lRatioGroup.getIdentification(0).getDatfile_query())) && lQuantFileName.equalsIgnoreCase((String) lRatioGroupCollection[i].getMetaData(QuantitationMetaType.FILENAME))) {
                                        lRatio.setQuantitationStoredInDb(lQuant);
                                        //set the valid status from the database and not from the original rov file
                                        lRatio.setValid(lQuant.getValid());
                                    }
                                }
                            }
                        }

                        //_____Do garbage collection______
                        System.gc();

                        //ToDo delete me

                        //i = i + 40;
                        //progressBar.setValue(progressBar.getValue() + 40);
                        //ToDo delete me

                    }

                    //update progress bar
                    progressBar.setIndeterminate(true);
                    progressBar.setStringPainted(false);
                    progressBar.setString("");

                    //_____Do garbage collection______
                    System.gc();

                    //Show in the gui that the matching is done
                    setIconOnPanel(jpanMatch, "apply.png", 3, 4);
                    //set create loading
                    lblCreate.setEnabled(true);
                    setIconOnPanel(jpanCreate, "clock.png", 3, 5);

                    //6. get all the protein accessions from the identifications
                    Vector<String> lProteinAccessions = new Vector<String>();
                    for (int i = 0; i < lRatioGroupCollection.length; i++) {
                        for (int j = 0; j < lRatioGroupCollection[i].size(); j++) {
                            RatioGroup lRatioGroup = lRatioGroupCollection[i].get(j);

                            String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                            for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                                //check if it's a new accession
                                boolean lNewAccession = true;
                                for (int l = 0; l < lProteinAccessions.size(); l++) {
                                    if (lProteinAccessions.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                                        lNewAccession = false;
                                    }
                                }
                                if (lNewAccession) {
                                    lProteinAccessions.add(lAccessionsForRatioGroup[k]);
                                }

                            }
                        }

                        //ToDo delete me

                        //i = i + 40;

                        //ToDo delete me
                    }

                    if (lRatioGroupCollection.length == 0) {
                        //show gui
                        JOptionPane.showMessageDialog(iParent, "No quantitative data could be found!\n The program will close.", "INFO", JOptionPane.INFORMATION_MESSAGE);
                        iParent.close();
                    }

                    //7.A get the types of the ratios from the first distiller ratio collecion
                    Vector<String> lRatioList = lRatioGroupCollection[0].getRatioTypes();
                    String[] lRatioTypes = new String[lRatioList.size()];
                    lRatioList.toArray(lRatioTypes);

                    //7.B get the types of the ratios from the first distiller ratio collecion
                    Vector<String> lComponentList = lRatioGroupCollection[0].getComponentTypes();
                    String[] lComponentTypes = new String[lComponentList.size()];
                    lComponentList.toArray(lComponentTypes);


                    //8. create all the distiller proteins
                    Vector<QuantitativeProtein> lQuantProtein = new Vector<QuantitativeProtein>();
                    for (int i = 0; i < lProteinAccessions.size(); i++) {
                        lQuantProtein.add(new QuantitativeProtein(lProteinAccessions.get(i), lRatioTypes));
                    }

                    //9. couple the distiller ratio groups to the distiller proteins
                    for (int i = 0; i < lRatioGroupCollection.length; i++) {

                        for (int j = 0; j < lRatioGroupCollection[i].size(); j++) {
                            //get the ratio group
                            RatioGroup lRatioGroup = lRatioGroupCollection[i].get(j);
                            //get all the protein accession linked to this ratiogroup
                            String[] lAccessions = lRatioGroup.getProteinAccessions();
                            for (int k = 0; k < lAccessions.length; k++) {
                                for (int l = 0; l < lQuantProtein.size(); l++) {
                                    if (lAccessions[k].equalsIgnoreCase(lQuantProtein.get(l).getAccession())) {
                                        //add the ratio group to the protein if the accession is the same
                                        lQuantProtein.get(l).addRatioGroup(lRatioGroup);
                                    }
                                }
                            }
                        }

                        //ToDo delete me

                        //i = i + 40;

                        //ToDo delete me
                    }

                    calculateRazorPeptides(lQuantProtein);
                    iQuantitativeValidationSingelton.setAllProteins(lQuantProtein);

                    //10. create a reference set with the "household" proteins with the most ratiogroups
                    ReferenceSet lReferenceSet = new ReferenceSet(new ArrayList<QuantitativeProtein>(), lRatioTypes, lComponentTypes);

                    MatchRatioWithComponent lMatch = new MatchRatioWithComponent(true);
                    while (lRatioTypes.length > iQuantitativeValidationSingelton.getMatchedRatioTypes().size()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            //sleep failed
                        }
                    }

                    //sort by the ratio group numbers
                    Collections.sort(lQuantProtein, new QuantitativeProteinSorterByRatioGroupNumbers());
                    //get the reference set size from the singelton
                    int lReferenceSetSize = iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet();
                    if (iQuantitativeValidationSingelton.getUseAllProteinsForReferenceSet()) {
                        lReferenceSetSize = lQuantProtein.size();
                    }
                    if (lReferenceSetSize > lQuantProtein.size()) {
                        lReferenceSetSize = lQuantProtein.size();
                    }
                    for (int i = 0; i < lReferenceSetSize; i++) {
                        lReferenceSet.addReferenceProtein(lQuantProtein.get(i));
                    }
                    //set the refernce set
                    iQuantitativeValidationSingelton.setReferenceSet(lReferenceSet);
                    //lReferenceSet.calculateStatisticsByRandomSampling();


                    //Show in the gui that the creation of the proteins is done
                    setIconOnPanel(jpanCreate, "apply.png", 3, 5);

                    //download the protein sequence
                    lblDownload.setEnabled(true);
                    setIconOnPanel(jpanDownload, "clock.png", 3, 6);
                    downloadProteinSequences(lQuantProtein);
                    setIconOnPanel(jpanDownload, "apply.png", 3, 6);
                    //update progress bar
                    progressBar.setIndeterminate(false);

                    //sort by the protein accession
                    Collections.sort(lQuantProtein, new QuantitativeProteinSorterByAccession());

                    //_____Do garbage collection______
                    System.gc();

                    //show gui
                    JOptionPane.showMessageDialog(iParent, "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                    QuantitationValidationGUI gui = new QuantitationValidationGUI(lQuantProtein, iParent.getMs_limsConnection(), iParent.isStandAlone());
                    gui.setVisible(true);


                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                // step 7.2 dispose the iText components.
                return true;
            }

            public void finished() {
                //
                iParent.closeFrame();
            }

        };
        lStarter.start();
    }

    /**
     * This method start the data acquisition process for a mascot distiller quantitation toolbox ms_lims project
     */
    public void startMs_limsRov() {
        //create a new swing worker
        final Flamable lFlamable = iParent;
        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                try {

                    QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
                    iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

                    //update progress bar
                    progressBar.setIndeterminate(true);
                    //set id loading
                    lblId.setEnabled(true);
                    setIconOnPanel(jpanId, "clock.png", 3, 2);

                    //1. get IdentificationExtension for a specific project
                    IdentificationExtension[] lQuantPeptides;
                    lQuantPeptides = IdentificationExtension.getIdentificationExtensionsforProject(iParent.getMs_limsConnection(), iParent.getSelectedProject().getProjectid(), null);
                    if (lQuantPeptides.length == 0) {
                        lFlamable.passHotPotato(new Throwable("No identifications found for this project!"));
                        return false;
                    }
                    //create a string with the identificationsids seperated by ","
                    String lIdentificationsIds = "";
                    for (int i = 0; i < lQuantPeptides.length; i++) {
                        lIdentificationsIds = lIdentificationsIds + lQuantPeptides[i].getIdentificationid() + " , ";
                    }
                    lIdentificationsIds = lIdentificationsIds.substring(0, lIdentificationsIds.lastIndexOf(","));

                    //2. get all the quantitation linkers (identification_to_quantitation)
                    Identification_to_quantitation[] lQuantLinkers = Identification_to_quantitation.getIdentification_to_quantitationForIdentificationIds(iParent.getMs_limsConnection(), lIdentificationsIds);
                    //now add them to the correct quantitative peptide
                    for (int i = 0; i < lQuantLinkers.length; i++) {
                        for (int j = 0; j < lQuantPeptides.length; j++) {
                            if (lQuantPeptides[j].getIdentificationid() == lQuantLinkers[i].getL_identificationid()) {
                                lQuantPeptides[j].addIdentification_to_quantitation(lQuantLinkers[i]);
                            }
                        }
                    }
                    if (lQuantLinkers.length == 0) {
                        lFlamable.passHotPotato(new Throwable("No quantitations found for this project!"));
                        return false;
                    }
                    //Show in the gui that we found all the identifications
                    setIconOnPanel(jpanId, "apply.png", 3, 2);
                    //set quant  loading
                    lblQuant.setEnabled(true);
                    setIconOnPanel(jpanQuant, "clock.png", 3, 3);

                    //3.get for all the QuantPeptides the original rov file name
                    ArrayList<Long> lRovFileIds = new ArrayList<Long>();
                    String sql;
                    if (iQuantitativeValidationSingelton.getMsLimsPre7_2()) {
                        sql = "select i.identificationid, f.filename, f.quantitation_fileid from identification as i, identification_to_quantitation as t, quantitation_file as f, quantitation as q where i.identificationid in (" + lIdentificationsIds + ") and i.identificationid = t.l_identificationid and t.quantitation_link = q.quantitation_link and q.l_quantitation_fileid  = f.quantitation_fileid";
                    } else {
                        sql = "select i.identificationid, f.filename, f.quantitation_fileid from identification as i, identification_to_quantitation as t, quantitation_file as f, quantitation_group as g where i.identificationid in (" + lIdentificationsIds + ") and i.identificationid = t.l_identificationid and t.l_quantitation_groupid = g.quantitation_groupid and g.L_quantitation_fileid  = f.quantitation_fileid";
                    }
                    PreparedStatement ps = iParent.getMs_limsConnection().prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        long lId = rs.getLong(1);
                        String lRovFileName = rs.getString(2);
                        long lRovFileId = rs.getLong(3);
                        //get a list of all the rov files
                        boolean lNewRovFile = true;
                        for (int r = 0; r < lRovFileIds.size(); r++) {
                            if (lRovFileId == lRovFileIds.get(r)) {
                                lNewRovFile = false;
                            }
                        }
                        if (lNewRovFile) {
                            lRovFileIds.add(lRovFileId);
                        }
                        //attach the rov file name to the quant peptides
                        for (int j = 0; j < lQuantPeptides.length; j++) {
                            if (lQuantPeptides[j].getIdentificationid() == lId) {
                                lQuantPeptides[j].setQuantitationFileName(lRovFileName);
                            }
                        }
                    }
                    rs.close();
                    ps.close();

                    //update progress bar
                    progressBar.setIndeterminate(false);
                    progressBar.setMaximum(lRovFileIds.size() + 1);
                    progressBar.setValue(0);
                    progressBar.setStringPainted(true);

                    //4.store all the rov files used for this project in the temp folder
                    //4.1 create the temp/ms_lims folder
                    File lTempfolder = File.createTempFile("temp", "temp").getParentFile();
                    File lTempRovFolder = new File(lTempfolder, "rover");
                    lTempRovFolder.deleteOnExit();
                    if (lTempRovFolder.exists() == false) {
                        lTempRovFolder.mkdir();
                    }
                    //4.2 get all the rov files
                    RatioGroupCollection[] lRatioGroupCollection = new RatioGroupCollection[lRovFileIds.size()];
                    for (int i = 0; i < lRovFileIds.size(); i++) {

                        //update progress bar
                        progressBar.setValue(progressBar.getValue() + 1);
                        progressBar.setString("Reading quantitative information from file " + (i + 1) + " of " + lRovFileIds.size() + " distiller quantitation xml files");

                        Quantitation_file lQuantFile = Quantitation_file.getQuantitation_fileForId(iParent.getMs_limsConnection(), lRovFileIds.get(i));
                        //store the file
                        byte[] lXmlBytes = lQuantFile.getUnzippedFile();
                        PrintWriter out = new PrintWriter(new FileWriter(lTempRovFolder.getPath() + "/" + lQuantFile.getFilename()));
                        out.write(new String(lXmlBytes));
                        out.flush();
                        out.close();
                        //create an xml reader
                        lRatioGroupCollection[i] = (new QuantitationXmlReader(new File(lTempRovFolder.getPath() + "/" + lQuantFile.getFilename()), lFlamable, lQuantFile.getFilename())).getRatioGroupCollection();
                        lRatioGroupCollection[i].setRoverSource(iParent.getRoverSource());

                        //_____Do garbage collection______
                        System.gc();

                        //ToDo delete me

                        //i = i + 40;
                        //progressBar.setValue(progressBar.getValue() + 40);

                        //ToDo delete me


                    }

                    //_____Do garbage collection______
                    System.gc();

                    //update progress bar
                    progressBar.setString("");
                    progressBar.setIndeterminate(true);

                    //Show in the gui that we found all the rov files
                    setIconOnPanel(jpanQuant, "apply.png", 3, 3);

                    //set match  loading
                    lblMatch.setEnabled(true);
                    setIconOnPanel(jpanMatch, "clock.png", 3, 4);

                    //5. couple the quantitative peptides to the distiller ratio groups
                    //get all the quantitations from the db
                    //these will be used to link to the DistillerRatios

                    //update progress bar
                    progressBar.setString("Getting quantitations from the db!");
                    progressBar.setStringPainted(true);

                    Vector<QuantitationExtension> lQuantitations = QuantitationExtension.getQuantitationForIdentifications(lIdentificationsIds, iParent.getMs_limsConnection());

                    //update progress bar
                    progressBar.setString("");
                    progressBar.setIndeterminate(false);
                    progressBar.setMaximum(lRovFileIds.size() + 1);
                    progressBar.setValue(0);
                    progressBar.setStringPainted(true);

                    for (int i = 0; i < lRatioGroupCollection.length; i++) {
                        if (i == 0) {
                            iQuantitativeValidationSingelton.setRatioTypes(lRatioGroupCollection[i].getRatioTypes());
                            iQuantitativeValidationSingelton.setComponentTypes(lRatioGroupCollection[i].getComponentTypes());
                        }
                        //update progress bar
                        progressBar.setValue(progressBar.getValue() + 1);
                        progressBar.setString("Matching identifications and ratios to .rov file number " + (i + 1) + " of " + lRovFileIds.size() + " distiller quantitation xml files");

                        //only give the IdentificationExtension for a specific rovFile
                        Vector<IdentificationExtension> lQuantPeptideForRovFileVector = new Vector<IdentificationExtension>();
                        for (int j = 0; j < lQuantPeptides.length; j++) {
                            String lPeptideRovFileName = lQuantPeptides[j].getQuantitationFileName();
                            String lCollectionRovFileName = ((String) lRatioGroupCollection[i].getMetaData(QuantitationMetaType.FILENAME)).substring(((String) lRatioGroupCollection[i].getMetaData(QuantitationMetaType.FILENAME)).lastIndexOf("\\") + 1);
                            if (lPeptideRovFileName != null) {
                                if (lPeptideRovFileName.equalsIgnoreCase(lCollectionRovFileName)) {
                                    lQuantPeptideForRovFileVector.add(lQuantPeptides[j]);
                                }
                            }
                        }

                        IdentificationExtension[] lQuantPeptideForRovFile = new IdentificationExtension[lQuantPeptideForRovFileVector.size()];
                        lQuantPeptideForRovFileVector.toArray(lQuantPeptideForRovFile);

                        //now match the identifications found for this rov file to the ratio groups
                        //we will clone it, because if we don't find quantitation from the db to link to the ratio group we will delete this ratiogroup
                        RatioGroupCollection lClonedCollection = (RatioGroupCollection) lRatioGroupCollection[i].clone();
                        for (int j = 0; j < lClonedCollection.size(); j++) {
                            DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) lClonedCollection.get(j);
                            lRatioGroup.linkIdentificationsAndQueries(lQuantPeptideForRovFile);

                            //match the quantitation with the ratios
                            boolean lRatioGroupLinkFound = false;
                            for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                                DistillerRatio lRatio = (DistillerRatio) lRatioGroup.getRatio(k);
                                BigDecimal lRatioValue = new BigDecimal(lRatio.getRatio(false));
                                lRatioValue = lRatioValue.setScale(5, BigDecimal.ROUND_HALF_DOWN);

                                boolean lLinkFound = false;

                                for (int l = 0; l < lQuantitations.size(); l++) {
                                    QuantitationExtension lQuant = lQuantitations.get(l);


                                    //check if the filename, the hitnumber, the ratio and the ratio type is the same
                                    String lQuantFileName = lQuant.getQuantitationFileName().substring(lQuant.getQuantitationFileName().lastIndexOf("\\") + 1);

                                    if (lRatioValue.doubleValue() == lQuant.getRatio() && lRatio.getType().equalsIgnoreCase(lQuant.getType()) && lQuant.getFile_ref().equalsIgnoreCase(String.valueOf(lRatioGroup.getReferenceOfParentHit())) && lQuantFileName.equalsIgnoreCase((String) lClonedCollection.getMetaData(QuantitationMetaType.FILENAME))) {
                                        lRatio.setQuantitationStoredInDb(lQuant);
                                        //set the valid status from the database and not from the original rov file
                                        lRatio.setValid(lQuant.getValid());
                                        lQuantitations.remove(lQuant);
                                        l = lQuantitations.size();
                                        lLinkFound = true;
                                        lRatioGroupLinkFound = true;
                                    }
                                }
                                if (!lLinkFound) {
                                    //no quantitation extension could be linked to a ratio, therefor we will delete the ratio
                                    lRatioGroup.deleteRatio(lRatio);
                                }

                            }

                            if (!lRatioGroupLinkFound) {
                                //no quantitation extension could be linked to one of the ratios in the ratio group, therefor we will delete the ratiogroup
                                lRatioGroupCollection[i].remove(lRatioGroup);
                            }
                        }

                        //_____Do garbage collection______
                        System.gc();

                        //ToDo delete me

                        //i = i + 40;
                        //progressBar.setValue(progressBar.getValue() + 40);
                        //ToDo delete me

                    }

                    /*if (lQuantitations.size() > 0) {
                        System.out.println(lQuantitations.size());
                    } */

                    //update progress bar
                    progressBar.setIndeterminate(true);
                    progressBar.setStringPainted(false);
                    progressBar.setString("");

                    //_____Do garbage collection______
                    System.gc();

                    //Show in the gui that the matching is done
                    setIconOnPanel(jpanMatch, "apply.png", 3, 4);
                    //set create loading
                    lblCreate.setEnabled(true);
                    setIconOnPanel(jpanCreate, "clock.png", 3, 5);

                    //6. get all the protein accessions from the identifications
                    Vector<String> lProteinAccessions = new Vector<String>();
                    for (int i = 0; i < lRatioGroupCollection.length; i++) {
                        for (int j = 0; j < lRatioGroupCollection[i].size(); j++) {
                            DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) lRatioGroupCollection[i].get(j);

                            if (lRatioGroup.getNumberOfIdentifications() != 0) {
                                String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                                for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                                    //check if it's a new accession
                                    boolean lNewAccession = true;
                                    for (int l = 0; l < lProteinAccessions.size(); l++) {
                                        if (lProteinAccessions.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                                            lNewAccession = false;
                                        }
                                    }
                                    if (lNewAccession) {
                                        lProteinAccessions.add(lAccessionsForRatioGroup[k]);
                                    }

                                }
                            } else {
                                //System.out.println("No identification found");
                            }
                        }

                        //ToDo delete me

                        //i = i + 40;

                        //ToDo delete me
                    }

                    if (lRatioGroupCollection.length == 0) {
                        //show gui
                        JOptionPane.showMessageDialog(iParent, "No quantitative data could be found!\n The program will close.", "INFO", JOptionPane.INFORMATION_MESSAGE);
                        iParent.close();
                    }

                    //7.A get the types of the ratios from the first distiller ratio collecion
                    Vector<String> lRatioList = lRatioGroupCollection[0].getRatioTypes();
                    String[] lRatioTypes = new String[lRatioList.size()];
                    lRatioList.toArray(lRatioTypes);

                    //7.B get the types of the ratios from the first distiller ratio collecion
                    Vector<String> lComponentList = lRatioGroupCollection[0].getComponentTypes();
                    String[] lComponentTypes = new String[lComponentList.size()];
                    lComponentList.toArray(lComponentTypes);


                    //8. create all the distiller proteins
                    Vector<QuantitativeProtein> lDistillerProtein = new Vector<QuantitativeProtein>();
                    for (int i = 0; i < lProteinAccessions.size(); i++) {
                        lDistillerProtein.add(new QuantitativeProtein(lProteinAccessions.get(i), lRatioTypes));
                    }

                    //9. couple the distiller ratio groups to the distiller proteins
                    for (int i = 0; i < lRatioGroupCollection.length; i++) {

                        for (int j = 0; j < lRatioGroupCollection[i].size(); j++) {
                            //get the ratio group
                            DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) lRatioGroupCollection[i].get(j);
                            //get all the protein accession linked to this ratiogroup
                            String[] lAccessions = lRatioGroup.getProteinAccessions();
                            for (int k = 0; k < lAccessions.length; k++) {
                                for (int l = 0; l < lDistillerProtein.size(); l++) {
                                    if (lAccessions[k].equalsIgnoreCase(lDistillerProtein.get(l).getAccession())) {
                                        //add the ratio group to the protein if the accession is the same
                                        lDistillerProtein.get(l).addRatioGroup(lRatioGroup);
                                    }
                                }
                            }
                        }

                        //ToDo delete me

                        //i = i + 40;

                        //ToDo delete me
                    }
                    calculateRazorPeptides(lDistillerProtein);
                    iQuantitativeValidationSingelton.setAllProteins(lDistillerProtein);

                    //10. create a reference set with the "household" proteins with the most ratiogroups
                    ReferenceSet lReferenceSet = new ReferenceSet(new ArrayList<QuantitativeProtein>(), lRatioTypes, lComponentTypes);

                    MatchRatioWithComponent lMatch = new MatchRatioWithComponent(true);
                    while (lRatioTypes.length > iQuantitativeValidationSingelton.getMatchedRatioTypes().size()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            //sleep failed
                        }
                    }

                    if (iQuantitativeValidationSingelton.isNormalization()) {
                        doNormalization(lDistillerProtein, lRatioList);
                    }
                    //sort by the ratio group numbers
                    Collections.sort(lDistillerProtein, new QuantitativeProteinSorterByRatioGroupNumbers());


                    //get the reference set size from the singelton
                    int lReferenceSetSize = iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet();
                    if (iQuantitativeValidationSingelton.getUseAllProteinsForReferenceSet()) {
                        lReferenceSetSize = lDistillerProtein.size();
                    }
                    if (lReferenceSetSize > lDistillerProtein.size()) {
                        lReferenceSetSize = lDistillerProtein.size();
                    }
                    for (int i = 0; i < lReferenceSetSize; i++) {
                        lReferenceSet.addReferenceProtein(lDistillerProtein.get(i));
                    }
                    iQuantitativeValidationSingelton.setReferenceSet(lReferenceSet);
                    //lReferenceSet.calculateStatisticsByRandomSampling();


                    //Show in the gui that the creation of the proteins is done
                    setIconOnPanel(jpanCreate, "apply.png", 3, 5);

                    //download the protein sequence
                    lblDownload.setEnabled(true);
                    setIconOnPanel(jpanDownload, "clock.png", 3, 6);
                    downloadProteinSequences(lDistillerProtein);
                    setIconOnPanel(jpanDownload, "apply.png", 3, 6);

                    //update progress bar
                    progressBar.setIndeterminate(false);

                    //sort by the protein accession
                    Collections.sort(lDistillerProtein, new QuantitativeProteinSorterByAccession());

                    //_____Do garbage collection______
                    System.gc();

                    //show gui
                    JOptionPane.showMessageDialog(iParent, "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                    QuantitationValidationGUI gui = new QuantitationValidationGUI(lDistillerProtein, iParent.getMs_limsConnection(), iParent.isStandAlone());
                    gui.setVisible(true);

                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                // step 7.2 dispose the iText components.
                return true;
            }

            public void finished() {
                //
                iParent.closeFrame();
            }

        };
        lStarter.start();
    }


    /**
     * This method start the data acquisition process for a maxquant ms_lims project
     */
    public void startMs_limsMaxQuant() {
        //create a new swing worker
        final Flamable lFlamable = iParent;
        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                try {

                    QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
                    iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

                    //update progress bar
                    progressBar.setIndeterminate(true);
                    //set id loading
                    lblId.setEnabled(true);
                    setIconOnPanel(jpanId, "clock.png", 3, 2);

                    //1. get IdentificationExtension for a specific project
                    IdentificationExtension[] lQuantPeptides;
                    lQuantPeptides = IdentificationExtension.getIdentificationExtensionsforProject(iParent.getMs_limsConnection(), iParent.getSelectedProject().getProjectid(), null);
                    if (lQuantPeptides.length == 0) {
                        lFlamable.passHotPotato(new Throwable("No identifications found for this project!"));
                        return false;
                    }
                    //create a string with the identificationsids seperated by ","
                    String lIdentificationsIds = "";
                    for (int i = 0; i < lQuantPeptides.length; i++) {
                        lIdentificationsIds = lIdentificationsIds + lQuantPeptides[i].getIdentificationid() + " , ";
                    }
                    lIdentificationsIds = lIdentificationsIds.substring(0, lIdentificationsIds.lastIndexOf(","));

                    //2. get all the quantitation linkers (identification_to_quantitation)
                    Identification_to_quantitation[] lQuantLinkers = Identification_to_quantitation.getIdentification_to_quantitationForIdentificationIds(iParent.getMs_limsConnection(), lIdentificationsIds);
                    //now add them to the correct quantitative peptide
                    for (int i = 0; i < lQuantLinkers.length; i++) {
                        for (int j = 0; j < lQuantPeptides.length; j++) {
                            if (lQuantPeptides[j].getIdentificationid() == lQuantLinkers[i].getL_identificationid()) {
                                lQuantPeptides[j].addIdentification_to_quantitation(lQuantLinkers[i]);
                            }
                        }
                    }
                    if (lQuantLinkers.length == 0) {
                        lFlamable.passHotPotato(new Throwable("No quantitations found for this project!"));
                        return false;
                    }
                    //Show in the gui that we found all the identifications
                    setIconOnPanel(jpanId, "apply.png", 3, 2);
                    //set quant  loading
                    lblQuant.setEnabled(true);
                    setIconOnPanel(jpanQuant, "clock.png", 3, 3);

                    //3.get for all the QuantPeptides the original  file name
                    ArrayList<Long> lMaxQuantFilesIds = new ArrayList<Long>();
                    String sql;
                    if (iQuantitativeValidationSingelton.getMsLimsPre7_2()) {
                        sql = "select i.identificationid, f.filename, f.quantitation_fileid from identification as i, identification_to_quantitation as t, quantitation_file as f, quantitation as q where i.identificationid in (" + lIdentificationsIds + ") and i.identificationid = t.l_identificationid and t.quantitation_link = q.quantitation_link and q.l_quantitation_fileid  = f.quantitation_fileid";
                    } else {
                        sql = "select i.identificationid, f.filename, f.quantitation_fileid from identification as i, identification_to_quantitation as t, quantitation_file as f, quantitation_group as g where i.identificationid in (" + lIdentificationsIds + ") and i.identificationid = t.l_identificationid and t.l_quantitation_groupid = g.quantitation_groupid and g.L_quantitation_fileid  = f.quantitation_fileid";
                    }
                    PreparedStatement ps = iParent.getMs_limsConnection().prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        long lId = rs.getLong(1);
                        String lMaxQuantName = rs.getString(2);
                        long lMaxQuantFilesId = rs.getLong(3);
                        //get a list of all the rov files
                        boolean lMaxQuantFile = true;
                        for (int r = 0; r < lMaxQuantFilesIds.size(); r++) {
                            if (lMaxQuantFilesId == lMaxQuantFilesIds.get(r)) {
                                lMaxQuantFile = false;
                            }
                        }
                        if (lMaxQuantFile) {
                            lMaxQuantFilesIds.add(lMaxQuantFilesId);
                        }
                        //attach the file name to the quant peptides
                        for (int j = 0; j < lQuantPeptides.length; j++) {
                            if (lQuantPeptides[j].getIdentificationid() == lId) {
                                lQuantPeptides[j].setQuantitationFileName(lMaxQuantName);
                            }
                        }
                    }
                    rs.close();
                    ps.close();

                    //update progress bar
                    progressBar.setIndeterminate(false);
                    progressBar.setMaximum(lMaxQuantFilesIds.size() + 1);
                    progressBar.setValue(0);
                    progressBar.setStringPainted(true);

                    //4.store all the rov files used for this project in the temp folder
                    //4.1 create the temp/ms_lims folder
                    File lTempfolder = File.createTempFile("temp", "temp").getParentFile();
                    File lTempRoverFolder = new File(lTempfolder, "rover");
                    lTempRoverFolder.deleteOnExit();
                    if (lTempRoverFolder.exists() == false) {
                        lTempRoverFolder.mkdir();
                    }
                    File lMQfolder = new File(lTempRoverFolder.getPath() + "/maxquant");
                    if (lMQfolder.exists()) {
                        WizardFrameHolder.deleteDir(lMQfolder);
                    }
                    //4.2 get all the rov files
                    RatioGroupCollection lRatioGroupCollection = null;

                    //update progress bar
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString("Reading quantitative information");

                    Quantitation_file lQuantFile = Quantitation_file.getQuantitation_fileForId(iParent.getMs_limsConnection(), lMaxQuantFilesIds.get(0));

                    //store the file
                    // Start the unzipping!
                    ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(lQuantFile.getFile()));
                    ZipEntry ze = null;
                    // Cycle the zip entries.
                    while ((ze = zis.getNextEntry()) != null) {
                        // Unzip each.
                        QuantitativeValidationSingelton.unzipEntry(lTempRoverFolder, ze, zis);
                    }
                    zis.close();


                    //create an xml reader
                    lRatioGroupCollection = (new MaxQuantEvidenceFile(new File(lTempRoverFolder.getPath() + "/maxquant/evidence.txt"), new File(lTempRoverFolder.getPath() + "/maxquant/msms.txt"), lFlamable)).getRatioGroupCollection();
                    lRatioGroupCollection.setRoverSource(iParent.getRoverSource());


                    //_____Do garbage collection______
                    System.gc();

                    //update progress bar
                    progressBar.setString("");
                    progressBar.setIndeterminate(true);

                    //Show in the gui that we found all the rov files
                    setIconOnPanel(jpanQuant, "apply.png", 3, 3);

                    //set match  loading
                    lblMatch.setEnabled(true);
                    setIconOnPanel(jpanMatch, "clock.png", 3, 4);

                    //5. couple the quantitative peptides to the distiller ratio groups
                    //get all the quantitations from the db
                    //these will be used to link to the DistillerRatios

                    //update progress bar
                    progressBar.setString("Getting quantitations from the db!");
                    progressBar.setStringPainted(true);

                    Vector<QuantitationExtension> lQuantitations = QuantitationExtension.getQuantitationForIdentifications(lIdentificationsIds, iParent.getMs_limsConnection());

                    //update progress bar
                    progressBar.setString("");
                    progressBar.setIndeterminate(false);
                    progressBar.setMaximum(lMaxQuantFilesIds.size() + 1);
                    progressBar.setValue(0);
                    progressBar.setStringPainted(true);

                    iQuantitativeValidationSingelton.setRatioTypes(lRatioGroupCollection.getRatioTypes());
                    iQuantitativeValidationSingelton.setComponentTypes(lRatioGroupCollection.getComponentTypes());
                    //update progress bar
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString("Matching identifications and ratios ");

                    //now match the identifications found for this rov file to the ratio groups
                    //we will clone it, because if we don't find quantitation from the db to link to the ratio group we will delete this ratiogroup
                    RatioGroupCollection lClonedCollection = (RatioGroupCollection) lRatioGroupCollection.clone();
                    //get the identificationids and the file_refs from the db
                    PreparedStatement ps2 = iParent.getMs_limsConnection().prepareStatement("SELECT i.identificationid, g.file_ref FROM identification_to_quantitation as t, quantitation_group as g, identification as i , spectrum as s where s.l_projectid = ? and s.spectrumid = i.l_spectrumid and i.identificationid = t.l_identificationid and t.l_quantitation_groupid = g.quantitation_groupid");
                    ps2.setLong(1, iParent.getSelectedProject().getProjectid());
                    ResultSet rs2 = ps2.executeQuery();
                    HashMap lIdRefMap = new HashMap();
                    while (rs2.next()) {
                        lIdRefMap.put(rs2.getLong(1), rs2.getLong(2));
                    }
                    rs2.close();
                    ps2.close();

                    for (int q = 0; q < lQuantPeptides.length; q++) {
                        lQuantPeptides[q].setFileRef((Long) lIdRefMap.get(lQuantPeptides[q].getIdentificationid()));
                    }

                    for (int j = 0; j < lClonedCollection.size(); j++) {
                        MaxQuantRatioGroup lRatioGroup = (MaxQuantRatioGroup) lClonedCollection.get(j);
                        lRatioGroup.linkIdentificationsAndQueries(lQuantPeptides);

                        //match the quantitation with the ratios
                        boolean lRatioGroupLinkFound = false;
                        for (int k = 0; k < lRatioGroup.getNumberOfRatios(); k++) {
                            MaxQuantRatio lRatio = (MaxQuantRatio) lRatioGroup.getRatio(k);
                            BigDecimal lRatioValue = new BigDecimal(lRatio.getRatio(false));
                            lRatioValue = lRatioValue.setScale(5, BigDecimal.ROUND_HALF_DOWN);

                            boolean lLinkFound = false;

                            for (int l = 0; l < lQuantitations.size(); l++) {
                                QuantitationExtension lQuant = lQuantitations.get(l);


                                //check if the filename, the hitnumber, the ratio and the ratio type is the same
                                String lQuantFileName = lQuant.getQuantitationFileName().substring(lQuant.getQuantitationFileName().lastIndexOf("\\") + 1);

                                if (lRatioValue.doubleValue() == lQuant.getRatio() && lRatio.getType().equalsIgnoreCase(lQuant.getType()) && lQuant.getFile_ref().equalsIgnoreCase(String.valueOf(lRatioGroup.getId()))) {
                                    lRatio.setQuantitationStoredInDb(lQuant);
                                    //set the valid status from the database and not from the original rov file
                                    lRatio.setValid(lQuant.getValid());
                                    lQuantitations.remove(lQuant);
                                    l = lQuantitations.size();
                                    lLinkFound = true;
                                    lRatioGroupLinkFound = true;
                                }
                            }
                            if (!lLinkFound) {
                                //no quantitation extension could be linked to a ratio, therefor we will delete the ratio
                                lRatioGroup.deleteRatio(lRatio);
                            }

                        }

                        if (!lRatioGroupLinkFound) {
                            //no quantitation extension could be linked to one of the ratios in the ratio group, therefor we will delete the ratiogroup
                            lRatioGroupCollection.remove(lRatioGroup);
                        }
                    }

                    //_____Do garbage collection______
                    System.gc();

                    //update progress bar
                    progressBar.setIndeterminate(true);
                    progressBar.setStringPainted(false);
                    progressBar.setString("");

                    //_____Do garbage collection______
                    System.gc();

                    //Show in the gui that the matching is done
                    setIconOnPanel(jpanMatch, "apply.png", 3, 4);
                    //set create loading
                    lblCreate.setEnabled(true);
                    setIconOnPanel(jpanCreate, "clock.png", 3, 5);

                    //6. get all the protein accessions from the identifications
                    Vector<String> lProteinAccessions = new Vector<String>();
                    for (int j = 0; j < lRatioGroupCollection.size(); j++) {
                        RatioGroup lRatioGroup = lRatioGroupCollection.get(j);

                        if (lRatioGroup.getNumberOfIdentifications() != 0) {
                            String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                            for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                                //check if it's a new accession
                                boolean lNewAccession = true;
                                for (int l = 0; l < lProteinAccessions.size(); l++) {
                                    if (lProteinAccessions.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                                        lNewAccession = false;
                                    }
                                }
                                if (lNewAccession) {
                                    lProteinAccessions.add(lAccessionsForRatioGroup[k]);
                                }

                            }
                        } else {
                            //System.out.println("No identification found");
                        }
                    }

                    if (lRatioGroupCollection == null) {
                        //show gui
                        JOptionPane.showMessageDialog(iParent, "No quantitative data could be found!\n The program will close.", "INFO", JOptionPane.INFORMATION_MESSAGE);
                        iParent.close();
                    }

                    //7.A get the types of the ratios from the first distiller ratio collecion
                    Vector<String> lRatioList = lRatioGroupCollection.getRatioTypes();
                    String[] lRatioTypes = new String[lRatioList.size()];
                    lRatioList.toArray(lRatioTypes);

                    //7.B get the types of the ratios from the first distiller ratio collecion
                    Vector<String> lComponentList = lRatioGroupCollection.getComponentTypes();
                    String[] lComponentTypes = new String[lComponentList.size()];
                    lComponentList.toArray(lComponentTypes);


                    //8. create all the distiller proteins
                    Vector<QuantitativeProtein> lProteins = new Vector<QuantitativeProtein>();
                    for (int i = 0; i < lProteinAccessions.size(); i++) {
                        lProteins.add(new QuantitativeProtein(lProteinAccessions.get(i), lRatioTypes));
                    }

                    //9. couple the distiller ratio groups to the distiller proteins

                    for (int j = 0; j < lRatioGroupCollection.size(); j++) {
                        //get the ratio group
                        RatioGroup lRatioGroup = lRatioGroupCollection.get(j);
                        //get all the protein accession linked to this ratiogroup
                        String[] lAccessions = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessions.length; k++) {
                            for (int l = 0; l < lProteins.size(); l++) {
                                if (lAccessions[k].equalsIgnoreCase(lProteins.get(l).getAccession())) {
                                    //add the ratio group to the protein if the accession is the same
                                    lProteins.get(l).addRatioGroup(lRatioGroup);
                                }
                            }
                        }
                    }
                    calculateRazorPeptides(lProteins);
                    iQuantitativeValidationSingelton.setAllProteins(lProteins);

                    //10. create a reference set with the "household" proteins with the most ratiogroups
                    ReferenceSet lReferenceSet = new ReferenceSet(new ArrayList<QuantitativeProtein>(), lRatioTypes, lComponentTypes);

                    MatchRatioWithComponent lMatch = new MatchRatioWithComponent(true);
                    while (lRatioTypes.length > iQuantitativeValidationSingelton.getMatchedRatioTypes().size()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            //sleep failed
                        }
                    }

                    if (iQuantitativeValidationSingelton.isNormalization()) {
                        doNormalization(lProteins, lRatioList);
                    }
                    //sort by the ratio group numbers
                    Collections.sort(lProteins, new QuantitativeProteinSorterByRatioGroupNumbers());


                    //get the reference set size from the singelton
                    int lReferenceSetSize = iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet();
                    if (iQuantitativeValidationSingelton.getUseAllProteinsForReferenceSet()) {
                        lReferenceSetSize = lProteins.size();
                    }
                    if (lReferenceSetSize > lProteins.size()) {
                        lReferenceSetSize = lProteins.size();
                    }
                    for (int i = 0; i < lReferenceSetSize; i++) {
                        lReferenceSet.addReferenceProtein(lProteins.get(i));
                    }
                    iQuantitativeValidationSingelton.setReferenceSet(lReferenceSet);
                    //lReferenceSet.calculateStatisticsByRandomSampling();


                    //Show in the gui that the creation of the proteins is done
                    setIconOnPanel(jpanCreate, "apply.png", 3, 5);

                    //download the protein sequence
                    lblDownload.setEnabled(true);
                    setIconOnPanel(jpanDownload, "clock.png", 3, 6);
                    downloadProteinSequences(lProteins);
                    setIconOnPanel(jpanDownload, "apply.png", 3, 6);

                    //update progress bar
                    progressBar.setIndeterminate(false);

                    //sort by the protein accession
                    Collections.sort(lProteins, new QuantitativeProteinSorterByAccession());

                    //_____Do garbage collection______
                    System.gc();

                    //show gui
                    JOptionPane.showMessageDialog(iParent, "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                    QuantitationValidationGUI gui = new QuantitationValidationGUI(lProteins, iParent.getMs_limsConnection(), iParent.isStandAlone());
                    gui.setVisible(true);

                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                // step 7.2 dispose the iText components.
                return true;
            }

            public void finished() {
                //
                iParent.closeFrame();
            }

        };
        lStarter.start();
    }


    /**
     * This method start the data acquisition process for a different mascot distiller quantitation toolbox files
     */
    public void startFileRov() {

        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
                iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

                //1.unzip all the file and check if the quantitation and identification information can be found
                Vector<File> lFiles = iParent.getFiles();
                Vector<RovFile> lRovFiles = new Vector<RovFile>();

                //update progress bar
                progressBar.setMaximum(lFiles.size() + 1);
                progressBar.setValue(0);
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(false);
                //set check loading
                setIconOnPanel(jpanCheck, "clock.png", 3, 1);
                lblCheckFiles.setEnabled(true);

                for (int i = 0; i < lFiles.size(); i++) {
                    //update progress bar
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString("Checking .rov file number " + (i + 1) + " of " + lFiles.size() + " distiller quantitation files");

                    RovFile lRovFile = new RovFile(lFiles.get(i));
                    boolean allOk = lRovFile.unzipRovFile();
                    if (!allOk) {
                        //problem with rov file
                        iParent.passHotPotato(new Throwable("A problem with the .rov file '" + lRovFile.getRovFilePath() + "' was detected!\nThis .rov file will not be used"));
                    } else {
                        //no problem
                        lRovFiles.add(lRovFile);
                    }

                    //ToDo delete me

                    //i = i + 10;

                    //ToDo delete me
                }
                //Show in the gui that we found all the rov files
                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(false);
                progressBar.setIndeterminate(true);
                setIconOnPanel(jpanCheck, "apply.png", 3, 1);

                //2.read the quantitation and the identification files

                //set id loading
                lblId.setEnabled(true);
                setIconOnPanel(jpanId, "clock.png", 3, 2);
                //set quant  loading
                lblQuant.setEnabled(true);
                setIconOnPanel(jpanQuant, "clock.png", 3, 3);

                //update progress bar
                progressBar.setValue(lRovFiles.size());
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(false);
                progressBar.setMaximum(lRovFiles.size() + 1);
                progressBar.setValue(0);

                for (int i = 0; i < lRovFiles.size(); i++) {

                    //update progress bar
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString("Parsing .rov file number " + (i + 1) + " of " + lRovFiles.size() + " distiller quantitation files");

                    RovFile lRovFile = lRovFiles.get(i);
                    lRovFile.setFlamable(iParent);
                    lRovFile.unzipRovFile();
                    lRovFile.readQuantitationXmlFile();

                    //_____Do garbage collection______
                    System.gc();

                    //ToDo delete me

                    //i = i + 10;

                    //ToDo delete me

                }

                //_____Do garbage collection______
                System.gc();

                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(false);
                progressBar.setIndeterminate(true);

                //set id done
                setIconOnPanel(jpanId, "apply.png", 3, 2);
                //set quant done
                setIconOnPanel(jpanQuant, "apply.png", 3, 3);

                //3.match ids to quantitations

                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(false);
                progressBar.setMaximum(lRovFiles.size() + 1);
                progressBar.setValue(0);

                //set match loading
                lblMatch.setEnabled(true);
                setIconOnPanel(jpanMatch, "clock.png", 3, 4);

                for (int i = 0; i < lRovFiles.size(); i++) {

                    //update progress bar
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString("Matching identification with ratios from .rov file number " + (i + 1) + " of " + lRovFiles.size() + " distiller quantitation files");

                    RovFile lRovFile = lRovFiles.get(i);
                    lRovFile.setThreshold(1.0 - iParent.getThreshold());
                    lRovFile.match();

                    //_____Do garbage collection______
                    System.gc();

                    //ToDo delete me

                    //i = i + 10;

                    //ToDo delete me
                }

                //_____Do garbage collection______
                System.gc();

                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(false);
                progressBar.setIndeterminate(true);

                //set match done
                setIconOnPanel(jpanMatch, "apply.png", 3, 4);

                //6. get all the protein accessions from the identifications

                //set create loading
                lblCreate.setEnabled(true);
                setIconOnPanel(jpanCreate, "clock.png", 3, 5);

                Vector<String> lProteinAccessions = new Vector<String>();
                for (int i = 0; i < lRovFiles.size(); i++) {
                    RovFile lRovFile = lRovFiles.get(i);
                    lRovFile.getRatioGroupCollection().setRoverSource(iParent.getRoverSource());
                    if (i == 0) {
                        iQuantitativeValidationSingelton.setRatioTypes(lRovFile.getRatioGroupCollection().getRatioTypes());
                        iQuantitativeValidationSingelton.setComponentTypes(lRovFile.getRatioGroupCollection().getComponentTypes());
                    }
                    for (int j = 0; j < lRovFile.getRatioGroupCollection().size(); j++) {
                        DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) lRovFile.getRatioGroupCollection().get(j);

                        String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                            //check if it's a new accession
                            boolean lNewAccession = true;
                            for (int l = 0; l < lProteinAccessions.size(); l++) {
                                if (lProteinAccessions.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                                    lNewAccession = false;
                                }
                            }
                            if (lNewAccession) {
                                lProteinAccessions.add(lAccessionsForRatioGroup[k]);
                            }

                        }
                    }

                    //ToDo delete me

                    //i = i + 10;

                    //ToDo delete me
                }

                if (lRovFiles.size() == 0) {
                    //show gui
                    JOptionPane.showMessageDialog(iParent, "No quantitative data could be found!\n The program will close.", "INFO", JOptionPane.INFORMATION_MESSAGE);
                    iParent.close();
                }

                //7.A get the types of the ratios from the first distiller ratio collecion
                RovFile lRovFileTemp = lRovFiles.get(0);
                Vector<String> lRatioList = lRovFileTemp.getRatioGroupCollection().getRatioTypes();
                String[] lRatioTypes = new String[lRatioList.size()];
                lRatioList.toArray(lRatioTypes);

                //7.B get the types of the ratios from the first distiller ratio collecion
                Vector<String> lComponentList = lRovFileTemp.getRatioGroupCollection().getComponentTypes();
                String[] lComponentTypes = new String[lComponentList.size()];
                lComponentList.toArray(lComponentTypes);


                //8. create all the distiller proteins
                Vector<QuantitativeProtein> lDistillerProtein = new Vector<QuantitativeProtein>();
                for (int i = 0; i < lProteinAccessions.size(); i++) {
                    lDistillerProtein.add(new QuantitativeProtein(lProteinAccessions.get(i), lRatioTypes));
                }

                //9. couple the distiller ratio groups to the distiller proteins
                for (int i = 0; i < lRovFiles.size(); i++) {
                    RovFile lRovFile = lRovFiles.get(i);
                    for (int j = 0; j < lRovFile.getRatioGroupCollection().size(); j++) {
                        //get the ratio group
                        DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) lRovFile.getRatioGroupCollection().get(j);
                        //get all the protein accession linked to this ratiogroup
                        String[] lAccessions = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessions.length; k++) {
                            for (int l = 0; l < lDistillerProtein.size(); l++) {
                                if (lAccessions[k].equalsIgnoreCase(lDistillerProtein.get(l).getAccession())) {
                                    //add the ratio group to the protein if the accession is the same
                                    lDistillerProtein.get(l).addRatioGroup(lRatioGroup);
                                }
                            }
                        }
                    }

                    //ToDo delete me

                    //i = i + 10;

                    //ToDo delete me
                }

                calculateRazorPeptides(lDistillerProtein);
                iQuantitativeValidationSingelton.setAllProteins(lDistillerProtein);

                //10. create a reference set with the "household" proteins with the most ratiogroups
                ReferenceSet lReferenceSet = new ReferenceSet(new ArrayList<QuantitativeProtein>(), lRatioTypes, lComponentTypes);

                MatchRatioWithComponent lMatch = new MatchRatioWithComponent(true);
                while (lRatioTypes.length > iQuantitativeValidationSingelton.getMatchedRatioTypes().size()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //sleep failed
                    }
                }
                if (iQuantitativeValidationSingelton.isNormalization()) {
                    doNormalization(lDistillerProtein, lRatioList);
                }

                //sort by the ratio group numbers
                Collections.sort(lDistillerProtein, new QuantitativeProteinSorterByRatioGroupNumbers());

                //get the reference set size from the singelton
                int lReferenceSetSize = iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet();
                if (iQuantitativeValidationSingelton.getUseAllProteinsForReferenceSet()) {
                    lReferenceSetSize = lDistillerProtein.size();
                }
                if (lReferenceSetSize > lDistillerProtein.size()) {
                    lReferenceSetSize = lDistillerProtein.size();
                }
                for (int i = 0; i < lReferenceSetSize; i++) {
                    lReferenceSet.addReferenceProtein(lDistillerProtein.get(i));
                }
                iQuantitativeValidationSingelton.setReferenceSet(lReferenceSet);
                //lReferenceSet.calculateStatisticsByRandomSampling();

                //Show in the gui that the creation of the proteins is done
                setIconOnPanel(jpanCreate, "apply.png", 3, 5);

                //download the protein sequence
                lblDownload.setEnabled(true);
                setIconOnPanel(jpanDownload, "clock.png", 3, 6);
                downloadProteinSequences(lDistillerProtein);
                setIconOnPanel(jpanDownload, "apply.png", 3, 6);

                //sort by the protein accession
                Collections.sort(lDistillerProtein, new QuantitativeProteinSorterByAccession());

                //_____Do garbage collection______
                System.gc();

                //show gui
                JOptionPane.showMessageDialog(iParent, "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                QuantitationValidationGUI gui = new QuantitationValidationGUI(lDistillerProtein, null, iParent.isStandAlone());
                gui.setVisible(true);
                return true;
            }

            public void finished() {
                //
                iParent.closeFrame();
            }

        };
        lStarter.start();

    }

    /**
     * This method start the data acquisition process for different dat files
     */
    public void startFileDat() {

        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
                iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

                //1.check the datfiles
                Vector<File> lFiles = iParent.getFiles();
                Vector<DatFile> lDatFiles = new Vector<DatFile>();

                //update progress bar
                progressBar.setMaximum(lFiles.size() + 1);
                progressBar.setValue(0);
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(false);
                //set checking files
                lblCheckFiles.setEnabled(true);
                setIconOnPanel(jpanCheck, "clock.png", 3, 1);

                //1.check the datfiles
                for (int i = 0; i < lFiles.size(); i++) {
                    lDatFiles.add(new DatFile(lFiles.get(i), iFlamable));

                    //update progress bar
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString("Checked " + lFiles.get(i).getName() + ".");
                }

                //update progress bar
                progressBar.setIndeterminate(true);
                //Show in the gui that we found all the rov files
                setIconOnPanel(jpanCheck, "apply.png", 3, 1);

                //set id loading
                lblId.setEnabled(true);
                setIconOnPanel(jpanId, "clock.png", 3, 2);
                //set quant loading
                lblQuant.setEnabled(true);
                setIconOnPanel(jpanQuant, "clock.png", 3, 3);

                //3.match ids to quantitations

                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(false);
                progressBar.setMaximum(lDatFiles.size() + 1);
                progressBar.setValue(0);

                //set id done
                setIconOnPanel(jpanId, "apply.png", 3, 2);
                //set quant done
                setIconOnPanel(jpanQuant, "apply.png", 3, 3);
                //set match loading
                lblMatch.setEnabled(true);
                setIconOnPanel(jpanMatch, "clock.png", 3, 4);

                Vector<RatioGroupCollection> lRatioGroupCollection = new Vector<RatioGroupCollection>();
                for (int i = 0; i < lDatFiles.size(); i++) {

                    //update progress bar
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString("Matching identification with ratios from dat file number " + (i + 1) + " of " + lDatFiles.size() + " mascot result files");

                    RatioGroupCollection lTemp = lDatFiles.get(i).getITraqRatioGroupCollection(iParent, iParent.getThreshold());
                    if (lTemp != null) {

                        lRatioGroupCollection.add(lDatFiles.get(i).getITraqRatioGroupCollection(iParent, iParent.getThreshold()));
                    }

                    //_____Do garbage collection______
                    System.gc();
                }

                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(false);
                progressBar.setIndeterminate(true);

                //set match done
                setIconOnPanel(jpanMatch, "apply.png", 3, 4);
                //set create loading
                lblCreate.setEnabled(true);
                setIconOnPanel(jpanCreate, "clock.png", 3, 5);

                //6. get all the protein accessions from the identifications
                Vector<String> lProteinAccessions = new Vector<String>();
                for (int i = 0; i < lRatioGroupCollection.size(); i++) {
                    if (i == 0) {
                        iQuantitativeValidationSingelton.setRatioTypes(lRatioGroupCollection.get(i).getRatioTypes());
                        iQuantitativeValidationSingelton.setComponentTypes(lRatioGroupCollection.get(i).getComponentTypes());
                    }

                    lRatioGroupCollection.get(i).setRoverSource(iParent.getRoverSource());

                    for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                        RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);

                        String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                            //check if it's a new accession
                            boolean lNewAccession = true;
                            for (int l = 0; l < lProteinAccessions.size(); l++) {
                                if (lProteinAccessions.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                                    lNewAccession = false;
                                }
                            }
                            if (lNewAccession) {
                                lProteinAccessions.add(lAccessionsForRatioGroup[k]);
                            }
                        }
                    }
                }

                if (lRatioGroupCollection.size() == 0) {
                    //show gui
                    JOptionPane.showMessageDialog(iParent, "No quantitative data could be found!\n The program will close.", "INFO", JOptionPane.INFORMATION_MESSAGE);
                    iParent.close();
                }

                //7.A get the types of the ratios from the first distiller ratio collecion
                Vector<String> lRatioList = lRatioGroupCollection.get(0).getRatioTypes();
                String[] lRatioTypes = new String[lRatioList.size()];
                lRatioList.toArray(lRatioTypes);

                //7.B get the types of the ratios from the first distiller ratio collecion
                Vector<String> lComponentList = lRatioGroupCollection.get(0).getComponentTypes();
                String[] lComponentTypes = new String[lComponentList.size()];
                lComponentList.toArray(lComponentTypes);


                //8. create all the distiller proteins
                Vector<QuantitativeProtein> lDistillerProtein = new Vector<QuantitativeProtein>();
                for (int i = 0; i < lProteinAccessions.size(); i++) {
                    lDistillerProtein.add(new QuantitativeProtein(lProteinAccessions.get(i), lRatioTypes));
                }

                //9. couple the distiller ratio groups to the distiller proteins
                for (int i = 0; i < lRatioGroupCollection.size(); i++) {
                    for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                        //get the ratio group
                        RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);
                        //get all the protein accession linked to this ratiogroup
                        String[] lAccessions = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessions.length; k++) {
                            for (int l = 0; l < lDistillerProtein.size(); l++) {
                                if (lAccessions[k].equalsIgnoreCase(lDistillerProtein.get(l).getAccession())) {
                                    //add the ratio group to the protein if the accession is the same
                                    lDistillerProtein.get(l).addRatioGroup(lRatioGroup);
                                }
                            }
                        }
                    }
                }

                calculateRazorPeptides(lDistillerProtein);
                iQuantitativeValidationSingelton.setAllProteins(lDistillerProtein);

                //10. create a reference set with the "household" proteins with the most ratiogroups
                ReferenceSet lReferenceSet = new ReferenceSet(new ArrayList<QuantitativeProtein>(), lRatioTypes, lComponentTypes);

                MatchRatioWithComponent lMatch = new MatchRatioWithComponent(true);
                while (lRatioTypes.length > iQuantitativeValidationSingelton.getMatchedRatioTypes().size()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //sleep failed
                    }
                }


                //sort by the ratio group numbers
                Collections.sort(lDistillerProtein, new QuantitativeProteinSorterByRatioGroupNumbers());

                //get the reference set size from the singelton
                int lReferenceSetSize = iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet();
                if (iQuantitativeValidationSingelton.getUseAllProteinsForReferenceSet()) {
                    lReferenceSetSize = lDistillerProtein.size();
                }
                for (int i = 0; i < lReferenceSetSize; i++) {
                    lReferenceSet.addReferenceProtein(lDistillerProtein.get(i));
                }
                iQuantitativeValidationSingelton.setReferenceSet(lReferenceSet);
                //lReferenceSet.calculateStatisticsByRandomSampling();

                //Show in the gui that the creation of the proteins is done
                setIconOnPanel(jpanCreate, "apply.png", 3, 5);

                //download the protein sequence
                lblDownload.setEnabled(true);
                setIconOnPanel(jpanDownload, "clock.png", 3, 6);
                downloadProteinSequences(lDistillerProtein);
                setIconOnPanel(jpanDownload, "apply.png", 3, 6);

                //sort by the protein accession
                Collections.sort(lDistillerProtein, new QuantitativeProteinSorterByAccession());

                //_____Do garbage collection______
                System.gc();

                //show gui
                JOptionPane.showMessageDialog(iParent, "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                QuantitationValidationGUI gui = new QuantitationValidationGUI(lDistillerProtein, null, iParent.isStandAlone());
                gui.setVisible(true);
                return true;
            }

            public void finished() {
                //
                iParent.closeFrame();
            }

        };
        lStarter.start();

    }

    /**
     * This method start the data acquisition process for MsQuant files
     */
    public void startMsQuant() {

        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
                iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

                //1.check the datfiles
                Vector<File> lFiles = iParent.getFiles();

                //update progress bar
                progressBar.setMaximum(lFiles.size() + 1);
                progressBar.setValue(0);
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(false);
                //set checking files
                lblCheckFiles.setEnabled(true);
                setIconOnPanel(jpanCheck, "clock.png", 3, 1);

                //1.check the MsQuant files
                for (int i = 0; i < lFiles.size(); i++) {
                    //update progress bar
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString("Checked " + lFiles.get(i).getName() + ".");
                }

                //update progress bar
                progressBar.setIndeterminate(true);
                //Show in the gui that we found all the rov files
                setIconOnPanel(jpanCheck, "apply.png", 3, 1);

                //set id loading
                lblId.setEnabled(true);
                setIconOnPanel(jpanId, "clock.png", 3, 2);
                //set quant loading
                lblQuant.setEnabled(true);
                setIconOnPanel(jpanQuant, "clock.png", 3, 3);

                //3.match ids to quantitations

                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(false);
                progressBar.setMaximum(lFiles.size() + 1);
                progressBar.setValue(0);

                //set id done
                setIconOnPanel(jpanId, "apply.png", 3, 2);
                //set quant done
                setIconOnPanel(jpanQuant, "apply.png", 3, 3);
                //set match loading
                lblMatch.setEnabled(true);
                setIconOnPanel(jpanMatch, "clock.png", 3, 4);

                Vector<RatioGroupCollection> lRatioGroupCollection = new Vector<RatioGroupCollection>();
                for (int i = 0; i < lFiles.size(); i++) {

                    //update progress bar
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString("Matching identification with msquant file " + (i + 1) + " of " + lFiles.size() + " msquant files");

                    MsQuantReader lReader = new MsQuantReader(lFiles.get(i), iFlamable);

                    RatioGroupCollection lTemp = lReader.getRatioGroupCollection();
                    if (lTemp != null) {
                        lTemp.setRoverSource(iParent.getRoverSource());
                        lRatioGroupCollection.add(lTemp);
                    }

                    //_____Do garbage collection______
                    System.gc();
                }

                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(false);
                progressBar.setIndeterminate(true);

                //set match done
                setIconOnPanel(jpanMatch, "apply.png", 3, 4);
                //set create loading
                lblCreate.setEnabled(true);
                setIconOnPanel(jpanCreate, "clock.png", 3, 5);

                //6. get all the protein accessions from the identifications
                Vector<String> lProteinAccessions = new Vector<String>();
                for (int i = 0; i < lRatioGroupCollection.size(); i++) {
                    if (i == 0) {
                        iQuantitativeValidationSingelton.setRatioTypes(lRatioGroupCollection.get(i).getRatioTypes());
                        iQuantitativeValidationSingelton.setComponentTypes(lRatioGroupCollection.get(i).getComponentTypes());
                    }
                    for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                        RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);

                        String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                            //check if it's a new accession
                            boolean lNewAccession = true;
                            for (int l = 0; l < lProteinAccessions.size(); l++) {
                                if (lProteinAccessions.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                                    lNewAccession = false;
                                }
                            }
                            if (lNewAccession) {
                                lProteinAccessions.add(lAccessionsForRatioGroup[k]);
                            }
                        }
                    }
                }

                if (lRatioGroupCollection.size() == 0) {
                    //show gui
                    JOptionPane.showMessageDialog(iParent, "No quantitative data could be found!\n The program will close.", "INFO", JOptionPane.INFORMATION_MESSAGE);
                    iParent.close();
                }

                //7.A get the types of the ratios from the first distiller ratio collecion
                Vector<String> lRatioList = lRatioGroupCollection.get(0).getRatioTypes();
                String[] lRatioTypes = new String[lRatioList.size()];
                lRatioList.toArray(lRatioTypes);

                //7.B get the types of the ratios from the first distiller ratio collecion
                Vector<String> lComponentList = lRatioGroupCollection.get(0).getComponentTypes();
                String[] lComponentTypes = new String[lComponentList.size()];
                lComponentList.toArray(lComponentTypes);


                //8. create all the distiller proteins
                Vector<QuantitativeProtein> lDistillerProtein = new Vector<QuantitativeProtein>();
                for (int i = 0; i < lProteinAccessions.size(); i++) {
                    lDistillerProtein.add(new QuantitativeProtein(lProteinAccessions.get(i), lRatioTypes));
                }

                //9. couple the distiller ratio groups to the distiller proteins
                for (int i = 0; i < lRatioGroupCollection.size(); i++) {
                    for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                        //get the ratio group
                        RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);
                        //get all the protein accession linked to this ratiogroup
                        String[] lAccessions = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessions.length; k++) {
                            for (int l = 0; l < lDistillerProtein.size(); l++) {
                                if (lAccessions[k].equalsIgnoreCase(lDistillerProtein.get(l).getAccession())) {
                                    //add the ratio group to the protein if the accession is the same
                                    lDistillerProtein.get(l).addRatioGroup(lRatioGroup);
                                }
                            }
                        }
                    }
                }
                calculateRazorPeptides(lDistillerProtein);
                iQuantitativeValidationSingelton.setAllProteins(lDistillerProtein);


                //10. create a reference set with the "household" proteins with the most ratiogroups
                ReferenceSet lReferenceSet = new ReferenceSet(new ArrayList<QuantitativeProtein>(), lRatioTypes, lComponentTypes);

                MatchRatioWithComponent lMatch = new MatchRatioWithComponent(true);
                while (lRatioTypes.length > iQuantitativeValidationSingelton.getMatchedRatioTypes().size()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //sleep failed
                    }
                }

                //sort by the ratio group numbers
                Collections.sort(lDistillerProtein, new QuantitativeProteinSorterByRatioGroupNumbers());

                //get the reference set size from the singelton
                int lReferenceSetSize = iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet();
                if (iQuantitativeValidationSingelton.getUseAllProteinsForReferenceSet()) {
                    lReferenceSetSize = lDistillerProtein.size();
                }
                for (int i = 0; i < lReferenceSetSize; i++) {
                    lReferenceSet.addReferenceProtein(lDistillerProtein.get(i));
                }
                iQuantitativeValidationSingelton.setReferenceSet(lReferenceSet);
                //lReferenceSet.calculateStatisticsByRandomSampling();


                //Show in the gui that the creation of the proteins is done
                setIconOnPanel(jpanCreate, "apply.png", 3, 5);

                //download the protein sequence
                lblDownload.setEnabled(true);
                setIconOnPanel(jpanDownload, "clock.png", 3, 6);
                downloadProteinSequences(lDistillerProtein);
                setIconOnPanel(jpanDownload, "apply.png", 3, 6);

                //sort by the protein accession
                Collections.sort(lDistillerProtein, new QuantitativeProteinSorterByAccession());

                //_____Do garbage collection______
                System.gc();

                //show gui
                JOptionPane.showMessageDialog(iParent, "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                QuantitationValidationGUI gui = new QuantitationValidationGUI(lDistillerProtein, null, iParent.isStandAlone());
                gui.setVisible(true);
                return true;
            }

            public void finished() {
                //
                iParent.closeFrame();
            }

        };
        lStarter.start();

    }


    /**
     * This method start the data acquisition process for MaxQuant files
     */
    public void startMaxQuant() {

        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
                iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);


                Vector<File> lFiles = iParent.getFiles();

                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(true);
                //set checking files
                lblCheckFiles.setEnabled(true);
                setIconOnPanel(jpanCheck, "clock.png", 3, 1);

                //1.check the MaxQuant files
                for (int i = 0; i < lFiles.size(); i++) {
                    //update progress bar
                    progressBar.setString("Checked " + lFiles.get(i).getName() + ".");
                }

                //update progress bar
                //Show in the gui that we found all the rov files
                setIconOnPanel(jpanCheck, "apply.png", 3, 1);

                //set id loading
                lblId.setEnabled(true);
                setIconOnPanel(jpanId, "clock.png", 3, 2);
                //set quant loading
                lblQuant.setEnabled(true);
                setIconOnPanel(jpanQuant, "clock.png", 3, 3);

                //3.match ids to quantitations

                //update progress bar
                progressBar.setString("");

                //set id done
                setIconOnPanel(jpanId, "apply.png", 3, 2);
                //set quant done
                setIconOnPanel(jpanQuant, "apply.png", 3, 3);
                //set match loading
                lblMatch.setEnabled(true);
                setIconOnPanel(jpanMatch, "clock.png", 3, 4);

                Vector<RatioGroupCollection> lRatioGroupCollection = new Vector<RatioGroupCollection>();
                for (int i = 0; i < lFiles.size(); i++) {

                    //update progress bar
                    progressBar.setString("Matching identification with ratios from evidence file " + (i + 1) + " of " + (lFiles.size() / 2) + " evidence files.");
                    MaxQuantEvidenceFile lFile;
                    if (lFiles.get(i).getName().startsWith("evidence")) {
                        lFile = new MaxQuantEvidenceFile(lFiles.get(i), lFiles.get(i + 1), iFlamable);
                    } else {
                        lFile = new MaxQuantEvidenceFile(lFiles.get(i + 1), lFiles.get(i), iFlamable);
                    }
                    i = i + 1;

                    RatioGroupCollection lTemp = lFile.getRatioGroupCollection();
                    if (lTemp != null) {
                        lTemp.setRoverSource(iParent.getRoverSource());
                        lRatioGroupCollection.add(lTemp);
                    }

                    //_____Do garbage collection______
                    System.gc();
                }

                //update progress bar
                progressBar.setString("");

                //set match done
                setIconOnPanel(jpanMatch, "apply.png", 3, 4);
                //set create loading
                lblCreate.setEnabled(true);
                setIconOnPanel(jpanCreate, "clock.png", 3, 5);

                //6. get all the protein accessions from the identifications
                //7.A get the types of the ratios from the first distiller ratio collecion
                Vector<String> lRatioList = lRatioGroupCollection.get(0).getRatioTypes();
                String[] lRatioTypes = new String[lRatioList.size()];
                lRatioList.toArray(lRatioTypes);

                //7.B get the types of the ratios from the first distiller ratio collecion
                Vector<String> lComponentList = lRatioGroupCollection.get(0).getComponentTypes();
                String[] lComponentTypes = new String[lComponentList.size()];
                lComponentList.toArray(lComponentTypes);

                //8. create all the distiller proteins
                Vector<QuantitativeProtein> lDistillerProtein = new Vector<QuantitativeProtein>();
                HashMap<String, QuantitativeProtein> lProteinMap = new HashMap<String, QuantitativeProtein>();

                if (lRatioGroupCollection.size() == 0) {
                    //show gui
                    JOptionPane.showMessageDialog(iParent, "No quantitative data could be found!\n The program will close.", "INFO", JOptionPane.INFORMATION_MESSAGE);
                    iParent.close();
                }

                for (int i = 0; i < lRatioGroupCollection.size(); i++) {
                    if (i == 0) {
                        iQuantitativeValidationSingelton.setRatioTypes(lRatioGroupCollection.get(i).getRatioTypes());
                        iQuantitativeValidationSingelton.setComponentTypes(lRatioGroupCollection.get(i).getComponentTypes());
                    }


                    //System.out.println("Found " + lRatioGroupCollection.get(i).size() + " ratio groups!");
                    for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                        RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);

                        /*if (j % 5000 == 0) {
                            System.out.println("Extracting protein accessions from ratio groups " + (j + 1) + "/" + lRatioGroupCollection.get(i).size() + "(found " + lDistillerProtein.size() + " proteins)");
                        } */

                        String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                            //check if it's a new accession
                            QuantitativeProtein lTempProtein = lProteinMap.get(lAccessionsForRatioGroup[k]);
                            if (lTempProtein != null) {
                                lTempProtein.addRatioGroup(lRatioGroup);
                            } else {
                                QuantitativeProtein lProtein = new QuantitativeProtein(lAccessionsForRatioGroup[k], lRatioTypes);
                                lProtein.addRatioGroup(lRatioGroup);
                                lDistillerProtein.add(lProtein);
                                lProteinMap.put(lAccessionsForRatioGroup[k], lProtein);
                            }
                        }
                    }
                }


                //10. create a reference set with the "household" proteins with the most ratiogroups
                ReferenceSet lReferenceSet = new ReferenceSet(new ArrayList<QuantitativeProtein>(), lRatioTypes, lComponentTypes);
                MatchRatioWithComponent lMatch = new MatchRatioWithComponent(true);
                while (lRatioTypes.length > iQuantitativeValidationSingelton.getMatchedRatioTypes().size()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //sleep failed
                    }
                }

                iQuantitativeValidationSingelton.setAllProteins(lDistillerProtein);
                if (iQuantitativeValidationSingelton.isNormalization()) {
                    doNormalization(lDistillerProtein, lRatioList);
                }
                //sort by the ratio group numbers
                Collections.sort(lDistillerProtein, new QuantitativeProteinSorterByRatioGroupNumbers());

                //get the reference set size from the singelton
                int lReferenceSetSize = iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet();
                if (iQuantitativeValidationSingelton.getUseAllProteinsForReferenceSet() || lDistillerProtein.size() < lReferenceSetSize) {
                    lReferenceSetSize = lDistillerProtein.size();
                }
                for (int i = 0; i < lReferenceSetSize; i++) {
                    lReferenceSet.addReferenceProtein(lDistillerProtein.get(i));
                }
                iQuantitativeValidationSingelton.setReferenceSet(lReferenceSet);
                //lReferenceSet.calculateStatisticsByRandomSampling();


                //Show in the gui that the creation of the proteins is done
                setIconOnPanel(jpanCreate, "apply.png", 3, 5);

                //download the protein sequence
                lblDownload.setEnabled(true);
                setIconOnPanel(jpanDownload, "clock.png", 3, 6);
                downloadProteinSequences(lDistillerProtein);
                setIconOnPanel(jpanDownload, "apply.png", 3, 6);


                //sort by the protein accession
                Collections.sort(lDistillerProtein, new QuantitativeProteinSorterByAccession());

                //_____Do garbage collection______
                System.gc();

                //show gui
                JOptionPane.showMessageDialog(iParent, "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                QuantitationValidationGUI gui = new QuantitationValidationGUI(lDistillerProtein, null, iParent.isStandAlone());
                gui.setVisible(true);
                return true;
            }

            public void finished() {
                //
                iParent.closeFrame();
            }

        };
        lStarter.start();

    }

    /**
     * This method start the data acquisition process for Census files
     */
    public void startCensus() {

        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
                iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);


                Vector<File> lFiles = iParent.getFiles();

                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(true);
                //set checking files
                lblCheckFiles.setEnabled(true);
                setIconOnPanel(jpanCheck, "clock.png", 3, 1);

                //1.check the MaxQuant files
                for (int i = 0; i < lFiles.size(); i++) {
                    //update progress bar
                    progressBar.setString("Checked " + lFiles.get(i).getName() + ".");
                }

                //update progress bar
                //Show in the gui that we found all the rov files
                setIconOnPanel(jpanCheck, "apply.png", 3, 1);

                //set id loading
                lblId.setEnabled(true);
                setIconOnPanel(jpanId, "clock.png", 3, 2);
                //set quant loading
                lblQuant.setEnabled(true);
                setIconOnPanel(jpanQuant, "clock.png", 3, 3);

                //3.match ids to quantitations

                //update progress bar
                progressBar.setString("");

                //set id done
                setIconOnPanel(jpanId, "apply.png", 3, 2);
                //set quant done
                setIconOnPanel(jpanQuant, "apply.png", 3, 3);
                //set match loading
                lblMatch.setEnabled(true);
                setIconOnPanel(jpanMatch, "clock.png", 3, 4);

                Vector<RatioGroupCollection> lRatioGroupCollection = new Vector<RatioGroupCollection>();
                for (int i = 0; i < lFiles.size(); i++) {

                    //update progress bar
                    progressBar.setString("Matching identification with ratios census file " + (i + 1) + " of " + (lFiles.size() / 2) + " evidence files.");

                    CensusReader lReader;
                    if (lFiles.get(i).getName().endsWith(".txt")) {
                        lReader = new CensusReader(lFiles.get(i), lFiles.get(i + 1), iFlamable);
                    } else {
                        lReader = new CensusReader(lFiles.get(i + 1), lFiles.get(i), iFlamable);
                    }
                    i = i + 1;

                    RatioGroupCollection lTemp = lReader.getRatioGroupCollection();
                    if (lTemp != null) {
                        lTemp.setRoverSource(iParent.getRoverSource());
                        lRatioGroupCollection.add(lTemp);
                    }

                    //_____Do garbage collection______
                    System.gc();
                }

                //update progress bar
                progressBar.setString("");

                //set match done
                setIconOnPanel(jpanMatch, "apply.png", 3, 4);
                //set create loading
                lblCreate.setEnabled(true);
                setIconOnPanel(jpanCreate, "clock.png", 3, 5);

                //6. get all the protein accessions from the identifications
                Vector<String> lProteinAccessions = new Vector<String>();
                for (int i = 0; i < lRatioGroupCollection.size(); i++) {
                    if (i == 0) {
                        iQuantitativeValidationSingelton.setRatioTypes(lRatioGroupCollection.get(i).getRatioTypes());
                        iQuantitativeValidationSingelton.setComponentTypes(lRatioGroupCollection.get(i).getComponentTypes());
                    }
                    for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                        RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);

                        String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                            //check if it's a new accession
                            boolean lNewAccession = true;
                            for (int l = 0; l < lProteinAccessions.size(); l++) {
                                if (lProteinAccessions.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                                    lNewAccession = false;
                                }
                            }
                            if (lNewAccession) {
                                lProteinAccessions.add(lAccessionsForRatioGroup[k]);
                            }
                        }
                    }
                }

                if (lRatioGroupCollection.size() == 0) {
                    //show gui
                    JOptionPane.showMessageDialog(iParent, "No quantitative data could be found!\n The program will close.", "INFO", JOptionPane.INFORMATION_MESSAGE);
                    iParent.close();
                }

                //7.A get the types of the ratios from the first distiller ratio collecion
                Vector<String> lRatioList = lRatioGroupCollection.get(0).getRatioTypes();
                String[] lRatioTypes = new String[lRatioList.size()];
                lRatioList.toArray(lRatioTypes);

                //7.B get the types of the ratios from the first distiller ratio collecion
                Vector<String> lComponentList = lRatioGroupCollection.get(0).getComponentTypes();
                String[] lComponentTypes = new String[lComponentList.size()];
                lComponentList.toArray(lComponentTypes);


                //8. create all the distiller proteins
                Vector<QuantitativeProtein> lProtein = new Vector<QuantitativeProtein>();
                for (int i = 0; i < lProteinAccessions.size(); i++) {
                    lProtein.add(new QuantitativeProtein(lProteinAccessions.get(i), lRatioTypes));
                }

                //9. couple the distiller ratio groups to the distiller proteins
                for (int i = 0; i < lRatioGroupCollection.size(); i++) {
                    for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                        //get the ratio group
                        RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);
                        //get all the protein accession linked to this ratiogroup
                        String[] lAccessions = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessions.length; k++) {
                            for (int l = 0; l < lProtein.size(); l++) {
                                if (lAccessions[k].equalsIgnoreCase(lProtein.get(l).getAccession())) {
                                    //add the ratio group to the protein if the accession is the same
                                    lProtein.get(l).addRatioGroup(lRatioGroup);
                                }
                            }
                        }
                    }
                }

                calculateRazorPeptides(lProtein);
                iQuantitativeValidationSingelton.setAllProteins(lProtein);


                //10. create a reference set with the "household" proteins with the most ratiogroups
                ReferenceSet lReferenceSet = new ReferenceSet(new ArrayList<QuantitativeProtein>(), lRatioTypes, lComponentTypes);
                MatchRatioWithComponent lMatch = new MatchRatioWithComponent(true);
                while (lRatioTypes.length > iQuantitativeValidationSingelton.getMatchedRatioTypes().size()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //sleep failed
                    }
                }


                //sort by the ratio group numbers
                Collections.sort(lProtein, new QuantitativeProteinSorterByRatioGroupNumbers());

                //get the reference set size from the singelton
                int lReferenceSetSize = iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet();
                if (iQuantitativeValidationSingelton.getUseAllProteinsForReferenceSet()) {
                    lReferenceSetSize = lProtein.size();
                }
                if (lReferenceSetSize > lProtein.size()) {
                    lReferenceSetSize = lProtein.size();
                }
                for (int i = 0; i < lReferenceSetSize; i++) {
                    lReferenceSet.addReferenceProtein(lProtein.get(i));
                }
                iQuantitativeValidationSingelton.setReferenceSet(lReferenceSet);
                //lReferenceSet.calculateStatisticsByRandomSampling();

                //Show in the gui that the creation of the proteins is done
                setIconOnPanel(jpanCreate, "apply.png", 3, 5);

                //download the protein sequence
                lblDownload.setEnabled(true);
                setIconOnPanel(jpanDownload, "clock.png", 3, 6);
                downloadProteinSequences(lProtein);
                setIconOnPanel(jpanDownload, "apply.png", 3, 6);


                //sort by the protein accession
                Collections.sort(lProtein, new QuantitativeProteinSorterByAccession());

                //_____Do garbage collection______
                System.gc();

                //show gui
                JOptionPane.showMessageDialog(iParent, "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                QuantitationValidationGUI gui = new QuantitationValidationGUI(lProtein, null, iParent.isStandAlone());
                gui.setVisible(true);
                return true;
            }

            public void finished() {
                //
                iParent.closeFrame();
            }

        };
        lStarter.start();

    }


    /**
     * This method start the iTraq data acquisition process for different mascot distiller files
     */
    public void startITraqRov() {

        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
                iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

                //1.check the files

                //update progress bar
                Vector<File> lFiles = iParent.getFiles();
                Vector<DatFile> lDatFiles = new Vector<DatFile>();

                progressBar.setMaximum(lFiles.size() + 1);
                progressBar.setValue(0);
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(false);
                //set checking files
                lblCheckFiles.setEnabled(true);
                setIconOnPanel(jpanCheck, "clock.png", 3, 1);

                Vector<File> lMgfFiles = new Vector<File>();
                Vector<File> lRovFiles = new Vector<File>();
                for (int i = 0; i < lFiles.size(); i++) {
                    if (lFiles.get(i).getAbsolutePath().endsWith(".mgf")) {
                        lMgfFiles.add(lFiles.get(i));
                    } else if (lFiles.get(i).getAbsolutePath().endsWith(".rov")) {
                        lRovFiles.add(lFiles.get(i));
                    }
                }
                progressBar.setMaximum(lMgfFiles.size() + 1);
                progressBar.setValue(0);
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(false);
                //check if you have a .rov file for every mgf file
                for (int i = 0; i < lMgfFiles.size(); i++) {

                    //update progress bar
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString("Checking " + lMgfFiles.get(i).getName() + " .");

                    boolean lRovFound = false;
                    File lRovFile = null;
                    String lMgfFileName = lMgfFiles.get(i).getName().substring(0, lMgfFiles.get(i).getName().indexOf("."));
                    for (int j = 0; j < lRovFiles.size(); j++) {
                        String lRovFileName = lRovFiles.get(j).getName().substring(0, lRovFiles.get(j).getName().indexOf("."));
                        if (lRovFileName.indexOf(lMgfFileName) > -1) {
                            //we found the corresponding rov file
                            lRovFile = lRovFiles.get(j);
                            lRovFound = true;

                        }
                    }
                    if (!lRovFound) {
                        //no rov file was found for this mgf file
                    } else {
                        //we need to unzip the rov file
                        try {

                            File lDatFile = null;

                            File lTempfolder = File.createTempFile("temp", "temp").getParentFile();
                            File lTempRovFolder = new File(lTempfolder, "rover");

                            if (lTempRovFolder.exists() == false) {
                                lTempRovFolder.mkdir();
                            }


                            File lTempUnzippedRovFileFolder = new File(lTempRovFolder, lRovFile.getName());
                            lTempUnzippedRovFileFolder.deleteOnExit();

                            if (!lTempUnzippedRovFileFolder.exists()) {
                                // Folder does not exist yet.
                                if (!lTempUnzippedRovFileFolder.mkdir()) {
                                    // Making of folder failed, quit!
                                    iParent.passHotPotato(new Throwable("Unable to create temporary directory ' "
                                            + lTempUnzippedRovFileFolder.getName()
                                            + "' for distiller rov project '" + lRovFile.getName() + "'!!"));
                                    // If temporary dir could not be created, return null to stop the process.
                                    return false;
                                }

                                // Unzip the files in the new temp folder

                                BufferedOutputStream out = null;
                                ZipInputStream in = new ZipInputStream(
                                        new BufferedInputStream(
                                                new FileInputStream(lRovFile)));
                                ZipEntry entry;
                                while ((entry = in.getNextEntry()) != null) {
                                    int count;
                                    byte data[] = new byte[1000];

                                    // write the files to the disk
                                    out = new BufferedOutputStream(
                                            new FileOutputStream(lTempUnzippedRovFileFolder.getPath() + "/" + entry.getName()), 1000);

                                    while ((count = in.read(data, 0, 1000)) != -1) {
                                        out.write(data, 0, count);
                                    }
                                    out.flush();
                                    out.close();
                                }
                                in.close();
                            }

                            // Ok, all files should have been unzipped  in the lTempUnzippedRovFileFolder by now.
                            // Try to find the distiller xml file..

                            File[] lUnzippedRovFiles = lTempUnzippedRovFileFolder.listFiles();
                            for (int j = 0; j < lUnzippedRovFiles.length; j++) {
                                File lUnzippedRovFile = lUnzippedRovFiles[j];
                                if (lUnzippedRovFile.getName().toLowerCase().indexOf("mdro_search_status+1") != -1) {
                                    lDatFile = lUnzippedRovFile;
                                }
                            }

                            if (lDatFile == null) {
                                // If we get here, it means that the mascot result file was not found in the unzipped directory!
                                iParent.passHotPotato(new Throwable("Peptide identification file was not found in the Distiller project file '" + lRovFile.getName() + "'!"));
                                return null;
                            } else {

                                //we found the dat file and have the mgf file
                                lDatFiles.add(new DatFile(lDatFile, lMgfFiles.get(i), iFlamable));
                            }
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }

                }

                //update progress bar
                progressBar.setIndeterminate(true);
                //Show in the gui that we found all the rov files
                setIconOnPanel(jpanCheck, "apply.png", 3, 1);

                //set id done
                lblId.setEnabled(true);
                setIconOnPanel(jpanId, "apply.png", 3, 2);
                //set quant done
                lblQuant.setEnabled(true);
                setIconOnPanel(jpanQuant, "apply.png", 3, 3);

                //3.match ids to quantitations

                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(true);
                progressBar.setIndeterminate(false);
                progressBar.setMaximum(lDatFiles.size() + 1);
                progressBar.setValue(0);

                //set match loading
                lblMatch.setEnabled(true);
                setIconOnPanel(jpanMatch, "clock.png", 3, 4);

                Vector<RatioGroupCollection> lRatioGroupCollection = new Vector<RatioGroupCollection>();
                for (int i = 0; i < lDatFiles.size(); i++) {

                    //update progress bar
                    progressBar.setValue(progressBar.getValue() + 1);
                    progressBar.setString("Matching identification with ratios from dat file number " + (i + 1) + " of " + lDatFiles.size() + " mascot result files");

                    lRatioGroupCollection.add(lDatFiles.get(i).getITraqRatioGroupCollection(iParent, iParent.getThreshold()));

                    //_____Do garbage collection______
                    System.gc();
                }

                //update progress bar
                progressBar.setString("");
                progressBar.setStringPainted(false);
                progressBar.setIndeterminate(true);

                //set match done
                setIconOnPanel(jpanMatch, "apply.png", 3, 4);
                //set create loading
                lblCreate.setEnabled(true);
                setIconOnPanel(jpanCreate, "clock.png", 3, 5);

                //6. get all the protein accessions from the identifications
                Vector<String> lProteinAccessions = new Vector<String>();
                for (int i = 0; i < lRatioGroupCollection.size(); i++) {
                    if (i == 0) {
                        iQuantitativeValidationSingelton.setRatioTypes(lRatioGroupCollection.get(i).getRatioTypes());
                        iQuantitativeValidationSingelton.setComponentTypes(lRatioGroupCollection.get(i).getComponentTypes());
                    }
                    lRatioGroupCollection.get(i).setRoverSource(iParent.getRoverSource());
                    for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                        RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);

                        String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                            //check if it's a new accession
                            boolean lNewAccession = true;
                            for (int l = 0; l < lProteinAccessions.size(); l++) {
                                if (lProteinAccessions.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                                    lNewAccession = false;
                                }
                            }
                            if (lNewAccession) {
                                lProteinAccessions.add(lAccessionsForRatioGroup[k]);
                            }
                        }
                    }
                }

                if (lRatioGroupCollection.size() == 0) {
                    //show gui
                    JOptionPane.showMessageDialog(iParent, "No quantitative data could be found!\n The program will close.", "INFO", JOptionPane.INFORMATION_MESSAGE);
                    iParent.close();
                }

                //7.A get the types of the ratios from the first distiller ratio collecion
                Vector<String> lRatioList = lRatioGroupCollection.get(0).getRatioTypes();
                String[] lRatioTypes = new String[lRatioList.size()];
                lRatioList.toArray(lRatioTypes);

                //7.B get the types of the ratios from the first distiller ratio collecion
                Vector<String> lComponentList = lRatioGroupCollection.get(0).getComponentTypes();
                String[] lComponentTypes = new String[lComponentList.size()];
                lComponentList.toArray(lComponentTypes);


                //8. create all the distiller proteins
                Vector<QuantitativeProtein> lProteins = new Vector<QuantitativeProtein>();
                for (int i = 0; i < lProteinAccessions.size(); i++) {
                    lProteins.add(new QuantitativeProtein(lProteinAccessions.get(i), lRatioTypes));
                }


                //9. couple the distiller ratio groups to the distiller proteins
                for (int i = 0; i < lRatioGroupCollection.size(); i++) {
                    for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                        //get the ratio group
                        RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);
                        //get all the protein accession linked to this ratiogroup
                        String[] lAccessions = lRatioGroup.getProteinAccessions();
                        for (int k = 0; k < lAccessions.length; k++) {
                            for (int l = 0; l < lProteins.size(); l++) {
                                if (lAccessions[k].equalsIgnoreCase(lProteins.get(l).getAccession())) {
                                    //add the ratio group to the protein if the accession is the same
                                    lProteins.get(l).addRatioGroup(lRatioGroup);
                                }
                            }
                        }
                    }
                }
                calculateRazorPeptides(lProteins);
                iQuantitativeValidationSingelton.setAllProteins(lProteins);

                //10. create a reference set with the "household" proteins with the most ratiogroups
                ReferenceSet lReferenceSet = new ReferenceSet(new ArrayList<QuantitativeProtein>(), lRatioTypes, lComponentTypes);

                MatchRatioWithComponent lMatch = new MatchRatioWithComponent(true);
                while (lRatioTypes.length > iQuantitativeValidationSingelton.getMatchedRatioTypes().size()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //sleep failed
                    }
                }


                //sort by the ratio group numbers
                Collections.sort(lProteins, new QuantitativeProteinSorterByRatioGroupNumbers());

                //get the reference set size from the singelton
                int lReferenceSetSize = iQuantitativeValidationSingelton.getNumberOfProteinsInReferenceSet();
                if (iQuantitativeValidationSingelton.getUseAllProteinsForReferenceSet()) {
                    lReferenceSetSize = lProteins.size();
                }
                for (int i = 0; i < lReferenceSetSize; i++) {
                    lReferenceSet.addReferenceProtein(lProteins.get(i));
                }
                iQuantitativeValidationSingelton.setReferenceSet(lReferenceSet);
                //lReferenceSet.calculateStatisticsByRandomSampling();

                //Show in the gui that the creation of the proteins is done
                setIconOnPanel(jpanCreate, "apply.png", 3, 5);

                //download the protein sequence
                lblDownload.setEnabled(true);
                setIconOnPanel(jpanDownload, "clock.png", 3, 6);
                downloadProteinSequences(lProteins);
                setIconOnPanel(jpanDownload, "apply.png", 3, 6);

                //sort by the protein accession
                Collections.sort(lProteins, new QuantitativeProteinSorterByAccession());

                //_____Do garbage collection______
                System.gc();

                //show gui
                JOptionPane.showMessageDialog(iParent, "All the data is loaded, ready to validate!", "INFO", JOptionPane.INFORMATION_MESSAGE);
                QuantitationValidationGUI gui = new QuantitationValidationGUI(lProteins, null, iParent.isStandAlone());
                gui.setVisible(true);
                return true;
            }

            public void finished() {
                //
                iParent.closeFrame();
            }

        };
        lStarter.start();

    }

    /**
     * This method will calculate the razor accession for every ratiogroup linked to the given proteins
     *
     * @param aProteins
     */
    public void calculateRazorPeptides(Vector<QuantitativeProtein> aProteins) {
        //create a hashmap with the protein accession and the number of peptide groups linked to the protein
        HashMap lProteinsPeptideNumber = new HashMap();
        HashMap lProteinsIdentificationNumber = new HashMap();
        for (int i = 0; i < aProteins.size(); i++) {
            lProteinsPeptideNumber.put(aProteins.get(i).getAccession().trim(), aProteins.get(i).getNumberOfPeptideGroups());
            lProteinsIdentificationNumber.put(aProteins.get(i).getAccession().trim(), aProteins.get(i).getNumberOfIdentifications());
        }
        //we will get the razor accession for every ratio group
        for (int i = 0; i < aProteins.size(); i++) {
            for (int j = 0; j < aProteins.get(i).getPeptideGroups(false).size(); j++) {
                for (int k = 0; k < aProteins.get(i).getPeptideGroups(false).get(j).getRatioGroups().size(); k++) {
                    RatioGroup lRatioGroup = aProteins.get(i).getPeptideGroups(false).get(j).getRatioGroups().get(k);
                    if (lRatioGroup.getRazorProteinAccession() == null) {
                        //the razor accession in not set yet
                        int lPeptideGroupsLinked = 0;
                        int lIdentficationsLinked = 0;
                        String lRazorAccession = null;
                        for (int l = 0; l < lRatioGroup.getProteinAccessions().length; l++) {
                            if (lPeptideGroupsLinked < (Integer) lProteinsPeptideNumber.get(lRatioGroup.getProteinAccessions()[l].trim())) {
                                lRazorAccession = lRatioGroup.getProteinAccessions()[l].trim();
                                lPeptideGroupsLinked = (Integer) lProteinsPeptideNumber.get(lRatioGroup.getProteinAccessions()[l].trim());
                                lIdentficationsLinked = (Integer) lProteinsIdentificationNumber.get(lRatioGroup.getProteinAccessions()[l].trim());
                            } else if (lPeptideGroupsLinked == (Integer) lProteinsPeptideNumber.get(lRatioGroup.getProteinAccessions()[l].trim())) {
                                if (lIdentficationsLinked < (Integer) lProteinsIdentificationNumber.get(lRatioGroup.getProteinAccessions()[l].trim())) {
                                    lRazorAccession = lRatioGroup.getProteinAccessions()[l].trim();
                                    lPeptideGroupsLinked = (Integer) lProteinsPeptideNumber.get(lRatioGroup.getProteinAccessions()[l].trim());
                                    lIdentficationsLinked = (Integer) lProteinsIdentificationNumber.get(lRatioGroup.getProteinAccessions()[l].trim());
                                }
                            }
                        }
                        lRatioGroup.setRazorProteinAccession(lRazorAccession);
                    }
                }
            }
        }


    }


    public void doNormalization(Vector<QuantitativeProtein> lProteins, Vector<String> lTypes) {
        iQuantitativeValidationSingelton.setNormalization(true);

        //ToDo do normalization

        for (int i = 0; i < lTypes.size(); i++) {

            String lUnregulatedComponent = null;
            String lRatioTypeTitle = null;
            RatioType lRatioType = null;

            Vector<RatioType> lRatioTypes = iQuantitativeValidationSingelton.getMatchedRatioTypes();
            for (int j = 0; j < lRatioTypes.size(); j++) {
                if (lRatioTypes.get(j).getType().equalsIgnoreCase(lTypes.get(i))) {
                    lUnregulatedComponent = lRatioTypes.get(j).getUnregulatedComponent();
                    lRatioTypeTitle = lRatioTypes.get(j).getType();
                    lRatioType = lRatioTypes.get(j);
                }
            }

            double lStDevOld = Double.MAX_VALUE;
            double lCoefVarOld = Double.MAX_VALUE;
            double lStDevNew = 0.0;
            double lCoefVarNew = 0.0;
            double lCoefVarDiff = 1.0;
            int lCycles = 0;
            Vector<Vector<Double>> lAllCalculatedMADs = new Vector<Vector<Double>>();

            //get the ratios
            Vector<Ratio> lValidUniqueRatios = new Vector<Ratio>();
            Vector<Ratio> lAllRatios = new Vector<Ratio>();
            Vector<Boolean> lAllRatiosTakenForNormalizationVector = new Vector<Boolean>();

            for (int j = 0; j < lProteins.size(); j++) {
                for (int k = 0; k < lProteins.get(j).getRatioGroups().size(); k++) {
                    Ratio lRatio = lProteins.get(j).getRatioGroups().get(k).getRatioByType(lTypes.get(i));
                    if (lRatio != null) {
                        if (!lAllRatios.contains(lRatio)) {
                            lAllRatios.add(lRatio);
                            lAllRatiosTakenForNormalizationVector.add(false);
                        }
                        if (lRatio.getValid()) {
                            if (!lValidUniqueRatios.contains(lRatio)) {
                                lValidUniqueRatios.add(lRatio);
                            }
                        }
                    }
                }
            }


            //sort the ratios by intesities
            RatioSorterByIntensity lSorter = new RatioSorterByIntensity(lUnregulatedComponent, lRatioTypeTitle);
            Collections.sort(lAllRatios, lSorter);
            Collections.sort(lValidUniqueRatios, lSorter);
            try {

                //Calendar now = Calendar.getInstance();
                //PrintWriter out = new PrintWriter(new FileWriter("C:\\" + "roverOut" + now.getTimeInMillis() + ".csv"));
                //out.println("^    Normalization cycles     ^     Old MAD Sd^Old MAD mean     ^     Old Coef of var    ^     New MAD SD    ^    New MAD mean     ^    New coef of var     ^");

                //while (lCycles < 10) {
                while (lCoefVarDiff > 0.0005) {

                    if (lCycles == 0) {
                        //only do a normalization if its the first cycle
                        //calculate log 2 median
                        double[] lRatios = new double[lValidUniqueRatios.size()];
                        for (int m = 0; m < lValidUniqueRatios.size(); m++) {
                            lRatios[m] = lValidUniqueRatios.get(m).getRatio(true);
                        }
                        double lMedian = BasicStats.median(lRatios, false);
                        //calculate the wanted log 2 median
                        double lWantedMedian = lRatioType.getMedian();
                        lWantedMedian = Math.log(lWantedMedian) / Math.log(2);

                        //use this median to correct every ratio
                        for (int m = 0; m < lAllRatios.size(); m++) {
                            Ratio lRatio = lAllRatios.get(m);
                            double lRatioValue = lRatio.getRatio(true);
                            lRatioValue = lRatioValue + (lWantedMedian - lMedian);
                            lRatio.setRecalculatedRatio(lRatioValue);
                        }

                    }

                    for (int p = 0; p < lAllRatiosTakenForNormalizationVector.size(); p++) {
                        lAllRatiosTakenForNormalizationVector.set(p, false);
                    }


                    //Create the holders for the divided ratio vectors
                    Vector<Vector<Ratio>> lDividedValidUniqueRatios = new Vector<Vector<Ratio>>();
                    Vector<Vector<Ratio>> lDividedValidUniqueSlidingWindowRatios = new Vector<Vector<Ratio>>();
                    Vector<Double> lDividedValidUniqueMADS = new Vector<Double>();
                    Vector<Double> lScalingFactor = new Vector<Double>();
                    Vector<Vector<Ratio>> lDividedAllRatios = new Vector<Vector<Ratio>>();
                    DescriptiveStatistics lOldMADs = new DescriptiveStatistics();
                    DescriptiveStatistics lNewMADs = new DescriptiveStatistics();
                    Vector<Double> lIntensities = new Vector<Double>();

                    //calculate the size of each of the 20 groups
                    int lHalfGroupSize = 200;
                    if (lCycles != 0) {
                        lHalfGroupSize = (int) (lHalfGroupSize * ((lCycles) / 0.6));
                    }
                    int lJumpFactor = 50;
                    int lGroupSize = lHalfGroupSize * 2 + lJumpFactor;
                    int lNumberOfGroups = (int) (lValidUniqueRatios.size() / Double.valueOf(lJumpFactor));
                    int lLastIndexAdded = -1;
                    int lMaximumIndexToAdd = 0;

                    //divide the ratios in to groups
                    Vector<Double> lGroupMedian = new Vector<Double>();
                    Vector<Double> lSlidingGroupMedian = new Vector<Double>();
                    double lUpperLast = Double.MIN_VALUE;
                    for (int j = 0; j < lNumberOfGroups; j++) {
                        Vector<Ratio> lTempRatiosSlidingGroupUnique = new Vector<Ratio>();
                        Vector<Ratio> lTempRatiosSmallGroupUnique = new Vector<Ratio>();
                        Vector<Ratio> lTempRatiosAll = new Vector<Ratio>();
                        for (int k = 0; k < lGroupSize; k++) {
                            int lIndex = k + (j * lJumpFactor) - ((lGroupSize - lJumpFactor) / 2);
                            if (lIndex >= 0 && lIndex < lValidUniqueRatios.size()) {
                                lTempRatiosSlidingGroupUnique.add(lValidUniqueRatios.get(lIndex));
                            }
                            if (k >= ((lGroupSize - lJumpFactor) / 2) && k < ((lGroupSize - lJumpFactor) / 2) + lJumpFactor) {
                                lTempRatiosSmallGroupUnique.add(lValidUniqueRatios.get(lIndex));
                                lTempRatiosAll.add(lValidUniqueRatios.get(lIndex));
                                lAllRatiosTakenForNormalizationVector.set(lIndex, true);
                                lMaximumIndexToAdd = lAllRatios.indexOf(lValidUniqueRatios.get(lIndex));
                            }
                        }
                        //we sorted the valid unique ratio, now we want to use the lower and upper intensities to create a subset of all ratios
                        Ratio o2 = lTempRatiosSmallGroupUnique.get(lTempRatiosSmallGroupUnique.size() - 1);

                        double lUpper = o2.getParentRatioGroup().getIntensityForComponent(lUnregulatedComponent);
                        if (lUpper == 0.0) {
                            lUpper = o2.getParentRatioGroup().getSummedIntensityForRatioType(lRatioTypeTitle);
                        }
                        lIntensities.add(lUpper);
                        boolean lAddingStarted = false;
                        if (j == lNumberOfGroups - 1) {
                            lUpper = Double.MAX_VALUE;
                            lMaximumIndexToAdd = lAllRatios.size() - 1;
                        }
                        for (int k = lLastIndexAdded + 1; k < lMaximumIndexToAdd + 1; k++) {
                            if (!lAllRatiosTakenForNormalizationVector.get(k)) {
                                double lInt = lAllRatios.get(k).getParentRatioGroup().getIntensityForComponent(lUnregulatedComponent);
                                if (lInt == 0.0) {
                                    lInt = lAllRatios.get(k).getParentRatioGroup().getSummedIntensityForRatioType(lRatioTypeTitle);
                                }
                                if (lUpperLast <= lInt && lInt < lUpper) {
                                    if (!lTempRatiosAll.contains(lAllRatios.get(k))) {
                                        //it's not yet added anywhere
                                        lTempRatiosAll.add(lAllRatios.get(k));
                                        lAllRatiosTakenForNormalizationVector.set(k, true);
                                        lAddingStarted = true;
                                        lLastIndexAdded = k;
                                    }
                                } else {
                                    //if we already added some things and now not anymore, we can stop this loop
                                    if (lAddingStarted) {
                                        k = lAllRatios.size();
                                    }
                                }
                            }
                        }
                        lUpperLast = lUpper;
                        lLastIndexAdded = lAllRatios.indexOf(lTempRatiosAll.get(lTempRatiosAll.size() - 1));

                        lDividedAllRatios.add(lTempRatiosAll);
                        lDividedValidUniqueRatios.add(lTempRatiosSmallGroupUnique);
                        lDividedValidUniqueSlidingWindowRatios.add(lTempRatiosSlidingGroupUnique);
                        double lMad = calculateMAD(lTempRatiosSlidingGroupUnique);
                        lOldMADs.addValue(lMad);
                        lDividedValidUniqueMADS.add(lMad);
                        lGroupMedian.add(calculateMedian(lTempRatiosSmallGroupUnique));
                        lSlidingGroupMedian.add(calculateMedian(lTempRatiosSlidingGroupUnique));
                    }
                    if (lCycles == 0) {
                        lAllCalculatedMADs.add(lIntensities);
                        lAllCalculatedMADs.add(lGroupMedian);
                        lAllCalculatedMADs.add(lSlidingGroupMedian);
                        lAllCalculatedMADs.add(lDividedValidUniqueMADS);
                    }

                    //calculate the scaling factor
                    double lRootedMADProduct = 0.0;
                    double lTempProduct = 1.0;
                    double lGroupCounts = (double) lDividedValidUniqueMADS.size();
                    for (int j = 0; j < lDividedValidUniqueMADS.size(); j++) {
                        if (lDividedValidUniqueMADS.get(j) != 0.0) {
                            lTempProduct = lTempProduct * Math.pow(lDividedValidUniqueMADS.get(j), (1.0 / lGroupCounts));
                        }
                    }

                    lRootedMADProduct = lTempProduct;


                    lScalingFactor = new Vector<Double>();
                    for (int j = 0; j < lDividedValidUniqueMADS.size(); j++) {
                        if (lDividedValidUniqueMADS.get(j) != 0.0) {
                            lScalingFactor.add(lDividedValidUniqueMADS.get(j) / lRootedMADProduct);
                        } else {
                            lScalingFactor.add(1.0);
                        }
                    }

                    //use the new scaling factors
                    for (int j = 0; j < lScalingFactor.size(); j++) {
                        for (int k = 0; k < lDividedAllRatios.get(j).size(); k++) {
                            Ratio lRatio = lDividedAllRatios.get(j).get(k);
                            double lRatioValue = lRatio.getRatio(true);
                            lRatioValue = lRatioValue / lScalingFactor.get(j);
                            lRatio.setRecalculatedRatio(lRatioValue);
                            lRatio.setNormalizationPart(j);
                            if (lCycles == 0) {
                                lRatio.setPreNormalizedMAD(lDividedValidUniqueMADS.get(j));
                            }
                        }
                    }
                    //calculate the new MADs
                    Vector<Double> lTempMADs = new Vector<Double>();
                    Vector<Double> lTempGroupMedians = new Vector<Double>();
                    Vector<Double> lTempSlidingMedians = new Vector<Double>();
                    for (int j = 0; j < lScalingFactor.size(); j++) {
                        double lNewMAD = 0.0;
                        lNewMAD = calculateMAD(lDividedValidUniqueSlidingWindowRatios.get(j));
                        lNewMADs.addValue(lNewMAD);
                        lTempMADs.add(lNewMAD);
                        lTempGroupMedians.add(calculateMedian(lDividedValidUniqueRatios.get(j)));
                        lTempSlidingMedians.add(calculateMedian(lDividedValidUniqueSlidingWindowRatios.get(j)));
                        for (int k = 0; k < lDividedAllRatios.get(j).size(); k++) {
                            Ratio lRatio = lDividedAllRatios.get(j).get(k);
                            lRatio.setNormalizedMAD(lNewMAD);
                        }
                    }

                    lStDevNew = lNewMADs.getStandardDeviation();
                    lStDevOld = lOldMADs.getStandardDeviation();


                    lCoefVarNew = Math.abs(lStDevNew / lNewMADs.getMean());
                    lCoefVarOld = Math.abs(lStDevOld / lOldMADs.getMean());
                    lCoefVarDiff = Math.abs(lCoefVarNew - lCoefVarOld);


                    //out.println("|   " + (lCycles + 1) + "   |   " + lStDevOld + "   |   " + lOldMADs.getMean() + "   |   " + lCoefVarOld + "   |   " + lStDevNew + "   |   " + lNewMADs.getMean() + "   |   " + lCoefVarNew + "   |");
                    lAllCalculatedMADs.add(lTempMADs);
                    lAllCalculatedMADs.add(lTempGroupMedians);
                    lAllCalculatedMADs.add(lTempSlidingMedians);
                    lCycles = lCycles + 1;
                }


                /*for (int y = 0; y < lAllCalculatedMADs.get(0).size(); y++) {
                    //System.out.print("\n");
                    out.print("\n");
                    for (int x = 0; x < lAllCalculatedMADs.size(); x++) {
                        //System.out.print(lAllCalculatedMADs.get(x).get(y) + ",");
                        out.print(lAllCalculatedMADs.get(x).get(y) + ",");
                    }
                }
                out.flush();
                out.close(); */
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }


    public double calculateMAD(Vector<Ratio> lRatios) {
        double[] lRatioDoubles = new double[lRatios.size()];
        for (int i = 0; i < lRatios.size(); i++) {
            lRatioDoubles[i] = lRatios.get(i).getRatio(true);
        }
        return BasicStats.mad(lRatioDoubles, false);
    }


    public double calculateMADFromRatios(Vector<Double> lRatios) {
        double[] lRatioDoubles = new double[lRatios.size()];
        for (int i = 0; i < lRatios.size(); i++) {
            lRatioDoubles[i] = lRatios.get(i);
        }
        return BasicStats.mad(lRatioDoubles, false);
    }

    public double calculateMedian(Vector<Ratio> lRatios) {
        double[] lRatioDoubles = new double[lRatios.size()];
        for (int i = 0; i < lRatios.size(); i++) {
            lRatioDoubles[i] = lRatios.get(i).getRatio(true);
        }
        return BasicStats.median(lRatioDoubles, false);
    }

    /**
     * This method will download all the protein sequences for the given proteins
     *
     * @param aProteins
     */
    public void downloadProteinSequences(Vector<QuantitativeProtein> aProteins) {
        progressBar.setString("Downloading protein sequences");
        progressBar.setStringPainted(true);
        progressBar.setMaximum(aProteins.size());
        progressBar.setIndeterminate(false);
        Vector<QuantitativeProtein> lProteins = (Vector<QuantitativeProtein>) aProteins.clone();
        if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.LOCAL)) {
            progressBar.setString("Finding protein sequences");
            //find the sequences in the fasta file
            try {
                //create the reader
                FileReader fDbReader = new FileReader(iQuantitativeValidationSingelton.getFastaDatabaseLocation());
                //create the line reader
                LineNumberReader lnreader = new LineNumberReader(fDbReader);
                //the current line
                String lLine = "";
                //create the header and the sequence strings
                String lHeader = "";
                String lSequence = "";
                int lCounter = 0;

                while ((lLine = lnreader.readLine()) != null) {
                    lCounter = lCounter + 1;
                    if (lLine.startsWith(">")) {
                        //find the previous one
                        if (lHeader.length() != 0 && lSequence.length() != 0) {
                            for (int i = 0; i < lProteins.size(); i++) {
                                if (lHeader.indexOf(lProteins.get(i).getAccession()) >= 0) {
                                    progressBar.setValue(progressBar.getValue() + 1);
                                    QuantitativeProtein lProtein = lProteins.get(i);
                                    lProtein.setSequence(lSequence);
                                    lProtein.setSequenceLength(lSequence.length());
                                    lProtein.getPeptideGroups(true);
                                    lProteins.remove(lProtein);
                                    i = lProteins.size();
                                }
                            }
                        }
                        lSequence = "";
                        lHeader = lLine;
                    } else if (!lLine.equalsIgnoreCase("\n")) {
                        lSequence = lSequence + lLine.replace("\n", "");
                    }
                }


            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

        } else {
            //download the sequences
            for (int i = 0; i < aProteins.size(); i++) {
                progressBar.setValue(progressBar.getValue() + 1);
                QuantitativeProtein lProtein = aProteins.get(i);

                try {
                    if (lProtein.getSequence() == null) {
                        if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.UNIPROT)) {
                            lProtein.setSequence((new UniprotSequenceRetriever(lProtein.getAccession())).getSequence());
                            lProtein.getPeptideGroups(true);
                            lProtein.setSequenceLength(lProtein.getSequence().length());
                            lProtein.setSequence("");
                        } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.IPI)) {
                            lProtein.setSequence((new IpiSequenceRetriever(lProtein.getAccession())).getSequence());
                            lProtein.getPeptideGroups(true);
                            lProtein.setSequenceLength(lProtein.getSequence().length());
                            lProtein.setSequence("");
                        } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.NCBI)) {
                            lProtein.setSequence((new NcbiSequenceRetriever(lProtein.getAccession())).getSequence());
                            lProtein.getPeptideGroups(true);
                            lProtein.setSequenceLength(lProtein.getSequence().length());
                            lProtein.setSequence("");
                        } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.TAIR)) {
                            lProtein.setSequence((new TairSequenceRetriever(lProtein.getAccession())).getSequence());
                            lProtein.getPeptideGroups(true);
                            lProtein.setSequenceLength(lProtein.getSequence().length());
                            lProtein.setSequence("");
                        } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.MIPS_CYGD)) {
                            lProtein.setSequence((new MipsCygdSequenceRetriever(lProtein.getAccession())).getSequence());
                            lProtein.getPeptideGroups(true);
                            lProtein.setSequenceLength(lProtein.getSequence().length());
                            lProtein.setSequence("");
                        }
                    }
                } catch (Exception e) {
                    //sequence not found
                    //e.printStackTrace();
                }
            }
        }

    }


    private void createUIComponents() {
        jpanCheck = new JPanel();
        jpanId = new JPanel();
        jpanQuant = new JPanel();
        jpanMatch = new JPanel();
        jpanCreate = new JPanel();
        jpanDownload = new JPanel();
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
        lblCheckFiles = new JLabel();
        lblCheckFiles.setEnabled(false);
        lblCheckFiles.setFont(new Font("Tahoma", lblCheckFiles.getFont().getStyle(), lblCheckFiles.getFont().getSize()));
        lblCheckFiles.setText("Checking files");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 5);
        jpanContent.add(lblCheckFiles, gbc);
        lblId = new JLabel();
        lblId.setEnabled(false);
        lblId.setFont(new Font("Tahoma", lblId.getFont().getStyle(), lblId.getFont().getSize()));
        lblId.setText("Loading identifications");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 5);
        jpanContent.add(lblId, gbc);
        lblQuant = new JLabel();
        lblQuant.setEnabled(false);
        lblQuant.setFont(new Font("Tahoma", lblQuant.getFont().getStyle(), lblQuant.getFont().getSize()));
        lblQuant.setText("Loading quantitations");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 5);
        jpanContent.add(lblQuant, gbc);
        lblMatch = new JLabel();
        lblMatch.setEnabled(false);
        lblMatch.setFont(new Font("Tahoma", lblMatch.getFont().getStyle(), lblMatch.getFont().getSize()));
        lblMatch.setText("Matching identifications and quantitations");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 5);
        jpanContent.add(lblMatch, gbc);
        lblCreate = new JLabel();
        lblCreate.setEnabled(false);
        lblCreate.setFont(new Font("Tahoma", lblCreate.getFont().getStyle(), lblCreate.getFont().getSize()));
        lblCreate.setText("Creating proteins and reference set");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 5);
        jpanContent.add(lblCreate, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(jpanCheck, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(jpanId, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(jpanQuant, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(jpanMatch, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(jpanCreate, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer4, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer5, gbc);
        progressBar = new JProgressBar();
        progressBar.setFont(new Font("Tahoma", progressBar.getFont().getStyle(), progressBar.getFont().getSize()));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(progressBar, gbc);
        lblDownload = new JLabel();
        lblDownload.setEnabled(false);
        lblDownload.setFont(new Font("Tahoma", lblDownload.getFont().getStyle(), lblDownload.getFont().getSize()));
        lblDownload.setText("Downloading protein sequence");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 5);
        jpanContent.add(lblDownload, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(jpanDownload, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer6, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }


    /**
     * This JPanel extension makes it easy to load an image on a panel
     */
    class ImagePanel extends JPanel {

        private Image img;

        public ImagePanel(String img) {
            this(new ImageIcon(img).getImage());
        }

        public ImagePanel(Image img) {
            this.img = img;
            Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
            setSize(size);
            setLayout(null);
        }

        public void paintComponent(Graphics g) {
            g.drawImage(img, 0, 0, null);
        }

    }


}
