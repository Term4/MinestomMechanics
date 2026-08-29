package io.github.term4.polyp.util;

import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The 1.8 no-entity-check set: no collision box, plus stairs (stale raytrace bounds gut the 1.8 check). */
class LegacyPlacementExemptTest {

    @Test
    void passableAndStairsAreExempt() {
        assertTrue(BlockContact.legacyPlacementCheckExempt(Block.LADDER));
        assertTrue(BlockContact.legacyPlacementCheckExempt(Block.VINE));
        assertTrue(BlockContact.legacyPlacementCheckExempt(Block.COBWEB));
        assertTrue(BlockContact.legacyPlacementCheckExempt(Block.OAK_STAIRS));
        assertTrue(BlockContact.legacyPlacementCheckExempt(Block.SANDSTONE_STAIRS));
    }

    @Test
    void collidingBlocksAreNot() {
        assertFalse(BlockContact.legacyPlacementCheckExempt(Block.STONE));
        assertFalse(BlockContact.legacyPlacementCheckExempt(Block.OAK_SLAB));
        assertFalse(BlockContact.legacyPlacementCheckExempt(Block.OAK_FENCE));
    }
}
