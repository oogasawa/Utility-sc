package com.github.oogasawa.utility.sc;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.github.oogasawa.utility.cli.CliCommands;
import com.github.oogasawa.utility.sc.paper.PaperInfo;
import com.github.oogasawa.utility.sc.paper.PaperSorter;
import com.github.oogasawa.utility.sc.pubmed.TableCreator;


public class App
{
    public static void main( String[] args ) throws URISyntaxException, IOException, InterruptedException, XMLStreamException
    {

        var helpStr = "java -jar utility-sc-fat.jar <command> <options>";
        var cli = new CliCommands();

        cli.addCommand("paper:sort", createPaperSortOptions(), "Sort papers into meaingful categories");
        cli.addCommand("paper:pmid_table", createPmidTableOptions(), "Print a table with respect to the elements wit PMIDs");
        //cli.addCommand("dummy", createDummyOptions());

        try {

            CommandLine cmd = cli.parse(args);

            if (cli.getCommand() == null) {
                // check universal options.
                if (cmd.hasOption("h") || cmd.hasOption("help")) {
                    cli.printHelp(helpStr);
                }

            }
            else if (cli.getCommand().equals("paper:sort")) {
                String infile = cmd.getOptionValue("infile");
                PaperSorter sorter = new PaperSorter.Builder(infile).build();
                sorter.sort();
                
            }

            else if (cli.getCommand().equals("paper:pmid_table")) {
                String infile = cmd.getOptionValue("infile");

                PaperSorter sorter = new PaperSorter.Builder(infile).build();
                List<PaperInfo> papersWithPmid = sorter.sortPubmed();
                
                TableCreator obj = new TableCreator.Builder(infile).build();
                obj.create(papersWithPmid);
                
            }

            else {
                cli.printHelp(helpStr);
            }

        } catch (ParseException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
            cli.printHelp(helpStr);
        }
    }



    public static Options createPaperSortOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                        .option("i")
                        .longOpt("infile")
                        .hasArg(true)
                        .argName("infile")
                        .desc("Input file (in TSV format with UTF-8 encoding)")
                        .required(true)
                        .build());

        return opts;
    }


    
    public static Options createPmidTableOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                        .option("i")
                        .longOpt("infile")
                        .hasArg(true)
                        .argName("infile")
                        .desc("Input file (in TSV format with UTF-8 encoding)")
                        .required(true)
                        .build());

        return opts;
    }


}

