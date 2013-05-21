package com.compomics.rover.gui;

import com.compomics.rover.general.PeptideIdentification.DefaultPeptideIdentification;
import org.apache.log4j.Logger;

import com.compomics.rover.general.sequenceretriever.TairSequenceRetriever;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.quantitation.QuantitativeProtein;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.QuantitativePeptideGroup;
import com.compomics.rover.general.quantitation.ReferenceSet;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatio;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatioGroup;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatio;
import com.compomics.rover.general.quantitation.source.MaxQuant.MaxQuantRatioGroup;
import com.compomics.rover.general.enumeration.ProteinDatabaseType;
import com.compomics.rover.general.enumeration.QuantitationMetaType;
import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.sequenceretriever.UniprotSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.IpiSequenceRetriever;
import com.compomics.rover.general.sequenceretriever.NcbiSequenceRetriever;
import com.compomics.rover.general.interfaces.Ratio;
import com.compomics.rover.general.interfaces.PeptideIdentification;
import com.compomics.rover.general.db.accessors.IdentificationExtension;
import com.compomics.util.sun.SwingWorker;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.Vector;
import java.util.HashMap;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.*;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.DefaultFontMapper;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 27-Apr-2009
 * Time: 14:30:30
 */
public class ExportGui extends JFrame {
    // Class specific log4j logger for ExportGui instances.
    private static Logger logger = Logger.getLogger(ExportGui.class);
    private JPanel jpanContent;
    private JRadioButton validatedProteinsRadioButton;
    private JRadioButton selectedProteinsRadioButton;
    private JRadioButton allProteinsRadioButton;
    private JRadioButton lastViewedProteinRadioButton;
    private JLabel lblValidated;
    private JLabel lblSelected;
    private JLabel lblAll;
    private JLabel lblLast;
    private JButton pdfBtn;
    private JButton peptideCsvBtn;
    private JButton roverBtn;
    private JButton cancelButton;
    private JProgressBar progressBar1;
    private JButton proteinCsvBtn;


    /**
     * This validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();

    /**
     * The last view protein
     */
    private QuantitativeProtein iProtein;
    /**
     * The selected proteins
     */
    private Vector<QuantitativeProtein> iSelectedProteins = iQuantitativeValidationSingelton.getSelectedProteins();
    /**
     * The validated proteins
     */
    private Vector<QuantitativeProtein> iValidatedProteins = iQuantitativeValidationSingelton.getValidatedProteins();
    /**
     * All proteins
     */
    private QuantitativeProtein[] iAllProteins;
    /**
     * The vector with the proteins to export
     */
    private Vector<QuantitativeProtein> iProteinsToExport;
    /**
     * The parent
     */
    private QuantitationValidationGUI iParent;

    /**
     * Constructor
     *
     * @param aProtein     The last viewed protein
     * @param aAllProteins All the proteins
     */
    public ExportGui(QuantitationValidationGUI aParent, QuantitativeProtein aProtein, QuantitativeProtein[] aAllProteins) {
        //Set the parent
        this.iParent = aParent;
        //set the last used protein
        this.iProtein = aProtein;
        //set the AllProteins
        this.iAllProteins = aAllProteins;
        this.iProteinsToExport = iSelectedProteins;
        //set actionlisteners
        this.setActionListeners();
        //set the labels
        lblAll.setText("# = " + iAllProteins.length);
        lblSelected.setText("# = " + iSelectedProteins.size());
        lblValidated.setText("# = " + iValidatedProteins.size());
        if (iProtein == null) {
            lblLast.setText(" ");
        } else {
            lblLast.setText(iProtein.toString());
        }
        //Set the progressBar invisible
        progressBar1.setVisible(false);

        //create the jframe
        this.setContentPane(jpanContent);
        this.setSize(600, 300);
        this.setLocation(250, 250);
        this.setVisible(true);


    }

