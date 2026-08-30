package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The legacy self-place semantics follow the PLACER's client: legacy gets the 1.8 rules, modern stays precise. */
class CompatPlacementBodyCheckTest extends HeadlessServerTest {

    @Test
    void legacyPlacersGetTheClutchModernStaysPrecise() {
        FakePlayer fp = FakePlayer.connect(instance, new Pos(0.5, 65, 864.5), "BodyGate");
        OptimizedPlayer op = (OptimizedPlayer) fp.player;
        BoundingBox player = new BoundingBox(0.6, 1.8, 0.6);
        Vec edge = new Vec(-0.2, 0, 0.5); // clips 0.1 into the cell
        Vec centered = new Vec(0.5, 0, 0.5);
        try {
            assertTrue(CompatPlacement.placementBodyCheck(op, Block.OAK_STAIRS, edge, player),
                    "modern: the precise shape cares about the clip");
            assertTrue(CompatPlacement.placementBodyCheck(op, Block.OAK_STAIRS, centered, player));

            op.compat().setLegacyClient(true);
            assertFalse(CompatPlacement.placementBodyCheck(op, Block.OAK_STAIRS, edge, player),
                    "legacy: the clutch placement passes");
            assertTrue(CompatPlacement.placementBodyCheck(op, Block.OAK_STAIRS, centered, player),
                    "legacy: covering the center still refuses");
            assertFalse(CompatPlacement.placementBodyCheck(op, Block.LADDER, centered, player));
        } finally {
            fp.player.remove();
        }
    }
}
