package com.github.oogasawa.utility.sc.pubmed;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


public class PubmedTableRow {

    private static final Logger logger = Logger.getLogger(PubmedTableRow.class.getName());


    String articleTitle = null;
    String journal = null;
    String volume  = null;
    String issue  = null;
    String year = null;
    String month = null;
    String day  = null;
    String pages = null;
    String pii   = null;

    String pmid = null;
    String doi = null;
    
    
    // public static void main(String[] args)  {

    //     PubmedTableRow obj = new PubmedTableRow();

    //     try {
    //         String pmid = "33290522";
    //         String xml = obj.efetch(pmid);
    //         obj.parse(xml);

    //         System.out.println(obj.toTSV());
            
    //     } catch (URISyntaxException | IOException | InterruptedException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     } catch (XMLStreamException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }

    // }


    public String efetch(String pmid) throws URISyntaxException, IOException, InterruptedException {
        
        HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();


        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=" + pmid + "&retmode=xml&api_key=8ad426257572ad23dc21a8cd170f86e7b008"))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        return response.body();
    }


    

    public void parse(String xml) throws XMLStreamException, UnsupportedEncodingException {
        
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance()
                    .createXMLStreamReader(new StringReader(xml));

            parseXml(reader);

            //System.out.println("取り出した文字列: " + data.getTitle());
            reader.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    
    public void parseXml(XMLStreamReader reader) throws XMLStreamException {

        String state = "initialized";

        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
            case XMLStreamConstants.START_ELEMENT:
                String elementName = reader.getLocalName();

                logger.finer(String.format("elementName: %s", elementName));

                if ("ArticleTitle".equals(elementName)) {
                    this.articleTitle = parseArticleTitle(reader);
                }
                else if ("Journal".equals(elementName)) {
                    state = "Journal";
                }
                else if ("ISOAbbreviation".equals(elementName) && state.equals("Journal")) {
                    this.journal = parseJournalTitle(reader);
                }
                else if ("Volume".equals(elementName) && state.equals("Journal")) {
                    this.volume = reader.getElementText();
                }
                else if ("Issue".equals(elementName) && state.equals("Journal")) {
                    this.issue = reader.getElementText();
                }
                else if ("Year".equals(elementName) && state.equals("Journal")) {
                    this.year = reader.getElementText();
                }
                else if ("Month".equals(elementName) && state.equals("Journal")) {
                    this.month = reader.getElementText();
                }
                else if ("Day".equals(elementName) && state.equals("Journal")) {
                    this.day = reader.getElementText();
                }
                else if ("MedlinePgn".equals(elementName)) {
                    this.pages = reader.getElementText();
                }
                else if ("PMID".equals(elementName)) {
                    this.pmid = reader.getElementText();
                }
                else if ("ELocationID".equals(elementName)) {
                    Map<String, String> attributes = parseAttributes(reader);
                    if (attributes.containsKey("EIdType")) {
                        if (attributes.get("EIdType").equals("doi")) {
                            this.doi = reader.getElementText();
                        }
                        else if (attributes.get("EIdType").equals("pii")) {
                            this.pii = reader.getElementText();
                        }
                    }
                    else {
                        Pattern pDoi = Pattern.compile("^10\\.[0-9]+");
                        String value = reader.getElementText();
                        Matcher m = pDoi.matcher(value);
                        if (m.find() && this.doi == null) {
                            this.doi = m.group(0);
                        }
                    }
                }
                
                break;
                
            case XMLStreamConstants.END_ELEMENT:
                if ("Journal".equals(reader.getLocalName())) {
                    state = "initialized";
                }
                break;
            }
        }

    }


    public Map<String, String> parseAttributes(XMLStreamReader reader) {

        Map<String, String> attributes = new TreeMap<String, String>();

        int attributeCount = reader.getAttributeCount();
        //System.out.println("Attributes for <" + tagName + ">:");
        for (int i = 0; i < attributeCount; i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            attributes.put(name, value);
        }

        return attributes;
        
    }




    
    
    public String parseArticleTitle(XMLStreamReader reader) throws XMLStreamException {

        StringBuilder titleText = new StringBuilder();
        boolean isInsideTitle = true;

        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
            case XMLStreamConstants.CHARACTERS:
                if (isInsideTitle) {
                    titleText.append(reader.getText());
                }
                break;

            case XMLStreamConstants.END_ELEMENT:
                if ("ArticleTitle".equals(reader.getLocalName())) {
                    isInsideTitle = false;
                }
                break;
            }

            if (isInsideTitle == false) {
                break;
            }
        }

        return titleText.toString();
    }


    
    public String parseJournalTitle(XMLStreamReader reader) throws XMLStreamException {

        StringBuilder journalTitleText = new StringBuilder();
        boolean isInsideJournalTitle = true;

        // Genes <b>and</b> Cells
        // を読んで
        // Genes and Cells
        // を取り出す。
        
        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
            case XMLStreamConstants.CHARACTERS:
                if (isInsideJournalTitle) {
                    journalTitleText.append(reader.getText());
                }
                break;

            case XMLStreamConstants.END_ELEMENT:
                if ("ISOAbbreviation".equals(reader.getLocalName())) {
                    isInsideJournalTitle = false;
                }
                break;
            }

            if (isInsideJournalTitle == false) {
                break;
            }

            
        }

        return journalTitleText.toString();
    }



    public String publishDate() {

        StringJoiner j = new StringJoiner(" ");
        j.add(this.year);
        if (this.month != null) {
            j.add(this.month);
            if (this.day != null)
                j.add(this.day);
        }

        return j.toString() + ";";
    }


    
    public String journalColumn() {
        StringJoiner joiner = new StringJoiner(" ");

        joiner.add(journal + ".");
        joiner.add(publishDate());

        if (this.issue == null) {
            joiner.add(this.volume + ":");
        }
        else {
            joiner.add(this.volume + "(" + this.issue + "):");            
        }


        if (this.pages != null) {
            joiner.add(this.pages + ".");
        }
        else if (this.pii != null) {
            joiner.add(this.pii + ".");
        }

        return joiner.toString();
    }


    public String toTSV() {

        StringJoiner joiner = new StringJoiner("\t");
        joiner.add(this.articleTitle);
        joiner.add(journalColumn());
        joiner.add(pmid);
        joiner.add(this.doi);

        return joiner.toString();
    }


    // ===== setters and getters =====
    
    public String getDoi() {
        return doi;
    }


    public void setDoi(String doi) {
        this.doi = doi;
    }


    public String getPmid() {
        return pmid;
    }


    public void setPmid(String pmid) {
        this.pmid = pmid;
    }


    public String getPages() {
        return pages;
    }


    public void setPages(String pages) {
        this.pages = pages;
    }


    public String getDay() {
        return day;
    }


    public void setDay(String day) {
        this.day = day;
    }


    public String getYear() {
        return year;
    }


    public void setYear(String year) {
        this.year = year;
    }


    public static Logger getLogger() {
        return logger;
    }


    public String getIssue() {
        return issue;
    }


    public void setIssue(String issue) {
        this.issue = issue;
    }


    public String getVolume() {
        return volume;
    }


    public void setVolume(String volume) {
        this.volume = volume;
    }


    public String getMonth() {
        return month;
    }


    public void setMonth(String month) {
        this.month = month;
    }


    public String getJournal() {
        return journal;
    }


    public void setJournal(String journal) {
        this.journal = journal;
    }


    public String getArticleTitle() {
        return articleTitle;
    }


    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }


    
}
