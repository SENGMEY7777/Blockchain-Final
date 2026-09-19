package org.example.controller;

import org.example.model.ProofNode;
import org.example.service.MerkleTreeService;
import org.example.utils.ConsoleUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class MerkleTreeController {

    private final MerkleTreeService treeService;
    private List<String> transactions;
    private List<List<byte[]>> currentTree;
    private String currentRootHex;
    private final Set<Integer> completedCases;

    public MerkleTreeController() {
        this.treeService = new MerkleTreeService();
        this.transactions = new ArrayList<>();
        this.currentTree = null;
        this.currentRootHex = null;
        this.completedCases = new HashSet<>();
    }

    public MerkleTreeController(MerkleTreeService treeService) {
        this.treeService = treeService != null ? treeService : new MerkleTreeService();
        this.transactions = new ArrayList<>();
        this.currentTree = null;
        this.currentRootHex = null;
        this.completedCases = new HashSet<>();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            ConsoleUtils.printHeader(completedCases);
            System.out.print(ConsoleUtils.PROMPT + "Enter your choice (0-5): " + ConsoleUtils.RESET);
            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";

            switch (input) {
                case "1":
                    handleCreateTransactions(scanner);
                    break;
                case "2":
                    handleBuildMerkleTree();
                    break;
                case "3":
                    handleVerifyProof(scanner);
                    break;
                case "4":
                    handleDetectTampering(scanner);
                    break;
                case "5":
                    handleRunVerificationTests();
                    break;
                case "0":
                case "exit":
                case "Exit":
                case "EXIT":
                    ConsoleUtils.printExitMessage();
                    running = false;
                    break;
                default:
                    ConsoleUtils.printErrorBox("Invalid Option '" + input + "'",
                            "Please choose a valid option between 0 and 5 from the menu.");
                    break;
            }

            if (running) {
                running = promptContinue(scanner);
                if (!running) {
                    ConsoleUtils.printExitMessage();
                }
            }
        }
    }

    public void handleCreateTransactions(Scanner scanner) {
        ConsoleUtils.printCaseHeader("CASE 1: CREATE TRANSACTIONS");
        System.out.print(ConsoleUtils.YELLOW + "Enter number of transactions (minimum 8, or press Enter for default 8): " + ConsoleUtils.RESET);
        String countInput = scanner.nextLine().trim();

        int count = 8;
        List<String> newTxs = new ArrayList<>();

        if (countInput.equalsIgnoreCase("default") || countInput.isEmpty()) {
            newTxs = createDefaultTransactions(8);
            System.out.println(ConsoleUtils.GREEN + "Loaded 8 sample transactions [10, 20, 30, 40, 50, 60, 70, 80] automatically." + ConsoleUtils.RESET);
        } else {
            try {
                int parsed = Integer.parseInt(countInput);
                if (parsed < 8) {
                    System.out.println(ConsoleUtils.YELLOW + "Minimum 8 required. Using 8 transactions." + ConsoleUtils.RESET);
                } else {
                    count = parsed;
                }
            } catch (NumberFormatException e) {
                System.out.println(ConsoleUtils.YELLOW + "Invalid number. Using 8 transactions." + ConsoleUtils.RESET);
            }

            System.out.println(ConsoleUtils.WHITE + "Enter " + count + " transactions (or type 'default' to use 10, 20, 30...):" + ConsoleUtils.RESET);
            System.out.print(ConsoleUtils.YELLOW + "Enter transaction 1: " + ConsoleUtils.RESET);
            String first = scanner.nextLine().trim();
            if (first.equalsIgnoreCase("default") || first.isEmpty()) {
                newTxs = createDefaultTransactions(count);
                System.out.println(ConsoleUtils.GREEN + "Loaded " + count + " sample transactions automatically." + ConsoleUtils.RESET);
            } else {
                while (!treeService.isValidTransaction(first)) {
                    System.out.print(ConsoleUtils.RED + "Invalid input. Only digits (0-9) are allowed (e.g. 10, 20). Enter transaction 1 again: " + ConsoleUtils.RESET);
                    first = scanner.nextLine().trim();
                }
                newTxs.add(first);

                for (int i = 1; i < count; i++) {
                    System.out.print(ConsoleUtils.YELLOW + "Enter transaction " + (i + 1) + ": " + ConsoleUtils.RESET);
                    String tx = scanner.nextLine().trim();
                    while (!treeService.isValidTransaction(tx)) {
                        System.out.print(ConsoleUtils.RED + "Invalid input. Only digits (0-9) are allowed (e.g. 10, 20). Enter transaction "
                                + (i + 1) + " again: " + ConsoleUtils.RESET);
                        tx = scanner.nextLine().trim();
                    }
                    newTxs.add(tx);
                }
            }
        }

        this.transactions = newTxs;
        this.currentTree = treeService.buildMerkleTree(transactions);
        this.currentRootHex = treeService.getRootHex(currentTree);
        this.completedCases.add(1);

        ConsoleUtils.printTransactionsTable(transactions);
        System.out.println(ConsoleUtils.GREEN + "Transactions stored and Merkle Tree computed successfully!" + ConsoleUtils.RESET);
    }

    public void handleBuildMerkleTree() {
        ensureTransactionsLoaded();
        ConsoleUtils.printCaseHeader("CASE 2: BUILD & DISPLAY MERKLE TREE");
        this.currentTree = treeService.buildMerkleTree(transactions);
        this.currentRootHex = treeService.getRootHex(currentTree);
        this.completedCases.add(2);

        ConsoleUtils.printTreeSummaryTable(transactions.size(), currentTree.size(), currentRootHex);
        ConsoleUtils.printMerkleTree(currentTree);
    }

    public void handleVerifyProof(Scanner scanner) {
        ensureTransactionsLoaded();
        ConsoleUtils.printCaseHeader("CASE 3: VERIFY PROOF (AUDIT PATH)");

        int targetIndex = readIndex(scanner, transactions.size());
        String targetTx = transactions.get(targetIndex);
        List<ProofNode> proof = treeService.getMerkleProof(currentTree, targetIndex);
        boolean isProofValid = treeService.verifyProof(targetTx, proof, currentRootHex);
        this.completedCases.add(3);

        ConsoleUtils.printProofTable(targetTx, targetIndex, proof, isProofValid);
    }

    public void handleDetectTampering(Scanner scanner) {
        ensureTransactionsLoaded();
        ConsoleUtils.printCaseHeader("CASE 4: DETECT TAMPERING");

        int targetIndex = readIndex(scanner, transactions.size());
        String originalTx = transactions.get(targetIndex);

        System.out.print(ConsoleUtils.YELLOW + "Enter a tampered value for transaction index " + targetIndex
                + " (original: " + originalTx + "): " + ConsoleUtils.RESET);
        String tamperedTx = scanner.nextLine().trim();
        while (!treeService.isValidTransaction(tamperedTx)) {
            System.out.print(ConsoleUtils.RED + "Invalid input. Only digits (0-9) are allowed. Enter it again: " + ConsoleUtils.RESET);
            tamperedTx = scanner.nextLine().trim();
        }

        List<String> tamperedTransactions = new ArrayList<>(transactions);
        tamperedTransactions.set(targetIndex, tamperedTx);

        List<List<byte[]>> tamperedTree = treeService.buildMerkleTree(tamperedTransactions);
        String tamperedRootHex = treeService.getRootHex(tamperedTree);
        boolean isRootMatched = currentRootHex.equals(tamperedRootHex);
        this.completedCases.add(4);

        ConsoleUtils.printTamperTable(originalTx, tamperedTx, currentRootHex, tamperedRootHex, isRootMatched);
    }

    public void handleRunVerificationTests() {
        ensureTransactionsLoaded();
        ConsoleUtils.printCaseHeader("CASE 5: VERIFICATION TEST RESULTS");

        int sampleIndex = 0;
        String sampleTx = transactions.get(sampleIndex);
        List<ProofNode> proof = treeService.getMerkleProof(currentTree, sampleIndex);
        boolean isProofValid = treeService.verifyProof(sampleTx, proof, currentRootHex);

        List<String> tamperedTransactions = new ArrayList<>(transactions);
        tamperedTransactions.set(sampleIndex, sampleTx + "999");
        String tamperedRootHex = treeService.getRootHex(treeService.buildMerkleTree(tamperedTransactions));
        boolean isRootMatched = currentRootHex.equals(tamperedRootHex);

        boolean oddCaseHandled = treeService.verifyOddCountHandling();
        this.completedCases.add(5);

        ConsoleUtils.printVerificationTests(transactions, proof, isProofValid, isRootMatched, oddCaseHandled);
    }

    private boolean promptContinue(Scanner scanner) {
        while (true) {
            System.out.print(ConsoleUtils.YELLOW + "\nDo you want to continue? (y/n): " + ConsoleUtils.RESET);
            if (!scanner.hasNextLine()) {
                return false;
            }
            String answer = scanner.nextLine().trim();
            if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes") || answer.isEmpty()) {
                System.out.println();
                return true;
            } else if (answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("no")) {
                return false;
            } else {
                ConsoleUtils.printErrorBox("Unrecognized Response '" + answer + "'",
                        "Please enter 'y' to continue or 'n' to exit.");
            }
        }
    }

    private void ensureTransactionsLoaded() {
        if (transactions == null || transactions.isEmpty() || currentTree == null) {
            System.out.println(ConsoleUtils.YELLOW + "No transactions loaded yet. Initializing with default 8 sample transactions [10, 20, 30...80]." + ConsoleUtils.RESET);
            this.transactions = createDefaultTransactions(8);
            this.currentTree = treeService.buildMerkleTree(transactions);
            this.currentRootHex = treeService.getRootHex(currentTree);
        }
    }

    private List<String> createDefaultTransactions(int count) {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(String.valueOf(i * 10));
        }
        return list;
    }

    private int readIndex(Scanner scanner, int transactionCount) {
        while (true) {
            System.out.print(ConsoleUtils.YELLOW + "Enter transaction index to verify (0-" + (transactionCount - 1) + "): " + ConsoleUtils.RESET);
            String input = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < transactionCount) {
                    return index;
                }
            } catch (NumberFormatException ignored) {
            }
            ConsoleUtils.printErrorBox("Invalid Index: '" + input + "'",
                    "Please enter a valid numeric index between 0 and " + (transactionCount - 1) + ".");
        }
    }

    public List<String> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public List<List<byte[]>> getCurrentTree() {
        return currentTree;
    }

    public String getCurrentRootHex() {
        return currentRootHex;
    }

    public Set<Integer> getCompletedCases() {
        return new HashSet<>(completedCases);
    }
}
