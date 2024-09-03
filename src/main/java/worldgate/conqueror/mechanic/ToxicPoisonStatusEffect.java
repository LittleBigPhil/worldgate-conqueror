package worldgate.conqueror.mechanic;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ToxicPoisonStatusEffect extends StatusEffect {
    protected ToxicPoisonStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        entity.damage(entity.getDamageSources().magic(), 1.0F);

        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int i = 20 >> amplifier;
        return i == 0 || duration % i == 0;
    }
}
