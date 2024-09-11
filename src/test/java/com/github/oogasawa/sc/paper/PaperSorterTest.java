package com.github.oogasawa.sc.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.oogasawa.utility.sc.paper.PaperInfo;
import com.github.oogasawa.utility.sc.paper.PaperSorter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

public class PaperSorterTest {

    private static final Logger logger = LoggerFactory.getLogger(PaperSorterTest.class);

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
            origTablePath = Path.of(System.getenv("PWD")).resolve("paper_list.utf8.txt");
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
                        logger.warn(String.format("%02d\t%d\t%s\t%s", lineNo, cols.size(), cols.get(0), cols.get(1)));
                    }
                    assertTrue(cols.size() == 16);
                }


            } catch (IOException e) {
                logger.error("Exception occurred when loading data.", e);
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
