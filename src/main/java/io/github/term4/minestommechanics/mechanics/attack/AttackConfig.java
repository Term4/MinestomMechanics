package io.github.term4.minestommechanics.mechanics.attack;

import io.github.term4.minestommechanics.mechanics.attack.rulesets.AttackProcessor;

public final class AttackConfig {

    public boolean enabled = true;

    public Integer idleTimeout = null;
    public Integer atkInvulnTicks = null;
    public Integer sprintBuffer = null; // This is separate from the KnockbackConfig sprint buffer

    // Hit Detectors // TODO: Update to be more than just a boolean (what detectors, what modes should the detectors be in)
    public boolean packetHits = true;
    public boolean swingHits = false;

    // Reach Settings
    public double packetReach = 10.0;    // The actual reach allowed (no padding) for attack packets
    public double swingReach = 3.0;     // The reach to use for swing hits
    // Hit padding (allowance for which a hit still registers beyond its set value)
    public double packetPadding = 2.0;  // Attack packets send from 2 blocks more than packetReach will still register (will flag)
    public double swingPadding = 0.0;   // This should typically be zero (swing hits are done via server-side ray intersection)

    // Attack Processing
    public AttackProcessor.Ruleset ruleset = AttackProcessor.legacy(); // What attack processor should we use by default?

    // TODO ? Could potentially map different detectors to different settings...
    //  BENEFITS: One config type per detector, consistency, completely configurable
    //  CONS: Unused methods, possible races / silent fails, could be messy

    public AttackConfig() {}

    public AttackConfig copy() {
        var c = new AttackConfig();
        c.enabled = enabled;
        c.idleTimeout = idleTimeout;
        c.atkInvulnTicks = atkInvulnTicks;
        c.sprintBuffer = sprintBuffer;
        c.packetHits = packetHits;
        c.swingHits = swingHits;
        c.packetReach = packetReach;
        c.swingReach = swingReach;
        c.packetPadding = packetPadding;
        c.swingPadding = swingPadding;
        c.ruleset = ruleset;

        return c;
    }

    /** Returns a new config with default values. */
    public static AttackConfig defaultConfig() { return new AttackConfig(); }
}
