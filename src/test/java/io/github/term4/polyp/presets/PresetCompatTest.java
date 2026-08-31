package io.github.term4.polyp.presets;

import io.github.term4.polyp.platform.compatibility.Compat18;
import io.github.term4.polyp.platform.compatibility.CompatConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Each preset carries the cross-version layer of the network it recreates, not just its mechanics. */
class PresetCompatTest {

    @Test
    void hypixelRefusesSelfOverlappingPlacementWhereThe18PresetsAllowIt() {
        assertEquals(Boolean.FALSE, Preset.HYPIXEL.compat().legacySelfPlace);
        assertEquals(Boolean.FALSE, Preset.HYPIXEL_BEDWARS.compat().legacySelfPlace);
        for (Preset legacy : new Preset[]{Preset.VANILLA18, Preset.MMC18, Preset.SCRIMS18}) {
            assertNull(legacy.compat().legacySelfPlace, legacy + " keeps the 1.8 mechanic");
        }
    }

    /** Everything else the 1.8 layer sets still rides along - hypixel is a delta, not a replacement. */
    @Test
    void hypixelKeepsTheRestOfTheLegacyLayer() {
        assertEquals(Preset.VANILLA18.compat().legacyHitbox, Preset.HYPIXEL.compat().legacyHitbox);
        assertEquals(Preset.VANILLA18.compat().blockPlaceReach, Preset.HYPIXEL.compat().blockPlaceReach);
        assertEquals(Preset.VANILLA18.compat().attackHitboxMargin, Preset.HYPIXEL.compat().attackHitboxMargin);
        assertNotNull(Preset.HYPIXEL.compat().disabledPoses);
    }

    @Test
    void modernPresetRunsNoLegacyLayer() {
        assertEquals(Boolean.FALSE, Preset.VANILLA.compat().legacyHitbox, "nothing to reconcile on 26.1");
    }

    /** At world scope a profile member replaces the global's, so a preset applies its deltas OVER what it displaces. */
    @Test
    void presetDeltasRideOverTheServersOwnKnobs() {
        CompatConfig server = Compat18.config().toBuilder().animatiumDebug(true).blockPlaceReach(3.0).build();

        CompatConfig hypixel = Preset.HYPIXEL.compat(server);
        assertEquals(Boolean.FALSE, hypixel.legacySelfPlace, "the network's delta applies");
        assertEquals(Boolean.TRUE, hypixel.animatiumDebug, "the server's own knob survives");
        assertEquals(3.0, hypixel.blockPlaceReach, "and so does a knob it tuned");

        assertEquals(Boolean.TRUE, Preset.MMC18.compat(server).animatiumDebug, "a 1.8 preset passes it through");
        assertNull(Preset.MMC18.compat(server).legacySelfPlace);
    }
}
