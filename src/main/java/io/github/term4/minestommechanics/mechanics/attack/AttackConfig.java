package io.github.term4.minestommechanics.mechanics.attack;

import io.github.term4.minestommechanics.mechanics.attack.rulesets.AttackProcessor;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/** Immutable attack config. Use {@link #builder()}, {@link #toBuilder()}. */
public final class AttackConfig {

    public record FieldValue<T>(Function<AttackConfigResolver.AttackContext, T> fn) {
        static <T> FieldValue<T> constant(T v) { return new FieldValue<>(ctx -> v); }
        static <T> FieldValue<T> of(Function<AttackConfigResolver.AttackContext, T> f) { return new FieldValue<>(f); }
        static <T> FieldValue<T> ofWithFallback(T fallback, Function<AttackConfigResolver.AttackContext, T> fn) {
            return new FieldValue<>(ctx -> { T r = fn.apply(ctx); return r != null ? r : fallback; });
        }
        T resolve(AttackConfigResolver.AttackContext ctx) { return fn.apply(ctx); }
        FieldValue<T> or(FieldValue<T> fallback) {
            return new FieldValue<>(ctx -> { T r = fn.apply(ctx); return r != null ? r : fallback.fn.apply(ctx); });
        }
    }

    private static <T> FieldValue<T> merge(@Nullable FieldValue<T> a, @Nullable FieldValue<T> b) {
        if (b == null) return a;
        if (a == null) return b;
        return a.or(b);
    }

    @Nullable public final Function<AttackConfigResolver.AttackContext, AttackConfig> subConfig;

    public final FieldValue<Boolean> enabled;
    public final FieldValue<Integer> idleTimeout;
    public final FieldValue<Integer> atkInvulnTicks;
    public final FieldValue<Integer> sprintBuffer;
    public final FieldValue<Integer> hitQueueBuffer;
    public final FieldValue<Boolean> packetHits;
    public final FieldValue<Boolean> swingHits;
    public final FieldValue<Double> packetReach;
    public final FieldValue<Double> swingReach;
    public final FieldValue<Double> packetPadding;
    public final FieldValue<Double> swingPadding;
    public final FieldValue<AttackProcessor.Ruleset> ruleset;

    private AttackConfig(Builder b) {
        subConfig = b.subConfig;
        enabled = b.enabled;
        idleTimeout = b.idleTimeout;
        atkInvulnTicks = b.atkInvulnTicks;
        sprintBuffer = b.sprintBuffer;
        hitQueueBuffer = b.hitQueueBuffer;
        packetHits = b.packetHits;
        swingHits = b.swingHits;
        packetReach = b.packetReach;
        swingReach = b.swingReach;
        packetPadding = b.packetPadding;
        swingPadding = b.swingPadding;
        ruleset = b.ruleset;
    }

    /** Merges this config over base. */
    public AttackConfig fromBase(AttackConfig base) {
        return new Builder()
                .subConfig(subConfig != null ? subConfig : base.subConfig)
                .enabled(merge(enabled, base.enabled))
                .idleTimeout(merge(idleTimeout, base.idleTimeout))
                .atkInvulnTicks(merge(atkInvulnTicks, base.atkInvulnTicks))
                .sprintBuffer(merge(sprintBuffer, base.sprintBuffer))
                .hitQueueBuffer(merge(hitQueueBuffer, base.hitQueueBuffer))
                .packetHits(merge(packetHits, base.packetHits))
                .swingHits(merge(swingHits, base.swingHits))
                .packetReach(merge(packetReach, base.packetReach))
                .swingReach(merge(swingReach, base.swingReach))
                .packetPadding(merge(packetPadding, base.packetPadding))
                .swingPadding(merge(swingPadding, base.swingPadding))
                .ruleset(merge(ruleset, base.ruleset))
                .build();
    }

    public Builder toBuilder() { return new Builder(this); }

    public static Builder builder() { return builder(null); }
    public static Builder builder(@Nullable AttackConfig base) { return base != null ? new Builder(base) : new Builder(); }

    /** Returns a new config with default values. */
    public static AttackConfig defaultConfig() { return builder().build(); }

    public static final class Builder {
        private Function<AttackConfigResolver.AttackContext, AttackConfig> subConfig;
        private FieldValue<Boolean> enabled;
        private FieldValue<Integer> idleTimeout;
        private FieldValue<Integer> atkInvulnTicks;
        private FieldValue<Integer> sprintBuffer;
        private FieldValue<Integer> hitQueueBuffer;
        private FieldValue<Boolean> packetHits;
        private FieldValue<Boolean> swingHits;
        private FieldValue<Double> packetReach;
        private FieldValue<Double> swingReach;
        private FieldValue<Double> packetPadding;
        private FieldValue<Double> swingPadding;
        private FieldValue<AttackProcessor.Ruleset> ruleset;

