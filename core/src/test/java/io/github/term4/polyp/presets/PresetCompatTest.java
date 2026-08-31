package io.github.term4.polyp.presets;

import io.github.term4.polyp.testsupport.HeadlessServerTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Each preset carries the cross-version layer of the network it recreates, not just its mechanics. */
class PresetCompatTest extends HeadlessServerTest { // profile() boots Fx, which needs the server

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

    /** Every preset hands back a complete layer - callers set it, they never assemble one. */
    @Test
    void everyPresetCarriesAWholeLayer() {
        for (Preset preset : Preset.values()) {
            assertNotNull(preset.compat(), preset + " has a compat layer");
            assertNotNull(preset.profile(), preset + " has a mechanics profile");
        }
    }
}
