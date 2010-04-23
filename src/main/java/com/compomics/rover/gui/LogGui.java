package com.compomics.rover.gui;

import com.compomics.rover.general.singelton.Log;

import javax.swing.*;
import java.util.Observer;
import java.util.Observable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 27-Apr-2009
 * Time: 11:19:20
 */
public class LogGui extends JFrame implements Observer {
    private JTextArea textArea1;
    private JButton closeLogButton;
    private JPanel jpanContent;

    /**
     * The log
     */
    private Log iLog = Log.getInstance();


    public LogGui() {
        super("Log");

        $$$setupUI$$$();

        //add the observer
        iLog.addObserver(this);

        //get the log
        textArea1.setText(iLog.getLog());

        //create the  jframe
        this.setContentPane(jpanContent);
        this.setSize(400, 300);
        this.setLocation(250, 250);
        this.setVisible(true);

        closeLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
    }

    public void update(Observable o, Object arg) {
        textArea1.setText(iLog.getLog());
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
        final JScrollPane scrollPane1 = new JScrollPane();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(scrollPane1, gbc);
        textArea1 = new JTextArea();
        scrollPane1.setViewportView(textArea1);
        closeLogButton = new JButton();
        closeLogButton.setText("Close log");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        jpanContent.add(closeLogButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return jpanContent;
    }
}