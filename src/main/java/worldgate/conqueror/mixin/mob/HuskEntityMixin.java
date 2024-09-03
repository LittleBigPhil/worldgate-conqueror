package worldgate.conqueror.mixin.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import worldgate.conqueror.mechanic.ModStatusEffects;
import worldgate.conqueror.mechanic.StatusEffectTarget;

@Mixin(HuskEntity.class)
public class HuskEntityMixin extends ZombieEntity {
    public HuskEntityMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Overwrite @Override
    public boolean tryAttack(Entity target) {
        boolean attackSuccessful = super.tryAttack(target);
        if (attackSuccessful && this.getMainHandStack().isEmpty() && target instanceof LivingEntity) {
            var hungerEffect = new StatusEffectInstance(ModStatusEffects.HUNGER, 140);
            ((StatusEffectTarget)target).addStatusEffectResistable(hungerEffect, this, 0);
        }

        return attackSuccessful;
    }
}
