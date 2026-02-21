package dev.term4;

import io.github.term4.minestommechanics.mechanics.knockback.KnockbackConfig;

import static io.github.term4.minestommechanics.mechanics.knockback.KnockbackConfig.defaultConfig;

public class MinemenConfig {

    public static KnockbackConfig minemen() {
        var c = defaultConfig();
        c.horizontal = 0.525;
        c.vertical = 0.4;
        c.extraHorizontal = 0.3534;
        c.extraVertical = 0.5;
        c.verticalLimit = 0.365;
        c.yawWeight = 0.5;
        c.extraYawWeight = 0.5;
        c.frictionH = 7.0;
        c.frictionV = 6.5;
        c.frictionExtraH = 6.0;
        c.frictionExtraV = 6.5;
        c.rangeStartExtraH = 4.0;
        c.rangeFactorExtraH = 0.25;
        c.rangeMaxH = 0.4;
        return c;
    }

}
