package com.github.oogasawa.utility.sc.paper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;



/**
 * 
 * <p>
 * The NIG supercomputer asks users to declare a list of their publications when they apply for end-of-year renewal.
 * The application for the end-of-year renewal is made using the "User Application System.
 * The list of publications can be output in tabular form (Excel file) from the "User Application System.
 * </p>
 *
 * <p>
 * However, this Excel file contains many cases where the PubMed ID is not written, the Journal name is not written, and so on.
 * The PaperSorter class is a simple and easy way to create a list of articles in a tabular format (Excel file).
 * </p>
 * <p>
 * In order to simplify the task of creating a list of published papers for the NIG supercomputer website,
 * the PaperSorter class classifies and sorts each row of the Excel file in the following order:
 * </p>
 * <ol>
 * <li>Data with PubMed IDs</li>
 * <li>data without PubMed ID but with correct journal name</li>
 * <li>data with neither PubMed ID nor journal name, but with DOI</li>
 * <li>Other data</li>
 * </ol>
 *
 * <hr style="border: none; border-top: 1px dotted #999;" />
 * 
 * <p>
 * 遺伝研スパコンでは年度末更新申請の際に、ユーザーに発表論文のリストを申告してもらっている。
 * 年度末更新申請は「利用申請システム」を用いて行われる。
 * 「利用申請システム」から発表論文のリストを表形式（Excelファイル）で出力することができる。
 * </p>
 *
 * <p>
 * しかし、このExcelファイルにはPubMed IDが書かれていない場合、Journal名が書かれていない場合など
 * 不完全なデータが多数含まれる。
 * </p>
 * <p>
 * PaperSorterクラスは、遺伝研スパコンホームページの発表論文リストを作る作業を簡単にするために
 * Excelファイルの各行を分類し以下の順番に並べ替える。
 * </p>
 * 
 * <ol>
 * <li>PubMed IDが書かれているデータ</li>
 * <li>PubMed IDが書かれていないが、正しくJournal名が書かれているデータ</li>
 * <li>PubMed IDもJournal名も書かれていないがDOIが書かれているデータ</li>
 * <li>それ以外のデータ</li>
 * </ol>
 *
 * 
 * 
 */
public class PaperSorter {

    private static final Logger logger = Logger.getLogger(PaperSorter.class.getName());
    
    /** A data file path. */
    Path infile;

    static public class Builder {

        /** A data file path. */
        Path infile;
        

        public Builder(Path infile) {
            if (infile == null)
                throw new NullPointerException();

            this.infile = infile;
        }

        
        public PaperSorter build() {
            PaperSorter sorter = new PaperSorter();
            sorter.setInfile(infile);
            
            return sorter;
        }
    }


    public List<PaperInfo> sort() {

        List<PaperInfo> results = new ArrayList<PaperInfo>();

        // 1. Sort rows
        results.addAll(sortPmidRows());
        results.addAll(sortDoiRows());
        results.addAll(sortOtherRows());

        // 2. Print out the results.
        results
            .stream()
            .forEach((info)->{
                    System.out.println(info.toTSV());
                });

        // 3. return the results.
        return results;
        
    }



    public List<PaperInfo> sortPmidRows() {

        List<PaperInfo> pmidRows = null;
        
        try {
            pmidRows
                = Files.lines(this.infile)
                .skip(1)
                .map(l -> {
                        return new PaperInfo(l);
                    })
                .filter(p -> {
                        return !p.getPubmedId().trim().isEmpty();
                    })
                .sorted((a, b)->{return a.getPubmedId().compareTo(b.getPubmedId());})
                .collect(Collectors.toList());

            //logger.info(String.format("Rows with PMID: %d", rowsWithPMID.size()));

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception occurred when loading data.", e);
        }

        return pmidRows;
    }


    
    public List<PaperInfo> sortDoiRows() {

        List<PaperInfo> doiRows = null;

        try {

            doiRows
                = Files.lines(this.infile)
                .skip(1)
                .map(l -> {
                        return new PaperInfo(l);
                    })
                .filter(p -> {
                        return p.getPubmedId().trim().isEmpty()
                            && !p.getDoi().trim().isEmpty();
                    })
                .sorted((a, b)->{return a.getDoi().compareTo(b.getDoi());})
                .collect(Collectors.toList());

            //logger.info(String.format("Rows with DOI: %d", rowsWithDoi.size()));

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception occurred when loading data.", e);
        }

        return doiRows;
        
    }


    public List<PaperInfo> sortOtherRows() {

        List<PaperInfo> otherRows = null;

        try {

            otherRows
                = Files.lines(this.infile)
                .skip(1)
                .map(l -> {
                        return new PaperInfo(l);
                    })
                .filter(p -> {
                        return p.getPubmedId().trim().isEmpty()
                            && p.getDoi().trim().isEmpty();
                    })
                .sorted((a, b)->{return a.getJournal().compareTo(b.getJournal());})
                .collect(Collectors.toList());

            //logger.info(String.format("otherRows: %d", otherRows.size()));

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception occurred when loading data.", e);
        }
        
        return otherRows;
    }


    
    public void setInfile(Path infile) {
        this.infile = infile;
    }

    
}    

