package io.github.term4.minestommechanics.mechanics.attack;

import io.github.term4.minestommechanics.MinestomMechanics;
import io.github.term4.minestommechanics.api.event.AttackEvent;
import io.github.term4.minestommechanics.mechanics.Cause;
import io.github.term4.minestommechanics.mechanics.attack.hitdetection.PacketHit;
import io.github.term4.minestommechanics.mechanics.attack.rulesets.AttackProcessor;
import io.github.term4.minestommechanics.util.TickClock;
import io.github.term4.minestommechanics.util.TickState;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDespawnEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public final class AttackSystem {

    private static final Tag<TickState> INVUL_ATTACK = Tag.Transient("mm:invul-attack");

    private final MinestomMechanics mm;
    private final AttackConfig config;
    private final EventNode<@NotNull Event> apiEvents;
    private final EventNode<@NotNull Event> node;

    /** Queue of attacks keyed by target. Processed when target's attack invul expires. */
    private final Map<LivingEntity, Queue<AttackSnapshot>> hitQueue = new ConcurrentHashMap<>();

    public AttackSystem(MinestomMechanics mm, AttackConfig config) {
        this.mm = mm;
        this.config = config;
        this.apiEvents = mm.events();
        this.node = EventNode.all("mm:attack");

        var services = mm.services();
        var resolvedStatic = AttackConfigResolver.resolve(config, AttackConfigResolver.AttackContext.of(
                new AttackSnapshot(null, null, Cause.ATTACK_PACKET, config), services));

        if (resolvedStatic.enabled() && resolvedStatic.packetHits()) {
            PacketHit.install(node, config, snap -> handleAttack(snap, services));
        }

        node.addListener(EntityDespawnEvent.class, e -> {
            if (e.getEntity() instanceof LivingEntity le) hitQueue.remove(le);
        });

        MinecraftServer.getSchedulerManager()
                .buildTask(this::tickQueue)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    private void handleAttack(AttackSnapshot snap, io.github.term4.minestommechanics.Services services) {
        AttackEvent api = new AttackEvent(snap, services);
        apiEvents.call(api);

        if (api.cancelled() || !api.process()) return;

        Entity target = snap.target();
        if (api.invulnerable() && !api.bypassInvul()) {
            if (target instanceof LivingEntity le) {
                var resolved = api.resolvedConfig();
                int buffer = resolved.hitQueueBuffer();
                if (buffer > 0 && remainingAttackInvulTicks(le) <= buffer) {
                    hitQueue.computeIfAbsent(le, k -> new ArrayDeque<>()).add(snap);
                    return;
                }
            }
            return;
        }

        processAttack(snap, services, api);
    }

    private void processAttack(AttackSnapshot snap, io.github.term4.minestommechanics.Services services, AttackEvent api) {
        AttackProcessor proc = api.processor() != null
                ? api.processor().create(services)
                : api.resolvedConfig().ruleset().create(services);
        proc.processAttack(api.finalSnap());
        Entity target = api.finalSnap().target();
        if (target != null) {
            int duration = api.resolvedConfig().atkInvulnTicks();
            if (duration > 0) setAttackInvulnerable(target, duration);
        }
    }

    private void tickQueue() {
        var services = mm.services();
        for (Map.Entry<LivingEntity, Queue<AttackSnapshot>> entry : hitQueue.entrySet()) {
            LivingEntity target = entry.getKey();
            if (target.isRemoved()) {
                hitQueue.remove(target);
                continue;
            }
            if (!isInvulnerableToAttack(target)) {
                Queue<AttackSnapshot> q = entry.getValue();
                AttackSnapshot snap = q.poll();
                if (snap != null) {
                    if (q.isEmpty()) hitQueue.remove(target);
                    AttackEvent api = new AttackEvent(snap, services);
                    apiEvents.call(api);
                    if (!api.cancelled() && api.process() && !api.invulnerable()) {
                        processAttack(api.finalSnap(), services, api);
                    }
                }
            }
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

    public static void setAttackInvulnerable(Entity e, int duration) {
        if (!(e instanceof LivingEntity le) || duration <= 0) return;
        le.setTag(INVUL_ATTACK, new TickState(TickClock.now(), duration));
    }

    public static boolean isInvulnerableToAttack(Entity e) {
        if (!(e instanceof LivingEntity le)) return false;
        TickState s = le.getTag(INVUL_ATTACK);
        return s != null && s.isActive();
    }

    /** Remaining attack invul ticks for the entity. 0 if not invulnerable. */
    public static int remainingAttackInvulTicks(Entity e) {
        if (!(e instanceof LivingEntity le)) return 0;
        TickState s = le.getTag(INVUL_ATTACK);
        return s != null ? s.remainingTicks() : 0;
    }
}
