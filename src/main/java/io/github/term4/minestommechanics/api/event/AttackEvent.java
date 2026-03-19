package io.github.term4.minestommechanics.api.event;

import io.github.term4.minestommechanics.Services;
import io.github.term4.minestommechanics.mechanics.attack.AttackConfigResolver;
import io.github.term4.minestommechanics.mechanics.attack.AttackSystem;
import io.github.term4.minestommechanics.mechanics.Cause;
import io.github.term4.minestommechanics.mechanics.attack.AttackSnapshot;
import io.github.term4.minestommechanics.mechanics.attack.rulesets.AttackProcessor;
import io.github.term4.minestommechanics.util.SprintTracker;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// This is the public facing API users can hook into to get information or change how an attack event happens
/** Fired when a hit is detected. */ // Future plans to allow mobs / other entities to fire this event, or for users to manually fire it.
public final class AttackEvent implements Event {

    private final AttackSnapshot snapshot;
    private AttackSnapshot finalSnap;

    private boolean process = true; // process this attack, true by default (probs update from a boolean for which processor to use)
    private @Nullable AttackProcessor.Ruleset ruleset; // override for which attack processor to use for this attack

    private Boolean overrideSprint;
    private ItemStack overrideItem;

    private final boolean invulnerable;
    private boolean bypassInvul;

    private boolean cancelled;

    private final Services services;
    private final AttackConfigResolver.ResolvedAttackConfig resolvedConfig;

    public AttackEvent(AttackSnapshot snapshot, Services services) {
        this.snapshot = snapshot;
        this.services = services;
        this.invulnerable = snapshot.target() != null && AttackSystem.isInvulnerableToAttack(snapshot.target());
        this.resolvedConfig = snapshot.config() != null
                ? AttackConfigResolver.resolve(snapshot.config(), AttackConfigResolver.AttackContext.of(snapshot, services))
                : AttackConfigResolver.ResolvedAttackConfig.defaults();
    }

    // Setters
    public void finalSnap(AttackSnapshot snap) { this.finalSnap = snap; }
    public void process(boolean process) { this.process = process; }
    public void processor(AttackProcessor.Ruleset ruleset) { this.ruleset = ruleset; }
    public void overrideSprint(@Nullable Boolean b) { this.overrideSprint = b; }
    public void overrideItem(@Nullable ItemStack item) { this.overrideItem = item; }
    public void bypassInvul(boolean b) { this.bypassInvul = b; }
    public void cancel() { this.cancelled = true; }

    // Getters
    public AttackSnapshot snapshot() { return snapshot; }
    public AttackSnapshot finalSnap() { return finalSnap != null ? finalSnap : snapshot; }
    public Entity attacker() { return finalSnap().attacker(); }
    public @Nullable Entity target() { return finalSnap().target(); }
    public Cause cause() { return finalSnap().cause(); }
    public boolean process() { return process; }
    public @Nullable AttackProcessor.Ruleset processor() { return ruleset; }
    public boolean invulnerable() { return invulnerable; }
    public boolean bypassInvul() { return bypassInvul; }
    public @Nullable Boolean overrideSprint() { return overrideSprint; }
    public @Nullable ItemStack overrideItem() { return overrideItem; }
    public boolean sprint() {
        if (overrideSprint != null) return overrideSprint;
        if (!(attacker() instanceof Player p) || services.sprintTracker() == null) { return attacker().isSprinting(); }
        int buffer = resolvedConfig.sprintBuffer() != null ? resolvedConfig.sprintBuffer() : 0;
        return SprintTracker.wasRecentlySprinting(services.sprintTracker(), p, buffer);
    }
    public @Nullable ItemStack item() {
        if (overrideItem != null) return overrideItem;
        return attacker() instanceof LivingEntity le ? le.getItemInMainHand() : ItemStack.AIR;
    }

    public boolean cancelled() { return cancelled; }

    /** Resolved attack config for this event. */
    public AttackConfigResolver.ResolvedAttackConfig resolvedConfig() { return resolvedConfig; }

}
