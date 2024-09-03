package worldgate.conqueror.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import worldgate.conqueror.WorldgateConqueror;
import worldgate.conqueror.mechanic.DamageTypeDistribution;
import worldgate.conqueror.mechanic.ModEntityAttributes;

import java.util.Optional;

public record ModularToolComponent(Optional<String> head, Optional<String> handle) {
    public static final Codec<ModularToolComponent> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(
                Codec.STRING
                        .optionalFieldOf("head")
                        .forGetter(ModularToolComponent::head),
                Codec.STRING
                        .optionalFieldOf("handle")
                        .forGetter(ModularToolComponent::handle)
        ).apply(builder, ModularToolComponent::new);
    });


    public ModularToolComponent add(ModularToolComponent that) {
        if (that != null) {
            Optional<String> newHead = this.head.or(() -> that.head);
            Optional<String> newHandle = this.handle.or(() -> that.handle);
            return new ModularToolComponent(newHead, newHandle);
        }
        return this;
    }

    public static ModularToolComponent defaultValue() {
        return new ModularToolComponent(Optional.empty(), Optional.empty());
    }


    public static class Heads {
        public final static String SWORD = "Sword";
        public final static String SPEAR = "Spear";
        public final static String HAMMER = "Hammer";

        public final static String PICK = "Pick";
        public final static String AXE = "Axe";
        public final static String SHOVEL = "Shovel";
        public final static String SCYTHE = "Scythe";

        public static Optional<TagKey<Block>> blockTagOf(String head) {
            return switch (head) {
                case PICK -> Optional.of(BlockTags.PICKAXE_MINEABLE);
                case AXE -> Optional.of(BlockTags.AXE_MINEABLE);
                case SHOVEL -> Optional.of(BlockTags.SHOVEL_MINEABLE);
                case SCYTHE -> Optional.of(BlockTags.HOE_MINEABLE);
                case SWORD -> Optional.of(BlockTags.SWORD_EFFICIENT);
                default -> Optional.empty();
            };
        }
    }
    public static class Handles {
        public final static String GRIP = "Grip";
        public final static String HANDLE = "Handle";
        public final static String POLE = "Pole";
    }

    public Optional<Item> getHeadItemAttached() {
        return head.map(head ->
                switch (head) {
                    case Heads.AXE ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_axe_attached"));
                    case Heads.SHOVEL ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_shovel_attached"));
                    case Heads.SWORD ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_sword_attached"));
                    case Heads.SPEAR ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_spear_attached"));
                    case Heads.HAMMER ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_hammer_attached"));
                    case Heads.SCYTHE ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_scythe_attached"));
                    default ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_pick_attached"));
                });
    }
    public Optional<Item> getHeadItemCentered() {
        return head.map(head ->
                switch (head) {
                    case Heads.AXE ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_axe_centered"));
                    case Heads.SHOVEL ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_shovel_centered"));
                    case Heads.SWORD ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_sword_centered"));
                    case Heads.SPEAR ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_spear_centered"));
                    case Heads.HAMMER ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_hammer_centered"));
                    case Heads.SCYTHE ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_scythe_centered"));
                    default ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "flint_pick_centered"));
                });
    }
    public Optional<Item> getHandleItem() {
        return handle.map(handle ->
                switch (handle) {
                    case Handles.GRIP ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "grip"));
                    case Handles.POLE ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "pole"));
                    default ->  Registries.ITEM.get(Identifier.of(WorldgateConqueror.MOD_ID, "handle"));
                });
    }

    public AttributeModifiersComponent calculateAttributes() {
        AttributeModifiersComponent.Builder attributes = AttributeModifiersComponent.builder();
        head.map(head ->
                switch (head) {
                    case Heads.SPEAR ->
                            attributes.add(
                                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_damage"), 3, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_knockback"), .4, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_SPEED,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_speed"), .40, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                                    AttributeModifierSlot.MAINHAND
                            );
                    case Heads.SWORD ->
                            attributes.add(
                                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_damage"), 3, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_knockback"), .4, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_SPEED,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_speed"), .20, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.sweeping_damage"), .5, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            );
                    case Heads.HAMMER ->
                            attributes.add(
                                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_damage"), 4, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_knockback"), 1, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            );
                    case Heads.AXE ->
                            attributes.add(
                                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_damage"), 3.5, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_knockback"), 1, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            );
                    case Heads.SHOVEL ->
                            attributes.add(
                                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_damage"), 3, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_knockback"), 2, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            );
                    case Heads.PICK ->
                            attributes.add(
                                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_damage"), 3, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_knockback"), .4, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            );
                    case Heads.SCYTHE ->
                            attributes.add(
                                    EntityAttributes.GENERIC_ATTACK_DAMAGE,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_damage"), 2.5, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_knockback"), .4, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_SPEED,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.attack_speed"), .40, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.PLAYER_SWEEPING_DAMAGE_RATIO,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.head.sweeping_damage"), .5, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            );
                    default -> "";
                });
        handle.map(handle ->
                switch (handle) {
                    case Handles.HANDLE ->
                            attributes.add(
                                    EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.handle.block_interaction_range"), 1.5, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.handle.entity_interaction_range"), .75, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_SPEED,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.handle.attack_speed"), -.4, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    ModEntityAttributes.ITEM_SWITCH_SPEED,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.handle.item_switch_speed"), .50, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                                    AttributeModifierSlot.MAINHAND
                            );
                    case Handles.POLE ->
                            attributes.add(
                                    EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.handle.block_interaction_range"), 3, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.handle.entity_interaction_range"), 1.5, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            ).add(
                                    EntityAttributes.GENERIC_ATTACK_SPEED,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.handle.attack_speed"), -.8, EntityAttributeModifier.Operation.ADD_VALUE),
                                    AttributeModifierSlot.MAINHAND
                            );
                    case Handles.GRIP ->
                            attributes.add(
                                    ModEntityAttributes.ITEM_SWITCH_SPEED,
                                    new EntityAttributeModifier(Identifier.of(WorldgateConqueror.MOD_ID,"tool.handle.item_switch_speed"), 1.00, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                                    AttributeModifierSlot.MAINHAND
                            );
                    default -> "";
                });
        return attributes.build();
    }

    public DamageTypeDistribution getDamageTypeDistribution() {
        return head.map(head ->
                switch (head) {
                    case Heads.AXE -> DamageTypeDistribution.AXE;
                    case Heads.SHOVEL, Heads.HAMMER -> DamageTypeDistribution.BLUNT;
                    case Heads.SPEAR, Heads.PICK -> DamageTypeDistribution.PIERCE;
                    case Heads.SCYTHE -> DamageTypeDistribution.SLASH;
                    case Heads.SWORD -> DamageTypeDistribution.MIXED;
                    default -> DamageTypeDistribution.MIXED;
                }).orElse(DamageTypeDistribution.MIXED);
    }


}
