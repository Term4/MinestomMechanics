package io.github.term4.minestommechanics.mechanics.attack;

import io.github.term4.minestommechanics.MinestomMechanics;
import io.github.term4.minestommechanics.api.event.AttackEvent;
import io.github.term4.minestommechanics.mechanics.attack.hitdetection.PacketHit;
import io.github.term4.minestommechanics.mechanics.attack.rulesets.AttackProcessor;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

public final class AttackSystem {

    private final AttackConfig config;
    private final EventNode<@NotNull Event> apiEvents;
    private final EventNode<@NotNull Event> node;


    public AttackSystem(MinestomMechanics mm, AttackConfig config) {
        this.config = config;
        this.apiEvents = mm.events();
        this.node = EventNode.all("mm:attack");

        // Replace with a switch? Some sort of method that accounts for multiple detectors with multiple modes
        if (config.enabled && config.packetHits) {
            var services = mm.services();
            var apiEvents = mm.events();

            PacketHit.install(node, config, snap -> {
                AttackEvent api = new AttackEvent(snap, services);
                apiEvents.call(api);
                if (api.cancelled() || !api.process() || (api.invulnerable() && !api.bypassInvul())) return;
                AttackProcessor proc = api.processor() != null ? api.processor().create(services) : config.ruleset.create(services);
                proc.processAttack(api.finalSnap());
            });
        }
    }

    public static AttackSystem install(MinestomMechanics mm, AttackConfig config) {
        var system = new AttackSystem(mm, config);
        mm.registerAttack(system);
        mm.install(system.node);
        return system;
    }

    public AttackConfig config() { return config; }
    public EventNode<@NotNull Event> node() { return node; }
}
