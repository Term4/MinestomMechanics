package io.github.term4.minestommechanics.mechanics;

// TODO: Potentially remove this entirely??
public enum Cause {
    ATTACK_PACKET, SWING_RAYCAST, SWEEPING, PROJECTILE, EXPLOSION, DAMAGE;

    /** Returns true if cause is melee. */
    public boolean isMelee() {
        return switch (this) {
            case ATTACK_PACKET, SWING_RAYCAST -> true;
            case SWEEPING, PROJECTILE, EXPLOSION , DAMAGE -> false;
        };
    }
}
