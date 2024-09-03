package worldgate.conqueror.mechanic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;
import worldgate.conqueror.util.RandomHelper;

import java.text.MessageFormat;
import java.util.Optional;

public interface StatusEffectTarget {
    LivingEntity entity();

    default void addStatusEffectResistable(StatusEffectInstance effect, @Nullable Entity source, float effectRawStrength) {
        var applied = !doesResistEffect(effect.getEffectType(), source, effectRawStrength);
        if (applied) {
            entity().addStatusEffect(effect, source);
        }
    }
    default boolean doesResistEffect(RegistryEntry<StatusEffect> effect, @Nullable Entity source, float effectRawStrength) {
        var effectStrength = effectRawStrength;
        var optAttribute = ModStatusEffects.attributeOf(effect);
        if (optAttribute.isPresent()) {
            var attribute = optAttribute.get();
            if (source instanceof LivingEntity sourceEntity) {
                if (!attribute.equals(ModEntityAttributes.HARDINESS)) {
                    effectStrength += (float) sourceEntity.getAttributeValue(attribute);
                } else {
                    effectStrength += (float) sourceEntity.getAttributeValue(ModEntityAttributes.POISON_STRENGTH);
                }
            }
            var targetResistance = entity().getAttributeValue(attribute);
            return !RandomHelper.savingThrow(entity().getRandom(), effectStrength, (float) targetResistance);
        } else {
            return false;
        }
    }
}
