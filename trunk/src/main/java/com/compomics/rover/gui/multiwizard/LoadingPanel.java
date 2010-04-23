package com.compomics.rover.gui.multiwizard;

import org.apache.log4j.Logger;

import com.compomics.rover.general.interfaces.WizardPanel;
import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.db.accessors.IdentificationExtension;
import com.compomics.rover.general.db.accessors.QuantitationExtension;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.QuantitativeProtein;
import com.compomics.rover.general.quantitation.RatioGroup;
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
import com.compomics.rover.general.sequenceretriever.UniprotSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.IpiSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.NcbiSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.TairSequenceRetriever;
import com.compomics.rover.gui.multiwizard.WizardFrameHolder;
import com.compomics.util.interfaces.Flamable;
import com.compomics.util.sun.SwingWorker;
import com.compomics.mslims.db.accessors.Identification_to_quantitation;
import com.compomics.mslims.db.accessors.Quantitation_file;

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
    private JPanel jpanContent;
    private JLabel lblId;
    private JLabel lblQuant;
    private JLabel lblMatch;
    private JLabel lblSet;

    private Vector<RatioGroupCollection> iCollection = new Vector<RatioGroupCollection>();
    private Vector<Integer> iCollectionIndexes = new Vector<Integer>();
    private Vector<String[]> iCollectionRatios = new Vector<String[]>();
    private Vector<String[]> iCollectionComponents = new Vector<String[]>();
    private Vector<String> iProteinAccession = new Vector<String>();


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
        setIconOnPanel(jpanCheck, "empty.png", 3, 2);
        setIconOnPanel(jpanId, "empty.png", 3, 3);
        setIconOnPanel(jpanQuant, "empty.png", 3, 4);
        setIconOnPanel(jpanMatch, "empty.png", 3, 5);
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
        iParent.setNextButtonEnabled(false);
        if (iParent.getUseMs_lims()) {
            //we are not using ms_lims, not files
            //do not show the file check lbl and panel
            lblCheckFiles.setVisible(false);
            jpanCheck.setVisible(false);
        }

        iQuantitativeValidationSingelton.setOriginalRoverSources(iParent.getRoverSources());
        iQuantitativeValidationSingelton.setTitles(iParent.getTitles());
        iQuantitativeValidationSingelton.setRoverSources(iParent.getRoverSources());

        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                Vector<RoverSource> lSources = iParent.getRoverSources();
                for (int i = 0; i < lSources.size(); i++) {
                    lblSet.setText(" Analyzing data set: " + iParent.getTitle(i) + " ");
                    if (lSources.get(i) == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                        startFileRov(i);
                    } else if (lSources.get(i) == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS) {
                        startMs_limsRov(i);
                    } else if (lSources.get(i) == RoverSource.ITRAQ_DAT) {
                        startFileDat(i);
                    } else if (lSources.get(i) == RoverSource.ITRAQ_MS_LIMS) {
                        startMs_limsiTraq(i);
                    } else if (lSources.get(i) == RoverSource.ITRAQ_ROV) {
                        startITraqRov(i);
                    } else if (lSources.get(i) == RoverSource.MS_QUANT) {
                        startMsQuant(i);
                    } else if (lSources.get(i) == RoverSource.MAX_QUANT || lSources.get(i) == RoverSource.MAX_QUANT_NO_SIGN) {
                        startMaxQuant(i);
                    } else if (lSources.get(i) == RoverSource.CENSUS) {
                        startCensus(i);
                    } else if (lSources.get(i) == RoverSource.TMT_DAT) {
                        startFileDat(i);
                    }
                }

                iParent.setCollectionsRatios(iCollectionRatios);
                iParent.setCollectionsComponents(iCollectionComponents);
                iParent.setCollections(iCollection);
                iQuantitativeValidationSingelton.setOriginalCollections(iCollection);
                iParent.setProteinAccessions(iProteinAccession);

                for (int i = 0; i < iCollection.size(); i++) {
                    iCollection.get(i).setIndex(iCollectionIndexes.get(i));
                    iCollection.get(i).setRoverSource(iParent.getRoverSource(iCollectionIndexes.get(i)));
                }
                iParent.setNextButtonEnabled(true);
                progressBar.setEnabled(false);
                iParent.clickNextButton();


                return true;
            }

            public void finished() {

            }

        };
        lStarter.start();


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
    public void startMs_limsiTraq(int aIndex) {

        final int iIndex = aIndex;
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
                    setIconOnPanel(jpanId, "clock.png", 3, 3);

                    //1. get IdentificationExtension for a specific project
                    IdentificationExtension[] lQuantPeptides;
                    lQuantPeptides = IdentificationExtension.getIdentificationExtensionsforProject(iParent.getMs_limsConnection(), iParent.getProject(iIndex).getProjectid(), null);
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
                    setIconOnPanel(jpanId, "apply.png", 3, 3);
                    //set quant loading
                    lblQuant.setEnabled(true);
                    setIconOnPanel(jpanQuant, "clock.png", 3, 4);

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

                        }
                        prepDat.close();
                        rsDat.close();


                        //_____Do garbage collection______
                        System.gc();
                    }

                    //_____Do garbage collection______
                    System.gc();
                    //update progress bar
                    progressBar.setString("");
                    progressBar.setIndeterminate(true);

                    //Show in the gui that we found all the rov files
                    setIconOnPanel(jpanQuant, "apply.png", 3, 4);
                    //set match  loading
                    lblMatch.setEnabled(true);
                    setIconOnPanel(jpanMatch, "clock.png", 3, 5);

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

                    }

                    //update progress bar
                    progressBar.setIndeterminate(true);
                    progressBar.setStringPainted(false);
                    progressBar.setString("");

                    //_____Do garbage collection______
                    System.gc();


                    //6. get all the protein accessions from the identifications
                    for (int i = 0; i < lRatioGroupCollection.length; i++) {
                        iCollection.add(lRatioGroupCollection[i]);
                        iCollectionIndexes.add(iIndex);
                        for (int j = 0; j < lRatioGroupCollection[i].size(); j++) {
                            RatioGroup lRatioGroup = lRatioGroupCollection[i].get(j);

                            String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                            for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                                //check if it's a new accession
                                boolean lNewAccession = true;
                                for (int l = 0; l < iProteinAccession.size(); l++) {
                                    if (iProteinAccession.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                                        lNewAccession = false;
                                    }
                                }
                                if (lNewAccession) {
                                    iProteinAccession.add(lAccessionsForRatioGroup[k]);
                                }

                            }
                        }

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
                    iCollectionRatios.add(lRatioTypes);

                    //7.B get the types of the ratios from the first distiller ratio collecion
                    Vector<String> lComponentList = lRatioGroupCollection[0].getComponentTypes();
                    String[] lComponentTypes = new String[lComponentList.size()];
                    lComponentList.toArray(lComponentTypes);
                    iCollectionComponents.add(lComponentTypes);

                    //Show in the gui that the matching is done
                    setIconOnPanel(jpanMatch, "apply.png", 3, 5);

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
            }

        };
        lStarter.start();
    }

    /**
     * This method start the data acquisition process for a mascot distiller quantitation toolbox ms_lims project
     */
    public void startMs_limsRov(int aIndex) {

        final int iIndex = aIndex;
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
                    setIconOnPanel(jpanId, "clock.png", 3, 3);

                    //1. get IdentificationExtension for a specific project
                    IdentificationExtension[] lQuantPeptides;
                    lQuantPeptides = IdentificationExtension.getIdentificationExtensionsforProject(iParent.getMs_limsConnection(), iParent.getProject(iIndex).getProjectid(), null);
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
                    setIconOnPanel(jpanId, "apply.png", 3, 3);
                    //set quant  loading
                    lblQuant.setEnabled(true);
                    setIconOnPanel(jpanQuant, "clock.png", 3, 4);

                    //3.get for all the QuantPeptides the original rov file name
                    ArrayList<Long> lRovFileIds = new ArrayList<Long>();
                    String query = "select i.identificationid, f.filename, f.quantitation_fileid from identification as i, identification_to_quantitation as t, quantitation as q, quantitation_file as f where i.identificationid in (" + lIdentificationsIds + ") and i.identificationid = t.l_identificationid and t.quantitation_link = q.quantitation_link and q.L_quantitation_fileid  = f.quantitation_fileid";
                    PreparedStatement ps = iParent.getMs_limsConnection().prepareStatement(query);
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
                    //4.1 create the temp/rover folder
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


                        //_____Do garbage collection______
                        System.gc();
                    }

                    //_____Do garbage collection______
                    System.gc();

                    //update progress bar
                    progressBar.setString("");
                    progressBar.setIndeterminate(true);

                    //Show in the gui that we found all the rov files
                    setIconOnPanel(jpanQuant, "apply.png", 3, 4);

                    //set match  loading
                    lblMatch.setEnabled(true);
                    setIconOnPanel(jpanMatch, "clock.png", 3, 5);

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


                    //6. get all the protein accessions from the identifications
                    for (int i = 0; i < lRatioGroupCollection.length; i++) {
                        iCollection.add(lRatioGroupCollection[i]);
                        iCollectionIndexes.add(iIndex);
                        for (int j = 0; j < lRatioGroupCollection[i].size(); j++) {
                            DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) lRatioGroupCollection[i].get(j);

                            if (lRatioGroup.getNumberOfIdentifications() != 0) {
                                String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                                for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                                    //check if it's a new accession
                                    boolean lNewAccession = true;
                                    for (int l = 0; l < iProteinAccession.size(); l++) {
                                        if (iProteinAccession.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                                            lNewAccession = false;
                                        }
                                    }
                                    if (lNewAccession) {
                                        iProteinAccession.add(lAccessionsForRatioGroup[k]);
                                    }

                                }
                            } else {
                                //System.out.println("No identification found");
                            }
                        }
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
                    iCollectionRatios.add(lRatioTypes);

                    //7.B get the types of the ratios from the first distiller ratio collecion
                    Vector<String> lComponentList = lRatioGroupCollection[0].getComponentTypes();
                    String[] lComponentTypes = new String[lComponentList.size()];
                    lComponentList.toArray(lComponentTypes);
                    iCollectionComponents.add(lComponentTypes);

                    //Show in the gui that the matching is done
                    setIconOnPanel(jpanMatch, "apply.png", 3, 5);


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
            }

        };
        lStarter.start();
    }

    /**
     * This method start the data acquisition process for a different mascot distiller quantitation toolbox files
     */
    public void startFileRov(int aIndex) {

        final int iIndex = aIndex;
        QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
        iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

        //1.unzip all the file and check if the quantitation and identification information can be found
        Vector<File> lFiles = iParent.getFiles(iIndex);
        Vector<RovFile> lRovFiles = new Vector<RovFile>();

        //update progress bar
        progressBar.setMaximum(lFiles.size() + 1);
        progressBar.setValue(0);
        progressBar.setString("");
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(false);
        //set check loading
        setIconOnPanel(jpanCheck, "clock.png", 3, 2);
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
        }
        //Show in the gui that we found all the rov files
        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(false);
        progressBar.setIndeterminate(true);
        setIconOnPanel(jpanCheck, "apply.png", 3, 2);

        //2.read the quantitation and the identification files

        //set id loading
        lblId.setEnabled(true);
        setIconOnPanel(jpanId, "clock.png", 3, 3);
        //set quant  loading
        lblQuant.setEnabled(true);
        setIconOnPanel(jpanQuant, "clock.png", 3, 4);

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
        }

        //_____Do garbage collection______
        System.gc();

        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(false);
        progressBar.setIndeterminate(true);

        //set id done
        setIconOnPanel(jpanId, "apply.png", 3, 3);
        //set quant done
        setIconOnPanel(jpanQuant, "apply.png", 3, 4);

        //3.match ids to quantitations

        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(lRovFiles.size() + 1);
        progressBar.setValue(0);

        //set match loading
        lblMatch.setEnabled(true);
        setIconOnPanel(jpanMatch, "clock.png", 3, 5);

        for (int i = 0; i < lRovFiles.size(); i++) {

            //update progress bar
            progressBar.setValue(progressBar.getValue() + 1);
            progressBar.setString("Matching identification with ratios from .rov file number " + (i + 1) + " of " + lRovFiles.size() + " distiller quantitation files");

            RovFile lRovFile = lRovFiles.get(i);
            lRovFile.setThreshold(1.0 - iParent.getThreshold(iIndex));
            lRovFile.match();
            iCollection.add(lRovFile.getRatioGroupCollection());
            iCollectionIndexes.add(iIndex);

            //_____Do garbage collection______
            System.gc();
        }

        //_____Do garbage collection______
        System.gc();

        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(false);
        progressBar.setIndeterminate(true);


        //6. get all the protein accessions from the identifications



        for (int i = 0; i < lRovFiles.size(); i++) {
            RovFile lRovFile = lRovFiles.get(i);

            for (int j = 0; j < lRovFile.getRatioGroupCollection().size(); j++) {
                DistillerRatioGroup lRatioGroup = (DistillerRatioGroup) lRovFile.getRatioGroupCollection().get(j);

                String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                    //check if it's a new accession
                    boolean lNewAccession = true;
                    for (int l = 0; l < iProteinAccession.size(); l++) {
                        if (iProteinAccession.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                            lNewAccession = false;
                        }
                    }
                    if (lNewAccession) {
                        iProteinAccession.add(lAccessionsForRatioGroup[k]);
                    }

                }
            }
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
        iCollectionRatios.add(lRatioTypes);

        //7.B get the types of the ratios from the first distiller ratio collecion
        Vector<String> lComponentList = lRovFileTemp.getRatioGroupCollection().getComponentTypes();
        String[] lComponentTypes = new String[lComponentList.size()];
        lComponentList.toArray(lComponentTypes);
        iCollectionComponents.add(lComponentTypes);

        //set match done
        setIconOnPanel(jpanMatch, "apply.png", 3, 5);


    }

    /**
     * This method start the data acquisition process for different dat files
     */
    public void startFileDat(int aIndex) {

        final int iIndex = aIndex;
        QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
        iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

        //1.check the datfiles
        Vector<File> lFiles = iParent.getFiles(iIndex);
        Vector<DatFile> lDatFiles = new Vector<DatFile>();

        //update progress bar
        progressBar.setMaximum(lFiles.size() + 1);
        progressBar.setValue(0);
        progressBar.setString("");
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(false);
        //set checking files
        lblCheckFiles.setEnabled(true);
        setIconOnPanel(jpanCheck, "clock.png", 3, 2);

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
        setIconOnPanel(jpanCheck, "apply.png", 3, 2);

        //set id loading
        lblId.setEnabled(true);
        setIconOnPanel(jpanId, "clock.png", 3, 3);
        //set quant loading
        lblQuant.setEnabled(true);
        setIconOnPanel(jpanQuant, "clock.png", 3, 4);

        //3.match ids to quantitations

        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(lDatFiles.size() + 1);
        progressBar.setValue(0);

        //set id done
        setIconOnPanel(jpanId, "apply.png", 3, 3);
        //set quant done
        setIconOnPanel(jpanQuant, "apply.png", 3, 4);
        //set match loading
        lblMatch.setEnabled(true);
        setIconOnPanel(jpanMatch, "clock.png", 3, 5);

        Vector<RatioGroupCollection> lRatioGroupCollection = new Vector<RatioGroupCollection>();
        for (int i = 0; i < lDatFiles.size(); i++) {

            //update progress bar
            progressBar.setValue(progressBar.getValue() + 1);
            progressBar.setString("Matching identification with ratios from dat file number " + (i + 1) + " of " + lDatFiles.size() + " mascot result files");

            RatioGroupCollection lTemp = lDatFiles.get(i).getITraqRatioGroupCollection(iParent, iParent.getThreshold(iIndex));
            if (lTemp != null) {
                iCollection.add(lDatFiles.get(i).getITraqRatioGroupCollection(iParent, iParent.getThreshold(iIndex)));
                iCollectionIndexes.add(iIndex);
                lRatioGroupCollection.add(lDatFiles.get(i).getITraqRatioGroupCollection(iParent, iParent.getThreshold(iIndex)));
            }

            //_____Do garbage collection______
            System.gc();
        }

        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(false);
        progressBar.setIndeterminate(true);



        //6. get all the protein accessions from the identifications
        for (int i = 0; i < lRatioGroupCollection.size(); i++) {

            for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);

                String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                    //check if it's a new accession
                    boolean lNewAccession = true;
                    for (int l = 0; l < iProteinAccession.size(); l++) {
                        if (iProteinAccession.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                            lNewAccession = false;
                        }
                    }
                    if (lNewAccession) {
                        iProteinAccession.add(lAccessionsForRatioGroup[k]);
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
        iCollectionRatios.add(lRatioTypes);

        //7.B get the types of the ratios from the first distiller ratio collecion
        Vector<String> lComponentList = lRatioGroupCollection.get(0).getComponentTypes();
        String[] lComponentTypes = new String[lComponentList.size()];
        lComponentList.toArray(lComponentTypes);
        iCollectionComponents.add(lComponentTypes);

        //set match done
        setIconOnPanel(jpanMatch, "apply.png", 3, 5);


    }

    /**
     * This method start the data acquisition process for MsQuant files
     */
    public void startMsQuant(int aIndex) {

        final int iIndex = aIndex;
        QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
        iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

        //1.check the datfiles
        Vector<File> lFiles = iParent.getFiles(iIndex);

        //update progress bar
        progressBar.setMaximum(lFiles.size() + 1);
        progressBar.setValue(0);
        progressBar.setString("");
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(false);
        //set checking files
        lblCheckFiles.setEnabled(true);
        setIconOnPanel(jpanCheck, "clock.png", 3, 2);

        //1.check the MsQuant files
        for (int i = 0; i < lFiles.size(); i++) {
            //update progress bar
            progressBar.setValue(progressBar.getValue() + 1);
            progressBar.setString("Checked " + lFiles.get(i).getName() + ".");
        }

        //update progress bar
        progressBar.setIndeterminate(true);
        //Show in the gui that we found all the rov files
        setIconOnPanel(jpanCheck, "apply.png", 3, 2);

        //set id loading
        lblId.setEnabled(true);
        setIconOnPanel(jpanId, "clock.png", 3, 3);
        //set quant loading
        lblQuant.setEnabled(true);
        setIconOnPanel(jpanQuant, "clock.png", 3, 4);

        //3.match ids to quantitations

        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(lFiles.size() + 1);
        progressBar.setValue(0);

        //set id done
        setIconOnPanel(jpanId, "apply.png", 3, 3);
        //set quant done
        setIconOnPanel(jpanQuant, "apply.png", 3, 4);
        //set match loading
        lblMatch.setEnabled(true);
        setIconOnPanel(jpanMatch, "clock.png", 3, 5);

        Vector<RatioGroupCollection> lRatioGroupCollection = new Vector<RatioGroupCollection>();
        for (int i = 0; i < lFiles.size(); i++) {

            //update progress bar
            progressBar.setValue(progressBar.getValue() + 1);
            progressBar.setString("Matching identification with msquant file " + (i + 1) + " of " + lFiles.size() + " msquant files");

            MsQuantReader lReader = new MsQuantReader(lFiles.get(i), iFlamable);

            RatioGroupCollection lTemp = lReader.getRatioGroupCollection();
            if (lTemp != null) {
                lRatioGroupCollection.add(lTemp);
                iCollection.add(lTemp);
                iCollectionIndexes.add(iIndex);
            }

            //_____Do garbage collection______
            System.gc();
        }

        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(false);
        progressBar.setIndeterminate(true);



        //6. get all the protein accessions from the identifications
        for (int i = 0; i < lRatioGroupCollection.size(); i++) {

            for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);

                String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                    //check if it's a new accession
                    boolean lNewAccession = true;
                    for (int l = 0; l < iProteinAccession.size(); l++) {
                        if (iProteinAccession.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                            lNewAccession = false;
                        }
                    }
                    if (lNewAccession) {
                        iProteinAccession.add(lAccessionsForRatioGroup[k]);
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
        iCollectionRatios.add(lRatioTypes);

        //7.B get the types of the ratios from the first distiller ratio collecion
        Vector<String> lComponentList = lRatioGroupCollection.get(0).getComponentTypes();
        String[] lComponentTypes = new String[lComponentList.size()];
        lComponentList.toArray(lComponentTypes);
        iCollectionComponents.add(lComponentTypes);

        //set match done
        setIconOnPanel(jpanMatch, "apply.png", 3, 5);


    }


    /**
     * This method start the data acquisition process for MaxQuant files
     */
    public void startMaxQuant(int aIndex) {

        final int iIndex = aIndex;
        QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
        iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);


        Vector<File> lFiles = iParent.getFiles(iIndex);

        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);
        //set checking files
        lblCheckFiles.setEnabled(true);
        setIconOnPanel(jpanCheck, "clock.png", 3, 2);

        //1.check the MaxQuant files
        for (int i = 0; i < lFiles.size(); i++) {
            //update progress bar
            progressBar.setString("Checked " + lFiles.get(i).getName() + ".");
        }

        //update progress bar
        //Show in the gui that we found all the rov files
        setIconOnPanel(jpanCheck, "apply.png", 3, 2);

        //set id loading
        lblId.setEnabled(true);
        setIconOnPanel(jpanId, "clock.png", 3, 3);
        //set quant loading
        lblQuant.setEnabled(true);
        setIconOnPanel(jpanQuant, "clock.png", 3, 4);

        //3.match ids to quantitations

        //update progress bar
        progressBar.setString("");

        //set id done
        setIconOnPanel(jpanId, "apply.png", 3, 3);
        //set quant done
        setIconOnPanel(jpanQuant, "apply.png", 3, 4);
        //set match loading
        lblMatch.setEnabled(true);
        setIconOnPanel(jpanMatch, "clock.png", 3, 5);

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
                lRatioGroupCollection.add(lTemp);
                iCollection.add(lTemp);
                iCollectionIndexes.add(iIndex);
            }

            //_____Do garbage collection______
            System.gc();
        }

        //update progress bar
        progressBar.setString("");



        //6. get all the protein accessions from the identifications
        for (int i = 0; i < lRatioGroupCollection.size(); i++) {

            for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);

                String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                    //check if it's a new accession
                    boolean lNewAccession = true;
                    for (int l = 0; l < iProteinAccession.size(); l++) {
                        if (iProteinAccession.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                            lNewAccession = false;
                        }
                    }
                    if (lNewAccession) {
                        iProteinAccession.add(lAccessionsForRatioGroup[k]);
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
        iCollectionRatios.add(lRatioTypes);

        //7.B get the types of the ratios from the first distiller ratio collecion
        Vector<String> lComponentList = lRatioGroupCollection.get(0).getComponentTypes();
        String[] lComponentTypes = new String[lComponentList.size()];
        lComponentList.toArray(lComponentTypes);
        iCollectionComponents.add(lComponentTypes);

        //set match done
        setIconOnPanel(jpanMatch, "apply.png", 3, 5);


    }

    /**
     * This method start the data acquisition process for Census files
     */
    public void startCensus(int aIndex) {

        final int iIndex = aIndex;
        QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
        iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);


        Vector<File> lFiles = iParent.getFiles(iIndex);

        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);
        //set checking files
        lblCheckFiles.setEnabled(true);
        setIconOnPanel(jpanCheck, "clock.png", 3, 2);

        //1.check the MaxQuant files
        for (int i = 0; i < lFiles.size(); i++) {
            //update progress bar
            progressBar.setString("Checked " + lFiles.get(i).getName() + ".");
        }

        //update progress bar
        //Show in the gui that we found all the rov files
        setIconOnPanel(jpanCheck, "apply.png", 3, 2);

        //set id loading
        lblId.setEnabled(true);
        setIconOnPanel(jpanId, "clock.png", 3, 3);
        //set quant loading
        lblQuant.setEnabled(true);
        setIconOnPanel(jpanQuant, "clock.png", 3, 4);

        //3.match ids to quantitations

        //update progress bar
        progressBar.setString("");

        //set id done
        setIconOnPanel(jpanId, "apply.png", 3, 3);
        //set quant done
        setIconOnPanel(jpanQuant, "apply.png", 3, 4);
        //set match loading
        lblMatch.setEnabled(true);
        setIconOnPanel(jpanMatch, "clock.png", 3, 5);

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
                lRatioGroupCollection.add(lTemp);
                iCollection.add(lTemp);
                iCollectionIndexes.add(iIndex);
            }

            //_____Do garbage collection______
            System.gc();
        }

        //update progress bar
        progressBar.setString("");



        //6. get all the protein accessions from the identifications
        for (int i = 0; i < lRatioGroupCollection.size(); i++) {

            for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);

                String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                    //check if it's a new accession
                    boolean lNewAccession = true;
                    for (int l = 0; l < iProteinAccession.size(); l++) {
                        if (iProteinAccession.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                            lNewAccession = false;
                        }
                    }
                    if (lNewAccession) {
                        iProteinAccession.add(lAccessionsForRatioGroup[k]);
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
        iCollectionRatios.add(lRatioTypes);

        //7.B get the types of the ratios from the first distiller ratio collecion
        Vector<String> lComponentList = lRatioGroupCollection.get(0).getComponentTypes();
        String[] lComponentTypes = new String[lComponentList.size()];
        lComponentList.toArray(lComponentTypes);
        iCollectionComponents.add(lComponentTypes);

        //set match done
        setIconOnPanel(jpanMatch, "apply.png", 3, 5);


    }


    /**
     * This method start the iTraq data acquisition process for different mascot distiller files
     */
    public void startITraqRov(int aIndex) {

        final int iIndex = aIndex;
        QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
        iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

        //1.check the files

        //update progress bar
        Vector<File> lFiles = iParent.getFiles(iIndex);
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
                            return;
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
                        return;
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
        setIconOnPanel(jpanCheck, "apply.png", 3, 2);

        //set id done
        lblId.setEnabled(true);
        setIconOnPanel(jpanId, "apply.png", 3, 3);
        //set quant done
        lblQuant.setEnabled(true);
        setIconOnPanel(jpanQuant, "apply.png", 3, 4);

        //3.match ids to quantitations

        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(lDatFiles.size() + 1);
        progressBar.setValue(0);

        //set match loading
        lblMatch.setEnabled(true);
        setIconOnPanel(jpanMatch, "clock.png", 3, 5);

        Vector<RatioGroupCollection> lRatioGroupCollection = new Vector<RatioGroupCollection>();
        for (int i = 0; i < lDatFiles.size(); i++) {

            //update progress bar
            progressBar.setValue(progressBar.getValue() + 1);
            progressBar.setString("Matching identification with ratios from dat file number " + (i + 1) + " of " + lDatFiles.size() + " mascot result files");

            lRatioGroupCollection.add(lDatFiles.get(i).getITraqRatioGroupCollection(iParent, iParent.getThreshold(iIndex)));
            iCollection.add(lDatFiles.get(i).getITraqRatioGroupCollection(iParent, iParent.getThreshold(iIndex)));
            iCollectionIndexes.add(iIndex);

            //_____Do garbage collection______
            System.gc();
        }

        //update progress bar
        progressBar.setString("");
        progressBar.setStringPainted(false);
        progressBar.setIndeterminate(true);



        //6. get all the protein accessions from the identifications
        for (int i = 0; i < lRatioGroupCollection.size(); i++) {

            for (int j = 0; j < lRatioGroupCollection.get(i).size(); j++) {
                RatioGroup lRatioGroup = lRatioGroupCollection.get(i).get(j);

                String[] lAccessionsForRatioGroup = lRatioGroup.getProteinAccessions();
                for (int k = 0; k < lAccessionsForRatioGroup.length; k++) {
                    //check if it's a new accession
                    boolean lNewAccession = true;
                    for (int l = 0; l < iProteinAccession.size(); l++) {
                        if (iProteinAccession.get(l).equalsIgnoreCase(lAccessionsForRatioGroup[k])) {
                            lNewAccession = false;
                        }
                    }
                    if (lNewAccession) {
                        iProteinAccession.add(lAccessionsForRatioGroup[k]);
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
        iCollectionRatios.add(lRatioTypes);

        //7.B get the types of the ratios from the first distiller ratio collecion
        Vector<String> lComponentList = lRatioGroupCollection.get(0).getComponentTypes();
        String[] lComponentTypes = new String[lComponentList.size()];
        lComponentList.toArray(lComponentTypes);
        iCollectionComponents.add(lComponentTypes);

        //set match done
        setIconOnPanel(jpanMatch, "apply.png", 3, 5);


    }



    private void createUIComponents() {
        jpanCheck = new JPanel();
        jpanId = new JPanel();
        jpanQuant = new JPanel();
        jpanMatch = new JPanel();
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
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 5);
        jpanContent.add(lblCheckFiles, gbc);
        lblId = new JLabel();
        lblId.setEnabled(false);
        lblId.setFont(new Font("Tahoma", lblId.getFont().getStyle(), lblId.getFont().getSize()));
        lblId.setText("Loading identifications");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 5);
        jpanContent.add(lblId, gbc);
        lblQuant = new JLabel();
        lblQuant.setEnabled(false);
        lblQuant.setFont(new Font("Tahoma", lblQuant.getFont().getStyle(), lblQuant.getFont().getSize()));
        lblQuant.setText("Loading quantitations");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 5);
        jpanContent.add(lblQuant, gbc);
        lblMatch = new JLabel();
        lblMatch.setEnabled(false);
        lblMatch.setFont(new Font("Tahoma", lblMatch.getFont().getStyle(), lblMatch.getFont().getSize()));
        lblMatch.setText("Matching identifications and quantitations");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 5, 10, 5);
        jpanContent.add(lblMatch, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(jpanCheck, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(jpanId, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(jpanQuant, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(jpanMatch, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(spacer4, gbc);
        progressBar = new JProgressBar();
        progressBar.setFont(new Font("Tahoma", progressBar.getFont().getStyle(), progressBar.getFont().getSize()));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(progressBar, gbc);
        lblSet = new JLabel();
        lblSet.setFont(new Font("Tahoma", Font.ITALIC, 14));
        lblSet.setHorizontalAlignment(0);
        lblSet.setHorizontalTextPosition(0);
        lblSet.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        jpanContent.add(lblSet, gbc);
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
