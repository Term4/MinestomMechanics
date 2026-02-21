package io.github.term4.minestommechanics.mechanics.combat.attack;

import io.github.term4.minestommechanics.api.event.combat.AttackEvent;
import io.github.term4.minestommechanics.mechanics.combat.Combat.Config;
import io.github.term4.minestommechanics.mechanics.combat.attack.hitdetection.PacketHit;
import io.github.term4.minestommechanics.mechanics.combat.attack.rulesets.AttackProcessor;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;

public final class AttackFeature {

    // This class allows the user to install the attack feature and config into the combat module

    private AttackFeature() {}

    /**
     * Install the attack feature with attack config (here's where you can set a custom processor)
     *
     * @param combatNode
     * @param cfg
     */
    public static void install(EventNode<Event> combatNode, Config.Attack cfg, AttackServices services, EventNode<Event> apiEvents) {

        // register the listener(s) here and forward t cfg.processor.processAttack()
        //  cfg.reach will be important later for validation + swing/raycast attacks

        if (!cfg.enabled) return;

        // Attack packets
        if (cfg.packetHits) {
            PacketHit.install(combatNode, services, cfg.sprintBuffer, snap -> {
                AttackEvent api = new AttackEvent(snap);
                apiEvents.call(api);
                if (api.cancelled() || !api.process()) return;

                if (api.invulnerable() && !api.bypassInvul()) return;

                AttackProcessor proc = api.processor() != null ? api.processor().create(services) : cfg.ruleset.create(services);
                proc.processAttack(api.finalSnap(), services);
            });
        }

        // Swing raycasts


        // Some other custom hit detection?
    }

}
