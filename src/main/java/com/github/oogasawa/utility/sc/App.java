package com.github.oogasawa.utility.sc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import com.github.oogasawa.utility.sc.apt.AptFormatter;
import com.github.oogasawa.utility.sc.apt.AptInstaller;
import com.github.oogasawa.utility.sc.apt.AptSearcher;
import com.github.oogasawa.utility.sc.iftop.IftopParser;
import com.github.oogasawa.utility.sc.monitor.WebMonitor;
import com.github.oogasawa.utility.sc.paper.PaperInfo;
import com.github.oogasawa.utility.sc.paper.PaperSorter;
import com.github.oogasawa.utility.sc.pubmed.PubmedTableRow;
import com.github.oogasawa.utility.sc.pubmed.StAXUtil;
import com.github.oogasawa.utility.sc.tsv.TableChecker;
import com.github.oogasawa.utility.sc.tsv.ToHtml;

import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Files;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;



public class App
{
    
    private static final Logger logger = Logger.getLogger(App.class.getName());

    String      synopsis = "java -jar Utility-sc-VERSION-fat.jar <command> <options>";
    CliCommands cmds     = new CliCommands();

    
    public static void main( String[] args )
    {

        try {
            LogManager.getLogManager()
                    .readConfiguration(WebMonitor.class.getClassLoader().getResourceAsStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        App app = new App();
        
        app.setupCommands();

        try {

            CommandLine cl = app.cmds.parse(args);
            String command = app.cmds.getCommand();
            
            if (command == null) {
                app.cmds.printCommandList(app.synopsis);
            }
            else if (app.cmds.hasCommand(command)) {
                app.cmds.execute(command, cl);
            }
            else {
                System.err.println("The specified command is not available: " + app.cmds.getCommand());
                app.cmds.printCommandList(app.synopsis);
            }

        } catch (ParseException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage() + "\n");
            app.cmds.printCommandHelp(app.cmds.getCommand());
        } 
            
    
    }


    
    public void setupCommands() {
    
        aptCommandCommand();
        aptFilterCommand();
        aptFormatCommand();
        aptInstallCommand();
        aptListCommand();
        aptRemoveCommand();

        iftopDownloadSpeedCommand();
        
        paperSortCommand();
        paperPmidTableCommand();
        paperPubmedXmlCommand();

        tsvToHtmlCommand();
        tsvCheckTableCommand();

        WebMonitorCommand();

    }


    /* ********** ********** ********** */
    /* Command defining methods         */
    /* ********** ********** ********** */

    

    public void aptCommandCommand() {

        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                .option("i")
                .longOpt("infile")
                .hasArg(true)
                .argName("infile")
                .desc("Input file with list of packages to install.")
                .required(true)
                .build());
        
    
        this.cmds.addCommand("apt:command", opts,
                       "Generate `apt install` commands",
                       (CommandLine cl)-> {
                                 String infile = cl.getOptionValue("infile");

                                 List<String> packages = AptInstaller.readFile(Path.of(infile));
                                 System.out.println(AptInstaller.toAptInstallCommand(packages));
                             });

    }


    
    public void aptFilterCommand() {
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


        this.cmds.addCommand("apt:filter", opts,
                "Filter `apt search` results",
                (CommandLine cl) -> {
                    List<String> pkgList = new ArrayList<>();

                    String[] pkgNames = cl.getOptionValues("list");

                    if (pkgNames != null) {
                        pkgList = List.of(cl.getOptionValue("list").split(","));
                    }

                    String pkg = cl.getOptionValue("infile");
                    if (pkg != null) {
                        pkgList.add(pkg);
                    }

                    if (pkgList.size() == 0) {
                        this.cmds.printCommandHelp(this.cmds.getCommand());
                    } else {
                        try (BufferedReader is = new BufferedReader(new InputStreamReader(System.in))) {
                            AptSearcher.filter(is, pkgList);
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "Error occured when reading from stdin", e);
                        }
                    }
                });

    }



    
    public void aptFormatCommand() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("Input file about the package to be installed.")
                       .required(false)
                       .build());



        this.cmds.addCommand("apt:format", opts,
                "Format the information on packages to be installed.",
                (CommandLine cl) -> {
                    String infile = cl.getOptionValue("infile");
                    logger.info("infile: " + infile);

                    String pwd = System.getenv("PWD");
                    logger.info("Current working directory: " + pwd);
                    Path infilePath = Path.of(pwd, infile);

                    logger.info(infilePath.toString());

                    try (BufferedReader is = new BufferedReader(new FileReader(infilePath.toFile()))) {
                        AptFormatter.format(is);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error occured when reading from: " + infile, e);
                    }

                });

    }



        
    public void aptInstallCommand() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("Input file with list of packages to install.")
                       .required(true)
                       .build());


        opts.addOption(Option.builder("from")
                       .option("f")
                       .longOpt("from")
                       .hasArg(true)
                       .argName("from")
                       .desc("Range of package numbers to install.")
                       .required(false)
                       .build());

        opts.addOption(Option.builder("to")
                       .option("t")
                       .longOpt("to")
                       .hasArg(true)
                       .argName("to")
                       .desc("Range of package numbers to install.")
                       .required(false)
                       .build());


        this.cmds.addCommand("apt:install", opts,
                "Batch installation with apt install",
                 (CommandLine cl) -> {
                    String infile = cl.getOptionValue("infile");

                    String fnumStr = cl.getOptionValue("from");
                    String tnumStr = cl.getOptionValue("to");

                    if (fnumStr != null && tnumStr != null) {
                        int fnum = Integer.parseInt(fnumStr);
                        int tnum = Integer.parseInt(tnumStr);
                        List<String> packages = AptInstaller.readFile(Path.of(infile));
                        AptInstaller.install(packages, fnum, tnum);
                    } else {
                        List<String> packages = AptInstaller.readFile(Path.of(infile));
                        AptInstaller.install(packages);
                    }

                });

    }





        
    public void aptListCommand() {
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


        this.cmds.addCommand("apt:install", opts,
                "Batch installation with apt install",
                 (CommandLine cl) -> {
                    String infile = cl.getOptionValue("infile");

                    String fnumStr = cl.getOptionValue("from");
                    String tnumStr = cl.getOptionValue("to");

                    if (fnumStr != null && tnumStr != null) {
                        int fnum = Integer.parseInt(fnumStr);
                        int tnum = Integer.parseInt(tnumStr);
                        List<String> packages = AptInstaller.readFile(Path.of(infile));
                        AptInstaller.install(packages, fnum, tnum);
                    } else {
                        List<String> packages = AptInstaller.readFile(Path.of(infile));
                        AptInstaller.install(packages);
                    }

                });

    }


    

    public void aptRemoveCommand() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("Input file with list of packages to remove.")
                       .required(true)
                       .build());

        
        opts.addOption(Option.builder("from")
                       .option("f")
                       .longOpt("from")
                       .hasArg(true)
                       .argName("from")
                       .desc("Range of package numbers to be removed.")
                       .required(false)
                       .build());

        opts.addOption(Option.builder("to")
                       .option("t")
                       .longOpt("to")
                       .hasArg(true)
                       .argName("to")
                       .desc("Range of package numbers to be removed.")
                       .required(false)
                       .build());


        opts.addOption(Option.builder("includeComment")
                       .option("c")
                       .longOpt("includeComment")
                       .hasArg(false)
                       .argName("includeComment")
                       .desc("Lines with the package name commented out will also be processed for that package.")
                       .required(false)
                       .build());


        this.cmds.addCommand("apt:remove", opts,
                "Batch uninstall with `apt remove`",
                 (CommandLine cl) -> {
                    String infile = cl.getOptionValue("infile");

                    String includeComment = cl.getOptionValue("includeComment");
                    List<String> packages = null;
                    if (includeComment != null) {
                        packages = AptInstaller.readFile(Path.of(infile), true);
                    } else {
                        packages = AptInstaller.readFile(Path.of(infile), false);
                    }

                    String fnumStr = cl.getOptionValue("from");
                    String tnumStr = cl.getOptionValue("to");

                    if (fnumStr != null && tnumStr != null) {
                        int fnum = Integer.parseInt(fnumStr);
                        int tnum = Integer.parseInt(tnumStr);
                        AptInstaller.remove(packages, fnum, tnum);
                    } else {
                        AptInstaller.remove(packages);
                    }

                });


        
        }



    public void iftopDownloadSpeedCommand() {

        Options opts = new Options();


        opts.addOption(Option.builder("FQDN")
                .option("n")
                .longOpt("FQDN")
                .hasArg(true)
                .argName("FQDN")
                .desc("A FQDN of the server from which the data is downloaded.")
                .required(true)
                .build());

        
        opts.addOption(Option.builder("infile")
                .option("i")
                .longOpt("infile")
                .hasArg(true)
                .argName("infile")
                .desc("File of speed generated by iftop command.")
                .required(true)
                .build());

        opts.addOption(Option.builder("outfile")
                .option("o")
                .longOpt("outfile")
                .hasArg(true)
                .argName("outfile")
                .desc("A data table file create by parsing.")
                .required(true)
                .build());

        
    
        this.cmds.addCommand("iftop:downloadspeed", opts,
                "Create a tally table of download speeds",
                (CommandLine cl)-> {
                        String fqdn = cl.getOptionValue("FQDN");
                        String infile = cl.getOptionValue("infile");
                        String outfile = cl.getOptionValue("outfile");

                        IftopParser parser = new IftopParser();
                        parser.makeDownloadSpeedTable(fqdn, new File(infile), new File(outfile));
                });

    }





    

       
    
    public void paperSortCommand() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("Input file (in TSV format with UTF-8 encoding)")
                       .required(true)
                       .build());


        this.cmds.addCommand("paper:sort", opts,
                "Sort papers into meaingful categories",
                 (CommandLine cl) -> {
                    String infile = cl.getOptionValue("infile");
                    PaperSorter sorter = new PaperSorter.Builder(Path.of(infile)).build();
                    sorter.sort();

                });
        

    }


    
    public void paperPmidTableCommand() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("Input file (in TSV format with UTF-8 encoding)")
                       .required(true)
                       .build());

        
        this.cmds.addCommand("paper:pmid_table", opts,
                "Print a table with respect to the elements with PMIDs",
                 (CommandLine cl) -> {
                    String infile = cl.getOptionValue("infile");

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

                });


    }



    public void paperPubmedXmlCommand() {
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


        this.cmds.addCommand("paper:pubmed_xml", opts,
                "Print an XML corresponding to the specified pubmed ID",
                 (CommandLine cl) -> {
                    String pmid = cl.getOptionValue("pmid");
                    StAXUtil obj = new StAXUtil();
                    String xml;
                    try {
                        xml = StAXUtil.efetch(pmid);
                        String tag = cl.getOptionValue("tag");
                        if (tag != null) {
                            xml = obj.extractXml(xml, tag);
                        }

                        System.out.println(xml);

                    } catch (URISyntaxException | IOException | InterruptedException e) {
                        logger.log(Level.WARNING, "Error occured when reading from: " + pmid, e);
                    } catch (XMLStreamException e) {
                        logger.log(Level.WARNING, "Error occured when reading from: " + pmid, e);
                    }
   
                });
        

    }


    
    public void tsvToHtmlCommand() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("Input file")
                       .required(true)
                       .build());

        
        this.cmds.addCommand("tsv:toHtml", opts,
                "Convert a TSV file to HTML table.",
                 (CommandLine cl) -> {
                    String infile = cl.getOptionValue("infile");

                    ToHtml converter = new ToHtml();
                    converter.convert(Path.of(infile));
                });


    }


        
    public void tsvCheckTableCommand() {
        Options opts = new Options();

        opts.addOption(Option.builder("infile")
                       .option("i")
                       .longOpt("infile")
                       .hasArg(true)
                       .argName("infile")
                       .desc("Input file")
                       .required(true)
                       .build());


        this.cmds.addCommand("tsv:check_table", opts,
                "Check the integrity of a TSV table.",
                 (CommandLine cl) -> {
                    String infile = cl.getOptionValue("infile");
                    TableChecker.check(Path.of(infile));
                });


        
    }



        
    public void WebMonitorCommand() {
        Options opts = new Options();

        opts.addOption(Option.builder("url")
                       .option("u")
                       .longOpt("url")
                       .hasArg(true)
                       .argName("url")
                       .desc("URL to be monitored.")
                       .required(true)
                       .build());


        this.cmds.addCommand("WebMonitor", opts,
                "Perform remote web page uptime monitoring.",
                 (CommandLine cl) -> {
                        String url = cl.getOptionValue("url");
                        WebMonitor monitor = new WebMonitor();
                        monitor.monitor(url);
                });


        
    }


    

    /* ********** ********** ********** */
    /* Utility methods                  */
    /* ********** ********** ********** */
    
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



