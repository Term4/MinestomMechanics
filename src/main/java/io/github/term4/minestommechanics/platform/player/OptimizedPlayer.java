package io.github.term4.minestommechanics.platform.player;

import io.github.term4.echofix.EchoFixPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;

/** Player implementation that throttles position broadcasts to viewers (Spigot-style). */
public class OptimizedPlayer extends EchoFixPlayer {

    private int positionBroadcastInterval = 1;

    public OptimizedPlayer(PlayerConnection connection, GameProfile gameProfile) {
        super(connection, gameProfile);
    }

    /**
     * How often this player's position is broadcast to viewers.
     * 1 = every tick (default Minestom), 2 = every other tick (Spigot).
     */
    public void setPositionBroadcastInterval(int interval) {
        if (interval < 1) throw new IllegalArgumentException("interval must be >= 1");
        this.positionBroadcastInterval = interval;
    }

    public int getPositionBroadcastInterval() {
        return positionBroadcastInterval;
    }

    @Override
    public void refreshPosition(Pos newPosition, boolean ignoreView, boolean sendPackets) {
        if (sendPackets && positionBroadcastInterval > 1) {
            if (getAliveTicks() % positionBroadcastInterval != 0) {
                sendPackets = false;
            }
        }
        // Technically api internal but it works. Bite me.
        super.refreshPosition(newPosition, ignoreView, sendPackets);
    }
}
