package com.compomics.rover.general.quantitation.source.Census;

import org.apache.log4j.Logger;

import com.compomics.rover.general.quantitation.DefaultRatio;
import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.source.distiller.DistillerRatioGroupPartner;
import com.compomics.rover.general.interfaces.PeptideIdentification;
import com.compomics.rover.general.interfaces.Ratio;

import java.util.ArrayList;
import java.util.Vector;
import java.awt.*;
import java.io.IOException;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RectangleAnchor;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 16-May-2009
 * Time: 13:58:53
 */

/**
 * The censsus ratio group
 */
public class CensusRatioGroup extends RatioGroup {
	// Class specific log4j logger for CensusRatioGroup instances.
	 private static Logger logger = Logger.getLogger(CensusRatioGroup.class);

    /**
     * The absolute intensities found in the DistillerRatioGroup in the distiller quantitation file.
     */
    private Double[] iRatioGroupAbsoluteIntensities;
   /**
     * The List with Identification instances to be linked to a ratio.
     */
    protected ArrayList<PeptideIdentification> iIdentifications = new ArrayList<PeptideIdentification>();
    /**
     * The List with Peptide types to be linked to a ratio.
     * Note that this list is created in sync with iIdentifications.
     * Therefore, iIdentifications[0] is of type iPeptideTypes[0]
     */
    protected ArrayList<String> iPeptideTypes =  new ArrayList<String>();
    /**
     * the List with Ratio instances.
     */
    protected ArrayList<Ratio> iRatios = new ArrayList<Ratio>();
    /**
     * The unmodified peptide sequence of this group.
     */
    protected String iPeptideSequence;
    /**
     * A reference to the parent RatioGroupCollection wherefrom this RatioGroup is part of.
     */
    protected RatioGroupCollection iParentCollection = null;
    /**
     * A String[] with the different protein accessions that are linked to this peptide identification
     */
    private String[] iProteinAccessions;
    /**
     * Boolean, true if a filter selected this RatioGroup
     */
    private boolean iSelected = false;
    /**
     * The razor protein accession
     */
    private String iRazorProteinAccession;

    /**
     * The start scan
     */
    private int iStartScan;
    /**
     * The end scan
     */
    private int iEndScan;
    /**
     * Vector with the light intensities
     */
    private Vector<Double> iLightIntensities;
    /**
     * Vector with the heavy intensities
     */
    private Vector<Double> iHeavyIntensities;
    /**
     * Vector with the scans
     */
    private Vector<Integer> iScans;

    /** Constructs a new RatioGroup. */ // Empty constructor.
    // Use the setters
    public CensusRatioGroup() {
    }

    public CensusRatioGroup(final RatioGroupCollection aRatioGroupCollection) {
        iParentCollection = aRatioGroupCollection;
    }

    /**
     * Getter for property 'parentCollection'.
     *
     * @return Value for property 'parentCollection'.
     */
    public RatioGroupCollection getParentCollection() {
        return iParentCollection;
    }

    /**
     * Getter for a specific peptide indentification by index
     * @param aIndex
     * @return PeptideIdentification
     */
    public PeptideIdentification getIdentification(int aIndex) {
        return iIdentifications.get(aIndex);
    }

    /**
     * Returns the Peptide Type at the given index in this group.
     * @param aIndex
     * @return
     */
    public String getPeptideType(int aIndex) {
        return iPeptideTypes.get(aIndex);
    }

    /**
     * Returns the
     * @param aIndex
     * @return
     */
    public Ratio getRatio(int aIndex) {
        return iRatios.get(aIndex);
    }

    /**
     * Getter for property 'numberOfIdentifications'.
     *
     * @return Value for property 'numberOfIdentifications'.
     */
    public int getNumberOfIdentifications(){
        return iIdentifications.size();
    }

    /**
     * Getter for property 'numberOfRatios'.
     *
     * @return Value for property 'numberOfRatios'.
     */
    public int getNumberOfRatios(){
        return iRatios.size();
    }

    /**
     * Getter for property 'numberOfTypes'.
     *
     * @return Value for property 'numberOfTypes'.
     */
    public int getNumberOfTypes(){
        return iPeptideTypes.size();
    }

    /**
     * Add a PeptideIdentification to the RatioGroup.
     * @param aIdentification The PeptideIdentification
     * @param aType The type of the given PeptideIdentification
     */
    public void addIdentification(PeptideIdentification aIdentification, String aType){
        aIdentification.setType(aType);
        iIdentifications.add(aIdentification);
        iPeptideTypes.add(aType);
    }

