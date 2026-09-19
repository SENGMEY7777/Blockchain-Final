package org.example.model;

import org.example.utils.HashUtils;
import java.util.Arrays;


public class ProofNode {

    private final byte[] siblingHash;
    private final char side;

    public ProofNode(byte[] siblingHash, char side) {
        if (siblingHash == null) {
            throw new IllegalArgumentException("Sibling hash cannot be null");
        }
        if (side != 'L' && side != 'R') {
            throw new IllegalArgumentException("Side must be 'L' or 'R', got: " + side);
        }
        this.siblingHash = siblingHash.clone();
        this.side = side;
    }

    public byte[] getSiblingHash() {
        return siblingHash.clone();
    }

    public char getSide() {
        return side;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProofNode that)) return false;
        return side == that.side && Arrays.equals(siblingHash, that.siblingHash);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(siblingHash);
        result = 31 * result + (int) side;
        return result;
    }

    @Override
    public String toString() {
        return "ProofNode{" +
                "side=" + side +
                ", siblingHash=" + HashUtils.bytesToHex(siblingHash) +
                '}';
    }
}

