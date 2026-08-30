package io.github.term4.polyp.util;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The 1.8 gutted-check set (no collision box, plus stairs) and what remains of the stairs check: the cell center. */
class LegacyPlacementExemptTest {

    private static final BoundingBox PLAYER = new BoundingBox(0.6, 1.8, 0.6);

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

    @Test
    void stairsBlockOnlyWhenTheBodyCoversTheCellCenter() {
        assertTrue(BlockContact.blocksLegacyPlacement(Block.OAK_STAIRS, new Vec(0.5, 0, 0.5), PLAYER),
                "standing centered in the cell");
        assertFalse(BlockContact.blocksLegacyPlacement(Block.OAK_STAIRS, new Vec(-0.2, 0, 0.5), PLAYER),
                "clipping 0.1 into the cell edge - the clutch placement");
        assertFalse(BlockContact.blocksLegacyPlacement(Block.OAK_STAIRS, new Vec(0.5, 0.6, 0.5), PLAYER),
                "feet above the center height");
    }

    @Test
    void passableNeverBlocksAndSolidsKeepTheirRealShape() {
        assertFalse(BlockContact.blocksLegacyPlacement(Block.LADDER, new Vec(0.5, 0, 0.5), PLAYER));
        assertTrue(BlockContact.blocksLegacyPlacement(Block.STONE, new Vec(0.5, 0, 0.5), PLAYER));
        assertTrue(BlockContact.blocksLegacyPlacement(Block.STONE, new Vec(-0.2, 0, 0.5), PLAYER),
                "the full cube still cares about an edge clip");
        assertFalse(BlockContact.blocksLegacyPlacement(Block.STONE, new Vec(1.5, 0, 0.5), PLAYER));
    }
}
