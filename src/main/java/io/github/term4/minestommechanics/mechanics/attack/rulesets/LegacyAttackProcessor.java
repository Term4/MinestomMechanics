package io.github.term4.minestommechanics.mechanics.attack.rulesets;

import io.github.term4.echofix.EchoFixPlayer;
import io.github.term4.minestommechanics.Services;
import io.github.term4.minestommechanics.mechanics.attack.AttackSnapshot;
import io.github.term4.minestommechanics.mechanics.damage.DamageRequest;
import io.github.term4.minestommechanics.mechanics.damage.DamageSystem;
import io.github.term4.minestommechanics.mechanics.knockback.KnockbackSnapshot;
import io.github.term4.minestommechanics.mechanics.knockback.KnockbackSystem;
import net.minestom.server.entity.LivingEntity;

import static io.github.term4.minestommechanics.mechanics.damage.DamageTypes.PLAYER_ATTACK;

// This class handles legacy (pre-1.9) combat attacks (damage, knockback, cooldown)
public record LegacyAttackProcessor(Services services) implements AttackProcessor {

    @Override public void processAttack(AttackSnapshot snap) {
        // 1. Knockback
        KnockbackSystem kb = services.knockback();
        if (kb != null) {
            var kbSnap = new KnockbackSnapshot(snap.target(), (snap.cause()), snap.attacker(), null, null, kb.config());
            kb.apply(kbSnap);
        }
        // 2. Damage
        if (snap.target() instanceof LivingEntity living) {
            DamageSystem damage = services.damage();
            if (damage != null) {
                // TODO: Replace with a damage snapshot
                damage.apply(DamageRequest.of(living, PLAYER_ATTACK)
                        .attacker(snap.attacker())
                        .source(snap.attacker())
                        .amount(0.01f)
                );
            }
        }
        // Modify attacker sprint
        if (snap.attacker() instanceof LivingEntity le) {
            if (le instanceof EchoFixPlayer efp) {
                efp.suppressSelf(() -> efp.setSprinting(false));
            } else {
                le.setSprinting(false);
            }
        }
    }
}