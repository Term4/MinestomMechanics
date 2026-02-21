package io.github.term4.minestommechanics.mechanics.damage;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

/**
 * @param amount optional damage amount
 */
public record DamageRequest(Entity target, RegistryKey<DamageType> type, @Nullable Entity source, @Nullable Entity attacker,
                            @Nullable Point sourcePosition, @Nullable Float amount) {

    // This class handles creating a damage request to be sent through the DamageSystem interface

    public static DamageRequest of(Entity target, RegistryKey<DamageType> type) {
        return new DamageRequest(target, type, null, null, null, null);
    }

    public DamageRequest source(@Nullable Entity source) {
        return new DamageRequest(target, type, source, attacker, sourcePosition, amount);
    }

    public DamageRequest attacker(@Nullable Entity attacker) {
        return new DamageRequest(target, type, source, attacker, sourcePosition, amount);
    }

    public DamageRequest sourcePosition(Point pos) {
        return new DamageRequest(target, type, source, attacker, pos, amount);
    }

    public DamageRequest amount(Float amount) {
        return new DamageRequest(target, type, source, attacker, sourcePosition, amount);
    }

}
