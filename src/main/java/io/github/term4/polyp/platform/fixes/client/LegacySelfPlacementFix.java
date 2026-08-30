package io.github.term4.polyp.platform.fixes.client;

import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.util.BlockContact;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.listener.BlockPlacementListener;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * 1.8 self-placement: 1.8 skips the placement entity check for no-collision-box blocks and guts it for stairs
 * (stale raytrace bounds); Minestom checks them all, so a LEGACY client's accepted placement desyncs. Arms
 * {@link OptimizedPlayer#setSelfPlacing} per placement - legacy placers only (a modern client refuses these
 * placements itself; server leniency would let it bury blocks in its own body), and for stairs only while the
 * placer doesn't cover the target cell's center ({@link BlockContact#blocksLegacyPlacement}). Wraps the stock
 * listener; shard worlds use {@code Shard.placementBodyCheck} instead - that check covers every body.
 */
public final class LegacySelfPlacementFix {

    private LegacySelfPlacementFix() {}

    /** Installs the stock placement listener wrapped with the 1.8 self-placement exclusion. */
    public static void install() {
        install(BlockPlacementListener::listener);
    }

    /** Installs {@code delegate} wrapped with the exclusion - the composition seam for replaced placement listeners. */
    public static void install(@NotNull BiConsumer<ClientPlayerBlockPlacementPacket, Player> delegate) {
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientPlayerBlockPlacementPacket.class,
                wrap(delegate)::accept);
    }

    /** {@code delegate} wrapped with the exclusion but NOT installed - for hosts that own the listener slot, where
     *  registration order must not matter. */
    public static @NotNull BiConsumer<ClientPlayerBlockPlacementPacket, Player> wrap(
            @NotNull BiConsumer<ClientPlayerBlockPlacementPacket, Player> delegate) {
        return (packet, player) -> wrapped(delegate, packet, player);
    }

    private static void wrapped(BiConsumer<ClientPlayerBlockPlacementPacket, Player> delegate,
                                ClientPlayerBlockPlacementPacket packet, Player player) {
        OptimizedPlayer op = player instanceof OptimizedPlayer o && excludesPlacer(packet, o) ? o : null;
        if (op != null) op.setSelfPlacing(true);
        try {
            delegate.accept(packet, player);
        } finally {
            if (op != null) op.setSelfPlacing(false);
        }
    }

    /** The null guard is required: a non-block item right-clicking a block has a {@code null} {@code Material#block()}. */
    private static boolean excludesPlacer(ClientPlayerBlockPlacementPacket packet, Player player) {
        if (!(player instanceof OptimizedPlayer op) || !op.compat().legacyClient()) return false;
        Block placing = player.getItemInHand(packet.hand()).material().block();
        if (placing == null || !BlockContact.legacyPlacementCheckExempt(placing)) return false;
        Point cell = placementCell(packet, player);
        return cell != null
                && !BlockContact.blocksLegacyPlacement(placing, player.getPosition().sub(cell), player.getBoundingBox());
    }

    /** The listener's own target resolution, reduced: a replaceable clicked block is replaced in place, else the neighbor. */
    private static @Nullable Point placementCell(ClientPlayerBlockPlacementPacket packet, Player player) {
        Instance instance = player.getInstance();
        Point clicked = packet.blockPosition();
        if (instance == null || !instance.isChunkLoaded(clicked)) return null;
        Block at = instance.getBlock(clicked);
        return at.isAir() || at.registry().isReplaceable() ? clicked : clicked.relative(packet.blockFace());
    }
}
