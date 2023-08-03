package com.github.oogasawa.utility.sc.tsv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ToHtml {

    public void convert(Path infile) {

        try {
            System.out.println("<table>");
            Files.lines(infile)
                .map(l->splitByTab(l))
                .forEach(row->{ // [nameJa, nameEn, title, ...]
                         System.out.print("<tr>");       
                         for (int i=0; i<row.size(); i++) {
                             System.out.print("<td>" + row.get(i) + "</td>");
                         }
                         System.out.println("</tr>");
                    });
            System.out.println("</table>");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
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
    
}
