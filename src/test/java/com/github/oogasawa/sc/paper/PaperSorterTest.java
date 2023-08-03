package com.github.oogasawa.sc.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.oogasawa.utility.sc.paper.PaperInfo;
import com.github.oogasawa.utility.sc.paper.PaperSorter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

 

public class PaperSorterTest {

    private static final Logger logger = Logger.getLogger("oogasawa.utility.sc.PaperSorterTest");

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Nested
    class UnitTest {

        /** An original table file. (e.g. {@code $HOME/paper_list.utf8.txt}) */
        static Path origTablePath;
        
        /** Creates temporary directory in which test documents are located.
         */
        @BeforeAll
        static void setUpClass() {
            try {
                // Read logging configuration.
                LogManager.getLogManager().readConfiguration(PaperSorterTest.class.getClassLoader().getResourceAsStream("logging.properties"));


                origTablePath = Path.of(System.getenv("PWD")).resolve("paper_list.utf8.txt");
                
            } catch (IOException e) {
                logger.log(Level.SEVERE, "could not create tmpdir", e);
            }            
        }


        @Test
        @Order(1)
        public void should_exist_test_data_file() {

            assertTrue(UnitTest.origTablePath.toFile().exists());
            
        }


        /** Checks if all lines of the data file have the same number of columns.
         * 
         */ 
        @Test
        @Order(2)
        public void should_have_the_same_number_of_columns() {

            int lineNo = 0;
            try {
                List<String> lines = Files.readAllLines(origTablePath);
                for (String line: lines) {
                    
                    List<String> cols = PaperInfo.splitByTab(line);
                    //
                    lineNo++;
                    if (cols.size() != 16) {
                        logger.warning(String.format("%02d\t%d\t%s\t%s", lineNo, cols.size(), cols.get(0), cols.get(1)));
                    }
                    assertTrue(cols.size() == 16);
                }


            } catch (IOException e) {
                logger.log(Level.SEVERE, "Exception occurred when loading data.", e);
            }
                        
        }


        @Test
        @Order(3)
        public void skip_method_example() {
            List<Integer> result
                = Stream.of(1,2,3,4,5)
                .skip(1)
                .collect(Collectors.toList());

            List<Integer> answer = new ArrayList<Integer>();
            for (int i=2; i<=5; i++) {
                answer.add(i);
            }

            assertEquals(result, answer);
        }
        

        @Test
        @Order(4)
        public void should_collect_rows_with_pmid() {

            PaperSorter sorter = new PaperSorter.Builder(origTablePath).build();
            List<PaperInfo> rowsWithPMID = sorter.sortPmidRows();
            logger.info(String.format("Rows with PMID: %d", rowsWithPMID.size()));

            assertTrue(rowsWithPMID.size() > 5);

        }

        

        @Test
        @Order(5)
        public void should_collect_rows_with_doi() {

            PaperSorter sorter = new PaperSorter.Builder(origTablePath).build();
            List<PaperInfo> rowsWithDoi = sorter.sortDoiRows();
            logger.info(String.format("Rows with DOI: %d", rowsWithDoi.size()));

            assertTrue(rowsWithDoi.size() > 5);
            
        }


        
        @Test
        @Order(6)
        public void should_collect_other_rows() {

            PaperSorter sorter = new PaperSorter.Builder(origTablePath).build();
            List<PaperInfo> otherRows = sorter.sortOtherRows();
            logger.info(String.format("Rows with DOI: %d", otherRows.size()));

            assertTrue(otherRows.size() > 5);

        }


        

        

    } // end of the nested class.


    
}
