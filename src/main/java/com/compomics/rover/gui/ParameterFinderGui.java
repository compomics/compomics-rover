package com.compomics.rover.gui;

import org.apache.log4j.Logger;

import com.compomics.rover.general.enumeration.RoverSource;
import com.compomics.rover.general.fileio.files.RovFile;
import com.compomics.rover.general.quantitation.QuantitativeProtein;
import com.compomics.rover.general.quantitation.ReferenceSet;
import com.compomics.rover.general.quantitation.sorters.QuantitativeProteinSorterByAccession;
import com.compomics.rover.general.quantitation.sorters.QuantitativeProteinSorterByRatioGroupNumbers;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatioGroup;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.util.sun.SwingWorker;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 05-Oct-2009
 * Time: 16:32:15
 * To change this template use File | Settings | File Templates.
 */
public class ParameterFinderGui extends JFrame {
	// Class specific log4j logger for ParameterFinderGui instances.
	 private static Logger logger = Logger.getLogger(ParameterFinderGui.class);
    private JButton openButton;
    private JLabel lblInfo;
    private JTextArea txtInfo;
    private JButton findParametersButton;
    private JPanel jpanContent;
    private JProgressBar progressBar1;
    private Vector<File> iFiles = new Vector<File>();


    public ParameterFinderGui() {
        openButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //open file chooser
                JFileChooser fc = new JFileChooser();
                fc.setMultiSelectionEnabled(true);
                //create the file filter to choose
                FileFilter lFilter = new RovFileFilter();

                fc.setFileFilter(lFilter);
                fc.showOpenDialog(new JFrame());
                File[] lFiles = fc.getSelectedFiles();
                for (int i = 0; i < lFiles.length; i++) {
                    if (i == 0) {
                    }
                    iFiles.add(lFiles[i]);
                }
                lblInfo.setText("Selected " + iFiles.size() + " .rov files");
            }
        });

        //create JFrame parameters
        this.setTitle("Rov file parameter extractor");
        this.setContentPane(jpanContent);
        this.setSize(800, 400);
        this.setLocation(150, 150);
        this.setVisible(true);
        findParametersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (iFiles.size() > 0) {
                    txtInfo.setText("");
                    startParameterFinding();
                } else {
                    txtInfo.setText("No files selected");

                }
            }
        });
    }


    /**
     * This method start the data acquisition process for a different mascot distiller quantitation toolbox files
     */
    public void startParameterFinding() {

        SwingWorker lStarter = new SwingWorker() {
            public Boolean construct() {
                progressBar1.setIndeterminate(true);
                QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
                iQuantitativeValidationSingelton.setUseOnlyValidRatioForProteinMean(true);

                //1.unzip all the file and check if the quantitation and identification information can be found


                for (int i = 0; i < iFiles.size(); i++) {
                    //update progress bar

                    RovFile lRovFile = new RovFile(iFiles.get(i));
                    boolean allOk = lRovFile.unzipRovFile();
                    //if (allOk != true){txtInfo.append("unzipfail\n");} checked out
                    File lXml = lRovFile.getQuantitationXmlFile();
                    try {
                        if (lXml == null) {
                            txtInfo.append((i + 1) + ". " + iFiles.get(i).getName() + "\tNo quantitation file found" + "\n");
                        } else {  
                            FileReader freader = new FileReader(lXml);
                            LineNumberReader lnreader = new LineNumberReader(freader);
                            String lLine = "";
                            boolean lReadFurther = true;
                            String lQuality;
                            String lCorrelation;
                            String lThreshold = null;
                            boolean lUseFraction = false;
                            String lFraction = null;
                            while (lReadFurther == true && (lLine = lnreader.readLine()) != null) {

                                 if (lLine.trim().startsWith("<mqm:method")) {
                                     lThreshold = lLine.substring(lLine.indexOf("sig_threshold_value=") + 21, lLine.indexOf("\">"));
                                 }
                                if (lLine.trim().startsWith("<mqm:quality")) {
                                    lUseFraction = Boolean.valueOf(lLine.substring(lLine.indexOf("isolated_precursor") + 20, lLine.indexOf("\"", lLine.indexOf("isolated_precursor") + 21)));
                                    lFraction = lLine.substring(lLine.indexOf("isolated_precursor_threshold") + 30, lLine.indexOf("\"", lLine.indexOf("isolated_precursor_threshold") + 31));
                                }
                                if (lLine.trim().startsWith("<mqm:integration")) {
                                    //we are in the wanted line
                                    lReadFurther = false;
                                    lQuality = lLine.substring(lLine.indexOf("elution_profile_correlation_threshold=") + 39, lLine.indexOf("\" ", lLine.indexOf("elution_profile_correlation_threshold=")));
                                    lCorrelation = lLine.substring(lLine.indexOf("matched_rho=") + 13, lLine.indexOf("\" ", lLine.indexOf("matched_rho=")));
                                    txtInfo.append((i + 1) + ". " + iFiles.get(i).getName() + "\t" + lThreshold + "\t" + lQuality + "\t" + lCorrelation + "\t" + lUseFraction + "\t" + lFraction + "\n");
                             }
                              
                        }
}
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();  
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }

            public void finished() {
                progressBar1.setIndeterminate(false);
            }

        };
        lStarter.start();

    }


    public static void main(String[] args) {
        ParameterFinderGui lFinder = new ParameterFinderGui();
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
        openButton = new JButton();
        openButton.setText("Open");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(openButton, gbc);
        lblInfo = new JLabel();
        lblInfo.setHorizontalAlignment(0);
        lblInfo.setText("test");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        jpanContent.add(lblInfo, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        jpanContent.add(scrollPane1, gbc);
        txtInfo = new JTextArea();
        scrollPane1.setViewportView(txtInfo);
        findParametersButton = new JButton();
        findParametersButton.setText("Find parameters");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(findParametersButton, gbc);
        progressBar1 = new JProgressBar();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpanContent.add(progressBar1, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }


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
}
