package io.github.term4.minestommechanics.mechanics.knockback;

import io.github.term4.minestommechanics.MinestomMechanics;
import io.github.term4.minestommechanics.Services;
import io.github.term4.minestommechanics.api.event.KnockbackEvent;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Knockback system: Config can be changed via KnockbackConfig or the KnockbackEvent API */
public final class KnockbackSystem {

    private final Services services;
    private final EventNode<@NotNull Event> apiEvents;
    private final KnockbackConfig config;
    private final KnockbackCalculator calc;

    public KnockbackSystem(MinestomMechanics mm, KnockbackConfig config) {
        this.config = config;
        this.apiEvents = mm.events();
        this.services = mm.services();
        this.calc = new KnockbackCalculator(services);
    }

    public void apply(KnockbackSnapshot snap) {

        // KnockbackEvent API
        var event = new KnockbackEvent(snap);
        apiEvents.call(event);
        if (event.cancelled()) return;
        KnockbackSnapshot finalSnap = event.finalSnap();

        // Knockback requires a target
        if (finalSnap.target() == null) return;

        // Early return if target is invulnerable TODO: Update to use KNOCKBACK invul specifically, not general
        if (event.invulnerable() && !event.bypassInvul()) return;

        // Build knockback velocity vector
        @Nullable Vec velocity;
        if (event.velocity() != null) { velocity = event.velocity(); }
        else {
            velocity = finalSnap.config() != null ? calc.compute(finalSnap) : calc.compute(finalSnap.withConfig(config));
            if (velocity != null && event.direction() != null) {
                double mag = Math.sqrt(velocity.x() * velocity.x() + velocity.z() * velocity.z());
                Vec dir = event.direction().normalize();
                velocity = new Vec(dir.x() * mag, velocity.y(), dir.z() * mag);
            }
        }

        // Apply knockback to target
        if (velocity != null) finalSnap.target().setVelocity(velocity);
    }

    public KnockbackConfig config() { return config; }

    public static KnockbackSystem install(MinestomMechanics mm, KnockbackConfig config) {
        var system = new KnockbackSystem(mm, config);
        mm.registerKnockback(system);
        return system;
    }

}