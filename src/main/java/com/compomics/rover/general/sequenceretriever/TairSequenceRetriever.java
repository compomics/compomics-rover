package com.compomics.rover.general.sequenceretriever;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 16-Nov-2009
 * Time: 15:10:40
 * To change this template use File | Settings | File Templates.
 */
public class TairSequenceRetriever {
/**
     * The protein sequence
     */
    private String iSequence = null;
    /**
     * The number of times the sequence retrieving was retried
     */
    private int iRetry = 0;

    /**
     * Constructor
     *
     * @param aTairAccession Protein accession
     * @throws Exception
     */
    public TairSequenceRetriever(String aTairAccession) throws Exception {
        iSequence = readSequenceUrl("http://www.arabidopsis.org/servlets/TairObject?type=aa_sequence&name=" + aTairAccession);
    }

    /**
     * This method reads a url a tries to extrect the protein sequence
     *
     * @param aUrl String with the url
     * @return String with the protein sequence
     * @throws Exception
     */
    public String readSequenceUrl(String aUrl) throws Exception {
        String sequence = "";

        URL myURL = new URL(aUrl);
        StringBuilder input = new StringBuilder();
        HttpURLConnection c = (HttpURLConnection) myURL.openConnection();
        BufferedInputStream in = new BufferedInputStream(c.getInputStream());
        Reader r = new InputStreamReader(in);

        int i;
        while ((i = r.read()) != -1) {
            input.append((char) i);
        }

        String inputString = input.toString();


        String[] lLines = inputString.split("\n");
        boolean lSequenceStarted = false;
        for (int j = 0; j < lLines.length; j++) {
            if (lSequenceStarted) {
                if(lLines[j].indexOf("gene-locus")>=0){
                    lSequenceStarted = false;
                    j = lLines.length;
                } else {
                    if(lLines[j].indexOf("hidden\" name=\"sequence\" value=\"")>=0){
                        sequence = sequence + lLines[j].substring(lLines[j].indexOf("value") + 8, lLines[j].lastIndexOf("\"") );
                    }
                }
            } else if (lLines[j].indexOf("     <th class=sm align=left valign=top>Sequence")>=0) {
                lSequenceStarted = true;
            }
        }
      

        if (sequence.length() == 0) {
            if(iRetry < 5){
                iRetry = iRetry + 1;
                sequence = readSequenceUrl(aUrl);
            } else {
                sequence = null;
            }

        }
        return sequence;
    }

    /**
     * Getter for the protein sequence
     *
     * @return String with protein sequence
     */
    public String getSequence() {
        return iSequence;
    }

    public static void main(String[] args) {
        try {
            TairSequenceRetriever retrieve = new TairSequenceRetriever("AT1G01750.1");
            System.out.println(retrieve.getSequence());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
