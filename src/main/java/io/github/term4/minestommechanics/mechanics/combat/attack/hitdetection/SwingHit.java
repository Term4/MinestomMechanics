package io.github.term4.minestommechanics.mechanics.combat.attack.hitdetection;

public class SwingHit {

    // Only process swing packets as emulated attacks if an attack packet was NOT sent (attack packets take priority)
    //  Then we check if there are any attackable entities within whatever reach we have set for the server / instance / player,
    //  ONLY then will we do raytracing / AABB to check if the emulated attack lands on the victim.

    // Idea for raytracing once we get there:
    //  Can narrow down possible targets based on the direction the player is facing + where the entities are
    //  then do real raytrace from the attackers eye position (NOTE: use compatibility hitbox + eyeheight if enabled, NOT actual)
    //  Then we can determine if their raytrace intersects with an attackable entity, and process the attack like normal
    //  WITHOUT looping through closest, next closest, and so on for each entity.

}