    /**
     * This method sets the action listeneres
     */
    public void setActionListeners() {
        //the radio buttons action listeners
        validatedProteinsRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (validatedProteinsRadioButton.isSelected()) {
                    iProteinsToExport = iValidatedProteins;
                }
            }
        });
        selectedProteinsRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedProteinsRadioButton.isSelected()) {
                    iProteinsToExport = iSelectedProteins;
                }
            }
        });
        allProteinsRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (allProteinsRadioButton.isSelected()) {
                    iProteinsToExport = new Vector<QuantitativeProtein>();
                    for (int i = 0; i < iAllProteins.length; i++) {
                        iProteinsToExport.add(iAllProteins[i]);
                    }
                }
            }
        });
        lastViewedProteinRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (lastViewedProteinRadioButton.isSelected()) {
                    iProteinsToExport = new Vector<QuantitativeProtein>();
                    iProteinsToExport.add(iProtein);
                }
            }
        });

        //the button listeners
        pdfBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writePdf();
            }
        });
        peptideCsvBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writePeptideCsv();
            }
        });
        proteinCsvBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeProteinCsv();
            }
        });
        roverBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                writeRover();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

    }


    /**
     * This method closes the frame
     */
    public void close() {
        this.setVisible(false);
        this.dispose();
    }

    /**
     * This method writes a pdf file
     */
    public void writePdf() {

        //check if we can export anything
        if (iProteinsToExport.size() == 0) {
            //nothing can be exported
            JOptionPane.showMessageDialog(new JFrame(), "No proteins could be found in your selected group.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //open file chooser
        final String lPath;
        JFileChooser fc = new JFileChooser();
        if (iQuantitativeValidationSingelton.getFileLocationOpener() != null) {
            fc.setCurrentDirectory(new File(iQuantitativeValidationSingelton.getFileLocationOpener()));
        }
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            iQuantitativeValidationSingelton.setFileLocationOpener(file.getParent());
            lPath = fc.getSelectedFile().getAbsolutePath() + ".pdf";
        } else {
            JOptionPane.showMessageDialog(new JFrame(), "Save command cancelled by user.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //set the protein bar to "print mode"
        //iQuantitativeValidationSingelton.setToPrint(true);

        //get the main thread
        final Thread lThreadGui = Thread.currentThread();
        //create a new swing worker
        SwingWorker lPdfSaver = new SwingWorker() {
            public Boolean construct() {
                try {
                    JOptionPane.showMessageDialog(new JFrame(), "All the selected proteins that will be saved will be vizualised very breafly", "Info", JOptionPane.INFORMATION_MESSAGE);
                    // step 1 create a document
                    Document document = new Document(PageSize.A4, 50, 50, 50, 50);
                    // step 2 create a writer
                    PdfWriter writer = null;
                    try {
                        writer = PdfWriter.getInstance(document, new FileOutputStream(lPath));
                    } catch (DocumentException ex) {
                        java.util.logging.Logger.getLogger(ExportGui.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // step 3 open the document
                    document.open();
                    // step 4 create the PDFContenteByte
                    PdfContentByte cb = writer.getDirectContent();

                    // step 5 set the selected protein as the filtered proteins
                    iParent.setFilteredProteins(iProteinsToExport);


                    // step 6 load every selected protein
                    for (int i = 0; i < iProteinsToExport.size(); i++) {
                        //select the protein
                        iParent.getProteinList().setSelectedIndex(i);
                        iParent.loadProtein(true);
                        //get the protein
                        QuantitativeProtein lProtein = (QuantitativeProtein) iParent.getProteinList().getSelectedValue();
                        //get the types
                        String[] lTypes = lProtein.getTypes();
                        //wait a second to load every panel correctly
                        lThreadGui.sleep(1000);
                        //create differen fonts
                        com.lowagie.text.Font lTitleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 24, 0);
                        com.lowagie.text.Font lTitleFontBlue = FontFactory.getFont(FontFactory.TIMES_ROMAN, 24, 0, new Color(0x00, 0x00, 0xFF));
                        com.lowagie.text.Font lTitleFontOrange = FontFactory.getFont(FontFactory.TIMES_ROMAN, 24, 0, new Color(0xFF, 0xBC, 0x00));
                        com.lowagie.text.Font lNormalFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, 0);
                        com.lowagie.text.Font lNormalFontGreen = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, 0, new Color(0x00, 0xFF, 0x00));
                        com.lowagie.text.Font lNormalFontRed = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, 0, new Color(0xFF, 0x00, 0x00));

                        //add the protein info
                        document.add(new Paragraph("Protein info", lTitleFont));
                        document.add(new Paragraph("   Protein accession : " + lProtein.toString(), lNormalFont));
                        for (int j = 0; j < lTypes.length; j++) {
                            document.add(new Paragraph("   Protein mean for " + lTypes[j] + " : " + lProtein.getProteinRatio(lTypes[j]), lNormalFont));
                            document.add(new Paragraph("   Peptide grouped protein mean for " + lTypes[j] + " : " + lProtein.getGroupedProteinRatio(lTypes[j]), lNormalFont));
                        }
                        //add the ratio info
                        Vector<QuantitativePeptideGroup> lPeptideGroups = lProtein.getPeptideGroups(true);
                        for (int k = 0; k < lPeptideGroups.size(); k++) {
                            Vector<RatioGroup> lGroup = lPeptideGroups.get(k).getRatioGroups();
                            if (lGroup.get(0).getProteinAccessions().length == 1) {
                                document.add(new Paragraph((k + 1) + ". " + lGroup.get(0).getPeptideSequence(), lTitleFontBlue));
                            } else {
                                document.add(new Paragraph((k + 1) + ". " + lGroup.get(0).getPeptideSequence(), lTitleFontOrange));
                            }
                            for (int j = 0; j < lGroup.size(); j++) {
                                RatioGroup lRatioGroup = lGroup.get(j);
                                if (lRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                                    DistillerRatioGroup lDistillerRatioGroup = (DistillerRatioGroup) lRatioGroup;
                                    document.add(new Paragraph(" Correlation: " + lDistillerRatioGroup.getCorrelation() + "   fraction: " + lDistillerRatioGroup.getFraction(), lNormalFont));
                                }
                                for (int l = 0; l < lRatioGroup.getNumberOfRatios(); l++) {
                                    if (lRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                                        DistillerRatio lRatio = (DistillerRatio) lRatioGroup.getRatio(l);
                                        HashMap lStatMeas = iQuantitativeValidationSingelton.getReferenceSet().getStatisticalMeasermentForRatio(lRatio.getType(), lRatio);
                                        String lRatioWrite = "   " + lRatio.getType() + "     " + lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) + "  Quality:  " + lRatio.getQuality() + "  Z-score:  " + String.valueOf(Math.round((Double) lStatMeas.get("significance") * 1000.0) / 1000.0) + "  P-value:  " + String.valueOf(Math.round(iQuantitativeValidationSingelton.calculateTwoSidedPvalueForZvalue((Double) lStatMeas.get("significance")) * 1000.0) / 1000.0);
                                        if (lRatio.getValid()) {
                                            document.add(new Paragraph(" " + lRatioWrite, lNormalFontGreen));
                                        } else {
                                            document.add(new Paragraph(" " + lRatioWrite, lNormalFontRed));
                                            for (int m = 0; m < lRatio.getNotValidState().size(); m++) {
                                                document.add(new Paragraph("      " + lRatio.getNotValidState().get(m) + "   limit: " + lRatio.getNotValidExtraInfo().get(m), lNormalFont));
                                            }
                                        }

                                    } else if (lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_NO_SIGN || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS) {
                                        MaxQuantRatio lRatio = (MaxQuantRatio) lRatioGroup.getRatio(l);
                                        HashMap lStatMeas = iQuantitativeValidationSingelton.getReferenceSet().getStatisticalMeasermentForRatio(lRatio.getType(), lRatio);

                                        String lRatioWrite = "   " + lRatio.getType() + "     " + lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) + "  PEP:  " + ((MaxQuantRatioGroup) lRatio.getParentRatioGroup()).getPEP() + "  Z-score:  " + String.valueOf(Math.round((Double) lStatMeas.get("significance") * 1000.0) / 1000.0) + "  P-value:  " + String.valueOf(Math.round(iQuantitativeValidationSingelton.calculateTwoSidedPvalueForZvalue((Double) lStatMeas.get("significance")) * 1000.0) / 1000.0);
                                        if (lRatio.getValid()) {
                                            document.add(new Paragraph(" " + lRatioWrite, lNormalFontGreen));
                                        } else {
                                            document.add(new Paragraph(" " + lRatioWrite, lNormalFontRed));

                                        }

                                    } else {
                                        Ratio lRatio = lRatioGroup.getRatio(l);
                                        if (lRatio.getValid()) {
                                            document.add(new Paragraph("   " + lRatio.getType() + "     " + lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()), lNormalFontGreen));
                                        } else {
                                            document.add(new Paragraph("   " + lRatio.getType() + "     " + lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()), lNormalFontRed));

                                        }

                                    }
                                }
                            }
                        }

                        //create a new page
                        document.setPageSize(new Rectangle(iParent.getProteinBar().getWidth(), iParent.getProteinBar().getHeight()));
                        document.newPage();

                        //save the protein bar

                        Graphics2D g2ProteinBar = cb.createGraphicsShapes(iParent.getProteinBar().getWidth(), iParent.getProteinBar().getHeight());
                        iParent.getProteinBar().paintAll(g2ProteinBar);
                        g2ProteinBar.dispose();
                        //create a new page and set the page size smaller

                        document.setPageSize(new Rectangle(450, 450));
                        document.newPage();

                        //save every chart on a different page
                        for (int j = 0; j < iParent.getChartPanels().length; j++) {
                            PdfTemplate tp = cb.createTemplate(450, 450);
                            Graphics2D g2d = tp.createGraphics(450, 450, new DefaultFontMapper());
                            Rectangle2D r2d = new Rectangle2D.Double(0, 0, 450, 450);
                            iParent.getChartPanels()[j].draw(g2d, r2d);
                            g2d.dispose();
                            cb.addTemplate(tp, 0, 0);
                            if (j == iParent.getChartPanels().length - 1) {
                                //it's the last panel, set it back to the normal size
                                document.setPageSize(PageSize.A4);
                            }
                            document.newPage();
                        }

                    }

                    // step 7 close the document
                    document.close();

                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                } catch (DocumentException e) {
                    logger.error(e.getMessage(), e);
                } catch (FileNotFoundException e) {
                    logger.error(e.getMessage(), e);
                }
                // step 7.2 dispose the iText components.
                return true;
            }

            public void finished() {
                //load all exported proteins in the list
                iParent.setFilteredProteins(iProteinsToExport);


                //show that the saving is done
                cancelButton.setText("close");
                JOptionPane.showMessageDialog(new JFrame(), "Saving done", "Info", JOptionPane.INFORMATION_MESSAGE);

            }

        };
        lPdfSaver.start();

    }

    /**
     * This method writes a peptide csv file
     */
    public void writePeptideCsv() {

        //check if we can export anything
        if (iProteinsToExport.size() == 0) {
            //nothing can be exported
            JOptionPane.showMessageDialog(new JFrame(), "No proteins could be found in your selected group.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //open file chooser
        final String lPath;
        JFileChooser fc = new JFileChooser();
        if (iQuantitativeValidationSingelton.getFileLocationOpener() != null) {
            fc.setCurrentDirectory(new File(iQuantitativeValidationSingelton.getFileLocationOpener()));
        }
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            iQuantitativeValidationSingelton.setFileLocationOpener(file.getParent());
            String lTemplPath = fc.getSelectedFile().getAbsolutePath();
            if (!lTemplPath.endsWith(".csv")) {
                lTemplPath = lTemplPath + ".csv";
            }
            lPath = lTemplPath;
        } else {
            JOptionPane.showMessageDialog(new JFrame(), "Save command cancelled by user.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //create a new swing worker
        SwingWorker lCsvSaver = new SwingWorker() {
            public Boolean construct() {
                try {

                    //create the separator
                    String lSeparator = ";";
                    //get the ratiotypes
                    String[] lTypes = iQuantitativeValidationSingelton.getReferenceSet().getTypes();
                    //get the ratio components
                    List<String> lComponentsVector = iQuantitativeValidationSingelton.getComponentTypes();
                    String[] lComponents = new String[lComponentsVector.size()];
                    lComponentsVector.toArray(lComponents);
                    //create the writer
                    BufferedWriter out = new BufferedWriter(new FileWriter(lPath));

                    boolean hasMaxQuant = false;
                    boolean hasMsQuantCensus = false;
                    boolean hasMSF = false;
                    boolean hasDistiller = false;


                    for (int p = 0; p < iQuantitativeValidationSingelton.getRoverSources().size(); p++) {
                        if (iQuantitativeValidationSingelton.getRoverSources().get(p) == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV || iQuantitativeValidationSingelton.getRoverSources().get(p) == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS) {
                            hasDistiller = true;
                        } else if (iQuantitativeValidationSingelton.getRoverSources().get(p) == RoverSource.MAX_QUANT || iQuantitativeValidationSingelton.getRoverSources().get(p) == RoverSource.MAX_QUANT_NO_SIGN || iQuantitativeValidationSingelton.getRoverSources().get(p) == RoverSource.MAX_QUANT_MS_LIMS) {
                            hasMaxQuant = true;
                        } else if (iQuantitativeValidationSingelton.getRoverSources().get(p) == RoverSource.THERMO_MSF_FILES) {
                            hasMSF = true;
                        } else {
                            hasMsQuantCensus = true;
                        }
                    }

                    //1.WRITE THE TITLE
                    String lTitle = "accession" + lSeparator + "comment" + lSeparator + "selected" + lSeparator + "validated" + lSeparator + "sequence" + lSeparator + "start" + lSeparator + "end" + lSeparator + "color type";

                    if (hasMaxQuant) {
                        lTitle = lTitle + lSeparator + "PEP";
                    }

                    for (int k = 0; k < lTypes.length; k++) {
                        if (iQuantitativeValidationSingelton.isNormalization()) {
                            //lTitle = lTitle + lSeparator + lTypes[k] + " part number" + lSeparator + lTypes[k] + " pre Norm MAD" + lSeparator + lTypes[k] + " Norm MAD" + lSeparator + lTypes[k] + " protein ratio" + lSeparator + lTypes[k] + " peptide grouped protein ratio" + lSeparator + lTypes[k] + " peptide grouped ratio";
                            lTitle = lTitle + lSeparator + lTypes[k] + " protein ratio" + lSeparator + lTypes[k] + " peptide grouped protein ratio" + lSeparator + lTypes[k] + " peptide grouped ratio";
                            lTitle = lTitle + lSeparator + lTypes[k] + " peptide group ratio SD" + lSeparator + lTypes[k] + " ratio" + lSeparator + lTypes[k] + " original ratio" + lSeparator + lTypes[k] + " normalization ratio diff (in log2)" + lSeparator + lTypes[k] + " comment" + lSeparator + lTypes[k] + " status" + lSeparator + lTypes[k] + " Z-score";
                        } else {
                            lTitle = lTitle + lSeparator + lTypes[k] + " protein ratio" + lSeparator + lTypes[k] + " peptide grouped protein ratio" + lSeparator + lTypes[k] + " peptide grouped ratio";
                            lTitle = lTitle + lSeparator + lTypes[k] + " peptide group ratio SD" + lSeparator + lTypes[k] + " ratio" + lSeparator + lTypes[k] + " comment" + lSeparator + lTypes[k] + " status" + lSeparator + lTypes[k] + " Z-score";
                        }
                        if (hasDistiller) {
                            lTitle = lTitle + lSeparator + lTypes[k] + " quality";
                            hasDistiller = true;
                        }

                    }
                    if (iQuantitativeValidationSingelton.isDatabaseMode()) {
                        // database mode => we have identificationid
                        for (int k = 0; k < lComponents.length; k++) {
                            lTitle = lTitle + lSeparator + lComponents[k] + " identificationid" + lSeparator + lComponents[k] + " modified sequence";
                        }
                    } else {
                        for (int k = 0; k < lComponents.length; k++) {
                            lTitle = lTitle + lSeparator + lComponents[k] + " identified";
                        }
                    }

                    if (hasDistiller) {
                        // distiller mode => we have a correlation, fraction, hit and .rov filename
                        lTitle = lTitle + lSeparator + "correlation" + lSeparator + "fraction" + lSeparator + "hit";
                    }
                    if (hasMaxQuant || hasDistiller) {
                        for (int k = 0; k < lComponents.length; k++) {
                            lTitle = lTitle + lSeparator + lComponents[k] + " absolute intensity";
                        }
                    }
                    if (hasMsQuantCensus || hasDistiller || hasMSF) {
                        // not in distiller mode => we only have a filename
                        lTitle = lTitle + lSeparator + "quantitation file name";
                    }

                    if (iQuantitativeValidationSingelton.isMultipleSources()) {
                        lTitle = lTitle + lSeparator + "Source";
                    }
                    out.write(lTitle + "\n");


                    //2.WRITE THE PROTEIN RATIOS
                    //set the progress bar
                    progressBar1.setVisible(true);
                    progressBar1.setMaximum(iProteinsToExport.size() + 1);
                    progressBar1.setValue(0);
                    for (int i = 0; i < iProteinsToExport.size(); i++) {

                        progressBar1.setValue(progressBar1.getValue() + 1);
                        QuantitativeProtein lProtein = iProteinsToExport.get(i);

                        try {
                            if (lProtein.getSequence() == null) {
                                if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.UNIPROT)) {
                                    lProtein.setSequence((new UniprotSequenceRetriever(lProtein.getAccession())).getSequence());
                                } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.IPI)) {
                                    lProtein.setSequence((new IpiSequenceRetriever(lProtein.getAccession())).getSequence());
                                } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.NCBI)) {
                                    lProtein.setSequence((new NcbiSequenceRetriever(lProtein.getAccession())).getSequence());
                                } else if (iQuantitativeValidationSingelton.getDatabaseType().equals(ProteinDatabaseType.TAIR)) {
                                    lProtein.setSequence((new TairSequenceRetriever(lProtein.getAccession())).getSequence());
                                }
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }

                        for (int j = 0; j < lProtein.getPeptideGroups(true).size(); j++) {
                            for (int l = 0; l < lProtein.getPeptideGroups(true).get(j).getRatioGroups().size(); l++) {
                                //the result to write
                                String lResult = "";
                                //the ratiogroups
                                RatioGroup lRatioGroup = lProtein.getPeptideGroups(true).get(j).getRatioGroups().get(l);
                                //get the start and end position of the identified peptide
                                int lStart = lProtein.getPeptideGroups(true).get(j).getStartPosition();
                                int lEnd = lProtein.getPeptideGroups(true).get(j).getEndPosition();
                                String lColor = "blue";
                                if (!lProtein.getPeptideGroups(true).get(j).isLinkedToMoreProteins()) {
                                    lColor = "blue";
                                } else if (lProtein.getAccession().trim().equalsIgnoreCase(lRatioGroup.getRazorProteinAccession().trim())) {
                                    lColor = "red";
                                } else {
                                    lColor = "orange";
                                }

                                //fill the result string
                                //write protein information
                                lResult = lResult + lProtein.getAccession() + lSeparator + lProtein.getProteinComment() + lSeparator + lProtein.getSelected() + lSeparator + lProtein.getValidated() + lSeparator + lRatioGroup.getPeptideSequence() + lSeparator + lStart + lSeparator + lEnd + lSeparator + lColor;
                                if (hasMaxQuant) {
                                    if (lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_NO_SIGN || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS) {
                                        MaxQuantRatioGroup lMQGroup = (MaxQuantRatioGroup) lRatioGroup;
                                        lResult = lResult + lSeparator + lMQGroup.getPEP();
                                    } else {
                                        lResult = lResult + lSeparator + "";
                                    }
                                }

                                for (int k = 0; k < lTypes.length; k++) {

                                    //write the peptide ratios, significance and comment
                                    Ratio lRatio = lRatioGroup.getRatioByType(lTypes[k]);
                                    if (lRatio != null) {
                                        if (iQuantitativeValidationSingelton.isNormalization()) {
                                            //add the protein ratios
                                            //lResult = lResult + lSeparator + (lRatio.getNormatlizationPart() + 1) + lSeparator + lRatio.getPreNormalizedMAD() + lSeparator + lRatio.getNormalizedMAD() + lSeparator + lProtein.getProteinRatio(lTypes[k]) + lSeparator + lProtein.getGroupedProteinRatio(lTypes[k]) + lSeparator + lProtein.getPeptideGroups(true).get(j).getMeanRatioForGroup(lTypes[k]) + lSeparator + lProtein.getPeptideGroups(true).get(j).getSDForGroup(lTypes[k]);
                                            lResult = lResult + lSeparator + lProtein.getProteinRatio(lTypes[k]) + lSeparator + lProtein.getGroupedProteinRatio(lTypes[k]) + lSeparator + lProtein.getPeptideGroups(true).get(j).getMeanRatioForGroup(lTypes[k]) + lSeparator + lProtein.getPeptideGroups(true).get(j).getSDForGroup(lTypes[k]);
                                            lResult = lResult + lSeparator + lRatio.getRatio(iQuantitativeValidationSingelton.isLog2()) + lSeparator + lRatio.getOriginalRatio(iQuantitativeValidationSingelton.isLog2()) + lSeparator + (lRatio.getRatio(true) - lRatio.getOriginalRatio(true));
                                        } else {
                                            //add the protein ratios
                                            lResult = lResult + lSeparator + lProtein.getProteinRatio(lTypes[k]) + lSeparator + lProtein.getGroupedProteinRatio(lTypes[k]) + lSeparator + lProtein.getPeptideGroups(true).get(j).getMeanRatioForGroup(lTypes[k]) + lSeparator + lProtein.getPeptideGroups(true).get(j).getSDForGroup(lTypes[k]);
                                            lResult = lResult + lSeparator + lRatio.getRatio(iQuantitativeValidationSingelton.isLog2());
                                        }

                                        HashMap lStatMeas = iQuantitativeValidationSingelton.getReferenceSet().getStatisticalMeasermentForRatio(lRatio.getType(), lRatio);
                                        //check if we have a comment
                                        String lComment = lRatio.getComment();
                                        if (lComment == null) {
                                            lComment = "/";
                                        }
                                        lResult = lResult + lSeparator + lComment + lSeparator + lRatio.getValid() + lSeparator + lStatMeas.get("significance");
                                        if (hasDistiller) {
                                            if (lRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                                                //it's from distiller we can write the quality
                                                DistillerRatio lDistillerRatio = (DistillerRatio) lRatio;
                                                lResult = lResult + lSeparator + lDistillerRatio.getQuality();
                                            } else {
                                                lResult = lResult + lSeparator;
                                            }
                                        }
                                    } else {
                                        if (iQuantitativeValidationSingelton.isNormalization()) {
                                            //add the protein ratios
                                            lResult = lResult + lSeparator + lSeparator + lSeparator + lSeparator + lSeparator + lSeparator + lSeparator;
                                            lResult = lResult + lSeparator + lSeparator + lSeparator;
                                        } else {
                                            //add the protein ratios
                                            lResult = lResult + lSeparator + lSeparator + lSeparator + lSeparator;
                                            lResult = lResult + lSeparator;
                                        }
                                        lResult = lResult + lSeparator + lSeparator + lSeparator;
                                        if (hasDistiller) {
                                            lResult = lResult + lSeparator;
                                        }
                                    }

                                }
                                for (int k = 0; k < lComponents.length; k++) {
                                    Vector<PeptideIdentification> lIdentifications = lRatioGroup.getIdentificationsForType(lComponents[k]);
                                    if (lIdentifications != null) {
                                        if (iQuantitativeValidationSingelton.isDatabaseMode()) {
                                            String lIds = "";
                                            String lMod = "";
                                            for (int lk = 0; lk < lIdentifications.size(); lk++) {
                                                if (lk == 0) {
                                                    lMod = lIdentifications.get(lk).getModified_sequence();
                                                    lIds = ((IdentificationExtension) lIdentifications.get(lk)).getIdentificationid() + "";
                                                } else {
                                                    lIds = lIds + "|" + ((IdentificationExtension) lIdentifications.get(lk)).getIdentificationid();
                                                }
                                            }
                                            lResult = lResult + lSeparator + lIds + lSeparator + lMod;
                                        } else {
                                            String lIds = "";
                                            String lMod = "";
                                            for (int lk = 0; lk < lIdentifications.size(); lk++) {
                                                if (lk == 0) {
                                                    lIds = lIdentifications.get(lk).getScore() + "";
                                                } else {
                                                    lIds = lIds + "|" + lIdentifications.get(lk).getScore();
                                                }
                                            }
                                        }
                                    } else {
                                        if (iQuantitativeValidationSingelton.isDatabaseMode()) {
                                            lResult = lResult + lSeparator + "NA" + lSeparator + "NA";
                                        } else {
                                            lResult = lResult + lSeparator + "NA";
                                        }
                                    }
                                }
                                if (hasDistiller) {
                                    if (lRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_MS_LIMS || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.DISTILLER_QUANT_TOOLBOX_ROV) {
                                        DistillerRatioGroup lDistillerRatioGroup = (DistillerRatioGroup) lRatioGroup;
                                        lResult = lResult + lSeparator + lDistillerRatioGroup.getCorrelation() + lSeparator + lDistillerRatioGroup.getFraction() + lSeparator + lDistillerRatioGroup.getReferenceOfParentHit();
                                        for (int k = 0; k < lComponents.length; k++) {
                                            DistillerRatioGroup lDistRatioGroup = (DistillerRatioGroup) lRatioGroup;
                                            lResult = lResult + lSeparator + lDistRatioGroup.getAbsoluteIntensities()[k];
                                        }
                                        lResult = lResult + lSeparator + lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME);
                                    } else {
                                        lResult = lResult + lSeparator + lSeparator;
                                        if (lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_NO_SIGN || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS) {
                                            lResult = lResult + lSeparator;
                                            for (int k = 0; k < lComponents.length; k++) {
                                                MaxQuantRatioGroup lMaxQuantRatioGroup = (MaxQuantRatioGroup) lRatioGroup;
                                                lResult = lResult + lSeparator + lMaxQuantRatioGroup.getAbsoluteIntensities()[k];
                                            }
                                            lResult = lResult + lSeparator;
                                        } else {
                                            for (int k = 0; k < lComponents.length; k++) {
                                                lResult = lResult + lSeparator;
                                            }
                                            if (lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME) == null) {
                                                lResult = lResult + lSeparator;
                                            } else {
                                                lResult = lResult + lSeparator + lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME);
                                            }
                                        }

                                    }

                                } else {
                                    if (hasMaxQuant) {
                                        if (lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_NO_SIGN || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT || lRatioGroup.getParentCollection().getRoverSource() == RoverSource.MAX_QUANT_MS_LIMS) {
                                            for (int k = 0; k < lComponents.length; k++) {
                                                MaxQuantRatioGroup lMaxQuantRatioGroup = (MaxQuantRatioGroup) lRatioGroup;
                                                lResult = lResult + lSeparator + lMaxQuantRatioGroup.getAbsoluteIntensities()[k];
                                            }
                                            if (hasMsQuantCensus || hasMSF) {
                                                lResult = lResult + lSeparator;
                                            }
                                        } else {
                                            for (int k = 0; k < lComponents.length; k++) {
                                                lResult = lResult + lSeparator;
                                            }
                                            if (lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME) == null) {
                                                lResult = lResult + lSeparator;
                                            } else {
                                                lResult = lResult + lSeparator + lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME);
                                            }
                                        }
                                    } else {
                                        if (lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME) == null) {
                                            lResult = lResult + lSeparator;
                                        } else {
                                            lResult = lResult + lSeparator + lRatioGroup.getParentCollection().getMetaData(QuantitationMetaType.FILENAME);
                                        }
                                    }
                                }

                                if (iQuantitativeValidationSingelton.isMultipleSources()) {
                                    lResult = lResult + lSeparator + iQuantitativeValidationSingelton.getTitles().get(lRatioGroup.getParentCollection().getIndex());
                                }
                                out.write(lResult + "\n");
                            }
                        }

                    }
                    progressBar1.setVisible(false);
                    out.close();
                    //show that the saving is done
                    JOptionPane.showMessageDialog(new JFrame(), "Saving done", "Info", JOptionPane.INFORMATION_MESSAGE);
                    cancelButton.setText("Close");
                } catch (IOException e) {
                    //show that the saving is done
                    JOptionPane.showMessageDialog(new JFrame(), "Saving with errors: " + e.getMessage(), "Info", JOptionPane.INFORMATION_MESSAGE);
                    cancelButton.setText("Close");
                    logger.error(e.getMessage(), e);
                }

                return true;
            }

            public void finished() {

            }
        };
        lCsvSaver.start();

    }

    /**
     * This method writes a peptide csv file
     */
    public void writeProteinCsv() {

        //check if we can export anything
        if (iProteinsToExport.size() == 0) {
            //nothing can be exported
            JOptionPane.showMessageDialog(new JFrame(), "No proteins could be found in your selected group.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //open file chooser
        final String lPath;
        JFileChooser fc = new JFileChooser();
        if (iQuantitativeValidationSingelton.getFileLocationOpener() != null) {
            fc.setCurrentDirectory(new File(iQuantitativeValidationSingelton.getFileLocationOpener()));
        }
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            iQuantitativeValidationSingelton.setFileLocationOpener(file.getParent());
            String lTemplPath = fc.getSelectedFile().getAbsolutePath();
            if (!lTemplPath.endsWith(".csv")) {
                lTemplPath = lTemplPath + ".csv";
            }
            lPath = lTemplPath;
        } else {
            JOptionPane.showMessageDialog(new JFrame(), "Save command cancelled by user.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //create a new swing worker
        SwingWorker lCsvSaver = new SwingWorker() {
            public Boolean construct() {
                try {

                    //create the separator
                    String lSeparator = ";";
                    //get the ratiotypes
                    String[] lTypes = iQuantitativeValidationSingelton.getReferenceSet().getTypes();
                    //get the ratio components
                    String[] lComponents = iQuantitativeValidationSingelton.getReferenceSet().getComponents();
                    //create the writer
                    BufferedWriter out = new BufferedWriter(new FileWriter(lPath));
                    ReferenceSet lReferenceSet = iQuantitativeValidationSingelton.getReferenceSet();

                    //1.WRITE THE TITLE
                    String lTitle = "accession" + lSeparator + "comment" + lSeparator + "selected" + lSeparator + "validated" + lSeparator + "number of identifications" + lSeparator + "number of different peptides" + lSeparator + "number of ratiogroups" + lSeparator + "protein coverage";

                    for (int k = 0; k < lTypes.length; k++) {
                        //lTitle = lTitle + lSeparator + lTypes[k] + " protein ratio" + lSeparator + lTypes[k] + " protein ratio SD" + lSeparator + lTypes[k] + " number of peptide ratios used";

                        //lTitle = lTitle + lSeparator + lTypes[k] + " protein ratio" + lSeparator + lTypes[k] + " protein ratio SD" + lSeparator + lTypes[k] + " protein ratio MAD" + lSeparator + lTypes[k] + " protein P-value" + lSeparator + lTypes[k] + " protein Z-score" + lSeparator + lTypes[k] + " protein Power" + lSeparator + lTypes[k] + " protein mean Random Z-Score" + lSeparator + lTypes[k] + " protein SD Random Z-Score" + lSeparator + lTypes[k] + " number of peptide ratios used";
                        //lTitle = lTitle + lSeparator + lTypes[k] + " protein intensity sum " + lSeparator + lTypes[k] + " protein intensity mean" + lSeparator + lTypes[k] + " protein intensity median" + lSeparator + lTypes[k] + " protein intensity SD" + lSeparator + lTypes[k] + " intensity sum Z-score" + lSeparator + lTypes[k] + " intensity mean Z-score" + lSeparator + lTypes[k] + " intensity median Z-score" + lSeparator + lTypes[k] + " intensity sd Z-score" + lSeparator + lTypes[k] + " original MAD SD" + lSeparator + lTypes[k] + " normalized MAD SD" + lSeparator + lTypes[k] + " original MAD Mean" + lSeparator + lTypes[k] + " normalized MAD Mean" + lSeparator + lTypes[k] + " peptide grouped protein ratio" + lSeparator + lTypes[k] + " ratios valid" + lSeparator + "Number of distinct peptide with one valid " + lTypes[k] + " ratio";

                        lTitle = lTitle + lSeparator + lTypes[k] + " protein ratio" + lSeparator + lTypes[k] + " protein ratio SD" + lSeparator + lTypes[k] + " protein Z-score" + lSeparator + lTypes[k] + " number of peptide ratios used";
                        lTitle = lTitle + lSeparator + lTypes[k] + " peptide grouped protein ratio" + lSeparator + lTypes[k] + " ratios valid" + lSeparator + "Number of distinct peptide with one valid " + lTypes[k] + " ratio";

                    }

                    /*String lSources = "";
                    if (iQuantitativeValidationSingelton.isMultipleSources()) {
                        lTitle = lTitle + lSeparator + "Sources used";
                        Vector<Boolean> lSelectedIndices = iQuantitativeValidationSingelton.getSelectedIndexes();
                        for (int i = 0; i < lSelectedIndices.size(); i++) {
                            if (lSelectedIndices.get(i)) {
                                lSources = lSources + (i + 1) + "_";
                            }
                        }
                        lSources = lSources.substring(0, lSources.lastIndexOf("_"));
                    } */

                    out.write(lTitle + "\n");


                    //2.WRITE THE PROTEIN RATIOS
                    //set the progress bar
                    progressBar1.setVisible(true);
                    progressBar1.setMaximum(iProteinsToExport.size() + 1);
                    progressBar1.setValue(0);
                    for (int i = 0; i < iProteinsToExport.size(); i++) {

                        progressBar1.setValue(progressBar1.getValue() + 1);
                        QuantitativeProtein lProtein = iProteinsToExport.get(i);

                        //System.out.print("\n" + i);
                        String lResult = "";
                        lResult = lResult + lProtein.getAccession() + lSeparator + lProtein.getProteinComment() + lSeparator + lProtein.getSelected() + lSeparator + lProtein.getValidated() + lSeparator + lProtein.getNumberOfIdentifications() + lSeparator + lProtein.getPeptideGroups(true).size() + lSeparator + lProtein.getNumberOfRatioGroups() + lSeparator + lProtein.getProteinCoverage();
                        for (int k = 0; k < lTypes.length; k++) {
                            //lResult = lResult + lSeparator + lProtein.getProteinRatio(lTypes[k]) + lSeparator + lProtein.getProteinRatioStandardDeviationForType(lTypes[k]) + lSeparator + lProtein.getNumberOfRatiosUsedForProteinMean(lTypes[k]);

                            //lResult = lResult + lSeparator + lProtein.getProteinRatio(lTypes[k]) + lSeparator + lProtein.getProteinRatioStandardDeviationForType(lTypes[k]) + lSeparator + lProtein.getProteinRatioMADForType(lTypes[k]) + lSeparator + lProtein.getProteinPvalue(lTypes[k], -1) + lSeparator + lProtein.getProteinZScore(lTypes[k], -1) + lSeparator + lProtein.getPower(lTypes[k], -1, 1.96) + lSeparator + lReferenceSet.getZscoreForRatioMean(lProtein.getRatioIndexMeanForType(lTypes[k]), lProtein.getNumberOfRatiosUsedForProteinMean(lTypes[k]), lTypes[k]) + lSeparator + lReferenceSet.getZscoreForRatioSd(lProtein.getRatioIndexSDForType(lTypes[k]), lProtein.getNumberOfRatiosUsedForProteinMean(lTypes[k]), lTypes[k]) + lSeparator + lProtein.getNumberOfRatiosUsedForProteinMean(lTypes[k]);
                            //lResult = lResult + lSeparator + lProtein.getSummedProteinIntensities(lTypes[k], -1, true) + lSeparator + lProtein.getMeanProteinIntensity(lTypes[k], -1, true) + lSeparator + lProtein.getMedianProteinIntensity(lTypes[k], -1, true) + lSeparator + lProtein.getStandardDeviationProteinIntensities(lTypes[k], -1, true) + lSeparator + lReferenceSet.getZscoreForIntensitySum(lProtein.getSummedProteinIntensities(lTypes[k], -1, true), lProtein.getNumberOfRatiosUsedForProteinMean(lTypes[k]), lTypes[k]) + lSeparator + lReferenceSet.getZscoreForIntensityMean(lProtein.getMeanProteinIntensity(lTypes[k], -1, true), lProtein.getNumberOfRatiosUsedForProteinMean(lTypes[k]), lTypes[k]) + lSeparator + lReferenceSet.getZscoreForIntensityMedian(lProtein.getMedianProteinIntensity(lTypes[k], -1, true), lProtein.getNumberOfRatiosUsedForProteinMean(lTypes[k]), lTypes[k]) + lSeparator + lReferenceSet.getZscoreForIntensitySd(lProtein.getStandardDeviationProteinIntensities(lTypes[k], -1, true), lProtein.getNumberOfRatiosUsedForProteinMean(lTypes[k]), lTypes[k]) + lSeparator + lProtein.getOriginalMadSD(lTypes[k]) + lSeparator + lProtein.getNormalizedMadSD(lTypes[k]) + lSeparator + lProtein.getOriginalMadMean(lTypes[k]) + lSeparator + lProtein.getNormalizedMadMean(lTypes[k]) + lSeparator + lProtein.getGroupedProteinRatio(lTypes[k]) + lSeparator + lProtein.getNumberOfValidRatioByType(lTypes[k]) + lSeparator + lProtein.getNumberOfDistinctPeptidesWithOneValidRatioByType(lTypes[k]);

                            lResult = lResult + lSeparator + lProtein.getProteinRatio(lTypes[k]) + lSeparator + lProtein.getProteinRatioStandardDeviationForType(lTypes[k]) + lSeparator + lProtein.getProteinZScore(lTypes[k], -1) + lSeparator + lProtein.getNumberOfRatiosUsedForProteinMean(lTypes[k]);
                            lResult = lResult + lSeparator + lProtein.getGroupedProteinRatio(lTypes[k]) + lSeparator + lProtein.getNumberOfValidRatioByType(lTypes[k]) + lSeparator + lProtein.getNumberOfDistinctPeptidesWithOneValidRatioByType(lTypes[k]);
                        }

                        /*
                        if (iQuantitativeValidationSingelton.isMultipleSources()) {
                            lResult = lResult + lSeparator + lSources;
                        } */

                        out.write(lResult + "\n");
                        //System.out.print("   done");
                    }
                    progressBar1.setVisible(false);
                    out.close();
                    //show that the saving is done
                    JOptionPane.showMessageDialog(new JFrame(), "Saving done", "Info", JOptionPane.INFORMATION_MESSAGE);
                    cancelButton.setText("Close");
                } catch (IOException e) {
                    //show that the saving is done
                    JOptionPane.showMessageDialog(new JFrame(), "Saving with errors: " + e.getMessage(), "Info", JOptionPane.INFORMATION_MESSAGE);
                    cancelButton.setText("Close");
                    logger.error(e.getMessage(), e);
                }

                return true;
            }

            public void finished() {

            }
        };
        lCsvSaver.start();

    }

    /**
     * This method writes a rover file
     */
    public void writeRover() {
        JOptionPane.showMessageDialog(new JFrame(), "All the data will be saved in the .rover file and not only your selected data.", "Info", JOptionPane.INFORMATION_MESSAGE);
        progressBar1.setIndeterminate(true);
        try {

            String lFileLocation = null;
            //open file chooser
            JFileChooser fc = new JFileChooser();
            if (iQuantitativeValidationSingelton.getFileLocationOpener() != null) {
                fc.setCurrentDirectory(new File(iQuantitativeValidationSingelton.getFileLocationOpener()));
            }
            int returnVal = fc.showSaveDialog(new JFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                iQuantitativeValidationSingelton.setFileLocationOpener(file.getParent());
                lFileLocation = fc.getSelectedFile().getAbsolutePath();
                if (!lFileLocation.endsWith(".rover")) {
                    lFileLocation = lFileLocation + ".rover";
                }
            } else {
                JOptionPane.showMessageDialog(new JFrame(), "Save command cancelled by user.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(lFileLocation));
            out.write("//protein accession, selected, validated\n");
            //write selected proteins
            for (int i = 0; i < iSelectedProteins.size(); i++) {
                String validated = "0";
                if (iSelectedProteins.get(i).getValidated()) {
                    validated = "1";
                }
                out.write(iSelectedProteins.get(i).getAccession() + ",1," + validated + "," + iSelectedProteins.get(i).getProteinComment() + "\n");
            }
            //write validated proteins
            for (int i = 0; i < iValidatedProteins.size(); i++) {
                //check if it is not in the selected proteins
                boolean found = false;
                for (int j = 0; j < iSelectedProteins.size(); j++) {
                    if (iSelectedProteins.get(j).getAccession().equalsIgnoreCase(iValidatedProteins.get(i).getAccession())) {
                        found = true;
                    }
                }
                if (!found) {
                    out.write(iValidatedProteins.get(i).getAccession() + ",0,1," + iValidatedProteins.get(i).getProteinComment() + "\n");
                }
            }
            //write commented proteins
            for (int i = 0; i < iQuantitativeValidationSingelton.getCommentedProteins().size(); i++) {
                //the validated and selected proteins are already written
                if (!iQuantitativeValidationSingelton.getCommentedProteins().get(i).getSelected() && !iQuantitativeValidationSingelton.getCommentedProteins().get(i).getValidated()) {
                    out.write(iQuantitativeValidationSingelton.getCommentedProteins().get(i).getAccession() + ",0,0," + iQuantitativeValidationSingelton.getCommentedProteins().get(i).getProteinComment() + "\n");
                }
            }
            out.write("//ratio type, ratio, sequence, proteins, valid, comment\n");
            for (int i = 0; i < iQuantitativeValidationSingelton.getValidatedRatios().size(); i++) {
                Ratio lRatio = iQuantitativeValidationSingelton.getValidatedRatios().get(i);
                out.write(iQuantitativeValidationSingelton.getValidatedRatios().get(i).getType() + "," + iQuantitativeValidationSingelton.getValidatedRatios().get(i).getRatio(false) + "," + lRatio.getParentRatioGroup().getPeptideSequence() + "," + lRatio.getParentRatioGroup().getProteinAccessionsAsString() + "|," + lRatio.getValid() + "," + lRatio.getComment() + "\n");
            }
            out.write("//protein, peptide\n");
            for (int i = 0; i < iQuantitativeValidationSingelton.getNotUsedPeptides().size(); i++) {
                out.write(iQuantitativeValidationSingelton.getNotUsedProteins().get(i) + "," + iQuantitativeValidationSingelton.getNotUsedPeptides().get(i) + "\n");
            }
            out.flush();
            out.close();

            //show that the saving is done
            JOptionPane.showMessageDialog(new JFrame(), "Saving done", "Info", JOptionPane.INFORMATION_MESSAGE);
            cancelButton.setText("Close");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }


    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
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
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 11;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        jpanContent.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder(null, "Export ...", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.ABOVE_TOP, new Font(panel1.getFont().getName(), Font.ITALIC, 16)));
        validatedProteinsRadioButton = new JRadioButton();
        validatedProteinsRadioButton.setText("your validated proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(validatedProteinsRadioButton, gbc);
        selectedProteinsRadioButton = new JRadioButton();
        selectedProteinsRadioButton.setSelected(true);
        selectedProteinsRadioButton.setText("your selected proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(selectedProteinsRadioButton, gbc);
        allProteinsRadioButton = new JRadioButton();
        allProteinsRadioButton.setText("all proteins");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(allProteinsRadioButton, gbc);
        lastViewedProteinRadioButton = new JRadioButton();
        lastViewedProteinRadioButton.setSelected(false);
        lastViewedProteinRadioButton.setText("the last viewed protein");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(lastViewedProteinRadioButton, gbc);
        lblValidated = new JLabel();
        lblValidated.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(lblValidated, gbc);
        lblSelected = new JLabel();
        lblSelected.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(lblSelected, gbc);
        lblAll = new JLabel();
        lblAll.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(lblAll, gbc);
        lblLast = new JLabel();
        lblLast.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(lblLast, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer1, gbc);
        pdfBtn = new JButton();
        pdfBtn.setBorderPainted(true);
        pdfBtn.setContentAreaFilled(false);
        pdfBtn.setFocusPainted(false);
        pdfBtn.setIcon(new ImageIcon(getClass().getResource("/pdf.gif")));
        pdfBtn.setText("");
        pdfBtn.setToolTipText("Export to PDF");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(pdfBtn, gbc);
        peptideCsvBtn = new JButton();
        peptideCsvBtn.setBorderPainted(true);
        peptideCsvBtn.setContentAreaFilled(false);
        peptideCsvBtn.setFocusPainted(false);
        peptideCsvBtn.setIcon(new ImageIcon(getClass().getResource("/csv.gif")));
        peptideCsvBtn.setText("");
        peptideCsvBtn.setToolTipText("Export peptide information to CSV");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(peptideCsvBtn, gbc);
        roverBtn = new JButton();
        roverBtn.setBorderPainted(true);
        roverBtn.setContentAreaFilled(false);
        roverBtn.setFocusPainted(false);
        roverBtn.setIcon(new ImageIcon(getClass().getResource("/rover.gif")));
        roverBtn.setText("");
        roverBtn.setToolTipText("Export to .rover");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(roverBtn, gbc);
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(cancelButton, gbc);
        progressBar1 = new JProgressBar();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 20, 5, 20);
        jpanContent.add(progressBar1, gbc);
        proteinCsvBtn = new JButton();
        proteinCsvBtn.setBorderPainted(true);
        proteinCsvBtn.setContentAreaFilled(false);
        proteinCsvBtn.setFocusPainted(false);
        proteinCsvBtn.setIcon(new ImageIcon(getClass().getResource("/csv_protein.gif")));
        proteinCsvBtn.setText("");
        proteinCsvBtn.setToolTipText("Export protein information to CSV");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(proteinCsvBtn, gbc);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(validatedProteinsRadioButton);
        buttonGroup.add(selectedProteinsRadioButton);
        buttonGroup.add(allProteinsRadioButton);
        buttonGroup.add(lastViewedProteinRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }
}
