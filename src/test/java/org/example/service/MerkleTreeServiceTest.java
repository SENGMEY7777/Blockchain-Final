package org.example.service;

import org.example.model.ProofNode;
import org.example.utils.HashUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MerkleTreeServiceTest {

    private MerkleTreeService service;

    @BeforeEach
    void setUp() {
        service = new MerkleTreeService();
    }

    @Test
    @DisplayName("isValidTransaction should accept only digit strings")
    void testValidTransactionFormats() {
        assertTrue(service.isValidTransaction("10"));
        assertTrue(service.isValidTransaction("20"));
        assertTrue(service.isValidTransaction("100"));
        assertTrue(service.isValidTransaction("5000"));
        assertTrue(service.isValidTransaction(" 100 "));
    }

    @Test
    @DisplayName("isValidTransaction should reject non-digits, letters, symbols, or empty inputs")
    void testInvalidTransactionFormats() {
        assertFalse(service.isValidTransaction(null));
        assertFalse(service.isValidTransaction(""));
        assertFalse(service.isValidTransaction("   "));
        assertFalse(service.isValidTransaction("d"));
        assertFalse(service.isValidTransaction("10a"));
        assertFalse(service.isValidTransaction("Tx1"));
        assertFalse(service.isValidTransaction("Alice->Bob:50"));
        assertFalse(service.isValidTransaction("-10"));
        assertFalse(service.isValidTransaction("10.50"));
    }

    @Test
    @DisplayName("buildMerkleTree should build tree for 8 transactions and produce a single root")
    void testBuildTreeEvenCount() {
        List<String> txs = List.of("10", "20", "30", "40", "50", "60", "70", "80");
        List<List<byte[]>> tree = service.buildMerkleTree(txs);

        assertNotNull(tree);
        assertEquals(4, tree.size());
        assertEquals(8, tree.get(0).size());
        assertEquals(4, tree.get(1).size());
        assertEquals(2, tree.get(2).size());
        assertEquals(1, tree.get(3).size());

        String rootHex = service.getRootHex(tree);
        assertNotNull(rootHex);
        assertEquals(64, rootHex.length());
    }

    @Test
    @DisplayName("buildMerkleTree should handle odd counts by duplicating the last node at odd levels")
    void testBuildTreeOddCount() {
        List<String> txs = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            txs.add(String.valueOf(i * 10));
        }

        List<List<byte[]>> tree = service.buildMerkleTree(txs);
        assertNotNull(tree);
        assertEquals(5, tree.size());
        assertEquals(1, tree.get(tree.size() - 1).size());
        assertTrue(service.verifyOddCountHandling());
    }

    @Test
    @DisplayName("getMerkleProof and verifyProof should successfully verify all transaction leaves")
    void testProofGenerationAndVerification() {
        List<String> txs = List.of("10", "20", "30", "40", "50", "60", "70", "80");
        List<List<byte[]>> tree = service.buildMerkleTree(txs);
        String rootHex = service.getRootHex(tree);

        for (int i = 0; i < txs.size(); i++) {
            String tx = txs.get(i);
            List<ProofNode> proof = service.getMerkleProof(tree, i);

            assertEquals(3, proof.size());

            boolean valid = service.verifyProof(tx, proof, rootHex);
            assertTrue(valid, "Proof verification failed for leaf index " + i + " (" + tx + ")");
        }
    }

    @Test
    @DisplayName("Tampering with a transaction must result in a different Merkle root and failed proof")
    void testTamperDetection() {
        List<String> originalTxs = List.of("10", "20", "30", "40", "50", "60", "70", "80");
        List<List<byte[]>> originalTree = service.buildMerkleTree(originalTxs);
        String originalRootHex = service.getRootHex(originalTree);

        int targetIndex = 3;
        String originalTx = originalTxs.get(targetIndex);
        String tamperedTx = "999";

        List<String> tamperedTxs = new ArrayList<>(originalTxs);
        tamperedTxs.set(targetIndex, tamperedTx);

        List<List<byte[]>> tamperedTree = service.buildMerkleTree(tamperedTxs);
        String tamperedRootHex = treeService.getRootHex(tamperedTree);

        assertNotEquals(originalRootHex, tamperedRootHex, "Tampering with a transaction must alter the root hash");

        List<ProofNode> originalProof = service.getMerkleProof(originalTree, targetIndex);
        boolean tamperedProofResult = service.verifyProof(tamperedTx, originalProof, originalRootHex);
        assertFalse(tamperedProofResult, "Proof verification must fail when transaction data is tampered");
    }

    @Test
    @DisplayName("buildMerkleTree should reject null or empty transaction lists")
    void testEdgeCases() {
        assertThrows(IllegalArgumentException.class, () -> service.buildMerkleTree(null));
        assertThrows(IllegalArgumentException.class, () -> service.buildMerkleTree(List.of()));

        List<String> listWithNull = new ArrayList<>();
        listWithNull.add("10");
        listWithNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> service.buildMerkleTree(listWithNull));
    }

    @Test
    @DisplayName("getMerkleProof should throw IndexOutOfBoundsException for out of range index")
    void testInvalidProofIndices() {
        List<String> txs = List.of("10", "20");
        List<List<byte[]>> tree = service.buildMerkleTree(txs);

        assertThrows(IndexOutOfBoundsException.class, () -> service.getMerkleProof(tree, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> service.getMerkleProof(tree, 2));
    }
}
