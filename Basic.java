import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Basic {

    static class Alignment{
        String x;
        String y;
        int cost;

        Alignment(String x, String y, int cost){
            this.x = x;
            this.y = y;
            this.cost = cost;
        }
    }

    static final int gapPenalty = 30;
    static final int[][] alphas = {
            {0, 110, 48, 94},
            {110, 0, 118, 48},
            {48, 118, 0, 110},
            {94, 48, 110, 0}
    };

    static final Map<Character, Integer> charToIndex = Map.of('A', 0, 'C', 1, 'G', 2, 'T', 3);

    static int mismatchCost(char a, char b) {
        return alphas[charToIndex.get(a)][charToIndex.get(b)];
    }

    private static double getMemoryInKB() {
        double total = Runtime.getRuntime().totalMemory();
        return (total-Runtime.getRuntime().freeMemory())/10e3;
    }

    private static double getTimeInMilliseconds() {
        return System.nanoTime()/10e6;
    }

    static Alignment sequenceAlignment(String s, String t) {
        int[][] dp = new int[s.length() + 1][t.length() + 1];

//        char[] chars = new char[1000000];
//        Arrays.fill(chars, 'a');

        // base case
        for (int c = 0; c <= t.length(); c++) {
            dp[0][c] = c * gapPenalty;
        }
        for (int r = 0; r <= s.length(); r++) {
            dp[r][0] = r * gapPenalty;
        }

        for (int i = 1; i <= s.length(); i++) {
            for (int j = 1; j <= t.length(); j++) {
                dp[i][j] = Math.min(mismatchCost(s.charAt(i - 1), t.charAt(j - 1)) + dp[i - 1][j - 1],
                        gapPenalty + Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }

        int minCost = dp[s.length()][t.length()];

        StringBuilder ss = new StringBuilder(), tt = new StringBuilder();
//        String ss = "";
//        String tt = "";
        // top down pass to get the actual alignments
        int i = s.length(), j = t.length();
        while (i > 0 || j > 0) {
            if (i >= 1 && j >= 1 && dp[i][j] == mismatchCost(s.charAt(i - 1), t.charAt(j - 1)) + dp[i - 1][j - 1]) {
//                    ss = ss + s.charAt(i-1);
//                    tt = tt + t.charAt(j-1);
                ss.append(s.charAt(i - 1));
                tt.append(t.charAt(j - 1));
                i--;
                j--;
            } else if (j >= 1 && dp[i][j] == gapPenalty + dp[i][j - 1]) {
                ss.append('_');
                tt.append(t.charAt(j - 1));
//                ss = ss + '_';
//                tt = tt + t.charAt(j-1);
                j--;
            } else {
//                ss = ss + s.charAt(i-1);
//                tt = tt + '_';
                ss.append(s.charAt(i - 1));
                tt.append('_');
                i--;
            }
        }

        return new Alignment(ss.reverse().toString(), tt.reverse().toString(), minCost);
//            return new Alignment(ss, tt, minCost);
//        double memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        

//        System.out.println(minCost);
//        System.out.println(ss.reverse().toString());
//        System.out.println(tt.reverse().toString());
//        System.out.println(getMemoryInKB() - memBefore);
//        return dp;
    }

    public static void main(String[] args) throws IOException {
        String inputFilePath = args[0];
        String outputFilePath = args[1];

        FileInputStream fstream = new FileInputStream(inputFilePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        StringBuilder sb = new StringBuilder();
        List<String> inputStrings = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            if (line.matches("\\d+")) { // current line represents an index
                int index = Integer.parseInt(line);
                sb.insert(index + 1, sb.toString());
            } else { // current line represents a base string
                if (sb.length() != 0) {
                    inputStrings.add(sb.toString());
                }
                sb = new StringBuilder(line);
            }
        }
        inputStrings.add(sb.toString());
        fstream.close();


        double beforeUsedMem=getMemoryInKB();
        double startTime = getTimeInMilliseconds();
        Alignment alignment = sequenceAlignment(inputStrings.get(0), inputStrings.get(1));
        double afterUsedMem = getMemoryInKB();
        double endTime = getTimeInMilliseconds();
        double totalUsage =  afterUsedMem-beforeUsedMem;
        double totalTime =  endTime - startTime;

//        double beforeUsedMem=getMemoryInKB();
//        double startTime = getTimeInMilliseconds();
//        long memorySize = ((com.sun.management.OperatingSystemMXBean)
//                ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
//
//        long freeMemorySize = ((com.sun.management.OperatingSystemMXBean)
//                ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
//        long beforeMem = memorySize - freeMemorySize;
//        sequenceAlignment(inputStrings.get(0), inputStrings.get(1));
//        memorySize = ((com.sun.management.OperatingSystemMXBean)
//                ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
//
//        freeMemorySize = ((com.sun.management.OperatingSystemMXBean)
//                ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
//        long afterMem = memorySize - freeMemorySize;
////        System.out.println((afterMem-beforeMem)/10e3);
//        double afterUsedMem = getMemoryInKB();
//        double endTime = getTimeInMilliseconds();
//        double totalUsage =  afterUsedMem-beforeUsedMem;
//        double totalTime =  endTime - startTime;

        List<String> lines = Arrays.asList(String.valueOf(alignment.cost), alignment.x, alignment.y, String.valueOf(totalTime), String.valueOf(totalUsage));
        Path file = Paths.get(outputFilePath);
        Files.write(file, lines, StandardCharsets.UTF_8);

    }
}
