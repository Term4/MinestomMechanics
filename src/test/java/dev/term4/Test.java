package dev.term4;

import io.github.term4.minestommechanics.MinestomMechanics;
import io.github.term4.minestommechanics.mechanics.combat.Combat;
import io.github.term4.minestommechanics.mechanics.damage.DamageConfig;
import io.github.term4.minestommechanics.mechanics.damage.DamageSystem;
import io.github.term4.minestommechanics.mechanics.knockback.KnockbackSystem;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;

public class Test {
    static void main() {
        // Could wrap these in compatibility methods (compat.applySystemProperties(mode: 1.7, 1.8, etc)
        // Unsure what this does but eh
        System.setProperty("minestom.new-socket-write-lock", "true");

        // Disable interaction range enforcement (mechanics lib handles reach)
        System.setProperty( "minestom.enforce-entity-interaction-range", "false");

        // Set up required flags for legacy players (prevents visual bugs)
        System.setProperty("minestom.chunk-view-distance", "12"); // less than 12 causes players to disappear at ~150 block from spawn

        // Set server TPS (default is 20, library should work with any TPS tested up to 1000)
        System.setProperty("minestom.tps", "20");

        // Initialize the server
        MinecraftServer server = MinecraftServer.init(new Auth.Bungee());

        MinecraftServer.getCommandManager().register(new TeleportCommand());

        // Enable ViaVersion proxy details
        MinestomMechanics mm = MinestomMechanics.getInstance();
        mm.viaProxyDetails = true;
        mm.initialize();

        // 1. Initialize knockback system
        KnockbackSystem.install(mm, MinemenConfig.minemen());

        // 2. Initialize damage system
        DamageSystem.install(mm, new DamageConfig());

        // 3. Initialize combat system
        Combat.install(mm, new Combat.Config());

        // Create the instance(world)
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        // Generate the world
        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));

        // Add lighting
        Instance instance = instanceManager.getInstance(instanceContainer.getUuid());
        instance.setChunkSupplier(LightingChunk::new);

        var scheduler = MinecraftServer.getSchedulerManager();

        // Add an event handler to handle player spawning
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));

            // Example of how to get a players protocol on login
            final int maxRuns = 3;
            final int[] runs = {0};

            scheduler.scheduleTask(() -> {
                if (!player.isOnline()) return TaskSchedule.stop();

                int protocol = mm.clientInfo().getProtocol(player);
                System.out.println(player.getUsername() + " protocol " + protocol);

                return (++runs[0] >= maxRuns) ? TaskSchedule.stop() : TaskSchedule.tick(20);
            }, TaskSchedule.tick(20));
        });

        server.start("0.0.0.0", 25566);
    }
}
