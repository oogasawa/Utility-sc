package com.github.oogasawa.utility.sc.paper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaperSorter {

    Logger logger = null;
    
    String infile;

    String outfile1 = "valid_papers.txt";
    String outfile2 = "invalid.txt";

    List<PaperInfo> infoList = new ArrayList<PaperInfo>();
    List<PaperInfo> otherList = new ArrayList<PaperInfo>();
    
    static public class Builder {

        String loggerName = "PaperSorter";
        String infile;
        

        public Builder(String infile) {
            if (infile == null)
                throw new NullPointerException();

            this.infile = infile;
        }

        public Builder loggerName(String name) {
            loggerName = name;
            return this;
        }
        
        public PaperSorter build() {
            PaperSorter sorter = new PaperSorter();
            sorter.setInfile(infile);
            sorter.logger = Logger.getLogger(loggerName);
            
            return sorter;
        }
    }


    public List<PaperInfo> deepCopy(List<PaperInfo> list) {

        List<PaperInfo> result = new ArrayList<PaperInfo>();

        for (PaperInfo info: list) {
            result.add(info);
        }

        return result;
    }



    
    public void printPaperInfoList(List<PaperInfo> infoList) {

        Collections.sort(infoList, new PaperInfoComparator());
        
        for (PaperInfo info : infoList) {
            System.out.println(info.toTSV());
        }
    }


    

    public void sort() {

        this.printPaperInfoList(this.sortPubmed());
        this.printPaperInfoList(this.sortOtherValidJournalName());
        this.printPaperInfoList(this.sortOtherDoi());
        
        this.printPaperInfoList(infoList);
    }

    
    public List<PaperInfo> sortPubmed() {

        List<PaperInfo> result = new ArrayList<PaperInfo>();
        
        String str = null;
        try (BufferedReader br = new BufferedReader(new FileReader(this.infile))) {
            while ((str = br.readLine()) != null) {
                List<String> cols = splitByTab(str, 15);

                
                PaperInfo info = new PaperInfo(cols);


                if (info.pubmedId() != null && info.pubmedId().length() > 0) {
                    result.add(info);
                }
                else {
                    otherList.add(info);
                }
                
            }
        } catch (FileNotFoundException f) {
            System.out.println(infile + " does not exist");
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.infoList = this.otherList;
        this.otherList = new ArrayList<PaperInfo>();
        
        return result;
        
    }



    public List<PaperInfo> sortOtherValidJournalName() {

        List<PaperInfo> result = new ArrayList<PaperInfo>();

        Pattern pValidJournal = Pattern.compile("^[a-zA-Z]+");
        
        for (PaperInfo info : this.infoList) {

            if (info.journal() != null) {
                Matcher m = pValidJournal.matcher(info.journal());
                if (m.find()) {
                    result.add(info);
                }
                else {
                    otherList.add(info);
                }
            }
            else {
                otherList.add(info);
            }
            
        }


        this.infoList = this.otherList;
        this.otherList = new ArrayList<PaperInfo>();
        
        return result;

        
    }


    
    public List<PaperInfo> sortOtherDoi() {

        List<PaperInfo> result = new ArrayList<PaperInfo>();
        
        for (PaperInfo info : this.infoList) {

            if (info.doi() != null && info.doi().length() > 0) {
                result.add(info);
            }
            else {
                otherList.add(info);
            }
            
        }

        this.infoList = this.otherList;
        this.otherList = new ArrayList<PaperInfo>();
        
        return result;

        
    }




    
    public List<String> splitByChar(String str, char ch) {

        List<String> ret = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                ret.add(sb.toString());
                sb.delete(0, sb.length());

                // When a delimiting character is at the last of a string,
                // the last element of the returned list should be an empty string.
                if (i == str.length() - 1) {
                    sb.append("");
                }
            } else {
                sb.append(str.charAt(i));
            }

        }

        ret.add(sb.toString());

        return ret;
    }


    
    public List<String> splitByTab(String str) {
        return splitByChar(str, '\t');
    }


    
    public List<String> splitByTab(String str, int length) {

        List<String> cols = splitByChar(str, '\t');

        if (cols.size() < length) {
            for (int i = cols.size() - 1; i < 15; i++) {
                cols.add("");
            }
        }

        return cols;
    }



    
    public void setInfile(String infile) {
        this.infile = infile;
    }

    
}    

