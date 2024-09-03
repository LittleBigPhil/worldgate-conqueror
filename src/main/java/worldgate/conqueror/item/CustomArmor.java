package worldgate.conqueror.item;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Equipment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.mechanic.DamageTypeDistribution;
import worldgate.conqueror.mechanic.ModEntityAttributes;
import worldgate.conqueror.mechanic.ModStatusEffects;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomArmor extends Item implements Equipment {

    private final EquipmentSlot slot;
    private final String name;
    private final DamageTypeDistribution resist;
    private final int bonusHealth;
    private final double moveSpeed;
    private final double waterSpeed;
    private final int bonusFood;
    private final double damageImmunityTime;
    private final double jumpHeight;
    private final double strength;
    private final double mind;
    private final double hardiness;
    private List<RegistryEntry<StatusEffect>> statusEffects;

    public CustomArmor(Settings settings, EquipmentSlot slot, String name, DamageTypeDistribution resist,
                       int bonusHealth, double moveSpeed, double waterSpeed, int bonusFood, double damageImmunityTime, double jumpHeight,
                       double strength, double mind, double hardiness,
                       List<RegistryEntry<StatusEffect>> statusEffects
    ) {
        super(settings);
        this.slot = slot;
        this.name = name;
        this.resist = resist;
        this.bonusHealth = bonusHealth;
        this.moveSpeed = moveSpeed;
        this.waterSpeed = waterSpeed;
        this.bonusFood = bonusFood;
        this.damageImmunityTime = damageImmunityTime;
        this.jumpHeight = jumpHeight;
        this.strength = strength;
        this.mind = mind;
        this.hardiness = hardiness;
        this.statusEffects = statusEffects;
    }
    public static class Builder {
        private final Settings settings;
        private final EquipmentSlot slot;
        private String name = "";
        private Optional<DamageTypeDistribution> resist = Optional.empty();
        private Optional<Integer> bonusHealth = Optional.empty();
        private Optional<Double> moveSpeed = Optional.empty();
        private Optional<Double> waterSpeed = Optional.empty();
        private Optional<Integer> bonusFood = Optional.empty();
        private Optional<Double> damageImmunityTime = Optional.empty();
        private Optional<Double> jumpHeight = Optional.empty();
        private Optional<Double> strength = Optional.empty();
        private Optional<Double> mind = Optional.empty();
        private Optional<Double> hardiness = Optional.empty();
        private final ArrayList<RegistryEntry<StatusEffect>> statusEffects = new ArrayList<>();

        public Builder(Settings settings, EquipmentSlot slot) {
            this.settings = settings;
            this.slot = slot;
        }
        public Builder withName(String name) {
            this.name = name;
            return this;
        }
        public Builder withResist(DamageTypeDistribution resist) {
            this.resist = Optional.of(resist);
            return this;
        }
        public Builder withHealth(int bonusHealth) {
            this.bonusHealth = Optional.of(bonusHealth);
            return this;
        }
        public Builder withMoveSpeed(double moveSpeed) {
            this.moveSpeed = Optional.of(moveSpeed);
            return this;
        }
        public Builder withWaterSpeed(double waterSpeed) {
            this.waterSpeed = Optional.of(waterSpeed);
            return this;
        }
        public Builder withFood(int bonusFood) {
            this.bonusFood = Optional.of(bonusFood);
            return this;
        }
        public Builder withDamageImmunityTime(double damageImmunityTime) {
            this.damageImmunityTime = Optional.of(damageImmunityTime);
            return this;
        }
        public Builder withJumpHeight(double jumpHeight) {
            this.jumpHeight = Optional.of(jumpHeight);
            return this;
        }
        public Builder withStrength(double strength) {
            this.strength = Optional.of(strength);
            return this;
        }
        public Builder withMind(double mind) {
            this.mind = Optional.of(mind);
            return this;
        }
        public Builder withHardiness(double hardiness) {
            this.hardiness = Optional.of(hardiness);
            return this;
        }
        public Builder withStatusEffect(RegistryEntry<StatusEffect> effect) {
            this.statusEffects.add(effect);
            return this;
        }
        public CustomArmor build() {
           return new CustomArmor(settings, slot, name,
                   this.resist.orElse(new DamageTypeDistribution(0, 0, 0)),
                   this.bonusHealth.orElse(0),
                   this.moveSpeed.orElse(0.0),
                   this.waterSpeed.orElse(0.0),
                   this.bonusFood.orElse(0),
                   this.damageImmunityTime.orElse(0.0),
                   this.jumpHeight.orElse(0.0),
                   this.strength.orElse(0.0),
                   this.mind.orElse(0.0),
                   this.hardiness.orElse(0.0),
                   this.statusEffects
                   );
        }
    }

    @Override
    public EquipmentSlot getSlotType() {
        return slot;
    }

    @Override
    public AttributeModifiersComponent getAttributeModifiers() {
        var modifierIdBase = slot.asString() + "." + name;
        var modifiers = super.getAttributeModifiers();
        if (resist.blunt() != 0) {
            modifiers = modifiers.with(
                    ModEntityAttributes.BLUNT_RESISTANCE,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".blunt"), resist.blunt(), EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }
        if (resist.pierce() != 0) {
            modifiers = modifiers.with(
                    ModEntityAttributes.PIERCE_RESISTANCE,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".pierce"), resist.pierce(), EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }
        if (resist.slash() != 0) {
            modifiers = modifiers.with(
                    ModEntityAttributes.SLASH_RESISTANCE,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".slash"), resist.slash(), EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }

        if (bonusHealth != 0) {
            modifiers = modifiers.with(
                    EntityAttributes.GENERIC_MAX_HEALTH,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".health"), bonusHealth, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }
        if (moveSpeed != 0) {
            modifiers = modifiers.with(
                    EntityAttributes.GENERIC_MOVEMENT_SPEED,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".move_speed"), moveSpeed, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }
        if (waterSpeed != 0) {
            modifiers = modifiers.with(
                    ModEntityAttributes.WATER_MOVEMENT_SPEED,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".water_speed"), waterSpeed, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }

        if (bonusFood != 0) {
            modifiers = modifiers.with(
                    ModEntityAttributes.MAX_FOOD_ATTRIBUTE,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".food"), bonusFood, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }
        if (damageImmunityTime != 0) {
            modifiers = modifiers.with(
                    ModEntityAttributes.DAMAGE_IMMUNITY_TIME,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".damage_immunity_time"), damageImmunityTime, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }
        if (jumpHeight != 0) {
            modifiers = modifiers.with(
                    EntityAttributes.GENERIC_JUMP_STRENGTH,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".jump_height"), jumpHeight, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }
        if (jumpHeight != 0) {
            modifiers = modifiers.with(
                    EntityAttributes.GENERIC_SAFE_FALL_DISTANCE,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".safe_fall_height"), jumpHeight, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }

        if (strength != 0) {
            modifiers = modifiers.with(
                    ModEntityAttributes.STRENGTH,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".strength"), strength, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }
        if (mind != 0) {
            modifiers = modifiers.with(
                    ModEntityAttributes.MIND,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".mind"), mind, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }
        if (hardiness != 0) {
            modifiers = modifiers.with(
                    ModEntityAttributes.HARDINESS,
                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID, modifierIdBase + ".hardiness"), hardiness, EntityAttributeModifier.Operation.ADD_VALUE),
                    AttributeModifierSlot.forEquipmentSlot(slot)
            );
        }

        return modifiers;
    }
    public List<StatusEffectInstance> getStatusEffects() {
        var toReturn = new ArrayList<StatusEffectInstance>();
        for (var effect : this.statusEffects) {
            toReturn.add(new StatusEffectInstance(effect, 10, 0, false, false, true));
        }
        return toReturn;
    }
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        for (var effect : this.statusEffects) {
            var translationKey = "effect." + effect.getIdAsString().replace(":",".");
            tooltip.add(Text.literal("+").append(Text.translatable(translationKey)).formatted(Formatting.BLUE));
        }
    }
}
