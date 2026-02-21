package io.github.term4.minestommechanics.mechanics.damage;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;

public final class DamageTypes {

    // This class is only used to cache Minestom damage registry lookups

    private DamageTypes() {}

    private static RegistryKey<DamageType> key(String id) {
        return MinecraftServer.getDamageTypeRegistry().getKey(Key.key(id));
    }

    public static final RegistryKey<DamageType> PLAYER_ATTACK = key("minecraft:player_attack");
    public static final RegistryKey<DamageType> GENERIC = key ("minecraft:generic");
    public static final RegistryKey<DamageType> FALL = key ("minecraft:fall");
    public static final RegistryKey<DamageType> FIRE = key ("minecraft:fire");

}
