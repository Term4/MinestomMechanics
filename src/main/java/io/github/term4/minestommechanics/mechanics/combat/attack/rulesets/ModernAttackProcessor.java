package io.github.term4.minestommechanics.mechanics.combat.attack.rulesets;

import io.github.term4.minestommechanics.mechanics.combat.attack.AttackServices;
import io.github.term4.minestommechanics.mechanics.combat.attack.AttackSnapshot;

public final class ModernAttackProcessor implements AttackProcessor {

    // This class will hande modern combat attacks (damage, knockback, cooldowns, modifiers)

    private final AttackServices services;

    public ModernAttackProcessor(AttackServices services) {
        this.services = services;
    }

    @Override
    public void processAttack(AttackSnapshot attack, AttackServices services) {

    }

}
