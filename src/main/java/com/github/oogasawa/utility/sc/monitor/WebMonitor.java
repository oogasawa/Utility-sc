package com.github.oogasawa.utility.sc.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class WebMonitor {

    private static final Logger logger = Logger.getLogger("WebMonitor");
    
    Map<String, Integer> statusCodeStats = new TreeMap<String, Integer>();

    String[] codeArray = {
        "200", "500", "501", "502", "503", "504", "505"
    };
    
    
    public static void main(String[] args) {
        try {
            LogManager.getLogManager()
                    .readConfiguration(WebMonitor.class.getClassLoader().getResourceAsStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String url = "https://ddbj.nig.ac.jp/public/ddbj_database/README.TXT";
        WebMonitor monitor = new WebMonitor();
        monitor.monitor(url);
    }

    public void addStats(String code, Integer count, Map<String, Integer> stats) {
        
        if (statusCodeStats.containsKey(code)) {
            Integer count0 = statusCodeStats.get(code);
            statusCodeStats.put(code, count0 + count);
        }
        else {
            statusCodeStats.put(code, count);
        }
    }

    
    public void monitor(String url) {
        Timer timer = new Timer();

        int[] minutesToRunExternalProgram = { 0, 10, 20, 30, 40, 50 };
        int[] minutesToRunFunction = { 59 };

        scheduleTasks(timer, minutesToRunExternalProgram, new ExecuteExternalProgramTask(url));        
        scheduleTasks(timer, minutesToRunFunction, new ExecuteSpecificFunctionTask());        
    }
    

    private void scheduleTasks(Timer timer, int[] minutes, TimerTask task) {
        for (int minute : minutes) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.MINUTE, minute);

            // If the time has already passed, schedule for the next hour
            if (calendar.getTime().before(new Date())) {
                calendar.add(Calendar.HOUR_OF_DAY, 1);
            }

            // Schedule the task at the specific minute every hour
            TimerTask clonedTask = null;
            if (task instanceof ExecuteExternalProgramTask) {
                clonedTask = (TimerTask)((ExecuteExternalProgramTask)task).clone();
                timer.scheduleAtFixedRate(clonedTask, calendar.getTime(), 60 * 60 * 1000);
            }
            else if (task instanceof ExecuteSpecificFunctionTask) {
                clonedTask = (TimerTask)((ExecuteSpecificFunctionTask)task).clone();
                timer.scheduleAtFixedRate(clonedTask, calendar.getTime(), 60 * 60 * 1000);
            }

        }
    }


    
    class ExecuteExternalProgramTask extends TimerTask {

        String url = null;

        public ExecuteExternalProgramTask(String url) {
            this.url = url;
        }
        
        @Override
        public void run() {
            try {
                // Execute the external program
                Process p
                    = new ProcessBuilder("curl", "-i", url)
                    .start();
                p.waitFor();

                InputStream stdout = p.getInputStream();
                String text = new String(stdout.readAllBytes(), StandardCharsets.UTF_8);

                String statusCode = parseStatusCode(text);

                if (Arrays.asList(codeArray).contains(statusCode)) {
                    addStats(statusCode, 1, statusCodeStats);
                }
                else {
                    addStats("Other", 1, statusCodeStats);
                }

                logger.info(String.format("StatusCode: %s", statusCode));
                //printHead(text);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Clone method to create new instances of TimerTask
        @Override
        public Object clone() {
            return new ExecuteExternalProgramTask(this.url);
        }

        
        public void addStats(String code, Integer count, Map<String, Integer> stats) {
            if (statusCodeStats.containsKey(code)) {
                Integer count0 = statusCodeStats.get(code);
                statusCodeStats.put(code, count0 + count);
            } else {
                statusCodeStats.put(code, count);
            }
        }


        public String parseStatusCode(String text) {
            String code = null;
            Pattern pCode = Pattern.compile("^HTTP/1.1\\s+([0-9]+)");
            String[] lines = text.split("\n");
            for (String line : lines) {
                Matcher m = pCode.matcher(line);
                if (m.find()) {
                    code = m.group(1);
                    break;
                }

            }
            return code;
        }

        // for testing purpose.
        public void printHead(String text) {
            String[] lines = text.split("\n");
            Stream.of(lines)
                .limit(10)
                .forEach(l->{System.out.println(l);});
        }
        
    }



    
    class ExecuteSpecificFunctionTask extends TimerTask {

        @Override
        public void run() {
            printStats();
            clearStatusCodeStats();
        }

        // Clone method to create new instances of TimerTask
        @Override
        public Object clone() {
            return new ExecuteSpecificFunctionTask();
        }

        public List<String> arrayToList(String[] a) {
            List<String> list = new ArrayList<String>();
            for (String s: a) {
                list.add(s);
            }
            return list;
        }
        

        public void printStats() {
            List<String> codeList = arrayToList(codeArray);
            codeList.add("Other");

            StringJoiner joiner = new StringJoiner("\t");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedTime = LocalDateTime.now().format(formatter);
            joiner.add(formattedTime);
            for (String code: codeArray) {
                if (statusCodeStats.containsKey(code)) {
                    joiner.add(String.valueOf(statusCodeStats.get(code)));
                }
                else {
                    joiner.add("0");
                }
            }

            logger.info(joiner.toString());
            System.out.println(joiner.toString());
        }

        public void clearStatusCodeStats() {
            statusCodeStats = new TreeMap<String, Integer>();
        }

    }

}

