package io.github.term4.polyp;

import io.github.term4.polyp.mechanics.attribute.AttributeSystem;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Sharpness;
import io.github.term4.polyp.mechanics.consumable.ConsumableSystem;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.presets.Preset;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bare installs (no global profile) + a full preset scoped onto one instance: consumable types, projectile
 * triggers, and attribute sources must all resolve from the scope. The bare sibling proves the boundary.
 */
class ScopedProfileIdentityTest extends HeadlessServerTest {

    private static InstanceContainer arena;
    private static InstanceContainer plain;
    private static AttributeSystem bareAttributes;

    @BeforeAll
    static void bareInstallsAndScopedArena() {
        polyp.unregister(AttributeSystem.class);
        bareAttributes = AttributeSystem.install(polyp);
        ConsumableSystem.install(polyp);
        ProjectileSystem.install(polyp);
        arena = flatInstance(Preset.HYPIXEL.profile());
        plain = flatInstance(null);
    }

    private static void click(FakePlayer p, Material material, int itemUseTime) {
        EventDispatcher.call(new PlayerUseItemEvent(p.player, PlayerHand.MAIN, ItemStack.of(material), itemUseTime));
    }

    private static boolean spawned(Instance in, EntityType type) {
        long deadline = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < deadline) {
            if (in.getEntities().stream().anyMatch(e -> e.getEntityType() == type)) return true;
            Thread.onSpinWait();
        }
        return false;
    }

    // spread spawns: a leftover in the throw line eats the projectile point-blank on its spawn tick

    @Test
    void snowballClickLaunchesOnlyInTheProfiledScope() {
        FakePlayer thrower = FakePlayer.connect(arena, new Pos(2.5, 65, 2.5), "ScopedThrower");
        click(thrower, Material.SNOWBALL, 0);
        assertTrue(spawned(arena, EntityType.SNOWBALL), "scoped profile arms the snowball trigger");

        FakePlayer bystander = FakePlayer.connect(plain, new Pos(2.5, 65, 2.5), "PlainThrower");
        click(bystander, Material.SNOWBALL, 0);
        assertFalse(plain.getEntities().stream().anyMatch(e -> e.getEntityType() == EntityType.SNOWBALL),
                "no profile, no install config: the click stays inert");
    }

    @Test
    void splashPotionClickLaunchesOnlyInTheProfiledScope() {
        FakePlayer thrower = FakePlayer.connect(arena, new Pos(12.5, 65, 2.5), "ScopedSplash");
        click(thrower, Material.SPLASH_POTION, 0);
        assertTrue(spawned(arena, EntityType.SPLASH_POTION), "scoped profile arms the splash potion trigger");

        FakePlayer bystander = FakePlayer.connect(plain, new Pos(12.5, 65, 2.5), "PlainSplash");
        click(bystander, Material.SPLASH_POTION, 0);
        assertFalse(plain.getEntities().stream().anyMatch(e -> e.getEntityType() == EntityType.SPLASH_POTION));
    }

    // POTION carries no FOOD component, so the component-food floor can't mask the scoped identity

    @Test
    void potionDrinksOnlyInTheProfiledScope() {
        FakePlayer drinker = FakePlayer.connect(arena, new Pos(2.5, 65, 12.5), "ScopedDrinker");
        var use = new PlayerUseItemEvent(drinker.player, PlayerHand.MAIN, ItemStack.of(Material.POTION), 999);
        EventDispatcher.call(use);
        assertNotEquals(999, use.getItemUseTime(), "scoped types make the potion a consumable");
        assertTrue(use.getItemUseTime() > 0);

        FakePlayer parched = FakePlayer.connect(plain, new Pos(2.5, 65, 12.5), "PlainDrinker");
        var dud = new PlayerUseItemEvent(parched.player, PlayerHand.MAIN, ItemStack.of(Material.POTION), 999);
        EventDispatcher.call(dud);
        assertEquals(999, dud.getItemUseTime(), "no identity anywhere: the use passes through untouched");
    }

    @Test
    void sharpnessSourceResolvesOnlyInTheProfiledScope() {
        ItemStack sword = enchanted(Material.DIAMOND_SWORD, Sharpness.KEY, 2);

        LivingEntity scoped = new LivingEntity(EntityType.ZOMBIE);
        scoped.setInstance(arena, new Pos(12.5, 65, 12.5)).join();
        assertFalse(bareAttributes.activeSources(scoped, sword).isEmpty(), "scoped catalog carries sharpness");

        LivingEntity unscoped = new LivingEntity(EntityType.ZOMBIE);
        unscoped.setInstance(plain, new Pos(12.5, 65, 12.5)).join();
        assertTrue(bareAttributes.activeSources(unscoped, sword).isEmpty(), "bare install + bare scope: no catalog");
    }
}
