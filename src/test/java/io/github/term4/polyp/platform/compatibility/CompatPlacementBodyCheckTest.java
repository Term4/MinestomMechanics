package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Source-verified 1.8 semantics: a legacy placer's own body never blocks (Paper excludes it) and passable
 *  blocks check nobody; other bodies - and modern placers throughout - stay on the precise check. */
class CompatPlacementBodyCheckTest extends HeadlessServerTest {

    @Test
    void legacySelfNeverBlocksModernStaysPrecise() {
        FakePlayer fp = FakePlayer.connect(instance, new Pos(0.5, 65, 864.5), "BodyGate");
        OptimizedPlayer op = (OptimizedPlayer) fp.player;
        LivingEntity other = looseZombie();
        BoundingBox player = new BoundingBox(0.6, 1.8, 0.6);
        Vec centered = new Vec(0.5, 0, 0.5);
        try {
            assertTrue(CompatPlacement.placementBodyCheck(op, op, Block.OAK_STAIRS, centered, player),
                    "modern: even your own body blocks, like your client predicts");
            assertTrue(CompatPlacement.placementBodyCheck(op, op, Block.STONE, centered, player));

            op.compat().setLegacyClient(true);
            assertFalse(CompatPlacement.placementBodyCheck(op, op, Block.OAK_STAIRS, centered, player),
                    "legacy self: stairs into your own face land, as on Paper 1.8");
            assertFalse(CompatPlacement.placementBodyCheck(op, op, Block.STONE, centered, player));
            assertFalse(CompatPlacement.placementBodyCheck(op, other, Block.LADDER, centered, player),
                    "no collision box: no check for anyone");
            assertTrue(CompatPlacement.placementBodyCheck(op, other, Block.OAK_STAIRS, centered, player),
                    "other bodies stay precise");
            assertTrue(CompatPlacement.placementBodyCheck(op, other, Block.STONE, centered, player));
        } finally {
            fp.player.remove();
        }
    }
}