    /**
     * Add a ratio to this ratiogroup
     * @param aRatio
     */
    public void addRatio(Ratio aRatio){
        iRatios.add(aRatio);
    }

    /**
     * Get a ratio by ratio type (L/H, ...)
     * @param aType a ratio type
     * @return Ratio
     */
    public Ratio getRatioByType(String aType){
        Ratio lRatio = null;
        for(int i = 0; i<this.getNumberOfRatios(); i++){
            if(aType.equalsIgnoreCase(this.getRatio(i).getType())){
                lRatio = this.getRatio(i);
            }
        }
        return lRatio;
    }

    /**
     * Getter for property 'peptideSequence'.
     *
     * @return Value for property 'peptideSequence'.
     */
    public String getPeptideSequence() {
        return iPeptideSequence;
    }

        /**
     * Set the AbsoluteIntensities of this DistillerRatioGroup.
     * @param aRatioGroupAbsoluteIntensities The corresponding absolute intensities.
     */
    public void setRatioGroupAbsoluteIntensities(Double[] aRatioGroupAbsoluteIntensities) {
        this.iRatioGroupAbsoluteIntensities = aRatioGroupAbsoluteIntensities;
    }

    /**
     * Getter for the absolute intensities
     * @return Double[] with the intensities
     */
    public Double[] getAbsoluteIntensities(){
        return iRatioGroupAbsoluteIntensities;
    }


    /**
     * This method creates a JFreeChart with the intensities
     * @return JFreeChart
     */
    public JFreeChart getIntensityChart() {
        //create dateset

        // row keys...
        Vector<String> lRowKeys = iParentCollection.getComponentTypes();

        // column keys...
        String lColumn = "Absolute intensity";
        // create the dataset...
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(int i = 0; i<lRowKeys.size(); i ++){

            dataset.addValue(iRatioGroupAbsoluteIntensities[i], lRowKeys.get(i), lColumn);
        }

         // create the chart...
        JFreeChart chart = ChartFactory.createBarChart(
             "Absolute intensities", // chart title
             "", // domain axis label
             "", // range axis label
             dataset, // data
             PlotOrientation.VERTICAL, // orientation
             true, // include legend
             false, // tooltips?
             false // URLs?
         );

         // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

         // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

         // get a reference to the plot for further customisation...
        CategoryPlot plot = chart.getCategoryPlot();
         plot.setBackgroundPaint(Color.white);
         plot.setDomainGridlinePaint(Color.black);
         plot.setDomainGridlinesVisible(true);
         plot.setRangeGridlinePaint(Color.black);


         // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

         // disable bar outlines...
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);

