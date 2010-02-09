package com.compomics.rover.general.sequenceretriever;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: niklaas
 * Date: 23-dec-2008
 * Time: 8:32:44
 */

/**
 * This class can retrieve the protein sequence for an UniProt protien accession
 */
public class UniprotSequenceRetriever {

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
     * @param aUniprotAccession Protein accession
     * @throws Exception
     */
    public UniprotSequenceRetriever(String aUniprotAccession) throws Exception {
        iSequence = readSequenceUrl("http://www.uniprot.org/uniprot/" + aUniprotAccession + ".fasta");
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
        for (int j = 1; j < lLines.length; j++) {
            sequence = sequence + lLines[j];
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

}
