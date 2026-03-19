package io.github.term4.minestommechanics.util;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks when entities were last in the air. Used for "on ground in past N ticks" predicates.
 */
public final class GroundTracker {

    public static final Tag<TickState> LAST_AIRBORNE_STATE = Tag.Transient("mm:last-airborne-state");
    /** Tick when entity was last on ground. Used to compute ticks-in-air for gravity-predicted velocity. */
    public static final Tag<Long> LAST_GROUND_TICK = Tag.Transient("mm:last-ground-tick");

    public GroundTracker() {}

    /**
     * Starts the ground tracker. Runs every tick to update lastAirborneState and lastGroundTick.
     */
    public void start() {
        MinecraftServer.getSchedulerManager()
                .buildTask(this::tick)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    private void tick() {
        long now = TickClock.now();
        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (p.isOnGround()) {
                p.setTag(LAST_GROUND_TICK, now);
            } else {
                p.setTag(LAST_AIRBORNE_STATE, new TickState(now, 0));
            }
        }
    }

    /**
     * Ticks since entity last touched ground. 0 when on ground (we update LAST_GROUND_TICK every tick)
     * or no history. Intentionally does NOT check isOnGround()—it can be 1 tick late from the client,
     * which would wrongly zero out gravity dampening at the moment of air hits.
     */
    public static int getTicksInAir(Entity entity) {
        if (entity == null) return 0;
        Long last = entity.getTag(LAST_GROUND_TICK);
        if (last == null) return 0;
        return (int) Math.max(0, TickClock.now() - last);
    }

    /**
     * Returns true if the entity was on ground for the entire past N ticks.
     * When in air, we set lastAirborneState = now. So "on ground in past N" = last airborne was more than N ticks ago.
     */
    public static boolean isOnGround(@Nullable GroundTracker tracker, Entity entity, int ticks) {
        if (!(entity instanceof LivingEntity le)) return false;
        if (le.isOnGround()) {
            TickState state = entity.getTag(LAST_AIRBORNE_STATE);
            if (state == null) return true;
            return state.isStaleAfter(ticks);
        }
        return false;
    }

    /**
     * Returns true if the entity is falling and will intersect a solid block within the next tick.
     * Used for knockback friction tier logic instead of isOnGround (which arrives 1 tick late from client).
     * Fallback: if instance is null or block lookup fails, returns entity.isOnGround().
     */
    public static boolean predictsLandingSoon(Entity entity) {
        if (entity == null) return false;
        Instance instance = entity.getInstance();
        if (instance == null) return entity.isOnGround();

        Vec vel = entity.getVelocity();
        if (vel.y() >= 0) return entity.isOnGround();

        double nextY = entity.getPosition().y() + vel.y();
        int bx = (int) Math.floor(entity.getPosition().x());
        int bz = (int) Math.floor(entity.getPosition().z());
        int by = (int) Math.floor(nextY) - 1;

        Block block = instance.getBlock(bx, by, bz);
        return block != null && !block.isAir() && block.isSolid();
    }
}
