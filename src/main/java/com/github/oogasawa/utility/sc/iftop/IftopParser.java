package com.github.oogasawa.utility.sc.iftop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A class that parses the output of the iftop command to obtain the time variation of the amount transferred.
 * 
 * <p>
 * Running the {@code iftop} command with the {@code -t} option produces the following output.
 * (One table is output every 2 seconds.)
 * </p>
 * 
 * <pre>{@code 
 * Listening on enp3s0
 *    # Host name (port/service if enabled)            last 2s   last 10s   last 40s cumulative
 * --------------------------------------------------------------------------------------------
 *    1 stonefly501                              =>     5.39Mb     5.39Mb     5.39Mb     1.35MB
 *      gw2.ddbj.nig.ac.jp                       <=      821Mb      821Mb      821Mb      205MB
 *    2 stonefly501                              =>     2.08Kb     2.08Kb     2.08Kb       532B
 *      dns.google                               <=     2.22Kb     2.22Kb     2.22Kb       568B
 *    3 stonefly501                              =>       208b       208b       208b        52B
 *      nrt12s36-in-f10.1e100.net                <=       860b       860b       860b       215B
 *    4 stonefly501                              =>       208b       208b       208b        52B
 *      192.168.210.1                            <=       768b       768b       768b       192B
 * --------------------------------------------------------------------------------------------
 * Total send rate:                                     5.39Mb     5.39Mb     5.39Mb
 * Total receive rate:                                   821Mb      821Mb      821Mb
 * Total send and receive rate:                          826Mb      826Mb      826Mb
 * --------------------------------------------------------------------------------------------
 * Peak rate (sent/received/total):                     5.39Mb      821Mb      826Mb
 * Cumulative (sent/received/total):                    1.35MB      205MB      207MB
 * ============================================================================================
 * 
 *    # Host name (port/service if enabled)            last 2s   last 10s   last 40s cumulative
 * --------------------------------------------------------------------------------------------
 *    1 stonefly501                              =>     4.40Mb     4.89Mb     4.89Mb     2.45MB
 *      gw2.ddbj.nig.ac.jp                       <=      686Mb      753Mb      753Mb      377MB
 *    2 stonefly501                              =>     23.1Kb     11.7Kb     11.7Kb     5.83KB
 *      192.168.210.1                            <=     1.77Kb     1.26Kb     1.26Kb       644B
 *    3 stonefly501                              =>     16.1Kb     8.04Kb     8.04Kb     4.02KB
 * }</pre>
 * 
 * <p>
 * From this output, take the speed of downloads from, for example, {@code gw2.ddbj.nig.ac.jp} and make a table.
 * </p>
 * 
 * 
 */
public class IftopParser {

     private static final Logger logger = Logger.getLogger(IftopParser.class.getName());
    
    /** Make a table of download speeds.
     *
     * This method collects the values of last 40 sec and make a table from the folling lines .
     *
     * <pre>{@code 
     *     gw2.ddbj.nig.ac.jp                       <=      821Mb      821Mb      821Mb      205MB
     * }</pre>
     */
    public void makeDownloadSpeedTable(String fqdn, File infile, File outfile) {
        try (BufferedReader br = new BufferedReader(new FileReader(infile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outfile))) {


            Pattern p =  Pattern.compile(fqdn + "\\s+<=\\s+[0-9]+[KMG]b\\s+[0-9]+[KMB]b\\s+([0-9]+)[KMG]b");

            int second = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    second +=2;
                    String speed = m.group(1);
                    bw.write(String.format("%d\t%s\n", second, speed));
                }
            }
        }
        catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "File not found", e);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "IOException", e);
        }
    }

    
}
