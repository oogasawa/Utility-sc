package com.github.oogasawa.utility.sc.pubmed;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StAXUtil {


    private static final Logger logger = LoggerFactory.getLogger(StAXUtil.class);


    public static String efetch(String pmid) throws URISyntaxException, IOException, InterruptedException {
        
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



    
    public static String simpleValue(XMLStreamReader reader, String tagName) throws XMLStreamException {

        String value = null;

        while (reader.hasNext()) {
            int event = reader.next();

            switch (event) {
            case XMLStreamConstants.START_ELEMENT:
                String elementName = reader.getLocalName();

                logger.debug(String.format("elementName: %s", elementName));

                if (tagName.equals(elementName)) {
                    value = reader.getElementText();
                }
                break;
            }
        }

        return value;

    }


    

    public String extractXml(String xml, String tagName) throws XMLStreamException, UnsupportedEncodingException {

        String regexp = "<" + tagName + ".+" + "<\\/\\s*" + tagName + ">";
        Pattern pSubexpr = Pattern.compile(regexp);

        String result = null;
        Matcher m = pSubexpr.matcher(xml);
        if (m.find()) {
            result = m.group(0);
        }
        return result;
    }

    
    
}