        return chart;

     }

    /**
     * Setter for property 'peptideSequence'.
     *
     * @param aPeptideSequence Value to set for property 'peptideSequence'.
     */
    public void setPeptideSequence(final String aPeptideSequence) {
        iPeptideSequence = aPeptideSequence;
    }

    /**
     * Returns the PeptideIdentification for the given Type.
     * @param aType The type of the PeptideIdentification
     * @return The requested PeptideIdentification. <br><b>null if no match!</b>
     */
    public PeptideIdentification getIdentificationForType(String aType){
        // Iterate over all the types of the RatioGroup.
        for (int i = 0; i < iPeptideTypes.size(); i++) {
            String s = iPeptideTypes.get(i);
            if(s.equals(aType)){
                // Return the Identification that matches the type parameter.
                return iIdentifications.get(i);
            }
        }
        return null;
    }

    /**
     * This method gets the protein accessions (also isoforms) linked to the identifications.
     * @return String[] with the different protein accessions
     */
    public String[] getProteinAccessions(){
        if(iProteinAccessions == null){
            //create an vector to store the accessions in
            Vector<String> lAccessionVector = new Vector<String>();
            for(int i = 0; i<iIdentifications.size(); i++){
                //find the proteins for every identification linked to this ratio group
                if(iIdentifications.get(i) != null){
                    String lProtein = iIdentifications.get(i).getAccession();
                    String lIsoforms = iIdentifications.get(i).getIsoforms();
                    //check if the protein is already found
                    boolean lNewProtein = true;
                    for(int j = 0; j<lAccessionVector.size(); j ++){
                        if(lAccessionVector.get(j).equalsIgnoreCase(lProtein)){
                            lNewProtein = false;
                        }
                    }
                    if(lNewProtein){
                        lAccessionVector.add(lProtein);
                    }
                    //check for every isoform if the isoform is already found
                    if(lIsoforms.length() > 0){

                        lIsoforms = lIsoforms.replace("^A",",");
                        String[] lIsoformsFound = lIsoforms.split(",");
                        for(int j = 0; j<lIsoformsFound.length; j++){
                            String lIsoform = lIsoformsFound[j];
                            if(lIsoform.length() > 0){
                                lIsoform = lIsoform.substring(0,lIsoform.indexOf(" "));
                                boolean lNewIsoform = true;
                                for(int k = 0; k<lAccessionVector.size(); k ++){
                                    if(lAccessionVector.get(k).equalsIgnoreCase(lIsoform)){
                                        lNewIsoform = false;
                                    }
                                }
                                if(lNewIsoform){
                                    lAccessionVector.add(lIsoform);
                                }
                            }
                        }
                    }
                }
            }
            String[] lProteins = new String[lAccessionVector.size()];
            lAccessionVector.toArray(lProteins);
            iProteinAccessions = lProteins;
        }
        return iProteinAccessions;
    }

    /**
     * This method creates a JFreeChart for the XIC
     * @return JFreeChart
     * @throws IOException
     */
    public JFreeChart getXicChart() throws IOException {
        //create dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries seriesL = new XYSeries("L");
        for(int j = 0; j<iLightIntensities.size(); j ++){
            seriesL.add(iScans.get(j),iLightIntensities.get(j));
        }
        XYSeries seriesH = new XYSeries("H");
        for(int j = 0; j<iHeavyIntensities.size(); j ++){
            seriesH.add(iScans.get(j),iHeavyIntensities.get(j));
        }

        dataset.addSeries(seriesL);
        dataset.addSeries(seriesH);


        // create the chart...
        JFreeChart chart = ChartFactory.createXYLineChart(
                "XIC", // chart title
                "scan nr.", // x axis label
                "", // y axis label
                dataset, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                false, // tooltips
                false // urls
        );
        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);
        // get a reference to the plot for further customisation...
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.black);
        plot.setRangeGridlinePaint(Color.black);

        //paint confident interval
        Marker area = new IntervalMarker(iStartScan, iEndScan);
        area.setPaint(Color.pink);
        area.setAlpha(0.5f);
        area.setLabel("Area used to calculate ratio");
        area.setLabelAnchor(RectangleAnchor.BOTTOM);
        plot.addDomainMarker(area);


        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setShapesVisible(true);
        renderer.setShapesFilled(true);


        // change the auto tick unit selection to integer units only...
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
        return chart;
    }




    /**
     * This method will return all the protein accessions linked to this ratio group as one string.
     * The accessions are seperated by ", ".
     *
     * @return String with the protein accessions seperated by ", "
     */
    public String getProteinAccessionsAsString(){
        String[] lProteinAccessions = this.getProteinAccessions();
        String lAccessions = lProteinAccessions[0];
        for(int i = 1; i<lProteinAccessions.length; i ++){
            lAccessions = lAccessions + ", " + lProteinAccessions[i];
        }
        return lAccessions;
    }

    /**
     * Getter for property 'iSelected'.
     *
     * @return Value for property 'iSelected'.
     */
    public boolean isSelected() {
        return iSelected;
    }

    /**
     * Setter for property 'iSelected'.
     * @param aSelected
     */
    public void setSelected(boolean aSelected) {
        this.iSelected = aSelected;
    }

    /**
     * Getter for the razor protein accession
     * @return String with the protein accession of the razor protein accession
     */
    public String getRazorProteinAccession() {
        return iRazorProteinAccession;
    }

    /**
     * Setter for the razor protein accession
     * @param aRazorAccession String with the accession to set
     */
    public void setRazorProteinAccession(String aRazorAccession) {
        this.iRazorProteinAccession = aRazorAccession;
    }

    /**
     * This method will parse the chro part from the chro xml. It will extract the XIC and the scan numbers
     * @param aChro String with the chro from the xml
     */
    public void setChro(String aChro) {
        //parse the chro text

        //create the vectors
        iLightIntensities = new Vector<Double>();
        iHeavyIntensities = new Vector<Double>();
        iScans = new Vector<Integer>();

        //split the chro string
        String[] lLines = aChro.split(";");
        //set the start and end scan
        iStartScan = Integer.valueOf(lLines[0].substring(2, lLines[0].indexOf(" ", 3)).trim());
        iEndScan = Integer.valueOf(lLines[0].substring(lLines[0].indexOf(" ", 3)).trim());

        //find the scan and intensities in the rest of the lines
        for(int i = 1; i<lLines.length; i ++ ){
            String[] lSplittedLine = lLines[i].split(" ");
            iLightIntensities.add(Double.valueOf(lSplittedLine[1]));
            iHeavyIntensities.add(Double.valueOf(lSplittedLine[2]));
            iScans.add(Integer.valueOf(lSplittedLine[0]));
        }
    }
}
