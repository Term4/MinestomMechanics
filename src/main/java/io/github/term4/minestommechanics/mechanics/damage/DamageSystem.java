package io.github.term4.minestommechanics.mechanics.damage;

import io.github.term4.minestommechanics.MinestomMechanics;
import io.github.term4.minestommechanics.api.event.DamageEvent;
import io.github.term4.minestommechanics.util.TickClock;
import io.github.term4.minestommechanics.util.TickState;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// This class is the main damage system. Applies damage, fires API, determines damage type, amount, etc.
public final class DamageSystem {

    private static final Tag<TickState> INVUL_DAMAGE = Tag.Transient("mm:invul-damage");

    public static final float DEFAULT_AMOUNT = 1.0f;
    private final EventNode<@NotNull Event> apiEvents;
    private final DamageConfig config;

    public DamageSystem(MinestomMechanics mm, DamageConfig config) {
        this.apiEvents = mm.events();
        this.config = config;
    }

    public void apply(DamageRequest req) {
        Entity target = req.target();
        if (!(target instanceof LivingEntity living)) return;

        float amount = req.amount() != null ? req.amount() : DEFAULT_AMOUNT;

        Entity source = req.source(); // source = projectile / explosion / etc
        Entity attacker = req.attacker(); // attacker = entity responsible (mob, player)

        Point sourcePos = req.sourcePosition();

        boolean invulnerable = isInvulnerableToDamage(living);
        int remaining = invulnerable ? remainingDamageInvulTicks(living) : 0;

        // API
        DamageEvent event = new DamageEvent(target, req.type(), req.source(), amount, invulnerable, remaining, config);
        apiEvents.call(event);
        if (event.cancelled() || (event.invulnerable() && !event.bypassInvul())) return;

        amount = event.amount();
        if (amount <= 0) return;

        // Build damage
        Damage damage = new Damage(
                req.type(),
                source,
                attacker,
                sourcePos,
                amount
        );

        living.damage(damage);
        setDamageInvulnerable(living, event.config().invulTicks);
    }

    public DamageConfig config() { return config; }

    public static DamageSystem install(MinestomMechanics mm, DamageConfig cfg) {
        var system = new DamageSystem(mm, cfg);
        mm.registerDamage(system);
        return system;
    }

    public static void setDamageInvulnerable(Entity e, int duration) {
        if (!(e instanceof LivingEntity le) || duration <= 0) return;
        le.setTag(INVUL_DAMAGE, new TickState(TickClock.now(), duration));
    }

    public static boolean isInvulnerableToDamage(Entity e) {
        if (!(e instanceof LivingEntity le)) return false;
        TickState s = getDamageInvul(le);
        return s != null && s.isActive();
    }

    public static int remainingDamageInvulTicks(LivingEntity le) {
        TickState s = getDamageInvul(le);
        return s != null ? s.remainingTicks() : 0;
    }

    private static @Nullable TickState getDamageInvul(LivingEntity le) {
        return le.getTag(INVUL_DAMAGE);
    }
}
