package io.github.term4.minestommechanics.util;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityTeleportEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.tag.Tag;

/** Position delta velocity estimation for players. Minestom's getVelocity does not work for clients. */
public final class VelocityEstimator {

    private record Frame(Vec velocity, Pos lastPos) {}
    private static final Tag<Frame> FRAME = Tag.Transient("mm:velocity-frame");
    private static final Tag<Boolean> TELEPORTED = Tag.Transient("mm:velocity-teleported");

    private VelocityEstimator() {}

    /** Install the tick listener. */
    public static void install(EventNode<Event> node) {

        // handles rare case where velocity is unpredictable due to teleportation
        node.addListener(EntityTeleportEvent.class, e -> {
            if (e.getEntity() instanceof Player p) {
                p.setTag(TELEPORTED, true);
            }
        });

        node.addListener(PlayerTickEvent.class, e -> {
            Player player = e.getPlayer();
            Pos current = player.getPosition();
            Frame prev = player.getTag(FRAME);

            Vec velocity;

            if (Boolean.TRUE.equals(player.getTag(TELEPORTED))) {
                player.removeTag(TELEPORTED);
                velocity = Vec.ZERO;
            } else if (prev != null) {
                velocity = new Vec(current.x() - prev.lastPos().x(), current.y() - prev.lastPos().y(), current.z() - prev.lastPos().z());
            }  else { velocity = Vec.ZERO; }

            Frame frame = new Frame(velocity, current);
            player.setTag(FRAME, frame);
        });
    }

    private static Vec get(Player player) {
        Frame f = player.getTag(FRAME);
        return f != null ? f.velocity() : Vec.ZERO;
    }

    /** General velocity method for any entity (uses estimate for player) */
    public static Vec getVelocity(Entity entity) {
        return entity instanceof Player p ? get(p) : entity.getVelocity();
    }

}
