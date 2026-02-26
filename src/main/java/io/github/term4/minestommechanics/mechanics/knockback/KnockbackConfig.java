package io.github.term4.minestommechanics.mechanics.knockback;

// TODO: Maybe have this be read from a yaml orr something, idk
/** Mutable knockback config. Use {@link #defaultConfig()} for fresh default config, use {@link #copy()} for overrides. */
public final class KnockbackConfig {

    public enum DegenerateFallback { LOOK, RANDOM } // Fallback for knockback direction when position is degenerate

    /** Determines how normal and extra knockback combine */
    public enum DirectionMode { SCALAR, VECTOR_ADDITION }

    /** Determines the formula used in calculating knockback */
    public enum KnockbackFormula { CLASSIC, MODERN } // Could add custom option? idk maybe a user makes their own, but like just fork at that point tbh

    // Settings
    public Integer idleTimeout = 0;         // This is the "buffer" used to determine when a player is "idle" or "active". It is based on time since last sprint tick (like sprint buffer)
    public Integer kbInvulnTicks = null;    // Based on damage invulnerability when null
    public int sprintBuffer = 0;            // Counts hits in the past N ticks as sprint hits regardless of the attackers current sprint state

    // Idle values
    // Strength
    public double horizontal = 0.4;                 // Base horizontal strength
    public double vertical = 0.4;                   // Base vertical strength
    public double extraHorizontal = 0.5;            // Extra "sprint" horizontal strength
    public double extraVertical = 0.1;              // Extra "sprint" vertical strength
    public double nonIdleHorizontal = 0.4;          // Non "idle" horizontal strength
    public double nonIdleVertical = 0.4;            // Non "idle" vertical strength
    public double nonIdleExtraHorizontal = 0.5;     // Non "idle" extra "sprint" horizontal strength
    public double nonIdleExtraVertical = 0.1;       // Non "idle" extra "sprint" vertical strength
    public double verticalLimit = 0.4;              // null for no limit

    // Direction (values MUST be between 0 and 1)
    public double yawWeight = 0.0;                  // The effect the source's yaw has on horizontal direction for non sprint hits (0-1)
    public double extraYawWeight = 1.0;             // The effect the source's yaw has on horizontal direction for sprint hits (0-1)
    public double pitchWeight = 0.0;                // The effect the source's pitch has on vertical "direction" for non sprint hits (0-1)
    public double extraPitchWeight = 0.0;           // The effect the source's pitch has on vertical "direction" for sprint hits (0-1)
    public double heightDelta  = 0.0;               // The effect the height difference between the source and victim has on vertical "direction" (0-1)
    public double extraHeightDelta = 0.0;
    public DirectionMode horizontalCombine = DirectionMode.VECTOR_ADDITION;     // The method used to combine the base + extra velocity vectors for the horizontal component of knockback
    public DirectionMode verticalCombine = DirectionMode.SCALAR;                // The method used to combine the base + extra velocity vectors for the vertical component of knockback
    public DegenerateFallback degenerateFallback = DegenerateFallback.RANDOM;   // The fallback method for determining the direction of the knockback vector (almost never used, here for completion)

    // Friction (NOTE: This is ADDITIVE. This does not individually affect base / extra. Setting a value for extra simply uses that value for the ENTIRE knockback vector)
    public double frictionH = 2.0;                  // The horizontal friction used in non-extra knockback events
    public double frictionV = 2.0;                  // The vertical friction used in non-extra knockback events
    public double frictionExtraH = 2.0;             // The horizontal friction used in extra knockback events
    public double frictionExtraV = 2.0;             // The vertical friction used in extra knockback events

    // Range Reduction
    public double rangeStartH = 0.0;                // The range at which base horizontal range reduction will begin
    public double rangeFactorH = 0.0;               // The factor that base horizontal range reduction is scaled by before being subtracted from the initial value
    public double rangeStartV = 0.0;                // The range at which base vertical range reduction will begin
    public double rangeFactorV = 0.0;               // The factor that base vertical range reduction is scaled by before being subtracted from the initial value
    public double rangeStartExtraH = 0.0;           // The range at which extra horizontal range reduction will begin
    public double rangeFactorExtraH = 0.0;          // The factor that extra horizontal range reduction is scaled by before being subtracted from the initial value
    public double rangeStartExtraV = 0.0;           // The range at which extra vertical range reduction will begin
    public double rangeFactorExtraV = 0.0;          // The factor that extra vertical range reduction is scaled by before being subtracted from the initial value
    public double rangeMaxH = 0.0;                  // The maximum value of horizontal range reduction to be subtracted from the initial value
    public double rangeMaxV = 0.0;                  // The maximum value of vertical range reduction to be subtracted from the initial value
    public double rangeMaxExtraH = 0.0;             // The maximum value of extra horizontal range reduction to be subtracted from the initial value
    public double rangeMaxExtraV = 0.0;             // The maximum value of extra vertical range reduction to be subtracted from the initial value

    // Air Multipliers -- NO vertical limit (haven't figured out how I want it to work yet lol)
    public double aMultH = 1.0;                     // The factor that base horizontal will be scaled by when the target is in the air
    public double aMultV = 1.0;                     // The factor that base vertical will be scaled by when the target is in the air
    public double aMultExtraH = 1.0;                // The factor that extra horizontal will be scaled by when the target is in the air
    public double aMultExtraV = 1.0;                // The factor that extra vertical will be scaled by when the target is in the air
    public double aMultVLimit = 0;                  // The maximum value that vertical can reach while the target is in the air (zero for no limit)

    // Modifiers
    public double sweepFactorH = 0.0;               // The factor to scale horizontal by for sweeping knockback
    public double sweepFactorV = 0.0;               // The factor to scale vertical by for sweeping knockback
    public double sweepFactorExtraH = 0.0;          // The factor to scale extra horizontal by for sweeping knockback
    public double sweepFactorExtraV = 0.0;          // The factor to scale extra vertical by for sweeping knockback

    // Formula
    public KnockbackFormula knockbackFormula = KnockbackFormula.CLASSIC;    // The actual knockback formula to use for the calculation

    public KnockbackConfig() {}

    public KnockbackConfig copy() {
        var c = new KnockbackConfig();
        c.kbInvulnTicks = kbInvulnTicks;
        c.sprintBuffer = sprintBuffer;
        c.horizontal = horizontal;
        c.vertical = vertical;
        c.extraHorizontal = extraHorizontal;
        c.extraVertical = extraVertical;
        c.nonIdleHorizontal = nonIdleHorizontal;
        c.nonIdleVertical = nonIdleVertical;
        c.nonIdleExtraHorizontal = nonIdleExtraHorizontal;
        c.nonIdleExtraVertical = nonIdleExtraVertical;
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
        c.knockbackFormula = knockbackFormula;

        return c;
    }

    /** Returns a new config with default values. */
    public static KnockbackConfig defaultConfig() {
        return new KnockbackConfig();
    }

}
