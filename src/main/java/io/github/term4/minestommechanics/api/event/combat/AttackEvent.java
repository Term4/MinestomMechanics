package io.github.term4.minestommechanics.api.event.combat;

import io.github.term4.minestommechanics.mechanics.combat.attack.rulesets.AttackProcessor;
import io.github.term4.minestommechanics.mechanics.combat.attack.AttackSnapshot;
import io.github.term4.minestommechanics.util.InvulnerabilityState;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;


/** Fired when a hit is detected. */ // Future plans to allow mobs / other entities to fire this event, or for users to manually fire it.
public final class AttackEvent implements Event {

    // This is the public facing API users can hook into to get information or change how an attack event happens

    // Was this attack "real" or emulated? Was this a projectile?
    public enum Cause { ATTACK_PACKET, SWING_RAYCAST, PROJECTILE, SWEEPING, EXPLOSION } // Probably add something here for mob / non player attacks

    private final AttackSnapshot snapshot;
    private AttackSnapshot finalSnap;

    private boolean process = true; // process this attack, true by default (probs update from a boolean for which processor to use)
    private @Nullable AttackProcessor.Ruleset ruleset; // override for which attack processor to use for this attack

    private boolean invulnerable;
    private boolean bypassInvul;

    private boolean cancelled;

    public AttackEvent(AttackSnapshot snapshot) {
        this.snapshot = snapshot;
        this.invulnerable = snapshot.target() != null && InvulnerabilityState.isInvulnerable(snapshot.target());
    }

    /** Original attack data (immutable) */
    public AttackSnapshot snapshot() { return snapshot; }

    /**
     * Snapshot that will be processed
     * Set via {@code event.finalSnap(event.snapshot().toBuilder().target(x).build())}
     */
    public AttackSnapshot finalSnap() {
        return finalSnap != null ? finalSnap : snapshot;
    }

    public void finalSnap(AttackSnapshot snap) { this.finalSnap = snap; }

    /** Whether to process the attack. False = observe / log only, True = process to attack pipeline. */
    public boolean process() { return process; }
    public void process(boolean process) { this.process = process; }

    /** Override which attack processor (ruleset) to use (null uses default processor) */
    public @Nullable AttackProcessor.Ruleset processor() { return ruleset; }
    public void processor(AttackProcessor.Ruleset ruleset) { this.ruleset = ruleset; }

    public boolean invulnerable() { return invulnerable; }
    public boolean bypassInvul() { return bypassInvul; }
    public void bypassInvul(boolean b) { this.bypassInvul = b; }

    public boolean cancelled() { return cancelled; }

    /** Cancel the attack event */
    public void cancel() { this.cancelled = true; }

    // public accessors
    public Entity attacker() { return finalSnap().attacker(); }
    public Cause cause() { return finalSnap().cause(); }
    public @Nullable Entity target() { return finalSnap().target(); }
    public boolean sprint() { return finalSnap().sprint(); }
    public ItemStack item() { return finalSnap().item(); }

}
