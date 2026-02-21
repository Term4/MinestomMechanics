package io.github.term4.minestommechanics.util;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerStopSprintingEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

// Subject to be moved later. Here now because I don't see where else to put it (maybe a tracker class? Player class?)

public final class SprintTracker {

    // Packet approach for tracking a players sprinting status with a buffer to offset ping

    private static final Tag<Long> LAST_SPRINT_TICK = Tag.Transient("mm:last-sprint-tick");

    public SprintTracker() { }

    /** Called when a player stops sprinting */
    public void markStopSprint(Player player) {
        player.setTag(LAST_SPRINT_TICK, TickClock.now());
    }

    /** Listener node that updates LAST_SPRINT_TICK. */ // Installed in the combat node
    public EventNode<PlayerEvent> node() {
        EventNode<PlayerEvent> node = EventNode.type("mm:sprint-tracker", EventFilter.PLAYER);

        node.addListener(PlayerStopSprintingEvent.class, e -> {
            markStopSprint(e.getPlayer());
        });

        return node;
    }

    /** Returns true if a player was sprinting within {@code @ticks}, returns raw sprint state if tracker doesn't exist */
    public static boolean isSprinting(@Nullable SprintTracker t, Player p, long ticks) {
        Long last = p.getTag(LAST_SPRINT_TICK);
        if  (t == null || p.isSprinting()) { return p.isSprinting(); }
        if (last == null) return false;
        return (TickClock.now() - last) <= ticks;
    }

}
