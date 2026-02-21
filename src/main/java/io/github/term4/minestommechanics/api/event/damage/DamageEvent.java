package io.github.term4.minestommechanics.api.event.damage;

import io.github.term4.minestommechanics.mechanics.damage.DamageConfig;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

// TODO: JavaDocs
public final class DamageEvent implements Event {

    // This is the public facing API users can hook into to get information or change how a damage event happens

    private final Entity target;
    private final RegistryKey<DamageType> type;
    private @Nullable Entity source;
    private boolean invulnerable;
    private int remainingInvul;
    private boolean bypassInvul;
    private DamageConfig config;

    private float amount;
    private boolean cancelled;

    public DamageEvent(Entity target, RegistryKey<DamageType> type, @Nullable Entity source, Float amount, boolean  invulnerable, int remainingInvul, DamageConfig config) {
        this.target = target;
        this.type = type;
        this.source = source;
        this.amount = amount;
        this.invulnerable = invulnerable;
        this.remainingInvul = remainingInvul;
        this.config = config;
    }

    public Entity target() { return target; }
    public RegistryKey<DamageType> type() { return type; }

    public @Nullable Entity source() { return source; }
    public void source(@Nullable Entity source) { this.source = source; }

    public boolean invulnerable() { return invulnerable; }
    public int remainingInvul() { return remainingInvul; }
    public boolean bypassInvul() { return bypassInvul; }
    public void bypassInvul(boolean bypass) { this.bypassInvul = bypass; }

    public DamageConfig config() { return config; }
    public void config(DamageConfig config) {
        this.config = config;
    }

    public float amount() { return amount; }
    public void amount(float amount) { this.amount = amount; }

    public boolean cancelled() { return cancelled; }
    public void cancel() {  this.cancelled = true; }

}
