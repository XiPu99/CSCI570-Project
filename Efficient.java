import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Efficient {

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

    static String reverseString(String s){
        return (new StringBuilder(s).reverse().toString());
    }

    private static double getMemoryInKB() {
        double total = Runtime.getRuntime().totalMemory();
        return (total-Runtime.getRuntime().freeMemory())/10e3;
    }

    private static double getTimeInMilliseconds() {
        return System.nanoTime()/10e6;
    }

    static Alignment naiveSequenceAlignment(String s, String t) {
        int[][] dp = new int[s.length() + 1][t.length() + 1];

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
        // top down pass to get the actual alignments
        int i = s.length(), j = t.length();
        while (i > 0 || j > 0) {
            if (i >= 1 && j >= 1 && dp[i][j] == mismatchCost(s.charAt(i - 1), t.charAt(j - 1)) + dp[i - 1][j - 1]) {
                ss.append(s.charAt(i - 1));
                tt.append(t.charAt(j - 1));
                i--;
                j--;
            } else if (j >= 1 && dp[i][j] == gapPenalty + dp[i][j - 1]) {
                ss.append('_');
                tt.append(t.charAt(j - 1));
                j--;
            } else {
                ss.append(s.charAt(i - 1));
                tt.append('_');
                i--;
            }
        }

        return new Alignment(ss.reverse().toString(), tt.reverse().toString(), minCost);
//        return new Alignment(ss, tt, minCost);
    }

    static Alignment sequenceAlignment(String s, String t){
        if(s.length() <= 2 || t.length() <= 2){
            return naiveSequenceAlignment(s, t);
        }

        String sLeft = s.substring(0, s.length()/2);
        String sRight = s.substring(s.length()/2);

        int[] leftToRight = splitCost(sLeft, t);
        int[] rightToLeft = splitCost(reverseString(sRight), reverseString(t));
//        int[] rightToLeft = splitCost(sRight, t);


        int splitIndex = -1;
        int minCost = Integer.MAX_VALUE;
        int n = leftToRight.length;

        for(int i = 0; i < n; i++){
            if(leftToRight[i] + rightToLeft[n-i-1] < minCost){
                minCost = leftToRight[i] + rightToLeft[n-i-1];
                splitIndex = i;
            }
        }


        Alignment left = sequenceAlignment(sLeft, t.substring(0, splitIndex));
        Alignment right = sequenceAlignment(sRight, t.substring(splitIndex));

        return new Alignment(left.x+right.x, left.y+right.y, left.cost + right.cost);
    }

    static int[] splitCost(String s, String t){
        int[] dp = new int[t.length()+1];

        // base case
        for(int c = 0; c <= t.length(); c++){
            dp[c] = gapPenalty * c;
        }

        for (int i = 1; i <= s.length(); i++) {
            int[] temp = new int[t.length()+1];
            temp[0] = gapPenalty * i;
            for (int j = 1; j <= t.length(); j++) {
                temp[j] = Math.min(mismatchCost(s.charAt(i - 1), t.charAt(j - 1)) + dp[j - 1],
                        gapPenalty + Math.min(dp[j], temp[j - 1]));
            }
            dp = temp;
        }

        return dp;
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
//        char[] chars = new char[1000000];
//        Arrays.fill(chars, 'a');
//        System.out.println(inputStrings.get(0).length());
//        System.out.println(inputStrings.get(1).length());
        Alignment alignment = sequenceAlignment(inputStrings.get(0), inputStrings.get(1));
        double afterUsedMem = getMemoryInKB();
        double endTime = getTimeInMilliseconds();
        double totalUsage =  afterUsedMem-beforeUsedMem;
        double totalTime =  endTime - startTime;

        List<String> lines = Arrays.asList(String.valueOf(alignment.cost), alignment.x, alignment.y, String.valueOf(totalTime), String.valueOf(totalUsage));
        Path file = Paths.get(outputFilePath);
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
