package com.github.oogasawa.utility.sc.paper;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PaperInfo {

    private static final Logger logger = LoggerFactory.getLogger(PaperInfo.class);
    
    String tracking_id;
    String uid;
    String accountNameJa;
    String accountNameEn;
    String title;
    String authors;
    String authorsJa;
    String authorsEn;
    String journal;
    String volume;
    String number;
    String pages;
    String publicationDate;
    String journalType;
    String pubmedId;
    String doi;


    
    public PaperInfo(List<String> cols) {

        this.setColumns(cols);

    }


    public PaperInfo(String line) {
        List<String> cols = splitByTab(line);
        this.setColumns(cols);
        //logger.info("PaperInfo, num of columns: " + this.toTSV().split("\t").length);
    }


    public void setColumns(List<String> cols) {

        //logger.info("PaperInfo, num of columns: " + cols.size());
        //logger.info("PaperInfo, columns: " + cols);
        
        tracking_id = cols.get(0);
        uid = cols.get(1);
        accountNameJa = cols.get(2);
        accountNameEn = cols.get(3);
        title = cols.get(4);
        authors = cols.get(5);
        authorsJa = cols.get(6);
        journal = cols.get(7);
        volume = cols.get(8);
        number = cols.get(9);
        pages = cols.get(10);
        publicationDate = cols.get(11);
        journalType = cols.get(12);
        pubmedId = cols.get(13);
        doi = cols.get(14);

        
    }

    

        public String toTSV() {
            StringJoiner joiner = new StringJoiner("\t");

            joiner.add(tracking_id);
            joiner.add(uid);
            joiner.add(accountNameJa);
            joiner.add(accountNameEn);
            joiner.add(title);
            joiner.add(authors);
            joiner.add(authorsJa);
            joiner.add(journal);
            joiner.add(volume);
            joiner.add(number);
            joiner.add(pages);
            joiner.add(publicationDate);
            joiner.add(journalType);
            joiner.add(pubmedId);
            joiner.add(doi);

            return joiner.toString();
        }

        
        static public List<String> splitByChar(String str, char ch) {

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

        static public List<String> splitByTab(String str) {
            return splitByChar(str, '\t');
        }

        static public List<String> splitByTab(String str, int length) {

            List<String> cols = splitByChar(str, '\t');

            if (cols.size() < length) {
                for (int i = cols.size() - 1; i < 15; i++) {
                    cols.add("");
                }
            }

            return cols;
        }


    
    public String getUid() {
        return uid;
    }


    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getTracking_id() {
        return tracking_id;
    }


    public void setTracking_id(String tracking_id) {
        this.tracking_id = tracking_id;
    }


    public String getDoi() {
        return doi;
    }


    public void setDoi(String doi) {
        this.doi = doi;
    }


    public String getPubmedId() {
        return pubmedId;
    }


    public void setPubmedId(String pubmedId) {
        this.pubmedId = pubmedId;
    }


    public String getJournalType() {
        return journalType;
    }


    public void setJournalType(String journalType) {
        this.journalType = journalType;
    }


    public String getPublicationDate() {
        return publicationDate;
    }


    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }


    public String getPages() {
        return pages;
    }


    public void setPages(String pages) {
        this.pages = pages;
    }


    public String getNumber() {
        return number;
    }


    public void setNumber(String number) {
        this.number = number;
    }


    public String getVolume() {
        return volume;
    }


    public void setVolume(String volume) {
        this.volume = volume;
    }


    public String getJournal() {
        return journal;
    }


    public void setJournal(String journal) {
        this.journal = journal;
    }


    public String getAuthors() {
        return authors;
    }


    public void setAuthors(String authors) {
        this.authors = authors;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getAccountNameEn() {
        return accountNameEn;
    }


    public void setAccountNameEn(String accountNameEn) {
        this.accountNameEn = accountNameEn;
    }


    public String getAccountNameJa() {
        return accountNameJa;
    }


    public void setAccountNameJa(String accountNameJa) {
        this.accountNameJa = accountNameJa;
    }

    public String getAuthorsEn() {
        return authorsEn;
    }


    public void setAuthorsEn(String authorsEn) {
        this.authorsEn = authorsEn;
    }


    public String getAuthorsJa() {
        return authorsJa;
    }


    public void setAuthorsJa(String authorsJa) {
        this.authorsJa = authorsJa;
    }


    }
