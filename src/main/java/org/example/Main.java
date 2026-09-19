package org.example;

import org.example.controller.MerkleTreeController;
import org.example.model.ProofNode;
import org.example.service.MerkleTreeService;

import java.util.List;

public class Main {

    private static final MerkleTreeService TREE_SERVICE = new MerkleTreeService();

    public static void main(String[] args) {
        MerkleTreeController controller = new MerkleTreeController(TREE_SERVICE);
        controller.start();
    }

    public static List<List<byte[]>> buildMerkleTree(List<String> items) {
        return TREE_SERVICE.buildMerkleTree(items);
    }

    public static List<ProofNode> getMerkleProof(List<List<byte[]>> tree, int leafIndex) {
        return TREE_SERVICE.getMerkleProof(tree, leafIndex);
    }

    public static boolean verifyProof(String item, List<ProofNode> proof, String expectedRootHex) {
        return TREE_SERVICE.verifyProof(item, proof, expectedRootHex);
    }

    public static boolean isValidTransaction(String value) {
        return TREE_SERVICE.isValidTransaction(value);
    }

    public static boolean verifyOddCountHandling() {
        return TREE_SERVICE.verifyOddCountHandling();
    }
}
