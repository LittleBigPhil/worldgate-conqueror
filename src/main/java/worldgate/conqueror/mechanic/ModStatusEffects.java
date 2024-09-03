package worldgate.conqueror.mechanic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import worldgate.conqueror.WorldgateConqueror;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ModStatusEffects {
    public static final RegistryEntry<StatusEffect> GRAPPLED = register("grappled",
            new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {}
                    .addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, Identifier.of(WorldgateConqueror.MOD_ID, "effect.grappled.knockback"), 1, EntityAttributeModifier.Operation.ADD_VALUE)
                    .addAttributeModifier(ModEntityAttributes.ARMOR_EFFICIENCY, Identifier.of(WorldgateConqueror.MOD_ID, "effect.grappled.armor"), -.50, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(ModEntityAttributes.DODGE, Identifier.of(WorldgateConqueror.MOD_ID, "effect.grappled.dodge"), -1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );
    public static final RegistryEntry<StatusEffect> LIFTED = register("lifted", new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {});
    public static final RegistryEntry<StatusEffect> PINNED = register("pinned", new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {});
    public static final RegistryEntry<StatusEffect> BOUND = register("bound", new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {});

    public static final RegistryEntry<StatusEffect> STUNNED = register("stunned", new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {});
    public static final RegistryEntry<StatusEffect> FRIGHTENED = register("frightened", new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {});
    public static final RegistryEntry<StatusEffect> CHARMED = register("charmed", new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {});
    public static final RegistryEntry<StatusEffect> CALMED = register("calmed", new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {});
    public static final RegistryEntry<StatusEffect> DARKNESS = register("darkness", new StatusEffect(StatusEffectCategory.HARMFUL, 2696993) {}.fadeTicks(4));

    public static final RegistryEntry<StatusEffect> TOXIC_POISONED = register("toxic_poison", new ToxicPoisonStatusEffect(StatusEffectCategory.HARMFUL, 8889187));
    public static final RegistryEntry<StatusEffect> BLEEDING = register("bleeding", new ToxicPoisonStatusEffect(StatusEffectCategory.HARMFUL, 9154528) {});
    public static final RegistryEntry<StatusEffect> BURNING = register("burning", new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {});
    public static final RegistryEntry<StatusEffect> SLUGGISH = register("sluggish",
            new StatusEffect(StatusEffectCategory.HARMFUL, 8889187) {}
                    .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, Identifier.of(WorldgateConqueror.MOD_ID, "effect.sluggish.attack_speed"), -.50, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, Identifier.of(WorldgateConqueror.MOD_ID, "effect.sluggish.movement_speed"), -.50, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );
    public static final RegistryEntry<StatusEffect> PARALYZED = register("paralyzed", new StatusEffect(StatusEffectCategory.HARMFUL, 8889187) {});
    public static final RegistryEntry<StatusEffect> CHILLED = register("chilled",
            new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {}
                    .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, Identifier.of(WorldgateConqueror.MOD_ID, "effect.chilled.attack_speed"), -.50, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, Identifier.of(WorldgateConqueror.MOD_ID, "effect.chilled.movement_speed"), -.50, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );
    public static final RegistryEntry<StatusEffect> FROZEN = register("frozen", new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {});
    public static final RegistryEntry<StatusEffect> HUNGER = register("hunger", new StrongHungerStatusEffect(StatusEffectCategory.HARMFUL, 5797459));
    public static final RegistryEntry<StatusEffect> NAUSEA = register("nausea", new StatusEffect(StatusEffectCategory.HARMFUL, 8889187) {});
    public static final RegistryEntry<StatusEffect> HEMOPHILIA = register("hemophilia", new StatusEffect(StatusEffectCategory.HARMFUL, 9154528) {});
    public static final RegistryEntry<StatusEffect> WEAKNESS = register("weakness",
            new StatusEffect(StatusEffectCategory.HARMFUL, 4738376) {}
                    .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, Identifier.of(WorldgateConqueror.MOD_ID, "effect.weakness"), -.50, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );

    public static Optional<RegistryEntry.Reference<EntityAttribute>> attributeOf(RegistryEntry<StatusEffect> effect) {
        if (STRENGTH_EFFECTS.contains(effect)) {
            return Optional.of(ModEntityAttributes.STRENGTH);
        } else if (MIND_EFFECTS.contains(effect)) {
            return Optional.of(ModEntityAttributes.MIND);
        } else if (HARDINESS_EFFECTS.contains(effect)) {
            return Optional.of(ModEntityAttributes.HARDINESS);
        } else {
            return Optional.empty();
        }
    }
    private static final Set<RegistryEntry<StatusEffect>> STRENGTH_EFFECTS = new HashSet<>();
    static {
        STRENGTH_EFFECTS.add(GRAPPLED);
        STRENGTH_EFFECTS.add(LIFTED);
        STRENGTH_EFFECTS.add(PINNED);
        STRENGTH_EFFECTS.add(BOUND);
    }
    private static final Set<RegistryEntry<StatusEffect>> MIND_EFFECTS = new HashSet<>();
    static {
        MIND_EFFECTS.add(STUNNED);
        MIND_EFFECTS.add(FRIGHTENED);
        MIND_EFFECTS.add(CHARMED);
        MIND_EFFECTS.add(CALMED);
        MIND_EFFECTS.add(DARKNESS);
    }
    private static final Set<RegistryEntry<StatusEffect>> HARDINESS_EFFECTS = new HashSet<>();
    static {
        HARDINESS_EFFECTS.add(TOXIC_POISONED);
        HARDINESS_EFFECTS.add(BLEEDING);
        HARDINESS_EFFECTS.add(BURNING);
        HARDINESS_EFFECTS.add(SLUGGISH);
        HARDINESS_EFFECTS.add(PARALYZED);
        HARDINESS_EFFECTS.add(CHILLED);
        HARDINESS_EFFECTS.add(FROZEN);
        HARDINESS_EFFECTS.add(HUNGER);
        HARDINESS_EFFECTS.add(NAUSEA);
        HARDINESS_EFFECTS.add(HEMOPHILIA);
        HARDINESS_EFFECTS.add(WEAKNESS);
    }


    public static boolean isImmobilized(LivingEntity entity) {
        for (var statusEffectInstance : entity.getStatusEffects()) {
            if (IMMOBILIZING_EFFECTS.contains(statusEffectInstance.getEffectType())) {
                return true;
            }
        }
        return false;
    }
    private static final Set<RegistryEntry<StatusEffect>> IMMOBILIZING_EFFECTS = new HashSet<>();
    static {
        IMMOBILIZING_EFFECTS.add(STUNNED);
        IMMOBILIZING_EFFECTS.add(PINNED);
        IMMOBILIZING_EFFECTS.add(BOUND);
        IMMOBILIZING_EFFECTS.add(PARALYZED);
        for(var effect : IMMOBILIZING_EFFECTS) {
            var compatibleIdString = effect.getIdAsString().replace(":",".");
            effect.value()
                .addAttributeModifier(ModEntityAttributes.ARMOR_EFFICIENCY, Identifier.of(WorldgateConqueror.MOD_ID, "effect." + compatibleIdString + ".armor"), -.50, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addAttributeModifier(ModEntityAttributes.DODGE, Identifier.of(WorldgateConqueror.MOD_ID, "effect." + compatibleIdString + ".dodge"), -1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    public static boolean isDisarmed(LivingEntity entity) {
        for (var statusEffectInstance : entity.getStatusEffects()) {
            if (DISARMING_EFFECTS.contains(statusEffectInstance.getEffectType())) {
                return true;
            }
        }
        return false;
    }
    private static final Set<RegistryEntry<StatusEffect>> DISARMING_EFFECTS = new HashSet<>();
    static {
        DISARMING_EFFECTS.add(STUNNED);
        DISARMING_EFFECTS.add(BOUND);
        DISARMING_EFFECTS.add(CALMED);
        DISARMING_EFFECTS.add(PARALYZED);
    }

    public record Instance(StatusEffectInstance effectInstance, float strength) {}
    public static Set<Instance> neurotoxicPoison(float strengthModifier) {
        var toReturn = new HashSet<Instance>();
        toReturn.add(new Instance(new StatusEffectInstance(PARALYZED, (int)(20*2.5f)), -25 + strengthModifier));
        toReturn.add(new Instance(new StatusEffectInstance(SLUGGISH, (int)(20*20f)), 0 + strengthModifier));
        toReturn.add(new Instance(new StatusEffectInstance(WEAKNESS, (int)(20*60f)), 25 + strengthModifier));
        return toReturn;
    }
    public static Set<Instance> hemorrhagicPoison(float strengthModifier) {
        var toReturn = new HashSet<Instance>();
        toReturn.add(new Instance(new StatusEffectInstance(BLEEDING, (int)(20*10f)), -25 + strengthModifier));
        toReturn.add(new Instance(new StatusEffectInstance(HEMOPHILIA, (int)(20*60f)), 0 + strengthModifier));
        return toReturn;
    }
    public static Set<Instance> debilitatingPoison(float strengthModifier) {
        var toReturn = new HashSet<Instance>();
        toReturn.add(new Instance(new StatusEffectInstance(NAUSEA, (int)(20*60f)), 0 + strengthModifier));
        toReturn.add(new Instance(new StatusEffectInstance(HUNGER, (int)(20*60f)), 0 + strengthModifier));
        toReturn.add(new Instance(new StatusEffectInstance(WEAKNESS, (int)(20*60f)), 0 + strengthModifier));
        return toReturn;
    }

    private static RegistryEntry<StatusEffect> register(String id, StatusEffect statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(WorldgateConqueror.MOD_ID, id), statusEffect);
    }

    public static void registerEffects() {
        WorldgateConqueror.LOGGER.info("Registering status effects for " + WorldgateConqueror.MOD_ID);
    }

    // copy of vanilla
    @Environment(EnvType.CLIENT)
    public static class DarknessFogModifier implements BackgroundRenderer.StatusEffectFogModifier {
        @Override
        public RegistryEntry<StatusEffect> getStatusEffect() {
            return DARKNESS;
        }

        private float getTimeOfDay(LivingEntity entity) {
            return ((entity.getWorld().getTimeOfDay()) % 24000);
        }
        private static final float SUNRISE_START = 23000;
        private static final float SUNSET_START = 13000;
        private static final float RISE_TIME = 1000;
        private float getFadeByTimeOfDay(LivingEntity entity) {
            if (entity.getWorld().getDimension().hasSkyLight()) {
                var timeFraction = getTimeOfDay(entity);

                if (timeFraction < SUNSET_START) {
                    return 0.0f;
                } else if (timeFraction < SUNSET_START + RISE_TIME) {
                    return (timeFraction - SUNSET_START) / RISE_TIME;
                } else if (timeFraction < SUNRISE_START) {
                    return 1.0f;
                } else {
                    return 1.0f - ((timeFraction - SUNRISE_START) / RISE_TIME);
                }
            } else {
                return 1.0f;
            }
        }
        private float getFade(LivingEntity entity, StatusEffectInstance effect, float tickDelta) {
            float fade = getFadeByTimeOfDay(entity);
            if (effect != null) {
                fade = Math.max(effect.getFadeFactor(entity, tickDelta), fade);
            }
            return fade;
        }

        @Override
        public boolean shouldApply(LivingEntity entity, float tickDelta) {
            var orCondition = !entity.getWorld().getDimension().hasSkyLight() || getTimeOfDay(entity) > SUNSET_START || entity.hasStatusEffect(getStatusEffect());
            return !entity.hasStatusEffect(StatusEffects.NIGHT_VISION) && orCondition;
        }

        @Override
        public void applyStartEndModifier(BackgroundRenderer.FogData fogData, LivingEntity entity, StatusEffectInstance effect, float viewDistance, float tickDelta) {
            float fadeFactor = getFade(entity, effect, tickDelta);
            float f = MathHelper.lerp(fadeFactor, viewDistance, 20.0F);
            fogData.fogStart = fogData.fogType == BackgroundRenderer.FogType.FOG_SKY ? 0.0F : f * 0.75F;
            fogData.fogEnd = f;
        }

        @Override
        public float applyColorModifier(LivingEntity entity, StatusEffectInstance effect, float f, float tickDelta) {
            float fadeFactor = getFade(entity, effect, tickDelta);
            return 1.0F - fadeFactor;//effect.getFadeFactor(entity, tickDelta);
        }
    }

    public static void registerClient() {
    }
}
