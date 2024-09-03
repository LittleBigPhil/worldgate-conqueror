package worldgate.conqueror.mechanic;

import net.minecraft.entity.LivingEntity;
import worldgate.conqueror.util.RandomHelper;

public interface KnockbackTarget {
    LivingEntity entity();

    default void maybeTakeKnockback(LivingEntity source, double strength, double x, double z) {
        var knockbackSuccessful = RandomHelper.savingThrow(entity().getRandom(), (float) source.getAttributeValue(ModEntityAttributes.STRENGTH), (float) entity().getAttributeValue(ModEntityAttributes.STRENGTH));
        if (knockbackSuccessful) {
            entity().takeKnockback(strength, x, z);
        }
    }
}
