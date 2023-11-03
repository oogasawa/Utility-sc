package com.github.oogasawa.utility.sc.apt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class AptFormatter {

    private static final Logger logger = Logger.getLogger(AptFormatter.class.getName());

    public static void format(BufferedReader is) {
        String line = null;
        StringBuilder strb = new StringBuilder();

        Pattern p = Pattern.compile("apt\\s+install\\s+-y\\s+(\\S+)");
        
        try {
            while ((line = is.readLine()) != null) {
                Matcher m = p.matcher(line);
                String pkgName = null;
                if (m.find()) {
                    pkgName = m.group(1);
                    printPackageInfo(pkgName);
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static void printPackageInfo(String pkgName) {

        Process p = null;
        try {
            p = new ProcessBuilder("apt", "list", pkgName)
                    .inheritIO()
                    .start();

            p.waitFor();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error occured while searching: " + pkgName, e);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted", e);
        }

    }

}
