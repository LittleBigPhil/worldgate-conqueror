package worldgate.conqueror.mixin.mob;

import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.mechanic.DamageTypeDistribution;
import worldgate.conqueror.mechanic.ModStatusEffects;
import worldgate.conqueror.mechanic.StatusEffectTarget;

@Mixin(SpiderEntity.class)
public class SpiderEntityMixin extends HostileEntity {
    protected SpiderEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method="createSpiderAttributes", at=@At("RETURN"), cancellable = true)
    private static void createSpiderAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        DefaultAttributeContainer.Builder builder = cir.getReturnValue();
        double defaultMoveSpeed = 0.30000001192092896;
        builder.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, defaultMoveSpeed * 1.5);
        builder.add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 20);
        DamageTypeDistribution.Mob.SPIDER.setBaseAttributesOf(builder);
        cir.setReturnValue(builder);
    }

    @Overwrite @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        entityData = super.initialize(world, difficulty, spawnReason, entityData);
        // no spider jockeys
        // no random status effects
        return entityData;
    }
}
