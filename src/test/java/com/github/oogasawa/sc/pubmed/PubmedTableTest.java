package com.github.oogasawa.sc.pubmed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import com.github.oogasawa.utility.sc.paper.PaperInfo;
import com.github.oogasawa.utility.sc.pubmed.PubmedTableRow;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

 

public class PubmedTableTest {

    private static final Logger logger = Logger.getLogger("oogasawa.utility.sc.PubmedTableTest");

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Nested
    class UnitTest {

        /** An original table file. (e.g. {@code $HOME/paper_list.utf8.txt}) */
        static Path origTablePath;

        static List<String> pmidList;


        
        /** Creates temporary directory in which test documents are located.
         */
        @BeforeAll
        static void setUpClass() {
            try {
                // Read logging configuration.
                LogManager.getLogManager().readConfiguration(PubmedTableTest.class.getClassLoader().getResourceAsStream("logging.properties"));


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
        public void should_gather_pmids() {

            Pattern pPmid = Pattern.compile("([0-9]+)");
            List<String> pmidList;
            
            try {
                pmidList =
                    Files.lines(UnitTest.origTablePath)
                    .skip(1)
                    .map(l->{return new PaperInfo(l);})
                    .filter(paperInfo->{
                            String pmid = paperInfo.getPubmedId();
                            if (pmid.length() > 0) {
                                Matcher m = pPmid.matcher(pmid);
                                if (m.matches()) {
                                    return true;
                                }
                            }
                            return false;
                        })
                    .map(paperInfo->paperInfo.getPubmedId())
                    .collect(Collectors.toList());

                logger.info(String.format("number of pmid rows: %d", pmidList.size()));
                assertTrue(pmidList.size() > 5);

                UnitTest.pmidList = pmidList;
                
            } catch (IOException e) {
                logger.log(Level.SEVERE, "", e);
            }

            
        }
        
        @Disabled
        @Test
        @Order(4)
        public void should_fetch_Pubmed_data() {

            try {
                for (int i=0; i<3; i++) {
                
                    String pmid = UnitTest.pmidList.get(i);
                    PubmedTableRow row = new PubmedTableRow();
                    String xml = row.efetch(pmid);

                    //System.out.println(xml);
                    
                    assertTrue(xml.startsWith("<?xml version=\"1.0\" ?>"));
                    
                    Thread.sleep(5000);
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected Error.", e);
            }
            
        }
        


        @Test
        @Order(5)
        public void shoud_parse_simple_tag() {
            String xml = "<Article PubModel=\"Print\"><Journal><ISSN IssnType=\"Electronic\">1574-6941</ISSN><JournalIssue CitedMedium=\"Internet\"><Volume>98</Volume><Issue>1</Issue><PubDate><Year>2022</Year><Month>Feb</Month><Day>10</Day></PubDate></JournalIssue><Title>FEMS microbiology ecology</Title><ISOAbbreviation>FEMS Microbiol Ecol</ISOAbbreviation></Journal><ArticleTitle>16S rRNA gene sequence diversity in Faecalibacterium prausnitzii-complex taxa has marked impacts on quantitative analysis.</ArticleTitle><ELocationID EIdType=\"pii\" ValidYN=\"Y\">fiac004</ELocationID><ELocationID EIdType=\"doi\" ValidYN=\"Y\">10.1093/femsec/fiac004</ELocationID><Abstract><AbstractText>Faecalibacterium prausnitzii has been suggested as a biomarker of a healthy microbiota in human adults. Here, we report a taxonomic study of F. prausnitzii using genomic information and evaluation of the quantitative real-time PCR (qPCR) assay by focusing on specific primers to quantify its population. Average nucleotide identity values revealed that strains deposited as F. prausnitzii in a public database were separated into eight genomogroups with significant differences at the species level. A total of six of the 10 primer pairs used in the previous studies for qPCR of F. prausnitzii contained sequence mismatches to 16S rRNA gene sequences of the tested strains with markedly different levels by in silico analysis. In vitro primer evaluation by qPCR generally agreed with the in silico analysis, and markedly reduced amount of DNA was recorded by qPCR in combination with the primer pairs containing sequence mismatches. The present study demonstrated that a part of the accumulated knowledge on F. prausnitzii is maybe based on biased results.</AbstractText><CopyrightInformation>&#xa9; The Author(s) 2022. Published by Oxford University Press on behalf of FEMS.</CopyrightInformation></Abstract><AuthorList CompleteYN=\"Y\"><Author ValidYN=\"Y\"><LastName>Tanno</LastName><ForeName>Hiroki</ForeName><Initials>H</Initials><AffiliationInfo><Affiliation>Department of Food, Aroma and Cosmetic Chemistry, Faculty of Bioindustry, Tokyo University of Agriculture, 196 Yasaka, Abashiri, Hokkaido 099-2493, Japan.</Affiliation></AffiliationInfo></Author><Author ValidYN=\"Y\"><LastName>Maeno</LastName><ForeName>Shintaro</ForeName><Initials>S</Initials><AffiliationInfo><Affiliation>Department of Food, Aroma and Cosmetic Chemistry, Faculty of Bioindustry, Tokyo University of Agriculture, 196 Yasaka, Abashiri, Hokkaido 099-2493, Japan.</Affiliation></AffiliationInfo></Author><Author ValidYN=\"Y\"><LastName>Salminen</LastName><ForeName>Seppo</ForeName><Initials>S</Initials><AffiliationInfo><Affiliation>Functional Food Forum, University of Turku, 20014 Turku, Finland.</Affiliation></AffiliationInfo></Author><Author ValidYN=\"Y\"><LastName>Gueimonde</LastName><ForeName>Miguel</ForeName><Initials>M</Initials><AffiliationInfo><Affiliation>Department of Microbiology and Biochemistry of Dairy Products, IPLA-CSIC, 33300 Villaviciosa, Spain.</Affiliation></AffiliationInfo></Author><Author ValidYN=\"Y\"><LastName>Endo</LastName><ForeName>Akihito</ForeName><Initials>A</Initials><Identifier Source=\"ORCID\">0000-0002-7108-1202</Identifier><AffiliationInfo><Affiliation>Department of Food, Aroma and Cosmetic Chemistry, Faculty of Bioindustry, Tokyo University of Agriculture, 196 Yasaka, Abashiri, Hokkaido 099-2493, Japan.</Affiliation></AffiliationInfo></Author></AuthorList><Language>eng</Language><PublicationTypeList><PublicationType UI=\"D016428\">Journal Article</PublicationType><PublicationType UI=\"D013485\">Research Support, Non-U.S. Gov't</PublicationType></PublicationTypeList></Article>";
            PubmedTableRow row = new PubmedTableRow();
            try {
                row.parse(xml);
                logger.info("pages: " + row.getPages());
                
            } catch (UnsupportedEncodingException | XMLStreamException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        @Test
        @Order(6)
        public void shoud_parse_tag_with_multiple_attributes() {
            
        }

        
        @Disabled
        @Test
        @Order(5)
        public void should_parse_Pubmed_data() {

            try {
                for (int i=0; i<1; i++) {
                
                    String pmid = UnitTest.pmidList.get(i);
                    PubmedTableRow row = new PubmedTableRow();
                    row.parse(row.efetch(pmid));
                    
                    System.out.println(row.toTSV());
                    
                    Thread.sleep(5000);
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected Error.", e);
            }
            

            
        }


        public String removeQuotations(String str) {
            Pattern pStr = Pattern.compile("\"(.+)\"");
            String result = "";
            Matcher m = pStr.matcher(str);
            if (m.matches()) {
                result = m.group(1);
            }
            return result;
        }


        @Disabled
        @Test
        @Order(6)
        public void should_make_final_table() {

            Pattern pPmid = Pattern.compile("([0-9]+)");
            
            try {
                    Files.lines(UnitTest.origTablePath)
                    .skip(1)
                    .limit(3)
                    .map(l->{return new PaperInfo(l);})
                    .filter(paperInfo->{
                            String pmid = paperInfo.getPubmedId();
                            if (pmid.length() > 0) {
                                Matcher m = pPmid.matcher(pmid);
                                if (m.matches()) {
                                    return true;
                                }
                            }
                            return false;
                        })
                    .forEach(paperInfo->{

                                try {

                                    String pmid = paperInfo.getPubmedId();
                                    PubmedTableRow row = new PubmedTableRow();
                                    row.parse(row.efetch(pmid));

                                    StringJoiner joiner = new StringJoiner("\t");
                                    joiner.add(paperInfo.getAccountNameJa());
                                    joiner.add(removeQuotations(paperInfo.getAccountNameEn()));
                                    joiner.add(row.toTSV());

                                    System.out.println(joiner.toString());
                                    Thread.sleep(10000);

                                } catch (Exception e) {
                                    logger.log(Level.SEVERE, "Unexpected exception", e);
                                }
                            });


                
            } catch (IOException e) {
                logger.log(Level.SEVERE, "", e);
            }
        }


    } // end of the nested class.


    
}
