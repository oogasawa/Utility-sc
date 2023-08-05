package com.github.oogasawa.utility.sc.tsv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.oogasawa.utility.sc.paper.PaperInfo;

public class TableChecker {


    private static final Logger logger = Logger.getLogger(TableChecker.class.getName());
    
    
    /** Check if the number of columns in the table is normal
     * 
     */ 
    public static void check(Path tablePath) {

        int lineNo = 0;
        try {
            List<String> lines = Files.readAllLines(tablePath);
            for (String line : lines) {

                List<String> cols = PaperInfo.splitByTab(line);
                //
                lineNo++;
                if (cols.size() != 16) {
                    logger.warning(String.format("%02d\t%d\t%s\t%s", lineNo, cols.size(), cols.get(0), cols.get(1)));
                }
                // assertTrue(cols.size() == 16);
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception occurred when loading data.", e);
        }

    }

    
}
