package worldgate.conqueror.mechanic;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;

import java.util.ArrayList;

public class BedMechanics {
    public static void register() {
        EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, sleepingPosition, vanillaResult) -> ActionResult.SUCCESS);
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPosition) -> {
            var harmfulEffects = new ArrayList<RegistryEntry<StatusEffect>>();
            for (var effect : entity.getStatusEffects()) {
                if (!effect.getEffectType().value().isBeneficial()) {
                    harmfulEffects.add(effect.getEffectType());
                }
            }
            for (var effect : harmfulEffects) {
                entity.removeStatusEffect(effect);
            }
            entity.heal(999);
        });
    }
}
