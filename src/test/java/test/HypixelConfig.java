package test;

import io.github.term4.minestommechanics.mechanics.knockback.KnockbackConfig;

import static io.github.term4.minestommechanics.mechanics.knockback.KnockbackConfig.defaultConfig;

// TODO: 99% sure hypixel does "hit chaining" where consecutive hits from the same entity deal different knockback configs.
public class HypixelConfig {

    public static KnockbackConfig hypixelKb() {
        var c = defaultConfig();
        c.sprintBuffer = 4;
        c.vertical = 0.36085;
        c.extraHorizontal = 0.5;
        c.extraVertical = 0.07;
        c.verticalLimit = 0.47;
        c.yawWeight = 1.0;
        c.extraYawWeight = 1.0;
        c.frictionH = 0;
        c.frictionV = 15;
        c.frictionExtraH = 0;
        c.frictionExtraV = 15;
        c.aMultExtraV = 1.475;
        return c;
    }

}
