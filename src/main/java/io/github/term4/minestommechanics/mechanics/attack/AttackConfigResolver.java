package io.github.term4.minestommechanics.mechanics.attack;

import io.github.term4.minestommechanics.Services;
import io.github.term4.minestommechanics.platform.Constants;
import io.github.term4.minestommechanics.mechanics.attack.rulesets.AttackProcessor;
import org.jetbrains.annotations.Nullable;

/** Resolves AttackConfig with context into plain values. */
public final class AttackConfigResolver {

    private AttackConfigResolver() {}

    public record AttackContext(AttackSnapshot snap, Services services) {
        public static AttackContext of(AttackSnapshot snap, Services services) {
            return new AttackContext(snap, services);
        }
    }

    public static ResolvedAttackConfig resolve(AttackConfig config, AttackContext ctx) {
        if (config == null) return ResolvedAttackConfig.defaults();

        AttackConfig cfg = config;
        if (cfg.subConfig != null) {
            AttackConfig sub = cfg.subConfig.apply(ctx);
            if (sub != null) cfg = sub.fromBase(cfg);
        }

        Integer atkInvulVal = resolve(cfg.atkInvulnTicks, ctx);
        if (atkInvulVal == null && ctx.services().damage() != null) {
            atkInvulVal = ctx.services().damage().config().invulTicks;
        }
        if (atkInvulVal == null) {
            atkInvulVal = Constants.DEFAULT_INVUL_TICKS;
        }

        Integer hitQueueVal = resolve(cfg.hitQueueBuffer, ctx);
        if (hitQueueVal == null) hitQueueVal = 0;

        Boolean enabledVal = resolve(cfg.enabled, ctx);
        Boolean packetHitsVal = resolve(cfg.packetHits, ctx);
        Boolean swingHitsVal = resolve(cfg.swingHits, ctx);
        Double packetReachVal = resolve(cfg.packetReach, ctx);
        Double swingReachVal = resolve(cfg.swingReach, ctx);
        Double packetPaddingVal = resolve(cfg.packetPadding, ctx);
        Double swingPaddingVal = resolve(cfg.swingPadding, ctx);
        AttackProcessor.Ruleset rulesetVal = resolve(cfg.ruleset, ctx);

        return new ResolvedAttackConfig(
                enabledVal != null ? enabledVal : true,
                resolve(cfg.idleTimeout, ctx),
                atkInvulVal,
                resolve(cfg.sprintBuffer, ctx),
                hitQueueVal,
                packetHitsVal != null ? packetHitsVal : true,
                swingHitsVal != null ? swingHitsVal : false,
                packetReachVal != null ? packetReachVal : 10.0,
                swingReachVal != null ? swingReachVal : 3.0,
                packetPaddingVal != null ? packetPaddingVal : 2.0,
                swingPaddingVal != null ? swingPaddingVal : 0.0,
                rulesetVal != null ? rulesetVal : AttackProcessor.legacy()
        );
    }

    private static <T> T resolve(@Nullable AttackConfig.FieldValue<T> fv, AttackContext ctx) {
        return fv != null ? fv.resolve(ctx) : null;
    }

    /** Resolved config with plain values. Used by AttackEvent and AttackSystem. */
    public record ResolvedAttackConfig(
            boolean enabled,
            @Nullable Integer idleTimeout,
            int atkInvulnTicks,
            @Nullable Integer sprintBuffer,
            int hitQueueBuffer,
            boolean packetHits,
            boolean swingHits,
            double packetReach,
            double swingReach,
            double packetPadding,
            double swingPadding,
            @Nullable AttackProcessor.Ruleset ruleset
    ) {
        public static ResolvedAttackConfig defaults() {
            return new ResolvedAttackConfig(
                    true,
                    null,
                    Constants.DEFAULT_INVUL_TICKS,
                    null,
                    0,
                    true,
                    false,
                    10.0,
                    3.0,
                    2.0,
                    0.0,
                    AttackProcessor.legacy()
            );
        }
    }
}
