package com.compomics.rover.general.sequenceretriever;

import org.apache.log4j.Logger;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedInputStream;
import java.io.Reader;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: Niklaas
 * Date: 08-Feb-2010
 * Time: 08:51:17
 * To change this template use File | Settings | File Templates.
 */
public class MipsCygdSequenceRetriever {
	// Class specific log4j logger for MipsCygdSequenceRetriever instances.
	 private static Logger logger = Logger.getLogger(MipsCygdSequenceRetriever.class);
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
     * @param aMipsCygd Protein accession
     * @throws Exception
     */
    public MipsCygdSequenceRetriever(String aMipsCygd) throws Exception {
        aMipsCygd = aMipsCygd.substring(0, aMipsCygd.length() - 1) + (String.valueOf(aMipsCygd.charAt(aMipsCygd.length() - 1)).toLowerCase());
        iSequence = readSequenceUrl("http://mips.helmholtz-muenchen.de/genre/proj/yeast/Search/sequence_view_sc_cgi.jsp?entry=" + aMipsCygd);
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

        String lTempSequence = "";

        String[] lLines = inputString.split("\n");
        boolean lSequenceStarted = false;
        for (int j = 0; j < lLines.length; j++) {
            if (lSequenceStarted) {
                    lTempSequence = lTempSequence + lLines[j];
            } else if (lLines[j].indexOf("Protein Sequence:") >= 0) {
                lSequenceStarted = true;
                lTempSequence = lLines[j];
            }
        }

        sequence = lTempSequence.substring(lTempSequence.indexOf("<textarea name=\"new_pep\" cols=\"70\" rows=\"15\" wrap=\"soft\">") + 57, lTempSequence.indexOf("</textarea></font></td></tr></td></tr></table></td></tr></table><div></div></form>"));

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
            MipsCygdSequenceRetriever retrieve = new MipsCygdSequenceRetriever("YDR233C");
            System.out.println(retrieve.getSequence());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
