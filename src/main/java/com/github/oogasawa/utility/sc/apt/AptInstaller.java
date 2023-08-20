package com.github.oogasawa.utility.sc.apt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class AptInstaller {

    private static final Logger logger = Logger.getLogger(AptInstaller.class.getName());
    
    Path infile;

    static public class Builder {
        Path infile;

        public Builder(Path infile) {
            if (infile == null)
                throw new NullPointerException();

            this.infile = infile;
        }


        public AptInstaller build() {
            AptInstaller obj = new AptInstaller();
            obj.setInfile(this.infile);

            return obj;
        }
    }



    

    public void install() {

        List<String> aptPackages = this.readFile();

        for (String pkg: aptPackages) {
            Process p;
            try {
                System.out.println("%Install " + pkg);
                p = new ProcessBuilder("apt", "install", "-y", pkg)
                    .inheritIO()
                    .start();

                p.waitFor();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error occured while installing: " + pkg, e);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Interrupted", e);
            }


        }
        
    }


    
    /** Reads the input file and extract packages to install.
     *
     * @return A list of package names to install.
     */
    public List<String> readFile() {

        List<String> result = new ArrayList<String>();
        
        Pattern p1 = Pattern.compile("^([a-zA-Z0-9-_]+)\\/");
        Pattern p2 = Pattern.compile("^([a-zA-Z0-9-_]+)$");
        try {
            result =
                Files.lines(this.infile)
                .map(line->{
                        Matcher m = p1.matcher(line);
                        if (m.find()) {
                            Optional<String> opt = Optional.of(m.group(1));
                            return opt;
                        }

                        m = p2.matcher(line);
                        if (m.find()) {
                            Optional<String> opt = Optional.of(m.group(1));
                            return opt;
                        }

                        Optional<String> opt = Optional.empty();
                        return opt;
                    })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can not read " + this.infile.toString(), e);
        }

        return result;
    }




    public String toAptCommand() {
        
        StringJoiner rowJoiner = new StringJoiner(" \\\n");
        
        List<String> aptPackages = this.readFile();

        StringJoiner colJoiner = new StringJoiner(" ");
        for (int i=1; i<=aptPackages.size(); i++) {
            colJoiner.add(aptPackages.get(i-1));
            if (i%5 == 0) {
                rowJoiner.add("    " + colJoiner.toString());
                colJoiner = new StringJoiner(" ");
            }
        }

        return "apt install -y " + rowJoiner.toString();
        
    }

    

    

    // --- setters and getters ---
    
    public Path getInfile() {
        return infile;
    }

    public void setInfile(Path infile) {
        this.infile = infile;
    }

}
