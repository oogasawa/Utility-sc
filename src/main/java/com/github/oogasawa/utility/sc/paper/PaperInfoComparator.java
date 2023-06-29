package com.github.oogasawa.utility.sc.paper;

import java.util.Comparator;

public class PaperInfoComparator implements Comparator<PaperInfo> {

    public int compare(PaperInfo a, PaperInfo b) {
        return a.pubmedId().compareTo(b.pubmedId());
    }
    
}
