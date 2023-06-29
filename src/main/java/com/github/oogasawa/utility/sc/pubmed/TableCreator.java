package com.github.oogasawa.utility.sc.pubmed;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import com.github.oogasawa.utility.sc.paper.PaperInfo;

public class TableCreator {

    Logger logger = null;
    
    static public class Builder {

        String loggerName = "TableCreator";
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
        
        public TableCreator build() {
            TableCreator obj = new TableCreator();
            //sorter.setInfile(infile);
            obj.logger = Logger.getLogger(loggerName);
            
            return obj;
        }
    }



    public void sleep(int sec) {
        try {
            Thread.sleep(sec * 1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    

    
    public void create(List<PaperInfo> infoList) throws URISyntaxException, IOException, InterruptedException, XMLStreamException {

        PaperTable table = new PaperTable();
        
        for (PaperInfo info: infoList) {
            if (info.pubmedId() != null && info.pubmedId().length() > 0) {
                String xml = table.efetch(info.pubmedId());
                table.parseXml(xml);
                System.out.println(table.row("\t", info.pubmedId()));

                sleep(5);
            }
        }

        
    }

    
    
}
