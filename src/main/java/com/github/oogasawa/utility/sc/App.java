package com.github.oogasawa.utility.sc;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import com.github.oogasawa.utility.cli.CliCommands;
import com.github.oogasawa.utility.sc.apt.AptInstaller;
import com.github.oogasawa.utility.sc.apt.AptSearcher;
import com.github.oogasawa.utility.sc.paper.PaperInfo;
import com.github.oogasawa.utility.sc.paper.PaperSorter;
import com.github.oogasawa.utility.sc.pubmed.PubmedTableRow;
import com.github.oogasawa.utility.sc.pubmed.StAXUtil;
import com.github.oogasawa.utility.sc.tsv.TableChecker;
import com.github.oogasawa.utility.sc.tsv.ToHtml;


public class App {

    private static final Logger logger = Logger.getLogger(App.class.getName());
    
    public static void main( String[] args ) throws URISyntaxException, IOException, InterruptedException, XMLStreamException
    {

        try {
            LogManager.getLogManager()
                    .readConfiguration(App.class.getClassLoader().getResourceAsStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        var helpStr = "java -jar utility-sc-fat.jar <command> <options>";
        var cli = new CliCommands();

        cli.addCommand("apt:command", createAptCommandOptions(), "Generate apt install command line.");
        cli.addCommand("apt:install", createAptInstallOptions(), "Batch installation with apt install");
        cli.addCommand("apt:filter", createAptFilterOptions(), "Filter apt search results.");
        cli.addCommand("apt:list", createAptListOptions(), "List deb packages");
        cli.addCommand("paper:sort", createPaperSortOptions(), "Sort papers into meaingful categories");
        cli.addCommand("paper:pmid_table", createPmidTableOptions(), "Print a table with respect to the elements with PMIDs");
        cli.addCommand("paper:pubmed_xml", createPubmedXmlOptions(), "Print an XML corresponding to the given Pubmed ID.");
        cli.addCommand("tsv:toHtml", createToHtmlOptions(), "Convert a TSV table to a HTML table.");
        cli.addCommand("tsv:check_table", createTableCheckerOptions(), "Check if the data in the table is normal.");


        
        try {

            CommandLine cmd = cli.parse(args);

            if (cli.getCommand() == null) {
                // check universal options.
                if (cmd.hasOption("h") || cmd.hasOption("help")) {
                    cli.printHelp(helpStr);
                }

            }

            
            else if (cli.getCommand().equals("apt:command")) {
                String infile = cmd.getOptionValue("infile");

                List<String> packages = AptInstaller.readFile(Path.of(infile));
                System.out.println(AptInstaller.toAptCommand(packages));
            }


            else if (cli.getCommand().equals("apt:filter")) {
                List<String> pkgList = new ArrayList<>();
                
                String[] pkgNames = cmd.getOptionValues("list");

                if (pkgNames != null) {
                    pkgList = List.of(cmd.getOptionValue("list").split(","));
                }
                
                String pkg = cmd.getOptionValue("infile");
                if (pkg != null) {
                    pkgList.add(pkg);
                }

                if (pkgList.size() == 0) {
                    cli.printHelp(helpStr);
                }
                else {
                    try (BufferedReader is = new BufferedReader(new InputStreamReader(System.in))) {
                        AptSearcher.filter(is, pkgList);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error occured when reading from stdin", e);
                    }
                }

            }

            
            else if (cli.getCommand().equals("apt:install")) {
                String infile = cmd.getOptionValue("infile");

                List<String> packages = AptInstaller.readFile(Path.of(infile));
                AptInstaller.install(packages);
            }


            
            else if (cli.getCommand().equals("apt:list")) {

                String category = cmd.getOptionValue("category");
                if (category != null) {
                    AptSearcher.list(category);
                }

                
                String infile = cmd.getOptionValue("infile");

                List<String> packages = AptInstaller.readFile(Path.of(infile));
                AptInstaller.install(packages);
            }


            
            
            else if (cli.getCommand().equals("paper:sort")) {
                String infile = cmd.getOptionValue("infile");
                PaperSorter sorter = new PaperSorter.Builder(Path.of(infile)).build();
                sorter.sort();
            }
            else if (cli.getCommand().equals("paper:pubmed_xml")) {
                String pmid = cmd.getOptionValue("pmid");
                StAXUtil obj = new StAXUtil();
                String xml = obj.efetch(pmid);
                String tag = cmd.getOptionValue("tag");
                if (tag != null) {
                    xml = obj.extractXml(xml, tag);
                }

                System.out.println(xml);
            }

            else if (cli.getCommand().equals("tsv:toHtml")) {
                String infile = cmd.getOptionValue("infile");

                ToHtml converter = new ToHtml();
                converter.convert(Path.of(infile));
                
            }

            else if (cli.getCommand().equals("tsv:check_table")) {
                String infile = cmd.getOptionValue("infile");

                TableChecker.check(Path.of(infile));
                
            }


            
            
            else if (cli.getCommand().equals("paper:pmid_table")) {

                String infile = cmd.getOptionValue("infile");

                Pattern pPmid = Pattern.compile("([0-9]+)");

                try {
                    Files.lines(Path.of(infile)).skip(1).map(l -> {
                        return new PaperInfo(l);
                    }).filter(paperInfo -> {
                        String pmid = paperInfo.getPubmedId();
                        if (pmid.length() > 0) {
                            Matcher m = pPmid.matcher(pmid);
                            if (m.matches()) {
                                return true;
                            }
                        }
                        return false;
                    }).forEach(paperInfo -> {

                        try {

                            String pmid = paperInfo.getPubmedId();
                            PubmedTableRow row = new PubmedTableRow();
                            row.parse(row.efetch(pmid));

                            StringJoiner joiner = new StringJoiner("\t");
                            joiner.add(paperInfo.getAccountNameJa());
                            joiner.add(normalize(removeQuotations(paperInfo.getAccountNameEn())));
                            joiner.add(row.toTSV());

                            System.out.println(joiner.toString());
                            Thread.sleep(15000);

                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Unexpected exception", e);
                        }
                    });

                } catch (IOException e) {
                    logger.log(Level.SEVERE, "", e);
                }

            }

            else {
                cli.printHelp(helpStr);
            }

        } catch (ParseException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
            cli.printHelp(helpStr);
        }
    }



        
    public static Options createAptCommandOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("Input file with list of packages to install.")
                       .required(true)
                       .build());

        return opts;
    }



    
    public static Options createAptFilterOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("pkg")
                       .option("p")
                       .longOpt("pkg")
                       .hasArg(true)
                       .argName("pkg")
                       .desc("A package name to display.")
                       .required(false)
                       .build());

        
        opts.addOption(Option.builder("list")
                       .option("l")
                       .longOpt("list")
                       .hasArg(true)
                       .argName("list")
                       .desc("A list of package names (comma delimited)")
                       .required(false)
                       .build());
        
        return opts;
    }


    
        
    public static Options createAptInstallOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("Input file with list of packages to install.")
                       .required(true)
                       .build());

        return opts;
    }



    
    public static Options createAptListOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("category")
                       .option("c")
                       .longOpt("category")
                       .hasArg(true)
                       .argName("category")
                       .desc("Software category name (e.g. r-cran)")
                       .required(false)
                       .build());

        
        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("A package name list file.")
                       .required(false)
                       .build());
        
        return opts;
    }





    
    
    public static Options createPaperSortOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile").option("i").longOpt("infile").hasArg(true).argName("infile")
                .desc("Input file (in TSV format with UTF-8 encoding)").required(true).build());

        return opts;
    }

    public static Options createPmidTableOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile").option("i").longOpt("infile").hasArg(true).argName("infile")
                .desc("Input file (in TSV format with UTF-8 encoding)").required(true).build());

        return opts;
    }



    public static Options createPubmedXmlOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("pmid")
                       .option("i")
                       .longOpt("pmid")
                       .hasArg(true)
                       .argName("pmid")
                       .desc("A pubmed ID")
                       .required(true)
                       .build());


        opts.addOption(Option.builder("tag")
                       .option("t")
                       .longOpt("tag")
                       .hasArg(true)
                       .argName("tag")
                       .desc("An XML tag")
                       .required(false)
                       .build());


        
        return opts;
    }


    
    public static Options createToHtmlOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("Input file")
                       .required(true)
                       .build());

        return opts;
    }


        
    public static Options createTableCheckerOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("Input file")
                       .required(true)
                       .build());

        return opts;
    }


    


    public static String removeQuotations(String str) {
        Pattern pStr = Pattern.compile("\"(.+)\"");
        String result = "";
        Matcher m = pStr.matcher(str);
        if (m.matches()) {
            result = m.group(1);
        }
        return result;
    }


    public static String normalize(String name) {
        Pattern pName = Pattern.compile("(\\S+)\\s*,\\s*(\\S+)");
        String result = null;
        Matcher m = pName.matcher(name);
        if (m.matches()) {
            result = m.group(2) + " " + m.group(1);
        }
        return result;
    }

    
}
