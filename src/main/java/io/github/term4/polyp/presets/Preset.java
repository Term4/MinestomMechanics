package io.github.term4.polyp.presets;

import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.platform.compatibility.Compat18;
import io.github.term4.polyp.platform.compatibility.CompatConfig;
import io.github.term4.polyp.presets.hypixel.Compat;
import io.github.term4.polyp.presets.hypixel.Hypixel;
import io.github.term4.polyp.presets.mmc18.Mmc18;
import io.github.term4.polyp.presets.scrims18.Scrims18;
import io.github.term4.polyp.presets.vanilla.Vanilla;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;

import java.util.function.Supplier;

/** The shipped server presets; the primed-TNT config rides each profile ({@code MechanicsKeys.TNT}). */
public enum Preset {
    VANILLA18(Vanilla18::profile, Compat18::config),
    /** Modern (26.1) mechanics - nothing to reconcile, so the compat layer stays off. */
    VANILLA(Vanilla::profile, Compat18::off),
    HYPIXEL(Hypixel::profile, Compat::config),
    /** {@link #HYPIXEL} with the BedWars-only quirks (the game-wide pearl landing). */
    HYPIXEL_BEDWARS(Hypixel::bedwars, Compat::config),
    MMC18(Mmc18::profile, Compat18::config),
    SCRIMS18(Scrims18::profile, Compat18::config);

    private final Supplier<MechanicsProfile> profile;
    private final Supplier<CompatConfig> compat;

    Preset(Supplier<MechanicsProfile> profile, Supplier<CompatConfig> compat) {
        this.profile = profile;
        this.compat = compat;
    }

    /** The mechanics profile (fresh build); the server layers compat + fixes on top. */
    public MechanicsProfile profile() { return profile.get(); }

    /**
     * The cross-version layer this network runs (fresh build) - the plain 1.8 set unless the preset is measured
     * to differ, as Hypixel does on self-overlapping placement. Set it under {@code MechanicsKeys.COMPAT},
     * {@code toBuilder()}-ing in the server's own knobs first.
     */
    public CompatConfig compat() { return compat.get(); }
}
