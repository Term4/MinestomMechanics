package io.github.term4.polyp.entity;

import io.github.term4.polyp.util.tick.TickScaler;
import io.github.term4.polyp.world.ExternallyTickable;
import io.github.term4.polyp.world.MechanicsWorld;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

/**
 * 1.8 {@code EntityFallingBlock}: gravity before the move, ×0.98 drag after; the first tick clears the origin
 * block. Landing places into a fallable-into cell (air/fire/liquid) unless the cell below is one too, else
 * drops the item (sand landing on a torch). Steps against ITS world's blocks, so shard towers carry it.
 */
public final class FallingBlockEntity extends Entity implements ExternallyTickable {

    private static final int TPS = ServerFlag.SERVER_TICKS_PER_SECOND;
    private static final int MAX_FALL_TICKS = 600;

    private final Block block;
    private final Point origin;
    private Vec motion = Vec.ZERO;
    private PhysicsResult lastPhysics;
    private int fallTime;

    private FallingBlockEntity(Block block, Point origin) {
        super(EntityType.FALLING_BLOCK);
        this.block = block;
        this.origin = origin;
        setBoundingBox(0.98, 0.98, 0.98);
        ((FallingBlockMeta) getEntityMeta()).setBlock(block);
    }

    /** Spawns at the 1.8 anchor (block center x/z, block-bottom y); the first tick clears {@code origin}. */
    public static FallingBlockEntity spawn(@NotNull MechanicsWorld world, @NotNull Point origin, @NotNull Block block) {
        FallingBlockEntity e = new FallingBlockEntity(block, origin);
        world.spawn(e, new Pos(origin.blockX() + 0.5, origin.blockY(), origin.blockZ() + 0.5));
        return e;
    }

    /** 1.8 {@code BlockFalling.canFallInto}: air, fire, or a liquid. */
    public static boolean canFallInto(@NotNull Block b) {
        return b.air() || b.compare(Block.FIRE) || b.liquid();
    }

    @Override
    public void tick(long time) {
        if (!MechanicsWorld.ownsCurrentTick(this)) return;
        super.tick(time);
    }

    @Override
    protected void movementTick() {
        this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;
        if (vehicle != null || getInstance() == null) return;
        this.lastPhysics = MechanicsWorld.step(this, velocity.div(TPS), lastPhysics, result -> {
            this.velocity = result.newVelocity().mul(TPS);
            this.onGround = result.isOnGround();
            refreshPosition(result.newPosition(), true, false);
        });
    }

    @Override
    public void update(long time) {
        if (isRemoved()) return;
        MechanicsWorld world = MechanicsWorld.of(this);
        if (fallTime++ == 0) {
            if (world.getBlock(origin).compare(block)) {
                world.setBlock(origin, Block.AIR);
                world.applyPhysics(origin);
            } else {
                remove();
                return;
            }
        }
        Aerodynamics aero = TickScaler.aerodynamics(this, getAerodynamics());
        double drag = aero.verticalAirResistance();
        motion = new Vec(motion.x() * aero.horizontalAirResistance(),
                (motion.y() - aero.gravity()) * drag,
                motion.z() * aero.horizontalAirResistance());
        this.velocity = motion.sub(0, aero.gravity(), 0).mul(TPS);

        if (onGround) {
            land(world);
        } else if (getPosition().y() < world.dimension().minY() - 32 || fallTime > MAX_FALL_TICKS) {
            remove();
        }
    }

    private void land(MechanicsWorld world) {
        remove();
        Point cell = getPosition().asVec().apply(Vec.Operator.FLOOR);
        Block at = world.getBlock(cell);
        Block below = world.getBlock(cell.add(0, -1, 0));
        if (canFallInto(at) && !canFallInto(below)) {
            world.setBlock(cell, block);
            world.applyPhysics(cell);
        } else {
            Material material = block.material();
            if (material != null) {
                DroppedItemEntity.spawn(world, new Pos(cell.blockX() + 0.5, cell.blockY() + 0.5, cell.blockZ() + 0.5),
                        Vec.ZERO, ItemStack.of(material), null, 10);
            }
        }
    }
}
