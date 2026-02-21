package io.github.term4.minestommechanics.mechanics.knockback;

/** Mutable knockback config. Use {@link #defaultConfig()} for fresh default config, use {@link #copy()} for overrides. */
public final class KnockbackConfig {

    public enum DegenerateFallback { LOOK, RANDOM } // fallback for knockback direction when position is degenerate

    /** Determines how normal and extra knockback combine */
    public enum DirectionMode { SCALAR, VECTOR_ADDITION }

    /** Determines the formula used in calculating knockback */
    public enum KnockbackFormula { CLASSIC, MODERN } // Could add custom option? idk maybe a user makes their own, but like just fork at that point tbh

    // Formula
    public KnockbackFormula knockbackFormula = KnockbackFormula.CLASSIC;

    // Strength
    public double horizontal = 0.4;
    public double vertical = 0.4;
    public double extraHorizontal = 0.5;
    public double extraVertical = 0.1;
    public double verticalLimit = 0.4; // null for no limit

    // Direction (values MUST be between 0 and 1)
    public double yawWeight = 0.0;
    public double extraYawWeight = 1.0;
    public double pitchWeight = 0.0;
    public double extraPitchWeight = 0.0;
    public double heightDelta  = 0.0;
    public double extraHeightDelta = 0.0;
    public DirectionMode horizontalCombine = DirectionMode.VECTOR_ADDITION;
    public DirectionMode verticalCombine = DirectionMode.SCALAR;
    public DegenerateFallback degenerateFallback = DegenerateFallback.RANDOM;

    // Friction
    public double frictionH = 2.0;
    public double frictionV = 2.0;
    public double frictionExtraH = 2.0;
    public double frictionExtraV = 2.0;

    // Range Reduction
    public double rangeStartH = 0.0;
    public double rangeFactorH = 0.0;
    public double rangeStartV = 0.0;
    public double rangeFactorV = 0.0;
    public double rangeStartExtraH = 0.0;
    public double rangeFactorExtraH = 0.0;
    public double rangeStartExtraV = 0.0;
    public double rangeFactorExtraV = 0.0;
    public double rangeMaxH = 0.0;
    public double rangeMaxV = 0.0;

    // Air Multipliers -- NO vertical limit (haven't figured out how I want it to work yet lol)
    public double aMultH = 1.0;
    public double aMultV = 1.0;
    public double aMultExtraH = 1.0;
    public double aMultExtraV = 1.0;
    public double aMultVLimit = 0; // zero for no limit

    // Modifiers
    public double sweepFactorH = 0.0;
    public double sweepFactorV = 0.0;
    public double sweepFactorExtraH = 0.0;
    public double sweepFactorExtraV = 0.0;

    public KnockbackConfig() {}

    public KnockbackConfig copy() {
        var c = new KnockbackConfig();
        c.knockbackFormula = knockbackFormula;
        c.horizontal = horizontal;
        c.vertical = vertical;
        c.extraHorizontal = extraHorizontal;
        c.extraVertical = extraVertical;
        c.verticalLimit = verticalLimit;
        c.yawWeight = yawWeight;
        c.pitchWeight = pitchWeight;
        c.extraYawWeight = extraYawWeight;
        c.extraPitchWeight = extraPitchWeight;
        c.heightDelta = heightDelta;
        c.extraHeightDelta = extraHeightDelta;
        c.horizontalCombine = horizontalCombine;
        c.verticalCombine = verticalCombine;
        c.degenerateFallback = degenerateFallback;
        c.frictionH = frictionH;
        c.frictionV = frictionV;
        c.frictionExtraH = frictionExtraH;
        c.frictionExtraV = frictionExtraV;
        c.rangeStartH = rangeStartH;
        c.rangeFactorH = rangeFactorH;
        c.rangeStartV = rangeStartV;
        c.rangeFactorV = rangeFactorV;
        c.rangeStartExtraH = rangeStartExtraH;
        c.rangeFactorExtraH = rangeFactorExtraH;
        c.rangeStartExtraV = rangeStartExtraV;
        c.rangeFactorExtraV = rangeFactorExtraV;
        c.rangeMaxH = rangeMaxH;
        c.rangeMaxV = rangeMaxV;
        c.aMultH = aMultH;
        c.aMultV = aMultV;
        c.aMultExtraH = aMultExtraH;
        c.aMultExtraV = aMultExtraV;
        c.aMultVLimit = aMultVLimit;
        c.sweepFactorH = sweepFactorH;
        c.sweepFactorV = sweepFactorV;
        c.sweepFactorExtraH = sweepFactorExtraH;
        c.sweepFactorExtraV = sweepFactorExtraV;
        return c;
    }

    /** Returns a new config with default values. */
    public static KnockbackConfig defaultConfig() {
        return new KnockbackConfig();
    }

}
