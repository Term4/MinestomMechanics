package io.github.term4.polyp.presets.hypixel;

import io.github.term4.polyp.platform.compatibility.Compat18;
import io.github.term4.polyp.platform.compatibility.CompatConfig;

/**
 * <b>Hypixel</b>'s cross-version layer: the 1.8 set ({@link Compat18#config()}) with the knobs this network is
 * measured to differ on. Only verified deltas belong here - a preset is a recreation, not a guess.
 */
public final class Compat {

    private Compat() {}

    public static CompatConfig config() {
        return Compat18.config().toBuilder()
                // a placement overlapping the placer is refused for 1.8 clients too (vanilla 1.8 allowed it);
                // no-collision-box blocks are unaffected, so the ladder clutch still lands
                .legacySelfPlace(false)
                .build();
    }
}
