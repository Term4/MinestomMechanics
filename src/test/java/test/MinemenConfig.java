package test;

import io.github.term4.minestommechanics.mechanics.knockback.KnockbackConfig;

import static io.github.term4.minestommechanics.mechanics.knockback.KnockbackConfig.defaultConfig;

public class MinemenConfig {

    public static KnockbackConfig minemenKb() {
        var c = defaultConfig();
        c.sprintBuffer = 8;
        c.horizontal = 0.52725;
        c.vertical = 0.4;
        c.extraHorizontal = 0.32625;
        c.extraVertical = 0.0;
        c.verticalLimit = 0.3615;
        c.yawWeight = 0.5;
        c.extraYawWeight = 0.5;
        c.frictionH = 0;
        c.frictionV = 0;
        c.frictionExtraH = 0;
        c.frictionExtraV = 0;
        c.rangeStartExtraH = 3.325;
        c.rangeFactorExtraH = 0.5;
        c.rangeMaxH = 0.40;
        return c;
    }

}
