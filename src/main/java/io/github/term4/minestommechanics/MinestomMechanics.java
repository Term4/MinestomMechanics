package io.github.term4.minestommechanics;

import io.github.term4.minestommechanics.mechanics.knockback.KnockbackSystem;
import io.github.term4.minestommechanics.mechanics.damage.DamageSystem;
import io.github.term4.minestommechanics.platform.client.ClientInfoService;
import io.github.term4.minestommechanics.platform.client.VersionDetector;
import io.github.term4.minestommechanics.util.SprintTracker;
import io.github.term4.minestommechanics.util.TickClock;
import io.github.term4.minestommechanics.util.VelocityEstimator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import org.jetbrains.annotations.Nullable;

public final class MinestomMechanics {

    // This is the main initialization class for the library.
    //  This class allows the user to enable / disable server level systems (version detection, maybe packet interception, nothing else for now)

    // Server level options (defaults)
    /** When enabled the server listens for player details sent from ViaVersion proxy message */
    public boolean viaProxyDetails = false; // false by default

    /** When enabled MinestomMechanics manually tracks a players sprinting status (useful for combat). Default: true */
    public boolean installSprintTracker = true;

    // might add an option for packet validation when not using a proxy? probably better to use a separate library for that though
    private final EventNode<Event> root = EventNode.all("mm:root");
    private final EventNode<Event> apiEvents = EventNode.all("mm:api-events");

    // Server level services
    private ClientInfoService clientInfo;

    // Optional registry
    private @Nullable DamageSystem damageSystem;
    private @Nullable KnockbackSystem knockbackSystem;
    private @Nullable SprintTracker sprintTracker;

    public void registerDamage(DamageSystem d) { damageSystem = d; }
    public void registerKnockback(KnockbackSystem k) { knockbackSystem = k; }
    void registerSprintTracker(SprintTracker s) { sprintTracker = s; }

    public @Nullable DamageSystem damageSystem() { return damageSystem; }
    public @Nullable KnockbackSystem knockbackSystem() { return knockbackSystem; }
    public @Nullable SprintTracker sprintTracker() { return sprintTracker; }

    private static final MinestomMechanics INSTANCE = new MinestomMechanics();
    private boolean initialized = false;

    private MinestomMechanics() {}

    public static MinestomMechanics getInstance() { return INSTANCE; }

    /** Initialize with current options (or defaults if no options specified) */
    public void initialize() {
        if (initialized) return;
        initialized = true;

        // Enable always-necessary functions
        TickClock.start();
        VelocityEstimator.install(root);

        if (installSprintTracker) {
            var tracker = new SprintTracker();
            registerSprintTracker(tracker);
            root.addChild(tracker.node());
        }

        clientInfo = new ClientInfoService();

        // Root node for all of MinestomMechanics
        MinecraftServer.getGlobalEventHandler().addChild(root);

        // Create child nodes
        EventNode<Event> detectors = EventNode.all("mm:detectors");

        // Add child nodes to root
        root.addChild(detectors);
        root.addChild(apiEvents);

        root.addListener(PlayerDisconnectEvent.class, e -> clientInfo.remove(e.getPlayer()));

        if (viaProxyDetails) detectors.addChild(VersionDetector.node(clientInfo));
    }

    /** Access client info (e.g. protocol version) from server level detectors */
    public ClientInfoService clientInfo() {
        if (!initialized) throw new IllegalStateException("MinestomMechanics has not been initialized");
        return clientInfo;
    }

    /** Public node for MinestomMechanics API events */
    public EventNode<Event> events() {
        if (!initialized) throw new IllegalStateException("MinestomMechanics has not been initialized");
        return apiEvents;
    }

    /** Public method to install a node to the root MinestomMechanics node */
    public void install(EventNode<Event> node) {
        if  (!initialized) throw new IllegalStateException("MinestomMechanics has not been initialized");
        root.addChild(node);
    }

    public boolean isInitialized() {
        return initialized;
    }
}
