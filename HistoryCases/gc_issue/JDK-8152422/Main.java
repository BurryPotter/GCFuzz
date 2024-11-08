package jprtlogparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author fa
 */
public class Main implements Runnable {

    private enum Format {NAME_TIME, TIME_NAME};
    private enum Sort {NO, NAME, FAST, SLOW};

    private final Sort sort;
    private final String[] logs;
    
    public static void main(String[] args) {
        if (args.length == 0 || args.length > 0 && args[0].startsWith("-")) {
            printHelp();
        } else {
            new Main(Sort.valueOf(System.getProperty("sort", "NO")), args).run();
        }
    }
    
    public Main(String... logs) {
        this(Sort.NO, logs);
    }

    private Main(Sort sort, String... logs) {
        this.sort = sort;
        if (sort == Sort.FAST || sort == Sort.SLOW) {
            Test.format = Format.TIME_NAME;
        } else {
            Test.format = Format.NAME_TIME;
        }
        this.logs = logs; 
    }
    
    private static void printHelp() {
        System.out.println("Usage: ");
        System.out.println("java [-Dsort=NO|NAME|FAST|SLOW] " + 
                Main.class.getCanonicalName() + "http://xxx.log  my.log ...");
    }

    public static void print(String... logs) {
        new Main(Sort.NO, logs).run();
    }
    public static void printSortedByName(String... logs) {
        new Main(Sort.NAME, logs).run();
    }
    public static void printSortFastFirst(String... logs) {
        new Main(Sort.FAST, logs).run();
    }
    public static void printSortSlowFirst(String... logs) {
        new Main(Sort.SLOW, logs).run();
    }
            
    public void run() {
        System.out.println("Yahoo: " + sort);
        for (String log: logs) {
            print(log);
        }
    }
    
    /**
     * The main method which does everything.
     * @param log either URL or file name.
     */
    private void print(String log) {
        System.out.println("--- " + log + " --- ");
        try {            
            List<Test> tests = new ArrayList<>();
//            findTests(read(log)).stream().filter(s -> s.name.indexOf("g1") < 0 && s.name.contains("survivor")).forEach(tests::add);
            findTests(read(log)).stream().forEach(tests::add);
            Test.maxNameLength = tests.stream().map(s -> s.name.length()).max((a, b) -> a - b).get();            
            tests.stream().sorted(new TestComparator(sort)).forEach(System.out::println);
            int totalTime  = new Double(tests.stream().collect(Collectors.summingDouble(s -> s.time))).intValue();
            System.out.println("SUMMARY: ");
            System.out.println("   Totat tests: " + tests.size());
            System.out.println("   Totat time: " + (totalTime / 60) + "m " + (totalTime % 60) + "s");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        System.out.println("------------------ ");
    }
    
    private List<Test> findTests(Stream<String> stream) {
        List<String> lines = new ArrayList<>();
        stream.forEach(lines::add);
        Pattern startP = Pattern.compile("TEST: (.*)");
        Pattern endP = Pattern.compile("TEST RESULT: (.*)");
        Pattern timeP = Pattern.compile(".*: (\\d+.\\d+) seconds");;
        
        List<Test> tests = new ArrayList<>();
        Test cur = null;
        for (String line : lines) {
            if (cur == null) {
                Matcher m = startP.matcher(line);
                if (m.matches()) {
                    cur = new Test(m.group(1));
                }
            } else {
                Matcher m = endP.matcher(line);
                if (m.matches()) {
                    cur.status = m.group(1);
                    tests.add(cur);
                    cur = null;
                } else {
                    m = timeP.matcher(line);
                    if (m.matches()) {
                        cur.time += Float.parseFloat(m.group(1));
                    }
                }
            }
        }
        return tests;
    }
          
    private Stream<String> read(String log) throws BadLuckException {
        try {
            if (log.startsWith("http://") || log.startsWith("https://") || log.startsWith("file://")) {
                return readFromURL(log);
            } else {
                return readFromFile(log);
            }
        } catch (Throwable t) {
            throw new BadLuckException(t);
        }
    }
    private Stream<String> readFromURL(String log) throws MalformedURLException, IOException {
        URL url = new URL(log);
        URLConnection conn = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        return reader.lines();
    }
    
    private Stream<String> readFromFile(String log) throws IOException {
        return Files.lines(Paths.get(log));
    }
    
    private static class BadLuckException extends Exception {
        BadLuckException(String m) {
            super(m);
        }
        BadLuckException(Throwable t) {
            super(t);
        }
        BadLuckException(String m, Throwable t) {
            super(m, t);
        }
        BadLuckException(Throwable t, String m) {
            super(m, t);
        }
    }
    
    private static class Test {
        private static final NumberFormat nf = new DecimalFormat("####0.00");
        private static int maxNameLength = 50;
        private static Format format;
        public String name;
        public float time;
        public String status;
        
        public Test(String name) {
            this.name = name;
            this.time = 0;
            this.status = null;
        }
        
        @Override
        public String toString() {
            String t = nf.format(time);
            switch (format) {
                case NAME_TIME: return name + ident(name) + " : " + t;
                default: return "           ".substring(t.length()) + t + " : " + name;
            }
        }
        
        private static String ident(String s) {
            int size = Test.maxNameLength - s.length();
            if (size > 0) {
                return new String(new char[size]).replace('\0', ' ');
            } else {
                return "";
            }
            
        }
    }
    
    private static class TestComparator implements Comparator<Test> {
        Sort sort;
        TestComparator(Sort s) {
            this.sort = s;
        }
        
        @Override
        public int compare(Test t1, Test t2) {
            switch (sort) {
                case NAME: return t1.name.compareTo(t2.name);
                case FAST: return new Float(t1.time).compareTo(t2.time);
                case SLOW: return new Float(t2.time).compareTo(t1.time);
                default: return -1;
            }
        }
    }            
}
