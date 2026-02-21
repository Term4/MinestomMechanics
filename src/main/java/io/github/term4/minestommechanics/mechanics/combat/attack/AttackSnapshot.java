package io.github.term4.minestommechanics.mechanics.combat.attack;

import io.github.term4.minestommechanics.api.event.combat.AttackEvent;
import io.github.term4.minestommechanics.api.event.combat.AttackEvent.Cause;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @param attacker the entity initiating the attack
 * @param target the target entity of the attack
 * @param cause the cause of the attack (what kind of attack was this?)
 * @param item the item used to attack (returns air for fist)
 * @param sprint was the attacker sprinting when it attacked?
 * @param attackerPos position of the attacker at the time of the attack
 * @param targetPos position of the target at the time of the attack
 */
public record AttackSnapshot (Entity attacker, @Nullable Entity target, AttackEvent.Cause cause,
                              ItemStack item, boolean sprint, @Nullable Point attackerPos, @Nullable Point targetPos) {

    public Builder toBuilder() { return new Builder(attacker, target, cause, item, sprint, attackerPos, targetPos); }

    public static final class Builder {
        private Entity attacker;
        private @Nullable Entity target;
        private final Cause cause;
        private ItemStack item;
        private boolean sprint;
        private @Nullable Point attackerPos;
        private @Nullable Point targetPos;

        Builder(Entity a, @Nullable Entity t, Cause c, ItemStack i, boolean s, @Nullable Point ap, @Nullable Point tp) {
            attacker = a; target = t; cause = c; item = i; sprint = s; attackerPos = ap; targetPos = tp;
        }

        public Builder attacker(Entity e) { attacker = e; attackerPos = e != null ? e.getPosition() : null; return this; }
        public Builder target(Entity e) { target = e; targetPos = e != null ? e.getPosition() : null; return this; }
        public Builder item(ItemStack i) { item = i; return this; }
        public Builder sprint(boolean s) { sprint = s; return this; }
        public Builder attackerPos(Point p) { attackerPos = p; return this; }
        public Builder targetPos(Point p) { targetPos = p; return this; }

        public AttackSnapshot build() {
            return new AttackSnapshot(attacker, target, cause, item, sprint, attackerPos, targetPos);
        }
    }
}
