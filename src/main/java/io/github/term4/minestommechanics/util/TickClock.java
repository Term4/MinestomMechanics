package io.github.term4.minestommechanics.util;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tick based timing for MinestomMechanics
 * Synchronizes all events with server ticks.
 */
public final class TickClock {
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static volatile long tick = 0;

    private TickClock() {}

    /** Returns the current server tick */
    public static long now() {
        return tick;
    }

    /** Starts the global tick clock */
    public static void start() {
        if (!STARTED.compareAndSet(false, true)) return;
        MinecraftServer.getSchedulerManager()
                .buildTask(() -> tick++)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

}
