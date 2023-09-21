package com.github.oogasawa.utility.sc.apt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** Filter {@code apt search} results to display the content you need.
 *
 * <p>Data format (output of {@code apt search})</p>
 *
 * <pre>
 * python3/jammy-updates,jammy-security,now 3.10.6-1~22.04 amd64 [installed]
 *   interactive high-level object-oriented language (default python3 version)
 *
 * python-is-python3/jammy,jammy,now 3.9.2-2 all [installed]
 *   symlinks /usr/bin/python to python3
 * </pre>
 *

 * 
 * @param aptPackages A list of package names to install.
 */
public class AptSearcher {
    private static final Logger logger = Logger.getLogger(AptSearcher.class.getName());


    /** Execute {@code apt search} command.
     * 
     * @param query A query string of {@code apt search}.
     */
    public static void aptSearch(String query) {
        Process p;
        try {
            p = new ProcessBuilder("apt", "search", query)
                .start();

            BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream()));

            p.waitFor();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error occured while installing: " + query, e);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted", e);
        }
    }


    

    /** Print the output of apt search to standard output
     * that exactly matches the package name given in the argument.
     *
     * @param is A BufferedReader object that reads the output of apt search.
     * @param pkgPattern A Pattern object that represents package names.
     */
    public static void filter(BufferedReader is, Pattern pkgPattern) {
        
        String line;
        
        try {
            int status = 0;
            while ((line = is.readLine()) != null) {

                Matcher m = pkgPattern.matcher(line);
                if (m.find()) { // match the pkgName
                    System.out.println(line);
                    status = 1;
                    continue;
                }


                // It is assumed that the information output
                // by apt search consists of two lines.
                if (status == 1) { 
                    System.out.println(line + "\n");
                    status = 0;
                    continue;
                }

            }

        } catch (IOException e) {
            logger.log(Level.SEVERE,
                       String.format("Can not read line from a BufferedReader object: %s", is.toString()), e);
        }

    }


    
    /** Print the output of apt search to standard output
     * that exactly matches the package name given in the argument.
     *
     * @param is A BufferedReader object that reads the output of apt search.
     * @param pkgName A list of package name patterns.
     */
    public static void filter(BufferedReader is, List<String> pkgList) {
        String line;


        
        try {
            int status = 0;
            while ((line = is.readLine()) != null) {
                
                for (String pkgName: pkgList) {
                    logger.info(String.format("regexp: %s", pkgList));

                    Pattern p = Pattern.compile(pkgName);

                    Matcher m = p.matcher(line);
                    
                    if (m.find()) {
                        System.out.println(line);
                        status = 1;
                        break;
                    }

                }

                // It is assumed that the information output
                // by apt search consists of two lines.
                if (status == 1) { 
                    System.out.println(line + "\n");
                    status = 0;
                    continue;
                }

            }

        } catch (IOException e) {
            logger.log(Level.SEVERE,
                       String.format("Can not read line from a BufferedReader object: %s", is.toString()), e);
        }
        
    }



    public static void list(String category) {
        if (category.equals("r-cran")) {
            aptSearch("r-cran-*");
        }
    }
    

    
}
