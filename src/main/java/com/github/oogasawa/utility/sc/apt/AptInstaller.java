package com.github.oogasawa.utility.sc.apt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    
    /** Applies {@code apt install} to each package given a list of package names.
     * 
     * @param aptPackages A list of package names to install.
     */
    public static void install(String pkg) {

        try {
            System.out.println("%Install " + pkg);
            Process p = new ProcessBuilder("apt", "install", "-y", pkg)
                    .inheritIO()
                    .start();

            p.waitFor();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error occured while installing: " + pkg, e);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted", e);
        }        
        
    }



    
    /** Applies {@code apt install} to each package given a list of package names.
     * 
     * @param aptPackages A list of package names to install.
     */
    public static void install(List<String> aptPackages) {

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


    
    /** Applies {@code apt install} to each package given a list of package names.
     * 
     * @param aptPackages A list of package names to install.
     */
    public static void install(List<String> aptPackages, int fnum, int tnum) {

        for (int i=fnum; i<=tnum; i++) {
            Process p;
            try {
                System.out.println("%Install " + aptPackages.get(i));
                p = new ProcessBuilder("apt", "install", "-y", aptPackages.get(i))
                    .inheritIO()
                    .start();

                p.waitFor();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error occured while installing: " + aptPackages.get(i), e);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Interrupted", e);
            }

        }
        
    }

    

    
    /** Reads the input file and extract packages to install.
     *
     * <h4>Example of the input data formats (1)</h4>
     *
     * <pre>{@code
     * python3/jammy-updates,jammy-security,now 3.10.6-1~22.04 amd64 [installed]
     *   interactive high-level object-oriented language (default python3 version)
     *
     * python-is-python3/jammy,jammy,now 3.9.2-2 all [installed]
     *   symlinks /usr/bin/python to python3
     * }</pre>
     *
     * <h4>Example of the input data formats (2)</h4>
     *  <pre>{@code
     * build-essential
     * gfortran
     * gcc-doc
     * flex
     * bison
     * automake
     * autoconf
     * libtool
     * autogen
     * shtool
     * lib6-dev-amd64
     * libarchive-dev
     * cmake
     *  }</pre>
     * 
     * 
     * @param infile A Path object of an input file.
     * @return A list of package names to install.
     */
    public static List<String> readFile(Path infile) {

        List<String> result = new ArrayList<String>();
        
        Pattern p1 = Pattern.compile("^([a-zA-Z0-9-_]+)\\/");
        Pattern p2 = Pattern.compile("^([a-zA-Z0-9-_]+)$");
        Pattern p3 = Pattern.compile("^([a-zA-Z0-9-_]+)\\s");
        try {
            result =
                Files.lines(infile)
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

                        m = p3.matcher(line);
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
            logger.log(Level.SEVERE, "Can not read " + infile.toString(), e);
        }

        return result;
    }




    public static List<String> readFile(Path infile, boolean ignoreCommentingOut) {
        if (ignoreCommentingOut) {
            return readFileWithIgnoringCommentingOut(infile);
        }
        else {
            return readFile(infile);
        }
    }


    

    
    /** Reads the input file and extract packages to install.
     *
     * <h4>Example of the input data formats (1)</h4>
     *
     * <pre>{@code
     * python3/jammy-updates,jammy-security,now 3.10.6-1~22.04 amd64 [installed]
     *   interactive high-level object-oriented language (default python3 version)
     *
     * python-is-python3/jammy,jammy,now 3.9.2-2 all [installed]
     *   symlinks /usr/bin/python to python3
     * }</pre>
     *
     * <h4>Example of the input data formats (2)</h4>
     *  <pre>{@code
     * build-essential
     * gfortran
     * gcc-doc
     * flex
     * bison
     * automake
     * autoconf
     * libtool
     * autogen
     * shtool
     * lib6-dev-amd64
     * libarchive-dev
     * cmake
     *  }</pre>
     * 
     * 
     * @param infile A Path object of an input file.
     * @return A list of package names to install.
     */
    public static List<String> readFileWithIgnoringCommentingOut(Path infile) {

        List<String> result = new ArrayList<String>();
        
        Pattern p1 = Pattern.compile("^#?\\s*([a-zA-Z0-9-_]+)\\/");
        Pattern p2 = Pattern.compile("^#?\\s*([a-zA-Z0-9-_]+)$");
        Pattern p3 = Pattern.compile("^#?\\s*([a-zA-Z0-9-_]+)\\s");
        try {
            result =
                Files.lines(infile)
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

                        m = p3.matcher(line);
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
            logger.log(Level.SEVERE, "Can not read " + infile.toString(), e);
        }

        return result;
    }



    

    /** Reads the input file and extract packages to install.
     *
     * @param resource A resource name (e.g. "/file.txt")
     * @return A list of package names to install.
     */
    public static List<String> readResource(String resource) {

        List<String> result = new ArrayList<String>();
        
        Pattern p1 = Pattern.compile("^([a-zA-Z0-9-_]+)\\/");
        Pattern p2 = Pattern.compile("^([a-zA-Z0-9-_]+)$");

        
        try (InputStream in = AptInstaller.class.getResourceAsStream(resource);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {


            result =
                reader.lines()
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


        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Can not read resource", e);
        }

        return result;
    }


    
    /** Applies {@code apt remove -y --purge} to each package given a list of package names.
     * 
     * @param aptPackages A list of package names to install.
     */
    public static void remove(List<String> aptPackages) {

        for (String pkg: aptPackages) {
            Process p;
            try {
                System.out.println("%Install " + pkg);
                p = new ProcessBuilder("apt", "remove", "-y", "--purge", pkg)
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


    
    /** Applies {@code apt remove -y --purge} to each package given a list of package names.
     * 
     * @param aptPackages A list of package names to install.
     */
    public static void remove(List<String> aptPackages, int fnum, int tnum) {

        for (int i=fnum; i<=tnum; i++) {
            Process p;
            try {
                System.out.println("%Install " + aptPackages.get(i));
                p = new ProcessBuilder("apt", "remove", "-y", "--purge", aptPackages.get(i))
                    .inheritIO()
                    .start();

                p.waitFor();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error occured while installing: " + aptPackages.get(i), e);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Interrupted", e);
            }

        }
        
    }



    

    /** Generates a list of apt commands from a list of apt package names.
     * <p>
     * This method returns the following string when a list of package names is given.
     * </p>
     *
     * <pre>{@code
     * apt install -y \
     *    build-essential \
     *    gfortran \
     *    gcc-doc
     * }</pre>
     * 
     * @param aptPackages apt package list.
     */
    public static String toAptInstallCommand(List<String> aptPackages) {
        
        StringJoiner rowJoiner = new StringJoiner(" \\\n");
        

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


    
    /** Generates a list of apt commands from a list of apt package names.
     * <p>
     * This method returns the following string when a list of package names is given.
     * </p>
     *
     * <pre>{@code
     * apt remove -y --purge \
     *    build-essential \
     *    gfortran \
     *    gcc-doc
     * }</pre>
     * 
     * @param aptPackages apt package list.
     */
    public static String toAptRemoveCommand(List<String> aptPackages) {
        
        StringJoiner rowJoiner = new StringJoiner(" \\\n");
        

        StringJoiner colJoiner = new StringJoiner(" ");
        for (int i=1; i<=aptPackages.size(); i++) {
            colJoiner.add(aptPackages.get(i-1));
            if (i%5 == 0) {
                rowJoiner.add("    " + colJoiner.toString());
                colJoiner = new StringJoiner(" ");
            }
        }

        return "apt remove -y --purge " + rowJoiner.toString();
        
    }



    /** Generates a list of apt commands from a list of apt package names.
     * <p>
     * This method returns a list of strings as shown in the following example.
     * </p>
     *
     * <pre>{@code
     * apt install -y build-essential
     * apt install -y gfortran
     * apt install -y gcc-doc
     * }</pre>
     * 
     * @param aptPackages apt package list.
     */
    public static List<String> toAptCommandList(List<String> aptPackages) {
        List<String> result = new ArrayList<>();

        for (int i=0; i<aptPackages.size(); i++) {
            result.add("apt install -y " + aptPackages.get(i));
        }
        
        return result;
    }
    

    

    // // --- setters and getters ---
    
    // public Path getInfile() {
    //     return infile;
    // }

    // public void setInfile(Path infile) {
    //     this.infile = infile;
    // }

}