        Builder() {
            enabled = FieldValue.constant(true);
            idleTimeout = null;
            atkInvulnTicks = null;
            sprintBuffer = null;
            hitQueueBuffer = FieldValue.constant(0);
            packetHits = FieldValue.constant(true);
            swingHits = FieldValue.constant(false);
            packetReach = FieldValue.constant(10.0);
            swingReach = FieldValue.constant(3.0);
            packetPadding = FieldValue.constant(2.0);
            swingPadding = FieldValue.constant(0.0);
            ruleset = FieldValue.constant(AttackProcessor.legacy());
        }

        Builder(AttackConfig c) {
            subConfig = c.subConfig;
            enabled = c.enabled;
            idleTimeout = c.idleTimeout;
            atkInvulnTicks = c.atkInvulnTicks;
            sprintBuffer = c.sprintBuffer;
            hitQueueBuffer = c.hitQueueBuffer;
            packetHits = c.packetHits;
            swingHits = c.swingHits;
            packetReach = c.packetReach;
            swingReach = c.swingReach;
            packetPadding = c.packetPadding;
            swingPadding = c.swingPadding;
            ruleset = c.ruleset;
        }

        public Builder subConfig(Function<AttackConfigResolver.AttackContext, AttackConfig> fn) { subConfig = fn; return this; }
        public Builder enabled(Boolean v) { enabled = FieldValue.constant(v); return this; }
        public Builder enabled(Function<AttackConfigResolver.AttackContext, Boolean> fn) { enabled = FieldValue.of(fn); return this; }
        public Builder idleTimeout(Integer v) { idleTimeout = FieldValue.constant(v); return this; }
        public Builder idleTimeout(Function<AttackConfigResolver.AttackContext, Integer> fn) { idleTimeout = FieldValue.of(fn); return this; }
        public Builder atkInvulnTicks(Integer v) { atkInvulnTicks = FieldValue.constant(v); return this; }
        public Builder atkInvulnTicks(Function<AttackConfigResolver.AttackContext, Integer> fn) { atkInvulnTicks = FieldValue.of(fn); return this; }
        public Builder atkInvulnTicks(Integer fallback, Function<AttackConfigResolver.AttackContext, Integer> fn) { atkInvulnTicks = FieldValue.ofWithFallback(fallback, fn); return this; }
        public Builder sprintBuffer(Integer v) { sprintBuffer = FieldValue.constant(v); return this; }
        public Builder sprintBuffer(Function<AttackConfigResolver.AttackContext, Integer> fn) { sprintBuffer = FieldValue.of(fn); return this; }
        public Builder hitQueueBuffer(Integer v) { hitQueueBuffer = FieldValue.constant(v); return this; }
        public Builder hitQueueBuffer(Function<AttackConfigResolver.AttackContext, Integer> fn) { hitQueueBuffer = FieldValue.of(fn); return this; }
        public Builder hitQueueBuffer(Integer fallback, Function<AttackConfigResolver.AttackContext, Integer> fn) { hitQueueBuffer = FieldValue.ofWithFallback(fallback, fn); return this; }
        public Builder packetHits(Boolean v) { packetHits = FieldValue.constant(v); return this; }
        public Builder swingHits(Boolean v) { swingHits = FieldValue.constant(v); return this; }
        public Builder packetReach(Double v) { packetReach = FieldValue.constant(v); return this; }
        public Builder swingReach(Double v) { swingReach = FieldValue.constant(v); return this; }
        public Builder packetPadding(Double v) { packetPadding = FieldValue.constant(v); return this; }
        public Builder swingPadding(Double v) { swingPadding = FieldValue.constant(v); return this; }
        public Builder ruleset(AttackProcessor.Ruleset v) { ruleset = FieldValue.constant(v); return this; }
        public Builder ruleset(Function<AttackConfigResolver.AttackContext, AttackProcessor.Ruleset> fn) { ruleset = FieldValue.of(fn); return this; }

        Builder enabled(FieldValue<Boolean> v) { enabled = v; return this; }
        Builder idleTimeout(FieldValue<Integer> v) { idleTimeout = v; return this; }
        Builder atkInvulnTicks(FieldValue<Integer> v) { atkInvulnTicks = v; return this; }
        Builder sprintBuffer(FieldValue<Integer> v) { sprintBuffer = v; return this; }
        Builder hitQueueBuffer(FieldValue<Integer> v) { hitQueueBuffer = v; return this; }
        Builder packetHits(FieldValue<Boolean> v) { packetHits = v; return this; }
        Builder swingHits(FieldValue<Boolean> v) { swingHits = v; return this; }
        Builder packetReach(FieldValue<Double> v) { packetReach = v; return this; }
        Builder swingReach(FieldValue<Double> v) { swingReach = v; return this; }
        Builder packetPadding(FieldValue<Double> v) { packetPadding = v; return this; }
        Builder swingPadding(FieldValue<Double> v) { swingPadding = v; return this; }
        Builder ruleset(FieldValue<AttackProcessor.Ruleset> v) { ruleset = v; return this; }

        public AttackConfig build() { return new AttackConfig(this); }
    }
}
