package com.github.oogasawa.utility.sc.pubmed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.StringJoiner;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class PaperTable {


    String articleTitle = null;
    String journal = null;
    String volume  = null;
    String issue  = null;
    String year = null;
    String month = null;
    String day  = null;
    String pages = null;

    //String pmid = null;
    String doi = null;
    
    public static void main(String[] args)  {

        PaperTable obj = new PaperTable();

        try {
            String pmid = "33290522";
            String xml = obj.efetch(pmid);
            obj.parseXml(xml);

            System.out.println(obj.row("\n", pmid));
            
        } catch (URISyntaxException | IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XMLStreamException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


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


    

    public void parseXml(String xml) throws XMLStreamException, UnsupportedEncodingException {
        
    
        byte[] byteArray = xml.getBytes("UTF-8");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = inputFactory.createXMLEventReader(inputStream);

        //MyObject object = new MyObject();

        int state = 0;
        StringJoiner titleJoiner = new StringJoiner(" ");
        while (reader.hasNext()) {
            XMLEvent event = (XMLEvent) reader.next();

            if (state == 10) {
                
                if (event.isCharacters()) {
                    Characters element = (Characters)event;
                    titleJoiner.add(element.getData());
                }
                
            }


            else if (event.isStartElement()) {

                StartElement element = (StartElement)event;
                

                if (element.getName().toString().equals("MedlinePgn")) {
                    this.pages = reader.getElementText();
                }

                else if (element.getName().toString().equals("ArticleTitle")) {
                    // this.articleTitle = reader.getElementText();
                    // this.articleTitle = reader.getText();
                    state = 10;
                }

                else if (element.getName().toString().equals("ISOAbbreviation")) {
                    this.journal = reader.getElementText();
                }

                else if (element.getName().toString().equals("Volume")) {
                    this.volume = reader.getElementText();
                }
                else if (element.getName().toString().equals("Issue")) {
                    this.issue = reader.getElementText();
                }
                else if (element.getName().toString().equals("ELocationID")) {
                    this.doi = reader.getElementText();
                }



                if (element.getName().toString().equals("PubDate")) {
                    //this.year = reader.getElementText();
                    state = 1;
                }

                
                if (state == 1 && element.getName().toString().equals("Year")) {
                    this.year = reader.getElementText();
                }
                else if (state == 1 && element.getName().toString().equals("Month")) {
                    this.month = reader.getElementText();
                }
                else if (state == 1 && element.getName().toString().equals("Day")) {
                    this.day = reader.getElementText();
                }
                
                
            }
            else if (event.isEndElement()) {
                EndElement element = (EndElement)event;
                if (state == 1 && element.getName().toString().equals("PubDate")) {
                    state = 0;
                }
                if (state == 10 && element.getName().toString().equals("ArticleTitle")) {
                    this.articleTitle = titleJoiner.toString();
                    titleJoiner = new StringJoiner(" ");
                    state = 0;
                }

            }
            
        }
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
        joiner.add(this.volume + "(" + this.issue + "):");
        joiner.add(this.pages + ".");

        return joiner.toString();
    }


    public String row(String delimiter, String pmid) {

        StringJoiner joiner = new StringJoiner(delimiter);
        joiner.add(this.articleTitle);
        joiner.add(journalColumn());
        joiner.add(pmid);
        joiner.add(this.doi);

        return joiner.toString();
    }
    
}
