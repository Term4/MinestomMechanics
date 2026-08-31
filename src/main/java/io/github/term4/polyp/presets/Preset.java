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

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/** The shipped server presets; the primed-TNT config rides each profile ({@code MechanicsKeys.TNT}). */
public enum Preset {
    VANILLA18(Vanilla18::profile, UnaryOperator.identity()),
    /** Modern (26.1) mechanics - nothing to reconcile, so the compat layer stays off. */
    VANILLA(Vanilla::profile, base -> Compat18.off()),
    HYPIXEL(Hypixel::profile, Compat::over),
    /** {@link #HYPIXEL} with the BedWars-only quirks (the game-wide pearl landing). */
    HYPIXEL_BEDWARS(Hypixel::bedwars, Compat::over),
    MMC18(Mmc18::profile, UnaryOperator.identity()),
    SCRIMS18(Scrims18::profile, UnaryOperator.identity());

    private final Supplier<MechanicsProfile> profile;
    private final UnaryOperator<CompatConfig> compat;

    Preset(Supplier<MechanicsProfile> profile, UnaryOperator<CompatConfig> compat) {
        this.profile = profile;
        this.compat = compat;
    }

    /** The mechanics profile (fresh build); the server layers compat + fixes on top. */
    public MechanicsProfile profile() { return profile.get(); }

    /** {@link #compat(CompatConfig)} over the plain 1.8 set. */
    public CompatConfig compat() { return compat(Compat18.config()); }

    /**
     * This network's cross-version deltas over {@code base} ({@code null} = the plain 1.8 set) - most presets
     * run 1.8 compat unchanged, Hypixel refuses self-overlapping placement, modern vanilla runs none. Set the
     * result under {@code MechanicsKeys.COMPAT}; passing the scope it displaces keeps the server's own knobs,
     * which matters at world scope where a profile member replaces the global's wholesale.
     */
    public CompatConfig compat(@Nullable CompatConfig base) {
        return compat.apply(base != null ? base : Compat18.config());
    }
}
