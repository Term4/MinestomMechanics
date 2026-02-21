package io.github.term4.minestommechanics.mechanics.combat.attack.rulesets;

import io.github.term4.minestommechanics.mechanics.combat.attack.AttackServices;
import io.github.term4.minestommechanics.mechanics.combat.attack.AttackSnapshot;

public final class CTSAttackProcessor implements AttackProcessor {

    // This class will handle combat test snapshot style attacks (damage, knockback, cooldown)

    private final AttackServices services;

    public CTSAttackProcessor(AttackServices services) {
        this.services = services;
    }

    @Override
    public void processAttack(AttackSnapshot attack, AttackServices services) {

    }

}
