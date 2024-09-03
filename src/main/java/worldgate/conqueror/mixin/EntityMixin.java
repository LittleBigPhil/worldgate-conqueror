package worldgate.conqueror.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import worldgate.conqueror.mechanic.SubmersionAccessor;

import java.util.List;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin implements SubmersionAccessor {
    public EntityMixin(Set<TagKey<Fluid>> submergedFluidTag) {
        this.submergedFluidTag = submergedFluidTag;
    }

    /**
     * @author Decayed
     * @reason Lets spiders target the player during daytime.
     */
    @Deprecated @Overwrite
    public float getBrightnessAtEyes() {
        return 0.0f;
    }

    @Shadow private static List<VoxelShape> findCollisionsForMovement(
            @Nullable Entity entity, World world, List<VoxelShape> regularCollisions, Box movingEntityBoundingBox
    ) {
        return null;
    }
    @Shadow private static float[] collectStepHeights(Box collisionBox, List<VoxelShape> collisions, float f, float stepHeight) {return null;}
    @Shadow private static Vec3d adjustMovementForCollisions(Vec3d movement, Box entityBoundingBox, List<VoxelShape> collisions) {return null;}

    /**
     * @author Decayed
     * @reason Made touching water act is if the player is on the ground for the purposes of applying step height.
     */
    @Overwrite
    private Vec3d adjustMovementForCollisions(Vec3d movement) {
        var entity = (Entity)(Object) this;
        Box box = entity.getBoundingBox();
        List<VoxelShape> list = entity.getWorld().getEntityCollisions(entity, box.stretch(movement));
        Vec3d vec3d = movement.lengthSquared() == 0.0 ? movement : Entity.adjustMovementForCollisions(entity, movement, box, entity.getWorld(), list);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = bl2 && movement.y < 0.0;
        if (entity.getStepHeight() > 0.0F && (bl4 || entity.isOnGround() || entity.isTouchingWater()) && (bl || bl3)) {
            Box box2 = bl4 ? box.offset(0.0, vec3d.y, 0.0) : box;
            Box box3 = box2.stretch(movement.x, (double)entity.getStepHeight(), movement.z);
            if (!bl4) {
                box3 = box3.stretch(0.0, -1.0E-5F, 0.0);
            }

            List<VoxelShape> list2 = findCollisionsForMovement(entity, entity.getWorld(), list, box3);
            float f = (float)vec3d.y;
            float[] fs = collectStepHeights(box2, list2, entity.getStepHeight(), f);

            for (float g : fs) {
                Vec3d vec3d2 = adjustMovementForCollisions(new Vec3d(movement.x, (double)g, movement.z), box2, list2);
                if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
                    double d = box.minY - box2.minY;
                    return vec3d2.add(0.0, -d, 0.0);
                }
            }
        }

        return vec3d;
    }

    /**
     * @author Decayed
     * @reason Made it work with all fluids, instead of only water.
     */
    @Overwrite
    public void updateSwimming() {
        var entity = (Entity)(Object) this;
        if (entity.isSwimming()) {
            entity.setSwimming(entity.isSprinting() && this.isTouchingFluid() && !entity.hasVehicle());
        } else {
            entity.setSwimming(entity.isSprinting() && this.isSubmergedInFluid() && !entity.hasVehicle());
        }
    }

    @Shadow public abstract boolean isSubmergedInWater();

    // Should cache this in baseTick();
    @Unique @Override public boolean isTouchingFluid() {
        var entity = (Entity)(Object) this;
        if (!entity.isRegionUnloaded()) {
            Box box = entity.getBoundingBox().contract(0.001);
            int i = MathHelper.floor(box.minX);
            int j = MathHelper.ceil(box.maxX);
            int k = MathHelper.floor(box.minY);
            int l = MathHelper.ceil(box.maxY);
            int m = MathHelper.floor(box.minZ);
            int n = MathHelper.ceil(box.maxZ);
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            boolean inAnyFluid = false;

            for (int p = i; p < j; p++) {
                for (int q = k; q < l; q++) {
                    for (int r = m; r < n; r++) {
                        mutable.set(p, q, r);
                        FluidState fluidState = entity.getWorld().getFluidState(mutable);
                        if (!fluidState.isEmpty()) {
                            double e = (double) ((float) q + fluidState.getHeight(entity.getWorld(), mutable));
                            if (e >= box.minY) {
                                return true;
                            }
                        }
                    }
                }
            }
            return inAnyFluid;

        }
        return false;
    }
    @Shadow @Final @Mutable private final Set<TagKey<Fluid>> submergedFluidTag;
    @Unique @Override public boolean isSubmergedInFluid() {
        return !this.submergedFluidTag.isEmpty();
    }
}
