package io.github.term4.polyp.presets;

import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.presets.hypixel.Hypixel;
import io.github.term4.polyp.presets.mmc18.Mmc18;

import java.util.function.Supplier;

/** A selectable server preset; the primed-TNT config rides the profile ({@code MechanicsKeys.TNT}). */
public enum Preset {
    HYPIXEL(Hypixel::profile),
    /** {@link #HYPIXEL} with the BedWars-only quirks (the game-wide pearl landing). */
    HYPIXEL_BEDWARS(Hypixel::bedwars),
    MMC18(Mmc18::profile);

    private final Supplier<MechanicsProfile> profile;

    Preset(Supplier<MechanicsProfile> profile) {
        this.profile = profile;
    }

    /** The mechanics profile (fresh build); the server layers compat + fixes on top. */
    public MechanicsProfile profile() { return profile.get(); }
}
