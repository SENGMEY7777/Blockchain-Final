package org.example.service;

import org.example.model.ProofNode;
import org.example.utils.HashUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class MerkleTreeService {

    public static final Pattern TRANSACTION_PATTERN = Pattern.compile("^[0-9]+$");

    public boolean isValidTransaction(String value) {
        return value != null && TRANSACTION_PATTERN.matcher(value.trim()).matches();
    }

    public List<List<byte[]>> buildMerkleTree(List<String> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one transaction is required");
        }

        List<List<byte[]>> tree = new ArrayList<>();
        List<byte[]> currentLayer = new ArrayList<>();

        for (String item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Transactions cannot contain null values");
            }
            currentLayer.add(HashUtils.sha256(item.getBytes(StandardCharsets.UTF_8)));
        }
        tree.add(Collections.unmodifiableList(currentLayer));

        while (currentLayer.size() > 1) {
            List<byte[]> paddedLayer = new ArrayList<>(currentLayer);
            if (paddedLayer.size() % 2 == 1) {
                paddedLayer.add(paddedLayer.get(paddedLayer.size() - 1));
            }

            List<byte[]> nextLayer = new ArrayList<>();
            for (int i = 0; i < paddedLayer.size(); i += 2) {
                byte[] left = paddedLayer.get(i);
                byte[] right = paddedLayer.get(i + 1);
                nextLayer.add(HashUtils.sha256(HashUtils.concatBytes(left, right)));
            }

            tree.add(Collections.unmodifiableList(nextLayer));
            currentLayer = nextLayer;
        }

        return tree;
    }

    public String getRootHex(List<List<byte[]>> tree) {
        if (tree == null || tree.isEmpty()) {
            throw new IllegalArgumentException("Merkle tree cannot be empty");
        }
        byte[] rootBytes = tree.get(tree.size() - 1).get(0);
        return HashUtils.bytesToHex(rootBytes);
    }

    public List<ProofNode> getMerkleProof(List<List<byte[]>> tree, int leafIndex) {
        if (tree == null || tree.isEmpty() || tree.get(0).isEmpty()) {
            throw new IllegalArgumentException("The Merkle tree cannot be empty");
        }
        if (leafIndex < 0 || leafIndex >= tree.get(0).size()) {
            throw new IndexOutOfBoundsException("Leaf index " + leafIndex + " is outside the tree leaves (0.."
                    + (tree.get(0).size() - 1) + ")");
        }

        List<ProofNode> proof = new ArrayList<>();
        int index = leafIndex;

        for (int layerIdx = 0; layerIdx < tree.size() - 1; layerIdx++) {
            List<byte[]> layer = tree.get(layerIdx);

            if (index % 2 == 0) {
                int siblingIndex = (index + 1 < layer.size()) ? index + 1 : index;
                proof.add(new ProofNode(layer.get(siblingIndex), 'R'));
            } else {
                proof.add(new ProofNode(layer.get(index - 1), 'L'));
            }
            index /= 2;
        }
        return proof;
    }

    public boolean verifyProof(String item, List<ProofNode> proof, String expectedRootHex) {
        if (item == null || proof == null || expectedRootHex == null) {
            return false;
        }

        byte[] currentHash = HashUtils.sha256(item.getBytes(StandardCharsets.UTF_8));

        for (ProofNode node : proof) {
            if (node.getSide() == 'R') {
                currentHash = HashUtils.sha256(HashUtils.concatBytes(currentHash, node.getSiblingHash()));
            } else {
                currentHash = HashUtils.sha256(HashUtils.concatBytes(node.getSiblingHash(), currentHash));
            }
        }

        return HashUtils.bytesToHex(currentHash).equalsIgnoreCase(expectedRootHex);
    }

    public boolean verifyOddCountHandling() {
        List<String> oddTransactions = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            oddTransactions.add(String.valueOf(i * 10));
        }

        List<List<byte[]>> oddTree = buildMerkleTree(oddTransactions);
        return oddTree.size() == 5
                && oddTree.get(oddTree.size() - 1).size() == 1;
    }
}
