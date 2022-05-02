import sys
from resource import *
import time
import psutil

GAP_PENALTY = 30
penalty = [[0, 110, 48, 94],
           [110, 0, 118, 48],
           [48, 118, 0, 110],
           [94, 48, 110, 0]]


def mismatchCost(a, b):
    s = "ACGT"
    i = s.find(a)
    j = s.find(b)

    return penalty[i][j]

def alignment(s, t):
    m, n = len(s), len(t)
    dp = [[0 for col in range(n + 1)] for row in range(m + 1)]

    # base case
    for c in range(n+1):
        dp[0][c] = c * GAP_PENALTY
    for r in range(m+1):
        dp[r][0] = r * GAP_PENALTY

    for i in range(1, m+1):
        for j in range(1, n+1):
            dp[i][j] = min(mismatchCost(s[i-1], t[j-1]) + dp[i-1][j-1], GAP_PENALTY + min(dp[i-1][j], dp[i][j-1]))

    minCost = dp[m][n]

    ss, tt = "", ""
    i, j = m, n
    while i > 0 or j > 0:
        if i >= 1 and j >= 1 and dp[i][j] == mismatchCost(s[i-1], t[j-1]) + dp[i-1][j-1]:
            ss = s[i-1] + ss
            tt = t[j-1] + tt
            i -= 1
            j -= 1
        elif j >= 1 and dp[i][j] == GAP_PENALTY + dp[i][j-1]:
            ss = '_' + ss
            tt = t[j-1] + tt
            j -= 1
        else:
            ss = s[i-1] + ss
            tt = '_' + tt
            i -= 1

    return minCost, ss, tt

# main entry point
inputFilePath = sys.argv[1]
outputFilePath = sys.argv[2]
f = open(inputFilePath, "r")

inputStrings = []
s = ""

for line in f.readlines():
    line = line.strip()
    if line.isdigit():
        index = int(line)
        s = s[0:index + 1] + s + s[index + 1:]
    else:
        if len(s) > 0:
            inputStrings.append(s)
        s = line

inputStrings.append(s)

minCost, s1, s2 = alignment(inputStrings[0], inputStrings[1])
f = open(outputFilePath, "wt")
f.write('\n'.join([str(minCost), s1, s2]))
f.close()
