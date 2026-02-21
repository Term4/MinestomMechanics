package io.github.term4.minestommechanics.mechanics.combat.attack.hitdetection;

import io.github.term4.minestommechanics.api.event.combat.AttackEvent;
import io.github.term4.minestommechanics.mechanics.combat.attack.AttackServices;
import io.github.term4.minestommechanics.mechanics.combat.attack.AttackSnapshot;
import io.github.term4.minestommechanics.util.SprintTracker;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;

import java.util.function.Consumer;

/** This is the hit detection logic for attack packet based hits */
public final class PacketHit {

    public static void install(EventNode<Event> node, AttackServices services, long sprintBuffer, Consumer<AttackSnapshot> onHit) {

        node.addListener(EntityAttackEvent.class, e -> {
            if (!(e.getEntity() instanceof Player attacker)) return; // in reality this will always be a player, still good practice though
            Entity target = e.getTarget();

            AttackSnapshot snap = new AttackSnapshot(
                    attacker,
                    target,
                    AttackEvent.Cause.ATTACK_PACKET,
                    attacker.getItemInMainHand(),
                    SprintTracker.isSprinting(services.sprintTracker(), attacker, sprintBuffer),
                    attacker.getPosition(),
                    target.getPosition()
            );

            onHit.accept(snap);
        });

    }

}
