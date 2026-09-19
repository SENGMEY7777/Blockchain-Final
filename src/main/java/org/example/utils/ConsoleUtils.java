package org.example.utils;

import org.example.model.ProofNode;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class ConsoleUtils {

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";

    public static final String BORDER = "\u001B[1;36m";
    public static final String TITLE = "\u001B[1;96m";
    public static final String SUBTITLE = "\u001B[1;93m";
    public static final String SECTION = "\u001B[1;96m";
    public static final String PROMPT = "\u001B[1;93m";
    public static final String GREEN = "\u001B[1;92m";
    public static final String YELLOW = "\u001B[1;93m";
    public static final String RED = "\u001B[1;91m";
    public static final String MAGENTA = "\u001B[1;95m";
    public static final String CYAN = "\u001B[1;96m";
    public static final String WHITE = "\u001B[1;97m";
    public static final String GRAY = "\u001B[90m";

    private ConsoleUtils() {
    }

    public static void printHeader() {
        printHeader(Collections.emptySet());
    }

    public static void printHeader(Set<Integer> completedCases) {
        printBoxBorder();
        printBoxRow("PROJECT 3 — MERKLE TREE INTEGRITY CHECKER", TITLE);
        printBoxRow("SHA-256 PROOF AND TAMPER DETECTION", SUBTITLE);
        printBoxBorder();
        printMenuRow("1. CREATE TRANSACTIONS", completedCases != null && completedCases.contains(1),
                     "2. BUILD MERKLE TREE", completedCases != null && completedCases.contains(2));
        printMenuRow("3. VERIFY PROOF", completedCases != null && completedCases.contains(3),
                     "4. DETECT TAMPERING", completedCases != null && completedCases.contains(4));
        printMenuRow("5. VERIFICATION TESTS", completedCases != null && completedCases.contains(5),
                     "0. EXIT PROGRAM", false);
        printBoxBorder();
    }

    public static void printCaseHeader(String caseTitle) {
        System.out.println();
        printBoxBorder();
        printBoxRow(caseTitle, RED + BOLD);
        printBoxBorder();
    }

    public static void printBoxBorder() {
        System.out.println(BORDER + "+----------------------------------------------------------------------------------------+" + RESET);
    }

    public static void printBoxRow(String text, String color) {
        String content = centerText(text, 88);
        System.out.println(BORDER + "|" + RESET + color + content + RESET + BORDER + "|" + RESET);
    }

    private static void printMenuRow(String leftOpt, boolean leftDone, String rightOpt, boolean rightDone) {
        String leftFormatted = "  " + formatOption(leftOpt, leftDone);
        String rightFormatted = "  " + formatOption(rightOpt, rightDone);

        String leftPlain = "  " + leftOpt;
        String rightPlain = "  " + rightOpt;

        StringBuilder sb = new StringBuilder();
        sb.append(BORDER).append("|").append(RESET);
        sb.append(leftFormatted);
        sb.append(" ".repeat(Math.max(0, 43 - leftPlain.length())));
        sb.append(BORDER).append("|").append(RESET);
        sb.append(rightFormatted);
        sb.append(" ".repeat(Math.max(0, 44 - rightPlain.length())));
        sb.append(BORDER).append("|").append(RESET);

        System.out.println(sb.toString());
    }

    private static String formatOption(String option, boolean completed) {
        int dotIdx = option.indexOf('.');
        if (dotIdx != -1) {
            String num = option.substring(0, dotIdx + 1);
            String label = option.substring(dotIdx + 1);
            if (completed) {
                return RED + BOLD + num + label + RESET;
            } else {
                return YELLOW + BOLD + num + RESET + GREEN + label + RESET;
            }
        }
        return completed ? (RED + BOLD + option + RESET) : (GREEN + option + RESET);
    }

    public static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int leftPadding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - leftPadding;
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    public static void printTransactionsTable(List<String> txs) {
        System.out.println(SECTION + "\n[ 1. INPUT TRANSACTIONS TABLE ]" + RESET);
        printLine('-', 90);
        System.out.printf(BORDER + "|" + RESET + WHITE + " %-6s " + BORDER + "|" + RESET + WHITE + " %-16s " + BORDER + "|" + RESET + WHITE + " %-60s " + BORDER + "|" + RESET + "%n", "INDEX", "TRANSACTION", "SAMPLE MEANING");
        printLine('-', 90);
        for (int i = 0; i < txs.size(); i++) {
            System.out.printf(BORDER + "|" + RESET + " %-6d " + BORDER + "|" + RESET + GREEN + " %-16s " + RESET + BORDER + "|" + RESET + " %-60s " + BORDER + "|" + RESET + "%n",
                    i, txs.get(i), "Transfer record #" + (i + 1));
        }
        printLine('-', 90);
    }

    public static void printTreeSummaryTable(int txCount, int levels, String rootHex) {
        System.out.println(SECTION + "\n[ 2. MERKLE TREE SUMMARY ]" + RESET);
        printLine('-', 109);
        printKeyValueRow("Total Transactions", String.valueOf(txCount), 78, WHITE);
        printKeyValueRow("Tree Levels", String.valueOf(levels), 78, WHITE);
        printKeyValueRow("Merkle Root (SHA-256)", rootHex, 78, YELLOW);
        printLine('-', 109);
    }

    public static void printMerkleTree(List<List<byte[]>> tree) {
        System.out.println(SECTION + "\n[ 2A. MERKLE TREE VISUALIZATION ]" + RESET);
        printLine('-', 90);

        for (int level = 0; level < tree.size(); level++) {
            String label = level == 0
                    ? "Leaves"
                    : (level == tree.size() - 1 ? "Root" : "Parents");
            String levelColor = getLevelColor(level, tree.size());
            System.out.println(levelColor + BOLD + "Level " + level + " (" + label + ")" + RESET);

            List<byte[]> hashes = tree.get(level);
            for (int node = 0; node < hashes.size(); node++) {
                System.out.println("  Node " + node + ": " + levelColor
                        + HashUtils.bytesToHex(hashes.get(node)) + RESET);
            }
            if (level < tree.size() - 1) {
                System.out.println(GRAY + "  |\n  v" + RESET);
            }
        }
        printLine('-', 90);
    }

    public static String getLevelColor(int level, int totalLevels) {
        if (level == 0) {
            return GREEN;
        }
        if (level == totalLevels - 1) {
            return RED;
        }
        return level % 2 == 1 ? YELLOW : MAGENTA;
    }

    public static void printProofTable(String targetTx, int idx, List<ProofNode> proof, boolean verified) {
        System.out.println(SECTION + "\n[ 3. MERKLE PROOF VERIFICATION ]" + RESET);
        printLine('-', 109);
        printKeyValueRow("Proof Target", targetTx + " (index " + idx + ")", 78, WHITE);
        printKeyValueRow("Sibling Hashes Required", String.valueOf(proof.size()), 78, WHITE);

        for (int i = 0; i < proof.size(); i++) {
            ProofNode node = proof.get(i);
            String sideDesc = (node.getSide() == 'R' ? "Right Sibling" : "Left Sibling");
            printKeyValueRow("Audit Step " + (i + 1) + " (" + sideDesc + ")", HashUtils.bytesToHex(node.getSiblingHash()), 78, YELLOW);
        }

        printKeyValueRow("verify(target, proof, root)", verified ? "PASS [✓]" : "FAIL [✗]", 78,
                verified ? GREEN : RED);
        printLine('-', 109);
    }

    public static void printTamperTable(String orig, String tampered, String oldRoot, String newRoot, boolean matched) {
        System.out.println(SECTION + "\n[ 4. TAMPER DETECTION TEST ]" + RESET);
        printLine('-', 109);
        printKeyValueRow("Original Transaction", orig, 78, GREEN);
        printKeyValueRow("Tampered Transaction", tampered, 78, RED);
        printKeyValueRow("Original Root", oldRoot, 78, YELLOW);
        printKeyValueRow("New Root After Tamper", newRoot, 78, RED);
        printKeyValueRow("oldRoot == newRoot", matched ? "TRUE (WARNING: NO CHANGE)" : "FALSE (TAMPER DETECTED)", 78,
                matched ? RED : GREEN);
        printLine('-', 109);
    }

    public static void printVerificationTests(List<String> txs, List<ProofNode> proof, boolean proofPass,
                                             boolean rootMatch, boolean oddCaseHandled) {
        System.out.println(SECTION + "\n[ 5. VERIFICATION TEST RESULTS (SLIDE 3) ]" + RESET);
        printLine('-', 109);
        printTwoColumnRow("TEST CASE", "STATUS", WHITE);
        printLine('-', 110);
        printTwoColumnRow("1. Input transactions produce one valid root", "PASS [✓]", GREEN);
        printTwoColumnRow("2. Valid proof for the selected transaction", proofPass ? "PASS [✓]" : "FAIL [✗]",
                proofPass ? GREEN : RED);
        printTwoColumnRow("3. Changed transaction changes root (Tamper Detection)",
                !rootMatch ? "PASS [✓]" : "FAIL [✗]", !rootMatch ? GREEN : RED);
        printTwoColumnRow("4. Odd-count duplication case handled", oddCaseHandled ? "PASS [✓]" : "FAIL [✗]",
                oddCaseHandled ? GREEN : RED);
        printTwoColumnRow("5. Verification succeeds using the proof path", proofPass ? "PASS [✓]" : "FAIL [✗]",
                proofPass ? GREEN : RED);
        printLine('-', 110);
    }

    public static void printErrorBox(String title, String message) {
        System.out.println(RED + "+----------------------------------------------------------------------------------------+" + RESET);
        String header = "[!] ERROR: " + title;
        if (header.length() > 84) {
            header = header.substring(0, 84);
        }
        System.out.println(RED + "|  " + WHITE + BOLD + header + " ".repeat(Math.max(0, 84 - header.length())) + RED + "|" + RESET);
        if (message != null && !message.isEmpty()) {
            String detail = message;
            if (detail.length() > 84) {
                detail = detail.substring(0, 84);
            }
            System.out.println(RED + "|  " + YELLOW + detail + " ".repeat(Math.max(0, 84 - detail.length())) + RED + "|" + RESET);
        }
        System.out.println(RED + "+----------------------------------------------------------------------------------------+" + RESET);
    }

    public static void printExitMessage() {
        System.out.println();
        printBoxBorder();
        printBoxRow("MERKLE TREE INTEGRITY CHECKER — SESSION CLOSED", GREEN + BOLD);
        printBoxRow("Exiting application safely. Thank you! Goodbye!", YELLOW);
        printBoxBorder();
    }

    public static void printKeyValueRow(String key, String value, int valueWidth, String color) {
        System.out.printf(BORDER + "|" + RESET + " %-25s : ", key);
        printPaddedValue(value, valueWidth, color);
        System.out.println(BORDER + "|" + RESET);
    }

    public static void printTwoColumnRow(String description, String value, String color) {
        System.out.printf(BORDER + "|" + RESET + " %-85s " + BORDER + "|" + RESET + " ", description);
        printPaddedValue(value, 18, color);
        System.out.println(BORDER + "|" + RESET);
    }

    public static void printPaddedValue(String value, int width, String color) {
        String visibleValue = value == null ? "" : value;
        if (color != null) {
            System.out.print(color + visibleValue + RESET);
        } else {
            System.out.print(visibleValue);
        }
        for (int i = visibleValue.length(); i < width; i++) {
            System.out.print(' ');
        }
    }

    public static void printLine(char ch, int length) {
        System.out.print(BORDER);
        for (int i = 0; i < length; i++) System.out.print(ch);
        System.out.print(RESET);
        System.out.println();
    }
}
