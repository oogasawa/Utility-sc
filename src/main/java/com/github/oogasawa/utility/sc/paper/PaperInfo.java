package com.github.oogasawa.utility.sc.paper;

import java.util.List;
import java.util.StringJoiner;

public record PaperInfo(

    String tracking_id,
    String uid,
    String title,
    String authors,
    String authorsJa,
    String journal,
    String volume,
    String number,
    String pages,
    String publicationDate,
    String journalType,
    String pubmedId,
    String doi,
    String description,
    String url
                        )
    {


        public PaperInfo(List<String> cols) {
            
            this(cols.get(0), cols.get(1), cols.get(2), cols.get(3), cols.get(4), cols.get(5), cols.get(6), cols.get(7),
                    cols.get(8), cols.get(9), cols.get(10), cols.get(11), cols.get(12), cols.get(13), cols.get(14));
        }



        public String toTSV() {
            StringJoiner joiner = new StringJoiner("\t");

            joiner.add(tracking_id);
            joiner.add(uid);
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
            joiner.add(description);
            joiner.add(url);

            return joiner.toString();
        }

    }
    
