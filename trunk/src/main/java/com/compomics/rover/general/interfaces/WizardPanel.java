package com.compomics.rover.general.interfaces;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 19-mrt-2009
 * Time: 9:19:09
 */
public interface WizardPanel {

    /**
     * This method gives the  JPanel with all the content of this wizard panel
     * @return JPanel
     */
    public JPanel getContentPane();

    /**
     * This method will be performed when the back button is clicked
     */
    public void backClicked();

    /**
     * This method will be performed when the next button is clicked
     */
    public void nextClicked();

    /**
     * This method will give a boolean that status if it's ok to go to the next panel
     * @return boolean that indicates if it's feasable to proceed
     */
    public boolean feasableToProceed();

    /**
     * This method gives a String with reasons why it's not ok to go to the next panel
     * @return String with reasons
     */
    public String getNotFeasableReason();

    /**
     * This method will construct things that are not done in the normal panel constructor
     */
    public void construct();
}
