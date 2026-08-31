package io.github.term4.polyp.world;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.fx.FxRegistry;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlaceSoundPositionTest extends HeadlessServerTest {

    private MechanicsProfile previous;

    @BeforeEach
    void keepScope() { previous = Polyp.getInstance().profiles().global(); }

    // restore, never null: other classes resolve their configs off whatever global is already installed
    @AfterEach
    void restoreScope() { Polyp.getInstance().profiles().setGlobal(previous); }

    /** Vanilla's ItemBlock/BlockItem sounds the block CENTER; client-side-sound mods dedup against it. */
    @Test
    void placeSoundsAtTheBlockCenter() {
        AtomicReference<Point> at = new AtomicReference<>();
        Polyp.getInstance().profiles().setGlobal(MechanicsProfile.builder()
                .set(MechanicsKeys.FX, FxRegistry.empty().register(Fx.BLOCK_PLACE, ctx -> at.set(ctx.position())))
                .build());
        FakePlayer placer = FakePlayer.connect(instance, new Pos(0.5, 41, 0.5), "Placer");
        try {
            EventDispatcher.call(new PlayerBlockPlaceEvent(placer.player, instance, Block.STONE,
                    BlockFace.TOP, new Pos(-4, 40, 10).asBlockVec(), new Pos(0.5, 1, 0.5), PlayerHand.MAIN));

            assertNotNull(at.get(), "the place emitter ran");
            assertEquals(-3.5, at.get().x(), 1e-9);
            assertEquals(40.5, at.get().y(), 1e-9);
            assertEquals(10.5, at.get().z(), 1e-9);
        } finally {
            placer.player.remove();
        }
    }
}
