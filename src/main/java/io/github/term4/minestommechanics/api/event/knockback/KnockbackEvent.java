package io.github.term4.minestommechanics.api.event.knockback;

import io.github.term4.minestommechanics.api.event.combat.AttackEvent;
import io.github.term4.minestommechanics.mechanics.knockback.KnockbackConfig;
import io.github.term4.minestommechanics.mechanics.combat.attack.AttackSnapshot;
import io.github.term4.minestommechanics.util.InvulnerabilityState;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class KnockbackEvent implements Event {

    // This is the public facing API users can hook into to get information or change how a knockback event happens

    public enum Cause {
        ATTACK_PACKET, SWING_RAYCAST, SWEEPING, PROJECTILE, EXPLOSION, DAMAGE;

        public static Cause from(AttackEvent.Cause attackCause) {
            return switch (attackCause) {
                case ATTACK_PACKET -> ATTACK_PACKET;
                case SWING_RAYCAST -> SWING_RAYCAST;
                case SWEEPING -> SWEEPING;
                case PROJECTILE -> PROJECTILE;
                case EXPLOSION -> EXPLOSION;
            };
        }
    }

    private final AttackSnapshot snapshot;
    private AttackSnapshot finalSnap;
    private Cause cause;
    private KnockbackConfig config;
    private @Nullable Vec velocity;
    private @Nullable Vec direction;
    private boolean invulnerable;
    private boolean bypassInvul;

    private boolean cancelled;

    public KnockbackEvent(AttackSnapshot snapshot, KnockbackConfig config) {
        this.snapshot = snapshot;
        this.config = config;
        this.cause = Cause.from(snapshot.cause());
        this.invulnerable = snapshot.target() != null && InvulnerabilityState.isInvulnerable(snapshot.target());
    }

    /** Original attack data (immutable) */
    public AttackSnapshot snapshot() { return snapshot; }

    /**
     * Snapshot used in knockback calculation.
     * Set via {@code event.finalSnap(event.snapshot().toBuilder().target(x).build())}
     */
    public AttackSnapshot finalSnap() {
        return finalSnap != null ? finalSnap : snapshot;
    }
    public void finalSnap(AttackSnapshot snap) { this.finalSnap = snap; }

    /** Knockback config used for calculation (mutable) */
    public KnockbackConfig config() { return config; }
    public void config(KnockbackConfig config) { this.config = config; }

    /** Knockback cause (derived from attack cause) */
    public Cause cause() { return cause; }
    public void cause(Cause c) { this.cause = c; }

    /** If set, used instead of running the calculator */
    public @Nullable Vec velocity() { return velocity; }
    public void velocity(@Nullable Vec velocity) { this.velocity = velocity; }

    /** If set, overides the computed horizontal (xz) knockback direction. */
    public @Nullable Vec direction() { return direction; }
    public void direction(@Nullable Vec direction) { this.direction = direction; }

    public boolean invulnerable() { return invulnerable; }
    public boolean bypassInvul() { return bypassInvul; }

    /** Deal knockback even if the target is invulnerable. */
    public void bypassInvul(boolean b) { this.bypassInvul = b; }

    public boolean cancelled() { return cancelled; }

    /** Cancel the knockback event */
    public void cancel() { this.cancelled = true; }

    // public accessors
    public Entity attacker() { return finalSnap().attacker(); }
    public @Nullable Entity target() { return finalSnap().target(); }
    public boolean sprint() { return finalSnap().sprint(); }
    public ItemStack item() { return finalSnap().item(); }

}
