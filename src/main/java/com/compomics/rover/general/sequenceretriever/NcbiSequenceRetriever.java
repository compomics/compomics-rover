package com.compomics.rover.general.sequenceretriever;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 26-jan-2009
 * Time: 15:21:31
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class can retrieve the protein sequence for an NCBI protien accession
 */
public class NcbiSequenceRetriever {
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
     * @param aIpiAccession Protein accession
     * @throws Exception
     */
    public NcbiSequenceRetriever(String aIpiAccession) throws Exception {
        iSequence = readSequenceUrl("http://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=protein&id=" + aIpiAccession  + "&rettype=fasta");
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
                if (lLines[j].startsWith("<")) {
                    lSequenceStarted = false;
                    j = lLines.length;
                } else {
                    //add the sequence
                    sequence = sequence + lLines[j].replace(" ", "");
                }
            } else if (lLines[j].startsWith(">")) {
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
            NcbiSequenceRetriever retrieve = new NcbiSequenceRetriever("62473526");
            System.out.println(retrieve.getSequence());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}


