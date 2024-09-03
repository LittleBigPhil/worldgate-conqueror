package worldgate.conqueror.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.world.World;
import worldgate.conqueror.mechanic.ModStatusEffects;
import worldgate.conqueror.mechanic.StatusEffectTarget;

import java.util.HashSet;
import java.util.Set;

public class SpiderVariantEntity extends SpiderEntity {
    public SpiderVariantEntity(EntityType<? extends SpiderVariantEntity> entityType, World world, PoisonType poisonType) {
        super(entityType, world);
        this.poisonType = poisonType;
    }

    public static EntityType<SpiderVariantEntity> entityType(PoisonType poisonType) {
        EntityType.EntityFactory<SpiderVariantEntity> factory = (entityType, world) -> {
            return new SpiderVariantEntity(entityType, world, poisonType);
        };
        return EntityType.Builder.create(factory, SpawnGroup.MONSTER)
                .dimensions(1.4F, 0.9F)
                .eyeHeight(0.65F)
                .passengerAttachments(0.765F)
                .maxTrackingRange(8)
                .build();
    }

    public enum PoisonType {
        NONE,
        NEUROTOXIC,
        HEMORRHAGIC,
        DEBILITATING
    }
    private PoisonType poisonType;
    public PoisonType getPoisonType() {
        return poisonType;
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean attackSuccessful = super.tryAttack(target);
        if (attackSuccessful && this.getMainHandStack().isEmpty() && target instanceof LivingEntity) {
            //WorldgateConqueror.LOGGER.info("{}",this.isOnGround());
            Set<ModStatusEffects.Instance> poisonEffects = new HashSet<>();
            switch (getPoisonType()) {
                case NEUROTOXIC -> poisonEffects = ModStatusEffects.neurotoxicPoison(0);
                case HEMORRHAGIC -> poisonEffects = ModStatusEffects.hemorrhagicPoison(0);
                case DEBILITATING -> poisonEffects = ModStatusEffects.debilitatingPoison(0);
            }

            for (var instance : poisonEffects) {
                ((StatusEffectTarget)target).addStatusEffectResistable(instance.effectInstance(), this, instance.strength());
            }
        }

        return attackSuccessful;
    }
}
