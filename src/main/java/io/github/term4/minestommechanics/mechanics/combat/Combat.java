package io.github.term4.minestommechanics.mechanics.combat;

import io.github.term4.minestommechanics.mechanics.combat.attack.AttackServices;
import io.github.term4.minestommechanics.mechanics.combat.attack.AttackFeature;
import io.github.term4.minestommechanics.mechanics.combat.attack.rulesets.AttackProcessor;
import io.github.term4.minestommechanics.MinestomMechanics;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;

public final class Combat {

    // This class is basically what the user uses to install different combat behaviors (e.g. Combat.install(attack/blocking/etc)

    private Combat() {}

    public static final class Config {
        public boolean enableAttack = true; // Attack is enabled by default
        public final Attack attack = new Attack();

        public static final class Attack {
            public boolean enabled = true;
            public boolean packetHits = true;
            /**
             * Advanced attack processing. Most modification can be done through tags or via AttackEvent.
             * Most users should leave this alone.
             */
            public AttackProcessor.Ruleset ruleset = AttackProcessor.legacy();

            public double reach = 3.0;
            public long sprintBuffer = 8;
        }
    }

    public static void install(MinestomMechanics mm) {
        install(mm, new Config());
    }

    public static void install(MinestomMechanics mm, Config cfg) {
         EventNode<Event> combat = EventNode.all("mm:combat");

        // Optional services (get from mm instance)
        var damage = mm.damageSystem();
        var knockback = mm.knockbackSystem();
        var sprintTracker = mm.sprintTracker();

        var services = new AttackServices(damage, knockback, sprintTracker);

         if (cfg.enableAttack) {
             AttackFeature.install(combat, cfg.attack, services, mm.events());
         }

         mm.install(combat);
    }
}
