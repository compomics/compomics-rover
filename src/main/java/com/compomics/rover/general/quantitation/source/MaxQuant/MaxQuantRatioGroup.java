package com.compomics.rover.general.quantitation.source.MaxQuant;

import com.compomics.rover.general.quantitation.RatioGroup;
import com.compomics.rover.general.quantitation.RatioGroupCollection;
import com.compomics.rover.general.quantitation.RatioType;
import com.compomics.rover.general.singelton.QuantitativeValidationSingelton;
import com.compomics.rover.general.interfaces.Ratio;

import java.util.ArrayList;
import java.util.Vector;
import java.awt.*;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 20-Apr-2009
 * Time: 08:57:45
 */

/**
 * The MaxQuant RatioGroup
 */
public class MaxQuantRatioGroup  extends RatioGroup {

    /**
     * The absolute intensities found in the DistillerRatioGroup in the distiller quantitation file.
     */
    private Double[] iRatioGroupAbsoluteIntensities;
     /**
     * This distiller validation singelton holds information for the calculation of the ratio
     */
    private QuantitativeValidationSingelton iQuantitativeValidationSingelton = QuantitativeValidationSingelton.getInstance();
    /**
     * The posterior erro probability score for this ratiogroup
     */
    private double iPEP;

    /**
     * Creates a DistillerRatioGroup implementing the RatioGroup interfaces
     * as well as the logic to tie up the Distiller structure to ms_lims Identifcation instances.
     * @param aRatioGroupCollection The RatioGroupCollection wherein this DistillerRatioGroup resides.
     */
    public MaxQuantRatioGroup(final RatioGroupCollection aRatioGroupCollection, double aPEP){
        super(aRatioGroupCollection);
        this.iPEP = aPEP;
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
     * @return Double[] with the absolute intensities
     */
    public Double[] getAbsoluteIntensities(){
        return iRatioGroupAbsoluteIntensities;
    }


    public double getSummedIntensityForRatioType(String aType){
        Vector<RatioType> iRatioTypes =  iQuantitativeValidationSingelton.getMatchedRatioTypes();
        RatioType lType = null;

        for(int i = 0; i<iRatioTypes.size(); i ++){
            if(iRatioTypes.get(i).getType().equalsIgnoreCase(aType)){
                lType = iRatioTypes.get(i);
            }
        }

        double lSum = 0.0;

        for(int i = 0; i<iQuantitativeValidationSingelton.getComponentTypes().size(); i++){
            for(int j = 0; j<lType.getComponents().length; j ++){
                if(lType.getComponents()[j].equalsIgnoreCase(iQuantitativeValidationSingelton.getComponentTypes().get(i))){
                    lSum = lSum + iRatioGroupAbsoluteIntensities[i];
                }
            }
        }
        return lSum;
    }


    /**
     * This method create a JFreeChart for the absolute intensities
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
     * To String method
     * @return String
     */
    public String toString(){
        String lTitle = "";
        // row keys...
        Vector<String> lTypes = iParentCollection.getRatioTypes();
        for(int i = 0; i<lTypes.size(); i ++){
            Ratio lRatio = getRatioByType(lTypes.get(i));
            if(lRatio !=  null){
                lTitle = lTitle + " " + lTypes.get(i) + ": " + this.getRatioByType(lTypes.get(i)).getRatio(iQuantitativeValidationSingelton.isLog2());
            } else {
                lTitle = lTitle + " " + lTypes.get(i) + ": /";
            }
        }
        return lTitle;
    }

    /**
     * This method is the getter for the PEP (posterior error probability score)
     * @return double with PEP
     */
    public double getPEP(){
        return iPEP;
    }

}
